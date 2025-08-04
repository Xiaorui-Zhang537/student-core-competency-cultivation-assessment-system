<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">我的成绩</h1>
      <p class="text-gray-500">查看您所有作业的评分和反馈</p>
    </div>

    <!-- Loading State -->
    <div v-if="gradeStore.loading" class="text-center py-12">
      <p>正在加载成绩...</p>
    </div>

    <!-- Grade List -->
    <div v-else-if="gradeStore.grades.length > 0" class="space-y-4">
      <div v-for="grade in gradeStore.grades" :key="grade.id" class="card p-4 flex justify-between items-center">
        <div class="flex-1">
          <h3 class="font-bold text-lg">作业 ID: {{ grade.submissionId }}</h3>
          <p class="text-sm text-gray-600 mt-1">{{ grade.feedback }}</p>
          <div class="text-sm text-gray-500 mt-2">
            评分于: {{ new Date(grade.gradedAt).toLocaleDateString() }}
          </div>
        </div>
        <div class="text-right ml-4">
          <div class="text-2xl font-bold" :class="getScoreColor(grade.score)">
            {{ grade.score }} / 100
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12 card">
      <h3 class="text-lg font-medium">暂无已评分的作业</h3>
      <p class="text-gray-500">完成并提交作业后，您可以在此处查看您的成绩。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useGradeStore } from '@/stores/grade';
import { useAuthStore } from '@/stores/auth';

const gradeStore = useGradeStore();
const authStore = useAuthStore();

const getScoreColor = (score: number) => {
  if (score >= 90) return 'text-green-600';
  if (score >= 80) return 'text-blue-600';
  if (score >= 70) return 'text-yellow-600';
  if (score >= 60) return 'text-orange-600';
  return 'text-red-600';
};

onMounted(() => {
  const user = authStore.user;
  if (user && user.role === 'STUDENT') {
    gradeStore.fetchGradesByStudent(user.id);
  }
});
</script>
