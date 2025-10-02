<template>
  <div class="max-w-xl mx-auto">
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold">{{ t('auth.checkEmail.title') }}</h2>
      <p class="mt-2 text-sm text-gray-500">{{ t('auth.checkEmail.subtitle') }}</p>
    </div>

    <div v-glass="{ strength: 'regular' }" class="rounded-lg p-4 mb-6">
      {{ t('auth.checkEmail.tip') }}
    </div>

    <div class="space-y-3">
      <Button class="w-full" variant="info" icon="arrow-right" @click="goLogin">{{ t('auth.checkEmail.toLogin') }}</Button>
      <Button as="a" href="/auth/register" variant="secondary" icon="user-plus" @click.prevent="goRegister">{{ t('auth.checkEmail.toRegister') }}</Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { setLocale, i18n } from '@/i18n'
import { useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const { t } = i18n.global

// 进入页面后做一次性强制刷新，避免白屏（使用 sessionStorage 标记防止循环）
onMounted(async () => {
  // 读取语言参数，优先使用链接语言
  const langQ = route.query?.lang as string | undefined
  if (langQ) {
    await setLocale(langQ as any)
  }
  // 基于URL参数的一次性刷新：首次进入追加 _rf=1 并刷新；带 _rf=1 时不再刷新
  const rfParam = (route.query as any)?._rf
  const alreadyRefreshed = Array.isArray(rfParam) ? rfParam[0] === '1' : rfParam === '1'
  if (!alreadyRefreshed) {
    try {
      await router.replace({ path: route.path, query: { ...route.query, _rf: '1' } })
    } catch {}
    // 初次进入执行一次刷新，确保资源与状态一致
    requestAnimationFrame(() => window.location.reload())
  }
})

const goLogin = async () => {
  await router.push('/auth/login')
  requestAnimationFrame(() => window.location.reload())
}

const goRegister = async () => {
  await router.push('/auth/register')
  requestAnimationFrame(() => window.location.reload())
}
</script>


