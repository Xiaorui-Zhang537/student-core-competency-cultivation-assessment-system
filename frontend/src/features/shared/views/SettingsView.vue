<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">系统设置</h1>
        <p class="text-gray-600 dark:text-gray-400">个性化您的学习体验和系统偏好</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧导航 -->
        <div class="lg:col-span-1">
          <card padding="sm">
            <nav class="space-y-1">
              <button
                v-for="tab in tabs"
                :key="tab.id"
                @click="activeTab = tab.id"
                class="w-full flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors"
                :class="activeTab === tab.id 
                  ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300' 
                  : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700'"
              >
                <component :is="tab.icon" class="w-4 h-4 mr-3" />
                {{ tab.name }}
              </button>
            </nav>
          </card>
        </div>

        <!-- 右侧内容 -->
        <div class="lg:col-span-3">
          <!-- 基本设置 -->
          <div v-if="activeTab === 'general'" class="space-y-6">
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">基本设置</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 语言设置 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">界面语言</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">选择您的首选界面语言</p>
                  </div>
                  <select v-model="settings.language" @change="saveSettings" class="input w-32">
                    <option value="zh-CN">中文</option>
                    <option value="en-US">English</option>
                    <option value="ja-JP">日本語</option>
                    <option value="ko-KR">한국어</option>
                  </select>
                </div>

                <!-- 时区设置 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">时区</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">选择您所在的时区</p>
                  </div>
                  <select v-model="settings.timezone" @change="saveSettings" class="input w-48">
                    <option value="Asia/Shanghai">北京时间 (UTC+8)</option>
                    <option value="Asia/Tokyo">东京时间 (UTC+9)</option>
                    <option value="America/New_York">纽约时间 (UTC-5)</option>
                    <option value="Europe/London">伦敦时间 (UTC+0)</option>
                  </select>
                </div>

                <!-- 日期格式 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">日期格式</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">选择日期显示格式</p>
                  </div>
                  <select v-model="settings.dateFormat" @change="saveSettings" class="input w-32">
                    <option value="YYYY-MM-DD">2024-01-15</option>
                    <option value="MM/DD/YYYY">01/15/2024</option>
                    <option value="DD/MM/YYYY">15/01/2024</option>
                  </select>
                </div>

                <!-- 自动保存 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">自动保存</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">自动保存学习进度和草稿</p>
                  </div>
                  <input 
                    v-model="settings.autoSave" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="saveSettings"
                  />
                </div>
              </div>
            </card>
          </div>

          <!-- 外观设置 -->
          <div v-if="activeTab === 'appearance'" class="space-y-6">
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">外观设置</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 主题设置 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">主题模式</h3>
                  <div class="grid grid-cols-3 gap-3">
                    <label 
                      v-for="theme in themeOptions"
                      :key="theme.value"
                      class="relative flex cursor-pointer rounded-lg border p-4 focus:outline-none"
                      :class="settings.theme === theme.value 
                        ? 'border-primary-600 ring-2 ring-primary-600' 
                        : 'border-gray-300 dark:border-gray-600'"
                    >
                      <input
                        v-model="settings.theme"
                        :value="theme.value"
                        type="radio"
                        name="theme"
                        class="sr-only"
                        @change="saveSettings"
                      />
                      <div class="flex flex-1 flex-col">
                        <div class="flex items-center space-x-3">
                          <component :is="theme.icon" class="w-5 h-5" />
                          <span class="block text-sm font-medium text-gray-900 dark:text-white">
                            {{ theme.name }}
                          </span>
                        </div>
                        <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
                          {{ theme.description }}
                        </p>
                      </div>
                    </label>
                  </div>
                </div>

                <!-- 字体大小 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">字体大小</h3>
                  <div class="flex items-center space-x-4">
                    <span class="text-sm text-gray-500 dark:text-gray-400">小</span>
                    <input
                      v-model="settings.fontSize"
                      type="range"
                      min="12"
                      max="20"
                      step="2"
                      class="flex-1"
                      @change="saveSettings"
                    />
                    <span class="text-sm text-gray-500 dark:text-gray-400">大</span>
                  </div>
                  <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
                    当前字体大小：{{ settings.fontSize }}px
                  </p>
                </div>

                <!-- 紧凑模式 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">紧凑模式</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">减少界面元素间距，显示更多内容</p>
                  </div>
                  <input 
                    v-model="settings.compactMode" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="saveSettings"
                  />
                </div>

                <!-- 动画效果 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">动画效果</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">启用界面过渡动画</p>
                  </div>
                  <input 
                    v-model="settings.animations" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="saveSettings"
                  />
                </div>
              </div>
            </card>
          </div>

          <!-- 通知设置 -->
          <div v-if="activeTab === 'notifications'" class="space-y-6">
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">通知设置</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 邮件通知 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-4">邮件通知</h3>
                  <div class="space-y-3">
                    <label v-for="item in emailNotifications" :key="item.key" class="flex items-center justify-between">
                      <div>
                        <span class="text-sm text-gray-700 dark:text-gray-300">{{ item.label }}</span>
                        <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.description }}</p>
                      </div>
                      <input 
                        v-model="settings.notifications.email[item.key]" 
                        type="checkbox" 
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                    </label>
                  </div>
                </div>

                <!-- 推送通知 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-4">推送通知</h3>
                  <div class="space-y-3">
                    <label v-for="item in pushNotifications" :key="item.key" class="flex items-center justify-between">
                      <div>
                        <span class="text-sm text-gray-700 dark:text-gray-300">{{ item.label }}</span>
                        <p class="text-xs text-gray-500 dark:text-gray-400">{{ item.description }}</p>
                      </div>
                      <input 
                        v-model="settings.notifications.push[item.key]" 
                        type="checkbox" 
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                    </label>
                  </div>
                </div>

                <!-- 免打扰时间 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">免打扰时间</h3>
                  <div class="flex items-center space-x-4">
                    <div>
                      <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">开始时间</label>
                      <input
                        v-model="settings.notifications.doNotDisturbStart"
                        type="time"
                        class="input input-sm"
                        @change="saveSettings"
                      />
                    </div>
                    <div>
                      <label class="block text-xs text-gray-500 dark:text-gray-400 mb-1">结束时间</label>
                      <input
                        v-model="settings.notifications.doNotDisturbEnd"
                        type="time"
                        class="input input-sm"
                        @change="saveSettings"
                      />
                    </div>
                  </div>
                  <label class="flex items-center mt-3">
                    <input 
                      v-model="settings.notifications.doNotDisturbEnabled" 
                      type="checkbox" 
                      class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      @change="saveSettings"
                    />
                    <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">启用免打扰模式</span>
                  </label>
                </div>
              </div>
            </card>
          </div>

          <!-- 学习设置 -->
          <div v-if="activeTab === 'learning'" class="space-y-6">
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">学习偏好</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 学习目标 -->
                <div>
                  <label class="block text-sm font-medium text-gray-900 dark:text-white mb-2">
                    每日学习目标（分钟）
                  </label>
                  <input
                    v-model.number="settings.learning.dailyGoal"
                    type="number"
                    min="15"
                    max="480"
                    step="15"
                    class="input w-32"
                    @change="saveSettings"
                  />
                  <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    设置您的每日学习时间目标
                  </p>
                </div>

                <!-- 学习提醒 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">学习提醒</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">在设定时间提醒您学习</p>
                  </div>
                  <input 
                    v-model="settings.learning.reminders" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="saveSettings"
                  />
                </div>

                <!-- 提醒时间 -->
                <div v-if="settings.learning.reminders">
                  <h4 class="text-sm font-medium text-gray-900 dark:text-white mb-3">提醒时间</h4>
                  <div class="grid grid-cols-2 gap-4">
                    <div v-for="(time, index) in settings.learning.reminderTimes" :key="index">
                      <input
                        v-model="time.time"
                        type="time"
                        class="input input-sm"
                        @change="saveSettings"
                      />
                      <button
                        @click="removeReminderTime(index)"
                        class="ml-2 text-red-500 hover:text-red-700"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                  <button variant="outline" size="sm" @click="addReminderTime" class="mt-2">
                    添加提醒时间
                  </button>
                </div>

                <!-- 自动暂停 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">学习休息提醒</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">长时间学习后提醒休息</p>
                  </div>
                  <input 
                    v-model="settings.learning.breakReminder" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="saveSettings"
                  />
                </div>

                <!-- 休息间隔 -->
                <div v-if="settings.learning.breakReminder" class="flex items-center space-x-4">
                  <label class="text-sm text-gray-700 dark:text-gray-300">每</label>
                  <input
                    v-model.number="settings.learning.breakInterval"
                    type="number"
                    min="30"
                    max="120"
                    step="15"
                    class="input w-20"
                    @change="saveSettings"
                  />
                  <label class="text-sm text-gray-700 dark:text-gray-300">分钟提醒休息</label>
                </div>

                <!-- 学习模式 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">默认学习模式</h3>
                  <div class="space-y-2">
                    <label v-for="mode in learningModes" :key="mode.value" class="flex items-center">
                      <input
                        v-model="settings.learning.defaultMode"
                        :value="mode.value"
                        type="radio"
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                      <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ mode.label }}</span>
                    </label>
                  </div>
                </div>
              </div>
            </card>
          </div>

          <!-- 隐私设置 -->
          <div v-if="activeTab === 'privacy'" class="space-y-6">
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">隐私与安全</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 数据收集 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-4">数据收集设置</h3>
                  <div class="space-y-3">
                    <label class="flex items-center justify-between">
                      <div>
                        <span class="text-sm text-gray-700 dark:text-gray-300">分析数据</span>
                        <p class="text-xs text-gray-500 dark:text-gray-400">帮助改善产品体验</p>
                      </div>
                      <input 
                        v-model="settings.privacy.analytics" 
                        type="checkbox" 
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                    </label>
                    <label class="flex items-center justify-between">
                      <div>
                        <span class="text-sm text-gray-700 dark:text-gray-300">错误报告</span>
                        <p class="text-xs text-gray-500 dark:text-gray-400">自动发送错误信息</p>
                      </div>
                      <input 
                        v-model="settings.privacy.errorReporting" 
                        type="checkbox" 
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                    </label>
                  </div>
                </div>

                <!-- 学习记录 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">学习记录可见性</h3>
                  <div class="space-y-2">
                    <label v-for="option in privacyOptions" :key="option.value" class="flex items-center">
                      <input
                        v-model="settings.privacy.learningVisibility"
                        :value="option.value"
                        type="radio"
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                      <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">{{ option.label }}</span>
                    </label>
                  </div>
                </div>

                <!-- 在线状态 -->
                <div class="flex items-center justify-between">
                  <div>
                    <h3 class="text-sm font-medium text-gray-900 dark:text-white">显示在线状态</h3>
                    <p class="text-sm text-gray-500 dark:text-gray-400">让其他用户看到您的在线状态</p>
                  </div>
                  <input 
                    v-model="settings.privacy.showOnlineStatus" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="saveSettings"
                  />
                </div>
              </div>
            </card>
          </div>

          <!-- 高级设置 -->
          <div v-if="activeTab === 'advanced'" class="space-y-6">
            <card padding="lg">
              <template #header>
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">高级设置</h2>
              </template>
              
              <div class="space-y-6">
                <!-- 实验性功能 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-4">实验性功能</h3>
                  <div class="space-y-3">
                    <label class="flex items-center justify-between">
                      <div>
                        <span class="text-sm text-gray-700 dark:text-gray-300">AI学习助手</span>
                        <p class="text-xs text-gray-500 dark:text-gray-400">启用AI智能推荐功能</p>
                      </div>
                      <input 
                        v-model="settings.advanced.aiAssistant" 
                        type="checkbox" 
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                    </label>
                    <label class="flex items-center justify-between">
                      <div>
                        <span class="text-sm text-gray-700 dark:text-gray-300">离线模式</span>
                        <p class="text-xs text-gray-500 dark:text-gray-400">在无网络时继续学习</p>
                      </div>
                      <input 
                        v-model="settings.advanced.offlineMode" 
                        type="checkbox" 
                        class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        @change="saveSettings"
                      />
                    </label>
                  </div>
                </div>

                <!-- 数据同步 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">数据同步</h3>
                  <div class="flex items-center justify-between">
                    <span class="text-sm text-gray-700 dark:text-gray-300">上次同步：{{ lastSyncTime }}</span>
                    <button variant="outline" size="sm" @click="syncData" :loading="isSyncing">
                      立即同步
                    </button>
                  </div>
                </div>

                <!-- 缓存清理 -->
                <div>
                  <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">存储管理</h3>
                  <div class="space-y-3">
                    <div class="flex items-center justify-between">
                      <span class="text-sm text-gray-700 dark:text-gray-300">缓存大小：{{ cacheSize }}</span>
                      <button variant="outline" size="sm" @click="clearCache" :loading="isClearingCache">
                        清理缓存
                      </button>
                    </div>
                    <div class="flex items-center justify-between">
                      <span class="text-sm text-gray-700 dark:text-gray-300">离线内容：{{ offlineSize }}</span>
                      <button variant="outline" size="sm" @click="clearOfflineData">
                        清理离线数据
                      </button>
                    </div>
                  </div>
                </div>

                <!-- 重置设置 -->
                <div class="pt-4 border-t border-gray-200 dark:border-gray-600">
                  <div class="flex items-center justify-between">
                    <div>
                      <h3 class="text-sm font-medium text-gray-900 dark:text-white">重置所有设置</h3>
                      <p class="text-sm text-gray-500 dark:text-gray-400">恢复到默认设置</p>
                    </div>
                    <button variant="danger" size="sm" @click="resetSettings">
                      重置设置
                    </button>
                  </div>
                </div>
              </div>
            </card>
          </div>
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
import {
  CogIcon,
  PaintBrushIcon,
  BellIcon,
  BookOpenIcon,
  ShieldCheckIcon,
  WrenchScrewdriverIcon,
  SunIcon,
  MoonIcon,
  ComputerDesktopIcon
} from '@heroicons/vue/24/outline'

