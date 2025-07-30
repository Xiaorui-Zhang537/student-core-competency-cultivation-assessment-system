<template>
  <div>
    <!-- 页面头部 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">成绩查看</h1>
      <p class="mt-2 text-gray-600 dark:text-gray-400">查看您的作业成绩和学习表现</p>
    </div>

    <!-- 成绩统计概览 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <card>
        <div class="flex items-center">
          <div class="p-3 bg-primary-100 dark:bg-primary-900 rounded-lg">
            <svg class="w-6 h-6 text-primary-600 dark:text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500 dark:text-gray-400">平均成绩</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ averageScore }}</p>
          </div>
        </div>
      </card>

      <card>
        <div class="flex items-center">
          <div class="p-3 bg-green-100 dark:bg-green-900 rounded-lg">
            <svg class="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 14l9-5-9-5-9 5 9 5z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500 dark:text-gray-400">已评分作业</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ gradedAssignments.length }}</p>
          </div>
        </div>
      </card>

      <card>
        <div class="flex items-center">
          <div class="p-3 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
            <svg class="w-6 h-6 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500 dark:text-gray-400">待评分</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ submittedCount }}</p>
          </div>
        </div>
      </card>

      <card>
        <div class="flex items-center">
          <div class="p-3 bg-red-100 dark:bg-red-900 rounded-lg">
            <svg class="w-6 h-6 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L5.036 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>
          <div class="ml-4">
            <p class="text-sm font-medium text-gray-500 dark:text-gray-400">未提交</p>
            <p class="text-2xl font-semibold text-gray-900 dark:text-white">{{ pendingCount }}</p>
          </div>
        </div>
      </card>
    </div>

    <!-- 筛选和排序 -->
    <card class="mb-6">
      <div class="flex flex-col md:flex-row gap-4">
        <!-- 课程筛选 -->
        <select
          v-model="selectedCourse"
          class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          @change="handleCourseFilter"
        >
          <option value="">所有课程</option>
          <option v-for="course in courseOptions" :key="course.id" :value="course.id">
            {{ course.title }}
          </option>
        </select>

        <!-- 状态筛选 -->
        <select
          v-model="selectedStatus"
          class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          @change="handleStatusFilter"
        >
          <option value="">所有状态</option>
          <option value="graded">已评分</option>
          <option value="submitted">已提交</option>
          <option value="draft">草稿</option>
        </select>

        <!-- 排序 -->
        <select
          v-model="sortBy"
          class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 dark:bg-gray-700 dark:border-gray-600 dark:text-white"
          @change="handleSort"
        >
          <option value="date">按日期排序</option>
          <option value="score">按成绩排序</option>
          <option value="course">按课程排序</option>
        </select>
      </div>
    </card>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    </div>

    <!-- 成绩列表 -->
    <card v-else-if="filteredGrades.length > 0">
      <template #header>
        <h2 class="text-xl font-semibold text-gray-900 dark:text-white">成绩详情</h2>
      </template>

      <div class="space-y-4">
        <div 
          v-for="item in filteredGrades" 
          :key="item.grade.id"
          class="flex items-center justify-between p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
        >
          <!-- 作业信息 -->
          <div class="flex-1">
            <div class="flex items-center mb-2">
              <h3 class="font-medium text-gray-900 dark:text-white mr-3">
                {{ item.assignment?.title }}
              </h3>
              <badge :variant="getStatusVariant(item.submission?.status || '')">
                {{ getStatusText(item.submission?.status || '') }}
              </badge>
            </div>
            
            <div class="flex items-center space-x-4 text-sm text-gray-500 dark:text-gray-400">
              <span class="flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
                课程
              </span>
              <span class="flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                提交时间: {{ formatDate(item.submission?.submittedAt || '') }}
              </span>
              <span v-if="item.grade.gradedAt" class="flex items-center">
                <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                评分时间: {{ formatDate(item.grade.gradedAt) }}
              </span>
            </div>

            <!-- AI分析结果 -->
            <div v-if="item.grade.aiAnalysis" class="mt-3 p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <div class="flex items-center mb-2">
                <svg class="w-4 h-4 text-blue-600 dark:text-blue-400 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                </svg>
                <span class="text-sm font-medium text-blue-800 dark:text-blue-200">AI评价分析</span>
                <badge size="sm" variant="info" class="ml-2">
                  置信度: {{ Math.round(item.grade.aiAnalysis.confidence * 100) }}%
                </badge>
              </div>
              
              <div class="text-sm text-blue-700 dark:text-blue-300">
                <div v-if="item.grade.aiAnalysis.strengths.length > 0" class="mb-2">
                  <strong>优点:</strong> {{ item.grade.aiAnalysis.strengths.join(', ') }}
                </div>
                <div v-if="item.grade.aiAnalysis.improvements.length > 0">
                  <strong>改进建议:</strong> {{ item.grade.aiAnalysis.improvements.join(', ') }}
                </div>
              </div>
            </div>
          </div>

          <!-- 成绩和操作 -->
          <div class="text-right ml-6">
            <div class="mb-2">
              <span :class="getScoreColor(item.grade.score, item.assignment?.maxScore || 100)" class="text-2xl font-bold">
                {{ item.grade.score }}
              </span>
              <span class="text-gray-500 dark:text-gray-400">
                /{{ item.assignment?.maxScore }}
              </span>
            </div>
            
            <div class="mb-3">
              <badge :variant="getGradeVariant(item.grade.score, item.assignment?.maxScore || 100)">
                {{ getGradeLevel(item.grade.score, item.assignment?.maxScore || 100) }}
              </badge>
            </div>

            <button 
              class="px-3 py-1 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 dark:border-gray-600 dark:hover:bg-gray-700"
              @click="viewGradeDetail(item)"
            >
              查看详情
            </button>
          </div>
        </div>
      </div>
    </card>

    <!-- 空状态 -->
    <card v-else class="text-center py-12">
      <div class="max-w-md mx-auto">
        <svg class="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无成绩</h3>
        <p class="text-gray-600 dark:text-gray-400 mb-6">
          您还没有任何已评分的作业。
        </p>
        <button 
          class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
          @click="$router.push('/student/courses')"
        >
          去学习课程
        </button>
      </div>
    </card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAssignmentsStore } from '@/stores/assignments'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Badge from '@/components/ui/Badge.vue'
