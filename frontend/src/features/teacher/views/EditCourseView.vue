<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-6xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">编辑课程</h1>
            <p class="text-gray-600 dark:text-gray-400">修改和更新您的课程内容</p>
          </div>
          <div class="flex items-center space-x-4">
            <badge :variant="courseForm.status === 'published' ? 'success' : courseForm.status === 'draft' ? 'warning' : 'secondary'">
              {{ getStatusText(courseForm.status) }}
            </badge>
            <button variant="outline" @click="saveDraft" :loading="isDraftSaving">
              <document-duplicate-icon class="w-4 h-4 mr-2" />
              保存草稿
            </button>
            <button variant="outline" @click="$router.go(-1)">
              取消
            </button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        <p class="mt-2 text-gray-600 dark:text-gray-400">加载课程数据中...</p>
      </div>

      <form v-else @submit.prevent="updateCourse" class="space-y-8">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- 左侧表单 -->
          <div class="lg:col-span-2 space-y-8">
            <!-- 基本信息 -->
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">基本信息</h2>
              </template>

              <div class="space-y-6">
                <!-- 课程标题 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    课程标题 <span class="text-red-500">*</span>
                  </label>
                  <input
                    v-model="courseForm.title"
                    type="text"
                    placeholder="输入课程标题..."
                    class="input"
                    :class="{ 'border-red-500': errors.title }"
                    @blur="validateField('title')"
                  />
                  <p v-if="errors.title" class="mt-1 text-sm text-red-600">{{ errors.title }}</p>
                </div>

                <!-- 课程简介 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    课程简介 <span class="text-red-500">*</span>
                  </label>
                  <textarea
                    v-model="courseForm.description"
                    rows="4"
                    placeholder="简要描述您的课程内容和学习目标..."
                    class="input"
                    :class="{ 'border-red-500': errors.description }"
                    @blur="validateField('description')"
                  ></textarea>
                  <p v-if="errors.description" class="mt-1 text-sm text-red-600">{{ errors.description }}</p>
                </div>

                <!-- 详细描述 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    详细描述
                  </label>
                  <textarea
                    v-model="courseForm.content"
                    rows="6"
                    placeholder="详细介绍课程内容、学习方法、适用对象等..."
                    class="input"
                  ></textarea>
                </div>

                <!-- 课程分类和标签 -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      课程分类 <span class="text-red-500">*</span>
                    </label>
                    <select
                      v-model="courseForm.category"
                      class="input"
                      :class="{ 'border-red-500': errors.category }"
                      @change="validateField('category')"
                    >
                      <option value="">选择分类</option>
                      <option v-for="category in categories" :key="category.value" :value="category.value">
                        {{ category.label }}
                      </option>
                    </select>
                    <p v-if="errors.category" class="mt-1 text-sm text-red-600">{{ errors.category }}</p>
                  </div>

                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      难度等级
                    </label>
                    <select v-model="courseForm.level" class="input">
                      <option value="beginner">初级</option>
                      <option value="intermediate">中级</option>
                      <option value="advanced">高级</option>
                    </select>
                  </div>
                </div>

                <!-- 标签 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    课程标签
                  </label>
                  <div class="flex flex-wrap gap-2 mb-2">
                    <badge v-for="tag in courseForm.tags" :key="tag" variant="secondary" class="flex items-center">
                      {{ tag }}
                      <button
                        type="button"
                        @click="removeTag(tag)"
                        class="ml-1 text-gray-500 hover:text-gray-700"
                      >
                        <x-mark-icon class="w-3 h-3" />
                      </button>
                    </badge>
                  </div>
                  <div class="flex">
                    <input
                      v-model="newTag"
                      type="text"
                      placeholder="输入标签后按回车添加"
                      class="input rounded-r-none"
                      @keydown.enter.prevent="addTag"
                    />
                    <button type="button" @click="addTag" variant="outline" class="rounded-l-none border-l-0">
                      添加
                    </button>
                  </div>
                </div>
              </div>
            </card>

            <!-- 课程章节 -->
            <card padding="lg">
              <template #header>
                <div class="flex justify-between items-center">
                  <h2 class="text-lg font-semibold text-gray-900 dark:text-white">课程章节</h2>
                  <div class="flex items-center space-x-2">
                    <button type="button" @click="addChapter" variant="outline" size="sm">
                      <plus-icon class="w-4 h-4 mr-2" />
                      添加章节
                    </button>
                    <button type="button" @click="reorderChapters" variant="outline" size="sm">
                      <arrows-up-down-icon class="w-4 h-4 mr-2" />
                      重新排序
                    </button>
                  </div>
                </div>
              </template>

              <div class="space-y-6">
                <div v-if="courseForm.chapters.length === 0" class="text-center py-8">
                  <book-open-icon class="w-12 h-12 text-gray-400 mx-auto mb-4" />
                  <p class="text-gray-500 dark:text-gray-400">还没有添加章节，点击上方按钮开始添加</p>
                </div>

                <div v-for="(chapter, chapterIndex) in courseForm.chapters" 
                     :key="chapter.id" 
                     class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
                  <div class="flex justify-between items-start mb-4">
                    <div class="flex-1">
                      <div class="flex items-center space-x-3 mb-2">
                        <span class="text-sm font-medium text-gray-500 dark:text-gray-400">
                          第{{ chapterIndex + 1 }}章
                        </span>
                        <badge v-if="chapter.isNew" variant="success" size="sm">新增</badge>
                        <badge v-else-if="chapter.isModified" variant="warning" size="sm">已修改</badge>
                      </div>
                      <input
                        v-model="chapter.title"
                        type="text"
                        placeholder="章节标题"
                        class="input mb-2"
                        @input="markChapterAsModified(chapter)"
                      />
                      <textarea
                        v-model="chapter.description"
                        rows="2"
                        placeholder="章节描述"
                        class="input"
                        @input="markChapterAsModified(chapter)"
                      ></textarea>
                    </div>
                    <div class="ml-4 flex items-center space-x-2">
                      <button type="button" @click="moveChapter(chapterIndex, -1)" variant="ghost" size="sm" :disabled="chapterIndex === 0">
                        <chevron-up-icon class="w-4 h-4" />
                      </button>
                      <button type="button" @click="moveChapter(chapterIndex, 1)" variant="ghost" size="sm" :disabled="chapterIndex === courseForm.chapters.length - 1">
                        <chevron-down-icon class="w-4 h-4" />
                      </button>
                      <button type="button" @click="duplicateChapter(chapterIndex)" variant="ghost" size="sm">
                        <document-duplicate-icon class="w-4 h-4" />
                      </button>
                      <button type="button" @click="removeChapter(chapterIndex)" variant="ghost" size="sm" class="text-red-600">
                        <trash-icon class="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  <!-- 章节课时 -->
                  <div class="ml-4">
                    <div class="flex justify-between items-center mb-3">
                      <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300">课时列表</h4>
                      <button type="button" @click="addLesson(chapterIndex)" variant="outline" size="sm">
                        <plus-icon class="w-3 h-3 mr-1" />
                        添加课时
                      </button>
                    </div>
                    
                    <div class="space-y-3">
                      <div v-for="(lesson, lessonIndex) in chapter.lessons" 
                           :key="lesson.id"
                           class="flex items-center space-x-3 p-3 bg-gray-50 dark:bg-gray-700 rounded">
                        <div class="flex-1">
                          <div class="flex items-center space-x-2 mb-1">
                            <input
                              v-model="lesson.title"
                              type="text"
                              placeholder="课时标题"
                              class="input input-sm flex-1"
                              @input="markLessonAsModified(lesson)"
                            />
                            <badge v-if="lesson.isNew" variant="success" size="sm">新增</badge>
                            <badge v-else-if="lesson.isModified" variant="warning" size="sm">已修改</badge>
                          </div>
                          <div class="grid grid-cols-2 gap-2">
                            <select v-model="lesson.type" class="input input-sm" @change="markLessonAsModified(lesson)">
                              <option value="video">视频</option>
                              <option value="document">文档</option>
                              <option value="quiz">测验</option>
                            </select>
                            <input
                              v-model.number="lesson.duration"
                              type="number"
                              placeholder="时长(分钟)"
                              class="input input-sm"
                              @input="markLessonAsModified(lesson)"
                            />
                          </div>
                        </div>
                        <button type="button" @click="removeLesson(chapterIndex, lessonIndex)" variant="ghost" size="sm" class="text-red-600">
                          <x-mark-icon class="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </card>

            <!-- 修改历史 -->
            <card padding="lg" v-if="hasModifications">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">修改历史</h2>
              </template>
              
              <div class="space-y-3">
                <div v-for="change in modificationHistory" :key="change.id" 
                     class="flex items-center justify-between p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                  <div>
                    <p class="text-sm font-medium text-gray-900 dark:text-white">{{ change.description }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">{{ formatDate(change.timestamp) }}</p>
                  </div>
                  <badge :variant="change.type === 'add' ? 'success' : change.type === 'modify' ? 'warning' : 'danger'" size="sm">
                    {{ getChangeTypeText(change.type) }}
                  </badge>
                </div>
              </div>
            </card>
          </div>

          <!-- 右侧设置 -->
          <div class="space-y-6">
            <!-- 封面图片 -->
            <card padding="lg">
              <template #header>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">课程封面</h3>
              </template>

              <div class="space-y-4">
                <div
                  @drop.prevent="handleCoverDrop"
                  @dragover.prevent
                  @dragenter.prevent
                  class="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-6 text-center cursor-pointer hover:border-primary-500 transition-colors"
                  :class="{ 'border-primary-500': isDragOver }"
                  @click="$refs.coverInput?.click()"
                >
                  <div v-if="!courseForm.coverUrl">
                    <photo-icon class="w-12 h-12 text-gray-400 mx-auto mb-2" />
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      点击上传或拖拽图片文件
                    </p>
                    <p class="text-xs text-gray-500 mt-1">
                      支持 JPG、PNG，最大 5MB
                    </p>
                  </div>
                  <div v-else class="relative">
                    <img :src="courseForm.coverUrl" alt="课程封面" class="w-full h-32 object-cover rounded" />
                    <button
                      type="button"
                      @click.stop="removeCover"
                      variant="ghost"
                      size="sm"
                      class="absolute top-2 right-2 bg-white/80 hover:bg-white"
                    >
                      <x-mark-icon class="w-4 h-4" />
                    </button>
                  </div>
                </div>
                <input
                  ref="coverInput"
                  type="file"
                  accept="image/*"
                  class="hidden"
                  @change="handleCoverUpload"
                />
              </div>
            </card>

            <!-- 发布设置 -->
            <card padding="lg">
              <template #header>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">发布设置</h3>
              </template>

              <div class="space-y-4">
                <!-- 课程状态 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    发布状态
                  </label>
                  <select v-model="courseForm.status" class="input" @change="handleStatusChange">
                    <option value="draft">草稿</option>
                    <option value="published">已发布</option>
                    <option value="archived">已归档</option>
                  </select>
                  <p v-if="statusChangeWarning" class="mt-1 text-xs text-yellow-600">
                    {{ statusChangeWarning }}
                  </p>
                </div>

                <!-- 课程价格 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    课程价格
                  </label>
                  <div class="flex">
                    <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 sm:text-sm">
                      ¥
                    </span>
                    <input
                      v-model.number="courseForm.price"
                      type="number"
                      min="0"
                      step="0.01"
                      placeholder="0.00"
                      class="input rounded-l-none"
                      @change="handlePriceChange"
                    />
                  </div>
                  <p class="mt-1 text-xs text-gray-500">设置为 0 表示免费课程</p>
                  <p v-if="priceChangeWarning" class="mt-1 text-xs text-yellow-600">
                    {{ priceChangeWarning }}
                  </p>
                </div>

                <!-- 课程设置 -->
                <div class="space-y-3">
                  <label class="flex items-center">
                    <input v-model="courseForm.allowComments" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                    <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">允许评论</span>
                  </label>
                  <label class="flex items-center">
                    <input v-model="courseForm.allowDownload" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                    <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">允许下载资料</span>
                  </label>
                  <label class="flex items-center">
                    <input v-model="courseForm.requireApproval" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
                    <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">需要审核报名</span>
                  </label>
                </div>

                <!-- 最大学生数 -->
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    最大学生数
                  </label>
                  <input
                    v-model.number="courseForm.maxStudents"
                    type="number"
                    min="1"
                    placeholder="不限制"
                    class="input"
                  />
                  <p class="mt-1 text-xs text-gray-500">留空表示不限制学生数量</p>
                </div>
              </div>
            </card>

            <!-- 操作按钮 -->
            <div class="space-y-3">
              <button
                type="submit"
                variant="primary"
                size="lg"
                class="w-full"
                :loading="isSubmitting"
                :disabled="!isFormValid || !hasChanges"
              >
                <check-icon class="w-4 h-4 mr-2" />
                保存更改
              </button>
              
              <button
                type="button"
                variant="outline"
                size="lg"
                class="w-full"
                @click="previewCourse"
              >
                <eye-icon class="w-4 h-4 mr-2" />
                预览课程
              </button>

              <button
                type="button"
                variant="outline"
                size="lg"
                class="w-full"
                @click="resetChanges"
                v-if="hasChanges"
              >
                <arrow-uturn-left-icon class="w-4 h-4 mr-2" />
                重置更改
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  PlusIcon,
  XMarkIcon,
  TrashIcon,
  ChevronUpIcon,
  ChevronDownIcon,
  PhotoIcon,
  BookOpenIcon,
  DocumentDuplicateIcon,
  ArrowsUpDownIcon,
  CheckIcon,
  EyeIcon,
  ArrowUturnLeftIcon
} from '@heroicons/vue/24/outline'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

