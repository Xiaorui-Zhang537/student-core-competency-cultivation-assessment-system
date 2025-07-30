<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">作业评分</h1>
          <p class="text-gray-600 dark:text-gray-400">AI辅助评分，提高评分效率和一致性</p>
        </div>
        <div class="flex items-center space-x-4">
          <badge variant="warning" v-if="teacherStore.pendingGradingCount > 0">
            {{ teacherStore.pendingGradingCount }} 待评分
          </badge>
          <button variant="outline" @click="showFilters = !showFilters">
            <funnel-icon class="w-4 h-4 mr-2" />
            筛选
          </button>
          <button variant="primary" @click="showBatchGradingModal = true" :disabled="selectedTasks.length === 0">
            <clipboard-document-check-icon class="w-4 h-4 mr-2" />
            批量评分 ({{ selectedTasks.length }})
          </button>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <card v-if="showFilters" padding="lg" class="mb-6">
      <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">课程</label>
          <select v-model="gradingFilter.courseId" @change="onFilterChange" class="input w-full">
            <option value="">全部课程</option>
            <option v-for="course in teacherStore.courses" :key="course.courseId" :value="course.courseId">
              {{ course.courseName }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">状态</label>
          <select v-model="gradingFilter.status" @change="onFilterChange" class="input w-full">
            <option value="">全部状态</option>
            <option value="pending">待评分</option>
            <option value="in_progress">评分中</option>
            <option value="completed">已完成</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">优先级</label>
          <select v-model="gradingFilter.priority" @change="onFilterChange" class="input w-full">
            <option value="">全部优先级</option>
            <option value="high">高优先级</option>
            <option value="medium">中优先级</option>
            <option value="low">低优先级</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">截止时间</label>
          <select v-model="dueDateFilter" @change="onFilterChange" class="input w-full">
            <option value="">全部</option>
            <option value="overdue">已逾期</option>
            <option value="today">今天截止</option>
            <option value="week">本周截止</option>
          </select>
        </div>
        <div class="flex items-end">
          <button variant="outline" @click="clearFilters" class="w-full">
            清除筛选
          </button>
        </div>
      </div>
    </card>

    <!-- 统计信息 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-orange-600 dark:text-orange-400 mb-2">
          {{ pendingCount }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">待评分</p>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-2">
          {{ inProgressCount }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">评分中</p>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-2">
          {{ completedCount }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">已完成</p>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-red-600 dark:text-red-400 mb-2">
          {{ overdueCount }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">已逾期</p>
      </card>
    </div>

    <!-- 作业列表 -->
    <card padding="lg">
      <template #header>
        <div class="flex justify-between items-center">
          <div class="flex items-center space-x-4">
            <label class="flex items-center">
              <input 
                type="checkbox" 
                :checked="isAllSelected"
                @change="toggleSelectAll"
                class="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
              />
              <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">全选</span>
            </label>
            <span class="text-sm text-gray-500 dark:text-gray-400">
              已选择 {{ selectedTasks.length }} 项
            </span>
          </div>
          <div class="flex items-center space-x-2">
            <button variant="outline" size="sm" @click="refreshTasks">
              <arrow-path-icon class="w-4 h-4 mr-1" />
              刷新
            </button>
            <select v-model="sortBy" @change="sortTasks" class="input input-sm">
              <option value="dueDate">按截止时间</option>
              <option value="priority">按优先级</option>
              <option value="submittedAt">按提交时间</option>
              <option value="studentName">按学生姓名</option>
            </select>
          </div>
        </div>
      </template>
      
      <div class="space-y-4">
        <div v-for="task in displayedTasks" 
             :key="task.id"
             class="border border-gray-200 dark:border-gray-600 rounded-lg p-4 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
          <div class="flex items-center space-x-4">
            <!-- 选择框 -->
            <input 
              type="checkbox" 
              :value="task.id"
              v-model="selectedTasks"
              class="rounded border-gray-300 text-primary-600 shadow-sm focus:border-primary-300 focus:ring focus:ring-primary-200 focus:ring-opacity-50"
            />
            
            <!-- 作业信息 -->
            <div class="flex-1 grid grid-cols-1 md:grid-cols-5 gap-4">
              <div class="md:col-span-2">
                <h3 class="font-medium text-gray-900 dark:text-white">{{ task.assignmentTitle }}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ task.courseName }}</p>
                <div class="flex items-center space-x-2 mt-1">
                  <badge :variant="getPriorityColor(task.priority)" size="sm">
                    {{ getPriorityText(task.priority) }}
                  </badge>
                  <badge :variant="getStatusColor(task.status)" size="sm">
                    {{ getStatusText(task.status) }}
                  </badge>
                </div>
              </div>
              
              <div>
                <p class="text-sm font-medium text-gray-900 dark:text-white">{{ task.studentName }}</p>
                <p class="text-xs text-gray-500 dark:text-gray-400">
                  提交时间：{{ formatDateTime(task.submittedAt) }}
                </p>
              </div>
              
              <div>
                <p class="text-sm text-gray-600 dark:text-gray-400">截止时间</p>
                <p class="text-sm font-medium" :class="isOverdue(task.dueDate) ? 'text-red-600 dark:text-red-400' : 'text-gray-900 dark:text-white'">
                  {{ formatDateTime(task.dueDate) }}
                </p>
                <p v-if="isOverdue(task.dueDate)" class="text-xs text-red-600 dark:text-red-400">
                  逾期 {{ getOverdueDays(task.dueDate) }} 天
                </p>
              </div>
              
              <div class="flex items-center space-x-2">
                <div class="text-sm text-gray-600 dark:text-gray-400">
                  预计：{{ task.estimatedTime }}分钟
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex items-center space-x-2">
              <button 
                v-if="task.status === 'pending'"
                variant="outline" 
                size="sm" 
                @click="getAISuggestion(task)"
                :loading="loadingAI === task.id"
              >
                <sparkles-icon class="w-4 h-4 mr-1" />
                AI建议
              </button>
              
              <button 
                variant="primary" 
                size="sm" 
                @click="openGradingModal(task)"
              >
                <pencil-icon class="w-4 h-4 mr-1" />
                {{ task.status === 'completed' ? '查看' : '评分' }}
              </button>
            </div>
          </div>
          
          <!-- AI建议 -->
          <div v-if="task.aiSuggestions && showAISuggestion === task.id" 
               class="mt-4 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
            <div class="flex items-start space-x-3">
              <sparkles-icon class="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
              <div class="flex-1">
                <h4 class="text-sm font-medium text-blue-900 dark:text-blue-100 mb-2">AI评分建议</h4>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                  <div>
                    <span class="text-blue-700 dark:text-blue-300">建议分数：</span>
                    <span class="font-medium text-blue-900 dark:text-blue-100">
                      {{ task.aiSuggestions.suggestedScore }} 分
                    </span>
                    <span class="text-xs text-blue-600 dark:text-blue-400 ml-1">
                      (置信度: {{ task.aiSuggestions.confidence }}%)
                    </span>
                  </div>
                  <div>
                    <span class="text-blue-700 dark:text-blue-300">优势：</span>
                    <span class="text-blue-900 dark:text-blue-100">
                      {{ task.aiSuggestions.strengths.join(', ') }}
                    </span>
                  </div>
                  <div>
                    <span class="text-blue-700 dark:text-blue-300">改进点：</span>
                    <span class="text-blue-900 dark:text-blue-100">
                      {{ task.aiSuggestions.improvements.join(', ') }}
                    </span>
                  </div>
                </div>
                <div class="mt-3">
                  <span class="text-blue-700 dark:text-blue-300">AI反馈：</span>
                  <p class="text-blue-900 dark:text-blue-100 mt-1">{{ task.aiSuggestions.feedback }}</p>
                </div>
                <div class="mt-3 flex space-x-2">
                  <button variant="primary" size="sm" @click="acceptAISuggestion(task)">
                    采用建议
                  </button>
                  <button variant="outline" size="sm" @click="showAISuggestion = ''">
                    关闭
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="displayedTasks.length === 0" class="text-center py-12">
          <clipboard-document-check-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无待评分作业</h3>
          <p class="text-gray-600 dark:text-gray-400">所有作业都已评分完成</p>
        </div>
      </div>
    </card>

    <!-- 详细评分弹窗 -->
    <div v-if="showGradingModal && currentTask" 
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
         @click.self="showGradingModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-4xl mx-4 max-h-[90vh] overflow-hidden flex flex-col">
        <div class="p-6 border-b border-gray-200 dark:border-gray-600">
          <div class="flex justify-between items-center">
            <div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ currentTask.assignmentTitle }}</h3>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                {{ currentTask.studentName }} · {{ currentTask.courseName }}
              </p>
            </div>
            <button variant="outline" size="sm" @click="showGradingModal = false">
              <x-mark-icon class="w-4 h-4" />
            </button>
          </div>
        </div>
        
        <div class="flex-1 overflow-hidden flex">
          <!-- 作业内容 -->
          <div class="flex-1 p-6 overflow-y-auto">
            <div class="space-y-6">
              <div>
                <h4 class="text-md font-medium text-gray-900 dark:text-white mb-3">作业内容</h4>
                <div class="prose dark:prose-invert max-w-none">
                  <p class="whitespace-pre-wrap">{{ currentTask.content || '这里是学生提交的作业内容...' }}</p>
                </div>
              </div>
              
              <div v-if="currentTask.files && currentTask.files.length > 0">
                <h4 class="text-md font-medium text-gray-900 dark:text-white mb-3">附件文件</h4>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div v-for="(file, index) in currentTask.files" 
                       :key="index"
                       class="flex items-center space-x-3 p-3 border border-gray-200 dark:border-gray-600 rounded-lg">
                    <document-text-icon class="w-6 h-6 text-gray-500" />
                    <div class="flex-1 min-w-0">
                      <p class="font-medium text-gray-900 dark:text-white truncate">{{ file.name }}</p>
                      <p class="text-sm text-gray-500 dark:text-gray-400">{{ formatFileSize(file.size) }}</p>
                    </div>
                    <button variant="outline" size="sm">
                      <eye-icon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 评分面板 -->
          <div class="w-80 border-l border-gray-200 dark:border-gray-600 p-6 bg-gray-50 dark:bg-gray-700">
            <form @submit.prevent="submitGrade" class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  分数 <span class="text-red-500">*</span>
                </label>
                <div class="flex items-center space-x-2">
                  <input 
                    v-model.number="gradeForm.score" 
                    type="number" 
                    min="0" 
                    max="100"
                    class="input flex-1"
                    required
                  />
                  <span class="text-sm text-gray-500 dark:text-gray-400">/ 100</span>
                </div>
                <div class="mt-2">
                  <progress :value="gradeForm.score" :max="100" color="primary" />
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  评语 <span class="text-red-500">*</span>
                </label>
                <textarea 
                  v-model="gradeForm.feedback" 
                  class="input w-full h-32 resize-none"
                  placeholder="请输入详细的评语..."
                  required
                ></textarea>
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  {{ gradeForm.feedback.length }} / 500 字符
                </p>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  优势表现
                </label>
                <div class="space-y-2">
                  <div v-for="(strength, index) in gradeForm.strengths" :key="index" class="flex items-center space-x-2">
                    <input 
                      v-model="gradeForm.strengths[index]" 
                      type="text" 
                      class="input flex-1 text-sm"
                      placeholder="输入优势点"
                    />
                    <button type="button" variant="outline" size="sm" @click="removeStrength(index)">
                      <x-mark-icon class="w-3 h-3" />
                    </button>
                  </div>
                  <button type="button" variant="outline" size="sm" @click="addStrength" class="w-full">
                    <plus-icon class="w-4 h-4 mr-1" />
                    添加优势点
                  </button>
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  改进建议
                </label>
                <div class="space-y-2">
                  <div v-for="(improvement, index) in gradeForm.improvements" :key="index" class="flex items-center space-x-2">
                    <input 
                      v-model="gradeForm.improvements[index]" 
                      type="text" 
                      class="input flex-1 text-sm"
                      placeholder="输入改进建议"
                    />
                    <button type="button" variant="outline" size="sm" @click="removeImprovement(index)">
                      <x-mark-icon class="w-3 h-3" />
                    </button>
                  </div>
                  <button type="button" variant="outline" size="sm" @click="addImprovement" class="w-full">
                    <plus-icon class="w-4 h-4 mr-1" />
                    添加改进建议
                  </button>
                </div>
              </div>
              
              <!-- 快速评语模板 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  快速评语
                </label>
                <div class="grid grid-cols-1 gap-2">
                  <button 
                    v-for="template in feedbackTemplates" 
                    :key="template.id"
                    type="button"
                    variant="outline" 
                    size="sm" 
                    @click="applyTemplate(template)"
                    class="text-left justify-start"
                  >
                    {{ template.text }}
                  </button>
                </div>
              </div>
              
              <div class="flex space-x-3 pt-4">
                <button type="button" variant="outline" @click="showGradingModal = false" class="flex-1">
                  取消
                </button>
                <button type="submit" variant="primary" :loading="submittingGrade" class="flex-1">
                  提交评分
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- 批量评分弹窗 -->
    <div v-if="showBatchGradingModal" 
         class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
         @click.self="showBatchGradingModal = false">
      <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-lg mx-4">
        <div class="p-6 border-b border-gray-200 dark:border-gray-600">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">批量评分</h3>
          <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
            对选中的 {{ selectedTasks.length }} 项作业进行批量评分
          </p>
        </div>
        
        <div class="p-6">
          <form @submit.prevent="submitBatchGrade" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                统一分数
              </label>
              <input 
                v-model.number="batchGradeForm.score" 
                type="number" 
                min="0" 
                max="100"
                class="input w-full"
                placeholder="输入分数"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                统一评语
              </label>
              <textarea 
                v-model="batchGradeForm.feedback" 
                class="input w-full h-24 resize-none"
                placeholder="输入评语..."
              ></textarea>
            </div>
            
            <div class="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4">
              <div class="flex items-start space-x-2">
                <exclamation-triangle-icon class="w-5 h-5 text-yellow-600 dark:text-yellow-400 flex-shrink-0 mt-0.5" />
                <div class="text-sm">
                  <p class="text-yellow-800 dark:text-yellow-300 font-medium">批量评分提醒</p>
                  <p class="text-yellow-700 dark:text-yellow-400 mt-1">
                    此操作将对所有选中的作业应用相同的分数和评语。请确认这些作业适合使用统一标准评分。
                  </p>
                </div>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button type="button" variant="outline" @click="showBatchGradingModal = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="submittingBatchGrade">
                确认批量评分
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTeacherStore } from '@/stores/teacher'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import type { GradingTask } from '@/types/teacher'
import {
  FunnelIcon,
  ClipboardDocumentCheckIcon,
  ArrowPathIcon,
  SparklesIcon,
  PencilIcon,
  XMarkIcon,
  EyeIcon,
  PlusIcon,
  ExclamationTriangleIcon,
  DocumentTextIcon
} from '@heroicons/vue/24/outline'

