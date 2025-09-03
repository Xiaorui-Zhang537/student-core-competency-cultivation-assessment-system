import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { studentApi } from '@/api/student.api';
import type { StudentDashboardData, StudentCourse, StudentLesson, StudentSubmission } from '@/types/student';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useStudentStore = defineStore('student', () => {
  const uiStore = useUIStore();

  // 默认仪表盘结构，避免模板首次渲染时报 undefined
  const defaultDashboardData: StudentDashboardData = {
    upcomingAssignments: [],
    activeCourses: [],
    recentGrades: [],
    overallProgress: 0,
    stats: {
      activeCourses: 0,
      pendingAssignments: 0,
      averageScore: 0,
      totalStudyTime: 0,
      weeklyStudyTime: 0,
      unreadNotifications: 0,
    },
  };

  const dashboardData = ref<StudentDashboardData>(defaultDashboardData);
  const myCourses = ref<StudentCourse[]>([]);
  const currentCourseProgress = ref<StudentCourse | null>(null);
  const currentLesson = ref<StudentLesson | null>(null);
  const mySubmissions = ref<StudentSubmission[]>([]);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchDashboardData = async () => {
    const response = await handleApiCall(
      () => studentApi.getDashboardData(),
      uiStore,
      '获取仪表盘数据失败'
    );
    if (response) {
      // 适配后端 StudentDashboardResponse 到前端期望结构
      const stats = (response as any)?.stats || {};
      const recentCourses = (response as any)?.recentCourses || [];
      const pendingAssignments = (response as any)?.pendingAssignments || [];
      const recentNotifications = (response as any)?.recentNotifications || [];

      let mapped: StudentDashboardData = {
        overallProgress: Number(stats.overallProgress || 0),
        activeCourses: recentCourses.map((c: any) => ({
          id: String(c.id),
          title: c.title,
          teacherName: c.teacherName,
          progress: Number(c.progress || 0),
          coverImage: c.coverImage || c.coverImageUrl,
        })),
        upcomingAssignments: pendingAssignments.map((a: any) => ({
          id: String(a.id),
          title: a.title,
          courseTitle: a.courseTitle,
          dueDate: a.dueDate,
        })),
        recentGrades: ((response as any)?.recentGrades || []).map((g: any) => ({
          assignmentTitle: g.assignmentTitle,
          courseTitle: g.courseTitle,
          score: Number(g.score || 0),
          gradedAt: g.gradedAt,
        })),
        stats: {
          activeCourses: Number(stats.activeCourses || 0),
          pendingAssignments: Number(stats.pendingAssignments || 0),
          averageScore: Number(stats.averageScore || 0),
          totalStudyTime: Number(stats.totalStudyTime || 0),
          weeklyStudyTime: Number(stats.weeklyStudyTime || 0),
          unreadNotifications: Number(stats.unreadNotifications || 0),
        },
      } as StudentDashboardData;

      // Fallback：若后端 recentCourses 为空，则回退到分页我的课程接口
      if (!mapped.activeCourses || mapped.activeCourses.length === 0) {
        const paged = await handleApiCall(
          () => studentApi.getMyCourses({ page: 1, size: 5 }),
          uiStore,
          '获取我的课程失败'
        );
        if (paged) {
          const items = (paged as any)?.items ?? (Array.isArray(paged) ? paged : []);
          const fallbackCourses = (items || []).slice(0, 5).map((c: any) => ({
            id: String(c.id),
            title: c.title,
            teacherName: c.teacherName,
            progress: Number(c.progress || 0),
            coverImage: c.coverImage || c.coverImageUrl,
          }));
          if (fallbackCourses.length > 0) {
            mapped = { ...mapped, activeCourses: fallbackCourses };
          }
        }
      }

      dashboardData.value = { ...defaultDashboardData, ...mapped };
    }
  };

  const fetchMyCourses = async (params?: { page?: number; size?: number; q?: string }) => {
    // 仅在 q 为非空字符串时传递，避免后端将空串当作过滤条件
    const p: { page?: number; size?: number; q?: string } = { page: params?.page, size: params?.size }
    if (params?.q && params.q.trim().length > 0) p.q = params.q.trim()

    const response = await handleApiCall(
      () => studentApi.getMyCourses(p),
      uiStore,
      '获取我的课程失败'
    );
    if (response) {
      const items = (response as any)?.items ?? (Array.isArray(response) ? response : []);
      myCourses.value = (items || []).map((c: any) => ({
        id: String(c.id),
        title: c.title || '',
        description: c.description || '',
        teacherName: c.teacherName || '',
        category: c.category || '',
        coverImageUrl: c.coverImageUrl || c.coverImage || '',
        progress: Number(c.progress ?? 0),
        enrolledAt: c.enrolledAt || ''
      })) as any;
    }
  };

  const fetchCourseProgress = async (courseId: string) => {
    const response = await handleApiCall(
      () => studentApi.getCourseProgress(courseId),
      uiStore,
      '获取课程进度失败'
    );
    if (response) {
      currentCourseProgress.value = response;
    }
  };

  const fetchLessonDetails = async (lessonId: string) => {
    const response = await handleApiCall(
      () => studentApi.getLessonDetails(lessonId),
      uiStore,
      '获取课程详情失败'
    );
    if (response) {
      currentLesson.value = response;
    }
  };
  
  const toggleLessonCompleted = async (lessonId: string, completed: boolean) => {
      const apiCall = completed 
          ? () => studentApi.markLessonAsCompleted(lessonId)
          : () => studentApi.markLessonAsIncomplete(lessonId);
          
      await handleApiCall(apiCall, uiStore, '更新课程状态失败');
      
      // Refresh course progress after update
      if(currentCourseProgress.value) {
          await fetchCourseProgress(String(currentCourseProgress.value.id));
      }
  }


  return {
    dashboardData,
    myCourses,
    currentCourseProgress,
    currentLesson,
    mySubmissions,
    loading,
    fetchDashboardData,
    fetchMyCourses,
    fetchCourseProgress,
    fetchLessonDetails,
    toggleLessonCompleted,
  };
});
