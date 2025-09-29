<template>
  <div class="p-4 lg:p-6">
    <!-- 面包屑 -->
    <nav class="mb-4 relative z-10">
      <ol class="flex items-center space-x-2 text-sm">
        <li>
          <router-link to="/student/courses" class="text-[var(--color-base-content)] hover:text-[var(--color-primary)]">{{ t('student.courses.title') }}</router-link>
        </li>
        <li v-if="lesson?.courseId"><span class="text-[color-mix(in_oklab,var(--color-base-content)_45%,transparent)]">&gt;</span></li>
        <li v-if="lesson?.courseId">
          <router-link :to="`/student/courses/${lesson.courseId}`" class="text-[var(--color-base-content)] hover:text-[var(--color-primary)]">{{ courseTitle }}</router-link>
        </li>
        <li><span class="text-[color-mix(in_oklab,var(--color-base-content)_45%,transparent)]">&gt;</span></li>
        <li class="font-medium text-[var(--color-base-content)] truncate">{{ lesson?.title }}</li>
      </ol>
    </nav>
    <div v-if="loading" class="text-center py-12">{{ t('student.courses.loading') }}</div>
    <div v-else-if="!lesson" class="text-center py-12 card">{{ t('student.courses.detail.notFoundTitle') }}</div>
    <div v-else class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 目录 -->
      <aside class="lg:col-span-1">
        <card padding="sm" class="max-h-[70vh] overflow-auto" tint="secondary">
          <h3 class="text-sm font-semibold mb-2">{{ t('student.courses.detail.contents') }}</h3>
          <div class="space-y-2">
            <div v-for="group in groupedChapters" :key="group.key" class="glass-ultraThin border rounded-xl">
              <div class="px-2 py-2.5 flex items-center justify-between cursor-pointer select-none" @click="toggleChapter(group.key)">
                <div class="text-sm font-semibold text-[var(--color-base-content)] truncate pl-2">{{ group.title }}</div>
                <svg v-if="isExpanded(group.key)" class="w-4 h-4" style="color: color-mix(in oklab, var(--color-base-content) 55%, transparent)" viewBox="0 0 20 20" fill="currentColor"><path d="M5.23 12.21a.75.75 0 001.06.02L10 8.73l3.71 3.5a.75.75 0 001.04-1.08l-4.23-4a.75.75 0 00-1.04 0l-4.25 4a.75.75 0 00.02 1.06z"/></svg>
                <svg v-else class="w-4 h-4" style="color: color-mix(in oklab, var(--color-base-content) 55%, transparent)" viewBox="0 0 20 20" fill="currentColor"><path d="M14.77 7.79a.75.75 0 00-1.06-.02L10 11.27 6.29 7.77a.75.75 0 00-1.04 1.08l4.23 4a.75.75 0 001.04 0l4.25-4a.75.75 0 00.02-1.06z"/></svg>
              </div>
              <ul v-show="isExpanded(group.key)" class="px-2 pb-2 space-y-1">
                <li v-for="(it, idx) in group.items" :key="it.id">
                  <router-link :to="`/student/lessons/${it.id}`" class="flex items-center gap-2 text-sm p-2 rounded-xl transition hover:bg-[color-mix(in_oklab,var(--color-base-content)_6%,transparent)]"
                    :class="{ 'font-semibold text-[var(--color-primary)] bg-[color-mix(in_oklab,var(--color-primary)_15%,transparent)] ring-1 ring-[color-mix(in_oklab,var(--color-primary)_28%,transparent)] rounded-xl': String(it.id)===String(lesson.id) }">
                    <span class="inline-flex items-center justify-center w-5 h-5 rounded-full text-[11px] font-medium"
                          :class="getLessonProgress(it.id) >= 100 ? 'text-[var(--color-success-content)] bg-[color-mix(in_oklab,var(--color-success)_80%,transparent)]' : 'text-[var(--color-primary-content)] bg-[color-mix(in_oklab,var(--color-primary)_70%,transparent)]'">
                      <template v-if="getLessonProgress(it.id) >= 100">✓</template>
                      <template v-else>{{ idx + 1 }}</template>
                    </span>
                    <span class="truncate flex-1">{{ it.title }}</span>
                    <span class="ml-2 shrink-0">
                      <progress-circle :value="getLessonProgress(it.id)" :size="18" :stroke="3" :show-label="false" />
                    </span>
              </router-link>
            </li>
          </ul>
            </div>
          </div>
        </card>
      </aside>

      <!-- 主体 -->
      <section class="lg:col-span-3 space-y-6">
        <!-- 本节说明（右上进度圈并入，去除额外顶部空白） -->
        <card padding="md" class="space-y-2" tint="primary">
          <h4 class="font-medium mb-4">{{ t('student.courses.detail.sectionIntro') || '本节说明' }}</h4>
          <p class="text-sm whitespace-pre-line" style="color: color-mix(in oklab, var(--color-base-content) 75%, transparent)">{{ lesson.content || lesson.description || '-' }}</p>
        </card>

        <!-- 视频 -->
        <card padding="md" class="space-y-4" tint="accent">
          <h4 class="font-medium mb-4">{{ t('student.lesson.videoTitle') || '视频' }}</h4>
          <div v-if="lesson.videoUrl" class="relative z-10" @click="onVideoClick" style="cursor: pointer;">
            <div class="aspect-video w-full rounded overflow-hidden relative">
              <video
                ref="videoRef"
                class="w-full h-full object-contain"
                style="pointer-events:auto;"
                controls
                :controlslist="controlsList"
                playsinline
                preload="metadata"
                crossorigin="anonymous"
                @loadedmetadata="onLoadedMetadata"
                @timeupdate="onTimeUpdate"
                @ended="onVideoEnded"
                @error="onVideoError"
                :key="videoSrc || lesson.videoUrl"
              >
                <source :src="videoSrc || resolveVideo(lesson.videoUrl)" :type="videoType" />
              </video>
              <!-- 覆盖底部控制栏，阻止拖动进度条（仅当不允许拖动时） -->
              <div v-if="blockScrub" class="absolute inset-x-0 bottom-0" style="height: 38px; pointer-events: auto;"></div>
            </div>
            <div v-if="videoError" class="mt-2 text-sm text-red-600">{{ videoError }}</div>
          </div>
        </card>

        <!-- 资料 -->
        <card padding="md" class="space-y-3" tint="info">
          <h4 class="font-medium mb-4">{{ t('student.lesson.materialsTitle') || t('student.courses.detail.materials') }}</h4>
            <template v-if="materials.length">
              <ul class="space-y-3">
                <li v-for="f in materials" :key="f.id">
                  <div class="space-y-2">
                    <document-viewer :file="f" />
                    <div :data-fid="String(f.id)" :ref="makeFileSentinelRef(String(f.id))" style="height:1px;"></div>
                  </div>
                </li>
              </ul>
            </template>
            <p v-else class="text-sm" style="color: color-mix(in oklab, var(--color-base-content) 60%, transparent)">{{ t('student.courses.detail.noMaterials') }}</p>
        </card>

        <!-- 关联作业 -->
        <card padding="md" class="space-y-3" tint="warning">
          <div class="flex items-center justify-between mb-4">
            <h4 class="font-medium">{{ t('student.assignments.title') || '关联作业' }}</h4>
          </div>
          <template v-if="relatedAssignments.length">
            <ul class="space-y-2">
              <li v-for="a in relatedAssignments" :key="a.id" class="flex items-center justify-between p-3 rounded-xl border glass-ultraThin">
                <div class="min-w-0">
                  <div class="font-medium truncate">{{ a.title }}</div>
                  <div class="text-xs mt-0.5" style="color: color-mix(in oklab, var(--color-base-content) 60%, transparent)">
                    {{ t('student.assignments.due') }}{{ formatDate(a.dueDate || a.dueAt) || '-' }}
                  </div>
                </div>
                <div class="shrink-0 ml-3">
                  <Button size="sm" variant="primary" @click="goAssignment(a.id)">
                    <template #icon>
                      <EyeIcon class="w-4 h-4" />
                    </template>
                    {{ t('student.assignments.actions.view') || '进入' }}
                  </Button>
                </div>
              </li>
            </ul>
          </template>
          <p v-else class="text-sm" style="color: color-mix(in oklab, var(--color-base-content) 60%, transparent)">{{ t('student.assignments.empty') || '本小节暂无关联作业' }}</p>
        </card>

        <!-- 笔记 -->
        <card padding="md" class="" tint="secondary">
          <h4 class="font-medium mb-4">{{ t('student.courses.note') || '学习笔记' }}</h4>
          <glass-textarea v-model="notes" :rows="4" :placeholder="t('student.courses.notePh') || '记录你的要点...'" />
          <div class="mt-2 flex justify-end">
            <Button size="sm" variant="purple" :loading="saving" @click="saveNotes">
              <template #icon>
                <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M7.629 13.707L3.586 9.664l1.414-1.414 2.629 2.629 7.071-7.071 1.414 1.414-8.485 8.485a1 1 0 01-1.414 0z"/></svg>
              </template>
              {{ t('student.courses.save') || '保存' }}
            </Button>
          </div>
        </card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { lessonApi } from '@/api/lesson.api'
