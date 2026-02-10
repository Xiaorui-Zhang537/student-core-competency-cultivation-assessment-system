<template>
  <teleport to="body">
  <div v-if="open" class="fixed inset-0 z-[12000]" role="dialog" aria-modal="true" :aria-labelledby="'chatDrawerTitle'">
    <!-- 透明遮罩：不变暗，仅用于点击关闭 -->
    <div class="absolute inset-0 z-[0] bg-transparent" @click="emit('close')"></div>
    <!-- 侧边抽屉：右侧定位包裹，内部仅负责玻璃与内容 -->
    <div class="absolute inset-y-0 right-0 w-[92vw] sm:w-[820px] z-[10]">
      <div
        class="w-full h-full rounded-l-2xl flex flex-col ring-1 shadow-2xl"
        :key="`shell-${activeTab}`"
        :style="{
          background: 'rgb(var(--glass-bg-rgb) / var(--glass-alpha-regular))',
          borderColor: 'var(--glass-border-color)',
          backdropFilter: 'blur(var(--glass-blur-regular)) saturate(var(--glass-saturate-regular)) contrast(var(--glass-contrast-regular))',
          WebkitBackdropFilter: 'blur(var(--glass-blur-regular)) saturate(var(--glass-saturate-regular)) contrast(var(--glass-contrast-regular))',
          boxShadow: 'var(--glass-inner-shadow), var(--glass-outer-shadow)'
        }"
      >
        <!-- 顶部标题栏（液态玻璃容器内的普通标题条） -->
        <div class="p-4 flex items-center justify-between" style="box-shadow: inset 0 -1px 0 rgba(255,255,255,0.18)">
          <div id="chatDrawerTitle" class="font-semibold text-gray-900 dark:text-white">{{ headerTitle }}</div>
          <Button variant="ghost" size="sm" @click="emit('close')">✕</Button>
        </div>

        <!-- 主体：左侧列表 + 右侧会话（整块由 LiquidGlass 提供半透明底，禁用折射） -->
        <div class="flex flex-1 min-h-0">
        <!-- 左侧：最近/联系人 列表 -->
        <div class="hidden sm:flex sm:flex-col w-64 shrink-0" :key="`left-${activeTab}`" :style="{ backgroundColor: 'var(--color-base-100)', boxShadow: 'inset -1px 0 rgba(255,255,255,0.18)' }">
          <div class="pt-3 pb-2 px-2">
            <div class="grid grid-cols-2 gap-2">
              <Button size="sm" :variant="activeTab==='recent' ? 'primary' : 'menu'" class="w-full justify-center" @click="activeTab='recent'">{{ t('shared.chat.recent') || '最近' }}</Button>
              <Button size="sm" :variant="activeTab==='contacts' ? 'primary' : 'menu'" class="w-full justify-center" @click="activeTab='contacts'">{{ t('shared.chat.contacts') || '联系人' }}</Button>
            </div>
          </div>
           <div class="px-3 pb-2" v-if="activeTab==='contacts'">
             <glass-search-input v-model="keyword" :placeholder="t('shared.chat.searchPlaceholder') as string || '搜索联系人'" size="sm" />
           </div>
            <div class="relative flex-1 overflow-y-auto px-2 pb-3 pt-3 space-y-1" :key="'list-'+activeTab" :style="{ backgroundColor: 'var(--color-base-100)' }">
            <div v-if="activeTab==='recent'" class="chatlist-surface">
              <div v-if="false"></div>
              <Button
                v-for="c in recentList"
                :key="c.id || c.notificationId || c._k"
                variant="menu"
                :class="['group w-full text-left px-0 py-3 min-h-[52px] rounded-lg transition-colors outline-none focus:outline-none focus-visible:outline-none ring-0 focus:ring-0 focus-visible:ring-0 focus:ring-offset-0 focus:shadow-none focus-visible:shadow-none hover:bg-transparent !justify-start !items-center', String(c.peerId)===String(peerActiveId) ? 'chat-selected' : '']"
                @click="choosePeer(c.peerId, c.displayName, c.courseId)"
              >
                <div class="flex items-center gap-4 pl-2">
                  <user-avatar :avatar="c.avatar || getContactAvatar(c.peerId)" :size="28">
                    <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700"></div>
                  </user-avatar>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 text-left">
                      <div class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ c.displayName }}</div>
                      <span v-if="c.unread > 0" class="glass-badge glass-badge-sm text-[11px] leading-none px-[6px]">{{ Math.min(Number(c.unread||0), 99) }}</span>
                    </div>
                    <div class="text-xs text-gray-500 dark:text-gray-300 truncate text-left">{{ c.content || c.preview }}</div>
                  </div>
                  <div class="flex items-center gap-1 ml-auto opacity-0 group-hover:opacity-100 transition-opacity">
                    <Button :title="(chat.isPinned(c.peerId, props.courseId||null) ? t('shared.chat.unpin') : t('shared.chat.pin')) as string" size="xs" variant="ghost" @click.stop="togglePinAction(c.peerId)">
                      <component :is="chat.isPinned(c.peerId, props.courseId||null) ? BookmarkIcon : BookmarkSlashIcon" class="w-4 h-4" />
                    </Button>
                    <Button :title="t('shared.chat.delete') as string" size="xs" variant="danger" @click.stop="deleteRecent(c.peerId)">
                      <x-mark-icon class="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </Button>
              <div v-if="recentList.length===0 && !chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.chat.emptyList') || '暂无会话' }}</div>
            </div>

            <div v-else-if="activeTab==='contacts'" class="chatlist-surface">
              <div v-if="false"></div>
              <div v-for="g in chat.contactGroups" :key="g.courseId" class="px-2">
                <Button type="button" variant="menu" class="w-full flex items-center justify-between px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 !justify-start" @click="onToggleGroup(g)">
                  <div class="text-sm font-semibold text-gray-800 dark:text-gray-100 truncate">{{ g.courseName }}</div>
                  <svg :class="['w-4 h-4 transition-transform', g.expanded ? 'rotate-90' : '']" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M6 6L14 10L6 14V6Z" clip-rule="evenodd"/></svg>
                </Button>
                <div v-show="g.expanded" class="mt-1 space-y-1">
                  <div v-if="g.loading" class="text-xs text-gray-500 dark:text-gray-300 px-3 py-2">{{ t('shared.loading') || '加载中...' }}</div>
                  <Button v-for="p in g.students" :key="p.id" variant="menu" :class="['w-full text-left px-0 py-3 min-h-[52px] rounded-lg transition-colors outline-none focus:outline-none focus-visible:outline-none ring-0 focus:ring-0 focus-visible:ring-0 focus:ring-offset-0 focus:shadow-none focus-visible:shadow-none hover:bg-transparent !justify-start !items-center', String(p.id)===String(peerActiveId) ? 'chat-selected' : '']" @click="choosePeer(p.id, p.name, g.courseId)">
                    <div class="flex items-center gap-4 pl-2">
                      <user-avatar :avatar="p.avatar" :size="28">
                        <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700"></div>
                      </user-avatar>
                      <div class="text-sm font-medium text-gray-900 dark:text-white truncate text-left">{{ displayUserName(p) || ('#'+p.id) }}</div>
                    </div>
                  </Button>
                </div>
              </div>
              <div v-if="(!chat.contactGroups || chat.contactGroups.length===0) && !chat.loadingLists" class="text-xs text-gray-500 px-2 py-2">{{ t('shared.chat.emptyList') || '暂无联系人' }}</div>
            </div>

            <div v-else class="chatlist-surface"></div>
          </div>
        </div>

        <!-- 右侧：会话区或占位 -->
        <div class="flex-1 flex flex-col min-w-0" :key="`content-${activeTab}-${peerActiveId}`" :style="{ backgroundColor: 'var(--color-base-100)' }">
          <div v-if="hasActivePeer" class="flex-1 overflow-y-auto p-4 space-y-3 no-scrollbar min-h-0" ref="scrollContainer" :key="`chat-${peerActiveId}`">
            <div v-if="loadingMessages" class="w-full h-full flex items-center justify-center py-16">
              <div class="flex flex-col items-center gap-3">
                <div class="theme-spinner theme-spinner-lg"></div>
                <div class="text-xs text-base-content/70">{{ t('shared.loading') || '加载中...' }}</div>
              </div>
            </div>
            <template v-if="!loadingMessages" v-for="item in renderedItems" :key="item.type==='message' ? item.data.id : item.key">
              <!-- 时间分隔条 -->
              <div v-if="item.type==='time-divider'" class="text-center my-2 text-xs text-gray-400 select-none">{{ item.timeText }}</div>

              <!-- 消息条目 -->
              <div v-else class="flex items-end gap-2" :class="item.data.isMine ? 'justify-end' : 'justify-start'">
                <!-- 对方头像（左） -->
                <user-avatar v-if="!item.data.isMine" :avatar="item.data.avatarUrl" :size="28">
                  <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700"></div>
                </user-avatar>

                <!-- 气泡与尾巴（仅当有文本时显示） -->
                <div v-if="hasText(item.data.content)" class="relative max-w-[70%]">
                  <div :class="[
                      'glass-bubble rounded-2xl px-3 py-2 whitespace-pre-wrap break-words',
                      item.data.isMine ? 'glass-bubble-mine text-white' : 'glass-bubble-peer text-base-content'
                    ]">
                    {{ item.data.content }}
                  </div>
                  <span v-if="!item.data.isMine" class="tail tail-left tail-peer"></span>
                  <span v-else class="tail tail-right tail-mine"></span>
                </div>

                <!-- 附件区域：图片网格 + 文件卡（支持灯箱） -->
                <div v-if="(item as any).data.attachmentFileIds && (item as any).data.attachmentFileIds.length>0" class="flex flex-col gap-2 max-w-[70%]" :class="item.data.isMine ? 'items-end' : 'items-start'">
                  <!-- 图片网格 -->
                  <div v-if="((item as any).data.attachmentFileIds as any[]).some(fid => canPreview(fid))" class="grid grid-cols-2 gap-2 w-full">
                    <div v-for="fid in (item as any).data.attachmentFileIds" :key="`att-img-${(item as any).data.id}-${fid}`" v-show="canPreview(fid)" class="relative group">
                      <img v-if="getPreviewUrl(fid)" :src="getPreviewUrl(fid)" class="w-full h-28 object-cover rounded-lg glass-img-border cursor-pointer" @click="openLightbox(((item as any).data.attachmentFileIds as any[]).filter((x:any)=>isImageAttachment(x)), fid)" />
                    </div>
                  </div>
                  <!-- 文件卡列表 -->
                  <div v-if="((item as any).data.attachmentFileIds as any[]).some(fid => !canPreview(fid))" class="space-y-2 w-full">
                    <div v-for="fid in (item as any).data.attachmentFileIds" :key="`att-file-${(item as any).data.id}-${fid}`" v-show="!canPreview(fid)" :class="['flex items-center justify-between p-2 rounded-lg', 'glass-attach', item.data.isMine ? 'glass-attach-mine' : 'glass-attach-peer']">
                      <div class="flex items-center gap-2 min-w-0">
                        <span class="inline-block w-6 h-6 rounded bg-gray-300 dark:bg-gray-600"></span>
                        <div class="text-xs text-gray-800 dark:text-gray-100 truncate">{{ fileName(fid) || ('文件 #' + fid) }}</div>
                      </div>
                      <Button variant="primary" size="xs" type="button" class="inline-flex items-center" @click="downloadAttachment(fid)">
                        <ArrowDownTrayIcon class="w-3.5 h-3.5 mr-1" />
                        {{ t('shared.download') || '下载' }}
                      </Button>
                    </div>
                  </div>
                </div>

                <!-- 发送状态（仅我方） -->
                <div v-if="item.data.isMine" class="ml-1 text-[10px] flex items-center gap-1">
                  <span v-if="item.data.status==='pending'" class="text-gray-400">{{ t('shared.chat.sending') || '发送中' }}</span>
                  <span v-else-if="item.data.status==='failed'" class="text-red-500">
                    {{ t('shared.chat.failed') || '发送失败' }}
                    <Button variant="ghost" size="xs" class="underline ml-1" @click="retrySend(item.data)">{{ t('shared.chat.retry') || '重试' }}</Button>
                  </span>
                </div>

                <!-- 我方头像（右） -->
                <user-avatar v-if="item.data.isMine" :avatar="item.data.avatarUrl" :size="28">
                  <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-700"></div>
                </user-avatar>
              </div>
            </template>

            <div v-if="renderedItems.length === 0" class="text-center text-gray-500 dark:text-gray-400 py-10">{{ emptyText }}</div>
          </div>
          <div v-else class="flex-1 flex items-center justify-center text-sm text-gray-500 dark:text-gray-400 px-4" :key="`placeholder-${activeTab}`">
            {{ t('shared.chat.pickSomeone') || '从左侧选择一位联系人开始聊天' }}
          </div>

          <!-- 输入区（含未发送附件预览） -->
        <div class="p-3 flex flex-col gap-2 relative shrink-0" style="box-shadow: inset 0 1px 0 rgba(255,255,255,0.18)">
            <!-- 未发送附件预览：图片缩略图网格 + 文件卡片 -->
            <div v-if="attachmentFileIds.length>0" class="flex flex-col gap-2">
              <div class="grid grid-cols-3 sm:grid-cols-4 gap-2" v-if="pendingImageIds.length>0">
                <div v-for="fid in pendingImageIds" :key="'pend-img-'+fid" class="relative group">
                  <img v-if="getPreviewUrl(fid)" :src="getPreviewUrl(fid)" class="w-full h-24 object-cover rounded-lg glass-img-border cursor-pointer" @click="openLightbox(pendingImageIds, fid)" />
                  <Button variant="danger" size="xs" type="button" class="absolute -top-1 -right-1 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop="removePending(fid)">×</Button>
                </div>
              </div>
              <div class="space-y-2" v-if="pendingFileIds.length>0">
                <div v-for="fid in pendingFileIds" :key="'pend-file-'+fid" class="flex items-center justify-between p-2 rounded-lg glass-attach glass-attach-mine">
                  <div class="flex items-center gap-2 min-w-0">
                    <span class="inline-block w-6 h-6 rounded bg-gray-300 dark:bg-gray-600"></span>
                    <div class="text-xs text-gray-800 dark:text-gray-100 truncate">{{ fileName(fid) || ('文件 #'+fid) }}</div>
                  </div>
                  <Button variant="ghost" size="xs" type="button" @click.stop="removePending(fid)">×</Button>
                </div>
              </div>
            </div>

          <div class="flex items-center gap-2 flex-nowrap min-w-0">
            <emoji-picker :variant="'primary'" :size="'sm'" :hideLabelOnSmall="true" :buttonClass="'!text-white whitespace-nowrap shrink-0'" @select="pickEmoji" />
            <input ref="fileInput" type="file" class="hidden" @change="onFilePicked" />
            <Button variant="success" size="sm" class="inline-flex items-center whitespace-nowrap shrink-0" @click="triggerPickFile">
              <ArrowUpTrayIcon class="w-4 h-4 mr-1" />
              <span class="whitespace-nowrap">{{ t('shared.uploadLabel') || '上传' }}</span>
            </Button>
            <glass-textarea ref="draftInput" v-model="(draft as any)" :rows="2" :placeholder="t('teacher.students.chat.placeholder') as string" class="flex-1 w-full min-h-[44px] max-h-36 resize-none" @keydown="onDraftKeydown" />
            <Button variant="primary" size="sm" class="whitespace-nowrap shrink-0" :disabled="sending || (!draft && attachmentFileIds.length===0)" @click="send()">
              <span class="whitespace-nowrap">{{ t('teacher.ai.send') }}</span>
            </Button>
          </div>
        </div>
        </div>
      </div>
      </div>
  </div>
  </div>
