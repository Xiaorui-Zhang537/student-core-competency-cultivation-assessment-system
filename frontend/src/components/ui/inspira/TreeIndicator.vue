<template>
  <div :dir="direction" class="absolute left-1.5 h-full w-px rounded-md bg-muted py-3 rtl:right-1.5" />
</template>

<script lang="ts" setup>
import { inject } from 'vue'
import type { TreeContextProps } from './fileTreeTypes'
import { TREE_CONTEXT_SYMBOL } from './fileTreeTypes'

const treeContext = inject<TreeContextProps>(TREE_CONTEXT_SYMBOL as any)
if (!treeContext) {
  throw new Error('[TreeIndicator] must be used inside <Tree>')
}

const { direction } = treeContext
</script>

<style scoped>
</style>

