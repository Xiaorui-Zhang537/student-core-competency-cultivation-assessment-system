<template>
  <div class="radar-chart-container">
    <div ref="chartRef" :style="{ width, height }" class="radar-chart"></div>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { resolveEChartsTheme, glassTooltipCss, resolveThemePalette } from '@/charts/echartsTheme'
import { getEChartsThemedTokens } from '@/utils/theme'

interface Indicator { name: string; max: number }
interface SeriesItem { name: string; values: number[]; color?: string }

interface Props {
  indicators: Indicator[]
  series: SeriesItem[] // 通常两条：学生 vs 班级
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
  // 是否将 tooltip 节点挂载到 body（在某些含有强 backdrop-filter 的容器中需关闭以避免合成层冲突）
  appendTooltipToBody?: boolean
  // 是否显示图例
  showLegend?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '380px',
  theme: 'auto',
  appendTooltipToBody: true,
  showLegend: true
})

const chartRef = ref<HTMLElement>()
let inst: echarts.ECharts | null = null
let darkObserver: { disconnect: () => void } | null = null
let lastIsDark: boolean | null = null
let reRenderScheduled = false

const buildOption = () => {
  const theme = props.theme === 'auto'
    ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
    : props.theme
  // Use transparent background to preserve glass effect behind the chart
  const bg = 'transparent'
  const tokens = getEChartsThemedTokens()
  return {
    backgroundColor: bg,
    title: props.title ? { text: props.title, left: 'center', textStyle: { color: tokens.textPrimary } } : undefined,
    legend: props.showLegend ? { bottom: 0, left: 'center', textStyle: { color: tokens.textPrimary } } : { show: false },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'transparent',
      borderColor: 'transparent',
      textStyle: { color: 'var(--color-base-content)' },
      extraCssText: glassTooltipCss(),
      className: 'echarts-glass-tooltip',
      renderMode: 'html',
      enterable: false,
      confine: true,
      alwaysShowContent: false,
      appendToBody: !!props.appendTooltipToBody,
      transitionDuration: 0,
      showDelay: 10,
      hideDelay: 60
    },
    radar: {
      indicator: props.indicators,
      axisName: { color: tokens.axisLabel },
      // 提高浅色模式下网格线可读性
      splitLine: { lineStyle: { color: tokens.splitLine } },
      // Make split areas transparent so chart does not cover glass
      splitArea: { areaStyle: { color: 'transparent' } },
      axisLine: { lineStyle: { color: tokens.axisLine } },
      animation: false
    },
    series: [
      {
        type: 'radar',
        // 增强浅色模式折线可读性（颜色交给统一主题；仅在显式传入 s.color 时覆盖）
        symbol: 'none',
        symbolSize: 0,
        emphasis: { focus: 'none', scale: false },
        data: props.series.map((s, idx) => {
          const palette = resolveThemePalette()
          const color = s.color || palette[idx % palette.length]
          const base: any = {
            value: s.values,
            name: s.name,
            lineStyle: { width: 2.5, opacity: 1, color },
            areaStyle: { opacity: theme === 'dark' ? 0.25 : 0.22, color }
          }
          base.itemStyle = { color }
          base.emphasis = {
            lineStyle: { width: 3.2, color },
            areaStyle: { opacity: theme === 'dark' ? 0.35 : 0.32, color }
          }
          return base
        })
      }
    ]
  }
}

const waitForContainer = async (maxTries = 10): Promise<boolean> => {
  for (let i = 0; i < maxTries; i++) {
    await nextTick()
    if (chartRef.value && chartRef.value.offsetWidth > 0 && chartRef.value.offsetHeight > 0) return true
    await new Promise(r => requestAnimationFrame(() => r(null)))
  }
  return !!chartRef.value
}

const render = async () => {
  const ok = await waitForContainer()
  if (!ok || !chartRef.value) return
  await nextTick()
  const theme = resolveEChartsTheme()
  if (!inst) {
    inst = echarts.init(chartRef.value as HTMLDivElement, theme as any)
  } else {
    try { inst.clear() } catch {}
  }
  inst.setOption(buildOption(), true)
  // 初始化后立即隐藏 tooltip，避免容器左上角残留
  try { inst?.dispatchAction({ type: 'hideTip' } as any) } catch {}
  // 鼠标离开画布时确保隐藏 tooltip，防止遗留触发布局抖动
  try { (inst as any).off && (inst as any).off('globalout') } catch {}
  try {
    inst.on('globalout', () => {
      try { inst?.dispatchAction({ type: 'hideTip' } as any) } catch {}
    })
  } catch {}
}

const scheduleRender = () => {
  if (reRenderScheduled) return
  reRenderScheduled = true
  requestAnimationFrame(() => {
    reRenderScheduled = false
    // 隐藏遗留的 tooltip 再重绘
    try { inst?.dispatchAction({ type: 'hideTip' } as any) } catch {}
    render()
  })
}

  let resizeHandler: (() => void) | null = null

  onMounted(() => {
    render()
    resizeHandler = () => inst?.resize()
    window.addEventListener('resize', resizeHandler)
    // 使用全局事件，避免 DOM 观察带来的抖动
    if (!darkObserver) {
      const onChanging = () => { try { inst?.dispatchAction({ type: 'hideTip' } as any) } catch {} }
      const onChanged = () => scheduleRender()
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
    inst?.dispose()
    if (resizeHandler) {
      window.removeEventListener('resize', resizeHandler)
      resizeHandler = null
    }
    if (darkObserver) { try { darkObserver.disconnect() } catch {}; darkObserver = null }
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
/* 确保 tooltip 元素不占据布局空间 */
:deep(.echarts-glass-tooltip) { position: fixed; left: 0; top: 0; }
</style>

