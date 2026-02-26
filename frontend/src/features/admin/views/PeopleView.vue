<template>
  <div class="p-6">
    <page-header :title="t('admin.sidebar.people')" :subtitle="t('admin.title')" />

    <loading-overlay v-if="kpiLoading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="kpiError"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="kpiError"
      :actionText="String(t('common.retry') || '重试')"
      @action="reloadKpis"
    />

    <div v-else class="mt-4 space-y-6">
      <admin-kpi-row :items="kpis" />
      <filter-bar tint="secondary" align="center" :dense="false" class="rounded-full h-19">
        <template #left>
          <div class="flex items-center gap-3 flex-wrap">
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.people.tabSelectorLabel') || '人员类型' }}</span>
              <segmented-pills :model-value="tab" :options="tabOptions" size="sm" variant="info" @update:modelValue="onPickTab" />
            </div>
            <div class="w-72">
              <glass-search-input
                v-model="keyword"
                :placeholder="String(t('common.search') || '搜索用户名/邮箱/学号/工号')"
                size="sm"
                tint="info"
              />
            </div>
            <div class="w-auto flex items-center gap-2">
              <span class="text-xs font-medium leading-tight text-subtle">{{ t('admin.people.statusLabel') || '状态' }}</span>
              <div class="w-40">
                <glass-popover-select v-model="status" :options="statusOptions" size="sm" tint="secondary" />
              </div>
            </div>
            <Button size="sm" variant="outline" :disabled="kpiLoading" @click="reloadKpis">
              {{ t('common.refresh') || '刷新' }}
            </Button>
          </div>
        </template>
        <template #right>
          <div class="text-xs text-subtle whitespace-nowrap">{{ t('admin.people.hint') || '同一套筛选应用到各 Tab；用户 Tab 支持创建与角色调整' }}</div>
        </template>
      </filter-bar>

      <div v-show="tab === 'users'">
        <admin-user-table
          mode="users"
          :keyword="keyword"
          :status="status"
          roleFixed=""
          :allowCreate="true"
          :allowRoleEdit="true"
        />
      </div>
      <div v-show="tab === 'students'">
        <admin-user-table
          mode="students"
          :keyword="keyword"
          :status="status"
          roleFixed="student"
          :allowCreate="false"
          :allowRoleEdit="false"
        />
      </div>
      <div v-show="tab === 'teachers'">
        <admin-user-table
          mode="teachers"
          :keyword="keyword"
          :status="status"
          roleFixed="teacher"
          :allowCreate="false"
          :allowRoleEdit="false"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import AdminKpiRow from '@/features/admin/components/AdminKpiRow.vue'
import AdminUserTable from '@/features/admin/components/AdminUserTable.vue'
import { useAdminCounts } from '@/features/admin/composables/useAdminCounts'
import { UsersIcon, UserIcon, ShieldCheckIcon } from '@heroicons/vue/24/outline'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const counts = useAdminCounts()

const keyword = ref(String((route.query as any)?.q || ''))
const status = ref(String((route.query as any)?.status || ''))
const tab = ref<'users' | 'students' | 'teachers'>((String((route.query as any)?.tab || 'users') as any) || 'users')

const statusOptions = [
  { label: String(t('admin.people.allStatus') || t('common.all') || '全部'), value: '' },
  { label: String(t('admin.people.userStatus.active') || 'active'), value: 'active' },
  { label: String(t('admin.people.userStatus.disabled') || 'disabled'), value: 'disabled' },
]

const tabOptions = computed(() => ([
  { label: t('admin.tabs.users') || '用户', value: 'users' },
  { label: t('admin.tabs.students') || '学生', value: 'students' },
  { label: t('admin.tabs.teachers') || '教师', value: 'teachers' },
]))

const kpiLoading = ref(false)
const kpiError = ref<string | null>(null)
const totals = ref<{ users: number; students: number; teachers: number; disabled: number }>({ users: 0, students: 0, teachers: 0, disabled: 0 })

const kpis = computed(() => ([
  { label: t('admin.kpi.totalUsers') || '用户总数', value: totals.value.users, tint: 'info' as const, icon: UsersIcon },
  { label: t('admin.kpi.students') || '学生', value: totals.value.students, tint: 'success' as const, icon: ShieldCheckIcon },
  { label: t('admin.kpi.teachers') || '教师', value: totals.value.teachers, tint: 'accent' as const, icon: UserIcon },
  { label: t('admin.kpi.disabledUsers') || '禁用账号', value: totals.value.disabled, tint: 'warning' as const, icon: UsersIcon },
]))

function syncQuery() {
  router.replace({
    path: '/admin/people',
    query: {
      ...route.query,
      tab: tab.value,
      q: keyword.value || undefined,
      status: status.value || undefined,
    }
  })
}

function onPickTab(v: any) {
  tab.value = String(v) as any
  syncQuery()
}

watch([keyword, status], () => syncQuery(), { deep: false })
watch(() => route.query, (q) => {
  const nextTab = String((q as any)?.tab || tab.value) as any
  if (nextTab && nextTab !== tab.value) tab.value = nextTab
}, { deep: true })

async function reloadKpis() {
  kpiLoading.value = true
  kpiError.value = null
  try {
    const [users, students, teachers, disabled] = await Promise.all([
      counts.countUsers({}),
      counts.countUsers({ role: 'student' }),
      counts.countUsers({ role: 'teacher' }),
      counts.countUsers({ status: 'disabled' }),
    ])
    totals.value = { users, students, teachers, disabled }
  } catch (e: any) {
    kpiError.value = e?.message || 'Failed to load'
  } finally {
    kpiLoading.value = false
  }
}

onMounted(reloadKpis)
</script>

