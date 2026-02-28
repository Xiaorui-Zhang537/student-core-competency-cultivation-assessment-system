<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <page-header :title="t('shared.community.title')" :subtitle="t('shared.community.subtitle')">
        <template #actions>
          <div class="flex items-center space-x-3">
            <Button v-if="!isAdminModeration" variant="primary" class="whitespace-nowrap" @click="showCreatePostModal = true">
              <plus-icon class="w-4 h-4 mr-2" />
              {{ t('shared.community.createPost') }}
            </Button>
            <Button v-else variant="primary" class="whitespace-nowrap" @click="goBackToGovernance">
              <shield-check-icon class="w-4 h-4 mr-2" />
              {{ t('admin.moderation.backToGovernance') || '返回治理表格' }}
            </Button>
          </div>
        </template>
      </page-header>

      <!-- Community Stats (与教师端一致：无外框，仅网格排列的 StatCard) -->
      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
        <start-card class="relative z-10" :hoverable="false" :label="t('shared.community.stats.posts') as string" :value="safeStats.totalPosts" tone="blue" :icon="DocumentTextIcon" />
        <start-card class="relative z-10" :hoverable="false" :label="t('shared.community.stats.users') as string" :value="safeStats.totalUsers" tone="emerald" :icon="UserGroupIcon" />
        <start-card class="relative z-10" :hoverable="false" :label="t('shared.community.stats.comments') as string" :value="safeStats.totalComments" tone="violet" :icon="ChatBubbleLeftRightIcon" />
        <start-card class="relative z-10" :hoverable="false" :label="t('shared.community.stats.activeToday') as string" :value="safeStats.activeUsersToday" tone="amber" :icon="BoltIcon" />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- Left Sidebar -->
        <div class="lg:col-span-1 space-y-6">
          <!-- Categories -->
          <div class="p-4 filter-container rounded-2xl glass-tint-secondary" v-glass="{ strength: 'thin', interactive: false }">
            <h2 class="text-lg font-semibold text-base-content mb-4">{{ t('shared.community.categories.title') }}</h2>
            <div class="space-y-2">
              <Button
                v-for="category in categories"
                :key="category.id"
                variant="menu"
                class="w-full justify-between pl-4 pr-3 py-3 text-sm rounded-xl transition-colors"
                @click="onCategoryChange(category.id)"
                :class="filterOptions.category === category.id ? 'bg-soft-primary ring-soft-primary text-strong' : 'text-subtle hover-bg-soft'"
              >
                <span class="flex items-center ml-2">
                  <component :is="category.icon" class="w-5 h-5 mr-3" />
                   <span>{{ category.name }}</span>
                </span>
              </Button>
            </div>
          </div>

          <!-- Hot Topics -->
          <div v-if="hotTopics.length" class="p-4 filter-container rounded-2xl glass-tint-secondary" v-glass="{ strength: 'thin', interactive: false }">
            <h3 class="text-lg font-semibold text-base-content mb-4">{{ t('shared.community.hotTopics') }}</h3>
            <div class="space-y-3">
              <div
                v-for="(topic, index) in hotTopics"
                :key="topic.topic"
                @click="searchByTopic(topic.topic)"
                class="flex items-center justify-between p-2.5 rounded-xl cursor-pointer transition-all duration-200"
                :class="selectedTopicTag === topic.topic ? 'bg-soft-primary ring-soft-primary' : 'hover-bg-soft'"
              >
                <div class="flex items-center space-x-2">
                  <span
                    class="inline-flex items-center justify-center w-6 h-6 rounded-full text-[11px] font-semibold shadow-sm ring-1"
                    :class="rankBadgeClass(index)"
                  >
                    {{ index + 1 }}
                  </span>
                  <span class="text-sm text-base-content">#{{ topic.topic }}</span>
                </div>
                <span class="text-xs text-subtle">{{ t('shared.community.sidebar.postsCount', { count: topic.postCount }) }}</span>
              </div>
            </div>
          </div>

          <!-- Active Users -->
          <div v-if="activeUsers.length" class="p-4 filter-container rounded-2xl glass-tint-secondary" v-glass="{ strength: 'thin', interactive: false }">
            <h3 class="text-lg font-semibold text-base-content mb-4">{{ t('shared.community.activeUsers') }}</h3>
            <div class="space-y-3">
              <div v-for="user in activeUsers" :key="user.userId" class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <user-avatar v-if="user.avatarUrl" :avatar="user.avatarUrl" :size="32" :rounded="true" :fit="'cover'" />
                  <div v-else class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                    <user-icon class="w-4 h-4 text-gray-400" />
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-base-content">{{ displayUserName(user) }}</p>
                  <p class="text-xs text-subtle">{{ t('shared.community.sidebar.userPosts', { count: user.postCount }) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content: Posts List -->
        <div class="lg:col-span-3">
          <div class="p-4 glass-regular glass-tint-primary rounded-2xl" v-glass="{ strength: 'regular', interactive: true }">
            <filter-bar dense tint="primary" align="center" class="mb-4">
              <template #left>
                <div class="flex items-center gap-2 min-w-0">
                  <h2 class="text-base md:text-lg font-semibold text-base-content truncate">{{ currentCategoryName }}</h2>
                  <span class="text-xs text-subtle whitespace-nowrap">{{ t('shared.community.list.total', { count: totalPosts }) }}</span>
                  <span
                    v-if="selectedTopicTag"
                    class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-soft-primary text-strong ring-soft-primary"
                  >
                    #{{ selectedTopicTag }}
                    <button type="button" class="leading-none opacity-70 hover:opacity-100" @click="clearTopicFilter">×</button>
                  </span>
                </div>
              </template>
              <template #right>
                <div class="relative w-64 max-w-full">
                  <glass-search-input
                    v-model="filterOptions.keyword"
                    :placeholder="t('shared.community.list.searchPlaceholder') as string"
                    size="sm"
                    @keyup.enter="applyFilters"
                    @update:modelValue="onKeywordInput"
                  />
                </div>
                <div class="w-36">
                  <glass-popover-select
                    v-model="filterOptions.orderBy"
                    :options="[
                      {label: t('shared.community.list.order.latest') as string, value: 'latest'},
                      {label: t('shared.community.list.order.hot') as string, value: 'hot'},
                      {label: t('shared.community.list.order.comments') as string, value: 'comments'},
                      {label: t('shared.community.list.order.likes') as string, value: 'likes'},
                      {label: t('shared.community.list.order.views') as string, value: 'views'}
                    ]"
                    size="sm"
                    @change="applyFilters"
                  />
                </div>
              </template>
            </filter-bar>

            <!-- Posts List -->
            <div v-if="!loading && posts.length" class="space-y-4">
              <div
                v-for="post in posts"
                :key="post.id"
                class="glass-ultraThin glass-interactive rounded-2xl p-4"
                v-glass="{ strength: 'ultraThin', interactive: true }"
              >
                  <div class="flex items-start space-x-4">
                  <div class="flex-shrink-0">
                    <user-avatar v-if="post.author?.avatar" :avatar="post.author.avatar" :size="40" :rounded="true" :fit="'cover'" />
                    <div v-else class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                      <user-icon class="w-5 h-5 text-gray-400" />
                    </div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div
                      class="cursor-pointer"
                      role="button"
                      tabindex="0"
                      @click="viewPost(post.id)"
                      @keydown.enter.prevent="viewPost(post.id)"
                    >
                      <div class="flex items-start justify-between gap-3">
                        <div class="min-w-0">
                          <div class="flex items-center gap-2 mb-1 min-w-0">
                            <h3 class="text-base font-semibold text-base-content line-clamp-2">{{ post.title }}</h3>
                            <badge size="sm" class="shrink-0" :variant="categoryVariant(post.category)">{{ displayCategory(post.category) }}</badge>
                          </div>
                          <p class="text-sm text-subtle line-clamp-2 mb-2 whitespace-pre-line" v-html="post.content"></p>
                        </div>
                        <div class="flex-shrink-0 flex items-center gap-1.5">
                          <Button v-if="(post as any).attachments?.length" variant="ghost" size="xs" icon="download" class="text-subtle" @click.stop="downloadFirstAttachment(post)">
                            {{ t('shared.download') }}
                          </Button>
                          <Button
                            v-if="authStore.user?.id && String(authStore.user.id) === String(post.author?.id || post.authorId)"
                            variant="outline"
                            size="xs"
                            icon="edit"
                            class="text-subtle"
                            @click.stop="onEditPost(post)"
                          >
                            {{ t('shared.community.list.edit') }}
                          </Button>
                          <Button
                            v-if="authStore.user?.id && String(authStore.user.id) === String(post.author?.id || post.authorId)"
                            variant="outline"
                            size="xs"
                            icon="delete"
                            class="text-subtle"
                            @click.stop="onDeletePost(post.id)"
                          >
                            {{ t('shared.community.list.delete') }}
                          </Button>
                          <Button
                            v-if="isAdminModeration"
                            variant="secondary"
                            size="xs"
                            class="bg-amber-500 text-white border-amber-500 hover:bg-amber-600"
                            @click.stop="openBlockPostModal(post)"
                          >
                            <shield-exclamation-icon class="w-3.5 h-3.5 mr-1" />
                            {{ t('admin.moderation.block') || '屏蔽' }}
                          </Button>
                          <Button
                            v-if="isAdminModeration"
                            variant="danger"
                            size="xs"
                            @click.stop="onAdminDeletePost(post)"
                          >
                            <trash-icon class="w-3.5 h-3.5 mr-1" />
                            {{ t('common.delete') || '删除' }}
                          </Button>
                        </div>
                      </div>
                    </div>
                    <div class="flex items-center flex-wrap gap-x-4 gap-y-2 text-xs text-subtle mt-2">
                      <span>{{ displayUserName(post.author) || t('shared.community.list.anonymous') }}</span>
                      <span>{{ formatDate(post.createdAt) }}</span>
                      <div class="flex items-center gap-1"><eye-icon class="w-3.5 h-3.5" /><span>{{ post.viewCount }}</span></div>
                      <div class="flex items-center gap-1"><chat-bubble-left-icon class="w-3.5 h-3.5" /><span>{{ post.commentCount }}</span></div>
                      <Button size="xs" variant="reaction" @click.stop="communityStore.toggleLikePost(post.id)" :class="post.isLiked ? 'reaction-liked' : ''">
                        <template #icon>
                          <hand-thumb-up-icon class="w-3.5 h-3.5" />
                        </template>
                        <span>{{ post.likeCount }}</span>
                      </Button>
                    </div>
                    <div v-if="post.tags?.length" class="flex items-center flex-wrap gap-2 mt-2">
                      <badge v-for="tag in post.tags" :key="tag.id" size="sm" :variant="(tagVariantByName(tag.name) as 'primary' | 'secondary' | 'success' | 'warning' | 'info' | 'danger' | 'accent')">#{{ tag.name }}</badge>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Loading State -->
             <div v-if="loading" class="text-center py-12">
              <p>{{ t('shared.community.detail.loading') }}</p>
            </div>

            <!-- Empty State -->
            <div v-if="!loading && !posts.length" class="text-center py-12">
              <chat-bubble-left-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-base-content mb-2">{{ t('shared.community.list.emptyTitle') }}</h3>
              <p class="text-subtle mb-4">
                {{ filterOptions.keyword ? t('shared.community.list.emptyDescKeyword') : t('shared.community.list.emptyDescCategory') }}
              </p>
              <Button v-if="!isAdminModeration" variant="primary" class="whitespace-nowrap" @click="showCreatePostModal = true">
                <plus-icon class="w-4 h-4 mr-2" />
                {{ t('shared.community.list.publishFirst') }}
              </Button>
            </div>

            <!-- Pagination -->
            <div v-if="!loading && totalPosts > filterOptions.size" class="mt-6 flex justify-between items-center">
               <span class="text-sm text-subtle">{{ t('shared.community.list.total', { count: totalPosts }) }}</span>
              <div class="flex space-x-1">
                <Button size="sm" variant="outline" @click="changePage(filterOptions.page - 1)" :disabled="filterOptions.page === 1">{{ t('shared.community.list.prev') }}</Button>
                <Button size="sm" variant="outline" @click="changePage(filterOptions.page + 1)" :disabled="filterOptions.page * filterOptions.size >= totalPosts">{{ t('shared.community.list.next') }}</Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Post Modal (GlassModal) -->
      <glass-modal v-if="showCreatePostModal" :title="t('shared.community.modal.createTitle') as string" size="md" heightVariant="tall" @close="showCreatePostModal=false">
        <form id="createPostForm" @submit.prevent="handleCreatePost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.title') }}</label>
              <glass-input v-model="newPost.title" :placeholder="t('shared.community.modal.title') as string" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.category') }}</label>
              <glass-popover-select
                v-model="newPost.category"
                :options="categoryOptions"
                :teleport="false"
                size="md"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.tags') }}</label>
              <glass-tags-input v-model="newPost.selectedTags" :placeholder="t('shared.community.modal.addTags') as string || '添加标签（回车确认）'" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.content') }}</label>
              <glass-textarea v-model="newPost.content" :rows="6" :placeholder="t('shared.community.modal.contentPlaceholder') as string" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">附件</label>
              <file-upload
                ref="postUploader"
                :accept="'.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar,image/*,video/*'"
                :multiple="true"
                :autoUpload="false"
                :upload-url="`${baseURL}/files/upload`"
                :upload-headers="uploadHeaders"
                :upload-data="postUploadData"
                @upload-success="onPostUploadSuccess"
                @upload-error="onPostUploadError"
              />
            </div>
          
        </form>
        <template #footer>
          <Button type="button" variant="secondary" @click="showCreatePostModal = false">{{ t('shared.community.modal.cancel') }}</Button>
          <Button type="submit" form="createPostForm" variant="primary" :disabled="loading">{{ t('shared.community.modal.publish') }}</Button>
        </template>
      </glass-modal>

      <!-- Edit Post Modal (GlassModal) -->
      <glass-modal v-if="editModal.visible" :title="t('shared.community.modal.editTitle') as string" size="md" heightVariant="tall" @close="editModal.visible=false">
        <form id="editPostForm" @submit.prevent="handleUpdatePost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.title') }}</label>
              <glass-input v-model="editModal.form.title" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.category') }}</label>
              <glass-popover-select
                v-model="editModal.form.category"
                :options="categoryOptions"
                :teleport="false"
                size="md"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.tags') }}</label>
              <glass-tags-input v-model="editModal.form.tags" :placeholder="t('shared.community.modal.addTags') as string || '添加标签（回车确认）'" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.content') }}</label>
              <glass-textarea v-model="editModal.form.content" :rows="6" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.attachment') }}</label>
              <file-upload
                ref="postEditUploader"
                :accept="'.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar,image/*,video/*'"
                :multiple="true"
                :autoUpload="true"
                :upload-url="`${baseURL}/files/upload`"
                :upload-headers="uploadHeaders"
                :upload-data="editUploadData"
                @upload-success="onEditUploadSuccess"
                @upload-error="onPostUploadError"
              />
              <div v-if="editAttachments.length" class="mt-3">
                <h4 class="text-sm font-medium mb-2">{{ t('shared.community.modal.existingTitle') }}</h4>
                <ul class="divide-y divide-gray-200 dark:divide-gray-700">
                  <li v-for="f in editAttachments" :key="f.id" class="py-2 flex items-center justify-between">
                    <div class="min-w-0 mr-3">
                      <div class="text-sm truncate">{{ f.originalName || f.fileName || ('#' + f.id) }}</div>
                      <div class="text-xs text-gray-500">{{ (f.fileSize ? (f.fileSize/1024/1024).toFixed(1)+' MB' : '') }}</div>
                    </div>
                    <div class="flex items-center gap-2">
                      <Button size="sm" variant="outline" type="button" @click="downloadEditAttachment(f)">{{ t('shared.community.modal.download') }}</Button>
                      <Button size="sm" variant="danger" type="button" @click="deleteEditAttachment(f.id)">{{ t('shared.community.modal.delete') }}</Button>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          
        </form>
        <template #footer>
          <Button type="button" variant="secondary" @click="editModal.visible = false">{{ t('shared.community.modal.cancel') }}</Button>
          <Button type="submit" :disabled="loading" form="editPostForm" variant="primary">{{ t('shared.community.modal.save') }}</Button>
        </template>
      </glass-modal>

      <!-- Admin block modal -->
      <glass-modal
        v-if="adminBlock.visible"
        :title="String(t('admin.moderation.blockWithReason') || '屏蔽并告知原因')"
        size="sm"
        @close="closeBlockModal"
      >
        <div class="space-y-3">
          <div class="text-sm text-subtle line-clamp-2">{{ adminBlock.targetTitle }}</div>
          <glass-textarea
            v-model="adminBlock.reason"
            :rows="4"
            :placeholder="String(t('admin.moderation.blockReasonPlaceholder') || '请输入屏蔽原因')"
          />
        </div>
        <template #footer>
          <Button type="button" variant="secondary" @click="closeBlockModal">{{ t('common.cancel') || '取消' }}</Button>
          <Button type="button" variant="danger" :disabled="!adminBlock.reason.trim() || loading" @click="confirmBlockPost">
            <shield-exclamation-icon class="w-4 h-4 mr-1" />
            {{ t('admin.moderation.confirmBlock') || '确认屏蔽' }}
          </Button>
        </template>
      </glass-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useCommunityStore } from '@/stores/community';
