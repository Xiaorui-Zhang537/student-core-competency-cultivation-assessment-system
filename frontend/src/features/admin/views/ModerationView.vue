<template>
  <div class="p-6">
    <page-header :title="t('admin.sidebar.moderation')" :subtitle="t('admin.title')" />

    <filter-bar tint="secondary" align="center" :dense="false" class="mt-4 mb-2 rounded-full h-19">
      <template #left>
        <div class="flex items-end gap-3 flex-wrap min-w-0">
          <div class="flex items-center gap-2">
            <div class="text-xs text-subtle">{{ t('admin.moderation.filterTypeLabel') || '类型' }}</div>
            <segmented-pills :model-value="tab" :options="tabOptions" size="sm" variant="warning" @update:modelValue="(v:any) => onPickTab(String(v))" />
          </div>
          <div class="w-44">
            <div class="flex items-center gap-2">
              <div class="text-xs text-subtle whitespace-nowrap">{{ t('admin.moderation.filterStatusLabel') || '状态' }}</div>
              <glass-popover-select v-model="currentStatus" :options="currentStatusOptions" size="sm" tint="secondary" />
            </div>
          </div>
        </div>
      </template>
      <template #right>
        <div class="flex items-end gap-2">
          <div class="w-72">
            <glass-search-input
              v-model="searchKeyword"
              :placeholder="String(t('common.search') || '搜索')"
              size="sm"
              tint="info"
              @keyup.enter="reload"
            />
          </div>
          <Button size="sm" variant="secondary" class="bg-sky-600 text-white border-sky-600 hover:bg-sky-700 whitespace-nowrap" @click="exportCommunity">
            <arrow-down-tray-icon class="w-4 h-4 mr-1" />
            {{ t('admin.moderation.exportCommunity') || '导出社区CSV' }}
          </Button>
          <Button size="sm" variant="primary" @click="goCenter" class="whitespace-nowrap">
            <users-icon class="w-4 h-4 mr-1" />
            {{ t('admin.moderation.viewCommunityCenter') || '查看具体社区' }}
          </Button>
        </div>
      </template>
    </filter-bar>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-if="tab === 'posts'" class="mt-4 space-y-4">
      <card padding="md" tint="secondary">
        <glass-table>
          <template #head>
            <tr>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.moderation.post') || '帖子' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.status') || '状态' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.pinned') || '置顶' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.comments') || '评论数' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.actions') || '操作' }}</th>
            </tr>
          </template>

          <template #body>
            <tr v-for="p in posts" :key="p.id" class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ p.id }}</td>
              <td class="px-6 py-3">
                <div class="font-medium">{{ truncateText(p.title, 36) }}</div>
                <div class="text-xs text-subtle line-clamp-1">{{ truncateText(p.content, 72) }}</div>
                <div class="text-xs text-subtle">{{ postAuthorLine(p) }}</div>
              </td>
              <td class="px-6 py-3 w-44 text-center">
                <glass-popover-select :model-value="p.status" :options="postStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderatePost(p.id, { status: String(v) })" />
              </td>
              <td class="px-6 py-3 w-28 text-center">
                <glass-popover-select :model-value="Boolean(p.pinned)" :options="boolOptions" size="sm" @update:modelValue="(v:any)=>moderatePost(p.id, { pinned: Boolean(v) })" />
              </td>
              <td class="px-6 py-3 text-sm text-center">{{ p.commentsCount ?? 0 }}</td>
              <td class="px-6 py-3 text-center whitespace-nowrap">
                <div class="flex items-center justify-center gap-2">
                <Button size="sm" variant="primary" @click="openCommentsFromPost(p.id)">
                  <chat-bubble-left-right-icon class="w-4 h-4 mr-1" />
                  {{ t('admin.moderation.viewComments') || '查看评论' }}
                </Button>
                <Button size="sm" variant="secondary" class="bg-amber-500 text-white border-amber-500 hover:bg-amber-600" @click="openBlockDialog('post', p)">
                  <shield-exclamation-icon class="w-4 h-4 mr-1" />
                  {{ t('admin.moderation.block') || '屏蔽' }}
                </Button>
                <Button size="sm" variant="danger" @click="deletePostWithNotice(p)">
                  <trash-icon class="w-4 h-4 mr-1" />
                  {{ t('common.delete') || '删除' }}
                </Button>
                </div>
              </td>
            </tr>
            <tr v-if="posts.length === 0">
              <td colspan="6" class="px-6 py-6">
                <empty-state :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </template>
        </glass-table>

        <pagination-bar
          :page="postPage"
          :page-size="postPageSize"
          :total-items="postTotal"
          :total-pages="postTotalPages"
          :disabled="loading"
          @update:page="(v: number) => { postPage = v; reloadPosts() }"
          @update:pageSize="(v: number) => { postPageSize = v; postPage = 1; reloadPosts() }"
        />
      </card>
    </div>

    <div v-else class="mt-4 space-y-4">
      <card padding="md" tint="secondary">
        <glass-table>
          <template #head>
            <tr>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.moderation.comment') || '评论' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.status') || '状态' }}</th>
              <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.delete') || '删除' }}</th>
            </tr>
          </template>
          <template #body>
            <tr v-for="c in comments" :key="c.id" class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ c.id }}</td>
              <td class="px-6 py-3">
                <div class="text-sm">{{ truncateText(c.content, 72) }}</div>
                <div class="text-xs text-subtle">{{ commentMetaLine(c) }}</div>
              </td>
              <td class="px-6 py-3 w-44 text-center">
                <glass-popover-select :model-value="c.status" :options="commentStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderateComment(c.id, { status: String(v) })" />
              </td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2">
                  <Button size="sm" variant="secondary" class="bg-amber-500 text-white border-amber-500 hover:bg-amber-600" @click="openBlockDialog('comment', c)">
                    <shield-exclamation-icon class="w-4 h-4 mr-1" />
                    {{ t('admin.moderation.block') || '屏蔽' }}
                  </Button>
                  <Button size="sm" variant="danger" @click="deleteCommentWithNotice(c)">
                    <trash-icon class="w-4 h-4 mr-1" />
                    {{ t('common.delete') || '删除' }}
                  </Button>
                </div>
              </td>
            </tr>
            <tr v-if="comments.length === 0">
              <td colspan="4" class="px-6 py-6">
                <empty-state :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </template>
        </glass-table>

        <pagination-bar
          :page="commentPage"
          :page-size="commentPageSize"
          :total-items="commentTotal"
          :total-pages="commentTotalPages"
          :disabled="loading"
          @update:page="(v: number) => { commentPage = v; reloadComments() }"
          @update:pageSize="(v: number) => { commentPageSize = v; commentPage = 1; reloadComments() }"
        />
      </card>
    </div>

    <glass-modal
      v-if="blockDialog.visible"
      :title="String(t('admin.moderation.blockWithReason') || '屏蔽并告知原因')"
      size="sm"
      @close="closeBlockDialog"
    >
      <div class="space-y-3">
        <div class="text-sm text-subtle line-clamp-2">{{ blockDialog.preview || '-' }}</div>
        <glass-textarea
          v-model="blockDialog.reason"
          :rows="4"
          :placeholder="String(t('admin.moderation.blockReasonPlaceholder') || '请输入屏蔽原因')"
        />
      </div>
      <template #footer>
        <Button type="button" variant="secondary" @click="closeBlockDialog">{{ t('common.cancel') || '取消' }}</Button>
        <Button type="button" variant="danger" :disabled="!blockDialog.reason.trim() || loading" @click="confirmBlockAction">
          <shield-exclamation-icon class="w-4 h-4 mr-1" />
          {{ t('admin.moderation.confirmBlock') || '确认屏蔽' }}
        </Button>
      </template>
    </glass-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { adminApi } from '@/api/admin.api'
