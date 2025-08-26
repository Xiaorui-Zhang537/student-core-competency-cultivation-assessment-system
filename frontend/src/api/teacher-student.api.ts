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
    return api.get(`/teachers/students/basic`, { params })
  }
};