// 状态
const isLoading = ref(true)
const isSubmitting = ref(false)
const isDraftSaving = ref(false)
const isDragOver = ref(false)
const newTag = ref('')
const originalCourse = ref<any>(null)
const statusChangeWarning = ref('')
const priceChangeWarning = ref('')

// 表单数据
const courseForm = reactive({
  id: '',
  title: '',
  description: '',
  content: '',
  category: '',
  level: 'beginner',
  tags: [] as string[],
  coverUrl: '',
  status: 'draft',
  price: 0,
  allowComments: true,
  allowDownload: true,
  requireApproval: false,
  maxStudents: null as number | null,
  chapters: [] as Array<{
    id: string
    title: string
    description: string
    isNew?: boolean
    isModified?: boolean
    lessons: Array<{
      id: string
      title: string
      type: 'video' | 'document' | 'quiz'
      duration: number
      isNew?: boolean
      isModified?: boolean
    }>
  }>
})

// 错误状态
const errors = reactive({
  title: '',
  description: '',
  category: ''
})

// 修改历史
const modificationHistory = ref<Array<{
  id: string
  type: 'add' | 'modify' | 'delete'
  description: string
  timestamp: string
}>>([])

// 课程分类选项
const categories = [
  { value: 'programming', label: '编程开发' },
  { value: 'design', label: '设计创意' },
  { value: 'business', label: '商业管理' },
  { value: 'marketing', label: '市场营销' },
  { value: 'language', label: '语言学习' },
  { value: 'science', label: '科学技术' },
  { value: 'art', label: '艺术人文' },
  { value: 'other', label: '其他' }
]

