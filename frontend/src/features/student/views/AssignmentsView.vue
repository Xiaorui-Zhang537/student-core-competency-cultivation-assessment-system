<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <page-header :title="t('student.assignments.title')" :subtitle="t('student.assignments.subtitle')" />

      <!-- 过滤条（取消外层嵌套容器，仅保留 FilterBar） -->
      <filter-bar tint="info" align="center" :dense="false" class="mb-6 rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-4">
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ (t('student.assignments.filters.statusLabel') as string) || '状态' }}</span>
              <div class="w-48">
                <glass-popover-select
                  v-model="filters.status"
                  :options="statusOptions"
                  size="sm"
                  tint="primary"
                />
              </div>
            </div>
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ (t('student.assignments.filters.courseLabel') as string) || '课程' }}</span>
              <div class="w-56">
                <glass-popover-select
                  v-model="filters.courseId"
                  :options="courseOptions"
                  size="sm"
                  tint="secondary"
                />
              </div>
            </div>
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ (t('student.assignments.filters.onlyPending') as string) || '仅显示待处理' }}</span>
              <glass-switch v-model="onlyPending" size="sm" />
            </div>
          </div>
        </template>
        <template #right>
          <div class="w-64">
            <glass-search-input
              v-model="searchText"
              :placeholder="(t('student.assignments.searchPlaceholder') as string) || (t('student.assignments.search') as string) || '搜索作业'"
              size="sm"
              tint="info"
            />
          </div>
        </template>
      </filter-bar>

      <!-- 列表（每行一个卡片） -->
      <div class="grid grid-cols-1 gap-4">
        <error-state
          v-if="errorMessage"
          title="加载失败"
          :message="errorMessage"
          :retry-label="String(t('teacher.submissions.retry') || '重试')"
          @retry="loadList"
        />

        <card v-for="a in list" :key="a.id" padding="md" tint="info" class="relative">
          <div class="min-w-0 pr-44">
            <div class="flex items-center gap-2">
              <span class="text-base font-semibold text-base-content truncate">{{ a.title }}</span>
              <badge size="sm" :variant="statusVariant(displayStatus(a))">{{ statusText(displayStatus(a)) }}</badge>
            </div>
            <div class="text-sm text-subtle mt-1 truncate">{{ a.courseTitle || a.courseName || a.course?.title }}</div>
            <div class="text-xs text-subtle mt-1">
              {{ t('student.assignments.due') }}
              <span v-if="String(a?.assignmentType || '').toLowerCase()==='course_bound'">
                {{ t('shared.noDeadline') || '无截止' }}
              </span>
              <span v-else>
                {{ formatTime(a.dueDate || a.dueAt) }}
              </span>
            </div>
          </div>
          <div class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-2">
            <Button v-if="displayStatus(a)==='PENDING' && !isPastDue(a.dueDate || a.dueAt)" variant="success" size="sm" @click="submit(a.id)">{{ t('student.assignments.actions.submit') }}</Button>
            <Button v-else-if="displayStatus(a)==='SUBMITTED' || isPastDue(a.dueDate || a.dueAt)" variant="info" size="sm" class="gap-3" @click="view(a.id)">
              <template #icon>
                <eye-icon class="h-4 w-4" />
              </template>
              {{ t('student.assignments.actions.view') }}
            </Button>
            <Button v-else variant="secondary" icon="confirm" size="sm" class="gap-3" @click="view(a.id)">{{ t('student.assignments.actions.review') }}</Button>
          </div>
        </card>

        <empty-state
          v-if="!loading && !errorMessage && list.length===0"
          :title="String(t('student.assignments.empty'))"
          tint="info"
        />
        <loading-overlay v-if="loading" :text="String(t('shared.loading'))" />
      </div>

      <!-- 分页：左侧总数+每页选择，右侧统一分页组件 -->
      <pagination-bar
        :page="currentPage"
        :page-size="pageSize"
        :total-pages="totalPages"
        :total-items="total"
        :disabled="loading"
        :page-size-options="[10, 20, 50]"
        @update:page="handlePageChange"
        @update:pageSize="handlePageSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import Button from '@/components/ui/Button.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import Card from '@/components/ui/Card.vue'
