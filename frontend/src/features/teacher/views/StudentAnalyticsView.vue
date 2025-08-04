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
        <select id="course-filter" v-model="selectedCourseId" @change="loadStudentProgress" class="input flex-grow">
          <option :value="null">所有课程</option>
          <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
            {{ course.title }}
          </option>
        </select>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ studentProgress.length }}</h3>
            <p class="text-sm text-gray-500">学生总数</p>
        </div>
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ averageProgress.toFixed(1) }}%</h3>
            <p class="text-sm text-gray-500">平均进度</p>
        </div>
        <div class="card p-4 text-center">
            <h3 class="text-2xl font-bold">{{ averageScore.toFixed(1) }}</h3>
            <p class="text-sm text-gray-500">平均分</p>
        </div>
    </div>
    
    <!-- Student List Table -->
    <div class="card overflow-x-auto">
        <div v-if="teacherStore.loading" class="text-center p-8">正在加载学生进度...</div>
        <div v-else-if="studentProgress.length === 0" class="text-center p-8">
            <p>没有找到符合条件的学生数据。</p>
        </div>
        <table v-else class="w-full text-left">
            <thead>
                <tr class="border-b">
                    <th class="p-4">学生姓名</th>
                    <th class="p-4">已完成课程数</th>
                    <th class="p-4">平均进度</th>
                    <th class="p-4">平均分</th>
                    <th class="p-4">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="student in studentProgress" :key="student.studentId" class="border-b hover:bg-gray-50">
                    <td class="p-4 font-medium">{{ student.studentName }}</td>
                    <td class="p-4">{{ student.completedCourses }}</td>
                    <td class="p-4">
                        <div class="w-full bg-gray-200 rounded-full h-2.5">
                            <div class="bg-blue-600 h-2.5 rounded-full" :style="{ width: student.progressPercentage + '%' }"></div>
                        </div>
                        <span class="text-sm">{{ student.progressPercentage.toFixed(1) }}%</span>
                    </td>
                    <td class="p-4">{{ student.averageScore.toFixed(1) }}</td>
                    <td class="p-4">
                        <router-link :to="`/teacher/student-detail/${student.studentId}?name=${encodeURIComponent(student.studentName)}`" class="btn btn-sm btn-outline-primary">
                            查看详情
                        </router-link>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useTeacherStore } from '@/stores/teacher';
import { useCourseStore } from '@/stores/course';

const teacherStore = useTeacherStore();
const courseStore = useCourseStore();

const selectedCourseId = ref<number | null>(null);

const studentProgress = computed(() => teacherStore.studentProgress);

const averageProgress = computed(() => {
    if (studentProgress.value.length === 0) return 0;
    const total = studentProgress.value.reduce((sum, s) => sum + s.progressPercentage, 0);
    return total / studentProgress.value.length;
});

const averageScore = computed(() => {
    if (studentProgress.value.length === 0) return 0;
    const total = studentProgress.value.reduce((sum, s) => sum + s.averageScore, 0);
    return total / studentProgress.value.length;
});

const loadStudentProgress = () => {
    teacherStore.fetchStudentProgress({
        courseId: selectedCourseId.value ?? undefined
    });
};

onMounted(() => {
    courseStore.fetchCourses();
    loadStudentProgress();
});
</script>
