<template>
  <div class="trend-area-chart-container">
    <div ref="chartRef" :style="{ width, height }" class="trend-area-chart"></div>

    <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-white/60 dark:bg-gray-900/40 rounded-lg">
      <div class="text-center">
        <div class="w-8 h-8 border-2 border-primary-600 border-t-transparent rounded-full animate-spin mb-2"></div>
        <p class="text-sm text-gray-600 dark:text-gray-400">加载中...</p>
      </div>
    </div>

    <div v-if="!loading && isEmpty" class="absolute inset-0 flex items-center justify-center bg-white/40 dark:bg-gray-900/20 rounded-lg">
      <div class="text-center text-sm text-gray-500 dark:text-gray-400">暂无数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss } from '@/charts/echartsTheme'

type Point = { x: string | number; y: number }
type SeriesInput = { name: string; data: Point[]; color?: string }

interface Props {
  series: SeriesInput[]
  xAxisData?: Array<string | number>
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
  animation?: boolean
  grid?: object
  legend?: object
  tooltip?: object
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  series: () => [],
  xAxisData: undefined,
  width: '100%',
  height: '280px',
  theme: 'auto',
  animation: true,
  loading: false
})

const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null
let ro: ResizeObserver | null = null

const isEmpty = computed(() => !props.series || props.series.length === 0 || props.series.every(s => !s || !Array.isArray(s.data) || s.data.length === 0))

const waitForContainer = async (maxTries = 10): Promise<boolean> => {
  for (let i = 0; i < maxTries; i++) {
    await nextTick()
    if (chartRef.value && chartRef.value.offsetWidth > 0 && chartRef.value.offsetHeight > 0) return true
    await new Promise(r => requestAnimationFrame(() => r(null)))
  }
  return !!chartRef.value
}

const buildOption = (): any => {
  const theme = props.theme === 'auto' ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light') : props.theme

  // 统一调色板，和雷达/饼图保持一致风格
  const palette = theme === 'dark'
    ? ['#93c5fd', '#22d3ee', '#f472b6', '#f59e0b', '#34d399']
    : ['#3b82f6', '#06b6d4', '#ec4899', '#f59e0b', '#10b981']

  const categories = Array.isArray(props.xAxisData) && props.xAxisData.length
    ? props.xAxisData
    : (props.series?.[0]?.data || []).map(p => (p?.x ?? ''))

  const normalized = (props.series || []).filter(s => s && Array.isArray(s.data)).map((s, idx) => {
    const base = s.color || palette[idx % palette.length]
    return {
      name: s.name,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: theme === 'dark' ? 5 : 4,
      data: s.data.map(p => Number((p && (p as any).y) || 0)),
      itemStyle: { color: base },
      lineStyle: { color: base, width: theme === 'dark' ? 3 : 2 },
      areaStyle: {
        color: new (echarts as any).graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: base + (theme === 'dark' ? '40' : '30') },
          { offset: 1, color: base + (theme === 'dark' ? '10' : '05') }
        ])
      }
    }
  })

  const option: any = {
    backgroundColor: 'transparent',
    title: props.title ? {
      text: props.title,
      left: 'center',
      textStyle: {
        color: theme === 'dark' ? '#ffffff' : '#111827',
        fontWeight: 'bold',
        fontSize: 16
      }
    } : undefined,
    grid: {
      left: '3%', right: '4%', bottom: '3%', containLabel: true,
      ...props.grid
    },
    legend: {
      top: 'bottom',
      textStyle: { color: theme === 'dark' ? '#e5e7eb' : '#374151' },
      ...props.legend
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      textStyle: { color: 'var(--color-base-content)' },
      extraCssText: glassTooltipCss(),
      ...props.tooltip
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: Array.isArray(categories) ? categories.map(c => String(c)) : [],
      axisLine: { lineStyle: { color: theme === 'dark' ? '#4b5563' : '#d1d5db' } },
      axisLabel: { color: theme === 'dark' ? '#9ca3af' : '#6b7280' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: theme === 'dark' ? '#6b7280' : '#d1d5db' } },
      axisLabel: { color: theme === 'dark' ? '#9ca3af' : '#6b7280' },
      splitLine: { lineStyle: { color: theme === 'dark' ? '#4b5563' : '#e5e7eb' } }
    },
    series: normalized
  }

  if (normalized.length === 0) {
    option.series = []
  }
  return option
}

const init = async () => {
  const ok = await waitForContainer()
  if (!ok || !chartRef.value) return
  try {
    if (ro) { try { ro.disconnect() } catch {} ro = null }
    if (chart) { try { chart.dispose() } catch {} chart = null }
    await nextTick()
    const theme = resolveEChartsTheme()
    chart = echarts.init(chartRef.value as HTMLDivElement, theme as any)
    chart.clear()
    chart.setOption(buildOption(), true)
    ro = new ResizeObserver(() => {
      if (!chart) return
      requestAnimationFrame(() => { if (chart) { try { chart.resize() } catch {} } })
    })
    ro.observe(chartRef.value)
  } catch (e) {
    // eslint-disable-next-line no-console
    console.error('趋势图初始化失败:', e)
  }
}

const update = () => {
  if (!chart) return
  chart.clear()
  chart.setOption(buildOption(), true)
}

watch(() => [props.series, props.xAxisData, props.theme], () => {
  if (chart) update()
}, { deep: true })

watch(() => document.documentElement.classList.contains('dark'), () => {
  if (props.theme === 'auto') init()
})

onMounted(init)
onUnmounted(() => {
  if (ro) { try { ro.disconnect() } catch {} ro = null }
  if (chart) { try { chart.dispose() } catch {} chart = null }
})

defineExpose({ getChartInstance: () => chart })
</script>

<style scoped lang="postcss">
.trend-area-chart-container { position: relative; display: flex; flex-direction: column; }
.trend-area-chart { flex: 1; min-height: 200px; }
</style>


