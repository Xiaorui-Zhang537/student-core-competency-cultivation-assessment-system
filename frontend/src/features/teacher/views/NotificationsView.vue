<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">通知中心</h1>
            <p class="text-gray-600 dark:text-gray-400">管理系统通知和消息推送设置</p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="markAllAsRead" :disabled="unreadCount === 0">
              <check-icon class="w-4 h-4 mr-2" />
              全部标记已读
            </button>
            <button variant="outline" @click="showNotificationSettings = true">
              <cog-icon class="w-4 h-4 mr-2" />
              通知设置
            </button>
            <button variant="primary" @click="showCreateNotificationModal = true">
              <plus-icon class="w-4 h-4 mr-2" />
              发送通知
            </button>
          </div>
        </div>
      </div>

      <!-- 通知统计 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
            {{ stats.total }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总通知数</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-red-600 dark:text-red-400 mb-1">
            {{ stats.unread }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">未读通知</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
            {{ stats.today }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">今日通知</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
            {{ stats.important }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">重要通知</p>
        </card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧通知分类 -->
        <div class="lg:col-span-1">
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">通知分类</h2>
            </template>
            
            <div class="space-y-2">
              <button
                v-for="category in categories"
                :key="category.id"
                @click="currentCategory = category.id"
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

          <!-- 快速操作 -->
          <card padding="lg" class="mt-6">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快速操作</h3>
            </template>
            
            <div class="space-y-2">
              <button variant="outline" size="sm" class="w-full justify-start" @click="createAnnouncement">
                <speaker-wave-icon class="w-4 h-4 mr-2" />
                发布公告
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="createReminder">
                <clock-icon class="w-4 h-4 mr-2" />
                设置提醒
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="scheduleNotification">
                <calendar-icon class="w-4 h-4 mr-2" />
                定时发送
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="manageTemplates">
                <document-text-icon class="w-4 h-4 mr-2" />
                通知模板
              </button>
            </div>
          </card>
        </div>

        <!-- 右侧通知列表 -->
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
                      placeholder="搜索通知..."
                      class="pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>
                  
                  <!-- 筛选 -->
                  <select v-model="filterType" class="input input-sm">
                    <option value="">全部类型</option>
                    <option value="system">系统通知</option>
                    <option value="announcement">公告</option>
                    <option value="reminder">提醒</option>
                    <option value="message">消息</option>
                  </select>
                  
                  <!-- 排序 -->
                  <select v-model="sortBy" class="input input-sm">
                    <option value="newest">最新优先</option>
                    <option value="oldest">最旧优先</option>
                    <option value="unread">未读优先</option>
                    <option value="important">重要优先</option>
                  </select>
                </div>
              </div>
            </template>

            <!-- 通知列表 -->
            <div class="space-y-4">
              <div
                v-for="notification in filteredNotifications"
                :key="notification.id"
                @click="readNotification(notification)"
                class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                :class="{
                  'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-700': !notification.read,
                  'border-red-200 dark:border-red-700': notification.priority === 'high'
                }"
              >
                <div class="flex items-start space-x-4">
                  <!-- 通知图标 -->
                  <div class="flex-shrink-0">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center"
                         :class="getNotificationIconBg(notification.type)">
                      <component :is="getNotificationIcon(notification.type)" class="w-5 h-5 text-white" />
                    </div>
                  </div>
                  
                  <!-- 通知内容 -->
                  <div class="flex-1 min-w-0">
                    <div class="flex items-start justify-between">
                      <div class="flex-1">
                        <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-1">
                          {{ notification.title }}
                          <span v-if="!notification.read" class="w-2 h-2 bg-blue-500 rounded-full inline-block ml-2"></span>
                        </h3>
                        <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">
                          {{ notification.content }}
                        </p>
                        <div class="flex items-center space-x-4 mt-2 text-xs text-gray-500">
                          <span>{{ formatDate(notification.createdAt) }}</span>
                          <badge :variant="getNotificationTypeVariant(notification.type)" size="sm">
                            {{ getNotificationTypeText(notification.type) }}
                          </badge>
                          <badge v-if="notification.priority === 'high'" variant="danger" size="sm">
                            重要
                          </badge>
                        </div>
                      </div>
                      
                      <!-- 操作按钮 -->
                      <div class="flex items-center space-x-1 ml-4">
                        <button
                          v-if="!notification.read"
                          variant="ghost"
                          size="sm"
                          @click.stop="markAsRead(notification)"
                          class="p-1"
                        >
                          <check-icon class="w-4 h-4" />
                        </button>
                        <button
                          variant="ghost"
                          size="sm"
                          @click.stop="toggleStarred(notification)"
                          class="p-1"
                          :class="notification.starred ? 'text-yellow-500' : ''"
                        >
                          <star-icon class="w-4 h-4" />
                        </button>
                        <button
                          variant="ghost"
                          size="sm"
                          @click.stop="deleteNotification(notification)"
                          class="p-1 text-red-600"
                        >
                          <trash-icon class="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredNotifications.length === 0" class="text-center py-12">
              <bell-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无通知</h3>
              <p class="text-gray-600 dark:text-gray-400">
                {{ searchQuery ? '没有找到匹配的通知' : '您还没有任何通知' }}
              </p>
            </div>

            <!-- 分页 -->
            <div v-if="filteredNotifications.length > 0" class="mt-6 flex justify-between items-center">
              <span class="text-sm text-gray-500">
                显示 {{ Math.min((currentPage - 1) * pageSize + 1, filteredNotifications.length) }} - {{ Math.min(currentPage * pageSize, filteredNotifications.length) }} 条，共 {{ filteredNotifications.length }} 条
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

      <!-- 发送通知弹窗 -->
      <div v-if="showCreateNotificationModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">发送新通知</h3>
          <form @submit.prevent="createNotification" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                通知标题
              </label>
              <input
                v-model="newNotification.title"
                type="text"
                placeholder="输入通知标题"
                class="input"
                required
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                通知类型
              </label>
              <select v-model="newNotification.type" class="input">
                <option value="announcement">公告</option>
                <option value="reminder">提醒</option>
                <option value="message">消息</option>
                <option value="system">系统通知</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                优先级
              </label>
              <select v-model="newNotification.priority" class="input">
                <option value="normal">普通</option>
                <option value="high">重要</option>
                <option value="urgent">紧急</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                接收对象
              </label>
              <select v-model="newNotification.target" class="input">
                <option value="all">全部用户</option>
                <option value="students">全部学生</option>
                <option value="teachers">全部教师</option>
                <option value="class">指定班级</option>
                <option value="custom">自定义</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                通知内容
              </label>
              <textarea
                v-model="newNotification.content"
                rows="4"
                placeholder="输入通知内容..."
                class="input"
                required
              ></textarea>
            </div>
            
            <div class="flex items-center space-x-4">
              <label class="flex items-center">
                <input
                  v-model="newNotification.sendEmail"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">发送邮件通知</span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="newNotification.sendSMS"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">发送短信通知</span>
              </label>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button variant="outline" @click="showCreateNotificationModal = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="isCreating">
                发送通知
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 通知设置弹窗 -->
      <div v-if="showNotificationSettings" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">通知设置</h3>
          
          <div class="space-y-6">
            <div>
              <h4 class="font-medium text-gray-900 dark:text-white mb-3">推送设置</h4>
              <div class="space-y-3">
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">系统通知</span>
                  <input
                    v-model="notificationSettings.system"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">学生消息</span>
                  <input
                    v-model="notificationSettings.studentMessage"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">作业提醒</span>
                  <input
                    v-model="notificationSettings.assignmentReminder"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">课程更新</span>
                  <input
                    v-model="notificationSettings.courseUpdate"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
              </div>
            </div>
            
            <div>
              <h4 class="font-medium text-gray-900 dark:text-white mb-3">邮件通知</h4>
              <div class="space-y-3">
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">重要通知</span>
                  <input
                    v-model="notificationSettings.emailImportant"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">每日摘要</span>
                  <input
                    v-model="notificationSettings.emailDaily"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">周报</span>
                  <input
                    v-model="notificationSettings.emailWeekly"
                    type="checkbox"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </label>
              </div>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end space-x-3">
            <button variant="outline" @click="showNotificationSettings = false">
              取消
            </button>
            <button variant="primary" @click="saveNotificationSettings">
              保存设置
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  CheckIcon,
  CogIcon,
  PlusIcon,
  MagnifyingGlassIcon,
  BellIcon,
  StarIcon,
  TrashIcon,
  SpeakerWaveIcon,
  ClockIcon,
  CalendarIcon,
  DocumentTextIcon,
  InboxIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  ChatBubbleLeftIcon
} from '@heroicons/vue/24/outline'

// Stores
const uiStore = useUIStore()

// 状态
const currentCategory = ref('all')
const searchQuery = ref('')
const filterType = ref('')
const sortBy = ref('newest')
const currentPage = ref(1)
const pageSize = 20
const showCreateNotificationModal = ref(false)
const showNotificationSettings = ref(false)
const isCreating = ref(false)

// 统计数据
const stats = reactive({
  total: 156,
  unread: 23,
  today: 8,
  important: 5
})

// 通知分类
const categories = ref([
  { id: 'all', name: '全部通知', icon: InboxIcon, count: 156 },
  { id: 'unread', name: '未读通知', icon: BellIcon, count: 23 },
  { id: 'important', name: '重要通知', icon: ExclamationTriangleIcon, count: 5 },
  { id: 'starred', name: '已收藏', icon: StarIcon, count: 12 },
  { id: 'system', name: '系统通知', icon: InformationCircleIcon, count: 45 },
  { id: 'messages', name: '消息通知', icon: ChatBubbleLeftIcon, count: 67 }
])

// 新通知表单
const newNotification = reactive({
  title: '',
  type: 'announcement',
  priority: 'normal',
  target: 'all',
  content: '',
  sendEmail: false,
  sendSMS: false
})

// 通知设置
const notificationSettings = reactive({
  system: true,
  studentMessage: true,
  assignmentReminder: true,
  courseUpdate: false,
  emailImportant: true,
  emailDaily: false,
  emailWeekly: true
})

// 通知数据
const notifications = ref([
  {
    id: '1',
    title: '系统维护通知',
    content: '系统将于今晚22:00-24:00进行维护更新，期间可能无法正常访问，请提前保存工作内容。',
    type: 'system',
    priority: 'high',
    read: false,
    starred: false,
    createdAt: '2024-01-15T10:30:00Z',
    sender: 'System'
  },
  {
    id: '2',
    title: '新学生张同学加入课程',
    content: '学生张同学已成功加入《高等数学》课程，请及时关注其学习进度。',
    type: 'message',
    priority: 'normal',
    read: false,
    starred: true,
    createdAt: '2024-01-15T09:15:00Z',
    sender: 'System'
  },
  {
    id: '3',
    title: '作业提交截止提醒',
    content: '《高等数学》第三章练习题将于明天23:59截止提交，目前还有12名学生未提交。',
    type: 'reminder',
    priority: 'high',
    read: true,
    starred: false,
    createdAt: '2024-01-14T16:20:00Z',
    sender: 'System'
  },
  {
    id: '4',
    title: '课程评价已开放',
    content: '本学期课程评价系统已开放，学生可以对课程进行评价和反馈。',
    type: 'announcement',
    priority: 'normal',
    read: true,
    starred: false,
    createdAt: '2024-01-14T14:10:00Z',
    sender: 'Admin'
  },
  {
    id: '5',
    title: '平台功能更新',
    content: '平台新增了学习分析功能，您可以查看更详细的学生学习数据和趋势分析。',
    type: 'system',
    priority: 'normal',
    read: true,
    starred: true,
    createdAt: '2024-01-13T11:45:00Z',
    sender: 'System'
  }
])

// 计算属性
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

const filteredNotifications = computed(() => {
  let result = notifications.value

  // 分类筛选
  if (currentCategory.value !== 'all') {
    switch (currentCategory.value) {
      case 'unread':
        result = result.filter(n => !n.read)
        break
      case 'important':
        result = result.filter(n => n.priority === 'high' || n.priority === 'urgent')
        break
      case 'starred':
        result = result.filter(n => n.starred)
        break
      case 'system':
        result = result.filter(n => n.type === 'system')
        break
      case 'messages':
        result = result.filter(n => n.type === 'message')
        break
    }
  }

  // 类型筛选
  if (filterType.value) {
    result = result.filter(n => n.type === filterType.value)
  }

  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(n => 
      n.title.toLowerCase().includes(query) ||
      n.content.toLowerCase().includes(query)
    )
  }

  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'newest':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'oldest':
        return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      case 'unread':
        if (a.read !== b.read) return a.read ? 1 : -1
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'important': {
        const priorityOrder: { [key: string]: number } = { urgent: 3, high: 2, normal: 1 }
        const priorityDiff = (priorityOrder[b.priority] || 1) - (priorityOrder[a.priority] || 1)
        if (priorityDiff !== 0) return priorityDiff
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      }
      default:
        return 0
    }
  })

  return result
})

