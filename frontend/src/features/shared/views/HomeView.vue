<template>
  <div>
    <!-- 概览（Hero + Gallery） -->
    <section id="overview" class="scroll-mt-24">
      <div class="relative">
        <section class="py-10 md:py-16">
          <div class="grid md:grid-cols-12 gap-8 items-center">
            <div class="md:col-span-6 min-w-0 relative z-10">
              <div class="mb-8 md:mb-10">
                <img src="/brand/logo.png" alt="System Logo" class="h-28 md:h-36 w-auto select-none pointer-events-none" />
              </div>
              <h1
                class="text-left text-4xl sm:text-5xl md:text-7xl font-extrabold leading-tight md:leading-[1.1] break-words break-all mb-6 text-[color:var(--color-base-content)]"
              >
                {{ t('app.home.hero.title') }}
              </h1>
              <p class="text-left text-base sm:text-lg md:text-2xl leading-relaxed break-words break-all text-subtle">
                {{ t('app.home.hero.subtitle') }}
              </p>

              <div class="mt-8 flex flex-wrap items-center gap-4">
                <Button
                  variant="primary"
                  size="xl"
                  class="heroCtaBtn px-8 min-w-[190px] relative overflow-hidden"
                  @click="goExperience"
                >
                  <span class="heroCtaGlow" aria-hidden="true"></span>
                  <span class="heroCtaSheen" aria-hidden="true"></span>
                  <span class="relative inline-flex items-center gap-2">
                    <SparklesIcon class="w-5 h-5" />
                    {{ authStore.isAuthenticated ? t('app.home.enterApp') : t('app.home.cta.login') }}
                    <span class="heroCtaArrow" aria-hidden="true">›</span>
                  </span>
                </Button>

                <Button variant="info" size="xl" class="px-7 min-w-[170px]" @click="goDocs">
                  <template #icon>
                    <BookOpenIcon class="w-5 h-5" />
                  </template>
                  {{ t('app.home.cta.docs') }}
                </Button>
              </div>
              
            </div>
            <div class="md:col-span-6">
              <liquid-glass :radius="16" :frost="0.06" :scale="-100" :tint="true" :tint-from="'var(--color-theme-primary)'" :tint-to="'var(--color-theme-accent)'" :container-class="'relative rounded-2xl w-full aspect-[4/3] sm:aspect-[16/10] max-h-[60vh] overflow-hidden'">
                <div class="relative w-full h-full">
                  <bending-gallery class="absolute inset-0" :bend="3" :border-radius="0.06" :items="galleryItems" :aspect-ratio="16/9" :dpr="2" :anisotropy="8" :wobble="0.015" />
                </div>
              </liquid-glass>
            </div>
          </div>
        </section>
      </div>
    </section>

    <!-- 滚动叙事 Features（ScrollTrigger pin） -->
    <home-feature-scroll-section />

    <!-- Rive 动效展示已从主页移除：保留组件模板以备后续使用 -->

    <!-- 对比（Compare） -->
    <section id="compare" class="mt-14 scroll-mt-24">
      <div class="space-y-20">
        <!-- 明暗主题对比场景：统一 sticky 行，左右同步 -->
        <div id="scene-lightdark" class="relative h-[140vh] sm:h-[200vh]">
          <div class="sticky top-0 grid md:grid-cols-2 items-center gap-8 h-screen">
            <div class="px-2">
              <liquid-glass
                :radius="18"
                :frost="0.06"
                containerClass="relative rounded-2xl w-full aspect-[16/10] max-h-[70vh] overflow-hidden"
              >
                <compare-slider
                  first-image="/home/compare/theme-light.png"
                  second-image="/home/compare/theme-dark.png"
                  :first-image-alt="t('app.home.compare.light')"
                  :second-image-alt="t('app.home.compare.dark')"
                  first-content-class="object-contain object-center"
                  second-content-class="object-contain object-center"
                  class="w-full h-full"
                  slide-mode="hover"
                  :autoplay="true"
                  :autoplay-duration="5000"
                />
              </liquid-glass>
            </div>
            <div class="px-2">
              <text-scroll-reveal
                :text="t('app.home.compareDesc.lightDark') as string"
                :sticky="false"
                progress-container-id="scene-lightdark"
                :reveal-portion="0.3"
              />
            </div>
          </div>
        </div>

        <!-- 中英文对比场景：统一 sticky 行，左右同步 -->
        <div id="scene-zhen" class="relative h-[140vh] sm:h-[200vh]">
          <div class="sticky top-0 grid md:grid-cols-2 items-center gap-8 h-screen">
            <div class="px-2">
              <liquid-glass
                :radius="18"
                :frost="0.06"
                containerClass="relative rounded-2xl w-full aspect-[16/10] max-h-[70vh] overflow-hidden"
              >
                <compare-slider
                  :first-image="langZhImg"
                  :second-image="langEnImg"
                  :first-image-alt="t('app.home.compare.zh')"
                  :second-image-alt="t('app.home.compare.en')"
                  first-content-class="object-contain object-center"
                  second-content-class="object-contain object-center"
                  class="w-full h-full"
                  slide-mode="hover"
                  :autoplay="true"
                  :autoplay-duration="5000"
                />
              </liquid-glass>
            </div>
            <div class="px-2">
              <text-scroll-reveal
                :text="t('app.home.compareDesc.zhEn') as string"
                :sticky="false"
                progress-container-id="scene-zhen"
                :reveal-portion="0.3"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    

    <!-- 走马灯（Marquee） -->
    <section id="help" class="mt-20 scroll-mt-24">
      <div id="scene-help" class="relative h-[180vh] sm:h-[260vh]">
        <div class="sticky top-[8vh]">
          <container-scroll>
            <template #title>
              <text-scroll-reveal :text="t('app.home.helpDesc') as string" :sticky="false" progress-container-id="scene-help" :reveal-portion="0.5" />
            </template>
            <template #card>
              <img :src="helpImageSrc" alt="Help Overview" class="w-full h-full object-cover rounded-xl" @error="onHelpImgError" />
            </template>
          </container-scroll>
        </div>
      </div>
    </section>

    <!-- 学生反馈与系统成效展示（Tracing Beam 区块，占位） -->
    <section id="feedback" class="mt-20 scroll-mt-24">
      <div id="scene-feedback" class="relative h-[160vh] sm:h-[200vh]">
        <div class="sticky top-[8vh]">
          <div class="mb-4">
            <text-scroll-reveal :text="t('app.home.feedbackDesc') as string" :sticky="false" progress-container-id="scene-feedback" :reveal-portion="0.35" />
          </div>
          <div class="w-full items-center justify-center px-4 md:px-8">
            <tracing-beam progress-container-id="scene-feedback" class="px-6">
              <div class="relative mx-auto max-w-2xl pt-4 antialiased">
                <div v-for="(item, index) in feedbackItems" :key="`fb-${index}`" class="mb-6">
                  <span class="ui-chip mb-2 inline-block text-base md:text-lg" :class="[item.variant, 'is-active']">{{ item.badge }}</span>
                  <div class="prose-base dark:prose-invert md:prose-lg prose-p:my-2 md:prose-p:my-3 prose-li:my-1 prose-p:leading-relaxed">
        <div>
                      <p v-for="(paragraph, idx) in item.description" :key="`fbp-${idx}`" v-html="paragraph"></p>
                    </div>
                  </div>
                </div>
                <!-- 备注：单独一项，淡色显示 -->
                <div class="mb-6">
                  <p class="text-subtle opacity-60 text-sm leading-relaxed">{{ t('app.home.feedbackSections.remark') }}</p>
                </div>
              </div>
            </tracing-beam>
        </div>
        </div>
      </div>
    </section>

    <!-- Ready CTA 滚动文本 + 按钮（自动触发彩带） -->
    <section id="ready" class="mt-28 md:mt-36 scroll-mt-24">
      <div id="scene-ready" class="relative h-[160vh] sm:h-[180vh]">
        <div class="sticky top-[16vh]">
          <div class="flex flex-col items-center gap-6">
            <text-scroll-reveal
              :text="t('app.cta.ready') as string"
              :sticky="false"
              progress-container-id="scene-ready"
              :reveal-portion="0.5"
            />
            <div class="flex items-center gap-4">
              <Button
                variant="primary"
                size="xl"
                class="px-7 min-w-[170px]"
                @click="goLogin"
              >
                <template #icon>
                  <sparkles-icon class="w-5 h-5" />
                </template>
                {{ t('app.home.cta.login') }}
              </Button>
              <Button
                variant="info"
                size="xl"
                class="px-7 min-w-[170px]"
                @click="goDocs"
              >
                <template #icon>
                  <book-open-icon class="w-5 h-5" />
                </template>
                {{ t('app.home.cta.docs') }}
              </Button>
            </div>

            <!-- 复用登录页走马灯（两行，评价卡片） -->
            <div class="mt-8 w-full max-w-5xl mx-auto px-4">
              <marquee pauseOnHover class="[--duration:22s] [--gap:1.25rem]">
                <review-card v-for="r in homeMarqueeRow1" :key="r.username" :img="r.img" :name="r.name" :username="r.username" :mbti="r.mbti" :body="r.body" :tint="r.tint" />
              </marquee>
              <marquee reverse pauseOnHover class="[--duration:24s] [--gap:1.25rem] mt-4">
                <review-card v-for="r in homeMarqueeRow2" :key="r.username" :img="r.img" :name="r.name" :username="r.username" :mbti="r.mbti" :body="r.body" :tint="r.tint" />
              </marquee>
            </div>
          </div>
        </div>
        <!-- 进入该区块下方时触发彩带 -->
        <div ref="confettiSentinel" class="absolute bottom-24 left-0 right-0 h-8"></div>
      </div>
    </section>

    <section id="marquee" class="mt-14 scroll-mt-24">
      <marquee :items="marqueeItems" />
    </section>

    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUIStore } from '@/stores/ui'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import { SparklesIcon, BookOpenIcon } from '@heroicons/vue/24/solid'