import { fileApi } from '@/api/file.api'
import { baseURL } from '@/api/config'
import { studentApi } from '@/api/student.api'
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import DocumentViewer from '@/components/viewers/DocumentViewer.vue'
import ProgressCircle from '@/components/ui/ProgressCircle.vue'
import { courseApi } from '@/api/course.api'
import { EyeIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const lesson = ref<any>(null)
const toc = ref<any[]>([])
const materials = ref<any[]>([])
const progressMap = ref<Record<string, number>>({})
// 提前声明，供资料相关 computed 使用
const materialsChecked = ref<Record<string, boolean>>({})
const rawAssignments = ref<any[]>([])
const relatedAssignments = computed(() => {
  const list = visibleAssignments(Array.isArray(rawAssignments.value) ? rawAssignments.value : [])
  const lid = String(lesson.value?.id || '')
  return list.filter((a: any) => String(a.lessonId || '') === lid)
})
const notes = ref('')
const saving = ref(false)
const videoRef = ref<HTMLVideoElement | null>(null)
const videoSrc = ref<string>('')
const videoType = ref<string>('video/mp4')
const videoError = ref('')
const videoProgress = ref(0)
const videoDuration = ref(0)
const videoWatchedSeconds = ref(0)
let fileObserver: IntersectionObserver | null = null
const requiredWatchPercent = 0.98
let hls: any = null
let lastAllowedTime = 0
let guardsBoundForSrc = ''

const courseTitle = ref('')

const controlsList = computed(() => {
  const l = lesson.value || {}
  const allowSpeed = (l.allowSpeedChange ?? true)
  const parts: string[] = []
  if (!allowSpeed) parts.push('noplaybackrate')
  return parts.join(' ')
})

const blockScrub = computed(() => {
  const l = lesson.value || {}
  return (l.allowScrubbing ?? true) === false
})

function resolveVideo(v: string) {
  return /^\d+$/.test(String(v)) ? `${baseURL}/files/${v}/stream` : v
}

function guessMimeFromUrl(url: string): string {
  const u = url.toLowerCase()
  if (u.endsWith('.webm')) return 'video/webm'
  if (u.endsWith('.mkv')) return 'video/x-matroska'
  if (u.endsWith('.mov')) return 'video/quicktime'
  if (u.endsWith('.avi')) return 'video/x-msvideo'
  return 'video/mp4'
}

let watchTimer: number | undefined
function startAutoReport() {
  stopAutoReport()
  watchTimer = window.setInterval(async () => {
    try {
      const vid = videoRef.value
      const progress = Math.min(100, Math.max(0, computeCombinedProgress()))
      const curId = String(lesson.value?.id || '')
      const storedNow = Number(progressMap.value[curId] || 0)
      // 不回退：仅在合成进度高于持久化进度时才上报
      if (!(progress > storedNow)) return
      const payload: any = { progress }
      if (vid) {
        const cur = Number(vid.currentTime || 0)
        payload.lastPosition = Math.round(cur)
        payload.studyTime = 5
      }
      await lessonApi.updateLessonProgress(String(lesson.value.id), payload)
      progressMap.value[String(lesson.value.id)] = progress
    } catch {}
  }, 5000)
}
function stopAutoReport() {
  if (watchTimer) window.clearInterval(watchTimer)
  watchTimer = undefined
}

async function loadAll(lessonId: string) {
  loading.value = true
  try {
    const data: any = await lessonApi.getLesson(lessonId)
    lesson.value = data
    // 先读取后端持久化进度，避免初次进入显示为 0 的闪烁
    try { await syncStoredProgressOnce(String(lessonId)) } catch {}
    // 初始化材料勾选状态
    try {
      const res: any = await lessonApi.getLessonMaterials(String(lessonId))
      const list = res?.data || res || []
      for (const it of (Array.isArray(list)?list:[])) {
        materialsChecked.value[String(it.id)] = false
      }
    } catch {}
    // 如果 videoUrl 是文件ID，则尝试预览接口优先
    if (lesson.value?.videoUrl && /^\d+$/.test(String(lesson.value.videoUrl))) {
      const id = String(lesson.value.videoUrl)
      // 直接使用 /download 播放通常可行；部分环境下需要带 token 的 blob URL
      try {
        const blob: Blob = await fetchVideoBlob(String(id))
        videoType.value = blob.type || 'video/mp4'
        const url = URL.createObjectURL(blob)
        videoSrc.value = url
        videoError.value = ''
      } catch {
        videoSrc.value = `${baseURL}/files/${id}/preview?inline=1`
        videoType.value = 'video/mp4'
        videoError.value = ''
      }
    } else {
      const direct = String(lesson.value?.videoUrl || '')
      // 如果是我方后端直链（/files/{id}/download），用带 token 的 Blob 播放，避免 <video> 无法附带鉴权头
      const m = direct.match(/\/files\/(\d+)\/(?:download|preview|stream)(\b|$)/)
      if (direct.startsWith(baseURL) && m && m[1]) {
        try {
          const id = m[1]
          const blob: Blob = await fetchVideoBlob(String(id))
          videoType.value = blob.type || guessMimeFromUrl(direct)
          videoSrc.value = URL.createObjectURL(blob)
          videoError.value = ''
        } catch {
          const id = m[1]
          videoSrc.value = `${baseURL}/files/${id}/stream`
          videoType.value = guessMimeFromUrl(direct)
          videoError.value = ''
        }
      } else {
        // HLS 处理优先
        if (/\.m3u8(\?|$)/i.test(direct)) {
          videoType.value = 'application/vnd.apple.mpegurl'
          await trySetupHls(direct)
          videoSrc.value = direct // 供 Safari 原生播放
          videoError.value = ''
        } else {
          videoSrc.value = ''
          videoType.value = direct ? guessMimeFromUrl(direct) : 'video/mp4'
          videoError.value = ''
        }
      }
    }
    await nextTick()
    attachVideoGuards()
    // 加载目录（同课程其他节次）
    try {
      const list: any = await lessonApi.getLessonsByCourse(String(data.courseId))
      toc.value = Array.isArray(list) ? list : (list?.items || [])
    } catch { toc.value = [] }
    // 读取课程标题用于面包屑
    try {
      if (data?.courseId) {
        const resp: any = await courseApi.getCourseById(Number(data.courseId))
        const ct = resp?.data?.title || resp?.title || ''
        if (ct) courseTitle.value = String(ct)
      }
    } catch { courseTitle.value = '' }
    // 加载学生在本课程下的进度列表（与本地合并取最大，避免回退）
    try {
      if (data?.courseId) {
        const res: any = await lessonApi.getStudentCourseProgressList(String(data.courseId))
        const arr = (res?.data || res || []) as any[]
        const map: Record<string, number> = {}
        for (const p of (Array.isArray(arr) ? arr : [])) {
          const lid = String(p.lessonId || p.lesson_id || p.lessonID || p.id || '')
          const val = Number(p.progress ?? p.percentage ?? 0)
          if (lid) map[lid] = Number.isFinite(val) ? Math.round(val) : 0
        }
        const merged: Record<string, number> = { ...progressMap.value }
        for (const [lid, val] of Object.entries(map)) {
          const existed = Number(merged[lid] || 0)
          merged[lid] = Math.max(existed, Number(val || 0))
        }
        progressMap.value = merged
      }
    } catch { progressMap.value = {} }
    // 加载资料（方案A：走 /lessons/{id}/materials）
    try {
      const res: any = await lessonApi.getLessonMaterials(String(lessonId))
      materials.value = res?.data || res || []
    } catch { materials.value = [] }
    // 依据内容与已存进度进行一次和解：
    try { await reconcileStoredProgressWithContent() } catch {}
    // 加载关联作业（按课程取，再按 lessonId 过滤）
    try {
      if (data?.courseId) {
        const res: any = await (await import('@/api/assignment.api')).assignmentApi.getAssignmentsByCourse(String(data.courseId), { page: 1, size: 200 })
        const items = res?.items || res?.data?.items || res || []
        rawAssignments.value = Array.isArray(items) ? items : (items.items || [])
      } else {
        rawAssignments.value = []
      }
    } catch { rawAssignments.value = [] }
    // 开始自动上报
    startAutoReport()
  } finally {
    loading.value = false
  }
}

async function reconcileStoredProgressWithContent() {
  const id = String(lesson.value?.id || '')
  if (!id) return
  // 后端存量
  let stored = progressMap.value[id]
  if (!Number.isFinite(stored)) {
    try {
      const detail: any = await studentApi.getLessonDetails(id)
      const p = detail?.data?.progress || detail?.progress || null
      const pv = Number(p?.progress ?? p?.percentage ?? p ?? 0)
      if (Number.isFinite(pv)) stored = Math.round(pv)
    } catch { stored = 0 as any }
  }
  const hasVideo = !!String(lesson.value?.videoUrl || '')
  const hasMaterials = (materials.value || []).length > 0
  const currentSig = computeContentSignature()
  const prevSig = readContentSignature(id)
  if (!hasVideo && !hasMaterials) {
    if (!Number.isFinite(stored) || stored < 100) {
      await lessonApi.updateLessonProgress(id, { progress: 100 })
      stored = 100 as any
    }
    writeContentSignature(id, currentSig)
  } else {
    // 不下调：尊重后端已有值（可能因作业完成被置 100）
    if (!Number.isFinite(stored)) stored = 0 as any
    // 若此前为完成态且内容变化，仅记录新签名，不在进入时下调；交给学习过程自然提升
    if ((stored as any) >= 100 && prevSig && prevSig !== currentSig) {
      writeContentSignature(id, currentSig)
    }
  }
  progressMap.value[id] = Number.isFinite(stored as any) ? Math.round(stored as any) : 0
}

async function syncStoredProgressOnce(lessonId: string) {
  if (!lessonId) return
  try {
    const detail: any = await studentApi.getLessonDetails(String(lessonId))
    const p = detail?.data?.progress || detail?.progress || null
    const pv = Number(p?.progress ?? p?.percentage ?? p ?? 0)
    if (Number.isFinite(pv)) {
      progressMap.value[String(lessonId)] = Math.round(pv)
    }
  } catch {}
}

async function saveNotes() {
  if (!lesson.value?.id) return
  saving.value = true
  try {
    await lessonApi.addLessonNotes(String(lesson.value.id), notes.value.trim())
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  const id = String(route.params.id || '')
  if (id) loadAll(id)
})
onBeforeUnmount(stopAutoReport)
onBeforeUnmount(() => { if (fileObserver) { try { fileObserver.disconnect() } catch {} fileObserver = null } })

function onVideoClick() {
  const v = videoRef.value
  if (!v) return
  if (v.paused) v.play().catch(() => {})
}

function onLoadedMetadata() {
  const v = videoRef.value
  if (!v) return
  videoDuration.value = Number(v.duration || 0)
}

function onTimeUpdate() {
  const v = videoRef.value
  if (!v) return
  const dur = Number(v.duration || 0)
  const cur = Number(v.currentTime || 0)
  if (dur > 0) videoProgress.value = Math.min(1, cur / dur)
  // 记录观看秒数（简单累加，依赖 timeupdate 触发频率）
  videoWatchedSeconds.value = Math.max(videoWatchedSeconds.value, Math.floor(cur))
}

function onVideoEnded() {
  videoProgress.value = 1
  try { reportProgressImmediate('video_end') } catch {}
}

function attachVideoGuards() {
  const v = videoRef.value
  if (!v) return
  const allowScrub = (lesson.value?.allowScrubbing ?? true)
  const allowSpeed = (lesson.value?.allowSpeedChange ?? true)

  // 只在源变更时重新绑定
  const curSrc = v.currentSrc || (videoSrc.value || resolveVideo(String(lesson.value?.videoUrl || '')))
  if (guardsBoundForSrc === curSrc) return
  guardsBoundForSrc = curSrc

  lastAllowedTime = 0
  v.addEventListener('loadedmetadata', () => {
    if (!allowSpeed) {
      try { v.playbackRate = 1 } catch {}
    }
    lastAllowedTime = 0
  }, { once: true })

  v.addEventListener('timeupdate', () => {
    lastAllowedTime = v.currentTime || lastAllowedTime
  })

  v.addEventListener('ratechange', () => {
    if (!allowSpeed && Math.abs(v.playbackRate - 1) > 0.001) {
      try { v.playbackRate = 1 } catch {}
    }
  })

  v.addEventListener('seeking', () => {
    if (!allowScrub) {
      const cur = v.currentTime || 0
      if (Math.abs(cur - lastAllowedTime) > 1) {
        try { v.currentTime = lastAllowedTime } catch {}
      }
    }
  })

  // 进一步阻止点击控制栏改变 currentTime（进度条点击/拖动）
  if (!allowScrub) {
    v.addEventListener('mousedown', (e) => {
      const target = e.target as HTMLElement
      // 拦截对 <video> 控制区域的点击尝试（大部分浏览器将 seek 映射为控件事件）
      try { e.stopPropagation() } catch {}
    }, { capture: true })
    v.addEventListener('click', (e) => { try { e.stopPropagation() } catch {} }, { capture: true })
  }
}

// 当 allow 配置改变时，重新应用限制
watch(() => [lesson.value?.allowScrubbing, lesson.value?.allowSpeedChange, videoSrc.value], async () => {
  await nextTick()
  attachVideoGuards()
})

// 当内容结构变化（视频地址或资料数量），触发一次持久化和解（用于“后期新增内容则重算”）
watch(() => [String(lesson.value?.videoUrl || ''), (materials.value || []).length], async () => {
  await nextTick()
  try { await reconcileStoredProgressWithContent() } catch {}
})

async function onVideoError() {
  try {
    const v = String(lesson.value?.videoUrl || '')
    if (!v) return
    // 如果此前未尝试 Blob，则尝试一次带鉴权的 Blob 拉取
    if (!/^blob:/.test(videoSrc.value)) {
      if (/^\d+$/.test(v)) {
        const blob: Blob = await fetchVideoBlob(String(v))
        videoType.value = blob.type || 'video/mp4'
        videoSrc.value = URL.createObjectURL(blob)
        videoError.value = ''
        await nextTick(); videoRef.value?.load?.()
        return
      }
      const m = v.match(/\/files\/(\d+)\/(?:download|preview|stream)(\b|$)/)
      if (v.startsWith(baseURL) && m && m[1]) {
        const blob: Blob = await fetchVideoBlob(String(m[1]))
        videoType.value = blob.type || guessMimeFromUrl(v)
        videoSrc.value = URL.createObjectURL(blob)
        videoError.value = ''
        await nextTick(); videoRef.value?.load?.()
        return
      }
    }
    // HLS 回退提示
    if (/\.m3u8(\?|$)/i.test(v)) {
      const ok = await trySetupHls(v)
      if (!ok) videoError.value = (t('student.courses.video.hlsNotSupported') as any) || '该浏览器不支持 HLS 播放，请尝试使用 Safari 或更换视频格式。'
    } else {
      videoError.value = (t('student.courses.video.playFailed') as any) || '视频播放失败，请检查网络或文件格式。'
    }
  } catch {
    videoError.value = (t('student.courses.video.playFailed') as any) || '视频播放失败，请检查网络或文件格式。'
  }
}

async function trySetupHls(url: string): Promise<boolean> {
  try {
    // 若浏览器原生支持（如 Safari），无需 HLS.js
    const v = videoRef.value
    if (!v) return false
    const canNative = v.canPlayType('application/vnd.apple.mpegurl')
    if (canNative) return true
    // 运行时从 CDN 加载 hls.js，避免打包期解析依赖
    let HlsCtor: any = (window as any).Hls
    if (!HlsCtor) {
      const ok = await loadHlsFromCdn()
      if (!ok) return false
      HlsCtor = (window as any).Hls
    }
    if (hls) { try { hls.destroy() } catch {} hls = null }
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

// 统一的受保护视频 Blob 获取：按多端点与 inline 优先策略回退
async function fetchVideoBlob(id: string): Promise<Blob> {
  const client = (await import('@/api/config')).default as any
  const tries = [
    `/files/${encodeURIComponent(id)}/stream`,
    `/files/${encodeURIComponent(id)}/download`
  ]
  let lastErr: any = null
  for (const path of tries) {
    try {
      const resp: any = await client.get(path, { responseType: 'blob' })
      const blob: Blob = resp instanceof Blob ? resp : new Blob([resp])
      if (blob && blob.size > 0) return blob
    } catch (e) {
      lastErr = e
    }
  }
  throw lastErr || new Error('Unable to fetch video blob')
}

function makeFileSentinelRef(id: string) {
  return (el: Element | null) => registerFileSentinel(el, id)
}

function registerFileSentinel(el: Element | null, id: string) {
  if (!el) return
  if (!('IntersectionObserver' in window)) {
    materialsChecked.value[id] = true
    return
  }
  if (!fileObserver) {
    fileObserver = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        const fid = (entry.target as HTMLElement).getAttribute('data-fid') || ''
        if (entry.isIntersecting && fid) {
          materialsChecked.value[fid] = true
          try { fileObserver?.unobserve(entry.target) } catch {}
          try { reportProgressImmediate('material_seen') } catch {}
        }
      }
    }, { root: null, rootMargin: '0px', threshold: 1.0 })
  }
  try { fileObserver.observe(el as Element) } catch {}
}

