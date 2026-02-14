<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.community')" :subtitle="t('admin.title')" />

    <Card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <GlassSearchInput v-model="keyword" :placeholder="String(t('common.search') || '搜索标题/内容')" size="sm" tint="secondary" />
        <div class="flex gap-2 flex-wrap">
          <GlassPopoverSelect v-model="status" :options="statusOptions" size="sm" width="160px" />
          <GlassPopoverSelect v-model="includeDeleted" :options="includeDeletedOptions" size="sm" width="160px" />
          <Button size="sm" variant="outline" :disabled="loading" @click="reloadPosts">{{ t('common.search') || '查询' }}</Button>
        </div>
      </div>
    </Card>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <ErrorState
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reloadPosts"
    />

    <Card v-else padding="md" tint="secondary" class="mt-4">
      <div class="overflow-x-auto">
        <table class="table w-full">
          <thead>
            <tr>
              <th>ID</th>
              <th>Post</th>
              <th>Status</th>
              <th>Pinned</th>
              <th>Comments</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in posts" :key="p.id">
              <td class="font-mono text-xs">{{ p.id }}</td>
              <td>
                <div class="font-medium">{{ p.title }}</div>
                <div class="text-xs text-subtle line-clamp-1">{{ p.content }}</div>
                <div class="text-xs text-subtle">by {{ p.author?.nickname || p.author?.username || p.authorId }}</div>
              </td>
              <td class="w-44">
                <GlassPopoverSelect :model-value="p.status" :options="postStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderatePost(p.id, { status: String(v) })" />
              </td>
              <td class="w-28">
                <GlassPopoverSelect :model-value="Boolean(p.pinned)" :options="boolOptions" size="sm" @update:modelValue="(v:any)=>moderatePost(p.id, { pinned: Boolean(v) })" />
              </td>
              <td class="text-sm">{{ p.commentsCount ?? 0 }}</td>
              <td class="text-right space-x-2">
                <Button size="sm" variant="outline" @click="openComments(p.id)">{{ t('shared.community.comments') || '评论' }}</Button>
                <Button size="sm" variant="outline" @click="moderatePost(p.id, { deleted: true })">{{ t('common.delete') || '删除' }}</Button>
              </td>
            </tr>
            <tr v-if="posts.length === 0">
              <td colspan="6">
                <EmptyState :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PaginationBar
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reloadPosts() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reloadPosts() }"
      />
    </Card>

    <GlassModal v-if="showComments" :title="'Comments'" size="lg" heightVariant="tall" @close="closeComments">
      <loading-overlay v-if="commentsLoading" :text="String(t('common.loading') || '加载中…')" />
      <Card v-else padding="md" tint="secondary">
        <div class="overflow-x-auto">
          <table class="table w-full">
            <thead>
              <tr>
                <th>ID</th>
                <th>Content</th>
                <th>Status</th>
                <th class="text-right">Delete</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in comments" :key="c.id">
                <td class="font-mono text-xs">{{ c.id }}</td>
                <td>
                  <div class="text-sm whitespace-pre-line">{{ c.content }}</div>
                  <div class="text-xs text-subtle">by {{ c.author?.nickname || c.author?.username || c.authorId }}</div>
                </td>
                <td class="w-44">
                  <GlassPopoverSelect :model-value="c.status" :options="commentStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderateComment(c.id, { status: String(v) })" />
                </td>
                <td class="text-right">
                  <Button size="sm" variant="outline" @click="moderateComment(c.id, { deleted: true })">{{ t('common.delete') || '删除' }}</Button>
                </td>
              </tr>
              <tr v-if="comments.length === 0">
                <td colspan="4">
                  <EmptyState :title="String(t('common.empty') || '暂无数据')" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>
    </GlassModal>
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

