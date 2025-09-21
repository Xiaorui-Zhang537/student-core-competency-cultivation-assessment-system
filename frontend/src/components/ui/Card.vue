<template>
  <div
    v-glass="{ strength: 'regular', interactive: true }"
    :class="[
      'rounded-2xl transition-all duration-200 glass-regular glass-interactive',
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
  </div>
</template>

<script setup lang="ts">
interface Props {
  hoverable?: boolean
  padding?: 'none' | 'sm' | 'md' | 'lg' | false
  hoverScale?: boolean
}

withDefaults(defineProps<Props>(), {
  hoverable: false,
  padding: 'md',
  hoverScale: true
})
</script> 