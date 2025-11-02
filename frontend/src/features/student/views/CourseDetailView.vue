<template>
  <div class="p-6">
    <div v-if="courseStore.loading || lessonStore.loading" class="text-center py-12">
      <p>{{ t('student.courses.loading') }}</p>
    </div>

    <div v-else-if="course" class="max-w-7xl mx-auto space-y-8">
      <!-- 面包屑 -->
      <nav class="relative z-10 mb-2">
        <ol class="flex items-center space-x-2 text-sm">
          <li>
            <router-link to="/student/courses" class="text-[var(--color-base-content)] hover:text-[var(--color-primary)]">{{ t('student.courses.title') }}</router-link>
          </li>
          <li><span class="text-[color-mix(in_oklab,var(--color-base-content)_45%,transparent)]">&gt;</span></li>
          <li class="font-medium text-[var(--color-base-content)] truncate">{{ course.title }}</li>
        </ol>
      </nav>

      <!-- 自适应网格：左列信息 + 内容；右列教师信息可跨行，去除左列上方空白 -->
      <div class="grid grid-cols-1 lg:grid-cols-10 gap-6 items-start">
        <div class="lg:col-span-7">
          <card tint="primary">
            <div class="p-5">
              <h1 class="text-2xl font-bold truncate">{{ course.title }}</h1>
              <p v-if="course.description" class="mt-2 text-muted whitespace-pre-line">{{ course.description }}</p>
              <!-- 1) 先显示开课/结课时间（玻璃Badge） -->
              <div class="mt-3 flex flex-wrap items-center gap-2 text-sm text-muted">
                <badge v-if="course.startDate" size="sm" variant="accent">
                  <span class="inline-flex items-center gap-1">
                    <span>📅</span>{{ t('student.courses.detail.startDate') }}: {{ formatDateOnly(course.startDate) }}
                  </span>
                </badge>
                <badge v-if="course.endDate" size="sm" variant="accent">
                  <span class="inline-flex items-center gap-1">
                    <span>⏳</span>{{ t('student.courses.detail.endDate') }}: {{ formatDateOnly(course.endDate) }}
                  </span>
                </badge>
              </div>
              <!-- 2) 换行显示 难度/分类/标签（全部玻璃Badge，标签无标题） -->
              <div class="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted">
                <badge v-if="course.difficulty" size="sm" :variant="difficultyVariant">{{ t('student.courses.detail.difficulty') }}: {{ localizedDifficulty }}</badge>
                <badge v-if="course.category" size="sm" :variant="categoryVariant">{{ t('student.courses.detail.category') }}: {{ localizedCategory }}</badge>
                <template v-if="tagsArray.length">
                  <badge v-for="tag in tagsArray" :key="tag" size="sm" :variant="getTagVariant(tag)">#{{ tag }}</badge>
                </template>
              </div>
              <!-- 3) 进度条（/ui/Progress） -->
              <div class="mt-3">
                <Progress v-if="typeof displayProgress === 'number'" :value="Math.round(displayProgress)" size="md" :color="Number(displayProgress)>=100 ? 'info' : 'primary'" />
              </div>
              <!-- 4) 报名学生：头像+姓名，可点击查看资料/联系 -->
              <div class="mt-4">
                <div v-if="studentsLoading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
                  <div v-for="i in 8" :key="i" class="p-2 rounded-xl glass-ultraThin border animate-pulse">
                    <div class="flex items-center gap-2">
                      <div class="w-10 h-10 rounded-full bg-[color-mix(in_oklab,var(--color-base-content)_16%,transparent)]" />
                      <div class="h-3 w-24 bg-[color-mix(in_oklab,var(--color-base-content)_16%,transparent)] rounded" />
                    </div>
                  </div>
                </div>
                <div v-else-if="students.length" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
                  <Button
                    v-for="s in students"
                    :key="String(s.id)"
                    variant="menu"
                    class="w-full !flex !items-center !justify-start min-h-[3.5rem] rounded-full border border-white/40 dark:border-white/10"
                    type="button"
                    :aria-label="resolveStudentName(s)"
                    @click="openStudentProfile(s)"
                  >
                    <div class="flex items-center gap-1.5 h-full w-full pl-4 pr-7 py-2.5">
                      <user-avatar class="flex-shrink-0" :avatar="s.avatar" :size="44" :alt="resolveStudentName(s)">
                        <span class="text-base font-medium text-muted">{{ resolveStudentName(s).charAt(0) }}</span>
                      </user-avatar>
                      <div class="flex-1 min-w-[7ch] overflow-hidden">
                        <span class="block w-full text-sm font-medium text-strong whitespace-nowrap overflow-hidden text-ellipsis max-w-[7ch]">{{ resolveStudentName(s) }}</span>
                      </div>
                    </div>
                  </Button>
                </div>
                <div v-else class="text-sm text-muted">{{ t('student.courses.detail.noEnrolled') || '暂无报名学生' }}</div>
              </div>
            </div>
          </card>
            </div>
        <!-- 右侧列容器：跨两行，内部纵向堆叠讲师信息与资料 -->
        <div class="lg:col-span-3 lg:row-span-2 self-start flex flex-col gap-4">
          <card tint="info">
            <div class="px-4 py-3 border-b">
              <h3 class="text-lg font-semibold">{{ t('student.courses.detail.instructorInfo') }}</h3>
            </div>
            <div class="p-4">
              <div class="flex items-center gap-4">
                <user-avatar :avatar="resolvedTeacherAvatar" :size="64" :alt="fullName || teacherName || course.teacherName">
                  <span class="text-xl font-medium text-muted">{{ (teacherName || course.teacherName || '#').charAt(0) }}</span>
                </user-avatar>
                <div class="min-w-0 flex-1">
                  <div class="font-semibold truncate">{{ fullName || teacherName || course.teacherName }}</div>
                  <div class="text-sm text-muted">
                    <div v-if="genderLabel">{{ t('student.courses.detail.gender') || '性别' }}：{{ genderLabel }}</div>
                    <div v-if="teacherBirthday">{{ t('student.courses.detail.birthday') || '生日' }}：{{ teacherBirthday }}</div>
                  </div>
                </div>
              </div>
              <div class="mt-3 grid grid-cols-1 gap-2 text-sm text-strong">
                <div v-if="teacher?.school"><span class="text-muted">{{ t('student.courses.detail.school') || '学校' }}：</span>{{ teacher.school }}</div>
                <div v-if="teacher?.email"><span class="text-muted">{{ t('student.courses.detail.email') || '邮箱' }}：</span>{{ teacher.email }}</div>
                <div v-if="teacher?.phone"><span class="text-muted">{{ t('student.courses.detail.phone') || '电话' }}：</span>{{ teacher.phone }}</div>
                <div v-if="teacher?.country || teacher?.province || teacher?.city">
                  <span class="text-muted">{{ t('student.courses.detail.location') || '地点' }}：</span>
                  {{ [teacher?.country, teacher?.province, teacher?.city].filter(Boolean).join(' / ') }}
                </div>
                <div v-if="teacher?.mbti"><span class="text-muted">MBTI：</span>{{ teacher.mbti }}</div>
                <div v-if="teacher?.studentNo || teacher?.teacherNo"><span class="text-muted">{{ t('student.courses.detail.code') || '编号' }}：</span>{{ teacher.teacherNo || teacher.studentNo }}</div>
              </div>
              <div v-if="teacher?.subject" class="mt-3 text-sm text-strong">
                <span class="text-muted">{{ t('student.courses.detail.major') || '专业科目' }}：</span>{{ teacher.subject }}
              </div>
              <div v-if="teacher?.bio" class="mt-3 text-sm text-strong">
                <span class="text-muted">{{ t('student.courses.detail.bioTitle') || '简介' }}：</span>{{ teacher?.bio }}
              </div>
              <div class="mt-4">
                <Button variant="primary" size="sm" class="w-full" @click="contactTeacher">
                  <template #icon>
                    <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor"><path d="M2 5a2 2 0 012-2h16a2 2 0 012 2v14a2 2 0 01-2 2H4a2 2 0 01-2-2V5zm2 1v12h16V6H4zm3 2h6a1 1 0 110 2H7a1 1 0 110-2zm0 4h10a1 1 0 110 2H7a1 1 0 110-2z"/></svg>
                  </template>
                  {{ t('student.courses.detail.contactTeacher') }}
                </Button>
              </div>
            </div>
          </card>
          <!-- 课程资料（右列，位于讲师信息下方） -->
          <card tint="accent">
            <div class="px-4 py-3 border-b">
              <h3 class="text-lg font-semibold">{{ t('student.courses.detail.materials') }}</h3>
            </div>
            <div class="p-4">
              <attachment-list :files="courseMaterials" :noCard="true" :hideHeader="true" :showDefaultDownload="false">
                <template #actions="{ file }">
                  <Button
                    size="sm"
                    variant="success"
                    class="whitespace-nowrap"
                    :title="String(t('student.courses.detail.download') || '下载')"
                    @click="() => fileApi.downloadFile(String(file?.id || file?.fileId || ''), String(file?.originalName || file?.fileName || 'file'))"
                  >
                    <template #icon>
                      <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                    </template>
                  </Button>
                </template>
              </attachment-list>
              <div v-if="!courseMaterials.length" class="text-sm text-muted mt-2">{{ t('student.courses.detail.noMaterials') }}</div>
            </div>
          </card>
        </div>

        <!-- 课程内容（左列，占 7 列） -->
        <div class="lg:col-span-7">
          <card tint="secondary">
            <div class="px-4 py-3 border-b flex items-center justify-between">
              <h3 class="text-lg font-semibold">{{ t('student.courses.detail.contents') }}</h3>
              <span class="text-sm text-muted">{{ completedByProgressCount }} / {{ lessons.length }} {{ t('student.courses.detail.completed') }}</span>
            </div>
            <div class="p-4 space-y-3">
              <template v-if="groupedChapters.length">
                <div v-for="group in groupedChapters" :key="group.key" class="space-y-2">
                  <div class="flex items-center justify-between">
                    <div class="text-sm font-semibold text-strong">{{ group.title }}</div>
                    <Button size="xs" variant="purple" class="inline-flex items-center" @click="toggleChapter(group.key)">
                      <template #icon>
                        <svg v-if="isExpanded(group.key)" class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M5.23 12.21a.75.75 0 001.06.02L10 8.73l3.71 3.5a.75.75 0 001.04-1.08l-4.23-4a.75.75 0 00-1.04 0l-4.25 4a.75.75 0 00-.02 1.06z"/></svg>
                        <svg v-else class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M14.77 7.79a.75.75 0 00-1.06-.02L10 11.27 6.29 7.77a.75.75 0 00-1.04 1.08l4.23 4a.75.75 0 001.04 0l4.25-4a.75.75 0 00.02-1.06z"/></svg>
                      </template>
                      {{ isExpanded(group.key) ? (t('student.courses.detail.collapse') || '收起') : (t('student.courses.detail.expand') || '展开') }}
                    </Button>
                  </div>
                  <div v-if="group.desc" class="text-xs text-subtle">{{ group.desc }}</div>
                  <div class="space-y-2" v-show="isExpanded(group.key)">
                    <div v-for="(lesson, index) in group.items" :key="lesson.id" class="p-4 border rounded-xl glass-ultraThin">
                      <div class="flex items-start gap-4">
                        <div class="flex-shrink-0 mr-1">
                          <div v-if="getLessonProgress(lesson.id) >= 100" class="w-8 h-8 rounded-full flex items-center justify-center text-[var(--color-success-content)] bg-[color-mix(in_oklab,var(--color-success)_80%,transparent)]">✓</div>
                          <div v-else class="w-8 h-8 rounded-full flex items-center justify-center font-medium text-[var(--color-primary-content)] bg-[color-mix(in_oklab,var(--color-primary)_70%,transparent)]">{{ index + 1 }}</div>
                        </div>
                        <div class="flex-1 min-w-0">
                          <div class="flex flex-wrap items-center justify-between gap-3">
                            <div class="min-w-0">
                              <div class="flex items-center gap-2 min-w-0">
                                <h3 class="font-medium truncate">{{ lesson.title }}</h3>
                                <badge v-if="getLessonProgress(lesson.id) >= 100" size="sm" variant="success">{{ t('student.courses.detail.completed') }}</badge>
                                <badge v-else size="sm" variant="secondary">{{ t('common.status.incomplete') || '未完成' }}</badge>
                              </div>
                              <p class="text-sm text-muted mt-1.5 whitespace-pre-line">{{ lesson.description || lesson.content }}</p>
                            </div>
                            <div class="flex items-center gap-2">
                              <Button size="sm" variant="success" @click="goLessonDetail(lesson.id)">
                                <template #icon>
                                  <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M10 3l7 7-7 7-1.5-1.5L13 11H3V9h10L8.5 4.5 10 3z"/></svg>
                                </template>
                                {{ t('student.courses.detail.viewDetail') }}
                              </Button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div v-for="(lesson, index) in lessons" :key="lesson.id" class="p-4 border rounded-xl glass-ultraThin">
                  <div class="flex items-start gap-4">
                    <div class="flex-shrink-0 mr-1">
                      <div v-if="getLessonProgress(lesson.id) >= 100" class="w-8 h-8 rounded-full flex items-center justify-center text-[var(--color-success-content)] bg-[color-mix(in_oklab,var(--color-success)_80%,transparent)]">✓</div>
                      <div v-else class="w-8 h-8 rounded-full flex items-center justify-center font-medium text-[var(--color-primary-content)] bg-[color-mix(in_oklab,var(--color-primary)_70%,transparent)]">{{ index + 1 }}</div>
                    </div>
                    <div class="flex-1 min-w-0">
                      <div class="flex flex-wrap items-center justify-between gap-3">
                            <div class="min-w-0">
                              <div class="flex items-center gap-2 min-w-0">
                                <h3 class="font-medium truncate">{{ lesson.title }}</h3>
                                <badge v-if="getLessonProgress(lesson.id) >= 100" size="sm" variant="success">{{ t('student.courses.detail.completed') }}</badge>
                                <badge v-else size="sm" variant="secondary">{{ t('common.status.incomplete') || '未完成' }}</badge>
                              </div>
                              <p class="text-sm text-muted mt-1.5 whitespace-pre-line">{{ lesson.description || lesson.content }}</p>
                            </div>
                        <div class="flex items-center gap-2">
                          <Button size="sm" variant="success" @click="goLessonDetail(lesson.id)">
                            <template #icon>
                              <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M10 3l7 7-7 7-1.5-1.5L13 11H3V9h10L8.5 4.5 10 3z"/></svg>
                            </template>
                            {{ t('student.courses.detail.viewDetail') }}
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </card>
        </div>
        
      </div>
    </div>

    <card v-else class="text-center py-12" tint="info">
      <h3 class="text-lg font-medium">{{ t('student.courses.detail.notFoundTitle') }}</h3>
      <p class="text-muted mt-2">{{ t('student.courses.detail.notFoundDesc') }}</p>
      <Button as="a" href="/student/courses" class="mt-4" variant="primary">
        <template #icon>
          <svg class="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M7 10l5 5V5l-5 5z"/></svg>
        </template>
        {{ t('student.courses.detail.backToList') }}
      </Button>
    </card>

    <!-- 学生资料弹窗挂载到页面根容器内 -->
    <student-profile-modal
      v-if="showProfile"
      :open="showProfile"
      :user-id="activeStudent?.id"
      :user-name="resolveName(activeStudent)"
      :user-avatar="activeStudent?.avatar"
      :course-id="(course as any)?.id || null"
      @close="closeStudentProfile"
      @contact="handleContact"
    />
  </div>
  
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import { useLessonStore } from '@/stores/lesson'
import Badge from '@/components/ui/Badge.vue'
import type { StudentLesson } from '@/types/lesson'
import { useChatStore } from '@/stores/chat'
import { useAuthStore } from '@/stores/auth'
import { studentApi } from '@/api/student.api'
import { lessonApi } from '@/api/lesson.api'
import { fileApi } from '@/api/file.api'
// @ts-ignore
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import Progress from '@/components/ui/Progress.vue'
import StudentProfileModal from '@/shared/views/StudentProfileModal.vue'
import AttachmentList from '@/features/shared/AttachmentList.vue'
import { getDifficultyVariant, getCategoryVariant, getTagVariant } from '@/shared/utils/badgeColor'
import { localizeDifficulty, localizeCategory } from '@/shared/utils/localize'
import apiClient, { baseURL } from '@/api/config'
import { userApi } from '@/api/user.api'
import { resolveUserDisplayName } from '@/shared/utils/user'
// 学生端不直接请求章节接口，基于课时列表推断章节结构

