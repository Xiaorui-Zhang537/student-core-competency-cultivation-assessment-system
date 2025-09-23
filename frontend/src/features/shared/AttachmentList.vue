<template>
  <component :is="noCard ? 'div' : 'div'" class="w-full">
    <Card v-if="!noCard" :hoverable="true" padding="md" class="w-full">
      <template v-if="!hideHeader" #header>
        <div class="flex items-center">
          <h3 class="text-lg font-semibold flex-1 truncate">{{ titleText }}</h3>
          <span v-if="countText" class="text-xs text-gray-500 dark:text-gray-400">{{ countText }}</span>
        </div>
      </template>
      <div v-if="!files || files.length === 0" class="text-sm text-gray-500 dark:text-gray-400">
        {{ i18nText('student.assignments.detail.noAttachments', '暂无附件') }}
      </div>
      <ul v-else class="divide-y divide-gray-200/60 dark:divide-gray-700/60">
        <li v-for="f in files" :key="String((f as any).id || (f as any).fileId)" class="py-2 flex items-center justify-between">
          <div class="min-w-0 mr-3">
            <div class="text-sm truncate">{{ fileName(f) }}</div>
            <div class="text-xs text-gray-500">{{ formatSize((f as any).fileSize) }}</div>
          </div>
          <div class="flex items-center gap-2">
            <Button v-if="showDefaultDownload" as="a" :href="`${baseURL}/files/${(f as any).id || (f as any).fileId}/download`" size="sm" variant="success" class="whitespace-nowrap">
              <template #icon>
                <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              </template>
              {{ i18nText('student.assignments.detail.download', '下载') }}
            </Button>
            <slot name="actions" :file="f" />
          </div>
        </li>
      </ul>
    </Card>
    <template v-else>
      <div v-if="!files || files.length === 0" class="text-sm text-gray-500 dark:text-gray-400">
        {{ i18nText('student.assignments.detail.noAttachments', '暂无附件') }}
      </div>
      <ul v-else class="divide-y divide-gray-200/60 dark:divide-gray-700/60">
        <li v-for="f in files" :key="String((f as any).id || (f as any).fileId)" class="py-2 flex items-center justify-between">
          <div class="min-w-0 mr-3">
            <div class="text-sm truncate">{{ fileName(f) }}</div>
            <div class="text-xs text-gray-500">{{ formatSize((f as any).fileSize) }}</div>
          </div>
          <div class="flex items-center gap-2">
            <Button v-if="showDefaultDownload" as="a" :href="`${baseURL}/files/${(f as any).id || (f as any).fileId}/download`" size="sm" variant="success" class="whitespace-nowrap">
              <template #icon>
                <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              </template>
              {{ i18nText('student.assignments.detail.download', '下载') }}
            </Button>
            <slot name="actions" :file="f" />
          </div>
        </li>
      </ul>
    </template>
  </component>
</template>

<script setup lang="ts">
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import { baseURL } from '@/api/config'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import { computed } from 'vue'

interface Props {
  files: any[]
  title?: string
  noCard?: boolean
  hideHeader?: boolean
  showDefaultDownload?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  noCard: false,
  hideHeader: false,
  showDefaultDownload: true
})
const { t } = useI18n()

function i18nText(key: string, fallback: string): string {
  try {
    const msg = (t(key) as any) as string
    return (msg && msg !== key) ? msg : fallback
  } catch {
    return fallback
  }
}

const titleText = computed(() => props.title || i18nText('student.assignments.detail.attachmentsTitle', '附件'))
const countText = computed(() => {
  const n = Array.isArray(props.files) ? props.files.length : 0
  return n > 0 ? `${n}` : ''
})

function fileName(f: any) {
  return f?.originalName || f?.fileName || `附件#${String(f?.id || f?.fileId || '')}`
}

function formatSize(bytes?: number) {
  if (!bytes || bytes <= 0) return '-'
  const units = ['B','KB','MB','GB']
  let i = 0
  let n = bytes
  while (n >= 1024 && i < units.length - 1) { n /= 1024; i++ }
  return `${n.toFixed(1)} ${units[i]}`
}
</script>


