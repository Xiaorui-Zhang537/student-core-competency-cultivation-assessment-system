<template>
  <card padding="md" tint="info">
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="text-sm font-medium">{{ t('admin.courses.tabs.students') || '学生' }}</div>
      <div class="flex items-center gap-2">
        <Button size="sm" variant="success" :disabled="loading" @click="onExportCsv">
          <template #icon>
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v12m0 0 4-4m-4 4-4-4M4 17.5v1A2.5 2.5 0 0 0 6.5 21h11a2.5 2.5 0 0 0 2.5-2.5v-1" />
            </svg>
          </template>
          {{ t('admin.tools.exportCsv') || '导出CSV' }}
        </Button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
      <start-card :label="t('admin.courseStudents.cards.total') as string" :value="total" tone="blue" :icon="UsersIcon" />
      <start-card :label="t('admin.courseStudents.cards.avgProgress') as string" :value="avgProgressText" tone="emerald" :icon="ChartBarIcon" />
      <start-card :label="t('admin.courseStudents.cards.avgGrade') as string" :value="avgGradeText" tone="amber" :icon="AcademicCapIcon" />
      <start-card :label="t('admin.courseStudents.cards.active') as string" :value="activeStudentsText" tone="violet" :icon="BoltIcon" />
    </div>

    <filter-bar tint="secondary" align="center" :dense="false" class="mb-3 rounded-full h-19">
      <template #left>
        <div class="flex items-center gap-3 flex-wrap">
          <div class="w-auto flex items-center gap-2">
            <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.grade') || '成绩' }}</span>
            <div class="w-44">
              <glass-popover-select v-model="gradeFilter" :options="gradeOptions" size="sm" tint="secondary" />
            </div>
          </div>

          <div class="w-auto flex items-center gap-2">
            <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.activity') || '活跃度' }}</span>
            <div class="w-44">
              <glass-popover-select v-model="activityFilter" :options="activityOptions" size="sm" tint="secondary" />
            </div>
          </div>

          <div class="w-auto flex items-center gap-2">
            <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courseStudents.filters.labels.sort') || '排序' }}</span>
            <div class="w-52">
              <glass-popover-select v-model="sortBy" :options="sortOptions" size="sm" tint="secondary" />
            </div>
          </div>
        </div>
      </template>
      <template #right>
        <div class="w-72">
          <glass-search-input
            v-model="studentSearch"
            :placeholder="String(t('admin.courseStudents.filters.searchPlaceholder') || t('common.search') || '搜索')"
            size="sm"
            tint="info"
          />
        </div>
      </template>
    </filter-bar>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state v-else-if="error" :title="String(t('common.error') || '加载失败')" :message="error" :actionText="String(t('common.retry') || '重试')" @action="reload" />

    <div v-else>
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.courseStudents.table.student') || (t('admin.sidebar.students') || '学生') }}</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-36">{{ t('admin.courseStudents.table.progress') || '进度' }}</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-56">{{ t('admin.courseStudents.table.radar') || '雷达' }}</th>
            <th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ averageGradeLabel }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-28">{{ t('admin.courseStudents.table.activity') || '活跃度' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.lastActive') || '最后活跃' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courses.actions') || '操作' }}</th>
          </tr>
        </template>

        <template #body>
          <tr v-for="s in students" :key="String(s.studentId)" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center font-mono text-xs">{{ s.studentId }}</td>
            <td class="px-6 py-3">
              <div class="flex items-center gap-3">
                <user-avatar :avatar="(s as any).avatar" :size="36">
                  <div class="w-9 h-9 rounded-full bg-gray-200 dark:bg-gray-600 ring-1 ring-white/20" />
                </user-avatar>
                <div class="min-w-0">
                  <div class="font-medium truncate">{{ compactStudentName((s as any).studentName || `#${s.studentId}`) }}</div>
                  <div class="text-xs text-subtle truncate">{{ studentSubline(s) }}</div>
                </div>
              </div>
            </td>
            <td class="px-4 py-3 text-center">
              <div class="flex items-center justify-center max-w-[120px] mx-auto">
                <div class="flex-1">
                  <div class="flex justify-between items-center mb-1">
                    <span class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ typeof (s as any).progress === 'number' ? `${(s as any).progress}%` : '--' }}
                    </span>
                    <span class="text-xs text-gray-500">
                      {{ ((s as any).completedLessons ?? '--') }}/{{ ((s as any).totalLessons ?? '--') }}
                    </span>
                  </div>
                  <Progress
                    :value="Number((s as any).progress || 0)"
                    size="sm"
                    :color="Number((s as any).progress || 0) >= 100 ? 'success' : 'primary'"
                  />
                </div>
              </div>
            </td>
            <td class="px-4 py-3 min-w-[220px] text-center">
              <div class="flex flex-col gap-2 items-center">
                <div class="flex items-center justify-center gap-2">
                  <badge :variant="getRadarBadgeVariant((s as any).radarClassification)" size="sm" class="px-2 font-semibold uppercase tracking-wide">
                    {{ formatRadarBadgeLabel((s as any).radarClassification) }}
                  </badge>
                  <span class="text-sm font-semibold text-gray-900 dark:text-white">
                    {{ typeof (s as any).radarArea === 'number' ? `${Number((s as any).radarArea).toFixed(1)}%` : '--' }}
                  </span>
                </div>
                <div v-if="radarDimensionEntries((s as any)).length" class="grid grid-cols-2 gap-x-3 gap-y-1 text-xs text-gray-600 dark:text-gray-400 text-left max-w-[210px]">
                  <div
                    v-for="([code, value], didx) in radarDimensionEntries((s as any))"
                    :key="`${String(s.studentId)}-${String(code)}-${didx}`"
                    class="inline-flex items-center justify-between gap-2"
                  >
                    <span class="truncate">{{ formatDimensionLabel(String(code)) }}</span>
                    <span class="tabular-nums">{{ formatRadarValue(value) }}</span>
                  </div>
                </div>
                <div v-else class="text-xs text-gray-400 dark:text-gray-500">
                  {{ t('teacher.students.table.noRadarData') || '暂无能力数据' }}
                </div>
              </div>
            </td>
            <td class="px-6 py-3 text-sm text-center">{{ typeof (s as any).averageGrade === 'number' ? Number((s as any).averageGrade).toFixed(1) : '-' }}</td>
            <td class="px-6 py-3 text-sm text-center whitespace-nowrap">{{ localizeActivityLevel((s as any).activityLevel) }}</td>
            <td class="px-6 py-3 text-xs text-subtle text-center">{{ formatDateTime((s as any).lastActiveAt) }}</td>
            <td class="px-6 py-3 text-right">
              <div class="flex items-center justify-end gap-2">
                <Button size="sm" variant="info" class="whitespace-nowrap" @click="openStudent((s as any).studentId)">
                  <template #icon>
                    <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12s3.75-6.75 9.75-6.75S21.75 12 21.75 12 18 18.75 12 18.75 2.25 12 2.25 12Z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </template>
                  {{ t('admin.courses.viewStudent') || '查看画像' }}
                </Button>
              </div>
            </td>
          </tr>

          <tr v-if="students.length === 0">
            <td colspan="8" class="px-6 py-6">
              <empty-state :title="String(t('common.empty') || '暂无数据')" />
            </td>
          </tr>
        </template>
      </glass-table>
    </div>

    <pagination-bar
      class="mt-2"
      :page="page"
      :page-size="pageSize"
      :total-items="total"
      :total-pages="totalPages"
      :disabled="loading"
      @update:page="(v: number) => { page = v; reload() }"
      @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
    />

  </card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import StartCard from '@/components/ui/StartCard.vue'
