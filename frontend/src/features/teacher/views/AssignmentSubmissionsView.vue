<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <div class="mb-6">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <!-- 新增: 课程管理 -->
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/courses')">{{ t('teacher.courses.breadcrumb') }}</span>
          <chevron-right-icon class="w-4 h-4" />
          <!-- 新增: 对应课程ID，可点击返回课程页 -->
          <span v-if="courseId" class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="goCourse()">{{ courseTitle || `#${courseId}` }}</span>
          <chevron-right-icon v-if="courseId" class="w-4 h-4" />
          <!-- 原有: 作业管理 -> 提交列表 -->
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/assignments')">{{ t('teacher.submissions.breadcrumb.assignments') }}</span>
          <chevron-right-icon class="w-4 h-4" />
          <span>{{ t('teacher.submissions.breadcrumb.self') }}</span>
        </nav>
        <page-header :title="t('teacher.submissions.title')" :subtitle="t('teacher.submissions.subtitle')">
          <template #actions>
            <div class="flex items-center gap-4">
              <!-- 统计摘要 -->
              <div class="hidden md:flex items-center gap-4 text-sm text-gray-600 dark:text-gray-300">
                <div>{{ t('teacher.submissions.stats.totalEnrolled') }}: <span class="font-semibold">{{ stats.totalEnrolled }}</span></div>
                <div>{{ t('teacher.submissions.stats.submitted') }}: <span class="font-semibold">{{ stats.submittedCount }}</span></div>
                <div>{{ t('teacher.submissions.stats.unsubmitted') }}: <span class="font-semibold">{{ stats.unsubmittedCount }}</span></div>
              </div>
              <Button variant="primary" @click="remindUnsubmitted" :loading="reminding" :disabled="reminding || stats.unsubmittedCount === 0 || isPastDue">
                {{ t('teacher.submissions.actions.remindUnsubmitted') }}
              </Button>
            </div>
          </template>
        </page-header>
      </div>

    <div v-if="loading" class="text-center py-12">{{ t('teacher.submissions.loading') }}</div>
    <div v-else>
      <Card v-if="errorMessage" padding="md" tint="danger" class="text-center">
        <p class="mb-3">{{ errorMessage }}</p>
         <Button variant="info" @click="fetch()">
           <arrow-path-icon class="w-4 h-4 mr-2" />
           {{ t('teacher.submissions.retry') }}
         </Button>
      </Card>
      <Card v-else-if="displayRows.length === 0" padding="md" tint="info" class="text-center text-gray-500">{{ t('teacher.submissions.empty') }}</Card>
      <div v-else class="space-y-3">
        <Card v-for="row in displayRows" :key="row.key" padding="md" tint="secondary" class="relative">
          <div class="flex items-center gap-3 pr-40 min-w-0">
            <div class="w-9 h-9">
              <user-avatar :avatar="row.avatar" :size="36">
                <div class="w-9 h-9 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
                  <user-icon class="w-4 h-4 text-gray-500" />
                </div>
              </user-avatar>
            </div>
            <div class="min-w-0">
              <div class="font-medium truncate max-w-[60vw]">{{ row.displayName || row.studentName || row.studentId }}</div>
              <div class="text-sm text-gray-500 mt-0.5">
                <template v-if="row.status && row.status !== 'unsubmitted'">
                  {{ t('teacher.submissions.submittedAt', { time: formatDate(row.submittedAt) }) }}
                  <span v-if="row.isLate" class="ml-2 text-red-600">({{ t('teacher.submissions.late') }})</span>
                </template>
                <template v-else>
                  {{ t('teacher.submissions.status.unsubmitted') }}
                </template>
              </div>
              <div class="text-xs mt-1">
                <span :class="badgeClass(row.status)">{{ statusText(row.status) }}</span>
              </div>
            </div>
          </div>
          <div class="absolute right-4 top-1/2 -translate-y-1/2">
            <Button size="sm" variant="purple" :disabled="loading" @click="goGrade(row)">
              <check-badge-icon class="w-4 h-4 mr-1" />
              {{ t('teacher.submissions.actions.grade') }}
            </Button>
          </div>
        </Card>
      </div>

      <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-700">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
          <div class="w-24">
            <glass-popover-select
              :options="[{label:'10', value:10},{label:'20', value:20},{label:'50', value:50}]"
              :model-value="pageSize"
              @update:modelValue="(v:any)=>{ pageSize = Number(v||10); changePageSize() }"
              size="sm"
            />
          </div>
          <span class="text-sm text-gray-700">{{ t('teacher.assignments.pagination.perPageSuffix') }}</span>
        </div>
        <div class="flex items-center space-x-2">
          <Button variant="outline" size="sm" :disabled="loading || currentPage===1" @click="prevPage">{{ t('teacher.assignments.pagination.prev') }}</Button>
          <span class="text-sm">{{ t('teacher.assignments.pagination.page', { page: currentPage }) }}</span>
          <Button variant="outline" size="sm" :disabled="loading || currentPage>=totalPages" @click="nextPage">{{ t('teacher.assignments.pagination.next') }}</Button>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { submissionApi } from '@/api/submission.api';