import {
  UserIcon, PlusIcon, MagnifyingGlassIcon, ChatBubbleLeftIcon,
  EyeIcon, HandThumbUpIcon, BookOpenIcon, QuestionMarkCircleIcon,
  LightBulbIcon, AcademicCapIcon, ChatBubbleOvalLeftEllipsisIcon,
  DocumentTextIcon, ChatBubbleLeftRightIcon, BoltIcon, UserGroupIcon,
  ShieldExclamationIcon, ShieldCheckIcon, TrashIcon
} from '@heroicons/vue/24/outline';
import UserAvatar from '@/components/ui/UserAvatar.vue'
import Button from '@/components/ui/Button.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import Badge from '@/components/ui/Badge.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTagsInput from '@/components/ui/inputs/GlassTagsInput.vue'
import { resolveUserDisplayName } from '@/shared/utils/user'
import FilterBar from '@/components/ui/filters/FilterBar.vue'

import { useAuthStore } from '@/stores/auth';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';
import { fileApi } from '@/api/file.api';
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { adminApi } from '@/api/admin.api'
import { notificationAPI } from '@/api/notification.api'

const router = useRouter();
const route = useRoute();
const communityStore = useCommunityStore();
const authStore = useAuthStore();
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
const { t, locale } = useI18n()


