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
        <div class="rounded-lg">
          <glass-input id="email" v-model="email" type="email" :disabled="uiStore.loading" placeholder="请输入您的邮箱地址" />
        </div>
      </div>

      <div>
        <Button class="w-full" variant="primary" type="submit" :disabled="uiStore.loading">
          {{ uiStore.loading ? '发送中...' : '发送重置链接' }}
        </Button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600">
        记起密码了？
        <a href="/auth/login" @click.prevent="goLogin" class="font-medium text-primary-600 hover:text-primary-500">返回登录</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui';
import { userApi } from '@/api/user.api';
import { useI18n } from 'vue-i18n'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import Button from '@/components/ui/Button.vue'

const uiStore = useUIStore();
const router = useRouter()
const { t } = useI18n()
const email = ref('');

const handleForgotPassword = async () => {
  if (!email.value) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('app.auth.forgot.emailRequired') });
    return;
  }
  
  uiStore.setLoading(true);
  try {
    await userApi.forgotPassword(email.value);
    uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title'), message: t('app.auth.forgot.emailSent') });
  } catch (error: any) {
    // For security reasons, we don't tell the user if the email exists or not.
    uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title'), message: t('app.auth.forgot.emailSent') });
  } finally {
    uiStore.setLoading(false);
  }
};

const goLogin = async () => {
  await router.push('/auth/login')
  requestAnimationFrame(() => window.location.reload())
}
</script>
