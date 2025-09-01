# 组件-Store-API 交互案例（含代码片段）

> 面向新手：用最小可运行的片段展示“组件 → Store → API → 后端”的完整闭环。

## 1. 登录表单（LoginView → useAuthStore → auth.api.ts）

最小组件片段：
```ts
<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const store = useAuthStore()
const username = ref('')
const password = ref('')

const onSubmit = async () => {
  try {
    await store.login({ username: username.value, password: password.value })
  } catch (e) {
    // TODO: 统一 toast
  }
}
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth.api'

export const useAuthStore = defineStore('auth', {
  state: () => ({ token: '', user: null as any }),
  actions: {
    async login(payload: { username: string; password: string }) {
      const { data } = await authApi.login(payload)
      this.token = data.token
      this.user = data.user
    }
  }
})
```

## 2. 课程分页（ManageCourseView → useCourseStore → course.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useCourseStore } from '@/stores/course'

const store = useCourseStore()
const query = ref('')
const page = ref(1)
const size = ref(10)

onMounted(() => store.fetchCourses({ page: page.value, size: size.value, query: query.value }))
watch([page, size, query], () => store.fetchCourses({ page: page.value, size: size.value, query: query.value }))
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { courseApi } from '@/api/course.api'

export const useCourseStore = defineStore('course', {
  state: () => ({ items: [] as any[], total: 0, page: 1, size: 10 }),
  actions: {
    async fetchCourses(params: { page: number; size: number; query?: string }) {
      const { data } = await courseApi.getCourses(params)
      this.items = data.items
      this.total = data.total
      this.page = data.page
      this.size = data.size
    }
  }
})
```

## 3. 作业提交与草稿（AssignmentSubmitView → useSubmissionStore → submission.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { ref } from 'vue'
import { useSubmissionStore } from '@/stores/submission'

const store = useSubmissionStore()
const content = ref('')

const saveDraft = () => store.saveDraft(88, { content: content.value, fileIds: [] })
const submit = () => store.submit(88, { content: content.value, fileIds: [] })
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { submissionApi } from '@/api/submission.api'

export const useSubmissionStore = defineStore('submission', {
  state: () => ({ lastId: '' }),
  actions: {
    async saveDraft(assignmentId: number, payload: any) {
      await submissionApi.saveDraft(assignmentId, payload)
    },
    async submit(assignmentId: number, payload: any) {
      const { data } = await submissionApi.submitAssignment(assignmentId, payload)
      this.lastId = data.id
    }
  }
})
```

## 4. SSE 通知订阅（NotificationBell → useNotificationStream → notification.api.ts）

组合式函数片段（精简版）：
```ts
import { onMounted, onUnmounted } from 'vue'
export function useNotificationStream() {
  let timer: any
  const start = () => {
    timer = setInterval(() => {
      // 调用 notificationApi.getMyNotifications
    }, 15000)
  }
  const stop = () => timer && clearInterval(timer)
  onMounted(start)
  onUnmounted(stop)
  return { start, stop }
}
```

## 5. 课程详情（CourseDetailView → useCourseStore → course.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCourseStore } from '@/stores/course'

const store = useCourseStore()
const route = useRoute()
const id = Number(route.params.id)

onMounted(() => store.fetchCourseById(id))
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { courseApi } from '@/api/course.api'

export const useCourseStore = defineStore('course', {
  state: () => ({ detail: null as any }),
  actions: {
    async fetchCourseById(id: number) {
      const { data } = await courseApi.getCourseById(id)
      this.detail = data
    }
  }
})
```

要点：
- 路由参数变化时需重新拉取；404 需跳回课程列表并提示

## 6. 成绩批改（GradeAssignmentView → useTeacherStore → grade.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { ref } from 'vue'
import { useTeacherStore } from '@/stores/teacher'

const store = useTeacherStore()
const score = ref(0)
const feedback = ref('')

const grade = async (submissionId: string, studentId: string, assignmentId: string) => {
  await store.gradeSubmission({ submissionId, studentId, assignmentId, score: score.value, feedback: feedback.value, maxScore: 100, publishImmediately: false })
}

const publish = async (gradeId: string) => {
  await store.publishGrade(gradeId)
}
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { gradeApi } from '@/api/grade.api'

export const useTeacherStore = defineStore('teacher', {
  actions: {
    async gradeSubmission(payload: any) {
      await gradeApi.gradeSubmission(payload)
    },
    async publishGrade(id: string) {
      await gradeApi.publishGrade(id)
    }
  }
})
```

要点：
- 评分与发布分两步，或一次性 `publishImmediately=true`
- 409 冲突时刷新成绩状态后再操作

## 7. 作业列表与筛选（AssignmentListView → useAssignmentStore → assignment.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useAssignmentStore } from '@/stores/assignment'

const store = useAssignmentStore()
const status = ref<'DRAFT'|'PUBLISHED'|'CLOSED'|''>('')
const keyword = ref('')
const page = ref(1)
const size = ref(10)

const fetch = () => store.fetchAssignments({ page: page.value, size: size.value, status: status.value || undefined, keyword: keyword.value || undefined })

onMounted(fetch)
watch([status, keyword, page, size], fetch)
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { assignmentApi } from '@/api/assignment.api'