// Stores
const teacherStore = useTeacherStore()
const uiStore = useUIStore()

// 状态
const showFilters = ref(false)
const showGradingModal = ref(false)
const showBatchGradingModal = ref(false)
const showAISuggestion = ref('')
const loadingAI = ref('')
const submittingGrade = ref(false)
const submittingBatchGrade = ref(false)
const selectedTasks = ref<string[]>([])
const currentTask = ref<GradingTask | null>(null)
const sortBy = ref('dueDate')
const dueDateFilter = ref('')

// 表单数据
const gradeForm = ref({
  score: 0,
  feedback: '',
  strengths: [''],
  improvements: ['']
})

const batchGradeForm = ref({
  score: 0,
  feedback: ''
})

// 快速评语模板
const feedbackTemplates = [
  { id: 1, text: '作业完成质量很高，思路清晰' },
  { id: 2, text: '基本要求已达到，但还有提升空间' },
  { id: 3, text: '创意不错，但执行上需要加强' },
  { id: 4, text: '答案正确，但解题过程不够详细' },
  { id: 5, text: '努力程度值得肯定，继续加油' }
]

// 筛选状态
const gradingFilter = computed(() => teacherStore.gradingFilter)

// 计算属性
const displayedTasks = computed(() => {
  let filtered = teacherStore.filteredGradingTasks
  
  if (dueDateFilter.value === 'overdue') {
    filtered = filtered.filter(task => isOverdue(task.dueDate))
  } else if (dueDateFilter.value === 'today') {
    const today = new Date().toISOString().split('T')[0]
    filtered = filtered.filter(task => task.dueDate.startsWith(today))
  } else if (dueDateFilter.value === 'week') {
    const weekFromNow = new Date()
    weekFromNow.setDate(weekFromNow.getDate() + 7)
    filtered = filtered.filter(task => new Date(task.dueDate) <= weekFromNow)
  }
  
  return filtered
})

