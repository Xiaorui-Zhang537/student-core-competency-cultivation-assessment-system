import { ref } from 'vue'
import { adminApi } from '@/api/admin.api'

type CacheEntry = { at: number; value: number }

const countCache = new Map<string, CacheEntry>()

function num(v: any): number {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

async function cachedCount(key: string, fetcher: () => Promise<number>, ttlMs = 12_000): Promise<number> {
  const now = Date.now()
  const hit = countCache.get(key)
  if (hit && now - hit.at <= ttlMs) return hit.value
  const value = await fetcher()
  countCache.set(key, { at: now, value })
  return value
}

export function useAdminCounts() {
  const loading = ref(false)
  const error = ref<string | null>(null)

  function clearCache(prefix?: string) {
    if (!prefix) {
      countCache.clear()
      return
    }
    Array.from(countCache.keys()).forEach((k) => {
      if (k.startsWith(prefix)) countCache.delete(k)
    })
  }

  async function countUsers(params: { role?: string; status?: string; keyword?: string; includeDeleted?: boolean }, ttlMs?: number) {
    const key = `users:${JSON.stringify(params || {})}`
    return cachedCount(
      key,
      async () => {
        const res = await adminApi.pageUsers({ page: 1, size: 1, ...params })
        return num((res as any)?.total)
      },
      ttlMs
    )
  }

  async function countCourses(params: { status?: string; query?: string; category?: string; difficulty?: string; teacherId?: number }, ttlMs?: number) {
    const key = `courses:${JSON.stringify(params || {})}`
    return cachedCount(
      key,
      async () => {
        const res: any = await adminApi.pageCourses({ page: 1, size: 1, ...params })
        return num(res?.total)
      },
      ttlMs
    )
  }

  async function countReports(status: string | undefined, ttlMs?: number) {
    const key = `reports:${String(status || '')}`
    return cachedCount(
      key,
      async () => {
        const res = await adminApi.pageReports({ status: status || undefined, page: 1, size: 1 })
        return num((res as any)?.total)
      },
      ttlMs
    )
  }

  async function countPosts(params: { status?: string; keyword?: string; category?: string; pinned?: boolean; includeDeleted?: boolean }, ttlMs?: number) {
    const key = `posts:${JSON.stringify(params || {})}`
    return cachedCount(
      key,
      async () => {
        const res = await adminApi.pageCommunityPosts({ page: 1, size: 1, ...params })
        return num((res as any)?.total)
      },
      ttlMs
    )
  }

  async function countComments(params: { status?: string; keyword?: string; postId?: number; includeDeleted?: boolean }, ttlMs?: number) {
    const key = `comments:${JSON.stringify(params || {})}`
    return cachedCount(
      key,
      async () => {
        const res = await adminApi.pageCommunityComments({ page: 1, size: 1, ...params })
        return num((res as any)?.total)
      },
      ttlMs
    )
  }

  return {
    loading,
    error,
    clearCache,
    countUsers,
    countCourses,
    countReports,
    countPosts,
    countComments,
  }
}