import { notificationAPI } from '@/api/notification.api'
import { useUIStore } from '@/stores/ui'
import { downloadCsv } from '@/utils/download'
import {
  UsersIcon,
  ChatBubbleLeftRightIcon,
  ShieldExclamationIcon,
  TrashIcon,
  ArrowDownTrayIcon,
} from '@heroicons/vue/24/outline'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const ui = useUIStore()

const tab = ref<'posts' | 'comments'>(String((route.query as any)?.tab || 'posts') as any)
const tabOptions = computed(() => ([
  { label: t('admin.tabs.posts') || '帖子', value: 'posts' },
  { label: t('admin.tabs.comments') || '评论', value: 'comments' },
]))

const loading = ref(false)
const error = ref<string | null>(null)

function truncateText(value: any, max = 48): string {
  const text = String(value || '').replace(/\s+/g, ' ').trim()
  if (!text) return '-'
  return text.length > max ? `${text.slice(0, max)}...` : text
}

function displayUserName(user: any, fallback?: any): string {
  return String(user?.nickname || user?.username || fallback || '-')
}

function postAuthorLine(post: any): string {
  return String(t('admin.moderation.postedBy', { name: displayUserName(post?.author, post?.authorId) }) || `发布者：${displayUserName(post?.author, post?.authorId)}`)
}

