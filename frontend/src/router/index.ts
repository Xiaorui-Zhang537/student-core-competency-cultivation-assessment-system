import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/auth/login'
    },
    // 认证路由
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
        }
      ]
    },
    // 学生端路由
    {
      path: '/student',
      component: () => import('@/layouts/StudentLayout.vue'),
      meta: { requiresAuth: true, role: 'student' },
      children: [
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
          name: 'CourseDetail',
          component: () => import('@/features/student/views/CourseDetailView.vue')
        },
        {
          path: 'assignments/:id/submit',
          name: 'AssignmentSubmit',
          component: () => import('@/features/student/views/AssignmentSubmitView.vue')
        },
        {
          path: 'grades',
          name: 'StudentGrades',
          component: () => import('@/features/student/views/GradesView.vue')
        },
        {
          path: 'ability',
          name: 'AbilityAnalysis',
          component: () => import('@/features/student/views/AbilityView.vue')
        },
        {
          path: 'community',
          name: 'Community',
          component: () => import('@/features/shared/views/CommunityView.vue')
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: () => import('@/features/shared/views/ProfileView.vue')
        }
      ]
    },
    // 教师端路由
    {
      path: '/teacher',
      component: () => import('@/layouts/TeacherLayout.vue'),
      meta: { requiresAuth: true, role: 'teacher' },
      children: [
        {
          path: '',
          redirect: '/teacher/dashboard'
        },
        {
          path: 'dashboard',
          name: 'TeacherDashboard',
          component: () => import('@/features/teacher/views/DashboardView.vue')
        },
        {
          path: 'courses',
          name: 'TeacherCourses',
          component: () => import('@/features/teacher/views/ManageCourseView.vue')
        },
        {
          path: 'courses/create',
          name: 'CreateCourse',
          component: () => import('@/features/teacher/views/CreateCourseView.vue')
        },
        {
          path: 'courses/:id',
          name: 'TeacherCourseDetail',
          component: () => import('@/features/teacher/views/CourseDetailView.vue')
        },
        {
          path: 'courses/:id/edit',
          name: 'EditCourse',
          component: () => import('@/features/teacher/views/EditCourseView.vue')
        },
        {
          path: 'courses/:id/students',
          name: 'CourseStudents',
          component: () => import('@/features/teacher/views/CourseStudentsView.vue')
        },
        {
          path: 'courses/:id/analytics',
          name: 'CourseAnalytics',
          component: () => import('@/features/teacher/views/CourseAnalyticsView.vue')
        },
        {
          path: 'grading',
          name: 'AssignmentGrading',
          component: () => import('@/features/teacher/views/ReviewAssignmentView.vue')
        },
        {
          path: 'assignments/:id/grade',
          name: 'GradeAssignment',
          component: () => import('@/features/teacher/views/GradeAssignmentView.vue')
        },
        {
          path: 'students',
          name: 'TeacherStudents',
          component: () => import('@/features/teacher/views/StudentAnalyticsView.vue')
        },
        {
          path: 'students/:id',
          name: 'StudentDetail',
          component: () => import('@/features/teacher/views/StudentDetailView.vue')
        },
        {
          path: 'analytics',
          name: 'TeacherAnalytics',
          component: () => import('@/features/teacher/views/AnalyticsView.vue')
        },
        {
          path: 'messages',
          name: 'TeacherMessages',
          component: () => import('@/features/teacher/views/MessagesView.vue')
        },
        {
          path: 'messages/compose',
          name: 'ComposeMessage',
          component: () => import('@/features/teacher/views/ComposeMessageView.vue')
        },
        {
          path: 'calendar',
          name: 'TeacherCalendar',
          component: () => import('@/features/teacher/views/CalendarView.vue')
        },
        {
          path: 'resources',
          name: 'TeachingResources',
          component: () => import('@/features/teacher/views/ResourcesView.vue')
        },
        {
          path: 'reports',
          name: 'TeacherReports',
          component: () => import('@/features/teacher/views/ReportsView.vue')
        },
        {
          path: 'notifications',
          name: 'TeacherNotifications',
          component: () => import('@/features/teacher/views/NotificationsView.vue')
        },
        {
          path: 'profile',
          name: 'TeacherProfile',
          component: () => import('@/features/shared/views/ProfileView.vue')
        },
        {
          path: 'settings',
          name: 'TeacherSettings',
          component: () => import('@/features/shared/views/SettingsView.vue')
        },
        {
          path: 'help',
          name: 'TeacherHelp',
          component: () => import('@/features/shared/views/HelpView.vue')
        }
      ]
    },
    // 404 页面
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/components/layout/NotFoundView.vue')
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 检查认证要求
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/auth/login')
    return
  }
  
  // 检查访客要求（已登录用户不能访问登录页）
  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    const userRole = authStore.user?.role
    next(userRole === 'teacher' ? '/teacher/dashboard' : '/student/dashboard')
    return
  }
  
  // 检查角色权限
  if (to.meta.role && authStore.user?.role !== to.meta.role) {
    next('/auth/login')
    return
  }
  
  next()
})

export default router 