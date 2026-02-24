<template>
  <div class="p-6">
    <page-header :title="t('admin.sidebar.moderation')" :subtitle="t('admin.title')" />

    <filter-bar tint="secondary" align="center" :dense="false" class="mt-4 mb-2 rounded-full h-19">
      <template #left>
        <segmented-pills :model-value="tab" :options="tabOptions" size="sm" variant="warning" @update:modelValue="(v:any) => onPickTab(String(v))" />
      </template>
      <template #right>
        <Button size="sm" variant="outline" :disabled="loading" @click="reload">{{ t('common.refresh') || '刷新' }}</Button>
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

    <!-- Ability Reports -->
    <div v-else-if="tab === 'ability'" class="mt-4 space-y-4">
      <filter-bar tint="secondary" align="center" :dense="false" class="rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="w-44">
              <glass-input v-model="abilityStudentId" type="number" :placeholder="String(t('admin.moderation.studentIdOptional') || 'studentId(可选)')" />
            </div>
            <div class="w-56">
              <glass-popover-select v-model="abilityReportType" :options="reportTypeOptions" size="sm" tint="info" />
            </div>
          </div>
        </template>
        <template #right>
          <Button size="sm" variant="outline" :disabled="loading" @click="reloadAbility">{{ t('common.search') || '查询' }}</Button>
        </template>
      </filter-bar>

      <card padding="md" tint="secondary">
        <glass-table>
          <template #head>
            <tr>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.tabs.students') || '学生' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.reportType') || '类型' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.score') || '分数' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.createdAt') || '创建时间' }}</th>
              <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.view') || '查看' }}</th>
            </tr>
          </template>

          <template #body>
            <tr v-for="r in abilityItems" :key="r.id" class="hover:bg-white/10 transition-colors duration-150">
              <td class="px-6 py-3 text-center font-mono text-xs">{{ r.id }}</td>
              <td class="px-6 py-3">
                <div class="font-medium">{{ r.studentName || '-' }}</div>
                <div class="text-xs text-subtle">#{{ r.studentId }} · {{ r.studentNumber || '-' }}</div>
              </td>
              <td class="px-6 py-3 text-sm text-center">{{ r.reportType }}</td>
              <td class="px-6 py-3 text-sm text-center">{{ r.overallScore ?? '-' }}</td>
              <td class="px-6 py-3 text-xs text-subtle text-center">{{ r.createdAt || '-' }}</td>
              <td class="px-6 py-3 text-right">
                <Button size="sm" variant="outline" @click="openAbility(r.id)">{{ t('common.view') || '查看' }}</Button>
              </td>
            </tr>
            <tr v-if="abilityItems.length === 0">
              <td colspan="6" class="px-6 py-6">
                <empty-state :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </template>
        </glass-table>

        <pagination-bar
          :page="abilityPage"
          :page-size="abilityPageSize"
          :total-items="abilityTotal"
          :total-pages="abilityTotalPages"
          :disabled="loading"
          @update:page="(v: number) => { abilityPage = v; reloadAbility() }"
          @update:pageSize="(v: number) => { abilityPageSize = v; abilityPage = 1; reloadAbility() }"
        />
      </card>

      <glass-modal v-if="showAbilityDetail" :title="String(t('admin.tabs.abilityReports') || '能力报告')" size="lg" heightVariant="tall" @close="closeAbility">
        <ability-report-viewer v-if="abilityDetail" :report="abilityDetail" />
      </glass-modal>
    </div>

    <!-- Posts moderation -->
    <div v-else-if="tab === 'posts'" class="mt-4 space-y-4">
      <filter-bar tint="secondary" align="center" :dense="false" class="rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="w-72">
              <glass-search-input v-model="postKeyword" :placeholder="String(t('common.search') || '搜索标题/内容')" size="sm" tint="info" />
            </div>
            <div class="w-44">
              <glass-popover-select v-model="postStatus" :options="postStatusOptions" size="sm" tint="secondary" />
            </div>
            <div class="w-52">
              <glass-popover-select v-model="includeDeleted" :options="includeDeletedOptions" size="sm" tint="secondary" />
            </div>
          </div>
        </template>
        <template #right>
          <Button size="sm" variant="outline" :disabled="loading" @click="reloadPosts">{{ t('common.search') || '查询' }}</Button>
        </template>
      </filter-bar>

      <card padding="md" tint="secondary">
        <glass-table>
          <template #head>
            <tr>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.moderation.post') || '帖子' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.status') || '状态' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.pinned') || '置顶' }}</th>
              <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.comments') || '评论数' }}</th>
              <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.moderation.actions') || '操作' }}</th>
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
                <Button size="sm" variant="outline" @click="openCommentsFromPost(p.id)">{{ t('shared.community.comments') || '评论' }}</Button>
                <Button size="sm" variant="outline" @click="moderatePost(p.id, { deleted: true })">{{ t('common.delete') || '删除' }}</Button>
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

    <!-- Comments moderation -->
    <div v-else class="mt-4 space-y-4">
      <filter-bar tint="secondary" align="center" :dense="false" class="rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="w-44">
              <glass-input v-model="commentPostId" type="number" :placeholder="String(t('admin.moderation.postIdOptional') || 'postId(可选)')" />
            </div>
            <div class="w-72">
              <glass-search-input v-model="commentKeyword" :placeholder="String(t('common.search') || '搜索内容')" size="sm" tint="info" />
            </div>
            <div class="w-44">
              <glass-popover-select v-model="commentStatus" :options="commentStatusOptions" size="sm" tint="secondary" />
            </div>
            <div class="w-52">
              <glass-popover-select v-model="commentIncludeDeleted" :options="includeDeletedOptions" size="sm" tint="secondary" />
            </div>
          </div>
        </template>
        <template #right>
          <Button size="sm" variant="outline" :disabled="loading" @click="reloadComments">{{ t('common.search') || '查询' }}</Button>
        </template>
      </filter-bar>

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
                <div class="text-sm whitespace-pre-line">{{ c.content }}</div>
                <div class="text-xs text-subtle">post #{{ c.postId }} · by {{ c.author?.nickname || c.author?.username || c.authorId }}</div>
              </td>
              <td class="px-6 py-3 w-44 text-center">
                <glass-popover-select :model-value="c.status" :options="commentStatusEditOptions" size="sm" @update:modelValue="(v:any)=>moderateComment(c.id, { status: String(v) })" />
              </td>
              <td class="px-6 py-3 text-right">
                <Button size="sm" variant="outline" @click="moderateComment(c.id, { deleted: true })">{{ t('common.delete') || '删除' }}</Button>
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
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import AbilityReportViewer from '@/shared/views/AbilityReportViewer.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { adminApi, type AbilityReport } from '@/api/admin.api'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const ui = useUIStore()

