<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.exports')" :subtitle="t('admin.title')" />

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="init"
    />

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-4">
      <card padding="md" tint="secondary" class="lg:col-span-3">
        <div class="text-sm font-medium mb-2">Export capabilities</div>
        <div class="text-xs text-subtle">Max rows: {{ caps?.maxRows ?? '-' }} · Formats: {{ (caps?.formats || []).join(', ') }}</div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-3">Users CSV</div>
        <div class="text-xs text-subtle mb-3">Export `/admin/users` list to CSV.</div>
        <div class="flex gap-2">
          <Button size="sm" variant="primary" :disabled="exporting" @click="exportUsers">Download</Button>
        </div>
      </card>

      <card padding="md" tint="secondary">
        <div class="text-sm font-medium mb-3">Ability reports CSV</div>
        <div class="text-xs text-subtle mb-3">Export `/admin/ability-reports` list to CSV.</div>
        <div class="flex gap-2">
          <Button size="sm" variant="primary" :disabled="exporting" @click="exportAbilityReports">Download</Button>
        </div>
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
import { adminApi } from '@/api/admin.api'
import { downloadCsv } from '@/utils/download'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const ui = useUIStore()

const loading = ref(false)
const error = ref<string | null>(null)
const caps = ref<any>(null)
const exporting = ref(false)

async function init() {
  loading.value = true
  error.value = null
  try {
    caps.value = await adminApi.getExportCapabilities()
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function exportUsers() {
  exporting.value = true
  try {
    const blob = await adminApi.exportUsersCsv({})
    downloadCsv(blob, 'users.csv')
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

async function exportAbilityReports() {
  exporting.value = true
  try {
    const blob = await adminApi.exportAbilityReportsCsv({})
    downloadCsv(blob, 'ability_reports.csv')
    ui.showNotification({ type: 'success', title: 'OK', message: 'Downloaded' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  } finally {
    exporting.value = false
  }
}

onMounted(init)
</script>

