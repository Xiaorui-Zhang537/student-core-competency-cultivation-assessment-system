<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">报告中心</h1>
            <p class="text-gray-600 dark:text-gray-400">生成和导出各类教学分析报告</p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="showReportHistory">
              <clock-icon class="w-4 h-4 mr-2" />
              历史报告
            </button>
            <button variant="primary" @click="showCustomReportModal = true">
              <document-plus-icon class="w-4 h-4 mr-2" />
              自定义报告
            </button>
          </div>
        </div>
      </div>

      <!-- 报告统计 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
            {{ stats.totalReports }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总报告数</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
            {{ stats.thisMonth }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">本月生成</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
            {{ stats.downloads }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">下载次数</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
            {{ stats.shared }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">分享次数</p>
        </card>
      </div>

      <!-- 快速生成报告 -->
      <card padding="lg" class="mb-8">
        <template #header>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">快速生成报告</h2>
        </template>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="template in reportTemplates"
            :key="template.id"
            @click="generateReport(template)"
            class="p-6 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors group"
          >
            <div class="flex items-start space-x-4">
              <div class="flex-shrink-0">
                <div class="w-12 h-12 rounded-lg flex items-center justify-center"
                     :class="template.iconBg">
                  <component :is="template.icon" class="w-6 h-6 text-white" />
                </div>
              </div>
              <div class="flex-1">
                <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ template.title }}</h3>
                <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ template.description }}</p>
                <div class="flex items-center space-x-4 text-xs text-gray-500">
                  <span>{{ template.estimatedTime }}</span>
                  <span>{{ template.lastGenerated }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </card>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 左侧报告配置 -->
        <div class="lg:col-span-2 space-y-8">
          <!-- 报告筛选器 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">报告筛选器</h3>
            </template>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  时间范围
                </label>
                <select v-model="reportFilters.timeRange" class="input">
                  <option value="1week">最近一周</option>
                  <option value="1month">最近一月</option>
                  <option value="3months">最近三月</option>
                  <option value="semester">本学期</option>
                  <option value="year">本学年</option>
                  <option value="custom">自定义</option>
                </select>
                
                <div v-if="reportFilters.timeRange === 'custom'" class="grid grid-cols-2 gap-4 mt-4">
                  <div>
                    <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">开始日期</label>
                    <input v-model="reportFilters.startDate" type="date" class="input input-sm" />
                  </div>
                  <div>
                    <label class="block text-sm text-gray-600 dark:text-gray-400 mb-1">结束日期</label>
                    <input v-model="reportFilters.endDate" type="date" class="input input-sm" />
                  </div>
                </div>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  选择课程
                </label>
                <select v-model="reportFilters.course" class="input">
                  <option value="">全部课程</option>
                  <option v-for="course in courses" :key="course.id" :value="course.id">
                    {{ course.title }}
                  </option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  选择班级
                </label>
                <select v-model="reportFilters.class" class="input">
                  <option value="">全部班级</option>
                  <option v-for="cls in classes" :key="cls.id" :value="cls.id">
                    {{ cls.name }}
                  </option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  报告格式
                </label>
                <select v-model="reportFilters.format" class="input">
                  <option value="pdf">PDF</option>
                  <option value="excel">Excel</option>
                  <option value="word">Word</option>
                  <option value="html">HTML</option>
                </select>
              </div>
            </div>
          </card>

          <!-- 报告内容选择 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">报告内容</h3>
            </template>
            
            <div class="space-y-4">
              <div v-for="section in reportSections" :key="section.id">
                <label class="flex items-start space-x-3">
                  <input
                    v-model="section.enabled"
                    type="checkbox"
                    class="mt-1 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <div class="flex-1">
                    <span class="font-medium text-gray-900 dark:text-white">{{ section.title }}</span>
                    <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">{{ section.description }}</p>
                    
                    <!-- 子选项 -->
                    <div v-if="section.enabled && section.options" class="ml-4 mt-3 space-y-2">
                      <label
                        v-for="option in section.options"
                        :key="option.id"
                        class="flex items-center space-x-2"
                      >
                        <input
                          v-model="option.enabled"
                          type="checkbox"
                          class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        />
                        <span class="text-sm text-gray-700 dark:text-gray-300">{{ option.title }}</span>
                      </label>
                    </div>
                  </div>
                </label>
              </div>
            </div>
            
            <div class="mt-6 flex justify-end space-x-3">
              <button variant="outline" @click="previewReport">
                <eye-icon class="w-4 h-4 mr-2" />
                预览报告
              </button>
              <button variant="primary" @click="generateCustomReport" :loading="isGenerating">
                <document-arrow-down-icon class="w-4 h-4 mr-2" />
                生成报告
              </button>
            </div>
          </card>
        </div>

        <!-- 右侧报告列表 -->
        <div class="lg:col-span-1 space-y-6">
          <!-- 最近报告 -->
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">最近报告</h3>
                <button variant="ghost" size="sm" @click="showAllReports">
                  查看全部
                </button>
              </div>
            </template>
            
            <div class="space-y-3">
              <div
                v-for="report in recentReports"
                :key="report.id"
                class="p-3 border border-gray-200 dark:border-gray-600 rounded-lg"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1">
                    <h4 class="font-medium text-gray-900 dark:text-white text-sm">{{ report.title }}</h4>
                    <p class="text-xs text-gray-500 mt-1">{{ report.createdAt }}</p>
                    <div class="flex items-center space-x-2 mt-2">
                      <badge :variant="getReportStatusVariant(report.status)" size="sm">
                        {{ getReportStatusText(report.status) }}
                      </badge>
                      <span class="text-xs text-gray-500">{{ report.format.toUpperCase() }}</span>
                    </div>
                  </div>
                  <div class="flex items-center space-x-1">
                    <button
                      v-if="report.status === 'completed'"
                      variant="ghost"
                      size="sm"
                      @click="downloadReport(report)"
                      class="p-1"
                    >
                      <arrow-down-tray-icon class="w-4 h-4" />
                    </button>
                    <button
                      variant="ghost"
                      size="sm"
                      @click="deleteReport(report)"
                      class="p-1 text-red-600"
                    >
                      <trash-icon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </card>

          <!-- 报告模板管理 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">模板管理</h3>
            </template>
            
            <div class="space-y-2">
              <button variant="outline" size="sm" class="w-full justify-start" @click="createTemplate">
                <document-plus-icon class="w-4 h-4 mr-2" />
                创建模板
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="importTemplate">
                <arrow-up-tray-icon class="w-4 h-4 mr-2" />
                导入模板
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="manageTemplates">
                <cog-icon class="w-4 h-4 mr-2" />
                管理模板
              </button>
            </div>
          </card>

          <!-- 快速统计 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">本周统计</h3>
            </template>
            
            <div class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">活跃学生:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ weekStats.activeStudents }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">提交作业:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ weekStats.submissions }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">平均成绩:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ weekStats.averageScore }}分</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">课程完成率:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ weekStats.completionRate }}%</span>
              </div>
            </div>
          </card>
        </div>
      </div>

      <!-- 自定义报告弹窗 -->
      <div v-if="showCustomReportModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-2xl w-full mx-4 max-h-96 overflow-y-auto">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">创建自定义报告</h3>
          <form @submit.prevent="createCustomReport" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                报告名称
              </label>
              <input
                v-model="customReport.name"
                type="text"
                placeholder="输入报告名称"
                class="input"
                required
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                报告描述
              </label>
              <textarea
                v-model="customReport.description"
                rows="3"
                placeholder="描述报告内容和用途"
                class="input"
              ></textarea>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  数据源
                </label>
                <select v-model="customReport.dataSource" class="input">
                  <option value="courses">课程数据</option>
                  <option value="students">学生数据</option>
                  <option value="assignments">作业数据</option>
                  <option value="assessments">评估数据</option>
                </select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  生成频率
                </label>
                <select v-model="customReport.frequency" class="input">
                  <option value="once">一次性</option>
                  <option value="daily">每日</option>
                  <option value="weekly">每周</option>
                  <option value="monthly">每月</option>
                </select>
              </div>
            </div>
            
            <div class="flex justify-end space-x-3">
              <button variant="outline" @click="showCustomReportModal = false">
                取消
              </button>
              <button type="submit" variant="primary">
                创建
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 报告预览弹窗 -->
      <div v-if="showPreviewModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-4xl w-full mx-4 max-h-96 overflow-y-auto">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">报告预览</h3>
            <button variant="ghost" @click="showPreviewModal = false">
              <x-mark-icon class="w-5 h-5" />
            </button>
          </div>
          
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6">
            <h4 class="text-center text-xl font-bold text-gray-900 dark:text-white mb-6">
              {{ previewData.title }}
            </h4>
            
            <div class="space-y-6">
              <div v-for="section in previewData.sections" :key="section.id">
                <h5 class="text-lg font-semibold text-gray-900 dark:text-white mb-3">{{ section.title }}</h5>
                <div class="text-sm text-gray-600 dark:text-gray-400">
                  {{ section.content }}
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end space-x-3">
            <button variant="outline" @click="showPreviewModal = false">
              关闭
            </button>
            <button variant="primary" @click="confirmGeneration">
              确认生成
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  ClockIcon,
  DocumentPlusIcon,
  EyeIcon,
  DocumentArrowDownIcon,
  ArrowDownTrayIcon,
  TrashIcon,
  ArrowUpTrayIcon,
  CogIcon,
  XMarkIcon,
  ChartBarIcon,
  UserGroupIcon,
  AcademicCapIcon,
  ClipboardDocumentListIcon
} from '@heroicons/vue/24/outline'