import { useLenisHomeScroll } from '@/composables/useLenisHomeScroll'
import { useAuthStore } from '@/stores/auth'
// 按钮已移除：不再需要相关图标

// Inspira 适配组件
// 极光背景已移除
import BendingGallery from '@/components/ui/inspira/BendingGallery.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import CompareSlider from '@/components/ui/inspira/CompareSlider.vue'
import TextScrollReveal from '@/components/ui/inspira/TextScrollReveal.vue'
import ContainerScroll from '@/components/ui/inspira/ContainerScroll.vue'
import TracingBeam from '@/components/ui/inspira/TracingBeam.vue'
import confetti from 'canvas-confetti'
import Marquee from '@/components/ui/inspira/Marquee.vue'
import ReviewCard from '@/components/ui/inspira/ReviewCard.vue'
import { DEFAULT_AVATARS } from '@/shared/utils/avatars'
import HomeFeatureScrollSection from '@/features/home/sections/HomeFeatureScrollSection.vue'

const router = useRouter()
const { t } = useI18n()
const uiStore = useUIStore()
const authStore = useAuthStore()
const { themeName } = storeToRefs(uiStore)
const isDarkMode = computed(() => {
  try { return (uiStore as any).isDarkMode || document.documentElement.classList.contains('dark') } catch { return document.documentElement.classList.contains('dark') }
})

