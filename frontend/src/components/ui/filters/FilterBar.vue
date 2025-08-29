<template>
  <div
    :class="[
      'filter-container rounded-lg',
      dense ? 'p-2' : 'p-3',
      wrap ? 'flex flex-wrap items-end gap-2' : 'flex items-end gap-2 overflow-x-auto no-scrollbar'
    ]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
interface Props {
  dense?: boolean
  wrap?: boolean
}

withDefaults(defineProps<Props>(), {
  dense: false,
  wrap: true
})
</script>


