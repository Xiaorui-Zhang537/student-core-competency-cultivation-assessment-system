<template>
  <div class="relative">
    <button
      class="px-2 py-1 rounded-md text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 text-sm"
      @click="open = !open"
    >
      {{ currentShortLabel }}
    </button>
    <div v-if="open" class="absolute right-0 mt-1 w-28 bg-white dark:bg-gray-800 rounded-md shadow-lg ring-1 ring-black ring-opacity-5 z-50">
      <button
        class="block w-full text-left px-3 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700"
        @click="change('zh-CN')"
      >
        {{ t('app.language.zhCN') }}
      </button>
      <button
        class="block w-full text-left px-3 py-2 text-sm hover:bg-gray-100 dark:hover:bg-gray-700"
        @click="change('en-US')"
      >
        {{ t('app.language.enUS') }}
      </button>
    </div>
  </div>
 </template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useLocale } from '@/i18n/useLocale'
import { loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'
import { useI18n } from 'vue-i18n'

const open = ref(false)
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
</script>


