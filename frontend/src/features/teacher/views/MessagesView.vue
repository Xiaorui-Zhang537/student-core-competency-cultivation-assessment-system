<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">消息中心</h1>
            <p class="text-gray-600 dark:text-gray-400">管理与学生的消息交流</p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="markAllAsRead" :disabled="unreadCount === 0">
              <check-icon class="w-4 h-4 mr-2" />
              全部标为已读
            </button>
            <button variant="primary" @click="$router.push('/teacher/messages/compose')">
              <pencil-square-icon class="w-4 h-4 mr-2" />
              撰写消息
            </button>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
            {{ stats.totalMessages }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总消息数</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-red-600 dark:text-red-400 mb-1">
            {{ stats.unreadMessages }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">未读消息</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
            {{ stats.todayMessages }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">今日消息</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
            {{ stats.activeConversations }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">活跃对话</p>
        </card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 左侧消息列表 -->
        <div class="lg:col-span-1">
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">对话列表</h2>
                <div class="flex items-center space-x-2">
                  <button variant="ghost" size="sm" @click="refreshMessages">
                    <arrow-path-icon class="w-4 h-4" />
                  </button>
                  <button variant="ghost" size="sm" @click="showFilters = !showFilters">
                    <funnel-icon class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </template>

            <!-- 搜索和筛选 -->
            <div class="mb-4 space-y-3">
              <div class="relative">
                <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索对话..."
                  class="input pl-10"
                />
              </div>

              <div v-show="showFilters" class="space-y-2">
                <select v-model="statusFilter" class="input input-sm">
                  <option value="">全部状态</option>
                  <option value="unread">未读</option>
                  <option value="read">已读</option>
                  <option value="important">重要</option>
                  <option value="archived">已归档</option>
                </select>

                <select v-model="typeFilter" class="input input-sm">
                  <option value="">全部类型</option>
                  <option value="question">问题咨询</option>
                  <option value="assignment">作业相关</option>
                  <option value="grade">成绩相关</option>
                  <option value="general">一般交流</option>
                </select>
              </div>
            </div>

            <!-- 对话列表 -->
            <div class="space-y-2 max-h-96 overflow-y-auto">
              <div
                v-for="conversation in filteredConversations"
                :key="conversation.id"
                @click="selectConversation(conversation)"
                class="p-3 rounded-lg cursor-pointer transition-colors"
                :class="[
                  selectedConversation?.id === conversation.id
                    ? 'bg-primary-50 dark:bg-primary-900/20 border border-primary-200 dark:border-primary-700'
                    : 'hover:bg-gray-50 dark:hover:bg-gray-700',
                  !conversation.read ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                ]"
              >
                <div class="flex items-start space-x-3">
                  <div class="flex-shrink-0">
                    <img
                      v-if="conversation.student.avatar"
                      :src="conversation.student.avatar"
                      :alt="conversation.student.name"
                      class="w-10 h-10 rounded-full"
                    />
                    <div v-else class="w-10 h-10 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                      <user-icon class="w-5 h-5 text-gray-400" />
                    </div>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between">
                      <p class="text-sm font-medium text-gray-900 dark:text-white truncate">
                        {{ conversation.student.name }}
                      </p>
                      <div class="flex items-center space-x-1">
                        <span class="text-xs text-gray-500">{{ formatTime(conversation.lastMessage.timestamp) }}</span>
                        <div v-if="!conversation.read" class="w-2 h-2 bg-blue-500 rounded-full"></div>
                      </div>
                    </div>
                    <p class="text-sm text-gray-600 dark:text-gray-400 truncate">
                      {{ conversation.lastMessage.preview }}
                    </p>
                    <div class="flex items-center justify-between mt-1">
                      <badge :variant="getTypeVariant(conversation.type)" size="sm">
                        {{ getTypeText(conversation.type) }}
                      </badge>
                      <div class="flex items-center space-x-1">
                        <star-icon 
                          v-if="conversation.important" 
                          class="w-3 h-3 text-yellow-500" 
                        />
                        <paper-clip-icon 
                          v-if="conversation.hasAttachment" 
                          class="w-3 h-3 text-gray-400" 
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="mt-4 flex justify-between items-center">
              <span class="text-sm text-gray-500">共 {{ filteredConversations.length }} 个对话</span>
              <div class="flex space-x-1">
                <button variant="ghost" size="sm" :disabled="currentPage === 1" @click="currentPage--">
                  上一页
                </button>
                <button variant="ghost" size="sm" :disabled="currentPage === totalPages" @click="currentPage++">
                  下一页
                </button>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧消息详情 -->
        <div class="lg:col-span-2">
          <card padding="lg" v-if="selectedConversation">
            <template #header>
              <div class="flex justify-between items-center">
                <div class="flex items-center space-x-3">
                  <img
                    v-if="selectedConversation.student.avatar"
                    :src="selectedConversation.student.avatar"
                    :alt="selectedConversation.student.name"
                    class="w-8 h-8 rounded-full"
                  />
                  <div v-else class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                    <user-icon class="w-4 h-4 text-gray-400" />
                  </div>
                  <div>
                    <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
                      {{ selectedConversation.student.name }}
                    </h2>
                    <p class="text-sm text-gray-500">{{ selectedConversation.student.studentId }}</p>
                  </div>
                </div>
                <div class="flex items-center space-x-2">
                  <button
                    variant="ghost"
                    size="sm"
                    @click="toggleImportant(selectedConversation.id)"
                    :class="selectedConversation.important ? 'text-yellow-500' : ''"
                  >
                    <star-icon class="w-4 h-4" />
                  </button>
                  <button variant="ghost" size="sm" @click="archiveConversation(selectedConversation.id)">
                    <archive-box-icon class="w-4 h-4" />
                  </button>
                  <button variant="ghost" size="sm" @click="deleteConversation(selectedConversation.id)">
                    <trash-icon class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </template>

            <!-- 消息列表 -->
            <div class="space-y-4 max-h-96 overflow-y-auto mb-4">
              <div
                v-for="message in selectedConversation.messages"
                :key="message.id"
                class="flex"
                :class="message.sender === 'teacher' ? 'justify-end' : 'justify-start'"
              >
                <div
                  class="max-w-xs lg:max-w-md px-4 py-2 rounded-lg"
                  :class="message.sender === 'teacher'
                    ? 'bg-primary-600 text-white'
                    : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white'"
                >
                  <p class="text-sm">{{ message.content }}</p>
                  <div class="flex items-center justify-between mt-2">
                    <span class="text-xs opacity-75">{{ formatDateTime(message.timestamp) }}</span>
                    <check-icon 
                      v-if="message.sender === 'teacher' && message.read"
                      class="w-3 h-3 opacity-75"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- 消息输入框 -->
            <div class="border-t border-gray-200 dark:border-gray-600 pt-4">
              <form @submit.prevent="sendMessage" class="flex space-x-3">
                <div class="flex-1">
                  <textarea
                    v-model="newMessage"
                    rows="3"
                    placeholder="输入消息..."
                    class="input resize-none"
                    @keydown.ctrl.enter="sendMessage"
                  ></textarea>
                </div>
                <div class="flex flex-col space-y-2">
                  <button
                    type="button"
                    variant="ghost"
                    size="sm"
                    @click="$refs.fileInput?.click()"
                  >
                    <paper-clip-icon class="w-4 h-4" />
                  </button>
                  <button
                    type="submit"
                    variant="primary"
                    size="sm"
                    :disabled="!newMessage.trim()"
                    :loading="isSending"
                  >
                    <paper-airplane-icon class="w-4 h-4" />
                  </button>
                </div>
              </form>
              <input ref="fileInput" type="file" class="hidden" @change="handleFileUpload" />
            </div>
          </card>

          <!-- 空状态 -->
          <card padding="lg" v-else>
            <div class="text-center py-12">
              <chat-bubble-left-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">选择一个对话</h3>
              <p class="text-gray-600 dark:text-gray-400 mb-4">选择左侧的对话开始查看消息</p>
              <button variant="primary" @click="$router.push('/teacher/messages/compose')">
                <plus-icon class="w-4 h-4 mr-2" />
                开始新对话
              </button>
            </div>
          </card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  CheckIcon,
  PencilSquareIcon,
  ArrowPathIcon,
  FunnelIcon,
  MagnifyingGlassIcon,
  UserIcon,
  StarIcon,
  PaperClipIcon,
  ArchiveBoxIcon,
  TrashIcon,
  ChatBubbleLeftIcon,
  PlusIcon,
  PaperAirplaneIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const router = useRouter()
const uiStore = useUIStore()

// 状态
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const showFilters = ref(false)
const selectedConversation = ref<any>(null)
const newMessage = ref('')
const isSending = ref(false)
const currentPage = ref(1)

// 统计数据
const stats = reactive({
  totalMessages: 89,
  unreadMessages: 12,
  todayMessages: 5,
  activeConversations: 23
})

// 对话数据
const conversations = ref([
  {
    id: '1',
    student: {
      id: '1',
      name: '张同学',
      studentId: '2021001001',
      avatar: ''
    },
    type: 'question',
    important: true,
    read: false,
    hasAttachment: false,
    archived: false,
    lastMessage: {
      id: '1',
      content: '老师，我对今天的作业有一些疑问，能请教一下吗？',
      preview: '老师，我对今天的作业有一些疑问...',
      sender: 'student',
      timestamp: '2024-01-15T10:30:00Z',
      read: false
    },
    messages: [
      {
        id: '1',
        content: '老师，我对今天的作业有一些疑问，能请教一下吗？',
        sender: 'student',
        timestamp: '2024-01-15T10:30:00Z',
        read: true
      },
      {
        id: '2',
        content: '当然可以，你有什么问题尽管问吧。',
        sender: 'teacher',
        timestamp: '2024-01-15T10:32:00Z',
        read: true
      },
      {
        id: '3',
        content: '第三题的解题思路我不太理解，您能详细解释一下吗？',
        sender: 'student',
        timestamp: '2024-01-15T10:35:00Z',
        read: false
      }
    ]
  },
  {
    id: '2',
    student: {
      id: '2',
      name: '李同学',
      studentId: '2021001002',
      avatar: ''
    },
    type: 'assignment',
    important: false,
    read: true,
    hasAttachment: true,
    archived: false,
    lastMessage: {
      id: '4',
      content: '作业已提交，请老师查看。',
      preview: '作业已提交，请老师查看。',
      sender: 'student',
      timestamp: '2024-01-14T16:20:00Z',
      read: true
    },
    messages: [
      {
        id: '4',
        content: '作业已提交，请老师查看。',
        sender: 'student',
        timestamp: '2024-01-14T16:20:00Z',
        read: true
      },
      {
        id: '5',
        content: '收到，我会尽快批改的。',
        sender: 'teacher',
        timestamp: '2024-01-14T16:25:00Z',
        read: true
      }
    ]
  },
  {
    id: '3',
    student: {
      id: '3',
      name: '王同学',
      studentId: '2021001003',
      avatar: ''
    },
    type: 'grade',
    important: false,
    read: false,
    hasAttachment: false,
    archived: false,
    lastMessage: {
      id: '6',
      content: '老师，我想了解一下期中考试的成绩分析。',
      preview: '老师，我想了解一下期中考试的成绩分析。',
      sender: 'student',
      timestamp: '2024-01-13T14:15:00Z',
      read: false
    },
    messages: [
      {
        id: '6',
        content: '老师，我想了解一下期中考试的成绩分析。',
        sender: 'student',
        timestamp: '2024-01-13T14:15:00Z',
        read: false
      }
    ]
  }
])

// 计算属性
const unreadCount = computed(() => {
  return conversations.value.filter(conv => !conv.read).length
})

const filteredConversations = computed(() => {
  return conversations.value.filter(conversation => {
    // 搜索筛选
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      if (!conversation.student.name.toLowerCase().includes(query) &&
          !conversation.lastMessage.content.toLowerCase().includes(query)) {
        return false
      }
    }

    // 状态筛选
    if (statusFilter.value) {
      switch (statusFilter.value) {
        case 'unread':
          if (conversation.read) return false
          break
        case 'read':
          if (!conversation.read) return false
          break
        case 'important':
          if (!conversation.important) return false
          break
        case 'archived':
          if (!conversation.archived) return false
          break
      }
    }

    // 类型筛选
    if (typeFilter.value && conversation.type !== typeFilter.value) {
      return false
    }

    return true
  })
})

const totalPages = computed(() => Math.ceil(filteredConversations.value.length / 10))

// 方法
const getTypeVariant = (type: string) => {
  switch (type) {
    case 'question': return 'primary'
    case 'assignment': return 'success'
    case 'grade': return 'warning'
    case 'general': return 'secondary'
    default: return 'secondary'
  }
}

const getTypeText = (type: string) => {
  switch (type) {
    case 'question': return '问题咨询'
    case 'assignment': return '作业相关'
    case 'grade': return '成绩相关'
    case 'general': return '一般交流'
    default: return '其他'
  }
}

const formatTime = (timestamp: string) => {
  const now = new Date()
  const time = new Date(timestamp)
  const diff = now.getTime() - time.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.toLocaleDateString('zh-CN')
}

const formatDateTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const selectConversation = (conversation: any) => {
  selectedConversation.value = conversation
  if (!conversation.read) {
    conversation.read = true
    stats.unreadMessages--
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedConversation.value) return

  isSending.value = true
  try {
    const message = {
      id: Date.now().toString(),
      content: newMessage.value,
      sender: 'teacher',
      timestamp: new Date().toISOString(),
      read: false
    }

    selectedConversation.value.messages.push(message)
    selectedConversation.value.lastMessage = {
      ...message,
      preview: message.content
    }

    newMessage.value = ''

    // 模拟发送延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    uiStore.showNotification({
      type: 'success',
      title: '发送成功',
      message: '消息已发送'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '发送失败',
      message: '发送消息时发生错误'
    })
  } finally {
    isSending.value = false
  }
}

