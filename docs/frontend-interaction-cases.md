# 组件-Store-API 交互案例（含代码片段）

> 面向新手：用最小可运行的片段展示“组件 → Store → API → 后端”的完整闭环。

## 0. 页面标题统一组件（PageHeader）

- 目的：统一各页面标题与简介的展示，保证中英文一致、布局规范、便于在右上角放置筛选器/操作按钮。
- 组件：`components/ui/PageHeader.vue`（已在 `main.ts` 全局注册）。
- 基础用法：
```vue
<PageHeader :title="t('student.courses.title')" :subtitle="t('student.courses.subtitle')" />
```
- 携带操作区（actions 插槽）：
```vue
<PageHeader :title="t('teacher.analytics.title')" :subtitle="t('teacher.analytics.subtitle')">
  <template #actions>
    <GlassPopoverSelect v-model="selectedCourseId" :options="courseSelectOptions" size="md" />
    <Button variant="primary" @click="onExport">{{ t('teacher.analytics.exportReport') }}</Button>
  </template>
</PageHeader>
```
- 已应用页面（节选）：
  - 学生端：Dashboard、Assignments、AssignmentSubmit、Courses、CourseDetail、Analysis、Analytics、Grades、Community、Ability
  - 教师端：Dashboard、ManageCourse、CourseDetail、CourseStudents、Analytics、ReviewAssignment、AssignmentSubmissions、GradeAssignment、StudentDetail、EditCourse
  - 共享：Community、AIAssistant、Notifications、NotificationDetail、PostDetail、CourseDiscovery、Help

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

### 2.1 学生端-我的课程（CoursesView → useStudentStore → student.api.ts）

要点：
- 顶部统一用 `PageHeader` 展示标题与简介，右上角可放操作按钮（如“浏览课程”）。
- 拉取接口：`GET /api/students/my-courses/paged`；仅当搜索词非空时传 `q`，避免空串被后端当过滤导致结果为空。
- Pinia 使用 `storeToRefs` 解构，保证响应性；映射后端 `StudentCourseResponse` 字段到前端 `StudentCourse`。
- 封面图加载失败时回退为首字母彩色块，避免空白卡片。
- 分类筛选仅基于已报名课程的本地字段（遵循“无后端，不开发”）。

组件片段（精简）：
```ts
<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import PageHeader from '@/components/ui/PageHeader.vue'
import Button from '@/components/ui/Button.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { useStudentStore } from '@/stores/student'

const studentStore = useStudentStore()
const { myCourses } = storeToRefs(studentStore)

const pageLoaded = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')

// 仅在 q 非空时传参
onMounted(async () => {
  await studentStore.fetchMyCourses({ page: 1, size: 12 })
  pageLoaded.value = true
})

// 过滤
const debouncedQuery = ref('')
const debounce = (fn: Function, wait = 250) => { let t: any; return (...a: any[]) => { clearTimeout(t); t = setTimeout(() => fn(...a), wait) } }
watch(searchQuery, debounce((v: string) => { debouncedQuery.value = v || '' }, 250))

const coursesSafe = computed(() => Array.isArray(myCourses.value) ? myCourses.value : [])
const categories = computed(() => Array.from(new Set(coursesSafe.value.map(c => c.category).filter(Boolean))).sort())
const categoryOptions = computed(() => (categories.value as string[]).map(name => ({ label: name, value: name })))

const filteredCourses = computed(() => {
  const q = (debouncedQuery.value || '').toLowerCase()
  const cat = selectedCategory.value || ''
  return coursesSafe.value.filter(c => {
    const title = (c.title || '').toLowerCase()
    const okQ = !q || title.includes(q)
    const okCat = !cat || c.category === cat
    return okQ && okCat
  })
})
</script>
```

Store 片段（字段映射与 q 处理）：
```ts
// stores/student.ts（节选）
const fetchMyCourses = async (params?: { page?: number; size?: number; q?: string }) => {
  const p: any = { page: params?.page, size: params?.size }
  if (params?.q && params.q.trim()) p.q = params.q.trim()
  const response = await handleApiCall(() => studentApi.getMyCourses(p), uiStore, '获取我的课程失败')
  if (response) {
    const items = (response as any)?.items ?? (Array.isArray(response) ? response : [])
    myCourses.value = (items || []).map((c: any) => ({
      id: String(c.id),
      title: c.title || '',
      description: c.description || '',
      teacherName: c.teacherName || '',
      category: c.category || '',
      coverImageUrl: c.coverImageUrl || c.coverImage || '',
      progress: Number(c.progress ?? 0),
      enrolledAt: c.enrolledAt || ''
    }))
  }
}
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

## 3.1 学生作业列表筛选/搜索/分页（AssignmentsView → student.api.ts）

组件片段（精简）：
```ts
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { studentApi } from '@/api/student.api'

const list = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const status = ref<'ALL'|'PENDING'|'SUBMITTED'|'GRADED'>('ALL')
const courseId = ref<string>('')
const keyword = ref('')

const fetch = async () => {
  const params: any = { page: page.value, size: size.value }
  if (courseId.value) params.courseId = courseId.value
  if (status.value !== 'ALL') params.status = status.value.toLowerCase()
  if (keyword.value) params.q = keyword.value
  const res: any = await studentApi.getAssignments(params)
  list.value = res?.items || res?.data?.items || []
}

onMounted(fetch)
watch([page, size, status, courseId, keyword], fetch)
</script>
```

要点：
- `status` 映射后端小写 submitted/graded；待提交对应 published 或空，由后端聚合返回。
- 搜索键为 `q`，与 `/students/my-courses/paged` 保持一致。

## 3.2 学生提交后成绩展示（AssignmentSubmitView → grade.api.ts）

组件片段（仅评分展示相关）：
```ts
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { gradeApi } from '@/api/grade.api'

const route = useRoute()
const auth = useAuthStore()
const grade = ref<any | null>(null)
const displayStatus = computed(() => 'GRADED') // 示例：实际由提交状态归一化得到

onMounted(async () => {
  if (displayStatus.value === 'GRADED' && auth.user?.id) {
    const res: any = await gradeApi.getGradeForStudentAssignment(String(auth.user.id), String(route.params.id))
    grade.value = res?.data ?? res
  }
})
</script>
```

要点：
- 仅在已评分时按需拉取成绩，避免列表页 N+1。
- 国际化键补充：`student.grades.score / student.grades.feedback`。

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