const totalPages = computed(() => Math.ceil(filteredNotifications.value.length / pageSize))

// 方法
const getCurrentCategoryName = () => {
  const category = categories.value.find(c => c.id === currentCategory.value)
  return category ? category.name : '全部通知'
}

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'system': return InformationCircleIcon
    case 'announcement': return SpeakerWaveIcon
    case 'reminder': return ClockIcon
    case 'message': return ChatBubbleLeftIcon
    default: return BellIcon
  }
}

const getNotificationIconBg = (type: string) => {
  switch (type) {
    case 'system': return 'bg-blue-500'
    case 'announcement': return 'bg-green-500'
    case 'reminder': return 'bg-yellow-500'
    case 'message': return 'bg-purple-500'
    default: return 'bg-gray-500'
  }
}

const getNotificationTypeVariant = (type: string) => {
  switch (type) {
    case 'system': return 'primary'
    case 'announcement': return 'success'
    case 'reminder': return 'warning'
    case 'message': return 'secondary'
    default: return 'secondary'
  }
}

const getNotificationTypeText = (type: string) => {
  switch (type) {
    case 'system': return '系统'
    case 'announcement': return '公告'
    case 'reminder': return '提醒'
    case 'message': return '消息'
    default: return '通知'
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

const readNotification = (notification: any) => {
  if (!notification.read) {
    notification.read = true
    stats.unread--
    updateCategoryCount('unread', -1)
  }
}

const markAsRead = (notification: any) => {
  notification.read = true
  stats.unread--
  updateCategoryCount('unread', -1)
  
  uiStore.showNotification({
    type: 'success',
    title: '标记成功',
    message: '通知已标记为已读'
  })
}

const markAllAsRead = () => {
  notifications.value.forEach(notification => {
    if (!notification.read) {
      notification.read = true
    }
  })
  
  const unreadCount = stats.unread
  stats.unread = 0
  updateCategoryCount('unread', -unreadCount)
  
  uiStore.showNotification({
    type: 'success',
    title: '操作成功',
    message: '所有通知已标记为已读'
  })
}

const toggleStarred = (notification: any) => {
  notification.starred = !notification.starred
  
  const change = notification.starred ? 1 : -1
  updateCategoryCount('starred', change)
  
  uiStore.showNotification({
    type: 'success',
    title: notification.starred ? '已收藏' : '取消收藏',
    message: notification.starred ? '通知已添加到收藏' : '通知已从收藏移除'
  })
}

const deleteNotification = (notification: any) => {
  if (confirm('确定要删除这条通知吗？')) {
    const index = notifications.value.findIndex(n => n.id === notification.id)
    if (index > -1) {
      notifications.value.splice(index, 1)
      stats.total--
      
      if (!notification.read) {
        stats.unread--
        updateCategoryCount('unread', -1)
      }
      if (notification.starred) {
        updateCategoryCount('starred', -1)
      }
      if (notification.priority === 'high' || notification.priority === 'urgent') {
        stats.important--
        updateCategoryCount('important', -1)
      }
      
      updateCategoryCount('all', -1)
      updateCategoryCount(notification.type, -1)
      
      uiStore.showNotification({
        type: 'success',
        title: '删除成功',
        message: '通知已删除'
      })
    }
  }
}

const updateCategoryCount = (categoryId: string, change: number) => {
  const category = categories.value.find(c => c.id === categoryId)
  if (category) {
    category.count = Math.max(0, category.count + change)
  }
}

const createNotification = async () => {
  if (!newNotification.title.trim() || !newNotification.content.trim()) {
    uiStore.showNotification({
      type: 'warning',
      title: '信息不完整',
      message: '请填写完整的通知信息'
    })
    return
  }

  isCreating.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))

    const notification = {
      id: Date.now().toString(),
      title: newNotification.title,
      content: newNotification.content,
      type: newNotification.type,
      priority: newNotification.priority,
      read: false,
      starred: false,
      createdAt: new Date().toISOString(),
      sender: 'Teacher'
    }

    notifications.value.unshift(notification)
    stats.total++
    stats.unread++
    stats.today++
    
    if (notification.priority === 'high' || notification.priority === 'urgent') {
      stats.important++
      updateCategoryCount('important', 1)
    }
    
    updateCategoryCount('all', 1)
    updateCategoryCount('unread', 1)
    updateCategoryCount(notification.type, 1)

    uiStore.showNotification({
      type: 'success',
      title: '发送成功',
      message: '通知已成功发送'
    })

    showCreateNotificationModal.value = false
    Object.assign(newNotification, {
      title: '',
      type: 'announcement',
      priority: 'normal',
      target: 'all',
      content: '',
      sendEmail: false,
      sendSMS: false
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '发送失败',
      message: '发送通知时发生错误'
    })
  } finally {
    isCreating.value = false
  }
}

