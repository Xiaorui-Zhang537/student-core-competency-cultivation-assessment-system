import * as echarts from 'echarts'
import { getEChartsThemedTokens, normalizeCssColor } from '@/utils/theme'

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
  // 浅色主题（含 retro）下提高不透明度，避免 tooltip “看不见”
  const bgAlpha = isDark ? 0.22 : 0.32
  const blur = isDark ? 8 : 10
  // 同步一个 class 名以避免 ECharts 内部拉取默认 tooltip 样式（黑底）
  return [
    'class:echarts-glass-tooltip;',
    'z-index: 9999 !important;',
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
  // 0) 最稳：直接从当前主题语义 CSS 变量读取（值可能是 oklch，但会被 normalize 为 rgba）
  try {
    const direct = readThemeSemanticPalette()
    if (direct.length) return direct
  } catch {}

  // 1) 次稳：使用主题语义色生成的 palette（始终跟随主题；避免主题切换时因 --chart-palette 无效而回退为白色）
  try {
    const tokensPalette = (getEChartsThemedTokens()?.palette || [])
      .map(normalizeCssColor)
      .filter(Boolean)
      .filter(c => /^#|^rgb/i.test(c))
    if (tokensPalette.length) return tokensPalette
  } catch {}

  // 2) 再次选：使用 CSS 中显式声明的 --chart-palette（若存在）
  try {
    const cssPalette = getComputedStyle(document.documentElement)
      .getPropertyValue('--chart-palette')
      .trim()
    if (cssPalette) {
      const arr = cssPalette
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
        .map(normalizeCssColor)
        .filter(Boolean)
        .filter(c => /^#|^rgb/i.test(c))
      if (arr.length) return arr
    }
  } catch {}

  // 3) 最后兜底：固定 palette
  const fallback = ['#3b82f6','#06b6d4','#f59e0b','#10b981','#ef4444','#8b5cf6','#84cc16','#f97316','#ec4899','#6366f1']
    .map(normalizeCssColor)
  return fallback
}

function readThemeSemanticPalette(): string[] {
  const rs = getComputedStyle(document.documentElement)
  const vars = [
    '--color-primary',
    '--color-accent',
    '--color-secondary',
    '--color-info',
    '--color-success',
    '--color-warning',
    '--color-error',
    '--color-neutral',
  ]
  const arr = vars
    .map((v) => rs.getPropertyValue(v).trim())
    .filter(Boolean)
    .map(normalizeCssColor)
    .filter(Boolean)
    .filter(c => /^#|^rgb/i.test(c))

  // 扩展几种辅助色，保证分类数较多时也有足够颜色
  const extended = [
    'oklch(76% 0.244 18)',
    'oklch(82% 0.195 62)',
    'oklch(72% 0.188 135)',
    'oklch(66% 0.162 250)'
  ].map(normalizeCssColor).filter(c => /^#|^rgb/i.test(c))

  return arr.concat(extended)
}
