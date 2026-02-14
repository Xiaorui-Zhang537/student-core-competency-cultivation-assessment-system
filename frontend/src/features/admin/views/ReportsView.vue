<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.reports')" :subtitle="t('admin.title')" />

    <Card padding="md" tint="secondary" class="mt-4">
      <div class="flex items-center justify-between flex-wrap gap-3">
        <SegmentedPills :model-value="tab" :options="tabOptions" size="sm" variant="primary" @update:modelValue="(v:any)=> { tab = String(v); onTab() }" />
        <Button size="sm" variant="outline" :disabled="loading" @click="reload">{{ t('common.refresh') || '刷新' }}</Button>
      </div>
    </Card>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <ErrorState
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <!-- Ability Reports -->
    <div v-else-if="tab === 'ability'" class="mt-4 space-y-4">
      <Card padding="md" tint="secondary">
        <div class="flex flex-col md:flex-row gap-3 md:items-center">
          <GlassInput v-model="abilityStudentId" type="number" :placeholder="'studentId(optional)'" />
          <GlassPopoverSelect v-model="abilityReportType" :options="reportTypeOptions" size="sm" width="180px" />
          <Button size="sm" variant="outline" :disabled="loading" @click="reloadAbility">{{ t('common.search') || '查询' }}</Button>
        </div>
      </Card>

      <Card padding="md" tint="secondary">
        <div class="overflow-x-auto">
          <table class="table w-full">
            <thead>
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>Type</th>
                <th>Score</th>
                <th>Created</th>
                <th class="text-right">Open</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in abilityItems" :key="r.id">
                <td class="font-mono text-xs">{{ r.id }}</td>
                <td>
                  <div class="font-medium">{{ r.studentName || '-' }}</div>
                  <div class="text-xs text-subtle">#{{ r.studentId }} · {{ r.studentNumber || '-' }}</div>
                </td>
                <td class="text-sm">{{ r.reportType }}</td>
                <td class="text-sm">{{ r.overallScore ?? '-' }}</td>
                <td class="text-xs text-subtle">{{ r.createdAt || '-' }}</td>
                <td class="text-right">
                  <Button size="sm" variant="outline" @click="openAbility(r.id)">{{ t('common.view') || '查看' }}</Button>
                </td>
              </tr>
              <tr v-if="abilityItems.length === 0">
                <td colspan="6">
                  <EmptyState :title="String(t('common.empty') || '暂无数据')" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <PaginationBar
          :page="abilityPage"
          :page-size="abilityPageSize"
          :total-items="abilityTotal"
          :total-pages="abilityTotalPages"
          :disabled="loading"
          @update:page="(v: number) => { abilityPage = v; reloadAbility() }"
          @update:pageSize="(v: number) => { abilityPageSize = v; abilityPage = 1; reloadAbility() }"
        />
      </Card>
    </div>

    <!-- Abuse Reports -->
    <div v-else class="mt-4 space-y-4">
      <Card padding="md" tint="secondary">
        <div class="flex flex-col md:flex-row gap-3 md:items-center">
          <GlassPopoverSelect v-model="abuseStatus" :options="abuseStatusOptions" size="sm" width="180px" />
          <Button size="sm" variant="outline" :disabled="loading" @click="reloadAbuse">{{ t('common.search') || '查询' }}</Button>
        </div>
      </Card>

      <Card padding="md" tint="secondary">
        <div class="overflow-x-auto">
          <table class="table w-full">
            <thead>
              <tr>
                <th>ID</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in abuseItems" :key="r.id">
                <td class="font-mono text-xs">{{ r.id }}</td>
                <td>
                  <div class="font-medium">{{ r.reason }}</div>
                  <div class="text-xs text-subtle line-clamp-1">{{ r.details || '' }}</div>
                </td>
                <td class="w-48">
                  <GlassPopoverSelect
                    :model-value="r.status"
                    :options="abuseStatusEditOptions"
                    size="sm"
                    @update:modelValue="(v:any) => updateAbuseStatus(r, v)"
                  />
                </td>
                <td class="text-xs text-subtle">{{ r.createdAt || '-' }}</td>
              </tr>
              <tr v-if="abuseItems.length === 0">
                <td colspan="4">
                  <EmptyState :title="String(t('common.empty') || '暂无数据')" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <PaginationBar
          :page="abusePage"
          :page-size="abusePageSize"
          :total-items="abuseTotal"
          :total-pages="abuseTotalPages"
          :disabled="loading"
          @update:page="(v: number) => { abusePage = v; reloadAbuse() }"
          @update:pageSize="(v: number) => { abusePageSize = v; abusePage = 1; reloadAbuse() }"
        />
      </Card>
    </div>

    <GlassModal v-if="showAbilityDetail" :title="String(t('admin.sidebar.reports') || '报告')" size="lg" heightVariant="tall" @close="closeAbility">
      <div v-if="abilityDetail" class="space-y-3">
        <div class="text-sm text-subtle">#{{ abilityDetail.id }} · {{ abilityDetail.reportType }}</div>
        <Card padding="md" tint="secondary">
          <div class="text-sm font-medium mb-2">{{ abilityDetail.title }}</div>
          <div class="text-sm text-subtle">Student: {{ abilityDetail.studentName }} (#{{ abilityDetail.studentId }})</div>
          <div class="text-sm">Score: {{ abilityDetail.overallScore }}</div>
        </Card>
        <Card padding="md" tint="secondary">
          <div class="text-sm font-medium mb-2">Recommendations</div>
          <div class="text-sm whitespace-pre-line">{{ abilityDetail.recommendations || '-' }}</div>
        </Card>
        <Card padding="md" tint="secondary">
          <div class="text-sm font-medium mb-2">DimensionScores(JSON)</div>
          <pre class="text-xs whitespace-pre-wrap break-words opacity-90">{{ abilityDetail.dimensionScores || '{}' }}</pre>
        </Card>
      </div>
    </GlassModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { adminApi, type AbilityReport, type ReportEntity } from '@/api/admin.api'
import { useUIStore } from '@/stores/ui'

const { t } = useI18n()
const ui = useUIStore()

const loading = ref(false)
const error = ref<string | null>(null)

const tab = ref<'ability' | 'abuse'>('ability')
const tabOptions = [
  { label: '能力报告', value: 'ability' },
  { label: '举报', value: 'abuse' },
]

// Ability reports state
const abilityItems = ref<AbilityReport[]>([])
const abilityPage = ref(1)
const abilityPageSize = ref(20)
const abilityTotal = ref(0)
const abilityTotalPages = ref(1)
const abilityStudentId = ref<string>('')
const abilityReportType = ref<string>('') // '' all
const reportTypeOptions = [
  { label: 'All', value: '' },
  { label: 'ai', value: 'ai' },
  { label: 'monthly', value: 'monthly' },
  { label: 'semester', value: 'semester' },
  { label: 'annual', value: 'annual' },
  { label: 'custom', value: 'custom' },
]

// Abuse reports state
const abuseItems = ref<ReportEntity[]>([])
const abusePage = ref(1)
const abusePageSize = ref(20)
const abuseTotal = ref(0)
const abuseTotalPages = ref(1)
const abuseStatus = ref<string>('')
const abuseStatusOptions = [
  { label: 'All', value: '' },
  { label: 'pending', value: 'pending' },
  { label: 'in_review', value: 'in_review' },
  { label: 'resolved', value: 'resolved' },
  { label: 'rejected', value: 'rejected' },
]
const abuseStatusEditOptions = abuseStatusOptions.filter(o => o.value !== '')

// Ability detail modal
const showAbilityDetail = ref(false)
const abilityDetail = ref<AbilityReport | null>(null)

function onTab() {
  error.value = null
  if (tab.value === 'ability') reloadAbility()
  else reloadAbuse()
}

async function reload() {
  if (tab.value === 'ability') return reloadAbility()
  return reloadAbuse()
}

async function reloadAbility() {
  loading.value = true
  error.value = null
  try {
    const sid = String(abilityStudentId.value || '').trim()
    const res = await adminApi.pageAbilityReports({
      page: abilityPage.value,
      size: abilityPageSize.value,
      studentId: sid ? Number(sid) : undefined,
      reportType: abilityReportType.value || undefined,
    })
    abilityItems.value = res.items || []
    abilityTotal.value = Number(res.total || 0)
    abilityTotalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function openAbility(id: number) {
  showAbilityDetail.value = true
  abilityDetail.value = null
  try {
    abilityDetail.value = await adminApi.getAbilityReport(id)
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}
function closeAbility() {
  showAbilityDetail.value = false
  abilityDetail.value = null
}

async function reloadAbuse() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.pageReports({
      status: abuseStatus.value || undefined,
      page: abusePage.value,
      size: abusePageSize.value,
    })
    abuseItems.value = res.items || []
    abuseTotal.value = Number(res.total || 0)
    abuseTotalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

async function updateAbuseStatus(r: ReportEntity, v: string) {
  try {
    await adminApi.updateReportStatus(r.id, String(v))
    r.status = String(v)
    ui.showNotification({ type: 'success', title: 'OK', message: 'Status updated' })
  } catch (e: any) {
    ui.showNotification({ type: 'error', title: 'Error', message: e?.message || 'Failed' })
  }
}

onMounted(reloadAbility)
</script>

