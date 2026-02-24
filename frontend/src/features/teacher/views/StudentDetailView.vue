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
          <card padding="md" tint="info">
            <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:gap-6">
              <div class="flex items-center gap-4 min-w-0">
                <user-avatar v-if="profile.avatar" :avatar="profile.avatar" :size="56" :rounded="true" :fit="'cover'" />
                <div class="min-w-0">
                  <div class="flex items-center gap-3">
                    <span class="text-xl font-semibold truncate">{{ studentName }}</span>
                    <badge v-if="profile.mbti" size="sm" :variant="mbtiVariant">MBTI · {{ profile.mbti }}</badge>
                  </div>
                 </div>
              </div>
              <div class="flex items-center gap-2 sm:ml-auto">
                <Button variant="primary" size="sm" @click="contactStudent">
                  <chat-bubble-left-right-icon class="w-4 h-4 mr-1" />
                  {{ t('teacher.studentDetail.actions.contact') }}
                </Button>
                <Button variant="outline" size="sm" icon="download" :loading="exportingReport" @click="exportReportPdf">
                  {{ t('teacher.studentDetail.actions.exportReport') }}
                </Button>
                <Button variant="outline" size="sm" @click="exportGrades">
                  <arrow-down-tray-icon class="w-4 h-4 mr-1" />
                  {{ t('teacher.studentDetail.actions.export') }}
                </Button>
              </div>
            </div>
          </card>

          <!-- Student Profile Info (Read-only) -->
          <card padding="md" tint="secondary">
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
                  <badge v-if="profile.mbti" size="sm" :variant="mbtiVariant">{{ profile.mbti }}</badge>
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
          </card>
          
          <!-- Course Filter (glass, keep original position) -->
          <card padding="sm" tint="accent" class="flex items-center gap-3 whitespace-nowrap">
            <label class="text-sm text-gray-600 whitespace-nowrap pr-2">{{ t('teacher.studentDetail.filter.label') }}</label>
            <glass-popover-select
              v-model="selectedCourseId"
              :options="[{ label: t('teacher.studentDetail.filter.all') as string, value: '' }, ...studentCourses.map((c:any)=>({ label: String(c.title||c.id), value: String(c.id) }))]"
              size="sm"
              width="18rem"
              @change="onCourseChange"
            />
          </card>
          <!-- 关键指标（四卡一行，根据课程筛选变化） -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
            <start-card :label="t('teacher.studentDetail.stats.graded') as string" :value="gradedAssignmentsCount" tone="blue" :icon="DocumentTextIcon" />
            <start-card :label="t('teacher.studentDetail.stats.average') as string" :value="averageScore.toFixed(1)" tone="amber" :icon="StarIcon" />
            <start-card :label="t('teacher.studentDetail.stats.completionRate') || '完成率(%)'" :value="formatPercent(selectedCourseId ? courseProgress : profile.completionRate)" tone="emerald" :icon="ChartPieIcon" />
            <start-card :label="t('teacher.studentDetail.stats.lastActive') || '最近活跃'" :value="formatDateTime(profile.lastAccessTime)" tone="sky" :icon="ChatBubbleLeftRightIcon" />
          </div>

          <!-- 最近学习 + 能力雷达（左右并排） -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <card padding="md" tint="info">
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
                          'bg-gray-400': ev.eventType==='visit',
                          'bg-fuchsia-500': ev.eventType==='ai',
                          'bg-violet-500': ev.eventType==='community_ask',
                          'bg-teal-500': ev.eventType==='community_answer'
                        }"
                      ></span>
                      <div class="truncate">
                        <div class="font-medium truncate">
                          <button
                            v-if="ev.link"
                            class="hover:underline text-left truncate max-w-full"
                            @click="openRecentEventLink(ev.link)"
                          >
                            {{ formatRecentEventTitle(ev) }}
                          </button>
                          <span v-else>{{ formatRecentEventTitle(ev) }}</span>
                        </div>
                        <div class="text-sm text-gray-500 truncate">
                          <span>{{ ev.courseTitle || (ev.courseId ? ('#'+ev.courseId) : '') }}</span>
                          <span v-if="ev.durationSeconds" class="mx-2">·</span>
                          <span v-if="ev.durationSeconds">{{ formatDuration(ev.durationSeconds) }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="text-sm text-gray-500">{{ formatDateTime(ev.occurredAt) }}</div>
                  </div>
                </template>
                <template v-else>
                  <div class="text-sm text-gray-500">{{ t('common.empty') || '暂无数据' }}</div>
                </template>
              </div>
            </card>

            <card padding="md" tint="secondary">
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <chart-pie-icon class="w-5 h-5 text-emerald-600" />
                  <h3 class="text-lg font-semibold">{{ t('teacher.studentDetail.radar.title') || '能力雷达' }}</h3>
                </div>
              </div>
              <div v-if="radarIndicators.length" class="w-full">
                <radar-chart :indicators="radarIndicators" :series="radarSeries" :height="'300px'" />
              </div>
              <div v-else class="text-sm text-gray-500 text-center">{{ t('teacher.analytics.charts.noRadar') }}</div>
            </card>
          </div>

          <!-- 行为洞察（阶段二：AI解释与建议，不算分；学生7天仅一次，教师可重跑） -->
          <div ref="reportInsightWrap">
            <behavior-insight-section
              v-if="studentId"
              :student-id="String(studentId)"
              :course-id="selectedCourseId || (route.query.courseId ? String(route.query.courseId) : undefined)"
              range="7d"
              :allow-teacher-generate="true"
              @update:insight="(v:any) => (behaviorInsight = v)"
            />
          </div>

          <!-- 行为证据（阶段一：纯代码聚合，不调用AI，不算分） -->
          <div ref="reportEvidenceWrap">
          <behavior-evidence-section
            v-if="studentId"
            :student-id="String(studentId)"
            :course-id="selectedCourseId || (route.query.courseId ? String(route.query.courseId) : undefined)"
            range="7d"
            @update:summary="(v:any) => (behaviorEvidenceSummary = v)"
          />
          </div>

          <!-- 风险预警/行动建议：改为完全从“洞察（AI）”读取（v2 结构化输出），不再展示规则容器 -->
          
          <!-- 成绩记录（重做为与“学生管理”一致的表格风格） -->
          <div ref="reportGradesWrap">
          <card padding="md" tint="secondary">
            <!-- 统计信息放在标题行右侧并右对齐 -->
            <div class="flex items-center justify-between gap-3 mb-4">
              <div class="flex items-center gap-2 min-w-0">
                <academic-cap-icon class="w-5 h-5 text-indigo-600" />
                <h3 class="text-lg font-semibold truncate">{{ t('teacher.studentDetail.table.title') }}</h3>
              </div>
              <div class="flex items-center gap-3 whitespace-nowrap">
                <span class="text-sm text-gray-500 dark:text-gray-400">
                  {{ t('teacher.studentDetail.stats.graded') }}: {{ gradedAssignmentsCount }} · {{ t('teacher.studentDetail.stats.average') }}: {{ averageScore.toFixed(1) }}
                </span>
                <span v-if="gradeStore.loading" class="text-xs text-gray-400">{{ t('teacher.studentDetail.loading') }}</span>
              </div>
            </div>

            <div class="overflow-x-auto no-scrollbar rounded-xl overflow-hidden glass-regular glass-interactive border border-gray-200/40 dark:border-gray-700/40">
              <div v-if="gradeStore.loading" class="text-center p-6 text-sm text-gray-500">{{ t('teacher.studentDetail.loading') }}</div>
              <table v-else class="min-w-full divide-y divide-white/10">
                <thead class="bg-white/5 dark:bg-white/5 backdrop-blur-sm">
                  <tr>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide min-w-[220px]">
                      {{ t('teacher.studentDetail.table.assignment') }}
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide min-w-[180px]">
                      {{ t('teacher.studentDetail.table.course') }}
                    </th>
                    <th class="px-4 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-20">
                      {{ t('teacher.studentDetail.table.score') }}
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide min-w-[120px]">
                      {{ t('teacher.studentDetail.table.teacher') }}
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap min-w-[120px]">
                      {{ t('teacher.studentDetail.table.date') }}
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-24">
                      {{ t('teacher.studentDetail.table.status') }}
                    </th>
                    <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap w-[220px]">
                      {{ t('teacher.studentDetail.table.actions') }}
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-transparent divide-y divide-white/10">
                  <tr v-for="grade in grades" :key="grade.id" class="hover:bg-white/10 transition-colors duration-150">
                    <td class="px-6 py-3 text-center">
                      <div class="text-sm font-medium text-gray-900 dark:text-white truncate">
                        {{ grade.assignmentTitle }}
                      </div>
                    </td>
                    <td class="px-6 py-3 text-center">
                      <div class="text-sm text-gray-700 dark:text-gray-200 truncate">
                        {{ grade.courseTitle }}
                      </div>
                    </td>
                    <td class="px-4 py-3 text-center">
                      <span class="text-sm font-semibold text-gray-900 dark:text-white">{{ grade.score }}</span>
                    </td>
                    <td class="px-6 py-3 text-center">
                      <div class="text-sm text-gray-700 dark:text-gray-200 truncate">{{ grade.teacherName }}</div>
                    </td>
                    <td class="px-6 py-3 text-center text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">
                      {{ formatDate(grade.gradedAt) }}
                    </td>
                    <td class="px-6 py-3 text-center">
                      <badge :variant="grade.isPublished ? 'success' : 'warning'" size="sm" class="whitespace-nowrap">
                        {{ grade.isPublished ? t('teacher.studentDetail.table.published') : t('teacher.studentDetail.table.unpublished') }}
                      </badge>
                    </td>
                    <td class="px-6 py-3 text-center">
                      <div class="flex items-center justify-center gap-2">
                        <Button variant="outline" size="sm" class="whitespace-nowrap" @click="viewSubmissions(grade)">
                          <document-text-icon class="w-4 h-4 mr-1" />
                          {{ t('teacher.studentDetail.actions.viewSubmissions') }}
                        </Button>
                        <Button variant="teal" size="sm" class="whitespace-nowrap" @click="goRegrade(grade)">
                          <pencil-square-icon class="w-4 h-4 mr-1" />
                          {{ t('teacher.studentDetail.actions.regrade') }}
                        </Button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="!gradeStore.loading && grades.length === 0" class="text-center p-8 text-sm text-gray-500">
              {{ t('teacher.studentDetail.table.empty') }}
            </div>

            <!-- 分页：左侧总数+每页选择，右侧统一分页组件 -->
            <pagination-bar
              class="mt-4"
              :page="currentPage"
              :page-size="pageSize"
              :total-pages="totalPages"
              :total-items="total"
              :disabled="gradeStore.loading"
              :page-size-options="[10, 20, 50]"
              @update:page="handleGradePageChange"
              @update:pageSize="handleGradePageSizeChange"
            />
          </card>
          </div>
      </div>

      <!-- 改为调用全局抽屉：删除本地 Teleport -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, watch, nextTick } from 'vue';
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
import PaginationBar from '@/components/ui/PaginationBar.vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { gradeApi } from '@/api/grade.api'
import { useUIStore } from '@/stores/ui'
const route = useRoute();
const router = useRouter();
const gradeStore = useGradeStore();
const courseStore = useCourseStore();
const authStore = useAuthStore();
const { t, locale } = useI18n()
const uiStore = useUIStore()

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
// 预留：若未来需要对“班级缺数据”给出提示，可在此扩展
const showClassAvgNoDataHint = ref(false)
// 行为洞察：由子组件回传（用于后续扩展，例如：父组件层面的埋点/统计）
let behaviorInsight: any = null
let behaviorEvidenceSummary: any = null