const route = useRoute()
const router = useRouter()
const courseStore = useCourseStore()
const lessonStore = useLessonStore()
const chat = useChatStore()
const auth = useAuthStore()
const { t, locale } = useI18n()

// State
const courseProgress = ref<number>(0)
const courseMaterials = ref<any[]>([])
const teacher = ref<any | null>(null)
const students = ref<any[]>([])
const studentsLoading = ref<boolean>(false)
const showProfile = ref(false)
const activeStudent = ref<any | null>(null)

// Computed
const course = computed(() => courseStore.currentCourse as any)
const lessons = computed(() => lessonStore.lessons as StudentLesson[])
const progressMap = ref<Record<string, number>>({})
const completedByProgressCount = computed(() => {
  return (lessons.value || []).filter((l: any) => {
    const id = String((l as any)?.id || '')
    const pm = Number(progressMap.value[id] || 0)
    const fallback = Number((l as any)?.progress || ((l as any)?.isCompleted ? 100 : 0) || 0)
    const p = Number.isFinite(pm) && pm > 0 ? pm : fallback
    return p >= 100
  }).length
})
// 兼容旧引用：统一用按进度统计的数量
const completedLessonsCount = computed(() => completedByProgressCount.value)
function getLessonProgress(id: string | number): number {
  try {
    const lid = String(id)
    const l: any = (lessons.value || []).find((x: any) => String(x.id) === lid)
    const pm = Number(progressMap.value[lid] || 0)
    const fallback = Number(l?.progress || (l?.isCompleted ? 100 : 0) || 0)
    const p = Number.isFinite(pm) && pm > 0 ? pm : fallback
    return Number.isFinite(p) ? Math.round(p) : 0
  } catch { return 0 }
}
const displayProgress = computed(() => {
  const api = Number(courseProgress.value || 0)
  const total = Number(lessons.value.length || 0)
  if (api > 0 || total === 0) return Math.round(api)
  const localPct = total > 0 ? Math.round((completedByProgressCount.value / total) * 100) : 0
  return localPct
})

