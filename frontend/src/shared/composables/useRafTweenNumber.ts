/**
 * 使用 requestAnimationFrame 将数字平滑过渡到目标值
 * - 内置 onUnmounted 清理，避免卸载后仍更新响应式状态
 */
import { ref, watch, onUnmounted, type Ref } from 'vue'

export interface RafTweenNumberOptions {
  duration?: number
  initial?: number
}

export function useRafTweenNumber(target: Ref<number>, options: RafTweenNumberOptions = {}) {
  const duration = Math.max(0, Number(options.duration ?? 300))
  const value = ref(Number(options.initial ?? 0))

  let raf: number | null = null

  const cancel = () => {
    if (raf != null) cancelAnimationFrame(raf)
    raf = null
  }

  watch(target, (to) => {
    const start = Number(value.value || 0)
    const end = Number(to || 0)
    if (!Number.isFinite(end)) return
    if (duration <= 0) {
      value.value = end
      return
    }
    const startTs = performance.now()
    cancel()
    const tick = (now: number) => {
      const p = Math.min(1, (now - startTs) / duration)
      value.value = start + (end - start) * p
      if (p < 1) raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
  })

  onUnmounted(() => cancel())

  return { value, cancel }
}

