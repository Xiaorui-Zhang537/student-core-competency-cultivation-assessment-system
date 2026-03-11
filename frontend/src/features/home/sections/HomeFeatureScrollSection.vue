<template>
  <section id="features" class="mt-16 md:mt-24 scroll-mt-24">
    <div class="relative">
      <div class="mx-auto max-w-6xl px-4">
        <div class="mb-8 md:mb-10">
          <h2 class="text-3xl sm:text-4xl md:text-5xl font-extrabold leading-tight text-[color:var(--color-base-content)]">
            {{ t('app.home.featureScroll.title') }}
          </h2>
          <p class="mt-3 text-base sm:text-lg text-subtle leading-relaxed max-w-2xl">
            {{ t('app.home.featureScroll.subtitle') }}
          </p>
        </div>

        <div ref="pinRef" class="relative">
          <div class="grid md:grid-cols-12 gap-8 items-start">
            <!-- Steps -->
            <div class="md:col-span-5 min-w-0">
              <div class="space-y-3">
                <card-spotlight
                  v-for="(s, idx) in steps"
                  :key="s.key"
                  class="feature-step"
                  :gradient-color="isDarkMode ? 'rgba(255,255,255,0.10)' : 'rgba(0,0,0,0.08)'"
                  :gradient-opacity="0.95"
                  :class="[
                    'rounded-2xl border border-white/12 dark:border-white/10 bg-white/30 dark:bg-white/5 backdrop-blur-md',
                    idx === activeIndex ? 'ring-1 ring-white/25 dark:ring-white/15' : 'opacity-70 hover:opacity-95'
                  ]"
                >
                  <button
                    type="button"
                    class="w-full text-left p-4 md:p-5"
                    @click="onStepClick(idx)"
                  >
                    <div class="flex items-start gap-3">
                      <div class="mt-0.5">
                        <div
                          class="h-8 w-8 rounded-xl grid place-items-center text-sm font-bold border border-white/20 dark:border-white/10"
                          :style="{
                            background:
                              idx === activeIndex
                                ? 'linear-gradient(135deg, color-mix(in oklch, var(--color-primary) 68%, transparent), color-mix(in oklch, var(--color-accent) 55%, transparent))'
                                : 'linear-gradient(135deg, rgba(255,255,255,0.10), rgba(255,255,255,0.02))'
                          }"
                        >
                          {{ idx + 1 }}
                        </div>
                      </div>
                      <div class="min-w-0 flex-1">
                        <div class="text-lg md:text-xl font-semibold leading-snug break-words">
                          {{ t(s.titleKey) }}
                        </div>
                        <div class="mt-2 text-sm md:text-[15px] text-subtle leading-relaxed break-words">
                          {{ t(s.descKey) }}
                        </div>
                        <div class="mt-3 flex items-center gap-2">
                          <span
                            class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium border border-white/15 dark:border-white/10"
                            :style="{
                              background:
                                idx === activeIndex
                                  ? 'linear-gradient(90deg, color-mix(in oklch, var(--color-primary) 22%, transparent), color-mix(in oklch, var(--color-accent) 18%, transparent))'
                                  : 'linear-gradient(90deg, rgba(255,255,255,0.08), rgba(255,255,255,0.02))'
                            }"
                          >
                            {{ t(s.badgeKey) }}
                          </span>
                          <span v-if="idx === activeIndex" class="text-xs opacity-70">
                            {{ t('app.home.featureScroll.scrollHint') }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </button>
                </card-spotlight>
              </div>
            </div>

            <!-- Visual -->
            <div class="md:col-span-7 min-w-0">
              <liquid-glass
                :radius="18"
                :frost="0.06"
                :tint="true"
                :tint-from="'var(--color-primary)'"
                :tint-to="'var(--color-accent)'"
                :container-class="'relative rounded-3xl overflow-hidden'"
              >
                <border-beam :size="260" :duration="14" :border-width="1.5" />

                <div ref="visualRef" class="relative p-5 sm:p-6">
                  <div class="absolute inset-0 pointer-events-none opacity-70" aria-hidden="true">
                    <meteors :count="10" />
                  </div>

                  <div class="relative">
                    <!-- Show the whole image (contain) and make the stage larger -->
                    <div class="relative w-full rounded-2xl overflow-hidden ring-1 ring-white/10 bg-black/5 dark:bg-white/5">
                      <div
                        class="pointer-events-none absolute inset-0 opacity-80"
                        aria-hidden="true"
                        :style="{
                          background:
                            'radial-gradient(circle at 30% 25%, color-mix(in oklch, var(--color-primary) 22%, transparent), transparent 58%), radial-gradient(circle at 70% 20%, color-mix(in oklch, var(--color-accent) 18%, transparent), transparent 62%)'
                        }"
                      />
                      <div
                        class="relative w-full max-h-[74vh]"
                        :style="{ aspectRatio: activeAspectRatio }"
                      >
                        <transition name="homeFeatureFade" mode="out-in">
                          <img
                            :key="steps[activeIndex]?.imageSrc"
                            :src="steps[activeIndex]?.imageSrc"
                            :alt="t(steps[activeIndex]?.imageAltKey || '') as string"
                            class="w-full h-full object-contain select-none pointer-events-none"
                            loading="lazy"
                            decoding="async"
                            @load="onImgLoad"
                          />
                        </transition>
                      </div>
                    </div>
                  </div>

                  <div class="relative mt-4">
                    <div class="flex items-center justify-between gap-3">
                      <div class="text-sm text-subtle">
                        {{ t('app.home.featureScroll.nowShowing') }}:
                        <span class="font-semibold text-[color:var(--color-base-content)]">
                          {{ t(steps[activeIndex]?.titleKey || '') }}
                        </span>
                      </div>
                      <div class="text-xs opacity-70 tabular-nums">
                        {{ activeIndex + 1 }} / {{ steps.length }}
                      </div>
                    </div>
                    <div class="mt-2 h-1.5 rounded-full overflow-hidden bg-black/10 dark:bg-white/10">
                      <div
                        class="h-full rounded-full"
                        :style="{
                          width: `${Math.round(((activeIndex + 1) / steps.length) * 100)}%`,
                          background:
                            'linear-gradient(90deg, var(--color-primary), color-mix(in oklch, var(--color-accent) 80%, var(--color-primary)))'
                        }"
                      />
                    </div>
                  </div>
                </div>
              </liquid-glass>
            </div>
          </div>

        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUIStore } from '@/stores/ui'
