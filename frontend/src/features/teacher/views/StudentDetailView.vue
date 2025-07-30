<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
              <router-link to="/teacher/students" class="hover:text-gray-700 dark:hover:text-gray-200">
                学生管理
              </router-link>
              <chevron-right-icon class="w-4 h-4" />
              <span>{{ student.name }}</span>
            </nav>
            <div class="flex items-center space-x-4">
              <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ student.name }}</h1>
              <badge :variant="getActivityBadgeVariant(student.activityLevel)">
                {{ getActivityText(student.activityLevel) }}
              </badge>
              <badge variant="secondary">{{ student.studentId }}</badge>
            </div>
            <p class="text-gray-600 dark:text-gray-400 mt-2">
              {{ student.department }} • 入学时间：{{ formatDate(student.enrollmentDate) }}
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="exportStudentReport">
              <document-arrow-down-icon class="w-4 h-4 mr-2" />
              导出报告
            </button>
            <button variant="outline" @click="sendMessage">
              <chat-bubble-left-icon class="w-4 h-4 mr-2" />
              发送消息
            </button>
            <button variant="primary" @click="showEditStudent = true">
              <pencil-icon class="w-4 h-4 mr-2" />
              编辑信息
            </button>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-4 gap-8">
        <!-- 左侧主要内容 -->
        <div class="xl:col-span-3 space-y-8">
          <!-- 学生概览 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学生概览</h2>
            </template>
            
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div class="text-center">
                <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
                  {{ student.stats.totalCourses }}
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">参与课程</p>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
                  {{ student.stats.completedCourses }}
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">完成课程</p>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
                  {{ student.stats.averageGrade.toFixed(1) }}
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">平均成绩</p>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
                  {{ student.stats.totalStudyTime }}h
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400">总学习时长</p>
              </div>
            </div>
          </card>

          <!-- 学习进度图表 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学习进度分析</h2>
                <select v-model="progressTimeRange" @change="updateProgressChart" class="input input-sm">
                  <option value="30d">最近30天</option>
                  <option value="90d">最近3个月</option>
                  <option value="180d">最近6个月</option>
                  <option value="1y">最近1年</option>
                </select>
              </div>
            </template>
            <div ref="progressChartRef" class="h-80 w-full"></div>
          </card>

          <!-- 课程学习记录 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">课程学习记录</h2>
                <div class="flex items-center space-x-2">
                  <select v-model="courseFilter" class="input input-sm">
                    <option value="">全部课程</option>
                    <option value="completed">已完成</option>
                    <option value="in-progress">进行中</option>
                    <option value="not-started">未开始</option>
                  </select>
                </div>
              </div>
            </template>
            
            <div class="space-y-4">
              <div v-for="course in filteredCourses" :key="course.id" 
                   class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
                <div class="flex items-center justify-between mb-3">
                  <div class="flex items-center space-x-3">
                    <img :src="course.coverUrl || '/api/placeholder/60/60'" 
                         :alt="course.name" 
                         class="w-12 h-12 rounded object-cover" />
                    <div>
                      <h3 class="font-medium text-gray-900 dark:text-white">{{ course.name }}</h3>
                      <p class="text-sm text-gray-500 dark:text-gray-400">{{ course.instructor }}</p>
                    </div>
                  </div>
                  <div class="flex items-center space-x-4">
                    <badge :variant="getCourseStatusVariant(course.status)">
                      {{ getCourseStatusText(course.status) }}
                    </badge>
                    <button variant="outline" size="sm" @click="viewCourseDetail(course.id)">
                      查看详情
                    </button>
                  </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-3">
                  <div>
                    <span class="text-sm text-gray-500 dark:text-gray-400">进度：</span>
                    <span class="font-medium">{{ course.progress }}%</span>
                  </div>
                  <div>
                    <span class="text-sm text-gray-500 dark:text-gray-400">成绩：</span>
                    <span class="font-medium">{{ course.grade || '--' }}</span>
                  </div>
                  <div>
                    <span class="text-sm text-gray-500 dark:text-gray-400">学习时长：</span>
                    <span class="font-medium">{{ course.studyTime }}h</span>
                  </div>
                  <div>
                    <span class="text-sm text-gray-500 dark:text-gray-400">最后学习：</span>
                    <span class="font-medium">{{ formatRelativeTime(course.lastStudyAt) }}</span>
                  </div>
                </div>
                
                <progress :value="course.progress" :max="100" size="sm" />
              </div>
            </div>
          </card>

          <!-- 成绩分析 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">成绩分析</h2>
            </template>
            
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <!-- 成绩趋势图 -->
              <div>
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">成绩趋势</h3>
                <div ref="gradesTrendRef" class="h-60 w-full"></div>
              </div>
              
              <!-- 成绩分布 -->
              <div>
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">成绩分布</h3>
                <div ref="gradesDistributionRef" class="h-60 w-full"></div>
              </div>
            </div>
            
            <!-- 详细成绩列表 -->
            <div class="mt-6">
              <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">近期成绩记录</h3>
              <div class="overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
                  <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">作业/考试</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">课程</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">成绩</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">提交时间</th>
                      <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">操作</th>
                    </tr>
                  </thead>
                  <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-600">
                    <tr v-for="grade in recentGrades" :key="grade.id">
                      <td class="px-4 py-3 text-sm font-medium text-gray-900 dark:text-white">
                        {{ grade.assignmentName }}
                      </td>
                      <td class="px-4 py-3 text-sm text-gray-500 dark:text-gray-400">
                        {{ grade.courseName }}
                      </td>
                      <td class="px-4 py-3">
                        <div class="flex items-center space-x-2">
                          <span class="text-sm font-medium">{{ grade.score }}/{{ grade.totalScore }}</span>
                          <badge :variant="getGradeBadgeVariant(grade.percentage)" size="sm">
                            {{ getGradeLevel(grade.percentage) }}
                          </badge>
                        </div>
                      </td>
                      <td class="px-4 py-3 text-sm text-gray-500 dark:text-gray-400">
                        {{ formatDate(grade.submittedAt) }}
                      </td>
                      <td class="px-4 py-3">
                        <button variant="ghost" size="sm" @click="viewGradeDetail(grade.id)">
                          查看详情
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </card>

          <!-- 能力分析 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">能力分析</h2>
            </template>
            
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <!-- 能力雷达图 -->
              <div>
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">能力雷达图</h3>
                <div ref="abilityRadarRef" class="h-80 w-full"></div>
              </div>
              
              <!-- 能力发展趋势 -->
              <div>
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">能力发展趋势</h3>
                <div ref="abilityTrendRef" class="h-80 w-full"></div>
              </div>
            </div>
            
            <!-- 能力详细评估 -->
            <div class="mt-6">
              <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">详细能力评估</h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="ability in student.abilities" :key="ability.name" 
                     class="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                  <div class="flex justify-between items-center mb-2">
                    <span class="text-sm font-medium text-gray-900 dark:text-white">{{ ability.name }}</span>
                    <span class="text-lg font-bold text-primary-600">{{ ability.score }}</span>
                  </div>
                  <progress :value="ability.score" :max="100" size="sm" class="mb-2" />
                  <p class="text-xs text-gray-600 dark:text-gray-400">{{ ability.description }}</p>
                  <div class="mt-2 flex justify-between items-center">
                    <span class="text-xs text-gray-500">上次评估：{{ formatDate(ability.lastAssessed) }}</span>
                    <badge 
                      :variant="ability.trend === 'up' ? 'success' : ability.trend === 'down' ? 'danger' : 'secondary'" 
                      size="sm"
                    >
                      {{ ability.trend === 'up' ? '↗ 提升' : ability.trend === 'down' ? '↘ 下降' : '→ 稳定' }}
                    </badge>
                  </div>
                </div>
              </div>
            </div>
          </card>

          <!-- 学习活动记录 -->
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学习活动记录</h2>
            </template>
            
            <div class="space-y-4">
              <div v-for="activity in learningActivities" :key="activity.id"
                   class="flex items-start space-x-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center"
                       :class="getActivityIconBg(activity.type)">
                    <component :is="getActivityIcon(activity.type)" class="w-4 h-4 text-white" />
                  </div>
                </div>
                <div class="flex-1">
                  <div class="flex items-center justify-between">
                    <h4 class="text-sm font-medium text-gray-900 dark:text-white">{{ activity.title }}</h4>
                    <span class="text-xs text-gray-500 dark:text-gray-400">{{ formatRelativeTime(activity.timestamp) }}</span>
                  </div>
                  <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ activity.description }}</p>
                  <div class="flex items-center space-x-4 mt-2 text-xs text-gray-500 dark:text-gray-400">
                    <span>课程：{{ activity.courseName }}</span>
                    <span v-if="activity.duration">时长：{{ activity.duration }}分钟</span>
                    <span v-if="activity.score">得分：{{ activity.score }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="mt-4 text-center">
              <button variant="outline" @click="loadMoreActivities">
                加载更多活动记录
              </button>
            </div>
          </card>
        </div>

        <!-- 右侧信息面板 -->
        <div class="space-y-6">
          <!-- 学生基本信息 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">基本信息</h3>
            </template>
            
            <div class="text-center mb-6">
              <div class="w-20 h-20 mx-auto mb-4">
                <img v-if="student.avatar" :src="student.avatar" :alt="student.name" 
                     class="w-20 h-20 rounded-full object-cover" />
                <div v-else class="w-20 h-20 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                  <user-icon class="w-10 h-10 text-gray-400" />
                </div>
              </div>
              <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ student.name }}</h3>
              <p class="text-sm text-gray-500 dark:text-gray-400">{{ student.email }}</p>
            </div>
            
            <div class="space-y-3 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">学号：</span>
                <span class="font-medium">{{ student.studentId }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">专业：</span>
                <span class="font-medium">{{ student.major }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">班级：</span>
                <span class="font-medium">{{ student.className }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">年级：</span>
                <span class="font-medium">{{ student.grade }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">手机：</span>
                <span class="font-medium">{{ student.phone || '--' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">入学时间：</span>
                <span class="font-medium">{{ formatDate(student.enrollmentDate) }}</span>
              </div>
            </div>
          </card>

          <!-- 学习统计 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习统计</h3>
            </template>
            
            <div class="space-y-4">
              <div class="text-center p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                <div class="text-lg font-bold text-blue-600 dark:text-blue-400">{{ student.stats.weeklyStudyTime }}h</div>
                <div class="text-xs text-blue-600 dark:text-blue-400">本周学习时长</div>
              </div>
              
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ student.stats.completionRate }}%</div>
                  <div class="text-gray-500 dark:text-gray-400">完成率</div>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ student.stats.rank }}</div>
                  <div class="text-gray-500 dark:text-gray-400">班级排名</div>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ student.stats.streakDays }}</div>
                  <div class="text-gray-500 dark:text-gray-400">连续天数</div>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ student.stats.submissions }}</div>
                  <div class="text-gray-500 dark:text-gray-400">作业提交</div>
                </div>
              </div>
            </div>
          </card>

          <!-- 快速操作 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快速操作</h3>
            </template>
            
            <div class="space-y-3">
              <button variant="outline" class="w-full justify-start" @click="viewAllCourses">
                <book-open-icon class="w-4 h-4 mr-3" />
                查看所有课程
              </button>
              <button variant="outline" class="w-full justify-start" @click="viewAllGrades">
                <academic-cap-icon class="w-4 h-4 mr-3" />
                查看所有成绩
              </button>
              <button variant="outline" class="w-full justify-start" @click="generateReport">
                <document-text-icon class="w-4 h-4 mr-3" />
                生成学习报告
              </button>
              <button variant="outline" class="w-full justify-start" @click="resetProgress">
                <arrow-path-icon class="w-4 h-4 mr-3" />
                重置学习进度
              </button>
              <button variant="outline" class="w-full justify-start" @click="managePermissions">
                <shield-check-icon class="w-4 h-4 mr-3" />
                管理权限
              </button>
            </div>
          </card>

          <!-- 最近消息 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">最近消息</h3>
            </template>
            
            <div class="space-y-3">
              <div v-for="message in recentMessages" :key="message.id"
                   class="p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                <p class="text-sm text-gray-900 dark:text-white">{{ message.content }}</p>
                <div class="flex justify-between items-center mt-2">
                  <span class="text-xs text-gray-500 dark:text-gray-400">{{ formatRelativeTime(message.timestamp) }}</span>
                  <badge :variant="message.from === 'student' ? 'secondary' : 'primary'" size="sm">
                    {{ message.from === 'student' ? '学生' : '教师' }}
                  </badge>
                </div>
              </div>
            </div>
            
            <div class="mt-4">
              <button variant="outline" size="sm" class="w-full" @click="viewAllMessages">
                查看所有消息
              </button>
            </div>
          </card>
        </div>
      </div>
    </div>

    <!-- 编辑学生信息弹窗 -->
    <div v-if="showEditStudent" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">编辑学生信息</h3>
        <form @submit.prevent="updateStudent" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">姓名</label>
            <input v-model="editForm.name" type="text" class="input" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">邮箱</label>
            <input v-model="editForm.email" type="email" class="input" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">手机</label>
            <input v-model="editForm.phone" type="tel" class="input" />
          </div>
          <div class="flex space-x-3">
            <button type="button" variant="outline" @click="showEditStudent = false" class="flex-1">
              取消
            </button>
            <button type="submit" variant="primary" class="flex-1" :loading="isUpdating">
              保存
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import {
  ChevronRightIcon,
  DocumentArrowDownIcon,
  ChatBubbleLeftIcon,
  PencilIcon,
  UserIcon,
  BookOpenIcon,
  AcademicCapIcon,
  DocumentTextIcon,
  ArrowPathIcon,
  ShieldCheckIcon,
  PlayIcon,
  DocumentIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const studentId = route.params.id as string
const progressTimeRange = ref('30d')
const courseFilter = ref('')
const showEditStudent = ref(false)
const isUpdating = ref(false)

// 图表引用
const progressChartRef = ref<HTMLElement>()
const gradesTrendRef = ref<HTMLElement>()
const gradesDistributionRef = ref<HTMLElement>()
const abilityRadarRef = ref<HTMLElement>()
const abilityTrendRef = ref<HTMLElement>()

let progressChart: echarts.ECharts | null = null
let gradesTrendChart: echarts.ECharts | null = null
let gradesDistributionChart: echarts.ECharts | null = null
let abilityRadarChart: echarts.ECharts | null = null
let abilityTrendChart: echarts.ECharts | null = null

// 学生数据
const student = reactive({
  id: studentId,
  name: '张同学',
  studentId: '2021001001',
  email: 'zhang@example.com',
  phone: '13800138000',
  avatar: '',
  major: '计算机科学与技术',
  department: '计算机学院',
  className: '计算机科学1班',
  grade: '2021级',
  enrollmentDate: '2021-09-01',
  activityLevel: 'high',
  stats: {
    totalCourses: 8,
    completedCourses: 5,
    averageGrade: 87.5,
    totalStudyTime: 245,
    weeklyStudyTime: 12,
    completionRate: 82,
    rank: 5,
    streakDays: 15,
    submissions: 28
  },
  abilities: [
    { name: '逻辑思维', score: 88, description: '逻辑推理和分析能力', lastAssessed: '2024-01-10', trend: 'up' },
    { name: '创新能力', score: 76, description: '创新思维和解决问题能力', lastAssessed: '2024-01-08', trend: 'stable' },
    { name: '沟通协作', score: 92, description: '团队协作和沟通能力', lastAssessed: '2024-01-12', trend: 'up' },
    { name: '学习能力', score: 85, description: '自主学习和知识吸收能力', lastAssessed: '2024-01-09', trend: 'stable' },
    { name: '实践应用', score: 79, description: '理论知识实际应用能力', lastAssessed: '2024-01-11', trend: 'down' },
    { name: '批判思维', score: 83, description: '批判性思维和分析能力', lastAssessed: '2024-01-07', trend: 'up' }
  ]
})

// 课程学习记录
const courses = ref([
  {
    id: '1',
    name: '高等数学',
    instructor: '王教授',
    coverUrl: '',
    progress: 95,
    grade: 92,
    studyTime: 45,
    status: 'completed',
    lastStudyAt: '2024-01-14T16:30:00Z'
  },
  {
    id: '2',
    name: '数据结构与算法',
    instructor: '李教授',
    coverUrl: '',
    progress: 78,
    grade: 85,
    studyTime: 38,
    status: 'in-progress',
    lastStudyAt: '2024-01-15T10:15:00Z'
  },
  {
    id: '3',
    name: '计算机网络',
    instructor: '张教授',
    coverUrl: '',
    progress: 60,
    grade: null,
    studyTime: 25,
    status: 'in-progress',
    lastStudyAt: '2024-01-13T14:20:00Z'
  }
])

// 近期成绩
const recentGrades = ref([
  {
    id: '1',
    assignmentName: '数据结构作业3',
    courseName: '数据结构与算法',
    score: 85,
    totalScore: 100,
    percentage: 85,
    submittedAt: '2024-01-12T18:00:00Z'
  },
  {
    id: '2',
    assignmentName: '网络协议分析',
    courseName: '计算机网络',
    score: 78,
    totalScore: 100,
    percentage: 78,
    submittedAt: '2024-01-10T16:30:00Z'
  },
  {
    id: '3',
    assignmentName: '高数期中考试',
    courseName: '高等数学',
    score: 92,
    totalScore: 100,
    percentage: 92,
    submittedAt: '2024-01-08T14:00:00Z'
  }
])

// 学习活动记录
const learningActivities = ref([
  {
    id: '1',
    type: 'lesson',
    title: '完成课时学习',
    description: '学习了《树的遍历算法》',
    courseName: '数据结构与算法',
    duration: 45,
    timestamp: '2024-01-15T10:15:00Z'
  },
  {
    id: '2',
    type: 'assignment',
    title: '提交作业',
    description: '提交了数据结构作业3',
    courseName: '数据结构与算法',
    score: 85,
    timestamp: '2024-01-12T18:00:00Z'
  },
  {
    id: '3',
    type: 'quiz',
    title: '完成测验',
    description: '完成了网络协议测验',
    courseName: '计算机网络',
    score: 78,
    timestamp: '2024-01-10T16:30:00Z'
  }
])

// 最近消息
const recentMessages = ref([
  {
    id: '1',
    content: '老师，我对今天的作业有一些疑问...',
    from: 'student',
    timestamp: '2024-01-15T09:30:00Z'
  },
  {
    id: '2',
    content: '你的作业完成得很好，继续保持！',
    from: 'teacher',
    timestamp: '2024-01-14T16:20:00Z'
  }
])

// 编辑表单
const editForm = reactive({
  name: '',
  email: '',
  phone: ''
})

// 计算属性
const filteredCourses = computed(() => {
  if (!courseFilter.value) return courses.value
  return courses.value.filter(course => course.status === courseFilter.value)
})

// 方法
const getActivityBadgeVariant = (level: string) => {
  switch (level) {
    case 'high': return 'success'
    case 'medium': return 'warning'
    case 'low': return 'danger'
    default: return 'secondary'
  }
}

const getActivityText = (level: string) => {
  switch (level) {
    case 'high': return '高活跃'
    case 'medium': return '中等活跃'
    case 'low': return '低活跃'
    case 'inactive': return '不活跃'
    default: return '未知'
  }
}

const getCourseStatusVariant = (status: string) => {
  switch (status) {
    case 'completed': return 'success'
    case 'in-progress': return 'primary'
    case 'not-started': return 'secondary'
    default: return 'secondary'
  }
}

const getCourseStatusText = (status: string) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'in-progress': return '进行中'
    case 'not-started': return '未开始'
    default: return '未知'
  }
}

