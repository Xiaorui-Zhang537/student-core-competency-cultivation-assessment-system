<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">教学资源</h1>
            <p class="text-gray-600 dark:text-gray-400">管理和分享您的教学资源文件</p>
          </div>
          <div class="flex items-center space-x-3">
            <button variant="outline" @click="showCreateFolderModal = true">
              <folder-plus-icon class="w-4 h-4 mr-2" />
              新建文件夹
            </button>
            <button variant="primary" @click="$refs.fileInput?.click()">
              <cloud-arrow-up-icon class="w-4 h-4 mr-2" />
              上传资源
            </button>
          </div>
        </div>
      </div>

      <!-- 资源统计 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-1">
            {{ stats.totalFiles }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">总文件数</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-green-600 dark:text-green-400 mb-1">
            {{ formatFileSize(stats.totalSize) }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">存储空间</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-purple-600 dark:text-purple-400 mb-1">
            {{ stats.shareCount }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">分享次数</p>
        </card>

        <card padding="lg" class="text-center">
          <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400 mb-1">
            {{ stats.downloadCount }}
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">下载次数</p>
        </card>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧文件夹导航 -->
        <div class="lg:col-span-1">
          <card padding="lg">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">文件夹</h2>
            </template>
            
            <div class="space-y-2">
              <button
                v-for="folder in folders"
                :key="folder.id"
                @click="currentFolder = folder.id"
                class="w-full flex items-center px-3 py-2 text-sm rounded-lg transition-colors"
                :class="currentFolder === folder.id
                  ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                <folder-icon class="w-4 h-4 mr-3" />
                <span class="flex-1 text-left">{{ folder.name }}</span>
                <span class="text-xs text-gray-500">({{ folder.fileCount }})</span>
              </button>
            </div>
          </card>

          <!-- 快速操作 -->
          <card padding="lg" class="mt-6">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快速操作</h3>
            </template>
            
            <div class="space-y-2">
              <button variant="outline" size="sm" class="w-full justify-start" @click="showRecentFiles">
                <clock-icon class="w-4 h-4 mr-2" />
                最近文件
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="showSharedFiles">
                <share-icon class="w-4 h-4 mr-2" />
                已分享文件
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="showRecycleBin">
                <trash-icon class="w-4 h-4 mr-2" />
                回收站
              </button>
            </div>
          </card>
        </div>

        <!-- 右侧文件列表 -->
        <div class="lg:col-span-3">
          <card padding="lg">
            <template #header>
              <div class="flex justify-between items-center">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
                  {{ getCurrentFolderName() }}
                </h2>
                <div class="flex items-center space-x-3">
                  <!-- 搜索 -->
                  <div class="relative">
                    <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                      v-model="searchQuery"
                      type="text"
                      placeholder="搜索文件..."
                      class="pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    />
                  </div>
                  
                  <!-- 视图切换 -->
                  <div class="flex items-center border border-gray-300 dark:border-gray-600 rounded-lg">
                    <button
                      @click="viewMode = 'grid'"
                      class="p-2 rounded-l-lg transition-colors"
                      :class="viewMode === 'grid' ? 'bg-primary-100 dark:bg-primary-900 text-primary-600' : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'"
                    >
                      <squares2-x2-icon class="w-4 h-4" />
                    </button>
                    <button
                      @click="viewMode = 'list'"
                      class="p-2 rounded-r-lg transition-colors"
                      :class="viewMode === 'list' ? 'bg-primary-100 dark:bg-primary-900 text-primary-600' : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'"
                    >
                      <list-bullet-icon class="w-4 h-4" />
                    </button>
                  </div>
                  
                  <!-- 排序 -->
                  <select v-model="sortBy" class="input input-sm">
                    <option value="name">名称</option>
                    <option value="date">修改时间</option>
                    <option value="size">文件大小</option>
                    <option value="type">文件类型</option>
                  </select>
                </div>
              </div>
            </template>

            <!-- 上传区域 -->
            <div
              v-if="isDragOver"
              @drop.prevent="handleFileDrop"
              @dragover.prevent
              @dragleave="isDragOver = false"
              class="border-2 border-dashed border-primary-500 rounded-lg p-8 mb-6 text-center bg-primary-50 dark:bg-primary-900/20"
            >
              <cloud-arrow-up-icon class="w-12 h-12 text-primary-500 mx-auto mb-2" />
              <p class="text-primary-600 dark:text-primary-400 font-medium">释放文件以上传</p>
            </div>

            <!-- 文件列表 -->
            <div v-if="viewMode === 'grid'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              <div
                v-for="file in filteredFiles"
                :key="file.id"
                @click="selectFile(file)"
                @dblclick="openFile(file)"
                class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                :class="{ 'ring-2 ring-primary-500 bg-primary-50 dark:bg-primary-900/20': selectedFiles.includes(file.id) }"
              >
                <div class="text-center">
                  <div class="w-12 h-12 mx-auto mb-3 flex items-center justify-center">
                    <component :is="getFileIcon(file.type)" class="w-8 h-8 text-gray-500" />
                  </div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white truncate" :title="file.name">
                    {{ file.name }}
                  </h3>
                  <p class="text-xs text-gray-500 mt-1">{{ formatFileSize(file.size) }}</p>
                  <p class="text-xs text-gray-500">{{ formatDate(file.updatedAt) }}</p>
                </div>
              </div>
            </div>

            <div v-else class="space-y-2">
              <div
                v-for="file in filteredFiles"
                :key="file.id"
                @click="selectFile(file)"
                @dblclick="openFile(file)"
                class="flex items-center justify-between p-3 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                :class="{ 'ring-2 ring-primary-500 bg-primary-50 dark:bg-primary-900/20': selectedFiles.includes(file.id) }"
              >
                <div class="flex items-center space-x-3">
                  <component :is="getFileIcon(file.type)" class="w-6 h-6 text-gray-500" />
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">{{ file.name }}</h3>
                    <p class="text-xs text-gray-500">{{ file.description || '无描述' }}</p>
                  </div>
                </div>
                <div class="flex items-center space-x-6 text-xs text-gray-500">
                  <span>{{ formatFileSize(file.size) }}</span>
                  <span>{{ formatDate(file.updatedAt) }}</span>
                  <div class="flex items-center space-x-1">
                    <button
                      variant="ghost"
                      size="sm"
                      @click.stop="shareFile(file)"
                      class="p-1"
                    >
                      <share-icon class="w-4 h-4" />
                    </button>
                    <button
                      variant="ghost"
                      size="sm"
                      @click.stop="downloadFile(file)"
                      class="p-1"
                    >
                      <arrow-down-tray-icon class="w-4 h-4" />
                    </button>
                    <button
                      variant="ghost"
                      size="sm"
                      @click.stop="deleteFile(file)"
                      class="p-1 text-red-600"
                    >
                      <trash-icon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="filteredFiles.length === 0" class="text-center py-12">
              <folder-icon class="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">暂无文件</h3>
              <p class="text-gray-600 dark:text-gray-400 mb-4">
                {{ searchQuery ? '没有找到匹配的文件' : '这个文件夹是空的' }}
              </p>
              <button variant="primary" @click="$refs.fileInput?.click()">
                <cloud-arrow-up-icon class="w-4 h-4 mr-2" />
                上传第一个文件
              </button>
            </div>
          </card>

          <!-- 批量操作栏 -->
          <div v-if="selectedFiles.length > 0" class="mt-4 p-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-600 rounded-lg">
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600 dark:text-gray-400">
                已选择 {{ selectedFiles.length }} 个文件
              </span>
              <div class="flex items-center space-x-2">
                <button variant="outline" size="sm" @click="moveSelectedFiles">
                  <folder-icon class="w-4 h-4 mr-2" />
                  移动
                </button>
                <button variant="outline" size="sm" @click="shareSelectedFiles">
                  <share-icon class="w-4 h-4 mr-2" />
                  分享
                </button>
                <button variant="outline" size="sm" @click="downloadSelectedFiles">
                  <arrow-down-tray-icon class="w-4 h-4 mr-2" />
                  下载
                </button>
                <button variant="outline" size="sm" @click="deleteSelectedFiles" class="text-red-600">
                  <trash-icon class="w-4 h-4 mr-2" />
                  删除
                </button>
                <button variant="ghost" size="sm" @click="selectedFiles = []">
                  取消选择
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 隐藏的文件输入 -->
      <input
        ref="fileInput"
        type="file"
        multiple
        class="hidden"
        @change="handleFileUpload"
      />

      <!-- 创建文件夹弹窗 -->
      <div v-if="showCreateFolderModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">创建新文件夹</h3>
          <form @submit.prevent="createFolder" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                文件夹名称
              </label>
              <input
                v-model="newFolderName"
                type="text"
                placeholder="输入文件夹名称"
                class="input"
                required
              />
            </div>
            <div class="flex justify-end space-x-3">
              <button variant="outline" @click="showCreateFolderModal = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="isCreatingFolder">
                创建
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 文件分享弹窗 -->
      <div v-if="showShareModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">分享文件</h3>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                分享链接
              </label>
              <div class="flex">
                <input
                  :value="shareLink"
                  readonly
                  class="input rounded-r-none"
                />
                <button variant="outline" @click="copyShareLink" class="rounded-l-none border-l-0">
                  复制
                </button>
              </div>
            </div>
            <div class="flex items-center space-x-3">
              <label class="flex items-center">
                <input
                  v-model="shareSettings.allowDownload"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">允许下载</span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="shareSettings.requirePassword"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">需要密码</span>
              </label>
            </div>
            <div v-if="shareSettings.requirePassword">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                访问密码
              </label>
              <input
                v-model="shareSettings.password"
                type="text"
                placeholder="设置访问密码"
                class="input"
              />
            </div>
          </div>
          <div class="mt-6 flex justify-end space-x-3">
            <button variant="outline" @click="showShareModal = false">
              关闭
            </button>
            <button variant="primary" @click="confirmShare">
              确认分享
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import {
  FolderPlusIcon,
  CloudArrowUpIcon,
  FolderIcon,
  ClockIcon,
  ShareIcon,
  TrashIcon,
  MagnifyingGlassIcon,
  Squares2X2Icon,
  ListBulletIcon,
  ArrowDownTrayIcon,
  DocumentIcon,
  PhotoIcon,
  FilmIcon,
  MusicalNoteIcon,
  DocumentTextIcon
} from '@heroicons/vue/24/outline'

// Stores
const uiStore = useUIStore()

// 状态
const currentFolder = ref('all')
const searchQuery = ref('')
const viewMode = ref<'grid' | 'list'>('grid')
const sortBy = ref('name')
const selectedFiles = ref<string[]>([])
const isDragOver = ref(false)
const showCreateFolderModal = ref(false)
const showShareModal = ref(false)
const isCreatingFolder = ref(false)
const newFolderName = ref('')
const shareLink = ref('')

// 统计数据
const stats = reactive({
  totalFiles: 234,
  totalSize: 2.8 * 1024 * 1024 * 1024, // 2.8GB
  shareCount: 45,
  downloadCount: 1289
})

// 文件夹数据
const folders = ref([
  { id: 'all', name: '全部文件', fileCount: 234 },
  { id: 'documents', name: '文档资料', fileCount: 89 },
  { id: 'presentations', name: 'PPT课件', fileCount: 56 },
  { id: 'videos', name: '教学视频', fileCount: 34 },
  { id: 'images', name: '图片素材', fileCount: 78 },
  { id: 'audios', name: '音频文件', fileCount: 23 },
  { id: 'exercises', name: '练习题库', fileCount: 67 },
  { id: 'exams', name: '考试试卷', fileCount: 45 }
])

// 文件数据
const files = ref([
  {
    id: '1',
    name: '高等数学第一章.pdf',
    type: 'pdf',
    size: 2.5 * 1024 * 1024,
    folder: 'documents',
    description: '函数与极限章节内容',
    updatedAt: '2024-01-15T10:30:00Z',
    createdBy: '张老师',
    shared: true,
    downloads: 156
  },
  {
    id: '2',
    name: '微积分基础.pptx',
    type: 'pptx',
    size: 15.8 * 1024 * 1024,
    folder: 'presentations',
    description: '微积分基础概念讲解',
    updatedAt: '2024-01-14T16:20:00Z',
    createdBy: '李老师',
    shared: false,
    downloads: 89
  },
  {
    id: '3',
    name: '导数应用实例.mp4',
    type: 'mp4',
    size: 125.4 * 1024 * 1024,
    folder: 'videos',
    description: '导数在实际问题中的应用',
    updatedAt: '2024-01-13T14:15:00Z',
    createdBy: '王老师',
    shared: true,
    downloads: 203
  },
  {
    id: '4',
    name: '函数图像.png',
    type: 'png',
    size: 1.2 * 1024 * 1024,
    folder: 'images',
    description: '各种函数的图像示例',
    updatedAt: '2024-01-12T09:45:00Z',
    createdBy: '赵老师',
    shared: false,
    downloads: 67
  },
  {
    id: '5',
    name: '练习题集.docx',
    type: 'docx',
    size: 856 * 1024,
    folder: 'exercises',
    description: '综合练习题目',
    updatedAt: '2024-01-11T11:30:00Z',
    createdBy: '钱老师',
    shared: true,
    downloads: 124
  }
])

// 分享设置
const shareSettings = reactive({
  allowDownload: true,
  requirePassword: false,
  password: ''
})

// 计算属性
const filteredFiles = computed(() => {
  let result = files.value

  // 文件夹筛选
  if (currentFolder.value !== 'all') {
    result = result.filter(file => file.folder === currentFolder.value)
  }

  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(file => 
      file.name.toLowerCase().includes(query) ||
      (file.description && file.description.toLowerCase().includes(query))
    )
  }

  // 排序
  result.sort((a, b) => {
    switch (sortBy.value) {
      case 'name':
        return a.name.localeCompare(b.name)
      case 'date':
        return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      case 'size':
        return b.size - a.size
      case 'type':
        return a.type.localeCompare(b.type)
      default:
        return 0
    }
  })

  return result
})

// 方法
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const getFileIcon = (type: string) => {
  switch (type) {
    case 'pdf':
    case 'doc':
    case 'docx':
      return DocumentTextIcon
    case 'ppt':
    case 'pptx':
      return DocumentIcon
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'gif':
      return PhotoIcon
    case 'mp4':
    case 'avi':
    case 'mov':
      return FilmIcon
    case 'mp3':
    case 'wav':
      return MusicalNoteIcon
    default:
      return DocumentIcon
  }
}

const getCurrentFolderName = () => {
  const folder = folders.value.find(f => f.id === currentFolder.value)
  return folder ? folder.name : '全部文件'
}

const selectFile = (file: any) => {
  const index = selectedFiles.value.indexOf(file.id)
  if (index > -1) {
    selectedFiles.value.splice(index, 1)
  } else {
    selectedFiles.value.push(file.id)
  }
}

const openFile = (file: any) => {
  // 实现文件预览或下载
  downloadFile(file)
}

const handleFileUpload = (event: Event) => {
  const files = Array.from((event.target as HTMLInputElement).files || [])
  uploadFiles(files)
}

const handleFileDrop = (event: DragEvent) => {
  isDragOver.value = false
  const files = Array.from(event.dataTransfer?.files || [])
  uploadFiles(files)
}

const uploadFiles = async (filesToUpload: File[]) => {
  for (const file of filesToUpload) {
    try {
      uiStore.showNotification({
        type: 'info',
        title: '上传中...',
        message: `正在上传 ${file.name}`
      })

      // 模拟上传延迟
      await new Promise(resolve => setTimeout(resolve, 1000))

      // 添加到文件列表
      const newFile = {
        id: Date.now().toString(),
        name: file.name,
        type: file.name.split('.').pop() || 'unknown',
        size: file.size,
        folder: currentFolder.value === 'all' ? 'documents' : currentFolder.value,
        description: '',
        updatedAt: new Date().toISOString(),
        createdBy: '当前用户',
        shared: false,
        downloads: 0
      }

      files.value.unshift(newFile)

      uiStore.showNotification({
        type: 'success',
        title: '上传成功',
        message: `${file.name} 上传完成`
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '上传失败',
        message: `${file.name} 上传失败`
      })
    }
  }
}

const createFolder = async () => {
  if (!newFolderName.value.trim()) return

  isCreatingFolder.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))

    folders.value.push({
      id: Date.now().toString(),
      name: newFolderName.value,
      fileCount: 0
    })

    uiStore.showNotification({
      type: 'success',
      title: '创建成功',
      message: `文件夹 "${newFolderName.value}" 创建成功`
    })

    showCreateFolderModal.value = false
    newFolderName.value = ''
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '创建失败',
      message: '创建文件夹时发生错误'
    })
  } finally {
    isCreatingFolder.value = false
  }
}

