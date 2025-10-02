<template>
  <div>
    <!-- 概览（Aurora + TextScrollReveal + LiquidLogo + AppleCardCarousel + Meteors） -->
    <section id="overview" class="scroll-mt-24">
      <div class="relative">
        <div class="absolute inset-0 -z-10 pointer-events-none opacity-80">
          <AuroraBackground />
        </div>

        <section class="py-10 md:py-16">
          <div class="grid md:grid-cols-12 gap-8 items-center">
            <div class="md:col-span-7">
              <h1 class="text-4xl md:text-6xl font-extrabold leading-tight mb-4 bg-clip-text text-transparent" style="background-image: linear-gradient(90deg,var(--color-base-content),var(--color-primary));">
                {{ t('app.home.hero.title') }}
              </h1>
              <TextScrollReveal :text="t('app.home.hero.subtitle')" class="text-lg md:text-xl text-subtle" />
              <div class="mt-8 flex flex-wrap gap-3">
                <ripple-button pill class="px-5 h-11 inline-flex items-center gap-2" @click="goLogin">
                  <arrow-right-on-rectangle-icon class="w-5 h-5" />
                  <span>{{ t('app.home.cta.login') }}</span>
                </ripple-button>
                <ripple-button pill class="px-5 h-11 inline-flex items-center gap-2" @click="goDocs">
                  <book-open-icon class="w-5 h-5" />
                  <span>{{ t('app.home.cta.docs') }}</span>
                </ripple-button>
              </div>
            </div>
            <div class="md:col-span-5">
              <div class="relative w-full aspect-video rounded-3xl overflow-hidden glass-regular p-6 flex items-center justify-center">
                <div class="absolute -inset-8 opacity-40 pointer-events-none">
                  <Meteors :count="14" />
                </div>
                <div class="relative text-center">
                  <liquid-logo :size="128" :image-url="'/brand/logo.png'" :liquid="14" :speed="1.1" />
                  <div class="mt-4 text-subtle">Liquid Logo</div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="mt-6 md:mt-10">
          <div class="relative">
            <div class="absolute inset-0 opacity-60 pointer-events-none">
              <Meteors :count="18" />
            </div>
            <div class="relative">
              <AppleCardCarousel :items="carouselItems" />
            </div>
          </div>
        </section>
      </div>
    </section>

    <!-- 特性（Meteors 背景 + 三列卡片） -->
    <section id="features" class="mt-14 scroll-mt-24">
      <div class="relative">
        <Meteors :count="20" class="pointer-events-none" />
        <div class="grid md:grid-cols-3 gap-4">
          <div v-for="c in featureCards" :key="c.titleKey" class="glass-regular rounded-2xl p-5">
            <h3 class="font-semibold text-lg mb-2">{{ t(c.titleKey) }}</h3>
            <p class="text-subtle">{{ t(c.descKey) }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 对比（Compare） -->
    <section id="compare" class="mt-14 scroll-mt-24">
      <div class="space-y-8">
        <div>
          <h2 class="text-xl font-semibold mb-3">{{ t('app.home.compare.studentVsTeacher') }}</h2>
          <Compare :leftTitle="t('app.home.compare.student.title')" :rightTitle="t('app.home.compare.teacher.title')">
            <template #left>
              <p class="text-subtle">{{ t('app.home.compare.student.desc') }}</p>
            </template>
            <template #right>
              <p class="text-subtle">{{ t('app.home.compare.teacher.desc') }}</p>
            </template>
          </Compare>
        </div>
        <div>
          <h2 class="text-xl font-semibold mb-3">{{ t('app.home.compare.lightVsDark') }}</h2>
          <Compare :leftTitle="t('app.home.compare.light')" :rightTitle="t('app.home.compare.dark')" />
        </div>
        <div>
          <h2 class="text-xl font-semibold mb-3">{{ t('app.home.compare.glassStyles') }}</h2>
          <Compare :leftTitle="t('app.home.compare.glassRegular')" :rightTitle="t('app.home.compare.glassUltra')" />
        </div>
      </div>
    </section>

    <!-- 时间线与历程（Timeline + TracingBeam） -->
    <section id="timeline" class="mt-14 scroll-mt-24">
      <div class="space-y-10">
        <div>
          <h2 class="text-xl font-semibold mb-3">{{ t('app.home.timeline.versions') }}</h2>
          <Timeline :items="versionItems" />
        </div>
        <div>
          <h2 class="text-xl font-semibold mb-3">{{ t('app.home.timeline.devJourney') }}</h2>
          <TracingBeam :items="journeyItems" />
        </div>
      </div>
    </section>

    <!-- 项目结构（Tree，无卡片，动态从后端获取） -->
    <section id="structure" class="mt-14 scroll-mt-24">
      <Tree :initial-expanded-items="initialExpanded" :initial-selected-id="initialSelected">
        <RenderElements :elements="tree" prefix="root" />
      </Tree>
    </section>

    <!-- 走马灯（Marquee） -->
    <section id="marquee" class="mt-14 scroll-mt-24">
      <Marquee :items="marqueeItems" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import RippleButton from '@/components/ui/RippleButton.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import { ArrowRightOnRectangleIcon, BookOpenIcon } from '@heroicons/vue/24/outline'

// Inspira 适配组件
import TextScrollReveal from '@/components/ui/inspira/TextScrollReveal.vue'
import Meteors from '@/components/ui/inspira/Meteors.vue'
import AuroraBackground from '@/components/ui/inspira/AuroraBackground.vue'
import AppleCardCarousel from '@/components/ui/inspira/AppleCardCarousel.vue'
import Marquee from '@/components/ui/inspira/Marquee.vue'
import Compare from '@/components/ui/inspira/Compare.vue'
import Timeline from '@/components/ui/inspira/Timeline.vue'
import TracingBeam from '@/components/ui/inspira/TracingBeam.vue'
import { Tree } from '@/components/ui/inspira'
import RenderElements from '@/components/ui/inspira/RenderElements.vue'
import { getProjectTree, type ProjectTreeNode } from '@/api/project.api'

const router = useRouter()
const { t } = useI18n()

function goLogin() { router.push('/auth/login') }
function goDocs() { router.push('/docs') }

const carouselItems = [
  { titleKey: 'app.home.cards.assessment.title', descKey: 'app.home.cards.assessment.desc' },
  { titleKey: 'app.home.cards.courses.title', descKey: 'app.home.cards.courses.desc' },
  { titleKey: 'app.home.cards.ai.title', descKey: 'app.home.cards.ai.desc' },
  { titleKey: 'app.home.cards.analytics.title', descKey: 'app.home.cards.analytics.desc' },
  { titleKey: 'app.home.cards.community.title', descKey: 'app.home.cards.community.desc' },
  { titleKey: 'app.home.cards.chat.title', descKey: 'app.home.cards.chat.desc' },
]

const featureCards = [
  { titleKey: 'app.home.cards.assessment.title', descKey: 'app.home.cards.assessment.desc' },
  { titleKey: 'app.home.cards.courses.title', descKey: 'app.home.cards.courses.desc' },
  { titleKey: 'app.home.cards.ai.title', descKey: 'app.home.cards.ai.desc' },
  { titleKey: 'app.home.cards.analytics.title', descKey: 'app.home.cards.analytics.desc' },
  { titleKey: 'app.home.cards.community.title', descKey: 'app.home.cards.community.desc' },
  { titleKey: 'app.home.cards.chat.title', descKey: 'app.home.cards.chat.desc' },
]

const marqueeItems = [
  { titleKey: 'app.home.pages.studentDashboard.title', descKey: 'app.home.pages.studentDashboard.desc' },
  { titleKey: 'app.home.pages.assignments.title', descKey: 'app.home.pages.assignments.desc' },
  { titleKey: 'app.home.pages.courses.title', descKey: 'app.home.pages.courses.desc' },
  { titleKey: 'app.home.pages.analysis.title', descKey: 'app.home.pages.analysis.desc' },
  { titleKey: 'app.home.pages.community.title', descKey: 'app.home.pages.community.desc' },
  { titleKey: 'app.home.pages.notifications.title', descKey: 'app.home.pages.notifications.desc' },
]

const versionItems = [
  { title: 'v0.1.0', date: '2025-07-01', desc: t('app.home.timeline.v010') },
  { title: 'v0.2.0', date: '2025-08-10', desc: t('app.home.timeline.v020') },
  { title: 'v0.3.0', date: '2025-09-25', desc: t('app.home.timeline.v030') },
  { title: 'v0.3.1', date: '2025-09-29', desc: t('app.home.timeline.v031') },
]

const journeyItems = [
  { title: t('app.home.journey.step1.title'), content: t('app.home.journey.step1.desc') },
  { title: t('app.home.journey.step2.title'), content: t('app.home.journey.step2.desc') },
  { title: t('app.home.journey.step3.title'), content: t('app.home.journey.step3.desc') },
  { title: t('app.home.journey.step4.title'), content: t('app.home.journey.step4.desc') },
]

const tree = ref<ProjectTreeNode[]>([])
const initialExpanded = ref<string[]>([])
const initialSelected = ref<string>('')

onMounted(async () => {
  try {
    tree.value = await getProjectTree({ depth: 0 })
    initialExpanded.value = collectAllDirectoryIds(tree.value)
    initialSelected.value = initialExpanded.value[0] || 'root/backend'
  } catch (e) {
    // 失败则回退到精简静态
    tree.value = [
      { name: 'backend', directory: true, children: [ { name: 'src', directory: true }, { name: 'pom.xml', directory: false } ] },
      { name: 'frontend', directory: true, children: [ { name: 'src', directory: true }, { name: 'package.json', directory: false } ] },
      { name: 'docs', directory: true, children: [ { name: 'README.md', directory: false } ] },
    ]
  }
})

function collectAllDirectoryIds(nodes: ProjectTreeNode[], base = 'root'): string[] {
  const result: string[] = []
  for (const n of nodes) {
    const id = `${base}/${n.name}`
    if (n.directory) {
      result.push(id)
      if (Array.isArray(n.children) && n.children.length > 0) {
        result.push(...collectAllDirectoryIds(n.children, id))
      }
    }
  }
  return result
}
</script>

<style scoped>
</style>


