<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/courses')">{{ t('teacher.courses.breadcrumb') }}</span>
              <ChevronRightIcon class="w-4 h-4" />
              <span v-if="assignment.courseName || assignmentCourseId" class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="goCourse()">{{ assignment.courseName || `#${assignmentCourseId}` }}</span>
              <ChevronRightIcon class="w-4 h-4" />
              <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/assignments')">{{ t('teacher.submissions.breadcrumb.assignments') }}</span>
              <ChevronRightIcon class="w-4 h-4" />
              <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push(`/teacher/assignments/${assignment.id}/submissions`)">{{ t('teacher.submissions.breadcrumb.self') }}</span>
              <ChevronRightIcon class="w-4 h-4" />
              <span>{{ submission.studentName || submission.studentId }}</span>
            </nav>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
              {{ t('teacher.grading.title', { name: submission.studentName }) }}
            </h1>
            <p class="text-gray-600 dark:text-gray-400">
              {{ t('teacher.submissions.submittedAt', { time: formatDate(submission.submittedAt) }) }}
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <badge :variant="getSubmissionStatusVariant(submission.status)">
              {{ getSubmissionStatusText(submission.status) }}
            </badge>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        <p class="mt-2 text-gray-600 dark:text-gray-400">{{ t('teacher.grading.loading') }}</p>
      </div>

      <div v-else class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <!-- 左侧主要内容 -->
        <div class="xl:col-span-2 space-y-8">
          <!-- 作业信息 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.assignment.requirements') }}</h2>
            </template>
            
            <div class="space-y-4">
              <div>
                <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ assignment.title }}</h3>
                <p class="text-gray-600 dark:text-gray-400">{{ assignment.description }}</p>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.assignment.due') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatDate(assignment.dueDate) }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.assignment.totalScore') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ assignment.totalScore }}{{ t('teacher.grading.history.scoreSuffix') }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.assignment.submitStatus') }}</span>
                  <span class="font-medium" :class="submission.isLate ? 'text-red-600' : 'text-green-600'">
                    {{ submission.isLate ? t('teacher.grading.assignment.late') : t('teacher.grading.assignment.onTime') }}
                  </span>
                </div>
              </div>
            </div>
          </card>

          <!-- 学生提交内容 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.submission.content') }}</h2>
            </template>
            
            <div class="space-y-6">
              <!-- 文字内容 -->
              <div v-if="submission.content">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.submission.answer') }}</h3>
                <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
                  <div class="prose max-w-none dark:prose-invert">
                    <p class="whitespace-pre-line">{{ submission.content }}</p>
                  </div>
                </div>
              </div>

              <!-- 附件（单文件） -->
              <div v-if="submission.fileName">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">{{ t('teacher.grading.submission.attachment') }}</h3>
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
                    <Button variant="ghost" size="sm" @click="previewSingleFile">
                      <eye-icon class="w-4 h-4" />
                    </Button>
                    <Button variant="ghost" size="sm" @click="downloadSingleFile">
                      <arrow-down-tray-icon class="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </div>

              <!-- 学生自评 -->
              <div v-if="submission.selfEvaluation">
              <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.submission.selfEvaluation') }}</h3>
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
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.ai.title') }}</h2>
                <badge variant="secondary" class="flex items-center">
                  <sparkles-icon class="w-3 h-3 mr-1" />
                  {{ t('teacher.grading.ai.badge') }}
                </badge>
              </div>
            </template>
            
            <div class="space-y-4">
              <div class="flex items-center space-x-4">
                <div class="text-center">
                  <div class="text-2xl font-bold text-blue-600 dark:text-blue-400">
                    {{ aiSuggestion.suggestedScore }}
                  </div>
                  <p class="text-xs text-gray-500">{{ t('teacher.grading.ai.suggestedScore') }}</p>
                </div>
                <div class="flex-1">
                  <div class="grid grid-cols-3 gap-2 text-sm">
                    <div class="text-center">
                      <div class="font-medium text-gray-900 dark:text-white">{{ aiSuggestion.accuracy }}%</div>
                      <p class="text-gray-500">{{ t('teacher.grading.ai.accuracy') }}</p>
                    </div>
                    <div class="text-center">
                      <div class="font-medium text-gray-900 dark:text-white">{{ aiSuggestion.completeness }}%</div>
                      <p class="text-gray-500">{{ t('teacher.grading.ai.completeness') }}</p>
                    </div>
                    <div class="text-center">
                      <div class="font-medium text-gray-900 dark:text-white">{{ aiSuggestion.clarity }}%</div>
                      <p class="text-gray-500">{{ t('teacher.grading.ai.clarity') }}</p>
                    </div>
                  </div>
                </div>
                <Button variant="outline" size="sm" @click="applyAiSuggestion">
                  {{ t('teacher.grading.ai.apply') }}
                </Button>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.analysis') }}</h4>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ aiSuggestion.analysis }}</p>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.improvements') }}</h4>
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
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.history.title') }}</h2>
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
                    <badge variant="secondary" size="sm">{{ history.score }}{{ t('teacher.grading.history.scoreSuffix') }}</badge>
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
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.form.title') }}</h3>
            </template>
            
            <form @submit.prevent="submitGrade" class="space-y-6">
              <!-- 分数输入 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ t('teacher.grading.form.score') }} <span class="text-red-500">*</span>
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
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('teacher.grading.form.ofTotal', { total: assignment.totalScore }) }}</span>
                </div>
                <p v-if="errors.score" class="mt-1 text-sm text-red-600">{{ errors.score }}</p>
                <div class="mt-2">
                  <progress :value="gradeForm.score" :max="assignment.totalScore" size="sm" color="primary" />
                </div>
              </div>

              <!-- 等级评定 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ t('teacher.grading.form.grade') }}
                </label>
                <select v-model="gradeForm.grade" class="input">
                  <option value="">{{ t('teacher.grading.form.selectGrade') }}</option>
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
                  {{ t('teacher.grading.form.feedback') }} <span class="text-red-500">*</span>
                </label>
                <textarea
                  v-model="gradeForm.feedback"
                  rows="6"
                  :placeholder="t('teacher.grading.form.feedbackPlaceholder')"
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
                    {{ t('teacher.grading.form.strengths') }}
                  </label>
                  <textarea
                    v-model="gradeForm.strengths"
                    rows="3"
                    :placeholder="t('teacher.grading.form.strengthsPlaceholder')"
                    class="input"
                  ></textarea>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    {{ t('teacher.grading.form.improvements') }}
                  </label>
                  <textarea
                    v-model="gradeForm.improvements"
                    rows="3"
                    :placeholder="t('teacher.grading.form.improvementsPlaceholder')"
                    class="input"
                  ></textarea>
                </div>
              </div>

              <!-- 评分选项 -->
              <div class="space-y-3">
                <label class="flex items-center">
                  <input v-model="gradeForm.allowResubmit" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.allowResubmit') }}</span>
                </label>
                <label class="flex items-center">
                  <input v-model="gradeForm.sendNotification" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.sendNotification') }}</span>
                </label>
                <label class="flex items-center">
                  <input v-model="gradeForm.publishImmediately" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.publishImmediately') }}</span>
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
                  <check-icon class="w-4 h-4 mr-2" />
                  {{ t('teacher.grading.form.submit') }}
                </Button>
                
                <Button
                  type="button"
                  variant="outline"
                  size="lg"
                  class="w-full"
                  @click="saveDraft"
                  :loading="isDraftSaving"
                >
                  <document-duplicate-icon class="w-4 h-4 mr-2" />
                  {{ t('teacher.grading.form.saveDraft') }}
                </Button>
              </div>
            </form>
          </card>

          <!-- 学生信息 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.student.title') }}</h3>
            </template>
            
            <div class="space-y-4">
              <div class="flex items-center space-x-3">
                <div class="w-12 h-12">
                  <user-avatar :avatar="submission.avatar" :size="48">
                    <div class="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                      <user-icon class="w-6 h-6 text-blue-600 dark:text-blue-400" />
                    </div>
                  </user-avatar>
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-white">{{ submission.studentName }}</h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">{{ submission.studentId }}</p>
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.course') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ assignment.courseName }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.class') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.className }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.avg') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.averageScore ?? '--' }}/100</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.rank') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.rank ?? '--' }}/{{ submission.totalStudents ?? '--' }}</span>
                </div>
              </div>
              
              <Button variant="primary" size="lg" class="justify-center" @click="viewStudentProfile">
                <UserIcon class="w-4 h-4 mr-2" />
                {{ t('teacher.grading.student.viewProfile') }}
              </Button>
            </div>
          </card>

          <!-- 快速操作 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.quick.title') }}</h3>
            </template>
            
            <div class="space-y-3">
              <Button variant="outline" class="w-full justify-start" @click="contactStudent">
                <chat-bubble-left-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.quick.contact') }}
              </Button>
              <Button variant="outline" class="w-full justify-start" @click="viewOtherSubmissions">
                <document-text-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.quick.viewOthers') }}
              </Button>
              <Button variant="outline" class="w-full justify-start" @click="exportSubmission">
                <arrow-down-tray-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.quick.export') }}
              </Button>
              <Button variant="outline" class="w-full justify-start" @click="reportPlagiarism">
                <exclamation-triangle-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.quick.report') }}
              </Button>
            </div>
          </card>
        </div>
      </div>
      <!-- 举报弹窗 -->
      <teleport to="body">
        <div v-if="showReport" class="fixed inset-0 z-50">
          <div class="absolute inset-0 bg-black/30" @click="showReport=false"></div>
          <div class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md modal glass-thick p-5 space-y-4" v-glass="{ strength: 'thick', interactive: true }">
            <div class="text-lg font-semibold">{{ t('teacher.grading.quick.report') }}</div>
            <div class="space-y-2">
              <label class="text-sm">{{ t('teacher.studentDetail.table.reason') || '原因' }}</label>
              <input v-model="reportForm.reason" class="input w-full" />
              <label class="text-sm">{{ t('teacher.studentDetail.table.details') || '详情' }}</label>
              <textarea v-model="reportForm.details" class="input w-full" rows="4"></textarea>
            </div>
            <div class="flex items-center justify-end gap-2">
              <Button variant="secondary" size="sm" @click="showReport=false">{{ t('common.cancel') || '取消' }}</Button>
              <Button variant="primary" size="sm" @click="submitReport">{{ t('common.submit') || '提交' }}</Button>
            </div>
          </div>
        </div>
      </teleport>

      <!-- 改为调用全局抽屉：删除本地 Teleport -->
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
  UserIcon,
  CheckIcon,
  DocumentDuplicateIcon,
  ChatBubbleLeftIcon,
  DocumentTextIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useLocale } from '@/i18n/useLocale'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()
