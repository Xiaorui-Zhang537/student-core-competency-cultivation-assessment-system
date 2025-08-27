<template>
  <div class="max-w-xl mx-auto">
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold">{{ t('auth.checkEmail.title') }}</h2>
      <p class="mt-2 text-sm text-gray-500">{{ t('auth.checkEmail.subtitle') }}</p>
    </div>

    <div class="bg-blue-50 border border-blue-200 text-blue-800 rounded-lg p-4 mb-6">
      {{ t('auth.checkEmail.tip') }}
    </div>

    <div class="space-y-3">
      <button class="w-full btn btn-primary" @click="goLogin">{{ t('auth.checkEmail.toLogin') }}</button>
      <a class="block text-center text-sm text-gray-500 hover:text-gray-700" href="/auth/register" @click.prevent="goRegister">{{ t('auth.checkEmail.toRegister') }}</a>
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
    window.location.reload()
  }
})

const goLogin = async () => {
  await router.push('/auth/login')
  window.location.reload()
}

const goRegister = async () => {
  await router.push('/auth/register')
  window.location.reload()
}
</script>


