import { api } from './config';
import type { PaginatedResponse } from '@/types/api';
import type { StudentDashboardData, StudentCourse, StudentLesson, StudentSubmission } from '@/types/student';
// 临时类型以便前端联调，不影响现有类型定义
type StudentAnalysis = {
  kpi: { avgScore: number; completionRate: number; studyHours: number; activeDays: number };
  radar: { invest: number; quality: number; mastery: number; stability: number; growth: number };
  trends: { score: Array<{ x: string; y: number }>; completion: Array<{ x: string; y: number }>; hours: Array<{ x: string; y: number }> };
  recentGrades: Array<{ assignmentTitle: string; courseTitle: string; score: number; gradedAt: string }>;
};

export const studentApi = {
  getDashboardData: (): Promise<StudentDashboardData> => {
    return api.get('/students/dashboard');
  },

  // 分页我的课程（包含 progress/teacherName）
  getMyCourses: (params?: { page?: number; size?: number; q?: string }): Promise<PaginatedResponse<StudentCourse>> => {
    return api.get('/students/my-courses/paged', { params });
  },

  getCourseProgress: (courseId: string): Promise<StudentCourse> => {
    return api.get(`/students/courses/${courseId}/progress`);
  },

  getLessonDetails: (lessonId: string): Promise<StudentLesson> => {
    return api.get(`/students/lessons/${lessonId}`);
  },

  // NOTE: backend returns a PageResult<Submission> (not a plain array).
  getMySubmissions: (params?: { page?: number; size?: number; courseId?: string | number }): Promise<{ items: StudentSubmission[]; page: number; size: number; total: number; totalPages: number }> => {
    return api.get('/students/my-submissions', { params });
  },

  // 新增：学生成绩分析聚合
  getAnalysis: (params?: { range?: '7d' | '30d' | 'term' }): Promise<StudentAnalysis> => {
    return api.get('/students/analysis', { params });
  },

  // 新增：学生端获取课程参与者（教师与同学）
  getCourseParticipants: (courseId: string, keyword?: string): Promise<any> => {
    return api.get(`/students/courses/${courseId}/participants`, { params: { keyword } });
  },

  // 新增：学生端获取作业列表（过滤 + 分页）
  getAssignments: (params?: { courseId?: string; status?: string; q?: string; page?: number; size?: number; includeHistory?: boolean; onlyPending?: boolean; excludeCourseBound?: boolean }): Promise<any> => {
    return api.get('/students/assignments', { params });
  },
};