import { studentApi } from '@/api/student.api'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import Badge from '@/components/ui/Badge.vue'
import { MagnifyingGlassIcon, EyeIcon } from '@heroicons/vue/24/outline'
import { useSubmissionStore } from '@/stores/submission'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import LoadingOverlay from '@/components/ui/LoadingOverlay.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const submissionStore = useSubmissionStore()

const loading = ref(false)
const list = ref<any[]>([])
const errorMessage = ref('')
const filters = ref<{ status: string, courseId: string | number | null, keyword: string }>({ status: 'ALL', courseId: null, keyword: '' })
const searchText = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const onlyPending = ref(false)

const statusOptions = computed(() => [
  { label: t('student.assignments.filters.all'), value: 'ALL' },
  { label: t('student.assignments.filters.pending'), value: 'PENDING' },
  { label: t('student.assignments.filters.submitted'), value: 'SUBMITTED' },
  { label: t('student.assignments.filters.graded'), value: 'GRADED' }
])

const courseOptions = ref<Array<{ label: string, value: string }>>([{ label: t('student.assignments.filters.courseAll'), value: '' }])

function statusVariant(s: string) {
  if (s === 'PENDING') return 'warning'
  if (s === 'SUBMITTED') return 'info'
  if (s === 'LATE') return 'danger'
  if (s === 'GRADED') return 'success'
  return 'secondary'
}
function statusText(s: string) {
  if (s === 'PENDING') return t('student.assignments.status.pending')
  if (s === 'SUBMITTED') return t('student.assignments.status.submitted')
  if (s === 'LATE') return t('student.assignments.status.late')
  if (s === 'GRADED') return t('student.assignments.status.graded')
  return t('student.assignments.status.unknown')
}
function displayStatus(a: any) {
  // 优先使用后端提交记录（Pinia）覆盖列表状态
  try {
    const sub = submissionStore.submissions.get(String(a?.id || '')) as any
    const s = String(sub?.status || '').toUpperCase()
    const hasContent = !!String(sub?.content || '').trim()
    const hasFiles = Array.isArray(sub?.fileIds) && sub.fileIds.length > 0
    const actuallySubmitted = (s === 'SUBMITTED' || s === 'GRADED' || s === 'LATE') && (hasContent || hasFiles || !!sub?.submittedAt)
    if (actuallySubmitted) return s === 'GRADED' ? 'GRADED' : 'SUBMITTED'
  } catch {}
  // 其次尝试读取后端返回在作业列表项上的提交状态字段（若存在）
  try {
    const s2 = String(a?.submissionStatus || a?.submission_status || '').toUpperCase()
    if (s2 === 'SUBMITTED' || s2 === 'GRADED') return s2
  } catch {}
  // 未提交情况下，若已过截止时间显示 LATE
  const due = a?.dueDate || a?.dueAt
  if (isPastDue(due)) return 'LATE'
  return normalizedStatus(a?.status)
}
function formatTime(ts: string) {
  const d = new Date(ts)
  return isNaN(d.getTime()) ? '' : d.toLocaleString()
}

function isPastDue(ts?: string) {
  if (!ts) return false
  const d = new Date(ts)
  if (isNaN(d.getTime())) return false
  return Date.now() > d.getTime()
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
  // 后端的 a.status 表示作业生命周期（draft/published/closed），
  // 仅当筛选“待完成”时可映射为已发布作业
  if (m === 'pending') return 'published'
  // “已提交/已评分”不能映射到作业生命周期，改为前端本地过滤
  return undefined
}

