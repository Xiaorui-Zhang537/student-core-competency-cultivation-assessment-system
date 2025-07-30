<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-6xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">创建新课程</h1>
            <p class="text-gray-600 dark:text-gray-400">设计和发布您的在线课程</p>
          </div>
          <div class="flex items-center space-x-4">
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

      <form @submit.prevent="createCourse" class="space-y-8">
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
                    <badge v-for="(tag, index) in courseForm.tags" :key="index" variant="secondary" class="flex items-center">
                      {{ tag }}
                      <button
                        type="button"
                        @click="removeTag(index)"
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
                  <button type="button" @click="addChapter" variant="outline" size="sm">
                    <plus-icon class="w-4 h-4 mr-2" />
                    添加章节
                  </button>
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
                      <input
                        v-model="chapter.title"
                        type="text"
                        placeholder="章节标题"
                        class="input mb-2"
                      />
                      <textarea
                        v-model="chapter.description"
                        rows="2"
                        placeholder="章节描述"
                        class="input"
                      ></textarea>
                    </div>
                    <div class="ml-4 flex items-center space-x-2">
                      <button type="button" @click="moveChapter(chapterIndex, -1)" variant="ghost" size="sm" :disabled="chapterIndex === 0">
                        <chevron-up-icon class="w-4 h-4" />
                      </button>
                      <button type="button" @click="moveChapter(chapterIndex, 1)" variant="ghost" size="sm" :disabled="chapterIndex === courseForm.chapters.length - 1">
                        <chevron-down-icon class="w-4 h-4" />
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
                          <input
                            v-model="lesson.title"
                            type="text"
                            placeholder="课时标题"
                            class="input input-sm mb-1"
                          />
                          <div class="grid grid-cols-2 gap-2">
                            <select v-model="lesson.type" class="input input-sm">
                              <option value="video">视频</option>
                              <option value="document">文档</option>
                              <option value="quiz">测验</option>
                            </select>
                            <input
                              v-model.number="lesson.duration"
                              type="number"
                              placeholder="时长(分钟)"
                              class="input input-sm"
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
                  <div v-if="!courseForm.thumbnail">
                    <photo-icon class="w-12 h-12 text-gray-400 mx-auto mb-2" />
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      点击上传或拖拽图片文件
                    </p>
                    <p class="text-xs text-gray-500 mt-1">
                      支持 JPG、PNG，最大 5MB
                    </p>
                  </div>
                  <div v-else class="relative">
                    <img :src="courseForm.thumbnail" alt="课程封面" class="w-full h-32 object-cover rounded" />
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
                  @change="handleFileChange"
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
                  <select v-model="courseForm.status" class="input">
                    <option value="draft">草稿</option>
                    <option value="published">已发布</option>
                    <option value="archived">已归档</option>
                  </select>
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
                    />
                  </div>
                  <p class="mt-1 text-xs text-gray-500">设置为 0 表示免费课程</p>
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
                :disabled="!isFormValid"
              >
                <check-icon class="w-4 h-4 mr-2" />
                {{ courseForm.status === 'published' ? '创建并发布课程' : '创建课程' }}
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
            </div>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { coursesAPI, type CreateCourseRequest } from '@/api/courses.api'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  DocumentDuplicateIcon,
  PlusIcon,
  XMarkIcon,
  TagIcon,
  PhotoIcon,
  EyeIcon,
  CheckIcon
} from '@heroicons/vue/24/outline'

// 路由和状态管理
const router = useRouter()
const uiStore = useUIStore()

// 响应式状态
const isSubmitting = ref(false)
const isDraftSaving = ref(false)
const newTag = ref('')
const fileInput = ref<HTMLInputElement>()

// 表单数据
const courseForm = reactive<CreateCourseRequest>({
  title: '',
  description: '',
  category: '',
  level: 'beginner',
  duration: 0,
  price: 0,
  tags: [],
  thumbnail: '',
  requirements: [],
  objectives: [],
  status: 'draft'
})

// 错误状态
const errors = reactive({
  title: '',
  description: '',
  category: ''
})

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

// 方法
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

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !courseForm.tags.includes(tag)) {
    courseForm.tags.push(tag)
    newTag.value = ''
  }
}

const removeTag = (index: number) => {
  courseForm.tags.splice(index, 1)
}

const addRequirement = () => {
  courseForm.requirements?.push('')
}

const removeRequirement = (index: number) => {
  courseForm.requirements?.splice(index, 1)
}

const addObjective = () => {
  courseForm.objectives?.push('')
}

const removeObjective = (index: number) => {
  courseForm.objectives?.splice(index, 1)
}

const uploadThumbnail = () => {
  fileInput.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    // 这里应该上传文件到服务器，获取URL
    // 暂时使用本地预览
    const reader = new FileReader()
    reader.onload = (e) => {
      courseForm.thumbnail = e.target?.result as string
    }
    reader.readAsDataURL(file)
  }
}

const saveDraft = async () => {
  isDraftSaving.value = true
  try {
    // 设置状态为草稿
    const draftData = { ...courseForm, status: 'draft' as const }
    
    const response = await coursesAPI.createCourse(draftData)
    
    uiStore.showNotification({
      type: 'success',
      title: '草稿保存成功',
      message: '您的课程草稿已保存'
    })
    
    // 跳转到课程管理页面
    router.push('/teacher/courses')
  } catch (error: any) {
    console.error('保存草稿失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: error.message || '保存草稿时发生错误'
    })
  } finally {
    isDraftSaving.value = false
  }
}

const createCourse = async () => {
  // 验证表单
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
    // 调用真正的API创建课程
    const response = await coursesAPI.createCourse(courseForm)
    
    uiStore.showNotification({
      type: 'success',
      title: '课程创建成功',
      message: courseForm.status === 'published' ? '课程已发布' : '课程已保存为草稿'
    })
    
    // 跳转到课程管理页面
    router.push('/teacher/courses')
  } catch (error: any) {
    console.error('创建课程失败:', error)
    uiStore.showNotification({
      type: 'error',
      title: '创建失败',
      message: error.message || '创建课程时发生错误'
    })
  } finally {
    isSubmitting.value = false
  }
}

const previewCourse = () => {
  // 打开预览窗口或跳转到预览页面
  uiStore.showNotification({
    type: 'info',
    title: '预览功能',
    message: '预览功能开发中...'
  })
}
</script>

