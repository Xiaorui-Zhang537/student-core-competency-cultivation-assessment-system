import { onMounted, onUnmounted, ref } from 'vue'

// Home-only smooth scroll (Lenis). We keep it isolated so other pages keep native scrolling.
export function useLenisHomeScroll(options?: {
  enabled?: boolean
  smoothWheel?: boolean
  smoothTouch?: boolean
}) {
  const enabled = options?.enabled ?? true
  const lenisRef = ref<any>(null)
  const rafId = ref<number | null>(null)

  async function setup() {
    if (!enabled) return
    if (typeof window === 'undefined') return
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches) return

    let LenisCtor: any = null
    try {
      // lenis is ESM; default export is the class.
      const mod: any = await import('lenis')
      LenisCtor = mod?.default || mod?.Lenis || mod
    } catch {
      return
    }
    if (!LenisCtor) return

    const lenis = new LenisCtor({
      smoothWheel: options?.smoothWheel ?? true,
      smoothTouch: options?.smoothTouch ?? false,
      // Keep it snappy to match ScrollTrigger scrubbing on the page.
      lerp: 0.12,
      wheelMultiplier: 0.95,
    })
    lenisRef.value = lenis

    const raf = (time: number) => {
      try {
        lenis.raf(time)
      } finally {
        rafId.value = requestAnimationFrame(raf)
      }
    }
    rafId.value = requestAnimationFrame(raf)

    // Expose for debugging only; safe no-op if blocked.
    try { ;(window as any).__lenis = lenis } catch {}
  }

  function destroy() {
    if (rafId.value != null) {
      try { cancelAnimationFrame(rafId.value) } catch {}
      rafId.value = null
    }
    const lenis = lenisRef.value
    if (lenis) {
      try { lenis.destroy?.() } catch {}
    }
    lenisRef.value = null
    try { if ((window as any).__lenis) delete (window as any).__lenis } catch {}
  }

  onMounted(() => { setup() })
  onUnmounted(() => { destroy() })

  return { lenis: lenisRef, destroy }
}

