<template>
  <div :class="['size-full', $props.class]">
    <div ref="rootRef" class="relative h-full overflow-auto px-2" :dir="direction">
      <div class="flex flex-col gap-1">
        <slot />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { toRefs, ref, onMounted, provide } from 'vue'
import type { TreeProps, TreeViewElement, TreeContextProps } from './fileTreeTypes'
import { TREE_CONTEXT_SYMBOL } from './fileTreeTypes'

const props = withDefaults(defineProps<TreeProps>(), {
  indicator: true,
  direction: 'ltr',
  openIcon: 'lucide:folder-open',
  closeIcon: 'lucide:folder',
  fileIcon: 'lucide:file'
})

const { initialSelectedId, indicator, elements, initialExpandedItems, openIcon, closeIcon, fileIcon, direction } = toRefs(props)

const selectedId = ref<string | undefined>(initialSelectedId?.value)
const expandedItems = ref<string[] | undefined>(initialExpandedItems?.value)

function handleExpand(id: string) {
  const current = (expandedItems.value ?? []).map(v => String(v))
  const key = String(id)
  if (current.includes(key)) {
    expandedItems.value = current.filter(item => item !== key)
  } else {
    expandedItems.value = [...current, key]
  }
}

function selectItem(id: string) {
  selectedId.value = id
}

function setExpandedItemsFn(items: string[] | undefined) {
  expandedItems.value = items
}

provide<TreeContextProps>(TREE_CONTEXT_SYMBOL as any, {
  selectedId,
  expandedItems,
  indicator: indicator.value,
  openIcon: openIcon.value!,
  closeIcon: closeIcon.value!,
  fileIcon: fileIcon.value!,
  direction: (direction.value as any) === 'rtl' ? 'rtl' : 'ltr',
  handleExpand,
  selectItem,
  setExpandedItems: setExpandedItemsFn
})

function expandSpecificTargetedElements(list?: TreeViewElement[], selectId?: string) {
  if (!list || !selectId) return
  function findParent(current: TreeViewElement, path: string[] = []) {
    const isSelectable = current.isSelectable ?? true
    const newPath = [...path, current.id]
    if (current.id === selectId) {
      if (isSelectable) {
        expandedItems.value = [ ...(expandedItems.value ?? []), ...newPath ]
      } else {
        newPath.pop()
        expandedItems.value = [ ...(expandedItems.value ?? []), ...newPath ]
      }
      return
    }
    if (current.children?.length) {
      current.children.forEach((child: TreeViewElement) => findParent(child, newPath))
    }
  }
  list.forEach((element: TreeViewElement) => findParent(element))
}

onMounted(() => {
  try {
    if (initialSelectedId?.value) {
      expandSpecificTargetedElements(elements?.value, initialSelectedId.value)
    }
  } catch {}
})

const rootRef = ref<HTMLElement | null>(null)
</script>

<style scoped>
</style>

