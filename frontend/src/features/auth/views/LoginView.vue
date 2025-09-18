<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">{{ t('auth.login.title') }}</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ t('auth.login.subtitle') }}</p>
    </div>

    <form @submit.prevent="handleLogin(form)" class="space-y-6">
      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.login.form.username.label') }}</label>
        <div class="rounded-lg">
          <GlassInput
            id="username"
            v-model="form.username"
            type="text"
            :disabled="authStore.loading"
            :placeholder="t('auth.login.form.username.placeholder') as string"
          />
        </div>
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.login.form.password.label') }}</label>
        <div class="rounded-lg">
          <GlassInput
            id="password"
            v-model="form.password"
            type="password"
            :disabled="authStore.loading"
            :placeholder="t('auth.login.form.password.placeholder') as string"
          />
        </div>
      </div>

      <div class="flex items-center justify-between">
        <div class="text-sm">
          <a href="/auth/forgot-password" @click.prevent="goForgot" class="font-medium text-primary-600 hover:text-primary-500">
            {{ t('auth.login.link.forgot') }}
          </a>
        </div>
      </div>

      <div>
        <Button class="w-full" variant="primary" type="submit" :disabled="authStore.loading">
          {{ authStore.loading ? t('auth.login.action.submitting') : t('auth.login.action.submit') }}
        </Button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        {{ t('auth.login.link.noAccount') }}
        <a href="/auth/register" @click.prevent="goRegister" class="font-medium text-primary-600 hover:text-primary-500">
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
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import Button from '@/components/ui/Button.vue'

const authStore = useAuthStore()
const router = useRouter()
const { t } = useI18n()

const form = ref<LoginRequest>({ username: '', password: '' })

const handleLogin = async (credentials: LoginRequest) => {
  const before = router.currentRoute.value.fullPath
  await authStore.login(credentials)
  // 登录后由 store 触发路由跳转；此处等待下一帧确保路由完成后强制刷新
  requestAnimationFrame(() => {
    const after = router.currentRoute.value.fullPath
    if (before !== after) {
      window.location.reload()
    }
  })
}

const goRegister = async () => {
  await router.push('/auth/register')
  window.location.reload()
}

const goForgot = async () => {
  await router.push('/auth/forgot-password')
  window.location.reload()
}
</script>