import { storeToRefs } from 'pinia'

import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import Meteors from '@/components/ui/inspira/Meteors.vue'
import BorderBeam from '@/components/ui/inspira/BorderBeam.vue'
import CardSpotlight from '@/components/ui/inspira/CardSpotlight.vue'

type FeatureStep = {
  key: string
  titleKey: string
  descKey: string
  badgeKey: string
  imageSrc: string
  imageAltKey: string
}

const { t } = useI18n()
const uiStore = useUIStore()
const { themeName } = storeToRefs(uiStore)

const isDarkMode = computed(() => {
  try {
    return (uiStore as any).isDarkMode || document.documentElement.classList.contains('dark')
  } catch {
    return document.documentElement.classList.contains('dark')
  }
})

const steps = computed<FeatureStep[]>(() => {
  // Theme name is referenced to re-render image choices on theme switches.
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  themeName?.value
  const dark = isDarkMode.value

  return [
    {
      key: 'assessment',
      titleKey: 'app.home.featureScroll.steps.assessment.title',
      descKey: 'app.home.featureScroll.steps.assessment.desc',
      badgeKey: 'app.home.featureScroll.steps.assessment.badge',
      imageSrc: dark ? '/home/apps/assessment-1.png' : '/home/apps/assessment-1.png',
      imageAltKey: 'app.home.cards.assessment.img1Alt',
    },
    {
      key: 'courses',
      titleKey: 'app.home.featureScroll.steps.courses.title',
      descKey: 'app.home.featureScroll.steps.courses.desc',
      badgeKey: 'app.home.featureScroll.steps.courses.badge',
      imageSrc: dark ? '/home/apps/courses-overview.png' : '/home/apps/courses-overview.png',
      imageAltKey: 'app.home.featureScroll.steps.courses.alt',
    },
    {
      key: 'ai',
      titleKey: 'app.home.featureScroll.steps.ai.title',
      descKey: 'app.home.featureScroll.steps.ai.desc',
      badgeKey: 'app.home.featureScroll.steps.ai.badge',
      imageSrc: dark ? '/home/apps/ai-overview.png' : '/home/apps/ai-overview.png',
      imageAltKey: 'app.home.featureScroll.steps.ai.alt',
    },
    {
      key: 'community',
      titleKey: 'app.home.featureScroll.steps.community.title',
      descKey: 'app.home.featureScroll.steps.community.desc',
      badgeKey: 'app.home.featureScroll.steps.community.badge',
      imageSrc: dark ? '/home/apps/community-overview.png' : '/home/apps/community-overview.png',
      imageAltKey: 'app.home.featureScroll.steps.community.alt',
    },
    {
      key: 'chat',
      titleKey: 'app.home.featureScroll.steps.chat.title',
      descKey: 'app.home.featureScroll.steps.chat.desc',
      badgeKey: 'app.home.featureScroll.steps.chat.badge',
      imageSrc: dark ? '/home/apps/chat-overview.png' : '/home/apps/chat-overview.png',
      imageAltKey: 'app.home.featureScroll.steps.chat.alt',
    },
  ]
})

