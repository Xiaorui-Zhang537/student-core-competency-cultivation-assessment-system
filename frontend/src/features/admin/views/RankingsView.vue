<template>
  <div class="p-6">
    <page-header :title="t('admin.sidebar.rankings') || '排名'" :subtitle="t('admin.title')" />

    <filter-bar tint="secondary" align="center" :dense="false" class="mt-4 mb-2 rounded-full h-19">
      <template #left>
        <div class="flex items-center gap-3 flex-wrap">
          <div class="w-80">
            <glass-search-input
              v-model="courseQuery"
              :placeholder="String(t('common.search') || '搜索课程')"
              size="sm"
              tint="info"
            />
          </div>
          <div class="w-80">
            <glass-popover-select
              v-model="courseId"
              :options="courseOptions"
              size="sm"
              tint="secondary"
            />
          </div>
          <div class="w-56">
            <glass-popover-select v-model="sortBy" :options="sortOptions" size="sm" tint="secondary" />
          </div>
        </div>
      </template>
      <template #right>
        <Button size="sm" variant="outline" :disabled="loadingCourses || loadingRank" @click="reloadAll">
          {{ t('common.refresh') || '刷新' }}
        </Button>
      </template>
    </filter-bar>

    <loading-overlay v-if="loadingRank" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reloadRank"
    />

    <card v-else padding="md" tint="secondary" class="mt-4">
      <div class="text-sm font-medium mb-3">
        {{ selectedCourseTitle || (t('admin.sidebar.courses') || '课程') }}
      </div>

      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">#</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.courseStudents.table.student') || (t('common.columns.student') || '学生') }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.progress') || '进度' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.grade') || '成绩' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.radar') || '雷达' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.courseStudents.table.lastActive') || '最后活跃' }}</th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.view') || '查看' }}</th>
          </tr>
        </template>

        <template #body>
          <tr v-for="(s, idx) in items" :key="s.studentId" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center font-mono text-xs">{{ idx + 1 }}</td>
            <td class="px-6 py-3">
              <div class="flex items-center gap-3">
                <user-avatar :avatar="s.avatar" :size="32">
                  <div class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 ring-1 ring-white/20" />
                </user-avatar>
                <div class="min-w-0">
                  <div class="font-medium truncate">{{ s.studentName || `#${s.studentId}` }}</div>
                  <div class="text-xs text-subtle truncate">{{ s.studentNo || '-' }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-3 text-center text-sm">{{ typeof s.progress === 'number' ? `${s.progress}%` : '--' }}</td>
            <td class="px-6 py-3 text-center text-sm">{{ typeof s.averageGrade === 'number' ? Number(s.averageGrade).toFixed(1) : '-' }}</td>
            <td class="px-6 py-3 text-center text-sm">
              <span class="inline-flex items-center gap-2">
                <badge :variant="getRadarBadgeVariant(s.radarClassification)" size="sm" class="px-2 font-semibold uppercase tracking-wide">
                  {{ String(s.radarClassification || '-') }}
                </badge>
                <span class="font-medium">{{ typeof s.radarArea === 'number' ? `${Number(s.radarArea).toFixed(1)}%` : '--' }}</span>
              </span>
            </td>
            <td class="px-6 py-3 text-center text-xs text-subtle">{{ s.lastActiveAt || '-' }}</td>
            <td class="px-6 py-3 text-right">
              <Button size="sm" variant="outline" @click="openStudent(s.studentId)">{{ t('common.view') || '查看' }}</Button>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="7" class="px-6 py-6">
              <empty-state :title="String(t('common.empty') || '暂无数据')" />
            </td>
          </tr>
        </template>
      </glass-table>
    </card>
  </div>
</template>

<script setup lang="ts">
/**
 * 管理员端排名页：
 * - 选择课程
 * - 选择排序维度（成绩/雷达/进度/活跃）
 * - 复用教师端课程学生表现接口（后端已放开 ADMIN 权限）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import Badge from '@/components/ui/Badge.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { adminApi } from '@/api/admin.api'
import { teacherApi } from '@/api/teacher.api'
import type { CourseStudentPerformanceItem } from '@/types/teacher'

const { t } = useI18n()
const router = useRouter()

const loadingCourses = ref(false)
const loadingRank = ref(false)
const error = ref<string | null>(null)

const courseQuery = ref('')
const courses = ref<any[]>([])
const courseId = ref<string>('')
const sortBy = ref<'grade'|'radar'|'progress'|'lastActive'|'activity'>('grade')

const items = ref<CourseStudentPerformanceItem[]>([])

const sortOptions = computed(() => ([
  { label: String(t('admin.courseStudents.filters.sort.grade') || '成绩'), value: 'grade' },
  { label: String(t('admin.courseStudents.filters.sort.radar') || '雷达'), value: 'radar' },
  { label: String(t('admin.courseStudents.filters.sort.progress') || '进度'), value: 'progress' },
  { label: String(t('admin.courseStudents.filters.sort.lastActive') || '最后活跃'), value: 'lastActive' },
  { label: String(t('admin.courseStudents.filters.sort.activity') || '活跃度'), value: 'activity' },
]))

const courseOptions = computed(() => ([
  { label: String(t('common.pleaseSelect') || '请选择…'), value: '' },
  ...courses.value.map((c: any) => ({ label: `${c.title || `#${c.id}`} (#${c.id})`, value: String(c.id) })),
]))

const selectedCourseTitle = computed(() => {
  const c = courses.value.find((x: any) => String(x.id) === String(courseId.value))
  return c?.title || ''
})

const getRadarBadgeVariant = (classification?: string) => {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}

async function reloadCourses() {
  loadingCourses.value = true
  try {
    const res: any = await adminApi.pageCourses({ page: 1, size: 50, query: courseQuery.value.trim() || undefined })
    courses.value = res?.items || []
    // 自动选择第一门课程
    if (!courseId.value && courses.value.length) {
      courseId.value = String(courses.value[0].id)
    }
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '加载失败')
  } finally {
    loadingCourses.value = false
  }
}

async function reloadRank() {
  if (!courseId.value) {
    items.value = []
    return
  }
  loadingRank.value = true
  error.value = null
  try {
    const res: any = await teacherApi.getCourseStudentPerformance(String(courseId.value), { page: 1, size: 30, sortBy: sortBy.value })
    items.value = (res?.items || []) as CourseStudentPerformanceItem[]
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '加载失败')
  } finally {
    loadingRank.value = false
  }
}

async function reloadAll() {
  await reloadCourses()
  await reloadRank()
}

function openStudent(studentId: number) {
  router.push(`/admin/courses/${courseId.value}/students/${studentId}`)
}

watch(courseId, () => reloadRank())
watch(sortBy, () => reloadRank())

onMounted(async () => {
  await reloadCourses()
  await reloadRank()
})
</script>

