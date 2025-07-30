<template>
  <div v-if="loading" class="flex justify-center items-center py-12">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
  </div>

  <div v-else-if="currentCourse" class="max-w-7xl mx-auto">
    <!-- 面包屑导航 -->
    <nav class="flex mb-6" aria-label="Breadcrumb">
      <ol class="inline-flex items-center space-x-1 md:space-x-3">
        <li class="inline-flex items-center">
          <router-link to="/student/courses" class="text-gray-700 hover:text-primary-600 dark:text-gray-400 dark:hover:text-white">
            我的课程
          </router-link>
        </li>
        <li>
          <div class="flex items-center">
            <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"></path>
            </svg>
            <span class="ml-1 font-medium text-gray-500 dark:text-gray-400">{{ currentCourse.title }}</span>
          </div>
        </li>
      </ol>
    </nav>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- 主要内容区域 -->
      <div class="lg:col-span-2">
        <!-- 课程标题和基本信息 -->
        <div class="mb-8">
          <div class="flex items-start justify-between mb-4">
            <div class="flex-1">
              <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                {{ currentCourse.title }}
              </h1>
              <p class="text-lg text-gray-600 dark:text-gray-400 mb-4">
                {{ currentCourse.description }}
              </p>
              
              <!-- 课程标签 -->
              <div class="flex flex-wrap gap-2 mb-4">
                <badge :variant="getLevelVariant(currentCourse.level)">
                  {{ getLevelText(currentCourse.level) }}
                </badge>
                <badge variant="secondary">{{ currentCourse.category }}</badge>
                <badge v-for="tag in currentCourse.tags" :key="tag" variant="info">
                  {{ tag }}
                </badge>
              </div>

              <!-- 评分和统计 -->
              <div class="flex items-center space-x-6 text-sm text-gray-500 dark:text-gray-400">
                <div class="flex items-center">
                  <svg class="w-5 h-5 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                  {{ currentCourse.rating }} 评分
                </div>
                <div>{{ currentCourse.studentsCount }} 名学生</div>
                <div>{{ formatDuration(currentCourse.duration) }}</div>
                <div>{{ currentCourse.totalLessons }} 课时</div>
              </div>
            </div>
          </div>

          <!-- 进度条 -->
          <progress 
            :value="currentCourse.progress" 
            :label="`学习进度: ${currentCourse.completedLessons}/${currentCourse.totalLessons} 课时已完成`"
            :show-label="true"
            :color="currentCourse.progress === 100 ? 'success' : 'primary'"
            size="lg"
          />
        </div>

        <!-- 章节列表 -->
        <card>
          <template #header>
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-semibold text-gray-900 dark:text-white">课程内容</h2>
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ lessons.filter((l: Lesson) => l.isCompleted).length }}/{{ lessons.length }} 已完成
              </span>
            </div>
          </template>

          <div class="space-y-3">
            <div 
              v-for="(lesson, index) in lessons" 
              :key="lesson.id"
              class="flex items-center p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors cursor-pointer"
              :class="{ 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800': lesson.isCompleted }"
              @click="playLesson(lesson)"
            >
              <!-- 课时状态图标 -->
              <div class="flex-shrink-0 mr-4">
                <div v-if="lesson.isCompleted" class="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div v-else-if="isCurrentLesson(lesson)" class="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M19 10a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div v-else class="w-8 h-8 bg-gray-300 dark:bg-gray-600 rounded-full flex items-center justify-center">
                  <span class="text-sm font-medium text-gray-600 dark:text-gray-300">{{ index + 1 }}</span>
                </div>
              </div>

              <!-- 课时信息 -->
              <div class="flex-1">
                <h3 class="font-medium text-gray-900 dark:text-white mb-1">
                  第{{ index + 1 }}课: {{ lesson.title }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">
                  {{ lesson.description }}
                </p>
                <div class="flex items-center space-x-4 text-xs text-gray-500 dark:text-gray-400">
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {{ formatDuration(lesson.duration) }}
                  </span>
                  <span class="flex items-center">
                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 4V2a1 1 0 011-1h8a1 1 0 011 1v2h4a1 1 0 011 1v1a1 1 0 01-1 1H3a1 1 0 01-1-1V5a1 1 0 011-1h4z" />
                    </svg>
                    {{ getLessonTypeText(lesson.type) }}
                  </span>
                </div>
              </div>

              <!-- 课时操作 -->
              <div class="flex-shrink-0">
                <button 
                  v-if="!lesson.isCompleted"
                  size="sm" 
                  variant="outline"
                  @click.stop="markLessonComplete(lesson)"
                >
                  标记完成
                </button>
                <span v-else class="text-sm text-green-600 dark:text-green-400 font-medium">
                  已完成
                </span>
              </div>
            </div>
          </div>
        </card>
      </div>

      <!-- 侧边栏 -->
      <div class="lg:col-span-1">
        <!-- 讲师信息 -->
        <card class="mb-6">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">讲师信息</h3>
          </template>
          
          <div class="text-center">
            <div class="w-16 h-16 bg-gray-300 rounded-full mx-auto mb-4 flex items-center justify-center">
              <img 
                v-if="currentCourse.instructor.avatar" 
                :src="currentCourse.instructor.avatar" 
                :alt="currentCourse.instructor.name"
                class="w-full h-full rounded-full object-cover"
              />
              <span v-else class="text-xl font-medium text-gray-600">
                {{ currentCourse.instructor.name.charAt(0) }}
              </span>
            </div>
            <h4 class="font-medium text-gray-900 dark:text-white mb-2">
              {{ currentCourse.instructor.name }}
            </h4>
            <p class="text-sm text-gray-600 dark:text-gray-400">
              专业讲师
            </p>
          </div>
        </card>

        <!-- 学习统计 -->
        <card class="mb-6">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习统计</h3>
          </template>
          
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">完成进度</span>
              <span class="font-medium text-gray-900 dark:text-white">{{ Math.round(currentCourse.progress) }}%</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">已完成课时</span>
              <span class="font-medium text-gray-900 dark:text-white">{{ currentCourse.completedLessons }}/{{ currentCourse.totalLessons }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-600 dark:text-gray-400">总时长</span>
              <span class="font-medium text-gray-900 dark:text-white">{{ formatDuration(currentCourse.duration) }}</span>
            </div>
          </div>
        </card>

        <!-- 操作按钮 -->
        <div class="space-y-3">
          <button 
            v-if="!currentCourse.isEnrolled"
            class="w-full"
            @click="enrollCourse"
            :loading="enrolling"
          >
            报名课程
          </button>
          <button 
            v-else-if="nextLesson"
            class="w-full"
            @click="continueStudy"
          >
            继续学习
          </button>
          <button 
            v-else-if="currentCourse.progress === 100"
            class="w-full"
            variant="secondary"
            disabled
          >
            课程已完成
          </button>
          <button 
            v-else
            class="w-full"
            @click="startStudy"
          >
            开始学习
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 课程不存在 -->
  <card v-else class="text-center py-12">
    <div class="max-w-md mx-auto">
      <svg class="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">课程不存在</h3>
      <p class="text-gray-600 dark:text-gray-400 mb-6">
        抱歉，您访问的课程不存在或已被删除。
      </p>
      <button @click="$router.push('/student/courses')">
        返回课程列表
      </button>
    </div>
  </card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCoursesStore } from '@/stores/courses'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import type { Lesson } from '@/types/course'

const route = useRoute()
const router = useRouter()
const coursesStore = useCoursesStore()
const uiStore = useUIStore()

// 响应式数据
const enrolling = ref(false)
const currentLessonIndex = ref(0)

// 计算属性
const { currentCourse, lessons, loading } = coursesStore

const nextLesson = computed(() => {
  return lessons.find((lesson: Lesson) => !lesson.isCompleted)
})

// 方法
const getLevelVariant = (level: string) => {
  const variants = {
    beginner: 'success' as const,
    intermediate: 'warning' as const,
    advanced: 'danger' as const
  }
  return variants[level as keyof typeof variants] || 'info'
}

const getLevelText = (level: string) => {
  const texts = {
    beginner: '初级',
    intermediate: '中级',
    advanced: '高级'
  }
  return texts[level as keyof typeof texts] || level
}

const getLessonTypeText = (type: string) => {
  const texts = {
    video: '视频课程',
    reading: '阅读材料',
    quiz: '练习题',
    assignment: '作业'
  }
  return texts[type as keyof typeof texts] || type
}

const formatDuration = (minutes: number) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  }
  return `${mins}分钟`
}

