<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.console')" :subtitle="t('admin.title')" />

    <FilterBar tint="secondary" align="center" :dense="false" class="mt-4 mb-2 rounded-full h-19">
      <template #left>
        <div class="flex items-center gap-3 flex-wrap">
          <div class="w-auto flex items-center gap-2">
            <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.console.daysLabel') || '时间窗口' }}</span>
            <div class="w-56">
              <GlassPopoverSelect v-model="days" :options="dayOptions" size="sm" tint="primary" />
            </div>
          </div>
          <Button size="sm" variant="outline" :disabled="loading" @click="reload">
            {{ t('common.refresh') || '刷新' }}
          </Button>
        </div>
      </template>
      <template #right>
        <div class="text-xs text-subtle whitespace-nowrap">
          {{ t('admin.console.dataSourceHint') || '数据来自管理员聚合接口与分页 totals 统计（size=1）' }}
        </div>
      </template>
    </FilterBar>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <ErrorState
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-else class="mt-4 space-y-6">
      <AdminKpiRow :items="kpis" />

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <AdminTrendsPanel
          class="lg:col-span-2"
          :title="t('admin.console.trendsTitle') || '关键趋势'"
          :subtitle="t('admin.console.trendsSubtitle', { days }) || `近 ${days} 天：活跃 / 新增 / 选课`"
          tint="secondary"
          :series="trendSeries"
          :xAxisData="trendDays"
          height="320px"
        >
          <template #actions>
            <Button size="sm" variant="outline" @click="router.push('/admin/moderation')">
              {{ t('admin.console.quickModeration') || '去治理台' }}
            </Button>
          </template>
        </AdminTrendsPanel>

        <Card padding="md" tint="secondary">
          <div class="text-sm font-medium mb-2">{{ t('admin.console.quickActions') || '快捷入口' }}</div>
          <div class="space-y-2">
            <Button class="w-full justify-start" variant="outline" size="sm" @click="router.push('/admin/people?tab=users')">
              {{ t('admin.console.gotoPeople') || '数据中心：用户与人群' }}
            </Button>
            <Button class="w-full justify-start" variant="outline" size="sm" @click="router.push('/admin/courses')">
              {{ t('admin.console.gotoCourses') || '课程中心：课程总览' }}
            </Button>
            <Button class="w-full justify-start" variant="outline" size="sm" @click="router.push('/admin/moderation?tab=ability')">
              {{ t('admin.console.gotoModeration') || '内容治理：报告/社区' }}
            </Button>
            <Button class="w-full justify-start" variant="outline" size="sm" @click="router.push('/admin/tools')">
              {{ t('admin.console.gotoTools') || '工具中心：导出' }}
            </Button>
          </div>

          <div class="border-t border-white/10 my-4"></div>
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div class="p-3 glass-thin rounded-2xl" v-glass="{ strength: 'thin', interactive: false }">
              <div class="text-xs text-subtle mb-1">{{ t('admin.console.activeUsers') || '近 N 天活跃' }}</div>
              <div class="font-semibold">{{ overview?.activity?.activeUsers ?? 0 }}</div>
            </div>
            <div class="p-3 glass-thin rounded-2xl" v-glass="{ strength: 'thin', interactive: false }">
              <div class="text-xs text-subtle mb-1">{{ t('admin.console.abilityReports') || '能力报告' }}</div>
              <div class="font-semibold">{{ abilityReportsTotal }}</div>
            </div>
          </div>
        </Card>

        <AdminDistributionPanel
          :title="t('admin.console.userRoleDist') || '用户角色分布'"
          tint="info"
          :data="userRolePie"
          height="320px"
        />

        <AdminDistributionPanel
          :title="t('admin.console.courseStatusDist') || '课程状态分布'"
          tint="accent"
          :data="courseStatusPie"
          height="320px"
        />

        <AdminDistributionPanel
          :title="t('admin.console.postStatusDist') || '帖子状态分布'"
          tint="success"
          :data="postStatusPie"
          height="320px"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import AdminKpiRow from '@/features/admin/components/AdminKpiRow.vue'
