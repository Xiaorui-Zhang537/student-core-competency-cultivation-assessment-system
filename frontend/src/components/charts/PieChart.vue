<template>
  <div class="pie-chart-container">
    <div 
      ref="chartRef" 
      :style="{ width: width, height: height }"
      class="pie-chart"
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
    
    <!-- 空数据状态 -->
    <div 
      v-if="!loading && (!data || data.length === 0)" 
      class="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-gray-800/80 rounded-lg"
    >
      <div class="text-center">
        <chart-pie-icon class="w-12 h-12 text-gray-400 mx-auto mb-2" />
        <p class="text-sm text-gray-600 dark:text-gray-400">暂无数据</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import { getEChartsThemedTokens, normalizeCssColor } from '@/utils/theme'
import { ChartPieIcon } from '@heroicons/vue/24/outline'

// Props
interface PieData {
  name: string
  value: number
  color?: string
  itemStyle?: any
  emphasis?: any
  blur?: any
  select?: any
}

interface Props {
  data: PieData[]
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
  radius?: string | [string, string]
  center?: [string, string]
  legend?: object
  tooltip?: object
  animation?: boolean
  backgroundColor?: string
  loading?: boolean
  showLabel?: boolean
  labelPosition?: 'inside' | 'outside'
  roseType?: boolean | 'radius' | 'area'
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '400px',
  theme: 'auto',
  radius: () => ['0%', '70%'] as [string, string],
  center: () => ['50%', '50%'] as [string, string],
  animation: true,
  backgroundColor: 'transparent',
  loading: false,
  showLabel: true,
  labelPosition: 'outside',
  roseType: false
})

// 状态
const chartRef = ref<HTMLElement>()
const chartInstance = ref<echarts.ECharts>()
let resizeObserver: ResizeObserver | null = null
let darkObserver: MutationObserver | null = null
let lastIsDark: boolean | null = null
let reRenderScheduled = false
let isUnmounted = false

// 等待容器可用且有尺寸（避免初始化过早导致 dom 被卸载/尚未布局）
const waitForContainer = async (maxTries = 10): Promise<boolean> => {
  for (let i = 0; i < maxTries; i++) {
    await nextTick()
    if (isUnmounted) return false
    if (chartRef.value && chartRef.value.offsetWidth > 0 && chartRef.value.offsetHeight > 0) return true
    await new Promise((r) => requestAnimationFrame(() => r(null)))
  }
  return !!chartRef.value && !isUnmounted
}

const ensurePalette = () => {
  const palette = getEChartsThemedTokens()?.palette
  if (Array.isArray(palette) && palette.length) {
    return palette.map(color => ensureOpaque(color))
  }
  return resolveThemePalette().map(color => ensureOpaque(color))
}

const hasData = () => Array.isArray(props.data) && props.data.length > 0

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

const applyStableStates = (item: PieData, baseColor: string): PieData => ({
  ...item,
  itemStyle: {
    color: baseColor
  },
  emphasis: { disabled: true },
  blur: { disabled: true },
  select: { disabled: true }
})

const disposeChart = () => {
  if (resizeObserver && chartRef.value) {
    try { resizeObserver.unobserve(chartRef.value) } catch {}
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance.value) {
    try { chartInstance.value.dispose() } catch {}
    chartInstance.value = undefined
  }
}

// 初始化图表
const resolveBg = () => {
  if (props.backgroundColor === 'auto') {
    // 保持玻璃卡片视觉：透明底色即可适配主题
    return 'transparent'
  }
  return props.backgroundColor as string
}