const { t } = useLocale()

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
  courseName: '',
  courseId: ''
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
const assignmentCourseId = computed(() => String(assignment.courseId || ''))

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
import { assignmentApi } from '@/api/assignment.api'
import { gradeApi } from '@/api/grade.api'
import { baseURL } from '@/api/config'
import { teacherStudentApi } from '@/api/teacher-student.api'
import { reportApi } from '@/api/report.api'
import { useChatStore } from '@/stores/chat'
import { submissionApi } from '@/api/submission.api'

const loadSubmission = async () => {
  try {
    const submissionId = String(route.params.submissionId || '')
    const assignmentId = String(route.params.assignmentId || '')
  const sRes = await submissionApi.getSubmissionById(submissionId)
  const s = sRes
    Object.assign(submission, s)
  const aRes = await assignmentApi.getAssignmentById(assignmentId)
  const a: any = aRes as any
    assignment.title = a.title
    assignment.description = a.description
    assignment.dueDate = a.dueDate
    assignment.totalScore = Number((a.maxScore ?? a.totalScore ?? 100))
    assignment.courseName = a.courseName || ''
    if (!assignment.courseName && (s as any)?.courseName) assignment.courseName = (s as any).courseName
    assignment.courseId = String(a.courseId || '')
    assignment.courseId = String(a.courseId || '')
    if ((s as any)?.gradeId) {
      try {
        const hist = await gradeApi.getGradeHistory(String((s as any).gradeId))
        gradingHistory.value = (hist as any) || []
      } catch {}
    } else {
      // 兜底：通过提交ID查询一次成绩摘要，取到 grade_id 再查历史
      try {
        const sg = await submissionApi.getSubmissionGrade(submissionId)
        const gid = (sg as any)?.grade_id
        // 兼容平均分、总人数、排名（注意后端字段名）
        const avgVal = (sg as any)?.averageScore ?? (sg as any)?.avg_score
        submission.averageScore = (avgVal === null || avgVal === undefined) ? undefined : Number(avgVal)
        const totalVal = (sg as any)?.totalStudents ?? (sg as any)?.total_students
        submission.totalStudents = (totalVal === null || totalVal === undefined) ? undefined : Number(totalVal)
        const rankVal = (sg as any)?.rank ?? (sg as any)?.student_rank
        submission.rank = (rankVal === null || rankVal === undefined) ? undefined : Number(rankVal)
        if (gid) {
          const hist = await gradeApi.getGradeHistory(String(gid))
          gradingHistory.value = (hist as any) || []
        }
      } catch {}
    }
    // 兜底头像
    if (!submission.avatar && submission.studentId) {
      try {
        const prof: any = await teacherStudentApi.getStudentProfile(String(submission.studentId))
        if (prof && (prof as any).avatar) submission.avatar = (prof as any).avatar
      } catch {}
    }
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.loadFailed'),
      message: t('teacher.grading.notify.loadFailedMsg')
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
    submitted: t('teacher.submissions.status.submitted'),
    graded: t('teacher.submissions.status.graded'),
    returned: t('teacher.submissions.status.returned'),
    late: t('teacher.submissions.status.late')
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
    errors.score = t('teacher.grading.errors.scoreTooLow')
  } else if (gradeForm.score > assignment.totalScore) {
    errors.score = t('teacher.grading.errors.scoreTooHigh', { max: assignment.totalScore }) as string
  } else {
    errors.score = ''
  }
}