const getGradeBadgeVariant = (grade: number) => {
  if (grade >= 90) return 'success'
  if (grade >= 80) return 'primary'
  if (grade >= 70) return 'warning'
  return 'danger'
}

const getGradeLevel = (grade: number) => {
  if (grade >= 90) return '优秀'
  if (grade >= 80) return '良好'
  if (grade >= 70) return '中等'
  return '需提高'
}

const getActivityIcon = (type: string) => {
  switch (type) {
    case 'lesson': return PlayIcon
    case 'assignment': return DocumentIcon
    case 'quiz': return CheckCircleIcon
    default: return DocumentIcon
  }
}

const getActivityIconBg = (type: string) => {
  switch (type) {
    case 'lesson': return 'bg-blue-500'
    case 'assignment': return 'bg-green-500'
    case 'quiz': return 'bg-yellow-500'
    default: return 'bg-gray-500'
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatRelativeTime = (timestamp: string) => {
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

// 图表初始化
const initCharts = () => {
  initProgressChart()
  initGradesTrendChart()
  initGradesDistributionChart()
  initAbilityRadarChart()
  initAbilityTrendChart()
}

const initProgressChart = () => {
  if (!progressChartRef.value) return

  progressChart = echarts.init(progressChartRef.value)
  
  const option = {
    title: { show: false },
    tooltip: { trigger: 'axis' },
    legend: { data: ['学习时长', '完成课时'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['第1周', '第2周', '第3周', '第4周'],
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      splitLine: { lineStyle: { color: '#f3f4f6' } }
    },
    series: [
      {
        name: '学习时长',
        type: 'line',
        data: [8, 12, 15, 12],
        lineStyle: { color: '#3b82f6', width: 3 },
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '完成课时',
        type: 'bar',
        data: [5, 8, 10, 7],
        itemStyle: { color: '#10b981' }
      }
    ]
  }
  
  progressChart.setOption(option)
}

const initGradesTrendChart = () => {
  if (!gradesTrendRef.value) return

  gradesTrendChart = echarts.init(gradesTrendRef.value)
  
  const option = {
    title: { show: false },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['作业1', '作业2', '作业3', '测验1', '期中'],
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      splitLine: { lineStyle: { color: '#f3f4f6' } }
    },
    series: [{
      type: 'line',
      data: [82, 85, 78, 88, 92],
      lineStyle: { color: '#f59e0b', width: 3 },
      itemStyle: { color: '#f59e0b' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 158, 11, 0.3)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.05)' }
          ]
        }
      }
    }]
  }
  
  gradesTrendChart.setOption(option)
}

