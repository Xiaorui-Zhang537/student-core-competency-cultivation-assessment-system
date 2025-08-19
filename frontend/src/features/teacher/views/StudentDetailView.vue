<template>
  <div class="p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <nav class="text-sm mb-2">
          <router-link to="/teacher/courses" class="text-gray-500 hover:text-blue-600">{{ t('teacher.courses.breadcrumb') }}</router-link>
          <span class="mx-2">/</span>
          <span class="font-medium">{{ studentName }}</span>
            </nav>
        <h1 class="text-3xl font-bold">{{ t('teacher.studentDetail.title', { name: studentName }) }}</h1>
      </div>

      <div v-if="gradeStore.loading" class="text-center card p-8">
          <p>{{ t('teacher.studentDetail.loading') }}</p>
            </div>
      
      <div v-else class="space-y-8">
          <!-- Profile Card + Actions -->
          <div class="card p-5 flex items-center justify-between">
            <div class="flex items-center gap-4">
              <div class="w-14 h-14 rounded-full overflow-hidden bg-gray-200">
                <img v-if="profile.avatar" :src="profile.avatar" class="w-full h-full object-cover" />
              </div>
              <div>
                <div class="text-xl font-semibold">{{ studentName }}</div>
                <div class="text-sm text-gray-500">{{ profile.email || '-' }}</div>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <Button variant="indigo" size="sm" @click="contactStudent">
                <ChatBubbleLeftRightIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.studentDetail.actions.contact') }}
              </Button>
              <Button variant="purple" size="sm" @click="viewOverview">
                <ChartPieIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.studentDetail.actions.overview') }}
              </Button>
              <Button variant="outline" size="sm" @click="exportGrades">
                <ArrowDownTrayIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.studentDetail.actions.export') }}
              </Button>
            </div>
          </div>
          
          <!-- Course Filter -->
          <div class="card p-4 flex items-center space-x-3">
            <label class="text-sm text-gray-600">{{ t('teacher.studentDetail.filter.label') }}</label>
            <select class="input input-sm w-72" v-model="selectedCourseId" @change="onCourseChange">
              <option :value="''">{{ t('teacher.studentDetail.filter.all') }}</option>
              <option v-for="c in studentCourses" :key="c.id" :value="String(c.id)">{{ c.title }}</option>
            </select>
          </div>
          <!-- Stats -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="card p-4 text-center">
                  <h3 class="text-2xl font-bold">{{ gradedAssignmentsCount }}</h3>
                  <p class="text-sm text-gray-500">{{ t('teacher.studentDetail.stats.graded') }}</p>
              </div>
              <div class="card p-4 text-center">
                  <h3 class="text-2xl font-bold">{{ averageScore.toFixed(1) }}</h3>
                  <p class="text-sm text-gray-500">{{ t('teacher.studentDetail.stats.average') }}</p>
              </div>
              <div class="card p-4 text-center">
                  <h3 class="text-2xl font-bold">{{ involvedCourses.length }}</h3>
                  <p class="text-sm text-gray-500">{{ t('teacher.studentDetail.stats.courses') }}</p>
              </div>
            </div>
          
          <!-- Grades Table -->
          <div class="card overflow-x-auto">
              <div class="card-header py-3">
                   <h2 class="text-xl font-semibold leading-tight">{{ t('teacher.studentDetail.table.title') }}</h2>
              </div>
              <table class="w-full text-left">
                  <thead>
                      <tr class="border-b">
                           <th class="p-4">{{ t('teacher.studentDetail.table.assignment') }}</th>
                           <th class="p-4">{{ t('teacher.studentDetail.table.course') }}</th>
                           <th class="p-4">{{ t('teacher.studentDetail.table.score') }}</th>
                           <th class="p-4">{{ t('teacher.studentDetail.table.teacher') }}</th>
                           <th class="p-4">{{ t('teacher.studentDetail.table.date') }}</th>
                           <th class="p-4">{{ t('teacher.studentDetail.table.status') }}</th>
                           <th class="p-4 text-right">{{ t('teacher.studentDetail.table.actions') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                      <tr v-for="grade in grades" :key="grade.id" class="border-b hover:bg-gray-50">
                          <td class="p-4 font-medium">{{ grade.assignmentTitle }}</td>
                          <td class="p-4">{{ grade.courseTitle }}</td>
                          <td class="p-4 font-bold">{{ grade.score }}</td>
                          <td class="p-4">{{ grade.teacherName }}</td>
                          <td class="p-4">{{ formatDate(grade.gradedAt) }}</td>
                          <td class="p-4">
                              <span class="badge" :class="grade.isPublished ? 'badge-success' : 'badge-warning'">
                                   {{ grade.isPublished ? t('teacher.studentDetail.table.published') : t('teacher.studentDetail.table.unpublished') }}
                              </span>
                      </td>
                      <td class="p-4">
                        <div class="flex items-center justify-end gap-2">
                          <Button variant="outline" size="sm" @click="viewSubmissions(grade)">
                            <DocumentTextIcon class="w-4 h-4 mr-1" />
                            {{ t('teacher.studentDetail.actions.viewSubmissions') }}
                          </Button>
                          <Button variant="teal" size="sm" @click="goRegrade(grade)">
                            <PencilSquareIcon class="w-4 h-4 mr-1" />
                            {{ t('teacher.studentDetail.actions.regrade') }}
                          </Button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              <div v-if="grades.length === 0" class="text-center p-8">
                  <p>{{ t('teacher.studentDetail.table.empty') }}</p>
              </div>
              <div class="p-4 flex items-center justify-between">
                <div class="flex items-center space-x-2">
                  <span class="text-sm text-gray-700">{{ t('teacher.studentDetail.table.perPage') }}</span>
                  <select class="input input-sm w-20" v-model.number="pageSize" @change="fetchPage(1)">
                    <option :value="10">10</option>
                    <option :value="20">20</option>
                    <option :value="50">50</option>
                  </select>
                  <span class="text-sm text-gray-700">{{ t('teacher.studentDetail.table.items') }}</span>
                </div>
                <div class="flex items-center space-x-2">
                  <button class="btn btn-sm btn-outline" :disabled="currentPage===1" @click="fetchPage(currentPage-1)">{{ t('teacher.studentDetail.table.prev') }}</button>
                  <span class="text-sm">{{ t('teacher.studentDetail.table.page', { page: currentPage, total: totalPages }) }}</span>
                  <button class="btn btn-sm btn-outline" :disabled="currentPage>=totalPages" @click="fetchPage(currentPage+1)">{{ t('teacher.studentDetail.table.next') }}</button>
                </div>
              </div>
          </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useGradeStore } from '@/stores/grade';

import { useCourseStore } from '@/stores/course';
import { useAuthStore } from '@/stores/auth';
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import { ChatBubbleLeftRightIcon, ChartPieIcon, ArrowDownTrayIcon, DocumentTextIcon, PencilSquareIcon } from '@heroicons/vue/24/outline'
import { teacherStudentApi } from '@/api/teacher-student.api'
const route = useRoute();
const router = useRouter();
const gradeStore = useGradeStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const { t } = useI18n()

const studentId = ref<string | null>(null);
// keep single t from useI18n to avoid redeclare
const studentName = ref(route.query.name as string || (t('teacher.students.table.student') as string));

const profile = ref<any>({})

const grades = computed(() => gradeStore.grades);

const gradedAssignmentsCount = computed(() => grades.value.length);

const averageScore = computed(() => {
    if (grades.value.length === 0) return 0;
    const total = grades.value.reduce((sum, grade) => sum + grade.score, 0);
    return total / grades.value.length;
});

const involvedCourses = computed(() => {
    const courseTitles = new Set(grades.value.map(g => g.courseTitle));
    return Array.from(courseTitles);
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = computed(() => gradeStore.totalGrades);
const totalPages = computed(() => Math.max(1, Math.ceil((total.value || 0) / pageSize.value)));
const selectedCourseId = ref<string>('');

const studentCourses = ref<any[]>([])

async function fetchPage(page: number) {
  if (!studentId.value) return;
  currentPage.value = page;
  await gradeStore.fetchGradesByStudent(studentId.value, { page: currentPage.value, size: pageSize.value, courseId: selectedCourseId.value || undefined });
}

function onCourseChange() {
  fetchPage(1);
}

function contactStudent() {
  alert(t('teacher.studentDetail.actions.contact'))
}

function viewOverview() {
  router.push('/teacher/analytics')
}

function exportGrades() {
  // 调用后端导出（先返回数据，后续可换为文件下载）
  const payload: any = { studentId: studentId.value, format: 'excel' }
  fetch(`${location.origin}/api/grades/export`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '' },
    body: JSON.stringify(payload)
  }).then(async r => {
    if (!r.ok) throw new Error('export failed')
    const res = await r.json()
    console.log('export data', res)
    alert(t('teacher.analytics.exportReport') as string)
  }).catch(() => alert(t('teacher.submissions.notify.remindFailed') as string))
}

function viewSubmissions(grade: any) {
  router.push(`/teacher/assignments/${grade.assignmentId}/submissions?highlightStudentId=${studentId.value}`)
}

function goRegrade(grade: any) {
  if ((grade as any).submissionId) {
    router.push(`/teacher/assignments/${grade.assignmentId}/submissions/${(grade as any).submissionId}/grade`)
  } else {
    viewSubmissions(grade)
  }
}

function formatDate(input: any): string {
  if (!input) return '-'
  // 兼容时间戳、ISO 字符串与可被 Date 解析的格式
  const num = Number(input)
  const date = Number.isFinite(num) && num > 0 ? new Date(num) : new Date(String(input))
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleDateString()
}

onMounted(async () => {
    const sid = (route.params as any).studentId as string || (route.params as any).id as string
    if (sid) {
        studentId.value = sid;
        try { profile.value = await teacherStudentApi.getStudentProfile(sid) } catch {}
        // 预取该学生相关课程（真实数据）
        try { studentCourses.value = await teacherStudentApi.getStudentCourses(sid) } catch {}
        fetchPage(1);
    }
});
</script>