// Store
const uiStore = useUIStore()

// 状态
const activeTab = ref('general')
const isSyncing = ref(false)
const isClearingCache = ref(false)
const lastSyncTime = ref('2024-01-15 14:30')
const cacheSize = ref('52.3 MB')
const offlineSize = ref('128.7 MB')

// 导航标签
const tabs = [
  { id: 'general', name: '基本设置', icon: CogIcon },
  { id: 'appearance', name: '外观', icon: PaintBrushIcon },
  { id: 'notifications', name: '通知', icon: BellIcon },
  { id: 'learning', name: '学习', icon: BookOpenIcon },
  { id: 'privacy', name: '隐私', icon: ShieldCheckIcon },
  { id: 'advanced', name: '高级', icon: WrenchScrewdriverIcon }
]

// 主题选项
const themeOptions = [
  { value: 'light', name: '明亮', description: '明亮主题', icon: SunIcon },
  { value: 'dark', name: '暗黑', description: '暗黑主题', icon: MoonIcon },
  { value: 'auto', name: '自动', description: '跟随系统', icon: ComputerDesktopIcon }
]

// 学习模式
const learningModes = [
  { value: 'focused', label: '专注模式' },
  { value: 'relaxed', label: '轻松模式' },
  { value: 'intensive', label: '强化模式' }
]

