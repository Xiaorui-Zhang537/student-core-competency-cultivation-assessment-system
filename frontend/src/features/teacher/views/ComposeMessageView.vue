<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <router-link to="/teacher/messages" class="hover:text-gray-700 dark:hover:text-gray-200">
                消息中心
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <span>撰写消息</span>
            </nav>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white">撰写消息</h1>
            <p class="text-gray-600 dark:text-gray-400 mt-2">发送消息给学生</p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="saveDraft" :loading="isSavingDraft">
              <document-text-icon class="w-4 h-4 mr-2" />
              保存草稿
            </button>
            <button variant="outline" @click="$router.go(-1)">
              取消
            </button>
          </div>
        </div>
      </div>

      <!-- 撰写表单 -->
      <card padding="lg">
        <form @submit.prevent="sendMessage" class="space-y-6">
          <!-- 收件人选择 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              收件人 <span class="text-red-500">*</span>
            </label>
            <div class="space-y-3">
              <!-- 收件人类型选择 -->
              <div class="flex space-x-4">
                <label class="flex items-center">
                  <input
                    v-model="recipientType"
                    type="radio"
                    value="individual"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">个人</span>
                </label>
                <label class="flex items-center">
                  <input
                    v-model="recipientType"
                    type="radio"
                    value="group"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">群组</span>
                </label>
                <label class="flex items-center">
                  <input
                    v-model="recipientType"
                    type="radio"
                    value="all"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">全部学生</span>
                </label>
              </div>

              <!-- 个人收件人选择 -->
              <div v-if="recipientType === 'individual'" class="space-y-3">
                <div class="relative">
                  <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    v-model="studentSearchQuery"
                    type="text"
                    placeholder="搜索学生姓名或学号..."
                    class="input pl-10"
                    @input="filterStudents"
                  />
                </div>
                
                <!-- 已选择的学生 -->
                <div v-if="selectedStudents.length > 0" class="flex flex-wrap gap-2">
                  <badge
                    v-for="student in selectedStudents"
                    :key="student.id"
                    variant="primary"
                    class="flex items-center"
                  >
                    {{ student.name }}
                    <button
                      type="button"
                      @click="removeStudent(student.id)"
                      class="ml-2 text-primary-300 hover:text-white"
                    >
                      <x-mark-icon class="w-3 h-3" />
                    </button>
                  </badge>
                </div>

                <!-- 学生列表 -->
                <div v-if="studentSearchQuery && filteredStudents.length > 0" 
                     class="max-h-48 overflow-y-auto border border-gray-200 dark:border-gray-600 rounded-lg">
                  <div
                    v-for="student in filteredStudents"
                    :key="student.id"
                    @click="addStudent(student)"
                    class="p-3 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer flex items-center space-x-3"
                  >
                    <img
                      v-if="student.avatar"
                      :src="student.avatar"
                      :alt="student.name"
                      class="w-8 h-8 rounded-full"
                    />
                    <div v-else class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                      <user-icon class="w-4 h-4 text-gray-400" />
                    </div>
                    <div>
                      <p class="text-sm font-medium text-gray-900 dark:text-white">{{ student.name }}</p>
                      <p class="text-xs text-gray-500">{{ student.studentId }} • {{ student.className }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 群组选择 -->
              <div v-else-if="recipientType === 'group'">
                <select v-model="selectedGroup" class="input">
                  <option value="">选择班级或群组</option>
                  <option v-for="group in groups" :key="group.id" :value="group.id">
                    {{ group.name }} ({{ group.studentCount }} 名学生)
                  </option>
                </select>
              </div>

              <!-- 全部学生提示 -->
              <div v-else-if="recipientType === 'all'" class="p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                <p class="text-sm text-yellow-800 dark:text-yellow-200">
                  将发送给所有学生 (共 {{ totalStudentCount }} 名)
                </p>
              </div>
            </div>
          </div>

          <!-- 消息主题 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              主题 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="messageForm.subject"
              type="text"
              placeholder="输入消息主题..."
              class="input"
              :class="{ 'border-red-500': errors.subject }"
            />
            <p v-if="errors.subject" class="mt-1 text-sm text-red-600">{{ errors.subject }}</p>
          </div>

          <!-- 消息类型 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              消息类型
            </label>
            <select v-model="messageForm.type" class="input">
              <option value="general">一般消息</option>
              <option value="assignment">作业通知</option>
              <option value="announcement">重要公告</option>
              <option value="reminder">提醒</option>
              <option value="grade">成绩反馈</option>
            </select>
          </div>

          <!-- 消息内容 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              消息内容 <span class="text-red-500">*</span>
            </label>
            <div class="border border-gray-300 dark:border-gray-600 rounded-lg">
              <!-- 格式工具栏 -->
              <div class="flex items-center space-x-2 p-3 border-b border-gray-200 dark:border-gray-600">
                <button
                  type="button"
                  variant="ghost"
                  size="sm"
                  @click="formatText('bold')"
                  class="text-gray-600 hover:text-gray-900"
                >
                  <span class="font-bold">B</span>
                </button>
                <button
                  type="button"
                  variant="ghost"
                  size="sm"
                  @click="formatText('italic')"
                  class="text-gray-600 hover:text-gray-900"
                >
                  <span class="italic">I</span>
                </button>
                <button
                  type="button"
                  variant="ghost"
                  size="sm"
                  @click="formatText('underline')"
                  class="text-gray-600 hover:text-gray-900"
                >
                  <span class="underline">U</span>
                </button>
                <div class="w-px h-4 bg-gray-300"></div>
                <button
                  type="button"
                  variant="ghost"
                  size="sm"
                  @click="insertTemplate"
                >
                  <document-text-icon class="w-4 h-4" />
                </button>
              </div>
              
              <!-- 内容编辑区 -->
              <textarea
                v-model="messageForm.content"
                ref="contentTextarea"
                rows="8"
                placeholder="输入消息内容..."
                class="w-full p-3 border-0 rounded-b-lg resize-none focus:ring-0 focus:border-transparent bg-transparent text-gray-900 dark:text-white"
                :class="{ 'border-red-500': errors.content }"
              ></textarea>
            </div>
            <p v-if="errors.content" class="mt-1 text-sm text-red-600">{{ errors.content }}</p>
            <div class="mt-2 flex justify-between text-sm text-gray-500">
              <span>支持 Ctrl+Enter 快捷发送</span>
              <span>{{ messageForm.content.length }}/2000 字符</span>
            </div>
          </div>

          <!-- 附件上传 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              附件
            </label>
            <div
              @drop.prevent="handleFileDrop"
              @dragover.prevent
              @dragenter.prevent
              class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-6 text-center cursor-pointer hover:border-primary-500 transition-colors"
              :class="{ 'border-primary-500': isDragOver }"
              @click="$refs.fileInput?.click()"
            >
              <cloud-arrow-up-icon class="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <p class="text-sm text-gray-600 dark:text-gray-400">
                点击上传或拖拽文件到此处
              </p>
              <p class="text-xs text-gray-500 mt-1">
                支持图片、文档、视频等格式，单个文件最大 50MB
              </p>
            </div>
            <input
              ref="fileInput"
              type="file"
              multiple
              class="hidden"
              @change="handleFileUpload"
            />

            <!-- 附件列表 -->
            <div v-if="messageForm.attachments.length > 0" class="mt-4 space-y-2">
              <div
                v-for="(file, index) in messageForm.attachments"
                :key="index"
                class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
              >
                <div class="flex items-center space-x-3">
                  <div class="flex-shrink-0">
                    <component :is="getFileIcon(file.type)" class="w-6 h-6 text-gray-500" />
                  </div>
                  <div>
                    <p class="text-sm font-medium text-gray-900 dark:text-white">{{ file.name }}</p>
                    <p class="text-xs text-gray-500">{{ formatFileSize(file.size) }}</p>
                  </div>
                </div>
                <button
                  type="button"
                  variant="ghost"
                  size="sm"
                  @click="removeAttachment(index)"
                  class="text-red-600 hover:text-red-800"
                >
                  <x-mark-icon class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <!-- 发送选项 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 优先级 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                优先级
              </label>
              <select v-model="messageForm.priority" class="input">
                <option value="normal">普通</option>
                <option value="high">高</option>
                <option value="urgent">紧急</option>
              </select>
            </div>

            <!-- 阅读回执 -->
            <div class="flex items-center space-x-3">
              <label class="flex items-center">
                <input
                  v-model="messageForm.requestReadReceipt"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">请求阅读回执</span>
              </label>
            </div>
          </div>

          <!-- 定时发送 -->
          <div>
            <label class="flex items-center mb-3">
              <input
                v-model="messageForm.scheduled"
                type="checkbox"
                class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
              />
              <span class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">定时发送</span>
            </label>
            
            <div v-if="messageForm.scheduled" class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">发送日期</label>
                <input
                  v-model="messageForm.scheduledDate"
                  type="date"
                  class="input"
                  :min="today"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">发送时间</label>
                <input
                  v-model="messageForm.scheduledTime"
                  type="time"
                  class="input"
                />
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center justify-between pt-6 border-t border-gray-200 dark:border-gray-600">
            <div class="flex items-center space-x-3">
              <button
                type="button"
                variant="outline"
                @click="previewMessage"
              >
                <eye-icon class="w-4 h-4 mr-2" />
                预览
              </button>
              <button
                type="button"
                variant="outline"
                @click="saveTemplate"
              >
                <bookmark-icon class="w-4 h-4 mr-2" />
                保存为模板
              </button>
            </div>
            
            <div class="flex items-center space-x-3">
              <button
                type="button"
                variant="outline"
                @click="$router.go(-1)"
              >
                取消
              </button>
              <button
                type="submit"
                variant="primary"
                :loading="isSending"
                :disabled="!isFormValid"
              >
                <paper-airplane-icon class="w-4 h-4 mr-2" />
                {{ messageForm.scheduled ? '定时发送' : '立即发送' }}
              </button>
            </div>
          </div>
        </form>
      </card>

      <!-- 消息模板选择弹窗 -->
      <div v-if="showTemplateModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">选择消息模板</h3>
          <div class="space-y-3">
            <div
              v-for="template in messageTemplates"
              :key="template.id"
              @click="useTemplate(template)"
              class="p-3 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <h4 class="font-medium text-gray-900 dark:text-white">{{ template.name }}</h4>
              <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ template.preview }}</p>
            </div>
          </div>
          <div class="mt-6 flex justify-end">
            <button variant="outline" @click="showTemplateModal = false">
              关闭
            </button>
          </div>
        </div>
      </div>

      <!-- 消息预览弹窗 -->
      <div v-if="showPreviewModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">消息预览</h3>
          <div class="space-y-4">
            <div>
              <span class="text-sm text-gray-500">收件人：</span>
              <span class="text-sm text-gray-900 dark:text-white">{{ getRecipientText() }}</span>
            </div>
            <div>
              <span class="text-sm text-gray-500">主题：</span>
              <span class="text-sm text-gray-900 dark:text-white">{{ messageForm.subject }}</span>
            </div>
            <div>
              <span class="text-sm text-gray-500">内容：</span>
              <div class="mt-2 p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <p class="text-sm text-gray-900 dark:text-white whitespace-pre-wrap">{{ messageForm.content }}</p>
              </div>
            </div>
            <div v-if="messageForm.attachments.length > 0">
              <span class="text-sm text-gray-500">附件：</span>
              <div class="mt-2 space-y-1">
                <div v-for="file in messageForm.attachments" :key="file.name" class="text-sm text-gray-900 dark:text-white">
                  {{ file.name }} ({{ formatFileSize(file.size) }})
                </div>
              </div>
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button variant="outline" @click="showPreviewModal = false">
              关闭
            </button>
            <button variant="primary" @click="sendMessage">
              确认发送
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  ChevronRightIcon,
  DocumentTextIcon,
  MagnifyingGlassIcon,
  XMarkIcon,
  UserIcon,
  CloudArrowUpIcon,
  PaperAirplaneIcon,
  EyeIcon,
  BookmarkIcon,
  DocumentIcon,
  PhotoIcon,
  FilmIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const recipientType = ref('individual')