// 内容签名：用于判定“后期新增内容”导致的重算，仅在此前为完成态时触发下调
function computeContentSignature(): string {
  try {
    const vid = String(lesson.value?.videoUrl || '')
    const mcount = (materials.value || []).length
    return `${vid ? 'v1' : 'v0'}#m${mcount}`
  } catch { return 'v0#m0' }
}

function signatureStorageKey(lessonId: string) { return `lesson_sig_${lessonId}` }
function readContentSignature(lessonId: string): string | null {
  try { return localStorage.getItem(signatureStorageKey(lessonId)) } catch { return null }
}
function writeContentSignature(lessonId: string, sig: string) {
  try { localStorage.setItem(signatureStorageKey(lessonId), sig) } catch {}
}

function goAssignment(id: string | number) {
  router.push(`/student/assignments/${id}/submit`)
}

function formatDate(v: any) {
  try { const d = new Date(v); if (isNaN(d.getTime())) return ''; return d.toLocaleString() } catch { return '' }
}

function visibleAssignments(list: any[]): any[] {
  const now = Date.now()
  return (Array.isArray(list) ? list : []).filter((a: any) => {
    const st = String(a?.status || '').toLowerCase()
    if (!st) return true
    if (st === 'crafted' || st === 'draft') return false
    if (st === 'scheduled') {
      const ts = a?.publishAt || a?.publish_at
      if (!ts) return false
      try { return now >= new Date(ts).getTime() } catch { return false }
    }
    return true
  })
}

