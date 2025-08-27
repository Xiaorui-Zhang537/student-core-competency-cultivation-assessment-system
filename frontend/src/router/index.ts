import AuthLayout from '@/layouts/AuthLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
import TeacherLayout from '@/layouts/TeacherLayout.vue'
import NotFoundView from '@/components/layout/NotFoundView.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/auth/login'
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
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/features/student/views/DashboardView.vue')
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
        component: () => import('@/features/student/views/NotificationsView.vue')
      },
      {
        path: 'notifications/:id',
        name: 'StudentNotificationDetail',
        component: () => import('@/features/student/views/NotificationDetailView.vue'),
        props: true
      }
      ,
      {
        path: 'assistant',
        name: 'StudentAssistant',
        component: () => import('@/features/shared/views/AIAssistantView.vue'),
        meta: { requiresAuth: true }
      }
      // ... 其他学生路由
    ]
  },
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
        path: 'analytics',
        name: 'TeacherAnalytics',
        component: () => import('@/features/teacher/views/AnalyticsView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      
      {
        path: 'ai',
        name: 'TeacherAI',
        component: () => import('@/features/shared/views/AIAssistantView.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
      },
      {
        path: 'assistant',
        name: 'TeacherAssistant',
        component: () => import('@/features/shared/views/AIAssistantView.vue'),
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
        component: () => import('@/features/teacher/views/NotificationsView.vue')
      },
      {
        path: 'notifications/:id',
        name: 'TeacherNotificationDetail',
        component: () => import('@/features/teacher/views/NotificationDetailView.vue'),
        props: true
      }
      // ... 其他教师路由
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
      const dashboard = userRole === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard';
      return next(dashboard);
    }
  }

  if (to.meta.requiresGuest && isAuthenticated) {
    const dashboard = userRole === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard';
    return next(dashboard);
  }

  next();
});

export default router;