function commentMetaLine(comment: any): string {
  const postTitle = truncateText(comment?.postTitle || comment?.title, 24)
  const postAuthor = displayUserName({ nickname: comment?.postAuthorName }, comment?.postAuthorId)
  return String(t('admin.moderation.commentOnPost', { title: postTitle, author: postAuthor }) || `帖子《${postTitle}》 · ${postAuthor}`)
}

function onPickTab(next: 'posts' | 'comments') {
  tab.value = next
  if (next === 'comments') {
    commentPage.value = 1
    commentPostId.value = ''
    syncRouteQuery('comments')
  } else {
    syncRouteQuery('posts')
  }
  error.value = null
  reload()
}

watch(() => route.query, (q) => {
  const next = String((q as any)?.tab || tab.value)
  if (next === 'posts' || next === 'comments') {
    tab.value = next
  }
  commentPostId.value = String((q as any)?.postId || '')
}, { deep: true })

// ---- Posts ----
const posts = ref<any[]>([])
const postPage = ref(1)
const postPageSize = ref(20)
const postTotal = ref(0)
const postTotalPages = ref(1)
const postKeyword = ref('')
const postStatus = ref('')
const includeDeleted = ref(false)

const postStatusOptions = computed(() => ([
  { label: t('admin.moderation.statusOption.all') || '全部', value: '' },
  { label: t('admin.moderation.statusOption.published') || '已发布', value: 'published' },
  { label: t('admin.moderation.statusOption.draft') || '草稿', value: 'draft' },
  { label: t('admin.moderation.statusOption.deleted') || '已删除', value: 'deleted' },
]))
const postStatusEditOptions = computed(() => postStatusOptions.value.filter(o => o.value !== ''))
const boolOptions = computed(() => ([
  { label: t('admin.moderation.bool.no') || '否', value: false },
  { label: t('admin.moderation.bool.yes') || '是', value: true },
]))

async function reloadPosts() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.pageCommunityPosts({
      page: postPage.value,
      size: postPageSize.value,
      keyword: postKeyword.value || undefined,
      status: postStatus.value || undefined,
      includeDeleted: Boolean(includeDeleted.value),
    })
    posts.value = res.items || []
    postTotal.value = Number(res.total || 0)
    postTotalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function moderatePost(id: number, data: any) {
  try {
    await adminApi.moderatePost(id, data)
    ui.showNotification({
      type: 'success',
      title: String(t('admin.moderation.notify.successTitle') || '成功'),
      message: String(t('admin.moderation.notify.updateSuccess') || '修改成功'),
    })
    await reloadPosts()
  } catch (e: any) {
    ui.showNotification({
      type: 'error',
      title: String(t('admin.moderation.notify.errorTitle') || '错误'),
      message: e?.message || String(t('admin.moderation.notify.updateFailed') || '修改失败'),
    })
  }
}

function syncRouteQuery(nextTab: 'posts' | 'comments', postId?: string) {
  const nextQuery: Record<string, any> = { ...route.query, tab: nextTab }
  if (postId) nextQuery.postId = postId
  else delete nextQuery.postId
  router.replace({ path: '/admin/moderation', query: nextQuery })
}

