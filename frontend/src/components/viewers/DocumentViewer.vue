<template>
  <div class="w-full">
    <div v-glass class="rounded-xl border border-white/30 dark:border-white/10 overflow-hidden">
      <div v-if="loading" class="text-sm text-gray-500 py-6 text-center">{{ t('student.courses.loading') }}</div>
      <div v-else-if="error" class="text-sm text-red-500 py-6 text-center">{{ error }}</div>

      <!-- PDF 预览 -->
      <div v-else-if="mode==='pdf'" ref="containerRef">
        <iframe :src="pdfSrc" class="w-full h-[70vh]" referrerpolicy="no-referrer"></iframe>
      </div>

      <!-- DOCX 预览（mammoth 转 HTML） -->
      <div v-else-if="mode==='docx'" ref="containerRef" class="prose max-w-none bg-white dark:bg-gray-800 p-4 overflow-auto" style="max-height: 70vh;">
        <div v-html="docxHtml"></div>
      </div>

      <!-- 其他类型：给出下载提示 -->
      <div v-else class="text-sm text-gray-600 py-6 text-center">
        <slot name="fallback">
          {{ t('student.courses.detail.noInlinePreview') || '该文件不支持在线预览，请下载查看。' }}
        </slot>
      </div>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import apiClient, { baseURL } from '@/api/config'

interface FileLike { id: string|number; originalName?: string; fileName?: string; mimeType?: string; extension?: string }
interface Props { file: FileLike }

const props = defineProps<Props>()
const { t } = useI18n()

const loading = ref(true)
const error = ref('')
const mode = ref<'pdf' | 'docx' | 'other'>('other')
const pdfSrc = ref('')
const docxHtml = ref('')
const containerRef = ref<HTMLElement | null>(null)
const displayName = ref('')

let urlRevoke: string | null = null

function getExt(): string {
  const name = (props.file.originalName || props.file.fileName || '').toLowerCase()
  const ext = props.file.extension || name.split('.').pop() || ''
  return ext.toLowerCase()
}

async function ensureMammoth(): Promise<any> {
  // 动态加载 mammoth 浏览器版
  const key = '__mammoth_loaded__'
  if ((window as any)[key]) return (window as any).mammoth
  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = 'https://unpkg.com/mammoth/mammoth.browser.min.js'
    s.onload = () => { (window as any)[key] = (window as any).mammoth; resolve((window as any).mammoth) }
    s.onerror = () => reject(new Error('mammoth load failed'))
    document.head.appendChild(s)
  })
}

async function preview() {
  loading.value = true
  error.value = ''
  pdfSrc.value = ''
  docxHtml.value = ''
  if (urlRevoke) { URL.revokeObjectURL(urlRevoke); urlRevoke = null }

  try {
    const ext = getExt()
    displayName.value = props.file.originalName || props.file.fileName || `#${props.file.id}`
    if (ext === 'pdf') {
      // 直接使用服务端预览直链 + token 查询参数，让浏览器 PDF 工具栏读取响应头中的文件名
      const token = localStorage.getItem('token')
      const qs = token ? `?token=${encodeURIComponent(token)}` : ''
      pdfSrc.value = `${baseURL}/files/${encodeURIComponent(String(props.file.id))}/preview${qs}`
      mode.value = 'pdf'
    } else if (ext === 'docx') {
      const arrBuf: ArrayBuffer = await apiClient.get(`/files/${encodeURIComponent(String(props.file.id))}/download`, { responseType: 'arraybuffer' })
      const mammoth = await ensureMammoth()
      const result = await mammoth.convertToHtml({ arrayBuffer: arrBuf })
      docxHtml.value = result.value
      mode.value = 'docx'
    } else {
      mode.value = 'other'
    }
  } catch (e: any) {
    try {
      // 某些情况下服务端返回 JSON 错误体（被 axios 解为 Blob），尝试读取文本
      if (e?.response?.data instanceof Blob) {
        const text = await (e.response.data as Blob).text()
        error.value = text || (e?.message || '加载失败')
      } else {
        error.value = e?.message || '加载失败'
      }
    } catch {
      error.value = e?.message || '加载失败'
    }
    mode.value = 'other'
  } finally {
    loading.value = false
  }
}

onMounted(preview)
watch(() => props.file?.id, preview)
</script>

<style scoped>
.prose :deep(h1,h2,h3,h4,h5) { margin: 0.5rem 0; }
.prose :deep(p) { margin: 0.25rem 0; }
</style>


