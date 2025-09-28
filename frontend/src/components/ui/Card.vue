<template>
  <LiquidGlass
    :container-class="containerClass"
    :class="[
      'rounded-2xl transition-all duration-200',
      hoverable ? 'hover:shadow-md' : '',
      (hoverable && hoverScale) ? 'hover:scale-[1.005]' : '',
      {
        'p-0': !padding,
        'p-4': padding === 'sm',
        'p-6': padding === 'md',
        'p-8': padding === 'lg'
      },
      $attrs.class
    ]"
    :radius="16"
    :frost="0.05"
    :border="0.07"
    :lightness="50"
    :alpha="0.93"
    :blur="11"
    :scale="-180"
    :rOffset="0"
    :gOffset="10"
    :bOffset="20"
    v-bind="$attrs"
  >
    <header v-if="$slots.header" class="pb-4 mb-4" style="box-shadow: inset 0 -1px 0 rgba(255,255,255,0.18);">
      <slot name="header" />
    </header>
    
    <main>
      <slot />
    </main>
    
    <footer v-if="$slots.footer" class="pt-4 mt-4" style="box-shadow: inset 0 1px 0 rgba(255,255,255,0.18);">
      <slot name="footer" />
    </footer>
  </LiquidGlass>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
interface Props {
  hoverable?: boolean
  padding?: 'none' | 'sm' | 'md' | 'lg' | false
  hoverScale?: boolean
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  hoverable: false,
  padding: 'md',
  hoverScale: true,
  tint: null
})

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')

const containerClass = computed(() => [
  'glass-regular',
  'glass-interactive',
  tintClass.value
].filter(Boolean).join(' '))
</script> 