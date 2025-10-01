<template>
  <div class="p-6 min-h-screen">
    <page-header :title="t('teacher.dashboard.header.title')" :subtitle="t('teacher.dashboard.header.subtitle')">
      <template #actions>
        <div class="flex items-end gap-4 w-full sm:w-auto">
          <div class="w-full sm:w-64">
            <glass-popover-select
              :label="t('teacher.dashboard.course.select.label') as string"
              v-model="selectedCourseId"
              :options="teacherCourseOptions"
              :placeholder="t('teacher.dashboard.course.select.placeholder') as string"
              size="md"
              stacked
              @change="onCourseSelect"
            />
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
import * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import { getEChartsThemedTokens } from '@/utils/theme'
import { useI18n } from 'vue-i18n'
import { loadLocaleMessages } from '@/i18n'
import { PlusIcon, CheckBadgeIcon, UserGroupIcon, CheckCircleIcon, StarIcon, ClipboardDocumentListIcon } from '@heroicons/vue/24/outline'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassSelect from '@/components/ui/filters/GlassSelect.vue'
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
let lastIsDark: boolean | null = null
let reRenderScheduled = false
const onResize = () => chart?.resize()

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

const toggleSidebar = () => uiStore.toggleSidebar()

const onCourseSelect = () => {
  if (selectedCourseId.value) {
    teacherStore.fetchCourseAnalytics(selectedCourseId.value)
    teacherStore.fetchClassPerformance(selectedCourseId.value)
  }
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
  // 复用已存在的实例，避免重复 init 警告
  const theme = resolveEChartsTheme()
  chart = echarts.getInstanceByDom(chartRef.value) || echarts.init(chartRef.value as HTMLDivElement, theme as any)
  if (!dist.length) {
    chart.clear()
    return
  }
  // 使用统一主题色盘（避免某些主题色盘过亮导致柱子接近白色）
  const palette = resolveThemePalette()
  const tokens = getEChartsThemedTokens()
  const categories = dist.map(d => d.gradeLevel ?? d.level ?? t('teacher.dashboard.chart.level.unknown'))
  const values = dist.map(d => d.count ?? 0)
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
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      textStyle: { color: 'var(--color-base-content)' },
      extraCssText: glassTooltipCss(),
      appendToBody: false,
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
      data: values,
      barWidth: computedBarWidth,
      barCategoryGap: '8%',
      // 重放动画：首次渲染或主题切换后，提供短促进入动画
      animation: true,
      animationDuration: 420,
      animationEasing: 'cubicOut',
      // 默认颜色 + 悬停加深
      itemStyle: {
        color: (params: any) => palette[params.dataIndex % palette.length]
      },
      emphasis: {
        focus: 'none',
        itemStyle: {
          // 仅降低不透明度表现“变暗”，不改变色相
          opacity: 0.85
        }
      }
    }]
  }, { notMerge: true, lazyUpdate: false })
}

// 主题轻量刷新：不销毁实例，仅更新颜色与文字样式
function refreshForTheme() {
  if (!chart) return
  try {
    const tokens = getEChartsThemedTokens()
    const palette = resolveThemePalette()
    chart.setOption({
      xAxis: { axisLine: { lineStyle: { color: tokens.axisLine } }, axisLabel: { color: tokens.axisLabel } },
      yAxis: { axisLine: { lineStyle: { color: tokens.axisLine } }, axisLabel: { color: tokens.axisLabel }, splitLine: { lineStyle: { color: tokens.splitLine } } },
      series: [{ itemStyle: { color: (params: any) => palette[params.dataIndex % palette.length] } }]
    }, false)
  } catch {}
}

function scheduleReinit() {
  if (reRenderScheduled) return
  reRenderScheduled = true
  requestAnimationFrame(() => {
    reRenderScheduled = false
    // 主题切换后进行一次带动画的轻量重绘（不销毁实例），并更新坐标/网格线颜色
    try { chart?.dispatchAction({ type: 'hideTip' } as any) } catch {}
    const dist: any[] = (classPerformance.value as any).gradeDistribution || []
    if (!chartRef.value || !chart || !dist.length) { initChart(); return }
    const palette = resolveThemePalette()
    const tokens = getEChartsThemedTokens()
    const categories = dist.map(d => d.gradeLevel ?? d.level ?? t('teacher.dashboard.chart.level.unknown'))
    const values = dist.map(d => d.count ?? 0)
    const containerWidth = (chartRef.value as HTMLElement).clientWidth || 800
    const innerWidth = Math.floor(containerWidth * 0.94)
    const n = Math.max(categories.length, 1)
    const minGapPx = 12
    const computedBarWidth = (() => {
      const widthIfDense = Math.floor(innerWidth / (n * 1.2))
      const widthIfFew = Math.floor((innerWidth - (n + 1) * minGapPx) / n)
      const base = n <= 6 ? widthIfFew : widthIfDense
      return Math.min(140, Math.max(36, base))
    })()
    chart.clear()
    chart.setOption({
      grid: { left: '4%', right: '2%', bottom: '5%', containLabel: true },
      xAxis: { type: 'category', data: categories, axisTick: { alignWithLabel: true }, axisLine: { lineStyle: { color: tokens.axisLine } }, axisLabel: { color: tokens.axisLabel } },
      yAxis: { type: 'value', axisLine: { lineStyle: { color: tokens.axisLine } }, axisLabel: { color: tokens.axisLabel }, splitLine: { lineStyle: { color: tokens.splitLine } } },
      series: [{ name: t('teacher.dashboard.chart.series.students'), type: 'bar', data: values, barWidth: computedBarWidth, barCategoryGap: '8%', animation: true, animationDuration: 420, animationEasing: 'cubicOut', itemStyle: { color: (params: any) => palette[params.dataIndex % palette.length] }, emphasis: { focus: 'none', itemStyle: { opacity: 0.85 } } }]
    }, { notMerge: true, lazyUpdate: false })
  })
}

watch(classPerformance, () => nextTick(initChart), { deep: true })
// 当语言切换时，重绘图表以应用新文案
watch(() => locale.value, () => nextTick(initChart))

onMounted(async () => {
  // 确保在获取课程之前，用户信息是可用的
  if (!authStore.user) {
    await authStore.fetchUser()
  }
  await courseStore.fetchCourses({ page: 1, size: 100 })
  // 5. 使用过滤后的列表
  if (teacherCourses.value.length) {
    selectedCourseId.value = String(teacherCourses.value[0].id)
    onCourseSelect()
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
    darkObserver = {
      disconnect() {
        try { window.removeEventListener('theme:changing', onChanging) } catch {}
        try { window.removeEventListener('theme:changed', onChanged) } catch {}
      }
    }
    try { window.addEventListener('theme:changing', onChanging) } catch {}
    try { window.addEventListener('theme:changed', onChanged) } catch {}
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
