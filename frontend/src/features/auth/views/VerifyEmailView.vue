<template>
  <div class="max-w-lg w-[92%] mx-auto">
    <LiquidGlass :radius="16" :frost="0.05" class="p-0" :container-class="'rounded-2xl w-full'">
      <div class="py-8 px-6 md:py-10 md:px-8 text-center">
        <h2 class="text-2xl md:text-3xl font-bold text-[color:var(--color-base-content)]">{{ t('auth.verify.title') }}</h2>
        <p class="mt-2 text-sm md:text-base text-subtle">{{ t('auth.verify.subtitle') }}</p>

        <div class="mt-6" v-if="verifying">
          <div class="inline-block px-4 py-2 rounded-lg glass-thin">
            <span class="text-[color:var(--color-base-content)]">{{ t('auth.verify.inProgress') }}</span>
          </div>
        </div>

        <div class="mt-6" v-else>
          <div class="inline-flex flex-wrap gap-3 justify-center">
            <Button variant="primary" size="xl" class="rounded-full px-6" @click="goLogin">{{ t('auth.verify.toLogin') }}</Button>
          </div>
        </div>
      </div>
    </LiquidGlass>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { userApi } from '@/api/user.api'
import { setLocale, i18n } from '@/i18n'
import Button from '@/components/ui/Button.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

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
    // 验证完成后引导回登录，无需强制刷新
    try {
      setTimeout(async () => {
        await router.replace('/auth/login')
        requestAnimationFrame(() => window.location.reload())
      }, 800)
    } catch {}
  }
})
</script>


