<template>
  <div class="flex items-start space-x-3">
    <div class="w-10 h-10 flex-shrink-0">
      <user-avatar :avatar="(comment.author && comment.author.avatar) ? String(comment.author.avatar) : undefined" :size="40">
        <div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
          <user-icon class="w-5 h-5 text-gray-500"/>
        </div>
      </user-avatar>
    </div>
    <div class="flex-1">
      <div class="glass-regular glass-interactive rounded-xl p-3" v-glass="{ strength: 'regular', interactive: true }">
        <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ displayUserName(comment.author) || t('shared.community.list.anonymous') }}</p>
        <p class="text-sm text-gray-700 dark:text-gray-200 mt-1 whitespace-pre-line">{{ comment.content }}</p>
      </div>
      <div class="text-xs text-gray-500 mt-3 flex items-center gap-3">
        <span>{{ formatDate(comment.createdAt) }}</span>
        <Button size="xs" variant="reaction" class="!px-1 !py-0.5" :class="comment.isLiked ? 'reaction-liked' : ''" @click="onLikeComment" :disabled="likeBusy">
          <template #icon>
            <hand-thumb-up-icon class="w-3.5 h-3.5" />
          </template>
          <span>{{ safeLikeCount }}</span>
        </Button>
        <Button size="xs" variant="ghost" class="!px-1.5 !py-0.5" @click="toggleReply">
          <template #icon>
            <chat-bubble-left-icon class="w-3.5 h-3.5" />
          </template>
          {{ t('shared.community.detail.reply') }}
        </Button>

        <div class="ml-auto relative" ref="menuAnchorRef">
          <Button
            size="xs"
            variant="ghost"
            class="!px-1.5 !py-0.5"
            :aria-label="t('shared.community.comment.more') as string"
            @click="toggleMenu"
          >
            <template #icon>
              <ellipsis-vertical-icon class="w-4 h-4" />
            </template>
          </Button>
          <div
            v-if="showMenu"
            ref="menuPanelRef"
            class="absolute right-0 mt-2 min-w-[10rem] rounded-2xl p-2 ui-popover-menu z-20"
            v-glass="{ strength: 'thin', interactive: false }"
          >
            <button type="button" class="w-full text-left px-3 py-2 rounded-xl text-sm hover-bg-soft" @click="onAskAiFromMenu">
              <span class="inline-flex items-center gap-2">
                <sparkles-icon class="w-4 h-4" />
                <span>{{ t('shared.community.detail.askAi') }}</span>
              </span>
            </button>
            <button
              v-if="canDelete"
              type="button"
              class="w-full text-left px-3 py-2 rounded-xl text-sm hover-bg-soft"
              @click="onDeleteFromMenu"
            >
              <span class="inline-flex items-center gap-2">
                <trash-icon class="w-4 h-4" />
                <span>{{ t('shared.community.list.delete') }}</span>
              </span>
            </button>
          </div>
        </div>
      </div>
      <!-- 回复输入框 -->
      <div v-if="showReplyBox" class="mt-4">
        <glass-textarea v-model="replyContent" :rows="2" class="w-full" :placeholder="t('shared.community.detail.writeComment') as string" />
        <div class="mt-4 flex items-center gap-4">
          <emoji-picker variant="outline" tint="accent" size="sm" @select="onReplyEmojiSelect" />
          <Button variant="primary" size="sm" :disabled="!replyContent.trim()" @click="submitReply">
            <template #icon>
              <paper-airplane-icon class="w-4 h-4" />
            </template>
            {{ t('shared.community.detail.postComment') }}
          </Button>
          <Button variant="ghost" size="sm" @click="toggleReply">{{ t('shared.community.modal.cancel') }}</Button>
        </div>
      </div>

      <!-- 子评论列表（递归） -->
      <div class="mt-4 pl-4 border-l border-gray-200 dark:border-gray-600" v-if="replies.items.length">
        <CommentThread
          v-for="rc in replies.items"
          :key="rc.id"
          :comment="rc"
          :post-id="postId"
          @deleted="handleChildDeleted"
        />
        <Button v-if="replies.items.length < replies.total" variant="ghost" size="sm" class="mt-3" @click="loadMoreReplies">{{ t('shared.community.detail.more') }}</Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCommunityStore } from '@/stores/community'
import { UserIcon, HandThumbUpIcon, SparklesIcon, ChatBubbleLeftIcon, TrashIcon, PaperAirplaneIcon, EllipsisVerticalIcon } from '@heroicons/vue/24/outline'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'
import { useRouter } from 'vue-router'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import Button from '@/components/ui/Button.vue'
import { resolveUserDisplayName } from '@/shared/utils/user'

defineOptions({ name: 'CommentThread' })

const props = defineProps<{ comment: any; postId: number }>()
const emit = defineEmits<{ (e: 'deleted', id: number): void }>()

const authStore = useAuthStore()
const communityStore = useCommunityStore()
const router = useRouter()
const { t } = useI18n()

function displayUserName(u: any): string { return resolveUserDisplayName(u) || String(u?.nickname || u?.username || '') }

