<template>
  <div class="max-w-3xl mx-auto py-6">
    <div v-if="loading" class="text-center text-gray-500 dark:text-gray-400">{{ t('notifications.loading') }}</div>
    <div v-else-if="!notification" class="text-center text-gray-500 dark:text-gray-400">{{ t('notifications.empty') }}</div>
    <div v-else class="bg-white dark:bg-gray-800 shadow rounded-xl p-6 border border-gray-100 dark:border-gray-700">
      <div class="flex items-start justify-between">
        <div class="flex items-center gap-2">
          <div class="h-9 w-9">
            <UserAvatar v-if="avatarUrl" :avatar="avatarUrl" :size="36">
              <span class="text-xs">{{ avatarNameInitial }}</span>
            </UserAvatar>
            <span v-else class="inline-flex items-center justify-center h-9 w-9 rounded-full bg-primary-100 dark:bg-primary-900/40 text-primary-600 dark:text-primary-300 ring-1 ring-primary-200 dark:ring-primary-800">
              <svg v-if="notification.type==='message'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5"><path d="M8 10h8M8 14h5"/><path fill-rule="evenodd" d="M4 5a2 2 0 012-2h12a2 2 0 012 2v10a2 2 0 01-2 2H9l-4 4V5z" clip-rule="evenodd"/></svg>
              <svg v-else-if="notification.type==='assignment'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5"><path d="M8 6h8v2H8zM8 10h8v2H8zM8 14h6v2H8z"/><path d="M5 4h14a1 1 0 011 1v14l-4-3H5a1 1 0 01-1-1V5a1 1 0 011-1z"/></svg>
              <svg v-else-if="notification.type==='grade'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5"><path d="M12 17l-5 3 1.9-5.9L4 9h6L12 3l2 6h6l-4.9 5.1L17 20z"/></svg>
              <svg v-else-if="notification.type==='course'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5"><path d="M4 6h16v2H4zM4 10h16v2H4zM4 14h10v2H4z"/></svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5"><path d="M12 2a9 9 0 00-9 9h3l-4 4-4-4h3a12 12 0 1124 0h-3l4 4 4-4h-3a9 9 0 00-9-9z" /></svg>
            </span>
          </div>
          <div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ notification.title }}</h2>
            <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ formatTime(notification.createdAt) }}</p>
          </div>
        </div>
        <span class="text-xs px-2 py-1 rounded bg-primary-100 text-primary-700 dark:bg-primary-900/40 dark:text-primary-300 capitalize shadow-sm">{{ notification.type }}</span>
      </div>

      <div class="mt-4 p-4 rounded-lg bg-gray-50 dark:bg-gray-900/40 text-gray-700 dark:text-gray-200 whitespace-pre-line">
        {{ notification.content }}
      </div>

      <div class="mt-6 flex flex-wrap gap-3">
        <button v-if="!notification.isRead" @click="markRead" class="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-primary-600 text-white hover:bg-primary-700">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-4 h-4"><path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{ t('notifications.actions.markRead') }}
        </button>
        <button @click="goRelated" class="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-emerald-600 text-white hover:bg-emerald-700">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M13 7l5 5-5 5M6 12h12" /></svg>
          {{ t('notifications.actions.goRelated') }}
        </button>
        <button @click="goCenter" class="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-gray-100 text-gray-800 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-100 dark:hover:bg-gray-700 border border-gray-300 dark:border-gray-700">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" /></svg>
          {{ t('notifications.actions.backToCenter') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'
import { useChatStore } from '@/stores/chat'
import { storeToRefs } from 'pinia'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import UserAvatar from '@/components/ui/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const notificationsStore = useNotificationsStore()
const { currentNotification: notification, loading } = storeToRefs(notificationsStore)
const chatStore = useChatStore()

const id = route.params.id as string

onMounted(async () => {
  await notificationsStore.fetchNotificationDetail(id)
})

const markRead = async () => { await notificationsStore.markAsRead(id) }
const goCenter = () => { router.push('/teacher/notifications') }

const goRelated = async () => {
  const n: any = notification.value
  if (!n) return
  const rt = String(n.relatedType || '').toLowerCase()
  const t = String(n.type || '').toLowerCase()

  const to = () => {
    switch (rt || t) {
      case 'assignment':
      case 'assignment_deadline':
        return `/teacher/assignments/${n.relatedId}/submissions`
      case 'course':
      case 'course_update':
        return `/teacher/courses/${n.relatedId}`
      case 'grade':
      case 'grade_posted':
        return '/teacher/analytics'
      case 'community_post':
        return `/teacher/community/post/${n.relatedId}`
      case 'message':
        return '/teacher/notifications'
      default:
        return ''
    }
  }

  const target = to()
  if (target) {
    if ((rt || t) === 'message') {
      chatStore.openChat(n.relatedId as any, parseMeta().senderName || '')
    }
    return router.push(target)
  }
  return goCenter()
}

const formatTime = (timestamp: string) => new Date(timestamp).toLocaleString()

const parseMeta = () => {
  try {
    const raw: any = (notification.value as any)?.data
    if (!raw) return {}
    if (typeof raw === 'string') return JSON.parse(raw)
    if (typeof raw === 'object') return raw
  } catch {}
  return {}
}
const avatarUrl = computed(() => {
  const m: any = parseMeta()
  return m.senderAvatar || m.avatar || m.userAvatar || m.studentAvatar || m.teacherAvatar || ''
})
const avatarNameInitial = computed(() => {
  const m: any = parseMeta()
  const name = m.senderName || m.name || m.studentName || m.teacherName || ''
  return String(name || 'U').charAt(0).toUpperCase()
})
</script>


