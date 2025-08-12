<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <span>作业评分</span>
              <chevron-right-icon class="w-4 h-4" />
              <span>{{ assignment.title }}</span>
            </nav>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
              评分: {{ submission.studentName }}
            </h1>
            <p class="text-gray-600 dark:text-gray-400">
              提交时间: {{ formatDate(submission.submittedAt) }}
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <badge :variant="getSubmissionStatusVariant(submission.status)">
              {{ getSubmissionStatusText(submission.status) }}
            </badge>
            <Button variant="outline" @click="$router.go(-1)">
              <ArrowUturnLeftIcon class="w-4 h-4 mr-2" />
              返回列表
            </Button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        <p class="mt-2 text-gray-600 dark:text-gray-400">加载作业详情中...</p>
      </div>

      <div v-else class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <!-- 左侧主要内容 -->
        <div class="xl:col-span-2 space-y-8">
          <!-- 作业信息 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">作业要求</h2>
            </template>
            
            <div class="space-y-4">
              <div>
                <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ assignment.title }}</h3>
                <p class="text-gray-600 dark:text-gray-400">{{ assignment.description }}</p>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">截止时间：</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatDate(assignment.dueDate) }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">总分：</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ assignment.totalScore }} 分</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">提交状态：</span>
                  <span class="font-medium" :class="submission.isLate ? 'text-red-600' : 'text-green-600'">
                    {{ submission.isLate ? '迟交' : '按时提交' }}
                  </span>
                </div>
              </div>
            </div>
          </card>

          <!-- 学生提交内容 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学生提交内容</h2>
            </template>
            
            <div class="space-y-6">
              <!-- 文字内容 -->
              <div v-if="submission.content">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">作业答案</h3>
                <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                  <div class="prose max-w-none dark:prose-invert">
                    <p class="whitespace-pre-line">{{ submission.content }}</p>
                  </div>
                </div>
              </div>

              <!-- 附件（单文件） -->
              <div v-if="submission.fileName">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">附件文件</h3>
                <div class="flex items-center justify-between p-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 rounded-lg">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <document-icon class="w-6 h-6 text-gray-400" />
                    </div>
                    <div>
                      <p class="text-sm font-medium text-gray-900 dark:text-white">{{ submission.fileName }}</p>
                    </div>
                  </div>
                  <div class="flex items-center space-x-2">
                    <button variant="ghost" size="sm" @click="previewSingleFile">
                      <eye-icon class="w-4 h-4" />
                    </button>
                    <button variant="ghost" size="sm" @click="downloadSingleFile">
                      <arrow-down-tray-icon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>

              <!-- 学生自评 -->
              <div v-if="submission.selfEvaluation">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">学生自评</h3>
                <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
                  <p class="text-sm text-gray-700 dark:text-gray-300">{{ submission.selfEvaluation }}</p>
                </div>
              </div>
            </div>
          </card>

          <!-- AI评分建议 -->
          <card padding="lg" v-if="aiSuggestion">
            <template #header>
              <div class="flex items-center justify-between">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">AI评分建议</h2>
                <badge variant="secondary" class="flex items-center">
                  <sparkles-icon class="w-3 h-3 mr-1" />
                  AI分析
                </badge>
              </div>
            </template>
            
            <div class="space-y-4">
              <div class="flex items-center space-x-4">
                <div class="text-center">
                  <div class="text-2xl font-bold text-blue-600 dark:text-blue-400">
                    {{ aiSuggestion.suggestedScore }}
                  </div>
                  <p class="text-xs text-gray-500">建议分数</p>
                </div>
                <div class="flex-1">
                  <div class="grid grid-cols-3 gap-2 text-sm">
                    <div class="text-center">
                      <div class="font-medium text-gray-900 dark:text-white">{{ aiSuggestion.accuracy }}%</div>
                      <p class="text-gray-500">准确性</p>
                    </div>
                    <div class="text-center">
                      <div class="font-medium text-gray-900 dark:text-white">{{ aiSuggestion.completeness }}%</div>
                      <p class="text-gray-500">完整性</p>
                    </div>
                    <div class="text-center">
                      <div class="font-medium text-gray-900 dark:text-white">{{ aiSuggestion.clarity }}%</div>
                      <p class="text-gray-500">清晰度</p>
                    </div>
                  </div>
                </div>
                <button variant="outline" size="sm" @click="applyAiSuggestion">
                  采用建议
                </button>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">AI分析</h4>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ aiSuggestion.analysis }}</p>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">改进建议</h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                  <li v-for="suggestion in aiSuggestion.improvements" :key="suggestion" class="flex items-start">
                    <span class="text-blue-500 mr-2">•</span>
                    {{ suggestion }}
                  </li>
                </ul>
              </div>
            </div>
          </card>

          <!-- 评分历史 -->
          <card padding="lg" v-if="gradingHistory.length > 0">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">评分历史</h2>
            </template>
            
            <div class="space-y-4">
              <div v-for="history in gradingHistory" 
                   :key="history.id"
                   class="flex items-start space-x-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                    <academic-cap-icon class="w-4 h-4 text-blue-600 dark:text-blue-400" />
                  </div>
                </div>
                <div class="flex-1">
                  <div class="flex items-center space-x-3 mb-1">
                    <span class="font-medium text-gray-900 dark:text-white">{{ history.graderName }}</span>
                    <span class="text-sm text-gray-500">{{ formatDate(history.gradedAt) }}</span>
                    <badge variant="secondary" size="sm">{{ history.score }}分</badge>
                  </div>
                  <p class="text-sm text-gray-600 dark:text-gray-400">{{ history.feedback }}</p>
                </div>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧评分面板 -->
        <div class="space-y-6">
          <!-- 评分表单 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">评分</h3>
            </template>
            
            <form @submit.prevent="submitGrade" class="space-y-6">
              <!-- 分数输入 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  分数 <span class="text-red-500">*</span>
                </label>
                <div class="flex items-center space-x-2">
                  <input
                    v-model.number="gradeForm.score"
                    type="number"
                    :min="0"
                    :max="assignment.totalScore"
                    step="0.5"
                    class="input flex-1"
                    :class="{ 'border-red-500': errors.score }"
                    @blur="validateScore"
                  />
                  <span class="text-sm text-gray-500 dark:text-gray-400">/ {{ assignment.totalScore }}</span>
                </div>
                <p v-if="errors.score" class="mt-1 text-sm text-red-600">{{ errors.score }}</p>
                <div class="mt-2">
                  <progress :value="gradeForm.score" :max="assignment.totalScore" size="sm" color="primary" />
                </div>
              </div>

              <!-- 等级评定 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  等级评定
                </label>
                <select v-model="gradeForm.grade" class="input">
                  <option value="">选择等级</option>
                  <option value="A+">A+ (优秀+)</option>
                  <option value="A">A (优秀)</option>
                  <option value="B+">B+ (良好+)</option>
                  <option value="B">B (良好)</option>
                  <option value="C+">C+ (中等+)</option>
                  <option value="C">C (中等)</option>
                  <option value="D">D (及格)</option>
                  <option value="F">F (不及格)</option>
                </select>
              </div>

              <!-- 反馈内容 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  反馈意见 <span class="text-red-500">*</span>
                </label>
                <textarea
                  v-model="gradeForm.feedback"
                  rows="6"
                  placeholder="请提供详细的反馈意见，帮助学生改进..."
                  class="input"
                  :class="{ 'border-red-500': errors.feedback }"
                  @blur="validateFeedback"
                ></textarea>
                <p v-if="errors.feedback" class="mt-1 text-sm text-red-600">{{ errors.feedback }}</p>
              </div>

              <!-- 优点和改进点 -->
              <div class="grid grid-cols-1 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    优点
                  </label>
                  <textarea
                    v-model="gradeForm.strengths"
                    rows="3"
                    placeholder="指出学生的优点和亮点..."
                    class="input"
                  ></textarea>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    改进建议
                  </label>
                  <textarea
                    v-model="gradeForm.improvements"
                    rows="3"
                    placeholder="提供具体的改进建议..."
                    class="input"
                  ></textarea>
                </div>
              </div>

              <!-- 评分选项 -->
              <div class="space-y-3">
                <label class="flex items-center">
                  <input v-model="gradeForm.allowResubmit" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">允许重新提交</span>
                </label>
                <label class="flex items-center">
                  <input v-model="gradeForm.sendNotification" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">发送通知给学生</span>
                </label>
                <label class="flex items-center">
                  <input v-model="gradeForm.publishImmediately" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">立即发布成绩</span>
                </label>
              </div>

              <!-- 提交按钮 -->
              <div class="space-y-3">
                <Button
                  type="submit"
                  variant="success"
                  size="lg"
                  class="w-full"
                  :loading="isSubmitting"
                  :disabled="!isFormValid"
                >
                  <CheckIcon class="w-4 h-4 mr-2" />
                  提交评分
                </Button>
                
                <Button
                  type="button"
                  variant="outline"
                  size="lg"
                  class="w-full"
                  @click="saveDraft"
                  :loading="isDraftSaving"
                >
                  <DocumentDuplicateIcon class="w-4 h-4 mr-2" />
                  保存草稿
                </Button>
              </div>
            </form>
          </card>

          <!-- 学生信息 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学生信息</h3>
            </template>
            
            <div class="space-y-4">
              <div class="flex items-center space-x-3">
                <div class="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                  <user-icon class="w-6 h-6 text-blue-600 dark:text-blue-400" />
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-white">{{ submission.studentName }}</h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ submission.studentId }}</p>
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">课程：</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ assignment.courseName }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">班级：</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.className }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">平均分：</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.averageScore }}/100</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">排名：</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.rank }}/{{ submission.totalStudents }}</span>
                </div>
              </div>
              
              <button variant="outline" size="sm" class="w-full" @click="viewStudentProfile">
                查看学生档案
              </button>
            </div>
          </card>

          <!-- 快速操作 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快速操作</h3>
            </template>
            
            <div class="space-y-3">
              <button variant="outline" class="w-full justify-start" @click="contactStudent">
                <chat-bubble-left-icon class="w-4 h-4 mr-3" />
                联系学生
              </button>
              <button variant="outline" class="w-full justify-start" @click="viewOtherSubmissions">
                <document-text-icon class="w-4 h-4 mr-3" />
                查看其他提交
              </button>
              <button variant="outline" class="w-full justify-start" @click="exportSubmission">
                <arrow-down-tray-icon class="w-4 h-4 mr-3" />
                导出提交内容
              </button>
              <button variant="outline" class="w-full justify-start" @click="reportPlagiarism">
                <exclamation-triangle-icon class="w-4 h-4 mr-3" />
                举报抄袭
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
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import {
  ChevronRightIcon,
  DocumentIcon,
  EyeIcon,
  ArrowDownTrayIcon,
  ArrowUturnLeftIcon,
  SparklesIcon,
  AcademicCapIcon,
  CheckIcon,
  DocumentDuplicateIcon,
  UserIcon,
  ChatBubbleLeftIcon,
  DocumentTextIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const isLoading = ref(true)
const isSubmitting = ref(false)
const isDraftSaving = ref(false)

// 作业和提交数据
const assignment = reactive({
  id: String(route.params.assignmentId || ''),
  title: '',
  description: '',
  dueDate: '',
  totalScore: 100,
  courseName: ''
})

const submission = reactive<any>({
  id: String(route.params.submissionId || ''),
  studentId: '',
  studentName: '',
  className: '',
  submittedAt: '',
  isLate: false,
  status: '',
  content: '',
  fileName: '',
  filePath: ''
})

// AI评分建议
const aiSuggestion = ref<any | null>(null)

// 评分历史
const gradingHistory = ref<any[]>([])

// 评分表单
const gradeForm = reactive({
  score: 0,
  grade: '',
  feedback: '',
  strengths: '',
  improvements: '',
  allowResubmit: false,
  sendNotification: true,
  publishImmediately: true
})

// 错误状态
const errors = reactive({
  score: '',
  feedback: ''
})

// 计算属性
const isFormValid = computed(() => {
  return gradeForm.score >= 0 &&
    gradeForm.score <= assignment.totalScore &&
    gradeForm.feedback.trim() !== '' &&
    Object.values(errors).every(error => error === '')
})

// 方法
import { submissionApi } from '@/api/submission.api'
import { assignmentApi } from '@/api/assignment.api'
import { gradeApi } from '@/api/grade.api'
import { baseURL } from '@/api/config'

const loadSubmission = async () => {
  try {
    const submissionId = String(route.params.submissionId || '')
    const assignmentId = String(route.params.assignmentId || '')
    const sRes = await submissionApi.getSubmissionById(submissionId)
    const s = sRes.data
    Object.assign(submission, s)
    const aRes = await assignmentApi.getAssignmentById(assignmentId)
    const a: any = aRes.data as any
    assignment.title = a.title
    assignment.description = a.description
    assignment.dueDate = a.dueDate
    assignment.totalScore = Number((a.maxScore ?? a.totalScore ?? 100))
    assignment.courseName = a.courseName || ''
    if ((s as any)?.gradeId) {
      try {
        const hist = await gradeApi.getGradeHistory(String((s as any).gradeId))
        gradingHistory.value = hist.data || []
      } catch {}
    } else {
      // 兜底：通过提交ID查询一次成绩摘要，取到 grade_id 再查历史
      try {
        const sg = await submissionApi.getSubmissionGrade(submissionId)
        const gid = (sg as any)?.data?.grade_id || (sg as any)?.grade_id
        if (gid) {
          const hist = await gradeApi.getGradeHistory(String(gid))
          gradingHistory.value = hist.data || []
        }
      } catch {}
    }
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '加载失败',
      message: '无法加载作业提交数据'
    })
  } finally {
    isLoading.value = false
  }
}