</teleport>

<!-- 灯箱渲染（teleport 到 body） -->
<teleport to="body" v-if="lightbox.open">
  <div class="fixed inset-0 z-[13000] flex items-center justify-center lightbox-backdrop" @click="closeLightbox">
    <div class="max-w-[85vw] max-h-[85vh] p-2 rounded-xl border lightbox-content relative" @click.stop>
      <img v-if="lightbox.current && getPreviewUrl(lightbox.current)" :src="getPreviewUrl(lightbox.current)" class="max-w-[80vw] max-h-[75vh] object-contain rounded" />
      <div class="absolute inset-x-0 -bottom-10 flex items-center justify-between px-4">
        <Button variant="menu" size="sm" @click="lightboxPrev">上一张</Button>
        <Button variant="menu" size="sm" @click="lightboxNext">下一张</Button>
      </div>
      <Button class="absolute -top-2 -right-2" variant="danger" size="xs" @click="closeLightbox">✕</Button>
  </div>
  </div>
</teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import { XMarkIcon, BookmarkIcon, BookmarkSlashIcon, ArrowDownTrayIcon, ArrowUpTrayIcon } from '@heroicons/vue/24/outline'
import Button from '@/components/ui/Button.vue'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'
import { chatApi } from '@/api/chat.api'
import { notificationAPI } from '@/api/notification.api'
import { teacherStudentApi } from '@/api/teacher-student.api'
import { fileApi } from '@/api/file.api'
import { resolveUserDisplayName } from '@/shared/utils/user'