const studentSearchQuery = ref('')
const selectedStudents = ref<any[]>([])
const selectedGroup = ref('')
const isSending = ref(false)
const isSavingDraft = ref(false)
const isDragOver = ref(false)
const showTemplateModal = ref(false)
const showPreviewModal = ref(false)
const contentTextarea = ref<HTMLTextAreaElement>()

// 表单数据
const messageForm = reactive({
  subject: '',
  type: 'general',
  content: '',
  attachments: [] as File[],
  priority: 'normal',
  requestReadReceipt: false,
  scheduled: false,
  scheduledDate: '',
  scheduledTime: ''
})

// 错误状态
const errors = reactive({
  subject: '',
  content: ''
})

// 今日日期
const today = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// 模拟数据
const students = ref([
  { id: '1', name: '张同学', studentId: '2021001001', className: '计算机1班', avatar: '' },
  { id: '2', name: '李同学', studentId: '2021001002', className: '计算机1班', avatar: '' },
  { id: '3', name: '王同学', studentId: '2021001003', className: '计算机2班', avatar: '' },
  { id: '4', name: '刘同学', studentId: '2021001004', className: '计算机2班', avatar: '' },
  { id: '5', name: '陈同学', studentId: '2021001005', className: '计算机1班', avatar: '' }
])