const getSubmissionStatusVariant = (status: string) => {
  const variantMap: Record<string, string> = {
    submitted: 'success',
    graded: 'secondary',
    returned: 'warning',
    late: 'danger'
  }
  return variantMap[status] || 'secondary'
}

const getSubmissionStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    submitted: '已提交',
    graded: '已评分',
    returned: '已返回',
    late: '迟交'
  }
  return textMap[status] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileType = (filename: string) => {
  const extension = filename.split('.').pop()?.toLowerCase()
  const typeMap: Record<string, string> = {
    pdf: 'PDF文档',
    doc: 'Word文档',
    docx: 'Word文档',
    jpg: '图片',
    jpeg: '图片',
    png: '图片',
    txt: '文本文件'
  }
  return typeMap[extension || ''] || '未知类型'
}

const validateScore = () => {
  if (gradeForm.score < 0) {
    errors.score = '分数不能小于0'
  } else if (gradeForm.score > assignment.totalScore) {
    errors.score = `分数不能大于${assignment.totalScore}`
  } else {
    errors.score = ''
  }
}

const validateFeedback = () => {
  if (gradeForm.feedback.trim() === '') {
    errors.feedback = '请输入反馈意见'
  } else {
    errors.feedback = ''
  }
}

const applyAiSuggestion = () => {
  if (!aiSuggestion.value) return
  gradeForm.score = aiSuggestion.value.suggestedScore
  gradeForm.feedback = aiSuggestion.value.analysis
  gradeForm.improvements = (aiSuggestion.value.improvements || []).join('\n')
  
  uiStore.showNotification({
    type: 'success',
    title: 'AI建议已应用',
    message: '已将AI评分建议填入表单'
  })
}

