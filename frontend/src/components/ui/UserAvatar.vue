<template>
  <div :class="wrapperClass" :style="wrapperStyle">
    <img
      v-if="modeResolved === 'img' && finalSrc"
      :src="finalSrc"
      :alt="alt || 'avatar'"
      :class="['w-full h-full', fitClass]"
      @error="onImgError"
    />
    <slot v-else></slot>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { baseURL } from '@/api/config'
import { api } from '@/api/config'
import { fileApi } from '@/api/file.api'

const props = withDefaults(defineProps<{
  avatar?: string | null
  size?: number | string
  alt?: string
  rounded?: boolean
  fit?: 'cover' | 'contain'
  mode?: 'auto' | 'img' | 'background'
  fallbackSrc?: string | null
}>(), {
  size: 32,
  rounded: true,
  fit: 'cover',
  mode: 'auto',
  fallbackSrc: null
})

const wrapperClass = computed(() => [
  'overflow-hidden bg-gray-100 dark:bg-gray-700 flex items-center justify-center',
  props.rounded ? 'rounded-full' : 'rounded'
])

const sizeStyle = computed(() => ({
  width: typeof props.size === 'number' ? `${props.size}px` : String(props.size),
  height: typeof props.size === 'number' ? `${props.size}px` : String(props.size)
}))

function extractFileIdFromPath(path: string): string | null {
  try {
    const m = String(path).match(/\/files\/(\d+)(?:\/|$)/)
    return m && m[1] ? m[1] : null
  } catch { return null }
}

const avatarKind = computed<'none' | 'http' | 'path' | 'fileId'>(() => {
  const av = props.avatar
  if (!av) return 'none'
  const s = String(av)
  if (/^data:/i.test(s)) return 'http'
  if (/^https?:\/\//i.test(s)) return 'http'
  if (s.startsWith('/')) return 'path'
  if (/^\d+$/.test(s)) return 'fileId'
  return 'none'
})

const imgSrc = computed(() => {
  const av = props.avatar
  if (!av) return null
  const s = String(av)
  const isHttp = /^https?:\/\//i.test(s)
  if (/^data:/i.test(s)) return s
  if (isHttp) return s
  // 非 http 的资源（fileId 或相对路径）需要带鉴权头，交给 tryLoadBlob 通过 fetch/axios 拉取
  return null
})

const isSvgLike = computed(() => {
  const av = props.avatar
  if (!av) return false
  const s = String(av)
  return /\.svg(\?|#|$)/i.test(s) || /format=svg/i.test(s) || /^data:image\/svg\+xml/i.test(s)
})

const modeResolved = computed(() => {
  if (props.mode === 'img' || props.mode === 'background') return props.mode
  return isSvgLike.value ? 'background' : 'img'
})

const fitClass = computed(() => (props.fit === 'contain' ? 'object-contain' : 'object-cover'))

const backgroundStyle = computed(() => {
  if (modeResolved.value !== 'background' || !imgSrc.value) return {}
  return {
    backgroundImage: `url("${imgSrc.value}")`,
    backgroundSize: props.fit === 'contain' ? 'contain' : 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat'
  } as Record<string, string>
})

const wrapperStyle = computed(() => ({
  ...sizeStyle.value,
  ...backgroundStyle.value
}))

const hadError = ref(false)
const blobUrl = ref<string | null>(null)
const finalSrc = computed(() => {
  if (hadError.value) return props.fallbackSrc || null
  // 非 http 头像必须先走鉴权拉取 blob（否则 <img> 直链会 401/400 并刷屏）
  if (avatarKind.value === 'http') return imgSrc.value
  return blobUrl.value || props.fallbackSrc || null
})

const tryLoadBlob = async () => {
  try {
    const av = props.avatar
    if (!av) return
    const s = String(av)
    const isHttp = /^https?:\/\//i.test(s)
    const isData = /^data:/i.test(s)
    if (isHttp || isData) { blobUrl.value = null; hadError.value = false; return }
    // 未登录时，不去请求受保护的 /files/*，避免控制台刷 401；直接走 fallback/slot
    const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
    if (!token) {
      blobUrl.value = null
      hadError.value = true
      return
    }
    // 受保护预览：优先根据文件ID走 /files/{id}/preview
    const id = s.startsWith('/') ? extractFileIdFromPath(s) : s
    let resp: any = null
    if (id && /^\d+$/.test(String(id))) {
      // 用 fetch 版本（会把后端 JSON 错误解析成 message，而不是 Blob）
      resp = await fileApi.getPreview(String(id))
    } else if (s.startsWith('/')) {
      // 无法提取ID时：尝试直接用 axios 拉取该相对路径（仍带鉴权头）
      resp = await api.get(s, { responseType: 'blob' })
    } else {
      // 非 http 且非路径，但也非纯数字，回退
      blobUrl.value = null
      return
    }
    if (blobUrl.value) {
      try { URL.revokeObjectURL(blobUrl.value) } catch {}
    }
    const url = URL.createObjectURL(resp as Blob)
    blobUrl.value = url
    hadError.value = false
  } catch {
    // 对于需要鉴权的头像，失败就直接走 fallback/slot，避免 <img> 重复请求刷屏
    blobUrl.value = null
    hadError.value = true
  }
}

const onImgError = () => { hadError.value = true }

onMounted(() => { tryLoadBlob() })
watch(() => props.avatar, () => { tryLoadBlob() })
onUnmounted(() => { if (blobUrl.value) { try { URL.revokeObjectURL(blobUrl.value) } catch {} } })
</script>

<style scoped></style>