const isCurrentLesson = (lesson: Lesson) => {
  return lessons.indexOf(lesson) === currentLessonIndex.value
}

const playLesson = (lesson: Lesson) => {
  // 设置当前课时
  currentLessonIndex.value = lessons.indexOf(lesson)
  
  // 跳转到课时学习页面或在当前页面播放
  // 这里可以实现视频播放逻辑
  console.log('播放课时:', lesson.title)
}

const markLessonComplete = async (lesson: Lesson) => {
  try {
    if (!currentCourse) return
    await coursesStore.completeLesson(currentCourse.id, lesson.id)
    uiStore.showNotification({
      type: 'success',
      title: '课时完成',
      message: `恭喜您完成了"${lesson.title}"`
    })
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '操作失败',
      message: error.message || '标记完成失败'
    })
  }
}

const enrollCourse = async () => {
  if (!currentCourse) return
  
  enrolling.value = true
  try {
    await coursesStore.enrollCourse(currentCourse.id)
    uiStore.showNotification({
      type: 'success',
      title: '报名成功',
      message: `您已成功报名课程：${currentCourse.title}`
    })
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '报名失败',
      message: error.message || '报名失败，请稍后重试'
    })
  } finally {
    enrolling.value = false
  }
}

const continueStudy = () => {
  if (nextLesson.value) {
    playLesson(nextLesson.value)
  }
}

const startStudy = () => {
  if (lessons.length > 0) {
    playLesson(lessons[0])
  }
}

// 生命周期
onMounted(async () => {
  const courseId = route.params.id as string
  
  try {
    await Promise.all([
      coursesStore.fetchCourse(courseId),
      coursesStore.fetchLessons(courseId)
    ])
  } catch (error: any) {
    uiStore.showNotification({
      type: 'error',
      title: '加载失败',
      message: error.message || '加载课程详情失败'
    })
  }
})
</script> 