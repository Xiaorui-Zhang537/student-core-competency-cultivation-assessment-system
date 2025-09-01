<template>
  <div class="space-y-6">
    <!-- 顶部走马灯 -->
    <section class="glass-thick glass-interactive rounded-2xl border border-gray-200/40 dark:border-gray-700/40 overflow-hidden" v-glass="{ strength: 'thick', interactive: true }">
      <div class="relative">
        <div class="overflow-hidden">
          <!-- 简化版轮播，占位三张，可后续替换为通用 Carousel 组件 -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-0">
            <div v-for="(item, idx) in banners" :key="idx" class="p-6">
              <div class="rounded-xl h-40 sm:h-48 md:h-56 w-full glass-regular flex items-center justify-center text-center px-6">
                <div>
                  <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ item.title }}</h3>
                  <p class="text-gray-600 dark:text-gray-300 mt-1">{{ item.subtitle }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能入口卡片 -->
    <section>
      <h2 class="text-xl font-bold text-gray-900 dark:text-white mb-3">{{ t('student.home.sections.quickEntries') }}</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
        <div v-for="c in cards" :key="c.key" class="glass-thick glass-interactive rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-5 flex items-start gap-4" v-glass="{ strength: 'thick', interactive: true }">
          <component :is="c.icon" class="w-6 h-6 text-primary-500 flex-shrink-0" />
          <div class="flex-1">
            <div class="flex items-center justify-between">
              <h3 class="text-base font-semibold text-gray-900 dark:text-white">{{ c.title }}</h3>
            </div>
            <p class="text-sm text-gray-600 dark:text-gray-300 mt-1">{{ c.desc }}</p>
            <div class="mt-3">
              <Button variant="primary" size="sm" @click="go(c.to)">{{ t('student.home.cards.enter') }}</Button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import Button from '@/components/ui/Button.vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import {
  HomeIcon,
  AcademicCapIcon,
  ClipboardDocumentListIcon,
  ChartBarIcon,
  SparklesIcon,
  ChatBubbleLeftRightIcon
} from '@heroicons/vue/24/outline'

const { t } = useI18n()
const router = useRouter()

const banners = [
  { title: t('student.home.carousel.1.title'), subtitle: t('student.home.carousel.1.subtitle') },
  { title: t('student.home.carousel.2.title'), subtitle: t('student.home.carousel.2.subtitle') },
  { title: t('student.home.carousel.3.title'), subtitle: t('student.home.carousel.3.subtitle') }
]

const cards = [
  { key: 'dashboard', title: t('student.home.cards.dashboard.title'), desc: t('student.home.cards.dashboard.desc'), to: '/student/dashboard', icon: HomeIcon },
  { key: 'courses', title: t('student.home.cards.courses.title'), desc: t('student.home.cards.courses.desc'), to: '/student/courses', icon: AcademicCapIcon },
  { key: 'assignments', title: t('student.home.cards.assignments.title'), desc: t('student.home.cards.assignments.desc'), to: '/student/assignments', icon: ClipboardDocumentListIcon },
  { key: 'analytics', title: t('student.home.cards.analytics.title'), desc: t('student.home.cards.analytics.desc'), to: '/student/analytics', icon: ChartBarIcon },
  { key: 'assistant', title: t('student.home.cards.assistant.title'), desc: t('student.home.cards.assistant.desc'), to: '/student/assistant', icon: SparklesIcon },
  { key: 'community', title: t('student.home.cards.community.title'), desc: t('student.home.cards.community.desc'), to: '/student/community', icon: ChatBubbleLeftRightIcon }
]

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
</style>


