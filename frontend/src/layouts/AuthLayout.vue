<template>
  <!-- 强制刷新标记: v2025-07-29-11:15 -->
  <div class="min-h-screen relative overflow-hidden text-base-content" :style="baseBgStyle">
    <BackgroundLayer />

    <!-- 主容器：分屏布局 -->
    <div class="relative z-10 min-h-screen py-8 md:py-10 pl-8 pr-8 md:pl-16 md:pr-16 xl:pl-24 xl:pr-24 2xl:pl-32 2xl:pr-32 max-w-8xl mx-auto">
      <!-- 顶部导航：右上 Dock，与主页一致 -->
      <nav class="flex items-center justify-end mb-6">
        <liquid-glass :radius="30" class="flex items-center justify-center h-full" containerClass="rounded-full h-[56px] px-2">
          <Dock :magnification="60" :distance="140" variant="transparent" paddingClass="pl-1.5 pr-1.5" heightClass="h-[52px]" roundedClass="rounded-full" gapClass="gap-3" class="!mt-0">
            <DockIcon>
              <ripple-button pill :title="t('layout.common.toggleTheme') as string" @click="uiStore.toggleDarkMode()">
                <sun-icon v-if="uiStore.isDarkMode" class="w-5 h-5" />
                <moon-icon v-else class="w-5 h-5" />
              </ripple-button>
            </DockIcon>
            <DockIcon>
              <span ref="bgBtnRef" class="inline-flex">
                <ripple-button pill :title="t('layout.common.bgPickerTitle') as string" @click="onToggleBgPicker">
                  <photo-icon class="w-5 h-5" />
                </ripple-button>
              </span>
            </DockIcon>
            <DockIcon>
              <span ref="cursorBtnRef" class="inline-flex">
                <ripple-button pill :title="t('layout.common.cursorTrail') as string" @click="onToggleCursorMenu">
                  <CursorArrowRaysIcon class="w-5 h-5" />
                </ripple-button>
              </span>
            </DockIcon>
            <DockIcon class="-ml-2">
              <language-switcher buttonClass="px-4 h-10 flex items-center rounded-full min-w-[64px]" />
            </DockIcon>
            <DockIcon>
              <ripple-button pill class="px-3 h-10" :title="('返回首页')" @click="goHome()">
                <HomeIcon class="w-5 h-5" />
              </ripple-button>
            </DockIcon>
          </Dock>
        </liquid-glass>
      </nav>
      <!-- 光标菜单弹层 -->
      <teleport to="body">
        <div v-if="showCursorMenu" class="fixed inset-0 z-[999]" @click="showCursorMenu = false"></div>
        <liquid-glass v-if="showCursorMenu" :style="cursorMenuStyle" containerClass="fixed z-[1000] rounded-2xl" class="p-1" :radius="16" :frost="0.05" @click.stop>
          <div class="px-3 py-2 text-xs text-subtle">
            <div class="font-medium mb-1">{{ t('layout.common.cursorTrailTitle') }}</div>
            <div class="opacity-90">{{ t('layout.common.cursorTrailDesc') }}</div>
          </div>
          <div class="border-t border-white/10 my-1"></div>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('fluid')">
            <span>{{ t('layout.common.cursorTrailFluid') }}</span>
            <span v-if="uiStore.cursorTrailMode==='fluid'" class="text-theme-primary">✓</span>
          </button>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('smooth')">
            <span>{{ t('layout.common.cursorTrailSmooth') }}</span>
            <span v-if="uiStore.cursorTrailMode==='smooth'" class="text-theme-primary">✓</span>
          </button>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('tailed')">
            <span>{{ t('layout.common.cursorTrailTailed') }}</span>
            <span v-if="uiStore.cursorTrailMode==='tailed'" class="text-theme-primary">✓</span>
          </button>
          <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('off')">
            <span>{{ t('layout.common.cursorTrailOff') }}</span>
            <span v-if="uiStore.cursorTrailMode==='off'" class="text-theme-primary">✓</span>
          </button>
        </liquid-glass>
        <!-- 背景选择悬浮菜单（与主题菜单一致样式） -->
        <div v-if="showBgPicker" class="fixed inset-0 z-[999]" @click="showBgPicker = false"></div>
        <liquid-glass v-if="showBgPicker" :style="bgMenuStyle" containerClass="fixed z-[1000] rounded-2xl" class="p-1" :radius="16" :frost="0.05" @click.stop>
          <div class="px-3 py-2 text-xs text-subtle">
            <div class="font-medium mb-1">{{ t('layout.common.bgPickerTitle') }}</div>
            <div class="opacity-90">{{ t('layout.common.bgPickerDesc') }}</div>
          </div>
          <div class="border-t border-white/10 my-1"></div>
          <div class="px-2 py-2">
          <div class="text-xs text-subtle mb-2">{{ t('layout.common.lightMode') }}</div>
          <div class="flex flex-col gap-2">
              <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setLight('none')">
                <span>{{ t('layout.common.bg.none') }}</span>
                <span v-if="uiStore.backgroundLight==='none'" class="text-theme-primary">✓</span>
              </button>
              <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setLight('aurora')">
                <span>{{ t('layout.common.bg.aurora') }}</span>
                <span v-if="uiStore.backgroundLight==='aurora'" class="text-theme-primary">✓</span>
              </button>
              <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setLight('tetris')">
                <span>{{ t('layout.common.bg.tetris') }}</span>
                <span v-if="uiStore.backgroundLight==='tetris'" class="text-theme-primary">✓</span>
              </button>
            </div>
          </div>
          <div class="px-2 py-2 pt-0">
          <div class="text-xs text-subtle mb-2">{{ t('layout.common.darkMode') }}</div>
          <div class="flex flex-col gap-2">
              <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setDark('none')">
                <span>{{ t('layout.common.bg.none') }}</span>
                <span v-if="uiStore.backgroundDark==='none'" class="text-theme-primary">✓</span>
              </button>
              <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setDark('neural')">
                <span>{{ t('layout.common.bg.neural') }}</span>
                <span v-if="uiStore.backgroundDark==='neural'" class="text-theme-primary">✓</span>
              </button>
              <button class="w-full text-left px-3 py-2 rounded-xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setDark('meteors')">
                <span>{{ t('layout.common.bg.meteors') }}</span>
                <span v-if="uiStore.backgroundDark==='meteors'" class="text-theme-primary">✓</span>
              </button>
            </div>
          </div>
        </liquid-glass>
      </teleport>
      <!-- 分屏栅格 -->
      <div class="grid grid-cols-1 md:grid-cols-12 gap-10 items-start mt-6 md:mt-16 lg:mt-24 lg:translate-y-2">
        <!-- 左侧品牌与卖点 -->
        <div class="md:col-span-6 md:col-start-2 order-2 md:order-1 md:pr-6 lg:pr-10">
          <div class="animate-fade-in">
            <!-- Logo 与标题 -->
            <div class="flex items-center gap-4 mb-12">
              <img src="/brand/logo.png" alt="Logo" class="shrink-0 h-20 md:h-24 w-auto select-none" />
              <div>
                <LetterPullup :words="t('app.title') as string" class="bg-gradient-to-r from-gray-900 to-gray-700 dark:from-white dark:to-gray-300 bg-clip-text text-transparent text-3xl md:text-5xl lg:text-6xl font-extrabold leading-tight" />
                <RadiantText :duration="5" :radiantWidth="90" class="mt-1 text-lg md:text-xl text-left">
                  {{ t('layout.auth.subtitle') }}
                </RadiantText>
              </div>
            </div>

            <!-- 卖点列表：2x2 玻璃卡片，带主题底色 -->
            <div class="grid grid-cols-2 gap-x-4 gap-y-8 mb-8">
              <!-- primary -->
              <div class="theme-surface theme-surface--primary rounded-xl border border-white/15 dark:border-white/10"
                   style="--spotlight-color: color-mix(in oklab, var(--color-primary) 60%, transparent);">
                <LiquidGlass :radius="16" :frost="0.05" :border="0.07" :alpha="0.93" :blur="11"
                              :container-class="'glass-regular glass-interactive glass-tint-primary rounded-xl'">
                  <CardSpotlight slotClass="p-4" :gradientColor="'var(--spotlight-color)'" :gradientOpacity="0.7" :gradientSize="220">
                    <div class="text-base font-medium text-base-content mb-1">{{ t('layout.auth.points.radar.title') }}</div>
                    <div class="text-sm text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.radar.descLong') }}</div>
                  </CardSpotlight>
                </LiquidGlass>
              </div>
              <!-- info -->
              <div class="theme-surface theme-surface--info rounded-xl border border-white/15 dark:border-white/10"
                   style="--spotlight-color: color-mix(in oklab, var(--color-info) 60%, transparent);">
                <LiquidGlass :radius="16" :frost="0.05" :border="0.07" :alpha="0.93" :blur="11"
                              :container-class="'glass-regular glass-interactive glass-tint-info rounded-xl'">
                  <CardSpotlight slotClass="p-4" :gradientColor="'var(--spotlight-color)'" :gradientOpacity="0.7" :gradientSize="220">
                    <div class="text-base font-medium text-base-content mb-1">{{ t('layout.auth.points.ai.title') }}</div>
                    <div class="text-sm text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.ai.descLong') }}</div>
                  </CardSpotlight>
                </LiquidGlass>
              </div>
              <!-- success -->
              <div class="theme-surface theme-surface--success rounded-xl border border-white/15 dark:border-white/10"
                   style="--spotlight-color: color-mix(in oklab, var(--color-success) 60%, transparent);">
                <LiquidGlass :radius="16" :frost="0.05" :border="0.07" :alpha="0.93" :blur="11"
                              :container-class="'glass-regular glass-interactive glass-tint-success rounded-xl'">
                  <CardSpotlight slotClass="p-4" :gradientColor="'var(--spotlight-color)'" :gradientOpacity="0.7" :gradientSize="220">
                    <div class="text-base font-medium text-base-content mb-1">{{ t('layout.auth.points.growth.title') }}</div>
                    <div class="text-sm text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.growth.descLong') }}</div>
                  </CardSpotlight>
                </LiquidGlass>
              </div>
              <!-- neutral (新增第4张) -->
              <div class="theme-surface theme-surface--neutral rounded-xl border border-white/15 dark:border-white/10"
                   style="--spotlight-color: color-mix(in oklab, var(--color-secondary, var(--color-accent)) 55%, transparent);">
                <LiquidGlass :radius="16" :frost="0.05" :border="0.07" :alpha="0.93" :blur="11"
                              :container-class="'glass-regular glass-interactive glass-tint-secondary rounded-xl'">
                  <CardSpotlight slotClass="p-4" :gradientColor="'var(--spotlight-color)'" :gradientOpacity="0.7" :gradientSize="220">
                    <div class="text-base font-medium text-base-content mb-1">{{ t('layout.auth.points.community.title') }}</div>
                    <div class="text-sm text-gray-600 dark:text-gray-400">{{ t('layout.auth.points.community.descLong') }}</div>
                  </CardSpotlight>
                </LiquidGlass>
              </div>
            </div>

            <!-- 走马灯评价区 -->
            <div class="mt-10">
              <Marquee pauseOnHover class="[--duration:22s] [--gap:1.25rem]">
                <ReviewCard v-for="r in marqueeRow1" :key="r.username" :img="r.img" :name="r.name" :username="r.username" :mbti="r.mbti" :body="r.body" :tint="r.tint" />
              </Marquee>
              <Marquee reverse pauseOnHover class="[--duration:24s] [--gap:1.25rem] mt-4">
                <ReviewCard v-for="r in marqueeRow2" :key="r.username" :img="r.img" :name="r.name" :username="r.username" :mbti="r.mbti" :body="r.body" :tint="r.tint" />
              </Marquee>
            </div>

          </div>
        </div>

        <!-- 右侧表单卡片 -->
        <div class="md:col-span-5 md:col-start-8 order-1 md:order-2 w-full md:pl-6 lg:pl-10 max-w-xl md:ml-12 lg:ml-20 xl:ml-28 mt-12 md:mt-16 lg:mt-20">
          <card :hoverable="false" padding="lg" tint="primary" class="relative border border-white/15 dark:border-white/10">
            <!-- 加载状态覆盖 -->
            <div v-if="isLoading" class="absolute inset-0 bg-white/90 dark:bg-gray-800/90 backdrop-blur-sm rounded-2xl flex items-center justify-center z-20">
              <div class="text-center">
                <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl mb-3" :style="{ backgroundColor: 'color-mix(in oklab, var(--color-primary) 18%, transparent)' }">
                  <div class="w-6 h-6 border-2 rounded-full animate-spin" :style="{ borderColor: 'var(--color-primary)', borderTopColor: 'transparent' }"></div>
                </div>
                <p class="text-sm text-theme-primary">{{ loadingText }}</p>
              </div>
            </div>

            <!-- 路由内容 -->
            <div class="transition-all duration-300" :class="{ 'opacity-30 pointer-events-none': isLoading }">
              <router-view>
                <template #default="{ Component, route }">
                  <transition
                    name="page"
                    mode="out-in"
                    @before-enter="onBeforeEnter"
                    @after-enter="onAfterEnter"
                  >
                    <component :is="Component" :key="route.path" />
                  </transition>
                </template>
              </router-view>
            </div>

            <template #footer>
              <div class="flex items-center justify-between text-xs text-info">
                <span v-glass="{ strength: 'thin' }" class="inline-flex items-center px-2 py-1 rounded-full font-medium bg-info/15 dark:bg-info/20 border border-info/30 dark:border-info/30">
                  <span class="w-1.5 h-1.5 bg-current rounded-full mr-1.5"></span>
                  {{ `v${version}` }}
                </span>
                <span v-glass="{ strength: 'thin' }" class="inline-flex items-center px-2 py-1 rounded-full bg-info/15 dark:bg-info/20 border border-info/30 dark:border-info/30">
                  <span class="w-1.5 h-1.5 bg-current rounded-full mr-1.5"></span>
                  {{ formattedTime }}
                </span>
              </div>
            </template>
          </card>
        </div>
      </div>
    </div>

    <cursor-trail-layer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useLocale } from '@/i18n/useLocale'
import { loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'
import { useI18n } from 'vue-i18n'
import {
  SunIcon,
  MoonIcon,
  AcademicCapIcon,
  SparklesIcon,
  ShieldCheckIcon,
  ChartBarIcon,
  UserGroupIcon,
  DevicePhoneMobileIcon,
  CloudIcon,
  CpuChipIcon,
  
  QuestionMarkCircleIcon
} from '@heroicons/vue/24/outline'
import { PaintBrushIcon, PhotoIcon } from '@heroicons/vue/24/outline'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import { LetterPullup, RadiantText } from '@/components/ui/inspira'
import { DEFAULT_AVATARS } from '@/shared/utils/avatars'
import Marquee from '@/components/ui/inspira/Marquee.vue'
import ReviewCard from '@/components/ui/inspira/ReviewCard.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import Dock from '@/components/ui/inspira/Dock.vue'
import DockIcon from '@/components/ui/inspira/DockIcon.vue'
import { HomeIcon, CursorArrowRaysIcon } from '@heroicons/vue/24/outline'
// FuturisticBackground 已移除
import Card from '@/components/ui/Card.vue'
import CardSpotlight from '@/components/ui/inspira/CardSpotlight.vue'
import BackgroundLayer from '@/components/ui/BackgroundLayer.vue'
import CursorTrailLayer from '@/components/ui/CursorTrailLayer.vue'

// 组合式API
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 响应式状态
const isLoading = ref(false)
const loadingText = ref('加载中...')
const showLanguageMenu = ref(false)
const currentLanguage = ref({ code: 'zh-CN', label: '中文' })
const version = '2.3.1'
const currentTime = ref(new Date())
let timer: number | null = null
const { locale, setLocale } = useLocale()
const { t, d } = useI18n()
const formattedTime = computed(() => d(currentTime.value, 'medium'))
const baseBgStyle = computed(() => ({ backgroundColor: 'color-mix(in oklab, var(--color-base-100) 86%, transparent)' }))

// 主题相关
const isDark = computed(() => uiStore.isDarkMode)
const isNewTheme = computed(() => {
  const theme = document.documentElement.getAttribute('data-theme') || ''
  return theme === 'retro' || theme === 'dracula'
})
// 主页背景改由 BackgroundLayer 控制

// 动画偏好与设备能力（背景组件内部已做处理，此处无需额外逻辑）

// 语言选项
const languages = [
  { code: 'zh-CN', label: '中文' },
  { code: 'en-US', label: 'English' }
]

// 浮动装饰形状
const floatingShapes = ref([
  {
    color: 'from-primary-400 to-blue-500',
    size: 'w-32 h-32',
    left: '10%',
    top: '20%',
    delay: '0s',
    duration: '20s'
  },
  {
    color: 'from-accent-400 to-orange-500',
    size: 'w-24 h-24',
    left: '80%',
    top: '60%',
    delay: '5s',
    duration: '25s'
  },
  {
    color: 'from-purple-400 to-pink-500',
    size: 'w-40 h-40',
    left: '70%',
    top: '10%',
    delay: '10s',
    duration: '30s'
  },
  {
    color: 'from-green-400 to-teal-500',
    size: 'w-28 h-28',
    left: '20%',
    top: '70%',
    delay: '15s',
    duration: '22s'
  },
  {
    color: 'from-indigo-400 to-purple-500',
    size: 'w-36 h-36',
    left: '50%',
    top: '80%',
    delay: '8s',
    duration: '28s'
  }
])

// 功能特性
const features = [
  { icon: ShieldCheckIcon, titleKey: 'layout.auth.features.secure.title', descKey: 'layout.auth.features.secure.desc' },
  { icon: ChartBarIcon, titleKey: 'layout.auth.features.analytics.title', descKey: 'layout.auth.features.analytics.desc' },
  { icon: UserGroupIcon, titleKey: 'layout.auth.features.collab.title', descKey: 'layout.auth.features.collab.desc' },
  { icon: DevicePhoneMobileIcon, titleKey: 'layout.auth.features.mobile.title', descKey: 'layout.auth.features.mobile.desc' },
  { icon: CloudIcon, titleKey: 'layout.auth.features.cloud.title', descKey: 'layout.auth.features.cloud.desc' },
  { icon: CpuChipIcon, titleKey: 'layout.auth.features.performance.title', descKey: 'layout.auth.features.performance.desc' }
]

// 走马灯数据：使用注册页默认头像，按索引循环分配；评论正文随语言切换实时更新
const avatars = (DEFAULT_AVATARS || []) as string[]
const marqueeBaseKeys = [
  { name: '王同学', username: '@wang', mbti: 'INTJ', tint: 'primary', bodyKey: 'layout.auth.reviews.r1' },
  { name: 'Li', username: '@li', mbti: 'ENFP', tint: 'info', bodyKey: 'layout.auth.reviews.r2' },
  { name: 'Zhang', username: '@zhang', mbti: 'ISTP', tint: 'success', bodyKey: 'layout.auth.reviews.r3' },
  { name: 'Chen', username: '@chen', mbti: 'ENTJ', tint: 'warning', bodyKey: 'layout.auth.reviews.r4' },
  { name: 'Liu', username: '@liu', mbti: 'INFJ', tint: 'secondary', bodyKey: 'layout.auth.reviews.r5' },
  { name: 'Zhao', username: '@zhao', mbti: 'ISTJ', tint: 'accent', bodyKey: 'layout.auth.reviews.r6' },
]
const marqueeAll = computed(() =>
  marqueeBaseKeys.map((r, idx) => ({
    name: r.name,
    username: r.username,
    mbti: r.mbti,
    tint: r.tint as any,
    img: avatars.length ? avatars[idx % avatars.length] : '/brand/logo.png',
    body: t(r.bodyKey as any) as string,
  }))
)
const marqueeRow1 = computed(() => marqueeAll.value.slice(0, Math.ceil(marqueeAll.value.length / 2)))
const marqueeRow2 = computed(() => marqueeAll.value.slice(Math.ceil(marqueeAll.value.length / 2)))

// 社交链接
const socialLinks = [
  {
    name: 'GitHub',
    url: 'https://github.com/Xiaorui-Zhang537/',
    icon: DevicePhoneMobileIcon // 实际项目中应该使用专门的社交图标
  },
  {
    name: '微信',
    url: '#',
    icon: CloudIcon
  },
  {
    name: '邮箱',
    url: '1519939545@qq.com',
    icon: CpuChipIcon
  },
  {
    name: '帮助中心',
    url: '/help',
    route: true,
    icon: QuestionMarkCircleIcon
  }
]

// 方法
function goHome() { router.push('/') }

function goLogin() {
  router.push('/auth/login')
}

// 光标选择菜单
const cursorBtnRef = ref<HTMLElement | null>(null)
const showCursorMenu = ref(false)
const cursorMenuStyle = ref<Record<string, string>>({})
const showBgPicker = ref(false)
const bgBtnRef = ref<HTMLElement | null>(null)
const bgMenuStyle = ref<Record<string, string>>({})

function onToggleCursorMenu() {
  showCursorMenu.value = !showCursorMenu.value
  if (!showCursorMenu.value) return
  nextTick(() => {
    try {
      const el = cursorBtnRef.value as HTMLElement
      const rect = el.getBoundingClientRect()
      cursorMenuStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 10}px`,
        left: `${Math.max(8, rect.right - 220)}px`,
        width: '14rem',
        zIndex: '1000'
      }
    } catch {}
  })
}

function onToggleBgPicker() {
  showBgPicker.value = !showBgPicker.value
}

function setLight(v: 'none' | 'aurora' | 'tetris') { uiStore.setBackgroundLight(v) }
function setDark(v: 'none' | 'neural' | 'meteors') { uiStore.setBackgroundDark(v) }

watch(showBgPicker, async (v: boolean) => {
  if (!v) return
  nextTick(() => {
    try {
      const el = bgBtnRef.value as HTMLElement
      const rect = el.getBoundingClientRect()
      bgMenuStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 10}px`,
        left: `${Math.max(8, rect.right - 220)}px`,
        width: '14rem',
        zIndex: '1000'
      }
    } catch {}
  })
})

