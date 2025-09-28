import * as echarts from 'echarts'
import { getEChartsThemedTokens } from '@/utils/theme'

/**
 * 生成与当前 CSS Variables 同步的 ECharts 主题对象
 * - 动态读取 CSS 变量，适配浅/深主题
 * - 统一调色盘、文字、坐标轴、网格线、tooltip 样式
 */
export function resolveEChartsTheme() {
  const t = getEChartsThemedTokens()
  const colorPalette = (Array.isArray(t.palette) && t.palette.length)
    ? t.palette.slice()
    : resolveThemePalette()

  const baseText = String(t.textPrimary || '#111827')
  const axisLine = String(t.axisLine || 'rgba(0,0,0,0.35)')
  const axisLabel = String(t.axisLabel || 'rgba(0,0,0,0.62)')
  const splitLine = String(t.splitLine || 'rgba(0,0,0,0.22)')

  const theme: echarts.EChartsCoreOption = {
    backgroundColor: 'transparent',
    color: colorPalette,
    textStyle: { color: baseText },
    axisPointer: {
      lineStyle: { color: axisLine },
      crossStyle: { color: axisLine },
      label: { color: baseText, backgroundColor: t.tooltipBg || 'rgba(0,0,0,0.8)', borderColor: t.tooltipBorder || 'rgba(0,0,0,0.18)' }
    },
    categoryAxis: {
      axisLine: { lineStyle: { color: axisLine } },
      axisLabel: { color: axisLabel },
      splitLine: { show: false }
    },
    valueAxis: {
      axisLine: { lineStyle: { color: axisLine } },
      axisLabel: { color: axisLabel },
      splitLine: { lineStyle: { color: splitLine } }
    },
    legend: { textStyle: { color: baseText } },
    tooltip: {
      backgroundColor: t.tooltipBg || '#ffffff',
      borderColor: t.tooltipBorder || 'rgba(0,0,0,0.18)',
      textStyle: { color: baseText }
    }
  } as any

  return theme
}

/**
 * 统一的液态玻璃 Tooltip 样式（使用 CSS Variables 与玻璃 tokens）
 */
export function glassTooltipCss(): string {
  const isDark = document.documentElement.classList.contains('dark')
  const bgAlpha = isDark ? 0.20 : 0.22
  const blur = isDark ? 8 : 10
  // 同步一个 class 名以避免 ECharts 内部拉取默认 tooltip 样式（黑底）
  return [
    'class:echarts-glass-tooltip;',
    `background: rgb(var(--glass-bg-rgb) / ${bgAlpha}) !important;`,
    'border: 1px solid var(--glass-border-color) !important;',
    `backdrop-filter: blur(${blur}px) saturate(1.5) !important;`,
    `-webkit-backdrop-filter: blur(${blur}px) saturate(1.5) !important;`,
    'border-radius: 12px !important;',
    'box-shadow: var(--glass-inner-shadow), var(--glass-outer-shadow) !important;',
    'color: var(--color-base-content) !important;',
    'padding: 8px 10px !important;',
    'pointer-events: none !important;',
    'will-change: transform, filter, backdrop-filter !important;'
  ].join(' ')
}

// 导出主题色盘，便于各图表在浅/深主题下获得多彩配色
export function resolveThemePalette(): string[] {
  try {
    const cssPalette = getComputedStyle(document.documentElement)
      .getPropertyValue('--chart-palette')
      .trim()
    if (cssPalette) {
      const arr = cssPalette.split(',').map(s => s.trim()).filter(Boolean)
      if (arr.length) return arr
    }
  } catch {}
  return ['#3b82f6','#06b6d4','#f59e0b','#10b981','#ef4444','#8b5cf6','#84cc16','#f97316','#ec4899','#6366f1']
}