const showReplyBox = ref(false)
const replyContent = ref('')
const replies = ref<{ items: any[]; page: number; size: number; total: number }>({ items: [], page: 0, size: 10, total: 0 })
const safeLikeCount = computed(() => {
  const v = (props.comment as any).likeCount ?? (props.comment as any).likesCount
  const n = Number(v)
  return Number.isFinite(n) ? Math.max(0, n) : 0
})
const likeBusy = ref(false)

const canDelete = computed(() => {
  const uid = authStore.user?.id
  if (!uid) return false
  return String(uid) === String((props.comment as any).author?.id || (props.comment as any).authorId)
})

const showMenu = ref(false)
const menuAnchorRef = ref<HTMLElement | null>(null)
const menuPanelRef = ref<HTMLElement | null>(null)

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const toggleReply = () => {
  showReplyBox.value = !showReplyBox.value
  if (!showReplyBox.value) replyContent.value = ''
}

const submitReply = async () => {
  if (!replyContent.value.trim()) return
  await communityStore.postComment(props.postId, replyContent.value, props.comment.id)
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.commentPostedTitle') as string, message: t('shared.community.notify.commentPostedMsg') as string })
  replyContent.value = ''
  showReplyBox.value = false
  // 直接刷新该节点的子回复，并确保最新在顶部
  await loadReplies(true)
}

const loadReplies = async (reset = false) => {
  const nextPage = reset ? 1 : replies.value.page + 1
  const { communityApi } = await import('@/api/community.api')
  const res = await communityApi.getCommentsByPostId(props.postId, { page: nextPage, size: replies.value.size, parentId: props.comment.id })
  const toBool = (v: any) => v === true || v === 1 || v === '1' || v === 'true'
  const toNum = (v: any) => { const n = Number(v); return Number.isFinite(n) ? Math.max(0, n) : 0 }
  const items = (res.items || []).map((c: any) => ({
    ...c,
    likeCount: toNum(c.likeCount ?? c.likesCount ?? 0),
    isLiked: toBool(c.isLiked ?? c.liked ?? false),
    author: c.author || (c.authorUsername || c.author_display_name
      ? { username: c.authorUsername || c.author_username, nickname: c.authorNickname || c.author_nickname, avatar: c.authorAvatar || c.author_avatar }
      : undefined),
  }))
  const total = res.total || 0
  replies.value = {
    items: reset ? items : [...replies.value.items, ...items],
    page: nextPage,
    size: replies.value.size,
    total
  }
}

const loadMoreReplies = async () => {
  await loadReplies(false)
}

const handleChildDeleted = async (id: number) => {
  // 从本地移除已删除的子回复
  replies.value.items = replies.value.items.filter(i => i.id !== id)
  replies.value.total = Math.max(0, replies.value.total - 1)
}

const handleDeleteSelf = async () => {
  if (!confirm(t('shared.community.confirm.deleteComment') as string)) return
  await communityStore.deleteComment(props.comment.id, props.postId)
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.commentDeletedTitle') as string, message: t('shared.community.notify.commentDeletedMsg') as string })
  emit('deleted', props.comment.id)
}

onMounted(async () => {
  await loadReplies(true)
  document.addEventListener('click', onDocClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick)
})

const toggleMenu = () => {
  showMenu.value = !showMenu.value
}

const closeMenu = () => {
  showMenu.value = false
}

const onDocClick = (ev: MouseEvent) => {
  if (!showMenu.value) return
  const target = ev.target as Node
  const anchor = menuAnchorRef.value
  const panel = menuPanelRef.value
  if (anchor && anchor.contains(target)) return
  if (panel && panel.contains(target)) return
  closeMenu()
}

const onLikeComment = async () => {
  if (likeBusy.value) return
  likeBusy.value = true
  try {
    const before = (props.comment as any).isLiked === true || (props.comment as any).isLiked === 'true' || (props.comment as any).isLiked === 1 || (props.comment as any).isLiked === '1'
    const liked = await communityStore.toggleLikeComment(props.comment.id)
    if (typeof liked === 'boolean') {
      // 仅在后端状态与本地原状态不同的情况下调整计数，避免双重加减
      const current = Number((props.comment as any).likeCount ?? (props.comment as any).likesCount) || 0
      if (liked !== before) {
        (props.comment as any).isLiked = liked
        ;(props.comment as any).likeCount = Math.max(0, current + (liked ? 1 : -1))
      } else {
        // 如果后端返回与本地一致，仅同步 liked，计数保持不变
        (props.comment as any).isLiked = liked
      }
    }
  } finally {
    likeBusy.value = false
  }
}

const askAiForComment = () => {
  const content = (props.comment?.content ? `【评论内容】${props.comment.content}` : '')
  router.push({ path: '/teacher/assistant', query: { q: content } })
}

const onAskAiFromMenu = () => {
  closeMenu()
  askAiForComment()
}

const onDeleteFromMenu = async () => {
  closeMenu()
  await handleDeleteSelf()
}

const onReplyEmojiSelect = (e: string) => {
  replyContent.value = (replyContent.value || '') + e
}
</script>

<style scoped>
</style>