import { gradeApi } from '@/api/grade.api';
import { assignmentApi } from '@/api/assignment.api';
import { useUIStore } from '@/stores/ui';
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { ArrowPathIcon, CheckBadgeIcon, UserIcon } from '@heroicons/vue/24/outline'
import { ChevronRightIcon } from '@heroicons/vue/24/outline';
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { resolveUserDisplayName } from '@/shared/utils/user'

const route = useRoute();
const router = useRouter();
const ui = useUIStore();
const { t } = useI18n()

const assignmentId = route.params.assignmentId as string;
const submissions = ref<any[]>([]);
const students = ref<any[]>([]);
const gradesByStudent = ref<Record<string, any>>({});
const loading = ref(false);
const errorMessage = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const totalPages = ref(1);
const courseId = ref<string | null>(null)
const courseTitle = ref<string | null>(null)
const stats = ref<{ totalEnrolled: number; submittedCount: number; unsubmittedCount: number }>({ totalEnrolled: 0, submittedCount: 0, unsubmittedCount: 0 })
const reminding = ref(false)
const isPastDue = ref(false)

function parseDateLoose(v: any): number {
  if (!v) return NaN
  if (typeof v === 'number') return v
  const s = String(v)
  // support 'yyyy-MM-dd HH:mm:ss' or ISO
  const d = new Date(s.includes(' ') && !s.includes('T') ? s.replace(' ', 'T') : s)
  return d.getTime()
}

function formatDate(d?: string) {
  return d ? new Date(d).toLocaleString('zh-CN') : '-';
}
// removed duplicate locale hook to avoid redeclare of t
function statusText(s?: string) {
  const map: Record<string, string> = {
    submitted: t('teacher.submissions.status.submitted') as string,
    graded: t('teacher.submissions.status.graded') as string,
    returned: t('teacher.submissions.status.returned') as string,
    draft: t('teacher.submissions.status.draft') as string,
    late: t('teacher.submissions.status.late') as string,
    unsubmitted: t('teacher.submissions.status.unsubmitted') as string,
  };
  return map[(s||'').toLowerCase()] || s || '-';
}
function badgeClass(s?: string) {
  const t = (s||'').toLowerCase();
  const base = 'inline-flex items-center text-xs px-2 py-0.5 rounded-full glass-ultraThin'
  if (t==='submitted') return `${base} bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300`;
  if (t==='graded') return `${base} bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300`;
  if (t==='returned') return `${base} bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300`;
  if (t==='late') return `${base} bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300`;
  if (t==='unsubmitted') return `${base} bg-gray-100 text-gray-800 dark:bg-gray-800/60 dark:text-gray-200`;
  return `${base} bg-gray-100 text-gray-800 dark:bg-gray-800/60 dark:text-gray-200`;
}