const { posts, totalPosts, stats, hotTopics, activeUsers, loading } = storeToRefs(communityStore);
const isAdminModeration = computed(() =>
  authStore.userRole === 'ADMIN' && String(route.path || '').startsWith('/admin/moderation/center')
)
// 统一用户名展示
function displayUserName(u: any): string {
  return resolveUserDisplayName(u) || String(u?.nickname || u?.username || '')
}
const safeStats = computed(() => ({
  totalPosts: stats.value?.totalPosts ?? 0,
  totalUsers: stats.value?.totalUsers ?? 0,
  totalComments: stats.value?.totalComments ?? 0,
  activeUsersToday: stats.value?.activeUsersToday ?? 0,
}))

const showCreatePostModal = ref(false);
const selectedTopicTag = ref('');
const filterOptions = reactive<{ page: number; size: number; category: string; keyword: string; orderBy: 'latest' | 'hot' | 'comments' | 'likes' | 'views' }>({
  page: 1,
  size: 10,
  category: 'all',
  keyword: '',
  orderBy: 'latest',
});

const newPost = reactive({
  title: '',
  category: 'study',
  content: '',
  tagsInput: '',
  tagSearch: '',
  tagOptions: [] as { id: number; name: string }[],
  selectedTags: [] as string[],
});
const editModal = reactive<{ visible: boolean; form: { id?: number; title: string; category: string; content: string; tags: string[] } }>({
  visible: false,
  form: { id: undefined, title: '', category: 'study', content: '', tags: [] }
})
const adminBlock = reactive<{ visible: boolean; postId?: number; authorId?: number | string; reason: string; targetTitle: string }>({
  visible: false,
  postId: undefined,
  authorId: undefined,
  reason: '',
  targetTitle: '',
})
const postUploader = ref();
const postEditUploader = ref();
const postUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'community_post' });
const editUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'community_post' });
const editAttachments = ref<any[]>([]);
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};

