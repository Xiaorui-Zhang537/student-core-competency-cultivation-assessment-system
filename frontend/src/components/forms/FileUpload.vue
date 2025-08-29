<template>
  <div
    class="file-upload-container"
    :class="dense ? 'inline-block align-middle' : ''"
    :style="dense ? { width: 'auto' } : {}"
  >
    <!-- 拖拽上传区域 -->
    <div
      ref="dropZoneRef"
      v-glass="{ strength: 'regular', interactive: true }"
      :class="[
        'rounded-xl transition-all duration-300 cursor-pointer glass-regular glass-interactive',
        dense ? 'p-2' : (compact ? 'p-3' : 'p-6'),
        dense ? 'inline-block w-[280px] sm:w-[300px] md:w-[340px] lg:w-[380px]' : '',
        {
          'ring-1 ring-primary-300': isDragOver,
          'ring-1 ring-gray-300/50': !isDragOver && !error,
          'ring-1 ring-red-400': error
        }
      ]"
      role="button"
      tabindex="0"
      @click="triggerFileSelect"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @keydown.enter.prevent="triggerFileSelect"
      @keydown.space.prevent="triggerFileSelect"
    >
      <div class="text-center flex flex-col items-center justify-center h-full" :style="dense ? { aspectRatio: '16 / 10' } : {}">
        <!-- 上传图标 -->
        <div class="mb-3">
          <CloudArrowUpIcon 
            v-if="!uploading"
            :class="['mx-auto text-gray-400', dense ? 'h-6 w-6' : (compact ? 'h-8 w-8' : 'h-12 w-12')]"
          />
          <div 
            v-else
            class="mx-auto h-12 w-12 flex items-center justify-center"
          >
            <div class="w-8 h-8 border-2 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        </div>

        <!-- 上传文本 -->
        <div :class="dense ? 'mb-2' : 'mb-4'">
          <p :class="[dense ? 'text-sm' : (compact ? 'text-base' : 'text-lg'), 'font-medium text-gray-900 dark:text-white mb-1']">
            {{ uploading ? t('shared.upload.uploading') : t('shared.upload.dragTip') }}
          </p>
          <p v-if="!dense" :class="[compact ? 'text-xs' : 'text-sm', 'text-gray-600 dark:text-gray-400']">
            {{ acceptText || t('shared.upload.acceptHint', { accept: accept || '*' }) }}
          </p>
          <p v-if="maxSize && !dense" class="text-xs text-gray-500 dark:text-gray-400 mt-1">
            {{ t('shared.upload.maxSize', { size: formatFileSize(maxSize) }) }}
          </p>
        </div>

        <!-- 错误信息 -->
        <div v-if="error" class="mb-4">
          <div class="flex items-center justify-center text-red-600 dark:text-red-400">
            <exclamation-triangle-icon class="w-5 h-5 mr-2" />
            <span class="text-sm">{{ error }}</span>
          </div>
        </div>

        <!-- 上传按钮 -->
        <Button 
          v-if="!uploading"
          variant="primary"
          :size="dense ? 'sm' : (compact ? 'sm' : 'md')"
          @click.stop="triggerFileSelect"
          type="button"
          :disabled="disabled"
        >
          <PhotoIcon :class="[dense ? 'w-3 h-3' : (compact ? 'w-3 h-3' : 'w-4 h-4'), 'mr-2']" />
          {{ t('shared.upload.select') }}
        </Button>

        <!-- 上传进度 -->
        <div v-if="uploading && progress > 0" class="mt-4">
          <div class="flex items-center justify-between mb-1">
            <span class="text-sm text-gray-600 dark:text-gray-400">{{ t('shared.upload.progress') }}</span>
            <span class="text-sm font-medium text-gray-900 dark:text-white">{{ progress }}%</span>
          </div>
          <Progress :value="progress" color="primary" size="sm" />
        </div>
      </div>
    </div>

    <!-- 文件输入框 -->
    <input
      ref="fileInputRef"
      type="file"
      :accept="accept"
      :multiple="multiple"
      class="hidden"
      @change="handleFileSelect"
    />

    <!-- 文件列表 -->
    <div v-if="files.length > 0" class="mt-4 space-y-2">
      <h4 class="text-sm font-medium text-gray-900 dark:text-white mb-2">
        {{ t('shared.upload.selectedCount', { count: files.length }) }}
      </h4>
      
      <div class="space-y-2 max-h-48 overflow-y-auto">
        <div
          v-for="(file, index) in files"
          :key="index"
          v-glass="{ strength: 'ultraThin', interactive: false }"
          class="flex items-center justify-between p-3 rounded-lg glass-ultraThin"
        >
          <div class="flex items-center space-x-3 flex-1 min-w-0">
            <!-- 文件图标 -->
            <div class="flex-shrink-0">
              <DocumentIcon 
                v-if="!isImage(file)"
                class="w-8 h-8 text-gray-400"
              />
              <PhotoIcon 
                v-else
                class="w-8 h-8 text-blue-500"
              />
            </div>
            
            <!-- 文件信息 -->
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 dark:text-white truncate">
                {{ file.name }}
              </p>
              <p class="text-xs text-gray-600 dark:text-gray-400">
                {{ formatFileSize(file.size) }}
                <span v-if="file.lastModified" class="ml-2">
                  {{ formatDate(file.lastModified) }}
                </span>
              </p>
            </div>
            
            <!-- 上传状态 -->
            <div class="flex-shrink-0">
              <CheckCircleIcon 
                v-if="file.uploaded"
                class="w-5 h-5 text-green-500"
              />
              <div 
                v-else-if="file.uploading"
                class="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"
              ></div>
              <ExclamationCircleIcon 
                v-else-if="file.error"
                class="w-5 h-5 text-red-500"
                :title="file.error"
              />
            </div>
          </div>
          
          <!-- 删除按钮 -->
          <Button
            v-if="!disabled"
            @click="removeFile(index)"
            class="ml-3 p-1.5 rounded hover:bg-red-50 dark:hover:bg-red-900/20 text-gray-400 hover:text-red-600 transition-colors"
            type="button"
            :title="t('shared.upload.removeWithName', { name: file.name })"
          >
            <XMarkIcon class="w-4 h-4" />
          </Button>
        </div>
      </div>
    </div>

    <!-- 图片预览 -->
    <div v-if="showPreview && imageFiles.length > 0" class="mt-4">
      <h4 class="text-sm font-medium text-gray-900 dark:text-white mb-2">{{ t('shared.upload.imagePreview') }}</h4>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
        <div
          v-for="(file, index) in imageFiles"
          :key="index"
          class="relative group"
        >
          <img
            :src="file.preview"
            :alt="file.name"
            class="w-full h-24 object-cover rounded-lg border border-gray-200 dark:border-gray-700"
          />
          <Button
            v-if="!disabled"
            @click="removeFile(files.indexOf(file))"
            class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
            type="button"
          >
            <XMarkIcon class="w-3 h-3" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Button from '@/components/ui/Button.vue'
