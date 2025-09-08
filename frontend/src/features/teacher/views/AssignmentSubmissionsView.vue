<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <div class="mb-6">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <!-- 新增: 课程管理 -->
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/courses')">{{ t('teacher.courses.breadcrumb') }}</span>
          <ChevronRightIcon class="w-4 h-4" />
          <!-- 新增: 对应课程ID，可点击返回课程页 -->
          <span v-if="courseId" class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="goCourse()">{{ courseTitle || `#${courseId}` }}</span>
          <ChevronRightIcon v-if="courseId" class="w-4 h-4" />
          <!-- 原有: 作业管理 -> 提交列表 -->
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/assignments')">{{ t('teacher.submissions.breadcrumb.assignments') }}</span>
          <ChevronRightIcon class="w-4 h-4" />
          <span>{{ t('teacher.submissions.breadcrumb.self') }}</span>
        </nav>
        <PageHeader :title="t('teacher.submissions.title')" :subtitle="t('teacher.submissions.subtitle')">
          <template #actions>
            <div class="flex items-center gap-4">
              <!-- 统计摘要 -->
              <div class="hidden md:flex items-center gap-4 text-sm text-gray-600 dark:text-gray-300">
                <div>{{ t('teacher.submissions.stats.totalEnrolled') }}: <span class="font-semibold">{{ stats.totalEnrolled }}</span></div>
                <div>{{ t('teacher.submissions.stats.submitted') }}: <span class="font-semibold">{{ stats.submittedCount }}</span></div>
                <div>{{ t('teacher.submissions.stats.unsubmitted') }}: <span class="font-semibold">{{ stats.unsubmittedCount }}</span></div>
              </div>
              <Button variant="primary" @click="remindUnsubmitted" :loading="reminding" :disabled="reminding || stats.unsubmittedCount === 0">
                {{ t('teacher.submissions.actions.remindUnsubmitted') }}
              </Button>
            </div>
          </template>
        </PageHeader>
      </div>

    <div v-if="loading" class="text-center py-12">{{ t('teacher.submissions.loading') }}</div>
    <div v-else>
      <div v-if="errorMessage" class="card p-6 text-center text-red-600">
        <p class="mb-3">{{ errorMessage }}</p>
         <Button variant="info" @click="fetch()">
           <arrow-path-icon class="w-4 h-4 mr-2" />
           {{ t('teacher.submissions.retry') }}
         </Button>
      </div>
      <div v-else-if="submissions.length === 0" class="card p-6 text-center text-gray-500">{{ t('teacher.submissions.empty') }}</div>
      <div v-else class="space-y-3">
        <div v-for="s in submissions" :key="s.id" class="glass-regular glass-interactive border border-gray-200/40 dark:border-gray-700/40 rounded-xl p-4 flex items-center justify-between" v-glass="{ strength: 'regular', interactive: true }">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9">
              <user-avatar :avatar="s.avatar" :size="36">
                <div class="w-9 h-9 rounded-full bg-gray-200 dark:bg-gray-700 flex items-center justify-center">
                  <user-icon class="w-4 h-4 text-gray-500" />
                </div>
              </user-avatar>
            </div>
            <div>
              <div class="font-medium">{{ s.studentName || s.studentId }}</div>
              <div class="text-sm text-gray-500">{{ t('teacher.submissions.submittedAt', { time: formatDate(s.submittedAt) }) }}<span v-if="s.isLate" class="ml-2 text-red-600">({{ t('teacher.submissions.late') }})</span></div>
              <div class="text-xs mt-1">
                <span :class="badgeClass(s.status)">{{ statusText(s.status) }}</span>
              </div>
            </div>
          </div>
          <div>
            <Button size="sm" variant="purple" :disabled="loading" @click="goGrade(s)">
              <check-badge-icon class="w-4 h-4 mr-1" />
              {{ t('teacher.submissions.actions.grade') }}
            </Button>
          </div>
        </div>
      </div>

      <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-700">{{ t('teacher.assignments.pagination.perPagePrefix') }}</span>
          <div class="w-24">
            <GlassPopoverSelect
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
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { submissionApi } from '@/api/submission.api';
import { assignmentApi } from '@/api/assignment.api';
import { useUIStore } from '@/stores/ui';
import Button from '@/components/ui/Button.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { ArrowPathIcon, CheckBadgeIcon, UserIcon } from '@heroicons/vue/24/outline'
import { ChevronRightIcon } from '@heroicons/vue/24/outline';
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

const route = useRoute();
const router = useRouter();
const ui = useUIStore();
const { t } = useI18n()

const assignmentId = route.params.assignmentId as string;
const submissions = ref<any[]>([]);
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
  } catch (e: any) {
    errorMessage.value = e?.message || (t('teacher.submissions.errors.fetch') as string);
  } finally {
    loading.value = false;
  }
}

function goGrade(s: any) {
  router.push(`/teacher/assignments/${assignmentId}/submissions/${s.id}/grade`);
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

