<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold">忘记密码</h2>
      <p class="mt-2 text-sm text-gray-500">
        输入您的邮箱地址，我们将发送重置密码的链接给您。
      </p>
    </div>

    <form @submit.prevent="handleForgotPassword" class="space-y-6">
      <div>
        <label for="email" class="block text-sm font-medium mb-2">邮箱地址</label>
        <input id="email" v-model="email" type="email" required class="input" :disabled="uiStore.loading" placeholder="请输入您的邮箱地址" />
      </div>

      <div>
        <button type="submit" :disabled="uiStore.loading" class="w-full btn btn-primary">
          {{ uiStore.loading ? '发送中...' : '发送重置链接' }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600">
        记起密码了？
        <router-link to="/auth/login" class="font-medium text-primary-600 hover:text-primary-500">
          返回登录
        </router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUIStore } from '@/stores/ui';
import { userApi } from '@/api/user.api';

const uiStore = useUIStore();
const email = ref('');

const handleForgotPassword = async () => {
  if (!email.value) {
    uiStore.showNotification({ type: 'error', title: '错误', message: '请输入您的邮箱地址。' });
    return;
  }
  
  uiStore.loading = true;
  try {
    await userApi.forgotPassword(email.value);
    uiStore.showNotification({ type: 'success', title: '成功', message: '如果该邮箱已注册，您将收到一封密码重置邮件。' });
  } catch (error: any) {
    // For security reasons, we don't tell the user if the email exists or not.
    uiStore.showNotification({ type: 'success', title: '成功', message: '如果该邮箱已注册，您将收到一封密码重置邮件。' });
  } finally {
    uiStore.loading = false;
  }
};
</script>
