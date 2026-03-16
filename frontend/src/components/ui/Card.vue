<template>
  <liquid-glass
    :container-class="containerClass"
    :class="[
      'transition-all duration-200',
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
    effect="occlusionBlur"
    :radius="28"
    :frost="0.08"
    :border="0"
    :lightness="50"
    :alpha="0.96"
    :blur="12"
    :tint="false"
    v-bind="$attrs"
  >
    <header v-if="$slots.header" class="pb-4 mb-4" style="box-shadow: inset 0 -1px 0 color-mix(in oklab, var(--panel-v2-tone, var(--color-primary)) 16%, transparent);">
      <slot name="header" />
    </header>
    
    <main>
      <slot />
    </main>
    
    <footer v-if="$slots.footer" class="pt-4 mt-4" style="box-shadow: inset 0 1px 0 color-mix(in oklab, var(--panel-v2-tone, var(--color-primary)) 16%, transparent);">
      <slot name="footer" />
    </footer>
  </liquid-glass>
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

const tintClass = computed(() => props.tint ? `panel-v2-${props.tint}` : 'panel-v2-neutral')

const containerClass = computed(() => [
  'panel-v2',
  props.hoverable ? 'panel-v2-interactive' : '',
  tintClass.value
].filter(Boolean).join(' '))
</script>
