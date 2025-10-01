<template>
  <div class="relative">
    <LiquidGlass :radius="16" :frost="0.05" class="rounded-2xl" :container-class="containerClass">
      <input
        :id="id"
        :type="isVisible ? 'text' : 'password'"
        class="input rounded-2xl border-none outline-none ring-0 focus:outline-none focus:ring-0 placeholder-muted bg-transparent pr-12"
        :class="[fullWidth ? 'w-full' : '']"
        :placeholder="placeholder"
        :disabled="disabled"
        :value="modelValue as any"
        @input="onInput"
        @keydown="onKeyState"
        @keyup="onKeyState"
        v-bind="$attrs"
      />
      <button
        type="button"
        class="absolute inset-y-0 right-0 px-3 flex items-center text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
        :title="isVisible ? t('layout.auth.password.hide') : t('layout.auth.password.show')"
        @click="toggleVisible"
        :disabled="disabled"
      >
        <component :is="isVisible ? EyeSlashIcon : EyeIcon" class="w-5 h-5" />
      </button>
      <div
        v-if="capsOn"
        class="absolute -bottom-6 left-2 text-[11px] text-warning-600 dark:text-warning-400"
      >
        {{ t('layout.auth.capsLockOn') }}
      </div>
    </LiquidGlass>
  </div>
  
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import { EyeIcon, EyeSlashIcon } from '@heroicons/vue/24/outline'

interface Props {
  modelValue: string | number | null
  placeholder?: string
  id?: string
  disabled?: boolean
  fullWidth?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '',
  id: undefined,
  disabled: false,
  fullWidth: true,
  tint: null,
})

const { t } = useI18n()

const emit = defineEmits<{ (e: 'update:modelValue', v: string | number | null): void }>()

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const containerClass = computed(() => ['glass-regular','glass-interactive', tintClass.value].filter(Boolean).join(' '))

const isVisible = ref(false)
const capsOn = ref(false)

function onInput(e: Event) {
  const target = e.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

function toggleVisible() {
  if (props.disabled) return
  isVisible.value = !isVisible.value
}

function onKeyState(e: KeyboardEvent) {
  try {
    capsOn.value = !!e.getModifierState && e.getModifierState('CapsLock')
  } catch {
    capsOn.value = false
  }
}
</script>

<style scoped>
</style>


