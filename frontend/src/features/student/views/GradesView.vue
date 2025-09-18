<template>
  <div class="min-h-screen p-6">
    <div class="max-w-6xl mx-auto">
      <div class="mb-6 flex items-center justify-between">
        <h1 class="text-2xl font-semibold text-gray-900 dark:text-white">{{ t('student.grades.title') || '我的成绩' }}</h1>
        <div class="flex items-center gap-3">
          <GlassPopoverSelect v-model="filters.courseId" :options="courseOptions" size="md" placeholder="{{ t('student.grades.filterCourse') || '筛选课程' }}" />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div v-for="g in grades" :key="g.id" class="rounded-xl glass-ultraThin p-5" v-glass="{ strength: 'ultraThin', interactive: true }">
          <div class="flex items-center justify-between mb-2">
            <div class="text-base font-medium text-gray-900 dark:text-white truncate">{{ g.assignmentTitle || ('#' + g.assignmentId) }}</div>
            <Badge variant="secondary">{{ formatDate(g.updatedAt || g.publishedAt || g.createdAt) }}</Badge>
          </div>

          <div class="flex items-center justify-between mb-2">
            <div class="text-3xl font-bold text-gray-900 dark:text-gray-100">{{ Number(g.score || 0).toFixed(1) }}</div>
            <div class="text-sm text-gray-500">/ {{ Number(g.maxScore || 100) }}</div>
          </div>

          <div class="mt-2 h-2 rounded-full bg-gray-200/60 dark:bg-gray-700/60 overflow-hidden">
            <div class="h-full rounded-full bg-primary-500/70 backdrop-blur-sm transition-all duration-500" :style="{ width: getPercent(g) + '%' }"></div>
          </div>

          <div class="mt-3 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-600 dark:text-gray-300">{{ t('student.grades.level') || '等级' }}</span>
              <Badge size="sm">{{ g.gradeLevel || autoLevel(g) }}</Badge>
            </div>
            <div class="text-sm text-gray-500">{{ t('student.grades.status.' + (g.status || 'published')) || g.status }}</div>
          </div>

          <div v-if="hasStrengths(g) || hasImprovements(g)" class="mt-4 grid grid-cols-1 gap-3">
            <div v-if="hasStrengths(g)">
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('student.grades.strengths') || '优点' }}</div>
              <div class="rounded-xl glass-ultraThin p-3" v-glass="{ strength: 'ultraThin', interactive: false }">
                <p class="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line">{{ normalizeText(g.strengths) }}</p>
              </div>
            </div>
            <div v-if="hasImprovements(g)">
              <div class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('student.grades.improvements') || '可改进' }}</div>
              <div class="rounded-xl glass-ultraThin p-3" v-glass="{ strength: 'ultraThin', interactive: false }">
                <p class="text-sm text-gray-700 dark:text-gray-300 whitespace-pre-line">{{ normalizeText(g.improvements) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useLocale } from '@/i18n/useLocale'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { gradeApi } from '@/api/grade.api'
import Badge from '@/components/ui/Badge.vue'
import { useAuthStore } from '@/stores/auth'

const { t } = useLocale()
const auth = useAuthStore()

const grades = ref<any[]>([])
const filters = ref<{ courseId?: string | number }>({})
const courseOptions = ref<{ label: string; value: string | number }[]>([])

function getPercent(g: any) {
  const max = Number(g.maxScore || 100)
  const val = Number(g.score || 0)
  return max > 0 ? Math.min(100, Math.max(0, (val / max) * 100)) : 0
}

function autoLevel(g: any) {
  const pct = getPercent(g)
  if (pct >= 95) return 'A+'
  if (pct >= 90) return 'A'
  if (pct >= 85) return 'B+'
  if (pct >= 80) return 'B'
  if (pct >= 70) return 'C+'
  if (pct >= 60) return 'C'
  if (pct >= 50) return 'D'
  return 'F'
}

function hasStrengths(g: any) { return !!(g.strengths && String(g.strengths).trim()) }
function hasImprovements(g: any) { return !!(g.improvements && String(g.improvements).trim()) }
function normalizeText(s: any) { return Array.isArray(s) ? s.join('\n') : String(s || '') }
function formatDate(s: any) { try { return new Date(s).toLocaleString('zh-CN') } catch { return s || '' } }

async function loadGrades() {
  const sid = String(auth.user?.id || '')
  const res: any = await gradeApi.getGradesByStudent(sid, { page: 1, size: 50, courseId: filters.value.courseId })
  const items = (res?.items as any[]) || (res?.content as any[]) || []
  grades.value = items
}

onMounted(() => { loadGrades() })
</script>

<style scoped>
</style>

<template>
  <div class="p-6">
    <PageHeader :title="t('student.grades.title')" :subtitle="t('student.grades.subtitle')" />

    <!-- Loading State -->
    <div v-if="gradeStore.loading" class="text-center py-12">
      <p>{{ t('student.grades.loading') }}</p>
    </div>

    <!-- Grade List -->
    <div v-else-if="gradeStore.grades.length > 0" class="space-y-4">
      <div v-for="grade in gradeStore.grades" :key="grade.id" class="glass-regular rounded-xl p-5" v-glass="{ strength: 'regular', interactive: true }">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <h3 class="font-bold text-lg truncate">{{ grade.assignmentTitle || (t('student.grades.assignmentId') + ': ' + grade.submissionId) }}</h3>
            <p v-if="grade.feedback" class="text-sm text-gray-700 dark:text-gray-300 mt-1 whitespace-pre-line">{{ grade.feedback }}</p>
            <div class="text-xs text-gray-500 mt-1">
              {{ t('student.grades.gradedAt') }}: {{ new Date(grade.gradedAt).toLocaleString() }}
            </div>
          </div>
          <div class="w-40 flex-shrink-0 text-right">
            <div class="text-2xl font-bold" :class="getScoreColor(grade.score)">
              {{ grade.score }}{{ t('student.grades.outOf') }}
            </div>
            <div v-if="grade.level" class="mt-1 inline-block text-xs px-2 py-0.5 rounded-full bg-white/60 dark:bg-white/10 border border-white/20">{{ grade.level }}</div>
          </div>
        </div>
        <!-- Animated glass progress bar -->
        <div class="mt-3">
          <div class="h-2 rounded-full bg-white/10 overflow-hidden">
            <div class="h-full rounded-full bg-primary-500/80 transition-all duration-500" :style="{ width: Math.min(100, Math.max(0, Number(grade.score || 0))) + '%' }"></div>
          </div>
        </div>
        <!-- Strengths / Improvements blocks -->
        <div class="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3">
          <div v-if="grade.strengths" class="glass-ultraThin rounded-lg p-3" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="text-sm font-medium mb-1">{{ t('teacher.grading.form.strengths') }}</div>
            <div class="text-sm whitespace-pre-line">{{ grade.strengths }}</div>
          </div>
          <div v-if="grade.improvements" class="glass-ultraThin rounded-lg p-3" v-glass="{ strength: 'ultraThin', interactive: false }">
            <div class="text-sm font-medium mb-1">{{ t('teacher.grading.form.improvements') }}</div>
            <div class="text-sm whitespace-pre-line">{{ grade.improvements }}</div>
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
import PageHeader from '@/components/ui/PageHeader.vue'

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
