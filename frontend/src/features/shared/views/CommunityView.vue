<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Page Header -->
      <div class="mb-8">
            <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">{{ t('shared.community.title') }}</h1>
            <p class="text-gray-600 dark:text-gray-400">{{ t('shared.community.subtitle') }}</p>
          </div>
           <div class="flex items-center space-x-3">
             <Button variant="primary" class="whitespace-nowrap" @click="showCreatePostModal = true">
               <PlusIcon class="w-4 h-4 mr-2" />
               {{ t('shared.community.createPost') }}
             </Button>
           </div>
        </div>
      </div>

      <!-- Community Stats (统一为工作台小卡片样式) -->
      <div v-if="stats" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-8">
        <StatCard :label="t('shared.community.stats.posts') as string" :value="stats.totalPosts" tone="blue" :icon="DocumentTextIcon" />
        <StatCard :label="t('shared.community.stats.users') as string" :value="stats.totalUsers" tone="emerald" :icon="UserGroupIcon" />
        <StatCard :label="t('shared.community.stats.comments') as string" :value="stats.totalComments" tone="violet" :icon="ChatBubbleLeftRightIcon" />
        <StatCard :label="t('shared.community.stats.activeToday') as string" :value="stats.activeUsersToday" tone="amber" :icon="BoltIcon" />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- Left Sidebar -->
        <div class="lg:col-span-1 space-y-6">
          <!-- Categories -->
          <div class="p-4 filter-container rounded-lg shadow" v-glass="{ strength: 'thin', interactive: false }">
             <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ t('shared.community.categories.title') }}</h2>
            <div class="space-y-2">
              <button
                v-for="category in categories"
                :key="category.id"
                @click="onCategoryChange(category.id)"
                class="w-full flex items-center justify-between px-3 py-2 text-sm rounded-lg transition-colors"
                :class="filterOptions.category === category.id ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300' : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                 <span class="flex items-center">
                  <component :is="category.icon" class="w-4 h-4 mr-3" />
                   <span>{{ category.name }}</span>
                </span>
              </button>
            </div>
          </div>

          <!-- Hot Topics -->
          <div v-if="hotTopics.length" class="p-4 filter-container rounded-lg shadow" v-glass="{ strength: 'thin', interactive: false }">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ t('shared.community.hotTopics') }}</h3>
            <div class="space-y-3">
              <div
                v-for="(topic, index) in hotTopics"
                :key="topic.topic"
                @click="searchByTopic(topic.topic)"
                class="flex items-center justify-between p-2 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg cursor-pointer"
              >
                <div class="flex items-center space-x-2">
                  <span class="w-6 h-6 bg-red-500 text-white rounded text-xs flex items-center justify-center">{{ index + 1 }}</span>
                  <span class="text-sm text-gray-900 dark:text-white">#{{ topic.topic }}</span>
                </div>
                <span class="text-xs text-gray-500">{{ topic.postCount }}</span>
              </div>
            </div>
          </div>

          <!-- Active Users -->
          <div v-if="activeUsers.length" class="p-4 filter-container rounded-lg shadow" v-glass="{ strength: 'thin', interactive: false }">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">{{ t('shared.community.activeUsers') }}</h3>
            <div class="space-y-3">
              <div v-for="user in activeUsers" :key="user.userId" class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                  <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="avatar" class="w-8 h-8 rounded-full object-cover" />
                  <div v-else class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                    <UserIcon class="w-4 h-4 text-gray-400" />
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ user.nickname || user.username }}</p>
                  <p class="text-xs text-gray-500">{{ user.postCount }} 帖子</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content: Posts List -->
        <div class="lg:col-span-3">
          <div class="p-4 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
                {{ categories.find((c: { id: string; name: string; icon: any; }) => c.id === filterOptions.category)?.name || '全部帖子' }}
              </h2>
              <div class="flex items-center space-x-3">
                <div class="relative">
                  <MagnifyingGlassIcon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    v-model="filterOptions.keyword"
                    @keyup.enter="applyFilters"
                    type="text"
                    :placeholder="t('shared.community.list.searchPlaceholder')"
                    class="input input--glass pl-10 pr-4 py-2"
                  />
                </div>
                <div class="w-36">
                  <GlassPopoverSelect
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
              </div>
            </div>

            <!-- Posts List -->
            <div v-if="!loading && posts.length" class="space-y-4">
              <div
                v-for="post in posts"
                :key="post.id"
                class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg transition-colors"
              >
                  <div class="flex items-start space-x-4">
                  <div class="flex-shrink-0">
                    <img v-if="post.author?.avatar" :src="post.author.avatar" alt="avatar" class="w-10 h-10 rounded-full object-cover" />
                    <div v-else class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                      <UserIcon class="w-5 h-5 text-gray-400" />
                    </div>
                  </div>
                  <div class="flex-1 min-w-0" @click="viewPost(post.id)">
                   <div class="flex items-center space-x-2 mb-1">
                      <h3 class="text-sm font-medium text-gray-900 dark:text-white">{{ post.title }}</h3>
                       <div class="text-xs px-2 py-0.5 rounded-full" :class="getCategoryClass(post.category)">{{ displayCategory(post.category) }}</div>
                    </div>
                   <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mb-2" v-html="post.content"></p>
                    <div class="flex items-center space-x-4 text-xs text-gray-500">
                      <span>{{ post.author?.nickname || post.author?.username || t('shared.community.list.anonymous') }}</span>
                      <span>{{ formatDate(post.createdAt) }}</span>
                       <div class="flex items-center space-x-1"><EyeIcon class="w-3 h-3" /><span>{{ post.viewCount }}</span></div>
                      <div class="flex items-center space-x-1"><ChatBubbleLeftIcon class="w-3 h-3" /><span>{{ post.commentCount }}</span></div>
                      <button @click.stop="communityStore.toggleLikePost(post.id)" :class="post.isLiked ? 'text-red-500' : ''" class="flex items-center space-x-1">
                        <HandThumbUpIcon class="w-3 h-3" />
                        <span>{{ post.likeCount }}</span>
                      </button>
                    </div>
                    <div v-if="post.tags?.length" class="flex items-center space-x-2 mt-2">
                      <span v-for="tag in post.tags" :key="tag.id" class="text-xs text-primary-600 dark:text-primary-400">#{{ tag.name }}</span>
                    </div>
                  </div>
                  <div class="flex-shrink-0 space-x-2">
                    <Button v-if="post.attachments?.length" variant="ghost" size="sm" @click.stop="downloadFirstAttachment(post)">下载附件</Button>
                    <Button v-if="authStore.user?.id && String(authStore.user.id) === String(post.author?.id || post.authorId)" variant="ghost" size="sm" @click.stop="onEditPost(post)">{{ t('shared.community.list.edit') }}</Button>
                    <Button v-if="authStore.user?.id && String(authStore.user.id) === String(post.author?.id || post.authorId)" variant="danger" size="sm" @click.stop="onDeletePost(post.id)">{{ t('shared.community.list.delete') }}</Button>
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
              <ChatBubbleLeftIcon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">{{ t('shared.community.list.emptyTitle') }}</h3>
              <p class="text-gray-600 dark:text-gray-400 mb-4">
                {{ filterOptions.keyword ? t('shared.community.list.emptyDescKeyword') : t('shared.community.list.emptyDescCategory') }}
              </p>
              <Button variant="primary" class="whitespace-nowrap" @click="showCreatePostModal = true">
                <PlusIcon class="w-4 h-4 mr-2" />
                {{ t('shared.community.list.publishFirst') }}
              </Button>
            </div>

            <!-- Pagination -->
            <div v-if="!loading && totalPosts > filterOptions.size" class="mt-6 flex justify-between items-center">
               <span class="text-sm text-gray-500">{{ t('shared.community.list.total', { count: totalPosts }) }}</span>
              <div class="flex space-x-1">
                <Button size="sm" variant="outline" @click="changePage(filterOptions.page - 1)" :disabled="filterOptions.page === 1">{{ t('shared.community.list.prev') }}</Button>
                <Button size="sm" variant="outline" @click="changePage(filterOptions.page + 1)" :disabled="filterOptions.page * filterOptions.size >= totalPosts">{{ t('shared.community.list.next') }}</Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Post Modal -->
      <div v-if="showCreatePostModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="modal glass-thick p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto" v-glass="{ strength: 'thick', interactive: true }">
           <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">{{ t('shared.community.modal.createTitle') }}</h3>
          <form @submit.prevent="handleCreatePost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.title') }}</label>
              <input v-model="newPost.title" type="text" :placeholder="t('shared.community.modal.title')" class="input" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.category') }}</label>
              <select v-model="newPost.category" class="input">
                <option value="study">{{ t('shared.community.categories.study') }}</option>
                <option value="help">{{ t('shared.community.categories.help') }}</option>
                <option value="share">{{ t('shared.community.categories.share') }}</option>
                <option value="qa">{{ t('shared.community.categories.qa') }}</option>
                <option value="chat">{{ t('shared.community.categories.chat') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.tags') }}</label>
              <input v-model="newPost.tagsInput" type="text" :placeholder="t('shared.community.modal.tags')" class="input mb-2" />
              <div class="mb-2">
                <input v-model="newPost.tagSearch" @input="searchTags" type="text" :placeholder="t('shared.community.modal.tagSearch')" class="input" />
                <div v-if="newPost.tagOptions.length" class="mt-2 border border-gray-200 dark:border-gray-700 rounded-md divide-y divide-gray-100 dark:divide-gray-700">
                  <button v-for="opt in newPost.tagOptions" :key="opt.id" type="button" class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 dark:hover:bg-gray-700" @click="addTag(opt.name)">#{{ opt.name }}</button>
                </div>
              </div>
              <div v-if="newPost.selectedTags.length" class="flex flex-wrap gap-2">
                <span v-for="t in newPost.selectedTags" :key="t" class="text-xs px-2 py-1 rounded-full bg-primary-50 text-primary-700 dark:bg-primary-900 dark:text-primary-300">
                  #{{ t }}
                  <button type="button" class="ml-1" @click="removeTag(t)">×</button>
                </span>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.content') }}</label>
              <textarea v-model="newPost.content" rows="6" :placeholder="t('shared.community.modal.contentPlaceholder')" class="input" required></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">附件</label>
              <FileUpload
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
            <div class="flex justify-end space-x-3 pt-4">
              <Button type="button" variant="secondary" @click="showCreatePostModal = false">{{ t('shared.community.modal.cancel') }}</Button>
              <Button type="submit" variant="primary" :disabled="loading">{{ t('shared.community.modal.publish') }}</Button>
            </div>
          </form>
        </div>
      </div>

      <!-- Edit Post Modal -->
      <div v-if="editModal.visible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div class="modal glass-thick p-6 max-w-2xl w-full mx-4 max-h-[80vh] overflow-y-auto" v-glass="{ strength: 'thick', interactive: true }">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">{{ t('shared.community.modal.editTitle') }}</h3>
          <form @submit.prevent="handleUpdatePost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.title') }}</label>
              <input v-model="editModal.form.title" type="text" class="input" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.category') }}</label>
              <select v-model="editModal.form.category" class="input">
                <option value="study">{{ t('shared.community.categories.study') }}</option>
                <option value="help">{{ t('shared.community.categories.help') }}</option>
                <option value="share">{{ t('shared.community.categories.share') }}</option>
                <option value="qa">{{ t('shared.community.categories.qa') }}</option>
                <option value="chat">{{ t('shared.community.categories.chat') }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.content') }}</label>
              <textarea v-model="editModal.form.content" rows="6" class="input" required></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.community.modal.attachment') }}</label>
              <FileUpload
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
            <div class="flex justify-end space-x-3 pt-4">
              <Button type="button" variant="secondary" @click="editModal.visible = false">{{ t('shared.community.modal.cancel') }}</Button>
              <Button type="submit" :disabled="loading" variant="primary">{{ t('shared.community.modal.save') }}</Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useCommunityStore } from '@/stores/community';
import {
  UserIcon, PlusIcon, MagnifyingGlassIcon, ChatBubbleLeftIcon,
  EyeIcon, HandThumbUpIcon, BookOpenIcon, QuestionMarkCircleIcon,
  LightBulbIcon, AcademicCapIcon, ChatBubbleOvalLeftEllipsisIcon,
  DocumentTextIcon, ChatBubbleLeftRightIcon, BoltIcon, UserGroupIcon
} from '@heroicons/vue/24/outline';
import UserAvatar from '@/components/ui/UserAvatar.vue'
import Button from '@/components/ui/Button.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import StatCard from '@/components/ui/StatCard.vue'

import { useAuthStore } from '@/stores/auth';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';
import { fileApi } from '@/api/file.api';

const router = useRouter();
const communityStore = useCommunityStore();
const authStore = useAuthStore();
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
const { t } = useI18n()


const { posts, totalPosts, stats, hotTopics, activeUsers, loading } = storeToRefs(communityStore);

const showCreatePostModal = ref(false);
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
const editModal = reactive<{ visible: boolean; form: { id?: number; title: string; category: string; content: string } }>({
  visible: false,
  form: { id: undefined, title: '', category: 'study', content: '' }
})
const postUploader = ref();
const postEditUploader = ref();
const postUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'community_post' });
const editUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'community_post' });
const editAttachments = ref<any[]>([]);
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};