// 隐私选项
const privacyOptions = [
  { value: 'public', label: '公开' },
  { value: 'friends', label: '仅好友' },
  { value: 'private', label: '私密' }
]

// 邮件通知选项
const emailNotifications = [
  { key: 'assignments', label: '作业通知', description: '新作业发布时通知' },
  { key: 'grades', label: '成绩通知', description: '成绩发布时通知' },
  { key: 'courses', label: '课程更新', description: '课程内容更新时通知' },
  { key: 'messages', label: '消息通知', description: '收到新消息时通知' },
  { key: 'reminders', label: '学习提醒', description: '学习计划提醒' }
]

// 推送通知选项
const pushNotifications = [
  { key: 'immediate', label: '即时通知', description: '重要事件立即推送' },
  { key: 'daily', label: '每日摘要', description: '每日学习摘要' },
  { key: 'weekly', label: '每周报告', description: '每周学习报告' }
]

// 设置数据
const settings = reactive({
  language: 'zh-CN',
  timezone: 'Asia/Shanghai',
  dateFormat: 'YYYY-MM-DD',
  autoSave: true,
  theme: 'light',
  fontSize: 14,
  compactMode: false,
  animations: true,
  notifications: {
    email: {
      assignments: true,
      grades: true,
      courses: true,
      messages: true,
      reminders: false
    },
    push: {
      immediate: true,
      daily: true,
      weekly: false
    },
    doNotDisturbEnabled: false,
    doNotDisturbStart: '22:00',
    doNotDisturbEnd: '08:00'
  },
  learning: {
    dailyGoal: 60,
    reminders: true,
    reminderTimes: [
      { time: '09:00' },
      { time: '19:00' }
    ],
    breakReminder: true,
    breakInterval: 60,
    defaultMode: 'focused'
  },
  privacy: {
    analytics: true,
    errorReporting: true,
    learningVisibility: 'public',
    showOnlineStatus: true
  },
  advanced: {
    aiAssistant: false,
    offlineMode: false
  }
})

