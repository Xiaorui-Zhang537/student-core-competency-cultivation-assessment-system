<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.community')" :subtitle="t('admin.title')" />

    <card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <glass-search-input v-model="keyword" :placeholder="String(t('common.search') || '搜索标题/内容')" size="sm" tint="secondary" />
        <div class="flex gap-2 flex-wrap">
          <glass-popover-select v-model="status" :options="statusOptions" size="sm" width="160px" />
          <glass-popover-select v-model="includeDeleted" :options="includeDeletedOptions" size="sm" width="160px" />
          <button size="sm" variant="outline" :disabled="loading" @click="reloadPosts">{{ t('common.search') || '查询' }}</button>
        </div>
      </div>
    </card>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reloadPosts"
    />

    <card v-else padding="md" tint="secondary" class="mt-4">
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.moderation.post') || t('common.columns.content') || 'Post' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.status') || t('common.columns.status') || 'Status' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.pinned') || t('common.columns.pinned') || 'Pinned' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.comments') || t('common.columns.comments') || 'Comments' }}</th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.actions') || t('common.columns.actions') || 'Actions' }}</th>
          </tr>
        </template>
        <template #body>
          <tr v-for="p in posts" :key="p.id" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center font-mono text-xs">{{ p.id }}</td>
            <td class="px-6 py-3">
              <div class="font-medium">{{ p.title }}</div>
              <div class="text-xs text-subtle line-clamp-1">{{ p.content }}</div>
              <div class="text-xs text-subtle">by {{ p.author?.nickname || p.author?.username || p.authorId }}</div>
            </td>
            <td class="px-6 py-3 w-44 text-center">
              <glass-popover-select :model-value="p.status" :options="postStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderatePost(p.id, { status: String(v) })" />
            </td>
            <td class="px-6 py-3 w-28 text-center">
              <glass-popover-select :model-value="Boolean(p.pinned)" :options="boolOptions" size="sm" @update:modelValue="(v:any)=>moderatePost(p.id, { pinned: Boolean(v) })" />
            </td>
            <td class="px-6 py-3 text-sm text-center">{{ p.commentsCount ?? 0 }}</td>
            <td class="px-6 py-3 text-right space-x-2 whitespace-nowrap">
              <button size="sm" variant="outline" @click="openComments(p.id)">{{ t('shared.community.comments') || '评论' }}</button>
              <button size="sm" variant="outline" @click="moderatePost(p.id, { deleted: true })">{{ t('common.delete') || '删除' }}</button>
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
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reloadPosts() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reloadPosts() }"
      />
    </card>

    <glass-modal v-if="showComments" :title="String(t('admin.tabs.comments') || t('shared.community.comments') || '评论')" size="lg" heightVariant="tall" @close="closeComments">
      <loading-overlay v-if="commentsLoading" :text="String(t('common.loading') || '加载中…')" />
      <card v-else padding="md" tint="secondary">
        <glass-table>
          <template #head>
            <tr>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('common.columns.content') || 'Content' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.status') || t('common.columns.status') || 'Status' }}</th>
              <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.delete') || t('common.columns.delete') || 'Delete' }}</th>
            </tr>
          </template>
          <template #body>
            <tr v-for="c in comments" :key="c.id" class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ c.id }}</td>
              <td class="px-6 py-3">
                <div class="text-sm whitespace-pre-line">{{ c.content }}</div>
                <div class="text-xs text-subtle">by {{ c.author?.nickname || c.author?.username || c.authorId }}</div>
              </td>
              <td class="px-6 py-3 w-44 text-center">
                <glass-popover-select :model-value="c.status" :options="commentStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderateComment(c.id, { status: String(v) })" />
              </td>
              <td class="px-6 py-3 text-right">
                <button size="sm" variant="outline" @click="moderateComment(c.id, { deleted: true })">{{ t('common.delete') || '删除' }}</button>
              </td>
            </tr>
            <tr v-if="comments.length === 0">
              <td colspan="4" class="px-6 py-6">
                <empty-state :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </template>
        </glass-table>
      </card>
    </glass-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import { adminApi } from '@/api/admin.api'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const ui = useUIStore()

const loading = ref(false)
const error = ref<string | null>(null)
const posts = ref<any[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)
const keyword = ref('')
const status = ref('')
const includeDeleted = ref(false)

const statusOptions = [
  { label: 'All', value: '' },
  { label: 'published', value: 'published' },
  { label: 'draft', value: 'draft' },
  { label: 'deleted', value: 'deleted' },
]
const includeDeletedOptions = [
  { label: 'Only active', value: false },
  { label: 'Include deleted', value: true },
]
const boolOptions = [
  { label: 'No', value: false },
  { label: 'Yes', value: true },
]
const postStatusEditOptions = statusOptions.filter(o => o.value !== '')
const commentStatusEditOptions = [
  { label: 'published', value: 'published' },
  { label: 'deleted', value: 'deleted' },
]

async function reloadPosts() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.pageCommunityPosts({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: status.value || undefined,
      includeDeleted: Boolean(includeDeleted.value),
    })
    posts.value = res.items || []
    total.value = Number(res.total || 0)
    totalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function moderatePost(id: number, data: any) {
  try {
    await adminApi.moderatePost(id, data)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Updated' })
    await reloadPosts()
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

// comments modal
const showComments = ref(false)
const commentsLoading = ref(false)
const comments = ref<any[]>([])
const currentPostId = ref<number | null>(null)

async function openComments(postId: number) {
  showComments.value = true
  currentPostId.value = postId
  commentsLoading.value = true
  try {
    const res = await adminApi.pageCommunityComments({ page: 1, size: 100, postId })
    comments.value = res.items || []
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    commentsLoading.value = false
  }
}

function closeComments() {
  showComments.value = false
  comments.value = []
  currentPostId.value = null
}

async function moderateComment(id: number, data: any) {
  try {
    await adminApi.moderateComment(id, data)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Updated' })
    if (currentPostId.value) {
      const res = await adminApi.pageCommunityComments({ page: 1, size: 100, postId: currentPostId.value })
      comments.value = res.items || []
    }
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

onMounted(reloadPosts)
</script>

