import AuthLayout from '@/layouts/AuthLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
import TeacherLayout from '@/layouts/TeacherLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import NotFoundView from '@/components/layout/NotFoundView.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/PublicLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/features/shared/views/HomeView.vue') },
    ]
  },
  // 兼容旧 home/* 路由：全部重定向到锚点
  { path: '/home/overview', redirect: '/' },
  { path: '/home/features', redirect: '/#features' },
  { path: '/home/compare', redirect: '/#compare' },
  { path: '/home/timeline', redirect: '/#timeline' },
  { path: '/home/journey', redirect: '/#timeline' },
  { path: '/home/structure', redirect: '/#structure' },
  { path: '/home/marquee', redirect: '/#marquee' },
  // 兼容旧地址 /student/analytics → 新的 /student/analysis
  {
    path: '/student/analytics',
    redirect: '/student/analysis'
  },
  {
    path: '/auth',
    component: AuthLayout,
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/features/auth/views/LoginView.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/features/auth/views/RegisterView.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/features/auth/views/ForgotPasswordView.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'reset-password',
        name: 'ResetPassword',
        component: () => import('@/features/auth/views/ResetPasswordView.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'verify-email',
        name: 'VerifyEmail',
        component: () => import('@/features/auth/views/VerifyEmailView.vue'),
        meta: { requiresGuest: true }
      },
      {
        path: 'check-email',
        name: 'CheckEmail',
        component: () => import('@/features/auth/views/CheckEmailView.vue'),
        meta: { requiresGuest: true }
      },
    ]
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: '',
        redirect: '/student/dashboard'
      },
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/features/student/views/DashboardView.vue')
      },
      {
        path: 'courses',
        name: 'StudentCourses',
        component: () => import('@/features/student/views/CoursesView.vue')
      },
      {
        path: 'courses/:id',
        name: 'StudentCourseDetail',
        component: () => import('@/features/student/views/CourseDetailView.vue'),
        props: true
      },
      {
        path: 'lessons/:id',
        name: 'StudentLessonDetail',
        component: () => import('@/features/student/views/LessonDetailView.vue'),
        props: true
      },
      {
        path: 'assignments',
        name: 'StudentAssignments',
        component: () => import('@/features/student/views/AssignmentsView.vue')
      },
      {
        path: 'assignments/:id/submit',
        name: 'StudentAssignmentSubmit',
        component: () => import('@/features/student/views/AssignmentSubmitView.vue'),
        props: true
      },
      // 合并为成绩分析
      {
        path: 'analysis',
        name: 'StudentAnalysis',
        component: () => import('@/features/student/views/AnalysisView.vue')
      },
      {
        path: 'grades',
        name: 'StudentGrades',
        redirect: { name: 'StudentAnalysis' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/features/shared/views/ProfileView.vue')
      },
      {
        path: 'community',
        name: 'StudentCommunity',
        component: () => import('@/features/shared/views/CommunityView.vue')
      },
      {
        path: 'community/post/:id',
        name: 'StudentPostDetail',
        component: () => import('@/features/shared/views/PostDetailView.vue'),
        props: true
      },
      {
        path: 'notifications',
        name: 'StudentNotifications',
        component: () => import('@/features/shared/views/NotificationsView.vue')
      },
      {
        path: 'notifications/:id',
        name: 'StudentNotificationDetail',
        component: () => import('@/features/shared/views/NotificationDetailView.vue'),
        props: true
      }
      ,
      {
        path: 'assistant',
        name: 'StudentAssistant',
        component: () => import('@/features/shared/views/AIAssistantView.vue'),
        meta: { requiresAuth: true }
      }
      ,
      {
        path: 'assistant/voice',
        name: 'StudentAssistantVoice',
        component: () => import('@/features/shared/views/VoicePracticeView.vue'),
        meta: { requiresAuth: true }
      }
      ,
      {
        path: 'help',
        name: 'StudentHelp',
        component: () => import('@/features/shared/views/HelpView.vue'),
        meta: { requiresAuth: true }
      }
      // ... 其他学生路由
    ]
  },
  // 学生首页已移除，统一使用 /student/dashboard
  {
    path: '/teacher',
    component: TeacherLayout,
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/features/teacher/views/DashboardView.vue')
      },
      {
        path: 'profile',
        name: 'TeacherProfile',
        component: () => import('@/features/shared/views/ProfileView.vue')
      },
      {
        path: 'profile/basic',
        name: 'TeacherProfileBasic',
        component: () => import('@/features/teacher/views/TeacherBasicInfoView.vue')
      },
      {
        path: 'community',
        name: 'TeacherCommunity',
        component: () => import('@/features/shared/views/CommunityView.vue')
      },
      {
        path: 'community/post/:id',
        name: 'TeacherPostDetail',
        component: () => import('@/features/shared/views/PostDetailView.vue'),
        props: true
      },
      {
        path: 'courses',
        name: 'TeacherCourseManagement',
        component: () => import('@/features/teacher/views/ManageCourseView.vue'),
      },
      {
        path: 'assignments',
        name: 'TeacherAssignments',
        component: () => import('@/features/teacher/views/ReviewAssignmentView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'ai-grading',
        name: 'TeacherAIGrading',
        component: () => import('@/features/teacher/views/TeacherAIGradingView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'ai-grading/history',
        name: 'TeacherAIGradingHistory',
        component: () => import('@/features/teacher/views/TeacherAIGradingHistoryView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'courses/:id/assignments',
        name: 'TeacherCourseAssignments',
        component: () => import('@/features/teacher/views/ReviewAssignmentView.vue'),
        props: true,
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'assignments/:assignmentId/submissions',
        name: 'TeacherAssignmentSubmissions',
        component: () => import('@/features/teacher/views/AssignmentSubmissionsView.vue'),
        props: true,
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'assignments/:assignmentId/submissions/:submissionId/grade',
        name: 'TeacherGradeAssignment',
        component: () => import('@/features/teacher/views/GradeAssignmentView.vue'),
        props: true,
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'assignments/:assignmentId/students/:studentId/grade',
        name: 'TeacherGradeAssignmentByStudent',
        component: () => import('@/features/teacher/views/GradeAssignmentView.vue'),
        props: true,
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'analytics',
        name: 'TeacherAnalytics',
        component: () => import('@/features/teacher/views/AnalyticsView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      
      {
        path: 'assistant',
        name: 'TeacherAssistant',
        component: () => import('@/features/shared/views/AIAssistantView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'assistant/voice',
        name: 'TeacherAssistantVoice',
        component: () => import('@/features/shared/views/VoicePracticeView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'courses/:id/students',
        name: 'TeacherCourseStudents',
        component: () => import('@/features/teacher/views/CourseStudentsView.vue'),
        props: true,
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'students/:studentId',
        name: 'TeacherStudentDetail',
        component: () => import('@/features/teacher/views/StudentDetailView.vue'),
        props: true,
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'courses/:id',
        name: 'TeacherCourseDetail',
        component: () => import('@/features/teacher/views/CourseDetailView.vue'),
        props: true
      },
      {
        path: 'notifications',
        name: 'TeacherNotifications',
        component: () => import('@/features/shared/views/NotificationsView.vue')
      },
      {
        path: 'notifications/:id',
        name: 'TeacherNotificationDetail',
        component: () => import('@/features/shared/views/NotificationDetailView.vue'),
        props: true
      }
      ,
      {
        path: 'help',
        name: 'TeacherHelp',
        component: () => import('@/features/shared/views/HelpView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      }
      // ... 其他教师路由
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/console' },
      {
        path: 'console',
        name: 'AdminConsole',
        component: () => import('@/features/admin/views/ConsoleView.vue')
      },
      {
        path: 'people',
        name: 'AdminPeople',
        component: () => import('@/features/admin/views/PeopleView.vue')
      },
      {
        path: 'moderation',
        name: 'AdminModeration',
        component: () => import('@/features/admin/views/ModerationView.vue')
      },
      {
        path: 'moderation/center',
        name: 'AdminModerationCenter',
        component: () => import('@/features/shared/views/CommunityView.vue')
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/features/admin/views/CoursesView.vue')
      },
      {
        path: 'courses/:id',
        name: 'AdminCourseDetail',
        component: () => import('@/features/admin/views/CourseDetailView.vue'),
        props: true
      },
      {
        path: 'tools',
        name: 'AdminTools',
        component: () => import('@/features/admin/views/ToolsView.vue')
      },
      {
        path: 'courses/:courseId/students/:studentId',
        name: 'AdminCourseStudentDetail',
        component: () => import('@/features/admin/views/StudentDetailView.vue'),
        props: true
      },
      {
        path: 'students/:id',
        name: 'AdminStudentDetail',
        component: () => import('@/features/admin/views/StudentDetailView.vue'),
        props: true
      },
      {
        path: 'teachers/:id',
        name: 'AdminTeacherDetail',
        component: () => import('@/features/admin/views/TeacherDetailView.vue'),
        props: true
      },
      {
        path: 'audit/:userId',
        name: 'AdminAiVoiceAudit',
        component: () => import('@/shared/views/AiVoiceAuditView.vue'),
        props: true
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/features/shared/views/ProfileView.vue')
      },
      {
        path: 'help',
        name: 'AdminHelp',
        component: () => import('@/features/shared/views/HelpView.vue'),
        meta: { requiresAuth: true }
      },

      // 旧入口兼容：重定向到新聚合页面（保留 query）
      { path: 'dashboard', redirect: (to: any) => ({ path: '/admin/console', query: to.query }) },
      { path: 'analytics', redirect: (to: any) => ({ path: '/admin/console', query: { ...to.query, panel: 'analytics' } }) },
      { path: 'users', redirect: (to: any) => ({ path: '/admin/people', query: { ...to.query, tab: 'users' } }) },
      { path: 'students', redirect: (to: any) => ({ path: '/admin/people', query: { ...to.query, tab: 'students' } }) },
      { path: 'teachers', redirect: (to: any) => ({ path: '/admin/people', query: { ...to.query, tab: 'teachers' } }) },
      { path: 'reports', redirect: (to: any) => ({ path: '/admin/moderation', query: to.query }) },
      { path: 'community', redirect: (to: any) => ({ path: '/admin/moderation', query: to.query }) },
      { path: 'exports', redirect: (to: any) => ({ path: '/admin/tools', query: to.query }) },
    ]
  },
  // 兼容旧路径：/assistant → 按角色重定向到嵌套路由，保留查询参数
  {
    path: '/assistant',
    name: 'AssistantCompat',
    redirect: (to: any) => {
      const authStore = useAuthStore();
      const isAuthed = authStore.isAuthenticated;
      const role = authStore.userRole;
      if (isAuthed && role === 'TEACHER') {
        return { path: '/teacher/assistant', query: to.query };
      }
      if (isAuthed) {
        return { path: '/student/assistant', query: to.query };
      }
      return { name: 'Login', query: { redirect: to.fullPath, ...to.query } };
    }
  },
  // 帮助页兼容路由：根据角色/登录态重定向到嵌套路由
  {
    path: '/help',
    name: 'HelpCompat',
    redirect: (to: any) => {
      const authStore = useAuthStore();
      const isAuthed = authStore.isAuthenticated;
      const role = authStore.userRole;
      if (isAuthed && role === 'TEACHER') {
        return { path: '/teacher/help', query: to.query };
      }
      if (isAuthed) {
        return { path: '/student/help', query: to.query };
      }
      return { name: 'Login', query: { redirect: to.fullPath, ...to.query } };
    }
  },
  // 文档站路由：跳转到外部 docs（由 VITE_DOCS_URL 控制）
  {
    path: '/docs',
    name: 'Docs',
    component: () => import('@/features/shared/views/DocsRedirectView.vue'),
    beforeEnter: (_to: any, _from: any, next: (v?: any) => void) => {
      try {
        const url = (import.meta as any).env.VITE_DOCS_URL as string | undefined
        if (url) {
          window.location.replace(url)
          return
        }
      } catch {}
      next()
    }
  },
  // 顶层兼容：邮件链接 /reset-password?token=xxx → 重定向到 /auth/reset-password 保留查询参数
  {
    path: '/reset-password',
    name: 'ResetPasswordAlias',
    redirect: (to: any) => ({ path: '/auth/reset-password', query: to.query })
  },
  {
    path: '/verify-email',
    name: 'VerifyEmailAlias',
    redirect: (to: any) => ({ path: '/auth/verify-email', query: to.query })
  },
  {
    path: '/check-email',
    name: 'CheckEmailAlias',
    redirect: (to: any) => ({ path: '/auth/check-email', query: to.query })
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFoundView
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach((to, from, next) => {
  // 一次性硬刷新 mail-check 页面，避免 SPA 首跳白屏
  if (to.path === '/auth/check-email') {
    const rf = (to.query as any)?._rf
    const already = Array.isArray(rf) ? rf[0] === '1' : rf === '1'
    if (!already) {
      try {
        const base = (import.meta as any).env.BASE_URL || '/'
        const url = new URL(window.location.href)
        url.pathname = base.replace(/\/$/, '/') + 'auth/check-email'
        url.searchParams.set('_rf', '1')
        const lang = (to.query as any)?.lang
        if (lang) url.searchParams.set('lang', String(lang))
        window.location.replace(url.toString())
        return
      } catch {
        // 兜底：若构造 URL 失败，回退到前端路由 replace，再由页面内逻辑刷新
        return next({ path: '/auth/check-email', query: { ...to.query, _rf: '1' }, replace: true })
      }
    }
  }

  const authStore = useAuthStore();
  const isAuthenticated = authStore.isAuthenticated;
  const userRole = authStore.userRole;

  if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      return next({ name: 'Login', query: { redirect: to.fullPath } });
    }
    if (to.meta.role && to.meta.role !== userRole) {
      const dashboard =
        userRole === 'TEACHER'
          ? '/teacher/dashboard'
          : userRole === 'ADMIN'
            ? '/admin/dashboard'
            : '/student/dashboard';
      return next(dashboard);
    }
  }

  if (to.meta.requiresGuest && isAuthenticated) {
    const dashboard =
      userRole === 'TEACHER'
        ? '/teacher/dashboard'
        : userRole === 'ADMIN'
          ? '/admin/dashboard'
          : '/student/dashboard';
    return next(dashboard);
  }

  next();
});

// 从登录页返回首页时，强制刷新一次，避免浏览器 bfcache 导致的白屏/旧状态
router.afterEach((to, from) => {
  try {
    const toName = (to as any)?.name
    const fromName = (from as any)?.name
    const comingBackToHome = toName === 'Home' && (fromName === 'Login')
    if (comingBackToHome) {
      setTimeout(() => { try { window.location.reload() } catch {} }, 0)
    }
  } catch {}
});

export default router;
