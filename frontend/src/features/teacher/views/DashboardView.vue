<template>
  <div class="p-6 min-h-screen">
    <page-header :title="t('teacher.dashboard.header.title')" :subtitle="t('teacher.dashboard.header.subtitle')">
      <template #actions>
        <div class="flex items-end gap-4 w-full sm:w-auto">
          <div class="w-full sm:w-auto flex items-center gap-3">
            <span class="text-sm font-semibold text-gray-700 dark:text-gray-300 whitespace-nowrap">
              {{ t('teacher.dashboard.course.select.label') as string }}
            </span>
            <div class="w-full sm:w-64">
              <glass-popover-select
                v-model="selectedCourseId"
                :options="teacherCourseOptions"
                :placeholder="t('teacher.dashboard.course.select.placeholder') as string"
                size="md"
                :fullWidth="false"
                width="260px"
                @change="onCourseSelect"
              />
            </div>
          </div>
          <div class="hidden sm:flex items-center gap-2">
            <Button variant="primary" @click="goPublishAssignment">
              <plus-icon class="w-4 h-4 mr-2" />{{ t('teacher.dashboard.actions.publish') }}
            </Button>
            <Button variant="success" @click="goGradeAssignments">
              <check-badge-icon class="w-4 h-4 mr-2" />{{ t('teacher.dashboard.actions.grade') }}
            </Button>
          </div>
        </div>
      </template>
    </page-header>

    <!-- Course Selector -->
    

    <!-- Loading State -->
    <div v-if="teacherStore.loading" class="text-center py-12">
        <p>{{ t('app.loading.title') }}</p>
      </div>

    <!-- Analytics Content -->
    <div v-else-if="selectedCourseId && courseAnalytics" class="space-y-8">
        <!-- Stats Cards (统一为工作台小卡片组件) -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <start-card :label="t('teacher.dashboard.cards.totalStudents') as string" :value="n(safeAnalytics.totalStudents, 'integer')" tone="blue" :icon="UserGroupIcon" />
          <start-card :label="t('teacher.dashboard.cards.completionRate') as string" :value="n((safeAnalytics.completionRate || 0) / 100, 'percent')" tone="emerald" :icon="CheckCircleIcon" />
          <start-card :label="t('teacher.dashboard.cards.averageScore') as string" :value="(safeAnalytics.averageScore || 0).toFixed(1)" tone="violet" :icon="StarIcon" />
          <start-card :label="t('teacher.dashboard.cards.totalAssignments') as string" :value="n(safeAnalytics.totalAssignments, 'integer')" tone="amber" :icon="ClipboardDocumentListIcon" />
        </div>

        <!-- Class Performance Chart -->
        <Card padding="lg" tint="accent">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold text-base-content">{{ t('teacher.dashboard.chart.title') }}</h2>
            <span class="text-xs text-subtle">{{ t('teacher.dashboard.chart.subtitle') }}</span>
          </div>
          <div ref="chartRef" class="h-[32rem] w-full"></div>
        </Card>
    </div>

     <!-- Empty State -->
    <div v-else class="text-center py-16 card">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-12 h-12 mx-auto text-gray-400 mb-3" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5a2 2 0 00-2 2v14l4-4h12a2 2 0 002-2V5a2 2 0 00-2-2z"/></svg>
      <h3 class="text-lg font-medium text-base-content">{{ t('teacher.dashboard.empty.title') }}</h3>
      <p class="text-subtle">{{ t('teacher.dashboard.empty.description') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useTeacherStore } from '@/stores/teacher'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth' // 1. 导入 useAuthStore
import type * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import { getEChartsThemedTokens } from '@/utils/theme'
import { getOrInitChart, bindHideTipOnGlobalOut, bindTooltipVisibility, bindThemeChangeEvents } from '@/charts/echartsHelpers'
import { useI18n } from 'vue-i18n'
import { loadLocaleMessages } from '@/i18n'
import { PlusIcon, CheckBadgeIcon, UserGroupIcon, CheckCircleIcon, StarIcon, ClipboardDocumentListIcon } from '@heroicons/vue/24/outline'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUIStore()
const router = useRouter()
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore() // 2. 获取 authStore 实例
const { t, n, locale } = useI18n()

const selectedCourseId = ref<string | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null
let resizeBound = false
let darkObserver: { disconnect: () => void } | null = null
let reRenderScheduled = false
const onResize = () => chart?.resize()

function gradeDistributionKey(dist: any[]): string {
  const arr = Array.isArray(dist) ? dist : []
  return arr.map(d => `${String(d?.gradeLevel ?? d?.level ?? '')}:${Number(d?.count ?? 0)}`).join('|')
}

// 3. 创建 teacherCourses 计算属性
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(course => String(course.teacherId) === String(authStore.user?.id))
})