async function fetch() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const data: any = await submissionApi.getSubmissionsByAssignment(assignmentId, { page: currentPage.value, size: pageSize.value });
    submissions.value = data?.items || [];
    total.value = data?.total ?? 0;
    totalPages.value = data?.totalPages ?? 1;
    // 刷新统计
    try {
      const st = await assignmentApi.getAssignmentSubmissionStats(assignmentId)
      const d: any = (st as any)?.data || st
      stats.value.totalEnrolled = Number(d?.totalEnrolled || 0)
      stats.value.submittedCount = Number(d?.submittedCount || 0)
      stats.value.unsubmittedCount = Number(d?.unsubmittedCount || Math.max(0, stats.value.totalEnrolled - stats.value.submittedCount))
    } catch (e) {
      ui.showNotification({ type: 'error', title: t('teacher.submissions.notify.statsLoadFailed'), message: t('teacher.submissions.notify.statsLoadFailedMsg') })
    }
    // 拉取课程学生（用于展示未提交者）
    try {
      if (courseId.value) {
        const res: any = await import('@/api/course.api').then(m => m.courseApi.getCourseStudents(String(courseId.value), { page: 1, size: 1000 }))
        const list: any[] = (res?.data?.items || res?.items || res || []) as any[]
        students.value = list.map((u: any) => ({ id: String(u?.id || ''), name: resolveUserDisplayName(u) || u?.nickname || u?.username || String(u?.id || ''), avatar: u?.avatar || '' }))
      }
    } catch {}
    // 拉取该作业的成绩（用于无提交但已评分的场景）
    try {
      const gres: any = await gradeApi.getGradesByAssignment(String(assignmentId), { page: 1, size: 1000 })
      const items: any[] = Array.isArray(gres?.data) ? gres.data : (Array.isArray(gres) ? gres : (Array.isArray(gres?.items) ? gres.items : (Array.isArray(gres?.data?.items) ? gres.data.items : [])))
      const map: Record<string, any> = {}
      for (const g of (items || [])) {
        const sid = String((g as any)?.studentId || (g as any)?.student_id || '')
        if (sid) map[sid] = g
      }
      gradesByStudent.value = map
    } catch {}
  } catch (e: any) {
    errorMessage.value = e?.message || (t('teacher.submissions.errors.fetch') as string);
  } finally {
    loading.value = false;
  }
}

// 合并“所有学生”与“已提交”生成显示行
const displayRows = computed(() => {
  const submissionByStudent: Record<string, any> = {}
  for (const s of (submissions.value || [])) {
    const sid = String(s.studentId || s.student_id || '')
    if (!sid) continue
    submissionByStudent[sid] = {
      key: `sub_${s.id}`,
      submissionId: s.id,
      studentId: sid,
      studentName: s.studentName || s.student_name || sid,
      avatar: s.avatar,
      submittedAt: s.submittedAt,
      isLate: !!s.isLate,
      status: String(s.status || 'submitted').toLowerCase()
    }
  }
  const rows: any[] = []
  for (const u of (students.value || [])) {
    const sid = String(u.id || '')
    const existed = submissionByStudent[sid]
    if (existed) {
      // 若有成绩态覆盖（returned/graded 优先于提交态）
      const g = gradesByStudent.value[sid]
      const gStatusRaw = String((g as any)?.status || (g as any)?.gradeStatus || '').toLowerCase()
      if (gStatusRaw === 'returned') {
        // 若重交截止已过，且未在打回之后重新提交，则视为未提交；
        // 若已在打回之后重新提交，则显示为已提交；否则显示已退回
        const untilMs = parseDateLoose((g as any)?.resubmitUntil || (g as any)?.resubmit_until)
        const nowMs = Date.now()
        const updatedMs = parseDateLoose((g as any)?.updatedAt || (g as any)?.updated_at)
        const submittedMs = parseDateLoose(existed.submittedAt)
        const resubmittedAfterReturn = Number.isFinite(submittedMs) && Number.isFinite(updatedMs) && submittedMs > updatedMs
        if (resubmittedAfterReturn) {
          existed.status = 'submitted'
        } else if (Number.isFinite(untilMs) && nowMs > untilMs) {
          existed.status = 'unsubmitted'
        } else {
          existed.status = 'returned'
        }
      } else if (gStatusRaw === 'published') {
        existed.status = 'graded'
      }
      rows.push({ ...existed, displayName: u.name })
    } else {
      const g = gradesByStudent.value[sid]
      const gStatusRaw = String((g as any)?.status || (g as any)?.gradeStatus || '').toLowerCase()
      let derivedStatus = 'unsubmitted'
      if (gStatusRaw === 'returned') {
        const untilMs = parseDateLoose((g as any)?.resubmitUntil || (g as any)?.resubmit_until)
        const nowMs = Date.now()
        derivedStatus = (Number.isFinite(untilMs) && nowMs > untilMs) ? 'unsubmitted' : 'returned'
      } else if (gStatusRaw) {
        derivedStatus = 'graded'
      }
      rows.push({
        key: `stu_${sid}`,
        submissionId: null,
        studentId: sid,
        studentName: u.name || sid,
        displayName: u.name || sid,
        avatar: u.avatar || '',
        submittedAt: null,
        isLate: false,
        status: derivedStatus
      })
    }
  }
  return rows
})

