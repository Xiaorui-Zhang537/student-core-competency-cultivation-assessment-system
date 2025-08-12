<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Page Header -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">学习社区</h1>
            <p class="text-gray-600 dark:text-gray-400">与同学交流学习心得，分享知识经验</p>
          </div>
           <div class="flex items-center space-x-3">
             <button @click="showCreatePostModal = true" class="btn btn-primary inline-flex items-center whitespace-nowrap px-4">
               <plus-icon class="w-4 h-4 mr-2" />
               发布帖子
             </button>
           </div>
        </div>
      </div>

      <!-- Community Stats -->
      <div v-if="stats" class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="text-center p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">{{ stats.totalPosts }}</div>
          <p class="text-sm text-gray-600 dark:text-gray-400">讨论帖子</p>
        </div>
        <div class="text-center p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">{{ stats.totalUsers }}</div>
          <p class="text-sm text-gray-600 dark:text-gray-400">社区成员</p>
        </div>
        <div class="text-center p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">{{ stats.totalComments }}</div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总评论数</p>
        </div>
        <div class="text-center p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
          <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">{{ stats.activeUsersToday }}</div>
          <p class="text-sm text-gray-600 dark:text-gray-400">今日活跃</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- Left Sidebar -->
        <div class="lg:col-span-1 space-y-6">
          <!-- Categories -->
          <div class="p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
             <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">讨论分类</h2>
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
          <div v-if="hotTopics.length" class="p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">热门话题</h3>
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
          <div v-if="activeUsers.length" class="p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">活跃用户</h3>
            <div class="space-y-3">
              <div v-for="user in activeUsers" :key="user.userId" class="flex items-center space-x-3">
                <div class="flex-shrink-0">
                   <div class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                    <user-icon class="w-4 h-4 text-gray-400" />
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ user.username }}</p>
                  <p class="text-xs text-gray-500">{{ user.postCount }} 帖子</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content: Posts List -->
        <div class="lg:col-span-3">
          <div class="p-4 bg-white dark:bg-gray-800 rounded-lg shadow">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
                {{ categories.find((c: { id: string; name: string; icon: any; }) => c.id === filterOptions.category)?.name || '全部帖子' }}
              </h2>
              <div class="flex items-center space-x-3">
                <div class="relative">
                  <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    v-model="filterOptions.keyword"
                    @keyup.enter="applyFilters"
                    type="text"
                    placeholder="搜索帖子..."
                    class="input pl-10 pr-4 py-2"
                  />
                </div>
                <select v-model="filterOptions.orderBy" @change="applyFilters" class="input input-sm">
                  <option value="latest">最新发布</option>
                  <option value="hot">最热</option>
                  <option value="comments">评论最多</option>
                  <option value="likes">点赞最多</option>
                  <option value="views">浏览最多</option>
                </select>
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
                    <div class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                      <user-icon class="w-5 h-5 text-gray-400" />
                    </div>
                  </div>
                  <div class="flex-1 min-w-0" @click="viewPost(post.id)">
                    <div class="flex items-center space-x-2 mb-1">
                      <h3 class="text-sm font-medium text-gray-900 dark:text-white">{{ post.title }}</h3>
                       <div class="text-xs px-2 py-0.5 rounded-full" :class="getCategoryClass(post.category)">{{ post.category }}</div>
                    </div>
                    <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mb-2" v-html="post.content"></p>
                    <div class="flex items-center space-x-4 text-xs text-gray-500">
                      <span>{{ post.author?.username || '匿名用户' }}</span>
                      <span>{{ formatDate(post.createdAt) }}</span>
                      <div class="flex items-center space-x-1"><eye-icon class="w-3 h-3" /><span>{{ post.viewCount }}</span></div>
                      <div class="flex items-center space-x-1"><chat-bubble-left-icon class="w-3 h-3" /><span>{{ post.commentCount }}</span></div>
                      <button @click.stop="communityStore.toggleLikePost(post.id)" :class="post.isLiked ? 'text-red-500' : ''" class="flex items-center space-x-1">
                        <hand-thumb-up-icon class="w-3 h-3" />
                        <span>{{ post.likeCount }}</span>
                      </button>
                    </div>
                    <div v-if="post.tags?.length" class="flex items-center space-x-2 mt-2">
                      <span v-for="tag in post.tags" :key="tag.id" class="text-xs text-primary-600 dark:text-primary-400">#{{ tag.name }}</span>
                    </div>
                  </div>
                  <div class="flex-shrink-0 space-x-2">
                    <button v-if="authStore.user?.id && String(authStore.user.id) === String(post.author?.id || post.authorId)" class="btn btn-ghost btn-sm" @click.stop="onEditPost(post)">编辑</button>
                    <button v-if="authStore.user?.id && String(authStore.user.id) === String(post.author?.id || post.authorId)" class="btn btn-ghost btn-sm" @click.stop="onDeletePost(post.id)">删除</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Loading State -->
            <div v-if="loading" class="text-center py-12">
              <p>加载中...</p>
            </div>

            <!-- Empty State -->
            <div v-if="!loading && !posts.length" class="text-center py-12">
              <chat-bubble-left-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无帖子</h3>
              <p class="text-gray-600 dark:text-gray-400 mb-4">
                {{ filterOptions.keyword ? '没有找到匹配的帖子' : '这个分类还没有帖子' }}
              </p>
              <button @click="showCreatePostModal = true" class="btn btn-primary inline-flex items-center whitespace-nowrap px-4">
                <plus-icon class="w-4 h-4 mr-2" />
                发布第一个帖子
              </button>
            </div>

            <!-- Pagination -->
            <div v-if="!loading && totalPosts > filterOptions.size" class="mt-6 flex justify-between items-center">
               <span class="text-sm text-gray-500">共 {{ totalPosts }} 条</span>
              <div class="flex space-x-1">
                <button @click="changePage(filterOptions.page - 1)" :disabled="filterOptions.page === 1" class="btn btn-ghost">上一页</button>
                <button @click="changePage(filterOptions.page + 1)" :disabled="filterOptions.page * filterOptions.size >= totalPosts" class="btn btn-ghost">下一页</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Create Post Modal -->
      <div v-if="showCreatePostModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">发布新帖子</h3>
          <form @submit.prevent="handleCreatePost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">帖子标题</label>
              <input v-model="newPost.title" type="text" placeholder="输入帖子标题" class="input" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">分类</label>
              <select v-model="newPost.category" class="input">
                <option value="学习讨论">学习讨论</option>
                <option value="作业求助">作业求助</option>
                <option value="经验分享">经验分享</option>
                <option value="问答">问答</option>
                <option value="闲聊">闲聊</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">话题标签</label>
              <input v-model="newPost.tagsInput" type="text" placeholder="输入话题标签，用空格分隔" class="input mb-2" />
              <div class="mb-2">
                <input v-model="newPost.tagSearch" @input="searchTags" type="text" placeholder="搜索标签..." class="input" />
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
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">帖子内容</label>
              <textarea v-model="newPost.content" rows="6" placeholder="分享你的想法..." class="input" required></textarea>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">图片附件（可选）</label>
              <file-upload
                ref="postUploader"
                :accept="'image/*'"
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
              <button type="button" @click="showCreatePostModal = false" class="btn btn-outline">取消</button>
              <button type="submit" :disabled="loading" class="btn btn-primary">发布帖子</button>
            </div>
          </form>
        </div>
      </div>

      <!-- Edit Post Modal -->
      <div v-if="editModal.visible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">编辑帖子</h3>
          <form @submit.prevent="handleUpdatePost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">帖子标题</label>
              <input v-model="editModal.form.title" type="text" class="input" required />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">分类</label>
              <select v-model="editModal.form.category" class="input">
                <option value="学习讨论">学习讨论</option>
                <option value="作业求助">作业求助</option>
                <option value="经验分享">经验分享</option>
                <option value="问答">问答</option>
                <option value="闲聊">闲聊</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">帖子内容</label>
              <textarea v-model="editModal.form.content" rows="6" class="input" required></textarea>
            </div>
            <div class="flex justify-end space-x-3 pt-4">
              <button type="button" @click="editModal.visible = false" class="btn btn-outline">取消</button>
              <button type="submit" :disabled="loading" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useCommunityStore } from '@/stores/community';