const initGradesDistributionChart = () => {
  if (!gradesDistributionRef.value) return

  gradesDistributionChart = echarts.init(gradesDistributionRef.value)
  
  const option = {
    title: { show: false },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 3, name: '优秀(90+)', itemStyle: { color: '#10b981' } },
        { value: 5, name: '良好(80-89)', itemStyle: { color: '#3b82f6' } },
        { value: 2, name: '中等(70-79)', itemStyle: { color: '#f59e0b' } },
        { value: 0, name: '需提高(<70)', itemStyle: { color: '#ef4444' } }
      ],
      label: { show: true, formatter: '{b}: {c}' }
    }]
  }
  
  gradesDistributionChart.setOption(option)
}

const initAbilityRadarChart = () => {
  if (!abilityRadarRef.value) return

  abilityRadarChart = echarts.init(abilityRadarRef.value)
  
  const option = {
    title: { show: false },
    tooltip: {},
    radar: {
      indicator: student.abilities.map(ability => ({
        name: ability.name,
        max: 100
      }))
    },
    series: [{
      type: 'radar',
      data: [{
        value: student.abilities.map(ability => ability.score),
        name: '能力评估',
        itemStyle: { color: '#3b82f6' },
        areaStyle: { color: 'rgba(59, 130, 246, 0.3)' }
      }]
    }]
  }
  
  abilityRadarChart.setOption(option)
}

