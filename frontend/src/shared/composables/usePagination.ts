/**
 * 通用分页状态管理（page/pageSize/totalPages + 纠正越界）
 */
import { ref, computed, watch, type Ref } from 'vue'

export interface UsePaginationOptions {
  initialPage?: number
  initialPageSize?: number
}

export function usePagination(totalItems: Ref<number>, options: UsePaginationOptions = {}) {
  const page = ref(Math.max(1, Number(options.initialPage || 1)))
  const pageSize = ref(Math.max(1, Number(options.initialPageSize || 20)))

  const totalPages = computed(() => {
    const total = Math.max(0, Number(totalItems.value || 0))
    const size = Math.max(1, Number(pageSize.value || 1))
    return Math.max(1, Math.ceil(total / size))
  })

  watch([totalPages, page], () => {
    if (page.value < 1) page.value = 1
    if (page.value > totalPages.value) page.value = totalPages.value
  })

  const setPage = (p: number) => {
    page.value = Math.max(1, Math.floor(Number(p || 1)))
  }

  const setPageSize = (s: number, resetPage = true) => {
    pageSize.value = Math.max(1, Math.floor(Number(s || 1)))
    if (resetPage) page.value = 1
  }

  return { page, pageSize, totalPages, setPage, setPageSize }
}

