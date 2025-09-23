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
        <PageHeader :title="t('teacher.studentDetail.title', { name: studentName })" :subtitle="''" />
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
              <Button variant="primary" size="sm" @click="contactStudent">
                <ChatBubbleLeftRightIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.studentDetail.actions.contact') }}
              </Button>
              <Button variant="info" size="sm" @click="viewOverview">
                <ChartPieIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.studentDetail.actions.overview') }}
              </Button>
              <Button variant="outline" size="sm" @click="exportGrades">
                <ArrowDownTrayIcon class="w-4 h-4 mr-1" />
                {{ t('teacher.studentDetail.actions.export') }}
              </Button>
            </div>
          </div>

          <!-- Student Profile Info (Read-only) -->
          <div class="card p-5">
            <h3 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.profileInfo') || '个人信息' }}</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.username') }}</label>
                <p class="text-sm">{{ profile.username || '-' }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.email') }}</label>
                <p class="text-sm">{{ profile.email || '-' }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.studentNo') }}</label>
                <p class="text-sm">{{ profile.studentNo || '-' }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">MBTI</label>
                <p class="text-sm">{{ profile.mbti || t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.gender') }}</label>
                <p class="text-sm">{{ profile.gender ? t('shared.profile.genders.' + String(profile.gender).toLowerCase()) : t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.school') }}</label>
                <p class="text-sm">{{ profile.school || t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.subject') }}</label>
                <p class="text-sm">{{ profile.subject || t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.phone') }}</label>
                <p class="text-sm">{{ profile.phone || t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.birthday') }}</label>
                <p class="text-sm">{{ profile.birthday ? formatDate(profile.birthday) : t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.country') }}</label>
                <p class="text-sm">{{ profile.country || t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.province') }}</label>
                <p class="text-sm">{{ profile.province || t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.city') }}</label>
                <p class="text-sm">{{ profile.city || t('shared.profile.status.notSet') }}</p>
              </div>
            </div>
          </div>
          
          <!-- Course Filter (glass, keep original position) -->
          <div class="card p-4 flex items-center gap-3 whitespace-nowrap">
            <label class="text-sm text-gray-600 whitespace-nowrap">{{ t('teacher.studentDetail.filter.label') }}</label>
            <GlassPopoverSelect
              v-model="selectedCourseId"
              :options="[{ label: t('teacher.studentDetail.filter.all') as string, value: '' }, ...studentCourses.map((c:any)=>({ label: String(c.title||c.id), value: String(c.id) }))]"
              size="sm"
              width="18rem"
              @change="onCourseChange"
            />
          </div>
          <!-- 关键指标（四卡一行，根据课程筛选变化） -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
            <StartCard :label="t('teacher.studentDetail.stats.graded') as string" :value="gradedAssignmentsCount" tone="blue" :icon="DocumentTextIcon" />
            <StartCard :label="t('teacher.studentDetail.stats.average') as string" :value="averageScore.toFixed(1)" tone="amber" :icon="StarIcon" />
            <StartCard :label="t('teacher.studentDetail.stats.completionRate') || '完成率(%)'" :value="formatPercent(selectedCourseId ? courseProgress : profile.completionRate)" tone="emerald" :icon="ChartPieIcon" />
            <StartCard :label="t('teacher.studentDetail.stats.lastActive') || '最近活跃'" :value="formatDateTime(profile.lastAccessTime)" tone="sky" :icon="ChatBubbleLeftRightIcon" />
          </div>

          <!-- 最近学习 + 能力雷达（左右并排） -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="card p-5">
              <div class="flex items-center gap-2 mb-4">
                <DocumentTextIcon class="w-5 h-5 text-indigo-600" />
                <h3 class="text-lg font-semibold">{{ t('teacher.studentDetail.recentStudy') || '最近学习' }}</h3>
              </div>
              <div class="space-y-3">
                <template v-if="recentEvents.length > 0">
                  <div v-for="ev in recentEvents" :key="String(ev.eventType)+'-'+String(ev.occurredAt)+'-'+String(ev.title||'')" class="flex items-center justify-between">
                    <div class="flex items-center gap-2 min-w-0">
                      <span
                        class="inline-block w-2 h-2 rounded-full"
                        :class="{
                          'bg-indigo-500': ev.eventType==='lesson',
                          'bg-emerald-500': ev.eventType==='submission',
                          'bg-amber-500': ev.eventType==='quiz',
                          'bg-sky-500': ev.eventType==='discussion',
                          'bg-gray-400': ev.eventType==='visit'
                        }"
                      ></span>
                      <div class="truncate">
                        <div class="font-medium truncate">{{ ev.title || fallbackEventTitle(ev.eventType) }}</div>
                        <div class="text-sm text-gray-500 truncate">{{ ev.courseTitle || (ev.courseId ? ('#'+ev.courseId) : '') }}</div>
                      </div>
                    </div>
                    <div class="text-sm text-gray-500">{{ formatDateTime(ev.occurredAt) }}</div>
                  </div>
                </template>
                <template v-else>
                  <div class="text-sm text-gray-500">{{ t('common.empty') || '暂无数据' }}</div>
                </template>
              </div>
            </div>

            <div class="card p-5">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <ChartPieIcon class="w-5 h-5 text-emerald-600" />
                  <h3 class="text-lg font-semibold">{{ t('teacher.studentDetail.radar.title') || '能力雷达' }}</h3>
                </div>
                <Button size="sm" variant="outline" @click="viewOverview">{{ t('teacher.studentDetail.radar.viewInsights') || '查看洞察' }}</Button>
              </div>
              <RadarChart :indicators="radarIndicators" :series="radarSeries" :height="'300px'" />
            </div>
          </div>

          <!-- 风险预警与个性化建议 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="card p-5">
              <h3 class="text-lg font-semibold mb-3">{{ t('teacher.studentDetail.alerts.title') || '风险预警' }}</h3>
              <div v-if="alerts.length===0" class="text-sm text-gray-500">{{ t('common.none') || '暂无' }}</div>
              <ul v-else class="space-y-2">
                <li v-for="a in alerts" :key="a.code + a.message" class="flex items-start gap-2">
                  <span :class="a.severity==='critical' ? 'text-red-600' : (a.severity==='warn' ? 'text-amber-600' : 'text-sky-600')">•</span>
                  <span class="text-sm">{{ a.message }}</span>
                </li>
              </ul>
            </div>
            <div class="card p-5">
              <h3 class="text-lg font-semibold mb-3">{{ t('teacher.studentDetail.recommendations.title') || '个性化建议' }}</h3>
              <div v-if="recommendations.length===0" class="text-sm text-gray-500">{{ t('common.empty') || '暂无数据' }}</div>
              <div v-else class="space-y-3">
                <div v-for="r in recommendations" :key="r.id" class="border border-white/10 rounded p-3">
                  <div class="font-medium mb-1 truncate">{{ r.title }}</div>
                  <div class="text-sm text-gray-500 line-clamp-2">{{ r.description }}</div>
                  <div class="mt-2 text-xs text-gray-400 flex items-center gap-2">
                    <span>{{ r.recommendationType }}</span>
                    <span v-if="r.estimatedTime">· {{ r.estimatedTime }}</span>
                    <a v-if="r.resourceUrl" class="text-indigo-600 hover:underline" :href="r.resourceUrl" target="_blank">{{ t('common.view') || '查看' }}</a>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Grades Table (aligned with students management table style) -->
          <div class="card overflow-x-auto glass-regular glass-interactive">
            <div class="card-header py-5 px-6 flex items-center justify-between">
              <div class="flex items-center gap-2">
                <AcademicCapIcon class="w-5 h-5 text-indigo-600" />
                <h2 class="text-xl font-semibold leading-tight text-gray-900 dark:text-white">{{ t('teacher.studentDetail.table.title') }}</h2>
                <span class="ml-2 text-sm text-gray-500">
                  {{ t('teacher.studentDetail.stats.graded') }}: {{ gradedAssignmentsCount }} · {{ t('teacher.studentDetail.stats.average') }}: {{ averageScore.toFixed(1) }}
                </span>
              </div>
              <div class="flex items-center gap-3">
                <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('teacher.students.table.total', { count: total }) }}</span>
              </div>
            </div>
            <div class="border-b border-gray-200 dark:border-gray-700 mx-6"></div>
            <div class="pt-5 px-6 pb-4">
            <table class="min-w-full divide-y divide-white/10 text-left">
              <thead class="bg-transparent">
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
              <tbody class="bg-transparent divide-y divide-white/10">
                <tr v-for="grade in grades" :key="grade.id" class="hover:bg-white/10 transition-colors duration-150">
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
            <div class="mt-6"></div>
            </div>
          </div>
      </div>

      <!-- 独立分页行（在卡片外，且不额外包容器） -->
      <div class="px-6 py-3 flex items-center justify-between whitespace-nowrap">
        <div class="flex items-center gap-2 whitespace-nowrap">
          <span class="text-sm text-gray-700 dark:text-gray-300 whitespace-nowrap">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
          <GlassPopoverSelect
            v-model="pageSize as any"
            :options="[{label:'10',value:10},{label:'20',value:20},{label:'50',value:50}]"
            size="sm"
            width="5rem"
            :teleport="false"
            @change="fetchPage(1)"
          />
          <span class="text-sm text-gray-700 dark:text-gray-300 whitespace-nowrap">{{ t('teacher.assignments.pagination.perPageSuffix') }}</span>
        </div>
        <div class="flex items-center space-x-2">
          <Button variant="outline" size="sm" :disabled="currentPage===1" @click="fetchPage(Math.max(1, currentPage-1))">{{ t('teacher.assignments.pagination.prev') }}</Button>
          <span class="text-sm">{{ t('teacher.assignments.pagination.page', { page: currentPage }) }}</span>
          <Button variant="outline" size="sm" :disabled="currentPage>=totalPages" @click="fetchPage(Math.min(totalPages, currentPage+1))">{{ t('teacher.assignments.pagination.next') }}</Button>
        </div>
      </div>

      <!-- 改为调用全局抽屉：删除本地 Teleport -->
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
import StartCard from '@/components/ui/StartCard.vue'
import { ChatBubbleLeftRightIcon, ChartPieIcon, ArrowDownTrayIcon, DocumentTextIcon, PencilSquareIcon, ChevronRightIcon, AcademicCapIcon } from '@heroicons/vue/24/outline'
import { StarIcon } from '@heroicons/vue/24/outline'
import { useChatStore } from '@/stores/chat'
import { teacherStudentApi } from '@/api/teacher-student.api'
import RadarChart from '@/components/charts/RadarChart.vue'
import { teacherApi } from '@/api/teacher.api'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
const route = useRoute();
const router = useRouter();
const gradeStore = useGradeStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const { t, locale } = useI18n()

const studentId = ref<string | null>(null);
const chat = useChatStore()
// keep single t from useI18n to avoid redeclare
const studentName = ref(route.query.name as string || (t('teacher.students.table.student') as string));

const profile = ref<any>({})
const recentLessons = ref<Array<{lessonId:number;lessonTitle:string;courseId:number;courseTitle:string;studiedAt:any}>>([])
const recentEvents = ref<Array<{eventType:string; title?:string; courseId?:number; courseTitle?:string; occurredAt:any; durationSeconds?:number; link?:string}>>([])
const radarIndicators = ref<Array<{name:string;max:number}>>([])
const radarSeries = ref<Array<{name:string;values:number[];color?:string}>>([])
const alerts = ref<Array<{code:string;message:string;severity:string}>>([])
const recommendations = ref<Array<any>>([])

const grades = computed(() => gradeStore.grades);

// 选中课程下的过滤视图统计
const filteredGrades = computed(() => {
    if (!selectedCourseId.value) return grades.value
    return grades.value.filter((g: any) => String(g.courseId || g.courseTitle || '') === String(selectedCourseId.value) || String(g.courseId || '') === String(selectedCourseId.value))
})

const gradedAssignmentsCount = computed(() => filteredGrades.value.length)

const averageScore = computed(() => {
    if (filteredGrades.value.length === 0) return 0
    const total = filteredGrades.value.reduce((sum: number, g: any) => sum + Number(g.score || 0), 0)
    return total / filteredGrades.value.length
})

const courseProgress = computed(() => {
    // 若已在后端 enrollment.progress 返回，可在拉取课程时补充存入 profile 或 studentCourses
    // 这里作为前端兜底：用成绩条目完成率近似（仅当课程选择器选中某课程时）
    if (!selectedCourseId.value) return profile.value?.completionRate
    // 无法可靠计算时返回 profile 的总体完成率
    return profile.value?.completionRate
})

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
  chat.openChat(String(studentId.value), String(studentName.value || ''), String(route.query.courseId || ''))
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

function formatDateTime(input: any): string {
  if (!input) return '-'
  const num = Number(input)
  const date = Number.isFinite(num) && num > 0 ? new Date(num) : new Date(String(input))
  if (isNaN(date.getTime())) return '-'
  // 按语言环境格式化去掉年份：
  // zh-CN: MM月dd日 HH:mm
  // en-US: MMM d HH:mm
  const isEn = String(locale?.value || 'zh-CN').toLowerCase().startsWith('en')
  if (!isEn) {
    const pad2 = (v: number) => (v < 10 ? '0' + v : String(v))
    const m = pad2(date.getMonth() + 1)
    const d = pad2(date.getDate())
    const hh = pad2(date.getHours())
    const mm = pad2(date.getMinutes())
    return `${m}月${d}日 ${hh}:${mm}`
  }
  const opts: Intl.DateTimeFormatOptions = { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }
  return new Intl.DateTimeFormat('en-US', opts).format(date).replace(',', '')
}

function formatPercent(v: any): string {
  if (v === null || v === undefined) return '-'
  const num = Number(v)
  if (!Number.isFinite(num)) return '-'
  return String(Math.round(num * 10) / 10)
}

async function fetchActivity() {
  if (!studentId.value) return
  try {
    const act = await teacherStudentApi.getStudentActivity(studentId.value, 7, 8)
    recentLessons.value = (act as any)?.recentLessons || []
    recentEvents.value = (act as any)?.recentEvents || []
    if (!recentEvents.value?.length && recentLessons.value?.length) {
      recentEvents.value = recentLessons.value.map((r:any)=>({ eventType:'lesson', title:r.lessonTitle, courseId:r.courseId, courseTitle:r.courseTitle, occurredAt:r.studiedAt }))
    }
  } catch {}
}

async function fetchAbilityRadar() {
  try {
    // 若带 courseId 则按该课程上下文拉取；否则尝试学生最近课程
    const courseId = String(route.query.courseId || '')
    if (!studentId.value || !courseId) return
    const today = new Date()
    const start = new Date(today.getTime() - 30*24*60*60*1000)
    const pad = (n:number) => (n<10? '0'+n : String(n))
    const fmt = (d:Date) => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`
    const resp = await teacherApi.getAbilityRadar({ courseId, studentId: String(studentId.value), startDate: fmt(start), endDate: fmt(today) })
    const data: any = (resp as any).data || resp
    const dims: string[] = data.dimensions || []
    radarIndicators.value = dims.map((name:string) => ({ name, max: 100 }))
    const s = { name: t('teacher.studentDetail.radar.student') || '学生', values: data.studentScores || [] }
    const c = { name: t('teacher.studentDetail.radar.classAvg') || '班级', values: data.classAvgScores || [], color: '#10b981' }
    radarSeries.value = [s, c]
  } catch {}
}

async function fetchAlertsAndRecommendations() {
  if (!studentId.value) return
  try {
    const a = await teacherStudentApi.getStudentAlerts(studentId.value)
    alerts.value = a?.alerts || []
  } catch {}
  try {
    recommendations.value = await teacherStudentApi.getStudentRecommendations(studentId.value, 6)
  } catch {}
}

function fallbackEventTitle(t: string): string {
  switch ((t||'').toLowerCase()) {
    case 'lesson': return '学习章节';
    case 'submission': return '提交作业';
    case 'quiz': return '测验答题';
    case 'discussion': return '讨论互动';
    case 'visit': return '访问课程';
    default: return '学习活动';
  }
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
        // 学生活跃度
        fetchActivity()
        // 能力雷达
        fetchAbilityRadar()
        // 预警与建议
        fetchAlertsAndRecommendations()
        fetchPage(1);
    }
});
</script>