const props = defineProps<{
  open: boolean
  peerId?: string | number
  courseId?: string | number
  peerName?: string
}>()
import { computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
const chat = useChatStore()
const auth = useAuthStore()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:peerId', v: string | number | null): void
  (e: 'update:peerName', v: string | null): void
  (e: 'update:courseId', v: string | number | null): void
}>()

const { t } = useI18n()

const title = ref('')
const messages = ref<any[]>([])
const loadingMessages = ref(false)
const page = ref(1)
const size = ref(100)
const total = ref(0)
const draft = ref('')
// 附件：上传成功的文件ID集合（发送时随消息提交）
const attachmentFileIds = ref<Array<string|number>>([])
// 移除标题
const sending = ref(false)
const draftInput = ref<HTMLTextAreaElement | null>(null)
const scrollContainer = ref<HTMLElement | null>(null)
const emptyText = t('teacher.students.table.empty') as string

// 左侧面板状态
const activeTab = ref<'recent' | 'contacts'>('recent')
const keyword = ref('')
// 统一数据源：优先 props，回退 store
const currentPeerId = computed(() => props.peerId ?? chat.peerId)
const currentPeerName = computed(() => props.peerName ?? chat.peerName)
const currentCourseId = computed(() => props.courseId ?? chat.courseId)
const hasActivePeer = computed(() => !!currentPeerId.value)
const peerActiveId = computed(() => currentPeerId.value ? String(currentPeerId.value) : '')
// 发送不再受角色或会话激活限制