import Progress from '@/components/ui/Progress.vue'
import { useLocale } from '@/i18n/useLocale'
import {
  CloudArrowUpIcon,
  PhotoIcon,
  DocumentIcon,
  XMarkIcon,
  CheckCircleIcon,
  ExclamationCircleIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

// Props
interface FileWithMeta extends File {
  uploaded?: boolean
  uploading?: boolean
  error?: string
  preview?: string
}

interface Props {
  accept?: string
  acceptText?: string
  multiple?: boolean
  maxSize?: number
  maxFiles?: number
  disabled?: boolean
  showPreview?: boolean
  autoUpload?: boolean
  uploadUrl?: string
  uploadHeaders?: Record<string, string>
  uploadData?: Record<string, any>
  compact?: boolean
  dense?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  accept: '*',
  multiple: false,
  maxSize: 50 * 1024 * 1024, // 50MB
  maxFiles: 10,
  disabled: false,
  showPreview: true,
  autoUpload: false,
  compact: false,
  dense: false
})

// Emits
const emit = defineEmits<{
  'update:files': [files: File[]]
  'file-added': [file: File]
  'file-removed': [file: File, index: number]
  'upload-progress': [progress: number, file: File]
  'upload-success': [response: any, file: File]
  'upload-error': [error: string, file: File]
  'files-selected': [files: File[]]
}>()

// 状态
const dropZoneRef = ref<HTMLElement>()
const fileInputRef = ref<HTMLInputElement>()
const files = ref<FileWithMeta[]>([])
const isDragOver = ref(false)
const uploading = ref(false)
const progress = ref(0)
const error = ref('')
const { t } = useLocale()

// 计算属性
const imageFiles = computed(() => 
  files.value.filter(file => file.preview)
)

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (timestamp: number): string => {
  return new Date(timestamp).toLocaleDateString()
}

// 检查是否为图片
const isImage = (file: File): boolean => {
  return file.type.startsWith('image/')
}

// 验证文件
const validateFile = (file: File): string | null => {
  // 检查文件大小
  if (props.maxSize && file.size > props.maxSize) {
    return t('shared.upload.sizeExceeded', { size: formatFileSize(props.maxSize) }) as string
  }

  // 检查文件类型
  if (props.accept && props.accept !== '*') {
    const acceptTypes = props.accept.split(',').map(type => type.trim())
    const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase()
    const mimeType = file.type

    const isValidType = acceptTypes.some(type => {
      if (type.startsWith('.')) {
        return fileExtension === type.toLowerCase()
      }
      if (type.includes('/*')) {
        return mimeType.startsWith(type.replace('/*', ''))
      }
      return mimeType === type
    })

    if (!isValidType) {
      return t('shared.upload.unsupportedType', { type: file.type || fileExtension }) as string
    }
  }

  return null
}

