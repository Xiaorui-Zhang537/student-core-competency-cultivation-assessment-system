<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-4xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">个人资料</h1>
        <p class="text-gray-600 dark:text-gray-400">管理您的个人信息和账户设置</p>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        <p class="mt-2 text-gray-600 dark:text-gray-400">加载用户信息中...</p>
      </div>

      <div v-else class="space-y-8">
        <!-- 个人信息概览 -->
        <card padding="lg">
          <template #header>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">个人信息</h2>
          </template>
          
          <div class="flex items-start space-x-6">
            <!-- 头像上传 -->
            <div class="flex-shrink-0">
              <div class="relative">
                <div class="w-24 h-24 rounded-full overflow-hidden bg-gray-100 dark:bg-gray-700">
                  <img
                    v-if="userProfile.avatar"
                    :src="userProfile.avatar"
                    :alt="userProfile.name"
                    class="w-full h-full object-cover"
                  />
                  <div v-else class="w-full h-full flex items-center justify-center">
                    <user-icon class="w-12 h-12 text-gray-400" />
                  </div>
                </div>
                <button
                  @click="$refs.avatarInput?.click()"
                  class="absolute bottom-0 right-0 w-8 h-8 bg-primary-600 hover:bg-primary-700 rounded-full flex items-center justify-center text-white shadow-lg transition-colors"
                >
                  <camera-icon class="w-4 h-4" />
                </button>
                <input
                  ref="avatarInput"
                  type="file"
                  accept="image/*"
                  class="hidden"
                  @change="handleAvatarUpload"
                />
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-2 text-center">
                点击更换头像
              </p>
            </div>

            <!-- 基本信息显示 -->
            <div class="flex-1">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    姓名
                  </label>
                  <p class="text-gray-900 dark:text-white font-medium">{{ userProfile.name }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    邮箱
                  </label>
                  <p class="text-gray-900 dark:text-white">{{ userProfile.email }}</p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    角色
                  </label>
                  <badge :variant="userProfile.role === 'teacher' ? 'primary' : 'secondary'">
                    {{ getRoleText(userProfile.role) }}
                  </badge>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                    注册时间
                  </label>
                  <p class="text-gray-900 dark:text-white">{{ formatDate(userProfile.createdAt) }}</p>
                </div>
              </div>
              <div class="mt-4">
                <button variant="outline" @click="showEditProfile = true">
                  <pencil-icon class="w-4 h-4 mr-2" />
                  编辑资料
                </button>
              </div>
            </div>
          </div>
        </card>

        <!-- 编辑个人信息表单 -->
        <card padding="lg" v-if="showEditProfile">
          <template #header>
            <div class="flex justify-between items-center">
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">编辑个人信息</h2>
              <button variant="ghost" size="sm" @click="showEditProfile = false">
                <x-mark-icon class="w-4 h-4" />
              </button>
            </div>
          </template>
          
          <form @submit.prevent="updateProfile" class="space-y-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- 姓名 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  姓名 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="profileForm.name"
                  type="text"
                  class="input"
                  :class="{ 'border-red-500': errors.name }"
                  @blur="validateField('name')"
                />
                <p v-if="errors.name" class="mt-1 text-sm text-red-600">{{ errors.name }}</p>
              </div>

              <!-- 昵称 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  昵称
                </label>
                <input
                  v-model="profileForm.nickname"
                  type="text"
                  class="input"
                  placeholder="设置一个昵称"
                />
              </div>

              <!-- 手机号 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  手机号
                </label>
                <input
                  v-model="profileForm.phone"
                  type="tel"
                  class="input"
                  placeholder="输入手机号"
                />
              </div>

              <!-- 生日 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  生日
                </label>
                <input
                  v-model="profileForm.birthday"
                  type="date"
                  class="input"
                />
              </div>

              <!-- 性别 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  性别
                </label>
                <select v-model="profileForm.gender" class="input">
                  <option value="">请选择</option>
                  <option value="male">男</option>
                  <option value="female">女</option>
                  <option value="other">其他</option>
                </select>
              </div>

              <!-- 所在地 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  所在地
                </label>
                <input
                  v-model="profileForm.location"
                  type="text"
                  class="input"
                  placeholder="城市"
                />
              </div>
            </div>

            <!-- 个人简介 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                个人简介
              </label>
              <textarea
                v-model="profileForm.bio"
                rows="4"
                class="input"
                placeholder="介绍一下自己..."
              ></textarea>
            </div>

            <!-- 学校/机构信息 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  学校/机构
                </label>
                <input
                  v-model="profileForm.school"
                  type="text"
                  class="input"
                  placeholder="学校或机构名称"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  专业/部门
                </label>
                <input
                  v-model="profileForm.department"
                  type="text"
                  class="input"
                  placeholder="专业或部门"
                />
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="flex justify-end space-x-3">
              <button type="button" variant="outline" @click="showEditProfile = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="isUpdating">
                保存更改
              </button>
            </div>
          </form>
        </card>

        <!-- 账户安全 -->
        <card padding="lg">
          <template #header>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">账户安全</h2>
          </template>
          
          <div class="space-y-6">
            <!-- 密码修改 -->
            <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">登录密码</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">上次修改时间：{{ formatDate(userProfile.passwordUpdatedAt) }}</p>
              </div>
              <button variant="outline" size="sm" @click="showChangePassword = true">
                修改密码
              </button>
            </div>

            <!-- 邮箱验证 -->
            <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">邮箱验证</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ userProfile.emailVerified ? '已验证' : '未验证' }}
                </p>
              </div>
              <button 
                v-if="!userProfile.emailVerified" 
                variant="outline" 
                size="sm" 
                @click="sendVerificationEmail"
                :loading="isSendingVerification"
              >
                发送验证邮件
              </button>
              <badge v-else variant="success">已验证</badge>
            </div>

            <!-- 两步验证 -->
            <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">两步验证</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">
                  {{ userProfile.twoFactorEnabled ? '已启用' : '未启用' }}
                </p>
              </div>
              <button variant="outline" size="sm" @click="toggleTwoFactor">
                {{ userProfile.twoFactorEnabled ? '关闭' : '启用' }}
              </button>
            </div>
          </div>
        </card>

        <!-- 修改密码表单 -->
        <card padding="lg" v-if="showChangePassword">
          <template #header>
            <div class="flex justify-between items-center">
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">修改密码</h2>
              <button variant="ghost" size="sm" @click="showChangePassword = false">
                <x-mark-icon class="w-4 h-4" />
              </button>
            </div>
          </template>
          
          <form @submit.prevent="changePassword" class="space-y-6">
            <!-- 当前密码 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                当前密码 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="passwordForm.currentPassword"
                type="password"
                class="input"
                :class="{ 'border-red-500': errors.currentPassword }"
                @blur="validateField('currentPassword')"
              />
              <p v-if="errors.currentPassword" class="mt-1 text-sm text-red-600">{{ errors.currentPassword }}</p>
            </div>

            <!-- 新密码 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                新密码 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                class="input"
                :class="{ 'border-red-500': errors.newPassword }"
                @blur="validateField('newPassword')"
              />
              <p v-if="errors.newPassword" class="mt-1 text-sm text-red-600">{{ errors.newPassword }}</p>
              
              <!-- 密码强度指示器 -->
              <div class="mt-2">
                <div class="flex space-x-1">
                  <div 
                    v-for="i in 4" 
                    :key="i"
                    class="h-1 flex-1 rounded"
                    :class="getPasswordStrengthColor(i)"
                  ></div>
                </div>
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                  密码强度：{{ getPasswordStrengthText() }}
                </p>
              </div>
            </div>

            <!-- 确认新密码 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                确认新密码 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                class="input"
                :class="{ 'border-red-500': errors.confirmPassword }"
                @blur="validateField('confirmPassword')"
              />
              <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-600">{{ errors.confirmPassword }}</p>
            </div>

            <!-- 操作按钮 -->
            <div class="flex justify-end space-x-3">
              <button type="button" variant="outline" @click="showChangePassword = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="isChangingPassword" :disabled="!isPasswordFormValid">
                修改密码
              </button>
            </div>
          </form>
        </card>

        <!-- 偏好设置 -->
        <card padding="lg">
          <template #header>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">偏好设置</h2>
          </template>
          
          <div class="space-y-6">
            <!-- 语言设置 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">界面语言</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">选择您的首选语言</p>
              </div>
              <select v-model="preferences.language" @change="updatePreferences" class="input w-32">
                <option value="zh-CN">中文</option>
                <option value="en-US">English</option>
              </select>
            </div>

            <!-- 主题设置 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">主题模式</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">选择明亮或暗黑主题</p>
              </div>
              <select v-model="preferences.theme" @change="updatePreferences" class="input w-32">
                <option value="light">明亮</option>
                <option value="dark">暗黑</option>
                <option value="auto">自动</option>
              </select>
            </div>

            <!-- 通知设置 -->
            <div>
              <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">通知设置</h3>
              <div class="space-y-3">
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">邮件通知</span>
                  <input 
                    v-model="preferences.emailNotifications" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="updatePreferences"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">浏览器通知</span>
                  <input 
                    v-model="preferences.browserNotifications" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="updatePreferences"
                  />
                </label>
                <label class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">声音提醒</span>
                  <input 
                    v-model="preferences.soundNotifications" 
                    type="checkbox" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="updatePreferences"
                  />
                </label>
              </div>
            </div>
          </div>
        </card>

        <!-- 隐私设置 -->
        <card padding="lg">
          <template #header>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">隐私设置</h2>
          </template>
          
          <div class="space-y-6">
            <!-- 个人资料可见性 -->
            <div>
              <h3 class="text-sm font-medium text-gray-900 dark:text-white mb-3">个人资料可见性</h3>
              <div class="space-y-3">
                <label class="flex items-center">
                  <input 
                    v-model="privacy.profileVisibility" 
                    type="radio" 
                    value="public" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="updatePrivacy"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">公开</span>
                </label>
                <label class="flex items-center">
                  <input 
                    v-model="privacy.profileVisibility" 
                    type="radio" 
                    value="friends" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="updatePrivacy"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">仅好友</span>
                </label>
                <label class="flex items-center">
                  <input 
                    v-model="privacy.profileVisibility" 
                    type="radio" 
                    value="private" 
                    class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                    @change="updatePrivacy"
                  />
                  <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">私密</span>
                </label>
              </div>
            </div>

            <!-- 学习记录可见性 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">学习记录可见</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">允许其他人查看您的学习进度</p>
              </div>
              <input 
                v-model="privacy.showLearningProgress" 
                type="checkbox" 
                class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                @change="updatePrivacy"
              />
            </div>

            <!-- 在线状态可见性 -->
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-sm font-medium text-gray-900 dark:text-white">在线状态可见</h3>
                <p class="text-sm text-gray-500 dark:text-gray-400">显示您的在线状态</p>
              </div>
              <input 
                v-model="privacy.showOnlineStatus" 
                type="checkbox" 
                class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                @change="updatePrivacy"
              />
            </div>
          </div>
        </card>

        <!-- 账户操作 -->
        <card padding="lg">
          <template #header>
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">账户操作</h2>
          </template>
          
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
              <div>
                <h3 class="text-sm font-medium text-yellow-800 dark:text-yellow-200">导出数据</h3>
                <p class="text-sm text-yellow-600 dark:text-yellow-300">下载您的个人数据副本</p>
              </div>
              <button variant="outline" size="sm" @click="exportData">
                导出数据
              </button>
            </div>

            <div class="flex items-center justify-between p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
              <div>
                <h3 class="text-sm font-medium text-red-800 dark:text-red-200">删除账户</h3>
                <p class="text-sm text-red-600 dark:text-red-300">永久删除您的账户和所有数据</p>
              </div>
              <button variant="danger" size="sm" @click="showDeleteAccount = true">
                删除账户
              </button>
            </div>
          </div>
        </card>
      </div>
    </div>

    <!-- 删除账户确认弹窗 -->
    <div v-if="showDeleteAccount" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">确认删除账户</h3>
        <p class="text-sm text-gray-600 dark:text-gray-400 mb-6">
          此操作将永久删除您的账户和所有相关数据，包括学习记录、作业和个人信息。此操作不可撤销。
        </p>
        <div class="flex space-x-3">
          <button variant="outline" @click="showDeleteAccount = false" class="flex-1">
            取消
          </button>
          <button variant="danger" @click="deleteAccount" class="flex-1" :loading="isDeletingAccount">
            确认删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  UserIcon,
  CameraIcon,
  PencilIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

// Stores
const authStore = useAuthStore()
const uiStore = useUIStore()

// 状态
const isLoading = ref(true)
const isUpdating = ref(false)
const isChangingPassword = ref(false)
const isSendingVerification = ref(false)
const isDeletingAccount = ref(false)
const showEditProfile = ref(false)
const showChangePassword = ref(false)
const showDeleteAccount = ref(false)

// 用户资料数据
const userProfile = reactive({
  id: 'user-001',
  name: '张同学',
  nickname: '学习小能手',
  email: 'zhang@example.com',
  phone: '13800138000',
  avatar: '',
  role: 'student',
  gender: 'male',
  birthday: '2000-01-01',
  location: '北京市',
  bio: '热爱学习，追求进步。',
  school: '清华大学',
  department: '计算机科学与技术',
  createdAt: '2023-09-01T00:00:00Z',
  passwordUpdatedAt: '2024-01-01T00:00:00Z',
  emailVerified: true,
  twoFactorEnabled: false
})

// 编辑表单
const profileForm = reactive({
  name: '',
  nickname: '',
  phone: '',
  birthday: '',
  gender: '',
  location: '',
  bio: '',
  school: '',
  department: ''
})

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 偏好设置
const preferences = reactive({
  language: 'zh-CN',
  theme: 'light',
  emailNotifications: true,
  browserNotifications: true,
  soundNotifications: false
})

// 隐私设置
const privacy = reactive({
  profileVisibility: 'public',
  showLearningProgress: true,
  showOnlineStatus: true
})

// 错误状态
const errors = reactive({
  name: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 计算属性
const isPasswordFormValid = computed(() => {
  return passwordForm.currentPassword !== '' &&
    passwordForm.newPassword !== '' &&
    passwordForm.confirmPassword !== '' &&
    Object.values(errors).every(error => error === '')
})

const passwordStrength = computed(() => {
  const password = passwordForm.newPassword
  let strength = 0
  
  if (password.length >= 8) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^A-Za-z0-9]/.test(password)) strength++
  
  return strength
})

// 方法
const loadUserProfile = async () => {
  try {
    // 模拟加载用户数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 填充编辑表单
    Object.assign(profileForm, {
      name: userProfile.name,
      nickname: userProfile.nickname,
      phone: userProfile.phone,
      birthday: userProfile.birthday,
      gender: userProfile.gender,
      location: userProfile.location,
      bio: userProfile.bio,
      school: userProfile.school,
      department: userProfile.department
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '加载失败',
      message: '无法加载用户信息'
    })
  } finally {
    isLoading.value = false
  }
}

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    student: '学生',
    teacher: '教师',
    admin: '管理员'
  }
  return roleMap[role] || role
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const validateField = (field: string) => {
  switch (field) {
    case 'name':
      errors.name = profileForm.name.trim() === '' ? '请输入姓名' : ''
      break
    case 'currentPassword':
      errors.currentPassword = passwordForm.currentPassword === '' ? '请输入当前密码' : ''
      break
    case 'newPassword':
      if (passwordForm.newPassword === '') {
        errors.newPassword = '请输入新密码'
      } else if (passwordForm.newPassword.length < 8) {
        errors.newPassword = '密码长度至少8位'
      } else {
        errors.newPassword = ''
      }
      break
    case 'confirmPassword':
      if (passwordForm.confirmPassword === '') {
        errors.confirmPassword = '请确认新密码'
      } else if (passwordForm.confirmPassword !== passwordForm.newPassword) {
        errors.confirmPassword = '两次输入的密码不一致'
      } else {
        errors.confirmPassword = ''
      }
      break
  }
}