const shareFile = (file: any) => {
  shareLink.value = `https://platform.example.com/share/${file.id}`
  showShareModal.value = true
}

const copyShareLink = async () => {
  try {
    await navigator.clipboard.writeText(shareLink.value)
    uiStore.showNotification({
      type: 'success',
      title: '复制成功',
      message: '分享链接已复制到剪贴板'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '复制失败',
      message: '无法复制到剪贴板'
    })
  }
}

const confirmShare = () => {
  uiStore.showNotification({
    type: 'success',
    title: '分享成功',
    message: '文件分享设置已保存'
  })
  showShareModal.value = false
}

const downloadFile = async (file: any) => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '下载中...',
      message: `正在下载 ${file.name}`
    })

    // 模拟下载延迟
    await new Promise(resolve => setTimeout(resolve, 1500))

    uiStore.showNotification({
      type: 'success',
      title: '下载完成',
      message: `${file.name} 下载完成`
    })

    // 更新下载次数
    file.downloads++
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '下载失败',
      message: '下载文件时发生错误'
    })
  }
}

const deleteFile = async (file: any) => {
  if (confirm(`确定要删除文件 "${file.name}" 吗？`)) {
    try {
      const index = files.value.findIndex(f => f.id === file.id)
      if (index > -1) {
        files.value.splice(index, 1)
        uiStore.showNotification({
          type: 'success',
          title: '删除成功',
          message: `${file.name} 已删除`
        })
      }
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '删除失败',
        message: '删除文件时发生错误'
      })
    }
  }
}

