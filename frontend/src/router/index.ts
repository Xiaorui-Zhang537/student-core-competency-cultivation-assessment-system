import AuthLayout from '@/layouts/AuthLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
import TeacherLayout from '@/layouts/TeacherLayout.vue'
import NotFoundView from '@/components/layout/NotFoundView.vue'
import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
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
        component: () => import('@/features/teacher/views/AiAssistantView.vue'),
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
      }
      // ... 其他教师路由
    ]
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