const getPasswordStrengthColor = (index: number) => {
  if (index <= passwordStrength.value) {
    switch (passwordStrength.value) {
      case 1: return 'bg-red-500'
      case 2: return 'bg-yellow-500'
      case 3: return 'bg-blue-500'
      case 4: return 'bg-green-500'
      default: return 'bg-gray-200 dark:bg-gray-600'
    }
  }
  return 'bg-gray-200 dark:bg-gray-600'
}

const getPasswordStrengthText = () => {
  switch (passwordStrength.value) {
    case 0: return '很弱'
    case 1: return '弱'
    case 2: return '一般'
    case 3: return '强'
    case 4: return '很强'
    default: return ''
  }
}

const handleAvatarUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      uiStore.showNotification({
        type: 'error',
        title: '文件过大',
        message: '头像文件不能超过 5MB'
      })
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      userProfile.avatar = e.target?.result as string
      uiStore.showNotification({
        type: 'success',
        title: '头像上传成功',
        message: '头像已更新'
      })
    }
    reader.readAsDataURL(file)
  }
}

const updateProfile = async () => {
  validateField('name')
  
  if (errors.name) {
    return
  }

  isUpdating.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 更新用户资料
    Object.assign(userProfile, profileForm)
    
    uiStore.showNotification({
      type: 'success',
      title: '更新成功',
      message: '个人资料已更新'
    })
    
    showEditProfile.value = false
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '更新失败',
      message: '更新个人资料时发生错误'
    })
  } finally {
    isUpdating.value = false
  }
}

