import type { Ref, InjectionKey } from 'vue'

export interface TreeProps {
  class?: string
  initialSelectedId?: string
  indicator?: boolean
  elements?: TreeViewElement[]
  initialExpandedItems?: string[]
  openIcon?: string
  closeIcon?: string
  fileIcon?: string
  direction?: 'rtl' | 'ltr'
}

export interface TreeContextProps {
  selectedId: Ref<string | undefined>
  expandedItems: Ref<string[] | undefined>
  indicator: boolean
  openIcon: string
  closeIcon: string
  fileIcon: string
  direction: 'rtl' | 'ltr'
  handleExpand: (id: string) => void
  selectItem: (id: string) => void
  setExpandedItems: (items: string[] | undefined) => void
}

export interface TreeViewElement {
  id: string
  name: string
  isSelectable?: boolean
  children?: TreeViewElement[]
}

export const TREE_CONTEXT_SYMBOL: InjectionKey<TreeContextProps> = Symbol('TREE_CONTEXT_SYMBOL')