// Stores
const uiStore = useUIStore()

// 状态
const showCustomReportModal = ref(false)
const showPreviewModal = ref(false)
const isGenerating = ref(false)

// 统计数据
const stats = reactive({
  totalReports: 156,
  thisMonth: 23,
  downloads: 847,
  shared: 34
})

// 报告筛选器
const reportFilters = reactive({
  timeRange: '1month',
  startDate: '',
  endDate: '',
  course: '',
  class: '',
  format: 'pdf'
})

// 自定义报告
const customReport = reactive({
  name: '',
  description: '',
  dataSource: 'courses',
  frequency: 'once'
})

// 预览数据
const previewData = ref<{
  title: string
  sections: { id: string; title: string; content: string }[]
}>({
  title: '',
  sections: []
})

// 本周统计
const weekStats = reactive({
  activeStudents: 145,
  submissions: 268,
  averageScore: 82.5,
  completionRate: 76
})

// 报告模板
const reportTemplates = ref([
  {
    id: '1',
    title: '学生成绩报告',
    description: '生成详细的学生成绩分析和统计报告',
    icon: ChartBarIcon,
    iconBg: 'bg-blue-500',
    estimatedTime: '2-3分钟',
    lastGenerated: '1天前'
  },
  {
    id: '2',
    title: '课程分析报告',
    description: '分析课程学习效果和学生参与度',
    icon: AcademicCapIcon,
    iconBg: 'bg-green-500',
    estimatedTime: '3-5分钟',
    lastGenerated: '3天前'
  },
  {
    id: '3',
    title: '班级表现报告',
    description: '比较不同班级的学习表现和进度',
    icon: UserGroupIcon,
    iconBg: 'bg-purple-500',
    estimatedTime: '2-4分钟',
    lastGenerated: '1周前'
  },
  {
    id: '4',
    title: '作业完成报告',
    description: '统计作业提交情况和完成质量',
    icon: ClipboardDocumentListIcon,
    iconBg: 'bg-orange-500',
    estimatedTime: '1-2分钟',
    lastGenerated: '2天前'
  }
])