// Home-only smooth scroll: enabled on this view lifecycle.
useLenisHomeScroll({ enabled: true, smoothWheel: true, smoothTouch: false })

// 语言对比图片（根据暗黑模式切换到同目录 -dark 资源）
const langZhImg = computed(() => isDarkMode.value ? '/home/compare/lang-zh-dark.png' : '/home/compare/lang-zh.png')
const langEnImg = computed(() => isDarkMode.value ? '/home/compare/lang-en-dark.png' : '/home/compare/lang-en.png')

// 帮助区图片资源路径（请将图片放置于 frontend/public/home/help/overview.png）
const helpImage = '/home/help/overview.png'
const helpImageDark = '/home/help/overview-dark.png'
const helpImageSrc = computed(() => (isDarkMode.value ? helpImageDark : helpImage))
function onHelpImgError(e: Event) {
  const target = e.target as HTMLImageElement
  // 回退到占位图
  target.src = '/brand/logo.png'
}

// Ready CTA 按钮行为
function goLogin() {
  router.push('/auth/login')
}
function goDocs() {
  const url = (import.meta as any).env.VITE_DOCS_URL || 'http://localhost:4174'
  try { window.location.href = url } catch { window.open(url, '_self') }
}

function goExperience() {
  if (!authStore.isAuthenticated) {
    goLogin()
    return
  }
  const role = authStore.userRole
  const target = role === 'TEACHER' ? '/teacher/dashboard' : role === 'ADMIN' ? '/admin/dashboard' : '/student/dashboard'
  router.push(target)
}
// 画廊图片路径固定，标题走 i18n
const galleryItems = computed(() => {
  // 读取主题，浅色用 1-6，深色用 7-12
  const dark = isDarkMode.value
  // 引用 themeName 以确保主题切换时触发依赖更新
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  themeName?.value
  const numbers = dark ? [7, 8, 9, 10, 11, 12] : [1, 2, 3, 4, 5, 6]
  const labels = [
    t('app.home.gallery.login'),
    t('app.home.gallery.analytics'),
    t('app.home.gallery.students'),
    t('app.home.gallery.aiReports'),
    t('app.home.gallery.assistant'),
    t('app.home.gallery.community')
  ]
  return numbers.map((n, idx) => ({ image: `/home/gallery/${n}.png`, text: labels[idx] }))
})