const initChart = async () => {
  const ok = await waitForContainer()
  if (!ok || !chartRef.value) return
  if (!hasData()) {
    disposeChart()
    return
  }
  
  try {
    disposeChart()
    
    await nextTick()

    // nextTick 期间可能发生路由切换导致卸载；此处必须二次校验
    if (isUnmounted || !chartRef.value) return
    
    // 创建图表实例（统一主题）
    const theme = resolveEChartsTheme()
    chartInstance.value = echarts.init(chartRef.value as HTMLDivElement, theme as any)
    const isDark = computeIsDark()
    const tokens = getEChartsThemedTokens()
    
    // 处理数据，添加默认颜色
    const palette = ensurePalette()
    const processedData = props.data.map((item, index) => {
      const rawColor = item.color || palette[index % palette.length]
      const baseColor = ensureOpaque(rawColor)
      return applyStableStates(item, baseColor)
    })
    
    // 配置选项
    const option = {
      backgroundColor: resolveBg(),
      title: props.title ? {
        text: props.title,
        left: 'center',
        top: '5%',
        textStyle: {
          color: tokens.textPrimary || (isDark ? '#f3f4f6' : '#1f2937'),
          fontSize: 16,
          fontWeight: 'bold'
        }
      } : undefined,
      
      tooltip: {
        trigger: 'item',
        triggerOn: 'mousemove',
        // 使用玻璃样式并禁用自带的阴影/边框
        backgroundColor: 'transparent',
        borderColor: 'transparent',
        textStyle: { color: 'var(--color-base-content)' },
        extraCssText: glassTooltipCss(),
        // 新增：强制 HTML 渲染与类名，避免默认黑底样式
        className: 'echarts-glass-tooltip',
        renderMode: 'html',
        // 避免 tooltip 抢焦造成闪烁
        enterable: false,
        confine: true,
        alwaysShowContent: false,
        appendToBody: false,
        transitionDuration: 0,
        showDelay: 0,
        hideDelay: 30,
        formatter: '{a} <br/>{b}: {c} ({d}%)',
        ...props.tooltip
      },
      
      legend: {
        type: 'scroll',
        orient: 'vertical',
        right: 10,
        top: 20,
        bottom: 20,
        textStyle: {
          color: tokens.textPrimary || (isDark ? '#f3f4f6' : '#1f2937')
        },
        ...props.legend
      },
      
      series: [
        {
          name: props.title || '数据分布',
          type: 'pie',
          radius: props.radius,
          center: props.center,
          avoidLabelOverlap: false,
          selectedMode: false,
          silent: false,
          hoverAnimation: false,
          selectedOffset: 0,
          data: processedData,
          roseType: props.roseType,
          emphasis: {
            focus: 'none',
            scale: false
          },
          label: {
            show: props.showLabel,
            position: props.labelPosition,
            color: tokens.textPrimary || (isDark ? '#f9fafb' : '#1f2937'),
            formatter: '{b}: {d}%'
          },
          labelLine: {
            show: props.showLabel && props.labelPosition === 'outside'
          },
          animation: !!props.animation,
          animationType: 'scale',
          animationEasing: 'linear',
          animationDelay: 0,
          animationDuration: props.animation ? 400 : 0,
          stateAnimation: { duration: 0 }
        }
      ]
    }
    
    // 设置选项
    chartInstance.value.setOption(option)
    // 初始化后隐藏 tooltip，避免容器左上角残留
    try { chartInstance.value?.dispatchAction({ type: 'hideTip' } as any) } catch {}
    
    // 响应式处理
    resizeObserver = new ResizeObserver(() => {
    chartInstance.value?.resize()
    })
    resizeObserver.observe(chartRef.value)

    // 全局移出时隐藏 tip，避免遗留
    try { (chartInstance.value as any).off && (chartInstance.value as any).off('globalout') } catch {}
    try {
      chartInstance.value?.on('globalout', () => {
        try { chartInstance.value?.dispatchAction({ type: 'hideTip' } as any) } catch {}
      })
    } catch {}
    
  } catch (err) {
    console.error('饼图初始化失败:', err)
  }
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) return
  
  if (!hasData()) {
    chartInstance.value.setOption({ series: [{ data: [] }] }, true)
    return
  }

  const palette = ensurePalette()
  const processedData = props.data.map((item, index) => {
    const rawColor = item.color || palette[index % palette.length]
    const baseColor = ensureOpaque(rawColor)
    return applyStableStates(item, baseColor)
  })
  
  const option = {
    series: [
      {
        data: processedData
      }
    ]
  }
  
  chartInstance.value.setOption(option)
}

// 主题轻量刷新（不销毁实例，只更新需要的样式与数据颜色）
const refreshForTheme = () => {
  if (!chartInstance.value) { initChart(); return }
  const tokens = getEChartsThemedTokens()
  const palette = ensurePalette()
  const processedData = props.data.map((item, index) => {
    const rawColor = item.color || palette[index % palette.length]
    const baseColor = ensureOpaque(rawColor)
    return applyStableStates(item, baseColor)
  })
  const option: any = {
    backgroundColor: resolveBg(),
    title: props.title ? { textStyle: { color: tokens.textPrimary } } : undefined,
    legend: { textStyle: { color: tokens.textPrimary } },
    series: [{ data: processedData }]
  }
  try { chartInstance.value.setOption(option, false) } catch {}
}

// 监听数据变化
watch(
  () => [props.data, props.theme],
  () => {
    if (chartInstance.value) {
      updateChart()
    }
  },
  { deep: true }
)


// 导出方法
const exportChart = (type: 'png' | 'jpeg' = 'png') => {
  if (!chartInstance.value) return null
  
  return chartInstance.value.getDataURL({
    type,
    pixelRatio: 2,
    backgroundColor: '#fff'
  })
}

const downloadChart = (filename: string = 'pie-chart', type: 'png' | 'jpeg' = 'png') => {
  const dataURL = exportChart(type)
  if (!dataURL) return
  
  const link = document.createElement('a')
  link.download = `${filename}.${type}`
  link.href = dataURL
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 高亮显示特定数据
const highlightData = (dataIndex: number) => {
  if (!chartInstance.value) return
  
  chartInstance.value.dispatchAction({
    type: 'highlight',
    seriesIndex: 0,
    dataIndex
  })
}

// 取消高亮
const downplayData = (dataIndex: number) => {
  if (!chartInstance.value) return
  
  chartInstance.value.dispatchAction({
    type: 'downplay',
    seriesIndex: 0,
    dataIndex
  })
}

// 暴露方法
defineExpose({
  exportChart,
  downloadChart,
  highlightData,
  downplayData,
  getChartInstance: () => chartInstance.value
})

// 生命周期
function scheduleReinit() {
  if (reRenderScheduled) return
  reRenderScheduled = true
  requestAnimationFrame(() => {
    reRenderScheduled = false
    refreshForTheme()
  })
}

onMounted(() => {
  initChart()
  if (!darkObserver) {
    // 监听全局主题切换，避免 MutationObserver
    const onChanging = () => { try { chartInstance.value?.dispatchAction({ type: 'hideTip' } as any) } catch {} }
    const onChanged = () => scheduleReinit()
    ;(darkObserver as any) = {
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
  isUnmounted = true
  disposeChart()
  if (darkObserver) {
    try { (darkObserver as any).disconnect?.() } catch {}
    darkObserver = null
  }
})
</script>

<style scoped lang="postcss">
.pie-chart-container {
  position: relative;
  display: flex;
  flex-direction: column;
}

.pie-chart {
  flex: 1;
  min-height: 200px;
}
/* 确保 tooltip 元素不占据布局空间 */
:deep(.echarts-glass-tooltip) { position: fixed; left: 0; top: 0; }
</style> 