const toggleImportant = (conversationId: string) => {
  const conversation = conversations.value.find(c => c.id === conversationId)
  if (conversation) {
    conversation.important = !conversation.important
    uiStore.showNotification({
      type: 'success',
      title: conversation.important ? '已标记为重要' : '已取消重要标记',
      message: ''
    })
  }
}

const archiveConversation = (conversationId: string) => {
  const conversation = conversations.value.find(c => c.id === conversationId)
  if (conversation) {
    conversation.archived = true
    selectedConversation.value = null
    uiStore.showNotification({
      type: 'success',
      title: '已归档',
      message: '对话已移至归档'
    })
  }
}

const deleteConversation = async (conversationId: string) => {
  if (confirm('确定要删除这个对话吗？')) {
    try {
      const index = conversations.value.findIndex(c => c.id === conversationId)
      if (index > -1) {
        conversations.value.splice(index, 1)
        selectedConversation.value = null
        
        uiStore.showNotification({
          type: 'success',
          title: '删除成功',
          message: '对话已删除'
        })
      }
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '删除失败',
        message: '删除对话时发生错误'
      })
    }
  }
}

const markAllAsRead = () => {
  conversations.value.forEach(conversation => {
    conversation.read = true
  })
  stats.unreadMessages = 0
  uiStore.showNotification({
    type: 'success',
    title: '操作成功',
    message: '所有消息已标记为已读'
  })
}

const refreshMessages = () => {
  uiStore.showNotification({
    type: 'info',
    title: '刷新完成',
    message: '消息列表已更新'
  })
}

const handleFileUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    uiStore.showNotification({
      type: 'info',
      title: '文件上传',
      message: '文件上传功能开发中...'
    })
  }
}

// 生命周期
onMounted(() => {
  // 自动选择第一个对话
  if (conversations.value.length > 0) {
    selectConversation(conversations.value[0])
  }
})
</script> 