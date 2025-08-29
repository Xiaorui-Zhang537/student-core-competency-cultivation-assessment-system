<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold">{{ t('auth.verify.title') }}</h2>
      <p class="mt-2 text-sm text-gray-500">{{ t('auth.verify.subtitle') }}</p>
    </div>

    <div class="text-center" v-if="verifying">
      <div v-glass="{ strength: 'ultraThin' }" class="inline-block px-4 py-2 rounded-lg">
        <span class="text-gray-700 dark:text-gray-200">{{ t('auth.verify.inProgress') }}</span>
      </div>
    </div>

    <div class="text-center" v-else>
      <div v-glass="{ strength: 'ultraThin' }" class="inline-block p-3 rounded-lg">
        <button class="btn btn-primary" @click="goLogin">{{ t('auth.verify.toLogin') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { userApi } from '@/api/user.api'
import { setLocale, i18n } from '@/i18n'

const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()
const { t } = i18n.global

const verifying = ref<boolean>(true)

const goLogin = async () => {
  await router.replace('/auth/login')
}

onMounted(async () => {
  // 根据邮件链接中的 lang 自动切换界面语言
  const langQ = route.query?.lang as string | undefined
  if (langQ) {
    await setLocale(langQ as any)
  }

  const q = route.query?.token
  const token = (Array.isArray(q) ? q[0] : q) || ''
  if (!token) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: t('auth.verify.invalid') })
    verifying.value = false
    return
  }
  try {
    await userApi.verifyEmail(token)
    uiStore.showNotification({ type: 'success', title: t('app.notifications.success.title'), message: t('auth.verify.success') })
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('app.notifications.error.title'), message: e?.message || t('auth.verify.fail') })
  } finally {
    verifying.value = false
    // 验证结果无论成功与否，给出返回登录提示；若成功则自动跳转并刷新
    // 避免你描述的“跳到登录白屏，需要刷新”的问题
    try {
      // 短暂延迟给用户看到提示
      setTimeout(async () => {
        await router.replace('/auth/login')
        // 强制刷新，确保登录页状态与资源正确加载
        window.location.reload()
      }, 800)
    } catch {}
  }
})
</script>


