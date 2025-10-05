<template>
  <div class="gauge-chart-container">
    <div ref="chartRef" :style="{ width, height }" class="gauge-chart"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme } from '@/charts/echartsTheme'
import { getThemeCoreColors, rgba } from '@/utils/theme'

interface Props {
  value: number
  title?: string
  width?: string
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  value: 0,
  width: '100%',
  height: '260px'
})

const chartRef = ref<HTMLElement>()
let inst: echarts.ECharts | null = null
let resizeObserver: ResizeObserver | null = null
let visibilityObserver: IntersectionObserver | null = null

const waitForContainer = async (maxTries = 20): Promise<boolean> => {
  for (let i = 0; i < maxTries; i++) {
    await nextTick()
    if (chartRef.value && chartRef.value.offsetWidth > 0 && chartRef.value.offsetHeight > 0) return true
    await new Promise(r => requestAnimationFrame(() => r(null)))
  }
  return !!chartRef.value
}

const buildOption = (valNum: number): echarts.EChartsCoreOption => {
  const theme = document.documentElement.classList.contains('dark') ? 'dark' : 'light'
  const baseColors = getThemeCoreColors()
  const base = baseColors.primary
  const isDark = document.documentElement.classList.contains('dark')
  const value = Math.max(0, Math.min(100, Number(valNum || 0)))
  const option: echarts.EChartsCoreOption = {
    backgroundColor: 'transparent',
    series: [
      { type: 'gauge', startAngle: 200, endAngle: -20, min: 0, max: 100,
        axisLine: { lineStyle: { width: 14, color: [[1, rgba(baseColors.baseContent, isDark ? 0.24 : 0.10)]] } },
        pointer: { show: false }, axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false }, detail: { show: false } },
      { type: 'gauge', startAngle: 200, endAngle: -20, min: 0, max: 100,
        progress: { show: true, width: 14, roundCap: true, itemStyle: { color: new (echarts as any).graphic.LinearGradient(0, 0, 1, 0, [ { offset: 0, color: rgba(base, 0.6) }, { offset: 1, color: rgba(base, 1) } ]), shadowColor: rgba(base, 0.4), shadowBlur: 6 } },
        axisLine: { lineStyle: { width: 14, color: [[1, 'transparent']] } },
        pointer: { show: false }, axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
        detail: { valueAnimation: true, fontSize: 40, fontWeight: 700, color: rgba(base, 1), offsetCenter: [0, '-4%'], formatter: (val: number) => `${Math.round(val)}` },
        data: [{ value }] },
      { type: 'gauge', startAngle: 200, endAngle: -20, min: 0, max: 100,
        axisLine: { lineStyle: { width: 14, color: [[1, 'transparent']] } },
        pointer: { show: false }, axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false },
        detail: { show: true, valueAnimation: false, fontSize: 12, color: isDark ? '#9ca3af' : '#6b7280', offsetCenter: [0, '34%'], formatter: () => String(props.title || '') },
        data: [{ value: 0 }] }
    ]
  }
  return option
}

const init = async () => {
  const ok = await waitForContainer()
  if (!ok || !chartRef.value) return
  const theme = resolveEChartsTheme()
  if (!inst) {
    inst = echarts.init(chartRef.value as HTMLDivElement, theme as any)
  }
  try { inst.setOption(buildOption(props.value), true) } catch {
    try { inst?.dispose(); inst = echarts.init(chartRef.value as HTMLDivElement, theme as any); inst.setOption(buildOption(props.value), true) } catch {}
  }
  requestAnimationFrame(() => { try { inst?.resize() } catch {} })
}

const update = () => {
  if (!inst) return
  try { inst.setOption(buildOption(props.value), true) } catch {}
}

onMounted(() => {
  init()
  resizeObserver = new ResizeObserver(() => { requestAnimationFrame(() => { try { inst?.resize() } catch {} }) })
  if (chartRef.value) resizeObserver.observe(chartRef.value)
  visibilityObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (entry.isIntersecting && entry.target === chartRef.value) {
        if (!inst) init(); else requestAnimationFrame(() => { try { inst?.resize() } catch {} })
      }
    }
  }, { root: null, threshold: 0.01 })
  if (chartRef.value) visibilityObserver.observe(chartRef.value)
  // 兜底：延时再刷新一次
  setTimeout(() => { try { if (!inst) init(); else update() } catch {} }, 300)
})

watch(() => props.value, () => { update() })

onUnmounted(() => {
  try { inst?.dispose() } catch {}
  try { if (resizeObserver && chartRef.value) resizeObserver.unobserve(chartRef.value) } catch {}
  try { resizeObserver?.disconnect(); resizeObserver = null } catch {}
  try { visibilityObserver?.disconnect(); visibilityObserver = null } catch {}
})
</script>

<style scoped>
.gauge-chart-container { position: relative; display: flex; flex-direction: column; }
.gauge-chart { flex: 1; min-height: 200px; }
</style>


