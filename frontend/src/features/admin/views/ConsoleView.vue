<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.console')" :subtitle="t('admin.title')" />

    <div class="mt-4">
      <AdminKpiRow :items="kpis" />
    </div>

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
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 items-start">
        <div class="xl:col-span-2 space-y-6">
          <AdminTrendsPanel
            :title="t('admin.console.trendsTitle') || '关键趋势'"
            :subtitle="t('admin.console.trendsSubtitle', { days }) || `近 ${days} 天：活跃 / 新增 / 选课`"
            tint="secondary"
            :series="trendSeries"
            :xAxisData="trendDays"
            height="640px"
            :grid="{ top: '10%', left: '6%', right: '6%', bottom: '16%' }"
            :legend="{ bottom: 0 }"
            :tooltip="{ confine: true }"
          />

          <Card padding="md" tint="accent">
            <div class="flex items-center justify-between gap-3 mb-3">
              <div class="text-sm font-medium">{{ t('admin.console.abilityRadarTitle') || '整体五维能力雷达' }}</div>
            </div>
            <RadarChart
              :indicators="radarIndicators"
              :series="radarSeries"
              height="360px"
              :showLegend="true"
              :appendTooltipToBody="false"
            />
            <div v-if="!radarHasData" class="mt-2 text-xs text-subtle">
              {{ t('admin.console.radarEmptyHint') || '当前时间窗口暂无有效能力评估数据，已展示基础维度占位。' }}
            </div>
          </Card>

          <Card padding="md" tint="info">
            <div class="flex items-center justify-between gap-3 mb-3">
              <div class="text-sm font-medium">{{ t('admin.console.aiUsageTitle') || 'AI 使用排行（访问数）' }}</div>
              <div class="text-xs text-subtle">{{ t('admin.console.aiUsageSubtitle', { days }) || `近 ${days} 天按消息数 Top 用户` }}</div>
            </div>
            <div class="mt-1 space-y-3">
              <div v-for="(item, idx) in aiUsageTopList" :key="item.userId" class="text-xs">
                <div class="flex items-center justify-between gap-3 mb-1">
                  <span class="truncate pr-3">#{{ idx + 1 }} {{ item.username }} ({{ item.role }})</span>
                  <span class="font-semibold">{{ aiUsageDisplayCount(item) }}</span>
                </div>
                <div class="h-2 rounded-full bg-white/10 overflow-hidden">
                  <div
                    class="h-full rounded-full transition-all"
                    :style="{ width: `${aiUsageBarPercent(item)}%`, backgroundColor: 'var(--color-primary)', opacity: '0.85' }"
                  />
                </div>
              </div>
            </div>
            <div class="mt-3 grid grid-cols-3 gap-2 text-xs">
              <div class="glass-thin rounded-xl p-2" v-glass="{ strength: 'thin', interactive: false }">
                <div class="text-subtle">{{ t('admin.console.aiSummaryConversations') || '会话总数' }}</div>
                <div class="font-semibold">{{ aiUsageSummary.conversationCount }}</div>
              </div>
              <div class="glass-thin rounded-xl p-2" v-glass="{ strength: 'thin', interactive: false }">
                <div class="text-subtle">{{ t('admin.console.aiSummaryMessages') || '消息总数' }}</div>
                <div class="font-semibold">{{ aiUsageSummary.messageCount }}</div>
              </div>
              <div class="glass-thin rounded-xl p-2" v-glass="{ strength: 'thin', interactive: false }">
                <div class="text-subtle">{{ t('admin.console.aiSummaryActiveUsers') || '活跃用户' }}</div>
                <div class="font-semibold">{{ aiUsageSummary.activeUsers }}</div>
              </div>
            </div>
          </Card>
        </div>

        <div class="xl:col-span-1 space-y-6">
          <AdminDistributionPanel
            :title="t('admin.console.userRoleDist') || '用户角色分布'"
            tint="info"
            :data="userRolePie"
            height="280px"
          />
          <AdminDistributionPanel
            :title="t('admin.console.courseStatusDist') || '课程状态分布'"
            tint="accent"
            :data="courseStatusPie"
            height="280px"
          />
          <AdminDistributionPanel
            :title="t('admin.console.postStatusDist') || '帖子状态分布'"
            tint="success"
            :data="postStatusPie"
            height="280px"
          />
          <AdminDistributionPanel
            :title="t('admin.console.gradeLevelDist') || '成绩等级分布'"
            tint="warning"
            :data="gradeLevelPie"
            height="280px"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import AdminKpiRow from '@/features/admin/components/AdminKpiRow.vue'
