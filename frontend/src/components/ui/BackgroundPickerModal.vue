<template>
  <glass-modal :title="t('layout.common.bgPickerTitle')" :maxWidth="'max-w-xl'" :backdropDark="true" @close="$emit('close')">
    <div class="space-y-5">
      <div>
        <div class="text-sm font-medium mb-2">{{ t('layout.common.lightMode') }}</div>
        <div class="grid grid-cols-3 gap-3">
          <button :class="btnClass('none', 'light')" @click="setLight('none')">{{ t('layout.common.bg.none') }}</button>
          <button :class="btnClass('aurora', 'light')" @click="setLight('aurora')">Aurora</button>
          <button :class="btnClass('tetris', 'light')" @click="setLight('tetris')">Tetris</button>
        </div>
      </div>
      <div>
        <div class="text-sm font-medium mb-2">{{ t('layout.common.darkMode') }}</div>
        <div class="grid grid-cols-3 gap-3">
          <button :class="btnClass('none', 'dark')" @click="setDark('none')">{{ t('layout.common.bg.none') }}</button>
          <button :class="btnClass('neural', 'dark')" @click="setDark('neural')">Neural</button>
          <button :class="btnClass('meteors', 'dark')" @click="setDark('meteors')">{{ t('layout.common.bg.meteors') }}</button>
        </div>
      </div>
      <div class="pt-1 text-xs text-subtle">{{ t('layout.common.bgPickerDesc') }}</div>
    </div>
  </glass-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUIStore } from '@/stores/ui'
import GlassModal from '@/components/ui/GlassModal.vue'

const emit = defineEmits<{ (e: 'close'): void }>()

const uiStore = useUIStore()
const { t } = useI18n()

function setLight(v: 'none' | 'aurora' | 'tetris') {
  uiStore.setBackgroundLight(v)
}
function setDark(v: 'none' | 'neural' | 'meteors') {
  uiStore.setBackgroundDark(v)
}

function btnClass(val: string, mode: 'light' | 'dark') {
  const active = mode === 'light' ? uiStore.backgroundLight === val : uiStore.backgroundDark === val
  return [
    'px-3 py-2 rounded-xl text-sm border transition-colors',
    active ? 'bg-primary/15 border-primary/40 text-primary' : 'bg-white/5 dark:bg-white/10 border-white/15 dark:border-white/10 hover:bg-white/10'
  ]
}
</script>

<style scoped>
</style>


