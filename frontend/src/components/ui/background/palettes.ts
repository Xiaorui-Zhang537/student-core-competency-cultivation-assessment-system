// Palette utilities for light/dark themes and emphasis factors
// High-verbosity, readable code per code style

export type ThemeMode = 'light' | 'dark'

export type PalettePresetName = 'youthful'

export interface DualPalette {
  light: string[]
  dark: string[]
}

const PALETTES: Record<PalettePresetName, DualPalette> = {
  youthful: {
    light: ['#4F8BFE', '#00D2FF', '#00E5C3', '#8BFF85', '#F9F871', '#FF8AE3'],
    dark: ['#60A5FA', '#22D3EE', '#2AFEB7', '#A6FF6E', '#F9F871', '#FF5AF1']
  }
}

export function getPalette(
  theme: ThemeMode,
  presetOrCustom?: PalettePresetName | DualPalette
): string[] {
  const dual: DualPalette =
    !presetOrCustom
      ? PALETTES.youthful
      : typeof presetOrCustom === 'string'
        ? PALETTES[presetOrCustom] || PALETTES.youthful
        : presetOrCustom

  const arr = (theme === 'dark' ? dual.dark : dual.light) || []
  if (arr.length >= 6) return arr
  // Ensure at least 6 colors by cycling
  const out: string[] = []
  const base = arr.length ? arr : PALETTES.youthful[theme]
  for (let i = 0; i < 6; i++) out.push(base[i % base.length])
  return out
}

export function getBackgroundGradient(theme: ThemeMode): { from: string; via?: string; to: string } {
  if (theme === 'dark') {
    return { from: '#060B1A', via: '#0A132A', to: '#0E1A39' }
  }
  return { from: '#F9FBFF', via: '#EAF3FF', to: '#E6F7FD' }
}

export function getEmphasisFactors(theme: ThemeMode, emphasis: boolean): {
  glow: number
  rippleSize: number
  pulse: number
} {
  // Slightly stronger factors for dark theme to maintain readability
  const baseGlow = theme === 'dark' ? 1.2 : 1
  const baseRipple = theme === 'dark' ? 1.15 : 1
  const basePulse = theme === 'dark' ? 1.1 : 1
  if (!emphasis) return { glow: baseGlow, rippleSize: baseRipple, pulse: basePulse }
  return {
    glow: baseGlow * 1.25,
    rippleSize: baseRipple * 1.2,
    pulse: basePulse * 1.15
  }
}


