<template>
  <div class="relative z-[60]" ref="host" v-click-outside="() => (open=false)">
    <button
      class="p-1 rounded-full text-gray-400 hover:text-gray-500 dark:hover:text-gray-300 focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 inline-flex items-center justify-center"
      @click="open = !open"
      :title="t('app.language.title') || 'Language'"
    >
      <span class="inline-flex h-6 items-center px-1 text-sm leading-none">{{ currentShortLabel }}</span>
    </button>
    <teleport to="body">
      <div v-if="open" class="glass-regular rounded-xl shadow-lg" v-glass="{ strength: 'regular', interactive: true }" :style="styleObj" @click.stop>
        <div class="px-3 py-2 text-xs text-gray-600 dark:text-gray-300">
          <div class="font-medium mb-1">{{ t('app.language.title') }}</div>
          <div class="opacity-90">{{ t('app.language.note') }}</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <button class="w-full text-left px-3 py-2 rounded hover:bg-white/10 text-sm flex items-center justify-between" @click="change('zh-CN')">
          <span>{{ t('app.language.zhCN') }}</span>
          <span v-if="locale.value==='zh-CN'" class="text-primary-500">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded hover:bg-white/10 text-sm flex items-center justify-between" @click="change('en-US')">
          <span>{{ t('app.language.enUS') }}</span>
          <span v-if="locale.value==='en-US'" class="text-primary-500">✓</span>
        </button>
      </div>
    </teleport>
  </div>
 </template>

<script setup lang="ts">
import { ref, computed, nextTick, watchEffect } from 'vue'
import { useLocale } from '@/i18n/useLocale'
import { loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'
import { useI18n } from 'vue-i18n'

const open = ref(false)
const host = ref<HTMLElement | null>(null)
const styleObj = ref<Record<string, string>>({})
const { locale, setLocale } = useLocale()
const { t } = useI18n()

const currentShortLabel = computed(() => (locale.value === 'zh-CN' ? t('app.language.short.zh') : t('app.language.short.en')))

// click-outside handled by directive

async function change(l: 'zh-CN' | 'en-US') {
  await loadLocaleMessages(l, [...REQUIRED_NAMESPACES])
  await setLocale(l)
  open.value = false
}

watchEffect(async () => {
  if (!open.value) return
  await nextTick()
  try {
    const el = host.value as HTMLElement
    const rect = el.getBoundingClientRect()
    styleObj.value = {
      position: 'fixed',
      top: `${rect.bottom + 8}px`,
      left: `${Math.max(8, rect.right - 160)}px`,
      width: '10rem',
      zIndex: '1000'
    }
  } catch {}
})
</script>


