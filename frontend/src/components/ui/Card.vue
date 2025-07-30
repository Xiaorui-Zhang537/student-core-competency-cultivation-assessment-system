<template>
  <div
    :class="[
      'bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 transition-all duration-200',
      {
        'hover:shadow-md hover:scale-[1.02]': hoverable,
        'p-0': !padding,
        'p-4': padding === 'sm',
        'p-6': padding === 'md',
        'p-8': padding === 'lg'
      },
      $attrs.class
    ]"
    v-bind="$attrs"
  >
    <header v-if="$slots.header" class="border-b border-gray-200 dark:border-gray-700 pb-4 mb-4">
      <slot name="header" />
    </header>
    
    <main>
      <slot />
    </main>
    
    <footer v-if="$slots.footer" class="border-t border-gray-200 dark:border-gray-700 pt-4 mt-4">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup lang="ts">
interface Props {
  hoverable?: boolean
  padding?: 'none' | 'sm' | 'md' | 'lg' | false
}

withDefaults(defineProps<Props>(), {
  hoverable: false,
  padding: 'md'
})
</script> 