import Progress from '@/components/ui/Progress.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { AcademicCapIcon, BoltIcon, ChartBarIcon, UsersIcon } from '@heroicons/vue/24/outline'
import { teacherApi } from '@/api/teacher.api'
import { downloadCsv } from '@/utils/download'
import type { CourseStudentPerformanceItem } from '@/types/teacher'

const { t, te } = useI18n()
const router = useRouter()

const props = withDefaults(defineProps<{ courseId: string; active?: boolean }>(), { active: true })
const emit = defineEmits<{ (e: 'stats', v: { total: number; averageProgress: number | null; averageGrade: number | null; activeStudents: number | null }): void }>()

const loading = ref(false)
const error = ref<string | null>(null)
const students = ref<CourseStudentPerformanceItem[]>([])

const studentSearch = ref('')
const gradeFilter = ref('')
const activityFilter = ref('')
const sortBy = ref('name')
let searchTimer: ReturnType<typeof setTimeout> | null = null

const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)
const avgProgress = ref<number | null>(null)
const avgGrade = ref<number | null>(null)
const activeStudents = ref<number | null>(null)

const avgProgressText = computed(() => (avgProgress.value == null ? '--' : `${Number(avgProgress.value).toFixed(1)}%`))
const avgGradeText = computed(() => (avgGrade.value == null ? '--' : Number(avgGrade.value).toFixed(1)))
const activeStudentsText = computed(() => (activeStudents.value == null ? '--' : Number(activeStudents.value)))
const averageGradeLabel = computed(() => {
  if (te('admin.courseStudents.table.averageGrade')) return String(t('admin.courseStudents.table.averageGrade'))
  if (te('admin.courseStudents.cards.avgGrade')) return String(t('admin.courseStudents.cards.avgGrade'))
  return '平均成绩'
})

const gradeOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.grade.all') || t('common.all') || '全部'), value: '' },
  { label: String(t('admin.courseStudents.filters.grade.excellent') || '优秀'), value: 'excellent' },
  { label: String(t('admin.courseStudents.filters.grade.good') || '良好'), value: 'good' },
  { label: String(t('admin.courseStudents.filters.grade.average') || '一般'), value: 'average' },
  { label: String(t('admin.courseStudents.filters.grade.below') || '待提升'), value: 'below' },
]))
const activityOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.activity.all') || t('common.all') || '全部'), value: '' },
  { label: String(t('admin.courseStudents.filters.activity.high') || '高'), value: 'high' },
  { label: String(t('admin.courseStudents.filters.activity.medium') || '中'), value: 'medium' },
  { label: String(t('admin.courseStudents.filters.activity.low') || '低'), value: 'low' },
  { label: String(t('admin.courseStudents.filters.activity.inactive') || '不活跃'), value: 'inactive' },
]))
const sortOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.sort.name') || '姓名'), value: 'name' },
  { label: String(t('admin.courseStudents.filters.sort.radar') || '雷达'), value: 'radar' },
  { label: String(t('admin.courseStudents.filters.sort.progress') || '进度'), value: 'progress' },
  { label: String(t('admin.courseStudents.filters.sort.grade') || '成绩'), value: 'grade' },
  { label: String(t('admin.courseStudents.filters.sort.lastActive') || '最后活跃'), value: 'lastActive' },
  { label: String(t('admin.courseStudents.filters.sort.activity') || '活跃度'), value: 'activity' },
]))

const getRadarBadgeVariant = (classification?: string) => {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}

const formatRadarBadgeLabel = (classification?: string) => {
  const key = (classification || 'D').toUpperCase() as 'A' | 'B' | 'C' | 'D'
  const desc = te(`teacher.students.radar.classification.${key}`)
    ? String(t(`teacher.students.radar.classification.${key}`))
    : key
  return `${key} · ${desc}`
}

const formatDimensionLabel = (code: string) => {
  if (te(`shared.radarLegend.dimensions.${code}.title`)) {
    return String(t(`shared.radarLegend.dimensions.${code}.title`))
  }
  return code
}

const formatRadarValue = (value?: number) => {
  if (!Number.isFinite(Number(value))) return '--'
  return Number(value).toFixed(1)
}

function radarDimensionEntries(s: any): Array<[string, number]> {
  const raw = s?.dimensionScores
  if (!raw) return []
  let obj: any = raw
  if (typeof raw === 'string') {
    try {
      obj = JSON.parse(raw)
    } catch {
      return []
    }
  }
  if (!obj || typeof obj !== 'object') return []
  return Object.entries(obj).filter(([k]) => !!k) as Array<[string, number]>
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res: any = await teacherApi.getCourseStudentPerformance(props.courseId, {
      page: page.value,
      size: pageSize.value,
      search: studentSearch.value || undefined,
      sortBy: sortBy.value || undefined,
      activity: activityFilter.value || undefined,
      grade: gradeFilter.value || undefined,
    })
    students.value = ((res?.items || []) as CourseStudentPerformanceItem[]) || []
    total.value = Number(res?.total || 0)
    totalPages.value = Math.max(1, Math.ceil(total.value / Math.max(1, pageSize.value)))
    avgProgress.value = (typeof res?.averageProgress === 'number') ? res.averageProgress : null
    avgGrade.value = (typeof res?.averageGrade === 'number') ? res.averageGrade : null
    activeStudents.value = (typeof res?.activeStudents === 'number') ? res.activeStudents : null

    emit('stats', { total: total.value, averageProgress: avgProgress.value, averageGrade: avgGrade.value, activeStudents: activeStudents.value })
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

function openStudent(studentId: string | number) {
  router.push(`/admin/courses/${props.courseId}/students/${studentId}`)
}

function compactStudentName(v: any): string {
  const name = String(v || '').trim()
  if (!name) return '-'
  if (name.length <= 3) return name
  return `${name.slice(0, 3)}...`
}

function studentSubline(s: any): string {
  const nickname = String(s?.nickname || '').trim()
  if (nickname) return nickname
  const studentNo = String(s?.studentNo || '').trim()
  return studentNo || '-'
}

function localizeActivityLevel(v: any): string {
  const raw = String(v || '').trim()
  const key = raw.toLowerCase()
  if (key === 'high') return String(t('admin.courseStudents.filters.activity.high') || '高')
  if (key === 'medium') return String(t('admin.courseStudents.filters.activity.medium') || '中')
  if (key === 'low') return String(t('admin.courseStudents.filters.activity.low') || '低')
  if (key === 'inactive') return String(t('admin.courseStudents.filters.activity.inactive') || '不活跃')
  return raw || '-'
}

function formatDateTime(v?: string) {
  const raw = String(v || '').trim()
  if (!raw) return '-'
  return raw.replace('T', ' ').slice(0, 16)
}

async function onExportCsv() {
  const blob = await teacherApi.exportCourseStudents(props.courseId, {
    search: studentSearch.value || undefined,
    sortBy: sortBy.value || undefined,
    activity: activityFilter.value || undefined,
    grade: gradeFilter.value || undefined,
  })
  downloadCsv(blob as any, `course_${props.courseId}_students.csv`)
}

watch([studentSearch, gradeFilter, activityFilter, sortBy], () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    reload()
  }, 300)
})

watch(
  () => props.active,
  (v) => {
    if (v && students.value.length === 0 && !loading.value) reload()
  },
  { immediate: true }
)

onMounted(() => {
  if (props.active !== false) reload()
})
</script>

