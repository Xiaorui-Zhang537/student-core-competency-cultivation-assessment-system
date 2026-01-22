<template>
  <div class="p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <router-link to="/teacher/courses" class="cursor-pointer pointer-events-auto hover:text-gray-700 dark:hover:text-gray-200">{{ t('teacher.courses.breadcrumb') }}</router-link>
          <chevron-right-icon class="w-4 h-4 pointer-events-none" />
          <template v-if="route.query.courseId && route.query.courseTitle">
            <router-link :to="`/teacher/courses/${route.query.courseId}`" class="cursor-pointer pointer-events-auto hover:text-gray-700 dark:hover:text-gray-200">{{ String(route.query.courseTitle) }}</router-link>
            <chevron-right-icon class="w-4 h-4 pointer-events-none" />
            <router-link :to="`/teacher/courses/${route.query.courseId}/students`" class="cursor-pointer pointer-events-auto hover:text-gray-700 dark:hover:text-gray-200">{{ t('teacher.students.breadcrumb.self') }}</router-link>
            <chevron-right-icon class="w-4 h-4 pointer-events-none" />
          </template>
          <span class="font-medium text-gray-900 dark:text-white">{{ studentName }}</span>
        </nav>
        <page-header :title="t('teacher.studentDetail.title', { name: studentName })" :subtitle="''" />
      </div>

      <div class="space-y-8">
          <!-- Profile Card + Actions -->
          <Card padding="md" tint="info">
            <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:gap-6">
              <div class="flex items-center gap-4 min-w-0">
                <user-avatar v-if="profile.avatar" :avatar="profile.avatar" :size="56" :rounded="true" :fit="'cover'" />
                <div class="min-w-0">
                  <div class="flex items-center gap-3">
                    <span class="text-xl font-semibold truncate">{{ studentName }}</span>
                    <Badge v-if="profile.mbti" size="sm" :variant="mbtiVariant">MBTI · {{ profile.mbti }}</Badge>
                  </div>
                 </div>
              </div>
              <div class="flex items-center gap-2 sm:ml-auto">
                <Button variant="primary" size="sm" @click="contactStudent">
                  <chat-bubble-left-right-icon class="w-4 h-4 mr-1" />
                  {{ t('teacher.studentDetail.actions.contact') }}
                </Button>
                <Button variant="info" size="sm" @click="viewOverview">
                  <chart-pie-icon class="w-4 h-4 mr-1" />
                  {{ t('teacher.studentDetail.actions.overview') }}
                </Button>
                <Button variant="outline" size="sm" @click="exportGrades">
                  <arrow-down-tray-icon class="w-4 h-4 mr-1" />
                  {{ t('teacher.studentDetail.actions.export') }}
                </Button>
              </div>
            </div>
          </Card>

          <!-- Student Profile Info (Read-only) -->
          <Card padding="md" tint="secondary">
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
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.gender') }}</label>
                <p class="text-sm">{{ profile.gender ? t('shared.profile.genders.' + String(profile.gender).toLowerCase()) : t('shared.profile.status.notSet') }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.mbti') || 'MBTI' }}</label>
                <div class="text-sm">
                  <Badge v-if="profile.mbti" size="sm" :variant="mbtiVariant">{{ profile.mbti }}</Badge>
                  <span v-else>{{ t('shared.profile.status.notSet') }}</span>
                </div>
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
          </Card>
          
          <!-- Course Filter (glass, keep original position) -->
          <Card padding="sm" tint="accent" class="flex items-center gap-3 whitespace-nowrap">
            <label class="text-sm text-gray-600 whitespace-nowrap pr-2">{{ t('teacher.studentDetail.filter.label') }}</label>
            <glass-popover-select
              v-model="selectedCourseId"
              :options="[{ label: t('teacher.studentDetail.filter.all') as string, value: '' }, ...studentCourses.map((c:any)=>({ label: String(c.title||c.id), value: String(c.id) }))]"
              size="sm"
              width="18rem"
              @change="onCourseChange"
            />
          </Card>
          <!-- 关键指标（四卡一行，根据课程筛选变化） -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
            <start-card :label="t('teacher.studentDetail.stats.graded') as string" :value="gradedAssignmentsCount" tone="blue" :icon="DocumentTextIcon" />
            <start-card :label="t('teacher.studentDetail.stats.average') as string" :value="averageScore.toFixed(1)" tone="amber" :icon="StarIcon" />
            <start-card :label="t('teacher.studentDetail.stats.completionRate') || '完成率(%)'" :value="formatPercent(selectedCourseId ? courseProgress : profile.completionRate)" tone="emerald" :icon="ChartPieIcon" />
            <start-card :label="t('teacher.studentDetail.stats.lastActive') || '最近活跃'" :value="formatDateTime(profile.lastAccessTime)" tone="sky" :icon="ChatBubbleLeftRightIcon" />
          </div>

          <!-- 最近学习 + 能力雷达（左右并排） -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card padding="md" tint="info">
              <div class="flex items-center gap-2 mb-4">
                <document-text-icon class="w-5 h-5 text-indigo-600" />
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
            </Card>

            <Card padding="md" tint="secondary">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <chart-pie-icon class="w-5 h-5 text-emerald-600" />
                  <h3 class="text-lg font-semibold">{{ t('teacher.studentDetail.radar.title') || '能力雷达' }}</h3>
                </div>
                <Button size="sm" variant="outline" @click="viewOverview">{{ t('teacher.studentDetail.radar.viewInsights') || '查看洞察' }}</Button>
              </div>
              <div v-if="radarIndicators.length" class="w-full">
                <radar-chart :indicators="radarIndicators" :series="radarSeries" :height="'300px'" />
              </div>
              <div v-else class="text-sm text-gray-500 text-center">{{ t('teacher.analytics.charts.noRadar') }}</div>
            </Card>
          </div>

          <!-- 行为洞察（阶段二：AI解释与建议，不算分；学生7天仅一次，教师可重跑） -->
          <BehaviorInsightSection
            v-if="studentId"
            :student-id="String(studentId)"
            :course-id="selectedCourseId || (route.query.courseId ? String(route.query.courseId) : undefined)"
            range="7d"
            :allow-teacher-generate="true"
          />

          <!-- 行为证据（阶段一：纯代码聚合，不调用AI，不算分） -->
          <BehaviorEvidenceSection
            :student-id="studentId || undefined"
            :course-id="selectedCourseId || (route.query.courseId ? String(route.query.courseId) : undefined)"
            range="7d"
          />

          <!-- 风险预警与个性化建议 -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card padding="md" tint="warning">
              <h3 class="text-lg font-semibold mb-3">{{ t('teacher.studentDetail.alerts.title') || '风险预警' }}</h3>
              <div v-if="alerts.length===0" class="text-sm text-gray-500">{{ t('common.none') || '暂无' }}</div>
              <ul v-else class="space-y-2">
                <li v-for="a in alerts" :key="a.code + a.message" class="flex items-start gap-2">
                  <span :class="a.severity==='critical' ? 'text-red-600' : (a.severity==='warn' ? 'text-amber-600' : 'text-sky-600')">•</span>
                  <span class="text-sm">{{ a.message }}</span>
                </li>
              </ul>
            </Card>
            <Card padding="md" tint="success">
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
            </Card>
          </div>
          
          <!-- Grades Table (aligned with students management table style) -->
          <Card padding="lg" tint="info" class="overflow-x-auto">
            <div class="card-header py-5 px-6 flex items-center justify-between">
              <div class="flex items-center gap-2">
                <academic-cap-icon class="w-5 h-5 text-indigo-600" />
                <h2 class="text-xl font-semibold leading-tight text-gray-900 dark:text-white">{{ t('teacher.studentDetail.table.title') }}</h2>
                <span class="ml-2 text-sm text-gray-500">
                  {{ t('teacher.studentDetail.stats.graded') }}: {{ gradedAssignmentsCount }} · {{ t('teacher.studentDetail.stats.average') }}: {{ averageScore.toFixed(1) }}
                </span>
              </div>
              <div class="flex items-center gap-3">
                <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('teacher.students.table.total', { count: total }) }}</span>
                <span v-if="gradeStore.loading" class="text-xs text-gray-400">{{ t('teacher.studentDetail.loading') }}</span>
              </div>
            </div>
            <div class="border-b border-gray-200 dark:border-gray-700 mx-6"></div>
            <div class="pt-5 px-6 pb-4">
            <div class="rounded-xl overflow-hidden border border-white/10">
              <div v-if="gradeStore.loading" class="text-center p-6 text-sm text-gray-500">{{ t('teacher.studentDetail.loading') }}</div>
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
                    <Badge :variant="grade.isPublished ? 'success' : 'warning'" size="sm">
                      {{ grade.isPublished ? t('teacher.studentDetail.table.published') : t('teacher.studentDetail.table.unpublished') }}
                    </Badge>
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex items-center justify-end gap-2">
                      <Button variant="outline" size="sm" @click="viewSubmissions(grade)">
                        <document-text-icon class="w-4 h-4 mr-1" />
                        {{ t('teacher.studentDetail.actions.viewSubmissions') }}
                      </Button>
                      <Button variant="teal" size="sm" @click="goRegrade(grade)">
                        <pencil-square-icon class="w-4 h-4 mr-1" />
                        {{ t('teacher.studentDetail.actions.regrade') }}
                      </Button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
            </div>
            <div v-if="grades.length === 0" class="text-center p-8">
              <p>{{ t('teacher.studentDetail.table.empty') }}</p>
            </div>
            <!-- Pagination aligned with students management -->
            <div class="mt-6"></div>
            </div>
          </Card>
      </div>

      <!-- 独立分页行（在卡片外，且不额外包容器） -->
      <div class="px-6 py-3 flex items-center justify-between whitespace-nowrap">
        <div class="flex items-center gap-2 whitespace-nowrap">
          <span class="text-sm text-gray-700 dark:text-gray-300 whitespace-nowrap">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
          <glass-popover-select
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
import { onMounted, computed, ref, watch } from 'vue';
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
import Card from '@/components/ui/Card.vue'
import Badge from '@/components/ui/Badge.vue'
import { getMbtiVariant } from '@/shared/utils/badgeColor'
import { userApi } from '@/api/user.api'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import BehaviorEvidenceSection from '@/features/shared/views/BehaviorEvidenceSection.vue'
import BehaviorInsightSection from '@/features/shared/views/BehaviorInsightSection.vue'
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
const mbtiVariant = computed(() => getMbtiVariant((profile.value as any)?.mbti))
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
    const nextIndicators = dims.map((name:string) => ({ name, max: 100 }))
    const s = { name: t('teacher.studentDetail.radar.student') || '学生', values: data.studentScores || [] }
    const c = { name: t('teacher.studentDetail.radar.classAvg') || '班级', values: data.classAvgScores || [], color: '#10b981' }
    const nextSeries = [s, c]
    // 避免重复设置引发双次渲染
    const sameDims = JSON.stringify(radarIndicators.value) === JSON.stringify(nextIndicators)
    const sameSeries = JSON.stringify(radarSeries.value) === JSON.stringify(nextSeries)
    if (!sameDims) radarIndicators.value = nextIndicators
    if (!sameSeries) radarSeries.value = nextSeries
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
          const res = await teacherStudentApi.getStudentProfile(sid)
          const data: any = (res as any)?.data?.data ?? (res as any)?.data ?? (res as any)
          profile.value = {
            ...data,
            mbti: data?.mbti || data?.MBTI || data?.profile?.mbti || data?.profile?.MBTI || ''
          }
          try { (window as any).__studentDetailProfile = { source: 'teacherStudentApi', raw: res, data: profile.value } } catch {}
          // 若教师端学生资料未包含 mbti，则回退到通用用户资料接口补齐
          if (!profile.value.mbti) {
            try {
              const ures: any = await userApi.getProfileById(String(sid))
              const udata: any = (ures?.data?.data) ?? (ures?.data) ?? ures
              if (udata) {
                const resolvedMbti = udata?.mbti || udata?.MBTI || udata?.profile?.mbti || udata?.profile?.MBTI || ''
                if (resolvedMbti) {
                  profile.value.mbti = String(resolvedMbti)
                }
                // 兜底补齐常用展示字段（不覆盖已有）
                if (!profile.value.nickname && udata?.nickname) profile.value.nickname = udata.nickname
                if (!profile.value.username && udata?.username) profile.value.username = udata.username
                if (!profile.value.name && (udata?.name || (udata?.lastName || '') + (udata?.firstName || ''))) profile.value.name = udata.name || ((udata?.lastName || '') + (udata?.firstName || ''))
              }
              try { (window as any).__studentDetailProfileFallback = { source: 'userApi', raw: ures, data: udata, merged: profile.value } } catch {}
            } catch {}
          }
          if (profile.value && (profile.value as any).name) {
            studentName.value = (profile.value as any).name
          }
        } catch {}
        // 并行拉取，减少首帧阻塞；失败不阻断其他任务
        const tasks: Array<Promise<any>> = [
          (async () => { try { studentCourses.value = await teacherStudentApi.getStudentCourses(sid) } catch {} })(),
          (async () => { try { await fetchActivity() } catch {} })(),
          (async () => { try { await fetchAbilityRadar() } catch {} })(),
          (async () => { try { await fetchAlertsAndRecommendations() } catch {} })(),
          (async () => { try { await fetchPage(1) } catch {} })()
        ]
        await Promise.allSettled(tasks)
    }
});
</script>