const moveSelectedFiles = () => {
  uiStore.showNotification({
    type: 'info',
    title: '移动文件',
    message: '文件移动功能开发中...'
  })
}

const shareSelectedFiles = () => {
  uiStore.showNotification({
    type: 'info',
    title: '批量分享',
    message: '批量分享功能开发中...'
  })
}

const downloadSelectedFiles = () => {
  uiStore.showNotification({
    type: 'info',
    title: '批量下载',
    message: '批量下载功能开发中...'
  })
}

const deleteSelectedFiles = () => {
  if (confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`)) {
    selectedFiles.value.forEach(fileId => {
      const index = files.value.findIndex(f => f.id === fileId)
      if (index > -1) {
        files.value.splice(index, 1)
      }
    })
    selectedFiles.value = []
    uiStore.showNotification({
      type: 'success',
      title: '删除成功',
      message: '选中的文件已删除'
    })
  }
}

const showRecentFiles = () => {
  currentFolder.value = 'all'
  sortBy.value = 'date'
}

const showSharedFiles = () => {
  currentFolder.value = 'all'
  // 这里可以添加筛选已分享文件的逻辑
}

const showRecycleBin = () => {
  uiStore.showNotification({
    type: 'info',
    title: '回收站',
    message: '回收站功能开发中...'
  })
}

// 拖拽事件监听
onMounted(() => {
  document.addEventListener('dragover', (e) => {
    e.preventDefault()
    isDragOver.value = true
  })
  
  document.addEventListener('dragleave', (e) => {
    if (!e.relatedTarget) {
      isDragOver.value = false
    }
  })
})
</script> 