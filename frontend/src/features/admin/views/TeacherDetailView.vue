<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.teachers')" :subtitle="`#${id}`">
      <template #actions>
        <Button variant="outline" @click="router.push('/admin/people?tab=teachers')">{{ t('common.back') || '返回' }}</Button>
        <Button variant="outline" class="ml-2" @click="openChatWithTeacher">{{ t('shared.chat.open') || '聊天' }}</Button>
        <Button variant="outline" class="ml-2" @click="openAudit">{{ t('admin.student360.auditAiVoice') || 'AI/口语审计' }}</Button>
      </template>
    </PageHeader>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
      <card padding="md" tint="secondary" class="lg:col-span-2">
        <div class="text-sm text-subtle mb-3">{{ t('admin.teacherDetail.title') || t('common.columns.teacher') || '教师' }}</div>
        <div class="space-y-1">
          <div class="font-medium">{{ data?.teacher?.nickname || data?.teacher?.username }}</div>
          <div class="text-sm text-subtle">{{ data?.teacher?.email }}</div>
          <div class="text-sm text-subtle" v-if="data?.teacher?.teacherNo">
            {{ t('admin.people.fields.teacherNo') || '工号' }}: {{ data?.teacher?.teacherNo }}
          </div>
          <div class="text-sm">
            {{ t('common.columns.status') || '状态' }}: {{ data?.teacher?.status }}
          </div>
        </div>
      </card>
      <card padding="md" tint="secondary">
        <div class="text-sm text-subtle mb-3">{{ t('admin.teacherDetail.summary') || '概览' }}</div>
        <div class="space-y-2 text-sm">
          <div>{{ t('admin.teacherDetail.courses') || '课程数' }}: {{ data?.courses ?? 0 }}</div>
          <div>{{ t('admin.teacherDetail.assignments') || '作业数' }}: {{ data?.assignments ?? 0 }}</div>
          <div>{{ t('admin.teacherDetail.activeEnrollments') || '在读人数' }}: {{ data?.activeEnrollments ?? 0 }}</div>
        </div>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
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
  chat.openChat(id, name, null)
}

function openAudit() {
  const name = String(data.value?.teacher?.nickname || data.value?.teacher?.username || `#${id}`)
  router.push({ path: `/admin/audit/${id}`, query: { name } })
}
</script>

