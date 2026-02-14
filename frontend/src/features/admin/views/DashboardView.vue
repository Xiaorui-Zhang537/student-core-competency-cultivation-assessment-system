<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.dashboard')" :subtitle="t('admin.title')" />

    <loading-overlay v-if="loading" :text="String(t('common.loading') || '加载中…')" />

    <ErrorState
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="fetchOverview"
    />

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
      <div class="lg:col-span-3 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        <StartCard :label="t('admin.sidebar.users') as string" :value="overview?.users?.total ?? 0" tone="blue" :icon="UsersIcon" />
        <StartCard :label="t('admin.sidebar.courses') as string" :value="overview?.courses?.total ?? 0" tone="violet" :icon="AcademicCapIcon" />
        <StartCard :label="t('admin.sidebar.community') as string" :value="overview?.community?.posts ?? 0" tone="emerald" :icon="ShieldCheckIcon" />
        <StartCard :label="t('admin.sidebar.reports') as string" :value="overview?.reports?.pending ?? 0" tone="amber" :icon="DocumentTextIcon" />
      </div>

      <Card padding="md" tint="secondary" class="lg:col-span-2">
        <div class="text-sm text-subtle mb-2">{{ t('admin.title') }}</div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="p-3 glass-thin rounded" v-glass="{ strength: 'thin', interactive: false }">
            <div class="text-xs text-subtle mb-1">Users</div>
            <div class="text-sm">
              {{ overview?.users?.students ?? 0 }} students · {{ overview?.users?.teachers ?? 0 }} teachers · {{ overview?.users?.admins ?? 0 }} admins
            </div>
          </div>
          <div class="p-3 glass-thin rounded" v-glass="{ strength: 'thin', interactive: false }">
            <div class="text-xs text-subtle mb-1">Courses</div>
            <div class="text-sm">
              {{ overview?.courses?.published ?? 0 }} published · {{ overview?.courses?.draft ?? 0 }} draft · {{ overview?.courses?.archived ?? 0 }} archived
            </div>
          </div>
          <div class="p-3 glass-thin rounded" v-glass="{ strength: 'thin', interactive: false }">
            <div class="text-xs text-subtle mb-1">Activity</div>
            <div class="text-sm">
              Active users ({{ overview?.daysActiveWindow ?? 7 }}d): {{ overview?.activity?.activeUsers ?? 0 }}
            </div>
          </div>
          <div class="p-3 glass-thin rounded" v-glass="{ strength: 'thin', interactive: false }">
            <div class="text-xs text-subtle mb-1">Reports</div>
            <div class="text-sm">
              Total: {{ overview?.reports?.total ?? 0 }} · Pending: {{ overview?.reports?.pending ?? 0 }}
            </div>
          </div>
        </div>
      </Card>

      <Card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-2">{{ t('admin.sidebar.exports') }}</div>
        <div class="text-sm text-subtle mb-3">CSV exports are available in Export Center.</div>
        <Button size="sm" variant="outline" @click="router.push('/admin/exports')">{{ t('admin.sidebar.exports') }}</Button>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import StartCard from '@/components/ui/StartCard.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import { adminApi, type AdminDashboardOverview } from '@/api/admin.api'
import { UsersIcon, AcademicCapIcon, ShieldCheckIcon, DocumentTextIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const overview = ref<AdminDashboardOverview | null>(null)

async function fetchOverview() {
  loading.value = true
  error.value = null
  try {
    overview.value = await adminApi.getDashboardOverview(7)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(fetchOverview)
</script>