const groups = ref([
  { id: '1', name: '计算机1班', studentCount: 35 },
  { id: '2', name: '计算机2班', studentCount: 32 },
  { id: '3', name: '软件工程1班', studentCount: 38 },
  { id: '4', name: '网络工程1班', studentCount: 30 }
])

const messageTemplates = ref([
  {
    id: '1',
    name: '作业提醒',
    preview: '请同学们按时完成作业...',
    content: '亲爱的同学们：\n\n请注意按时完成本周的作业，截止时间为{deadline}。如有疑问，请及时联系我。\n\n祝学习进步！\n老师'
  },
  {
    id: '2',
    name: '考试通知',
    preview: '期中考试安排通知...',
    content: '各位同学：\n\n期中考试将于{date}举行，考试时间：{time}，考试地点：{location}。请同学们做好复习准备。\n\n考试注意事项：\n1. 请携带学生证和身份证\n2. 提前15分钟到达考场\n3. 不得携带电子设备\n\n祝考试顺利！\n老师'
  },
  {
    id: '3',
    name: '成绩反馈',
    preview: '本次作业成绩反馈...',
    content: '同学你好：\n\n你的{assignment}已经批改完成，成绩为{score}分。\n\n{feedback}\n\n希望你继续努力！\n老师'
  }
])

