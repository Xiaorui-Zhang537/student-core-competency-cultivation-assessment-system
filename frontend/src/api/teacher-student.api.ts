import { api } from './config';

export interface TeacherStudentProfileDto {
  id: string | number;
  name: string;

  // users 表完整画像（可选字段）
  username?: string;
  avatar?: string;
  email?: string;
  emailVerified?: boolean;
  studentNo?: string;
  firstName?: string;
  lastName?: string;
  nickname?: string;
  gender?: string;
  bio?: string;
  birthday?: string | number | null;
  country?: string;
  province?: string;
  city?: string;
  phone?: string;
  school?: string;
  subject?: string;
  grade?: string;
  createdAt?: string | number | null;
  updatedAt?: string | number | null;

  // 汇总指标
  className?: string;
  enrolledCourseCount?: number;
  averageScore?: number | null;
  completionRate?: number | null; // 0-100
  lastAccessTime?: string | number | null;

  // 预留
  rank?: number | null;
  percentile?: number | null;
}

export const teacherStudentApi = {
  getStudentProfile(studentId: string) {
    return api.get<TeacherStudentProfileDto>(`/teachers/students/${studentId}`);
  },
  getStudentCourses(studentId: string) {
    return api.get<any[]>(`/teachers/students/${studentId}/courses`);
  },
  getStudentActivity(studentId: string, days = 7, limit = 5) {
    return api.get<{ lastAccessTime: string | number | null; weeklyStudyMinutes: number | null; recentLessons: Array<{ lessonId: number; lessonTitle: string; courseId: number; courseTitle: string; studiedAt: string | number; }>; }>(`/teachers/students/${studentId}/activity`, { params: { days, limit } });
  },
  getStudentAlerts(studentId: string) {
    return api.get<{ alerts: Array<{ code: string; message: string; severity: 'info'|'warn'|'critical' }> }>(`/teachers/students/${studentId}/alerts`)
  },
  getStudentRecommendations(studentId: string, limit = 6) {
    return api.get<Array<{ id:number; title:string; description:string; recommendationType:string; resourceUrl?:string; difficultyLevel?:string; estimatedTime?:string; priorityScore?:number; isRead?:boolean; isAccepted?:boolean }>>(`/teachers/students/${studentId}/recommendations`, { params: { limit } })
  },
  // 课程下学生基础列表（用于联系人列表）
  getCourseStudentsBasic(courseId: string, page = 1, size = 10000, keyword?: string) {
    const params: any = { courseId, page, size }
    if (keyword) params.keyword = keyword
    // 抑制控制台错误打印 + 将 400/403 视作空列表返回，避免在控制台产生噪音
    return api
      .get(`/teachers/students/basic`, { params, suppressLog: true })
      .catch((e: any) => {
        if (e && (e.code === 400 || e.code === 403)) {
          return { items: [] } as any
        }
        throw e
      })
  }
};
