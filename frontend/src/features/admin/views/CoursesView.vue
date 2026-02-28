<template>
  <div class="p-6">
    <div class="mb-4">
      <PageHeader :title="t('admin.sidebar.courses')" :subtitle="t('admin.title')" />
    </div>

    <div class="mt-4 space-y-6">
      <loading-overlay v-if="statsLoading" :text="String(t('common.loading') || '加载中…')" />
      <admin-kpi-row v-else :items="statsKpis" />
      <!-- filters (under KPI cards) -->
      <filter-bar tint="secondary" align="center" :dense="false" class="rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courses.statusLabel') || '状态' }}</span>
              <div class="w-44">
                <glass-popover-select v-model="status" :options="statusOptions" size="sm" tint="secondary" />
              </div>
            </div>

            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ t('shared.course.fields.difficulty') || '难度' }}</span>
              <div class="w-44">
                <glass-popover-select v-model="difficulty" :options="difficultyOptions" size="sm" tint="secondary" />
              </div>
            </div>
          </div>
        </template>
        <template #right>
          <div class="w-72">
            <glass-search-input
              v-model="query"
              :placeholder="String(t('common.search') || '搜索课程标题/教师')"
              size="sm"
              tint="info"
            />
          </div>
        </template>
      </filter-bar>

      <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
      <error-state
        v-else-if="error"
        :title="String(t('common.error') || '加载失败')"
        :message="error"
        :actionText="String(t('common.retry') || '重试')"
        @action="reload"
      />

      <card v-else padding="md" tint="secondary">
        <glass-table>
          <template #head>
            <tr>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
                {{ t('common.columns.id') || 'ID' }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">
                {{ t('admin.sidebar.courses') }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
                {{ t('common.columns.teacher') || 'Teacher' }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
                {{ t('common.columns.status') || 'Status' }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap hidden xl:table-cell">
                {{ t('shared.course.fields.category') || '类别' }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap hidden xl:table-cell">
                {{ t('shared.course.fields.difficulty') || '难度' }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap hidden xl:table-cell">
                {{ t('common.columns.createdAt') || '创建时间' }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
                {{ t('common.columns.student') || 'Students' }}
              </th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
                {{ t('common.columns.actions') || '操作' }}
              </th>
            </tr>
          </template>

          <template #body>
            <tr v-for="c in items" :key="c.id" class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs align-middle">{{ c.id }}</td>
              <td class="px-6 py-3 align-middle">
                <div class="font-medium">{{ c.title }}</div>
                <div class="text-xs text-subtle line-clamp-1">{{ c.description }}</div>
              </td>
              <td class="px-6 py-3 text-sm text-center align-middle">
                {{ (c as any).teacherName || (c as any).teacher?.nickname || (c as any).teacher?.username || '-' }}
              </td>
              <td class="px-6 py-3 text-sm text-center align-middle">
                <badge size="sm" :variant="statusVariant(c.status)">{{ statusLabel(c.status) }}</badge>
              </td>
              <td class="px-6 py-3 text-sm text-center hidden xl:table-cell align-middle">
                <badge v-if="(c as any).category" size="sm" :variant="getCategoryVariant(String((c as any).category))">
                  {{ localizeCategory((c as any).category, t) }}
                </badge>
                <span v-else class="text-subtle">-</span>
              </td>
              <td class="px-6 py-3 text-sm text-center hidden xl:table-cell align-middle">
                <badge v-if="(c as any).difficulty" size="sm" :variant="getDifficultyVariant(String((c as any).difficulty))">
                  {{ localizeDifficulty((c as any).difficulty, t) }}
                </badge>
                <span v-else class="text-subtle">-</span>
              </td>
              <td class="px-6 py-3 text-xs text-subtle text-center hidden xl:table-cell align-middle">
                {{ formatDateTime((c as any).createdAt) }}
              </td>
              <td class="px-6 py-3 text-sm text-center align-middle">
                {{ (c as any).enrollmentCount ?? (c as any).studentCount ?? '-' }}
              </td>
              <td class="px-6 py-3 text-center align-middle">
                <Button size="sm" variant="primary" @click="router.push(`/admin/courses/${c.id}`)">
                  <EyeIcon class="w-4 h-4 mr-2" />
                  {{ t('common.view') || '查看' }}
                </Button>
              </td>
            </tr>

            <tr v-if="items.length === 0">
              <td colspan="9" class="px-6 py-6">
                <empty-state :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </template>
        </glass-table>

        <pagination-bar
          :page="page"
          :page-size="pageSize"
          :total-items="total"
          :total-pages="totalPages"
          :disabled="loading"
          @update:page="(v: number) => { page = v; reload() }"
          @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
        />
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import { adminApi } from '@/api/admin.api'
import type { Course } from '@/types/course'
import AdminKpiRow from '@/features/admin/components/AdminKpiRow.vue'
import { useAdminCounts } from '@/features/admin/composables/useAdminCounts'
import { AcademicCapIcon, EyeIcon } from '@heroicons/vue/24/outline'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import { useRouter } from 'vue-router'
import { getCategoryVariant, getDifficultyVariant } from '@/shared/utils/badgeColor'
import { localizeCategory, localizeDifficulty } from '@/shared/utils/localize'

const { t } = useI18n()
const counts = useAdminCounts()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const items = ref<Course[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)

const query = ref<string>('')
const status = ref<string>('') // '' 表示全部
const difficulty = ref<string>('') // '' 表示全部

const statusOptions = [
  { label: String(t('common.all') || 'All'), value: '' },
  { label: String(t('admin.courseStatus.draft') || 'draft'), value: 'draft' },
  { label: String(t('admin.courseStatus.published') || 'published'), value: 'published' },
  { label: String(t('admin.courseStatus.archived') || 'archived'), value: 'archived' },
]

const difficultyOptions = computed(() => ([
  { label: String(t('common.all') || 'All'), value: '' },
  { label: String(t('shared.course.difficultyMap.beginner') || 'beginner'), value: 'beginner' },
  { label: String(t('shared.course.difficultyMap.intermediate') || 'intermediate'), value: 'intermediate' },
  { label: String(t('shared.course.difficultyMap.advanced') || 'advanced'), value: 'advanced' },
]))

const statsLoading = ref(false)
const stats = ref<{ total: number; draft: number; published: number; archived: number }>({ total: 0, draft: 0, published: 0, archived: 0 })

const statsKpis = ref<any[]>([])

async function reloadStats() {
  statsLoading.value = true
  try {
    const q = query.value || undefined
    const diff = difficulty.value || undefined
    const [total, draft, published, archived] = await Promise.all([
      counts.countCourses({ query: q, difficulty: diff }),
      counts.countCourses({ query: q, difficulty: diff, status: 'draft' }),
      counts.countCourses({ query: q, difficulty: diff, status: 'published' }),
      counts.countCourses({ query: q, difficulty: diff, status: 'archived' }),
    ])
    stats.value = { total, draft, published, archived }
    statsKpis.value = [
      { label: t('admin.kpi.totalCourses') || '课程总数', value: total, tint: 'info', icon: AcademicCapIcon },
      { label: t('admin.courseStatus.draft') || 'draft', value: draft, tint: 'secondary', icon: AcademicCapIcon },
      { label: t('admin.courseStatus.published') || 'published', value: published, tint: 'success', icon: AcademicCapIcon },
      { label: t('admin.courseStatus.archived') || 'archived', value: archived, tint: 'warning', icon: AcademicCapIcon },
    ]
  } catch {
    // KPI 失败不阻塞列表
    statsKpis.value = [
      { label: t('admin.kpi.totalCourses') || '课程总数', value: '-', tint: 'info', icon: AcademicCapIcon },
    ]
  } finally {
    statsLoading.value = false
  }
}

function normalizeStatus(v: any): string {
  const s = String(v || '').trim()
  return s.toLowerCase()
}

function statusLabel(v: any): string {
  const s = normalizeStatus(v)
  if (s === 'draft') return String(t('admin.courseStatus.draft') || 'draft')
  if (s === 'published') return String(t('admin.courseStatus.published') || 'published')
  if (s === 'archived') return String(t('admin.courseStatus.archived') || 'archived')
  return String(v || '-')
}

function statusVariant(v: any) {
  const s = normalizeStatus(v)
  if (s === 'published') return 'success'
  if (s === 'draft') return 'secondary'
  if (s === 'archived') return 'warning'
  return 'info'
}

function formatDateTime(v?: string) {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleDateString()
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res: any = await adminApi.pageCourses({
      page: page.value,
      size: pageSize.value,
      query: query.value || undefined,
      status: status.value || undefined,
      difficulty: difficulty.value || undefined,
    })
    items.value = (res?.items || []) as Course[]
    total.value = Number(res?.total || 0)
    totalPages.value = Number(res?.totalPages || 1)
    // 课程概览卡片：按当前 query 维度统计（不受 status filter 影响）
    reloadStats()
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

let debounceTimer: any = null
watch([query, status, difficulty], () => {
  page.value = 1
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => reload(), 250)
})

onMounted(() => {
  reload()
})
</script>

