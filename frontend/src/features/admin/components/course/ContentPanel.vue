<template>
  <card padding="md" tint="secondary">
    <div class="flex items-center justify-between gap-3 mb-3">
      <div class="text-sm font-medium">{{ t('admin.courses.contentTitle') || '课程内容' }}</div>
    </div>

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reloadAll"
    />

    <div v-else class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 左侧目录 -->
      <aside class="lg:col-span-1">
        <card padding="sm" class="max-h-[70vh] overflow-auto" tint="secondary">
          <div class="flex items-center justify-between mb-2">
            <h3 class="text-sm font-semibold">{{ t('student.courses.detail.contents') || t('admin.courses.contents') || '目录' }}</h3>
            <badge size="sm" variant="secondary">{{ lessonsFlat.length }}</badge>
          </div>

          <div class="space-y-2">
            <div v-for="group in groupedChapters" :key="group.key" class="glass-ultraThin border rounded-xl">
              <div class="px-2 py-2.5 flex items-center justify-between cursor-pointer select-none" @click="toggleChapter(group.key)">
                <div class="text-sm font-semibold truncate pl-2">{{ group.title }}</div>
                <svg
                  v-if="isExpanded(group.key)"
                  class="w-4 h-4"
                  style="color: color-mix(in oklab, var(--color-base-content) 55%, transparent)"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    d="M5.23 12.21a.75.75 0 001.06.02L10 8.73l3.71 3.5a.75.75 0 001.04-1.08l-4.23-4a.75.75 0 00-1.04 0l-4.25 4a.75.75 0 00.02 1.06z"
                  />
                </svg>
                <svg
                  v-else
                  class="w-4 h-4"
                  style="color: color-mix(in oklab, var(--color-base-content) 55%, transparent)"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    d="M14.77 7.79a.75.75 0 00-1.06-.02L10 11.27 6.29 7.77a.75.75 0 00-1.04 1.08l4.23 4a.75.75 0 001.04 0l4.25-4a.75.75 0 00.02-1.06z"
                  />
                </svg>
              </div>

              <ul v-show="isExpanded(group.key)" class="px-2 pb-2 space-y-1">
                <li v-for="(it, idx) in group.items" :key="String(it.id)">
                  <button
                    type="button"
                    class="w-full flex items-center gap-2 text-sm p-2 rounded-xl transition text-left hover:bg-[color-mix(in_oklab,var(--color-base-content)_6%,transparent)]"
                    :class="{
                      'font-semibold text-[var(--color-primary)] bg-[color-mix(in_oklab,var(--color-primary)_15%,transparent)] ring-1 ring-[color-mix(in_oklab,var(--color-primary)_28%,transparent)]':
                        String(it.id) === String(selectedLessonId),
                    }"
                    @click="selectLesson(String(it.id))"
                  >
                    <span class="inline-flex items-center justify-center w-5 h-5 rounded-full text-[11px] font-medium text-[var(--color-primary-content)] bg-[color-mix(in_oklab,var(--color-primary)_70%,transparent)]">
                      {{ idx + 1 }}
                    </span>
                    <span class="truncate flex-1">{{ it.title || `Lesson #${it.id}` }}</span>
                  </button>
                </li>
              </ul>
            </div>
          </div>

          <empty-state v-if="lessonsFlat.length === 0" :title="String(t('common.empty') || '暂无数据')" />
        </card>
      </aside>

      <!-- 右侧详情 -->
      <section class="lg:col-span-3 space-y-6">
        <empty-state v-if="!selectedLesson" :title="String(t('admin.courses.pickLesson') || '请选择一个节次')" />

        <template v-else>
          <!-- 本节说明 -->
          <card padding="md" class="space-y-2" tint="primary">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <h4 class="font-medium mb-1">{{ selectedLesson.title || `Lesson #${selectedLesson.id}` }}</h4>
                <p class="text-sm whitespace-pre-line" style="color: color-mix(in oklab, var(--color-base-content) 75%, transparent)">
                  {{ selectedLesson.content || selectedLesson.description || '-' }}
                </p>
              </div>
              <div class="shrink-0 text-xs text-subtle font-mono">#{{ selectedLesson.id }}</div>
            </div>
          </card>

          <!-- 视频 -->
          <card padding="md" class="space-y-4" tint="accent">
            <div class="flex items-center justify-between gap-3 mb-1">
              <h4 class="font-medium">{{ t('student.lesson.videoTitle') || t('admin.courses.video') || '视频' }}</h4>
              <badge v-if="selectedLesson.videoUrl" size="sm" variant="secondary">{{ t('common.preview') || '预览' }}</badge>
            </div>

            <div v-if="selectedLesson.videoUrl" class="relative z-10">
              <div class="aspect-video w-full rounded overflow-hidden relative border border-white/10 bg-black/20">
                <video
                  ref="videoRef"
                  class="w-full h-full object-contain"
                  controls
                  playsinline
                  preload="metadata"
                  crossorigin="anonymous"
                  :key="videoKey"
                >
                  <source v-if="!isHls(videoResolvedUrl)" :src="videoResolvedUrl" :type="videoType" />
                </video>
              </div>
              <div v-if="videoError" class="mt-2 text-sm text-red-600">{{ videoError }}</div>
              <div v-else-if="isHls(videoResolvedUrl)" class="mt-2 text-xs text-subtle">
                {{ t('admin.courses.video.hlsHint') || '如果无法播放 HLS（.m3u8），请尝试 Safari 或更换格式。' }}
              </div>
            </div>
            <p v-else class="text-sm" style="color: color-mix(in oklab, var(--color-base-content) 60%, transparent)">
              {{ t('admin.courses.noVideo') || '本节暂无视频。' }}
            </p>
          </card>

          <!-- 资料 -->
          <card padding="md" class="space-y-3" tint="info">
            <div class="flex items-center justify-between gap-3 mb-1">
              <h4 class="font-medium">{{ t('student.lesson.materialsTitle') || t('admin.courses.materials') || '资料' }}</h4>
              <badge size="sm" variant="secondary">{{ materials.length }}</badge>
            </div>
            <template v-if="materials.length">
              <ul class="space-y-3">
                <li v-for="f in materials" :key="String(f.id)" class="space-y-2">
                  <div class="flex items-center justify-between gap-3">
                    <div class="min-w-0">
                      <div class="text-sm font-medium truncate">{{ f.originalName || f.fileName || `#${f.id}` }}</div>
                      <div class="text-xs text-subtle truncate">#{{ f.id }}</div>
                    </div>
                    <div class="shrink-0 flex items-center gap-2">
                      <a class="btn btn-sm btn-outline rounded-full" :href="downloadHref(f.id)" target="_blank" rel="noreferrer">
                        {{ t('student.courses.detail.download') || t('common.download') || '下载' }}
                      </a>
                    </div>
                  </div>
                  <document-viewer :file="f" />
                </li>
              </ul>
            </template>
            <p v-else class="text-sm" style="color: color-mix(in oklab, var(--color-base-content) 60%, transparent)">
              {{ t('student.courses.detail.noMaterials') || t('admin.courses.noMaterials') || '本节暂无资料。' }}
            </p>
          </card>

          <!-- 关联作业 -->
          <card padding="md" class="space-y-3" tint="warning">
            <div class="flex items-center justify-between mb-1">
              <h4 class="font-medium">{{ t('student.assignments.title') || t('admin.courses.relatedAssignments') || '关联作业' }}</h4>
              <badge size="sm" variant="secondary">{{ relatedAssignments.length }}</badge>
            </div>
            <template v-if="relatedAssignments.length">
              <ul class="space-y-2">
                <li v-for="a in relatedAssignments" :key="String(a.id)" class="flex items-center justify-between p-3 rounded-xl border glass-ultraThin">
                  <div class="min-w-0">
                    <div class="font-medium truncate">{{ a.title || `#${a.id}` }}</div>
                    <div class="text-xs mt-0.5 text-subtle">
                      {{ t('student.assignments.due') || '截止：' }}{{ formatDate(a.dueDate || a.dueAt) || '-' }}
                    </div>
                  </div>
                  <div class="shrink-0 ml-3 text-xs text-subtle font-mono">#{{ a.id }}</div>
                </li>
              </ul>
              <div class="text-xs text-subtle mt-2">
                {{ t('admin.courses.assignmentNavHint') || '管理员端当前仅展示关联信息（只读）。' }}
              </div>
            </template>
            <p v-else class="text-sm" style="color: color-mix(in oklab, var(--color-base-content) 60%, transparent)">
              {{ t('student.assignments.empty') || '本小节暂无关联作业' }}
            </p>
          </card>

          <!-- 课堂笔记审计区 -->
          <LessonNotesPanel
            :course-id="courseId"
            :lessons="lessonsFlat"
            :default-lesson-id="selectedLessonId || null"
          />
        </template>
      </section>
    </div>
  </card>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import DocumentViewer from '@/components/viewers/DocumentViewer.vue'