const marqueeItems = [
  { titleKey: 'app.home.pages.studentDashboard.title', descKey: 'app.home.pages.studentDashboard.desc' },
  { titleKey: 'app.home.pages.assignments.title', descKey: 'app.home.pages.assignments.desc' },
  { titleKey: 'app.home.pages.courses.title', descKey: 'app.home.pages.courses.desc' },
  { titleKey: 'app.home.pages.analysis.title', descKey: 'app.home.pages.analysis.desc' },
  { titleKey: 'app.home.pages.community.title', descKey: 'app.home.pages.community.desc' },
  { titleKey: 'app.home.pages.notifications.title', descKey: 'app.home.pages.notifications.desc' },
]

onMounted(() => {
  // 观察滚动触发彩带
  try {
    if (confettiObserver) confettiObserver.disconnect()
    confettiObserver = new IntersectionObserver((entries) => {
      const entry = entries[0]
      if (!entry) return
      // 进入可视区域：触发（若尚未触发）
      if (entry.isIntersecting && !confettiFired.value) {
        confettiFired.value = true
        triggerFireworks()
      }
      // 离开可视区域：复位，允许再次触发
      if (!entry.isIntersecting) {
        confettiFired.value = false
      }
    }, { threshold: 0.35 })
    const el = confettiSentinel.value
    if (el) confettiObserver.observe(el)
  } catch {}
})

onUnmounted(() => {
  try { if (confettiObserver) confettiObserver.disconnect() } catch {}
})

// 彩带：左右两侧喷射的烟花效果
const confettiSentinel = ref<HTMLElement | null>(null)
const confettiFired = ref(false)
let confettiObserver: IntersectionObserver | null = null

function randomInRange(min: number, max: number) {
  return Math.random() * (max - min) + min
}

// 走马灯（复用登录页的评价卡）
const avatars = (DEFAULT_AVATARS || []) as string[]
const baseReviews = [
  { name: '王同学', username: '@wang', mbti: 'INTJ', tint: 'primary', bodyKey: 'layout.auth.reviews.r1' },
  { name: 'Li', username: '@li', mbti: 'ENFP', tint: 'info', bodyKey: 'layout.auth.reviews.r2' },
  { name: 'Zhang', username: '@zhang', mbti: 'ISTP', tint: 'success', bodyKey: 'layout.auth.reviews.r3' },
  { name: 'Chen', username: '@chen', mbti: 'ENTJ', tint: 'warning', bodyKey: 'layout.auth.reviews.r4' },
  { name: 'Liu', username: '@liu', mbti: 'INFJ', tint: 'secondary', bodyKey: 'layout.auth.reviews.r5' },
  { name: 'Zhao', username: '@zhao', mbti: 'ISTJ', tint: 'accent', bodyKey: 'layout.auth.reviews.r6' }
]
const homeMarqueeAll = computed(() => baseReviews.map((r, idx) => ({
  ...r,
  img: avatars.length ? avatars[idx % avatars.length] : '/brand/logo.png',
  body: t(r.bodyKey as any) as string
})))
const homeMarqueeRow1 = computed(() => homeMarqueeAll.value.slice(0, Math.ceil(homeMarqueeAll.value.length / 2)))
const homeMarqueeRow2 = computed(() => homeMarqueeAll.value.slice(Math.ceil(homeMarqueeAll.value.length / 2)))

