<template>
  <div class="line-chart-container">
    <div 
      ref="chartRef" 
      :style="{ width: width, height: height }"
      class="line-chart"
    ></div>
    
    <!-- 加载状态 -->
    <div 
      v-if="loading" 
      class="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-gray-800/80 rounded-lg"
    >
      <div class="text-center">
        <div class="w-8 h-8 border-2 border-primary-600 border-t-transparent rounded-full animate-spin mb-2"></div>
        <p class="text-sm text-gray-600 dark:text-gray-400">加载中...</p>
      </div>
    </div>
    
    <!-- 错误状态 -->
    <div 
      v-if="error" 
      class="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-gray-800/80 rounded-lg"
    >
      <div class="text-center">
        <exclamation-triangle-icon class="w-12 h-12 text-red-500 mx-auto mb-2" />
        <p class="text-sm text-gray-600 dark:text-gray-400">图表加载失败</p>
        <button 
          @click="initChart" 
          class="mt-2 text-xs text-primary-600 hover:text-primary-700"
        >
          重试
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import { getEChartsThemedTokens, normalizeCssColor } from '@/utils/theme'

// Props
interface ChartData {
  name: string
  data: number[]
  color?: string
  type?: 'line' | 'bar'
  smooth?: boolean
}

interface Props {
  data: ChartData[]
  xAxisData: string[]
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
  grid?: object
  legend?: object
  tooltip?: object
  animation?: boolean
  backgroundColor?: string
  loading?: boolean
  empty?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  xAxisData: () => [],
  width: '100%',
  height: '400px',
  theme: 'auto',
  animation: true,
  backgroundColor: 'transparent',
  loading: false,
  empty: false
})

// 状态
const chartRef = ref<HTMLElement>()
const chartInstance = ref<echarts.ECharts>()
const error = ref(false)
let resizeObserver: ResizeObserver | null = null

const ensurePalette = () => {
  const palette = getEChartsThemedTokens().palette
  // 若主题返回的颜色不足或存在透明色，则回退到标准色盘
  if (Array.isArray(palette) && palette.length) {
    return palette.map(color => ensureOpaque(color))
  }
  return resolveThemePalette().map(color => ensureOpaque(color))
}

const computeIsDark = () => {
  if (props.theme === 'auto') {
    return document.documentElement.classList.contains('dark')
  }
  return props.theme === 'dark'
}

interface RgbaColor { r: number; g: number; b: number; a: number }

const clamp = (value: number, min: number, max: number) => Math.max(min, Math.min(max, value))

const parseRgba = (color: string): RgbaColor | null => {
  const normalized = normalizeCssColor(color)
  const match = normalized.match(/rgba?\(([^)]+)\)/i)
  if (!match) return null
  const parts = match[1].split(',').map(p => Number(p.trim()))
  if (parts.length < 3 || parts.some(n => Number.isNaN(n))) return null
  const [r, g, b, a = 1] = parts
  return { r, g, b, a }
}

const toRgbaString = ({ r, g, b, a }: RgbaColor) => `rgba(${Math.round(clamp(r, 0, 255))}, ${Math.round(clamp(g, 0, 255))}, ${Math.round(clamp(b, 0, 255))}, ${clamp(a, 0, 1)})`

const ensureOpaque = (color: string, floor = 1) => {
  const parsed = parseRgba(color)
  if (!parsed) return color
  return toRgbaString({ ...parsed, a: Math.max(parsed.a, floor) })
}

const darkenColor = (color: string, ratio = 0.18) => {
  const parsed = parseRgba(color)
  if (!parsed) return color
  const factor = clamp(1 - Math.abs(ratio), 0, 1)
  return toRgbaString({
    r: parsed.r * factor,
    g: parsed.g * factor,
    b: parsed.b * factor,
    a: parsed.a
  })
}

const safeAlpha = (color: string, alpha: number) => {
  const parsed = parseRgba(color)
  if (!parsed) return color
  return toRgbaString({ ...parsed, a: clamp(alpha, 0, 1) })
}

const normalizeSeries = () => (props.data || []).filter((s: any) => s && Array.isArray(s.data))

const buildSeries = (isDark: boolean, palette: string[]) => {
  const seriesList = normalizeSeries()
  return seriesList.map((item: ChartData, idx: number) => {
    const type = item.type || 'line'
    const isBar = type === 'bar'
    const rawColor = item.color || palette[idx % palette.length]
    const baseColor = ensureOpaque(rawColor)
    const darkened = ensureOpaque(darkenColor(baseColor, 0.22))

    const base: any = {
      name: item.name,
      type: type as 'line' | 'bar',
      data: Array.isArray(item.data) ? item.data : [],
      animationDuration: props.animation ? 900 : 0,
      itemStyle: { color: baseColor, opacity: 1 },
      hoverAnimation: false,
      stateAnimation: { duration: 0 },
      emphasis: { disabled: true },
      blur: { disabled: true },
      select: { disabled: true }
    }

    if (isBar) {
      base.barMaxWidth = 32
      base.itemStyle.borderRadius = [6, 6, 6, 6]
    } else {
      base.smooth = item.smooth !== false
      base.symbol = 'circle'
      base.symbolSize = isDark ? 5 : 4
      base.lineStyle = {
        color: baseColor,
        width: isDark ? 3 : 2
      }
      const areaAlpha = isDark ? 0.28 : 0.2
      const topAlpha = Math.min(areaAlpha + 0.08, 1)
      const bottomAlpha = Math.min(areaAlpha * 0.36, 1)
      base.areaStyle = {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: safeAlpha(baseColor, topAlpha) },
          { offset: 1, color: safeAlpha(baseColor, bottomAlpha) }
        ] }
      }
      base.emphasis = { disabled: true }
      base.blur = { disabled: true }
      base.select = { disabled: true }
    }

    return base
  })
}