// 创建图片预览
const createImagePreview = (file: File): Promise<string> => {
  return new Promise((resolve) => {
    if (!isImage(file)) {
      resolve('')
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      resolve(e.target?.result as string)
    }
    reader.readAsDataURL(file)
  })
}

// 添加文件
const addFiles = async (newFiles: FileList | File[]) => {
  error.value = ''
  const filesToAdd: FileWithMeta[] = []

  for (const file of Array.from(newFiles)) {
    // 检查文件数量限制
    if (props.maxFiles && files.value.length + filesToAdd.length >= props.maxFiles) {
      error.value = t('shared.upload.maxFilesExceeded', { count: props.maxFiles }) as string
      break
    }

    // 验证文件
    const validationError = validateFile(file)
    if (validationError) {
      error.value = validationError
      continue
    }

    // 检查重复文件
    const isDuplicate = files.value.some(existingFile => 
      existingFile.name === file.name && existingFile.size === file.size
    )
    if (isDuplicate) {
      error.value = t('shared.upload.duplicate', { name: file.name }) as string
      continue
    }

    const fileWithMeta: FileWithMeta = file as FileWithMeta
    
    // 创建图片预览
    if (props.showPreview && isImage(file)) {
      fileWithMeta.preview = await createImagePreview(file)
    }

    filesToAdd.push(fileWithMeta)
    emit('file-added', file)
  }

  files.value.push(...filesToAdd)
  emit('update:files', files.value)
  emit('files-selected', filesToAdd)

  // 自动上传
  if (props.autoUpload && props.uploadUrl) {
    await uploadFiles(filesToAdd)
  }
}

// 移除文件
const removeFile = (index: number) => {
  const file = files.value[index]
  files.value.splice(index, 1)
  emit('update:files', files.value)
  emit('file-removed', file, index)
  
  // 清空错误信息
  if (files.value.length === 0) {
    error.value = ''
  }
}

// 清空所有文件
const clearFiles = () => {
  files.value = []
  error.value = ''
  emit('update:files', [])
}

// 触发文件选择
const triggerFileSelect = () => {
  if (!props.disabled) {
    fileInputRef.value?.click()
  }
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    addFiles(target.files)
    target.value = '' // 清空输入框
  }
}

// 拖拽事件处理
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (!props.disabled) {
    isDragOver.value = true
  }
}

const handleDragEnter = (event: DragEvent) => {
  event.preventDefault()
  if (!props.disabled) {
    isDragOver.value = true
  }
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  // 只有当离开整个拖拽区域时才设置为false
  const rect = dropZoneRef.value?.getBoundingClientRect()
  if (rect) {
    const { clientX, clientY } = event
    if (
      clientX < rect.left ||
      clientX > rect.right ||
      clientY < rect.top ||
      clientY > rect.bottom
    ) {
      isDragOver.value = false
    }
  }
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  isDragOver.value = false
  
  if (!props.disabled && event.dataTransfer?.files) {
    addFiles(event.dataTransfer.files)
  }
}

// 上传文件
const uploadFiles = async (filesToUpload: FileWithMeta[] = files.value) => {
  if (!props.uploadUrl) return

  uploading.value = true

  for (const file of filesToUpload) {
    try {
      file.uploading = true
      file.error = undefined

      const formData = new FormData()
      formData.append('file', file)
      
      // 添加额外数据
      if (props.uploadData) {
        Object.entries(props.uploadData).forEach(([key, value]) => {
          formData.append(key, value)
        })
      }

      const xhr = new XMLHttpRequest()
      
      // 上传进度
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const fileProgress = Math.round((event.loaded / event.total) * 100)
          progress.value = fileProgress
          emit('upload-progress', fileProgress, file)
        }
      })

      const response = await new Promise((resolve, reject) => {
        xhr.addEventListener('load', () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(JSON.parse(xhr.responseText))
          } else {
            reject(new Error(`上传失败: ${xhr.statusText}`))
          }
        })

        xhr.addEventListener('error', () => {
          reject(new Error('网络错误'))
        })

        xhr.open('POST', props.uploadUrl!)
        
        // 设置请求头
        if (props.uploadHeaders) {
          Object.entries(props.uploadHeaders).forEach(([key, value]) => {
            xhr.setRequestHeader(key, value)
          })
        }

        xhr.send(formData)
      })

      file.uploaded = true
      file.uploading = false
      emit('upload-success', response, file)

    } catch (err: any) {
      file.error = err.message
      file.uploading = false
      emit('upload-error', err.message, file)
    }
  }

  uploading.value = false
  progress.value = 0
}

// 暴露方法
defineExpose({
  addFiles,
  removeFile,
  clearFiles,
  uploadFiles,
  triggerFileSelect
})

// 监听文件变化
watch(files, (newFiles) => {
  emit('update:files', newFiles)
}, { deep: true })
</script>

<style scoped lang="postcss">
.file-upload-container {
  width: 100%;
}

/* 拖拽区域样式 */
.file-upload-container [role="button"] {
  cursor: pointer;
}

.file-upload-container input[type="file"] {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style> 