const initAbilityTrendChart = () => {
  if (!abilityTrendRef.value) return

  abilityTrendChart = echarts.init(abilityTrendRef.value)
  
  const option = {
    title: { show: false },
    tooltip: { trigger: 'axis' },
    legend: { data: ['逻辑思维', '创新能力', '沟通协作'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['9月', '10月', '11月', '12月', '1月'],
      axisLine: { lineStyle: { color: '#e5e7eb' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      splitLine: { lineStyle: { color: '#f3f4f6' } }
    },
    series: [
      {
        name: '逻辑思维',
        type: 'line',
        data: [75, 78, 82, 85, 88],
        lineStyle: { color: '#3b82f6', width: 2 },
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '创新能力',
        type: 'line',
        data: [70, 72, 74, 75, 76],
        lineStyle: { color: '#10b981', width: 2 },
        itemStyle: { color: '#10b981' }
      },
      {
        name: '沟通协作',
        type: 'line',
        data: [85, 87, 89, 91, 92],
        lineStyle: { color: '#f59e0b', width: 2 },
        itemStyle: { color: '#f59e0b' }
      }
    ]
  }
  
  abilityTrendChart.setOption(option)
}

const updateProgressChart = () => {
  initProgressChart()
}

// 操作方法
const exportStudentReport = async () => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '导出中...',
      message: '正在生成学生报告'
    })
    
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    uiStore.showNotification({
      type: 'success',
      title: '导出成功',
      message: '学生报告已下载到本地'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出报告时发生错误'
    })
  }
}