const pendingCount = computed(() => 
  teacherStore.gradingTasks.filter(task => task.status === 'pending').length
)

const inProgressCount = computed(() => 
  teacherStore.gradingTasks.filter(task => task.status === 'in_progress').length
)

const completedCount = computed(() => 
  teacherStore.gradingTasks.filter(task => task.status === 'completed').length
)

const overdueCount = computed(() => 
  teacherStore.gradingTasks.filter(task => isOverdue(task.dueDate) && task.status !== 'completed').length
)

const isAllSelected = computed(() => 
  displayedTasks.value.length > 0 && selectedTasks.value.length === displayedTasks.value.length
)

// 方法
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

const isOverdue = (dueDate: string) => {
  return new Date() > new Date(dueDate)
}

const getOverdueDays = (dueDate: string) => {
  const now = new Date()
  const due = new Date(dueDate)
  return Math.floor((now.getTime() - due.getTime()) / (1000 * 60 * 60 * 24))
}

const getPriorityColor = (priority: string) => {
  switch (priority) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    default: return 'secondary'
  }
}

const getPriorityText = (priority: string) => {
  switch (priority) {
    case 'high': return '高优先级'
    case 'medium': return '中优先级'
    default: return '低优先级'
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'completed': return 'success'
    case 'in_progress': return 'primary'
    default: return 'warning'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'in_progress': return '评分中'
    default: return '待评分'
  }
}

