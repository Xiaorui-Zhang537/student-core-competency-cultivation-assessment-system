<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">{{ t('student.grades.title') }}</h1>
      <p class="text-gray-500">{{ t('student.grades.subtitle') }}</p>
    </div>

    <!-- Loading State -->
    <div v-if="gradeStore.loading" class="text-center py-12">
      <p>{{ t('student.grades.loading') }}</p>
    </div>

    <!-- Grade List -->
    <div v-else-if="gradeStore.grades.length > 0" class="space-y-4">
      <div v-for="grade in gradeStore.grades" :key="grade.id" class="glass-regular rounded-lg p-4 flex justify-between items-center" v-glass="{ strength: 'regular', interactive: true }">
        <div class="flex-1">
          <h3 class="font-bold text-lg">{{ t('student.grades.assignmentId') }}: {{ grade.submissionId }}</h3>
          <p class="text-sm text-gray-600 mt-1">{{ grade.feedback }}</p>
          <div class="text-sm text-gray-500 mt-2">
            {{ t('student.grades.gradedAt') }}: {{ new Date(grade.gradedAt).toLocaleDateString() }}
          </div>
        </div>
        <div class="text-right ml-4">
          <div class="text-2xl font-bold" :class="getScoreColor(grade.score)">
            {{ grade.score }}{{ t('student.grades.outOf') }}
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12 glass-regular rounded-lg" v-glass="{ strength: 'regular', interactive: true }">
      <h3 class="text-lg font-medium">{{ t('student.grades.emptyTitle') }}</h3>
      <p class="text-gray-500">{{ t('student.grades.emptyDesc') }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n'
import { useGradeStore } from '@/stores/grade';
import { useAuthStore } from '@/stores/auth';

const gradeStore = useGradeStore();
const authStore = useAuthStore();
const { t } = useI18n()

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