// 导出报告（PDF）：洞察 + 行为证据 + 成绩记录
const exportingReport = ref(false)
const reportInsightWrap = ref<HTMLElement | null>(null)
const reportEvidenceWrap = ref<HTMLElement | null>(null)
const reportGradesWrap = ref<HTMLElement | null>(null)

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

// 确保 studentId 在首帧渲染前尽量就绪，避免子组件首帧以 undefined 发请求（导致“studentId 必填”）
const resolvedStudentId = computed(() => {
  const sid = (route.params as any).studentId as string || (route.params as any).id as string
  return sid ? String(sid) : ''
})
watch(resolvedStudentId, (sid) => {
  if (sid) studentId.value = sid
}, { immediate: true })

async function fetchPage(page: number) {
  if (!studentId.value) return;
  currentPage.value = page;
  await gradeStore.fetchGradesByStudent(studentId.value, { page: currentPage.value, size: pageSize.value, courseId: selectedCourseId.value || undefined });
}

function handleGradePageChange(page: number) {
  if (page === currentPage.value) return
  fetchPage(page)
}

function handleGradePageSizeChange(size: number) {
  const n = Math.max(1, Math.floor(Number(size || 10)))
  if (n === pageSize.value) return
  pageSize.value = n
  fetchPage(1)
}

