<template>
  <div class="min-h-screen relative text-base-content" :style="baseBgStyle">
    <BackgroundLayer />
    <!-- 顶部导航：品牌 + 右上 Dock（已移除中部菜单） -->
    <nav class="sticky top-0 z-40 px-6 pt-6 pb-6">
      <div class="flex items-center gap-3">
        <!-- 品牌：液态 Logo + 标题 -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-3 h-full" containerClass="rounded-full h-[60px] px-5 whitespace-nowrap">
          <div class="flex items-center justify-center gap-3">
            <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
            <sparkles-text :text="t('app.title') as string" :sparklesCount="18" class="text-2xl md:text-3xl" />
          </div>
        </liquid-glass>

        <!-- 中部菜单移除：根据需求不再显示“概览/特性”等锚点链接 -->

        <div class="flex-1"></div>

        <!-- 右上 Dock：包裹到 LiquidGlass 药丸，统一液态玻璃视觉 -->
        <liquid-glass :radius="30" class="flex items-center justify-center h-full" containerClass="rounded-full h-[60px] px-2">
          <Dock :magnification="60" :distance="140" variant="transparent" paddingClass="pl-1.5 pr-5" heightClass="h-[56px]" roundedClass="rounded-full" gapClass="gap-3" class="!mt-0 min-w-0">
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
            
            <DockIcon class="-ml-2 whitespace-nowrap">
              <language-switcher buttonClass="px-4 h-10 flex items-center rounded-full min-w-[64px]" />
            </DockIcon>
            <DockIcon>
              <ripple-button pill class="px-4 h-10" :title="t('app.home.cta.docs') as string" @click="goDocs()">
                <BookOpenIcon class="w-5 h-5" />
              </ripple-button>
            </DockIcon>
            <DockIcon>
              <ripple-button pill title="GitHub" @click="openExternal(githubUrl)">
                <svg viewBox="0 0 16 16" fill="currentColor" class="w-5 h-5"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38C13.71 14.53 16 11.54 16 8c0-4.42-3.58-8-8-8z"/></svg>
              </ripple-button>
            </DockIcon>
            <DockIcon class="min-w-0">
              <ripple-button pill class="px-4 h-10 min-w-0" :title="authStore.isAuthenticated ? (t('app.home.enterApp') as string) : (t('app.home.cta.login') as string)" @click="authStore.isAuthenticated ? goDashboard() : goLogin()">
                <user-icon v-if="!authStore.isAuthenticated" class="w-5 h-5" />
                <arrow-right-start-on-rectangle-icon v-else class="w-5 h-5" />
              </ripple-button>
            </DockIcon>
          </Dock>
        </liquid-glass>
      </div>
    </nav>

    <!-- 内容区域（增加左右留白） -->
    <main class="relative z-10">
      <div class="w-full max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6 py-6">
        <router-view />
      </div>
    </main>

    <!-- 背景选择悬浮菜单（与主题菜单一致样式） -->
    <teleport to="body">
      <div v-if="showBgPicker" class="fixed inset-0 z-[999]" @click="showBgPicker = false"></div>
      <liquid-glass
        v-if="showBgPicker"
        effect="occlusionBlur"
        :style="bgMenuStyle"
        containerClass="fixed z-[1000] rounded-2xl"
        class="p-1"
        :radius="16"
        :frost="0.05"
        @click.stop
      >
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

    <!-- 光标菜单弹层 -->
    <teleport to="body">
      <div v-if="showCursorMenu" class="fixed inset-0 z-[999]" @click="showCursorMenu = false"></div>
      <liquid-glass v-if="showCursorMenu" effect="occlusionBlur" :style="cursorMenuStyle" containerClass="fixed z-[1000] rounded-2xl" class="p-1" :radius="16" :frost="0.05" @click.stop>
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
    </teleport>

    

    <cursor-trail-layer />
  </div>
  
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted, defineAsyncComponent } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { watch } from 'vue'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import BackgroundLayer from '@/components/ui/BackgroundLayer.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import SparklesText from '@/components/ui/SparklesText.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import CursorTrailLayer from '@/components/ui/CursorTrailLayer.vue'
import Dock from '@/components/ui/inspira/Dock.vue'
import DockIcon from '@/components/ui/inspira/DockIcon.vue'
import { SunIcon, MoonIcon, BookOpenIcon, ArrowRightStartOnRectangleIcon, UserIcon, CursorArrowRaysIcon, PaintBrushIcon, PhotoIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const { t } = useI18n()

const baseBgStyle = computed(() => ({ backgroundColor: 'color-mix(in oklab, var(--color-base-100) 86%, transparent)' }))

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

// 主页不再提供主题家族选择

function setCursor(v: 'off' | 'fluid' | 'smooth' | 'tailed') {
  uiStore.setCursorTrailMode(v)
  showCursorMenu.value = false
}

function openExternal(url?: string) {
  if (!url) return
  try { window.open(url, '_blank', 'noopener') } catch { window.location.href = url }
}

const docsUrl = (import.meta as any).env.VITE_DOCS_URL as string | undefined
function goDocs() {
  const target = docsUrl || 'http://localhost:4174'
  try { sessionStorage.setItem('leavingTo', 'docs') } catch {}
  // 同窗口跳转到文档服务，避免 Vite 双服务器混用解析导致的别名错误
  window.location.href = target
}

function goLogin() {
  router.push('/auth/login')
}

function goDashboard() {
  const role = authStore.userRole
  const target = role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard'
  router.push(target)
}

const githubUrl = import.meta.env.VITE_GITHUB_URL || 'https://github.com/Xiaorui-Zhang537/student-core-competency-cultivation-assessment-system'

// 返回时自动处理主页恢复：
// - 若从 Docs 返回（leavingTo==='docs'），自动跳到首页
// - 其他 back/forward 恢复则刷新以避免白屏
const onPageShow = (ev: PageTransitionEvent) => {
  try {
    const nav = (performance.getEntriesByType('navigation') as any)[0]
    const isBack = (nav && nav.type === 'back_forward') || (ev as any).persisted
    const leavingTo = sessionStorage.getItem('leavingTo')
    if (leavingTo === 'docs') {
      try { sessionStorage.removeItem('leavingTo') } catch {}
      router.replace('/')
      return
    }
    if (isBack) {
      window.location.reload()
    }
  } catch {}
}

onMounted(() => {
  try { window.addEventListener('pageshow', onPageShow as any) } catch {}
})
onUnmounted(() => {
  try { window.removeEventListener('pageshow', onPageShow as any) } catch {}
})
</script>

<style scoped>
</style>