const categoryOptions = computed(() => [
  { label: t('shared.community.categories.study') as string, value: 'study' },
  { label: t('shared.community.categories.help') as string, value: 'help' },
  { label: t('shared.community.categories.share') as string, value: 'share' },
  { label: t('shared.community.categories.qa') as string, value: 'qa' },
  { label: t('shared.community.categories.chat') as string, value: 'chat' },
])

const categories = computed(() => ([
  { id: 'all', name: t('shared.community.categories.all') as string, icon: ChatBubbleLeftIcon },
  { id: 'study', name: t('shared.community.categories.study') as string, icon: BookOpenIcon },
  { id: 'help', name: t('shared.community.categories.help') as string, icon: QuestionMarkCircleIcon },
  { id: 'share', name: t('shared.community.categories.share') as string, icon: LightBulbIcon },
  { id: 'qa', name: t('shared.community.categories.qa') as string, icon: AcademicCapIcon },
  { id: 'chat', name: t('shared.community.categories.chat') as string, icon: ChatBubbleOvalLeftEllipsisIcon }
]));
const currentCategoryName = computed(() => {
  return categories.value.find((c: any) => c.id === filterOptions.category)?.name || (t('shared.community.categories.all') as string)
})

const categoryIdToLabel: Record<string, string> = {
  study: '学习讨论',
  help: '作业求助',
  share: '经验分享',
  qa: '问答',
  chat: '闲聊',
}
const labelToCategoryId: Record<string, string> = Object.fromEntries(Object.entries(categoryIdToLabel).map(([k, v]) => [v, k]))

