<template>
  <div class="p-6 min-h-screen">
    <PageHeader :title="t('teacher.dashboard.header.title')" :subtitle="t('teacher.dashboard.header.subtitle')">
      <template #actions>
        <div class="flex items-end gap-4 w-full sm:w-auto">
          <div class="w-full sm:w-64">
            <GlassPopoverSelect
              :label="t('teacher.dashboard.course.select.label') as string"
              v-model="selectedCourseId"
              :options="teacherCourseOptions"
              :placeholder="t('teacher.dashboard.course.select.placeholder') as string"
              size="md"
              stacked
              @change="onCourseSelect"
            />
          </div>
          <div class="hidden sm:flex items-center gap-2">
            <Button variant="primary" @click="goPublishAssignment">
              <PlusIcon class="w-4 h-4 mr-2" />{{ t('teacher.dashboard.actions.publish') }}
            </Button>
            <Button variant="success" @click="goGradeAssignments">
              <CheckBadgeIcon class="w-4 h-4 mr-2" />{{ t('teacher.dashboard.actions.grade') }}
            </Button>
          </div>
        </div>
      </template>
    </PageHeader>

    <!-- Course Selector -->
    

    <!-- Loading State -->
    <div v-if="teacherStore.loading" class="text-center py-12">
        <p>{{ t('app.loading.title') }}</p>
      </div>

    <!-- Analytics Content -->
    <div v-else-if="selectedCourseId && courseAnalytics" class="space-y-8">
        <!-- Stats Cards (统一为工作台小卡片组件) -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <StartCard :label="t('teacher.dashboard.cards.totalStudents') as string" :value="n(safeAnalytics.totalStudents, 'integer')" tone="blue" :icon="UserGroupIcon" />
          <StartCard :label="t('teacher.dashboard.cards.completionRate') as string" :value="n((safeAnalytics.completionRate || 0) / 100, 'percent')" tone="emerald" :icon="CheckCircleIcon" />
          <StartCard :label="t('teacher.dashboard.cards.averageScore') as string" :value="(safeAnalytics.averageScore || 0).toFixed(1)" tone="violet" :icon="StarIcon" />
          <StartCard :label="t('teacher.dashboard.cards.totalAssignments') as string" :value="n(safeAnalytics.totalAssignments, 'integer')" tone="amber" :icon="ClipboardDocumentListIcon" />
        </div>

        <!-- Class Performance Chart -->
        <div class="card p-6">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ t('teacher.dashboard.chart.title') }}</h2>
            <span class="text-xs text-gray-500">{{ t('teacher.dashboard.chart.subtitle') }}</span>
          </div>
          <div ref="chartRef" class="h-[32rem] w-full"></div>
        </div>
    </div>

     <!-- Empty State -->
    <div v-else class="text-center py-16 card">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-12 h-12 mx-auto text-gray-400 mb-3" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5a2 2 0 00-2 2v14l4-4h12a2 2 0 002-2V5a2 2 0 00-2-2z"/></svg>
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ t('teacher.dashboard.empty.title') }}</h3>
      <p class="text-gray-500">{{ t('teacher.dashboard.empty.description') }}</p>
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
import { useI18n } from 'vue-i18n'
import { loadLocaleMessages } from '@/i18n'
import { PlusIcon, CheckBadgeIcon, UserGroupIcon, CheckCircleIcon, StarIcon, ClipboardDocumentListIcon } from '@heroicons/vue/24/outline'
import Button from '@/components/ui/Button.vue'
import StartCard from '@/components/ui/StartCard.vue'
import GlassSelect from '@/components/ui/filters/GlassSelect.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUIStore()
const router = useRouter()
const teacherStore = useTeacherStore()
const courseStore = useCourseStore()
const authStore = useAuthStore() // 2. 获取 authStore 实例
const { t, n, locale } = useI18n()

const selectedCourseId = ref<string | null>(null)
const chartRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null
let resizeBound = false
const onResize = () => chart?.resize()

// 3. 创建 teacherCourses 计算属性
const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(course => String(course.teacherId) === String(authStore.user?.id))
})

const teacherCourseOptions = computed(() =>
  teacherCourses.value.map((c: { id: string | number; title: string }) => ({ label: c.title, value: String(c.id) }))
)

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
  if (!chartRef.value) return
  // 复用已存在的实例，避免重复 init 警告
  chart = echarts.getInstanceByDom(chartRef.value) || echarts.init(chartRef.value)
  if (!dist.length) {
    chart.clear()
    return
  }
  const palette = ['#3b82f6', '#06b6d4', '#ec4899', '#f59e0b', '#10b981', '#8b5cf6', '#f43f5e']
  const categories = dist.map(d => d.gradeLevel ?? d.level ?? t('teacher.dashboard.chart.level.unknown'))
  const values = dist.map(d => d.count ?? 0)
  // 动态计算更粗的柱宽（默认近似直方图，仍保留一定间隔）
  const containerWidth = (chartRef.value as HTMLElement).clientWidth || 800
  const innerWidth = Math.floor(containerWidth * 0.94) // 近似减去 grid 边距
  const n = Math.max(categories.length, 1)
  const minGapPx = 12
  const computedBarWidth = (() => {
    const widthIfDense = Math.floor(innerWidth / (n * 1.2)) // 普通密度
    const widthIfFew = Math.floor((innerWidth - (n + 1) * minGapPx) / n) // 类直方图（少分类更粗）
    const base = n <= 6 ? widthIfFew : widthIfDense
    return Math.min(140, Math.max(36, base))
  })()
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `${p.name}<br/>${t('teacher.dashboard.chart.series.students')}: ${p.value}`
    },
    grid: { left: '4%', right: '2%', bottom: '5%', containLabel: true },
    xAxis: { type: 'category', data: categories, axisTick: { alignWithLabel: true } },
    yAxis: { type: 'value' },
    series: [{
      name: t('teacher.dashboard.chart.series.students'),
      type: 'bar',
      data: values,
      barWidth: computedBarWidth,
      barCategoryGap: '8%',
      itemStyle: {
        color: (params: any) => palette[params.dataIndex % palette.length]
      }
    }]
  }, { notMerge: true, lazyUpdate: false })
}

watch(classPerformance, () => nextTick(initChart), { deep: true })
// 当语言切换时，重绘图表以应用新文案
watch(() => locale.value, () => nextTick(initChart))

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
  // 预加载教师端命名空间
  await loadLocaleMessages(locale.value as 'zh-CN' | 'en-US', ['teacher'])
  if (!resizeBound) {
    window.addEventListener('resize', onResize)
    resizeBound = true
  }
})

onUnmounted(() => {
  if (resizeBound) {
    window.removeEventListener('resize', onResize)
    resizeBound = false
  }
  chart?.dispose()
})
</script>
