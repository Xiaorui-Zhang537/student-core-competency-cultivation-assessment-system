<template>
  <card padding="md" tint="secondary">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="text-sm font-medium">{{ t('admin.courses.lessonNotes.title') || '课堂笔记' }}</div>
      <div class="flex items-center gap-2">
        <Button size="sm" variant="info" :disabled="loading" @click="reload">
          <template #icon>
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992V4.356m-1.333 1.332A9 9 0 1 0 21 12h-2.25" />
            </svg>
          </template>
          {{ t('common.refresh') || '刷新' }}
        </Button>
        <Button size="sm" variant="success" :disabled="loading" @click="onExportCsv">
          <template #icon>
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v12m0 0 4-4m-4 4-4-4M4 17.5v1A2.5 2.5 0 0 0 6.5 21h11a2.5 2.5 0 0 0 2.5-2.5v-1" />
            </svg>
          </template>
          {{ t('admin.tools.exportCsv') || '导出CSV' }}
        </Button>
        <Button size="sm" variant="warning" :disabled="loading" @click="onExportZip">
          <template #icon>
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 8.5V18a2 2 0 0 1-2 2H5.75a2 2 0 0 1-2-2V8.5m17-3.5H3.25m9.5 0v4m-3-2h6" />
            </svg>
          </template>
          {{ t('admin.tools.exportZip') || '导出ZIP' }}
        </Button>
      </div>
    </div>

    <filter-bar tint="secondary" align="center" :dense="false" class="mb-3 rounded-full h-19">
      <template #left>
        <div class="flex items-center gap-3 flex-wrap">
          <div class="w-auto flex items-center gap-2">
            <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.courses.lessonNotes.lesson') || '节次' }}</span>
            <div class="w-64">
              <glass-popover-select v-model="lessonId" :options="lessonOptions" size="sm" tint="primary" />
            </div>
          </div>
        </div>
      </template>
      <template #right>
        <div class="w-80">
          <glass-search-input v-model="q" :placeholder="String(t('admin.courses.lessonNotes.searchPh') || '搜索笔记/姓名/学号')" size="sm" tint="info" />
        </div>
      </template>
    </filter-bar>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-else>
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('admin.courses.lessonNotes.columns.student') || '学生' }}
            </th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('admin.courses.lessonNotes.columns.lesson') || '节次' }}
            </th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">
              {{ t('admin.courses.lessonNotes.columns.notes') || '笔记' }}
            </th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('admin.courses.lessonNotes.columns.updatedAt') || '更新时间' }}
            </th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">
              {{ t('admin.courses.lessonNotes.columns.actions') || '操作' }}
            </th>
          </tr>
        </template>

        <template #body>
          <tr v-for="it in items" :key="`${it.studentId}-${it.lessonId}-${it.updatedAt || ''}`" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center">
              <div class="min-w-0">
                <div class="font-medium truncate">{{ it.studentName || `#${it.studentId}` }}</div>
                <div class="text-xs text-subtle truncate">{{ it.studentNo || '-' }} · #{{ it.studentId }}</div>
              </div>
            </td>
            <td class="px-6 py-3 text-center">
              <div class="min-w-0">
                <div class="font-medium truncate">{{ it.lessonTitle || `#${it.lessonId}` }}</div>
                <div class="text-xs text-subtle truncate">{{ it.chapterTitle || (t('student.courses.detail.noChapter') || '未分组') }}</div>
              </div>
            </td>
            <td class="px-6 py-3 text-center">
              <div class="text-sm text-subtle line-clamp-1 break-all">{{ it.notes || '-' }}</div>
            </td>
            <td class="px-6 py-3 text-center text-xs text-subtle whitespace-nowrap">{{ formatDateTime(it.updatedAt) }}</td>
            <td class="px-6 py-3 text-right">
              <Button size="sm" variant="info" class="whitespace-nowrap" @click="openModal(it)">
                <template #icon>
                  <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12s3.75-6.75 9.75-6.75S21.75 12 21.75 12 18 18.75 12 18.75 2.25 12 2.25 12Z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </template>
                {{ t('admin.courses.lessonNotes.actions.view') || '查看' }}
              </Button>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="5" class="px-6 py-6">
              <empty-state :title="String(t('common.empty') || '暂无数据')" />
            </td>
          </tr>
        </template>
      </glass-table>

      <pagination-bar
        class="mt-2"
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reload() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
      />
    </div>

    <Teleport to="body">
      <glass-modal
        v-if="modalOpen && selected"
        :title="String(t('admin.courses.lessonNotes.modalTitle') || '课堂笔记')"
        size="xl"
        height-variant="max"
        :backdrop-dark="false"
        @close="closeModal"
      >
        <div class="space-y-3">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-primary shadow-sm px-3 py-2">
              <div class="text-xs text-subtle">{{ t('admin.courses.lessonNotes.columns.student') || '学生' }}</div>
              <div class="font-medium mt-1">{{ selected.studentName || `#${selected.studentId}` }}</div>
            </div>
            <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-info shadow-sm px-3 py-2">
              <div class="text-xs text-subtle">{{ t('admin.courses.lessonNotes.columns.lesson') || '节次' }}</div>
              <div class="font-medium mt-1">{{ selected.lessonTitle || `#${selected.lessonId}` }}</div>
            </div>
          </div>

          <div class="rounded-xl border border-white/20 dark:border-white/10 bg-white/5 glass-ultraThin glass-tint-warning shadow-sm px-3 py-2">
            <div class="text-xs text-subtle">{{ t('admin.courses.lessonNotes.columns.updatedAt') || '更新时间' }}</div>
            <div class="font-medium mt-1">{{ formatDateTime(selected.updatedAt) }}</div>
          </div>

          <div class="rounded-2xl border border-white/10 bg-white/5 p-4">
            <pre class="whitespace-pre-wrap text-sm leading-6">{{ selected.notes || '' }}</pre>
          </div>
        </div>
        <template #footer>
          <Button variant="outline" @click="closeModal">{{ t('common.close') || '关闭' }}</Button>
        </template>
      </glass-modal>
    </Teleport>
  </card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { adminApi, type AdminLessonNoteListItem } from '@/api/admin.api'
