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
import { computed, ref } from 'vue'
import { baseURL } from '@/api/config'

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
const finalSrc = computed(() => {
  if (hadError.value) return props.fallbackSrc || null
  return imgSrc.value
})

const onImgError = () => { hadError.value = true }
</script>

<style scoped></style>