const tab = ref<'ability' | 'posts' | 'comments'>(String((route.query as any)?.tab || 'ability') as any)
const tabOptions = computed(() => ([
  { label: t('admin.tabs.abilityReports') || '能力报告', value: 'ability' },
  { label: t('admin.tabs.posts') || '帖子', value: 'posts' },
  { label: t('admin.tabs.comments') || '评论', value: 'comments' },
]))

const loading = ref(false)
const error = ref<string | null>(null)

function onPickTab(next: 'ability' | 'posts' | 'comments') {
  tab.value = next
  router.replace({ path: '/admin/moderation', query: { ...route.query, tab: next } })
  error.value = null
  reload()
}

watch(() => route.query, (q) => {
  const next = String((q as any)?.tab || tab.value) as any
  if (next && next !== tab.value) tab.value = next
}, { deep: true })

// ---- Ability reports ----
const abilityItems = ref<AbilityReport[]>([])
const abilityPage = ref(1)
const abilityPageSize = ref(20)
const abilityTotal = ref(0)
const abilityTotalPages = ref(1)
const abilityStudentId = ref<string>('')
const abilityReportType = ref<string>('') // '' all
const reportTypeOptions = [
  { label: 'All', value: '' },
  { label: 'ai', value: 'ai' },
  { label: 'monthly', value: 'monthly' },
  { label: 'semester', value: 'semester' },
  { label: 'annual', value: 'annual' },
  { label: 'custom', value: 'custom' },
]

// ability detail
const showAbilityDetail = ref(false)
const abilityDetail = ref<AbilityReport | null>(null)

async function reloadAbility() {
  loading.value = true
  error.value = null
  try {
    const sid = String(abilityStudentId.value || '').trim()
    const res = await adminApi.pageAbilityReports({
      page: abilityPage.value,
      size: abilityPageSize.value,
      studentId: sid ? Number(sid) : undefined,
      reportType: abilityReportType.value || undefined,
    })
    abilityItems.value = res.items || []
    abilityTotal.value = Number(res.total || 0)
    abilityTotalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function openAbility(id: number) {
  showAbilityDetail.value = true
  abilityDetail.value = null
  try {
    abilityDetail.value = await adminApi.getAbilityReport(id)
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}
function closeAbility() {
  showAbilityDetail.value = false
  abilityDetail.value = null
}

// ---- Posts ----
const posts = ref<any[]>([])
const postPage = ref(1)
const postPageSize = ref(20)
const postTotal = ref(0)
const postTotalPages = ref(1)
const postKeyword = ref('')
const postStatus = ref('')
const includeDeleted = ref(false)

const postStatusOptions = [
  { label: 'All', value: '' },
  { label: 'published', value: 'published' },
  { label: 'draft', value: 'draft' },
  { label: 'deleted', value: 'deleted' },
]
const postStatusEditOptions = postStatusOptions.filter(o => o.value !== '')
const includeDeletedOptions = [
  { label: 'Only active', value: false },
  { label: 'Include deleted', value: true },
]
const boolOptions = [
  { label: 'No', value: false },
  { label: 'Yes', value: true },
]

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
    ui.showNotification({ type: 'success', title: 'OK', message: 'Updated' })
    await reloadPosts()
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

function openCommentsFromPost(postId: number) {
  tab.value = 'comments'
  commentPostId.value = String(postId)
  router.replace({ path: '/admin/moderation', query: { ...route.query, tab: 'comments', postId: String(postId) } })
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

const commentStatusOptions = [
  { label: 'All', value: '' },
  { label: 'published', value: 'published' },
  { label: 'deleted', value: 'deleted' },
]
const commentStatusEditOptions = commentStatusOptions.filter(o => o.value !== '')

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
    ui.showNotification({ type: 'success', title: 'OK', message: 'Updated' })
    await reloadComments()
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

async function reload() {
  if (tab.value === 'ability') return reloadAbility()
  if (tab.value === 'posts') return reloadPosts()
  return reloadComments()
}

onMounted(() => reload())
</script>

