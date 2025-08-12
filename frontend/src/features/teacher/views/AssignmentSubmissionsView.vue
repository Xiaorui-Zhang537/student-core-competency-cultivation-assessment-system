<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
            <span>作业管理</span>
            <chevron-right-icon class="w-4 h-4" />
            <span>提交列表</span>
          </nav>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-white">提交列表</h1>
          <p class="text-gray-600 dark:text-gray-400">查看并前往评分</p>
        </div>
        <Button variant="outline" @click="$router.push('/teacher/assignments')">
          <ArrowUturnLeftIcon class="w-4 h-4 mr-2" />
          返回作业
        </Button>
      </div>

    <div v-if="loading" class="text-center py-12">正在加载...</div>
    <div v-else>
      <div v-if="errorMessage" class="card p-6 text-center text-red-600">
        <p class="mb-3">{{ errorMessage }}</p>
         <Button variant="indigo" @click="fetch()">
           <ArrowPathIcon class="w-4 h-4 mr-2" />
           重试
         </Button>
      </div>
      <div v-else-if="submissions.length === 0" class="card p-6 text-center text-gray-500">暂无提交</div>
      <div v-else class="space-y-3">
        <div v-for="s in submissions" :key="s.id" class="card p-4 flex items-center justify-between">
          <div>
            <div class="font-medium">{{ s.studentName || s.studentId }}</div>
            <div class="text-sm text-gray-500">提交时间：{{ formatDate(s.submittedAt) }}<span v-if="s.isLate" class="ml-2 text-red-600">(迟交)</span></div>
            <div class="text-xs mt-1">
              <span :class="badgeClass(s.status)">{{ statusText(s.status) }}</span>
            </div>
          </div>
          <div>
            <Button size="sm" variant="purple" :disabled="loading" @click="goGrade(s)">
              <CheckBadgeIcon class="w-4 h-4 mr-1" />
              评分
            </Button>
          </div>
        </div>
      </div>

      <div class="mt-6 flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-700">每页</span>
          <select class="input input-sm w-20" v-model.number="pageSize" :disabled="loading" @change="changePageSize">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="text-sm text-gray-700">条</span>
        </div>
        <div class="flex items-center space-x-2">
          <button class="btn btn-sm btn-outline" :disabled="loading || currentPage===1" @click="prevPage">上一页</button>
          <span class="text-sm">第 {{ currentPage }} 页</span>
          <button class="btn btn-sm btn-outline" :disabled="loading || currentPage>=totalPages" @click="nextPage">下一页</button>
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
import { useUIStore } from '@/stores/ui';
import Button from '@/components/ui/Button.vue'
import { ArrowUturnLeftIcon, ArrowPathIcon, CheckBadgeIcon } from '@heroicons/vue/24/outline'
import { ChevronRightIcon } from '@heroicons/vue/24/outline';

const route = useRoute();
const router = useRouter();
const ui = useUIStore();

const assignmentId = route.params.assignmentId as string;
const submissions = ref<any[]>([]);
const loading = ref(false);
const errorMessage = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const totalPages = ref(1);

function formatDate(d?: string) {
  return d ? new Date(d).toLocaleString('zh-CN') : '-';
}
function statusText(s?: string) {
  const map: Record<string, string> = { submitted: '已提交', graded: '已评分', returned: '已退回', draft: '草稿', late: '迟交' };
  return map[(s||'').toLowerCase()] || s || '-';
}
function badgeClass(s?: string) {
  const t = (s||'').toLowerCase();
  if (t==='submitted') return 'badge bg-blue-100 text-blue-800';
  if (t==='graded') return 'badge bg-green-100 text-green-800';
  if (t==='returned') return 'badge bg-yellow-100 text-yellow-800';
  return 'badge bg-gray-100 text-gray-800';
}

async function fetch() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const res = await submissionApi.getSubmissionsByAssignment(assignmentId, { page: currentPage.value, size: pageSize.value });
    const data = res.data;
    submissions.value = data.items || [];
    total.value = data.total || 0;
    totalPages.value = data.pages || 1;
  } catch (e: any) {
    errorMessage.value = e?.message || '获取提交列表失败';
  } finally {
    loading.value = false;
  }
}

function goGrade(s: any) {
  router.push(`/teacher/assignments/${assignmentId}/submissions/${s.id}/grade`);
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

onMounted(fetch);
</script>

<style scoped>
.badge { @apply inline-block px-2 py-0.5 rounded text-xs; }
.card { @apply bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded; }
.btn { @apply inline-flex items-center justify-center px-3 py-1.5 rounded border; }
.btn-outline { @apply border-gray-300 text-gray-700 hover:bg-gray-50; }
.btn-primary { @apply bg-blue-600 text-white border-blue-600 hover:opacity-90; }
.btn-sm { @apply text-sm; }
</style>