const previewFile = (file: any) => {
  // 实现文件预览
  uiStore.showNotification({
    type: 'info',
    title: '文件预览',
    message: `正在预览文件: ${file.name}`
  })
}

const downloadFile = (file: any) => {
  // 实现文件下载
  uiStore.showNotification({
    type: 'success',
    title: '下载开始',
    message: `正在下载: ${file.name}`
  })
}

const previewSingleFile = () => {
  if (!submission.filePath) return
  // 后端有 /files/{fileId}/preview 或 download，此处 filePath 为存储路径，若后端返回 fileId 可替换为基于ID的URL
  // 简化：直接打开下载链接（若有预览接口可切换）
  const url = submission.filePath.startsWith('http') ? submission.filePath : `${baseURL}${submission.filePath}`
  window.open(url, '_blank')
}

const downloadSingleFile = () => {
  if (!submission.filePath) return
  const url = submission.filePath.startsWith('http') ? submission.filePath : `${baseURL}${submission.filePath}`
  const a = document.createElement('a')
  a.href = url
  a.download = submission.fileName || 'attachment'
  document.body.appendChild(a)
  a.click()
  a.remove()
}

const saveDraft = async () => {
  isDraftSaving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    uiStore.showNotification({
      type: 'success',
      title: '草稿已保存',
      message: '评分草稿已保存'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: '保存草稿时发生错误'
    })
  } finally {
    isDraftSaving.value = false
  }
}