function openCommentsFromPost(postId: number) {
  tab.value = 'comments'
  commentPage.value = 1
  commentPostId.value = String(postId)
  syncRouteQuery('comments', String(postId))
  reloadComments()
}

// ---- Comments ----
const comments = ref<any[]>([])
const commentPage = ref(1)
const commentPageSize = ref(20)
const commentTotal = ref(0)
const commentTotalPages = ref(1)
const commentPostId = ref<string>(String((route.query as any)?.postId || ''))
const commentKeyword = ref('')
const commentStatus = ref('')
const commentIncludeDeleted = ref(false)

const commentStatusOptions = computed(() => ([
  { label: t('admin.moderation.statusOption.all') || '全部', value: '' },
  { label: t('admin.moderation.statusOption.published') || '已发布', value: 'published' },
  { label: t('admin.moderation.statusOption.deleted') || '已删除', value: 'deleted' },
]))
const commentStatusEditOptions = computed(() => commentStatusOptions.value.filter(o => o.value !== ''))

const currentStatus = computed({
  get: () => (tab.value === 'posts' ? postStatus.value : commentStatus.value),
  set: (v: string) => {
    if (tab.value === 'posts') postStatus.value = v
    else commentStatus.value = v
  },
})
const currentStatusOptions = computed(() => (tab.value === 'posts' ? postStatusOptions.value : commentStatusOptions.value))
const searchKeyword = computed({
  get: () => (tab.value === 'posts' ? postKeyword.value : commentKeyword.value),
  set: (v: string) => {
    if (tab.value === 'posts') postKeyword.value = v
    else commentKeyword.value = v
  },
})

const blockDialog = ref<{
  visible: boolean
  type: 'post' | 'comment'
  targetId?: number
  authorId?: number | string
  preview: string
  reason: string
}>({
  visible: false,
  type: 'post',
  targetId: undefined,
  authorId: undefined,
  preview: '',
  reason: '',
})

