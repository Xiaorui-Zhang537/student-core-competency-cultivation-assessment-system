<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.students')" :subtitle="`#${id}`">
      <template #actions>
        <Button variant="outline" @click="router.push('/admin/students')">{{ t('common.back') || '返回' }}</Button>
      </template>
    </PageHeader>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <ErrorState
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
      <Card padding="md" tint="secondary" class="lg:col-span-2">
        <div class="text-sm text-subtle mb-3">Student</div>
        <div class="space-y-1">
          <div class="font-medium">{{ data?.student?.nickname || data?.student?.username }}</div>
          <div class="text-sm text-subtle">{{ data?.student?.email }}</div>
          <div class="text-sm text-subtle" v-if="data?.student?.studentNo">No: {{ data?.student?.studentNo }}</div>
          <div class="text-sm">Status: {{ data?.student?.status }}</div>
        </div>
      </Card>
      <Card padding="md" tint="secondary">
        <div class="text-sm text-subtle mb-3">Summary</div>
        <div class="space-y-2 text-sm">
          <div>Enrolled courses: {{ data?.enrolledCourses ?? 0 }}</div>
          <div>Avg grade: {{ data?.avgGradePercentage ?? '-' }}</div>
          <div>Ability reports: {{ data?.abilityReports ?? 0 }}</div>
          <div>Last active: {{ data?.lastActiveAt ?? '-' }}</div>
        </div>
      </Card>
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

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const id = String(route.params.id || '')

const loading = ref(false)
const error = ref<string | null>(null)
const data = ref<any>(null)

async function reload() {
  loading.value = true
  error.value = null
  try {
    data.value = await adminApi.getStudentDetail(id)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