const changePassword = async () => {
  validateField('currentPassword')
  validateField('newPassword')
  validateField('confirmPassword')
  
  if (!isPasswordFormValid.value) {
    return
  }

  isChangingPassword.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    uiStore.showNotification({
      type: 'success',
      title: '密码修改成功',
      message: '密码已更新，请牢记新密码'
    })
    
    // 重置表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    showChangePassword.value = false
    userProfile.passwordUpdatedAt = new Date().toISOString()
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '修改失败',
      message: '修改密码时发生错误'
    })
  } finally {
    isChangingPassword.value = false
  }
}

const sendVerificationEmail = async () => {
  isSendingVerification.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    uiStore.showNotification({
      type: 'success',
      title: '验证邮件已发送',
      message: '请检查您的邮箱并点击验证链接'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '发送失败',
      message: '发送验证邮件时发生错误'
    })
  } finally {
    isSendingVerification.value = false
  }
}

const toggleTwoFactor = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    userProfile.twoFactorEnabled = !userProfile.twoFactorEnabled
    
    uiStore.showNotification({
      type: 'success',
      title: userProfile.twoFactorEnabled ? '已启用两步验证' : '已关闭两步验证',
      message: userProfile.twoFactorEnabled ? '您的账户安全性已提升' : '两步验证已关闭'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '操作失败',
      message: '修改两步验证设置时发生错误'
    })
  }
}