import LessonNotesPanel from '@/features/admin/components/course/LessonNotesPanel.vue'
import { baseURL } from '@/api/config'
import { chapterApi } from '@/api/chapter.api'
import { lessonApi } from '@/api/lesson.api'
import { assignmentApi } from '@/api/assignment.api'

const { t } = useI18n()

const props = defineProps<{
  courseId: string
  active?: boolean
}>()

const emit = defineEmits<{
  (e: 'counts', v: { lessonCount: number; chapterCount: number; materialCount: number; assignmentCount: number }): void
}>()

const loading = ref(false)
const error = ref('')
const chapters = ref<any[]>([])
const lessons = ref<any[]>([])
const assignments = ref<any[]>([])
const materials = ref<any[]>([])

const selectedLessonId = ref<string>('')
const selectedLesson = computed(() => {
  const id = String(selectedLessonId.value || '')
  if (!id) return null
  return lessons.value.find(l => String(l?.id) === id) || null
})

const lessonsFlat = computed(() => {
  const list = Array.isArray(lessons.value) ? [...lessons.value] : []
  const orderOf = (it: any) => Number(it?.orderIndex ?? it?.order ?? it?.id ?? 0)
  return list.sort((a, b) => orderOf(a) - orderOf(b))
})

function chapterTitleOf(chapterId: string) {
  const c = chapters.value.find(x => String(x?.id) === String(chapterId))
  return String(c?.title || '')
}

