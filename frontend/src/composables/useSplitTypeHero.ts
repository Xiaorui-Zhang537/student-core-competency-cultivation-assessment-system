import { onMounted, onUnmounted } from 'vue'

export function useSplitTypeHero(options?: { selector?: string }) {
  // Prefer targeting a plain-text element (SplitType needs actual text nodes).
  const selector = options?.selector ?? 'h1[data-splittype]'

  let cleanup: (() => void) | null = null

  onMounted(async () => {
    if (typeof window === 'undefined') return
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches) return

    let SplitTypeCtor: any = null
    try {
      const mod: any = await import('split-type')
      SplitTypeCtor = mod?.default || mod
    } catch {
      return
    }
    if (!SplitTypeCtor) return

    let gsap: any = null
    try {
      const mod: any = await import('gsap')
      gsap = mod?.gsap || mod?.default
    } catch {
      gsap = null
    }

    const el = document.querySelector(selector) as HTMLElement | null
    if (!el) return

    const split = new SplitTypeCtor(el, { types: 'words,chars', tagName: 'span' })

    // If GSAP is available, do a crisp character reveal. Otherwise, fallback to CSS class.
    if (gsap && split?.chars?.length) {
      gsap.set(split.chars, { opacity: 0, yPercent: 35, rotate: -1.2, filter: 'blur(8px)' })
      const tl = gsap.timeline()
      tl.to(split.chars, {
        opacity: 1,
        yPercent: 0,
        rotate: 0,
        filter: 'blur(0px)',
        duration: 0.55,
        ease: 'power3.out',
        stagger: 0.018,
      })

      cleanup = () => {
        try { tl.kill() } catch {}
        try { split.revert?.() } catch {}
      }
      return
    }

    el.classList.add('splittype-ready')
    cleanup = () => {
      try { split.revert?.() } catch {}
      try { el.classList.remove('splittype-ready') } catch {}
    }
  })

  onUnmounted(() => {
    try { cleanup?.() } finally { cleanup = null }
  })

  return {}
}