import {
  UserIcon, PlusIcon, MagnifyingGlassIcon, ChatBubbleLeftIcon,
  EyeIcon, HandThumbUpIcon, BookOpenIcon, QuestionMarkCircleIcon,
  LightBulbIcon, AcademicCapIcon, ChatBubbleOvalLeftEllipsisIcon
} from '@heroicons/vue/24/outline';

import { useAuthStore } from '@/stores/auth';
import FileUpload from '@/components/forms/FileUpload.vue';
import { baseURL } from '@/api/config';

const router = useRouter();
const communityStore = useCommunityStore();
const authStore = useAuthStore();


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
  category: '学习讨论',
  content: '',
  tagsInput: '',
  tagSearch: '',
  tagOptions: [] as { id: number; name: string }[],
  selectedTags: [] as string[],
});
const editModal = reactive<{ visible: boolean; form: { id?: number; title: string; category: string; content: string } }>({
  visible: false,
  form: { id: undefined, title: '', category: '学习讨论', content: '' }
})
const postUploader = ref();
const postUploadData = reactive<{ purpose: string; relatedId?: string | number }>({ purpose: 'community_post' });
const uploadHeaders = {
  Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
};

const categories = ref([
  { id: 'all', name: '全部帖子', icon: ChatBubbleLeftIcon },
  { id: '学习讨论', name: '学习讨论', icon: BookOpenIcon },
  { id: '作业求助', name: '作业求助', icon: QuestionMarkCircleIcon },
  { id: '经验分享', name: '经验分享', icon: LightBulbIcon },
  { id: '问答', name: '问答', icon: AcademicCapIcon },
  { id: '闲聊', name: '闲聊', icon: ChatBubbleOvalLeftEllipsisIcon }
]);

