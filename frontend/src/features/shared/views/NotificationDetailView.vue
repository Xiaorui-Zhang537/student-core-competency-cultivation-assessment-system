<template>
  <div class="max-w-3xl mx-auto py-6 px-4">
    <page-header :title="t('notifications.detail.title') || t('shared.notifications.title') || '通知详情'" />

    <div v-if="loading" class="text-center text-gray-500 dark:text-gray-400 py-12">{{ t('notifications.loading') || '加载中...' }}</div>
    <div v-else-if="!notification" class="text-center text-gray-500 dark:text-gray-400 py-12">{{ t('notifications.empty') || '暂无通知' }}</div>

    <div v-else class="space-y-4">
      <!-- 主卡片 -->
      <card padding="lg" :tint="typeTint">
        <!-- 顶部：图标/标题在左，查看详情/返回在右上 -->
        <div class="flex flex-wrap items-start gap-3">
          <div class="flex items-start gap-3 flex-1 min-w-0">
            <!-- 类型图标圆圈 -->
            <div class="shrink-0 w-10 h-10 rounded-full flex items-center justify-center" :class="iconBgClass">
              <component :is="typeIcon" class="w-5 h-5" :class="iconColorClass" />
            </div>

            <div class="flex-1 min-w-0">
              <!-- 标题 + Badges -->
              <div class="flex items-center flex-wrap gap-2">
                <h2 class="text-lg font-semibold text-base-content">{{ localizedTitle }}</h2>
                <!-- 类型 Badge -->
                <badge size="sm" :variant="typeVariant">{{ localizedType }}</badge>
                <!-- 优先级 Badge -->
                <badge size="sm" :variant="priorityVariant">{{ localizedPriority }}</badge>
                <!-- 已读状态 -->
                <badge v-if="notification.isRead" size="sm" variant="secondary">{{ t('notifications.status.read') || '已读' }}</badge>
                <badge v-else size="sm" variant="success">{{ t('notifications.status.unread') || '未读' }}</badge>
              </div>

              <!-- 时间 -->
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ formatDetailTime(notification.createdAt) }}</p>
            </div>
          </div>

          <div class="ml-auto flex shrink-0 items-center gap-2">
            <button variant="success" size="sm" @click="goRelated">
              <svg class="w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5l7 7-7 7"/></svg>
              {{ t('notifications.actions.goRelated') || '查看详情' }}
            </button>
            <button variant="outline" size="sm" @click="goCenter">
              <svg class="w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>
              {{ t('notifications.actions.backToCenter') || '返回通知中心' }}
            </button>
          </div>
        </div>

        <!-- 分隔线 -->
        <div class="border-t border-gray-200/50 dark:border-gray-700/50 my-4"></div>

        <!-- 内容区域：根据类型不同展示 -->
        <div class="space-y-3">
          <!-- 消息类型：显示发送者信息 -->
          <div v-if="nType === 'message' && senderName" class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300">
            <user-avatar v-if="avatarUrl" :avatar="avatarUrl" :size="24" />
            <span>{{ t('notifications.detail.from') || '来自' }}: <strong>{{ senderName }}</strong></span>
          </div>

          <!-- 作业/成绩类型：显示关联信息 -->
          <div v-if="(nType === 'assignment' || nType === 'grade') && metaInfo.assignmentTitle" class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300">
            <svg class="w-4 h-4 shrink-0 text-orange-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/></svg>
            <span>{{ t('notifications.detail.assignment') || '作业' }}: <strong>{{ metaInfo.assignmentTitle }}</strong></span>
          </div>

          <!-- 课程类型：显示课程名 -->
          <div v-if="nType === 'course' && metaInfo.courseTitle" class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300">
            <svg class="w-4 h-4 shrink-0 text-purple-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>
            <span>{{ t('notifications.detail.course') || '课程' }}: <strong>{{ metaInfo.courseTitle }}</strong></span>
          </div>

          <!-- 成绩信息 -->
          <div v-if="nType === 'grade' && metaInfo.score != null" class="flex items-center gap-2 text-sm">
            <svg class="w-4 h-4 shrink-0 text-green-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
            <span>{{ t('notifications.detail.score') || '成绩' }}: <strong class="text-green-600 dark:text-green-400">{{ metaInfo.score }}</strong></span>
          </div>

          <!-- 正文内容 -->
          <div class="p-4 rounded-xl text-base-content whitespace-pre-line leading-relaxed" style="background: color-mix(in oklab, var(--color-base-200) 30%, transparent)">
            {{ notification.content }}
          </div>
        </div>
      </card>

      <!-- 操作按钮 -->
      <div v-if="!notification.isRead" class="flex flex-wrap gap-3">
        <button variant="primary" size="sm" @click="markRead">
          <svg class="w-4 h-4 mr-1.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
          {{ t('notifications.actions.markRead') || '标记已读' }}
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
import { useI18n } from 'vue-i18n'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import Badge from '@/components/ui/Badge.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { submissionApi } from '@/api/submission.api'
import { gradeApi } from '@/api/grade.api'
import {
  BellIcon,
  InformationCircleIcon,
  DocumentTextIcon,
  AcademicCapIcon,
  ChatBubbleLeftRightIcon,
  StarIcon,
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const notificationsStore = useNotificationsStore()
const { currentNotification: notification, loading } = storeToRefs(notificationsStore)
const chatStore = useChatStore()

const id = route.params.id as string
const isTeacher = computed(() => String(route.path || '').startsWith('/teacher'))
const base = computed(() => (isTeacher.value ? '/teacher' : '/student'))

onMounted(async () => {
  await notificationsStore.fetchNotificationDetail(id)
})

// ---- 类型相关计算属性 ----
const nType = computed(() => String(notification.value?.type || '').toLowerCase())

const typeIcon = computed(() => {
  const map: Record<string, any> = { system: InformationCircleIcon, assignment: DocumentTextIcon, grade: StarIcon, course: AcademicCapIcon, message: ChatBubbleLeftRightIcon, post: ChatBubbleLeftRightIcon }
  return map[nType.value] || BellIcon
})

const iconBgClass = computed(() => {
  const map: Record<string, string> = { system: 'bg-blue-100 dark:bg-blue-900/30', assignment: 'bg-orange-100 dark:bg-orange-900/30', grade: 'bg-green-100 dark:bg-green-900/30', course: 'bg-purple-100 dark:bg-purple-900/30', message: 'bg-indigo-100 dark:bg-indigo-900/30', post: 'bg-pink-100 dark:bg-pink-900/30' }
  return map[nType.value] || 'bg-gray-100 dark:bg-gray-800'
})

const iconColorClass = computed(() => {
  const map: Record<string, string> = { system: 'text-blue-600 dark:text-blue-400', assignment: 'text-orange-600 dark:text-orange-400', grade: 'text-green-600 dark:text-green-400', course: 'text-purple-600 dark:text-purple-400', message: 'text-indigo-600 dark:text-indigo-400', post: 'text-pink-600 dark:text-pink-400' }
  return map[nType.value] || 'text-gray-600 dark:text-gray-400'
})

const typeTint = computed(() => {
  const map: Record<string, string> = { system: 'primary', assignment: 'warning', grade: 'success', course: 'accent', message: 'secondary', post: 'secondary' }
  return map[nType.value] || 'secondary'
})

const typeVariant = computed(() => {
  const map: Record<string, string> = { system: 'info', assignment: 'warning', grade: 'success', course: 'default', message: 'secondary', post: 'secondary' }
  return map[nType.value] || 'default'
})

const localizedType = computed(() => {
  const keyMap: Record<string, string> = { system: 'notifications.types.system', assignment: 'notifications.types.assignment', grade: 'notifications.types.grade', course: 'notifications.types.course', message: 'notifications.types.message', post: 'notifications.types.post' }
  const key = keyMap[nType.value]
  return key ? (t(key) as string) : nType.value
})

const localizedTitle = computed(() => {
  const n: any = notification.value
  if (!n) return ''
  // 尝试使用 i18n 映射的标题
  const rt = String(n.relatedType || '').toLowerCase()
  const keyByRelated: Record<string, string> = {
    'assignment_created': 'notifications.categoryTitles.assignmentPublished',
    'assignment_updated': 'notifications.categoryTitles.assignmentGeneral',
    'assignment_returned': 'notifications.categoryTitles.assignmentGeneral',
    'grade_published': 'notifications.categoryTitles.gradePosted',
    'deadline_reminder': 'notifications.categoryTitles.assignmentDeadline',
  }
  if (keyByRelated[rt]) {
    const val = t(keyByRelated[rt]) as string
    if (val && !val.startsWith('notifications.')) return val
  }
  return String(n.title || '')
})

const priorityVariant = computed(() => {
  const p = String(notification.value?.priority || 'normal').toLowerCase()
  if (p === 'urgent') return 'danger'
  if (p === 'high') return 'warning'
  if (p === 'normal') return 'info'
  return 'secondary'
})

const localizedPriority = computed(() => {
  const p = String(notification.value?.priority || 'normal').toLowerCase()
  const keyMap: Record<string, string> = { urgent: 'notifications.priority.urgent', high: 'notifications.priority.high', normal: 'notifications.priority.normal', low: 'notifications.priority.low' }
  const key = keyMap[p] || keyMap.normal
  return t(key) as string
})

// ---- 元数据解析 ----
const parseMeta = () => {
  try {
    const raw: any = (notification.value as any)?.data
    if (!raw) return {}
    if (typeof raw === 'string') return JSON.parse(raw)
    if (typeof raw === 'object') return raw
  } catch {}
  return {}
}

const metaInfo = computed(() => {
  const m: any = parseMeta()
  return {
    assignmentTitle: m.assignmentTitle || m.title || '',
    courseTitle: m.courseTitle || m.courseName || '',
    score: m.score || m.finalScore || null,
    senderName: m.senderName || m.username || '',
    senderAvatar: m.senderAvatar || m.avatar || '',
  }
})

const senderName = computed(() => {
  const m = metaInfo.value
  return m.senderName || ''
})

const avatarUrl = computed(() => metaInfo.value.senderAvatar || '')

// ---- 时间格式化 ----
const formatDetailTime = (timestamp: string) => {
  if (!timestamp) return ''
  const d = new Date(timestamp)
  const loc = String(locale.value || 'zh-CN')
  return d.toLocaleString(loc, { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// ---- 操作 ----
const markRead = async () => { await notificationsStore.markAsRead(id) }
const goCenter = () => { router.push(`${base.value}/notifications`) }

const goRelated = async () => {
  const n: any = notification.value
  if (!n) return
  const rt = String(n.relatedType || '').toLowerCase()
  const tp = String(n.type || '').toLowerCase()

  // 消息类型：打开聊天
  if (tp === 'message') {
    const data: any = parseMeta()
    const myId = String(localStorage.getItem('userId') || '')
    const senderId = n.senderId != null ? String(n.senderId) : (data?.senderId != null ? String(data.senderId) : '')
    const recipientId = n.recipientId != null ? String(n.recipientId) : (data?.recipientId != null ? String(data.recipientId) : '')
    const peer = senderId && senderId !== myId ? senderId : (recipientId && recipientId !== myId ? recipientId : senderId || recipientId || '')
    const peerName = data?.senderName || n.senderName || n.title || ''
    const cid = String(n.relatedId || '') || undefined
    if (peer) { chatStore.openChat(peer, peerName, cid as any); return }
    return goCenter()
  }

  // 帖子类型：跳转帖子详情
  if (tp === 'post') {
    const postId = n.relatedId || (() => { try { const d = typeof n.data === 'string' ? JSON.parse(n.data) : n.data; return d?.postId } catch { return undefined } })()
    if (postId) { router.push(`${base.value}/community/post/${postId}`); return }
    return goCenter()
  }

  // 作业/成绩/课程类型
  async function resolveAssignmentId(): Promise<string> {
    try {
      const meta: any = parseMeta()
      if (meta?.assignmentId) return String(meta.assignmentId)
      if ((tp === 'grade') && rt === 'assignment' && n.relatedId) return String(n.relatedId)
      const maybeGradeId = String(meta?.gradeId || meta?.id || '')
      if (maybeGradeId && tp === 'grade') {
        try { const gres: any = await gradeApi.getGradeById(maybeGradeId); const g = gres?.data ?? gres; if (g?.assignmentId) return String(g.assignmentId) } catch {}
      }
      if (rt === 'assignment' && n.relatedId) return String(n.relatedId)
      return ''
    } catch { return '' }
  }

  switch (rt || tp) {
    case 'assignment':
    case 'assignment_created':
    case 'assignment_updated':
    case 'assignment_returned':
    case 'assignment_deadline':
    case 'deadline_reminder': {
      const aid = await resolveAssignmentId()
      if (aid) { router.push(isTeacher.value ? `${base.value}/assignments/${aid}/submissions` : `${base.value}/assignments/${aid}/submit`); return }
      break
    }
    case 'grade':
    case 'grade_published': {
      const aid = await resolveAssignmentId()
      if (aid) { router.push(isTeacher.value ? `${base.value}/assignments/${aid}/submissions` : `${base.value}/assignments/${aid}/submit`); return }
      router.push(`${base.value}/assignments`); return
    }
    case 'course':
    case 'course_update':
      if (n.relatedId) { router.push(`${base.value}/courses/${n.relatedId}`); return }
      break
  }
  goCenter()
}
</script>