function onCourseChange() {
  fetchPage(1);
}

function contactStudent() {
  if (!studentId.value) return
  chat.openChat(String(studentId.value), String(studentName.value || ''), String(route.query.courseId || ''))
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

function safeFilename(input: string) {
  return String(input || 'report')
    .trim()
    .replace(/[\\\/:*?"<>|\n\r\t]+/g, '_')
    .slice(0, 80)
}

function cloneForExport(el: HTMLElement) {
  const clone = el.cloneNode(true) as HTMLElement
  // 移除按钮等交互元素（只保留展示内容）
  clone.querySelectorAll('button').forEach(b => b.remove())
  // 移除可能的 svg loading/spinner（避免导出时出现转圈）
  clone.querySelectorAll('svg.animate-spin').forEach(s => s.remove())
  return clone
}

function buildGradesTableForExport(allGrades: any[]) {
  const wrap = document.createElement('div')
  wrap.className = 'rounded-xl overflow-hidden border border-gray-200/40'
  wrap.innerHTML = `
    <table class="min-w-full divide-y divide-white/10">
      <thead class="bg-white/5 backdrop-blur-sm">
        <tr>
          <th class="px-6 py-3 text-center text-xs font-semibold tracking-wide min-w-[220px]">${String(t('teacher.studentDetail.table.assignment'))}</th>
          <th class="px-6 py-3 text-center text-xs font-semibold tracking-wide min-w-[180px]">${String(t('teacher.studentDetail.table.course'))}</th>
          <th class="px-4 py-3 text-center text-xs font-semibold tracking-wide whitespace-nowrap w-20">${String(t('teacher.studentDetail.table.score'))}</th>
          <th class="px-6 py-3 text-center text-xs font-semibold tracking-wide min-w-[120px]">${String(t('teacher.studentDetail.table.teacher'))}</th>
          <th class="px-6 py-3 text-center text-xs font-semibold tracking-wide whitespace-nowrap min-w-[120px]">${String(t('teacher.studentDetail.table.date'))}</th>
          <th class="px-6 py-3 text-center text-xs font-semibold tracking-wide whitespace-nowrap w-24">${String(t('teacher.studentDetail.table.status'))}</th>
        </tr>
      </thead>
      <tbody class="bg-transparent divide-y divide-white/10">
        ${(allGrades || []).map((g: any) => `
          <tr>
            <td class="px-6 py-3 text-center"><div class="text-sm font-medium truncate">${String(g.assignmentTitle || '')}</div></td>
            <td class="px-6 py-3 text-center"><div class="text-sm truncate">${String(g.courseTitle || '')}</div></td>
            <td class="px-4 py-3 text-center"><span class="text-sm font-semibold">${String(g.score ?? '')}</span></td>
            <td class="px-6 py-3 text-center"><div class="text-sm truncate">${String(g.teacherName || '')}</div></td>
            <td class="px-6 py-3 text-center text-sm whitespace-nowrap">${String(formatDate(g.gradedAt) || '')}</td>
            <td class="px-6 py-3 text-center">
              <span class="text-xs px-2 py-1 rounded-full border border-white/20">${g.isPublished ? String(t('teacher.studentDetail.table.published')) : String(t('teacher.studentDetail.table.unpublished'))}</span>
            </td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `
  return wrap
}

async function renderElementToCanvas(el: HTMLElement, opts?: { sanitizeThemeColors?: boolean }) {
  const sanitizeThemeColors = opts?.sanitizeThemeColors !== false
  return await html2canvas(el, {
    scale: 2,
    backgroundColor: '#ffffff',
    useCORS: true,
    logging: false,
    scrollX: 0,
    scrollY: 0,
    windowWidth: el.scrollWidth || el.clientWidth,
    windowHeight: el.scrollHeight || el.clientHeight,
    // 兼容：仅在“截取页面真实 DOM（带主题 CSS）”时才需要强行净化颜色函数
    onclone: sanitizeThemeColors
      ? (doc: Document) => {
          try {
            // 清理克隆 DOM 中可能包含 oklab/oklch/color-mix 的 inline style（html2canvas 不支持）
            try {
              const styled = doc.querySelectorAll('[style]')
              styled.forEach((el) => {
                const s = String((el as HTMLElement).getAttribute('style') || '')
                if (/oklab|oklch|color-mix/i.test(s)) {
                  ;(el as HTMLElement).removeAttribute('style')
                }
              })
            } catch {}

            // 覆盖主题色变量（项目里大量使用 oklch/oklab 定义变量，html2canvas 不支持）
            try {
              const root = doc.documentElement as any
              const applyVars = (el: any) => {
                if (!el || !el.style) return
                el.style.setProperty('color-scheme', 'light')
                el.style.setProperty('--color-base-100', '#ffffff')
                el.style.setProperty('--color-base-content', '#111827')
                el.style.setProperty('--color-primary', '#6366f1')
                el.style.setProperty('--color-primary-content', '#111827')
                el.style.setProperty('--color-secondary', '#a78bfa')
                el.style.setProperty('--color-secondary-content', '#111827')
                el.style.setProperty('--color-accent', '#22d3ee')
                el.style.setProperty('--color-accent-content', '#111827')
                el.style.setProperty('--color-neutral', '#e5e7eb')
                el.style.setProperty('--color-neutral-content', '#111827')
                el.style.setProperty('--color-info', '#0ea5e9')
                el.style.setProperty('--color-info-content', '#0b1220')
                el.style.setProperty('--color-success', '#10b981')
                el.style.setProperty('--color-success-content', '#052e2b')
                el.style.setProperty('--color-warning', '#f59e0b')
                el.style.setProperty('--color-warning-content', '#1f1605')
                el.style.setProperty('--color-error', '#ef4444')
                el.style.setProperty('--color-error-content', '#1f0b0b')
              }
              applyVars(root)
              applyVars(doc.body as any)
            } catch {}

            const style = doc.createElement('style')
            style.setAttribute('data-export-style', 'true')
            // 注意：这里不要再做“全黑白覆盖”，否则导出会变成黑白报告
            style.textContent = `
              :root, body { background: #ffffff !important; color: #111827 !important; }
              canvas { display: none !important; }
              * { -webkit-backdrop-filter: none !important; backdrop-filter: none !important; }
            `
            doc.head.appendChild(style)
          } catch {}
        }
      : undefined,
    // 避免把玻璃背景 canvas / 图表 canvas 捕获进来
    ignoreElements: (node: any) => {
      try {
        if (!node) return false
        const tag = String(node.tagName || '').toUpperCase()
        if (tag === 'CANVAS') return true
      } catch {}
      return false
    }
  } as any)
}

function addCanvasToPdfSliced(pdf: any, canvas: HTMLCanvasElement, opts: { margin: number }) {
  const margin = opts.margin
  const imgData = canvas.toDataURL('image/png')
  const pageWidth = pdf.internal.pageSize.getWidth()
  const pageHeight = pdf.internal.pageSize.getHeight()
  const innerW = pageWidth - margin * 2
  const innerH = pageHeight - margin * 2
  const imgH = (canvas.height * innerW) / canvas.width

  let heightLeft = imgH
  pdf.addImage(imgData, 'PNG', margin, margin, innerW, imgH)
  heightLeft -= innerH

  while (heightLeft > 0) {
    pdf.addPage()
    const position = margin - (imgH - heightLeft)
    pdf.addImage(imgData, 'PNG', margin, position, innerW, imgH)
    heightLeft -= innerH
  }
  const usedOnLastPage = Math.min(innerH, heightLeft + innerH)
  return { usedOnLastPage }
}

function escapeHtml(s: any) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderInsightHtml(ins: any) {
  if (!ins) return `<div class="muted">No insight.</div>`
  const parts: string[] = []
  const risk = Array.isArray(ins.riskAlerts) ? ins.riskAlerts : []
  const judge = Array.isArray(ins.stageJudgements) ? ins.stageJudgements : []
  const sugg = Array.isArray(ins.formativeSuggestions) ? ins.formativeSuggestions : []
  const act = Array.isArray(ins.actionRecommendations) ? ins.actionRecommendations : []
  const explain = ins.explainScore?.text ? String(ins.explainScore.text) : ''

  if (explain) {
    parts.push(`
      <h2>${escapeHtml(t('shared.behaviorInsight.sections.explain'))}</h2>
      <div class="card">${escapeHtml(explain)}</div>
    `)
  }
  if (judge.length) {
    parts.push(`<h2>${escapeHtml(t('shared.behaviorInsight.sections.judgement'))}</h2>`)
    parts.push(`<div class="grid">` + judge.map((it: any) => `
      <div class="card">
        <div class="row">
          <div class="strong">${escapeHtml(it.dimensionCode || '')}</div>
          <span class="badge"><span class="badge-text">${escapeHtml(it.level || '')}</span></span>
        </div>
        <div class="muted">${escapeHtml(it.rationale || '')}</div>
      </div>
    `).join('') + `</div>`)
  }
  if (risk.length) {
    parts.push(`<h2>${escapeHtml(t('shared.behaviorInsight.sections.riskAlerts'))}</h2>`)
    parts.push(risk.map((a: any) => `
      <div class="card">
        <div class="row">
          <div class="strong">${escapeHtml(a.title || '')}</div>
          <span class="badge"><span class="badge-text">${escapeHtml(a.severity || '')}</span></span>
        </div>
        <div class="muted">${escapeHtml(a.message || '')}</div>
      </div>
    `).join(''))
  }
  if (sugg.length) {
    parts.push(`<h2>${escapeHtml(t('shared.behaviorInsight.sections.suggestions'))}</h2>`)
    parts.push(sugg.map((s: any) => `
      <div class="card">
        <div class="strong">${escapeHtml(s.title || '')}</div>
        <div class="muted">${escapeHtml(s.description || '')}</div>
        ${(Array.isArray(s.nextActions) && s.nextActions.length) ? `<ul>${s.nextActions.map((x:any)=>`<li>${escapeHtml(x)}</li>`).join('')}</ul>` : ''}
      </div>
    `).join(''))
  }
  if (act.length) {
    parts.push(`<h2>${escapeHtml(t('shared.behaviorInsight.sections.actionRecommendations'))}</h2>`)
    parts.push(act.map((s: any) => `
      <div class="card">
        <div class="strong">${escapeHtml(s.title || '')}</div>
        <div class="muted">${escapeHtml(s.description || '')}</div>
        ${(Array.isArray(s.nextActions) && s.nextActions.length) ? `<ul>${s.nextActions.map((x:any)=>`<li>${escapeHtml(x)}</li>`).join('')}</ul>` : ''}
      </div>
    `).join(''))
  }
  return parts.join('\n')
}

function renderEvidenceHtml(sum: any) {
  if (!sum) return `<div class="muted">No behavior evidence.</div>`
  const aiQ = Number(sum?.activityStats?.ai?.questionCount ?? 0)
  const aiF = Number(sum?.activityStats?.ai?.followUpCount ?? 0)
  const ask = Number(sum?.activityStats?.community?.askCount ?? 0)
  const ans = Number(sum?.activityStats?.community?.answerCount ?? 0)
  const sub = Number(sum?.activityStats?.assignment?.submitCount ?? 0)
  const resub = Number(sum?.activityStats?.assignment?.resubmitCount ?? 0)
  const fb = Number(sum?.activityStats?.feedback?.viewCount ?? 0)
  const items = Array.isArray(sum.evidenceItems) ? sum.evidenceItems : []
  return `
    <h2>${escapeHtml(t('shared.behaviorEvidence.title'))}</h2>
    <div class="grid">
      <div class="card"><div class="strong">${escapeHtml(t('shared.behaviorEvidence.stats.ai'))}</div><div class="muted">${escapeHtml(t('shared.behaviorEvidence.stats.aiQuestions'))}: ${aiQ} · ${escapeHtml(t('shared.behaviorEvidence.stats.aiFollowUps'))}: ${aiF}</div></div>
      <div class="card"><div class="strong">${escapeHtml(t('shared.behaviorEvidence.stats.community'))}</div><div class="muted">${escapeHtml(t('shared.behaviorEvidence.stats.asks'))}: ${ask} · ${escapeHtml(t('shared.behaviorEvidence.stats.answers'))}: ${ans}</div></div>
      <div class="card"><div class="strong">${escapeHtml(t('shared.behaviorEvidence.stats.assignment'))}</div><div class="muted">${escapeHtml(t('shared.behaviorEvidence.stats.submits'))}: ${sub} · ${escapeHtml(t('shared.behaviorEvidence.stats.resubmits'))}: ${resub}</div></div>
      <div class="card"><div class="strong">${escapeHtml(t('shared.behaviorEvidence.stats.feedback'))}</div><div class="muted">${escapeHtml(t('shared.behaviorEvidence.stats.views'))}: ${fb}</div></div>
    </div>
    <h2>${escapeHtml(t('shared.behaviorEvidence.evidence.title'))}</h2>
    ${items.length ? items.map((it:any)=>`
      <div class="card">
        <div class="row">
          <div class="strong">${escapeHtml(it.title || it.evidenceType || '')}</div>
          <span class="badge"><span class="badge-text">${escapeHtml(it.evidenceId || '')}</span></span>
        </div>
        <div class="muted">${escapeHtml(it.description || '')}</div>
      </div>
    `).join('') : `<div class="muted">${escapeHtml(t('shared.behaviorEvidence.evidence.empty'))}</div>`}
  `
}

/**
 * 成绩表：为 PDF 导出生成纯 HTML（不依赖 Tailwind/主题变量，确保不触发 oklab/oklch）。
 */
function renderGradesTableHtml(grades: any[]) {
  const rows = (grades || []).map((g: any) => `
    <tr>
      <td><div class="cell-strong">${escapeHtml(g.assignmentTitle || '')}</div></td>
      <td>${escapeHtml(g.courseTitle || '')}</td>
      <td class="cell-center">${escapeHtml(g.score ?? '')}</td>
      <td>${escapeHtml(g.teacherName || '')}</td>
      <td class="cell-nowrap">${escapeHtml(formatDate(g.gradedAt) || '')}</td>
      <td class="cell-center">
        <span class="pill"><span class="badge-text">${escapeHtml(g.isPublished ? t('teacher.studentDetail.table.published') : t('teacher.studentDetail.table.unpublished'))}</span></span>
      </td>
    </tr>
  `).join('')
  return `
    <table class="table">
      <thead>
        <tr>
          <th style="width: 28%;">${escapeHtml(t('teacher.studentDetail.table.assignment'))}</th>
          <th style="width: 22%;">${escapeHtml(t('teacher.studentDetail.table.course'))}</th>
          <th style="width: 10%;" class="cell-center">${escapeHtml(t('teacher.studentDetail.table.score'))}</th>
          <th style="width: 16%;">${escapeHtml(t('teacher.studentDetail.table.teacher'))}</th>
          <th style="width: 14%;" class="cell-nowrap">${escapeHtml(t('teacher.studentDetail.table.date'))}</th>
          <th style="width: 10%;" class="cell-center">${escapeHtml(t('teacher.studentDetail.table.status'))}</th>
        </tr>
      </thead>
      <tbody>
        ${rows || `<tr><td colspan="6" class="muted">${escapeHtml(t('teacher.studentDetail.table.empty') || '-')}</td></tr>`}
      </tbody>
    </table>
  `
}

/**
 * 成绩分页：避免“一张长图切页”导致卡片/表格被截断。
 * 这里按行粗略分块，每块单独生成一个 section 并独立渲染进 PDF。
 */
function renderGradesSectionsHtml(allGrades: any[]) {
  const perPage = 18
  const total = Math.max(1, Math.ceil((allGrades?.length || 0) / perPage))
  const chunks: string[] = []
  for (let i = 0; i < total; i++) {
    const slice = (allGrades || []).slice(i * perPage, (i + 1) * perPage)
    chunks.push(`
      <section class="section export-section tint-emerald">
        <div class="section-head">
          <div class="section-title">${escapeHtml(t('teacher.studentDetail.table.title') as any || '成绩记录')}</div>
          <div class="section-meta">${escapeHtml(`${i + 1}/${total}`)}</div>
        </div>
        ${renderGradesTableHtml(slice)}
      </section>
    `)
  }
  return chunks.join('\n')
}

async function exportReportPdf() {
  if (exportingReport.value) return
  const sid = studentId.value || resolvedStudentId.value
  if (!sid) return

  exportingReport.value = true
  try {
    const courseId = selectedCourseId.value || undefined
    // 拉取全部成绩（不受分页影响）
    const allResp: any = await gradeApi.getGradesByStudent(String(sid), { page: 1, size: 10000, courseId })
    const allGrades: any[] = (allResp as any)?.items || (allResp as any)?.content || []

    // 用 iframe 构造“纯白、无主题CSS”的导出 DOM，彻底避开 oklab/oklch/color-mix
    const iframe = document.createElement('iframe')
    iframe.style.position = 'fixed'
    iframe.style.left = '-100000px'
    iframe.style.top = '0'
    // A4 近似宽度（便于获得稳定的分页/比例）
    iframe.style.width = '794px'
    iframe.style.height = '10px'
    iframe.style.border = '0'
    iframe.setAttribute('aria-hidden', 'true')
    document.body.appendChild(iframe)
    const doc = iframe.contentDocument!
    doc.open()
    doc.write(`
      <!doctype html>
      <html>
        <head>
          <meta charset="utf-8" />
          <style>
            :root { --ink:#0f172a; --muted:#64748b; --line:#e2e8f0; --paper:#ffffff; --soft:#f8fafc; }
            * { box-sizing: border-box; }
            body { margin:0; padding:28px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif; color:var(--ink); background:var(--paper); }
            h1 { font-size:18px; margin:0; letter-spacing: .2px; }
            .header { padding:16px 16px 14px; border:1px solid var(--line); border-radius:16px; background: linear-gradient(135deg, #f8fafc 0%, #ffffff 55%, #f1f5f9 100%); }
            .meta { margin-top:6px; font-size:12px; color:var(--muted); }
            .divider { height:14px; }

            .section { border:1px solid var(--line); border-radius:16px; padding:14px 14px 12px; background: #ffffff; }
            .section + .section { margin-top:14px; }
            .section-head { display:flex; align-items:center; justify-content:space-between; gap:10px; margin-bottom:10px; }
            .section-title { font-size:14px; font-weight:800; letter-spacing:.2px; display:flex; align-items:center; gap:8px; }
            .section-title:before { content:""; width:10px; height:10px; border-radius:999px; background: currentColor; opacity:.9; }
            .section-meta { font-size:12px; color:var(--muted); }
            /* 章节内小标题与正文间距（避免贴太紧） */
            .section h2 { font-size:13px; margin:14px 0 10px; line-height: 1.25; }

            /* 分区配色（纯 hex/rgb，避免 oklab/oklch） */
            .tint-indigo { color:#4f46e5; background: linear-gradient(180deg, rgba(79,70,229,.06), rgba(255,255,255,1)); }
            .tint-cyan { color:#0891b2; background: linear-gradient(180deg, rgba(8,145,178,.06), rgba(255,255,255,1)); }
            .tint-emerald { color:#059669; background: linear-gradient(180deg, rgba(5,150,105,.06), rgba(255,255,255,1)); }

            .card { border:1px solid var(--line); border-radius:14px; padding:12px; margin:10px 0; background: #ffffff; }
            .grid { display:grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap:10px; }
            .row { display:flex; align-items:center; justify-content:space-between; gap:10px; margin-bottom:8px; }
            .strong { font-weight:800; color:var(--ink); line-height: 1.25; }
            .muted { color:var(--muted); font-size:12px; white-space:pre-wrap; line-height: 1.55; }
            /* html2canvas 偶发把 badge 的文字绘制偏下：用 inner span 锁定字形位置 */
            .badge, .pill {
              display:inline-flex;
              align-items:center;
              justify-content:center;
              vertical-align:middle;
              box-sizing:border-box;
              height:20px; /* 给字体留余量，避免 html2canvas 偏下时被裁掉 */
              padding:0 9px;
              border-radius:999px;
              border:1px solid var(--line);
              background:#ffffff;
              color:#0f172a;
              /* 不裁剪：优先保证可读性 */
              overflow:visible;
              white-space:nowrap;
            }
            .badge-text {
              display:inline-block;
              font-size:11px;
              line-height:20px;
              transform: translateY(-1px); /* 比 top 更稳定 */
            }
            ul { margin:6px 0 0 18px; color: var(--muted); font-size: 12px; line-height: 1.55; }

            .table { width:100%; border-collapse:separate; border-spacing:0; overflow:hidden; border:1px solid var(--line); border-radius:14px; background:#fff; }
            .table thead th { background: var(--soft); color:#0f172a; font-weight:800; font-size:12px; text-align:left; padding:9px 10px; border-bottom:1px solid var(--line); }
            .table tbody td { font-size:12px; color:#0f172a; padding:9px 10px; border-bottom:1px solid var(--line); vertical-align:top; }
            .table tbody tr:last-child td { border-bottom:none; }
            .cell-nowrap { white-space:nowrap; }
            .cell-center { text-align:center; }
            .cell-strong { font-weight:700; }
          </style>
        </head>
        <body>
          <div class="header export-section" id="export-header">
            <h1>${escapeHtml(safeFilename(studentName.value || `Student ${sid}`))} · ${escapeHtml(t('teacher.studentDetail.actions.exportReport') as any || '导出报告')}</h1>
            <div class="meta">${escapeHtml(new Date().toLocaleString())}${courseId ? ` · courseId: ${escapeHtml(courseId)}` : ''}</div>
          </div>
          <div class="divider" style="height: 10px;"></div>

          <section class="section export-section tint-indigo" id="export-insight">
            <div class="section-head">
              <div class="section-title">${escapeHtml(t('shared.behaviorInsight.title'))}</div>
              <div class="section-meta">${escapeHtml(t('shared.common.tip') as any || '')}</div>
            </div>
            ${renderInsightHtml(behaviorInsight)}
          </section>

          <section class="section export-section tint-cyan" id="export-evidence">
            <div class="section-head">
              <div class="section-title">${escapeHtml(t('shared.behaviorEvidence.title'))}</div>
              <div class="section-meta">${escapeHtml('')}</div>
            </div>
            ${renderEvidenceHtml(behaviorEvidenceSummary)}
          </section>

          ${renderGradesSectionsHtml(allGrades)}
        </body>
      </html>
    `)
    doc.close()

    const pdf = new jsPDF({ unit: 'pt', format: 'a4' })
    // 自动“排版到同页”：尽量把多个 section 放到一页里，减少大片空白；
    // 如果某个 section 太高，再单独切页渲染（仍保持不截断）。
    const margin = 18
    const gap = 10
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const innerW = pageWidth - margin * 2
    const innerH = pageHeight - margin * 2

    let cursorY = margin
    const sections = Array.from(doc.querySelectorAll('.export-section')) as HTMLElement[]
    for (let i = 0; i < sections.length; i++) {
      const el = sections[i]
      const canvas = await renderElementToCanvas(el, { sanitizeThemeColors: false })
      const imgH = (canvas.height * innerW) / canvas.width

      // 超过一页：单独切片
      if (imgH > innerH) {
        if (cursorY !== margin) {
          pdf.addPage()
          cursorY = margin
        }
        const { usedOnLastPage } = addCanvasToPdfSliced(pdf, canvas, { margin })
        cursorY = margin + usedOnLastPage + gap
        continue
      }

      // 放不下就换页
      if (cursorY + imgH > margin + innerH) {
        pdf.addPage()
        cursorY = margin
      }

      const imgData = canvas.toDataURL('image/png')
      pdf.addImage(imgData, 'PNG', margin, cursorY, innerW, imgH)
      cursorY += imgH + gap
    }

    const name = safeFilename(studentName.value || `student_${sid}`)
    pdf.save(`${name}_report.pdf`)
    try { document.body.removeChild(iframe) } catch {}
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: (t('shared.common.tip') as any) || '提示', message: e?.message || '导出失败' })
  } finally {
    exportingReport.value = false
  }
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
    // 与“数据分析”页保持一致：不强行限定日期窗口，避免把班级/学生四维数据过滤掉
    const resp = await teacherApi.getAbilityRadar({ courseId, studentId: String(studentId.value) })
    const data: any = (resp as any)?.data?.data ?? (resp as any)?.data ?? resp
    const dims: string[] = data.dimensions || []
    const nextIndicators = dims.map((name:string) => ({ name, max: 100 }))
    const padScores = (arr: any, len: number): number[] => {
      const a = Array.isArray(arr) ? arr.map((v:any)=>Number(v ?? 0)) : []
      const out: number[] = []
      for (let i = 0; i < len; i++) out.push(Number.isFinite(a[i]) ? a[i] : 0)
      return out
    }
    const studentVals = padScores(data.studentScores, dims.length)
    const classVals = padScores(data.classAvgScores, dims.length)
    const s = { name: t('teacher.studentDetail.radar.student') || '学生', values: studentVals }
    const c = { name: t('teacher.studentDetail.radar.classAvg') || '班级', values: classVals, color: '#10b981' }

    // 班级数据应与数据分析页一致：始终展示“学生 vs 班级”
    showClassAvgNoDataHint.value = false
    const nextSeries = [s, c]
    // 避免重复设置引发双次渲染
    const sameDims = JSON.stringify(radarIndicators.value) === JSON.stringify(nextIndicators)
    const sameSeries = JSON.stringify(radarSeries.value) === JSON.stringify(nextSeries)
    if (!sameDims) radarIndicators.value = nextIndicators
    if (!sameSeries) radarSeries.value = nextSeries
  } catch {}
}

function fallbackEventTitle(eventType: string): string {
  const key = (eventType || '').toLowerCase()
  const map: any = {
    lesson: t('teacher.studentDetail.recentEventTypes.lesson'),
    submission: t('teacher.studentDetail.recentEventTypes.submission'),
    quiz: t('teacher.studentDetail.recentEventTypes.quiz'),
    discussion: t('teacher.studentDetail.recentEventTypes.discussion'),
    visit: t('teacher.studentDetail.recentEventTypes.visit'),
    ai: t('teacher.studentDetail.recentEventTypes.ai'),
    community_ask: t('teacher.studentDetail.recentEventTypes.communityAsk'),
    community_answer: t('teacher.studentDetail.recentEventTypes.communityAnswer'),
  }
  const v = map[key]
  return (v && v !== `teacher.studentDetail.recentEventTypes.${key}`) ? String(v) : (t('teacher.studentDetail.recentEventTypes.default') as any) || '学习活动'
}

function formatRecentEventTitle(ev: any): string {
  const label = fallbackEventTitle(ev?.eventType)
  const title = String(ev?.title || '').trim()
  // 社区/AI：优先展示“动作标签”，内容作为补充，不再只显示内容文本
  if (ev?.eventType === 'ai' || ev?.eventType === 'community_ask' || ev?.eventType === 'community_answer') {
    return title ? `${label}：${title}` : label
  }
  return title || label
}

function formatDuration(seconds: any): string {
  const s = Number(seconds)
  if (!Number.isFinite(s) || s <= 0) return ''
  const mins = Math.max(1, Math.round(s / 60))
  const isEn = String(locale?.value || 'zh-CN').toLowerCase().startsWith('en')
  return isEn ? `${mins} min` : `${mins} 分钟`
}

function openRecentEventLink(link: any) {
  const l = String(link || '').trim()
  if (!l) return
  if (/^https?:\/\//i.test(l)) {
    window.open(l, '_blank')
    return
  }
  if (l.startsWith('/')) {
    router.push(l)
    return
  }
  // 兜底：当作相对链接处理
  router.push('/' + l)
}

onMounted(async () => {
    const sid = studentId.value || resolvedStudentId.value
    if (sid) {
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
          (async () => { try { await fetchPage(1) } catch {} })()
        ]
        await Promise.allSettled(tasks)
    }
});
</script>