// 方法
const saveSettings = async () => {
  try {
    // 模拟保存延迟
    await new Promise(resolve => setTimeout(resolve, 300))
    
    uiStore.showNotification({
      type: 'success',
      title: '设置已保存',
      message: '您的设置已自动保存'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: '保存设置时发生错误'
    })
  }
}

const addReminderTime = () => {
  settings.learning.reminderTimes.push({ time: '12:00' })
}

const removeReminderTime = (index: number) => {
  settings.learning.reminderTimes.splice(index, 1)
}

const syncData = async () => {
  isSyncing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    lastSyncTime.value = new Date().toLocaleString('zh-CN')
    
    uiStore.showNotification({
      type: 'success',
      title: '同步完成',
      message: '数据已与服务器同步'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '同步失败',
      message: '数据同步时发生错误'
    })
  } finally {
    isSyncing.value = false
  }
}

const clearCache = async () => {
  isClearingCache.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    cacheSize.value = '0 MB'
    
    uiStore.showNotification({
      type: 'success',
      title: '缓存已清理',
      message: '系统缓存已清理完成'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '清理失败',
      message: '清理缓存时发生错误'
    })
  } finally {
    isClearingCache.value = false
  }
}

const clearOfflineData = async () => {
  if (confirm('确定要清理所有离线数据吗？这将删除已下载的课程内容。')) {
    try {
      await new Promise(resolve => setTimeout(resolve, 1000))
      offlineSize.value = '0 MB'
      
      uiStore.showNotification({
        type: 'success',
        title: '离线数据已清理',
        message: '所有离线内容已删除'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '清理失败',
        message: '清理离线数据时发生错误'
      })
    }
  }
}