import { downloadBlobAsFile, downloadCsv } from '@/utils/download'

const { t } = useI18n()

const props = withDefaults(defineProps<{
  courseId: string
  lessons?: Array<{ id: string | number; title?: string }>
  defaultLessonId?: string | number | null
  defaultStudentId?: string | number | null
}>(), {
  lessons: () => [],
  defaultLessonId: null,
  defaultStudentId: null,
})

const loading = ref(false)
const error = ref<string | null>(null)
const items = ref<AdminLessonNoteListItem[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)

const q = ref('')
const lessonId = ref<string>('')
let searchTimer: ReturnType<typeof setTimeout> | null = null

const lessonOptions = computed(() => {
  const opts = [{ label: String(t('common.all') || '全部'), value: '' }]
  for (const l of (props.lessons || [])) {
    const id = String((l as any)?.id ?? '')
    if (!id) continue
    opts.push({ label: String((l as any)?.title || `#${id}`), value: id })
  }
  return opts
})

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res: any = await adminApi.pageCourseLessonNotes(props.courseId, {
      page: page.value,
      size: pageSize.value,
      q: q.value || undefined,
      lessonId: lessonId.value ? Number(lessonId.value) : undefined,
    })
    items.value = res?.items || res?.data?.items || []
    total.value = Number(res?.total || res?.data?.total || 0)
    totalPages.value = Number(res?.totalPages || res?.data?.totalPages || Math.max(1, Math.ceil(total.value / Math.max(1, pageSize.value))))
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function onExportCsv() {
  try {
    const blob = await adminApi.exportCourseLessonNotesCsv({
      courseId: props.courseId,
      q: q.value || undefined,
      lessonId: lessonId.value ? Number(lessonId.value) : undefined,
    })
    downloadCsv(blob as any, `course_${props.courseId}_lesson_notes.csv`)
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '导出失败')
  }
}

async function onExportZip() {
  try {
    const blob = await adminApi.exportCourseLessonNotesZip({
      courseId: props.courseId,
      q: q.value || undefined,
      lessonId: lessonId.value ? Number(lessonId.value) : undefined,
    })
    downloadBlobAsFile(blob as any, `course_${props.courseId}_lesson_notes.zip`)
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '导出失败')
  }
}

const modalOpen = ref(false)
const selected = ref<AdminLessonNoteListItem | null>(null)
function openModal(it: AdminLessonNoteListItem) {
  selected.value = it
  modalOpen.value = true
}
function closeModal() {
  modalOpen.value = false
  selected.value = null
}

watch(
  () => props.defaultLessonId,
  (v) => {
    if (v == null) return
    const id = String(v)
    if (!lessonId.value) lessonId.value = id
  },
  { immediate: true }
)

function formatDateTime(v?: string) {
  const raw = String(v || '').trim()
  if (!raw) return '-'
  return raw.replace('T', ' ').slice(0, 16)
}

watch(
  [q, lessonId],
  () => {
    if (searchTimer) clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
      page.value = 1
      reload()
    }, 300)
  }
)

onMounted(reload)
</script>

