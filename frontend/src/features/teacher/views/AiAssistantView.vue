<template>
  <div class="min-h-screen p-6">
    <div class="max-w-5xl mx-auto">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-white">{{ t('teacher.ai.title') }}</h1>
          <p class="text-gray-600 dark:text-gray-400">{{ t('teacher.ai.subtitle') }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Chat Panel -->
        <div class="lg:col-span-2 glass-thick glass-interactive rounded-2xl border border-gray-200/40 dark:border-gray-700/40 p-0 overflow-hidden" v-glass="{ strength: 'thick', interactive: true }">
          <div class="h-[60vh] overflow-y-auto p-4 space-y-4 bg-transparent">
            <div v-for="(m, idx) in messages" :key="idx" class="flex" :class="m.role === 'user' ? 'justify-end' : 'justify-start'">
              <div class="max-w-[80%] rounded-2xl px-4 py-2 text-sm"
                   :class="m.role === 'user' ? 'bg-primary-600 text-white rounded-br-none' : 'bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-gray-100 rounded-bl-none'">
                <p class="whitespace-pre-wrap">{{ m.content }}</p>
              </div>
            </div>
            <div v-if="loading" class="text-center text-sm text-gray-500">{{ t('teacher.ai.thinking') }}</div>
          </div>
          <div class="border-t border-white/25 dark:border-white/10 p-3 bg-transparent flex items-end gap-2">
            <textarea v-model="input" @keydown.enter.prevent="send" rows="1" :placeholder="t('teacher.ai.placeholder')"
                      class="flex-1 input input--glass resize-none" />
            <Button variant="primary" :disabled="!input.trim() || loading" @click="send">{{ t('teacher.ai.send') }}</Button>
          </div>
        </div>

        <!-- Context Panel -->
        <div class="space-y-4">
          <div class="filter-container p-4 rounded-xl" v-glass="{ strength: 'thin', interactive: false }">
            <h3 class="font-semibold mb-2">{{ t('teacher.ai.context.title') }}</h3>
            <p class="text-sm text-gray-600 dark:text-gray-400">{{ t('teacher.ai.context.desc') }}</p>
            <select v-model="selectedCourseId" class="input mt-3">
              <option :value="null">{{ t('teacher.ai.context.noCourse') }}</option>
              <option v-for="c in teacherCourses" :key="c.id" :value="String(c.id)">{{ c.title }}</option>
            </select>
            <div v-if="selectedCourseId" class="mt-3 space-y-2">
              <label class="text-xs text-gray-500">{{ t('teacher.ai.context.selectStudents') }}</label>
              <div class="flex gap-2">
                <select v-model.number="studentToAdd" class="input flex-1">
                  <option :value="0">{{ t('teacher.ai.context.selectStudent') }}</option>
                  <option v-for="s in courseStudents" :key="s.id" :value="Number(s.id)">
                    {{ s.nickname || s.username || (t('teacher.students.table.list') + ' #' + s.id) }}
                  </option>
                </select>
                <Button variant="primary" :disabled="!studentToAdd || selectedStudentIds.length>=5 || selectedStudentIds.includes(studentToAdd)" @click="addStudent">{{ t('teacher.ai.context.add') }}</Button>
              </div>
              <div v-if="selectedStudentIds.length" class="flex flex-wrap gap-2 mt-2">
                <span v-for="sid in selectedStudentIds" :key="sid" class="px-2 py-1 rounded-full text-xs bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200">
                  {{ studentLabel(sid) }}
                  <button class="ml-1" @click="removeStudent(sid)">×</button>
                </span>
                <Button variant="outline" size="sm" @click="clearStudents">{{ t('teacher.ai.context.clear') }}</Button>
              </div>
              <p class="text-xs text-gray-500" v-if="selectedStudentIds.length">{{ t('teacher.ai.context.applied', { course: courseTitle, count: selectedStudentIds.length }) }}</p>
            </div>
          </div>
          <div class="filter-container p-4 rounded-xl" v-glass="{ strength: 'thin', interactive: false }">
            <h3 class="font-semibold mb-2">{{ t('teacher.ai.model.title') }}</h3>
            <div class="space-y-2">
              <label class="text-xs text-gray-500">{{ t('teacher.ai.model.label') }}</label>
              <select v-model="model" class="input">
                <option value="openai/gpt-4o-mini">{{ t('teacher.ai.model.default') }}</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth'
import { aiApi } from '@/api/ai.api'
import { courseApi } from '@/api/course.api'
import Button from '@/components/ui/Button.vue'

type ChatMessage = { role: 'user' | 'assistant'; content: string }

const courseStore = useCourseStore()
const authStore = useAuthStore()

import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const messages = ref<ChatMessage[]>([
  { role: 'assistant', content: t('teacher.ai.greeting') }
])
const input = ref('')
const loading = ref(false)
const selectedCourseId = ref<string | null>(null)
const courseStudents = ref<any[]>([])
const selectedStudentIds = ref<number[]>([])
const studentToAdd = ref<number>(0)
const model = ref<string>('openai/gpt-4o-mini')

const courseTitle = computed(() => {
  const c = teacherCourses.value.find(c => String(c.id) === String(selectedCourseId.value))
  return c?.title || ''
})

watch(selectedCourseId, async (val) => {
  selectedStudentIds.value = []
  courseStudents.value = []
  studentToAdd.value = 0
  if (val) {
    const res: any = await courseApi.getCourseStudents(String(val), { page: 1, size: 100 })
    const items = res?.data?.items || res?.items || []
    courseStudents.value = items
  }
})

function addStudent() {
  const sid = Number(studentToAdd.value)
  if (!sid || selectedStudentIds.value.includes(sid)) return
  if (selectedStudentIds.value.length >= 5) return
  selectedStudentIds.value = [...selectedStudentIds.value, sid]
  studentToAdd.value = 0
}
function removeStudent(sid: number) {
  selectedStudentIds.value = selectedStudentIds.value.filter(id => id !== sid)
}
function clearStudents() { selectedStudentIds.value = [] }
function studentLabel(sid: number) {
  const s = courseStudents.value.find((x:any) => Number(x.id) === Number(sid))
  return s?.nickname || s?.username || `${t('teacher.students.table.list')} #${sid}`
}

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
      messages: messages.value as any,
      courseId: selectedCourseId.value ? Number(selectedCourseId.value) : undefined,
      studentIds: selectedStudentIds.value.length ? selectedStudentIds.value : undefined,
      model: model.value,
    })
    const answer = (resp as any)?.answer || t('teacher.ai.sendFailed')
    messages.value.push({ role: 'assistant', content: String(answer) })
  } catch (e: any) {
    messages.value.push({ role: 'assistant', content: t('teacher.ai.sendFailed') })
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

