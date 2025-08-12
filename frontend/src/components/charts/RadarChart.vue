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

const buildOption = () => {
  const theme = props.theme === 'auto'
    ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
    : props.theme
  const palette = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
  return {
    title: props.title ? { text: props.title, left: 'center' } : undefined,
    legend: { bottom: 0, left: 'center' },
    tooltip: { trigger: 'item' },
    radar: {
      indicator: props.indicators,
      axisName: { color: theme === 'dark' ? '#e5e7eb' : '#374151' }
    },
    series: [
      {
        type: 'radar',
        data: props.series.map((s, i) => ({
          value: s.values,
          name: s.name,
          itemStyle: { color: s.color || palette[i % palette.length] },
          areaStyle: { opacity: 0.15 }
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
  })

  onUnmounted(() => {
    inst?.dispose()
    if (resizeHandler) {
      window.removeEventListener('resize', resizeHandler)
      resizeHandler = null
    }
  })

watch(() => [props.indicators, props.series, props.theme], () => {
  if (inst) inst.setOption(buildOption(), true)
}, { deep: true })

defineExpose({
  getDataURL: (type: 'png' | 'jpeg' = 'png') => inst?.getDataURL({ type, pixelRatio: 2, backgroundColor: '#fff' })
})
</script>

<style scoped lang="postcss">
.radar-chart-container { position: relative; }
.radar-chart { min-height: 260px; }
</style>