const groupedChapters = computed(() => {
  const buckets: Record<string, any[]> = {}
  for (const l of lessonsFlat.value) {
    const key = (l as any)?.chapterId ? String((l as any).chapterId) : '__ungrouped__'
    if (!buckets[key]) buckets[key] = []
    buckets[key].push(l)
  }
  const chapterKeys = Object.keys(buckets).filter(k => k !== '__ungrouped__')
  const orderOfChapter = (id: string) => {
    const c = chapters.value.find(x => String(x?.id) === String(id))
    return Number(c?.orderIndex ?? c?.order ?? c?.id ?? 0)
  }
  chapterKeys.sort((a, b) => orderOfChapter(a) - orderOfChapter(b))
  const list: Array<{ key: string; title: string; items: any[] }> = []
  for (let i = 0; i < chapterKeys.length; i++) {
    const k = chapterKeys[i]
    const title = chapterTitleOf(k) || `第${i + 1}章`
    list.push({ key: k, title, items: buckets[k] || [] })
  }
  const ungrouped = buckets['__ungrouped__'] || []
  if (ungrouped.length) {
    list.push({ key: '__ungrouped__', title: (t('student.courses.detail.noChapter') as any) || '未分组', items: ungrouped })
  }
  return list
})

const collapsedChapters = ref<Set<string>>(new Set<string>())
function isExpanded(key: string): boolean {
  return !collapsedChapters.value.has(key)
}
function toggleChapter(key: string) {
  const set = new Set(collapsedChapters.value)
  if (set.has(key)) set.delete(key)
  else set.add(key)
  collapsedChapters.value = set
}

const relatedAssignments = computed(() => {
  const lid = String(selectedLessonId.value || '')
  if (!lid) return []
  const list = Array.isArray(assignments.value) ? assignments.value : []
  return list.filter((a: any) => String(a?.lessonId || '') === lid)
})

function formatDate(v?: string) {
  if (!v) return ''
  return String(v).replace('T', ' ').slice(0, 19)
}

function downloadHref(fileId: string | number): string {
  const token = (() => {
    try {
      return localStorage.getItem('token')
    } catch {
      return null
    }
  })()
  const qs = token ? `?token=${encodeURIComponent(String(token))}` : ''
  return `${baseURL}/files/${encodeURIComponent(String(fileId))}/download${qs}`
}

