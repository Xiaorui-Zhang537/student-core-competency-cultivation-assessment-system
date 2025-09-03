<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-6xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <PageHeader :title="t('shared.help.title') || '帮助中心'" :subtitle="t('shared.help.subtitle') || '在这里找到常见问题的答案，查看使用指南，或联系技术支持获得帮助'" />
      </div>

      <!-- 搜索框 -->
      <div class="mb-8">
        <div class="max-w-2xl mx-auto relative">
          <magnifying-glass-icon class="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索帮助内容..."
            class="w-full pl-12 pr-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white"
            @input="searchHelp"
          />
        </div>
      </div>

      <!-- 快速入口 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
        <card
          v-for="item in quickAccess"
          :key="item.id"
          padding="lg"
          hoverable
          class="text-center cursor-pointer"
          @click="scrollToSection(item.target)"
        >
          <div class="flex flex-col items-center">
            <div class="w-12 h-12 rounded-lg flex items-center justify-center mb-4"
                 :class="item.iconBg">
              <component :is="item.icon" class="w-6 h-6 text-white" />
            </div>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">{{ item.title }}</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ item.description }}</p>
          </div>
        </card>
      </div>

      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧导航 -->
        <div class="lg:col-span-1">
          <card padding="lg" class="sticky top-6">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">导航</h2>
            </template>
            <nav class="space-y-2">
              <button
                v-for="section in sections"
                :key="section.id"
                @click="activeSection = section.id"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                :class="activeSection === section.id
                  ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                {{ section.title }}
              </button>
            </nav>
          </card>
        </div>

        <!-- 右侧内容 -->
        <div class="lg:col-span-3 space-y-8">
          <!-- 常见问题 -->
          <section v-if="activeSection === 'faq'" id="faq">
            <card padding="lg">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">常见问题</h2>
              </template>
              
              <div class="space-y-6">
                <div
                  v-for="category in faqCategories"
                  :key="category.id"
                  class="border-b border-gray-200 dark:border-gray-600 last:border-b-0 pb-6 last:pb-0"
                >
                  <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">{{ category.name }}</h3>
                  <div class="space-y-4">
                    <div
                      v-for="faq in category.faqs"
                      :key="faq.id"
                      class="border border-gray-200 dark:border-gray-600 rounded-lg"
                    >
                      <button
                        @click="toggleFaq(faq.id)"
                        class="w-full px-4 py-3 text-left flex justify-between items-center hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg"
                      >
                        <span class="font-medium text-gray-900 dark:text-white">{{ faq.question }}</span>
                        <chevron-down-icon
                          class="w-5 h-5 text-gray-500 transform transition-transform"
                          :class="{ 'rotate-180': expandedFaqs.includes(faq.id) }"
                        />
                      </button>
                      <div
                        v-show="expandedFaqs.includes(faq.id)"
                        class="px-4 pb-4 text-gray-600 dark:text-gray-400"
                      >
                        <div v-html="faq.answer"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </card>
          </section>

          <!-- 使用指南 -->
          <section v-if="activeSection === 'guide'" id="guide">
            <card padding="lg">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">使用指南</h2>
              </template>
              
              <div class="space-y-8">
                <div v-for="guide in userGuides" :key="guide.id">
                  <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4 flex items-center">
                    <component :is="guide.icon" class="w-5 h-5 mr-2 text-primary-600" />
                    {{ guide.title }}
                  </h3>
                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div
                      v-for="step in guide.steps"
                      :key="step.id"
                      class="border border-gray-200 dark:border-gray-600 rounded-lg p-4"
                    >
                      <div class="flex items-center mb-3">
                        <span class="w-6 h-6 bg-primary-600 text-white rounded-full flex items-center justify-center text-sm font-medium mr-3">
                          {{ step.step }}
                        </span>
                        <h4 class="font-medium text-gray-900 dark:text-white">{{ step.title }}</h4>
                      </div>
                      <p class="text-sm text-gray-600 dark:text-gray-400">{{ step.description }}</p>
                      <button
                        v-if="step.action"
                        variant="outline"
                        size="sm"
                        class="mt-3"
                        @click="step.action"
                      >
                        {{ step.actionText }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </card>
          </section>

          <!-- 视频教程 -->
          <section v-if="activeSection === 'tutorials'" id="tutorials">
            <card padding="lg">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">视频教程</h2>
              </template>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div
                  v-for="tutorial in videoTutorials"
                  :key="tutorial.id"
                  class="border border-gray-200 dark:border-gray-600 rounded-lg overflow-hidden"
                >
                  <div class="aspect-video bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
                    <play-icon class="w-12 h-12 text-gray-400" />
                  </div>
                  <div class="p-4">
                    <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ tutorial.title }}</h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ tutorial.description }}</p>
                    <div class="flex items-center justify-between">
                      <span class="text-xs text-gray-500">{{ tutorial.duration }}</span>
                      <button variant="outline" size="sm" @click="playTutorial(tutorial.id)">
                        观看
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </card>
          </section>

          <!-- 技术支持 -->
          <section v-if="activeSection === 'support'" id="support">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- 联系方式 -->
              <card padding="lg">
                <template #header>
                  <h2 class="text-xl font-semibold text-gray-900 dark:text-white">联系技术支持</h2>
                </template>
                
                <div class="space-y-6">
                  <div v-for="contact in supportContacts" :key="contact.type">
                    <div class="flex items-start space-x-3">
                      <div class="flex-shrink-0">
                        <div class="w-10 h-10 rounded-lg flex items-center justify-center"
                             :class="contact.iconBg">
                          <component :is="contact.icon" class="w-5 h-5 text-white" />
                        </div>
                      </div>
                      <div class="flex-1">
                        <h3 class="font-medium text-gray-900 dark:text-white">{{ contact.title }}</h3>
                        <p class="text-sm text-gray-600 dark:text-gray-400 mb-2">{{ contact.description }}</p>
                        <div class="text-sm">
                          <p class="text-gray-900 dark:text-white font-medium">{{ contact.value }}</p>
                          <p class="text-gray-500">{{ contact.time }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </card>

              <!-- 提交工单 -->
              <card padding="lg">
                <template #header>
                  <h2 class="text-xl font-semibold text-gray-900 dark:text-white">提交技术工单</h2>
                </template>
                
                <form @submit.prevent="submitTicket" class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      问题类型
                    </label>
                    <select v-model="ticketForm.type" class="input">
                      <option value="">选择问题类型</option>
                      <option value="technical">技术问题</option>
                      <option value="account">账户问题</option>
                      <option value="feature">功能建议</option>
                      <option value="bug">错误报告</option>
                      <option value="other">其他</option>
                    </select>
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      问题标题
                    </label>
                    <input
                      v-model="ticketForm.title"
                      type="text"
                      placeholder="简要描述您的问题"
                      class="input"
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      问题详情
                    </label>
                    <textarea
                      v-model="ticketForm.description"
                      rows="4"
                      placeholder="请详细描述您遇到的问题..."
                      class="input"
                    ></textarea>
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      优先级
                    </label>
                    <select v-model="ticketForm.priority" class="input">
                      <option value="low">低</option>
                      <option value="medium">中</option>
                      <option value="high">高</option>
                      <option value="urgent">紧急</option>
                    </select>
                  </div>
                  
                  <button
                    type="submit"
                    variant="primary"
                    class="w-full"
                    :loading="isSubmittingTicket"
                  >
                    提交工单
                  </button>
                </form>
              </card>
            </div>
          </section>

          <!-- 系统状态 -->
          <section v-if="activeSection === 'status'" id="status">
            <card padding="lg">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">系统状态</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 总体状态 -->
                <div class="flex items-center justify-between p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                  <div class="flex items-center space-x-3">
                    <div class="w-3 h-3 bg-green-500 rounded-full"></div>
                    <span class="font-medium text-green-800 dark:text-green-200">所有系统运行正常</span>
                  </div>
                  <span class="text-sm text-green-600 dark:text-green-400">最后检查: 2分钟前</span>
                </div>

                <!-- 各系统状态 -->
                <div class="space-y-4">
                  <div v-for="service in systemServices" :key="service.name" 
                       class="flex items-center justify-between p-4 border border-gray-200 dark:border-gray-600 rounded-lg">
                    <div class="flex items-center space-x-3">
                      <div class="w-3 h-3 rounded-full"
                           :class="service.status === 'operational' ? 'bg-green-500' : 
                                   service.status === 'degraded' ? 'bg-yellow-500' : 'bg-red-500'">
                      </div>
                      <span class="font-medium text-gray-900 dark:text-white">{{ service.name }}</span>
                    </div>
                    <div class="text-right">
                      <div class="text-sm text-gray-900 dark:text-white">
                        {{ getStatusText(service.status) }}
                      </div>
                      <div class="text-xs text-gray-500">响应时间: {{ service.responseTime }}ms</div>
                    </div>
                  </div>
                </div>

                <!-- 维护通知 -->
                <div v-if="maintenanceNotices.length > 0" class="space-y-4">
                  <h3 class="font-medium text-gray-900 dark:text-white">维护通知</h3>
                  <div v-for="notice in maintenanceNotices" :key="notice.id"
                       class="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-700 rounded-lg">
                    <div class="flex items-start space-x-3">
                      <calendar-icon class="w-5 h-5 text-blue-600 mt-0.5" />
                      <div>
                        <h4 class="font-medium text-blue-900 dark:text-blue-100">{{ notice.title }}</h4>
                        <p class="text-sm text-blue-700 dark:text-blue-200 mt-1">{{ notice.description }}</p>
                        <p class="text-xs text-blue-600 dark:text-blue-300 mt-2">{{ notice.scheduledTime }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </card>
          </section>

          <!-- 反馈 -->
          <section v-if="activeSection === 'feedback'" id="feedback">
            <card padding="lg">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">意见反馈</h2>
              </template>
              
              <form @submit.prevent="submitFeedback" class="space-y-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    反馈类型
                  </label>
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <label
                      v-for="type in feedbackTypes"
                      :key="type.value"
                      class="flex items-center p-3 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
                      :class="{ 'border-primary-500 bg-primary-50 dark:bg-primary-900/20': feedbackForm.type === type.value }"
                    >
                      <input
                        v-model="feedbackForm.type"
                        type="radio"
                        :value="type.value"
                        class="sr-only"
                      />
                      <component :is="type.icon" class="w-5 h-5 mr-2 text-gray-600" />
                      <span class="text-sm font-medium">{{ type.label }}</span>
                    </label>
                  </div>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    反馈内容
                  </label>
                  <textarea
                    v-model="feedbackForm.content"
                    rows="6"
                    placeholder="请详细描述您的反馈意见..."
                    class="input"
                  ></textarea>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    联系方式 (可选)
                  </label>
                  <input
                    v-model="feedbackForm.contact"
                    type="text"
                    placeholder="如需回复，请留下您的邮箱或电话"
                    class="input"
                  />
                </div>

                <div class="flex items-center justify-between">
                  <label class="flex items-center">
                    <input
                      v-model="feedbackForm.anonymous"
                      type="checkbox"
                      class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    />
                    <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">匿名反馈</span>
                  </label>

                  <button
                    type="submit"
                    variant="primary"
                    :loading="isSubmittingFeedback"
                  >
                    提交反馈
                  </button>
                </div>
              </form>
            </card>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import {
  MagnifyingGlassIcon,
  QuestionMarkCircleIcon,
  BookOpenIcon,
  PlayIcon,
  PhoneIcon,
  ChatBubbleLeftIcon,
  EnvelopeIcon,
  ChevronDownIcon,
  CalendarIcon,
  ExclamationTriangleIcon,
  LightBulbIcon,
  HeartIcon,
  BugAntIcon,
  AcademicCapIcon,
  CogIcon,
  UserGroupIcon,
  ChartBarIcon
} from '@heroicons/vue/24/outline'

// Stores
const uiStore = useUIStore()

// 状态
const activeSection = ref('faq')
const searchQuery = ref('')
const expandedFaqs = ref<string[]>([])
const isSubmittingTicket = ref(false)
const isSubmittingFeedback = ref(false)

// 表单数据
const ticketForm = reactive({
  type: '',
  title: '',
  description: '',
  priority: 'medium'
})

const feedbackForm = reactive({
  type: '',
  content: '',
  contact: '',
  anonymous: false
})

// 快速入口
const quickAccess = [
  {
    id: 'faq',
    title: '常见问题',
    description: '查找常见问题的解答',
    icon: QuestionMarkCircleIcon,
    iconBg: 'bg-blue-500',
    target: 'faq'
  },
  {
    id: 'guide',
    title: '使用指南',
    description: '学习如何使用系统',
    icon: BookOpenIcon,
    iconBg: 'bg-green-500',
    target: 'guide'
  },
  {
    id: 'support',
    title: '技术支持',
    description: '联系我们获得帮助',
    icon: ChatBubbleLeftIcon,
    iconBg: 'bg-purple-500',
    target: 'support'
  },
  {
    id: 'feedback',
    title: '意见反馈',
    description: '告诉我们您的想法',
    icon: HeartIcon,
    iconBg: 'bg-pink-500',
    target: 'feedback'
  }
]

// 导航菜单
const sections = [
  { id: 'faq', title: '常见问题' },
  { id: 'guide', title: '使用指南' },
  { id: 'tutorials', title: '视频教程' },
  { id: 'support', title: '技术支持' },
  { id: 'status', title: '系统状态' },
  { id: 'feedback', title: '意见反馈' }
]

// FAQ数据
const faqCategories = [
  {
    id: 'account',
    name: '账户相关',
    faqs: [
      {
        id: 'login',
        question: '忘记密码怎么办？',
        answer: '您可以在登录页面点击"忘记密码"链接，输入您的邮箱地址，系统会发送密码重置链接到您的邮箱。请检查邮箱（包括垃圾邮件文件夹）并按照指示操作。'
      },
      {
        id: 'profile',
        question: '如何修改个人信息？',
        answer: '登录后，点击右上角的头像，选择"个人资料"，在个人资料页面您可以修改姓名、邮箱、电话等信息。修改后请点击"保存"按钮。'
      },
      {
        id: 'security',
        question: '如何确保账户安全？',
        answer: '建议您：<br/>1. 使用强密码（包含字母、数字和符号）<br/>2. 定期更换密码<br/>3. 不在公共设备上保存登录信息<br/>4. 及时退出登录'
      }
    ]
  },
  {
    id: 'courses',
    name: '课程相关',
    faqs: [
      {
        id: 'enrollment',
        question: '如何报名课程？',
        answer: '在课程列表页面找到您想要学习的课程，点击"立即学习"或"报名"按钮。如果是付费课程，需要完成付款流程。报名成功后，课程会出现在您的"我的课程"中。'
      },
      {
        id: 'progress',
        question: '学习进度如何计算？',
        answer: '学习进度根据您完成的课时数量计算。每完成一个课时，进度会自动更新。您可以在课程详情页面查看详细的学习进度和统计信息。'
      },
      {
        id: 'certificate',
        question: '如何获得课程证书？',
        answer: '完成课程所有必修内容并通过考核后，系统会自动生成课程证书。您可以在"我的证书"页面查看和下载证书。'
      }
    ]
  },
  {
    id: 'technical',
    name: '技术问题',
    faqs: [
      {
        id: 'browser',
        question: '支持哪些浏览器？',
        answer: '我们支持以下浏览器的最新版本：<br/>• Chrome (推荐)<br/>• Firefox<br/>• Safari<br/>• Edge<br/>为了获得最佳体验，建议使用Chrome浏览器。'
      },
      {
        id: 'video',
        question: '视频无法播放怎么办？',
        answer: '请尝试以下解决方案：<br/>1. 检查网络连接<br/>2. 刷新页面<br/>3. 清除浏览器缓存<br/>4. 尝试使用其他浏览器<br/>5. 检查是否安装了广告拦截插件'
      },
      {
        id: 'mobile',
        question: '支持手机端使用吗？',
        answer: '是的，我们的平台支持响应式设计，可以在手机和平板设备上正常使用。我们也提供了移动应用，您可以在应用商店搜索下载。'
      }
    ]
  }
]

// 使用指南
const userGuides = [
  {
    id: 'student',
    title: '学生使用指南',
    icon: AcademicCapIcon,
    steps: [
      {
        id: 1,
        step: 1,
        title: '注册账户',
        description: '填写基本信息完成注册',
        actionText: '立即注册',
        action: () => {}
      },
      {
        id: 2,
        step: 2,
        title: '浏览课程',
        description: '在课程库中找到感兴趣的课程',
        actionText: '浏览课程',
        action: () => {}
      },
      {
        id: 3,
        step: 3,
        title: '开始学习',
        description: '报名课程并开始学习',
        actionText: '我的课程',
        action: () => {}
      }
    ]
  },
  {
    id: 'teacher',
    title: '教师使用指南',
    icon: UserGroupIcon,
    steps: [
      {
        id: 1,
        step: 1,
        title: '创建课程',
        description: '设计课程结构和内容',
        actionText: '创建课程',
        action: () => {}
      },
      {
        id: 2,
        step: 2,
        title: '管理学生',
        description: '查看学生学习情况',
        actionText: '学生管理',
        action: () => {}
      },
      {
        id: 3,
        step: 3,
        title: '批改作业',
        description: '评分和反馈学生作业',
        actionText: '作业批改',
        action: () => {}
      }
    ]
  }
]

// 视频教程
const videoTutorials = [
  {
    id: 1,
    title: '平台基础功能介绍',
    description: '了解平台的主要功能和界面',
    duration: '5:30',
    thumbnail: ''
  },
  {
    id: 2,
    title: '如何创建和管理课程',
    description: '教师用户创建课程的完整流程',
    duration: '8:45',
    thumbnail: ''
  },
  {
    id: 3,
    title: '学生学习流程演示',
    description: '从注册到完成课程的全过程',
    duration: '6:20',
    thumbnail: ''
  },
  {
    id: 4,
    title: '作业提交和批改',
    description: '作业系统的使用方法',
    duration: '7:15',
    thumbnail: ''
  }
]

// 技术支持联系方式
const supportContacts = [
  {
    type: 'phone',
    title: '电话支持',
    description: '紧急问题请直接拨打',
    value: '400-123-4567',
    time: '工作日 9:00-18:00',
    icon: PhoneIcon,
    iconBg: 'bg-green-500'
  },
  {
    type: 'email',
    title: '邮件支持',
    description: '非紧急问题请发送邮件',
    value: 'support@platform.com',
    time: '24小时内回复',
    icon: EnvelopeIcon,
    iconBg: 'bg-blue-500'
  },
  {
    type: 'chat',
    title: '在线客服',
    description: '实时在线解答问题',
    value: '点击开启对话',
    time: '工作日 9:00-22:00',
    icon: ChatBubbleLeftIcon,
    iconBg: 'bg-purple-500'
  }
]

// 系统服务状态
const systemServices = [
  { name: '用户认证服务', status: 'operational', responseTime: 45 },
  { name: '课程管理系统', status: 'operational', responseTime: 120 },
  { name: '视频播放服务', status: 'operational', responseTime: 200 },
  { name: '文件存储服务', status: 'operational', responseTime: 80 },
  { name: '消息通知系统', status: 'operational', responseTime: 65 }
]

// 维护通知
const maintenanceNotices = [
  {
    id: 1,
    title: '系统维护通知',
    description: '我们将在本周日凌晨2:00-4:00进行系统维护，期间可能影响部分功能使用。',
    scheduledTime: '2024年1月21日 02:00 - 04:00'
  }
]

// 反馈类型
const feedbackTypes = [
  { value: 'bug', label: '错误报告', icon: BugAntIcon },
  { value: 'feature', label: '功能建议', icon: LightBulbIcon },
  { value: 'experience', label: '用户体验', icon: HeartIcon },
  { value: 'other', label: '其他反馈', icon: ChatBubbleLeftIcon }
]

// 方法
const scrollToSection = (sectionId: string) => {
  activeSection.value = sectionId
}

const searchHelp = () => {
  // 实现搜索功能
  console.log('搜索:', searchQuery.value)
}

const toggleFaq = (faqId: string) => {
  const index = expandedFaqs.value.indexOf(faqId)
  if (index > -1) {
    expandedFaqs.value.splice(index, 1)
  } else {
    expandedFaqs.value.push(faqId)
  }
}

const playTutorial = (tutorialId: number) => {
  uiStore.showNotification({
    type: 'info',
    title: '视频教程',
    message: '视频播放功能开发中...'
  })
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'operational': return '正常运行'
    case 'degraded': return '性能下降'
    case 'outage': return '服务中断'
    default: return '未知状态'
  }
}

const submitTicket = async () => {
  if (!ticketForm.type || !ticketForm.title || !ticketForm.description) {
    uiStore.showNotification({
      type: 'warning',
      title: '表单不完整',
      message: '请填写所有必填字段'
    })
    return
  }

  isSubmittingTicket.value = true
  try {
    // 模拟提交延迟
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    uiStore.showNotification({
      type: 'success',
      title: '工单提交成功',
      message: '我们会尽快处理您的问题并与您联系'
    })

    // 重置表单
    Object.assign(ticketForm, {
      type: '',
      title: '',
      description: '',
      priority: 'medium'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '提交失败',
      message: '提交工单时发生错误，请稍后重试'
    })
  } finally {
    isSubmittingTicket.value = false
  }
}

const submitFeedback = async () => {
  if (!feedbackForm.type || !feedbackForm.content) {
    uiStore.showNotification({
      type: 'warning',
      title: '表单不完整',
      message: '请选择反馈类型并填写反馈内容'
    })
    return
  }

  isSubmittingFeedback.value = true
  try {
    // 模拟提交延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    uiStore.showNotification({
      type: 'success',
      title: '反馈提交成功',
      message: '感谢您的反馈，我们会认真考虑您的建议'
    })

    // 重置表单
    Object.assign(feedbackForm, {
      type: '',
      content: '',
      contact: '',
      anonymous: false
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '提交失败',
      message: '提交反馈时发生错误，请稍后重试'
    })
  } finally {
    isSubmittingFeedback.value = false
  }
}

// 生命周期
onMounted(() => {
  // 可以根据URL参数设置默认打开的部分
})
</script> 