function setCursor(v: 'off' | 'fluid' | 'smooth' | 'tailed') {
  uiStore.setCursorTrailMode(v)
  showCursorMenu.value = false
}
const goAndReload = async (path: string) => {
  try {
    await router.push(path)
  } finally {
    requestAnimationFrame(() => window.location.reload())
  }
}


const setLanguage = async (langCode: string) => {
  if (langCode !== 'zh-CN' && langCode !== 'en-US') return
  const lang = languages.find(l => l.code === langCode)
  if (!lang) return
  await loadLocaleMessages(langCode, [...REQUIRED_NAMESPACES])
  await setLocale(langCode)
  currentLanguage.value = lang
  showLanguageMenu.value = false
}

const simulateLoading = (text: string, duration: number = 1000) => {
  isLoading.value = true
  loadingText.value = text
  
  setTimeout(() => {
    isLoading.value = false
  }, duration)
}

// 路由动画事件
const onBeforeEnter = () => {
  // 路由切换前的处理
}

const onAfterEnter = () => {
  // 路由切换后的处理
}

// 监听路由变化
const handleRouteChange = () => {
  // 根据路由显示不同的加载文本
  const routeLoadingTexts: Record<string, string> = {
    '/auth/login': '正在加载登录页面...',
    '/auth/register': '正在加载注册页面...',
    '/auth/forgot-password': '正在加载密码重置...',
    '/auth/reset-password': '正在加载密码重置...'
  }
  
  const loadingText = routeLoadingTexts[route.path] || '加载中...'
  simulateLoading(loadingText, 800)
}

