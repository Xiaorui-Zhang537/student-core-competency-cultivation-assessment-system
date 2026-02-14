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
  fallbackLocale: 'zh-CN',
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
export const REQUIRED_NAMESPACES = ['common', 'app', 'layout', 'teacher', 'shared', 'auth', 'student', 'admin', 'notifications'] as const

export async function loadLocaleMessages(locale: SupportedLocale | string, namespaces: string[]): Promise<void> {
  const normalized = normalizeLocale(String(locale)) || DEFAULT_LOCALE
  const loaded: Record<string, boolean> = (i18n.global as any).__loaded || {}
  // 同步合并到别名（防止外部将 locale 设为 zh/en 而非标准码）
  const aliasLocales: string[] = normalized === 'zh-CN' ? ['zh-CN', 'zh'] : normalized === 'en-US' ? ['en-US', 'en'] : [normalized]
  for (const ns of namespaces) {
    for (const loc of aliasLocales) {
      const key = `${loc}:${ns}`
      if (loaded[key]) continue
      try {
        const path = `../locales/${normalized}/${ns}.json`
        const mod = localeModules[path]
        if (!mod) {
          console.warn(`[i18n] Missing locale module: ${path}`)
          continue
        }
        const messages = (mod as any).default || mod
        ;(i18n.global as any).mergeLocaleMessage(loc, messages)
        loaded[key] = true
      } catch (e) {
        console.error(`[i18n] Failed to merge namespace "${ns}" for locale "${loc}":`, e)
      }
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