const headerTitle = computed(() => {
  if (hasActivePeer.value) return (t('teacher.students.chat.title', { name: currentPeerName.value || title.value }) as string)
  return t('shared.chat.open') as string || '聊天'
})

function displayUserName(u: any): string { return resolveUserDisplayName(u) || String(u?.nickname || u?.username || u?.name || '') }

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
  // 不再强行插入当前会话的占位项，避免空白记录
  const activeId = props.peerId ? String(props.peerId) : ''
  const list = base.slice()
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

// 我的用户ID（字符串）
const getMyIdString = (): string => {
  try {
    // @ts-ignore
    const aId = auth.user?.id != null ? String(auth.user.id) : null
    if (aId) return aId
  } catch {}
  try {
    const ls = localStorage.getItem('userId')
    if (ls) return String(ls)
  } catch {}
  return ''
}

const extractId = (obj: any, keys: string[]): string | null => {
  for (const k of keys) {
    const v = obj?.[k]
    if (v != null && v !== '') return String(v)
  }
  return null
}

const messageIsMine = (n: any): boolean => {
  const myId = getMyIdString()
  const sender = extractId(n, ['senderId','sender_id','fromUserId','from_user_id','fromId','from_id','userId','user_id'])
  if (sender) return String(sender) === myId
  const recipient = extractId(n, ['recipientId','recipient_id','toUserId','to_user_id','toId','to_id'])
  // 若仅有 recipientId，则当其等于当前会话对端，推断为我发出
  if (recipient) return String(recipient) === String(currentPeerId.value || '')
  return false
}

// 头像与渲染辅助
type MessageItem = { id: string | number; content: string; createdAt: string; isMine: boolean; status?: 'sent' | 'pending' | 'failed'; attachmentFileIds?: Array<string|number> }
type RenderMessage = { type: 'message'; data: MessageItem & { avatarUrl: string | null } }
type RenderDivider = { type: 'time-divider'; key: string; timeText: string }
type RenderItem = RenderMessage | RenderDivider

// 对端头像缓存，避免联系人未加载时消息区头像缺失
const peerAvatarMap = ref<Record<string, string>>({})

const getPeerAvatar = (pid: string | number | undefined | null): string => {
  if (!pid) return ''
  const cached = peerAvatarMap.value[String(pid)]
  if (cached) return cached
  // 1) 联系人
  const fromContacts = (() => {
    const found = (chat.contacts as any[] || []).find((c: any) => String(c.id) === String(pid))
    return found?.avatar || ''
  })()
  if (fromContacts) return fromContacts
  // 2) 最近会话
  const fromRecent = (() => {
    const found = (chat.recentConversations as any[] || []).find((c: any) => String(c.peerId) === String(pid))
    return found?.avatar || ''
  })()
  if (fromRecent) return fromRecent
  // 3) 占位
  return ''
}

const ensurePeerAvatar = async (pid: string | number) => {
  const idStr = String(pid)
  if (peerAvatarMap.value[idStr]) return
  const existing = getPeerAvatar(idStr)
  if (existing) { peerAvatarMap.value[idStr] = existing; return }
  try {
    // 教师端直接拉取学生资料可能因权限返回 400/403；此处对教师角色跳过远程获取
    const role = String((auth.user as any)?.role || '').toUpperCase()
    if (role === 'TEACHER') return
    const profile: any = await teacherStudentApi.getStudentProfile(idStr)
    const avatar = profile?.avatar || profile?.avatarUrl || profile?.avatar_url || profile?.studentAvatar || profile?.student_avatar || profile?.photo || profile?.image || ''
    if (avatar) peerAvatarMap.value[idStr] = String(avatar)
  } catch { /* ignore */ }
}

const getMyAvatar = (): string => {
  // @ts-ignore
  return (auth.user as any)?.avatar || ''
}