const disposeChart = () => {
  if (resizeObserver && chartRef.value) {
    try { resizeObserver.unobserve(chartRef.value) } catch (err) {}
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance.value) {
    try { chartInstance.value.dispose() } catch (err) {}
    chartInstance.value = undefined
  }
}

// 等待容器可用且有尺寸
const waitForContainer = async (maxTries = 10): Promise<boolean> => {
  for (let i = 0; i < maxTries; i++) {
    await nextTick()
    if (chartRef.value && chartRef.value.offsetWidth > 0 && chartRef.value.offsetHeight > 0) return true
    await new Promise(r => requestAnimationFrame(() => r(null)))
  }
  return !!chartRef.value
}

// 初始化图表
const initChart = async () => {
  const ok = await waitForContainer()
  if (!ok || !chartRef.value) return
  
  try {
    error.value = false
    
    disposeChart()
    
    await nextTick()
    
    // 创建图表实例（统一主题）
    const theme = resolveEChartsTheme()
    chartInstance.value = echarts.init(chartRef.value as HTMLDivElement, theme as any)

    const isDark = computeIsDark()
    const palette = ensurePalette()
    const tokens = getEChartsThemedTokens()
    const normalizedSeries = normalizeSeries()
    const hasBar = normalizedSeries.some(s => (s.type || 'line') === 'bar')

    const option: any = {
      backgroundColor: props.backgroundColor,
      title: props.title ? {
        text: props.title,
        left: 'center',
        textStyle: {
          color: tokens.textPrimary || (isDark ? '#f3f4f6' : '#1f2937'),
          fontSize: 16,
          fontWeight: 'bold'
        }
      } : undefined,
      
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
        ...props.grid
      },
      
      legend: {
        top: 'bottom',
        textStyle: {
          color: tokens.textPrimary || (isDark ? '#f3f4f6' : '#1f2937')
        },
        ...props.legend
      },
      
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'transparent',
        borderColor: 'transparent',
        textStyle: { color: 'var(--color-base-content)' },
        extraCssText: glassTooltipCss(),
        className: 'echarts-glass-tooltip',
        renderMode: 'html',
        enterable: false,
        confine: true,
        appendToBody: true,
        transitionDuration: 0,
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: safeAlpha(tokens.textPrimary || (isDark ? '#f9fafb' : '#111827'), isDark ? 0.16 : 0.12),
            color: tokens.textPrimary || (isDark ? '#f9fafb' : '#111827')
          },
          animation: false
        },
        ...props.tooltip
      },
      
      xAxis: {
        type: 'category',
        boundaryGap: hasBar ? true : false,
        data: Array.isArray(props.xAxisData) ? props.xAxisData : [],
        axisLine: {
          lineStyle: {
            color: tokens.axisLine
          }
        },
        axisLabel: {
          color: tokens.axisLabel
        }
      },
      
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: tokens.axisLine
          }
        },
        axisLabel: {
          color: tokens.axisLabel
        },
        splitLine: {
          lineStyle: {
            color: tokens.splitLine
          }
        }
      },
      
      series: buildSeries(isDark, palette)
    }
    if (normalizedSeries.length === 0 || !Array.isArray(props.xAxisData)) {
      option.series = []
      option.xAxis.data = []
    }
    
    // 设置选项（完全替换以避免保留无效旧系列）
    chartInstance.value.setOption(option, true)
    
    // 响应式处理
    resizeObserver = new ResizeObserver(() => {
      if (!chartInstance.value) return
      requestAnimationFrame(() => chartInstance.value?.resize())
    })
    resizeObserver.observe(chartRef.value)
    
  } catch (err) {
    console.error('图表初始化失败:', err)
    error.value = true
  }
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) {
    initChart()
    return
  }

  const palette = ensurePalette()
  const normalizedSeries = normalizeSeries()
  const hasBar = normalizedSeries.some(s => (s.type || 'line') === 'bar')
  const isDark = computeIsDark()

  const option: any = {
    xAxis: {
      data: Array.isArray(props.xAxisData) ? props.xAxisData : [],
      boundaryGap: hasBar ? true : false
    },
    series: buildSeries(isDark, palette)
  }
  if (normalizedSeries.length === 0) {
    option.series = []
  }

  chartInstance.value.setOption(option, true)
}

// 监听数据变化
watch(
  () => [props.data, props.xAxisData, props.theme],
  () => {
    if (chartInstance.value) {
      updateChart()
    }
  },
  { deep: true }
)

// 监听主题变化
watch(
  () => document.documentElement.classList.contains('dark'),
  () => {
    if (props.theme === 'auto') {
      initChart()
    }
  }
)

// 导出方法
const exportChart = (type: 'png' | 'jpeg' = 'png') => {
  if (!chartInstance.value) return null
  const isDark = document.documentElement.classList.contains('dark')
  const bg = isDark ? '#0b0f1a' : '#ffffff'
  return chartInstance.value.getDataURL({ type, pixelRatio: 2, backgroundColor: bg })
}

const downloadChart = (filename: string = 'chart', type: 'png' | 'jpeg' = 'png') => {
  const dataURL = exportChart(type)
  if (!dataURL) return
  
  const link = document.createElement('a')
  link.download = `${filename}.${type}`
  link.href = dataURL
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 暴露方法
defineExpose({
  exportChart,
  downloadChart,
  getChartInstance: () => chartInstance.value
})

// 生命周期
onMounted(() => {
  initChart()
})

onUnmounted(() => {
  disposeChart()
})
</script>

<style scoped lang="postcss">
.line-chart-container {
  position: relative;
  display: flex;
  flex-direction: column;
}

.line-chart {
  flex: 1;
  min-height: 200px;
}
</style> 