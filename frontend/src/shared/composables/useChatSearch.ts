/**
 * 聊天消息搜索 composable
 * 支持在当前会话消息中进行本地搜索并高亮跳转
 */
import { ref, computed } from 'vue'

export interface SearchableMessage {
  id: string | number
  content: string
  [key: string]: any
}

export function useChatSearch(messages: () => SearchableMessage[]) {
  const searchQuery = ref('')
  const isSearching = ref(false)
  const currentMatchIndex = ref(0)

  const matches = computed(() => {
    const q = searchQuery.value.trim().toLowerCase()
    if (!q) return []
    return messages()
      .map((m, idx) => ({ ...m, _index: idx }))
      .filter(m => m.content && m.content.toLowerCase().includes(q))
  })

  const totalMatches = computed(() => matches.value.length)

  const currentMatch = computed(() => {
    if (!matches.value.length) return null
    const idx = Math.max(0, Math.min(currentMatchIndex.value, matches.value.length - 1))
    return matches.value[idx]
  })

  function openSearch() {
    isSearching.value = true
    searchQuery.value = ''
    currentMatchIndex.value = 0
  }

  function closeSearch() {
    isSearching.value = false
    searchQuery.value = ''
    currentMatchIndex.value = 0
  }

  function nextMatch() {
    if (matches.value.length === 0) return
    currentMatchIndex.value = (currentMatchIndex.value + 1) % matches.value.length
  }

  function prevMatch() {
    if (matches.value.length === 0) return
    currentMatchIndex.value = (currentMatchIndex.value - 1 + matches.value.length) % matches.value.length
  }

  /** 高亮文本中的搜索关键词 */
  function highlightText(text: string): string {
    const q = searchQuery.value.trim()
    if (!q || !text) return text
    const escaped = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark class="bg-yellow-200 dark:bg-yellow-700/60 rounded px-0.5">$1</mark>')
  }

  return {
    searchQuery,
    isSearching,
    currentMatchIndex,
    matches,
    totalMatches,
    currentMatch,
    openSearch,
    closeSearch,
    nextMatch,
    prevMatch,
    highlightText,
  }
}
