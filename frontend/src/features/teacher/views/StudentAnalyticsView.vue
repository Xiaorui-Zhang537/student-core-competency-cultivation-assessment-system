<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">学生分析</h1>
          <p class="text-gray-600 dark:text-gray-400">深入了解学生学习状况，提供个性化指导</p>
        </div>
        <div class="flex items-center space-x-4">
          <button variant="outline" @click="showFilters = !showFilters">
            <funnel-icon class="w-4 h-4 mr-2" />
            筛选
          </button>
          <button variant="outline" @click="exportAnalytics">
            <document-arrow-down-icon class="w-4 h-4 mr-2" />
            导出报告
          </button>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <card v-if="showFilters" padding="lg" class="mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">课程</label>
          <select v-model="selectedCourseId" @change="onFilterChange" class="input w-full">
            <option value="">全部课程</option>
            <option v-for="course in teacherStore.courses" :key="course.courseId" :value="course.courseId">
              {{ course.courseName }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">学生状态</label>
          <select v-model="selectedStatus" @change="onFilterChange" class="input w-full">
            <option value="">全部状态</option>
            <option value="active">活跃</option>
            <option value="at_risk">需要关注</option>
            <option value="inactive">不活跃</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">年级专业</label>
          <select v-model="selectedGrade" @change="onFilterChange" class="input w-full">
            <option value="">全部年级</option>
            <option value="freshman">大一</option>
            <option value="sophomore">大二</option>
            <option value="junior">大三</option>
            <option value="senior">大四</option>
          </select>
        </div>
        <div class="flex items-end">
          <button variant="outline" @click="clearFilters" class="w-full">
            清除筛选
          </button>
        </div>
      </div>
    </card>

    <!-- 班级概览统计 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-2">
          {{ totalStudents }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">总学生数</p>
        <div class="mt-2">
          <span class="text-xs text-green-600 dark:text-green-400">
            +{{ Math.floor(Math.random() * 5) + 1 }} 本月新增
          </span>
        </div>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-2">
          {{ activeStudentsCount }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">活跃学生</p>
        <div class="mt-2">
          <span class="text-xs text-blue-600 dark:text-blue-400">
            {{ Math.round((activeStudentsCount / totalStudents) * 100) }}% 活跃率
          </span>
        </div>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-2">
          {{ atRiskStudentsCount }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">需要关注</p>
        <div class="mt-2">
          <badge variant="warning" size="sm">
            重点关注
          </badge>
        </div>
      </card>
      
      <card padding="lg" class="text-center">
        <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-2">
          {{ averageScore.toFixed(1) }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">班级平均分</p>
        <div class="mt-2">
          <div class="flex justify-center">
            <progress :value="averageScore" :max="100" color="primary" size="sm" class="w-full" />
          </div>
        </div>
      </card>
    </div>

    <!-- 主要内容区域 -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
      <!-- 左侧：学生列表和详细分析 -->
      <div class="xl:col-span-2 space-y-8">
        <!-- 学生列表 -->
        <card padding="lg">
          <template #header>
            <div class="flex justify-between items-center">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学生概览</h3>
              <div class="flex items-center space-x-2">
                <select v-model="sortBy" @change="sortStudents" class="input input-sm">
                  <option value="name">按姓名</option>
                  <option value="score">按成绩</option>
                  <option value="progress">按进度</option>
                  <option value="lastActivity">按活跃度</option>
                </select>
                <button variant="outline" size="sm" @click="refreshStudents">
                  <arrow-path-icon class="w-4 h-4" />
                </button>
              </div>
            </div>
          </template>
          
          <div class="space-y-4">
            <div v-for="student in displayedStudents" 
                 :key="student.id"
                 class="flex items-center justify-between p-4 border border-gray-200 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer transition-colors"
                 @click="selectStudent(student)">
              <div class="flex items-center space-x-4">
                <div class="relative">
                  <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-medium">
                    {{ student.name.charAt(0) }}
                  </div>
                  <div :class="[
                    'absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 border-white',
                    getStatusColor(student.status)
                  ]"></div>
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-white">{{ student.name }}</h4>
                  <p class="text-sm text-gray-500 dark:text-gray-400">
                    {{ student.studentCode }} · {{ student.major }}
                  </p>
                  <p class="text-xs text-gray-400 dark:text-gray-500">
                    最后活跃：{{ formatRelativeTime(student.lastActivity) }}
                  </p>
                </div>
              </div>
              
              <div class="flex items-center space-x-4">
                <div class="text-right">
                  <p class="text-sm font-medium text-gray-900 dark:text-white">
                    平均分：{{ student.averageScore }}
                  </p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    完成：{{ student.completedAssignments }}/{{ student.enrolledCourses * 5 }}
                  </p>
                </div>
                <div class="flex flex-col space-y-1">
                  <badge :variant="getStudentStatusColor(student.status)" size="sm">
                    {{ getStudentStatusText(student.status) }}
                  </badge>
                  <div class="w-16">
                    <progress :value="student.averageScore" :max="100" size="sm" color="primary" />
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="displayedStudents.length === 0" class="text-center py-8">
              <users-icon class="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p class="text-gray-500 dark:text-gray-400">暂无学生数据</p>
            </div>
          </div>
        </card>

        <!-- 学生详细分析 (当选中学生时显示) -->
        <card v-if="selectedStudent" padding="lg">
          <template #header>
            <div class="flex justify-between items-center">
              <div class="flex items-center space-x-4">
                <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-medium text-lg">
                  {{ selectedStudent.name.charAt(0) }}
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ selectedStudent.name }}</h3>
                  <p class="text-sm text-gray-600 dark:text-gray-400">
                    {{ selectedStudent.studentCode }} · {{ selectedStudent.major }}
                  </p>
                </div>
              </div>
              <div class="flex items-center space-x-2">
                <button variant="outline" size="sm" @click="sendMessage(selectedStudent)">
                  <chat-bubble-left-right-icon class="w-4 h-4 mr-1" />
                  发消息
                </button>
                <button variant="outline" size="sm" @click="generateReport(selectedStudent)">
                  <document-text-icon class="w-4 h-4 mr-1" />
                  生成报告
                </button>
              </div>
            </div>
          </template>
          
          <div class="space-y-6">
            <!-- 能力雷达图 -->
            <div>
              <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">能力发展分析</h4>
              <div ref="abilityRadarRef" class="h-80 w-full"></div>
            </div>
            
            <!-- 学习趋势图 -->
            <div>
              <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">学习趋势</h4>
              <div ref="learningTrendRef" class="h-60 w-full"></div>
            </div>
            
            <!-- 详细能力分析 -->
            <div>
              <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4">能力详细分析</h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="ability in selectedStudent.abilityScores" 
                     :key="ability.dimensionId"
                     class="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-sm font-medium text-gray-900 dark:text-white">
                      {{ ability.dimensionName }}
                    </span>
                    <span class="text-sm text-gray-600 dark:text-gray-400">
                      {{ ability.score }} 分
                    </span>
                  </div>
                  <progress :value="ability.score" :max="100" size="sm" color="primary" />
                  <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                    等级：{{ ability.level }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </card>
      </div>

      <!-- 右侧：班级分析和建议 -->
      <div class="space-y-8">
        <!-- 班级能力分布 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">班级能力分布</h3>
          </template>
          <div ref="classAbilityRef" class="h-64 w-full"></div>
        </card>

        <!-- 学习行为分析 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习行为分析</h3>
          </template>
          
          <div class="space-y-4">
            <div class="flex justify-between items-center py-2">
              <span class="text-sm text-gray-600 dark:text-gray-400">平均在线时长</span>
              <span class="text-sm font-medium text-gray-900 dark:text-white">2.5小时/天</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-sm text-gray-600 dark:text-gray-400">作业按时完成率</span>
              <span class="text-sm font-medium text-gray-900 dark:text-white">85%</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-sm text-gray-600 dark:text-gray-400">主动提问频率</span>
              <span class="text-sm font-medium text-gray-900 dark:text-white">3.2次/周</span>
            </div>
            <div class="flex justify-between items-center py-2">
              <span class="text-sm text-gray-600 dark:text-gray-400">同伴协作参与度</span>
              <span class="text-sm font-medium text-gray-900 dark:text-white">72%</span>
            </div>
            
            <div class="pt-4 border-t border-gray-200 dark:border-gray-600">
              <h5 class="text-sm font-medium text-gray-900 dark:text-white mb-3">活跃时段分布</h5>
              <div ref="activityHeatmapRef" class="h-32 w-full"></div>
            </div>
          </div>
        </card>

        <!-- 风险预警 -->
        <card padding="lg">
          <template #header>
            <div class="flex items-center space-x-2">
              <exclamation-triangle-icon class="w-5 h-5 text-yellow-500" />
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">风险预警</h3>
            </div>
          </template>
          
          <div class="space-y-3">
            <div v-for="alert in riskAlerts" 
                 :key="alert.id"
                 class="p-3 rounded-lg border-l-4"
                 :class="getRiskAlertClass(alert.level)">
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <h4 class="text-sm font-medium mb-1">{{ alert.title }}</h4>
                  <p class="text-xs text-gray-600 dark:text-gray-400">{{ alert.description }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-500 mt-1">
                    影响学生：{{ alert.affectedStudents.join(', ') }}
                  </p>
                </div>
                <badge :variant="getRiskBadgeVariant(alert.level)" size="sm">
                  {{ getRiskLevelText(alert.level) }}
                </badge>
              </div>
            </div>
            
            <div v-if="riskAlerts.length === 0" class="text-center py-4">
              <check-circle-icon class="w-8 h-8 text-green-500 mx-auto mb-2" />
              <p class="text-sm text-gray-500 dark:text-gray-400">暂无风险预警</p>
            </div>
          </div>
        </card>

        <!-- 个性化建议 -->
        <card padding="lg">
          <template #header>
            <div class="flex items-center space-x-2">
              <light-bulb-icon class="w-5 h-5 text-blue-500" />
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">教学建议</h3>
            </div>
          </template>
          
          <div class="space-y-3">
            <div v-for="suggestion in teachingSuggestions" 
                 :key="suggestion.id"
                 class="p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg border border-blue-200 dark:border-blue-800">
              <div class="flex items-start space-x-3">
                <div class="flex-shrink-0 w-6 h-6 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center mt-0.5">
                  <span class="text-xs font-medium text-blue-600 dark:text-blue-400">{{ suggestion.priority }}</span>
                </div>
                <div class="flex-1">
                  <h4 class="text-sm font-medium text-blue-900 dark:text-blue-100 mb-1">{{ suggestion.title }}</h4>
                  <p class="text-sm text-blue-800 dark:text-blue-200">{{ suggestion.description }}</p>
                  <div class="mt-2 flex items-center justify-between">
                    <span class="text-xs text-blue-600 dark:text-blue-400">{{ suggestion.category }}</span>
                    <button variant="outline" size="sm" @click="applySuggestion(suggestion)">
                      应用建议
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useTeacherStore } from '@/stores/teacher'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import type { StudentOverview } from '@/types/teacher'
import {
  FunnelIcon,
  DocumentArrowDownIcon,
  ArrowPathIcon,
  UsersIcon,
  ChatBubbleLeftRightIcon,
  DocumentTextIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  LightBulbIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const teacherStore = useTeacherStore()
const uiStore = useUIStore()

// 状态
const showFilters = ref(false)
const selectedStudent = ref<StudentOverview | null>(null)
const selectedCourseId = ref('')
const selectedStatus = ref('')
const selectedGrade = ref('')
const sortBy = ref('name')

// 图表引用
const abilityRadarRef = ref<HTMLElement>()
const learningTrendRef = ref<HTMLElement>()
const classAbilityRef = ref<HTMLElement>()
const activityHeatmapRef = ref<HTMLElement>()

let abilityRadarChart: echarts.ECharts | null = null
let learningTrendChart: echarts.ECharts | null = null
let classAbilityChart: echarts.ECharts | null = null
let activityHeatmapChart: echarts.ECharts | null = null

// 模拟数据
const riskAlerts = ref([
  {
    id: '1',
    level: 'high',
    title: '学习进度明显落后',
    description: '有3名学生连续2周未完成作业，需要及时干预',
    affectedStudents: ['张三', '李四', '王五']
  },
  {
    id: '2',
    level: 'medium',
    title: '在线活跃度下降',
    description: '班级整体在线学习时长比上月下降15%',
    affectedStudents: ['全班']
  }
])

const teachingSuggestions = ref([
  {
    id: '1',
    priority: 1,
    title: '增加互动环节',
    description: '建议在课堂中增加小组讨论和问答环节，提高学生参与度',
    category: '教学方法'
  },
  {
    id: '2',
    priority: 2,
    title: '个性化作业分配',
    description: '根据学生能力水平分配不同难度的作业，促进个性化学习',
    category: '作业设计'
  },
  {
    id: '3',
    priority: 3,
    title: '加强能力薄弱学生辅导',
    description: '对逻辑思维能力较弱的学生提供额外辅导和练习机会',
    category: '个别辅导'
  }
])

// 计算属性
const displayedStudents = computed(() => {
  let filtered = teacherStore.students

  if (selectedCourseId.value) {
    // 这里需要根据实际数据结构进行筛选
    // filtered = filtered.filter(student => student.courses.includes(selectedCourseId.value))
  }

  if (selectedStatus.value) {
    filtered = filtered.filter(student => student.status === selectedStatus.value)
  }

  if (selectedGrade.value) {
    filtered = filtered.filter(student => student.grade === selectedGrade.value)
  }

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'score':
        return b.averageScore - a.averageScore
      case 'progress':
        return b.completedAssignments - a.completedAssignments
      case 'lastActivity':
        return new Date(b.lastActivity).getTime() - new Date(a.lastActivity).getTime()
      default:
        return a.name.localeCompare(b.name)
    }
  })

  return filtered
})

const totalStudents = computed(() => teacherStore.students.length)
const activeStudentsCount = computed(() => teacherStore.students.filter(s => s.status === 'active').length)
const atRiskStudentsCount = computed(() => teacherStore.students.filter(s => s.status === 'at_risk').length)
const averageScore = computed(() => {
  if (teacherStore.students.length === 0) return 0
  const total = teacherStore.students.reduce((sum, student) => sum + student.averageScore, 0)
  return total / teacherStore.students.length
})

// 方法
const formatRelativeTime = (timestamp: string) => {
  const now = new Date()
  const date = new Date(timestamp)
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60))
  
  if (diffInHours < 1) return '刚刚'
  if (diffInHours < 24) return `${diffInHours}小时前`
  if (diffInHours < 48) return '昨天'
  return `${Math.floor(diffInHours / 24)}天前`
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'active': return 'bg-green-400'
    case 'at_risk': return 'bg-yellow-400'
    case 'inactive': return 'bg-red-400'
    default: return 'bg-gray-400'
  }
}

