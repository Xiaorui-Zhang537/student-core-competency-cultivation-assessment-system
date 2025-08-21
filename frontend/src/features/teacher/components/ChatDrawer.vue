<template>
  <div v-if="open" class="fixed inset-0 z-50">
    <div class="absolute inset-0 bg-black/30" @click="emit('close')"></div>
    <div class="absolute right-0 top-0 h-full w-full sm:w-[420px] bg-white dark:bg-gray-800 shadow-xl flex flex-col">
      <div class="p-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
        <div class="font-semibold text-gray-900 dark:text-white">{{ (t('teacher.students.chat.title', { name: peerName || title }) as string) }}</div>
        <button class="text-gray-500 hover:text-gray-700" @click="emit('close')">✕</button>
      </div>
      <div class="flex-1 overflow-y-auto p-4 space-y-3" ref="scrollContainer">
        <div v-for="m in messages" :key="m.id" :class="m.isMine ? 'text-right' : 'text-left'">
          <div :class="['inline-block px-3 py-2 rounded-lg', m.isMine ? 'bg-blue-600 text-white' : 'bg-gray-100 dark:bg-gray-700 dark:text-gray-100']">
            <div class="whitespace-pre-wrap break-words">{{ m.content }}</div>
            <div class="text-[10px] opacity-70 mt-1">{{ formatTime(m.createdAt) }}</div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="text-center text-gray-500 dark:text-gray-400 py-10">{{ emptyText }}</div>
      </div>
      <div class="p-3 border-t border-gray-200 dark:border-gray-700 flex items-center gap-2 relative">
        <button type="button" class="btn btn-ghost btn-sm flex items-center" @click="toggleEmoji">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5 mr-1"><path d="M12 2a10 10 0 100 20 10 10 0 000-20zm0 2a8 8 0 110 16A8 8 0 0112 4zm-3 7a1 1 0 100-2 1 1 0 000 2zm8 0a1 1 0 100-2 1 1 0 000 2zm-8.536 3.464a1 1 0 011.414 0A4 4 0 0012 16a4 4 0 002.121-.536 1 1 0 011.05 1.7A6 6 0 0112 18a6 6 0 01-3.172-.836 1 1 0 01-.364-1.7z"/></svg>
          {{ t('shared.emojiPicker.button') }}
        </button>
        <input ref="draftInput" v-model="draft" class="input flex-1" :placeholder="t('teacher.students.chat.placeholder') as string" @keydown.enter.prevent="send" />
        <button class="btn btn-primary" :disabled="sending || !draft" @click="send">{{ t('teacher.ai.send') }}</button>

        <div v-if="showEmoji" class="absolute bottom-12 left-2 z-50 p-2 w-60 max-h-56 overflow-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow grid grid-cols-8 gap-1">
          <button v-for="(e, idx) in emojiList" :key="idx" type="button" class="text-xl hover:bg-gray-100 dark:hover:bg-gray-700 rounded" @click="pickEmoji(e)">{{ e }}</button>
        </div>
      </div>
    </div>
  </div>
  </template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'

const props = defineProps<{
  open: boolean
  peerId: string | number
  courseId?: string | number
  peerName?: string
}>()
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
    await notificationAPI.sendNotification(payload)
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

onMounted(async () => {
  title.value = props.peerName ? `${props.peerName}` : (t('teacher.students.table.message') as string)
  await load()
})

watch(() => props.open, async (v) => { if (v) await load() })
watch(() => props.peerId, async () => { await load() })
</script>

<style scoped>
.input { @apply border rounded px-3 py-2 text-sm bg-white dark:bg-gray-900 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-gray-100; }
.btn { @apply inline-flex items-center justify-center rounded px-3 py-2 text-sm font-medium; }
.btn-primary { @apply bg-blue-600 text-white hover:bg-blue-700; }
</style>