import AdminTrendsPanel from '@/features/admin/components/AdminTrendsPanel.vue'
import AdminDistributionPanel from '@/features/admin/components/AdminDistributionPanel.vue'
import {
  adminApi,
  type AdminAiUsageOverviewResponse,
  type AdminAbilityRadarOverviewResponse,
  type AdminDashboardOverview,
} from '@/api/admin.api'
import { useAdminCounts } from '@/features/admin/composables/useAdminCounts'
import { UsersIcon, AcademicCapIcon, ShieldCheckIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const counts = useAdminCounts()

const days = ref(180)
const dayOptions = computed(() => ([
  { label: t('admin.console.dayOptions.d7') || '7天', value: 7 },
  { label: t('admin.console.dayOptions.d30') || '30天', value: 30 },
  { label: t('admin.console.dayOptions.d90') || '90天', value: 90 },
  { label: t('admin.console.dayOptions.d180') || '180天', value: 180 },
  { label: t('admin.console.dayOptions.d365') || '365天', value: 365 },
]))

const loading = ref(false)
const error = ref<string | null>(null)
const overview = ref<AdminDashboardOverview | null>(null)
const series = ref<any>(null)
const abilityRadar = ref<AdminAbilityRadarOverviewResponse | null>(null)
const aiUsage = ref<AdminAiUsageOverviewResponse | null>(null)

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

const trendDays = computed(() => lastNDays(Number(days.value) || 180))

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

const gradeLevelPie = computed(() => ([
  { name: t('admin.gradeLevel.A') || 'A', value: overview.value?.grades?.A ?? 0 },
  { name: t('admin.gradeLevel.B') || 'B', value: overview.value?.grades?.B ?? 0 },
  { name: t('admin.gradeLevel.C') || 'C', value: overview.value?.grades?.C ?? 0 },
  { name: t('admin.gradeLevel.D') || 'D', value: overview.value?.grades?.D ?? 0 },
  { name: t('admin.gradeLevel.E') || 'E', value: overview.value?.grades?.E ?? 0 },
  { name: t('admin.gradeLevel.F') || 'F', value: overview.value?.grades?.F ?? 0 },
]))

const defaultRadarDimensions = computed(() => ([
  { code: 'MORAL_COGNITION', name: t('admin.console.radarDims.moral') || '道德认知' },
  { code: 'LEARNING_ATTITUDE', name: t('admin.console.radarDims.attitude') || '学习态度' },
  { code: 'LEARNING_ABILITY', name: t('admin.console.radarDims.ability') || '学习能力' },
  { code: 'LEARNING_METHOD', name: t('admin.console.radarDims.method') || '学习方法' },
  { code: 'ACADEMIC_GRADE', name: t('admin.console.radarDims.academic') || '学业成绩' },
]))

const radarDataMap = computed(() => {
  const map = new Map<string, number>()
  ;(abilityRadar.value?.dimensions || []).forEach((item) => {
    const value = Number(item?.value || 0)
    if (item?.code) map.set(String(item.code), value)
    const name = String(item?.name || '')
    if (name) map.set(name, value)
    if (name.includes('道德')) map.set('MORAL_COGNITION', value)
    if (name.includes('态度')) map.set('LEARNING_ATTITUDE', value)
    if (name.includes('能力')) map.set('LEARNING_ABILITY', value)
    if (name.includes('方法')) map.set('LEARNING_METHOD', value)
    if (name.includes('成绩')) map.set('ACADEMIC_GRADE', value)
  })
  return map
})

const radarIndicators = computed(() => defaultRadarDimensions.value.map((item) => ({ name: item.name, max: 100 })))

const radarSeries = computed(() => {
  return [
    {
      name: t('admin.console.radarSeriesSchool') || '全局均值',
      values: defaultRadarDimensions.value.map((item) => Number(radarDataMap.value.get(item.code) || 0)),
    },
  ]
})
const radarHasData = computed(() => (abilityRadar.value?.dimensions || []).some((item) => Number(item.sampleSize || 0) > 0))

const aiUsageTopList = computed(() => (aiUsage.value?.users || []).slice(0, 10))
function aiUsageDisplayCount(item: { messageCount?: number; conversationCount?: number }): number {
  const messages = Number(item?.messageCount || 0)
  const conversations = Number(item?.conversationCount || 0)
  return Math.max(messages, conversations)
}
const aiUsageMax = computed(() => Math.max(1, ...aiUsageTopList.value.map((item) => aiUsageDisplayCount(item))))
function aiUsageBarPercent(item: { messageCount?: number; conversationCount?: number }): number {
  const current = aiUsageDisplayCount(item)
  return Math.max(4, Math.round((current / aiUsageMax.value) * 100))
}
const aiUsageSummary = computed(() => ({
  conversationCount: Number(aiUsage.value?.summary?.conversationCount || 0),
  messageCount: Number(aiUsage.value?.summary?.messageCount || 0),
  activeUsers: Number(aiUsage.value?.summary?.activeUsers || 0),
}))

async function reload() {
  loading.value = true
  error.value = null
  try {
    const d = Number(days.value) || 180
    const [ov, ser, radar, ai] = await Promise.all([
      adminApi.getDashboardOverview(d),
      adminApi.getAnalyticsSeriesOverview(d),
      adminApi.getAbilityRadarOverview(0),
      adminApi.getAiUsageOverview(d, 20),
    ])
    overview.value = ov
    series.value = ser
    abilityRadar.value = radar
    aiUsage.value = ai

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