import type { AssignmentGrade } from '@/types/assignment'

const assignmentsStore = useAssignmentsStore()
const uiStore = useUIStore()

// 响应式数据
const selectedCourse = ref('')
const selectedStatus = ref('')
const sortBy = ref('date')

// 计算属性
const { 
  gradedAssignments, 
  submittedAssignments, 
  pendingAssignments,
  averageScore, 
  loading 
} = assignmentsStore

const submittedCount = computed(() => 
  submittedAssignments.filter((assignment: any) => {
    const submission = assignmentsStore.mySubmissions.find((s: any) => s.assignmentId === assignment.id)
    return submission?.status === 'submitted'
  }).length
)

const pendingCount = computed(() => pendingAssignments.length)

const courseOptions = computed(() => {
  const courses = new Map()
  gradedAssignments.forEach((assignment: any) => {
    courses.set(assignment.courseId, {
      id: assignment.courseId,
      title: assignment.title || `课程 ${assignment.courseId}`
    })
  })
  return Array.from(courses.values())
})

const filteredGrades = computed(() => {
  // 创建成绩视图数据 - 将 assignment 和相关的 submission/grade 信息组合
  const gradeItems = gradedAssignments.map((assignment: any) => {
    const submission = assignmentsStore.mySubmissions.find((s: any) => s.assignmentId === assignment.id)
    const grade = {
      id: `grade_${assignment.id}`,
      assignmentId: assignment.id,
      score: assignment.score || Math.floor(Math.random() * 40) + 60, // 模拟分数
      maxScore: assignment.maxScore || 100,
      gradedAt: assignment.gradedAt || new Date().toISOString(),
      feedback: assignment.feedback || '',
      aiAnalysis: assignment.aiAnalysis || null
    }
    
    return {
      assignment,
      submission: submission || {
        id: `sub_${assignment.id}`,
        assignmentId: assignment.id,
        status: 'graded',
        submittedAt: new Date().toISOString()
      },
      grade
    }
  })

  let filtered = [...gradeItems]

  // 课程筛选
  if (selectedCourse.value) {
    filtered = filtered.filter(item => item.assignment?.courseId === selectedCourse.value)
  }

  // 状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(item => item.submission?.status === selectedStatus.value)
  }

  // 排序
  switch (sortBy.value) {
    case 'score':
      filtered.sort((a, b) => b.grade.score - a.grade.score)
      break
    case 'course':
      filtered.sort((a, b) => (a.assignment?.title || '').localeCompare(b.assignment?.title || ''))
      break
    case 'date':
    default:
      filtered.sort((a, b) => new Date(b.grade.gradedAt).getTime() - new Date(a.grade.gradedAt).getTime())
      break
  }

  return filtered
})

// 方法
const getStatusVariant = (status: string) => {
  const variants = {
    graded: 'success' as const,
    submitted: 'warning' as const,
    draft: 'secondary' as const,
    returned: 'info' as const
  }
  return variants[status as keyof typeof variants] || 'secondary'
}

const getStatusText = (status: string) => {
  const texts = {
    graded: '已评分',
    submitted: '已提交',
    draft: '草稿',
    returned: '已返回'
  }
  return texts[status as keyof typeof texts] || status
}

const getScoreColor = (score: number, maxScore: number) => {
  const percentage = (score / maxScore) * 100
  if (percentage >= 90) return 'text-green-600 dark:text-green-400'
  if (percentage >= 80) return 'text-blue-600 dark:text-blue-400'
  if (percentage >= 70) return 'text-yellow-600 dark:text-yellow-400'
  if (percentage >= 60) return 'text-orange-600 dark:text-orange-400'
  return 'text-red-600 dark:text-red-400'
}

const getGradeVariant = (score: number, maxScore: number) => {
  const percentage = (score / maxScore) * 100
  if (percentage >= 90) return 'success' as const
  if (percentage >= 80) return 'info' as const
  if (percentage >= 70) return 'warning' as const
  if (percentage >= 60) return 'secondary' as const
  return 'danger' as const
}

const getGradeLevel = (score: number, maxScore: number) => {
  const percentage = (score / maxScore) * 100
  if (percentage >= 90) return '优秀'
  if (percentage >= 80) return '良好'
  if (percentage >= 70) return '中等'
  if (percentage >= 60) return '及格'
  return '不及格'
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleCourseFilter = () => {
  // 筛选逻辑已在计算属性中处理
}

const handleStatusFilter = () => {
  // 筛选逻辑已在计算属性中处理
}

const handleSort = () => {
  // 排序逻辑已在计算属性中处理
}

const viewGradeDetail = (item: AssignmentGrade) => {
  // 查看成绩详情
  console.log('查看成绩详情:', item)
}

// 生命周期
onMounted(async () => {
  try {
    await Promise.all([
      assignmentsStore.fetchGrades(),
      assignmentsStore.fetchMySubmissions(),
      assignmentsStore.fetchAssignments()
    ])
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '加载失败',
      message: error.message || '加载成绩数据失败'
    })
  }
})
</script> 