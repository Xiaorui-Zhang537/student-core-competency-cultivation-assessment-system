<template>
  <div class="min-h-screen relative">
    <div class="relative z-10 max-w-6xl mx-auto p-6">
      <!-- 页面标题 -->
      <div class="mb-8">
        <page-header :title="t('shared.help.title')" :subtitle="t('shared.help.subtitle')" />
      </div>

      

      <!-- 顶部指标（StartCard 四卡） -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-4">
        <div class="cursor-pointer" @click="activeSection = 'articles'">
          <StartCard :label="t('shared.help.cards.categories') || '分类总数'"
                     :value="categoriesCount"
                     tone="blue"
                     :icon="BookOpenIcon" />
            </div>
        <div class="cursor-pointer" @click="activeSection = 'articles'">
          <StartCard :label="t('shared.help.cards.hotArticles') || '热门文章'"
                     :value="hotArticlesCount"
                     tone="emerald"
                     :icon="ChartBarIcon" />
          </div>
        <div class="cursor-pointer" @click="activeSection = 'support'">
          <StartCard :label="t('shared.help.cards.myTickets') || '我的工单'"
                     :value="myTicketsCount"
                     tone="violet"
                     :icon="ChatBubbleLeftIcon" />
        </div>
        <div class="cursor-pointer" @click="activeSection = 'articles'">
          <StartCard :label="t('shared.help.cards.new7d') || '近7日新增'"
                     :value="newArticles7dCount"
                     tone="amber"
                     :icon="LightBulbIcon" />
        </div>
      </div>

      

      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧导航 -->
        <div class="lg:col-span-1">
          <card padding="lg" tint="secondary" class="sticky top-6 pt-2">
            <template #header>
              <h2 class="text-lg font-semibold text-base-content">{{ t('shared.help.nav') || '导航' }}</h2>
            </template>
            <nav class="space-y-2 pb-4">
              <Button
                v-for="section in sections"
                :key="section.id"
                @click="activeSection = section.id"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                :class="activeSection === section.id
                  ? 'bg-emerald-500/15 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-200 ring-1 ring-emerald-500/20'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                {{ section.title }}
              </Button>
              <Button
                @click="router.push('/docs')"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                {{ t('shared.help.sections.docs') || '文档' }}
              </Button>
            </nav>
          </card>
        </div>

        <!-- 右侧内容 -->
        <div class="lg:col-span-3 space-y-8">
          <!-- 文章列表 -->
          <section v-if="activeSection === 'articles'" id="articles">
            <card padding="lg" tint="secondary">
              <template #header>
                <div class="flex items-center justify-between gap-3 flex-wrap">
                  <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.articles') || '帮助文章' }}</h2>
                  <div class="flex items-center gap-2">
                    <Button size="sm" :variant="sortMode==='latest' ? 'primary' : 'outline'" @click="changeSort('latest')">{{ t('shared.common.latest') || '最新' }}</Button>
                    <Button size="sm" :variant="sortMode==='hot' ? 'primary' : 'outline'" @click="changeSort('hot')">{{ t('shared.common.hot') || '最热' }}</Button>
                  </div>
                </div>
              </template>

              <!-- 分类筛选 -->
              <div class="flex flex-wrap gap-2 mb-4">
                <Button
                  size="sm"
                  :variant="selectedCategoryId===null ? 'primary' : 'outline'"
                  @click="pickCategory(null)">
                  {{ t('shared.common.all') || '全部' }}
                </Button>
                <Button
                  v-for="c in helpStore.categories"
                  :key="c.id"
                  size="sm"
                  :variant="selectedCategoryId===c.id ? 'primary' : 'outline'"
                  @click="pickCategory(c.id)">
                  {{ c.name }}
                </Button>
              </div>

              <!-- 文章卡片列表 -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <card v-for="a in helpStore.articles" :key="a.id" padding="lg" hoverable class="cursor-pointer" @click="openArticle(a.slug)">
                  <h3 class="font-medium text-base-content mb-2">{{ a.title }}</h3>
                  <p class="text-sm text-subtle line-clamp-3" v-if="a.contentMd">{{ a.contentMd.slice(0, 120) }}...</p>
                  <div class="mt-3 text-xs text-subtle flex items-center justify-between">
                    <span>{{ (a.views || 0) }} {{ t('shared.common.views') || '浏览' }}</span>
                    <span>{{ (a.upVotes || 0) - (a.downVotes || 0) }} {{ t('shared.common.score') || '评分' }}</span>
                  </div>
                </card>
              </div>

              <div v-if="!helpStore.loading && helpStore.articles.length===0" class="text-sm text-subtle py-6 text-center">
                {{ t('shared.common.empty') || '暂无内容' }}
              </div>
            </card>
          </section>

          <!-- 文章详情 -->
          <section v-if="activeSection === 'articleDetail' && helpStore.article" id="articleDetail">
            <card padding="lg" tint="secondary">
              <template #header>
                <div class="flex items-center gap-3 flex-wrap">
                  <h2 class="text-xl font-semibold text-base-content">{{ helpStore.article?.title }}</h2>
                  <div class="inline-flex items-center gap-2 flex-nowrap whitespace-nowrap">
                    <Button size="sm" variant="outline" @click="activeSection='articles'">{{ t('shared.common.back') || '返回' }}</Button>
                    <Button size="sm" :loading="isVoting" @click="voteArticle(true)">{{ t('shared.help.actions.useful') || '有帮助' }}</Button>
                    <Button size="sm" variant="outline" :loading="isVoting" @click="voteArticle(false)">{{ t('shared.help.actions.notUseful') || '没帮助' }}</Button>
                  </div>
                </div>
              </template>

              <div class="prose dark:prose-invert max-w-none" v-html="helpStore.article?.contentHtml || ''"></div>
            </card>
          </section>
          <!-- 常见问题（daisyUI Accordion with plus/minus） -->
          <section v-if="activeSection === 'faq'" id="faq">
            <card padding="lg" tint="secondary">
              <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.faq') || '常见问题' }}</h2>
              </template>
              
              <div class="space-y-8">
                <div
                  v-for="category in faqCategories"
                  :key="category.id"
                  class="last:pb-0"
                >
                  <h3 class="text-lg font-medium text-base-content mb-4">{{ category.name }}</h3>
                  <div class="join join-vertical w-full rounded-2xl overflow-hidden border border-base-300">
                    <div
                      v-for="(faq, idx) in category.faqs"
                      :key="faq.id"
                      class="collapse collapse-plus join-item bg-base-100 border-base-300 border"
                    >
                      <input type="radio" :name="`faq-accordion-${category.id}`" :checked="idx === 0" />
                      <div class="collapse-title font-medium text-base-content">{{ faq.question }}</div>
                      <div class="collapse-content text-sm text-subtle">
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
            <card padding="lg" tint="secondary">
              <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.guide') || '使用指南' }}</h2>
              </template>
              
              <div class="space-y-8">
                <div v-for="guide in userGuidesFiltered" :key="guide.id">
                  <h3 class="text-lg font-medium text-base-content mb-4 flex items-center">
                    <component :is="guide.icon" class="w-5 h-5 mr-2 text-emerald-600 dark:text-emerald-400" />
                    {{ guide.title }}
                  </h3>
                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div
                      v-for="step in guide.steps"
                      :key="step.id"
                      class="border border-gray-200 dark:border-gray-600 rounded-lg p-4"
                    >
                      <div class="flex items-center mb-3">
                        <span class="w-6 h-6 bg-emerald-600 dark:bg-emerald-500 text-white rounded-full flex items-center justify-center text-sm font-medium mr-3">
                          {{ step.step }}
                        </span>
                        <h4 class="font-medium text-base-content">{{ step.title }}</h4>
                      </div>
                      <p class="text-sm text-subtle">{{ step.description }}</p>
                      <Button
                        v-if="step.action"
                        variant="outline"
                        size="sm"
                        class="mt-3"
                        @click="step.action"
                      >
                        {{ step.actionText }}
                      </Button>
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
              <card padding="lg" tint="secondary">
                <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.support') || '联系技术支持' }}</h2>
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
                        <h3 class="font-medium text-base-content">{{ contact.title }}</h3>
                        <p class="text-sm text-subtle mb-2">{{ contact.description }}</p>
                        <div class="text-sm">
                          <p class="text-base-content font-medium">{{ contact.value }}</p>
                          <p class="text-subtle">{{ contact.time }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </card>

              <!-- 提交工单 -->
              <card padding="lg" tint="secondary">
                <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.ticket') || '提交技术工单' }}</h2>
                </template>
                
                <form @submit.prevent="submitTicket" class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.type') || '问题类型' }}</label>
                    <glass-popover-select
                      v-model="ticketForm.type as any"
                      :options="[
                        { label: t('shared.help.form.selectType') || '选择问题类型', value: '', disabled: true },
                        { label: t('shared.help.form.typeTechnical') || '技术问题', value: 'technical' },
                        { label: t('shared.help.form.typeAccount') || '账户问题', value: 'account' },
                        { label: t('shared.help.form.typeFeature') || '功能建议', value: 'feature' },
                        { label: t('shared.help.form.typeBug') || '错误报告', value: 'bug' },
                        { label: t('shared.help.form.typeOther') || '其他', value: 'other' }
                      ]"
                      :placeholder="t('shared.help.form.selectType') || '选择问题类型'"
                      stacked
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.title') || '问题标题' }}</label>
                    <input
                      v-model="ticketForm.title"
                      type="text"
                      :placeholder="t('shared.help.form.titlePh') || '简要描述您的问题'"
                      class="input"
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.description') || '问题详情' }}</label>
                    <textarea
                      v-model="ticketForm.description"
                      rows="4"
                      :placeholder="t('shared.help.form.descriptionPh') || '请详细描述您遇到的问题...'"
                      class="input"
                    ></textarea>
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.priority') || '优先级' }}</label>
                    <glass-popover-select
                      v-model="ticketForm.priority as any"
                      :options="[
                        { label: t('shared.help.form.priLow') || '低', value: 'low' },
                        { label: t('shared.help.form.priMedium') || '中', value: 'medium' },
                        { label: t('shared.help.form.priHigh') || '高', value: 'high' },
                        { label: t('shared.help.form.priUrgent') || '紧急', value: 'urgent' }
                      ]"
                      stacked
                    />
                  </div>
                  
                  <Button
                    type="submit"
                    variant="primary"
                    class="w-full"
                    :loading="isSubmittingTicket"
                  >
                    {{ t('shared.help.actions.submitTicket') || '提交工单' }}
                  </Button>
                </form>
              </card>
              
              <!-- 我的工单（仅登录后显示） -->
              <card v-if="authStore.isAuthenticated" padding="lg" tint="secondary" class="md:col-span-2">
                <template #header>
                  <div class="flex items-center justify-between">
                    <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.myTickets') || '我的工单' }}</h2>
                    <Button size="sm" variant="outline" @click="helpStore.fetchMyTickets()">{{ t('shared.common.latest') || '最新' }}</Button>
                  </div>
                </template>
                <div v-if="helpStore.tickets.length === 0" class="text-sm text-subtle">
                  {{ t('shared.common.empty') || '暂无内容' }}
                </div>
                <div v-else class="divide-y divide-gray-200/60 dark:divide-gray-700/60">
                  <div v-for="ticket in helpStore.tickets" :key="ticket.id" class="py-3 flex items-start justify-between">
                    <div class="pr-3 w-full">
                      <div class="font-medium text-base-content">{{ ticket.title }}</div>
                      <div class="text-xs text-subtle mt-1">#{{ ticket.id }} · {{ ticket.createdAt }}</div>
                    </div>
                    <div class="inline-flex items-center gap-2 flex-nowrap whitespace-nowrap">
                      <Button size="sm" variant="outline" class="whitespace-nowrap" @click="editTicket(ticket)">{{ t('shared.common.edit') || '编辑' }}</Button>
                      <Button size="sm" variant="outline" class="whitespace-nowrap" @click="deleteTicket(ticket.id)">{{ t('shared.common.delete') || '删除' }}</Button>
                    </div>
                  </div>
                </div>
              </card>
            </div>
          </section>

          

          <!-- 反馈 -->
          <section v-if="activeSection === 'feedback'" id="feedback">
            <card padding="lg" tint="secondary">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ t('shared.help.sections.feedback') || '意见反馈' }}</h2>
              </template>
              
              <form @submit.prevent="submitFeedback" class="space-y-6">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    反馈类型
                  </label>
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <label
                      v-for="type in feedbackTypeOptions"
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
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.feedback') || '反馈内容' }}</label>
                  <textarea
                    v-model="feedbackForm.content"
                    rows="6"
                    :placeholder="t('shared.help.form.feedbackPh') || '请详细描述您的反馈意见...'"
                    class="input"
                  ></textarea>
                </div>

                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.contact') || '联系方式 (可选)' }}</label>
                  <input
                    v-model="feedbackForm.contact"
                    type="text"
                    :placeholder="t('shared.help.form.contactPh') || '如需回复，请留下您的邮箱或电话'"
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
                    <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ t('shared.help.form.anonymous') || '匿名反馈' }}</span>
                  </label>

                  <Button
                    type="submit"
                    variant="primary"
                    :loading="isSubmittingFeedback"
                  >
                    {{ t('shared.help.actions.submitFeedback') || '提交反馈' }}
                  </Button>
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StartCard from '@/components/ui/StartCard.vue'
// 动态背景已移除
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { useHelpStore } from '@/stores/help'
import { useAuthStore } from '@/stores/auth'
import GlassSelect from '@/components/ui/filters/GlassSelect.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { useRouter, useRoute } from 'vue-router'
import {
  MagnifyingGlassIcon,
  QuestionMarkCircleIcon,
  BookOpenIcon,
  PhoneIcon,
  ChatBubbleLeftIcon,
  EnvelopeIcon,
  ChevronDownIcon,
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
const helpStore = useHelpStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

// 状态
const activeSection = ref('faq')
const searchQuery = ref('')
const expandedFaqs = ref<string[]>([])
const isSubmittingTicket = ref(false)
const isSubmittingFeedback = ref(false)
const selectedCategoryId = ref<number | null>(null)
const sortMode = ref<'latest' | 'hot'>('hot')
const isVoting = ref(false)

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

// 顶部 StartCard 统计
const categoriesCount = computed(() => (helpStore.categories?.length || 0))
const hotArticlesCount = computed(() => (helpStore.articles?.length || 0))
const myTicketsCount = computed(() => (authStore.isAuthenticated ? (helpStore.tickets?.length || 0) : 0))
const newArticles7dCount = computed(() => {
  const list = helpStore.articles || []
  const now = Date.now()
  const sevenDays = 7 * 24 * 60 * 60 * 1000
  let cnt = 0
  for (const a of list) {
    const ts = a?.createdAt ? Date.parse(String(a.createdAt)) : NaN
    if (!isNaN(ts) && (now - ts) <= sevenDays) cnt++
  }
  return cnt
})

// 导航菜单（随语言切换动态更新）
const sections = computed(() => ([
  { id: 'faq', title: t('shared.help.sections.faq') || '常见问题' },
  { id: 'articles', title: t('shared.help.sections.articles') || '帮助文章' },
  { id: 'guide', title: t('shared.help.sections.guide') || '使用指南' },
  { id: 'support', title: t('shared.help.sections.support') || '技术支持' },
  { id: 'feedback', title: t('shared.help.sections.feedback') || '意见反馈' }
]))

// FAQ数据（i18n 驱动）
const faqCategories = computed(() => ([
  {
    id: 'account',
    name: t('shared.help.faq.account.title') || '账户相关',
    faqs: [
      {
        id: 'login',
        question: t('shared.help.faq.account.login.q') || '忘记密码怎么办？',
        answer: t('shared.help.faq.account.login.a') || '您可以在登录页面点击"忘记密码"链接…'
      },
      {
        id: 'profile',
        question: t('shared.help.faq.account.profile.q') || '如何修改个人信息？',
        answer: t('shared.help.faq.account.profile.a') || '登录后前往个人资料页面进行修改…'
      },
      {
        id: 'security',
        question: t('shared.help.faq.account.security.q') || '如何确保账户安全？',
        answer: t('shared.help.faq.account.security.a') || '建议使用强密码、定期更换、勿在公共设备保存登录信息…'
      },
      {
        id: 'support',
        question: t('shared.help.faq.account.support.q') || '如何联系技术支持？',
        answer: t('shared.help.faq.account.support.a') || '您可以通过页面的“技术支持”模块使用邮箱或微信联系。'
      }
    ]
  },
  {
    id: 'courses',
    name: t('shared.help.faq.courses.title') || '课程相关',
    faqs: [
      {
        id: 'enrollment',
        question: t('shared.help.faq.courses.enrollment.q') || '如何报名课程？',
        answer: t('shared.help.faq.courses.enrollment.a') || '在课程列表页面选择课程并报名…'
      },
      {
        id: 'progress',
        question: t('shared.help.faq.courses.progress.q') || '学习进度如何计算？',
        answer: t('shared.help.faq.courses.progress.a') || '按已完成课时统计，并在课程详情页查看…'
      },
      {
        id: 'submit',
        question: t('shared.help.faq.courses.submit.q') || '如何提交作业？',
        answer: t('shared.help.faq.courses.submit.a') || '前往“作业”页面，在待提交列表中进入作业详情，按要求填写/上传后提交。'
      }
    ]
  },
  {
    id: 'technical',
    name: t('shared.help.faq.technical.title') || '技术问题',
    faqs: [
      {
        id: 'browser',
        question: t('shared.help.faq.technical.browser.q') || '支持哪些浏览器？',
        answer: t('shared.help.faq.technical.browser.a') || '为获得最完整体验，请使用 Chrome 最新版本。其他浏览器也可访问，但体验可能受限。'
      },
      {
        id: 'cache',
        question: t('shared.help.faq.technical.cache.q') || '遇到异常如何清除缓存？',
        answer: t('shared.help.faq.technical.cache.a') || '在浏览器设置中清除站点缓存与 Cookie，然后重新登录再试。'
      },
      {
        id: 'uploads',
        question: t('shared.help.faq.technical.uploads.q') || '上传失败怎么办？',
        answer: t('shared.help.faq.technical.uploads.a') || '请检查文件格式与大小是否符合页面提示，确保网络稳定后重试。若仍失败，请联系技术支持。'
      }
    ]
  }
]))

// 使用指南（中英文切换、按角色展示）
const userGuides = computed(() => ([
  {
    id: 'student',
    title: t('shared.help.guide.student.title') || '学生使用指南',
    icon: AcademicCapIcon,
    steps: [
      {
        id: 1,
        step: 1,
        title: t('shared.help.guide.student.steps.browseCourses.title') || '浏览课程',
        description: t('shared.help.guide.student.steps.browseCourses.desc') || '在课程库中检索并预览适合你的课程。',
        actionText: t('shared.help.guide.student.steps.browseCourses.action') || '去浏览',
        action: () => router.push({ name: 'StudentCourses' })
      },
      {
        id: 2,
        step: 2,
        title: t('shared.help.guide.student.steps.submitAssignment.title') || '提交作业',
        description: t('shared.help.guide.student.steps.submitAssignment.desc') || '在作业中心查看待交作业并按要求上传提交。',
        actionText: t('shared.help.guide.student.steps.submitAssignment.action') || '去提交',
        action: () => router.push({ name: 'StudentAssignments' })
      },
      {
        id: 3,
        step: 3,
        title: t('shared.help.guide.student.steps.viewGrades.title') || '浏览成绩',
        description: t('shared.help.guide.student.steps.viewGrades.desc') || '在成绩中心查看每门课程的成绩与评语。',
        actionText: t('shared.help.guide.student.steps.viewGrades.action') || '去查看',
        action: () => router.push({ name: 'StudentAnalysis' })
      },
      {
        id: 4,
        step: 4,
        title: t('shared.help.guide.student.steps.askAI.title') || '询问AI',
        description: t('shared.help.guide.student.steps.askAI.desc') || '打开学习助手，针对课程/作业提出问题并获得建议。',
        actionText: t('shared.help.guide.student.steps.askAI.action') || '去提问',
        action: () => router.push({ name: 'StudentAssistant' })
      },
      {
        id: 5,
        step: 5,
        title: t('shared.help.guide.student.steps.communityQA.title') || '社区答疑',
        description: t('shared.help.guide.student.steps.communityQA.desc') || '在学习社区向同学与老师求助，互相解答问题。',
        actionText: t('shared.help.guide.student.steps.communityQA.action') || '去交流',
        action: () => router.push({ name: 'StudentCommunity' })
      }
    ]
  },
  {
    id: 'teacher',
    title: t('shared.help.guide.teacher.title') || '教师使用指南',
    icon: UserGroupIcon,
    steps: [
      {
        id: 1,
        step: 1,
        title: t('shared.help.guide.teacher.steps.createCourse.title') || '创建课程',
        description: t('shared.help.guide.teacher.steps.createCourse.desc') || '在课程管理中创建并完善课程结构与内容。',
        actionText: t('shared.help.guide.teacher.steps.createCourse.action') || '去创建',
        action: () => router.push({ name: 'TeacherCourseManagement', query: { create: '1' } })
      },
      {
        id: 2,
        step: 2,
        title: t('shared.help.guide.teacher.steps.manageStudents.title') || '管理学生',
        description: t('shared.help.guide.teacher.steps.manageStudents.desc') || '查看学生学习情况，邀请/移除学生并导出数据。',
        actionText: t('shared.help.guide.teacher.steps.manageStudents.action') || '去管理',
        action: async () => {
          try {
            if (authStore.userRole === 'TEACHER') {
              const { useCourseStore } = await import('@/stores/course')
              const cs = useCourseStore()
              if (!Array.isArray(cs.courses) || cs.courses.length === 0) {
                const teacherId = authStore.user?.id
                if (teacherId) {
                  await cs.fetchCourses({ page: 1, size: 10, teacherId: String(teacherId) })
                }
              }
              const first = Array.isArray(cs.courses) ? cs.courses[0] : null
              if (first && (first as any).id) {
                return router.push({ name: 'TeacherCourseStudents', params: { id: (first as any).id } })
              }
            }
          } catch {}
          return router.push({ name: 'TeacherCourseManagement' })
        }
      },
      {
        id: 3,
        step: 3,
        title: t('shared.help.guide.teacher.steps.gradeAssignments.title') || '批改作业',
        description: t('shared.help.guide.teacher.steps.gradeAssignments.desc') || '在作业管理中查看提交并进行评分与反馈。',
        actionText: t('shared.help.guide.teacher.steps.gradeAssignments.action') || '去批改',
        action: () => router.push({ name: 'TeacherAssignments' })
      },
      {
        id: 4,
        step: 4,
        title: t('shared.help.guide.teacher.steps.aiGrading.title') || 'AI批改',
        description: t('shared.help.guide.teacher.steps.aiGrading.desc') || '使用 AI 辅助批改，提升批改效率与一致性。',
        actionText: t('shared.help.guide.teacher.steps.aiGrading.action') || '去体验',
        action: () => router.push({ name: 'TeacherAIGrading' })
      },
      {
        id: 5,
        step: 5,
        title: t('shared.help.guide.teacher.steps.analytics.title') || '数据分析',
        description: t('shared.help.guide.teacher.steps.analytics.desc') || '查看课程关键指标与学生表现，辅助教学决策。',
        actionText: t('shared.help.guide.teacher.steps.analytics.action') || '去分析',
        action: () => router.push({ name: 'TeacherAnalytics' })
      },
      {
        id: 6,
        step: 6,
        title: t('shared.help.guide.teacher.steps.askAI.title') || '询问AI',
        description: t('shared.help.guide.teacher.steps.askAI.desc') || '打开教学助手，撰写内容、出题与答疑辅助。',
        actionText: t('shared.help.guide.teacher.steps.askAI.action') || '去咨询',
        action: () => router.push({ name: 'TeacherAssistant' })
      },
      {
        id: 7,
        step: 7,
        title: t('shared.help.guide.teacher.steps.communityQA.title') || '社区答疑',
        description: t('shared.help.guide.teacher.steps.communityQA.desc') || '在社区与学生互动，答疑解惑并分享教学经验。',
        actionText: t('shared.help.guide.teacher.steps.communityQA.action') || '去交流',
        action: () => router.push({ name: 'TeacherCommunity' })
      }
    ]
  }
]))

// 按角色过滤显示对应指南（随语言切换自动更新）
const userGuidesFiltered = computed(() => {
  const all = userGuides.value
  const role = authStore.userRole
  if (role === 'TEACHER') return all.filter(g => g.id === 'teacher')
  return all.filter(g => g.id === 'student')
})

// 移除视频教程（无后端支持）

// 技术支持联系方式（i18n 驱动，仅邮箱 + 微信）
const supportContacts = computed(() => ([
  {
    type: 'email',
    title: t('shared.help.support.email.title') || '邮件支持',
    description: t('shared.help.support.email.desc') || '非紧急问题请发送邮件',
    value: 'xiaorui537@gmail.com',
    time: t('shared.help.support.email.time') || '24小时内回复',
    icon: EnvelopeIcon,
    iconBg: 'bg-blue-500'
  },
  {
    type: 'wechat',
    title: t('shared.help.support.wechat.title') || '微信 WeChat',
    description: t('shared.help.support.wechat.desc') || '添加微信获取支持',
    value: 'Xiao_Rui537',
    time: t('shared.help.support.wechat.time') || '工作日 9:00-18:00',
    icon: ChatBubbleLeftIcon,
    iconBg: 'bg-emerald-500'
  }
]))

// 移除系统状态（无后端支持）

// 反馈类型（随语言切换动态更新）
const feedbackTypeOptions = computed(() => ([
  { value: 'bug', label: t('shared.help.form.typeBug') || '错误报告', icon: BugAntIcon },
  { value: 'feature', label: t('shared.help.form.typeFeature') || '功能建议', icon: LightBulbIcon },
  { value: 'experience', label: t('shared.help.form.typeExperience') || '用户体验', icon: HeartIcon },
  { value: 'other', label: t('shared.help.form.typeOther') || '其他反馈', icon: ChatBubbleLeftIcon }
]))

// 方法
const scrollToSection = (sectionId: string) => {
  activeSection.value = sectionId
}
function pickCategory(id: number | null) {
  selectedCategoryId.value = id
  helpStore.searchArticles(searchQuery.value || undefined, id === null ? undefined : id, undefined, sortMode.value)
}

function changeSort(s: 'latest' | 'hot') {
  sortMode.value = s
  helpStore.searchArticles(searchQuery.value || undefined, selectedCategoryId.value === null ? undefined : selectedCategoryId.value, undefined, sortMode.value)
}

async function openArticle(slug: string) {
  await helpStore.fetchArticle(slug, true)
  activeSection.value = 'articleDetail'
}

async function voteArticle(helpful: boolean) {
  if (!helpStore.article) return
  isVoting.value = true
  try {
    await helpStore.voteOrFeedback(helpStore.article.id, { helpful })
    uiStore.showNotification({ type: 'success', title: t('shared.help.feedback.thanks') || '感谢反馈', message: '' })
  } finally {
    isVoting.value = false
  }
}

async function editTicket(ticket: any) {
  const title = prompt(t('shared.common.edit') || '编辑标题', ticket.title)
  if (title === null) return
  const desc = prompt(t('shared.common.edit') || '编辑描述', ticket.description || '')
  if (desc === null) return
  await helpStore.updateTicket(ticket.id, title, desc)
  uiStore.showNotification({ type: 'success', title: t('shared.common.saved') || '已保存', message: '' })
}

async function deleteTicket(id: number) {
  if (!confirm(t('shared.common.confirm') || '确认删除？')) return
  await helpStore.deleteTicket(id)
  uiStore.showNotification({ type: 'success', title: t('shared.common.deleted') || '已删除', message: '' })
}

const searchHelp = () => {
  helpStore.searchArticles(searchQuery.value || undefined)
}

const toggleFaq = (faqId: string) => {
  const index = expandedFaqs.value.indexOf(faqId)
  if (index > -1) {
    expandedFaqs.value.splice(index, 1)
  } else {
    expandedFaqs.value.push(faqId)
  }
}

// 移除视频/状态相关方法

const ticketStatusText = (s: string) => {
  switch (s) {
    case 'open': return t('shared.help.ticket.open') || '已创建'
    case 'in_progress': return t('shared.help.ticket.inProgress') || '处理中'
    case 'resolved': return t('shared.help.ticket.resolved') || '已解决'
    case 'closed': return t('shared.help.ticket.closed') || '已关闭'
    default: return s
  }
}

const submitTicket = async () => {
  if (!authStore.isAuthenticated) {
    uiStore.showNotification({ type: 'warning', title: t('shared.common.tip') || '提示', message: t('auth.loginRequired') || '请先登录再提交工单' })
    return router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
  }
  if (!ticketForm.type || !ticketForm.title || !ticketForm.description) {
    uiStore.showNotification({
      type: 'warning',
      title: t('shared.help.validation.incomplete') || '表单不完整',
      message: t('shared.help.validation.fillRequired') || '请填写所有必填字段'
    })
    return
  }

  isSubmittingTicket.value = true
  try {
    const composedTitle = `${ticketForm.title}`
    const composedDesc = `[${ticketForm.type.toUpperCase()} | ${ticketForm.priority}]\n${ticketForm.description}`
    await helpStore.submitTicket(composedTitle, composedDesc)
    uiStore.showNotification({
      type: 'success',
      title: t('shared.help.notify.ticketSuccessTitle') || '工单提交成功',
      message: t('shared.help.notify.ticketSuccessMsg') || '我们会尽快处理您的问题并与您联系'
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
      title: t('shared.help.notify.submitFailTitle') || '提交失败',
      message: t('shared.help.notify.ticketFailMsg') || '提交工单时发生错误，请稍后重试'
    })
  } finally {
    isSubmittingTicket.value = false
  }
}

const submitFeedback = async () => {
  if (!authStore.isAuthenticated) {
    uiStore.showNotification({ type: 'warning', title: t('shared.common.tip') || '提示', message: t('auth.loginRequired') || '请先登录再提交反馈' })
    return router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
  }
  if (!feedbackForm.type || !feedbackForm.content) {
    uiStore.showNotification({
      type: 'warning',
      title: t('shared.help.validation.incomplete') || '表单不完整',
      message: t('shared.help.validation.pickTypeAndFill') || '请选择反馈类型并填写反馈内容'
    })
    return
  }

  isSubmittingFeedback.value = true
  try {
    const title = `[Feedback] ${feedbackForm.type}`
    const desc = `${feedbackForm.content}${feedbackForm.contact ? `\nContact: ${feedbackForm.contact}` : ''}`
    await helpStore.submitTicket(title, desc)
    uiStore.showNotification({
      type: 'success',
      title: t('shared.help.notify.feedbackSuccessTitle') || '反馈提交成功',
      message: t('shared.help.notify.feedbackSuccessMsg') || '感谢您的反馈，我们会认真考虑您的建议'
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
      title: t('shared.help.notify.submitFailTitle') || '提交失败',
      message: t('shared.help.notify.feedbackFailMsg') || '提交反馈时发生错误，请稍后重试'
    })
  } finally {
    isSubmittingFeedback.value = false
  }
}

// 生命周期
onMounted(() => {
  helpStore.fetchCategories().catch(() => {})
  // 初始加载热门文章
  helpStore.searchArticles(undefined, undefined, undefined, 'hot').catch(() => {})
  if (authStore.isAuthenticated) {
    helpStore.fetchMyTickets().catch(() => {})
  }
  // 恢复分区：优先 query.section，其次 hash
  try {
    const sectionFromQuery = String((route.query as any)?.section || '').trim()
    const sectionFromHash = String((route.hash || '').replace(/^#/, '')).trim()
    const candidate = sectionFromQuery || sectionFromHash
    const allowed = new Set(['faq','articles','guide','support','feedback','articleDetail'])
    if (candidate && allowed.has(candidate)) {
      activeSection.value = candidate
    }
  } catch {}
})

// 分区变化时写回到 URL，避免主题/语言切换导致重置
watch(activeSection, (s) => {
  try {
    const q = { ...(route.query as any), section: s }
    router.replace({ query: q })
  } catch {}
})
</script> 