// 键盘快捷键
const handleKeydown = (event: KeyboardEvent) => {
  // Ctrl+Shift+T 切换主题
  if (event.ctrlKey && event.shiftKey && event.key === 'T') {
    event.preventDefault()
    uiStore.toggleDarkMode()
  }
  
  // Esc 关闭语言菜单
  if (event.key === 'Escape') {
    showLanguageMenu.value = false
  }
}

// 视差已在 FuturisticBackground 内部处理，这里不再重复实现

// 生命周期
onMounted(() => {
  // 监听键盘事件
  document.addEventListener('keydown', handleKeydown)

  // 实时时间更新
  timer = window.setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
  
  // 初始化语言标签
  const init = languages.find(l => l.code === locale.value)
  if (init) currentLanguage.value = init
  
  // 初始加载动画
  nextTick(() => {
    handleRouteChange()
  })
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  if (timer) window.clearInterval(timer)
})

// 监听路由变化
// watch(() => route.path, handleRouteChange)
</script>

<style scoped lang="postcss">
/* 页面切换动画 */
.page-enter-active {
  transition: all 0.3s ease-out;
}

.page-leave-active {
  transition: all 0.3s ease-in;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 淡入动画 */
.animate-fade-in {
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 浮动动画 */
.animate-float {
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  25% {
    transform: translateY(-20px) rotate(90deg);
  }
  50% {
    transform: translateY(0px) rotate(180deg);
  }
  75% {
    transform: translateY(20px) rotate(270deg);
  }
}

/* 背景渐变动画 */
.bg-gradient-to-br {
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 文字渐变效果 */
.bg-clip-text {
  -webkit-background-clip: text;
  background-clip: text;
}

/* 毛玻璃效果增强 */
.backdrop-blur-xl {
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
}

.backdrop-blur-sm {
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

/* 响应式调整 */
@media (max-width: 640px) {
  .animate-float {
    animation-duration: 15s;
  }
  
  .floatingShapes {
    display: none; /* 移动端隐藏装饰元素以提升性能 */
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: more) {
  .bg-white\/80 {
    background-color: rgb(255 255 255 / 0.95);
  }
  
  .dark .bg-gray-800\/80 {
    background-color: rgb(31 41 55 / 0.95);
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .animate-float,
  .animate-pulse,
  .animate-spin,
  .bg-gradient-to-br {
    animation: none;
  }
  
  .transition-all,
  .transition-colors,
  .transition-transform {
    transition: none;
  }
}
</style> 