/**
 * 教师端：按课程加载作业选项（用于对比模式下的 A/B 集合选择）
 * - 避免页面内重复请求/重复解析
 * - 内置简单缓存：同一 courseId 复用结果
 */
import { ref, watch, type Ref } from 'vue'
import { assignmentApi } from '@/api/assignment.api'

export interface AssignmentOptionItem {
  id: string
  title: string
}

function extractAssignmentItems(res: any): any[] {
  // 兼容多种返回形态
  return res?.data?.items?.items || res?.data?.items || res?.items || []
}

export function useCourseAssignmentsOptions(selectedCourseId: Ref<string | null>, enabled: Ref<boolean>) {
  const assignmentOptions = ref<AssignmentOptionItem[]>([])
  const loading = ref(false)
  const lastLoadedCourseId = ref<string | null>(null)
  const cache = new Map<string, AssignmentOptionItem[]>()

  const clear = () => {
    assignmentOptions.value = []
    lastLoadedCourseId.value = null
  }

  const loadAssignments = async () => {
    if (!enabled.value) {
      clear()
      return
    }
    const cid = selectedCourseId.value
    if (!cid) {
      clear()
      return
    }
    if (lastLoadedCourseId.value === cid && assignmentOptions.value.length) return

    const cached = cache.get(cid)
    if (cached) {
      assignmentOptions.value = cached
      lastLoadedCourseId.value = cid
      return
    }

    loading.value = true
    try {
      const res: any = await assignmentApi.getAssignmentsByCourse(cid, { page: 1, size: 1000 } as any)
      const items = extractAssignmentItems(res)
      const normalized: AssignmentOptionItem[] = (items || []).map((it: any) => ({
        id: String(it.id),
        title: it.title || `#${it.id}`
      }))
      assignmentOptions.value = normalized
      cache.set(cid, normalized)
      lastLoadedCourseId.value = cid
    } catch {
      assignmentOptions.value = []
      lastLoadedCourseId.value = cid
    } finally {
      loading.value = false
    }
  }

  // 关闭对比模式：清空选项，避免 UI 误显示旧课程数据
  watch(enabled, (v) => {
    if (!v) clear()
  })

  // 切课：如处于 enabled，则允许由页面决定何时触发 loadAssignments（避免 watch 触发顺序导致重复请求）

  return { assignmentOptions, loading, loadAssignments, clear }
}

