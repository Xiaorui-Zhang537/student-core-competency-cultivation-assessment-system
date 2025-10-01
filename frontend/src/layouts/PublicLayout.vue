<template>
  <div class="min-h-screen relative text-base-content" :style="baseBgStyle">
    <!-- 顶部导航：品牌 + 菜单 + 右上 Dock -->
    <nav class="sticky top-0 z-40 px-6 pt-6 pb-6">
      <div class="flex items-center gap-3">
        <!-- 品牌：液态 Logo + 标题 -->
        <liquid-glass :radius="30" class="flex items-center justify-center gap-3 h-full" containerClass="rounded-full h-[60px] px-5 whitespace-nowrap">
          <div class="flex items-center justify-center gap-3">
            <liquid-logo :size="36" :image-url="'/brand/logo.png'" :liquid="12" :speed="1.0" />
            <sparkles-text :text="t('app.title') as string" :sparklesCount="18" class="text-2xl md:text-3xl" />
          </div>
        </liquid-glass>

        <!-- 中部菜单：主页各子页 -->
        <div class="hidden md:flex items-center gap-1 ml-2">
          <a v-for="item in menuItems" :key="item.href" :href="item.href" class="px-3 py-2 rounded-full text-sm hover:bg-white/10 dark:hover:bg-white/10 transition">
            {{ t(item.label) }}
          </a>
        </div>

        <div class="flex-1"></div>

        <!-- 右上 Dock：主题/语言/光标/通知/聊天/GitHub/登录 -->
        <Dock
          @toggle-theme="uiStore.toggleDarkMode()"
          @toggle-cursor="onToggleCursorMenu()"
          @open-docs="router.push('/docs')"
          @open-github="openExternal(githubUrl)"
          @login-or-enter="authStore.isAuthenticated ? goDashboard() : goLogin()"
        >
          <template #language>
            <language-switcher buttonClass="px-3 h-10 flex items-center rounded-full" />
          </template>
          <template #login-label>
            {{ authStore.isAuthenticated ? t('app.home.enterApp') : t('app.home.cta.login') }}
          </template>
        </Dock>
      </div>
    </nav>

    <!-- 内容区域 -->
    <main class="relative z-10">
      <div class="w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <router-view />
      </div>
    </main>

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
    </teleport>

    <cursor-trail-layer />
  </div>
  
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
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
import { SunIcon, MoonIcon, SparklesIcon } from '@heroicons/vue/24/outline'

const router = useRouter()
const uiStore = useUIStore()
const authStore = useAuthStore()
const { t } = useI18n()

const baseBgStyle = computed(() => ({ background: 'var(--color-base-100)' }))

const menuItems = [
  { href: '#overview', label: 'app.home.menu.overview' },
  { href: '#features', label: 'app.home.menu.features' },
  { href: '#compare', label: 'app.home.menu.compare' },
  { href: '#timeline', label: 'app.home.menu.timeline' },
  { href: '#structure', label: 'app.home.menu.structure' },
  { href: '#marquee', label: 'app.home.menu.marquee' },
]

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

function setCursor(v: 'off' | 'fluid' | 'smooth' | 'tailed') {
  uiStore.setCursorTrailMode(v)
  showCursorMenu.value = false
}

function openExternal(url?: string) {
  if (!url) return
  window.open(url, '_blank', 'noopener')
}

function goLogin() {
  router.push('/auth/login')
}

function goDashboard() {
  const role = authStore.userRole
  const target = role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard'
  router.push(target)
}

const githubUrl = import.meta.env.VITE_GITHUB_URL || 'https://github.com'
</script>

<style scoped>
</style>