const formatTimeDivider = (ts: string) => {
  const d = new Date(ts)
  if (isNaN(d.getTime())) return ''
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  const yest = new Date(now)
  yest.setDate(now.getDate() - 1)
  const isYesterday = d.toDateString() === yest.toDateString()
  const hm = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  if (sameDay) return `今天 ${hm}`
  if (isYesterday) return `昨天 ${hm}`
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${hm}`
}

const buildRenderedItems = (list: MessageItem[]): RenderItem[] => {
  const items: RenderItem[] = []
  if (!list || list.length === 0) return items
  const sorted = list.slice().sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
  const THRESHOLD = 5 * 60 * 1000 // 5分钟
  let lastTs = 0
  for (const m of sorted) {
    const curTs = new Date(m.createdAt).getTime() || 0
    if (lastTs === 0 || Math.abs(curTs - lastTs) > THRESHOLD || new Date(curTs).toDateString() !== new Date(lastTs).toDateString()) {
      items.push({ type: 'time-divider', key: `t-${m.id}-${curTs}`, timeText: formatTimeDivider(m.createdAt) })
    }
    lastTs = curTs
    items.push({
      type: 'message',
      data: {
        ...m,
        status: m.status || 'sent',
        avatarUrl: m.isMine ? getMyAvatar() : getPeerAvatar(peerActiveId.value)
      }
    })
  }
  return items
}

const renderedItems = computed<RenderItem[]>(() => { void peerAvatarMap.value; return buildRenderedItems(messages.value as MessageItem[]) })

const scrollToBottom = async () => {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

// 附件元数据与渲染辅助
const fileMetaMap = ref<Record<string, any>>({})
const previewUrlMap = ref<Record<string, string>>({})
const previewBlockedMap = ref<Record<string, boolean>>({})
function revokePreviewUrl(fid: string | number) {
  const key = String(fid)
  const url = previewUrlMap.value[key]
  if (url) {
    try { URL.revokeObjectURL(url) } catch {}
    delete previewUrlMap.value[key]
  }
}
function getPreviewUrl(fid: string | number): string {
  const key = String(fid)
  const url = previewUrlMap.value[key]
  if (url) return url
  // 懒加载生成，触发后续 ensurePreviewBlob
  void ensurePreviewBlob(fid)
  return ''
}
async function ensurePreviewBlob(fid: string | number) {
  const key = String(fid)
  if (previewUrlMap.value[key]) return
  // 未登录直接标记不可预览，显示文件卡，避免 401
  try { if (!auth.isAuthenticated) { previewBlockedMap.value[key] = true; return } } catch {}
  try {
    const blob = await fileApi.getPreview(key)
    // 若后端返回 json 错误（400 等），Blob.type 会是 application/json
    if (blob && (blob as any).type && String((blob as any).type).includes('application/json')) {
      previewBlockedMap.value[key] = true
      return
    }
    const url = URL.createObjectURL(blob)
    previewUrlMap.value[key] = url
  } catch (e: any) {
    // 失败：标记该文件不支持预览，退回文件卡
    previewBlockedMap.value[key] = true
    // 若权限错误，提示一次
    try {
      const code = Number(e?.code || 0)
      if (code === 1705 || code === 403) {
        console.debug('[preview-blocked]', key, code)
      }
    } catch {}
  }
}
async function downloadAttachment(fid: string | number) {
  try {
    const name = fileName(fid) || `file_${String(fid)}`
    await fileApi.downloadFile(String(fid), name || undefined)
  } catch {}
}
function hasText(s: unknown): boolean { return String(s ?? '').trim().length > 0 }
function fileName(fid: string | number): string | null {
  const meta = fileMetaMap.value[String(fid)]
  return meta?.originalName || meta?.original_name || null
}
function isImageAttachment(fid: string | number): boolean {
  const meta = fileMetaMap.value[String(fid)]
  const mime = String(meta?.mimeType || meta?.mime || '')
  return mime.startsWith('image/')
}
function canPreview(fid: string | number): boolean {
  const key = String(fid)
  if (previewBlockedMap.value[key]) return false
  return isImageAttachment(fid)
}
async function ensureFileMeta(fid: string | number) {
  const key = String(fid)
  if (fileMetaMap.value[key]) return
  try {
    const info: any = await fileApi.getFileInfo(String(fid))
    fileMetaMap.value[key] = info?.data || info || {}
    // 若是图片则预取 Blob 以加速首屏
    if (isImageAttachment(fid)) {
      void ensurePreviewBlob(fid)
    }
  } catch { /* ignore */ }
}

// 优先按“会话ID”批量标记为已读，失败时回退到按对端标记
async function markAllReadForCurrentPeer() {
  if (!currentPeerId.value) return
  try {
    const pid = String(currentPeerId.value)
    const cid = currentCourseId.value != null && currentCourseId.value !== '' ? String(currentCourseId.value) : ''
    const isStudent = (() => { try { return window.location.pathname.startsWith('/student') } catch { return false } })()
    const list: any[] = (chat.recentConversations as any[]) || []
    const targets = list.filter((c: any) => {
      const samePeer = String(c.peerId) === pid
      if (!samePeer) return false
      // 学生端按 peer 维度聚合；教师端按 peer+course 维度
      if (isStudent) return true
      return String(c.courseId || '') === String(cid || '')
    })
    let usedConversationId = false
    for (const c of targets) {
      if (c && c.id != null) {
        try { await chatApi.markConversationRead(c.id) } catch {}
        usedConversationId = true
      }
    }
    // 若没有有效的会话ID可用，则回退到按对端标记
    if (!usedConversationId) {
      try { await chatApi.markReadByPeer(currentPeerId.value as any, cid ? Number(cid) : undefined) } catch {}
      try { await chatApi.markReadByPeer(currentPeerId.value as any) } catch {}
    }
    // 前端本地状态清零
    try { chat.markPeerRead(String(pid), String(cid || '')) } catch {}
  } catch {}
}

const load = async () => {
  if (!currentPeerId.value) return
  loadingMessages.value = true
  const paramsBase: any = { page: page.value, size: size.value }
  const courseParam = currentCourseId.value ? Number(currentCourseId.value) : undefined
  const res: any = await chatApi.getMessages(currentPeerId.value as any, { ...paramsBase, courseId: courseParam })
  const list = (res?.items || []).map((n: any) => ({
    id: n.id,
    content: n.content,
    createdAt: n.createdAt || n.created_at || new Date().toISOString(),
    isMine: messageIsMine(n),
    attachmentFileIds: (n.attachmentFileIds || n.attachment_file_ids || n.attachments || []).map((x:any) => (typeof x === 'object' && x?.fileId != null) ? x.fileId : x),
    status: 'sent'
  }))
  // 合并：以服务端为准合并现有消息，避免乐观消息被覆盖
  if (Array.isArray(list)) {
    const byId = new Map<string, any>()
    for (const m of (messages.value as any[])) byId.set(String(m.id), m)
    for (const m of list) byId.set(String(m.id), { ...byId.get(String(m.id)), ...m })
    messages.value = Array.from(byId.values()).sort((a: any, b: any) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    total.value = res?.total || (res?.data?.total) || (messages.value as any[]).length
    // 预取附件元数据与图片预览 Blob
    try {
      const ids = new Set<string>()
      for (const m of (messages.value as any[])) {
        const arr = (m.attachmentFileIds || []) as Array<string|number>
        for (const fid of arr) ids.add(String(fid))
      }
      for (const id of ids) {
        await ensureFileMeta(id)
      }
    } catch {}
  }
  // 若结果为空或明显少于一条，尝试另一种参数（带/不带课程）再合并，避免后端筛选差异导致看不到
  if (((messages.value as any[]).length === 0 || (messages.value as any[]).length < 1)) {
    try {
      const otherParams = courseParam != null ? { ...paramsBase } : { ...paramsBase, courseId: 0 }
      const res2: any = await chatApi.getMessages(currentPeerId.value as any, otherParams)
      const list2 = (res2?.items || []).map((n: any) => ({
        id: n.id,
        content: n.content,
        createdAt: n.createdAt || n.created_at || new Date().toISOString(),
        isMine: messageIsMine(n),
        attachmentFileIds: (n.attachmentFileIds || n.attachment_file_ids || n.attachments || []).map((x:any) => (typeof x === 'object' && x?.fileId != null) ? x.fileId : x),
        status: 'sent'
      }))
      const byId = new Map<string, any>()
      for (const m of (messages.value as any[])) byId.set(String(m.id), m)
      for (const m of list2) byId.set(String(m.id), { ...byId.get(String(m.id)), ...m })
      messages.value = Array.from(byId.values()).sort((a: any, b: any) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    } catch {}
  }

  // 仍为空则进一步回退到 /notifications/conversation 接口合并
  if ((messages.value as any[]).length === 0) {
    try {
      const res3: any = await notificationAPI.getConversation(currentPeerId.value as any, { page: page.value, size: size.value })
      const items = (res3?.items || res3?.data?.items || [])
      const list3 = items.map((n: any) => ({
        id: n.id,
        content: n.content,
        createdAt: n.createdAt || n.created_at || new Date().toISOString(),
        isMine: messageIsMine(n),
        attachmentFileIds: (n.attachmentFileIds || n.attachment_file_ids || n.attachments || []).map((x:any) => (typeof x === 'object' && x?.fileId != null) ? x.fileId : x),
        status: 'sent'
      }))
      messages.value = list3
    } catch {}
  }
  // 标记为已读（先带课程上下文，再无课程上下文各尝试一次）
  try { await markAllReadForCurrentPeer() } catch {}
  // 同步刷新“最近”，确保未读角标与后端一致
  try { await chat.loadLists({ courseId: currentCourseId.value || undefined }) } catch {}
  // 兼容历史多会话ID/不同 courseId 的数据：对同一 peer 的所有最近会话逐一标记
  try {
    const list: any[] = (chat.recentConversations as any[]) || []
    for (const c of list) {
      if (String(c.peerId) === String(currentPeerId.value || '')) {
        // 再次按会话ID兜底
        if (c && c.id != null) { try { await chatApi.markConversationRead(c.id) } catch {} }
        try { chat.markPeerRead(String(c.peerId), String(c.courseId || '')) } catch {}
      }
    }
  } catch {}
  // 首次进入或切换会话：滚到最新一条
  await nextTick(); await scrollToBottom()
  try { if (currentPeerId.value) await ensurePeerAvatar(String(currentPeerId.value)) } catch {}
  loadingMessages.value = false
}

const createLocalPendingMessage = (content: string, attachments?: Array<string|number>): MessageItem => {
  const id = `local-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
  return { id, content, createdAt: new Date().toISOString(), isMine: true, status: 'pending', attachmentFileIds: attachments && attachments.length ? attachments.slice() : [] }
}