async function reloadAll() {
  loading.value = true
  error.value = ''
  try {
    const [cRes, lRes] = await Promise.all([chapterApi.listByCourse(props.courseId), lessonApi.getLessonsByCourse(props.courseId)])
    chapters.value = (cRes as any)?.data || cRes || []
    lessons.value = (lRes as any)?.data || lRes || []

    try {
      const aRes: any = await assignmentApi.getAssignmentsByCourse(props.courseId, { page: 1, size: 200 })
      const items = aRes?.items || aRes?.data?.items || aRes || []
      assignments.value = Array.isArray(items) ? items : (items?.items || [])
    } catch {
      assignments.value = []
    }

    if (!selectedLessonId.value) {
      const first = lessonsFlat.value[0]
      if (first?.id != null) selectedLessonId.value = String(first.id)
    }

    emit('counts', {
      lessonCount: Array.isArray(lessons.value) ? lessons.value.length : 0,
      chapterCount: Array.isArray(chapters.value) ? chapters.value.length : 0,
      materialCount: 0,
      assignmentCount: Array.isArray(assignments.value) ? assignments.value.length : 0,
    })
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function selectLesson(id: string) {
  selectedLessonId.value = id
}

// Video (HLS via CDN, same approach as student)
const videoRef = ref<HTMLVideoElement | null>(null)
let hls: any = null
const videoError = ref('')
const videoType = ref('video/mp4')

const videoResolvedUrl = computed(() => {
  const v = String((selectedLesson.value as any)?.videoUrl || '')
  if (!v) return ''
  if (/^\\d+$/.test(v)) return buildAuthedFileUrl(`/files/${encodeURIComponent(v)}/stream`)
  if (v.startsWith('/')) return buildAuthedFileUrl(v)
  return v
})

const videoKey = computed(() => `${selectedLessonId.value}::${videoResolvedUrl.value}`)

function isHls(url: string) {
  return /\.m3u8(\?|$)/i.test(String(url || ''))
}

function buildAuthedFileUrl(path: string): string {
  const token = (() => {
    try {
      return localStorage.getItem('token')
    } catch {
      return null
    }
  })()
  const cleanBase = String(baseURL || '/api').replace(/\/+$/, '')
  const cleanPath = path.startsWith('/') ? path : `/${path}`
  if (!token) return `${cleanBase}${cleanPath}`
  const join = cleanPath.includes('?') ? '&' : '?'
  return `${cleanBase}${cleanPath}${join}token=${encodeURIComponent(String(token))}`
}

function guessMimeFromUrl(url: string): string {
  const u = String(url || '').toLowerCase()
  if (u.endsWith('.webm')) return 'video/webm'
  if (u.endsWith('.mov')) return 'video/quicktime'
  if (u.endsWith('.avi')) return 'video/x-msvideo'
  return 'video/mp4'
}

async function loadHlsFromCdn(): Promise<boolean> {
  try {
    if ((window as any).Hls) return true
    await new Promise<void>((resolve, reject) => {
      const s = document.createElement('script')
      s.src = 'https://cdn.jsdelivr.net/npm/hls.js@1.5.14/dist/hls.min.js'
      s.async = true
      s.onload = () => resolve()
      s.onerror = () => reject(new Error('hls.js cdn failed'))
      document.head.appendChild(s)
    })
    return !!(window as any).Hls
  } catch {
    return false
  }
}

async function trySetupHls(url: string): Promise<boolean> {
  try {
    const v = videoRef.value
    if (!v) return false
    const canNative = v.canPlayType('application/vnd.apple.mpegurl')
    if (canNative) return true
    let HlsCtor: any = (window as any).Hls
    if (!HlsCtor) {
      const ok = await loadHlsFromCdn()
      if (!ok) return false
      HlsCtor = (window as any).Hls
    }
    if (hls) {
      try {
        hls.destroy()
      } catch {}
      hls = null
    }
    if (HlsCtor && HlsCtor.isSupported && HlsCtor.isSupported()) {
      hls = new HlsCtor()
      hls.loadSource(url)
      hls.attachMedia(v)
      return true
    }
    return false
  } catch {
    return false
  }
}

async function reloadMaterialsForLesson(lessonId: string) {
  try {
    const res: any = await lessonApi.getLessonMaterials(String(lessonId))
    materials.value = res?.data || res || []
  } catch {
    materials.value = []
  }
  emit('counts', {
    lessonCount: Array.isArray(lessons.value) ? lessons.value.length : 0,
    chapterCount: Array.isArray(chapters.value) ? chapters.value.length : 0,
    materialCount: Array.isArray(materials.value) ? materials.value.length : 0,
    assignmentCount: Array.isArray(assignments.value) ? assignments.value.length : 0,
  })
}

watch(
  () => selectedLessonId.value,
  async (id) => {
    if (!id) return
    await reloadMaterialsForLesson(id)
    videoError.value = ''
    const url = videoResolvedUrl.value
    videoType.value = guessMimeFromUrl(url)
    await nextTick()
    const v = videoRef.value
    if (!v || !url) return
    if (isHls(url)) {
      const ok = await trySetupHls(url)
      if (!ok) videoError.value = (t('student.courses.video.hlsNotSupported') as any) || '该浏览器不支持 HLS 播放，请尝试使用 Safari 或更换视频格式。'
    } else {
      try {
        if (hls) {
          try {
            hls.destroy()
          } catch {}
          hls = null
        }
        v.load?.()
      } catch {}
    }
  },
  { immediate: true }
)

watch(
  () => props.active,
  (v) => {
    if (v && !chapters.value.length && !lessons.value.length && !loading.value) reloadAll()
  },
  { immediate: true }
)

onMounted(() => {
  if (props.active !== false) reloadAll()
})
</script>