const onFilterChange = () => {
  teacherStore.setGradingFilter(gradingFilter.value)
}

const clearFilters = () => {
  teacherStore.clearFilters()
  dueDateFilter.value = ''
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedTasks.value = []
  } else {
    selectedTasks.value = displayedTasks.value.map(task => task.id)
  }
}

const sortTasks = () => {
  // 排序逻辑由store处理
}

const refreshTasks = async () => {
  await teacherStore.fetchGradingTasks()
}

const getAISuggestion = async (task: GradingTask) => {
  loadingAI.value = task.id
  try {
    // 模拟AI API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟AI建议结果
    task.aiSuggestions = {
      suggestedScore: Math.floor(Math.random() * 30) + 70, // 70-100分
      confidence: Math.floor(Math.random() * 20) + 80, // 80-100%
      strengths: ['逻辑清晰', '内容完整'],
      improvements: ['可以增加更多细节', '格式需要规范'],
      feedback: '作业整体完成较好，思路清晰，但在某些细节方面还有提升空间。'
    }
    
    showAISuggestion.value = task.id
    
    uiStore.showNotification({
      type: 'success',
      title: 'AI分析完成',
      message: '已生成评分建议'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: 'AI分析失败',
      message: '无法获取AI建议，请重试'
    })
  } finally {
    loadingAI.value = ''
  }
}

