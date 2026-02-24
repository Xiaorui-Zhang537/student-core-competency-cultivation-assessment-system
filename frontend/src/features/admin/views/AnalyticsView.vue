<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.analytics')" :subtitle="t('admin.title')" />

    <card padding="md" tint="secondary" class="mt-4">
      <div class="flex items-center gap-3 flex-wrap">
        <div class="w-48">
          <glass-popover-select v-model="days" :options="dayOptions" size="sm" />
        </div>
        <Button size="sm" variant="outline" :disabled="loading" @click="reload">{{ t('common.refresh') || '刷新' }}</Button>
      </div>
    </card>

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
      <card padding="md" tint="secondary" class="lg:col-span-3">
        <div class="text-sm font-medium mb-2">Series (last {{ days }} days)</div>
        <div class="text-xs text-subtle">该页面首期以“后端聚合序列”展示为主；后续可升级为完整 ECharts 面板。</div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-2">Active users (daily)</div>
        <pre class="text-xs whitespace-pre-wrap break-words">{{ JSON.stringify(series?.activeUsersDaily || [], null, 2) }}</pre>
      </card>
      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-2">New users (daily)</div>
        <pre class="text-xs whitespace-pre-wrap break-words">{{ JSON.stringify(series?.newUsersDaily || [], null, 2) }}</pre>
      </card>
      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-2">Enrollments (daily)</div>
        <pre class="text-xs whitespace-pre-wrap break-words">{{ JSON.stringify(series?.enrollmentsDaily || [], null, 2) }}</pre>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { adminApi } from '@/api/admin.api'

const { t } = useI18n()

const days = ref(30)
const dayOptions = [
  { label: '7 days', value: 7 },
  { label: '30 days', value: 30 },
  { label: '90 days', value: 90 },
  { label: '180 days', value: 180 },
]

const loading = ref(false)
const error = ref<string | null>(null)
const series = ref<any>(null)

async function reload() {
  loading.value = true
  error.value = null
  try {
    series.value = await adminApi.getAnalyticsSeriesOverview(Number(days.value) || 30)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