const difficultyVariant = computed(() => getDifficultyVariant((course.value as any)?.difficulty))
const categoryVariant = computed(() => getCategoryVariant((course.value as any)?.category))
const localizedDifficulty = computed(() => localizeDifficulty((course.value as any)?.difficulty, t))
const localizedCategory = computed(() => localizeCategory((course.value as any)?.category, t))

const teacherName = computed(() => {
  const c = course.value
  return (teacher.value?.name) || c?.teacherName || c?.teacher?.name || c?.teacher?.username || ''
})

const teacherTitle = computed(() => {
  // 可能来自公开资料的职称/头衔保存在 nickname/subject/firstName+lastName 组合等，优先有含义字段
  const t = teacher.value as any
  return t?.title || t?.subject || [t?.firstName, t?.lastName].filter(Boolean).join(' ')
})

const fullName = computed(() => {
  const t = teacher.value as any
  const composite = [t?.lastName, t?.firstName].filter(Boolean).join('')
  return composite || teacherName.value
})

const genderLabel = computed(() => {
  const g = (teacher.value as any)?.gender
  if (!g) return ''
  const v = String(g).toLowerCase()
  if (v === 'male' || v === 'm' || v === '男') return t('common.gender.male') || '男'
  if (v === 'female' || v === 'f' || v === '女') return t('common.gender.female') || '女'
  return t('common.gender.other') || '其它'
})

