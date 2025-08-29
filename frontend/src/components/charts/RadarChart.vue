<template>
  <div class="radar-chart-container">
    <div ref="chartRef" :style="{ width, height }" class="radar-chart"></div>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

interface Indicator { name: string; max: number }
interface SeriesItem { name: string; values: number[]; color?: string }

interface Props {
  indicators: Indicator[]
  series: SeriesItem[] // 通常两条：学生 vs 班级
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '380px',
  theme: 'auto'
})

const chartRef = ref<HTMLElement>()
let inst: echarts.ECharts | null = null
let darkObserver: MutationObserver | null = null
let reRenderScheduled = false

const buildOption = () => {
  const theme = props.theme === 'auto'
    ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
    : props.theme
  const palette = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
  // Use transparent background to preserve glass effect behind the chart
  const bg = 'transparent'
  return {
    backgroundColor: bg,
    title: props.title ? { text: props.title, left: 'center', textStyle: { color: theme === 'dark' ? '#e5e7eb' : '#111827' } } : undefined,
    legend: { bottom: 0, left: 'center', textStyle: { color: theme === 'dark' ? '#e5e7eb' : '#374151' } },
    tooltip: {
      trigger: 'item',
      backgroundColor: theme === 'dark' ? '#374151' : '#ffffff',
      borderColor: theme === 'dark' ? '#4b5563' : '#e5e7eb',
      textStyle: { color: theme === 'dark' ? '#ffffff' : '#111827' }
    },
    radar: {
      indicator: props.indicators,
      axisName: { color: theme === 'dark' ? '#e5e7eb' : '#374151' },
      // 提高浅色模式下网格线可读性
      splitLine: { lineStyle: { color: theme === 'dark' ? ['#4b5563'] : ['#cbd5e1'] } },
      // Make split areas transparent so chart does not cover glass
      splitArea: { areaStyle: { color: 'transparent' } },
      axisLine: { lineStyle: { color: theme === 'dark' ? '#6b7280' : '#d1d5db' } }
    },
    series: [
      {
        type: 'radar',
        // 增强浅色模式折线可读性
        lineStyle: { width: 3, opacity: 1 },
        symbol: 'circle',
        symbolSize: 3,
        data: props.series.map((s, i) => ({
          value: s.values,
          name: s.name,
          itemStyle: { color: s.color || palette[i % palette.length] },
          lineStyle: { color: s.color || palette[i % palette.length], width: 3, opacity: 1 },
          areaStyle: { opacity: theme === 'dark' ? 0.25 : 0.22 }
        }))
      }
    ]
  }
}

const render = async () => {
  if (!chartRef.value) return
  // 确保无论内部引用或 DOM 上缓存的实例都被正确释放
  try { inst?.dispose() } catch {}
  const existing = echarts.getInstanceByDom(chartRef.value)
  if (existing) {
    try { existing.dispose() } catch {}
  }
  await nextTick()
  const theme = props.theme === 'auto'
    ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
    : props.theme
  inst = echarts.init(chartRef.value, theme)
  inst.setOption(buildOption())
}

const scheduleRender = () => {
  if (reRenderScheduled) return
  reRenderScheduled = true
  window.setTimeout(() => {
    reRenderScheduled = false
    render()
  }, 60)
}

  let resizeHandler: (() => void) | null = null

  onMounted(() => {
    render()
    resizeHandler = () => inst?.resize()
    window.addEventListener('resize', resizeHandler)
    // 监听根元素的 class 变化以捕获深浅色切换
    if (!darkObserver) {
      darkObserver = new MutationObserver(() => {
        if (props.theme === 'auto') {
          // 在主题切换动画期间可能会触发多次 class 变更，这里合并重渲染
          scheduleRender()
        }
      })
      darkObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
    }
  })

  onUnmounted(() => {
    inst?.dispose()
    if (resizeHandler) {
      window.removeEventListener('resize', resizeHandler)
      resizeHandler = null
    }
    if (darkObserver) {
      darkObserver.disconnect()
      darkObserver = null
    }
  })

watch(
  () => [props.indicators, props.series, props.theme],
  () => {
    // 主题或数据变化，重新初始化以应用 ECharts 主题（合并频繁更新）
    scheduleRender()
  },
  { deep: true }
)

defineExpose({
  getDataURL: (type: 'png' | 'jpeg' = 'png') => {
    const isDark = document.documentElement.classList.contains('dark')
    const bg = isDark ? '#0b0f1a' : '#ffffff'
    return inst?.getDataURL({ type, pixelRatio: 2, backgroundColor: bg })
  }
})
</script>

<style scoped lang="postcss">
.radar-chart-container { position: relative; }
.radar-chart { min-height: 260px; }
</style>

