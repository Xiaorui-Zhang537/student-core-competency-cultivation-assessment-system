# 前端 API：teacher-student.api.ts

## 方法签名
- `getStudentProfile(studentId)` → `GET /teachers/students/{studentId}`
  - 返回 `TeacherStudentProfileDto`
- `getStudentCourses(studentId)` → `GET /teachers/students/{studentId}/courses`
  - 返回该学生所关联课程的精简列表
- `getCourseStudentsBasic(courseId, page=1, size=10000, keyword?)` → `GET /teachers/students/basic`
  - 请求参数：`{ courseId, page, size, keyword? }`
  - 特性：对 400/403 进行捕获并返回空列表 `{ items: [] }`，同时抑制控制台错误打印（`suppressLog: true`）

### 数据类型（节选）
```ts
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
```

## 最小示例
```ts
import { teacherStudentApi } from '@/api/teacher-student.api'

// 学生画像
const p = await teacherStudentApi.getStudentProfile('1001')

// 学生所修课程
const list = await teacherStudentApi.getStudentCourses('1001')

// 课程下学生（联系人用途）
const basic = await teacherStudentApi.getCourseStudentsBasic('2001', 1, 10000, '张')
```

## 注意事项
- 接口面向教师端，权限与可见性取决于后端鉴权
- `getCourseStudentsBasic` 默认返回大页（1万）以满足联系人侧栏场景，如需更小分页可自行调整参数