const activeIndex = ref(0)

const pinRef = ref<HTMLElement | null>(null)
const visualRef = ref<HTMLElement | null>(null)

let cleanup: (() => void) | null = null
let refreshScroll: (() => void) | null = null
let activeScrollTrigger: any = null

const aspectByKey = ref<Record<string, number>>({})
const activeKey = computed(() => steps.value[activeIndex.value]?.key || '')
const activeAspectRatio = computed(() => {
  const r = aspectByKey.value[activeKey.value]
  // Default: slightly cinematic. Clamp extremes to avoid wild layout shifts.
  const fallback = 16 / 10
  const ratio = Number.isFinite(r) && r! > 0 ? (r as number) : fallback
  const clamped = Math.min(2.25, Math.max(0.75, ratio))
  return String(clamped)
})

function onImgLoad(e: Event) {
  try {
    const img = e.target as HTMLImageElement
    const w = img?.naturalWidth || 0
    const h = img?.naturalHeight || 0
    if (!w || !h) return
    const key = activeKey.value
    if (!key) return
    aspectByKey.value = { ...aspectByKey.value, [key]: w / h }
  } catch {}
  try { refreshScroll?.() } catch {}
}

function onStepClick(idx: number) {
  const total = steps.value.length
  if (!total) return
  const targetIndex = Math.min(total - 1, Math.max(0, Math.trunc(idx)))
  activeIndex.value = targetIndex

  const st = activeScrollTrigger
  const start = Number(st?.start)
  const end = Number(st?.end)
  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) return

  // Pick the center of each segment so ScrollTrigger's floor() mapping lands on the intended step.
  const progress = Math.min(0.999, Math.max(0, (targetIndex + 0.5) / total))
  const targetY = start + (end - start) * progress

  try {
    const anyWin = window as any
    const lenis = anyWin?.__lenis
    if (lenis && typeof lenis.scrollTo === 'function') {
      lenis.scrollTo(targetY, {
        duration: 0.55,
        easing: (t: number) => 1 - Math.pow(1 - t, 3),
      })
      return
    }
  } catch {}

  try {
    window.scrollTo({ top: targetY, behavior: 'smooth' })
  } catch {
    window.scrollTo(0, targetY)
  }
}