const getStudentStatusColor = (status: string) => {
  switch (status) {
    case 'active': return 'success'
    case 'at_risk': return 'warning'
    case 'inactive': return 'secondary'
    default: return 'secondary'
  }
}

const getStudentStatusText = (status: string) => {
  switch (status) {
    case 'active': return '活跃'
    case 'at_risk': return '需关注'
    case 'inactive': return '不活跃'
    default: return '未知'
  }
}

const getRiskAlertClass = (level: string) => {
  switch (level) {
    case 'high': return 'bg-red-50 dark:bg-red-900/20 border-red-500'
    case 'medium': return 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-500'
    case 'low': return 'bg-blue-50 dark:bg-blue-900/20 border-blue-500'
    default: return 'bg-gray-50 dark:bg-gray-800 border-gray-500'
  }
}

const getRiskBadgeVariant = (level: string) => {
  switch (level) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    case 'low': return 'info'
    default: return 'secondary'
  }
}

const getRiskLevelText = (level: string) => {
  switch (level) {
    case 'high': return '高风险'
    case 'medium': return '中风险'
    case 'low': return '低风险'
    default: return '未知'
  }
}

const onFilterChange = () => {
  // 筛选逻辑
}

const clearFilters = () => {
  selectedCourseId.value = ''
  selectedStatus.value = ''
  selectedGrade.value = ''
}