function goGrade(row: any) {
  if (row.submissionId) {
    router.push(`/teacher/assignments/${assignmentId}/submissions/${row.submissionId}/grade`)
  } else if (row.studentId) {
    router.push(`/teacher/assignments/${assignmentId}/students/${row.studentId}/grade`)
  }
}

function goCourse() {
  if (!courseId.value) return
  router.push(`/teacher/courses/${courseId.value}`)
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value -= 1;
    fetch();
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value += 1;
    fetch();
  }
}

function changePageSize() {
  currentPage.value = 1;
  fetch();
}

onMounted(async () => {
  // 先获取作业信息以解析 courseId 用于面包屑
  try {
    const a: any = await assignmentApi.getAssignmentById(assignmentId)
    courseId.value = a?.courseId ? String(a.courseId) : null
    courseTitle.value = a?.courseName || null
    // 计算是否已过截止
    try {
      const dv = a?.dueDate || a?.dueAt
      if (dv) {
        const d = new Date(dv)
        isPastDue.value = !isNaN(d.getTime()) && Date.now() > d.getTime()
      } else {
        isPastDue.value = false
      }
    } catch { isPastDue.value = false }
  } catch {}
  await fetch()
});

async function remindUnsubmitted() {
  if (reminding.value) return
  reminding.value = true
  try {
    const res = await assignmentApi.remindUnsubmitted(assignmentId)
    const d: any = (res as any)?.data || res
    const sent = Number(
      d?.sent ?? d?.successCount ?? d?.success ?? d?.count ?? 0
    )
    ui.showNotification({ type: 'success', title: t('teacher.submissions.notify.remindSuccess'), message: t('teacher.submissions.notify.remindSuccessMsg', { count: sent }) as string })
    // 发送后可选择刷新统计
    await fetch()
  } catch (e) {
    ui.showNotification({ type: 'error', title: t('teacher.submissions.notify.remindFailed'), message: t('teacher.submissions.notify.remindFailedMsg') })
  } finally {
    reminding.value = false
  }
}
</script>

<style scoped>
.badge { @apply inline-block px-2 py-0.5 rounded text-xs; }
.card { @apply bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded; }
.btn { @apply inline-flex items-center justify-center px-3 py-1.5 rounded border; }
.btn-outline { @apply border-gray-300 text-gray-700 hover:bg-gray-50; }
.btn-primary { @apply bg-blue-600 text-white border-blue-600 hover:opacity-90; }
.btn-sm { @apply text-sm; }
</style>