const totalMaterials = computed(() => (materials.value || []).length)
const viewedMaterials = computed(() => Object.values(materialsChecked.value || {}).filter(Boolean).length)
const materialsProgressPercent = computed(() => {
  const total = Number(totalMaterials.value || 0)
  if (total <= 0) return 0
  const viewed = Number(viewedMaterials.value || 0)
  return Math.min(100, Math.round((viewed / total) * 100))
})

function computeCombinedProgress(): number {
  const hasVideo = !!String(lesson.value?.videoUrl || '')
  const hasMaterials = (totalMaterials.value || 0) > 0
  const videoPct = Math.round((videoProgress.value || 0) * 100)
  const materialsPct = materialsProgressPercent.value
  if (hasVideo && hasMaterials) return Math.round((videoPct + materialsPct) / 2)
  if (hasVideo) return videoPct
  if (hasMaterials) return materialsPct
  return 0
}

async function reportProgressImmediate(reason?: string) {
  try {
    const progress = Math.min(100, Math.max(0, computeCombinedProgress()))
    const curId = String(lesson.value?.id || '')
    const storedNow = Number(progressMap.value[curId] || 0)
    // 不回退：仅当新合成进度高于已存进度时才上报
    if (!(progress > storedNow)) return
    const v = videoRef.value
    const payload: any = { progress }
    if (v) {
      const cur = Number(v.currentTime || 0)
      payload.lastPosition = Math.round(cur)
    }
    await lessonApi.updateLessonProgress(String(lesson.value.id), payload)
    progressMap.value[String(lesson.value.id)] = progress
    if (progress >= 100) {
    try {
      const cid = String(lesson.value?.courseId || '')
      if (cid) {
          const res: any = await lessonApi.getStudentCourseProgressList(cid)
          const arr = (res?.data || res || []) as any[]
          const map: Record<string, number> = {}
          for (const p of (Array.isArray(arr) ? arr : [])) {
            const lid = String(p.lessonId || p.lesson_id || p.lessonID || p.id || '')
            const val = Number(p.progress ?? p.percentage ?? 0)
            if (lid) map[lid] = Number.isFinite(val) ? Math.round(val) : 0
          }
          progressMap.value = { ...progressMap.value, ...map }
        }
      } catch {}
      }
    } catch {}
}