const markMessageStatus = (id: string | number, status: 'sent' | 'failed' | 'pending') => {
  const idx = (messages.value as MessageItem[]).findIndex(m => String(m.id) === String(id))
  if (idx >= 0) {
    const cloned = (messages.value as MessageItem[]).slice()
    cloned[idx] = { ...cloned[idx], status }
    messages.value = cloned
  }
}

const send = async (contentOverride?: string | unknown, tempIdToResolve?: string | number) => {
  // 允许任何登录用户发送（权限由后端控制）
  const raw = (typeof contentOverride === 'string' ? contentOverride : draft.value)
  const content = ((raw ?? '') as any).toString().trim()
  // 允许“仅附件无文本”也能发送
  if (!content && attachmentFileIds.value.length === 0) return
  // 插入或复用本地 pending 消息（带附件快照）
  let localMsg: MessageItem
  const attachedSnapshot = (attachmentFileIds.value && attachmentFileIds.value.length > 0) ? attachmentFileIds.value.slice() : []
  if (tempIdToResolve != null) {
    const idx = (messages.value as MessageItem[]).findIndex(m => String(m.id) === String(tempIdToResolve))
    if (idx >= 0) {
      const cloned = (messages.value as MessageItem[]).slice()
      const updated = { ...cloned[idx], status: 'pending' as const }
      cloned[idx] = updated
      messages.value = cloned
      localMsg = updated
    } else {
      localMsg = createLocalPendingMessage(content, attachedSnapshot)
      ;(messages.value as MessageItem[]).push(localMsg)
    }
  } else {
    localMsg = createLocalPendingMessage(content, attachedSnapshot)
    ;(messages.value as MessageItem[]).push(localMsg)
  }
  await nextTick()
  await scrollToBottom()
  draft.value = ''

  try {
    sending.value = true
    const payload: any = {
      recipientId: Number(currentPeerId.value),
      content: content,
      relatedType: currentCourseId.value ? 'course' : undefined,
      relatedId: currentCourseId.value ? Number(currentCourseId.value) : undefined,
      attachmentFileIds: (attachmentFileIds.value && attachmentFileIds.value.length > 0) ? attachmentFileIds.value : undefined
    }
    let sent: any
    try {
      sent = await chatApi.sendMessage(payload)
    } catch (err: any) {
      const msg = String(err?.message || '')
      const code = Number(err?.code || 0)
      // 只要不是未登录(401)且携带课程上下文，就降级为普通私聊重试一次
      if (payload.relatedType && code !== 401) {
        try {
          const retryPayload: any = { ...payload }
          delete retryPayload.relatedType
          delete retryPayload.relatedId
          sent = await chatApi.sendMessage(retryPayload)
        } catch (err2) {
          throw err2
        }
      } else {
        throw err
      }
    }
    // 立即为附件预取元数据，保证发送后可见
    try {
      for (const fid of attachedSnapshot) { await ensureFileMeta(String(fid)) }
    } catch {}
    // 标记本地消息为 sent
    markMessageStatus(localMsg.id, 'sent')
    // 若后端返回了真实消息 id，则把本地临时 id 替换为服务端 id，避免后续去重导致“丢失”
    try {
      const serverId = sent?.id || sent?.data?.id
      if (serverId) {
        const idx = (messages.value as MessageItem[]).findIndex(m => String(m.id) === String(localMsg.id))
        if (idx >= 0) {
          const cloned = (messages.value as MessageItem[]).slice()
          cloned[idx] = { ...cloned[idx], id: serverId, status: 'sent', attachmentFileIds: attachedSnapshot }
          messages.value = cloned
        }
      }
    } catch {}
    // 更新“最近”列表预览/排序（利用后端带回的 recent mirror）
    try {
      const data = (sent && (sent.data || sent))
      const mirror = (() => { try { return typeof data?.data === 'string' ? JSON.parse(data.data) : (typeof data?.data === 'object' ? data.data : (typeof data?.mirror === 'object' ? data.mirror : (data?.data && JSON.parse(data.data)))) } catch { return null } })()
      const convId = mirror?.conversationId || data?.conversationId
      chat.upsertRecentAfterSend(String(currentPeerId.value || ''), String(currentCourseId.value || ''), content, content, currentPeerName.value ?? undefined, convId)
    } catch {
      chat.upsertRecentAfterSend(String(currentPeerId.value || ''), String(currentCourseId.value || ''), content, content, currentPeerName.value ?? undefined)
    }
    // 刷新“最近”列表以拿到会话ID等后端字段；并在联系人页发送后自动切到“最近”
    try { await chat.loadLists({ courseId: currentCourseId.value || undefined }) } catch {}
    if (activeTab.value !== 'recent') activeTab.value = 'recent'
    try { await load() } catch {}
    await scrollToBottom()
    // 关闭短时间内的自动刷新，避免干扰乐观消息展示
  } catch (e) {
    markMessageStatus(localMsg.id, 'failed')
  } finally {
    sending.value = false
    // 清空附件队列
    attachmentFileIds.value = []
  }
}