const applyFilters = () => {
  filterOptions.page = 1;
  const params: any = {
    page: filterOptions.page,
    size: filterOptions.size,
    orderBy: filterOptions.orderBy,
    keyword: filterOptions.keyword,
    category: filterOptions.category === 'all' ? undefined : filterOptions.category
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
  router.push({ name: 'TeacherAiAssistant', query: { q: content } })
}

const onEditPost = (post: any) => {
  editModal.visible = true
  editModal.form.id = post.id
  editModal.form.title = post.title
  editModal.form.category = post.category
  editModal.form.content = post.content
}

const handleUpdatePost = async () => {
  if (!editModal.form.id) return
  await communityStore.updatePost(editModal.form.id, {
    title: editModal.form.title,
    category: editModal.form.category,
    content: editModal.form.content,
  })
  editModal.visible = false
  applyFilters()
}

const handleCreatePost = async () => {
  const postData = {
    title: newPost.title,
    content: newPost.content,
    category: newPost.category,
    tags: Array.from(new Set([...(newPost.tagsInput.split(' ').filter(t => t)), ...newPost.selectedTags])),
  };
  
  const created = await communityStore.createPost(postData);
  if (created) {
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
    newPost.category = '学习讨论';
    applyFilters(); // Refresh list
  }
};

const onPostUploadSuccess = () => {
  // 可选：提示成功
};
const onPostUploadError = (msg: string) => {
  console.error('帖子附件上传失败:', msg);
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


onMounted(() => {
  communityStore.fetchCommunityStats();
  communityStore.fetchHotTopics();
  communityStore.fetchActiveUsers();
  applyFilters();
});

const onDeletePost = async (postId: number) => {
  if (!confirm('确认删除该帖子？')) return;
  await communityStore.deletePost(postId);
}
</script>