const submitGrade = async () => {
  validateScore()
  validateFeedback()
  
  if (!isFormValid.value) {
    uiStore.showNotification({
      type: 'error',
      title: '表单验证失败',
      message: '请检查并填写必填字段'
    })
    return
  }

  isSubmitting.value = true
  try {
    const payload = {
      submissionId: submission.id,
      studentId: submission.studentId,
      score: gradeForm.score,
      feedback: gradeForm.feedback
    }
    const gr = await gradeApi.gradeSubmission(payload)
    const gradeId = gr?.data?.id
    if (gradeForm.publishImmediately && gradeId) {
      await gradeApi.publishGrade(String(gradeId))
    }
    
    uiStore.showNotification({
      type: 'success',
      title: '评分提交成功',
      message: gradeForm.publishImmediately ? '成绩已发布给学生' : '评分已保存，待发布'
    })
    
    // 跳转回作业列表
    router.push('/teacher/assignments')
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '提交失败',
      message: '提交评分时发生错误'
    })
  } finally {
    isSubmitting.value = false
  }
}

const viewStudentProfile = () => {
  router.push(`/teacher/students/${submission.studentId}`)
}

const contactStudent = () => {
  uiStore.showNotification({
    type: 'info',
    title: '联系学生',
    message: '消息功能开发中...'
  })
}

const viewOtherSubmissions = () => {
        router.push('/teacher/grading')
}

const exportSubmission = () => {
  uiStore.showNotification({
    type: 'success',
    title: '导出中...',
    message: '正在生成提交内容报告'
  })
}

const reportPlagiarism = () => {
  uiStore.showNotification({
    type: 'warning',
    title: '举报抄袭',
    message: '抄袭检测功能开发中...'
  })
}

// 生命周期
onMounted(() => {
  loadSubmission()
})
</script> 