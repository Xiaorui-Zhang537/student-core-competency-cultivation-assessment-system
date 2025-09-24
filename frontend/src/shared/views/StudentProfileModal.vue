<template>
  <GlassModal v-if="open" :title="modalTitle" maxWidth="max-w-xl" height-variant="large" :backdrop-dark="false" @close="$emit('close')">
    <div class="flex items-start gap-4">
      <UserAvatar :avatar="userAvatar || profile?.avatar" :size="80" :alt="displayName">
        <span class="text-2xl font-medium text-gray-600">{{ displayName.charAt(0) || '#' }}</span>
      </UserAvatar>
      <div class="min-w-0 flex-1">
        <div class="flex items-center gap-2 flex-wrap">
          <h3 class="text-xl font-semibold truncate">{{ displayName }}</h3>
          <Badge v-if="profile?.mbti" size="sm" :variant="mbtiVariant">MBTI: {{ profile.mbti }}</Badge>
          <Badge v-if="profile?.grade" size="sm" variant="success">{{ profile.grade }}</Badge>
        </div>
        <p v-if="profile?.bio" class="mt-1 text-sm text-gray-600 line-clamp-3">{{ profile.bio }}</p>
      </div>
    </div>

    <!-- 加载骨架 -->
    <div v-if="loading" class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3">
      <div v-for="i in 6" :key="i" class="h-4 bg-gray-300/60 rounded animate-pulse" />
    </div>

    <div v-else class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3 text-sm text-gray-700">
      <div><span class="text-gray-500">ID：</span>{{ userId }}</div>
      <div v-if="profile?.username"><span class="text-gray-500">用户名：</span>{{ profile.username }}</div>
      <div v-if="profile?.nickname"><span class="text-gray-500">昵称：</span>{{ profile.nickname }}</div>
      <div v-if="profile?.email">
        <span class="text-gray-500">{{ t('student.courses.detail.email') || '邮箱' }}：</span>{{ profile.email }}
        <Badge v-if="profile?.emailVerified" size="sm" variant="success" class="ml-2">{{ t('common.verified') || '已验证' }}</Badge>
      </div>
      <div v-if="profile?.phone"><span class="text-gray-500">{{ t('student.courses.detail.phone') || '电话' }}：</span>{{ profile.phone }}</div>
      <div v-if="profile?.school"><span class="text-gray-500">{{ t('student.courses.detail.school') || '学校' }}：</span>{{ profile.school }}</div>
      <div v-if="profile?.subject"><span class="text-gray-500">{{ t('student.courses.detail.subject') || '学科' }}：</span>{{ profile.subject }}</div>
      <div v-if="locationText"><span class="text-gray-500">{{ t('student.courses.detail.location') || '地点' }}：</span>{{ locationText }}</div>
      <div v-if="profile?.studentNo || profile?.teacherNo"><span class="text-gray-500">{{ t('student.courses.detail.code') || '编号' }}：</span>{{ profile.studentNo || profile.teacherNo }}</div>
      <div v-if="birthdayText"><span class="text-gray-500">{{ t('student.courses.detail.birthday') || '生日' }}：</span>{{ birthdayText }}</div>
      <div v-if="profile?.bio" class="md:col-span-2"><span class="text-gray-500">简介：</span>{{ profile.bio }}</div>
      <div v-if="!profile || Object.keys(profile).length === 0" class="col-span-2 text-gray-500">{{ t('common.empty') || '无内容' }}</div>
    </div>

    <template #footer>
      <Button variant="outline" @click="$emit('close')">{{ t('common.close') || '关闭' }}</Button>
      <Button variant="primary" :disabled="!userId" @click="contact">{{ t('student.courses.detail.contact') || '联系TA' }}</Button>
    </template>
  </GlassModal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import GlassModal from '@/components/ui/GlassModal.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { userApi } from '@/api/user.api'
import { getMbtiVariant } from '@/shared/utils/badgeColor'

interface Props {
  open: boolean
  userId?: string | number | null
  userName?: string | null
  userAvatar?: string | null
  courseId?: string | number | null
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userId: null,
  userName: null,
  userAvatar: null,
  courseId: null
})

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'contact', payload: { userId: string | number, userName: string | null, courseId: string | number | null }): void
}>()

const { t, locale } = useI18n()

type AnyProfile = Record<string, any>
const profile = ref<AnyProfile | null>(null)
const loading = ref(false)
const cache = new Map<string, AnyProfile>()

const displayName = computed(() => {
  const p = (profile.value || {}) as AnyProfile
  const lastFirst = [p.lastName, p.firstName].filter(Boolean).join('')
  return String(props.userName || p.nickname || lastFirst || p.username || p.name || 'User')
})
const locationText = computed(() => {
  const p = profile.value || ({} as AnyProfile)
  const arr = [p.country, p.province, p.city].filter(Boolean)
  return arr.join(' / ')
})
const birthdayText = computed(() => {
  const b = (profile.value as any)?.birthday
  if (!b) return ''
  try {
    const d = new Date(b)
    const lang = String(locale.value || 'zh-CN').toLowerCase()
    const isZh = lang.startsWith('zh')
    if (isZh) {
      const y = d.getFullYear(); const m = d.getMonth()+1; const day = d.getDate()
      return `${y}年${m}月${day}日`
    }
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch { return String(b) }
})
const modalTitle = computed(() => t('student.courses.detail.viewProfile') || '查看资料')

const mbtiVariant = computed(() => getMbtiVariant((profile.value as any)?.mbti))

watch(() => props.open, async (v) => {
  if (!v || !props.userId) return
  const key = String(props.userId)
  // 先用最小信息占位，避免空白
  profile.value = {
    id: props.userId,
    username: props.userName,
    avatar: props.userAvatar
  } as AnyProfile
  try { (window as any).__lastProfile = { stage: 'placeholder', profile: profile.value } } catch {}
  if (cache.has(key)) {
    profile.value = cache.get(key) as AnyProfile
    try { (window as any).__lastProfile = { stage: 'cache', profile: profile.value } } catch {}
    return
  }
  loading.value = true
  try {
    const res: any = await userApi.getProfileById(String(props.userId))
    // 兼容 ApiResponse 包装：优先取 data.data，其次 data，最后原始对象
    const data = (res?.data?.data) ?? (res?.data) ?? res ?? null
    if (data) {
      profile.value = data as AnyProfile
      cache.set(key, data as AnyProfile)
      try { (window as any).__lastProfile = profile.value } catch {}
    } else {
      profile.value = (profile.value || {})
    }
  } catch {
    // 保持占位信息，避免空白
    profile.value = (profile.value || {})
    try { (window as any).__lastProfile = { error: 'fetch_failed', profile: profile.value } } catch {}
  } finally {
    loading.value = false
  }
}, { immediate: true })

function contact() {
  if (!props.userId) return
  emit('contact', { userId: props.userId, userName: props.userName || null, courseId: props.courseId || null })
}
</script>

<style scoped>
</style>