const acceptAISuggestion = (task: GradingTask) => {
  if (task.aiSuggestions) {
    gradeForm.value.score = task.aiSuggestions.suggestedScore
    gradeForm.value.feedback = task.aiSuggestions.feedback
    gradeForm.value.strengths = [...task.aiSuggestions.strengths, '']
    gradeForm.value.improvements = [...task.aiSuggestions.improvements, '']
    
    openGradingModal(task)
    showAISuggestion.value = ''
  }
}

const openGradingModal = (task: GradingTask) => {
  currentTask.value = task
  
  // 如果是查看已评分的作业，加载现有评分
  if (task.status === 'completed') {
    // 这里应该从API加载现有评分数据
    gradeForm.value = {
      score: 85, // 示例数据
      feedback: '作业完成质量不错...',
      strengths: ['逻辑清晰', ''],
      improvements: ['可以增加更多细节', '']
    }
  } else {
    // 重置表单
    gradeForm.value = {
      score: 0,
      feedback: '',
      strengths: [''],
      improvements: ['']
    }
  }
  
  showGradingModal.value = true
}

const addStrength = () => {
  gradeForm.value.strengths.push('')
}

const removeStrength = (index: number) => {
  gradeForm.value.strengths.splice(index, 1)
}

const addImprovement = () => {
  gradeForm.value.improvements.push('')
}

