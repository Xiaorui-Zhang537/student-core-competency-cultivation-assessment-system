import { ref } from 'vue'
import { baseURL } from '@/api/config'
import { useNotificationsStore } from '@/stores/notifications'
import { useUIStore } from '@/stores/ui'

// Factory to create a stream controller. Caller decides when to connect/disconnect.
export function createNotificationStream() {
  const es = ref<EventSource | null>(null)
  const connected = ref(false)
  const notificationsStore = useNotificationsStore()

  const connect = () => {
    if (es.value) return
    const token = localStorage.getItem('token')
    if (!token) return

    const url = `${baseURL.replace(/\/$/, '')}/notifications/stream?token=${encodeURIComponent(token.replace(/^Bearer\s+/i, ''))}`
    const source = new EventSource(url, { withCredentials: true })
    es.value = source

    source.addEventListener('connected', async () => {
      connected.value = true
      await notificationsStore.refreshStatsFromSse()
    })

    source.addEventListener('new', async (ev: MessageEvent) => {
      try {
        const payload = ev?.data ? JSON.parse(ev.data) : null
        if (payload && typeof payload === 'object') {
          notificationsStore.insertOrUpdateFromSse(payload)
          // 弹出右上角 toast（排除聊天消息，聊天由 ChatDrawer 自行处理）
          const pType = String(payload.type || '').toLowerCase()
          if (pType !== 'message') {
            try {
              const uiStore = useUIStore()
              uiStore.showNotification({
                type: 'info',
                title: String(payload.title || '新通知'),
                message: String(payload.content || '').slice(0, 100),
              })
            } catch {}
          }
        }
      } catch {}
      await notificationsStore.fetchUnreadCount()
    })

    source.addEventListener('update', async (ev: MessageEvent) => {
      try {
        const payload = ev?.data ? JSON.parse(ev.data) : null
        if (payload && typeof payload === 'object') {
          await notificationsStore.applyReadUpdateFromSse(payload)
          return
        }
      } catch {}
      await notificationsStore.refresh()
    })

    source.addEventListener('delete', async (ev: MessageEvent) => {
      try {
        const payload = ev?.data ? JSON.parse(ev.data) : null
        const id = payload?.id ?? payload?.notificationId
        if (id != null) {
          notificationsStore.removeByIdFromSse(id)
        }
      } catch {}
      await notificationsStore.refreshStatsFromSse()
    })

    source.addEventListener('stats', async () => {
      await notificationsStore.refreshStatsFromSse()
    })

    source.onerror = () => {
      disconnect()
      // Basic backoff reconnect
      setTimeout(connect, 3000)
    }
  }

  const disconnect = () => {
    if (es.value) {
      es.value.close()
      es.value = null
    }
    connected.value = false
  }

  return { connect, disconnect, connected }
}


