<template>
  <div class="p-4 lg:p-6">
    <div v-if="loading" class="text-center py-12">{{ t('student.courses.loading') }}</div>
    <div v-else-if="!lesson" class="text-center py-12 card">{{ t('student.courses.detail.notFoundTitle') }}</div>
    <div v-else class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 目录 -->
      <aside class="lg:col-span-1">
        <Card padding="sm" class="max-h-[70vh] overflow-auto" v-glass>
          <h3 class="text-sm font-semibold mb-2">{{ t('student.courses.detail.contents') }}</h3>
          <ul class="space-y-1">
            <li v-for="it in toc" :key="it.id">
              <router-link :to="`/student/lessons/${it.id}`" class="text-sm hover:underline" :class="{ 'font-semibold text-primary-600': String(it.id)===String(lesson.id) }">
                {{ it.title }}
              </router-link>
            </li>
          </ul>
        </Card>
      </aside>

      <!-- 主体 -->
      <section class="lg:col-span-3">
        <PageHeader :title="lesson.title" :subtitle="lesson.description || ''" />

        <!-- 视频/资料 -->
        <Card padding="md" class="space-y-4" v-glass>
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

          <div>
            <h4 class="font-medium mb-2">{{ t('student.courses.detail.materials') }}</h4>
            <template v-if="materials.length">
              <ul class="space-y-3">
                <li v-for="f in materials" :key="f.id">
                  <div class="space-y-2">
                    <DocumentViewer :file="f" />
                    <div :data-fid="String(f.id)" :ref="makeFileSentinelRef(String(f.id))" style="height:1px;"></div>
                  </div>
                </li>
              </ul>
            </template>
            <p v-else class="text-sm text-gray-500">{{ t('student.courses.detail.noMaterials') }}</p>
            
          </div>
        </Card>

        <!-- 关联作业 -->
        <Card padding="md" class="mt-4 space-y-3" v-glass>
          <div class="flex items-center justify-between">
            <h4 class="font-medium">{{ t('student.assignments.title') || '关联作业' }}</h4>
            <Button size="sm" variant="menu" @click="router.push('/student/assignments')">{{ t('student.assignments.viewAll') || '查看全部' }}</Button>
          </div>
          <template v-if="relatedAssignments.length">
            <ul class="space-y-2">
              <li v-for="a in relatedAssignments" :key="a.id" class="flex items-center justify-between p-3 rounded border">
                <div class="min-w-0">
                  <div class="font-medium truncate">{{ a.title }}</div>
                  <div class="text-xs text-gray-500 mt-0.5">
                    {{ t('student.assignments.due') }}{{ formatDate(a.dueDate || a.dueAt) || '-' }}
                  </div>
                </div>
                <div class="shrink-0 ml-3">
                  <Button size="sm" variant="primary" @click="goAssignment(a.id)">{{ t('student.assignments.actions.view') || '进入' }}</Button>
                </div>
              </li>
            </ul>
          </template>
          <p v-else class="text-sm text-gray-500">{{ t('student.assignments.empty') || '本小节暂无关联作业' }}</p>
        </Card>

        <!-- 笔记 -->
        <Card padding="md" class="mt-4" v-glass>
          <h4 class="font-medium mb-2">{{ t('student.courses.note') || '学习笔记' }}</h4>
          <GlassTextarea v-model="notes" :rows="4" :placeholder="t('student.courses.notePh') || '记录你的要点...'" />
          <div class="mt-2 flex justify-end">
            <Button size="sm" variant="primary" :loading="saving" @click="saveNotes">{{ t('student.courses.save') || '保存' }}</Button>
          </div>
        </Card>
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
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import DocumentViewer from '@/components/viewers/DocumentViewer.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const lesson = ref<any>(null)
const toc = ref<any[]>([])
const materials = ref<any[]>([])
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
const materialsChecked = ref<Record<string, boolean>>({})
let fileObserver: IntersectionObserver | null = null
const completingLesson = ref(false)
const requiredWatchPercent = 0.98
let hls: any = null
let lastAllowedTime = 0
let guardsBoundForSrc = ''

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
      const payload: any = {}
      if (vid) {
        const dur = Number(vid.duration || 0)
        const cur = Number(vid.currentTime || 0)
        if (dur > 0) payload.progress = Math.min(100, Math.round((cur / dur) * 100))
        payload.lastPosition = Math.round(cur)
        payload.studyTime = 5
      }
      await lessonApi.updateLessonProgress(String(lesson.value.id), payload)
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
    // 加载资料（方案A：走 /lessons/{id}/materials）
    try {
      const res: any = await lessonApi.getLessonMaterials(String(lessonId))
      materials.value = res?.data || res || []
    } catch { materials.value = [] }
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
        }
      }
    }, { root: null, rootMargin: '0px', threshold: 1.0 })
  }
  try { fileObserver.observe(el as Element) } catch {}
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

const allMaterialsChecked = computed(() => {
  const vals = Object.values(materialsChecked.value || {})
  return vals.length === 0 ? true : vals.every(Boolean)
})

const canMarkCompleted = computed(() => {
  // 条件：1) 看满 requiredWatchPercent 2) 所有资料勾选
  const okVideo = videoProgress.value >= requiredWatchPercent
  return okVideo && allMaterialsChecked.value
})

watch(canMarkCompleted, async (ok) => {
  if (!ok) return
  if (!lesson.value?.id) return
  if (completingLesson.value) return
  completingLesson.value = true
  try {
    await lessonApi.updateLessonProgress(String(lesson.value.id), { progress: 100, lastPosition: Math.floor(videoDuration.value) })
    try { await lessonApi.completeLesson(String(lesson.value.id)) } catch { /* 学生无权直接更新 Lesson，忽略 */ }
    // 刷新课程页的进度（若来自课程详情）
    try {
      const cid = String(lesson.value?.courseId || '')
      if (cid) {
        const { useCourseStore } = await import('@/stores/course')
        const cs = useCourseStore()
        await cs.fetchCourseById(cid)
      }
    } catch {}
  } finally {
    completingLesson.value = false
  }
})
</script>

 


