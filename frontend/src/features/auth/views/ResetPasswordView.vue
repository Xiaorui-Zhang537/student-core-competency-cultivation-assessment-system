<template>
  <div>
    <div class="text-center mb-8">
      <h2 class="text-3xl font-bold">重置密码</h2>
      <p class="mt-2 text-sm text-gray-500">请输入新密码并确认。</p>
    </div>

    <form @submit.prevent="handleReset" class="space-y-6">
      <div>
        <label for="password" class="block text-sm font-medium mb-2">新密码</label>
        <div class="rounded-lg">
          <glass-input id="password" v-model="password" type="password" :disabled="uiStore.loading" placeholder="请输入新密码（至少6位）" tint="primary" />
        </div>
      </div>
      <div>
        <label for="confirm" class="block text-sm font-medium mb-2">确认新密码</label>
        <div class="rounded-lg">
          <glass-input id="confirm" v-model="confirm" type="password" :disabled="uiStore.loading" placeholder="请再次输入新密码" tint="primary" />
        </div>
      </div>

      <div>
        <button class="w-full" variant="info" type="submit" :disabled="uiStore.loading">
          {{ uiStore.loading ? '提交中...' : '重置密码' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import { userApi } from '@/api/user.api'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import Button from '@/components/ui/Button.vue'

const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()

const token = ref<string>('')
const password = ref<string>('')
const confirm = ref<string>('')

onMounted(() => {
  const q = route.query?.token
  token.value = (Array.isArray(q) ? q[0] : q) || ''
})

const handleReset = async () => {
  if (!token.value) {
    uiStore.showNotification({ type: 'error', title: '错误', message: '重置链接无效或已过期，请重新申请。' })
    return
  }
  if (!password.value || password.value.length < 6) {
    uiStore.showNotification({ type: 'error', title: '错误', message: '新密码至少6位。' })
    return
  }
  if (password.value !== confirm.value) {
    uiStore.showNotification({ type: 'error', title: '错误', message: '两次输入的密码不一致。' })
    return
  }

  uiStore.setLoading(true)
  try {
    await userApi.resetPassword({ token: token.value, newPassword: password.value })
    uiStore.showNotification({ type: 'success', title: '成功', message: '密码已重置，请使用新密码登录。' })
    await router.replace('/auth/login')
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: '错误', message: e?.message || '重置失败，请重试或重新申请重置。' })
  } finally {
    uiStore.setLoading(false)
  }
}
</script>


