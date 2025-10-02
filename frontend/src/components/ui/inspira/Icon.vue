<template>
  <span class="inline-flex items-center justify-center" :style="{ width: sizePx, height: sizePx }">
    <Iconify :icon="resolved" :width="sizePx" :height="sizePx" />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Icon as Iconify } from '@iconify/vue'
import folder from '@iconify-icons/lucide/folder'
import folderOpen from '@iconify-icons/lucide/folder-open'
import fileIcon from '@iconify-icons/lucide/file'
const props = withDefaults(defineProps<{ name: string; size?: string | number }>(), {
  size: 16
})

const sizePx = computed(() => typeof props.size === 'number' ? `${props.size}px` : String(props.size))
const resolved = computed(() => {
  const n = String(props.name || '')
  if (n === 'lucide:folder') return folder
  if (n === 'lucide:folder-open') return folderOpen
  if (n === 'lucide:file') return fileIcon
  return n
})
</script>

<style scoped>
</style>