const applyFilters = () => {
  filterOptions.page = 1;
  const hasTagFilter = String(selectedTopicTag.value || '').trim() !== ''
  const keyword = hasTagFilter ? undefined : (String(filterOptions.keyword || '').trim() === '' ? undefined : filterOptions.keyword)
  if (isAdminModeration.value) {
    adminLoadPosts({
      page: filterOptions.page,
      size: filterOptions.size,
      keyword,
      status: undefined,
      includeDeleted: true,
    })
    return
  }
  const params: any = {
    page: filterOptions.page,
    size: filterOptions.size,
    orderBy: filterOptions.orderBy,
    // 标签筛选启用时，以后端 tag 精确筛选为准；关键词留空避免“内容搜索”误导。
    keyword,
    tag: hasTagFilter ? selectedTopicTag.value : undefined,
    category: filterOptions.category === 'all' ? undefined : categoryIdToLabel[filterOptions.category] || filterOptions.category
  };
  communityStore.fetchPosts(params);
};

const adminLoadPosts = async (params: { page: number; size: number; keyword?: string; status?: string; includeDeleted?: boolean }) => {
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  try {
    ui.setLoading(true)
    const res = await adminApi.pageCommunityPosts(params as any)
    posts.value = (res.items || []) as any
    totalPosts.value = Number(res.total || 0)
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    ui.setLoading(false)
  }
}

