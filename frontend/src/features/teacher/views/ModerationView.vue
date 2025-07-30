<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">社区管理</h1>
          <p class="text-gray-600 dark:text-gray-400">管理问答社区的内容和用户</p>
        </div>
        <div class="flex items-center space-x-3">
          <button variant="outline" @click="exportReport">
            <document-arrow-down-icon class="w-4 h-4 mr-2" />
            导出报告
          </button>
          <button variant="primary" @click="openSettings">
            <cog6-tooth-icon class="w-4 h-4 mr-2" />
            社区设置
          </button>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <card padding="lg" class="text-center">
        <div class="flex items-center justify-center mb-2">
          <chat-bubble-left-right-icon class="w-8 h-8 text-blue-500" />
        </div>
        <div class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
          {{ stats.totalPosts.toLocaleString() }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">总帖子数</p>
        <div class="mt-2 text-xs text-green-600 dark:text-green-400">
          +{{ stats.todayPosts }} 今日新增
        </div>
      </card>

      <card padding="lg" class="text-center">
        <div class="flex items-center justify-center mb-2">
          <users-icon class="w-8 h-8 text-green-500" />
        </div>
        <div class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
          {{ stats.activeUsers.toLocaleString() }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">活跃用户</p>
        <div class="mt-2 text-xs text-blue-600 dark:text-blue-400">
          {{ stats.onlineUsers }} 当前在线
        </div>
      </card>

      <card padding="lg" class="text-center">
        <div class="flex items-center justify-center mb-2">
          <exclamation-triangle-icon class="w-8 h-8 text-orange-500" />
        </div>
        <div class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
          {{ stats.pendingReports }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">待处理举报</p>
        <div class="mt-2 text-xs text-red-600 dark:text-red-400">
          {{ stats.urgentReports }} 紧急处理
        </div>
      </card>

      <card padding="lg" class="text-center">
        <div class="flex items-center justify-center mb-2">
          <check-circle-icon class="w-8 h-8 text-purple-500" />
        </div>
        <div class="text-2xl font-bold text-gray-900 dark:text-white mb-1">
          {{ stats.pendingApprovals }}
        </div>
        <p class="text-sm text-gray-600 dark:text-gray-400">待审核内容</p>
        <div class="mt-2 text-xs text-purple-600 dark:text-purple-400">
          需要审核
        </div>
      </card>
    </div>

    <!-- 标签页导航 -->
    <div class="mb-6">
      <nav class="flex space-x-1 bg-white dark:bg-gray-800 rounded-lg p-1 shadow-sm">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'flex-1 px-4 py-2 text-sm font-medium rounded-md transition-colors',
            activeTab === tab.key
              ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300'
              : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-700'
          ]"
        >
          <component :is="tab.icon" class="w-4 h-4 mr-2 inline" />
          {{ tab.label }}
          <span
            v-if="tab.badge"
            class="ml-2 px-2 py-0.5 text-xs bg-red-100 dark:bg-red-900 text-red-600 dark:text-red-300 rounded-full"
          >
            {{ tab.badge }}
          </span>
        </button>
      </nav>
    </div>

    <!-- 内容管理 -->
    <div v-if="activeTab === 'content'">
      <card padding="lg" class="mb-6">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between space-y-4 md:space-y-0">
          <div class="flex items-center space-x-4">
            <div class="relative">
              <magnifying-glass-icon class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="contentFilter.search"
                type="text"
                placeholder="搜索帖子或用户..."
                class="pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
            <select v-model="contentFilter.status" class="input">
              <option value="">全部状态</option>
              <option value="published">已发布</option>
              <option value="pending">待审核</option>
              <option value="reported">被举报</option>
              <option value="hidden">已隐藏</option>
            </select>
            <select v-model="contentFilter.category" class="input">
              <option value="">全部分类</option>
              <option value="question">问题</option>
              <option value="discussion">讨论</option>
              <option value="share">分享</option>
              <option value="help">求助</option>
            </select>
          </div>
          <div class="flex items-center space-x-2">
            <button variant="outline" size="sm" @click="bulkAction('approve')" :disabled="selectedPosts.length === 0">
              <check-icon class="w-4 h-4 mr-1" />
              批量通过 ({{ selectedPosts.length }})
            </button>
            <button variant="outline" size="sm" @click="bulkAction('hide')" :disabled="selectedPosts.length === 0">
              <eye-slash-icon class="w-4 h-4 mr-1" />
              批量隐藏
            </button>
          </div>
        </div>
      </card>

      <card>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-gray-200 dark:border-gray-700">
                <th class="text-left py-3 px-4">
                  <input
                    type="checkbox"
                    @change="toggleSelectAll"
                    :checked="selectedPosts.length === filteredPosts.length && filteredPosts.length > 0"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </th>
                <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">内容</th>
                <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">作者</th>
                <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">分类</th>
                <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">状态</th>
                <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">时间</th>
                <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="post in filteredPosts"
                :key="post.id"
                class="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800"
              >
                <td class="py-3 px-4">
                  <input
                    type="checkbox"
                    :value="post.id"
                    v-model="selectedPosts"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </td>
                <td class="py-3 px-4">
                  <div class="flex items-start space-x-3">
                    <div class="flex-1 min-w-0">
                      <h4 class="text-sm font-medium text-gray-900 dark:text-white line-clamp-2">
                        {{ post.title }}
                      </h4>
                      <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-1 mt-1">
                        {{ post.content }}
                      </p>
                      <div class="flex items-center space-x-4 mt-2 text-xs text-gray-500">
                        <span>{{ post.views }} 浏览</span>
                        <span>{{ post.replies }} 回复</span>
                        <span>{{ post.likes }} 点赞</span>
                      </div>
                    </div>
                  </div>
                </td>
                <td class="py-3 px-4">
                  <div class="flex items-center space-x-2">
                    <img
                      :src="post.author.avatar"
                      :alt="post.author.name"
                      class="w-6 h-6 rounded-full"
                    />
                    <span class="text-sm text-gray-900 dark:text-white">{{ post.author.name }}</span>
                  </div>
                </td>
                <td class="py-3 px-4">
                  <badge :variant="getCategoryVariant(post.category)" size="sm">
                    {{ getCategoryText(post.category) }}
                  </badge>
                </td>
                <td class="py-3 px-4">
                  <badge :variant="getStatusVariant(post.status)" size="sm">
                    {{ getStatusText(post.status) }}
                  </badge>
                  <div v-if="post.reports > 0" class="text-xs text-red-600 dark:text-red-400 mt-1">
                    {{ post.reports }} 次举报
                  </div>
                </td>
                <td class="py-3 px-4 text-sm text-gray-600 dark:text-gray-400">
                  {{ formatDate(post.createdAt) }}
                </td>
                <td class="py-3 px-4">
                  <div class="flex items-center space-x-1">
                    <button
                      v-if="post.status === 'pending'"
                      variant="success"
                      size="xs"
                      @click="approvePost(post)"
                    >
                      <check-icon class="w-3 h-3" />
                    </button>
                    <button
                      v-if="post.status === 'published'"
                      variant="warning"
                      size="xs"
                      @click="hidePost(post)"
                    >
                      <eye-slash-icon class="w-3 h-3" />
                    </button>
                    <button
                      variant="outline"
                      size="xs"
                      @click="viewPost(post)"
                    >
                      <eye-icon class="w-3 h-3" />
                    </button>
                    <button
                      variant="outline"
                      size="xs"
                      @click="showPostMenu(post)"
                    >
                      <ellipsis-horizontal-icon class="w-3 h-3" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="flex items-center justify-between p-4 border-t border-gray-200 dark:border-gray-700">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            显示 {{ ((currentPage - 1) * pageSize) + 1 }} - {{ Math.min(currentPage * pageSize, filteredPosts.length) }} 条，共 {{ filteredPosts.length }} 条
          </div>
          <div class="flex items-center space-x-2">
            <button
              variant="outline"
              size="sm"
              @click="currentPage--"
              :disabled="currentPage === 1"
            >
              上一页
            </button>
            <span class="text-sm text-gray-600 dark:text-gray-400">
              {{ currentPage }} / {{ totalPages }}
            </span>
            <button
              variant="outline"
              size="sm"
              @click="currentPage++"
              :disabled="currentPage === totalPages"
            >
              下一页
            </button>
          </div>
        </div>
      </card>
    </div>

    <!-- 举报处理 -->
    <div v-if="activeTab === 'reports'">
      <card>
        <div class="p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">举报处理中心</h3>
          
          <div class="space-y-4">
            <div
              v-for="report in reports"
              :key="report.id"
              class="border border-gray-200 dark:border-gray-700 rounded-lg p-4"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-2">
                    <badge :variant="getReportSeverityVariant(report.severity)" size="sm">
                      {{ getReportSeverityText(report.severity) }}
                    </badge>
                    <span class="text-sm text-gray-600 dark:text-gray-400">
                      {{ report.reason }}
                    </span>
                  </div>
                  
                  <div class="bg-gray-50 dark:bg-gray-800 rounded-lg p-3 mb-3">
                    <h4 class="font-medium text-gray-900 dark:text-white mb-1">被举报内容</h4>
                    <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-3">
                      {{ report.content }}
                    </p>
                  </div>
                  
                  <div class="flex items-center space-x-4 text-sm text-gray-500">
                    <span>举报人: {{ report.reporter }}</span>
                    <span>时间: {{ formatDate(report.createdAt) }}</span>
                    <span>举报次数: {{ report.count }}</span>
                  </div>
                </div>
                
                <div class="flex items-center space-x-2 ml-4">
                  <button variant="success" size="sm" @click="resolveReport(report, 'approve')">
                    忽略举报
                  </button>
                  <button variant="warning" size="sm" @click="resolveReport(report, 'warn')">
                    警告用户
                  </button>
                  <button variant="danger" size="sm" @click="resolveReport(report, 'remove')">
                    删除内容
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </card>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'">
      <card>
        <div class="p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">用户管理</h3>
            <div class="flex items-center space-x-2">
              <input
                v-model="userSearch"
                type="text"
                placeholder="搜索用户..."
                class="px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-gray-200 dark:border-gray-700">
                  <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">用户</th>
                  <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">状态</th>
                  <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">活跃度</th>
                  <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">注册时间</th>
                  <th class="text-left py-3 px-4 font-medium text-gray-900 dark:text-white">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="user in filteredUsers"
                  :key="user.id"
                  class="border-b border-gray-200 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800"
                >
                  <td class="py-3 px-4">
                    <div class="flex items-center space-x-3">
                      <img
                        :src="user.avatar"
                        :alt="user.name"
                        class="w-8 h-8 rounded-full"
                      />
                      <div>
                        <div class="font-medium text-gray-900 dark:text-white">{{ user.name }}</div>
                        <div class="text-sm text-gray-600 dark:text-gray-400">{{ user.email }}</div>
                      </div>
                    </div>
                  </td>
                  <td class="py-3 px-4">
                    <badge :variant="getUserStatusVariant(user.status)" size="sm">
                      {{ getUserStatusText(user.status) }}
                    </badge>
                  </td>
                  <td class="py-3 px-4">
                    <div class="text-sm">
                      <div class="text-gray-900 dark:text-white">{{ user.posts }} 帖子</div>
                      <div class="text-gray-600 dark:text-gray-400">{{ user.reputation }} 声誉</div>
                    </div>
                  </td>
                  <td class="py-3 px-4 text-sm text-gray-600 dark:text-gray-400">
                    {{ formatDate(user.joinedAt) }}
                  </td>
                  <td class="py-3 px-4">
                    <div class="flex items-center space-x-1">
                      <button
                        v-if="user.status === 'active'"
                        variant="warning"
                        size="xs"
                        @click="warnUser(user)"
                      >
                        警告
                      </button>
                      <button
                        v-if="user.status !== 'banned'"
                        variant="danger"
                        size="xs"
                        @click="banUser(user)"
                      >
                        封禁
                      </button>
                      <button
                        v-if="user.status === 'banned'"
                        variant="success"
                        size="xs"
                        @click="unbanUser(user)"
                      >
                        解封
                      </button>
                      <button
                        variant="outline"
                        size="xs"
                        @click="viewUserProfile(user)"
                      >
                        查看
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </card>
    </div>

    <!-- 数据分析 -->
    <div v-if="activeTab === 'analytics'">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 社区活跃度图表 -->
        <card padding="lg">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">社区活跃度趋势</h3>
          <div ref="activityChartRef" class="h-80"></div>
        </card>

        <!-- 内容分类统计 -->
        <card padding="lg">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">内容分类统计</h3>
          <div ref="categoryChartRef" class="h-80"></div>
        </card>

        <!-- 用户行为分析 -->
        <card padding="lg">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">用户行为分析</h3>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <span class="text-gray-600 dark:text-gray-400">平均每日发帖</span>
              <span class="font-semibold text-gray-900 dark:text-white">{{ analytics.avgDailyPosts }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-gray-600 dark:text-gray-400">平均回复率</span>
              <span class="font-semibold text-gray-900 dark:text-white">{{ analytics.replyRate }}%</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-gray-600 dark:text-gray-400">用户留存率</span>
              <span class="font-semibold text-gray-900 dark:text-white">{{ analytics.retentionRate }}%</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-gray-600 dark:text-gray-400">热门话题数</span>
              <span class="font-semibold text-gray-900 dark:text-white">{{ analytics.hotTopics }}</span>
            </div>
          </div>
        </card>

        <!-- 最近活动 -->
        <card padding="lg">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">管理操作记录</h3>
          <div class="space-y-3">
            <div
              v-for="activity in recentActivities"
              :key="activity.id"
              class="flex items-start space-x-3 p-3 bg-gray-50 dark:bg-gray-800 rounded-lg"
            >
              <div class="flex-shrink-0 mt-1">
                <component
                  :is="getActivityIcon(activity.type)"
                  class="w-4 h-4 text-gray-500"
                />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm text-gray-900 dark:text-white">{{ activity.description }}</p>
                <p class="text-xs text-gray-600 dark:text-gray-400">{{ formatDate(activity.timestamp) }}</p>
              </div>
            </div>
          </div>
        </card>
      </div>
    </div>

    <!-- 设置弹窗 -->
    <div
      v-if="showSettings"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showSettings = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg w-full max-w-2xl mx-4 max-h-[90vh] overflow-hidden flex flex-col">
        <div class="p-6 border-b border-gray-200 dark:border-gray-600 flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">社区设置</h3>
          <button variant="outline" size="sm" @click="showSettings = false">
            <x-mark-icon class="w-4 h-4" />
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto flex-1">
          <div class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-900 dark:text-white mb-2">
                发帖审核模式
              </label>
              <select v-model="settings.moderationMode" class="input w-full">
                <option value="auto">自动审核</option>
                <option value="manual">人工审核</option>
                <option value="mixed">混合模式</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-900 dark:text-white mb-2">
                举报阈值
              </label>
              <input
                v-model.number="settings.reportThreshold"
                type="number"
                class="input w-full"
                min="1"
                max="10"
              />
              <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                达到此举报次数后自动隐藏内容
              </p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-900 dark:text-white mb-2">
                允许的文件类型
              </label>
              <div class="space-y-2">
                <label v-for="type in fileTypes" :key="type.value" class="flex items-center">
                  <input
                    type="checkbox"
                    :value="type.value"
                    v-model="settings.allowedFileTypes"
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ type.label }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>
        
        <div class="p-6 border-t border-gray-200 dark:border-gray-600 flex justify-end space-x-3">
          <button variant="outline" @click="showSettings = false">取消</button>
          <button variant="primary" @click="saveSettings">保存设置</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import * as echarts from 'echarts'
import {
  ChatBubbleLeftRightIcon,
  UsersIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  DocumentArrowDownIcon,
  Cog6ToothIcon,
  MagnifyingGlassIcon,
  CheckIcon,
  EyeSlashIcon,
  EyeIcon,
  EllipsisHorizontalIcon,
  XMarkIcon,
  ShieldCheckIcon,
  ChartBarIcon,
  FlagIcon
} from '@heroicons/vue/24/outline'

// Stores & Router
const router = useRouter()
const uiStore = useUIStore()

// 状态
const activeTab = ref('content')
const selectedPosts = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const userSearch = ref('')
const showSettings = ref(false)

// 图表引用
const activityChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()

// 筛选条件
const contentFilter = ref({
  search: '',
  status: '',
  category: ''
})

// 标签页配置
const tabs = computed(() => [
  {
    key: 'content',
    label: '内容管理',
    icon: ChatBubbleLeftRightIcon,
    badge: stats.value.pendingApprovals || null
  },
  {
    key: 'reports',
    label: '举报处理',
    icon: FlagIcon,
    badge: stats.value.pendingReports || null
  },
  {
    key: 'users',
    label: '用户管理',
    icon: UsersIcon,
    badge: null
  },
  {
    key: 'analytics',
    label: '数据分析',
    icon: ChartBarIcon,
    badge: null
  }
])

// 统计数据
const stats = ref({
  totalPosts: 1248,
  todayPosts: 23,
  activeUsers: 367,
  onlineUsers: 45,
  pendingReports: 8,
  urgentReports: 2,
  pendingApprovals: 12
})

// 模拟帖子数据
const posts = ref([
  {
    id: '1',
    title: '如何提高数学学习效率？',
    content: '我在学习高等数学时遇到了一些困难，希望大家能分享一些学习方法和技巧...',
    author: {
      id: '1',
      name: '张同学',
      avatar: 'https://ui-avatars.com/api/?name=张同学&background=6366f1&color=fff'
    },
    category: 'question',
    status: 'published',
    views: 245,
    replies: 12,
    likes: 18,
    reports: 0,
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    title: '分享一个很有用的学习APP',
    content: '最近发现了一个很好用的学习应用，界面简洁功能强大，推荐给大家...',
    author: {
      id: '2',
      name: '李同学',
      avatar: 'https://ui-avatars.com/api/?name=李同学&background=10b981&color=fff'
    },
    category: 'share',
    status: 'pending',
    views: 0,
    replies: 0,
    likes: 0,
    reports: 0,
    createdAt: '2024-01-15T14:20:00Z'
  },
  {
    id: '3',
    title: '这个答案有问题',
    content: '楼主的回答明显是错误的，大家不要被误导了...',
    author: {
      id: '3',
      name: '王同学',
      avatar: 'https://ui-avatars.com/api/?name=王同学&background=f59e0b&color=fff'
    },
    category: 'discussion',
    status: 'published',
    views: 89,
    replies: 5,
    likes: 3,
    reports: 3,
    createdAt: '2024-01-15T09:15:00Z'
  }
])

// 举报数据
const reports = ref([
  {
    id: '1',
    content: '这个回答包含不当内容，可能误导其他学生...',
    reason: '内容不当',
    severity: 'medium',
    reporter: '匿名用户',
    count: 3,
    createdAt: '2024-01-15T11:30:00Z'
  },
  {
    id: '2',
    content: '发布垃圾广告信息，与学习无关...',
    reason: '垃圾信息',
    severity: 'high',
    reporter: '学生A',
    count: 5,
    createdAt: '2024-01-15T13:45:00Z'
  }
])

// 用户数据
const users = ref([
  {
    id: '1',
    name: '张同学',
    email: 'zhang@example.com',
    avatar: 'https://ui-avatars.com/api/?name=张同学&background=6366f1&color=fff',
    status: 'active',
    posts: 25,
    reputation: 150,
    joinedAt: '2023-12-01T00:00:00Z'
  },
  {
    id: '2',
    name: '李同学',
    email: 'li@example.com',
    avatar: 'https://ui-avatars.com/api/?name=李同学&background=10b981&color=fff',
    status: 'warned',
    posts: 12,
    reputation: 80,
    joinedAt: '2024-01-05T00:00:00Z'
  },
  {
    id: '3',
    name: '王同学',
    email: 'wang@example.com',
    avatar: 'https://ui-avatars.com/api/?name=王同学&background=f59e0b&color=fff',
    status: 'banned',
    posts: 8,
    reputation: 20,
    joinedAt: '2024-01-10T00:00:00Z'
  }
])

// 分析数据
const analytics = ref({
  avgDailyPosts: 12.5,
  replyRate: 68,
  retentionRate: 85,
  hotTopics: 8
})

// 最近活动
const recentActivities = ref([
  {
    id: '1',
    type: 'approve',
    description: '审核通过了帖子 "如何提高学习效率"',
    timestamp: '2024-01-15T14:30:00Z'
  },
  {
    id: '2',
    type: 'ban',
    description: '封禁了用户 "垃圾用户123"',
    timestamp: '2024-01-15T13:20:00Z'
  },
  {
    id: '3',
    type: 'report',
    description: '处理了关于不当内容的举报',
    timestamp: '2024-01-15T12:10:00Z'
  }
])

// 设置
const settings = ref({
  moderationMode: 'mixed',
  reportThreshold: 3,
  allowedFileTypes: ['image', 'document']
})

const fileTypes = [
  { value: 'image', label: '图片 (JPG, PNG, GIF)' },
  { value: 'document', label: '文档 (PDF, DOC, PPT)' },
  { value: 'video', label: '视频 (MP4, AVI)' },
  { value: 'audio', label: '音频 (MP3, WAV)' }
]

// 计算属性
const filteredPosts = computed(() => {
  let filtered = posts.value

  if (contentFilter.value.search) {
    const query = contentFilter.value.search.toLowerCase()
    filtered = filtered.filter(post =>
      post.title.toLowerCase().includes(query) ||
      post.content.toLowerCase().includes(query) ||
      post.author.name.toLowerCase().includes(query)
    )
  }

  if (contentFilter.value.status) {
    filtered = filtered.filter(post => post.status === contentFilter.value.status)
  }

  if (contentFilter.value.category) {
    filtered = filtered.filter(post => post.category === contentFilter.value.category)
  }

  return filtered
})

const filteredUsers = computed(() => {
  if (!userSearch.value) return users.value

  const query = userSearch.value.toLowerCase()
  return users.value.filter(user =>
    user.name.toLowerCase().includes(query) ||
    user.email.toLowerCase().includes(query)
  )
})

const totalPages = computed(() => Math.ceil(filteredPosts.value.length / pageSize.value))

// 方法
const getCategoryVariant = (category: string) => {
  switch (category) {
    case 'question': return 'primary'
    case 'discussion': return 'secondary'
    case 'share': return 'success'
    case 'help': return 'warning'
    default: return 'secondary'
  }
}

const getCategoryText = (category: string) => {
  switch (category) {
    case 'question': return '问题'
    case 'discussion': return '讨论'
    case 'share': return '分享'
    case 'help': return '求助'
    default: return '其他'
  }
}

const getStatusVariant = (status: string) => {
  switch (status) {
    case 'published': return 'success'
    case 'pending': return 'warning'
    case 'reported': return 'danger'
    case 'hidden': return 'secondary'
    default: return 'secondary'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'published': return '已发布'
    case 'pending': return '待审核'
    case 'reported': return '被举报'
    case 'hidden': return '已隐藏'
    default: return '未知'
  }
}

const getReportSeverityVariant = (severity: string) => {
  switch (severity) {
    case 'low': return 'secondary'
    case 'medium': return 'warning'
    case 'high': return 'danger'
    default: return 'secondary'
  }
}

const getReportSeverityText = (severity: string) => {
  switch (severity) {
    case 'low': return '轻微'
    case 'medium': return '中等'
    case 'high': return '严重'
    default: return '未知'
  }
}

const getUserStatusVariant = (status: string) => {
  switch (status) {
    case 'active': return 'success'
    case 'warned': return 'warning'
    case 'banned': return 'danger'
    default: return 'secondary'
  }
}

const getUserStatusText = (status: string) => {
  switch (status) {
    case 'active': return '正常'
    case 'warned': return '已警告'
    case 'banned': return '已封禁'
    default: return '未知'
  }
}

const getActivityIcon = (type: string) => {
  switch (type) {
    case 'approve': return CheckCircleIcon
    case 'ban': return ExclamationTriangleIcon
    case 'report': return FlagIcon
    default: return ChatBubbleLeftRightIcon
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const toggleSelectAll = () => {
  if (selectedPosts.value.length === filteredPosts.value.length) {
    selectedPosts.value = []
  } else {
    selectedPosts.value = filteredPosts.value.map(post => post.id)
  }
}

const bulkAction = (action: string) => {
  uiStore.showNotification({
    type: 'success',
    title: '批量操作',
    message: `已对 ${selectedPosts.value.length} 个帖子执行 ${action} 操作`
  })
  selectedPosts.value = []
}

const approvePost = (post: any) => {
  post.status = 'published'
  uiStore.showNotification({
    type: 'success',
    title: '审核通过',
    message: `帖子 "${post.title}" 已审核通过`
  })
}

const hidePost = (post: any) => {
  post.status = 'hidden'
  uiStore.showNotification({
    type: 'success',
    title: '隐藏成功',
    message: `帖子 "${post.title}" 已隐藏`
  })
}

const viewPost = (post: any) => {
  uiStore.showNotification({
    type: 'info',
    title: '查看帖子',
    message: `正在查看帖子 "${post.title}"`
  })
}

const showPostMenu = (post: any) => {
  uiStore.showNotification({
    type: 'info',
    title: '更多操作',
    message: '帖子操作菜单功能开发中...'
  })
}

const resolveReport = (report: any, action: string) => {
  const actionText = {
    approve: '忽略举报',
    warn: '警告用户',
    remove: '删除内容'
  }[action] || '处理'

  uiStore.showNotification({
    type: 'success',
    title: '举报处理',
    message: `已${actionText}，举报已解决`
  })

  // 从列表中移除
  const index = reports.value.findIndex(r => r.id === report.id)
  if (index > -1) {
    reports.value.splice(index, 1)
    stats.value.pendingReports--
  }
}

const warnUser = (user: any) => {
  user.status = 'warned'
  uiStore.showNotification({
    type: 'warning',
    title: '用户警告',
    message: `已对用户 "${user.name}" 发出警告`
  })
}

const banUser = (user: any) => {
  user.status = 'banned'
  uiStore.showNotification({
    type: 'success',
    title: '用户封禁',
    message: `用户 "${user.name}" 已被封禁`
  })
}

const unbanUser = (user: any) => {
  user.status = 'active'
  uiStore.showNotification({
    type: 'success',
    title: '解除封禁',
    message: `用户 "${user.name}" 已解除封禁`
  })
}

const viewUserProfile = (user: any) => {
  uiStore.showNotification({
    type: 'info',
    title: '查看用户',
    message: `正在查看用户 "${user.name}" 的详细信息`
  })
}

const exportReport = () => {
  uiStore.showNotification({
    type: 'success',
    title: '导出报告',
    message: '社区管理报告正在生成，请稍候...'
  })
}

const openSettings = () => {
  showSettings.value = true
}

const saveSettings = () => {
  uiStore.showNotification({
    type: 'success',
    title: '设置保存',
    message: '社区设置已成功保存'
  })
  showSettings.value = false
}

// 初始化图表
const initCharts = async () => {
  await nextTick()

  // 活跃度趋势图表
  if (activityChartRef.value) {
    const activityChart = echarts.init(activityChartRef.value)
    const activityOption = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['发帖量', '回复量', '活跃用户']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '发帖量',
          type: 'line',
          stack: '总量',
          data: [12, 18, 15, 23, 19, 25, 21]
        },
        {
          name: '回复量',
          type: 'line',
          stack: '总量',
          data: [35, 42, 38, 51, 45, 58, 49]
        },
        {
          name: '活跃用户',
          type: 'line',
          stack: '总量',
          data: [28, 35, 31, 42, 38, 47, 41]
        }
      ]
    }
    activityChart.setOption(activityOption)
  }

  // 分类统计图表
  if (categoryChartRef.value) {
    const categoryChart = echarts.init(categoryChartRef.value)
    const categoryOption = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '内容分类',
          type: 'pie',
          radius: '50%',
          data: [
            { value: 45, name: '问题' },
            { value: 30, name: '讨论' },
            { value: 15, name: '分享' },
            { value: 10, name: '求助' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
    categoryChart.setOption(categoryOption)
  }
}

// 生命周期
onMounted(() => {
  initCharts()
})
</script>

<style scoped lang="postcss">
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 