const resetSettings = async () => {
  if (confirm('确定要重置所有设置吗？此操作不可撤销。')) {
    try {
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // 重置到默认设置
      Object.assign(settings, {
        language: 'zh-CN',
        timezone: 'Asia/Shanghai',
        dateFormat: 'YYYY-MM-DD',
        autoSave: true,
        theme: 'light',
        fontSize: 14,
        compactMode: false,
        animations: true,
        notifications: {
          email: {
            assignments: true,
            grades: true,
            courses: true,
            messages: true,
            reminders: false
          },
          push: {
            immediate: true,
            daily: true,
            weekly: false
          },
          doNotDisturbEnabled: false,
          doNotDisturbStart: '22:00',
          doNotDisturbEnd: '08:00'
        },
        learning: {
          dailyGoal: 60,
          reminders: true,
          reminderTimes: [
            { time: '09:00' },
            { time: '19:00' }
          ],
          breakReminder: true,
          breakInterval: 60,
          defaultMode: 'focused'
        },
        privacy: {
          analytics: true,
          errorReporting: true,
          learningVisibility: 'public',
          showOnlineStatus: true
        },
        advanced: {
          aiAssistant: false,
          offlineMode: false
        }
      })
      
      uiStore.showNotification({
        type: 'success',
        title: '设置已重置',
        message: '所有设置已恢复到默认值'
      })
    } catch (error) {
      uiStore.showNotification({
        type: 'error',
        title: '重置失败',
        message: '重置设置时发生错误'
      })
    }
  }
}

// 生命周期
onMounted(() => {
  // 初始化设置
})
</script> 