const saveNotificationSettings = () => {
  uiStore.showNotification({
    type: 'success',
    title: '设置已保存',
    message: '通知设置已更新'
  })
  showNotificationSettings.value = false
}

const createAnnouncement = () => {
  newNotification.type = 'announcement'
  showCreateNotificationModal.value = true
}

const createReminder = () => {
  newNotification.type = 'reminder'
  showCreateNotificationModal.value = true
}

const scheduleNotification = () => {
  uiStore.showNotification({
    type: 'info',
    title: '定时发送',
    message: '定时发送功能开发中...'
  })
}

const manageTemplates = () => {
  uiStore.showNotification({
    type: 'info',
    title: '通知模板',
    message: '通知模板管理功能开发中...'
  })
}

// 生命周期
onMounted(() => {
  // 更新分类计数
  categories.value.forEach(category => {
    switch (category.id) {
      case 'all':
        category.count = notifications.value.length
        break
      case 'unread':
        category.count = notifications.value.filter(n => !n.read).length
        break
      case 'important':
        category.count = notifications.value.filter(n => n.priority === 'high' || n.priority === 'urgent').length
        break
      case 'starred':
        category.count = notifications.value.filter(n => n.starred).length
        break
      case 'system':
        category.count = notifications.value.filter(n => n.type === 'system').length
        break
      case 'messages':
        category.count = notifications.value.filter(n => n.type === 'message').length
        break
    }
  })
})
</script> 