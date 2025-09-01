<template>
  <div class="space-y-5">
    <header class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('student.assignments.title') || '我的作业' }}</h1>
        <p class="text-gray-600 dark:text-gray-400">{{ t('student.assignments.subtitle') || '查看与提交作业' }}</p>
      </div>
    </header>

    <!-- 过滤条 -->
    <FilterBar>
      <template #left>
        <GlassSelect v-model="filters.status" :options="statusOptions" class="w-40"/>
        <GlassSelect v-model="filters.courseId" :options="courseOptions" class="w-56 ml-2"/>
      </template>
      <template #right>
        <input v-model="filters.keyword" :placeholder="t('student.assignments.search') || '搜索作业'" class="input" />
      </template>
    </FilterBar>

    <!-- 列表 -->
    <div class="glass-thick glass-interactive rounded-2xl border border-gray-200/40 dark:border-gray-700/40 overflow-hidden" v-glass="{ strength: 'thick', interactive: true }">
      <div class="divide-y divide-gray-200/40 dark:divide-gray-700/40">
        <div v-for="a in list" :key="a.id" class="p-4 flex items-center justify-between">
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <span class="text-base font-semibold text-gray-900 dark:text-white truncate">{{ a.title }}</span>
              <span class="text-xs px-2 py-0.5 rounded-full" :class="statusBadgeClass(normalizedStatus(a.status))">{{ statusText(normalizedStatus(a.status)) }}</span>
            </div>
            <div class="text-sm text-gray-600 dark:text-gray-300 mt-1 truncate">{{ a.courseTitle || a.courseName || a.course?.title }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">
              {{ t('student.assignments.due') || '截止：' }}{{ formatTime(a.dueDate || a.dueAt) }}
            </div>
          </div>
          <div class="flex items-center gap-2">
            <Button v-if="normalizedStatus(a.status)==='PENDING'" variant="primary" size="sm" @click="submit(a.id)">{{ t('student.assignments.actions.submit') || '提交' }}</Button>
            <Button v-else-if="normalizedStatus(a.status)==='SUBMITTED'" variant="menu" size="sm" @click="view(a.id)">{{ t('student.assignments.actions.view') || '查看' }}</Button>
            <Button v-else variant="menu" size="sm" @click="view(a.id)">{{ t('student.assignments.actions.review') || '查看评分' }}</Button>
          </div>
        </div>
        <div v-if="!loading && list.length===0" class="p-8 text-center text-gray-500 dark:text-gray-400">{{ t('student.assignments.empty') || '暂无作业' }}</div>
        <div v-if="loading" class="p-8 text-center text-gray-500 dark:text-gray-400">{{ t('shared.loading') || '加载中...' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import Button from '@/components/ui/Button.vue'
import GlassSelect from '@/components/ui/filters/GlassSelect.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import { studentApi } from '@/api/student.api'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const list = ref<any[]>([])
const filters = ref<{ status: string, courseId: string | number | null, keyword: string }>({ status: 'ALL', courseId: null, keyword: '' })

const statusOptions = computed(() => [
  { label: t('student.assignments.filters.all') || '全部', value: 'ALL' },
  { label: t('student.assignments.filters.pending') || '待提交', value: 'PENDING' },
  { label: t('student.assignments.filters.submitted') || '已提交', value: 'SUBMITTED' },
  { label: t('student.assignments.filters.graded') || '已评分', value: 'GRADED' }
])

const courseOptions = ref<Array<{ label: string, value: string }>>([{ label: t('student.assignments.filters.courseAll') || '全部课程', value: '' }])

function statusBadgeClass(s: string) {
  if (s === 'PENDING') return 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300'
  if (s === 'SUBMITTED') return 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300'
  if (s === 'GRADED') return 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300'
  return 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'
}
function statusText(s: string) {
  if (s === 'PENDING') return t('student.assignments.status.pending') || '待提交'
  if (s === 'SUBMITTED') return t('student.assignments.status.submitted') || '已提交'
  if (s === 'GRADED') return t('student.assignments.status.graded') || '已评分'
  return t('student.assignments.status.unknown') || '未知'
}
function formatTime(ts: string) {
  const d = new Date(ts)
  return isNaN(d.getTime()) ? '' : d.toLocaleString()
}

async function loadCourses() {
  try {
    const res: any = await studentApi.getMyCourses({ page: 1, size: 200 })
    const items = res?.data?.items?.items || res?.data?.items || res?.items || []
    const opts = items.map((c: any) => ({ label: c.title || c.name || `#${c.id}`, value: String(c.id) }))
    courseOptions.value = [{ label: t('student.assignments.filters.courseAll') || '全部课程', value: '' }, ...opts]
  } catch {
    courseOptions.value = [{ label: t('student.assignments.filters.courseAll') || '全部课程', value: '' }]
  }
}

function mapToBackendStatus(v: string) {
  if (!v || v === 'ALL') return undefined
  const m = v.toLowerCase()
  if (m === 'pending' || m === 'submitted' || m === 'graded') return m
  return undefined
}

function normalizedStatus(s: string) {
  if (!s) return 'UNKNOWN'
  const lower = s.toLowerCase()
  if (lower === 'published') return 'PENDING'
  if (lower === 'submitted') return 'SUBMITTED'
  if (lower === 'graded') return 'GRADED'
  return s.toUpperCase()
}

async function loadList() {
  loading.value = true
  try {
    const params: any = {}
    if (filters.value.courseId) params.courseId = filters.value.courseId
    const st = mapToBackendStatus(filters.value.status)
    if (st) params.status = st
    if (filters.value.keyword) params.q = filters.value.keyword
    const res: any = await studentApi.getAssignments(params)
    const items = res?.data?.items || res?.items || res || []
    list.value = Array.isArray(items) ? items : (items.items || [])
  } finally {
    loading.value = false
  }
}

function submit(id: number | string) {
  router.push(`/student/assignments/${id}/submit`)
}
function view(id: number | string) {
  router.push(`/student/assignments/${id}/submit`)
}

watch(filters, loadList, { deep: true })
onMounted(async () => {
  await loadCourses()
  await loadList()
})
</script>

<style scoped>
.input { @apply border rounded px-3 py-2 text-sm bg-white dark:bg-gray-900 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-gray-100; }
</style>