// 课程列表
const courses = ref([
  { id: '1', title: '高等数学' },
  { id: '2', title: '线性代数' },
  { id: '3', title: '概率论' },
  { id: '4', title: '数据结构' }
])

// 班级列表
const classes = ref([
  { id: '1', name: '计算机1班' },
  { id: '2', name: '计算机2班' },
  { id: '3', name: '软件工程1班' },
  { id: '4', name: '网络工程1班' }
])

// 报告内容部分
const reportSections = ref([
  {
    id: '1',
    title: '基本统计信息',
    description: '包括学生数量、课程数量、完成率等基础数据',
    enabled: true,
    options: [
      { id: '1a', title: '学生总数和活跃度', enabled: true },
      { id: '1b', title: '课程完成情况', enabled: true },
      { id: '1c', title: '平均成绩统计', enabled: true }
    ]
  },
  {
    id: '2',
    title: '学习进度分析',
    description: '分析学生的学习进度和时间分布',
    enabled: true,
    options: [
      { id: '2a', title: '进度趋势图', enabled: true },
      { id: '2b', title: '学习时间分布', enabled: false },
      { id: '2c', title: '章节完成情况', enabled: true }
    ]
  },
  {
    id: '3',
    title: '成绩分析',
    description: '详细的成绩分析和排名',
    enabled: false,
    options: [
      { id: '3a', title: '成绩分布图', enabled: true },
      { id: '3b', title: '排名变化', enabled: false },
      { id: '3c', title: '优秀学生名单', enabled: true }
    ]
  },
  {
    id: '4',
    title: '问题诊断',
    description: '识别学习中的问题和困难',
    enabled: false,
    options: [
      { id: '4a', title: '薄弱知识点', enabled: true },
      { id: '4b', title: '需要帮助的学生', enabled: true },
      { id: '4c', title: '改进建议', enabled: false }
    ]
  }
])

// 最近报告
const recentReports = ref([
  {
    id: '1',
    title: '高等数学期中报告',
    status: 'completed',
    format: 'pdf',
    createdAt: '2024-01-15 14:30',
    downloadUrl: ''
  },
  {
    id: '2',
    title: '班级表现分析',
    status: 'generating',
    format: 'excel',
    createdAt: '2024-01-15 16:20',
    downloadUrl: ''
  },
  {
    id: '3',
    title: '学生成绩统计',
    status: 'completed',
    format: 'word',
    createdAt: '2024-01-14 10:15',
    downloadUrl: ''
  },
  {
    id: '4',
    title: '课程效果评估',
    status: 'failed',
    format: 'pdf',
    createdAt: '2024-01-14 09:45',
    downloadUrl: ''
  }
])