const sortStudents = () => {
  // 排序逻辑已在计算属性中处理
}

const refreshStudents = async () => {
  await teacherStore.fetchStudents()
}

const selectStudent = (student: StudentOverview) => {
  selectedStudent.value = student
  nextTick(() => {
    initStudentCharts()
  })
}

const sendMessage = (student: StudentOverview) => {
  router.push(`/teacher/messages/compose?to=${student.id}`)
}

const generateReport = async (student: StudentOverview) => {
  try {
    // 这里应该调用API生成报告
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    uiStore.showNotification({
      type: 'success',
      title: '报告生成成功',
      message: `已为${student.name}生成详细分析报告`
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '报告生成失败',
      message: '无法生成学生分析报告，请重试'
    })
  }
}

const exportAnalytics = async () => {
  try {
    await teacherStore.exportStudentGrades(selectedCourseId.value || 'all', 'excel')
  } catch (error) {
    console.error('导出失败:', error)
  }
}

const applySuggestion = (suggestion: any) => {
  uiStore.showNotification({
    type: 'info',
    title: '建议已记录',
    message: `教学建议"${suggestion.title}"已记录到您的教学计划中`
  })
}

// 初始化图表
const initStudentCharts = () => {
  if (!selectedStudent.value) return

  // 能力雷达图
  if (abilityRadarRef.value) {
    abilityRadarChart = echarts.init(abilityRadarRef.value)
    
    const radarOption = {
      title: {
        show: false
      },
      radar: {
        indicator: selectedStudent.value.abilityScores.map(ability => ({
          name: ability.dimensionName,
          max: 100
        })),
        shape: 'polygon',
        splitNumber: 5,
        axisName: {
          color: '#6b7280',
          fontSize: 12
        },
        splitLine: {
          lineStyle: {
            color: '#e5e7eb'
          }
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: ['rgba(59, 130, 246, 0.1)', 'rgba(59, 130, 246, 0.05)']
          }
        }
      },
      series: [{
        type: 'radar',
        data: [{
          value: selectedStudent.value.abilityScores.map(ability => ability.score),
          name: selectedStudent.value.name,
          areaStyle: {
            color: 'rgba(59, 130, 246, 0.3)'
          },
          lineStyle: {
            color: '#3b82f6',
            width: 2
          },
          itemStyle: {
            color: '#3b82f6'
          }
        }]
      }]
    }
    
    abilityRadarChart.setOption(radarOption)
  }

  // 学习趋势图
  if (learningTrendRef.value) {
    learningTrendChart = echarts.init(learningTrendRef.value)
    
    // 模拟趋势数据
    const trendData = Array.from({ length: 12 }, (_, i) => ({
      month: `${i + 1}月`,
      score: Math.floor(Math.random() * 30) + 70
    }))
    
    const trendOption = {
      title: {
        show: false
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: trendData.map(d => d.month),
        axisLine: {
          lineStyle: {
            color: '#e5e7eb'
          }
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLine: {
          lineStyle: {
            color: '#e5e7eb'
          }
        }
      },
      series: [{
        type: 'line',
        data: trendData.map(d => d.score),
        smooth: true,
        lineStyle: {
          color: '#10b981',
          width: 3
        },
        itemStyle: {
          color: '#10b981'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
            ]
          }
        }
      }]
    }
    
    learningTrendChart.setOption(trendOption)
  }
}