// 计算属性
const isFormValid = computed(() => {
  return courseForm.title.trim() !== '' &&
    courseForm.description.trim() !== '' &&
    courseForm.category !== '' &&
    Object.values(errors).every(error => error === '')
})

const hasChanges = computed(() => {
  if (!originalCourse.value) return false
  return JSON.stringify(courseForm) !== JSON.stringify(originalCourse.value)
})

const hasModifications = computed(() => {
  return modificationHistory.value.length > 0
})

// 方法
const loadCourse = async () => {
  try {
    // 模拟加载课程数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const mockCourse = {
      id: route.params.id as string,
      title: '高等数学基础教程',
      description: '系统学习高等数学的基础概念和应用，适合理工科学生和数学爱好者',
      content: '本课程涵盖极限、导数、积分等核心内容，通过理论讲解与实例演示相结合的方式，帮助学生掌握高等数学的核心知识点。',
      category: 'science',
      level: 'intermediate',
      tags: ['数学', '高等数学', '理工科', '基础教程'],
      coverUrl: '',
      status: 'published',
      price: 199,
      allowComments: true,
      allowDownload: true,
      requireApproval: false,
      maxStudents: null,
      chapters: [
        {
          id: '1',
          title: '函数与极限',
          description: '学习函数的基本概念和极限理论',
          lessons: [
            { id: '1-1', title: '函数的概念', type: 'video', duration: 45 },
            { id: '1-2', title: '极限的定义', type: 'video', duration: 50 },
            { id: '1-3', title: '极限的计算', type: 'video', duration: 55 }
          ]
        },
        {
          id: '2',
          title: '导数与微分',
          description: '掌握导数的计算方法和应用',
          lessons: [
            { id: '2-1', title: '导数的定义', type: 'video', duration: 40 },
            { id: '2-2', title: '导数的计算法则', type: 'video', duration: 60 },
            { id: '2-3', title: '导数的应用', type: 'quiz', duration: 30 }
          ]
        }
      ]
    }
    
    // 保存原始数据用于比较
    originalCourse.value = JSON.parse(JSON.stringify(mockCourse))
    
    // 填充表单
    Object.assign(courseForm, mockCourse)
    
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '加载失败',
      message: '无法加载课程数据'
    })
  } finally {
    isLoading.value = false
  }
}

