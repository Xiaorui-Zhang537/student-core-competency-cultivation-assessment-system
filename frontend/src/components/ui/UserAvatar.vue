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

const imgSrc = computed(() => {
  const av = props.avatar
  if (!av) return null
  const s = String(av)
  const isHttp = /^https?:\/\//i.test(s)
  if (isHttp) return s
  if (s.startsWith('/')) {
    // 服务器返回了相对接口路径，如 /api/files/{id}/download
    return `${baseURL}${s}`
  }
  // 视为文件ID
  return `${baseURL}/files/${encodeURIComponent(s)}/preview`
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
  return blobUrl.value || imgSrc.value
})

const tryLoadBlob = async () => {
  try {
    const av = props.avatar
    if (!av) return
    const s = String(av)
    const isHttp = /^https?:\/\//i.test(s)
    if (isHttp) { blobUrl.value = null; return }
    // 受保护预览：优先根据文件ID走 /files/{id}/preview
    const id = s.startsWith('/') ? extractFileIdFromPath(s) : s
    let resp: any = null
    if (id && /^\d+$/.test(String(id))) {
      resp = await api.get(`/files/${encodeURIComponent(String(id))}/preview`, { responseType: 'blob' })
    } else if (s.startsWith('/')) {
      // 无法提取ID时，直接请求该相对路径
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
    // 回退到直接 src（可能为公开链接）或 fallback
    blobUrl.value = null
  }
}

const onImgError = () => { hadError.value = true }

onMounted(() => { tryLoadBlob() })
watch(() => props.avatar, () => { tryLoadBlob() })
onUnmounted(() => { if (blobUrl.value) { try { URL.revokeObjectURL(blobUrl.value) } catch {} } })
</script>

<style scoped></style>