// Feedback items（根据 i18n 组装为示例样式）
const feedbackItems = computed(() => {
  // 为每个分点分配不同颜色变体：primary/info/success/secondary
  const variants = ['pill-variant-primary', 'pill-variant-info', 'pill-variant-warning', 'pill-variant-secondary']
  const items = [
    {
      badge: t('app.home.feedbackSections.s1.badge') as string,
      title: t('app.home.feedbackSections.s1.title') as string,
      description: [
        t('app.home.feedbackSections.s1.p1') as string,
        `“${t('app.home.feedbackSections.s1.q1') as string}”`,
        `“${t('app.home.feedbackSections.s1.q2') as string}”`
      ]
    },
    {
      badge: t('app.home.feedbackSections.s2.badge') as string,
      title: t('app.home.feedbackSections.s2.title') as string,
      description: [
        t('app.home.feedbackSections.s2.p1') as string,
        t('app.home.feedbackSections.s2.p2') as string,
        t('app.home.feedbackSections.s2.p3') as string,
        t('app.home.feedbackSections.s2.p4') as string
      ]
    },
    {
      badge: t('app.home.feedbackSections.s3.badge') as string,
      title: t('app.home.feedbackSections.s3.title') as string,
      description: [
        t('app.home.feedbackSections.s3.p1') as string,
        t('app.home.feedbackSections.s3.p2') as string,
        t('app.home.feedbackSections.s3.p3') as string,
        t('app.home.feedbackSections.s3.p4') as string
      ]
    },
    {
      badge: t('app.home.feedbackSections.s5.badge') as string,
      title: t('app.home.feedbackSections.s5.title') as string,
      description: [
        `“${t('app.home.feedbackSections.s5.q1') as string}”`,
        t('app.home.feedbackSections.s5.p1') as string
      ]
    }
  ]
  return items.map((it, idx) => ({ ...it, variant: variants[idx % variants.length] }))
})

function triggerFireworks() {
  const duration = 4000
  const animationEnd = Date.now() + duration
  const defaults = { startVelocity: 30, spread: 360, ticks: 60, zIndex: 1000, useWorker: true }
  const interval = window.setInterval(() => {
    const timeLeft = animationEnd - Date.now()
    if (timeLeft <= 0) {
      clearInterval(interval)
      return
    }
    const particleCount = Math.max(20, 50 * (timeLeft / duration))
    confetti({
      ...defaults,
      particleCount: particleCount as number,
      origin: { x: randomInRange(0.1, 0.3), y: Math.random() - 0.2 }
    })
    confetti({
      ...defaults,
      particleCount: particleCount as number,
      origin: { x: randomInRange(0.7, 0.9), y: Math.random() - 0.2 }
    })
  }, 250)
}
</script>

<style scoped>
.heroCtaBtn {
  transition: transform 180ms ease, box-shadow 180ms ease;
  box-shadow: 0 18px 42px color-mix(in oklch, var(--color-primary) 26%, transparent);
}
.heroCtaBtn:hover {
  transform: translateY(-1px) scale(1.02);
  box-shadow: 0 22px 54px color-mix(in oklch, var(--color-primary) 34%, transparent);
}
.heroCtaBtn:active {
  transform: translateY(0) scale(0.99);
}
.heroCtaGlow {
  position: absolute;
  inset: -40%;
  pointer-events: none;
  background: radial-gradient(circle at 30% 30%, color-mix(in oklch, var(--color-accent) 45%, transparent), transparent 55%),
    radial-gradient(circle at 70% 60%, color-mix(in oklch, var(--color-primary) 42%, transparent), transparent 58%);
  filter: blur(26px);
  opacity: 0.9;
}
.heroCtaSheen {
  position: absolute;
  inset: -60% -60%;
  pointer-events: none;
  background: linear-gradient(115deg, transparent 30%, rgba(255,255,255,0.28) 46%, transparent 60%);
  transform: translateX(-35%) rotate(8deg);
  animation: heroCtaSheen 2.6s ease-in-out infinite;
  mix-blend-mode: soft-light;
}
.heroCtaArrow {
  display: inline-block;
  margin-left: 2px;
  opacity: 0.9;
  transform: translateX(0);
  transition: transform 160ms ease;
}
.heroCtaBtn:hover .heroCtaArrow {
  transform: translateX(3px);
}
@keyframes heroCtaSheen {
  0% { transform: translateX(-35%) rotate(8deg); opacity: 0.0; }
  18% { opacity: 0.0; }
  45% { opacity: 0.65; }
  55% { opacity: 0.65; }
  85% { opacity: 0.0; }
  100% { transform: translateX(35%) rotate(8deg); opacity: 0.0; }
}
@media (prefers-reduced-motion: reduce) {
  .heroCtaSheen { animation: none; }
  .heroCtaBtn { transition: none; }
}
</style>