const validateField = (field: string) => {
  switch (field) {
    case 'title':
      errors.title = courseForm.title.trim() === '' ? '请输入课程标题' : ''
      break
    case 'description':
      errors.description = courseForm.description.trim() === '' ? '请输入课程简介' : ''
      break
    case 'category':
      errors.category = courseForm.category === '' ? '请选择课程分类' : ''
      break
  }
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    published: '已发布',
    draft: '草稿',
    archived: '已归档'
  }
  return statusMap[status] || status
}

const getChangeTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    add: '新增',
    modify: '修改',
    delete: '删除'
  }
  return typeMap[type] || type
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !courseForm.tags.includes(tag)) {
    courseForm.tags.push(tag)
    newTag.value = ''
    addToHistory('modify', `添加标签: ${tag}`)
  }
}

const removeTag = (tag: string) => {
  const index = courseForm.tags.indexOf(tag)
  if (index > -1) {
    courseForm.tags.splice(index, 1)
    addToHistory('modify', `删除标签: ${tag}`)
  }
}

const addChapter = () => {
  const newChapter = {
    id: Date.now().toString(),
    title: '',
    description: '',
    isNew: true,
    lessons: []
  }
  courseForm.chapters.push(newChapter)
  addToHistory('add', '添加新章节')
}

