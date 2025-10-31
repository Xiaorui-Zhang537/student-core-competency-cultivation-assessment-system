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

const imgSrc = computed(() => {
  const av = props.avatar
  if (!av) return null
  const isHttp = /^https?:\/\//i.test(String(av))
  return isHttp ? String(av) : `${baseURL}/files/${encodeURIComponent(String(av))}/preview`
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
    const isHttp = /^https?:\/\//i.test(String(av))
    if (isHttp) { blobUrl.value = null; return }
    // 受保护预览：用带鉴权的 api 获取 blob，避免 <img> 无法带上 Authorization
    const resp: any = await api.get(`/files/${encodeURIComponent(String(av))}/preview`, { responseType: 'blob' })
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