async function setupScrollOrchestration() {
  const root = pinRef.value
  if (!root) return

  // Reduced motion: keep it interactive, but don't pin/scrub.
  if (window.matchMedia?.('(prefers-reduced-motion: reduce)')?.matches) return

  let gsap: any = null
  let ScrollTrigger: any = null
  try {
    const mod = await import('gsap')
    gsap = (mod as any).gsap || (mod as any).default
    const st = await import('gsap/ScrollTrigger')
    ScrollTrigger = (st as any).ScrollTrigger || (st as any).default
    if (gsap && ScrollTrigger) gsap.registerPlugin(ScrollTrigger)
  } catch {
    return
  }
  if (!gsap || !ScrollTrigger) return

  refreshScroll = () => {
    try { ScrollTrigger.refresh() } catch {}
  }

  // If Lenis is used on the page, make ScrollTrigger track its RAF updates.
  // Home mounts Lenis and exposes it on window for debugging; we can piggyback safely.
  try {
    const anyWin = window as any
    if (anyWin?.__lenis && typeof anyWin.__lenis.on === 'function') {
      anyWin.__lenis.on('scroll', () => {
        try { ScrollTrigger.update() } catch {}
      })
      // Help ScrollTrigger recalc when Lenis changes dimensions.
      ScrollTrigger.refresh()
    }
  } catch {}

  const ctx = gsap.context(() => {
    const total = steps.value.length
    let stopWatch: (() => void) | null = null

    // Pin the entire block so the left narrative and right visual "stay" while scrolling.
    const st = ScrollTrigger.create({
      trigger: root,
      start: 'top top+=96',
      end: () => `+=${Math.max(1, total) * window.innerHeight * 0.85}`,
      pin: true,
      pinSpacing: true,
      scrub: 0.9,
      onUpdate(self: any) {
        const idx = Math.min(total - 1, Math.max(0, Math.floor(self.progress * total)))
        if (idx !== activeIndex.value) activeIndex.value = idx
      },
    })
    activeScrollTrigger = st

    // Subtle camera-like drift on the visual container to avoid static feeling when pinned.
    if (visualRef.value) {
      gsap.to(visualRef.value, {
        yPercent: -4,
        rotate: -0.3,
        transformOrigin: '50% 50%',
        ease: 'none',
        scrollTrigger: {
          trigger: root,
          start: 'top top+=96',
          end: st.end,
          scrub: true,
        },
      })
    }

    // Step cards get a gentle staggered "breathing" when active index changes.
    stopWatch = watch(
      () => activeIndex.value,
      (idx) => {
        const cards = Array.from(root.querySelectorAll('.feature-step')) as HTMLElement[]
        cards.forEach((el, i) => {
          const isActive = i === idx
          gsap.to(el, {
            opacity: isActive ? 1 : 0.62,
            y: isActive ? -2 : 0,
            duration: 0.35,
            ease: 'power2.out',
          })
        })
      },
      { immediate: true }
    )

    return () => {
      try {
        st.kill()
      } catch {}
      activeScrollTrigger = null
      try {
        stopWatch?.()
      } catch {}
    }
  }, root)

  const refresh = () => {
    try {
      ScrollTrigger.refresh()
    } catch {}
  }

  // Defer one refresh to ensure layout is stable (images / fonts).
  nextTick(() => refresh())

  window.addEventListener('resize', refresh, { passive: true })
  cleanup = () => {
    window.removeEventListener('resize', refresh)
    try {
      ctx.revert()
    } catch {}
    refreshScroll = null
    activeScrollTrigger = null
  }
}

onMounted(() => {
  setupScrollOrchestration()
})

onUnmounted(() => {
  try {
    cleanup?.()
  } finally {
    cleanup = null
  }
})
</script>

<style scoped>
.homeFeatureFade-enter-active,
.homeFeatureFade-leave-active {
  transition: opacity 260ms ease, transform 260ms ease, filter 260ms ease;
}
.homeFeatureFade-enter-from,
.homeFeatureFade-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.985);
  filter: blur(6px);
}
</style>
