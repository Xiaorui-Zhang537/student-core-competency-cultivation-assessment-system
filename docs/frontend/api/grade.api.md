# 前端 API：grade.api.ts

## 方法签名
- `gradeSubmission(data)` / `publishGrade(id)` / `gradeBatchSubmissions(grades)` / `publishBatchGrades(ids)`
- `getGradeById(id)` / `getGradeHistory(id)`
- `getGradeForStudentAssignment(studentId, assignmentId)`
- `getGradesByStudent(studentId, params)` / `getGradesByAssignment(assignmentId, params)`

## 注意事项
- 打分/发布需教师身份
- 历史/分页查询用于成绩演进展示