function normalizedStatus(s: string) {
  if (!s) return 'UNKNOWN'
  const lower = s.toLowerCase()
  if (lower === 'published') return 'PENDING'
  if (lower === 'submitted') return 'SUBMITTED'
  if (lower === 'graded') return 'GRADED'
  if (lower === 'crafted' || lower === 'draft') return 'DRAFT'
  return s.toUpperCase()
}

async function loadList() {
  loading.value = true
  errorMessage.value = ''
  try {
    const params: any = {}
    if (filters.value.courseId) params.courseId = filters.value.courseId
    const st = mapToBackendStatus(filters.value.status)
    if (st) params.status = st
    if (filters.value.keyword) params.q = filters.value.keyword
    params.page = currentPage.value
    params.size = pageSize.value
    // 默认包含历史课程作业；仅显示待处理由后端过滤
    params.includeHistory = true
    params.onlyPending = !!onlyPending.value
    // 课程作业-长期（course_bound）应仅在课节详情里展示，不出现在“我的作业”
    params.excludeCourseBound = true
    const res: any = await studentApi.getAssignments(params)
    const items = res?.items || res?.data?.items || res || []
    const rawList = Array.isArray(items) ? items : (items.items || [])
    // 先拉取本页所有作业的提交记录，便于基于“我是否提交过”进行可见性判断
    try {
      const idsAll: string[] = (rawList || []).map((a: any) => String(a.id)).filter(Boolean)
      await Promise.allSettled(idsAll.map((id: string) => submissionStore.fetchSubmissionForAssignment(id)))
    } catch {}
    // 仅展示可见作业：
    // - 定时未到：隐藏；
    // - 草稿：包含（后端已通过 includeHistory 返回历史作业，这里不再隐藏草稿）
    let visible = (rawList || []).filter((a: any) => {
      const st = String(a?.status || '').toLowerCase()
      if (!st) return true
      if (st === 'crafted' || st === 'draft') return true
      if (st === 'scheduled') {
        const ts = a?.publishAt || a?.publish_at
        if (!ts) return false
        try { return Date.now() >= new Date(ts).getTime() } catch { return false }
      }
      return true
    })
    // 取消前端提交状态过滤，改由后端 onlyPending 控制；已保留最小可见性过滤（草稿若“我”提交过则显示；未到发布时间隐藏）
    list.value = visible
    // 再次保证徽章数据完整（已提前拉取，这里冗余兜底）
    try {
      const ids: string[] = (visible || []).map((a: any) => String(a.id))
      await Promise.allSettled(ids.map((id: string) => submissionStore.fetchSubmissionForAssignment(id)))
    } catch {}
    total.value = Number(res?.total ?? visible.length)
    const tp = Number(res?.totalPages)
    totalPages.value = Number.isFinite(tp) && tp > 0 ? tp : Math.max(1, Math.ceil((total.value || 0) / pageSize.value))
  } catch (e: any) {
    errorMessage.value = e?.message || (t('teacher.submissions.errors.fetch') as string)
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

// 搜索框防抖：将输入绑定到 searchText，经300ms后同步到 filters.keyword
let searchDebounceTimer: any
watch(searchText, (val) => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    filters.value.keyword = val || ''
  }, 300)
})

watch(filters, loadList, { deep: true })
watch([currentPage, pageSize], () => loadList())
watch(onlyPending, () => { currentPage.value = 1; loadList() })

onMounted(async () => {
  const initialCourseId = String(route.query.courseId || '')
  if (initialCourseId) {
    filters.value.courseId = initialCourseId
  }
  await loadCourses()
  await loadList()
})

function handlePageChange(page: number) {
  if (page === currentPage.value) return
  currentPage.value = page
}

function handlePageSizeChange(size: number) {
  const n = Math.max(1, Math.floor(Number(size || 10)))
  if (n === pageSize.value) return
  pageSize.value = n
  currentPage.value = 1
}
</script>

<style scoped>
:deep(.pill.filter-container[class*="rounded"]) {
  border-radius: 9999px !important;
}
/* 使用全局 input 主题化样式（见 styles/main.postcss），移除本地硬编码颜色 */
</style>


