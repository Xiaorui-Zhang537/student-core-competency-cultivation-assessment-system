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
  }
};
