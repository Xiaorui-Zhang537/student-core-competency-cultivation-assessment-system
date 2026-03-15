// Utilities to resolve current theme colors from CSS variables (daisyUI theme)

type CssVarName = '--color-primary' | '--color-secondary' | '--color-accent' | '--color-info' | '--color-success' | '--color-warning' | '--color-error' | '--color-neutral' | '--color-base-100' | '--color-base-200' | '--color-base-300' | '--color-base-content'

export function resolveCssVarColor(variable: CssVarName): string {
  const el = document.createElement('span')
  el.style.color = `var(${variable})`
  el.style.display = 'none'
  document.body.appendChild(el)
  const color = getComputedStyle(el).color || 'rgb(0, 0, 0)'
  try { document.body.removeChild(el) } catch {}
  return normalizeCssColor(color)
}

export function parseRgb(rgb: string): { r: number; g: number; b: number } {
  // Accept rgb/rgba strings
  // Support both comma-separated and space-separated CSS Color 4 formats:
  // - rgb(12, 34, 56)
  // - rgb(12 34 56)
  // - rgba(12 34 56 / 0.5)
  const m = rgb.match(/rgba?\(\s*(\d+)\s*(?:,|\s)\s*(\d+)\s*(?:,|\s)\s*(\d+)/i)
  if (!m) return { r: 0, g: 0, b: 0 }
  return { r: Number(m[1]), g: Number(m[2]), b: Number(m[3]) }
}

export function rgba(rgb: string, alpha: number): string {
  const { r, g, b } = parseRgb(rgb)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

let colorProbe: HTMLSpanElement | null = null

export function normalizeCssColor(color: string): string {
  if (!color || typeof window === 'undefined') return color
  // 兼容 oklch(0.8 0.114 19.57) 或 oklch(80% 0.114 19.57) 两种写法
  const oklchMatch = color.trim().match(/^oklch\(\s*([\d.]+)(%?)\s+([\d.]+)\s+([\d.]+)(?:deg)?(?:\s*\/\s*([\d.]+))?\s*\)$/i)
  if (oklchMatch) {
    const [, lRaw, lUnit, cStr, hStr, alphaStr] = oklchMatch as any
    const lNum = Number(lRaw)
    const l = lUnit === '%' ? (lNum / 100) : lNum
    const c = Number(cStr)
    const h = Number(hStr)
    const alpha = alphaStr !== undefined ? Number(alphaStr) : 1
    return oklchToRgba(l, c, h, alpha)
  }
  try {
    if (!colorProbe) {
      colorProbe = document.createElement('span')
      colorProbe.style.display = 'none'
      document.body.appendChild(colorProbe)
    }
    colorProbe.style.color = ''
    colorProbe.style.color = color
    const computed = getComputedStyle(colorProbe).color
    if (computed && computed !== 'rgba(0, 0, 0, 0)') {
      return computed
    }
  } catch {}
  return color
}

const srgbFromLinear = (c: number) => {
  const clamped = Math.max(0, Math.min(1, c))
  return clamped <= 0.0031308 ? clamped * 12.92 : 1.055 * Math.pow(clamped, 1 / 2.4) - 0.055
}

const oklabToLinearRgb = (l: number, a: number, b: number) => {
  const l_ = l + 0.3963377774 * a + 0.2158037573 * b
  const m_ = l - 0.1055613458 * a - 0.0638541728 * b
  const s_ = l - 0.0894841775 * a - 1.2914855480 * b

  const l3 = l_ ** 3
  const m3 = m_ ** 3
  const s3 = s_ ** 3

  const r = 4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3
  const g = -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3
  const bLin = -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3

  return [r, g, bLin]
}

const oklchToRgba = (l: number, c: number, hDeg: number, alpha = 1): string => {
  const hRad = (hDeg * Math.PI) / 180
  const a = Math.cos(hRad) * c
  const b = Math.sin(hRad) * c
  const [rLin, gLin, bLin] = oklabToLinearRgb(l, a, b)
  const r = Math.round(srgbFromLinear(rLin) * 255)
  const g = Math.round(srgbFromLinear(gLin) * 255)
  const bVal = Math.round(srgbFromLinear(bLin) * 255)
  return `rgba(${r}, ${g}, ${bVal}, ${Math.max(0, Math.min(1, alpha))})`
}

export function getThemeCoreColors() {
  const primary = resolveCssVarColor('--color-primary')
  const secondary = resolveCssVarColor('--color-secondary')
  const accent = resolveCssVarColor('--color-accent')
  const info = resolveCssVarColor('--color-info')
  const success = resolveCssVarColor('--color-success')
  const warning = resolveCssVarColor('--color-warning')
  const error = resolveCssVarColor('--color-error')
  const base100 = resolveCssVarColor('--color-base-100')
  const base200 = resolveCssVarColor('--color-base-200')
  const base300 = resolveCssVarColor('--color-base-300')
  const baseContent = resolveCssVarColor('--color-base-content')
  const neutral = resolveCssVarColor('--color-neutral')
  return { primary, secondary, accent, info, success, warning, error, neutral, base100, base200, base300, baseContent }
}

const DEFAULT_ECHARTS_PALETTE = [
  '#3b82f6',
  '#06b6d4',
  '#f59e0b',
  '#10b981',
  '#ef4444',
  '#8b5cf6',
  '#84cc16',
  '#f97316',
  '#ec4899',
  '#6366f1',
].map(normalizeCssColor)

type RgbTuple = { r: number; g: number; b: number }

const parseRgbTuple = (color: string): RgbTuple | null => {
  const normalized = normalizeCssColor(color)
  const m = normalized.match(/rgba?\(\s*(\d+)\s*(?:,|\s)\s*(\d+)\s*(?:,|\s)\s*(\d+)/i)
  if (!m) return null
  return { r: Number(m[1]), g: Number(m[2]), b: Number(m[3]) }
}

const isNearWhite = (c: RgbTuple) => c.r >= 245 && c.g >= 245 && c.b >= 245
const isNearGray = (c: RgbTuple) => (Math.max(c.r, c.g, c.b) - Math.min(c.r, c.g, c.b)) <= 8

const shouldFallbackPalette = (palette: string[]) => {
  const head = palette
    .slice(0, 4)
    .map(parseRgbTuple)
    .filter((item): item is RgbTuple => !!item)
  if (head.length < 3) return false

  const uniqueCount = new Set(head.map(c => `${Math.round(c.r / 8)}-${Math.round(c.g / 8)}-${Math.round(c.b / 8)}`)).size
  const nearNeutralCount = head.filter(c => isNearWhite(c) || isNearGray(c)).length
  return uniqueCount <= 1 || nearNeutralCount >= 3
}

const ensureChartPalette = (palette: string[]): string[] => {
  const normalized = (Array.isArray(palette) ? palette : [])
    .map((color) => normalizeCssColor(String(color || '')).trim())
    .filter(Boolean)

  if (!normalized.length) return DEFAULT_ECHARTS_PALETTE
  if (shouldFallbackPalette(normalized)) return DEFAULT_ECHARTS_PALETTE
  return normalized
}

export function buildEChartsPalette(): string[] {
  const c = getThemeCoreColors()
  const basePalette = [c.primary, c.accent, c.info, c.warning, c.success, c.secondary, c.error, c.neutral]
  const extended = [
    'oklch(76% 0.244 18)',
    'oklch(82% 0.195 62)',
    'oklch(72% 0.188 135)',
    'oklch(66% 0.162 250)'
  ]
  return ensureChartPalette(basePalette.concat(extended))
}

export function getEChartsThemedTokens() {
  const c = getThemeCoreColors()
  return {
    palette: buildEChartsPalette(),
    textPrimary: c.baseContent,
    axisLine: rgba(c.baseContent, 0.35),
    axisLabel: rgba(c.baseContent, 0.62),
    splitLine: rgba(c.baseContent, 0.22),
    tooltipBg: rgba(c.base100, 0.98),
    tooltipBorder: rgba(c.baseContent, 0.18),
    cardBg: rgba(c.base100, 0.0)
  }
}

export function makeAreaGradient(echarts: any, baseColor: string, stronger = false) {
  // Use rgba steps based on base color for better canvas compatibility
  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: rgba(baseColor, stronger ? 0.38 : 0.28) },
    { offset: 1, color: rgba(baseColor, stronger ? 0.10 : 0.06) }
  ])
}