async function reloadComments() {
  loading.value = true
  error.value = null
  try {
    const postId = String(commentPostId.value || '').trim()
    const res = await adminApi.pageCommunityComments({
      page: commentPage.value,
      size: commentPageSize.value,
      postId: postId ? Number(postId) : undefined,
      keyword: commentKeyword.value || undefined,
      status: commentStatus.value || undefined,
      includeDeleted: Boolean(commentIncludeDeleted.value),
    })
    comments.value = res.items || []
    commentTotal.value = Number(res.total || 0)
    commentTotalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function moderateComment(id: number, data: any) {
  try {
    await adminApi.moderateComment(id, data)
    ui.showNotification({
      type: 'success',
      title: String(t('admin.moderation.notify.successTitle') || '成功'),
      message: String(t('admin.moderation.notify.updateSuccess') || '修改成功'),
    })
    await reloadComments()
  } catch (e: any) {
    ui.showNotification({
      type: 'error',
      title: String(t('admin.moderation.notify.errorTitle') || '错误'),
      message: e?.message || String(t('admin.moderation.notify.updateFailed') || '修改失败'),
    })
  }
}

function openBlockDialog(type: 'post' | 'comment', row: any) {
  blockDialog.value = {
    visible: true,
    type,
    targetId: Number(row?.id),
    authorId: row?.author?.id || row?.authorId,
    preview: type === 'post' ? String(row?.title || row?.content || '') : String(row?.content || ''),
    reason: '',
  }
}

function closeBlockDialog() {
  blockDialog.value = {
    visible: false,
    type: 'post',
    targetId: undefined,
    authorId: undefined,
    preview: '',
    reason: '',
  }
}

async function sendBlockNotice(reason: string) {
  const recipientId = blockDialog.value.authorId
  if (!recipientId) return
  await notificationAPI.batchSend({
    recipientIds: [recipientId],
    title: blockDialog.value.type === 'post' ? '社区帖子已被屏蔽' : '社区评论已被屏蔽',
    content: blockDialog.value.type === 'post'
      ? `你发布的社区帖子《${blockDialog.value.preview || '未命名帖子'}》已被管理员屏蔽。原因：${reason}`
      : `你发布的社区评论已被管理员屏蔽。原因：${reason}`,
    type: 'system',
    category: 'communityModeration',
    priority: 'high',
    relatedType: blockDialog.value.type === 'post' ? 'community_post' : 'community_comment',
    relatedId: String(blockDialog.value.targetId || ''),
  })
}

async function sendDeleteNotice(type: 'post' | 'comment', row: any) {
  const recipientId = row?.author?.id || row?.authorId
  if (!recipientId) return
  await notificationAPI.batchSend({
    recipientIds: [recipientId],
    title: type === 'post' ? '社区帖子已被删除' : '社区评论已被删除',
    content: type === 'post'
      ? `你发布的社区帖子《${String(row?.title || row?.content || '未命名帖子')}》已被管理员删除。`
      : '你发布的社区评论已被管理员删除。',
    type: 'system',
    category: 'communityModeration',
    priority: 'high',
    relatedType: type === 'post' ? 'community_post' : 'community_comment',
    relatedId: String(row?.id || ''),
  })
}

async function deletePostWithNotice(row: any) {
  try {
    await adminApi.moderatePost(Number(row?.id), { deleted: true })
    await sendDeleteNotice('post', row)
    ui.showNotification({
      type: 'success',
      title: String(t('admin.moderation.notify.successTitle') || '成功'),
      message: '删除成功，已通知作者',
    })
    await reloadPosts()
  } catch (e: any) {
    ui.showNotification({
      type: 'error',
      title: String(t('admin.moderation.notify.errorTitle') || '错误'),
      message: e?.message || String(t('admin.moderation.notify.updateFailed') || '修改失败'),
    })
  }
}

async function deleteCommentWithNotice(row: any) {
  try {
    await adminApi.moderateComment(Number(row?.id), { deleted: true })
    await sendDeleteNotice('comment', row)
    ui.showNotification({
      type: 'success',
      title: String(t('admin.moderation.notify.successTitle') || '成功'),
      message: '删除成功，已通知作者',
    })
    await reloadComments()
  } catch (e: any) {
    ui.showNotification({
      type: 'error',
      title: String(t('admin.moderation.notify.errorTitle') || '错误'),
      message: e?.message || String(t('admin.moderation.notify.updateFailed') || '修改失败'),
    })
  }
}

async function confirmBlockAction() {
  const reason = blockDialog.value.reason.trim()
  if (!reason || !blockDialog.value.targetId) return
  try {
    if (blockDialog.value.type === 'post') {
      await adminApi.moderatePost(blockDialog.value.targetId, { status: 'deleted', deleted: true })
      await sendBlockNotice(reason)
      await reloadPosts()
    } else {
      await adminApi.moderateComment(blockDialog.value.targetId, { status: 'deleted', deleted: true })
      await sendBlockNotice(reason)
      await reloadComments()
    }
    ui.showNotification({
      type: 'success',
      title: String(t('admin.moderation.notify.successTitle') || '成功'),
      message: String(t('admin.moderation.blockedAndNotified') || '已屏蔽并通知作者'),
    })
    closeBlockDialog()
  } catch (e: any) {
    ui.showNotification({
      type: 'error',
      title: String(t('admin.moderation.notify.errorTitle') || '错误'),
      message: e?.message || String(t('admin.moderation.notify.blockFailed') || '屏蔽失败'),
    })
  }
}

async function exportCommunity() {
  try {
    const blob = await adminApi.exportCommunityCsv()
    downloadCsv(blob, 'community_posts_comments.csv')
    ui.showNotification({
      type: 'success',
      title: String(t('admin.moderation.notify.successTitle') || '成功'),
      message: String(t('admin.moderation.notify.exportSuccess') || '导出成功'),
    })
  } catch (e: any) {
    ui.showNotification({
      type: 'error',
      title: String(t('admin.moderation.notify.errorTitle') || '错误'),
      message: e?.message || String(t('admin.moderation.notify.exportFailed') || '导出失败'),
    })
  }
}

async function reload() {
  if (tab.value === 'posts') return reloadPosts()
  return reloadComments()
}

function goCenter() {
  router.push('/admin/moderation/center')
}

onMounted(() => reload())
</script>