// 章节分组与折叠
const groupedChapters = computed(() => {
  const buckets: Record<string, any[]> = {}
  for (const l of (toc.value || [])) {
    const key = (l as any)?.chapterId ? String((l as any).chapterId) : '__ungrouped__'
    if (!buckets[key]) buckets[key] = []
    buckets[key].push(l)
  }
  const chapterKeys = Object.keys(buckets).filter(k => k !== '__ungrouped__')
  const orderOf = (it: any) => Number(it?.orderIndex ?? it?.order ?? it?.id ?? 0)
  chapterKeys.sort((a, b) => {
    const la = [...buckets[a]].sort((x:any,y:any)=> orderOf(x) - orderOf(y))[0]
    const lb = [...buckets[b]].sort((x:any,y:any)=> orderOf(x) - orderOf(y))[0]
    return orderOf(la) - orderOf(lb)
  })
  const list: Array<{ key: string, title: string, items: any[] }> = []
  for (let i = 0; i < chapterKeys.length; i++) {
    const k = chapterKeys[i]
    const items = [...buckets[k]].sort((x:any,y:any)=> orderOf(x) - orderOf(y))
    const title = `第${i + 1}章`
    list.push({ key: k, title, items })
  }
  const ungrouped = buckets['__ungrouped__'] || []
  if (ungrouped.length) {
    const items = [...ungrouped].sort((x:any,y:any)=> orderOf(x) - orderOf(y))
    list.push({ key: '__ungrouped__', title: (t('student.courses.detail.noChapter') as any) || '未分组', items })
  }
  return list
})

const collapsedChapters = ref<Set<string>>(new Set<string>())
function isExpanded(key: string): boolean { return !collapsedChapters.value.has(key) }
function toggleChapter(key: string) {
  const set = new Set(collapsedChapters.value)
  if (set.has(key)) {
    set.delete(key)
  } else {
    set.add(key)
  }
  collapsedChapters.value = set
}

// 进度
function getLessonProgress(lessonId: string | number): number {
  const v = progressMap.value[String(lessonId)]
  return Number.isFinite(v) ? Math.round(v) : 0
}
const myProgressPercent = computed(() => {
  const id = String(lesson.value?.id || '')
  if (!id) return Math.min(100, Math.max(0, computeCombinedProgress()))
  const v = progressMap.value[id]
  if (Number.isFinite(v)) return Math.round(v)
  return Math.min(100, Math.max(0, computeCombinedProgress()))
})
</script>

 


