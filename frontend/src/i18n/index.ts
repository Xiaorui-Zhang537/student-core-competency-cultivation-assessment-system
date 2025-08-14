import { createI18n } from 'vue-i18n'

export const SUPPORTED_LOCALES = ['zh-CN', 'en-US'] as const
export type SupportedLocale = typeof SUPPORTED_LOCALES[number]

const DEFAULT_LOCALE: SupportedLocale = 'zh-CN'

const numberFormats = {
  'zh-CN': {
    percent: {
      style: 'percent',
      minimumFractionDigits: 1,
      maximumFractionDigits: 1
    },
    integer: {
      style: 'decimal',
      useGrouping: true,
      maximumFractionDigits: 0
    }
  },
  'en-US': {
    percent: {
      style: 'percent',
      minimumFractionDigits: 1,
      maximumFractionDigits: 1
    },
    integer: {
      style: 'decimal',
      useGrouping: true,
      maximumFractionDigits: 0
    }
  }
} as const

const datetimeFormats = {
  'zh-CN': {
    short: {
      year: 'numeric', month: '2-digit', day: '2-digit'
    },
    medium: {
      year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
    }
  },
  'en-US': {
    short: {
      year: 'numeric', month: '2-digit', day: '2-digit'
    },
    medium: {
      year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
    }
  }
} as const

export const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: false,
  messages: {},
  numberFormats,
  datetimeFormats
})

function normalizeLocale(input: string | null | undefined): SupportedLocale | null {
  if (!input) return null
  const aliasMap: Record<string, SupportedLocale> = {
    'zh': 'zh-CN',
    'zh-cn': 'zh-CN',
    'zh-CN': 'zh-CN',
    'en': 'en-US',
    'en-us': 'en-US',
    'en-US': 'en-US',
  }
  const key = String(input).trim()
  const normalized = aliasMap[key] || null
  return normalized
}

function getInitialLocale(): SupportedLocale {
  const savedRaw = localStorage.getItem('locale')
  const saved = normalizeLocale(savedRaw)
  if (saved && SUPPORTED_LOCALES.includes(saved)) {
    if (savedRaw !== saved) localStorage.setItem('locale', saved)
    return saved
  }
  return DEFAULT_LOCALE
}

// Pre-bundle all locale json files at build time for reliable dynamic loading
const localeModules: Record<string, any> = import.meta.glob('../locales/*/*.json', { eager: true }) as Record<string, any>

// Namespaces required across the app (ensure consistent loading on init and language switch)
export const REQUIRED_NAMESPACES = ['common', 'app', 'layout', 'teacher', 'shared'] as const

export async function loadLocaleMessages(locale: SupportedLocale, namespaces: string[]): Promise<void> {
  const loaded: Record<string, boolean> = (i18n.global as any).__loaded || {}
  for (const ns of namespaces) {
    const key = `${locale}:${ns}`
    if (loaded[key]) continue
    try {
      const path = `../locales/${locale}/${ns}.json`
      const mod = localeModules[path]
      if (!mod) {
        console.warn(`[i18n] Missing locale module: ${path}`)
        continue
      }
      const messages = (mod as any).default || mod
      ;(i18n.global as any).mergeLocaleMessage(locale, messages)
      loaded[key] = true
    } catch (e) {
      console.error(`[i18n] Failed to merge namespace "${ns}" for locale "${locale}":`, e)
    }
  }
  ;(i18n.global as any).__loaded = loaded
}

export async function setLocale(locale: SupportedLocale): Promise<void> {
  const normalized = normalizeLocale(locale) || DEFAULT_LOCALE
  if (!SUPPORTED_LOCALES.includes(normalized)) return
  await loadLocaleMessages(normalized, [...REQUIRED_NAMESPACES])
  i18n.global.locale.value = normalized
  localStorage.setItem('locale', normalized)
}

export function t(key: string, args?: Record<string, unknown>): string {
  return i18n.global.t(key, args as any) as string
}


