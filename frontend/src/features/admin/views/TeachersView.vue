<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.teachers')" :subtitle="t('admin.title')" />

    <card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <glass-search-input v-model="keyword" :placeholder="String(t('common.search') || '搜索教师')" size="sm" tint="secondary" />
        <div class="flex gap-2 flex-wrap">
          <glass-popover-select v-model="status" :options="statusOptions" size="sm" width="140px" />
          <button size="sm" variant="outline" :disabled="loading" @click="reload">{{ String(t('common.search') || '查询') }}</button>
        </div>
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

    <card v-else padding="md" tint="secondary" class="mt-4">
      <glass-table>
        <template #head>
          <tr>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
            <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('common.columns.teacher') || 'Teacher' }}</th>
            <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.status') || 'Status' }}</th>
            <th class="px-6 py-3 text-right text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.open') || t('common.view') || 'Open' }}</th>
          </tr>
        </template>
        <template #body>
          <tr v-for="u in items" :key="u.id" class="hover:bg-white/10 transition-colors duration-150">
            <td class="px-6 py-3 text-center font-mono text-xs">{{ u.id }}</td>
            <td class="px-6 py-3">
              <div class="font-medium">{{ u.nickname || u.username }}</div>
              <div class="text-xs text-subtle">{{ u.email }}</div>
              <div class="text-xs text-subtle" v-if="u.teacherNo">{{ u.teacherNo }}</div>
            </td>
            <td class="px-6 py-3 text-sm text-center">{{ u.status }}</td>
            <td class="px-6 py-3 text-right">
              <button size="sm" variant="outline" @click="router.push(`/admin/teachers/${u.id}`)">{{ t('common.view') || '查看' }}</button>
            </td>
          </tr>
          <tr v-if="items.length === 0">
            <td colspan="4" class="px-6 py-6">
              <empty-state :title="String(t('common.empty') || '暂无数据')" />
            </td>
          </tr>
        </template>
      </glass-table>

      <pagination-bar
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reload() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
      />
    </card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import { adminApi, type AdminUserListItem } from '@/api/admin.api'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const items = ref<AdminUserListItem[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)
const keyword = ref('')
const status = ref('')

const statusOptions = [
  { label: String(t('admin.people.allStatus') || t('common.all') || 'All'), value: '' },
  { label: String(t('admin.people.userStatus.active') || 'active'), value: 'active' },
  { label: String(t('admin.people.userStatus.disabled') || 'disabled' ), value: 'disabled' },
]

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.pageUsers({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      role: 'teacher',
      status: status.value || undefined,
    })
    items.value = res.items || []
    total.value = Number(res.total || 0)
    totalPages.value = Number(res.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

