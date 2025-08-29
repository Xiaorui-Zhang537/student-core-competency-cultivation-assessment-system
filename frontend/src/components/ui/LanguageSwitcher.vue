<template>
  <div class="relative z-[60]" ref="host">
    <button
      class="px-2 py-1 rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 text-sm"
      @click="open = !open"
    >
      {{ currentShortLabel }}
    </button>
    <teleport to="body">
      <div v-if="open" class="glass-regular rounded-xl shadow-lg" v-glass="{ strength: 'regular', interactive: true }" :style="styleObj">
        <button class="block w-full text-left px-3 py-2 text-sm hover:bg-gray-100/60 dark:hover:bg-gray-700/60" @click="change('zh-CN')">{{ t('app.language.zhCN') }}</button>
        <button class="block w-full text-left px-3 py-2 text-sm hover:bg-gray-100/60 dark:hover:bg-gray-700/60" @click="change('en-US')">{{ t('app.language.enUS') }}</button>
      </div>
    </teleport>
  </div>
 </template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watchEffect } from 'vue'
import { useLocale } from '@/i18n/useLocale'
import { loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'
import { useI18n } from 'vue-i18n'

const open = ref(false)
const host = ref<HTMLElement | null>(null)
const styleObj = ref<Record<string, string>>({})
const { locale, setLocale } = useLocale()
const { t } = useI18n()

const currentShortLabel = computed(() => (locale.value === 'zh-CN' ? t('app.language.short.zh') : t('app.language.short.en')))

const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.relative')) {
    open.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

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
      left: `${Math.max(8, rect.right - 112)}px`,
      width: '7rem',
      zIndex: '1000'
    }
  } catch {}
})
</script>


