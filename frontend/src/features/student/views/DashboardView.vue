<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">学生仪表盘</h1>
      <p class="text-gray-500">总览您的学习进度和任务</p>
    </div>

    <!-- Loading State -->
    <div v-if="studentStore.loading" class="text-center py-12">
      <p>正在加载仪表盘数据...</p>
    </div>

    <!-- Main Content -->
    <div v-else-if="dashboardData" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Left Column: Assignments and Courses -->
      <div class="lg:col-span-2 space-y-8">
        <!-- Upcoming Assignments -->
        <div class="card p-6">
          <h2 class="text-xl font-semibold mb-4">即将到期的作业</h2>
          <div v-if="dashboardData.upcomingAssignments.length > 0" class="space-y-3">
            <div v-for="assignment in dashboardData.upcomingAssignments" :key="assignment.id" class="p-3 bg-gray-50 rounded flex justify-between items-center">
              <div>
                <h4 class="font-medium">{{ assignment.title }}</h4>
                <p class="text-sm text-gray-500">截止日期: {{ new Date(assignment.dueDate).toLocaleDateString() }}</p>
              </div>
              <router-link :to="`/student/assignments/${assignment.id}/submit`" class="btn btn-sm btn-outline">去完成</router-link>
            </div>
          </div>
          <p v-else class="text-gray-500">暂无即将到期的作业。</p>
        </div>

        <!-- Active Courses -->
        <div class="card p-6">
          <h2 class="text-xl font-semibold mb-4">进行中的课程</h2>
          <div v-if="dashboardData.activeCourses.length > 0" class="space-y-3">
            <div v-for="course in dashboardData.activeCourses" :key="course.id" class="p-3 bg-gray-50 rounded flex justify-between items-center">
              <div>
                <h4 class="font-medium">{{ course.title }}</h4>
                <p class="text-sm text-gray-500">{{ course.teacherName }}</p>
              </div>
              <router-link :to="`/student/courses/${course.id}`" class="btn btn-sm btn-outline">继续学习</router-link>
            </div>
          </div>
          <p v-else class="text-gray-500">您还没有加入任何课程。</p>
        </div>
      </div>

      <!-- Right Column: Grades and Progress -->
      <div class="space-y-8">
        <!-- Overall Progress -->
        <div class="card p-6 text-center">
          <h2 class="text-xl font-semibold mb-4">总学习进度</h2>
          <div class="text-4xl font-bold text-primary-600">{{ dashboardData.overallProgress }}%</div>
        </div>

        <!-- Recent Grades -->
        <div class="card p-6">
          <h2 class="text-xl font-semibold mb-4">最近成绩</h2>
          <div v-if="dashboardData.recentGrades.length > 0" class="space-y-3">
            <div v-for="(grade, index) in dashboardData.recentGrades" :key="index" class="p-3 bg-gray-50 rounded flex justify-between items-center">
              <div>
                <h4 class="font-medium">{{ grade.assignmentTitle }}</h4>
                <p class="text-sm text-gray-500">{{ grade.courseTitle }}</p>
              </div>
              <div class="text-lg font-bold" :class="getScoreColor(grade.score)">{{ grade.score }}</div>
            </div>
          </div>
          <p v-else class="text-gray-500">暂无最近的成绩记录。</p>
        </div>
      </div>
    </div>
     <!-- Empty State -->
    <div v-else class="text-center py-12 card">
        <h3 class="text-lg font-medium">无法加载仪表盘数据</h3>
        <p class="text-gray-500">请稍后再试。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue';
import { useStudentStore } from '@/stores/student';

const studentStore = useStudentStore();
const dashboardData = computed(() => studentStore.dashboardData);

const getScoreColor = (score: number) => {
  if (score >= 90) return 'text-green-600';
  if (score >= 80) return 'text-blue-600';
  if (score >= 70) return 'text-yellow-600';
  if (score >= 60) return 'text-orange-600';
  return 'text-red-600';
};

onMounted(() => {
  studentStore.fetchDashboard();
});
</script>
