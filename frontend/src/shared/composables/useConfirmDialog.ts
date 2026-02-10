/**
 * 轻量确认弹窗：Promise<boolean> 风格
 * - 由页面渲染 `ConfirmDialog.vue`（统一玻璃风格）
 */
import { ref, reactive } from 'vue'

export interface ConfirmDialogOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  confirmVariant?: string
}

export function useConfirmDialog(defaults: Partial<ConfirmDialogOptions> = {}) {
  const open = ref(false)
  const state = reactive<ConfirmDialogOptions>({
    title: defaults.title || '',
    message: defaults.message || '',
    confirmText: defaults.confirmText || '',
    cancelText: defaults.cancelText || '',
    confirmVariant: defaults.confirmVariant || 'danger',
  })

  let resolver: ((v: boolean) => void) | null = null

  const confirm = (opts: ConfirmDialogOptions) => {
    state.title = opts.title
    state.message = opts.message
    state.confirmText = opts.confirmText ?? defaults.confirmText ?? ''
    state.cancelText = opts.cancelText ?? defaults.cancelText ?? ''
    state.confirmVariant = opts.confirmVariant ?? defaults.confirmVariant ?? 'danger'
    open.value = true
    return new Promise<boolean>((resolve) => {
      resolver = resolve
    })
  }

  const close = (result: boolean) => {
    open.value = false
    try { resolver?.(result) } finally { resolver = null }
  }

  const onConfirm = () => close(true)
  const onCancel = () => close(false)

  return { open, state, confirm, onConfirm, onCancel }
}