const totalStudentCount = 156

// 计算属性
const filteredStudents = ref<any[]>([])

const isFormValid = computed(() => {
  const hasRecipients = recipientType.value === 'all' || 
                       (recipientType.value === 'group' && selectedGroup.value) ||
                       (recipientType.value === 'individual' && selectedStudents.value.length > 0)
  
  return hasRecipients && 
         messageForm.subject.trim() !== '' && 
         messageForm.content.trim() !== ''
})

// 方法
const filterStudents = () => {
  if (!studentSearchQuery.value.trim()) {
    filteredStudents.value = []
    return
  }

  const query = studentSearchQuery.value.toLowerCase()
  filteredStudents.value = students.value.filter(student => 
    !selectedStudents.value.find(s => s.id === student.id) &&
    (student.name.toLowerCase().includes(query) || 
     student.studentId.toLowerCase().includes(query))
  ).slice(0, 10)
}

const addStudent = (student: any) => {
  if (!selectedStudents.value.find(s => s.id === student.id)) {
    selectedStudents.value.push(student)
    studentSearchQuery.value = ''
    filteredStudents.value = []
  }
}

const removeStudent = (studentId: string) => {
  const index = selectedStudents.value.findIndex(s => s.id === studentId)
  if (index > -1) {
    selectedStudents.value.splice(index, 1)
  }
}