const validateFeedback = () => {
  if (gradeForm.feedback.trim() === '') {
    errors.feedback = t('teacher.grading.errors.feedbackRequired')
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
    title: t('teacher.grading.notify.aiApplied'),
    message: t('teacher.grading.notify.aiAppliedMsg')
  })
}

const previewFile = (file: any) => {
  // 实现文件预览
  uiStore.showNotification({
    type: 'info',
    title: t('teacher.grading.notify.filePreview'),
    message: `\${t('teacher.grading.notify.filePreview')}: ${file.name}`
  })
}

const downloadFile = (file: any) => {
  // 实现文件下载
  uiStore.showNotification({
    type: 'success',
    title: t('teacher.grading.notify.downloading'),
    message: `${t('teacher.grading.notify.downloading')}: ${file.name}`
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
    const payload: any = {
      submissionId: String(submission.id),
      studentId: String(submission.studentId),
      assignmentId: String(assignment.id),
      score: Number(gradeForm.score || 0),
      maxScore: Number(assignment.totalScore || 100),
      feedback: gradeForm.feedback || '',
      strengths: gradeForm.strengths || '',
      improvements: gradeForm.improvements || '',
      allowResubmit: !!gradeForm.allowResubmit,
      status: 'draft' as const,
      publishImmediately: false
    }
    await gradeApi.gradeSubmission(payload)
    uiStore.showNotification({
      type: 'success',
      title: t('teacher.grading.notify.draftSaved'),
      message: t('teacher.grading.notify.draftSavedMsg')
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.saveFailed'),
      message: t('teacher.grading.notify.saveFailedMsg')
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
      title: t('teacher.grading.notify.formInvalid'),
      message: t('teacher.grading.notify.formInvalidMsg')
    })
    return
  }

  isSubmitting.value = true
  try {
    const payload: any = {
      submissionId: String(submission.id),
      studentId: String(submission.studentId),
      assignmentId: String(assignment.id),
      score: Number(gradeForm.score),
      maxScore: Number(assignment.totalScore || 100),
      feedback: gradeForm.feedback || '',
      strengths: gradeForm.strengths || '',
      improvements: gradeForm.improvements || '',
      allowResubmit: !!gradeForm.allowResubmit,
      publishImmediately: !!gradeForm.publishImmediately,
      status: (gradeForm.publishImmediately ? 'published' : 'draft') as 'published' | 'draft'
    }
    const gr = await gradeApi.gradeSubmission(payload)
    const gradeId = (gr as any)?.id
    if (gradeForm.publishImmediately && gradeId) {
      await gradeApi.publishGrade(String(gradeId))
    }
    
    uiStore.showNotification({
      type: 'success',
      title: t('teacher.grading.notify.submitSuccess'),
      message: gradeForm.publishImmediately ? t('teacher.grading.notify.submitPublished') : t('teacher.grading.notify.submitSaved')
    })
    
    // 跳转回该作业的提交列表，便于继续批改
    router.push(`/teacher/assignments/${assignment.id}/submissions`)
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.submitFailed'),
      message: t('teacher.grading.notify.submitFailedMsg')
    })
  } finally {
    isSubmitting.value = false
  }
}

