import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/auth/login'
  },
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
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
    component: () => import('@/layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
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
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
    children: [
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
      }
      // ... 其他教师路由
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/components/layout/NotFoundView.vue')
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const token = localStorage.getItem('token');

  // If user is not loaded but token exists, try to fetch user
  if (!authStore.user && token) {
    await authStore.fetchUser();
  }

  const isAuthenticated = authStore.isAuthenticated;
  const userRole = authStore.userRole;

  if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      return next({ name: 'Login' });
    }
    if (to.meta.role && to.meta.role !== userRole) {
      // Redirect to their own dashboard if role does not match
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