// 方法
const getReportStatusVariant = (status: string) => {
  switch (status) {
    case 'completed': return 'success'
    case 'generating': return 'warning'
    case 'failed': return 'danger'
    default: return 'secondary'
  }
}

const getReportStatusText = (status: string) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'generating': return '生成中'
    case 'failed': return '失败'
    default: return '未知'
  }
}

const generateReport = async (template: any) => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '生成中...',
      message: `正在生成${template.title}`
    })

    // 模拟生成延迟
    await new Promise(resolve => setTimeout(resolve, 3000))

    const newReport = {
      id: Date.now().toString(),
      title: template.title,
      status: 'completed',
      format: reportFilters.format,
      createdAt: new Date().toLocaleString('zh-CN'),
      downloadUrl: '#'
    }

    recentReports.value.unshift(newReport)

    uiStore.showNotification({
      type: 'success',
      title: '生成成功',
      message: `${template.title} 已生成完成`
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '生成失败',
      message: '报告生成时发生错误'
    })
  }
}

const previewReport = () => {
  const enabledSections = reportSections.value
    .filter(section => section.enabled)
    .map(section => ({
      id: section.id,
      title: section.title,
      content: `这里是${section.title}的预览内容。实际报告将包含详细的数据分析和图表。`
    }))

  previewData.value = {
    title: '自定义分析报告',
    sections: enabledSections
  }

  showPreviewModal.value = true
}

const confirmGeneration = () => {
  showPreviewModal.value = false
  generateCustomReport()
}

const generateCustomReport = async () => {
  isGenerating.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))

    const newReport = {
      id: Date.now().toString(),
      title: '自定义分析报告',
      status: 'completed',
      format: reportFilters.format,
      createdAt: new Date().toLocaleString('zh-CN'),
      downloadUrl: '#'
    }

    recentReports.value.unshift(newReport)

    uiStore.showNotification({
      type: 'success',
      title: '报告生成成功',
      message: '自定义报告已生成完成'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '生成失败',
      message: '报告生成时发生错误'
    })
  } finally {
    isGenerating.value = false
  }
}

const downloadReport = async (report: any) => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '下载中...',
      message: `正在下载 ${report.title}`
    })

    // 模拟下载延迟
    await new Promise(resolve => setTimeout(resolve, 1500))

    uiStore.showNotification({
      type: 'success',
      title: '下载完成',
      message: `${report.title} 已下载到本地`
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '下载失败',
      message: '下载报告时发生错误'
    })
  }
}

const deleteReport = (report: any) => {
  if (confirm(`确定要删除报告 "${report.title}" 吗？`)) {
    const index = recentReports.value.findIndex(r => r.id === report.id)
    if (index > -1) {
      recentReports.value.splice(index, 1)
      uiStore.showNotification({
        type: 'success',
        title: '删除成功',
        message: '报告已删除'
      })
    }
  }
}

const createCustomReport = () => {
  if (!customReport.name.trim()) {
    uiStore.showNotification({
      type: 'warning',
      title: '信息不完整',
      message: '请输入报告名称'
    })
    return
  }

  uiStore.showNotification({
    type: 'success',
    title: '创建成功',
    message: `自定义报告 "${customReport.name}" 已创建`
  })

  showCustomReportModal.value = false
  Object.assign(customReport, {
    name: '',
    description: '',
    dataSource: 'courses',
    frequency: 'once'
  })
}

const showReportHistory = () => {
  uiStore.showNotification({
    type: 'info',
    title: '历史报告',
    message: '历史报告功能开发中...'
  })
}

const showAllReports = () => {
  uiStore.showNotification({
    type: 'info',
    title: '全部报告',
    message: '查看全部报告功能开发中...'
  })
}

const createTemplate = () => {
  uiStore.showNotification({
    type: 'info',
    title: '创建模板',
    message: '创建模板功能开发中...'
  })
}

const importTemplate = () => {
  uiStore.showNotification({
    type: 'info',
    title: '导入模板',
    message: '导入模板功能开发中...'
  })
}

const manageTemplates = () => {
  uiStore.showNotification({
    type: 'info',
    title: '管理模板',
    message: '模板管理功能开发中...'
  })
}

// 生命周期
onMounted(() => {
  // 设置默认日期范围
  const today = new Date()
  const oneMonthAgo = new Date(today.getFullYear(), today.getMonth() - 1, today.getDate())
  
  reportFilters.startDate = oneMonthAgo.toISOString().split('T')[0]
  reportFilters.endDate = today.toISOString().split('T')[0]
})
</script> 