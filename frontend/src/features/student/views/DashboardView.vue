<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">{{ t('student.dashboard.title') }}</h1>
      <p class="text-gray-500">{{ t('student.dashboard.subtitle') }}</p>
    </div>

    <!-- Loading State -->
    <div v-if="studentStore.loading" class="text-center py-12">
      <p>{{ t('student.dashboard.loading') }}</p>
    </div>

    <!-- Main Content -->
    <div v-else-if="dashboardReady && !studentStore.loading" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
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
              <router-link :to="`/student/assignments/${assignment.id}/submit`" class="btn btn-sm btn-outline">{{ t('student.dashboard.goDo') }}</router-link>
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
              <router-link :to="`/student/courses/${course.id}`" class="btn btn-sm btn-outline">{{ t('student.dashboard.continue') }}</router-link>
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
import { onMounted, computed } from 'vue'
import { useUIStore } from '@/stores/ui'
import { useStudentStore } from '@/stores/student'
import { useI18n } from 'vue-i18n'

const uiStore = useUIStore()
const studentStore = useStudentStore()
const { t } = useI18n()

// 渲染所需派生状态，避免模板直接访问未定义属性
const dashboardReady = computed(() => !studentStore.loading)
const upcomingAssignments = computed(() => (studentStore.dashboardData?.upcomingAssignments) || [])
const activeCourses = computed(() => (studentStore.dashboardData?.activeCourses) || [])
const recentGrades = computed(() => (studentStore.dashboardData?.recentGrades) || [])
const overallProgress = computed(() => Number(studentStore.dashboardData?.overallProgress || 0))

const getScoreColor = (score: number) => {
  const s = Number(score || 0)
  if (s >= 85) return 'text-green-600'
  if (s >= 70) return 'text-yellow-600'
  return 'text-red-600'
}

onMounted(() => {
  studentStore.fetchDashboardData()
})
</script>
