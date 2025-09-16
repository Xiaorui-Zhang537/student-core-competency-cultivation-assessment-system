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
            <div class="aspect-video w-full rounded overflow-hidden">
              <video
                ref="videoRef"
                class="w-full h-full object-contain"
                style="pointer-events:auto;"
                controls
                :controlslist="controlsList"
                playsinline
                preload="metadata"
                :src="videoSrc || resolveVideo(lesson.videoUrl)"
                :key="videoSrc || lesson.videoUrl"
              />
            </div>
          </div>

          <div>
            <h4 class="font-medium mb-2">{{ t('student.courses.detail.materials') }}</h4>
            <template v-if="materials.length">
              <ul class="space-y-3">
                <li v-for="f in materials" :key="f.id">
                  <div class="space-y-2">
                    <DocumentViewer :file="f" />
                  </div>
                </li>
              </ul>
            </template>
            <p v-else class="text-sm text-gray-500">{{ t('student.courses.detail.noMaterials') }}</p>
          </div>
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
const notes = ref('')
const saving = ref(false)
const videoRef = ref<HTMLVideoElement | null>(null)
const videoSrc = ref<string>('')
const videoType = ref<string>('video/mp4')
let lastAllowedTime = 0
let guardsBoundForSrc = ''

const controlsList = computed(() => {
  const l = lesson.value || {}
  const allowSpeed = (l.allowSpeedChange ?? true)
  const parts: string[] = []
  if (!allowSpeed) parts.push('noplaybackrate')
  return parts.join(' ')
})

function resolveVideo(v: string) {
  return /^\d+$/.test(String(v)) ? `${baseURL}/files/${v}/download` : v
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
    // 如果 videoUrl 是文件ID，则尝试预览接口优先
    if (lesson.value?.videoUrl && /^\d+$/.test(String(lesson.value.videoUrl))) {
      const id = String(lesson.value.videoUrl)
      // 直接使用 /download 播放通常可行；部分环境下需要带 token 的 blob URL
      try {
        const resp: any = await (await import('@/api/config')).default.get(`/files/${encodeURIComponent(id)}/download`, { responseType: 'blob' })
        const blob: Blob = resp instanceof Blob ? resp : new Blob([resp])
        videoType.value = blob.type || 'video/mp4'
        const url = URL.createObjectURL(blob)
        videoSrc.value = url
      } catch {
        videoSrc.value = `${baseURL}/files/${id}/download`
        videoType.value = 'video/mp4'
      }
    } else {
      const direct = String(lesson.value?.videoUrl || '')
      // 如果是我方后端直链（/files/{id}/download），用带 token 的 Blob 播放，避免 <video> 无法附带鉴权头
      const m = direct.match(/\/files\/(\d+)\/download(\b|$)/)
      if (direct.startsWith(baseURL) && m && m[1]) {
        try {
          const id = m[1]
          const resp: any = await (await import('@/api/config')).default.get(`/files/${encodeURIComponent(id)}/download`, { responseType: 'blob' })
          const blob: Blob = resp instanceof Blob ? resp : new Blob([resp])
          videoType.value = blob.type || guessMimeFromUrl(direct)
          videoSrc.value = URL.createObjectURL(blob)
        } catch {
          videoSrc.value = direct
          videoType.value = guessMimeFromUrl(direct)
        }
      } else {
        videoSrc.value = ''
        videoType.value = direct ? guessMimeFromUrl(direct) : 'video/mp4'
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

function onVideoClick() {
  const v = videoRef.value
  if (!v) return
  if (v.paused) v.play().catch(() => {})
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
}

// 当 allow 配置改变时，重新应用限制
watch(() => [lesson.value?.allowScrubbing, lesson.value?.allowSpeedChange, videoSrc.value], async () => {
  await nextTick()
  attachVideoGuards()
})
</script>

<style scoped>
.card { @apply bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded; }
</style>


