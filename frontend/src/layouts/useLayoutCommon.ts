/**
 * 布局公共逻辑 composable
 * 抽取 TeacherLayout 和 StudentLayout 中约 95% 相同的状态和方法
 */
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useI18n } from 'vue-i18n'
import { useGlobalShortcuts } from '@/composables/useGlobalShortcuts'

export interface LayoutConfig {
  /** 角色前缀：'teacher' | 'student' */
  rolePrefix: string
  /** Dock 导航项 */
  dockItems: Array<{ key: string; label: string; icon: any }>
  /** Dock key -> 路由路径映射 */
  dockRoutes: Record<string, string>
}

export function useLayoutCommon(config: LayoutConfig) {
  const router = useRouter()
  const uiStore = useUIStore()
  const authStore = useAuthStore()
  const chat = useChatStore()
  const { t } = useI18n()

  // ---- 通用状态 ----
  const baseBgStyle = computed(() => ({ backgroundColor: 'color-mix(in oklab, var(--color-base-100) 86%, transparent)' }))
  const isDockVisible = ref(true)
  const showUserMenu = ref(false)
  const userMenuBtn = ref<HTMLElement | null>(null)
  const userMenuStyle = ref<Record<string, string>>({})
  const showThemeMenu = ref(false)
  const themeBtnRef = ref<HTMLElement | null>(null)
  const themeMenuStyle = ref<Record<string, string>>({})
  const showCursorMenu = ref(false)
  const cursorBtnRef = ref<HTMLElement | null>(null)
  const cursorMenuStyle = ref<Record<string, string>>({})
  const showBgPicker = ref(false)
  const bgBtnRef = ref<HTMLElement | null>(null)
  const bgMenuStyle = ref<Record<string, string>>({})

  const displayName = computed(() =>
    (authStore.user as any)?.nickname ||
    (authStore.user as any)?.name ||
    (authStore.user as any)?.username ||
    t('layout.common.me') || 'Me'
  )

  const topbarBtnClass = [
    'hover:bg-black/5 active:bg-black/10',
    'dark:hover:bg-white/10 dark:active:bg-white/15',
    'focus-visible:shadow-[0_0_0_2px_rgba(59,130,246,0.35)]',
  ].join(' ')

  // ---- 操作 ----
  const handleLogout = async () => {
    showUserMenu.value = false
    await authStore.logout()
    router.push('/auth/login')
  }

  // ---- 弹层定位 watchers ----
  function positionPopover(elRef: { value: HTMLElement | null }, styleRef: { value: Record<string, string> }, width = '14rem', offsetTop = 10) {
    return async (v: boolean) => {
      if (!v) return
      await nextTick()
      try {
        const el = elRef.value as HTMLElement
        const rect = el.getBoundingClientRect()
        const w = parseInt(width) * 16
        styleRef.value = {
          position: 'fixed',
          top: `${rect.bottom + offsetTop}px`,
          left: `${Math.max(8, rect.right - w)}px`,
          width,
          zIndex: '1000'
        }
      } catch {}
    }
  }

  watch(showUserMenu, positionPopover(userMenuBtn, userMenuStyle, '12rem', 8))
  watch(showThemeMenu, positionPopover(themeBtnRef, themeMenuStyle))
  watch(showCursorMenu, positionPopover(cursorBtnRef, cursorMenuStyle))
  watch(showBgPicker, positionPopover(bgBtnRef, bgMenuStyle))

  // ---- 主题/背景/光标 ----
  function setTheme(v: 'retro' | 'dracula' | 'light' | 'dark' | 'cupcake' | 'coffee') {
    uiStore.setTheme(v)
    showThemeMenu.value = false
  }
  function setCursor(v: 'off' | 'fluid' | 'smooth' | 'tailed') {
    uiStore.setCursorTrailMode(v)
    showCursorMenu.value = false
  }
  function setLight(v: 'none' | 'aurora' | 'tetris') { uiStore.setBackgroundLight(v) }
  function setDark(v: 'none' | 'neural' | 'meteors') { uiStore.setBackgroundDark(v) }

  // ---- 弹层切换 ----
  function closeTopbarPopovers() {
    showThemeMenu.value = false
    showUserMenu.value = false
    showCursorMenu.value = false
    showBgPicker.value = false
  }

  function onToggleThemeMenu() {
    try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
    showThemeMenu.value = !showThemeMenu.value
  }
  function onToggleCursorMenu() {
    try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
    showCursorMenu.value = !showCursorMenu.value
  }
  function onToggleBgPicker() {
    try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
    showBgPicker.value = !showBgPicker.value
  }
  function onToggleUserMenu() {
    try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
    showUserMenu.value = !showUserMenu.value
  }

  // ---- 聊天抽屉 ----
  function toggleChatDrawer(ev?: Event) {
    try { ev?.stopPropagation(); ev?.preventDefault() } catch {}
    try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
    if (chat.isOpen) return chat.closeChat()
    chat.openChat()
  }

  // ---- Dock 导航 ----
  const activeDock = computed<string>({
    get() {
      const p = router.currentRoute.value.path
      for (const [key, route] of Object.entries(config.dockRoutes)) {
        if (p.startsWith(route)) return key
      }
      return ''
    },
    set() { /* no-op */ }
  })

  function onSelectDock(k: string) {
    const to = config.dockRoutes[k] || `/${config.rolePrefix}/dashboard`
    if (router.currentRoute.value.path !== to) router.push(to)
  }

  // ---- 全局快捷键 ----
  useGlobalShortcuts()

  // ---- 生命周期 ----
  onMounted(() => {
    try { window.addEventListener('ui:close-topbar-popovers', closeTopbarPopovers) } catch {}
  })
  onUnmounted(() => {
    try { window.removeEventListener('ui:close-topbar-popovers', closeTopbarPopovers) } catch {}
  })

  return {
    // stores
    uiStore, authStore, chat, t, router,
    // state
    baseBgStyle, isDockVisible,
    showUserMenu, userMenuBtn, userMenuStyle,
    showThemeMenu, themeBtnRef, themeMenuStyle,
    showCursorMenu, cursorBtnRef, cursorMenuStyle,
    showBgPicker, bgBtnRef, bgMenuStyle,
    displayName, topbarBtnClass,
    activeDock,
    // methods
    handleLogout,
    setTheme, setCursor, setLight, setDark,
    closeTopbarPopovers,
    onToggleThemeMenu, onToggleCursorMenu, onToggleBgPicker, onToggleUserMenu,
    toggleChatDrawer,
    onSelectDock,
  }
}
