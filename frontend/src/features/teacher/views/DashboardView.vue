<template>
  <div class="p-6 min-h-screen">
    <!-- Header -->
    <div class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">工作台</h1>
        <p class="text-gray-600 dark:text-gray-400">快捷掌握课程核心指标与近期动态</p>
      </div>
      <div class="flex items-end gap-4 w-full sm:w-auto">
        <div class="w-full sm:w-64">
          <label for="course-select" class="block text-xs font-medium mb-1 text-gray-500 dark:text-gray-400">选择课程</label>
          <select id="course-select" v-model="selectedCourseId" @change="onCourseSelect" class="input">
            <option :value="null" disabled>请选择一门课程</option>
            <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
              {{ course.title }}
            </option>
          </select>
        </div>
        <div class="hidden sm:flex items-center gap-2">
          <button class="btn btn-primary inline-flex items-center" @click="goPublishAssignment">
            <span class="mr-2">＋</span>发布作业
          </button>
          <button class="btn inline-flex items-center bg-indigo-600 hover:bg-indigo-700 text-white" @click="goGradeAssignments">
            批改作业
          </button>
        </div>
      </div>
    </div>

    <!-- Course Selector -->
    

    <!-- Loading State -->
    <div v-if="teacherStore.loading" class="text-center py-12">
      <p>正在加载分析数据...</p>
    </div>

    <!-- Analytics Content -->
    <div v-else-if="selectedCourseId && courseAnalytics" class="space-y-8">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <div class="p-6 rounded-2xl bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 border border-blue-100 dark:border-blue-800 shadow-sm">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-blue-700 dark:text-blue-300">总学生数</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-blue-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c-3.866 0-7 1.79-7 4v2h14v-2c0-2.21-3.134-4-7-4zm0-2a4 4 0 100-8 4 4 0 000 8z"/></svg>
            </div>
            <div class="text-3xl font-bold text-blue-700 dark:text-blue-300">{{ safeAnalytics.totalStudents }}</div>
          </div>
          <div class="p-6 rounded-2xl bg-gradient-to-br from-emerald-50 to-emerald-100 dark:from-emerald-900/20 dark:to-emerald-800/20 border border-emerald-100 dark:border-emerald-800 shadow-sm">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-emerald-700 dark:text-emerald-300">作业完成率</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-emerald-500" viewBox="0 0 24 24" fill="currentColor"><path d="M10 15l-3.5-3.5 1.42-1.42L10 12.17l5.59-5.59L17 8l-7 7z"/></svg>
            </div>
            <div class="text-3xl font-bold text-emerald-700 dark:text-emerald-300">{{ (safeAnalytics.completionRate || 0).toFixed(1) }}%</div>
          </div>
          <div class="p-6 rounded-2xl bg-gradient-to-br from-violet-50 to-violet-100 dark:from-violet-900/20 dark:to-violet-800/20 border border-violet-100 dark:border-violet-800 shadow-sm">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-violet-700 dark:text-violet-300">平均成绩</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-violet-500" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"/></svg>
            </div>
            <div class="text-3xl font-bold text-violet-700 dark:text-violet-300">{{ (safeAnalytics.averageScore || 0).toFixed(1) }}</div>
          </div>
          <div class="p-6 rounded-2xl bg-gradient-to-br from-amber-50 to-amber-100 dark:from-amber-900/20 dark:to-amber-800/20 border border-amber-100 dark:border-amber-800 shadow-sm">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-amber-700 dark:text-amber-300">作业数量</span>
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-amber-500" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5a2 2 0 00-2 2v14l4-4h12a2 2 0 002-2V5a2 2 0 00-2-2z"/></svg>
            </div>
            <div class="text-3xl font-bold text-amber-700 dark:text-amber-300">{{ safeAnalytics.totalAssignments }}</div>
          </div>
        </div>

        <!-- Class Performance Chart -->
        <div class="card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">班级成绩分布</h2>
            <span class="text-xs text-gray-500">按成绩区间统计</span>
          </div>
          <div ref="chartRef" class="h-96 w-full"></div>
        </div>
    </div>

     <!-- Empty State -->
    <div v-else class="text-center py-16 card">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-12 h-12 mx-auto text-gray-400 mb-3" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5a2 2 0 00-2 2v14l4-4h12a2 2 0 002-2V5a2 2 0 00-2-2z"/></svg>
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">请选择一门课程</h3>
      <p class="text-gray-500">选择一门课程后，将显示相关的统计数据与图表</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { useTeacherStore } from '@/stores/teacher'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth' // 1. 导入 useAuthStore
import * as echarts from 'echarts'

const uiStore = useUIStore()
const router = useRouter()
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore() // 2. 获取 authStore 实例

const selectedCourseId = ref<string | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

// 3. 创建 teacherCourses 计算属性
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(course => String(course.teacherId) === String(authStore.user?.id))
})

const loading = teacherStore.loading
const courseAnalytics = computed(() => teacherStore.courseAnalytics)
const classPerformance = computed(() => teacherStore.classPerformance)

const safeAnalytics = computed(() => ({
  totalStudents: (courseAnalytics.value as any).totalStudents ?? courseAnalytics.value.enrollmentCount ?? 0,
  completionRate: (courseAnalytics.value as any).completionRate ?? courseAnalytics.value.averageCompletionRate ?? 0,
  averageScore: courseAnalytics.value.averageScore ?? 0,
  totalAssignments: (courseAnalytics.value as any).totalAssignments ?? courseAnalytics.value.assignmentCount ?? 0,
}))

const toggleSidebar = () => uiStore.toggleSidebar()

const onCourseSelect = () => {
  if (selectedCourseId.value) {
    teacherStore.fetchCourseAnalytics(selectedCourseId.value)
    teacherStore.fetchClassPerformance(selectedCourseId.value)
  }
}

const goPublishAssignment = () => {
  const query: Record<string, string> = {}
  if (selectedCourseId.value) query.courseId = selectedCourseId.value
  query.openCreate = '1'
  router.push({ name: 'TeacherAssignments', query })
}

const goGradeAssignments = () => {
  const query: Record<string, string> = {}
  if (selectedCourseId.value) query.courseId = selectedCourseId.value
  router.push({ name: 'TeacherAssignments', query })
}

const initChart = () => {
  const dist: any[] = (classPerformance.value as any).gradeDistribution || []
  if (!chartRef.value || !dist.length) return
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    xAxis: { type: 'category', data: dist.map(d => d.gradeLevel ?? d.level ?? '未知') },
    yAxis: { type: 'value' },
    series: [{ name: '学生人数', type: 'bar', data: dist.map(d => d.count ?? 0) }]
  })
}

watch(classPerformance, () => nextTick(initChart), { deep: true })

onMounted(async () => {
  // 确保在获取课程之前，用户信息是可用的
  if (!authStore.user) {
    await authStore.fetchUser()
  }
  await courseStore.fetchCourses({ page: 1, size: 100 })
  // 5. 使用过滤后的列表
  if (teacherCourses.value.length) {
    selectedCourseId.value = String(teacherCourses.value[0].id)
    onCourseSelect()
  }
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})
</script>