const teacherCourseOptions = computed(() =>
  teacherCourses.value.map((c: { id: string | number; title: string }) => ({ label: c.title, value: String(c.id) }))
)

const loading = teacherStore.loading
const courseAnalytics = computed(() => teacherStore.courseAnalytics)
const classPerformance = computed(() => teacherStore.classPerformance)

const safeAnalytics = computed(() => ({
  totalStudents: (courseAnalytics.value as any).totalStudents ?? courseAnalytics.value.enrollmentCount ?? 0,
  completionRate: (courseAnalytics.value as any).completionRate ?? courseAnalytics.value.averageCompletionRate ?? 0,
  averageScore: courseAnalytics.value.averageScore ?? 0,
  totalAssignments: (courseAnalytics.value as any).totalAssignments ?? courseAnalytics.value.assignmentCount ?? 0,
}))

const onCourseSelect = async () => {
  if (!selectedCourseId.value) return
  try {
    await Promise.all([
      teacherStore.fetchCourseAnalytics(selectedCourseId.value),
      teacherStore.fetchClassPerformance(selectedCourseId.value),
    ])
  } catch { /* errors are handled in store */ }
  // 关键：即便数据“看起来没变”（key 相同），切回页面后也要确保图表会 init/setOption
  await nextTick()
  initChart()
  chart?.resize()
}

const goPublishAssignment = () => {
  const query: Record<string, string> = {}
  if (selectedCourseId.value) query.courseId = selectedCourseId.value
  query.openCreate = '1'
  router.push({ name: 'TeacherAssignments', query })
}

const goGradeAssignments = () => {
  const query: Record<string, string> = {}
  if (selectedCourseId.value) query.courseId = selectedCourseId.value
  router.push({ name: 'TeacherAssignments', query })
}

