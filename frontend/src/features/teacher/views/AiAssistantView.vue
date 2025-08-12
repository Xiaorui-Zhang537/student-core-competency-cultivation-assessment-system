<template>
  <div class="min-h-screen p-6">
    <div class="max-w-5xl mx-auto">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">AI 助理</h1>
          <p class="text-gray-600 dark:text-gray-400">与智能体对话，获取教学建议与分析洞察</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Chat Panel -->
        <div class="lg:col-span-2 card p-0 overflow-hidden">
          <div class="h-[60vh] overflow-y-auto p-4 space-y-4 bg-white dark:bg-gray-800">
            <div v-for="(m, idx) in messages" :key="idx" class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
              <div class="max-w-[80%] rounded-2xl px-4 py-2 text-sm"
                   :class="m.role === 'user' ? 'bg-primary-600 text-white rounded-br-none' : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-100 rounded-bl-none'">
                <p class="whitespace-pre-wrap">{{ m.content }}</p>
              </div>
            </div>
            <div v-if="loading" class="text-center text-sm text-gray-500">AI 正在思考...</div>
          </div>
          <div class="border-t border-gray-200 dark:border-gray-700 p-3 bg-gray-50 dark:bg-gray-900 flex items-end gap-2">
            <textarea v-model="input" @keydown.enter.prevent="send" rows="1" placeholder="输入你的问题，按 Enter 发送"
                      class="flex-1 input resize-none" />
            <button class="btn btn-primary" :disabled="!input.trim() || loading" @click="send">发送</button>
          </div>
        </div>

        <!-- Context Panel -->
        <div class="space-y-4">
          <div class="card p-4">
            <h3 class="font-semibold mb-2">上下文</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">可选：选择课程为 AI 提供上下文，获得更相关的建议。</p>
            <select v-model="selectedCourseId" class="input mt-3">
              <option :value="null">不指定课程</option>
              <option v-for="c in teacherCourses" :key="c.id" :value="String(c.id)">{{ c.title }}</option>
            </select>
          </div>
          <div class="card p-4">
            <h3 class="font-semibold mb-2">模型设置</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">当前为占位实现，后端未接入大模型时将返回友好的提示。</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth'
import { aiApi } from '@/api/ai.api'

type ChatMessage = { role: 'user' | 'assistant'; content: string }

const courseStore = useCourseStore()
const authStore = useAuthStore()

const messages = ref<ChatMessage[]>([
  { role: 'assistant', content: '你好，我是 AI 助理。你可以向我咨询课程分析、作业设计、学习建议等问题。' }
])
const input = ref('')
const loading = ref(false)
const selectedCourseId = ref<string | null>(null)

const teacherCourses = computed(() => {
  if (!authStore.user?.id) return [] as any[]
  return courseStore.courses.filter(c => String(c.teacherId) === String(authStore.user?.id))
})

const send = async () => {
  const content = input.value.trim()
  if (!content) return
  messages.value.push({ role: 'user', content })
  input.value = ''
  loading.value = true
  try {
    const resp = await aiApi.chat({
      messages: messages.value,
      courseId: selectedCourseId.value ? Number(selectedCourseId.value) : undefined,
    })
    const answer = (resp as any)?.answer || '后端尚未接入大模型，现在返回占位响应。'
    messages.value.push({ role: 'assistant', content: String(answer) })
  } catch (e: any) {
    messages.value.push({ role: 'assistant', content: '发送失败，请稍后重试。' })
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const route = useRoute()
  if (!authStore.user) {
    await authStore.fetchUser()
  }
  await courseStore.fetchCourses({ page: 1, size: 200 })
  const q = route.query.q as string | undefined
  if (q && q.trim()) {
    input.value = q
  }
})
</script>

<style scoped>
.card {
  @apply bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700;
}
.input {
  @apply w-full rounded-md border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary-500;
}
.btn {
  @apply inline-flex items-center justify-center px-3 py-2 rounded-md text-sm font-medium;
}
.btn-primary {
  @apply bg-primary-600 text-white hover:bg-primary-700 disabled:opacity-60;
}
</style>

