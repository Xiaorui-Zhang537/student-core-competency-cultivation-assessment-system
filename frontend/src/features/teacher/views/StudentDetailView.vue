<template>
  <div class="p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <router-link to="/teacher/courses" class="cursor-pointer pointer-events-auto hover:text-gray-700 dark:hover:text-gray-200">{{ t('teacher.courses.breadcrumb') }}</router-link>
          <ChevronRightIcon class="w-4 h-4 pointer-events-none" />
          <template v-if="route.query.courseId && route.query.courseTitle">
            <router-link :to="`/teacher/courses/${route.query.courseId}`" class="cursor-pointer pointer-events-auto hover:text-gray-700 dark:hover:text-gray-200">{{ String(route.query.courseTitle) }}</router-link>
            <ChevronRightIcon class="w-4 h-4 pointer-events-none" />
            <router-link :to="`/teacher/courses/${route.query.courseId}/students`" class="cursor-pointer pointer-events-auto hover:text-gray-700 dark:hover:text-gray-200">{{ t('teacher.students.breadcrumb.self') }}</router-link>
            <ChevronRightIcon class="w-4 h-4 pointer-events-none" />
          </template>
          <span class="font-medium text-gray-900 dark:text-white">{{ studentName }}</span>
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
          
          <!-- Grades Table (aligned with students management table style) -->
          <div class="card overflow-x-auto">
            <div class="card-header py-5 px-6 flex items-center justify-between">
              <div class="flex items-center gap-2">
                <AcademicCapIcon class="w-5 h-5 text-indigo-600" />
                <h2 class="text-xl font-semibold leading-tight text-gray-900 dark:text-white">{{ t('teacher.studentDetail.table.title') }}</h2>
                <span class="ml-2 text-sm text-gray-500">
                  {{ t('teacher.studentDetail.stats.graded') }}: {{ gradedAssignmentsCount }} · {{ t('teacher.studentDetail.stats.average') }}: {{ averageScore.toFixed(1) }}
                </span>
              </div>
              <div class="flex items-center space-x-2">
                <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('teacher.students.table.total', { count: total }) }}</span>
              </div>
            </div>
            <div class="border-b border-gray-200 dark:border-gray-700 mx-6"></div>
            <div class="pt-5 px-6 pb-4">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600 text-left">
              <thead class="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.assignment') }}</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.course') }}</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.score') }}</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.teacher') }}</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.date') }}</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.status') }}</th>
                  <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('teacher.studentDetail.table.actions') }}</th>
                </tr>
              </thead>
              <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-600">
                <tr v-for="grade in grades" :key="grade.id" class="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td class="px-6 py-4 font-medium">{{ grade.assignmentTitle }}</td>
                  <td class="px-6 py-4">{{ grade.courseTitle }}</td>
                  <td class="px-6 py-4 font-bold">{{ grade.score }}</td>
                  <td class="px-6 py-4">{{ grade.teacherName }}</td>
                  <td class="px-6 py-4">{{ formatDate(grade.gradedAt) }}</td>
                  <td class="px-6 py-4">
                    <span :class="grade.isPublished ? 'inline-block px-2 py-0.5 rounded text-xs bg-green-100 text-green-800' : 'inline-block px-2 py-0.5 rounded text-xs bg-yellow-100 text-yellow-800'">
                      {{ grade.isPublished ? t('teacher.studentDetail.table.published') : t('teacher.studentDetail.table.unpublished') }}
                    </span>
                  </td>
                  <td class="px-6 py-4">
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
            <!-- Pagination aligned with students management -->
            <div class="mt-6 flex items-center justify-between">
              <div class="flex items-center space-x-2">
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
                <select class="input input-sm w-20" v-model.number="pageSize" @change="fetchPage(1)">
                  <option :value="10">10</option>
                  <option :value="20">20</option>
                  <option :value="50">50</option>
                </select>
                <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.assignments.pagination.perPageSuffix') }}</span>
              </div>
              <div class="flex items-center space-x-2">
                <Button variant="outline" size="sm" :disabled="currentPage===1" @click="fetchPage(Math.max(1, currentPage-1))">{{ t('teacher.assignments.pagination.prev') }}</Button>
                <span class="text-sm">{{ t('teacher.assignments.pagination.page', { page: currentPage }) }}</span>
                <Button variant="outline" size="sm" :disabled="currentPage>=totalPages" @click="fetchPage(Math.min(totalPages, currentPage+1))">{{ t('teacher.assignments.pagination.next') }}</Button>
              </div>
            </div>
            </div>
          </div>
      </div>

      <!-- 聊天抽屉，与学生管理页保持一致的聊天体验 -->
      <teleport to="body">
        <ChatDrawer
          :open="chattingOpen"
          :peer-id="chattingPeerId || ''"
          :course-id="String(route.query.courseId || '')"
          :peer-name="chattingPeerName"
          @close="chattingOpen=false"
        />
      </teleport>
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
import { ChatBubbleLeftRightIcon, ChartPieIcon, ArrowDownTrayIcon, DocumentTextIcon, PencilSquareIcon, ChevronRightIcon, AcademicCapIcon } from '@heroicons/vue/24/outline'
import ChatDrawer from '@/features/teacher/components/ChatDrawer.vue'
import { teacherStudentApi } from '@/api/teacher-student.api'
const route = useRoute();
const router = useRouter();
const gradeStore = useGradeStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const { t } = useI18n()

const studentId = ref<string | null>(null);
const chattingOpen = ref(false)
const chattingPeerId = ref<string | null>(null)
const chattingPeerName = ref('')
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
  if (!studentId.value) return
  chattingPeerId.value = studentId.value
  chattingPeerName.value = String(studentName.value || '')
  chattingOpen.value = true
}

function viewOverview() {
  const cid = String(route.query.courseId || '')
  const sid = studentId.value ? String(studentId.value) : ''
  router.push({ name: 'TeacherAnalytics', query: { courseId: cid || undefined, studentId: sid || undefined } })
}

function exportGrades() {
  // 前端直接导出当前列表为 CSV（与其他导出方式保持一致体验）
  const rows = (grades.value || []).map((g: any) => ({
    assignment: g.assignmentTitle,
    course: g.courseTitle,
    score: g.score,
    teacher: g.teacherName,
    gradedAt: formatDate(g.gradedAt),
    status: g.isPublished ? 'published' : 'unpublished'
  }))
  const headers = ['assignment','course','score','teacher','graded_at','status']
  const esc = (v: any) => `"${String(v ?? '').replace(/"/g, '""')}"`
  const csv = [
    headers.join(','),
    ...rows.map(r => [esc(r.assignment), esc(r.course), String(r.score ?? ''), esc(r.teacher), esc(r.gradedAt), esc(r.status)].join(','))
  ].join('\n') + '\n'
  const blob = new Blob([csv], { type: 'text/csv;charset=UTF-8' })
  const link = document.createElement('a')
  const sid = studentId.value ? String(studentId.value) : 'unknown'
  link.href = URL.createObjectURL(blob)
  link.download = `student_${sid}_grades.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
        try {
          profile.value = await teacherStudentApi.getStudentProfile(sid)
          if (profile.value && (profile.value as any).name) {
            studentName.value = (profile.value as any).name
          }
        } catch {}
        // 预取该学生相关课程（真实数据）
        try { studentCourses.value = await teacherStudentApi.getStudentCourses(sid) } catch {}
        fetchPage(1);
    }
});
</script>