const retrySend = async (msg: MessageItem) => {
  if (!msg || msg.status !== 'failed') return
  await send(msg.content, msg.id)
}

const onEmoji = async (emoji: string) => {
  draft.value = (draft.value || '') + emoji
  await nextTick()
  try { draftInput.value?.focus() } catch {}
}
const pickEmoji = async (e: string) => { await onEmoji(e) }

const choosePeer = async (id: string | number, name?: string | null, cId?: string | number | null) => {
  // 仅设置会话，不强制切换标签，避免跳回“最近”
  chat.setPeer(id, name ?? null, (cId ?? props.courseId) ?? null)
  // 受控模式同步父组件
  try { emit('update:peerId', id) } catch {}
  try { emit('update:peerName', name ?? null) } catch {}
  try { emit('update:courseId', (cId ?? props.courseId) ?? null) } catch {}
  await nextTick()
  try {
    // 先清空本地消息，避免旧会话内容闪烁
    messages.value = []
    await load()
  } catch {}
  try { await ensurePeerAvatar(String(id)) } catch {}
}

const onToggleGroup = async (g: any) => {
  chat.toggleContactGroup(g.courseId)
}

// 移除系统消息逻辑

// 切换标签时，强制清理对方选择与系统选中，避免界面叠加残留
watch(activeTab, async () => {
  try {
    await nextTick()
    if (scrollContainer.value) scrollContainer.value.scrollTop = 0
  } catch {}
})

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
  // 未登录时不触发任何远端加载，避免 401 噪音
  if (!auth.isAuthenticated) return
  title.value = props.peerName ? `${props.peerName}` : (t('teacher.students.table.message') as string)
  // 始终加载列表，保证左侧数据完整
  // 优先从当前上下文（props.courseId 或最近持久化）加载联系人，避免切入口不同导致联系人为空
  const persisted = (() => { try {
    const uid = String(localStorage.getItem('userId') || '')
    const role = (() => { try { return (auth.user as any)?.role || '' } catch { return '' } })()
    const key = `chat:${String(role||'GUEST').toUpperCase()}:${uid||'anon'}:recent`
    return JSON.parse(localStorage.getItem(key) || '[]')
  } catch { return [] } })()
  const last = persisted && persisted[0]
  const cid = currentCourseId.value || (last?.courseId || undefined)
  await chat.loadLists({ courseId: cid })
  // 若已选中会话，再加载会话消息
  if (currentPeerId.value) await load()
})

// Esc 键关闭抽屉
function onKeydown(e: KeyboardEvent) {
  try {
    if (e.key === 'Escape' && props.open) {
      emit('close')
    }
  } catch {}
}
onMounted(() => { try { window.addEventListener('keydown', onKeydown) } catch {} })
onUnmounted(() => { try { window.removeEventListener('keydown', onKeydown) } catch {} })
onUnmounted(() => {
  try {
    const keys = Object.keys(previewUrlMap.value)
    for (const k of keys) {
      try { URL.revokeObjectURL(previewUrlMap.value[k]) } catch {}
    }
  } catch {}
})

watch(() => props.open, async (v) => {
  if (v) {
    if (!auth.isAuthenticated) return
    try { console.debug('[ChatDrawer] open prop changed ->', v) } catch {}
    // 先尝试标记当前 peer 的会话为已读（带/不带课程），避免先加载列表闪现红点
    try { if (currentPeerId.value) await markAllReadForCurrentPeer() } catch {}
    const persisted = (() => { try {
      const uid = String(localStorage.getItem('userId') || '')
      const role = (() => { try { return (auth.user as any)?.role || '' } catch { return '' } })()
      const key = `chat:${String(role||'GUEST').toUpperCase()}:${uid||'anon'}:recent`
      return JSON.parse(localStorage.getItem(key) || '[]')
    } catch { return [] } })()
    const last = persisted && persisted[0]
    const cid = currentCourseId.value || (last?.courseId || undefined)
    await chat.loadLists({ courseId: cid })
    // 再次对所有同 peer 分支逐一标记已读，确保历史分支清零
    try {
      const list: any[] = (chat.recentConversations as any[]) || []
      for (const c of list) {
        if (String(c.peerId) === String(currentPeerId.value || '')) {
          if (c && c.id != null) { try { await chatApi.markConversationRead(c.id) } catch {} }
          try { chat.markPeerRead(String(c.peerId), String(c.courseId || '')) } catch {}
        }
      }
    } catch {}
    if (currentPeerId.value) await load()
  }
})
watch(() => [props.peerId, chat.peerId], async () => { await load() })

// 发送输入区键盘行为：Enter 发送，Shift+Enter 换行
function onDraftKeydown(e: KeyboardEvent) {
  try {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      send()
    }
  } catch {}
}

