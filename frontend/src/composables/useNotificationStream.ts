import { ref } from 'vue'
import { baseURL } from '@/api/config'
import { useNotificationsStore } from '@/stores/notifications'

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
      await notificationsStore.fetchUnreadCount()
      await notificationsStore.fetchStats()
    })

    source.addEventListener('new', async () => {
      await notificationsStore.fetchNotifications(true)
      await notificationsStore.fetchUnreadCount()
      await notificationsStore.fetchStats()
    })

    source.addEventListener('update', async () => {
      await notificationsStore.fetchUnreadCount()
      await notificationsStore.fetchStats()
    })

    source.addEventListener('delete', async () => {
      await notificationsStore.fetchNotifications(true)
      await notificationsStore.fetchUnreadCount()
      await notificationsStore.fetchStats()
    })

    source.addEventListener('stats', async () => {
      await notificationsStore.fetchUnreadCount()
      await notificationsStore.fetchStats()
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


