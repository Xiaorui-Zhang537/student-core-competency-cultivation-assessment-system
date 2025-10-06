# 模型对照：课程 / 作业 / 提交 / 评分

> 表结构以 `schema.sql` 为准；前端类型以 `frontend/src/types/*` 为准。

## 课程（Course）
- TS：`types/course.ts` → `Course`、`CourseDetailed`、`CourseCreationRequest`、`CourseUpdateRequest`
- 字段要点：
  - `status`: `draft | published | archived`（与 schema.sql 的 enum 对齐；前端枚举建议使用大写映射以避免大小写歧义）
  - `teacherName` 为 UI 便捷字段
  - 变更影响：`course.api.ts`、相关视图/Store
  - 入课密钥策略：`require_enroll_key`（TINYINT）与 `enroll_key_hash`（VARCHAR）

## 作业（Assignment）
- TS：`types/assignment.ts` → `Assignment`、`AssignmentCreationRequest`、`AssignmentUpdateRequest`
- 字段要点：
  - `dueDate`：`yyyy-MM-dd HH:mm:ss`（前端归一化）
  - 状态：`draft | scheduled | published | closed`（与 schema.sql 的 enum 对齐）
  - 定时发布：`publish_at`（timestamp，可空）
  - 绑定节次：`assignment_type`（normal/course_bound）与 `lesson_id`（当 course_bound 时使用）
  - 变更影响：`assignment.api.ts`、作业相关视图

## 提交（Submission）
- TS：`types/submission.ts` → `Submission`、`SubmissionRequest`、`DraftRequest`
- 字段要点：
  - `fileIds`: 关联上传文件
  - 状态：`submitted | late | graded`
  - 变更影响：`submission.api.ts`、提交/导出逻辑

## 评分（Grade）
- TS：`types/grade.ts` → `Grade`、`GradeRequest`、`GradePublishRequest`
- 字段要点：
  - `score`、`max_score`、`percentage`、`grade_level`、`feedback`
  - 状态：`draft | published | returned`
  - UI 便捷字段：`assignmentTitle/courseTitle/teacherName`
  - 变更影响：`grade.api.ts`、评分视图与报表

> 注：本页与 `backend/src/main/resources/schema.sql` 对齐；如 schema 有变更，请同步更新此页以及 `frontend/src/types/*` 对应类型。
