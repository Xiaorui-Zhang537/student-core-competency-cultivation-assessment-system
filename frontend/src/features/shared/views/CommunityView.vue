<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">学习社区</h1>
            <p class="text-gray-600 dark:text-gray-400">与同学交流学习心得，分享知识经验</p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="showMyPosts">
              <user-icon class="w-4 h-4 mr-2" />
              我的帖子
            </button>
            <button variant="primary" @click="showCreatePostModal = true">
              <plus-icon class="w-4 h-4 mr-2" />
              发布帖子
            </button>
          </div>
        </div>
      </div>

      <!-- 社区统计 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
            {{ communityStore.stats.totalPosts }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">讨论帖子</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
            {{ communityStore.stats.activeMembers }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">活跃成员</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
            {{ communityStore.stats.todayPosts }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">今日发帖</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
            {{ communityStore.stats.totalViews }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总浏览量</p>
        </card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧分类导航 -->
        <div class="lg:col-span-1">
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">讨论分类</h2>
            </template>
            
            <div class="space-y-2">
              <button
                v-for="category in categories"
                :key="category.id"
                @click="onCategoryChange(category.id)"
                class="w-full flex items-center justify-between px-3 py-2 text-sm rounded-lg transition-colors"
                :class="currentCategory === category.id
                  ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                <div class="flex items-center">
                  <component :is="category.icon" class="w-4 h-4 mr-3" />
                  <span>{{ category.name }}</span>
                </div>
                <span v-if="category.count > 0" class="text-xs bg-gray-200 dark:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-full px-2 py-1">
                  {{ category.count }}
                </span>
              </button>
            </div>
          </card>

          <!-- 热门话题 -->
          <card padding="lg" class="mt-6">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">热门话题</h3>
            </template>
            
            <div class="space-y-3">
              <div
                v-for="topic in communityStore.hotTopics"
                :key="topic.id"
                @click="searchByTopic(topic.name)"
                class="flex items-center justify-between p-2 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg cursor-pointer"
              >
                <div class="flex items-center space-x-2">
                  <span class="w-6 h-6 bg-red-500 text-white rounded text-xs flex items-center justify-center">
                    {{ topic.rank }}
                  </span>
                  <span class="text-sm text-gray-900 dark:text-white">#{{ topic.name }}</span>
                </div>
                <span class="text-xs text-gray-500">{{ topic.postCount }}</span>
              </div>
            </div>
          </card>

          <!-- 活跃用户 -->
          <card padding="lg" class="mt-6">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">活跃用户</h3>
            </template>
            
            <div class="space-y-3">
              <div
                v-for="user in communityStore.activeUsers"
                :key="user.id"
                class="flex items-center space-x-3"
              >
                <div class="flex-shrink-0">
                  <img
                    v-if="user.avatar"
                    :src="user.avatar"
                    :alt="user.name"
                    class="w-8 h-8 rounded-full"
                  />
                  <div v-else class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                    <user-icon class="w-4 h-4 text-gray-400" />
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">{{ user.name }}</p>
                  <p class="text-xs text-gray-500">{{ user.postCount }} 帖子</p>
                </div>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧帖子列表 -->
        <div class="lg:col-span-3">
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
                  {{ getCurrentCategoryName() }}
                </h2>
                <div class="flex items-center space-x-3">
                  <!-- 搜索 -->
                  <div class="relative">
                    <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                      v-model="searchQuery"
                      type="text"
                      placeholder="搜索帖子..."
                      class="pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                      @input="onSearchChange"
                    />
                  </div>
                  
                  <!-- 排序 -->
                  <select v-model="sortBy" class="input input-sm" @change="onSortChange">
                    <option value="latest">最新发布</option>
                    <option value="popular">最多回复</option>
                    <option value="hottest">最多点赞</option>
                    <option value="views">最多浏览</option>
                  </select>
                </div>
              </div>
            </template>

            <!-- 帖子列表 -->
            <div class="space-y-4">
              <div
                v-for="post in filteredPosts"
                :key="post.id"
                @click="viewPost(post)"
                class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                :class="{ 'border-yellow-200 dark:border-yellow-700 bg-yellow-50 dark:bg-yellow-900/20': post.pinned }"
              >
                <div class="flex items-start space-x-4">
                  <!-- 用户头像 -->
                  <div class="flex-shrink-0">
                    <img
                      v-if="post.author.avatar"
                      :src="post.author.avatar"
                      :alt="post.author.name"
                      class="w-10 h-10 rounded-full"
                    />
                    <div v-else class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                      <user-icon class="w-5 h-5 text-gray-400" />
                    </div>
                  </div>
                  
                  <!-- 帖子内容 -->
                  <div class="flex-1 min-w-0">
                    <div class="flex items-start justify-between">
                      <div class="flex-1">
                        <div class="flex items-center space-x-2 mb-1">
                          <star-icon v-if="post.pinned" class="w-4 h-4 text-yellow-500" />
                          <h3 class="text-sm font-medium text-gray-900 dark:text-white">{{ post.title }}</h3>
                          <badge :variant="getCategoryVariant(post.category)" size="sm">
                            {{ getCategoryName(post.category) }}
                          </badge>
                        </div>
                        <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2 mb-2">
                          {{ post.content }}
                        </p>
                        <div class="flex items-center space-x-4 text-xs text-gray-500">
                          <span>{{ post.author.name }}</span>
                          <span>{{ formatDate(post.createdAt) }}</span>
                          <div class="flex items-center space-x-1">
                            <eye-icon class="w-3 h-3" />
                            <span>{{ post.views }}</span>
                          </div>
                          <div class="flex items-center space-x-1">
                            <chat-bubble-left-icon class="w-3 h-3" />
                            <span>{{ post.replies }}</span>
                          </div>
                          <div class="flex items-center space-x-1">
                            <hand-thumb-up-icon class="w-3 h-3" />
                            <span>{{ post.likes }}</span>
                          </div>
                        </div>
                        
                        <!-- 话题标签 -->
                        <div v-if="post.tags.length > 0" class="flex items-center space-x-2 mt-2">
                          <span
                            v-for="tag in post.tags.slice(0, 3)"
                            :key="tag"
                            @click.stop="searchByTopic(tag)"
                            class="text-xs text-primary-600 dark:text-primary-400 hover:underline cursor-pointer"
                          >
                            #{{ tag }}
                          </span>
                        </div>
                      </div>
                      
                      <!-- 最后回复信息 -->
                      <div v-if="post.lastReply" class="text-right">
                        <p class="text-xs text-gray-500">最后回复</p>
                        <p class="text-xs text-gray-900 dark:text-white">{{ post.lastReply.author }}</p>
                        <p class="text-xs text-gray-500">{{ formatDate(post.lastReply.createdAt) }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredPosts.length === 0" class="text-center py-12">
              <chat-bubble-left-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无帖子</h3>
              <p class="text-gray-600 dark:text-gray-400 mb-4">
                {{ searchQuery ? '没有找到匹配的帖子' : '这个分类还没有帖子' }}
              </p>
              <button variant="primary" @click="showCreatePostModal = true">
                <plus-icon class="w-4 h-4 mr-2" />
                发布第一个帖子
              </button>
            </div>

            <!-- 分页 -->
            <div v-if="filteredPosts.length > 0" class="mt-6 flex justify-between items-center">
              <span class="text-sm text-gray-500">
                显示 {{ Math.min((currentPage - 1) * pageSize + 1, filteredPosts.length) }} - {{ Math.min(currentPage * pageSize, filteredPosts.length) }} 条，共 {{ filteredPosts.length }} 条
              </span>
              <div class="flex space-x-1">
                <button
                  variant="ghost"
                  size="sm"
                  :disabled="currentPage === 1"
                  @click="currentPage--"
                >
                  上一页
                </button>
                <button
                  variant="ghost"
                  size="sm"
                  :disabled="currentPage === totalPages"
                  @click="currentPage++"
                >
                  下一页
                </button>
              </div>
            </div>
          </card>
        </div>
      </div>

      <!-- 发布帖子弹窗 -->
      <div v-if="showCreatePostModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">发布新帖子</h3>
          <form @submit.prevent="createPost" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                帖子标题
              </label>
              <input
                v-model="newPost.title"
                type="text"
                placeholder="输入帖子标题"
                class="input"
                required
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                分类
              </label>
              <select v-model="newPost.category" class="input">
                <option value="study">学习讨论</option>
                <option value="homework">作业求助</option>
                <option value="share">经验分享</option>
                <option value="qa">问答</option>
                <option value="chat">闲聊</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                话题标签
              </label>
              <input
                v-model="newPost.tagsInput"
                type="text"
                placeholder="输入话题标签，用空格分隔"
                class="input"
              />
              <p class="text-xs text-gray-500 mt-1">例如: 数学 微积分 期中考试</p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                帖子内容
              </label>
              <textarea
                v-model="newPost.content"
                rows="6"
                placeholder="分享你的想法..."
                class="input"
                required
              ></textarea>
            </div>
            
            <div class="flex items-center space-x-4">
              <label class="flex items-center">
                <input
                  v-model="newPost.anonymous"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">匿名发布</span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="newPost.allowComments"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">允许评论</span>
              </label>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button variant="outline" @click="showCreatePostModal = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="isCreating">
                发布帖子
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 查看帖子弹窗 -->
      <div v-if="showPostModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-4xl w-full mx-4 max-h-96 overflow-y-auto">
          <div class="flex justify-between items-start mb-4">
            <div class="flex-1">
              <div class="flex items-center space-x-2 mb-2">
                <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ selectedPost?.title }}</h3>
                <badge :variant="getCategoryVariant(selectedPost?.category || 'study')" size="sm">
                  {{ getCategoryName(selectedPost?.category || 'study') }}
                </badge>
              </div>
              <div class="flex items-center space-x-4 text-sm text-gray-500">
                <span>{{ selectedPost?.author.name }}</span>
                <span>{{ formatDateTime(selectedPost?.createdAt) }}</span>
                <div class="flex items-center space-x-1">
                  <eye-icon class="w-4 h-4" />
                  <span>{{ selectedPost?.views }}</span>
                </div>
              </div>
            </div>
            <button variant="ghost" @click="showPostModal = false">
              <x-mark-icon class="w-5 h-5" />
            </button>
          </div>
          
          <div class="space-y-4">
            <div class="prose dark:prose-invert max-w-none">
              <p class="text-gray-600 dark:text-gray-400">{{ selectedPost?.content }}</p>
            </div>
            
            <!-- 话题标签 -->
            <div v-if="selectedPost?.tags.length" class="flex items-center space-x-2">
              <span class="text-sm text-gray-500">话题:</span>
              <span
                v-for="tag in selectedPost.tags"
                :key="tag"
                class="text-sm text-primary-600 dark:text-primary-400 hover:underline cursor-pointer"
                @click="searchByTopic(tag)"
              >
                #{{ tag }}
              </span>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex items-center space-x-4 pt-4 border-t border-gray-200 dark:border-gray-600">
              <button
                variant="ghost"
                @click="likePost(selectedPost)"
                :class="selectedPost?.liked ? 'text-red-500' : ''"
              >
                <hand-thumb-up-icon class="w-4 h-4 mr-1" />
                {{ selectedPost?.likes }}
              </button>
              <button variant="ghost" @click="replyToPost">
                <chat-bubble-left-icon class="w-4 h-4 mr-1" />
                回复
              </button>
              <button variant="ghost" @click="sharePost">
                <share-icon class="w-4 h-4 mr-1" />
                分享
              </button>
            </div>
            
            <!-- 简单回复列表 -->
            <div v-if="selectedPost?.sampleReplies?.length" class="space-y-3">
              <h4 class="font-medium text-gray-900 dark:text-white">最新回复:</h4>
              <div
                v-for="reply in selectedPost.sampleReplies"
                :key="reply.id"
                class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
              >
                <div class="flex items-center space-x-2 mb-1">
                  <span class="text-sm font-medium text-gray-900 dark:text-white">{{ reply.author }}</span>
                  <span class="text-xs text-gray-500">{{ formatDate(reply.createdAt) }}</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ reply.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useCommunityStore } from '@/stores/community'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  UserIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  ChatBubbleLeftIcon,
  EyeIcon,
  HandThumbUpIcon,
  StarIcon,
  ShareIcon,
  XMarkIcon,
  BookOpenIcon,
  QuestionMarkCircleIcon,
  LightBulbIcon,
  AcademicCapIcon,
  ChatBubbleOvalLeftEllipsisIcon
} from '@heroicons/vue/24/outline'

// Stores
const communityStore = useCommunityStore()
const uiStore = useUIStore()

// 状态
const currentCategory = ref('all')
const searchQuery = ref('')
const sortBy = ref('latest')
const currentPage = ref(1)
const pageSize = 20
const showCreatePostModal = ref(false)
const showPostModal = ref(false)
const isCreating = ref(false)
const selectedPost = ref<any>(null)

// 分类
const categories = ref([
  { id: 'all', name: '全部帖子', icon: ChatBubbleLeftIcon, count: 0 },
  { id: 'study', name: '学习讨论', icon: BookOpenIcon, count: 0 },
  { id: 'homework', name: '作业求助', icon: QuestionMarkCircleIcon, count: 0 },
  { id: 'share', name: '经验分享', icon: LightBulbIcon, count: 0 },
  { id: 'qa', name: '问答', icon: AcademicCapIcon, count: 0 },
  { id: 'chat', name: '闲聊', icon: ChatBubbleOvalLeftEllipsisIcon, count: 0 }
])

// 新帖子表单
const newPost = reactive({
  title: '',
  category: 'study',
  content: '',
  tagsInput: '',
  anonymous: false,
  allowComments: true
})

// 计算属性
const filteredPosts = computed(() => {
  return communityStore.filteredPosts
})

const totalPages = computed(() => Math.ceil(communityStore.pagination.total / communityStore.pagination.pageSize))

// 方法
const getCurrentCategoryName = () => {
  const category = categories.value.find(c => c.id === currentCategory.value)
  return category ? category.name : '全部帖子'
}

const getCategoryVariant = (category: string) => {
  switch (category) {
    case 'study': return 'primary'
    case 'homework': return 'warning'
    case 'share': return 'success'
    case 'qa': return 'secondary'
    case 'chat': return 'info'
    default: return 'secondary'
  }
}

const getCategoryName = (category: string) => {
  switch (category) {
    case 'study': return '学习讨论'
    case 'homework': return '作业求助'
    case 'share': return '经验分享'
    case 'qa': return '问答'
    case 'chat': return '闲聊'
    default: return '其他'
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (dateString?: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

const searchByTopic = (topic: string) => {
  searchQuery.value = topic
  communityStore.setFilters({ keyword: topic })
  loadPosts()
}

const viewPost = async (post: any) => {
  selectedPost.value = await communityStore.fetchPostDetail(post.id)
  if (selectedPost.value) {
    showPostModal.value = true
  }
}

const createPost = async () => {
  if (!newPost.title.trim() || !newPost.content.trim()) {
    uiStore.showNotification({
      type: 'warning',
      title: '信息不完整',
      message: '请填写完整的帖子信息'
    })
    return
  }

  isCreating.value = true
  try {
    await communityStore.createPost({
      title: newPost.title,
      content: newPost.content,
      category: newPost.category,
      tagsInput: newPost.tagsInput,
      anonymous: newPost.anonymous,
      allowComments: newPost.allowComments
    })

    uiStore.showNotification({
      type: 'success',
      title: '发布成功',
      message: '帖子已成功发布'
    })

    showCreatePostModal.value = false
    Object.assign(newPost, {
      title: '',
      category: 'study',
      content: '',
      tagsInput: '',
      anonymous: false,
      allowComments: true
    })

    // 重新加载数据
    await loadInitialData()
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '发布失败',
      message: '发布帖子时发生错误'
    })
  } finally {
    isCreating.value = false
  }
}

const likePost = async (post: any) => {
  try {
    const liked = await communityStore.likePost(post.id)
    
    uiStore.showNotification({
      type: liked ? 'success' : 'info',
      title: liked ? '点赞成功' : '取消点赞',
      message: liked ? '感谢你的点赞！' : '已取消对此帖子的点赞'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '操作失败',
      message: '点赞操作失败，请重试'
    })
  }
}

const replyToPost = () => {
  uiStore.showNotification({
    type: 'info',
    title: '回复功能',
    message: '回复功能开发中...'
  })
}

const sharePost = () => {
  uiStore.showNotification({
    type: 'info',
    title: '分享功能',
    message: '分享功能开发中...'
  })
}

const showMyPosts = () => {
  uiStore.showNotification({
    type: 'info',
    title: '我的帖子',
    message: '个人帖子管理功能开发中...'
  })
}

const loadPosts = async () => {
  await communityStore.fetchPosts({
    page: currentPage.value,
    size: pageSize,
    category: currentCategory.value === 'all' ? undefined : currentCategory.value,
    keyword: searchQuery.value,
    orderBy: sortBy.value
  })
}

const loadInitialData = async () => {
  await Promise.all([
    communityStore.fetchStats(),
    communityStore.fetchHotTopics(),
    communityStore.fetchActiveUsers(),
    loadPosts()
  ])
  
  // 更新分类计数 - 使用stats中的数据
  categories.value.forEach(category => {
    if (category.id === 'all') {
      category.count = communityStore.stats.totalPosts
    } else {
      // 暂时设为0，后续可以通过API获取具体分类统计
      category.count = 0
    }
  })
}

// 监听筛选条件变化
const onCategoryChange = (categoryId: string) => {
  currentCategory.value = categoryId
  currentPage.value = 1
  communityStore.setFilters({ category: categoryId === 'all' ? '' : categoryId })
  loadPosts()
}

const onSearchChange = () => {
  currentPage.value = 1
  communityStore.setFilters({ keyword: searchQuery.value })
  loadPosts()
}

const onSortChange = () => {
  currentPage.value = 1
  communityStore.setFilters({ orderBy: sortBy.value })
  loadPosts()
}

// 生命周期
onMounted(() => {
  loadInitialData()
})
</script> 