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

const buildOption = () => {
  const theme = props.theme === 'auto'
    ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
    : props.theme
  const palette = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
  const bg = theme === 'dark' ? '#0b0f1a' : '#ffffff'
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
      splitLine: { lineStyle: { color: theme === 'dark' ? ['#4b5563'] : ['#e5e7eb'] } },
      splitArea: { areaStyle: { color: theme === 'dark' ? ['#111827', '#0f172a'] : ['#ffffff', '#f9fafb'] } },
      axisLine: { lineStyle: { color: theme === 'dark' ? '#6b7280' : '#d1d5db' } }
    },
    series: [
      {
        type: 'radar',
        lineStyle: { width: theme === 'dark' ? 3 : 2 },
        data: props.series.map((s, i) => ({
          value: s.values,
          name: s.name,
          itemStyle: { color: s.color || palette[i % palette.length] },
          areaStyle: { opacity: theme === 'dark' ? 0.25 : 0.15 }
        }))
      }
    ]
  }
}

const render = async () => {
  if (!chartRef.value) return
  if (inst) inst.dispose()
  await nextTick()
  const theme = props.theme === 'auto'
    ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
    : props.theme
  inst = echarts.init(chartRef.value, theme)
  inst.setOption(buildOption())
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
          render()
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
    // 主题或数据变化，重新初始化以应用 ECharts 主题
    render()
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