const formatText = (type: string) => {
  // 简单的文本格式化功能
  uiStore.showNotification({
    type: 'info',
    title: '格式化',
    message: '文本格式化功能开发中...'
  })
}

const insertTemplate = () => {
  showTemplateModal.value = true
}

const useTemplate = (template: any) => {
  messageForm.content = template.content
  showTemplateModal.value = false
  uiStore.showNotification({
    type: 'success',
    title: '模板已应用',
    message: `已使用模板: ${template.name}`
  })
}

const handleFileUpload = (event: Event) => {
  const files = Array.from((event.target as HTMLInputElement).files || [])
  addFiles(files)
}

const handleFileDrop = (event: DragEvent) => {
  isDragOver.value = false
  const files = Array.from(event.dataTransfer?.files || [])
  addFiles(files)
}

const addFiles = (files: File[]) => {
  for (const file of files) {
    if (file.size > 50 * 1024 * 1024) {
      uiStore.showNotification({
        type: 'error',
        title: '文件过大',
        message: `文件 ${file.name} 超过 50MB 限制`
      })
      continue
    }
    
    if (!messageForm.attachments.find(f => f.name === file.name)) {
      messageForm.attachments.push(file)
    }
  }
}

const removeAttachment = (index: number) => {
  messageForm.attachments.splice(index, 1)
}

const getFileIcon = (fileType: string) => {
  if (fileType.startsWith('image/')) return PhotoIcon
  if (fileType.startsWith('video/')) return FilmIcon
  return DocumentIcon
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getRecipientText = () => {
  if (recipientType.value === 'all') {
    return `全部学生 (${totalStudentCount} 名)`
  } else if (recipientType.value === 'group') {
    const group = groups.value.find(g => g.id === selectedGroup.value)
    return group ? `${group.name} (${group.studentCount} 名学生)` : ''
  } else {
    return selectedStudents.value.map(s => s.name).join(', ')
  }
}

const validateForm = () => {
  errors.subject = messageForm.subject.trim() ? '' : '请输入消息主题'
  errors.content = messageForm.content.trim() ? '' : '请输入消息内容'
  
  return !errors.subject && !errors.content
}

const previewMessage = () => {
  if (!validateForm()) return
  showPreviewModal.value = true
}

const saveDraft = async () => {
  isSavingDraft.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    uiStore.showNotification({
      type: 'success',
      title: '草稿已保存',
      message: '消息已保存为草稿'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: '保存草稿时发生错误'
    })
  } finally {
    isSavingDraft.value = false
  }
}

const saveTemplate = () => {
  if (!messageForm.subject || !messageForm.content) {
    uiStore.showNotification({
      type: 'warning',
      title: '内容不完整',
      message: '请先填写主题和内容'
    })
    return
  }

  uiStore.showNotification({
    type: 'success',
    title: '模板已保存',
    message: '消息已保存为模板'
  })
}

const sendMessage = async () => {
  showPreviewModal.value = false
  
  if (!validateForm()) return

  isSending.value = true
  try {
    // 模拟发送延迟
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    uiStore.showNotification({
      type: 'success',
      title: messageForm.scheduled ? '定时发送已设置' : '发送成功',
      message: messageForm.scheduled ? '消息将在指定时间发送' : '消息已成功发送'
    })
    
    router.push('/teacher/messages')
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

// 生命周期
onMounted(() => {
  // 检查URL参数是否有预设收件人
  const toParam = route.query.to as string
  if (toParam) {
    const student = students.value.find(s => s.id === toParam)
    if (student) {
      selectedStudents.value = [student]
    }
  }
  
  // 设置默认定时发送时间
  const now = new Date()
  now.setHours(now.getHours() + 1)
  messageForm.scheduledDate = now.toISOString().split('T')[0]
  messageForm.scheduledTime = now.toTimeString().slice(0, 5)
})
</script> 