<template>
  <div v-if="open" class="fixed inset-0 z-50">
    <div class="absolute inset-0 bg-black/30" @click="emit('close')"></div>
    <div class="absolute right-0 top-0 h-full w-full sm:w-[820px] bg-white dark:bg-gray-800 shadow-xl flex flex-col">
      <!-- 顶部标题栏 -->
      <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <div class="font-semibold text-gray-900 dark:text-white">{{ headerTitle }}</div>
        <button class="text-gray-500 hover:text-gray-700" @click="emit('close')">✕</button>
      </div>

      <!-- 主体：左侧列表 + 右侧会话 -->
      <div class="flex flex-1 min-h-0">
        <!-- 左侧：最近/联系人 列表 -->
        <div class="hidden sm:flex sm:flex-col w-64 border-r border-gray-200 dark:border-gray-700">
          <div class="px-3 pt-3 pb-2 flex items-center gap-2">
            <button :class="['px-3 py-1 rounded text-sm', activeTab==='recent' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 dark:text-gray-100']" @click="activeTab='recent'">{{ t('shared.chat.recent') || '最近' }}</button>
            <button :class="['px-3 py-1 rounded text-sm', activeTab==='contacts' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 dark:text-gray-100']" @click="activeTab='contacts'">{{ t('shared.chat.contacts') || '联系人' }}</button>
            <button :class="['px-3 py-1 rounded text-sm', activeTab==='system' ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 dark:text-gray-100']" @click="activeTab='system'">{{ t('shared.chat.system') || '系统消息' }}</button>
          </div>
          <div class="px-3 pb-2">
            <input v-model="keyword" :placeholder="t('shared.chat.searchPlaceholder') as string || '搜索联系人'" class="input w-full" />
          </div>
          <div class="flex-1 overflow-y-auto px-2 pb-3 space-y-1">
            <!-- 最近会话列表 -->
            <template v-if="activeTab==='recent'">
              <div v-if="chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.loading') || '加载中...' }}</div>
              <button
                v-for="c in recentList"
                :key="c.id || c.notificationId || c._k"
                :class="['group w-full text-left px-3 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700', String(c.peerId)===String(peerActiveId) ? 'bg-blue-50 dark:bg-blue-900/30' : '']"
                @click="choosePeer(c.peerId, c.displayName, c.courseId)"
              >
                <div class="flex items-center gap-3">
                  <UserAvatar :avatar="c.avatar || getContactAvatar(c.peerId)" :size="28">
                    <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700"></div>
                  </UserAvatar>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between">
                      <div class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ c.displayName }}</div>
                      <span v-if="c.unread > 0" class="ml-2 inline-flex items-center justify-center w-5 h-5 rounded-full bg-red-500 text-white text-xs">{{ c.unread }}</span>
                    </div>
                    <div class="text-xs text-gray-500 dark:text-gray-300 truncate">{{ c.content || c.preview }}</div>
                  </div>
                  <div class="flex items-center gap-1 ml-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      :title="(chat.isPinned(c.peerId, props.courseId||null) ? t('shared.chat.unpin') : t('shared.chat.pin')) as string"
                      class="p-1 rounded-full hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-500 dark:text-gray-400"
                      @click.stop="togglePinAction(c.peerId)"
                    >
                      <component :is="chat.isPinned(c.peerId, props.courseId||null) ? BookmarkIcon : BookmarkSlashIcon" class="w-4 h-4" />
                    </button>
                    <button
                      :title="t('shared.chat.delete') as string"
                      class="p-1 rounded-full hover:bg-red-100 dark:hover:bg-red-900/30 text-red-500 dark:text-red-400"
                      @click.stop="deleteRecent(c.peerId)"
                    >
                      <XMarkIcon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </button>
              <div v-if="recentList.length===0 && !chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.chat.emptyList') || '暂无会话' }}</div>
            </template>

            <!-- 联系人列表（按课程分组，可折叠） -->
            <template v-else-if="activeTab==='contacts'">
              <div v-if="chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.loading') || '加载中...' }}</div>
              <div v-for="g in chat.contactGroups" :key="g.courseId" class="px-2">
                <button type="button" class="w-full flex items-center justify-between px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700"
                        @click="chat.toggleContactGroup(g.courseId)">
                  <div class="text-sm font-semibold text-gray-800 dark:text-gray-100 truncate">{{ g.courseName }}</div>
                  <svg :class="['w-4 h-4 transition-transform', g.expanded ? 'rotate-90' : '']" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M6 6L14 10L6 14V6Z" clip-rule="evenodd"/></svg>
                </button>
                <div v-show="g.expanded" class="mt-1 space-y-1">
                  <button
                    v-for="p in g.students"
                    :key="p.id"
                    :class="['w-full text-left px-3 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700', String(p.id)===String(peerActiveId) ? 'bg-blue-50 dark:bg-blue-900/30' : '']"
                    @click="choosePeer(p.id, p.name, g.courseId)"
                  >
                    <div class="flex items-center gap-3">
                      <UserAvatar :avatar="p.avatar" :size="28">
                        <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700"></div>
                      </UserAvatar>
                      <div class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ p.name || ('#'+p.id) }}</div>
                    </div>
                  </button>
                </div>
              </div>
              <div v-if="(!chat.contactGroups || chat.contactGroups.length===0) && !chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.chat.emptyList') || '暂无联系人' }}</div>
            </template>

            <!-- 系统消息列表（点击查看详情） -->
            <template v-else>
              <div v-if="chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.loading') || '加载中...' }}</div>
              <button
                v-for="n in chat.systemMessages"
                :key="n.id"
                class="w-full px-3 py-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-left"
                :class="{ 'bg-blue-50 dark:bg-blue-900/30': String(selectedSystem?.id||'')===String(n.id) }"
                @click="chooseSystem(n)"
              >
                <div class="flex items-center gap-3">
                  <UserAvatar :avatar="''" :size="28">
                    <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center text-xs">S</div>
                  </UserAvatar>
                  <div class="flex-1 min-w-0">
                    <div class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ n.title || '系统' }}</div>
                    <div class="text-xs text-gray-500 dark:text-gray-300 truncate">{{ n.content }}</div>
                  </div>
                </div>
              </button>
              <div v-if="(!chat.systemMessages || chat.systemMessages.length===0) && !chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.chat.emptyList') || '暂无系统消息' }}</div>
            </template>
          </div>
        </div>

        <!-- 右侧：会话区或占位 -->
        <div class="flex-1 flex flex-col min-w-0">
          <div v-if="hasActivePeer && activeTab!=='system'" class="flex-1 overflow-y-auto p-4 space-y-3" ref="scrollContainer">
        <div v-for="m in messages" :key="m.id" :class="m.isMine ? 'text-right' : 'text-left'">
          <div :class="['inline-block px-3 py-2 rounded-lg', m.isMine ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 dark:text-gray-100']">
            <div class="whitespace-pre-wrap break-words">{{ m.content }}</div>
            <div class="text-[10px] opacity-70 mt-1">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="text-center text-gray-500 dark:text-gray-400 py-10">{{ emptyText }}</div>
      </div>
          <div v-else-if="activeTab==='system' && selectedSystem" class="flex-1 overflow-y-auto p-4 space-y-3">
            <div class="space-y-2">
              <div class="text-base font-semibold text-gray-900 dark:text-white truncate">{{ selectedSystem.title || (t('shared.chat.system')||'系统消息') }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ formatTime(selectedSystem.createdAt || selectedSystem.created_at) }}</div>
              <div class="mt-2 whitespace-pre-wrap break-words text-sm text-gray-800 dark:text-gray-100">{{ selectedSystem.content }}</div>
            </div>
          </div>
          <div v-else class="flex-1 flex items-center justify-center text-sm text-gray-500 dark:text-gray-400 px-4">
            {{ t('shared.chat.pickSomeone') || '从左侧选择一位联系人开始聊天' }}
          </div>

          <!-- 输入区 -->
      <div v-if="activeTab!=='system'" class="p-3 border-t border-gray-200 dark:border-gray-700 flex items-center gap-2 relative">
            <button type="button" class="btn btn-ghost btn-sm flex items-center" :disabled="!hasActivePeer" @click="toggleEmoji">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5 mr-1"><path d="M12 2a10 10 0 100 20 10 10 0 000-20zm0 2a8 8 0 110 16A8 8 0 0112 4zm-3 7a1 1 0 100-2 1 1 0 000 2zm8 0a1 1 0 100-2 1 1 0 000 2zm-8.536 3.464a1 1 0 011.414 0A4 4 0 0012 16a4 4 0 002.121-.536 1 1 0 011.05 1.7A6 6 0 0112 18a6 6 0 01-3.172-.836 1 1 0 01-.364-1.7z"/></svg>
          {{ t('shared.emojiPicker.button') }}
        </button>
            <input ref="draftInput" v-model="draft" class="input flex-1" :disabled="!hasActivePeer" :placeholder="t('teacher.students.chat.placeholder') as string" @keydown.enter.prevent="send" />
            <button class="btn btn-primary" :disabled="sending || !draft || !hasActivePeer" @click="send">{{ t('teacher.ai.send') }}</button>

        <div v-if="showEmoji" class="absolute bottom-12 left-2 z-50 p-2 w-60 max-h-56 overflow-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow grid grid-cols-8 gap-1">
          <button v-for="(e, idx) in emojiList" :key="idx" type="button" class="text-xl hover:bg-gray-100 dark:hover:bg-gray-700 rounded" @click="pickEmoji(e)">{{ e }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { XMarkIcon, BookmarkIcon, BookmarkSlashIcon } from '@heroicons/vue/24/outline'

const props = defineProps<{
  open: boolean
  peerId?: string | number
  courseId?: string | number
  peerName?: string
}>()
import { computed } from 'vue'
import { useChatStore } from '@/stores/chat'
const chat = useChatStore()
const emit = defineEmits<{
  (e: 'close'): void
}>()

const { t } = useI18n()

const title = ref('')
const messages = ref<any[]>([])
const page = ref(1)
const size = ref(50)
const total = ref(0)
const draft = ref('')
// 移除标题
const sending = ref(false)
const draftInput = ref<HTMLInputElement | null>(null)
const showEmoji = ref(false)
const emojiList = [
  '😀','😁','😂','🤣','😊','😇','🙂','😉','😍','😘','😗','😄','😅','😆','😌','🤗',
  '🤔','🤨','😐','😑','😶','🙄','😏','😴','😪','😷','🤒','🤕','🤧','🥳','🤩','🥰',
  '👍','👎','🙏','👏','🙌','💪','🤝','👌','✌️','🤘','👋','🤙','🫶','❤️','💛','💙',
  '💜','🖤','🤍','🤎','💯','🔥','✨','🌟','🎉','🎊','🍀','🌈','🍻','☕'
]
const scrollContainer = ref<HTMLElement | null>(null)
const emptyText = t('teacher.students.table.empty') as string

// 左侧面板状态
const activeTab = ref<'recent' | 'contacts' | 'system'>('recent')
const keyword = ref('')
const hasActivePeer = computed(() => !!props.peerId)
const peerActiveId = computed(() => props.peerId ? String(props.peerId) : '')

const headerTitle = computed(() => {
  if (hasActivePeer.value) return (t('teacher.students.chat.title', { name: props.peerName || title.value }) as string)
  return t('shared.chat.open') as string || '聊天'
})

const recentList = computed(() => {
  const base = (chat.recentConversations || []).map((n: any, idx: number) => ({
    _k: idx,
    peerId: String(n.peerId || ''),
    courseId: n.courseId || '',
    displayName: n.peerName || '',
    avatar: n.avatar || '',
    content: n.content || '',
    preview: n.preview || '',
    unread: Number(n.unread || 0),
    lastAt: n.lastAt || 0
  }))
  // 确保当前会话在列表中（从学生管理/详情直达时）
  const activeId = props.peerId ? String(props.peerId) : ''
  const list = base.slice()
  if (activeId && !list.some((x: any) => String(x.peerId) === activeId)) {
    let displayName = props.peerName || ''
    if (!displayName && chat.contacts && activeId) {
      const found = (chat.contacts as any[]).find((c: any) => String(c.id) === activeId)
      if (found) displayName = found.name
    }
    list.push({ _k: -1, peerId: activeId, courseId: props.courseId || '', displayName, avatar: '', content: '', preview: '', unread: 0, lastAt: 0 })
  }
  // 排序：置顶优先 + 按最近时间倒序
  list.sort((a: any, b: any) => {
    const ap = chat.isPinned(a.peerId, a.courseId)
    const bp = chat.isPinned(b.peerId, b.courseId)
    if (ap !== bp) return ap ? -1 : 1
    return (b.lastAt || 0) - (a.lastAt || 0)
  })
  if (!keyword.value) return list
  const q = keyword.value.toLowerCase()
  return list.filter((x: any) => String(x.displayName || '').toLowerCase().includes(q))
})

const contactList = computed(() => {
  const list = chat.contacts || []
  if (!keyword.value) return list
  const q = keyword.value.toLowerCase()
  return list.filter((x: any) => String(x.name || '').toLowerCase().includes(q))
})

const formatTime = (ts: string) => {
  const d = new Date(ts)
  return isNaN(d.getTime()) ? '' : d.toLocaleString()
}

const scrollToBottom = async () => {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

const load = async () => {
  if (!props.peerId) return
  const { notificationAPI } = await import('@/api/notification.api')
  const res: any = await notificationAPI.getConversation(props.peerId, { page: page.value, size: size.value })
  const list = (res?.items || []).map((n: any) => ({
    id: n.id,
    content: n.content,
    createdAt: n.createdAt || n.created_at || new Date().toISOString(),
    isMine: n.senderId ? String(n.senderId) === String(localStorage.getItem('userId')) : false
  }))
  messages.value = list
  total.value = res?.total || list.length
  // 标记为已读
  try { await notificationAPI.readConversation(props.peerId) } catch {}
  await scrollToBottom()
}

const send = async () => {
  if (!draft.value) return
  try {
    sending.value = true
    const { notificationAPI } = await import('@/api/notification.api')
    const payload: any = {
      // @ts-ignore
      recipientId: Number(props.peerId),
      title: '',
      content: draft.value,
      type: 'message',
      category: 'course',
      relatedType: props.courseId ? 'course' : undefined,
      // @ts-ignore
      relatedId: props.courseId ? Number(props.courseId) : undefined
    }
    const sent: any = await notificationAPI.sendNotification(payload)
    const latestContent = payload.content
    const latestPreview = payload.content
    // 即时更新左侧最近会话顺序与预览
    chat.upsertRecentAfterSend(String(props.peerId || ''), String(props.courseId || ''), latestContent, latestPreview, props.peerName ?? undefined)
    draft.value = ''
    await load()
  } finally {
    sending.value = false
  }
}

const onEmoji = async (emoji: string) => {
  draft.value = (draft.value || '') + emoji
  await nextTick()
  draftInput.value?.focus()
}

const toggleEmoji = () => { showEmoji.value = !showEmoji.value }
const pickEmoji = async (e: string) => { await onEmoji(e); showEmoji.value = false }

const choosePeer = async (id: string | number, name?: string | null, cId?: string | number | null) => {
  chat.setPeer(id, name ?? null, (cId ?? props.courseId) ?? null)
}

const selectedSystem = ref<any | null>(null)
const chooseSystem = async (n: any) => {
  selectedSystem.value = n
  try {
    const { notificationAPI } = await import('@/api/notification.api')
    if (n?.id) await notificationAPI.markAsRead(String(n.id))
  } catch {}
  // 若系统消息包含 actionUrl（如帖子链接），支持直接跳转
  try {
    if (n?.actionUrl) {
      const { default: router } = await import('@/router')
      router.push(n.actionUrl)
    }
  } catch {}
}

const getContactAvatar = (pid: string | number) => {
  const found = (chat.contacts as any[] || []).find((c: any) => String(c.id) === String(pid))
  return found?.avatar || ''
}

const getLastContent = (pid: string | number) => {
  const conv = (chat.recentConversations as any[] || []).find((c: any) => String(c.peerId) === String(pid))
  return (conv && (conv.content || conv.preview)) || ''
}

const deleteRecent = (pid: string | number) => {
  chat.removeRecent(pid, props.courseId ?? null)
}

const togglePinAction = (pid: string | number) => {
  chat.togglePin(pid, props.courseId ?? null)
}

onMounted(async () => {
  title.value = props.peerName ? `${props.peerName}` : (t('teacher.students.table.message') as string)
  // 始终加载列表，保证左侧数据完整
  // 优先从当前上下文（props.courseId 或最近持久化）加载联系人，避免切入口不同导致联系人为空
  const persisted = (() => { try { return JSON.parse(localStorage.getItem('chat:recent')||'[]') } catch { return [] } })()
  const last = persisted && persisted[0]
  const cid = props.courseId || chat.courseId || (last?.courseId || undefined)
  await chat.loadLists({ courseId: cid })
  // 若已选中会话，再加载会话消息
  if (props.peerId) await load()
})

watch(() => props.open, async (v) => {
  if (v) {
    const persisted = (() => { try { return JSON.parse(localStorage.getItem('chat:recent')||'[]') } catch { return [] } })()
    const last = persisted && persisted[0]
    const cid = props.courseId || chat.courseId || (last?.courseId || undefined)
    await chat.loadLists({ courseId: cid })
    if (props.peerId) await load()
  }
})
watch(() => props.peerId, async () => { await load() })
</script>

<style scoped lang="postcss">
.input { @apply border rounded px-3 py-2 text-sm bg-white dark:bg-gray-900 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-gray-100; }
.btn { @apply inline-flex items-center justify-center rounded px-3 py-2 text-sm font-medium; }
.btn-primary { @apply bg-blue-600 text-white hover:bg-blue-700; }
</style>


