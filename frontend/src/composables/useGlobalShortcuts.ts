/**
 * 全局快捷键系统
 * 在布局层挂载，提供 Ctrl/Cmd+K 搜索、Esc 关闭等常用快捷键
 */
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'

export function useGlobalShortcuts() {
  const router = useRouter()
  const chat = useChatStore()

  function handler(e: KeyboardEvent) {
    const isMeta = e.metaKey || e.ctrlKey
    const tag = (e.target as HTMLElement)?.tagName?.toLowerCase() || ''
    const isInput = tag === 'input' || tag === 'textarea' || tag === 'select' || (e.target as HTMLElement)?.isContentEditable

    // Esc: 关闭聊天抽屉 / 弹层
    if (e.key === 'Escape') {
      if (chat.isOpen) {
        chat.closeChat()
        e.preventDefault()
        return
      }
      // 触发全局弹层关闭事件
      try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
      return
    }

    // Ctrl/Cmd + K: 全局搜索（跳转到课程页触发搜索）
    if (isMeta && e.key === 'k') {
      e.preventDefault()
      const p = router.currentRoute.value.path
      if (p.startsWith('/teacher')) {
        router.push('/teacher/courses')
      } else if (p.startsWith('/student')) {
        router.push('/student/courses')
      }
      return
    }

    // Ctrl/Cmd + Shift + N: 新建（根据上下文）
    if (isMeta && e.shiftKey && e.key === 'N') {
      e.preventDefault()
      const p = router.currentRoute.value.path
      if (p.startsWith('/teacher/assignments') || p.startsWith('/teacher/courses')) {
        router.push({ name: 'TeacherAssignments', query: { openCreate: '1' } })
      } else if (p.startsWith('/teacher/assistant')) {
        // AI 新对话由页面自身处理
      }
      return
    }

    // Ctrl/Cmd + Enter: 在非输入框内时，触发全局提交事件
    if (isMeta && e.key === 'Enter' && !isInput) {
      e.preventDefault()
      try { window.dispatchEvent(new CustomEvent('ui:global-submit')) } catch {}
      return
    }
  }

  onMounted(() => {
    window.addEventListener('keydown', handler)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handler)
  })
}
