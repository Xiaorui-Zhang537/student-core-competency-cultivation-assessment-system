// Performance tier detection and capability matrix

export type PerfTier = 'auto' | 'high' | 'medium' | 'low' | 'low-static'

export interface Capabilities {
  enableRaymarch: boolean
  enableMarchingCubes: boolean
  enableBloom: boolean
  trailMode: 'gpu' | 'gpu-lite' | 'canvas'
  maxParticles: number
  rippleQuality: 'high' | 'medium' | 'low'
  badgeCount: number
}

export function detectTier(): Exclude<PerfTier, 'auto'> {
  const reduced = matchMedia('(prefers-reduced-motion: reduce)').matches
  if (reduced) return 'low-static'

  const isMobile = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent)
  const cores = (navigator as any).hardwareConcurrency || 2
  const mem = (navigator as any).deviceMemory || 4

  try {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
    if (!gl) return 'low'
  } catch {
    return 'low'
  }

  if (!isMobile && cores >= 8 && mem >= 8) return 'high'
  if (cores >= 4 && mem >= 4) return 'medium'
  return 'low'
}

export function getCapabilities(tier: Exclude<PerfTier, 'auto'>): Capabilities {
  switch (tier) {
    case 'low-static':
      return {
        enableRaymarch: false,
        enableMarchingCubes: false,
        enableBloom: false,
        trailMode: 'canvas',
        maxParticles: 0,
        rippleQuality: 'low',
        badgeCount: 6
      }
    case 'low':
      return {
        enableRaymarch: false,
        enableMarchingCubes: false,
        enableBloom: false,
        trailMode: 'canvas',
        maxParticles: 100,
        rippleQuality: 'low',
        badgeCount: 6
      }
    case 'medium':
      return {
        enableRaymarch: false,
        enableMarchingCubes: true,
        enableBloom: false,
        trailMode: 'gpu-lite',
        maxParticles: 240,
        rippleQuality: 'medium',
        badgeCount: 8
      }
    case 'high':
    default:
      return {
        enableRaymarch: true,
        enableMarchingCubes: true,
        enableBloom: true,
        trailMode: 'gpu',
        maxParticles: 480,
        rippleQuality: 'high',
        badgeCount: 12
      }
  }
}


