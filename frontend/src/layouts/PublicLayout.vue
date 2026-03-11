<template>
  <div class="min-h-screen relative text-base-content" :style="baseBgStyle">
    <!-- 顶部导航：品牌 + 右上 Dock（已移除中部菜单） -->
    <nav class="sticky top-0 z-40 px-3 sm:px-6 pt-3 sm:pt-6 pb-3 sm:pb-6">
      <div class="flex flex-wrap items-center gap-2 sm:gap-3">
        <!-- 品牌：液态 Logo + 标题 -->
        <liquid-glass effect="occlusionBlur" :tint="false" :radius="30" class="flex items-center justify-center gap-3 h-full" containerClass="rounded-full h-[52px] sm:h-[60px] px-4 sm:px-5 whitespace-nowrap">
          <div class="flex items-center justify-center gap-3">
            <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
            <sparkles-text :text="t('app.title') as string" :sparklesCount="18" class="text-lg sm:text-2xl md:text-3xl" />
          </div>
        </liquid-glass>

        <!-- 中部菜单移除：根据需求不再显示“概览/特性”等锚点链接 -->

        <div class="hidden sm:block flex-1"></div>

        <!-- 右上 Dock：恢复外层玻璃容器，保持原始包裹感 -->
        <liquid-glass effect="occlusionBlur" :tint="false" :radius="30" class="flex items-center justify-center h-full max-w-full w-full sm:w-auto" containerClass="rounded-full h-[52px] sm:h-[60px] px-1 sm:px-1.5 max-w-full">
          <dock :magnification="0" :distance="0" :animate="false" variant="transparent" paddingClass="pl-0.5 pr-2 sm:pl-1 sm:pr-4" heightClass="h-[48px] sm:h-[56px]" roundedClass="rounded-full" gapClass="gap-2 sm:gap-3" class="!mt-0 min-w-0 overflow-x-auto">
            <dock-icon :animate="false" class="mr-[-6px] sm:relative sm:left-[8px] sm:mr-[-10px]">
              <glass-tooltip :content="(t('layout.common.toggleTheme') as string) || '主题'">
                <ripple-button pill :duration="0" :class="topbarBtnClass" :title="t('layout.common.toggleTheme') as string" @click="uiStore.toggleDarkMode()">
                  <sun-icon v-if="uiStore.isDarkMode" class="w-4 h-4 sm:w-5 sm:h-5" />
                  <moon-icon v-else class="w-4 h-4 sm:w-5 sm:h-5 relative top-[1px]" />
                </ripple-button>
              </glass-tooltip>
            </dock-icon>
            <dock-icon :animate="false" class="hidden sm:block">
              <span ref="cursorBtnRef" class="inline-flex">
                <glass-tooltip :content="(t('layout.common.cursorTrail') as string) || '鼠标轨迹'">
                  <ripple-button pill :duration="0" :class="topbarBtnClass" :title="t('layout.common.cursorTrail') as string" @click="onToggleCursorMenu">
                    <cursor-arrow-rays-icon class="w-4 h-4 sm:w-5 sm:h-5 relative -left-[1px]" />
                  </ripple-button>
                </glass-tooltip>
              </span>
            </dock-icon>
            
            <dock-icon :animate="false" class="-ml-2 whitespace-nowrap sm:relative sm:left-[3px]">
              <glass-tooltip :content="(t('layout.common.language') as string) || '语言'">
                <language-switcher buttonClass="px-3 h-10 flex items-center rounded-full min-w-[56px] whitespace-nowrap" />
              </glass-tooltip>
            </dock-icon>
            <dock-icon :animate="false" class="hidden md:block">
              <glass-tooltip :content="(t('app.home.cta.docs') as string) || '文档'">
                <ripple-button pill :duration="0" :class="topbarBtnClass" :title="t('app.home.cta.docs') as string" @click="goDocs()">
                  <book-open-icon class="w-4 h-4 sm:w-5 sm:h-5" />
                </ripple-button>
              </glass-tooltip>
            </dock-icon>
            <dock-icon :animate="false" class="hidden lg:block">
              <glass-tooltip content="GitHub">
                <ripple-button pill :duration="0" :class="topbarBtnClass" title="GitHub" @click="openExternal(githubUrl)">
                  <svg viewBox="0 0 16 16" fill="currentColor" class="w-4 h-4 sm:w-5 sm:h-5"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38C13.71 14.53 16 11.54 16 8c0-4.42-3.58-8-8-8z"/></svg>
                </ripple-button>
              </glass-tooltip>
            </dock-icon>
            <dock-icon :animate="false" class="min-w-0">
              <glass-tooltip :content="authStore.isAuthenticated ? ((t('app.home.enterApp') as string) || '进入系统') : ((t('app.home.cta.login') as string) || '登录')">
                <ripple-button pill :duration="0" :class="topbarBtnClass" :title="authStore.isAuthenticated ? (t('app.home.enterApp') as string) : (t('app.home.cta.login') as string)" @click="authStore.isAuthenticated ? goDashboard() : goLogin()">
                  <user-icon v-if="!authStore.isAuthenticated" class="w-4 h-4 sm:w-5 sm:h-5 relative top-[1.5px]" />
                  <arrow-right-start-on-rectangle-icon v-else class="w-4 h-4 sm:w-5 sm:h-5" />
                </ripple-button>
              </glass-tooltip>
            </dock-icon>
          </dock>
        </liquid-glass>
      </div>
    </nav>

    <!-- 内容区域（增加左右留白） -->
    <main class="relative z-10">
      <div class="w-full max-w-[1920px] mx-auto px-2 sm:px-4 lg:px-6 py-4 sm:py-6">
        <router-view />
      </div>
    </main>

    <!-- 光标菜单弹层 -->
    <teleport to="body">
      <div v-if="showCursorMenu" class="fixed inset-0 z-[999]" @click="showCursorMenu = false"></div>
      <liquid-glass
        v-if="showCursorMenu"
        effect="occlusionBlur"
        :style="cursorMenuStyle"
        containerClass="fixed z-[1000] rounded-2xl border border-white/20 dark:border-white/12 overflow-hidden shadow-lg"
        class="p-1"
        :radius="16"
        :frost="0.14"
        :alpha="0.96"
        :blur="14"
        :tint="false"
        @click.stop
      >
        <div class="px-3 py-2 text-xs text-subtle">
          <div class="font-medium mb-1">{{ t('layout.common.cursorTrailTitle') }}</div>
          <div class="opacity-90">{{ t('layout.common.cursorTrailDesc') }}</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="setCursor('off')">
          <span>{{ t('layout.common.cursorTrailOff') }}</span>
          <span v-if="uiStore.cursorTrailMode==='off'" class="text-theme-primary">✓</span>
        </button>
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
      </liquid-glass>
    </teleport>

    

    <cursor-trail-layer />
  </div>
  
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import LanguageSwitcher from '@/components/ui/LanguageSwitcher.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import RippleButton from '@/components/ui/RippleButton.vue'
import SparklesText from '@/components/ui/SparklesText.vue'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
import CursorTrailLayer from '@/components/ui/CursorTrailLayer.vue'
import Dock from '@/components/ui/inspira/Dock.vue'
import DockIcon from '@/components/ui/inspira/DockIcon.vue'
import GlassTooltip from '@/components/ui/GlassTooltip.vue'
import { SunIcon, MoonIcon, BookOpenIcon, ArrowRightStartOnRectangleIcon, UserIcon, CursorArrowRaysIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const { t } = useI18n()

const baseBgStyle = computed(() => ({
  backgroundColor: 'color-mix(in oklab, var(--color-base-100) 91%, var(--color-primary) 9%)'
}))
const topbarBtnClass = [
  'hover:bg-black/5 active:bg-black/10',
  'dark:hover:bg-white/10 dark:active:bg-white/15',
  'focus-visible:shadow-[0_0_0_2px_rgba(59,130,246,0.35)]',
].join(' ')

const cursorBtnRef = ref<HTMLElement | null>(null)
const showCursorMenu = ref(false)
const cursorMenuStyle = ref<Record<string, string>>({})

function onToggleCursorMenu() {
  showCursorMenu.value = !showCursorMenu.value
  if (!showCursorMenu.value) return
  nextTick(() => {
    try {
      const el = cursorBtnRef.value as HTMLElement
      const rect = el.getBoundingClientRect()
      const width = 224 // 14rem
      const pad = 8
      const maxLeft = Math.max(pad, (window.innerWidth || 0) - width - pad)
      const left = Math.min(Math.max(pad, rect.right - width), maxLeft)
      cursorMenuStyle.value = {
        position: 'fixed',
        top: `${rect.bottom + 10}px`,
        left: `${left}px`,
        width: '14rem',
        zIndex: '1000'
      }
    } catch {}
  })
}

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