const onCategoryChange = (categoryId: string) => {
  filterOptions.category = categoryId;
  applyFilters();
};

const changePage = (page: number) => {
  if (page < 1 || (page - 1) * filterOptions.size >= totalPosts.value) return;
  filterOptions.page = page;
  const hasTagFilter = String(selectedTopicTag.value || '').trim() !== ''
  const keyword = hasTagFilter ? undefined : (String(filterOptions.keyword || '').trim() === '' ? undefined : filterOptions.keyword)
  if (isAdminModeration.value) {
    adminLoadPosts({
      page: filterOptions.page,
      size: filterOptions.size,
      keyword,
      status: undefined,
      includeDeleted: true,
    })
    return
  }
   const params: any = {
    ...filterOptions,
    // 标签筛选启用时，以后端 tag 精确筛选为准；关键词留空避免“内容搜索”误导。
    keyword,
    tag: hasTagFilter ? selectedTopicTag.value : undefined,
    category: filterOptions.category === 'all' ? undefined : filterOptions.category
  };
  communityStore.fetchPosts(params);
};

const searchByTopic = (topic: string) => {
  selectedTopicTag.value = String(topic || '').trim();
  filterOptions.keyword = '';
  applyFilters();
};

const clearTopicFilter = () => {
  selectedTopicTag.value = ''
  applyFilters()
}

const onKeywordInput = (v: string | null) => {
  const text = String(v ?? '').trim()
  if (text && selectedTopicTag.value) {
    selectedTopicTag.value = ''
  }
  if (!text) {
    applyFilters()
  }
}

const viewPost = (postId: number) => {
  if (isAdminModeration.value) return
  const routeName = authStore.userRole === 'TEACHER' ? 'TeacherPostDetail' : 'StudentPostDetail';
  router.push({ name: routeName, params: { id: postId } });
};

const askAiForPost = (post: any) => {
  const content = (post?.title ? `【问题标题】${post.title}\n` : '') +
                  (post?.content ? `【问题内容】${post.content}` : '')
  const target = authStore.userRole === 'TEACHER' ? { path: '/teacher/assistant' } : { path: '/student/assistant' }
  router.push({ ...target, query: { q: content } })
}

const onEditPost = (post: any) => {
  editModal.visible = true
  editModal.form.id = post.id
  editModal.form.title = post.title
  // 将后端中文分类转换为前端英文 id（保持选项值稳定）
  editModal.form.category = labelToCategoryId[post.category] || post.category
  editModal.form.content = post.content
  editModal.form.tags = Array.isArray(post.tags) ? post.tags.map((t:any)=> String(t.name || t)) : []
  editUploadData.relatedId = post.id
  refreshEditAttachments(post.id)
}

const handleUpdatePost = async () => {
  if (!editModal.form.id) return
  await communityStore.updatePost(editModal.form.id, {
    title: editModal.form.title,
    // 提交给后端中文分类（由稳定 id -> 中文标签）
    category: categoryIdToLabel[editModal.form.category] || editModal.form.category,
    content: editModal.form.content,
    tags: Array.isArray(editModal.form.tags) ? editModal.form.tags : [],
  })
  {
    const { useUIStore } = await import('@/stores/ui')
    const ui = useUIStore()
    ui.showNotification({ type: 'success', title: t('shared.community.notify.postUpdatedTitle') as string, message: t('shared.community.notify.postUpdatedMsg') as string })
  }
  editModal.visible = false
  applyFilters()
}

const handleCreatePost = async () => {
  const postData = {
    title: newPost.title,
    content: newPost.content,
    // 提交给后端中文分类（由稳定 id -> 中文标签）
    category: categoryIdToLabel[newPost.category] || newPost.category,
    tags: Array.from(new Set([...(newPost.selectedTags || [])])),
  };
  
  const created = await communityStore.createPost(postData);
  if (created) {
    const { useUIStore } = await import('@/stores/ui')
    const ui = useUIStore()
    ui.showNotification({ type: 'success', title: t('shared.community.notify.postCreatedTitle') as string, message: t('shared.community.notify.postCreatedMsg') as string })
    const postId = (created as any)?.id || (created as any)?.data?.id;
    if (postId && postUploader.value) {
      postUploadData.relatedId = postId;
      await postUploader.value.uploadFiles?.();
    }
    showCreatePostModal.value = false;
    newPost.title = '';
    newPost.content = '';
    newPost.tagsInput = '';
    newPost.tagSearch = '';
    newPost.selectedTags = [];
    newPost.category = 'study';
    applyFilters(); // Refresh list
  }
};

