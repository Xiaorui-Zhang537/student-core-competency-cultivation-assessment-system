<template>
  <div class="glass-regular rounded-full h-12 px-2 flex items-center gap-1 shadow-sm">
    <ripple-button pill :title="t('layout.common.toggleTheme') as string" @click="$emit('toggle-theme')">
      <slot name="theme-icon">
        <sun-icon v-if="isDark" class="w-5 h-5" />
        <moon-icon v-else class="w-5 h-5" />
      </slot>
    </ripple-button>

    <span class="inline-flex"><ripple-button pill :title="t('layout.common.cursorTrail') as string" @click="$emit('toggle-cursor')"><sparkles-icon class="w-5 h-5" /></ripple-button></span>

    <slot name="language" />

    <ripple-button pill title="Docs" @click="$emit('open-docs')">
      <book-open-icon class="w-5 h-5" />
    </ripple-button>

    <ripple-button pill title="GitHub" @click="$emit('open-github')">
      <svg viewBox="0 0 16 16" fill="currentColor" class="w-5 h-5"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38C13.71 14.53 16 11.54 16 8c0-4.42-3.58-8-8-8z"/></svg>
    </ripple-button>

    <ripple-button pill class="px-3" @click="$emit('login-or-enter')">
      <slot name="login-label">Login</slot>
    </ripple-button>
  </div>
</template>

<script setup lang="ts">
import RippleButton from '@/components/ui/RippleButton.vue'
import { useUIStore } from '@/stores/ui'
import { useI18n } from 'vue-i18n'
import { SunIcon, MoonIcon, SparklesIcon, BookOpenIcon } from '@heroicons/vue/24/outline'
import { computed } from 'vue'

defineEmits(['toggle-theme','toggle-cursor','open-docs','open-github','login-or-enter'])
const uiStore = useUIStore()
const { t } = useI18n()
const isDark = computed(() => uiStore.isDarkMode)
</script>

<style scoped>
</style>


