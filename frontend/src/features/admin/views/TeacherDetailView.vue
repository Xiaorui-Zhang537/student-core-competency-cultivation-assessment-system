<template>
  <div class="p-6">
    <div class="max-w-7xl mx-auto">
      <div class="mb-8">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <router-link to="/admin/people?tab=teachers" class="hover:text-gray-700 dark:hover:text-gray-200">
            {{ t('admin.sidebar.teachers') || '教师' }}
          </router-link>
          <chevron-right-icon class="w-4 h-4" />
          <span>{{ displayName }}</span>
        </nav>
        <page-header :title="t('admin.teacherDetail.title') || '教师详情'" :subtitle="`#${id}`">
          <template #actions>
            <div class="flex items-center gap-2">
              <Button variant="outline" @click="router.push('/admin/people?tab=teachers')">{{ t('common.back') || '返回' }}</Button>
              <Button variant="outline" @click="openChatWithTeacher">{{ t('shared.chat.open') || '聊天' }}</Button>
              <Button variant="outline" @click="openAudit">{{ t('admin.student360.auditAiVoice') || 'AI/口语审计' }}</Button>
            </div>
          </template>
        </page-header>
      </div>

      <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
      <error-state
        v-else-if="error"
        class="mt-4"
        :title="String(t('common.error') || '加载失败')"
        :message="error"
        :actionText="String(t('common.retry') || '重试')"
        @action="reload"
      />

      <div v-else class="space-y-6">
        <card padding="md" tint="info">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:gap-6">
            <div class="flex items-center gap-4 min-w-0">
              <user-avatar :avatar="teacher?.avatar" :size="56" :rounded="true" :fit="'cover'" />
              <div class="min-w-0">
                <div class="flex items-center gap-3">
                  <span class="text-xl font-semibold truncate">{{ displayName }}</span>
                  <badge v-if="teacher?.mbti" size="sm" :variant="getMbtiVariant(teacher?.mbti)">MBTI · {{ teacher.mbti }}</badge>
                </div>
                <div class="text-sm text-subtle">{{ teacher?.email || '-' }}</div>
                <div class="text-xs text-subtle mt-1">{{ t('admin.people.fields.teacherNo') || '工号' }}: {{ teacher?.teacherNo || '-' }}</div>
              </div>
            </div>
          </div>
        </card>

        <card padding="md" tint="secondary">
          <h3 class="text-lg font-semibold mb-4">{{ t('shared.profile.section.profileInfo') || '个人信息' }}</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.username') }}</label><p class="text-sm">{{ teacher?.username || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.nickname') }}</label><p class="text-sm">{{ teacher?.nickname || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.firstName') }}</label><p class="text-sm">{{ teacher?.firstName || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.lastName') }}</label><p class="text-sm">{{ teacher?.lastName || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.gender') }}</label><p class="text-sm">{{ teacher?.gender || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.phone') }}</label><p class="text-sm">{{ teacher?.phone || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.school') }}</label><p class="text-sm">{{ teacher?.school || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.subject') }}</label><p class="text-sm">{{ teacher?.subject || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.birthday') }}</label><p class="text-sm">{{ teacher?.birthday || '-' }}</p></div>
            <div><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.country') }}</label><p class="text-sm">{{ [teacher?.country, teacher?.province, teacher?.city].filter(Boolean).join(' / ') || '-' }}</p></div>
            <div class="md:col-span-2"><label class="block text-sm font-medium mb-1">{{ t('shared.profile.fields.bio') }}</label><p class="text-sm whitespace-pre-wrap">{{ teacher?.bio || '-' }}</p></div>
          </div>
        </card>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <start-card :label="t('admin.teacherDetail.courses') || '课程数'" :value="summaryStats[0].value" tone="blue" :icon="BookOpenIcon" />
          <start-card :label="t('admin.teacherDetail.assignments') || '作业数'" :value="summaryStats[1].value" tone="amber" :icon="DocumentTextIcon" />
          <start-card :label="t('admin.teacherDetail.activeEnrollments') || '在读人数'" :value="summaryStats[2].value" tone="emerald" :icon="UserGroupIcon" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChevronRightIcon, BookOpenIcon, DocumentTextIcon, UserGroupIcon } from '@heroicons/vue/24/outline'
import { getMbtiVariant } from '@/shared/utils/badgeColor'
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { adminApi } from '@/api/admin.api'
import { useChatStore } from '@/stores/chat'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const chat = useChatStore()
const id = String(route.params.id || '')

const loading = ref(false)
const error = ref<string | null>(null)
const data = ref<any>(null)
const teacher = computed(() => data.value?.teacher || null)
const displayName = computed(() => teacher.value?.nickname || teacher.value?.username || `#${id}`)
const summaryStats = computed(() => ([
  { label: t('admin.teacherDetail.courses') || '课程数', value: data.value?.courses ?? 0 },
  { label: t('admin.teacherDetail.assignments') || '作业数', value: data.value?.assignments ?? 0 },
  { label: t('admin.teacherDetail.activeEnrollments') || '在读人数', value: data.value?.activeEnrollments ?? 0 },
]))

async function reload() {
  loading.value = true
  error.value = null
  try {
    data.value = await adminApi.getTeacherDetail(id)
  } catch (e: any) {
    error.value = e?.message || String(t('common.error') || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(reload)

/**
 * 打开与当前教师的一对一聊天抽屉（管理员视角）。
 */
function openChatWithTeacher() {
  const name = String(data.value?.teacher?.nickname || data.value?.teacher?.username || `#${id}`)
  const avatar = String(data.value?.teacher?.avatar || '')
  chat.openChat(id, name, null, avatar || null)
}

function openAudit() {
  const name = String(data.value?.teacher?.nickname || data.value?.teacher?.username || `#${id}`)
  router.push({ path: `/admin/audit/${id}`, query: { name } })
}
</script>