const teacherBirthday = computed(() => {
  const b = (teacher.value as any)?.birthday
  if (!b) return ''
  try {
    const d = new Date(b)
    const lang = String(locale.value || 'zh-CN').toLowerCase()
    const isZh = lang.startsWith('zh')
    if (isZh) {
      const y = d.getFullYear(); const m = d.getMonth()+1; const day = d.getDate()
      return `${y}年${m}月${day}日`
    }
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch { return String(b) }
})

const resolvedTeacherAvatar = computed(() => {
  const c = course.value as any
  // 可能字段：teacher.avatar / teacherAvatar / avatar
  return (teacher.value?.avatar) || c?.teacherAvatar || c?.teacher?.avatar || null
})

const tagsArray = computed(() => {
  const raw = (course.value as any)?.tags
  if (!raw) return [] as string[]
  if (Array.isArray(raw)) return raw.filter(Boolean).map((x: any) => String(x))
  if (typeof raw === 'string') return raw.split(',').map(s => s.trim()).filter(Boolean)
  return [] as string[]
})

const groupedLessons = computed<Record<string, any[]>>(() => {
  const groups: Record<string, any[]> = {}
  for (const l of lessons.value || []) {
    const chap = (l as any).chapterId ? String((l as any).chapterId) : '__ungrouped__'
    if (!groups[chap]) groups[chap] = []
    groups[chap].push(l)
  }
  return groups
})

// 章节分组（仅从课时推断顺序与章节编号）
const groupedChapters = computed(() => {
  const buckets: Record<string, any[]> = {}
  for (const l of lessons.value || []) {
    const key = (l as any)?.chapterId ? String((l as any).chapterId) : '__ungrouped__'
    if (!buckets[key]) buckets[key] = []
    buckets[key].push(l)
  }
  const chapterKeys = Object.keys(buckets).filter(k => k !== '__ungrouped__')
  // 用每组内最小顺序值排序章节
  const orderOf = (it: any) => Number(it?.orderIndex ?? it?.order ?? it?.id ?? 0)
  chapterKeys.sort((a, b) => {
    const la = [...buckets[a]].sort((x:any,y:any)=> orderOf(x) - orderOf(y))[0]
    const lb = [...buckets[b]].sort((x:any,y:any)=> orderOf(x) - orderOf(y))[0]
    return orderOf(la) - orderOf(lb)
  })
  const isZh = String(locale.value || 'zh-CN').toLowerCase().startsWith('zh')
  const list: Array<{ key: string, title: string, items: any[] }> = []
  for (let i = 0; i < chapterKeys.length; i++) {
    const k = chapterKeys[i]
    const items = [...buckets[k]].sort((x:any,y:any)=> orderOf(x) - orderOf(y))
    const title = isZh ? `第${i + 1}章` : `Chapter ${i + 1}`
    list.push({ key: k, title, items })
  }
  const ungrouped = buckets['__ungrouped__'] || []
  if (ungrouped.length) {
    const items = [...ungrouped].sort((x:any,y:any)=> orderOf(x) - orderOf(y))
    list.push({ key: '__ungrouped__', title: t('student.courses.detail.noChapter') || '未分组', items })
  }
  return list
})

// 章节展开/收起状态（使用“折叠集合”，默认全部展开）
const collapsedChapters = ref<Set<string>>(new Set<string>())
function isExpanded(key: string): boolean {
  return !collapsedChapters.value.has(key)
}
function toggleChapter(key: string) {
  const set = new Set(collapsedChapters.value)
  if (set.has(key)) set.delete(key)
  else set.add(key)
  collapsedChapters.value = set
}

function resolveName(user: any): string {
  return String(user?.username || user?.userName || user?.name || user?.nickname || `#${user?.id || ''}`)
}

// 学生展示名优先级：姓氏+名字 > 昵称 > 用户名
function resolveStudentName(user: any): string { return resolveUserDisplayName(user) || resolveName(user) }

function formatDateOnly(v: any): string {
  try {
    const d = new Date(v)
    if (Number.isNaN(d.getTime())) return String(v)
    const lang = String(locale.value || 'zh-CN').toLowerCase()
    const isZh = lang.startsWith('zh')
    if (isZh) {
      const y = d.getFullYear()
      const m = d.getMonth() + 1
      const day = d.getDate()
      return `${y}年${m}月${day}日`
    }
    // 英文：Month D, YYYY
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch {
    return String(v)
  }
}

// Methods
function goLessonDetail(lessonId: string) { router.push(`/student/lessons/${lessonId}`) }

function contactTeacher() {
  const c = course.value
  if (!c) return
  const teacherId = (teacher.value?.id) || c.teacherId || c.teacher?.id
  const name = teacherName.value
  if (teacherId) chat.openChat(String(teacherId), name || null, String(c.id))
}

function openStudentProfile(student: any) {
  activeStudent.value = student
  showProfile.value = true
}

function closeStudentProfile() {
  showProfile.value = false
}

function contactStudent(payload?: { userId?: string | number, userName?: string | null }) {
  const c = course.value
  if (!c) return
  const id = payload?.userId ?? activeStudent.value?.id
  const name = payload?.userName ?? resolveName(activeStudent.value)
  if (id) chat.openChat(String(id), name || null, String((c as any).id))
  showProfile.value = false
}

function handleContact(payload: { userId: string | number, userName: string | null, courseId?: string | number | null }) {
  contactStudent({ userId: payload.userId, userName: payload.userName })
}

// Lifecycle
onMounted(async () => {
  const courseId = route.params.id as string
  if (!courseId) return
    await Promise.all([
      courseStore.fetchCourseById(courseId),
      lessonStore.fetchLessonsForCourse(courseId)
  ])
  try { await loadStudentProgress(courseId) } catch {}
  await fetchCourseProgress(courseId)
  await loadCourseMaterials(courseId)
  studentsLoading.value = true
  try { await loadTeacher(courseId) } finally { studentsLoading.value = false }
})

async function loadStudentProgress(courseId: string) {
  try {
    const res: any = await lessonApi.getStudentCourseProgressList(String(courseId))
    const arr = (res?.data || res || []) as any[]
    const map: Record<string, number> = {}
    for (const p of (Array.isArray(arr) ? arr : [])) {
      const lid = String(p.lessonId || p.lesson_id || p.lessonID || p.id || '')
      const val = Number(p.progress ?? p.percentage ?? 0)
      if (lid) map[lid] = Number.isFinite(val) ? Math.round(val) : 0
    }
    progressMap.value = map
  } catch { progressMap.value = {} }
}

async function fetchCourseProgress(courseId: string) {
  try {
    const res: any = await studentApi.getCourseProgress(courseId as any)
    const v = res?.progress ?? res?.data ?? res
    const n = Number(v || 0)
    courseProgress.value = Number.isFinite(n) ? Math.round(n) : 0
    if (courseProgress.value === 0) {
      try {
        const p: any = await lessonApi.getCourseProgressPercent(courseId)
        const pv = p?.data ?? p
        const pn = Number(pv || 0)
        if (Number.isFinite(pn) && pn > 0) courseProgress.value = Math.round(pn)
      } catch {}
    }
  } catch {
    const total = Number(lessons.value.length || 0)
    courseProgress.value = total > 0 ? Math.round((completedByProgressCount.value / total) * 100) : 0
  }
}

async function loadCourseMaterials(courseId: string) {
  try {
    const res: any = await fileApi.getRelatedFiles('course_material', Number(courseId))
    courseMaterials.value = res?.data || res || []
  } catch { courseMaterials.value = [] }
}

// 仅允许常见可在线预览的类型显示“浏览”按钮
function isPreviewable(file: any): boolean {
  const name = String(file?.originalName || file?.fileName || '').toLowerCase()
  const mime = String(file?.mimeType || file?.contentType || '').toLowerCase()
  const ext = name.split('.').pop() || ''
  const okExt = ['pdf','png','jpg','jpeg','webp','gif','mp4','webm','mp3','wav']
  if (okExt.includes(ext)) return true
  if (mime.startsWith('image/') || mime.startsWith('video/') || mime.startsWith('audio/')) return true
  if (mime === 'application/pdf') return true
  return false
}

async function openPreview(file: any) {
  const id = String(file?.id || file?.fileId || '')
  if (!id) return
  const tryFetch = async (path: string) => {
    const resp: any = await apiClient.get(path, { responseType: 'blob' })
    const blob: Blob = resp instanceof Blob
      ? resp
      : (resp?.data instanceof Blob
          ? resp.data
          : (resp && (resp.byteLength !== undefined || ArrayBuffer.isView(resp))
              ? new Blob([resp])
              : new Blob()))
    if (blob.type && blob.type.includes('application/json')) {
      try {
        const text = await blob.text()
        const j = JSON.parse(text)
        alert(String(j?.message || '预览失败'))
        return null
      } catch { return null }
    }
    return blob
  }
  try {
    let blob = await tryFetch(`/files/${encodeURIComponent(id)}/preview`)
    if (!blob) blob = await tryFetch(`/files/${encodeURIComponent(id)}/download`)
    if (!blob) return
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank', 'noopener')
    window.setTimeout(() => URL.revokeObjectURL(url), 10000)
  } catch {
    alert('预览失败')
  }
}

async function downloadFile(file: any) {
  const id = String(file?.id || file?.fileId || '')
  if (!id) return
  try {
    const resp: any = await apiClient.get(`/files/${encodeURIComponent(id)}/download`, { responseType: 'blob' })
    const blob: Blob = resp instanceof Blob
      ? resp
      : (resp?.data instanceof Blob
          ? resp.data
          : (resp && (resp.byteLength !== undefined || ArrayBuffer.isView(resp))
              ? new Blob([resp])
              : new Blob()))
    if (blob.type && blob.type.includes('application/json')) {
      try {
        const text = await blob.text()
        const j = JSON.parse(text)
        alert(String(j?.message || '下载失败'))
        return
      } catch { alert('下载失败'); return }
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = String(file?.originalName || file?.fileName || `file_${id}`)
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.setTimeout(()=> URL.revokeObjectURL(url), 10000)
  } catch { alert('下载失败') }
}

async function loadTeacher(courseId: string) {
  try {
    const r: any = await studentApi.getCourseParticipants(courseId)
    const teachers = (r?.data?.teachers) || (r?.teachers) || []
    const classmates = (r?.data?.classmates) || (r?.classmates) || []
    const basic = (teachers && teachers[0]) ? teachers[0] : null
    if (basic && basic.id) {
      try {
        const prof: any = await userApi.getProfileById(String(basic.id))
        // 合并：participants 基础字段 + 公开资料字段
        teacher.value = { ...basic, ...(prof?.data || prof || {}) }
      } catch {
        teacher.value = basic
      }
    } else {
      teacher.value = basic
    }
    // 学生列表仅保留必要字段，过滤当前登录用户
    const myId = (() => {
      try { return String(localStorage.getItem('userId') || '') } catch { return '' }
    })()
    const normalized = (classmates || []).map((s: any) => ({
      id: String(s.id || s.studentId || s.student_id || ''),
      name: s.name,
      firstName: s.firstName || s.first_name,
      lastName: s.lastName || s.last_name,
      nickname: s.nickname || s.nickName,
      username: s.username || s.userName || s.name,
      avatar: s.avatar || null
    })).filter((s: any) => s.id)
    students.value = normalized.filter((s: any) => !myId || String(s.id) !== myId)
  } catch { teacher.value = null }
}
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
