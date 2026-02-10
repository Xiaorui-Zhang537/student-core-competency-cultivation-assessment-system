<template>
  <div class="mt-6 flex items-center justify-between gap-3">
    <div class="flex items-center gap-2">
      <span class="text-sm text-gray-700 dark:text-gray-300 whitespace-nowrap">{{ perPagePrefix }}</span>
      <GlassPopoverSelect
        :model-value="pageSize"
        :options="normalizedPageSizeOptions"
        :disabled="disabled"
        :size="selectSize"
        :width="selectWidth"
        @update:modelValue="onPageSizeChange"
      />
      <span class="text-sm text-gray-700 dark:text-gray-300 whitespace-nowrap">{{ perPageSuffix }}</span>
    </div>

    <div class="flex items-center gap-2">
      <Button
        :variant="buttonVariant"
        :size="buttonSize"
        class="whitespace-nowrap"
        :disabled="disabled || page <= 1"
        @click="emit('update:page', Math.max(1, page - 1))"
      >
        {{ prevText }}
      </Button>
      <span class="text-sm whitespace-nowrap">
        {{ pageText }}
      </span>
      <Button
        :variant="buttonVariant"
        :size="buttonSize"
        class="whitespace-nowrap"
        :disabled="disabled || page >= resolvedTotalPages"
        @click="emit('update:page', Math.min(resolvedTotalPages, page + 1))"
      >
        {{ nextText }}
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
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

const buttonSize = computed(() => props.buttonSize || 'sm')
const buttonVariant = computed(() => props.buttonVariant || 'outline')
const selectSize = computed(() => props.selectSize || 'sm')
const selectWidth = computed(() => props.selectWidth || '80px')

const perPagePrefix = computed(() => (t('teacher.assignments.pagination.perPagePrefix') as string) || '每页')
const perPageSuffix = computed(() => (t('teacher.assignments.pagination.perPageSuffix') as string) || '条')
const prevText = computed(() => (t('teacher.assignments.pagination.prev') as string) || '上一页')
const nextText = computed(() => (t('teacher.assignments.pagination.next') as string) || '下一页')
const pageText = computed(() => {
  const cur = Math.max(1, Number(props.page || 1))
  const base = (t('teacher.assignments.pagination.page', { page: cur }) as string) || `第 ${cur} 页`
  if (props.showTotalPages) return `${base} / ${resolvedTotalPages.value}`
  return base
})

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
</script>

<style scoped></style>