const viewStudentProfile = () => {
  router.push(`/teacher/students/${submission.studentId}`)
}

const chat = useChatStore()
const contactStudent = () => {
  if (!submission.studentId) return
  chat.openChat(String(submission.studentId), String(submission.studentName || ''), String(assignment.courseId || ''))
}

const viewOtherSubmissions = () => {
  router.push(`/teacher/assignments/${assignment.id}/submissions?highlightStudentId=${submission.studentId}`)
}

const exportSubmission = async () => {
  if (!submission.id) return
  try {
    const blob = await submissionApi.exportSubmission(String(submission.id))
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `submission_${submission.id}.zip`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (e) {
    uiStore.showNotification({ type: 'error', title: t('teacher.analytics.messages.exportFailed') || 'Export failed', message: t('teacher.analytics.messages.exportFailedMsg') || 'Please try again later' })
  }
}

function goCourse() {
  if (assignmentCourseId.value) {
    router.push(`/teacher/courses/${assignmentCourseId.value}`)
  }
}

const showReport = ref(false)
const reportForm = reactive({ reason: '', details: '' })
const reportPlagiarism = () => { showReport.value = true }
const submitReport = async () => {
  if (!reportForm.reason.trim()) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.quick.report'), message: t('teacher.grading.errors.feedbackRequired') })
    return
  }
  try {
    await reportApi.createReport({
      reportedStudentId: String(submission.studentId || ''),
      courseId: String(assignment.courseId || ''),
      assignmentId: String(assignment.id || ''),
      submissionId: String(submission.id || ''),
      reason: reportForm.reason,
      details: reportForm.details || ''
    })
    uiStore.showNotification({ type: 'success', title: t('teacher.grading.quick.report'), message: t('teacher.grading.notify.submitSuccess') })
    showReport.value = false
    reportForm.reason = ''
    reportForm.details = ''
  } catch (e) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.quick.report'), message: t('teacher.grading.notify.submitFailed') })
  }
}

// 生命周期
onMounted(() => {
  loadSubmission()
})
</script> 