export const useAssignmentStore = defineStore('assignment', {
  state: () => ({ items: [] as any[], total: 0, page: 1, size: 10 }),
  actions: {
    async fetchAssignments(params: { page: number; size: number; status?: string; keyword?: string }) {
      const { data } = await assignmentApi.getAssignments(params)
      this.items = data.items
      this.total = data.total
      this.page = data.page
      this.size = data.size
    }
  }
})
```

## 8. 通知中心操作（NotificationCenter → useNotificationStore → notification.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notifications'

const store = useNotificationStore()

onMounted(() => store.fetchMy({ page: 1, size: 20 }))
const markRead = (id: number) => store.markAsRead(id)
const markAll = () => store.markAllAsRead()
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { notificationApi } from '@/api/notification.api'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({ items: [] as any[], unread: 0 }),
  actions: {
    async fetchMy(params: any) {
      const { data } = await notificationApi.getMyNotifications(params)
      this.items = data.items
      this.unread = data.items.filter((n: any) => !n.read).length
    },
    async markAsRead(id: number) {
      await notificationApi.markAsRead(id)
      const n = this.items.find((x: any) => x.id === id)
      if (n) n.read = true
      this.unread = Math.max(0, this.unread - 1)
    },
    async markAllAsRead() {
      await notificationApi.markAllAsRead()
      this.items.forEach((n: any) => (n.read = true))
      this.unread = 0
    }
  }
})
```

## 9. 文件管理（FileManager → useFileStore → file.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { ref } from 'vue'
import { useFileStore } from '@/stores/file'

const store = useFileStore()
const file = ref<File | null>(null)

const onUpload = async () => {
  if (!file.value) return
  const form = new FormData()
  form.append('file', file.value)
  await store.upload(form)
}
const onDownload = (id: number) => store.download(id)
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { fileApi } from '@/api/file.api'

export const useFileStore = defineStore('file', {
  state: () => ({ list: [] as any[] }),
  actions: {
    async upload(form: FormData) {
      const { data } = await fileApi.upload(form)
      this.list.unshift(data)
    },
    async download(id: number) {
      const blob = await fileApi.download(id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'file'
      a.click()
      URL.revokeObjectURL(url)
    }
  }
})
```

## 10. 课时进度异常重传（LessonPlayer → useLessonStore → lesson.api.ts）

组件片段（含退避重试）：
```ts
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useLessonStore } from '@/stores/lesson'

const store = useLessonStore()
const progress = ref(0)

const onProgress = async (p: number) => {
  progress.value = p
  await store.updateProgressWithRetry({ id: 123, progress: p })
}

onMounted(() => {
  // 播放器绑定 onProgress 回调
})
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { lessonApi } from '@/api/lesson.api'

export const useLessonStore = defineStore('lesson', {
  actions: {
    async updateProgress(id: number, p: number) {
      await lessonApi.updateLessonProgress(id, p)
    },
    async updateProgressWithRetry({ id, progress }: { id: number; progress: number }) {
      let delay = 1000
      for (let i = 0; i < 5; i++) {
        try {
          await this.updateProgress(id, progress)
          return
        } catch (e: any) {
          if (e?.response?.status && [401, 403, 429, 503].includes(e.response.status)) {
            await new Promise((r) => setTimeout(r, delay))
            delay = Math.min(delay * 2, 10000)
            continue
          }
          throw e
        }
      }
    }
  }
})
```

## 11. 社区发帖/评论（CommunityView → useCommunityStore → community.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCommunityStore } from '@/stores/community'

const store = useCommunityStore()
const content = ref('')
const comment = ref('')

onMounted(() => store.fetchPosts({ page: 1, size: 10 }))
const createPost = () => store.createPost({ content: content.value })
const addComment = (postId: number) => store.addComment(postId, { content: comment.value })
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { communityApi } from '@/api/community.api'

export const useCommunityStore = defineStore('community', {
  state: () => ({ posts: [] as any[], total: 0 }),
  actions: {
    async fetchPosts(params: any) {
      const { data } = await communityApi.getPosts(params)
      this.posts = data.items
      this.total = data.total
    },
    async createPost(payload: any) {
      const { data } = await communityApi.createPost(payload)
      this.posts.unshift(data)
    },
    async addComment(postId: number, payload: any) {
      const { data } = await communityApi.addComment(postId, payload)
      const post = this.posts.find((p: any) => p.id === postId)
      if (post) post.comments.unshift(data)
    }
  }
})
```

## 12. 教师仪表盘指标（DashboardView → useDashboardStore → teacher.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'

const store = useDashboardStore()
const range = ref<'7d'|'30d'|'90d'>('7d')

onMounted(() => store.fetchMetrics({ range: range.value }))
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { teacherApi } from '@/api/teacher.api'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({ kpis: null as any, trends: [] as any[] }),
  actions: {
    async fetchMetrics(params: { range: string }) {
      const { data } = await teacherApi.getDashboard(params)
      this.kpis = data.kpis
      this.trends = data.trends
    }
  }
})
```

## 13. 学生能力雷达/趋势（StudentAbilityView → useStudentStore → ability.api.ts）

组件片段：
```ts
<script setup lang="ts">
import { onMounted } from 'vue'
import { useStudentStore } from '@/stores/student'

const store = useStudentStore()

onMounted(() => store.fetchAbility({ studentId: 1001 }))
</script>
```

Store 片段：
```ts
import { defineStore } from 'pinia'
import { abilityApi } from '@/api/ability.api'

export const useStudentStore = defineStore('student', {
  state: () => ({ radar: [] as any[], trends: [] as any[] }),
  actions: {
    async fetchAbility(params: { studentId: number }) {
      const { data } = await abilityApi.getStudentAbility(params.studentId)
      this.radar = data.radar
      this.trends = data.trends
    }
  }
})
```
