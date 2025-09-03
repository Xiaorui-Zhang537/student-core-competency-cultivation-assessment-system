<template>
  <div
    :class="[
      'filter-container rounded-lg',
      dense ? 'p-2' : 'p-3',
      wrap ? 'flex flex-wrap items-end gap-2' : 'flex items-end gap-2 overflow-x-auto no-scrollbar'
    ]"
  >
    <div class="flex items-end gap-2 w-full flex-wrap">
      <slot name="left" />
      <div class="ml-auto flex items-end gap-2">
        <slot name="right" />
      </div>
      <!-- 兼容旧用法：默认插槽仍然渲染 -->
      <slot />
    </div>
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


