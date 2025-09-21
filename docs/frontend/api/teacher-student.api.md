# 前端 API：teacher-student.api.ts

## 方法签名
- `getStudentProfile(studentId)` → `GET /teachers/students/{studentId}`
  - 返回 `TeacherStudentProfileDto`
- `getStudentCourses(studentId)` → `GET /teachers/students/{studentId}/courses`
  - 返回该学生所关联课程的精简列表
- `getStudentActivity(studentId, days=7, limit=5)` → `GET /teachers/students/{studentId}/activity`
  - 返回 `{ lastAccessTime, weeklyStudyMinutes, recentLessons: [{ lessonId, lessonTitle, courseId, courseTitle, studiedAt }] }`
- `getStudentAlerts(studentId)` → `GET /teachers/students/{studentId}/alerts`
  - 返回 `{ alerts: [{ code, message, severity }] }`
- `getStudentRecommendations(studentId, limit=6)` → `GET /teachers/students/{studentId}/recommendations`
  - 返回 `LearningRecommendation[]`
- `getCourseStudentsBasic(courseId, page=1, size=10000, keyword?)` → `GET /teachers/students/basic`
  - 请求参数：`{ courseId, page, size, keyword? }`
  - 特性：对 400/403 进行捕获并返回空列表 `{ items: [] }`，同时抑制控制台错误打印（`suppressLog: true`）

### 数据类型（节选）
```ts
export interface TeacherStudentProfileDto {
  id: string | number;
  name: string;
  // 基本画像（节选）
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
  country?: string; province?: string; city?: string;
  phone?: string; school?: string; subject?: string; grade?: string;
  createdAt?: string | number | null; updatedAt?: string | number | null;
  // 汇总指标
  className?: string;
  enrolledCourseCount?: number;
  averageScore?: number | null;
  completionRate?: number | null; // 0-100
  lastAccessTime?: string | number | null;
  // 预留
  rank?: number | null; percentile?: number | null;
}
```

## 最小示例
```ts
import { teacherStudentApi } from '@/api/teacher-student.api'

// 学生画像
const p = await teacherStudentApi.getStudentProfile('1001')

// 学生所修课程
const list = await teacherStudentApi.getStudentCourses('1001')

// 活跃度与最近学习
const act = await teacherStudentApi.getStudentActivity('1001', 7, 5)

// 风险预警与个性化建议
const alerts = await teacherStudentApi.getStudentAlerts('1001')
const recs = await teacherStudentApi.getStudentRecommendations('1001', 6)

// 课程下学生（联系人用途）
const basic = await teacherStudentApi.getCourseStudentsBasic('2001', 1, 10000, '张')
```

## 注意事项
- 接口面向教师端，权限与可见性取决于后端鉴权
- `getCourseStudentsBasic` 默认返回大页（1万）以满足联系人侧栏场景，如需更小分页可自行调整参数

## 参数与返回
- `TeacherStudentProfileDto` 见上节
- `getCourseStudentsBasic` 额外特性：当后端返回 400/403 时，文档实现将返回空列表 `{ items: [] }` 并抑制控制台报错（`suppressLog: true`）

## 示例
```ts
// 头像+昵称+班级
const profile = await teacherStudentApi.getStudentProfile('2001')

// 联系人侧边栏（大页取数）
const basic = await teacherStudentApi.getCourseStudentsBasic('1001', 1, 10000)
```
