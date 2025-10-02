<template>
  <button
    ref="fileRef"
    type="button"
    :disabled="!isSelectable"
    :class="[
      cn(
        'flex w-fit items-center gap-1 rounded-sm pr-1 text-sm duration-200 ease-in-out rtl:pl-1 rtl:pr-0',
        isSelected && isSelectable ? 'bg-muted' : '',
        isSelectable ? 'cursor-pointer' : 'cursor-not-allowed opacity-50',
        $props.class,
      ),
    ]"
    :dir="direction"
    @click="onClickHandler"
  >
    <Icon :name="iconName" size="16" />
    <span class="select-none">{{ name }}</span>
  </button>
</template>

<script lang="ts" setup>
import { inject, computed, toRefs, ref } from 'vue'
import { cn } from '@/lib/utils'
import Icon from './Icon.vue'
import type { TreeContextProps } from './fileTreeTypes'
import { TREE_CONTEXT_SYMBOL } from './fileTreeTypes'

interface LocalFileProps {
  id: string | number
  name: string
  isSelectable?: boolean
  isSelect?: boolean
}

const props = withDefaults(defineProps<LocalFileProps>(), {
  isSelectable: true
})

const { id, name, isSelectable, isSelect } = toRefs(props)

const treeContext = inject<TreeContextProps>(TREE_CONTEXT_SYMBOL as any)
if (!treeContext) {
  throw new Error('[File] must be used inside <Tree>')
}

const { selectedId, selectItem, direction, fileIcon } = treeContext
const iconName = computed(() => fileIcon || 'lucide:file')

const isSelected = computed<boolean>(() => {
  return (isSelect?.value as any) || selectedId.value === (id.value as any)
})

function onClickHandler() {
  if (!isSelectable.value) return
  selectItem(String(id.value))
}

const fileRef = ref<HTMLButtonElement | null>(null)
</script>

<style scoped>
</style>

