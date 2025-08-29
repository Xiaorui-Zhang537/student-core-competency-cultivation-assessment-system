/**
 * v-glass directive
 * Usage: v-glass="{ strength: 'thin'|'regular'|'thick'|'ultraThin'|'ultraThick', interactive: boolean }"
 * Defaults: strength='regular', interactive=true
 * Detects backdrop-filter support and applies strength class. Optionally adds interactive sweep.
 */

import type { DirectiveBinding } from 'vue'
import { createApp, h } from 'vue'
import GlassBackdrop from '@/components/ui/GlassBackdrop.vue'

type GlassStrength = 'ultraThin' | 'thin' | 'regular' | 'thick' | 'ultraThick'

interface GlassOptions {
  strength?: GlassStrength
  interactive?: boolean
}

const supportsBackdrop = (): boolean => {
  if (typeof CSS === 'undefined' || !CSS.supports) return false
  return CSS.supports('backdrop-filter: blur(1px)') || CSS.supports('-webkit-backdrop-filter: blur(1px)')
}

const strengthClass = (s: GlassStrength): string => {
  switch (s) {
    case 'ultraThin': return 'glass-ultraThin'
    case 'thin': return 'glass-thin'
    case 'regular': return 'glass-regular'
    case 'thick': return 'glass-thick'
    case 'ultraThick': return 'glass-ultraThick'
    default: return 'glass-regular'
  }
}

const apply = (el: HTMLElement, binding: DirectiveBinding<GlassOptions | GlassStrength | undefined>) => {
  const value = binding.value
  const opts: GlassOptions = typeof value === 'string'
    ? { strength: value as GlassStrength, interactive: true }
    : (value || {})

  const strength: GlassStrength = opts.strength || 'regular'
  const interactive = opts.interactive !== false

  // reset classes first
  el.classList.remove('glass-ultraThin', 'glass-thin', 'glass-regular', 'glass-thick', 'glass-ultraThick', 'glass-interactive')

  if (supportsBackdrop()) {
    el.classList.add(strengthClass(strength))
    if (interactive) el.classList.add('glass-interactive')
  } else {
    // Fallback: mount a GlassBackdrop child to emulate translucency
    let host = el.querySelector('[data-glass-fallback]') as HTMLElement | null
    if (!host) {
      host = document.createElement('div')
      host.setAttribute('data-glass-fallback', '1')
      host.style.position = 'absolute'
      host.style.inset = '0'
      host.style.pointerEvents = 'none'
      host.style.zIndex = '0'
      // ensure el is positioned
      const cs = window.getComputedStyle(el)
      if (cs.position === 'static') el.style.position = 'relative'
      el.prepend(host)
      const app = createApp({ render: () => h(GlassBackdrop, { blur: 10, opacity: 0.86 }) })
      app.mount(host)
    }
    // Ensure readable base background color in fallback
    const isDark = document.documentElement.classList.contains('dark')
    el.style.backgroundColor = isDark ? 'rgba(17,24,39,0.78)' : 'rgba(255,255,255,0.78)'
    el.style.border = '1px solid rgba(255,255,255,0.35)'
    el.style.boxShadow = 'inset 0 1px 0 rgba(255,255,255,0.25), 0 6px 18px rgba(0,0,0,0.08)'
  }
}

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding<GlassOptions | GlassStrength | undefined>) {
    apply(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<GlassOptions | GlassStrength | undefined>) {
    apply(el, binding)
  }
}


