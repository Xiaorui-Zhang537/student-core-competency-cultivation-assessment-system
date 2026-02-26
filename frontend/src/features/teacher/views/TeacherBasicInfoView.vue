<template>
  <div class="p-6">
    <div class="max-w-6xl mx-auto space-y-6">
      <div>
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <router-link to="/teacher/profile" class="hover:text-gray-700 dark:hover:text-gray-200">
            {{ t('shared.profile.title') || '个人中心' }}
          </router-link>
          <chevron-right-icon class="w-4 h-4" />
          <span>{{ t('teacher.profileBasic.title') || '教师基本信息' }}</span>
        </nav>
        <page-header :title="t('teacher.profileBasic.title') || '教师基本信息'" :subtitle="t('teacher.profileBasic.subtitle') || '基于学生详情页模板的只读信息页'">
          <template #actions>
            <Button variant="outline" @click="goEditProfile">{{ t('shared.profile.actions.edit') || '编辑资料' }}</Button>
          </template>
        </page-header>
      </div>

      <card padding="md" tint="info">
        <div class="flex items-center justify-between gap-4">
          <div class="min-w-0 flex items-center gap-4">
            <user-avatar :avatar="profile.avatar" :size="56" :rounded="true" :fit="'cover'" />
            <div class="min-w-0">
              <div class="text-2xl font-semibold truncate">{{ profile.nickname || profile.username || '-' }}</div>
              <div class="text-sm text-subtle">{{ profile.email || '-' }}</div>
              <div class="text-xs text-subtle mt-1">{{ t('shared.profile.fields.teacherNo') || '工号' }}: {{ profile.teacherNo || '-' }}</div>
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <div class="glass-badge px-3 py-2 justify-between">
              <span class="text-subtle">{{ t('shared.profile.fields.role') || '角色' }}</span>
              <span class="font-semibold">{{ profile.role || '-' }}</span>
            </div>
            <div class="glass-badge px-3 py-2 justify-between">
              <span class="text-subtle">{{ t('shared.profile.fields.email') || '邮箱' }}</span>
              <span class="font-semibold">{{ profile.emailVerified ? (t('shared.profile.status.verified') || '已验证') : (t('shared.profile.status.notVerified') || '未验证') }}</span>
            </div>
          </div>
        </div>
      </card>

      <card padding="md" tint="secondary">
        <h3 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.profileInfo') || '个人信息' }}</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.username') }}</label><p class="text-sm">{{ profile.username || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.nickname') }}</label><p class="text-sm">{{ profile.nickname || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.firstName') }}</label><p class="text-sm">{{ profile.firstName || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.lastName') }}</label><p class="text-sm">{{ profile.lastName || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.gender') }}</label><p class="text-sm">{{ profile.gender || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.mbti') || 'MBTI' }}</label><p class="text-sm">{{ profile.mbti || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.school') }}</label><p class="text-sm">{{ profile.school || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.subject') }}</label><p class="text-sm">{{ profile.subject || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.teacherNo') }}</label><p class="text-sm">{{ profile.teacherNo || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.phone') }}</label><p class="text-sm">{{ profile.phone || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.birthday') }}</label><p class="text-sm">{{ profile.birthday || '-' }}</p></div>
          <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.country') }}</label><p class="text-sm">{{ [profile.country, profile.province, profile.city].filter(Boolean).join(' / ') || '-' }}</p></div>
          <div class="md:col-span-2"><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.bio') }}</label><p class="text-sm whitespace-pre-wrap">{{ profile.bio || '-' }}</p></div>
        </div>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ChevronRightIcon } from '@heroicons/vue/24/outline'
import { userApi } from '@/api/user.api'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'

const { t } = useI18n()
const router = useRouter()

const profile = reactive<any>({
  username: '',
  nickname: '',
  email: '',
  role: '',
  emailVerified: false,
  avatar: '',
  firstName: '',
  lastName: '',
  gender: '',
  mbti: '',
  school: '',
  subject: '',
  teacherNo: '',
  phone: '',
  birthday: '',
  country: '',
  province: '',
  city: '',
  bio: '',
})

function goEditProfile() {
  router.push('/teacher/profile')
}

async function loadProfile() {
  const res: any = await userApi.getProfile()
  const data = res?.data ?? res
  Object.assign(profile, data || {})
}

onMounted(loadProfile)
</script>