const onPostUploadSuccess = () => {
  // 可选：提示成功
};
const onPostUploadError = (msg: string) => {
  console.error('帖子附件上传失败:', msg);
};

const refreshEditAttachments = async (postId: number | string) => {
  const res: any = await fileApi.getRelatedFiles('community_post', postId);
  editAttachments.value = res?.data || res || [];
};

const onEditUploadSuccess = async () => {
  if (editModal.form.id) {
    await refreshEditAttachments(editModal.form.id);
  }
  const { useUIStore } = await import('@/stores/ui')
  const ui = useUIStore()
  ui.showNotification({ type: 'success', title: t('shared.community.notify.attachmentUploadedTitle') as string, message: t('shared.community.notify.attachmentUploadedMsg') as string })
};

const deleteEditAttachment = async (fileId: number | string) => {
  if (!editModal.form.id) return;
  await fileApi.deleteFile(String(fileId));
  await refreshEditAttachments(editModal.form.id);
};

const downloadEditAttachment = async (f: any) => {
  await fileApi.downloadFile(f.id, f.originalName || f.fileName || `attachment_${f.id}`);
};

let tagSearchTimer: number | undefined
const searchTags = async () => {
  if (tagSearchTimer) clearTimeout(tagSearchTimer)
  tagSearchTimer = window.setTimeout(async () => {
    if (!newPost.tagSearch.trim()) { newPost.tagOptions = []; return }
    const { communityApi } = await import('@/api/community.api')
    const res = await communityApi.searchTags(newPost.tagSearch, 10)
    newPost.tagOptions = (res || []).map(t => ({ id: (t as any).id, name: (t as any).name }))
  }, 300)
}

const addTag = (name: string) => {
  if (!name) return
  if (!newPost.selectedTags.includes(name)) newPost.selectedTags.push(name)
}

const removeTag = (name: string) => {
  newPost.selectedTags = newPost.selectedTags.filter(t => t !== name)
}

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return t('shared.community.list.justNow') as string;
  if (minutes < 60) return t('shared.community.list.minutesAgo', { count: minutes }) as string;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return t('shared.community.list.hoursAgo', { count: hours }) as string;
  const days = Math.floor(hours / 24);
  if (days < 7) return t('shared.community.list.daysAgo', { count: days }) as string;
  const loc = String(locale.value || 'zh-CN')
  return date.toLocaleDateString(loc.startsWith('zh') ? 'zh-CN' : 'en-US');
};

const getCategoryClass = (category: string) => {
  const classMap: { [key: string]: string } = {
    '学习讨论': 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300',
    '作业求助': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300',
    '经验分享': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300',
    '问答': 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-300',
    '闲聊': 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-300',
  };
  return classMap[category] || 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
};

// 根据当前语言显示分类的本地化文案
const displayCategory = (category: string) => {
  const mapZhToEn: Record<string, string> = {
    '学习讨论': t('shared.community.categories.study') as string,
    '作业求助': t('shared.community.categories.help') as string,
    '经验分享': t('shared.community.categories.share') as string,
    '问答': t('shared.community.categories.qa') as string,
    '闲聊': t('shared.community.categories.chat') as string,
  };
  const mapEnToKey: Record<string, string> = {
    study: t('shared.community.categories.study') as string,
    help: t('shared.community.categories.help') as string,
    share: t('shared.community.categories.share') as string,
    qa: t('shared.community.categories.qa') as string,
    chat: t('shared.community.categories.chat') as string,
  };
  // 若后端返回中文分类
  if (mapZhToEn[category]) return mapZhToEn[category];
  // 若后端返回英文 id
  // 兼容可能的 'study' | 'help' | 'share' | 'qa' | 'chat'
  return mapEnToKey[category] || category;
};

// 将后端分类映射到 Badge 语义色，保证不同分类颜色区分
const categoryVariant = (category: string) => {
  const key = (labelToCategoryId[category] || category).toString().toLowerCase()
  if (key === 'study') return 'primary'
  if (key === 'help') return 'warning'
  if (key === 'share') return 'success'
  if (key === 'qa') return 'info'
  if (key === 'chat') return 'secondary'
  return 'secondary'
}

