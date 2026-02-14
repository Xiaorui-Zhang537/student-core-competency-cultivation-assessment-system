<template>
  <div class="p-6">
    <PageHeader :title="t('admin.sidebar.courses')" :subtitle="t('admin.title')" />

    <Card padding="md" tint="secondary" class="mt-4">
      <div class="flex flex-col md:flex-row gap-3 md:items-center">
        <GlassSearchInput v-model="query" :placeholder="String(t('common.search') || '搜索课程标题/教师')" size="sm" tint="secondary" />
        <div class="flex gap-2">
          <GlassPopoverSelect
            :model-value="status"
            :options="statusOptions"
            size="sm"
            width="140px"
            @update:modelValue="(v: any) => (status = v)"
          />
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
              <th>{{ t('admin.sidebar.courses') }}</th>
              <th>Teacher</th>
              <th>Status</th>
              <th>Students</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in items" :key="c.id">
              <td class="font-mono text-xs">{{ c.id }}</td>
              <td>
                <div class="font-medium">{{ c.title }}</div>
                <div class="text-xs text-subtle line-clamp-1">{{ c.description }}</div>
              </td>
              <td class="text-sm">{{ (c as any).teacherName || (c as any).teacher?.nickname || (c as any).teacher?.username || '-' }}</td>
              <td class="text-sm">{{ c.status }}</td>
              <td class="text-sm">{{ (c as any).enrollmentCount ?? (c as any).studentCount ?? '-' }}</td>
            </tr>
            <tr v-if="items.length === 0">
              <td colspan="5">
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
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import { adminApi } from '@/api/admin.api'
import type { Course } from '@/types/course'

const { t } = useI18n()

const loading = ref(false)
const error = ref<string | null>(null)
const items = ref<Course[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(1)

const query = ref<string>('')
const status = ref<string>('') // '' 表示全部

const statusOptions = [
  { label: 'All', value: '' },
  { label: 'draft', value: 'draft' },
  { label: 'published', value: 'published' },
  { label: 'archived', value: 'archived' },
]

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res: any = await adminApi.pageCourses({
      page: page.value,
      size: pageSize.value,
      query: query.value || undefined,
      status: status.value || undefined,
    })
    items.value = (res?.items || []) as Course[]
    total.value = Number(res?.total || 0)
    totalPages.value = Number(res?.totalPages || 1)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