// 上传
const fileInput = ref<HTMLInputElement | null>(null)
function triggerPickFile() {
  try { fileInput.value?.click() } catch {}
}
const lightbox = ref<{ open: boolean; items: Array<string|number>; current?: string|number|null }>({ open: false, items: [], current: null })
const pendingImageIds = computed(() => attachmentFileIds.value.filter(fid => isImageAttachment(fid)))
const pendingFileIds = computed(() => attachmentFileIds.value.filter(fid => !isImageAttachment(fid)))
function openLightbox(items: Array<string|number>, current?: string|number) {
  lightbox.value = { open: true, items: items.slice(), current: current ?? (items[0] || null) }
}
function closeLightbox() { lightbox.value.open = false }
function lightboxPrev() {
  if (!lightbox.value.open || !lightbox.value.items.length) return
  const arr = lightbox.value.items
  const idx = Math.max(0, arr.findIndex(x => String(x) === String(lightbox.value.current)))
  const next = idx <= 0 ? arr[arr.length - 1] : arr[idx - 1]
  lightbox.value.current = next
}
function lightboxNext() {
  if (!lightbox.value.open || !lightbox.value.items.length) return
  const arr = lightbox.value.items
  const idx = Math.max(0, arr.findIndex(x => String(x) === String(lightbox.value.current)))
  const next = idx >= arr.length - 1 ? arr[0] : arr[idx + 1]
  lightbox.value.current = next
}
function removePending(fid: string|number) {
  const i = attachmentFileIds.value.findIndex(x => String(x) === String(fid))
  if (i >= 0) attachmentFileIds.value.splice(i, 1)
}
async function onFilePicked(ev: Event) {
  try {
    const input = ev.target as HTMLInputElement
    const file = input?.files?.[0]
    if (!file) return
    const uploaded: any = await fileApi.uploadFile(file, { purpose: 'chat', relatedId: String(currentPeerId.value || '') })
    const fileId = uploaded?.id || uploaded?.data?.id
    if (fileId != null) {
      attachmentFileIds.value.push(fileId)
      try { await ensureFileMeta(String(fileId)) } catch {}
    }
    await nextTick()
    try { draftInput.value?.focus() } catch {}
    if (input) input.value = ''
    // 若没有文本，默认立即发送“仅附件”消息，避免切换会话后丢失感知
    try {
      if (!hasText(draft.value) && attachmentFileIds.value.length > 0) {
        await send()
      }
    } catch {}
  } catch {}
}
</script>

<style scoped lang="postcss">
.input { @apply border rounded px-3 py-2 text-sm bg-white dark:bg-gray-900 border-gray-300 dark:border-gray-700 text-gray-900 dark:text-gray-100; }
.btn { @apply inline-flex items-center justify-center rounded px-3 py-2 text-sm font-medium; }
.btn-primary { @apply bg-blue-600 text-white hover:bg-blue-700; }

/* 左侧列表选中：使用主题强调色加深，无任何边框线 */
.chat-selected {
  background-color: color-mix(in oklab, var(--color-accent) 20%, transparent);
  box-shadow: none;
  outline: none;
  border: none;
  /* 彻底去掉蓝色外圈（Safari/Chrome focus ring/focus-visible/moz outline等） */
}
button, .btn, [role="button"] {
  outline: none !important;
}
button:focus, .btn:focus, [role="button"]:focus,
button:focus-visible, .btn:focus-visible, [role="button"]:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}

/* 气泡尾巴：使用额外元素实现，保证与暗黑模式背景一致 */
.tail { position: absolute; width: 10px; height: 10px; }
.tail-left { left: -4px; bottom: 10px; clip-path: polygon(0 50%, 100% 0, 100% 100%); }
.tail-right { right: -4px; bottom: 10px; clip-path: polygon(0 0, 100% 50%, 0 100%); }

.bubble-peer { position: relative; }
.bubble-mine { position: relative; }

/* 主题色融合 + 玻璃风格的聊天气泡 */
.bubble {
  background: rgb(var(--glass-bg-rgb) / var(--glass-alpha-regular));
  border-color: var(--glass-border-color);
  backdrop-filter: blur(var(--glass-blur-regular)) saturate(var(--glass-saturate-regular)) contrast(var(--glass-contrast-regular));
  -webkit-backdrop-filter: blur(var(--glass-blur-regular)) saturate(var(--glass-saturate-regular)) contrast(var(--glass-contrast-regular));
  box-shadow: var(--glass-inner-shadow), var(--glass-outer-shadow);
}
.bubble-mine {
  /* 融合主题强调色，保证深色主题对比度 */
  background: color-mix(in oklab, var(--color-accent) 28%, rgb(var(--glass-bg-rgb) / var(--glass-alpha-regular)));
  border-color: color-mix(in oklab, var(--color-accent) 55%, var(--glass-border-color));
}
.bubble-peer {
  background: color-mix(in oklab, var(--color-base-100) 85%, rgb(var(--glass-bg-rgb) / var(--glass-alpha-ultraThin)));
  border-color: var(--glass-border-color);
}

/* 尾巴与背景一致 */
.tail-peer { background: color-mix(in oklab, var(--color-base-100) 85%, rgb(var(--glass-bg-rgb) / var(--glass-alpha-ultraThin))); }
.tail-mine { background: color-mix(in oklab, var(--color-accent) 28%, rgb(var(--glass-bg-rgb) / var(--glass-alpha-regular))); }

/* 隐藏滚动条（仍可滚动） */
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }

/* 简易灯箱样式（玻璃风格） */
.lightbox-backdrop { background: rgba(0,0,0,0.5); }
.lightbox-content {
  background: rgb(var(--glass-bg-rgb) / var(--glass-alpha-regular));
  border-color: var(--glass-border-color);
  backdrop-filter: blur(var(--glass-blur-regular)) saturate(var(--glass-saturate-regular)) contrast(var(--glass-contrast-regular));
  -webkit-backdrop-filter: blur(var(--glass-blur-regular)) saturate(var(--glass-saturate-regular)) contrast(var(--glass-contrast-regular));
  box-shadow: var(--glass-inner-shadow), var(--glass-outer-shadow);
}
</style>