// 为标签随机分配玻璃色系（稳定映射）
const tagColorMap = new Map<string, string>()
const tagVariants = ['primary','secondary','success','warning','info','purple','teal']
function tagVariantByName(name: string): string {
  const key = String(name || '').toLowerCase()
  if (tagColorMap.has(key)) return tagColorMap.get(key) as string
  const idx = Math.abs(hashCode(key)) % tagVariants.length
  const variant = tagVariants[idx]
  tagColorMap.set(key, variant)
  return variant
}
function hashCode(s: string): number {
  let h = 0
  for (let i=0;i<s.length;i++) { h = ((h<<5)-h) + s.charCodeAt(i); h |= 0 }
  return h
}

const rankBadgeClass = (index: number) => {
  if (index === 0) return 'bg-amber-500/20 text-amber-700 dark:text-amber-300 ring-amber-500/30'
  if (index === 1) return 'bg-slate-500/20 text-slate-700 dark:text-slate-300 ring-slate-500/30'
  if (index === 2) return 'bg-orange-500/20 text-orange-700 dark:text-orange-300 ring-orange-500/30'
  return 'bg-primary-500/15 text-primary-700 dark:text-primary-300 ring-primary-500/25'
}


onMounted(() => {
  communityStore.fetchCommunityStats();
  communityStore.fetchHotTopics();
  communityStore.fetchActiveUsers();
  applyFilters();
});

const goBackToGovernance = () => {
  router.push('/admin/moderation')
}

const openBlockPostModal = (post: any) => {
  adminBlock.visible = true
  adminBlock.postId = Number(post?.id)
  adminBlock.authorId = post?.author?.id || post?.authorId
  adminBlock.reason = ''
  adminBlock.targetTitle = String(post?.title || '')
}

const closeBlockModal = () => {
  adminBlock.visible = false
  adminBlock.postId = undefined
  adminBlock.authorId = undefined
  adminBlock.reason = ''
  adminBlock.targetTitle = ''
}

const sendBlockNotificationToAuthor = async (authorId: string | number | undefined, reason: string, postTitle: string) => {
  if (!authorId) return
  await notificationAPI.batchSend({
    recipientIds: [authorId],
    title: String(t('admin.moderation.blockNoticeTitle') || '社区内容已被屏蔽'),
    content: String(t('admin.moderation.blockNoticeContent', { reason, title: postTitle || '-' }) || `你的社区内容已被管理员屏蔽，原因：${reason}`),
    type: 'system',
    category: 'communityModeration',
    priority: 'high',
    relatedType: 'community_post',
    relatedId: String(adminBlock.postId || ''),
  })
}

const confirmBlockPost = async () => {
  if (!adminBlock.postId || !adminBlock.reason.trim()) return
  try {
    await adminApi.moderatePost(adminBlock.postId, { status: 'deleted', deleted: true })
    await sendBlockNotificationToAuthor(adminBlock.authorId, adminBlock.reason.trim(), adminBlock.targetTitle)
    {
      const { useUIStore } = await import('@/stores/ui')
      const ui = useUIStore()
      ui.showNotification({ type: 'success', title: 'OK', message: String(t('admin.moderation.blockedAndNotified') || '已屏蔽并通知作者') })
    }
    closeBlockModal()
    applyFilters()
  } catch (e: any) {
    const { useUIStore } = await import('@/stores/ui')
    const ui = useUIStore()
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

const onAdminDeletePost = async (post: any) => {
  if (!post?.id) return
  try {
    await adminApi.moderatePost(post.id, { deleted: true })
    {
      const { useUIStore } = await import('@/stores/ui')
      const ui = useUIStore()
      ui.showNotification({ type: 'success', title: 'OK', message: String(t('admin.moderation.deleted') || '删除成功') })
    }
    applyFilters()
  } catch (e: any) {
    const { useUIStore } = await import('@/stores/ui')
    const ui = useUIStore()
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

const onDeletePost = async (postId: number) => {
  if (!confirm(t('shared.community.confirm.deletePost') as string)) return;
  await communityStore.deletePost(postId);
  {
    const { useUIStore } = await import('@/stores/ui')
    const ui = useUIStore()
    ui.showNotification({ type: 'success', title: t('shared.community.notify.postDeletedTitle') as string, message: t('shared.community.notify.postDeletedMsg') as string })
  }
}

// 下载首个附件（如需列表可扩展UI，这里作为示例）
const downloadFirstAttachment = async (post: any) => {
  const att = (post.attachments || [])[0];
  if (!att) return;
  await fileApi.downloadFile(att.id, att.originalName || att.fileName || `attachment_${att.id}`);
}
</script>
