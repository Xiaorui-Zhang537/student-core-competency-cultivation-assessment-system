<template>
  <div class="p-6">
    <PageHeader :title="t('student.dashboard.title')" :subtitle="t('student.dashboard.subtitle')" />

    <!-- Loading State -->
    <div v-if="studentStore.loading" class="text-center py-12">
      <p>{{ t('student.dashboard.loading') }}</p>
    </div>

    <!-- Main Content -->
    <div v-else-if="dashboardReady && !studentStore.loading" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Top KPI Cards (full width) -->
      <div class="lg:col-span-3">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 mb-2">
          <StartCard :label="t('student.dashboard.cards.activeCourses') as string" :value="stats.activeCourses" tone="blue" :icon="AcademicCapIcon" />
          <StartCard :label="t('student.dashboard.cards.pendingAssignments') as string" :value="stats.pendingAssignments" tone="amber" :icon="ClipboardDocumentListIcon" />
          <StartCard :label="t('student.dashboard.cards.averageScore') as string" :value="Number(stats.averageScore || 0).toFixed(1)" tone="violet" :icon="StarIcon" />
          <StartCard :label="t('student.dashboard.cards.weeklyStudyHours') as string" :value="weeklyStudyHours" tone="emerald" :icon="ClockIcon" />
          <StartCard v-if="abilityOverallScore>0" :label="t('student.ability.radar') as string" :value="abilityOverallScore.toFixed(1)" tone="indigo" :icon="StarIcon" />
        </div>
      </div>
      <!-- Left Column: Assignments and Courses -->
      <div class="lg:col-span-2 space-y-8">
        <!-- Upcoming Assignments -->
        <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.dashboard.upcomingAssignments') }}</h2>
          <div v-if="upcomingAssignments.length > 0" class="space-y-3">
            <div v-for="assignment in upcomingAssignments" :key="assignment.id" class="p-3 glass-thin rounded flex justify-between items-center" v-glass="{ strength: 'thin', interactive: false }">
              <div>
                <h4 class="font-medium">{{ assignment.title }}</h4>
                <p class="text-sm text-gray-500">{{ new Date(assignment.dueDate).toLocaleDateString() }}</p>
              </div>
              <Button size="sm" variant="outline" @click="router.push(`/student/assignments/${assignment.id}/submit`)">{{ t('student.dashboard.goDo') }}</Button>
            </div>
          </div>
          <p v-else class="text-gray-500">{{ t('student.dashboard.noUpcoming') }}</p>
        </div>

        <!-- Active Courses -->
        <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.dashboard.activeCourses') }}</h2>
          <div v-if="activeCourses.length > 0" class="space-y-3">
            <div v-for="course in activeCourses" :key="course.id" class="p-3 glass-thin rounded flex justify-between items-center" v-glass="{ strength: 'thin', interactive: false }">
              <div>
                <h4 class="font-medium">{{ course.title }}</h4>
                <p class="text-sm text-gray-500">{{ course.teacherName }}</p>
              </div>
              <Button size="sm" variant="outline" @click="router.push(`/student/courses/${course.id}`)">{{ t('student.dashboard.continue') }}</Button>
            </div>
          </div>
          <p v-else class="text-gray-500">{{ t('student.dashboard.noCourses') }}</p>
        </div>
      </div>

      <!-- Right Column: Grades and Progress -->
      <div class="space-y-8">
        <!-- Overall Progress -->
        <div class="p-6 glass-regular rounded-lg shadow text-center" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.dashboard.overallProgress') }}</h2>
          <div class="text-4xl font-bold text-primary-600">{{ overallProgress }}%</div>
        </div>

        <!-- Recent Grades -->
        <div class="p-6 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
          <h2 class="text-xl font-semibold mb-4">{{ t('student.dashboard.recentGrades') }}</h2>
          <div v-if="recentGrades.length > 0" class="space-y-3">
            <div v-for="(grade, index) in recentGrades" :key="index" class="p-3 glass-thin rounded flex justify-between items-center" v-glass="{ strength: 'thin', interactive: false }">
              <div>
                <h4 class="font-medium">{{ grade.assignmentTitle }}</h4>
                <p class="text-sm text-gray-500">{{ grade.courseTitle }}</p>
              </div>
              <div class="text-lg font-bold" :class="getScoreColor(grade.score)">{{ grade.score }}</div>
            </div>
          </div>
          <p v-else class="text-gray-500">{{ t('student.dashboard.noRecentGrades') }}</p>
        </div>
      </div>
    </div>
     <!-- Empty State -->
    <div v-else class="text-center py-12 glass-regular rounded-lg shadow" v-glass="{ strength: 'regular', interactive: true }">
        <h3 class="text-lg font-medium">{{ t('student.dashboard.errorTitle') }}</h3>
        <p class="text-gray-500">{{ t('student.dashboard.errorDesc') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, watch } from 'vue'
import { useUIStore } from '@/stores/ui'
import { useStudentStore } from '@/stores/student'
import { useI18n } from 'vue-i18n'
import StartCard from '@/components/ui/StartCard.vue'
import { AcademicCapIcon, ClipboardDocumentListIcon, StarIcon, ClockIcon } from '@heroicons/vue/24/outline'
import PageHeader from '@/components/ui/PageHeader.vue'
import { useSubmissionStore } from '@/stores/submission'
import Button from '@/components/ui/Button.vue'

const uiStore = useUIStore()
const studentStore = useStudentStore()
const submissionStore = useSubmissionStore()
const { t } = useI18n()

// 渲染所需派生状态，避免模板直接访问未定义属性
const dashboardReady = computed(() => !studentStore.loading)

// 工具函数：是否已过期（截至时间已到或已过）
const isOverdue = (dueDate?: string) => {
  if (!dueDate) return false
  const d = new Date(dueDate)
  if (isNaN(d.getTime())) return false
  return Date.now() >= d.getTime()
}

// 工具函数：是否已提交或已评分
const isSubmittedOrGraded = (assignmentId: string) => {
  try {
    const sub = submissionStore.submissions.get(String(assignmentId)) as any
    if (!sub) return false
    const status = String(sub.status || '').toUpperCase()
    const hasContent = !!String(sub.content || '').trim()
    const hasFiles = Array.isArray(sub.fileIds) && sub.fileIds.length > 0
    const hasSubmittedAt = !!sub.submittedAt
    // 认为有任何有效提交内容即为已提交；评分状态直接排除
    if (status === 'GRADED') return true
    if (status === 'SUBMITTED' || status === 'LATE') return true
    return hasContent || hasFiles || hasSubmittedAt
  } catch {
    return false
  }
}

// 过滤：仅显示未提交且未过期的作业
const upcomingAssignments = computed(() => {
  const base = (studentStore.dashboardData?.upcomingAssignments) || []
  return base.filter((a: any) => {
    const due = a?.dueDate
    if (isOverdue(due)) return false
    if (isSubmittedOrGraded(String(a?.id))) return false
    return true
  })
})
const activeCourses = computed(() => (studentStore.dashboardData?.activeCourses) || [])
const recentGrades = computed(() => (studentStore.dashboardData?.recentGrades) || [])
const overallProgress = computed(() => Number(studentStore.dashboardData?.overallProgress || 0))
const stats = computed(() => (studentStore.dashboardData as any)?.stats || { activeCourses: 0, pendingAssignments: 0, averageScore: 0, weeklyStudyTime: 0 })
const abilityOverallScore = computed(() => Number((studentStore.dashboardData as any)?.abilityOverallScore || 0))
const weeklyStudyHours = computed(() => {
  const minutes = Number((stats.value as any)?.weeklyStudyTime || 0)
  const hours = minutes / 60
  return Math.round(hours * 10) / 10
})

const getScoreColor = (score: number) => {
  const s = Number(score || 0)
  if (s >= 85) return 'text-green-600'
  if (s >= 70) return 'text-yellow-600'
  return 'text-red-600'
}

onMounted(() => {
  studentStore.fetchDashboardData()
})

// 拉取即将到期作业对应的提交记录，便于准确过滤（避免显示已提交或已评分）
const fetchedSubmissionIds = new Set<string>()
watch(
  () => studentStore.dashboardData?.upcomingAssignments,
  async (list) => {
    try {
      const items = Array.isArray(list) ? list : []
      const ids = items.map((x: any) => String(x?.id)).filter(Boolean)
      const toFetch = ids.filter(id => !fetchedSubmissionIds.has(id))
      if (toFetch.length > 0) {
        await Promise.allSettled(toFetch.map(id => submissionStore.fetchSubmissionForAssignment(id)))
        toFetch.forEach(id => fetchedSubmissionIds.add(id))
      }
    } catch {}
  },
  { immediate: true, deep: false }
)
</script>