const categories = computed(() => ([
  { id: 'all', name: t('shared.community.categories.all') as string, icon: ChatBubbleLeftIcon },
  { id: 'study', name: t('shared.community.categories.study') as string, icon: BookOpenIcon },
  { id: 'help', name: t('shared.community.categories.help') as string, icon: QuestionMarkCircleIcon },
  { id: 'share', name: t('shared.community.categories.share') as string, icon: LightBulbIcon },
  { id: 'qa', name: t('shared.community.categories.qa') as string, icon: AcademicCapIcon },
  { id: 'chat', name: t('shared.community.categories.chat') as string, icon: ChatBubbleOvalLeftEllipsisIcon }
]));

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
  const params: any = {
    page: filterOptions.page,
    size: filterOptions.size,
    orderBy: filterOptions.orderBy,
    keyword: filterOptions.keyword,
    category: filterOptions.category === 'all' ? undefined : categoryIdToLabel[filterOptions.category] || filterOptions.category
  };
  communityStore.fetchPosts(params);
};

const onCategoryChange = (categoryId: string) => {
  filterOptions.category = categoryId;
  applyFilters();
};

const changePage = (page: number) => {
  if (page < 1 || (page - 1) * filterOptions.size >= totalPosts.value) return;
  filterOptions.page = page;
   const params: any = {
    ...filterOptions,
    category: filterOptions.category === 'all' ? undefined : filterOptions.category
  };
  communityStore.fetchPosts(params);
};

const searchByTopic = (topic: string) => {
  filterOptions.keyword = topic;
  applyFilters();
};

const viewPost = (postId: number) => {
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
    tags: Array.from(new Set([...(newPost.tagsInput.split(' ').filter(t => t)), ...newPost.selectedTags])),
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
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString('zh-CN');
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


onMounted(() => {
  communityStore.fetchCommunityStats();
  communityStore.fetchHotTopics();
  communityStore.fetchActiveUsers();
  applyFilters();
});

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
