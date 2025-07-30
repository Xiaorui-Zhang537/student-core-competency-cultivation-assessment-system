<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center space-x-4 mb-4">
        <button 
          variant="outline" 
          size="sm" 
          @click="$router.back()"
          class="flex items-center"
        >
          <arrow-left-icon class="w-4 h-4 mr-2" />
          返回
        </button>
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">作业提交</h1>
          <p class="text-gray-600 dark:text-gray-400 mt-1">
            {{ assignment ? assignment.title : '加载中...' }}
          </p>
        </div>
      </div>
      
      <!-- 作业信息 -->
      <card v-if="assignment" padding="lg" class="mb-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">作业要求</h3>
            <p class="text-gray-900 dark:text-white">{{ assignment.description }}</p>
          </div>
          <div>
            <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">截止时间</h3>
            <p class="text-gray-900 dark:text-white flex items-center">
              <clock-icon class="w-4 h-4 mr-2" />
              {{ formatDateTime(assignment.dueDate) }}
            </p>
            <p v-if="isOverdue" class="text-red-600 dark:text-red-400 text-sm mt-1">
              已逾期 {{ getOverdueDays() }} 天
            </p>
            <p v-else class="text-green-600 dark:text-green-400 text-sm mt-1">
              还剩 {{ getRemainingDays() }} 天
            </p>
          </div>
          <div>
            <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400 mb-2">提交状态</h3>
            <badge :variant="getSubmissionStatusColor()" size="lg">
              {{ getSubmissionStatusText() }}
            </badge>
          </div>
        </div>
      </card>
    </div>

    <!-- 主要内容 -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
      <!-- 提交表单 -->
      <div class="xl:col-span-2 space-y-6">
        <!-- 文本内容 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">作业内容</h3>
          </template>
          
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                作业标题
              </label>
              <input 
                v-model="submissionForm.title" 
                type="text" 
                class="input w-full"
                placeholder="请输入作业标题"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                作业正文 <span class="text-red-500">*</span>
              </label>
              <textarea 
                v-model="submissionForm.content"
                class="input w-full h-48 resize-none"
                placeholder="请输入作业内容..."
                required
              ></textarea>
              <p class="text-sm text-gray-500 dark:text-gray-400 mt-2">
                字数：{{ submissionForm.content.length }} / 最少 100 字
              </p>
            </div>
          </div>
        </card>

        <!-- 文件上传 -->
        <card padding="lg">
          <template #header>
            <div class="flex justify-between items-center">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">附件上传</h3>
              <badge variant="secondary" size="sm">
                {{ uploadedFiles.length }}/{{ maxFileCount }} 个文件
              </badge>
            </div>
          </template>
          
          <!-- 拖拽上传区域 -->
          <div 
            class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-8 text-center hover:border-primary-500 dark:hover:border-primary-400 transition-colors"
            :class="{ 'border-primary-500 bg-primary-50 dark:bg-primary-900/20': isDragging }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="handleFileDrop"
          >
            <cloud-arrow-up-icon class="mx-auto h-12 w-12 text-gray-400 dark:text-gray-500 mb-4" />
            <p class="text-lg font-medium text-gray-900 dark:text-white mb-2">
              拖拽文件到此处或
              <button 
                @click="$refs.fileInput.click()"
                class="text-primary-600 dark:text-primary-400 hover:text-primary-500 underline"
              >
                点击选择文件
              </button>
            </p>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              支持 PDF、DOC、DOCX、PPT、PPTX、XLS、XLSX、TXT、图片等格式，单个文件不超过 10MB
            </p>
            
            <input 
              ref="fileInput"
              type="file" 
              multiple 
              class="hidden"
              :accept="allowedFileTypes.join(',')"
              @change="handleFileSelect"
            />
          </div>
          
          <!-- 文件列表 -->
          <div v-if="uploadedFiles.length > 0" class="mt-6 space-y-3">
            <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300">已上传的文件</h4>
            <div 
              v-for="(file, index) in uploadedFiles" 
              :key="index"
              class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
            >
              <div class="flex items-center space-x-3">
                <component :is="getFileIcon(file.type)" class="w-8 h-8 text-gray-500" />
                <div>
                  <p class="font-medium text-gray-900 dark:text-white">{{ file.name }}</p>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    {{ formatFileSize(file.size) }} · {{ getFileTypeName(file.type) }}
                  </p>
                </div>
              </div>
              <div class="flex items-center space-x-2">
                <div v-if="file.uploading" class="flex items-center space-x-2">
                  <div class="w-4 h-4 animate-spin rounded-full border-2 border-primary-500 border-t-transparent"></div>
                  <span class="text-sm text-gray-500">{{ file.progress }}%</span>
                </div>
                <badge v-else-if="file.uploaded" variant="success" size="sm">已上传</badge>
                <button 
                  variant="outline" 
                  size="sm" 
                  @click="removeFile(index)"
                  class="text-red-600 hover:text-red-800"
                >
                  <trash-icon class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </card>

        <!-- 提交设置 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">提交设置</h3>
          </template>
          
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                  允许教师查看
                </label>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  教师可以查看您的作业内容和附件
                </p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input 
                  v-model="submissionForm.allowTeacherView" 
                  type="checkbox" 
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
              </label>
            </div>
            
            <div class="flex items-center justify-between">
              <div>
                <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                  允许同学查看
                </label>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  其他同学可以查看您的作业（仅在教师允许的情况下）
                </p>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input 
                  v-model="submissionForm.allowPeerView" 
                  type="checkbox" 
                  class="sr-only peer"
                >
                <div class="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 dark:peer-focus:ring-primary-800 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-primary-600"></div>
              </label>
            </div>
          </div>
        </card>

        <!-- 操作按钮 -->
        <div class="flex justify-between items-center">
          <div class="flex space-x-4">
            <button 
              variant="outline" 
              @click="saveDraft"
              :loading="savingDraft"
              :disabled="!canSaveDraft"
            >
              <document-icon class="w-4 h-4 mr-2" />
              保存草稿
            </button>
            <button 
              variant="outline" 
              @click="previewSubmission"
              :disabled="!canPreview"
            >
              <eye-icon class="w-4 h-4 mr-2" />
              预览
            </button>
          </div>
          
          <button 
            variant="primary" 
            size="lg"
            @click="showSubmitConfirm = true"
            :disabled="!canSubmit"
            :loading="submitting"
          >
            <paper-airplane-icon class="w-4 h-4 mr-2" />
            提交作业
          </button>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="space-y-6">
        <!-- 作业评分标准 -->
        <card v-if="assignment?.rubric" padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">评分标准</h3>
          </template>
          
          <div class="space-y-4">
            <div 
              v-for="criterion in assignment.rubric.criteria" 
              :key="criterion.id"
              class="border-l-4 border-primary-500 pl-4"
            >
              <h4 class="font-medium text-gray-900 dark:text-white">{{ criterion.name }}</h4>
              <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ criterion.description }}</p>
              <p class="text-sm font-medium text-primary-600 dark:text-primary-400 mt-2">
                权重：{{ criterion.weight }}% ({{ criterion.maxPoints }} 分)
              </p>
            </div>
          </div>
        </card>

        <!-- 提交历史 -->
        <card v-if="submissionHistory.length > 0" padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">提交历史</h3>
          </template>
          
          <div class="space-y-3">
            <div 
              v-for="(submission, index) in submissionHistory" 
              :key="submission.id"
              class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
            >
              <div class="flex justify-between items-start mb-2">
                <badge :variant="getSubmissionStatusColor(submission.status)" size="sm">
                  {{ getSubmissionStatusText(submission.status) }}
                </badge>
                <span class="text-xs text-gray-500 dark:text-gray-400">
                  版本 {{ submissionHistory.length - index }}
                </span>
              </div>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                {{ formatDateTime(submission.submittedAt) }}
              </p>
              <div v-if="submission.grade" class="mt-2">
                <p class="text-sm font-medium text-gray-900 dark:text-white">
                  分数：{{ submission.grade.score }}/{{ submission.grade.totalScore }}
                </p>
              </div>
            </div>
          </div>
        </card>

        <!-- 帮助提示 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white flex items-center">
              <question-mark-circle-icon class="w-5 h-5 mr-2 text-primary-500" />
              提交提示
            </h3>
          </template>
          
          <div class="space-y-3 text-sm text-gray-600 dark:text-gray-400">
            <div class="flex items-start space-x-2">
              <check-circle-icon class="w-4 h-4 text-green-500 mt-0.5 flex-shrink-0" />
              <p>确保作业内容完整，至少包含 100 字的文字说明</p>
            </div>
            <div class="flex items-start space-x-2">
              <check-circle-icon class="w-4 h-4 text-green-500 mt-0.5 flex-shrink-0" />
              <p>检查附件文件格式和大小符合要求</p>
            </div>
            <div class="flex items-start space-x-2">
              <check-circle-icon class="w-4 h-4 text-green-500 mt-0.5 flex-shrink-0" />
              <p>提交前建议保存草稿，避免意外丢失</p>
            </div>
            <div class="flex items-start space-x-2">
              <check-circle-icon class="w-4 h-4 text-green-500 mt-0.5 flex-shrink-0" />
              <p>提交后可以查看提交状态和老师反馈</p>
            </div>
          </div>
        </card>
      </div>
    </div>

    <!-- 提交确认弹窗 -->
    <div 
      v-if="showSubmitConfirm"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showSubmitConfirm = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">确认提交作业</h3>
        
        <div class="space-y-4 mb-6">
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">作业标题：</span>
            <span class="font-medium text-gray-900 dark:text-white">{{ submissionForm.title }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">正文字数：</span>
            <span class="font-medium text-gray-900 dark:text-white">{{ submissionForm.content.length }} 字</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600 dark:text-gray-400">附件数量：</span>
            <span class="font-medium text-gray-900 dark:text-white">{{ uploadedFiles.length }} 个</span>
          </div>
        </div>
        
        <div class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4 mb-6">
          <div class="flex items-start space-x-2">
            <exclamation-triangle-icon class="w-5 h-5 text-yellow-600 dark:text-yellow-400 flex-shrink-0 mt-0.5" />
            <div class="text-sm">
              <p class="text-yellow-800 dark:text-yellow-300 font-medium mb-1">提交须知</p>
              <p class="text-yellow-700 dark:text-yellow-400">
                提交后{{ assignment?.allowResubmit ? '可以重新提交，但会显示提交历史' : '不能修改，请确认内容无误' }}
              </p>
            </div>
          </div>
        </div>
        
        <div class="flex justify-end space-x-3">
          <button variant="outline" @click="showSubmitConfirm = false">
            取消
          </button>
          <button variant="primary" @click="submitAssignment" :loading="submitting">
            确认提交
          </button>
        </div>
      </div>
    </div>

    <!-- 预览弹窗 -->
    <div 
      v-if="showPreview"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showPreview = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-4xl mx-4 max-h-[90vh] overflow-hidden flex flex-col">
        <div class="p-6 border-b border-gray-200 dark:border-gray-600 flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">作业预览</h3>
          <button variant="outline" size="sm" @click="showPreview = false">
            <x-mark-icon class="w-4 h-4" />
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto flex-1">
          <div class="space-y-6">
            <div>
              <h4 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">{{ submissionForm.title }}</h4>
              <div class="prose dark:prose-invert max-w-none">
                <p class="whitespace-pre-wrap">{{ submissionForm.content }}</p>
              </div>
            </div>
            
            <div v-if="uploadedFiles.length > 0">
              <h4 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">附件文件</h4>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div 
                  v-for="(file, index) in uploadedFiles" 
                  :key="index"
                  class="flex items-center space-x-3 p-3 border border-gray-200 dark:border-gray-600 rounded-lg"
                >
                  <component :is="getFileIcon(file.type)" class="w-6 h-6 text-gray-500" />
                  <div class="flex-1 min-w-0">
                    <p class="font-medium text-gray-900 dark:text-white truncate">{{ file.name }}</p>
                    <p class="text-sm text-gray-500 dark:text-gray-400">{{ formatFileSize(file.size) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAssignmentsStore } from '@/stores/assignments'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import type { Assignment, AssignmentSubmission } from '@/types/assignment'
import {
  ArrowLeftIcon,
  ClockIcon,
  CloudArrowUpIcon,
  DocumentIcon,
  EyeIcon,
  PaperAirplaneIcon,
  QuestionMarkCircleIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  XMarkIcon,
  TrashIcon,
  DocumentTextIcon,
  PhotoIcon,
  FilmIcon,
  SpeakerWaveIcon,
  ArchiveBoxIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const route = useRoute()
const router = useRouter()
const assignmentStore = useAssignmentsStore()
const uiStore = useUIStore()

// 上传文件类型定义
interface UploadFile {
  name: string
  size: number
  type: string
  file: File
  uploading: boolean
  uploaded: boolean
  progress: number
  url: string
}

// 状态
const assignment = ref<Assignment | null>(null)
const submissionHistory = ref<AssignmentSubmission[]>([])
const isDragging = ref(false)
const uploadedFiles = ref<UploadFile[]>([])
const showSubmitConfirm = ref(false)
const showPreview = ref(false)
const submitting = ref(false)
const savingDraft = ref(false)

// 表单数据
const submissionForm = ref({
  title: '',
  content: '',
  allowTeacherView: true,
  allowPeerView: false
})

// 文件上传配置
const maxFileCount = 5
const maxFileSize = 10 * 1024 * 1024 // 10MB
const allowedFileTypes = [
  '.pdf', '.doc', '.docx', '.ppt', '.pptx', '.xls', '.xlsx', '.txt',
  '.jpg', '.jpeg', '.png', '.gif', '.svg',
  '.mp4', '.avi', '.mov', '.wmv',
  '.mp3', '.wav', '.m4a',
  '.zip', '.rar', '.7z'
]

// 计算属性
const isOverdue = computed(() => {
  if (!assignment.value) return false
  return new Date() > new Date(assignment.value.dueDate)
})

const canSaveDraft = computed(() => {
  return submissionForm.value.title.trim() || submissionForm.value.content.trim()
})

const canPreview = computed(() => {
  return submissionForm.value.title.trim() && submissionForm.value.content.trim()
})

const canSubmit = computed(() => {
  return submissionForm.value.title.trim() && 
         submissionForm.value.content.length >= 100 &&
         uploadedFiles.value.every(file => file.uploaded)
})

// 方法
const getRemainingDays = () => {
  if (!assignment.value) return 0
  const diff = new Date(assignment.value.dueDate).getTime() - new Date().getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

const getOverdueDays = () => {
  if (!assignment.value) return 0
  const diff = new Date().getTime() - new Date(assignment.value.dueDate).getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

const getSubmissionStatusColor = (status?: string) => {
  const currentStatus = status || (submissionHistory.value.length > 0 ? submissionHistory.value[0].status : 'not_submitted')
  
  switch (currentStatus) {
    case 'submitted': return 'success'
    case 'graded': return 'primary'
    case 'draft': return 'warning'
    case 'late': return 'danger'
    default: return 'secondary'
  }
}

const getSubmissionStatusText = (status?: string) => {
  const currentStatus = status || (submissionHistory.value.length > 0 ? submissionHistory.value[0].status : 'not_submitted')
  
  switch (currentStatus) {
    case 'submitted': return '已提交'
    case 'graded': return '已评分'
    case 'draft': return '草稿'
    case 'late': return '逾期提交'
    default: return '未提交'
  }
}

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const formatFileSize = (bytes: number) => {
  const sizes = ['B', 'KB', 'MB', 'GB']
  if (bytes === 0) return '0 B'
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return Math.round(bytes / Math.pow(1024, i) * 100) / 100 + ' ' + sizes[i]
}

const getFileIcon = (fileType: string) => {
  if (fileType.startsWith('image/')) return PhotoIcon
  if (fileType.startsWith('video/')) return FilmIcon
  if (fileType.startsWith('audio/')) return SpeakerWaveIcon
  if (fileType.includes('pdf') || fileType.includes('document')) return DocumentTextIcon
  return ArchiveBoxIcon
}

const getFileTypeName = (fileType: string) => {
  if (fileType.startsWith('image/')) return '图片'
  if (fileType.startsWith('video/')) return '视频'
  if (fileType.startsWith('audio/')) return '音频'
  if (fileType.includes('pdf')) return 'PDF'
  if (fileType.includes('document') || fileType.includes('word')) return 'Word文档'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'Excel表格'
  if (fileType.includes('presentation') || fileType.includes('powerpoint')) return 'PPT演示'
  if (fileType.includes('text')) return '文本文件'
  if (fileType.includes('zip') || fileType.includes('rar')) return '压缩文件'
  return '其他文件'
}

const handleFileSelect = (event: Event) => {
  const files = (event.target as HTMLInputElement).files
  if (files) {
    processFiles(Array.from(files))
  }
}

const handleFileDrop = (event: DragEvent) => {
  isDragging.value = false
  const files = event.dataTransfer?.files
  if (files) {
    processFiles(Array.from(files))
  }
}

const processFiles = (files: File[]) => {
  for (const file of files) {
    if (uploadedFiles.value.length >= maxFileCount) {
      uiStore.showNotification({
        type: 'warning',
        title: '文件数量限制',
        message: `最多只能上传 ${maxFileCount} 个文件`
      })
      break
    }
    
    if (file.size > maxFileSize) {
      uiStore.showNotification({
        type: 'error',
        title: '文件过大',
        message: `文件 ${file.name} 超过 10MB 限制`
      })
      continue
    }
    
    const fileObj = {
      name: file.name,
      size: file.size,
      type: file.type,
      file: file,
      uploading: false,
      uploaded: false,
      progress: 0,
      url: ''
    }
    
    uploadedFiles.value.push(fileObj)
    uploadFile(fileObj)
  }
}

const uploadFile = async (fileObj: UploadFile) => {
  fileObj.uploading = true
  
  try {
    // 模拟文件上传
    for (let i = 0; i <= 100; i += 10) {
      fileObj.progress = i
      await new Promise(resolve => setTimeout(resolve, 100))
    }
    
    fileObj.uploading = false
    fileObj.uploaded = true
    fileObj.url = `https://example.com/uploads/${fileObj.name}`
    
    uiStore.showNotification({
      type: 'success',
      title: '上传成功',
      message: `文件 ${fileObj.name} 上传完成`
    })
  } catch (error) {
    fileObj.uploading = false
    uiStore.showNotification({
      type: 'error',
      title: '上传失败',
      message: `文件 ${fileObj.name} 上传失败`
    })
  }
}

const removeFile = (index: number) => {
  uploadedFiles.value.splice(index, 1)
}

const saveDraft = async () => {
  if (!canSaveDraft.value) return
  
  savingDraft.value = true
  try {
         const content = JSON.stringify({
       title: submissionForm.value.title,
       content: submissionForm.value.content,
       files: uploadedFiles.value.filter((f: UploadFile) => f.uploaded).map((f: UploadFile) => ({
         name: f.name,
         url: f.url,
         size: f.size,
         type: f.type
       })),
       settings: {
         allowTeacherView: submissionForm.value.allowTeacherView,
         allowPeerView: submissionForm.value.allowPeerView
       }
     })
     
     await assignmentStore.saveDraft(route.params.id as string, content)
    
    uiStore.showNotification({
      type: 'success',
      title: '保存成功',
      message: '草稿已保存'
    })
  } catch (error) {
    console.error('保存草稿失败:', error)
  } finally {
    savingDraft.value = false
  }
}

const previewSubmission = () => {
  if (!canPreview.value) return
  showPreview.value = true
}

const submitAssignment = async () => {
  if (!canSubmit.value) return
  
  submitting.value = true
  try {
    const submissionData = {
      assignmentId: route.params.id as string,
      title: submissionForm.value.title,
      content: submissionForm.value.content,
             files: uploadedFiles.value.filter((f: UploadFile) => f.uploaded).map((f: UploadFile) => ({
         name: f.name,
         url: f.url,
         size: f.size,
         type: f.type
       })),
       settings: {
         allowTeacherView: submissionForm.value.allowTeacherView,
         allowPeerView: submissionForm.value.allowPeerView
       },
       submittedAt: new Date().toISOString()
     }
     
     const formData = new FormData()
     formData.append('data', JSON.stringify(submissionData))
     
     await assignmentStore.submitAssignment(route.params.id as string, formData)
    
    uiStore.showNotification({
      type: 'success',
      title: '提交成功',
      message: '作业已成功提交'
    })
    
    showSubmitConfirm.value = false
    
    // 跳转到作业列表或详情页
          router.push('/student/grades')
  } catch (error) {
    console.error('提交作业失败:', error)
  } finally {
    submitting.value = false
  }
}

// 加载数据
onMounted(async () => {
  const assignmentId = route.params.id as string
  
  try {
    // 加载作业信息
    await assignmentStore.fetchAssignment(assignmentId)
    assignment.value = assignmentStore.currentAssignment
    
    // 加载提交历史
    await assignmentStore.fetchMySubmissions()
    submissionHistory.value = assignmentStore.mySubmissions.filter((s: AssignmentSubmission) => s.assignmentId === assignmentId)
    
    // 如果有草稿，加载草稿内容
    const draft = submissionHistory.value.find((s: AssignmentSubmission) => s.status === 'draft')
    if (draft) {
      submissionForm.value.title = draft.title || ''
      submissionForm.value.content = draft.content || ''
      // 加载草稿的文件和设置
    }
  } catch (error) {
    console.error('加载作业信息失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '加载失败',
      message: '无法加载作业信息'
    })
  }
})
</script>

<style scoped lang="postcss">
.prose {
  @apply text-gray-700 dark:text-gray-300;
}

.prose p {
  @apply mb-4;
}
</style> 