const updatePreferences = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    uiStore.showNotification({
      type: 'success',
      title: '设置已保存',
      message: '偏好设置已更新'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: '保存偏好设置时发生错误'
    })
  }
}

const updatePrivacy = async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    
    uiStore.showNotification({
      type: 'success',
      title: '设置已保存',
      message: '隐私设置已更新'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '保存失败',
      message: '保存隐私设置时发生错误'
    })
  }
}

const exportData = async () => {
  try {
    uiStore.showNotification({
      type: 'info',
      title: '正在准备数据',
      message: '正在生成您的数据导出文件...'
    })
    
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    uiStore.showNotification({
      type: 'success',
      title: '导出完成',
      message: '数据已导出，请检查下载文件夹'
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '导出失败',
      message: '导出数据时发生错误'
    })
  }
}

const deleteAccount = async () => {
  isDeletingAccount.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    uiStore.showNotification({
      type: 'success',
      title: '账户已删除',
      message: '您的账户和所有数据已被永久删除'
    })
    
    // 注销并跳转到登录页
    authStore.logout()
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '删除失败',
      message: '删除账户时发生错误'
    })
  } finally {
    isDeletingAccount.value = false
    showDeleteAccount.value = false
  }
}

// 生命周期
onMounted(() => {
  loadUserProfile()
})
</script> 