const removeChapter = (index: number) => {
  const chapter = courseForm.chapters[index]
  if (confirm('确定要删除这个章节吗？')) {
    courseForm.chapters.splice(index, 1)
    addToHistory('delete', `删除章节: ${chapter.title || '未命名章节'}`)
  }
}

const moveChapter = (index: number, direction: number) => {
  const newIndex = index + direction
  if (newIndex >= 0 && newIndex < courseForm.chapters.length) {
    const chapter = courseForm.chapters.splice(index, 1)[0]
    courseForm.chapters.splice(newIndex, 0, chapter)
    addToHistory('modify', '调整章节顺序')
  }
}

const duplicateChapter = (index: number) => {
  const originalChapter = courseForm.chapters[index]
  const duplicatedChapter = {
    ...JSON.parse(JSON.stringify(originalChapter)),
    id: Date.now().toString(),
    title: `${originalChapter.title} (副本)`,
    isNew: true,
    lessons: originalChapter.lessons.map(lesson => ({
      ...lesson,
      id: Date.now().toString() + Math.random(),
      isNew: true
    }))
  }
  courseForm.chapters.splice(index + 1, 0, duplicatedChapter)
  addToHistory('add', `复制章节: ${originalChapter.title}`)
}

const addLesson = (chapterIndex: number) => {
  const newLesson = {
    id: Date.now().toString(),
    title: '',
    type: 'video' as const,
    duration: 0,
    isNew: true
  }
  courseForm.chapters[chapterIndex].lessons.push(newLesson)
  addToHistory('add', '添加新课时')
}

