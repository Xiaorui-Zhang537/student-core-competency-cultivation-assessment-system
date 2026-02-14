<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.teachers')" :subtitle="t('admin.title')" />

    <Card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <GlassSearchInput v-model="keyword" :placeholder="String(t('common.search') || '搜索教师')" size="sm" tint="secondary" />
        <div class="flex gap-2 flex-wrap">
          <GlassPopoverSelect v-model="status" :options="statusOptions" size="sm" width="140px" />
          <Button size="sm" variant="outline" :disabled="loading" @click="reload">{{ String(t('common.search') || '查询') }}</Button>
        </div>
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

    <Card v-else padding="md" tint="secondary" class="mt-4">
      <div class="overflow-x-auto">
        <table class="table w-full">
          <thead>
            <tr>
              <th>ID</th>
              <th>Teacher</th>
              <th>Status</th>
              <th class="text-right">Open</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in items" :key="u.id">
              <td class="font-mono text-xs">{{ u.id }}</td>
              <td>
                <div class="font-medium">{{ u.nickname || u.username }}</div>
                <div class="text-xs text-subtle">{{ u.email }}</div>
                <div class="text-xs text-subtle" v-if="u.teacherNo">{{ u.teacherNo }}</div>
              </td>
              <td class="text-sm">{{ u.status }}</td>
              <td class="text-right">
                <Button size="sm" variant="outline" @click="router.push(`/admin/teachers/${u.id}`)">{{ t('common.view') || '查看' }}</Button>
              </td>
            </tr>
            <tr v-if="items.length === 0">
              <td colspan="4">
                <EmptyState :title="String(t('common.empty') || '暂无数据')" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <PaginationBar
        :page="page"
        :page-size="pageSize"
        :total-items="total"
        :total-pages="totalPages"
        :disabled="loading"
        @update:page="(v: number) => { page = v; reload() }"
        @update:pageSize="(v: number) => { pageSize = v; page = 1; reload() }"
      />
    </Card>
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
  { label: 'All status', value: '' },
  { label: 'active', value: 'active' },
  { label: 'disabled', value: 'disabled' },
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