const sendMessage = () => {
  router.push(`/teacher/messages/compose?to=${student.id}`)
}

const updateStudent = async () => {
  isUpdating.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    Object.assign(student, editForm)
    
    uiStore.showNotification({
      type: 'success',
      title: '更新成功',
      message: '学生信息已更新'
    })
    
    showEditStudent.value = false
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '更新失败',
      message: '更新学生信息时发生错误'
    })
  } finally {
    isUpdating.value = false
  }
}

const viewCourseDetail = (courseId: string) => {
  router.push(`/teacher/courses/${courseId}`)
}

const viewGradeDetail = (gradeId: string) => {
  router.push(`/teacher/assignments/${gradeId}/grade`)
}

const loadMoreActivities = () => {
  uiStore.showNotification({
    type: 'info',
    title: '加载更多',
    message: '功能开发中...'
  })
}

const resizeCharts = () => {
  progressChart?.resize()
  gradesTrendChart?.resize()
  gradesDistributionChart?.resize()
  abilityRadarChart?.resize()
  abilityTrendChart?.resize()
}

// 生命周期
onMounted(() => {
  // 初始化编辑表单
  Object.assign(editForm, {
    name: student.name,
    email: student.email,
    phone: student.phone
  })
  
  nextTick(() => {
    initCharts()
  })
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  progressChart?.dispose()
  gradesTrendChart?.dispose()
  gradesDistributionChart?.dispose()
  abilityRadarChart?.dispose()
  abilityTrendChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script> 