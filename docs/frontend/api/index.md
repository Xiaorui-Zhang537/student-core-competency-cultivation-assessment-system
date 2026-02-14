# 前端 API 参考（按文件）

与 `src/api/*.ts` 一一对应，列出方法签名、参数与返回类型，并给出最小调用示例。

- `auth.api.ts` → 认证
- `user.api.ts` → 用户
- `course.api.ts` → 课程
- `lesson.api.ts` → 课时
- `chapter.api.ts` → 章节
- `assignment.api.ts` → 作业
- `submission.api.ts` → 提交
- `grade.api.ts` → 评分
- `student.api.ts` → 学生
- `teacher.api.ts` → 教师
- `teacher-student.api.ts` → 教师视角学生画像/列表
- `community.api.ts` → 社区
- `notification.api.ts` → 通知
- `file.api.ts` → 文件上传/下载
- `report.api.ts` → 举报/报表
- `admin.api.ts` → 管理员（仪表盘/课程/用户/数据中心/报告/治理/导出）
- `ai.api.ts` → AI 聊天
- `ability.api.ts` → 能力评估（学生端）

> 详细方法在对应子页查看（如 `frontend/api/lesson.api.md`、`frontend/api/admin.api.md` 等）。

## 学生端成绩分析（新增：雷达图与维度评语）

- 页面：`frontend/src/features/student/views/AnalysisView.vue`
- 前端复用组件：`components/charts/RadarChart.vue`
- 前端 API：`abilityApi.getStudentRadar`、`abilityApi.postStudentDimensionInsights`
- 后端接口：
  - `GET /ability/student/radar`（返回 dimensions、studentScores、classAvgScores）
  - `POST /ability/student/dimension-insights`（返回 items：name、analysis、suggestion、scoreA/scoreB、delta 等）
- 行为说明：
  - 学生只能查看自身数据；课程通过页面顶部“课程选择”选择，切换时刷新雷达与评语。
  - 维度名称后端固定中文返回，前端按教师端键 `teacher.analytics.weights.dimensions.*` 本地化。
  - 新增按钮：
    - 导出 PNG：导出当前雷达图快照至本地。
    - 导出 CSV：基于当前雷达指标与学生/班级两条曲线在前端合成 CSV 并下载。
    - 询问 AI：将当前课程、雷达数据与维度评语打包为 JSON 传递到学生助手页 `/student/assistant` 的查询参数 `q` 供 AI 上下文使用。
