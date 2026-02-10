<template>
  <div class="mt-6 flex flex-wrap items-center justify-between gap-3 text-sm">
    <!-- 左侧：总条数 + 每页选择 -->
    <div class="flex items-center gap-2 text-gray-500 dark:text-gray-400">
      <template v-if="showTotal">
        <span class="whitespace-nowrap">{{ totalLabel }}</span>
        <span class="mx-1 text-gray-300 dark:text-gray-600">|</span>
      </template>
      <span class="whitespace-nowrap">{{ perPagePrefix }}</span>
      <GlassPopoverSelect
        :model-value="pageSize"
        :options="normalizedPageSizeOptions"
        :disabled="disabled"
        :size="selectSize"
        :width="selectWidth"
        @update:modelValue="onPageSizeChange"
      />
      <span class="whitespace-nowrap">{{ perPageSuffix }}</span>
    </div>

    <!-- 右侧：页码导航 -->
    <div class="flex items-center gap-1">
      <!-- 首页 -->
      <button
        class="pg-btn"
        :disabled="disabled || page <= 1"
        :title="firstText"
        @click="goPage(1)"
      >
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="11 17 6 12 11 7"/><polyline points="18 17 13 12 18 7"/></svg>
      </button>
      <!-- 上一页 -->
      <button
        class="pg-btn"
        :disabled="disabled || page <= 1"
        :title="prevText"
        @click="goPage(page - 1)"
      >
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
      </button>

      <!-- 页码指示器 -->
      <div class="flex items-center gap-1 px-2">
        <span class="pg-current">{{ page }}</span>
        <span class="text-gray-400 dark:text-gray-500 text-xs">/</span>
        <span class="text-gray-500 dark:text-gray-400 text-xs">{{ resolvedTotalPages }}</span>
      </div>

      <!-- 下一页 -->
      <button
        class="pg-btn"
        :disabled="disabled || page >= resolvedTotalPages"
        :title="nextText"
        @click="goPage(page + 1)"
      >
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
      <!-- 末页 -->
      <button
        class="pg-btn"
        :disabled="disabled || page >= resolvedTotalPages"
        :title="lastText"
        @click="goPage(resolvedTotalPages)"
      >
        <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="13 17 18 12 13 7"/><polyline points="6 17 11 12 6 7"/></svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'

const props = defineProps<{
  page: number
  pageSize: number
  totalItems?: number
  totalPages?: number
  disabled?: boolean
  pageSizeOptions?: Array<number | { label: string; value: number }>
  buttonSize?: 'xs' | 'sm' | 'md'
  buttonVariant?: string
  selectSize?: 'xs' | 'sm' | 'md'
  selectWidth?: string
  showTotalPages?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:page', v: number): void
  (e: 'update:pageSize', v: number): void
}>()

const { t } = useI18n()

const disabled = computed(() => !!props.disabled)

const resolvedTotalPages = computed(() => {
  if (Number(props.totalPages || 0) > 0) return Math.max(1, Number(props.totalPages || 1))
  const total = Math.max(0, Number(props.totalItems || 0))
  const size = Math.max(1, Number(props.pageSize || 1))
  return Math.max(1, Math.ceil(total / size))
})

const showTotal = computed(() => Number(props.totalItems || 0) > 0)
const totalLabel = computed(() => {
  const n = Number(props.totalItems || 0)
  return (t('pagination.total', { n }) as string) || `共 ${n} 条`
})

const selectSize = computed(() => props.selectSize || 'sm')
const selectWidth = computed(() => props.selectWidth || '72px')

const perPagePrefix = computed(() => (t('teacher.assignments.pagination.perPagePrefix') as string) || '每页')
const perPageSuffix = computed(() => (t('teacher.assignments.pagination.perPageSuffix') as string) || '条')
const prevText = computed(() => (t('teacher.assignments.pagination.prev') as string) || '上一页')
const nextText = computed(() => (t('teacher.assignments.pagination.next') as string) || '下一页')
const firstText = computed(() => (t('pagination.first') as string) || '首页')
const lastText = computed(() => (t('pagination.last') as string) || '末页')

const normalizedPageSizeOptions = computed(() => {
  const raw = props.pageSizeOptions && props.pageSizeOptions.length ? props.pageSizeOptions : [10, 20, 50]
  return raw.map((it: any) => {
    if (typeof it === 'number') return { label: String(it), value: it }
    const v = Number(it?.value || 10)
    return { label: String(it?.label || v), value: v }
  })
})

function onPageSizeChange(v: any) {
  const n = Math.max(1, Math.floor(Number(v || 10)))
  emit('update:pageSize', n)
}

function goPage(p: number) {
  const clamped = Math.max(1, Math.min(resolvedTotalPages.value, p))
  if (clamped !== props.page) emit('update:page', clamped)
}
</script>

<style scoped>
/* 页码导航按钮 */
.pg-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 0.5rem;
  border: none;
  background: transparent;
  color: var(--color-base-content, #666);
  opacity: 0.6;
  cursor: pointer;
  transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) {
  opacity: 1;
  background: color-mix(in oklab, var(--color-base-200) 50%, transparent);
}
.pg-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

/* 当前页码高亮 */
.pg-current {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 1.5rem;
  height: 1.5rem;
  border-radius: 0.375rem;
  font-size: 0.8125rem;
  font-weight: 700;
  color: white;
  background: color-mix(in oklab, var(--color-primary, #3b82f6) 85%, transparent);
  padding: 0 0.375rem;
}
</style>