import AdminTrendsPanel from '@/features/admin/components/AdminTrendsPanel.vue'
import AdminDistributionPanel from '@/features/admin/components/AdminDistributionPanel.vue'
import { adminApi, type AdminDashboardOverview } from '@/api/admin.api'
import { useAdminCounts } from '@/features/admin/composables/useAdminCounts'
import { UsersIcon, AcademicCapIcon, ShieldCheckIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const router = useRouter()
const counts = useAdminCounts()

const days = ref(30)
const dayOptions = [
  { label: '7 days', value: 7 },
  { label: '30 days', value: 30 },
  { label: '90 days', value: 90 },
  { label: '180 days', value: 180 },
  { label: '365 days', value: 365 },
]

const loading = ref(false)
const error = ref<string | null>(null)
const overview = ref<AdminDashboardOverview | null>(null)
const series = ref<any>(null)

const abilityReportsTotal = ref(0)
const postTotals = ref<Record<string, number>>({ published: 0, draft: 0, deleted: 0 })

function toYmd(d: Date): string {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function lastNDays(n: number): string[] {
  const days = Math.max(1, Math.min(Number(n) || 30, 365))
  const out: string[] = []
  const now = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(now.getDate() - i)
    out.push(toYmd(d))
  }
  return out
}

function mapSeries(list: any[]): Map<string, number> {
  const m = new Map<string, number>()
  ;(Array.isArray(list) ? list : []).forEach((it: any) => {
    const k = String(it?.day || '').slice(0, 10)
    if (!k) return
    m.set(k, Number(it?.value || 0))
  })
  return m
}

const trendDays = computed(() => lastNDays(Number(days.value) || 30))

const trendSeries = computed(() => {
  const x = trendDays.value
  const activeMap = mapSeries(series.value?.activeUsersDaily || [])
  const newMap = mapSeries(series.value?.newUsersDaily || [])
  const enrollMap = mapSeries(series.value?.enrollmentsDaily || [])
  return [
    { name: t('admin.console.series.active') || '活跃', data: x.map((d) => ({ x: d, y: activeMap.get(d) ?? 0 })) },
    { name: t('admin.console.series.newUsers') || '新增', data: x.map((d) => ({ x: d, y: newMap.get(d) ?? 0 })) },
    { name: t('admin.console.series.enrollments') || '选课', data: x.map((d) => ({ x: d, y: enrollMap.get(d) ?? 0 })) },
  ]
})

const kpis = computed(() => ([
  { label: t('admin.kpi.totalUsers') || '用户总数', value: overview.value?.users?.total ?? 0, tint: 'info' as const, icon: UsersIcon },
  { label: t('admin.kpi.totalCourses') || '课程总数', value: overview.value?.courses?.total ?? 0, tint: 'accent' as const, icon: AcademicCapIcon },
  { label: t('admin.kpi.communityPosts') || '社区帖子', value: overview.value?.community?.posts ?? 0, tint: 'success' as const, icon: ShieldCheckIcon },
  { label: t('admin.kpi.abilityReports') || '能力报告', value: abilityReportsTotal.value ?? 0, tint: 'warning' as const, icon: DocumentTextIcon },
]))

const userRolePie = computed(() => ([
  { name: t('admin.roles.student') || 'student', value: overview.value?.users?.students ?? 0 },
  { name: t('admin.roles.teacher') || 'teacher', value: overview.value?.users?.teachers ?? 0 },
  { name: t('admin.roles.admin') || 'admin', value: overview.value?.users?.admins ?? 0 },
]))

const courseStatusPie = computed(() => ([
  { name: t('admin.courseStatus.draft') || 'draft', value: overview.value?.courses?.draft ?? 0 },
  { name: t('admin.courseStatus.published') || 'published', value: overview.value?.courses?.published ?? 0 },
  { name: t('admin.courseStatus.archived') || 'archived', value: overview.value?.courses?.archived ?? 0 },
]))

const postStatusPie = computed(() => ([
  { name: t('admin.postStatus.published') || 'published', value: postTotals.value.published ?? 0 },
  { name: t('admin.postStatus.draft') || 'draft', value: postTotals.value.draft ?? 0 },
  { name: t('admin.postStatus.deleted') || 'deleted', value: postTotals.value.deleted ?? 0 },
]))

async function reload() {
  loading.value = true
  error.value = null
  try {
    const d = Number(days.value) || 30
    const [ov, ser] = await Promise.all([
      adminApi.getDashboardOverview(d),
      adminApi.getAnalyticsSeriesOverview(d),
    ])
    overview.value = ov
    series.value = ser

    try {
      const res = await adminApi.pageAbilityReports({ page: 1, size: 1 })
      abilityReportsTotal.value = Number((res as any)?.total || 0)
    } catch {
      abilityReportsTotal.value = 0
    }

    const postStatuses = ['published', 'draft', 'deleted'] as const
    const [published, draft, deleted] = await Promise.all(postStatuses.map(s => counts.countPosts({ status: s, includeDeleted: true })))
    postTotals.value = { published, draft, deleted }
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

