<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.courses')" :subtitle="t('admin.title')" />

    <filter-bar tint="secondary" align="center" :dense="false" class="mt-4 mb-2 rounded-full h-19">
      <template #left>
        <div class="flex items-center gap-3 flex-wrap">
          <div class="w-72">
            <glass-search-input v-model="query" :placeholder="String(t('common.search') || '搜索课程标题/教师')" size="sm" tint="info" />
          </div>
          <div class="w-auto flex items-center gap-2">
            <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courses.statusLabel') || '状态' }}</span>
            <div class="w-44">
              <glass-popover-select
                :model-value="status"
                :options="statusOptions"
                size="sm"
                tint="secondary"
                @update:modelValue="(v: any) => (status = v)"
              />
            </div>
          </div>
        </div>
      </template>
      <template #right>
        <button size="sm" variant="outline" :disabled="loading" @click="reload">{{ String(t('common.search') || '查询') }}</button>
      </template>
    </filter-bar>

    <div class="mt-4">
      <loading-overlay v-if="statsLoading" :text="String(t('common.loading') || '加载中…')" />
      <admin-kpi-row v-else :items="statsKpis" />
    </div>

    <div v-if="!statsLoading" class="mt-4 grid grid-cols-1 lg:grid-cols-3 gap-6">
      <admin-distribution-panel
        class="lg:col-span-2"
        :title="t('admin.courses.statusDistTitle') || '课程状态分布'"
        :subtitle="t('admin.courses.statusDistSubtitle') || '基于当前搜索条件（query）统计 totals，便于快速判断整体结构'"
        tint="accent"
        :data="statusPie"
        height="320px"
      />
      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-2">{{ t('admin.courses.tipsTitle') || '提示' }}</div>
        <div class="text-xs text-subtle space-y-2">
          <div>{{ t('admin.courses.tip1') || '筛选条件越具体，分布图与概览卡片越贴近当前任务。' }}</div>
          <div>{{ t('admin.courses.tip2') || '如需更复杂的统计（按教师/分类/难度），建议后端增加聚合接口。' }}</div>
        </div>
      </card>
    </div>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <card v-else padding="md" tint="secondary" class="mt-4">
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('common.columns.id') || 'ID' }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">
              {{ t('admin.sidebar.courses') }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('common.columns.teacher') || 'Teacher' }}
            </th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('common.columns.status') || 'Status' }}
            </th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('common.columns.student') || 'Students' }}
            </th>
          </tr>
        </template>

        <template #body>
          <tr v-for="c in items" :key="c.id" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center font-mono text-xs">{{ c.id }}</td>
            <td class="px-6 py-3">
              <div class="font-medium">{{ c.title }}</div>
              <div class="text-xs text-subtle line-clamp-1">{{ c.description }}</div>
            </td>
            <td class="px-6 py-3 text-sm">
              {{ (c as any).teacherName || (c as any).teacher?.nickname || (c as any).teacher?.username || '-' }}
            </td>
            <td class="px-6 py-3 text-sm text-center">{{ c.status }}</td>
            <td class="px-6 py-3 text-sm">
              <div class="flex items-center justify-between gap-2">
                <span>{{ (c as any).enrollmentCount ?? (c as any).studentCount ?? '-' }}</span>
                <button size="sm" variant="outline" @click="router.push(`/admin/courses/${c.id}`)">{{ t('common.view') || '查看' }}</button>
              </div>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="5" class="px-6 py-6">
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
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
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
import { AcademicCapIcon } from '@heroicons/vue/24/outline'
import AdminDistributionPanel from '@/features/admin/components/AdminDistributionPanel.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import { useRouter } from 'vue-router'

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

const statusOptions = [
  { label: String(t('common.all') || 'All'), value: '' },
  { label: String(t('admin.courseStatus.draft') || 'draft'), value: 'draft' },
  { label: String(t('admin.courseStatus.published') || 'published'), value: 'published' },
  { label: String(t('admin.courseStatus.archived') || 'archived'), value: 'archived' },
]

const statsLoading = ref(false)
const stats = ref<{ total: number; draft: number; published: number; archived: number }>({ total: 0, draft: 0, published: 0, archived: 0 })

const statsKpis = ref<any[]>([])

const statusPie = ref<{ name: string; value: number }[]>([])

async function reloadStats() {
  statsLoading.value = true
  try {
    const q = query.value || undefined
    const [total, draft, published, archived] = await Promise.all([
      counts.countCourses({ query: q }),
      counts.countCourses({ query: q, status: 'draft' }),
      counts.countCourses({ query: q, status: 'published' }),
      counts.countCourses({ query: q, status: 'archived' }),
    ])
    stats.value = { total, draft, published, archived }
    statsKpis.value = [
      { label: t('admin.kpi.totalCourses') || '课程总数', value: total, tint: 'info', icon: AcademicCapIcon },
      { label: t('admin.courseStatus.draft') || 'draft', value: draft, tint: 'secondary', icon: AcademicCapIcon },
      { label: t('admin.courseStatus.published') || 'published', value: published, tint: 'success', icon: AcademicCapIcon },
      { label: t('admin.courseStatus.archived') || 'archived', value: archived, tint: 'warning', icon: AcademicCapIcon },
    ]
    statusPie.value = [
      { name: t('admin.courseStatus.draft') || 'draft', value: draft },
      { name: t('admin.courseStatus.published') || 'published', value: published },
      { name: t('admin.courseStatus.archived') || 'archived', value: archived },
    ]
  } catch {
    // KPI 失败不阻塞列表
    statsKpis.value = [
      { label: t('admin.kpi.totalCourses') || '课程总数', value: '-', tint: 'info', icon: AcademicCapIcon },
    ]
    statusPie.value = []
  } finally {
    statsLoading.value = false
  }
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

onMounted(reload)
</script>