const removeLesson = (chapterIndex: number, lessonIndex: number) => {
  const lesson = courseForm.chapters[chapterIndex].lessons[lessonIndex]
  if (confirm('确定要删除这个课时吗？')) {
    courseForm.chapters[chapterIndex].lessons.splice(lessonIndex, 1)
    addToHistory('delete', `删除课时: ${lesson.title || '未命名课时'}`)
  }
}

const markChapterAsModified = (chapter: any) => {
  if (!chapter.isNew) {
    chapter.isModified = true
  }
}

const markLessonAsModified = (lesson: any) => {
  if (!lesson.isNew) {
    lesson.isModified = true
  }
}

const handleStatusChange = () => {
  if (originalCourse.value && courseForm.status !== originalCourse.value.status) {
    if (courseForm.status === 'published' && originalCourse.value.status === 'draft') {
      statusChangeWarning.value = '发布后学生将能看到此课程'
    } else if (courseForm.status === 'archived' && originalCourse.value.status === 'published') {
      statusChangeWarning.value = '归档后学生将无法访问此课程'
    } else {
      statusChangeWarning.value = ''
    }
  }
}

const handlePriceChange = () => {
  if (originalCourse.value && courseForm.price !== originalCourse.value.price) {
    if (courseForm.price > originalCourse.value.price) {
      priceChangeWarning.value = '涨价可能影响学生报名积极性'
    } else if (courseForm.price < originalCourse.value.price) {
      priceChangeWarning.value = '降价不会影响已报名学生'
    } else {
      priceChangeWarning.value = ''
    }
  }
}

const handleCoverUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    uploadCover(file)
  }
}

const handleCoverDrop = (event: DragEvent) => {
  isDragOver.value = false
  const file = event.dataTransfer?.files[0]
  if (file && file.type.startsWith('image/')) {
    uploadCover(file)
  }
}

const uploadCover = (file: File) => {
  if (file.size > 5 * 1024 * 1024) {
    uiStore.showNotification({
      type: 'error',
      title: '文件过大',
      message: '图片文件不能超过 5MB'
    })
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    courseForm.coverUrl = e.target?.result as string
    addToHistory('modify', '更新课程封面')
  }
  reader.readAsDataURL(file)
}

const removeCover = () => {
  courseForm.coverUrl = ''
  addToHistory('modify', '删除课程封面')
}

const addToHistory = (type: 'add' | 'modify' | 'delete', description: string) => {
  modificationHistory.value.push({
    id: Date.now().toString(),
    type,
    description,
    timestamp: new Date().toISOString()
  })
}

const saveDraft = async () => {
  isDraftSaving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    uiStore.showNotification({
      type: 'success',
      title: '草稿已保存',
      message: '课程修改已保存为草稿'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: '保存草稿时发生错误'
    })
  } finally {
    isDraftSaving.value = false
  }
}

const updateCourse = async () => {
  validateField('title')
  validateField('description')
  validateField('category')
  
  if (!isFormValid.value) {
    uiStore.showNotification({
      type: 'error',
      title: '表单验证失败',
      message: '请检查并填写必填字段'
    })
    return
  }

  isSubmitting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    uiStore.showNotification({
      type: 'success',
      title: '更新成功',
      message: '课程信息已成功更新'
    })
    
    // 更新原始数据
    originalCourse.value = JSON.parse(JSON.stringify(courseForm))
    modificationHistory.value = []
    
    // 跳转回课程详情页
    router.push(`/teacher/courses/${courseForm.id}`)
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '更新失败',
      message: '更新课程时发生错误'
    })
  } finally {
    isSubmitting.value = false
  }
}

const resetChanges = () => {
  if (confirm('确定要重置所有更改吗？此操作不可撤销。')) {
    if (originalCourse.value) {
      Object.assign(courseForm, JSON.parse(JSON.stringify(originalCourse.value)))
      modificationHistory.value = []
      statusChangeWarning.value = ''
      priceChangeWarning.value = ''
      
      uiStore.showNotification({
        type: 'info',
        title: '已重置',
        message: '所有更改已重置到原始状态'
      })
    }
  }
}

const previewCourse = () => {
  window.open(`/preview/course/${courseForm.id}`, '_blank')
}

// 生命周期
onMounted(() => {
  loadCourse()
})
</script> 