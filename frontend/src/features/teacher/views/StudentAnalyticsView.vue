<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">学生进度概览</h1>
      <p class="text-gray-500">查看并跟踪您所有课程中学生的学习进度</p>
    </div>

    <!-- Filters -->
    <div class="mb-6 card p-4">
      <div class="flex items-center gap-4">
        <label for="course-filter" class="font-medium">按课程筛选:</label>
        <select id="course-filter" v-model="selectedCourseId" @change="loadClassPerformance" class="input flex-grow">
          <option :value="null">请选择课程</option>
          <option v-for="course in teacherCourses" :key="course.id" :value="String(course.id)">
            {{ course.title }}
          </option>
        </select>
      </div>
    </div>

    <!-- Stats (from class performance) -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ (classPerformance?.totalStudents || 0) }}</h3>
            <p class="text-sm text-gray-500">学生总数</p>
        </div>
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ (avgScoreFromStats || 0).toFixed(1) }}</h3>
            <p class="text-sm text-gray-500">平均分</p>
        </div>
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ (passRateFromStats || 0).toFixed(1) }}%</h3>
            <p class="text-sm text-gray-500">及格率</p>
        </div>
    </div>

    <!-- Placeholder content since backend has no student progress list API -->
    <div class="card p-8 text-center text-gray-500">
      <p>当前为按课程的班级整体表现汇总。若需查看学生逐项进度，请提供后端分页列表接口。</p>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useTeacherStore } from '@/stores/teacher';
import { useCourseStore } from '@/stores/course';
import { useAuthStore } from '@/stores/auth';

const teacherStore = useTeacherStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();

const selectedCourseId = ref<string | null>(null);

const teacherCourses = computed(() => {
  if (!authStore.user?.id) return []
  return courseStore.courses.filter(c => String(c.teacherId) === String(authStore.user?.id))
});

const classPerformance = computed(() => teacherStore.classPerformance as any);

const avgScoreFromStats = computed(() => {
  const gradeStats = classPerformance.value?.gradeStats as any;
  if (!gradeStats) return 0;
  // 允许后端返回 { averageScore: number } 或分布；仅提取平均分
  if (typeof gradeStats.averageScore === 'number') return gradeStats.averageScore;
  return 0;
});

const passRateFromStats = computed(() => {
  const gradeStats = classPerformance.value?.gradeStats as any;
  if (!gradeStats) return 0;
  if (typeof gradeStats.passRate === 'number') return gradeStats.passRate;
  return 0;
});

const loadClassPerformance = () => {
  if (!selectedCourseId.value) return;
  teacherStore.fetchClassPerformance(selectedCourseId.value);
};

onMounted(async () => {
  if (!authStore.user) {
    await authStore.fetchUser();
  }
  await courseStore.fetchCourses({ page: 1, size: 100 });
  await nextTick();
  if (teacherCourses.value.length) {
    selectedCourseId.value = String(teacherCourses.value[0].id);
    loadClassPerformance();
  }
});
</script>