const initChart = () => {
  const dist: any[] = (classPerformance.value as any).gradeDistribution || []
  if (!chartRef.value) return
  // 复用实例，避免重复 init 警告
  chart = getOrInitChart(chartRef.value, resolveEChartsTheme())
  if (!chart) return
  if (!dist.length) {
    chart.clear()
    return
  }
  // 使用统一主题色盘（避免某些主题色盘过亮导致柱子接近白色）
  const palette = resolveThemePalette()
  const tokens = getEChartsThemedTokens()
  const categories = dist.map(d => d.gradeLevel ?? d.level ?? t('teacher.dashboard.chart.level.unknown'))
  const values = dist.map(d => d.count ?? 0)
  const seriesData = values.map((v, idx) => {
    const base = palette[idx % palette.length]
    return {
      value: v,
      itemStyle: { color: base }
    }
  })
  // 动态计算更粗的柱宽（默认近似直方图，仍保留一定间隔）
  const containerWidth = (chartRef.value as HTMLElement).clientWidth || 800
  const innerWidth = Math.floor(containerWidth * 0.94) // 近似减去 grid 边距
  const n = Math.max(categories.length, 1)
  const minGapPx = 12
  const computedBarWidth = (() => {
    const widthIfDense = Math.floor(innerWidth / (n * 1.2)) // 普通密度
    const widthIfFew = Math.floor((innerWidth - (n + 1) * minGapPx) / n) // 类直方图（少分类更粗）
    const base = n <= 6 ? widthIfFew : widthIfDense
    return Math.min(140, Math.max(36, base))
  })()
  chart.setOption({
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      textStyle: { color: 'var(--color-base-content)' },
      // 默认隐藏，由 showTip/hideTip 控制可见性（避免残留）
      extraCssText: glassTooltipCss() + ';visibility:hidden;pointer-events:none;',
      className: 'echarts-glass-tooltip',
      renderMode: 'html',
      enterable: false,
      // appendToBody 时不再 confine，避免浅色模式下定位异常导致“看似不显示”
      confine: false,
      // 关键：tooltip 需要挂到 body，避免被 LiquidGlass 的 overflow:hidden 裁剪
      appendToBody: true,
      transitionDuration: 0,
      showDelay: 0,
      hideDelay: 30,
      formatter: (p: any) => `${p.name}<br/>${t('teacher.dashboard.chart.series.students')}: ${p.value}`
    },
    grid: { left: '4%', right: '2%', bottom: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      data: categories,
      axisTick: { alignWithLabel: true },
      axisLine: { lineStyle: { color: tokens.axisLine } },
      axisLabel: { color: tokens.axisLabel }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: tokens.axisLine } },
      axisLabel: { color: tokens.axisLabel },
      splitLine: { lineStyle: { color: tokens.splitLine } }
    },
    series: [{
      name: t('teacher.dashboard.chart.series.students'),
      type: 'bar',
      data: seriesData,
      barWidth: computedBarWidth,
      barCategoryGap: '8%',
      // 重放动画：首次渲染或主题切换后，提供短促进入动画
      animation: true,
      animationDuration: 420,
      animationEasing: 'cubicOut',
      // 禁用 hover/emphasis 状态，避免任何“变暗/变色”行为
      emphasis: { disabled: true },
      // hover/emphasis 样式在 data 内逐条定义，避免 params.dataIndex 缺失导致“跳色”
    }]
  }, { notMerge: true, lazyUpdate: false })

  bindTooltipVisibility(chart)
  bindHideTipOnGlobalOut(chart)
}

function scheduleReinit() {
  if (reRenderScheduled) return
  reRenderScheduled = true
  requestAnimationFrame(() => {
    reRenderScheduled = false
    // 主题切换后：直接走 initChart 的“完整配置重绘”（包含 tooltip），避免 clear 后丢失 tooltip 配置
    try { chart?.dispatchAction({ type: 'hideTip' } as any) } catch {}
    initChart()
  })
}

watch(() => gradeDistributionKey((classPerformance.value as any)?.gradeDistribution || []), () => nextTick(initChart), { immediate: true })
// 当语言切换时，重绘图表以应用新文案
watch(() => locale.value, () => nextTick(initChart), { immediate: true })
// 当从其它页面切回工作台时，本次组件会重新挂载，但数据可能未变化（watch 不触发）；
// 这里监听 DOM ref 挂载，确保图表必定初始化一次。
watch(chartRef, () => nextTick(() => { initChart(); chart?.resize() }))

onMounted(async () => {
  // 确保在获取课程之前，用户信息是可用的
  if (!authStore.user) {
    await authStore.fetchUser()
  }
  await courseStore.fetchCourses({ page: 1, size: 100 })
  // 5. 使用过滤后的列表
  if (teacherCourses.value.length) {
    selectedCourseId.value = String(teacherCourses.value[0].id)
    await onCourseSelect()
  }
  // 预加载教师端命名空间
  await loadLocaleMessages(locale.value as 'zh-CN' | 'en-US', ['teacher'])
  if (!resizeBound) {
    window.addEventListener('resize', onResize)
    resizeBound = true
  }
  // 监听全局主题事件：切换中隐藏 tooltip，切换完成后重放动画
  if (!darkObserver) {
    const onChanging = () => { try { chart?.dispatchAction({ type: 'hideTip' } as any) } catch {} }
    const onChanged = () => scheduleReinit()
    darkObserver = bindThemeChangeEvents({ onChanging, onChanged })
  }
})

onUnmounted(() => {
  if (resizeBound) {
    window.removeEventListener('resize', onResize)
    resizeBound = false
  }
  chart?.dispose()
  if (darkObserver) { try { darkObserver.disconnect() } catch {} darkObserver = null }
})
</script>
