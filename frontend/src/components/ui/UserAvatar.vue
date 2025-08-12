<template>
  <div :class="wrapperClass" :style="sizeStyle">
    <img v-if="imgSrc" :src="imgSrc" :alt="alt || 'avatar'" class="w-full h-full object-cover" />
    <slot v-else></slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { baseURL } from '@/api/config'

const props = withDefaults(defineProps<{
  avatar?: string | null
  size?: number | string
  alt?: string
  rounded?: boolean
}>(), {
  size: 32,
  rounded: true
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
</script>

<style scoped></style>

