<template>
  <div class="relative h-full">
    <div
      class="flex cursor-pointer items-center gap-1 rounded-md text-sm transition-all duration-200"
      :class="[
        cn(
          'flex cursor-pointer items-center gap-1 rounded-md text-sm',
          isSelect && isSelectable ? 'bg-muted' : '',
          !isSelectable ? 'cursor-not-allowed opacity-50' : '',
          $props.class,
        ),
      ]"
      :dir="direction"
      @click="onTriggerClick"
    >
      <icon v-if="isExpanded" :name="openIcon" size="16" />
      <icon v-else :name="closeIcon" size="16" />
      <span class="select-none">{{ name }}</span>
    </div>

    <div v-if="isExpanded" class="relative text-sm">
      <tree-indicator v-if="name && indicator" aria-hidden="true" />
      <div class="ml-5 flex flex-col gap-1 py-1 rtl:mr-5" :dir="direction">
        <slot />
      </div>
    </div>
  </div>
  
</template>

<script lang="ts" setup>
import { inject, computed, toRefs } from 'vue'
import { cn } from '@/lib/utils'
import Icon from './Icon.vue'
import TreeIndicator from './TreeIndicator.vue'
import type { TreeContextProps } from './fileTreeTypes'
import { TREE_CONTEXT_SYMBOL } from './fileTreeTypes'

interface LocalFolderProps {
  id: string | number
  name: string
  isSelectable?: boolean
  isSelect?: boolean
}

const props = withDefaults(defineProps<LocalFolderProps>(), {
  isSelectable: true
})

const { id, name, isSelectable, isSelect } = toRefs(props)

const treeContext = inject<TreeContextProps>(TREE_CONTEXT_SYMBOL as any)
if (!treeContext) {
  throw new Error('[Folder] must be used inside <Tree>')
}

const { expandedItems, handleExpand, direction, openIcon, closeIcon, indicator } = treeContext

const isExpanded = computed<boolean>(() => {
  const list = expandedItems.value || []
  return list.map(v => String(v)).includes(String(id.value))
})

function onTriggerClick() {
  if (!isSelectable.value) return
  handleExpand(String(id.value))
}
</script>

<style scoped>
</style>

