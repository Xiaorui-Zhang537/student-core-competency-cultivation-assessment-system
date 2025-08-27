import { api } from './config';

export interface TeacherStudentProfileDto {
  id: string | number;
  name: string;
  avatar?: string;
  email?: string;
  className?: string;
  enrolledCourseCount?: number;
  averageScore?: number;
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
