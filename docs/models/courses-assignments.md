# 模型对照：课程 / 作业 / 提交 / 评分

> 表结构以 `schema.sql` 为准；前端类型以 `frontend/src/types/*` 为准。

## 课程（Course）
- TS：`types/course.ts` → `Course`、`CourseDetailed`、`CourseCreationRequest`、`CourseUpdateRequest`
- 字段要点：
  - `status`: `DRAFT | PUBLISHED | ARCHIVED`
  - `teacherName` 为 UI 便捷字段
  - 变更影响：`course.api.ts`、相关视图/Store

## 作业（Assignment）
- TS：`types/assignment.ts` → `Assignment`、`AssignmentCreationRequest`、`AssignmentUpdateRequest`
- 字段要点：
  - `dueDate`：`yyyy-MM-dd HH:mm:ss`（前端归一化）
  - 状态：`DRAFT | PUBLISHED | CLOSED`
  - 变更影响：`assignment.api.ts`、作业相关视图

## 提交（Submission）
- TS：`types/submission.ts` → `Submission`、`SubmissionRequest`、`DraftRequest`
- 字段要点：
  - `fileIds`: 关联上传文件
  - 状态：`SUBMITTED | LATE | GRADED`
  - 变更影响：`submission.api.ts`、提交/导出逻辑

## 评分（Grade）
- TS：`types/grade.ts` → `Grade`、`GradeRequest`、`GradePublishRequest`
- 字段要点：
  - `score`、`feedback`、`isPublished`
  - UI 便捷字段：`assignmentTitle/courseTitle/teacherName`
  - 变更影响：`grade.api.ts`、评分视图与报表
