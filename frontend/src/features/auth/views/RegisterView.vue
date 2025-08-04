<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">创建账户</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">加入我们的学习平台</p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6">
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">账户类型</label>
        <div class="grid grid-cols-2 gap-4">
          <label class="relative" v-for="roleOption in roles" :key="roleOption.value">
            <input v-model="form.role" type="radio" :value="roleOption.value" class="sr-only peer" />
            <div class="p-4 border-2 rounded-lg cursor-pointer peer-checked:border-primary-500">
              <div class="text-center">{{ roleOption.label }}</div>
            </div>
          </label>
        </div>
      </div>

      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">用户名</label>
        <input id="username" v-model="form.username" type="text" required class="input" :disabled="authStore.loading" />
      </div>

      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">邮箱地址</label>
        <input id="email" v-model="form.email" type="email" required class="input" :disabled="authStore.loading" />
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">密码</label>
        <input id="password" v-model="form.password" type="password" required class="input" :disabled="authStore.loading" />
      </div>

      <div>
        <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">确认密码</label>
        <input id="confirmPassword" v-model="form.confirmPassword" type="password" required class="input" :disabled="authStore.loading" />
      </div>

      <div>
        <button type="submit" :disabled="authStore.loading" class="w-full btn btn-primary">
          {{ authStore.loading ? '注册中...' : '创建账户' }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        已有账户？
        <router-link to="/auth/login" class="font-medium text-primary-600 hover:text-primary-500">
          立即登录
        </router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useUIStore } from '@/stores/ui';
import type { RegisterRequest } from '@/types/auth';

const authStore = useAuthStore();
const uiStore = useUIStore();

const roles = [
  { label: '学生', value: 'STUDENT' },
  { label: '教师', value: 'TEACHER' },
];

const form = ref<RegisterRequest>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'STUDENT',
});

const handleSubmit = () => {
  if (form.value.password !== form.value.confirmPassword) {
    uiStore.showNotification({ type: 'error', title: '注册失败', message: '两次输入的密码不一致' });
    return;
  }
  authStore.register(form.value);
};
</script>