const removeImprovement = (index: number) => {
  gradeForm.value.improvements.splice(index, 1)
}

const applyTemplate = (template: any) => {
  gradeForm.value.feedback = template.text
}

const submitGrade = async () => {
  if (!currentTask.value) return
  
  submittingGrade.value = true
  try {
    await teacherStore.submitGrade(currentTask.value.id, {
      score: gradeForm.value.score,
      feedback: gradeForm.value.feedback,
      strengths: gradeForm.value.strengths.filter(s => s.trim()),
      improvements: gradeForm.value.improvements.filter(i => i.trim())
    })
    
    showGradingModal.value = false
    selectedTasks.value = selectedTasks.value.filter(id => id !== currentTask.value!.id)
    
    uiStore.showNotification({
      type: 'success',
      title: '评分成功',
      message: '作业评分已提交'
    })
  } catch (error) {
    console.error('评分失败:', error)
  } finally {
    submittingGrade.value = false
  }
}

const submitBatchGrade = async () => {
  if (selectedTasks.value.length === 0) return
  
  submittingBatchGrade.value = true
  try {
    const grades = selectedTasks.value.map(taskId => ({
      taskId,
      score: batchGradeForm.value.score,
      feedback: batchGradeForm.value.feedback
    }))
    
    // 调用批量评分API
    await new Promise(resolve => setTimeout(resolve, 2000)) // 模拟API调用
    
    selectedTasks.value = []
    showBatchGradingModal.value = false
    
    // 重置表单
    batchGradeForm.value = {
      score: 0,
      feedback: ''
    }
    
    await teacherStore.fetchGradingTasks()
    
    uiStore.showNotification({
      type: 'success',
      title: '批量评分成功',
      message: `已成功评分 ${grades.length} 份作业`
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '批量评分失败',
      message: '批量评分操作失败，请重试'
    })
  } finally {
    submittingBatchGrade.value = false
  }
}

// 生命周期
onMounted(async () => {
  await teacherStore.fetchGradingTasks()
})
</script> 