const initClassCharts = () => {
  // 班级能力分布图
  if (classAbilityRef.value) {
    classAbilityChart = echarts.init(classAbilityRef.value)
    
    const classOption = {
      title: {
        show: false
      },
      tooltip: {
        trigger: 'item'
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 35, name: '优秀(90-100分)' },
          { value: 45, name: '良好(80-89分)' },
          { value: 15, name: '中等(70-79分)' },
          { value: 5, name: '需改进(<70分)' }
        ],
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 12,
            fontWeight: 'bold'
          }
        }
      }]
    }
    
    classAbilityChart.setOption(classOption)
  }

  // 活跃时段热力图
  if (activityHeatmapRef.value) {
    activityHeatmapChart = echarts.init(activityHeatmapRef.value)
    
    // 模拟热力图数据
    const hours = Array.from({ length: 24 }, (_, i) => `${i}时`)
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const heatmapData = []
    
    for (let i = 0; i < days.length; i++) {
      for (let j = 0; j < hours.length; j++) {
        heatmapData.push([j, i, Math.floor(Math.random() * 100)])
      }
    }
    
    const heatmapOption = {
      tooltip: {
        position: 'top'
      },
      grid: {
        height: '60%',
        top: '10%'
      },
      xAxis: {
        type: 'category',
        data: hours,
        splitArea: {
          show: true
        },
        axisLabel: {
          fontSize: 10
        }
      },
      yAxis: {
        type: 'category',
        data: days,
        splitArea: {
          show: true
        },
        axisLabel: {
          fontSize: 10
        }
      },
      visualMap: {
        min: 0,
        max: 100,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '5%',
        textStyle: {
          fontSize: 10
        }
      },
      series: [{
        type: 'heatmap',
        data: heatmapData,
        label: {
          show: false
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    }
    
    activityHeatmapChart.setOption(heatmapOption)
  }
}

const resizeCharts = () => {
  abilityRadarChart?.resize()
  learningTrendChart?.resize()
  classAbilityChart?.resize()
  activityHeatmapChart?.resize()
}

// 监听选中学生变化
watch(selectedStudent, (newStudent) => {
  if (newStudent) {
    nextTick(() => {
      initStudentCharts()
    })
  }
})

// 生命周期
onMounted(async () => {
  await teacherStore.fetchStudents()
  
  nextTick(() => {
    initClassCharts()
  })
  
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  abilityRadarChart?.dispose()
  learningTrendChart?.dispose()
  classAbilityChart?.dispose()
  activityHeatmapChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script> 