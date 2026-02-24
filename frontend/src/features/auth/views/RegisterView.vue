<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold text-gray-900 dark:text-white">{{ t('auth.register.title') }}</h2>
      <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">{{ t('auth.register.subtitle') }}</p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- 默认头像选择（统一使用 UserAvatar 显示，圆形裁切） -->
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.register.form.avatar') || '选择头像（可选）' }}</label>
        <div v-glass="{ strength: 'ultraThin' }" class="grid grid-cols-5 gap-2 rounded-lg p-2">
          <button
            v-for="(url, idx) in defaultAvatars"
            :key="idx"
            type="button"
            class="h-12 w-12 rounded-full overflow-hidden border-2 transition-all bg-transparent flex items-center justify-center"
            :class="
              form.avatar === url
                ? 'border-primary-500 ring-2 ring-primary-500 ring-offset-2 ring-offset-white dark:ring-offset-gray-800 shadow-md'
                : 'border-gray-300 dark:border-gray-600 hover:border-primary-400'
            "
            @click="form.avatar = url"
            :title="'默认头像 ' + (idx+1)"
          >
            <user-avatar :avatar="url" :size="44" :rounded="true" :fit="'cover'" />
          </button>
        </div>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">{{ t('auth.register.form.role.label') }}</label>
        <div class="grid grid-cols-2 gap-4">
          <label class="relative" v-for="roleOption in roles" :key="roleOption.value">
            <input v-model="form.role" type="radio" :value="roleOption.value" class="sr-only peer" />
            <div v-glass="{ strength: 'regular', interactive: true }" class="p-4 border-2 rounded-lg cursor-pointer peer-checked:border-primary-500">
              <div class="text-center">
                {{ roleOption.value === 'student' ? t('auth.register.form.role.student') : t('auth.register.form.role.teacher') }}
              </div>
            </div>
          </label>
        </div>
      </div>

      <div>
        <label for="username" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.register.form.username.label') }}</label>
        <div class="rounded-lg">
          <glass-input id="username" v-model="form.username" type="text" :disabled="authStore.loading" tint="primary" />
        </div>
        <p class="mt-1 text-xs text-subtle">{{ usernameHint }}</p>
        <p v-if="form.username && !isUsernameValid" class="mt-1 text-xs text-red-600">{{ t('auth.register.error.usernameInvalid') }}</p>
      </div>

      <div>
        <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.register.form.email.label') }}</label>
        <div class="rounded-lg">
          <glass-input id="email" v-model="form.email" type="email" :disabled="authStore.loading" tint="primary" />
        </div>
        <p v-if="form.email && !isEmailValid" class="mt-1 text-xs text-red-600">{{ t('auth.register.error.emailInvalid') }}</p>
      </div>

      <div>
        <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.register.form.password.label') }}</label>
        <div class="rounded-lg">
          <glass-password-input id="password" v-model="form.password" :disabled="authStore.loading" tint="primary" />
        </div>
        <p class="mt-1 text-xs text-subtle">{{ passwordHint }}</p>
        <p v-if="form.password && !isPasswordValid" class="mt-1 text-xs text-red-600">{{ t('auth.register.error.passwordInvalid') }}</p>
      </div>

      <div>
        <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('auth.register.form.confirmPassword.label') }}</label>
        <div class="rounded-lg">
          <glass-password-input id="confirmPassword" v-model="confirmPassword" :disabled="authStore.loading" tint="primary" />
        </div>
      </div>

      <div>
        <button class="w-full" variant="info" icon="confirm" type="submit" :disabled="authStore.loading || (form.username && !isUsernameValid) || (form.email && !isEmailValid) || (form.password && !isPasswordValid)">
          {{ authStore.loading ? t('auth.register.action.submitting') : t('auth.register.action.submit') }}
        </button>
      </div>
    </form>

    <div class="mt-6 text-center">
      <p class="text-sm text-gray-600 dark:text-gray-400">
        {{ t('auth.register.link.hasAccount') }}
        <a href="/auth/login" @click.prevent="goLogin" class="font-medium text-primary hover:text-primary/80">
          {{ t('auth.register.link.toLogin') }}
        </a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useUIStore } from '@/stores/ui';
import type { RegisterRequest } from '@/types/auth';
import { useI18n } from 'vue-i18n'
import { i18n } from '@/i18n'
import { useRouter } from 'vue-router'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassPasswordInput from '@/components/ui/inputs/GlassPasswordInput.vue'
import Button from '@/components/ui/Button.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { DEFAULT_AVATARS } from '@/shared/utils/avatars'

const authStore = useAuthStore();
const uiStore = useUIStore();
const { t } = useI18n()
const router = useRouter()

const roles = [
  { label: t('auth.register.form.role.student'), value: 'student' },
  { label: t('auth.register.form.role.teacher'), value: 'teacher' },
];

const form = reactive<RegisterRequest & { avatar?: string }>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'student',
  avatar: undefined,
  lang: i18n.global.locale.value as any,
});
const confirmPassword = ref('');
const USERNAME_MIN = 4
const PASSWORD_MIN = 8
const isUsernameValid = computed(() => {
  const v = form.username || ''
  return v.length >= USERNAME_MIN && /^[A-Za-z0-9]+$/.test(v)
})
const isEmailValid = computed(() => /^[\w.!#$%&'*+/=?^`{|}~-]+@[\w-]+(?:\.[\w-]+)+$/.test(form.email || ''))
const isPasswordValid = computed(() => {
  const v = form.password || ''
  return v.length >= PASSWORD_MIN && /^[A-Za-z0-9@#$%^&*!._-]+$/.test(v)
})

// i18n 提示（使用 useI18n 的 t，确保参数插值与语言切换生效）
const usernameHint = computed(() => i18n.global.t('auth.register.form.hint.usernameMin', { min: String(USERNAME_MIN) }) as string)
const passwordHint = computed(() => i18n.global.t('auth.register.form.hint.passwordMin', { min: String(PASSWORD_MIN) }) as string)

const defaultAvatars = DEFAULT_AVATARS as string[]

const handleSubmit = () => {
  if (!isUsernameValid.value) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('auth.register.error.usernameInvalid') });
    return;
  }
  if (!isEmailValid.value) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('auth.register.error.emailInvalid') });
    return;
  }
  if (!isPasswordValid.value) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('auth.register.error.passwordInvalid') });
    return;
  }
  form.confirmPassword = confirmPassword.value
  if (form.password !== confirmPassword.value) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('auth.register.error.passwordMismatch') });
    return;
  }
  authStore.register(form);
};

const goLogin = async () => {
  await router.push('/auth/login')
  requestAnimationFrame(() => window.location.reload())
}
</script>
