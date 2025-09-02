<template>
  <div class="space-y-5">
    <header class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">{{ t('student.assignments.title') }}</h1>
        <p class="text-gray-600 dark:text-gray-400">{{ t('student.assignments.subtitle') }}</p>
      </div>
    </header>

    <!-- 过滤条 -->
    <FilterBar>
      <template #left>
        <GlassSelect v-model="filters.status" :options="statusOptions" class="w-40"/>
        <GlassSelect v-model="filters.courseId" :options="courseOptions" class="w-56 ml-2"/>
      </template>
      <template #right>
        <input v-model="filters.keyword" :placeholder="t('student.assignments.search')" class="input" />
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
              {{ t('student.assignments.due') }}{{ formatTime(a.dueDate || a.dueAt) }}
            </div>
          </div>
          <div class="flex items-center gap-2">
            <Button v-if="normalizedStatus(a.status)==='PENDING'" variant="primary" size="sm" @click="submit(a.id)">{{ t('student.assignments.actions.submit') }}</Button>
            <Button v-else-if="normalizedStatus(a.status)==='SUBMITTED'" variant="menu" size="sm" @click="view(a.id)">{{ t('student.assignments.actions.view') }}</Button>
            <Button v-else variant="menu" size="sm" @click="view(a.id)">{{ t('student.assignments.actions.review') }}</Button>
          </div>
        </div>
        <div v-if="!loading && list.length===0" class="p-8 text-center text-gray-500 dark:text-gray-400">{{ t('student.assignments.empty') }}</div>
        <div v-if="loading" class="p-8 text-center text-gray-500 dark:text-gray-400">{{ t('shared.loading') }}</div>
      </div>
    </div>

    <!-- 分页控制 -->
    <div class="mt-6 flex items-center justify-between">
      <div class="flex items-center space-x-2">
        <span class="text-sm text-gray-700">{{ t('student.assignments.pagination.perPagePrefix') }}</span>
        <div class="w-24">
          <GlassPopoverSelect
            :options="[{label:'10', value:10},{label:'20', value:20},{label:'50', value:50}]"
            :model-value="pageSize"
            @update:modelValue="(v:any)=>{ pageSize = Number(v||10); changePageSize() }"
            size="sm"
          />
        </div>
        <span class="text-sm text-gray-700">{{ t('student.assignments.pagination.perPageSuffix') }}</span>
      </div>
      <div class="flex items-center space-x-2">
        <Button variant="outline" size="sm" :disabled="loading || currentPage===1" @click="prevPage">{{ t('student.assignments.pagination.prev') }}</Button>
        <span class="text-sm">{{ t('student.assignments.pagination.page', { page: currentPage }) }}</span>
        <Button variant="outline" size="sm" :disabled="loading || currentPage>=totalPages" @click="nextPage">{{ t('student.assignments.pagination.next') }}</Button>
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
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const list = ref<any[]>([])
const filters = ref<{ status: string, courseId: string | number | null, keyword: string }>({ status: 'ALL', courseId: null, keyword: '' })
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)

const statusOptions = computed(() => [
  { label: t('student.assignments.filters.all'), value: 'ALL' },
  { label: t('student.assignments.filters.pending'), value: 'PENDING' },
  { label: t('student.assignments.filters.submitted'), value: 'SUBMITTED' },
  { label: t('student.assignments.filters.graded'), value: 'GRADED' }
])

const courseOptions = ref<Array<{ label: string, value: string }>>([{ label: t('student.assignments.filters.courseAll'), value: '' }])

function statusBadgeClass(s: string) {
  if (s === 'PENDING') return 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-300'
  if (s === 'SUBMITTED') return 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300'
  if (s === 'GRADED') return 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-300'
  return 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'
}
function statusText(s: string) {
  if (s === 'PENDING') return t('student.assignments.status.pending')
  if (s === 'SUBMITTED') return t('student.assignments.status.submitted')
  if (s === 'GRADED') return t('student.assignments.status.graded')
  return t('student.assignments.status.unknown')
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
    courseOptions.value = [{ label: t('student.assignments.filters.courseAll'), value: '' }, ...opts]
  } catch {
    courseOptions.value = [{ label: t('student.assignments.filters.courseAll'), value: '' }]
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
    params.page = currentPage.value
    params.size = pageSize.value
    const res: any = await studentApi.getAssignments(params)
    const items = res?.items || res?.data?.items || res || []
    list.value = Array.isArray(items) ? items : (items.items || [])
    total.value = Number(res?.total ?? (Array.isArray(list.value) ? list.value.length : 0))
    const tp = Number(res?.totalPages)
    totalPages.value = Number.isFinite(tp) && tp > 0 ? tp : Math.max(1, Math.ceil((total.value || 0) / pageSize.value))
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
watch([currentPage, pageSize], () => loadList())
onMounted(async () => {
  await loadCourses()
  await loadList()
})

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value -= 1
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1
  }
}

function changePageSize() {
  currentPage.value = 1
}
</script>

<style scoped>
.input { @apply border rounded px-3 py-2 text-sm bg-white dark:bg-gray-900 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-gray-100; }
</style>


