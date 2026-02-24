<template>
  <div class="max-w-md w-[92%] mx-auto">
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">{{ t('auth.login.title') }}</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ t('auth.login.subtitle') }}</p>
    </div>

    <form @submit.prevent="handleLogin(form)" class="space-y-6">
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.login.form.username.label') }}</label>
        <div class="rounded-lg">
          <glass-input
            id="username"
            v-model="form.username"
            type="text"
            :disabled="authStore.loading"
            :placeholder="t('auth.login.form.username.placeholder') as string"
            tint="primary"
          />
        </div>
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.login.form.password.label') }}</label>
        <div class="rounded-lg">
          <glass-password-input
            id="password"
            v-model="form.password"
            :disabled="authStore.loading"
            :placeholder="t('auth.login.form.password.placeholder') as string"
            tint="primary"
          />
        </div>
      </div>

      <div class="flex items-center justify-between">
        <div class="text-sm">
          <a href="/auth/forgot-password" @click.prevent="goForgot" class="font-medium text-primary hover:text-primary/80">
            {{ t('auth.login.link.forgot') }}
          </a>
        </div>
      </div>

      <div>
        <button class="w-full rounded-full" size="xl" variant="info" type="submit" :disabled="authStore.loading">
          {{ authStore.loading ? t('auth.login.action.submitting') : t('auth.login.action.submit') }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        {{ t('auth.login.link.noAccount') }}
        <a href="/auth/register" @click.prevent="goRegister" class="font-medium text-primary hover:text-primary/80">
          {{ t('auth.login.link.toRegister') }}
        </a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { LoginRequest } from '@/types/auth'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { i18n } from '@/i18n'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassPasswordInput from '@/components/ui/inputs/GlassPasswordInput.vue'
import Button from '@/components/ui/Button.vue'
import { useUIStore } from '@/stores/ui'

const authStore = useAuthStore()
const router = useRouter()
const { t } = useI18n()
const uiStore = useUIStore()

const form = ref<LoginRequest>({ username: '', password: '' })

const handleLogin = async (credentials: LoginRequest) => {
  let notified = false
  try {
    await authStore.login(credentials, { notify: false })
    // 若登录未成功（例如服务端返回错误但未抛异常），给出提示并中止刷新
    if (!(authStore as any).isAuthenticated) {
      uiStore.showNotification({ type: 'error', title: i18n.global.t('app.notifications.error.title') as string, message: i18n.global.t('auth.login.error.invalidCredentials') as string })
      return
    }
    // 登录成功：保持原有的刷新逻辑
    requestAnimationFrame(() => window.location.reload())
  } catch (e: any) {
    uiStore.showNotification({
      type: 'error',
      title: i18n.global.t('app.notifications.error.title') as string,
      message: i18n.global.t('auth.login.error.invalidCredentials') as string
    })
  }
}

const goRegister = async () => {
  await router.push('/auth/register')
  requestAnimationFrame(() => window.location.reload())
}

const goForgot = async () => {
  await router.push('/auth/forgot-password')
  requestAnimationFrame(() => window.location.reload())
}
</script>
