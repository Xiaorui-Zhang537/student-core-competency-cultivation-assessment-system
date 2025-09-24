# 五维雷达数据口径与流转规范（教师端/学生端）

更新时间：2025-09-21

## 维度与顺序
- 固定五维（中文名，顺序）：
  1) 道德认知（MORAL_COGNITION）
  2) 学习态度（LEARNING_ATTITUDE）
  3) 学习能力（LEARNING_ABILITY）
  4) 学习方法（LEARNING_METHOD）
  5) 学习成绩（ACADEMIC_GRADE）

后端 `AbilityAnalyticsServiceImpl` 以以上顺序返回 `dimensions`、`studentScores`、`classAvgScores`，前端雷达图以 `max=100` 渲染。

## 量纲与归一化
- 非成绩四维：前端写入刻度为 0–5；后端在 `ability_assessments.max_score=5` 记录；分析端以 `score/max_score*100` 归一化为百分制。
- 学习成绩维度：分析端直接基于 `grades` 汇总并归一化（0–100），与四维保持同一百分制。

## 数据写入（教师端批改发布时自动写入）
- 四个能力维度（AI）：`assessment_type=assignment_ai`，`score=0..5`，`max_score=5`，`related_id=assignment.id`，`evidence=AI整体反馈`。
- 学习成绩维度：不依赖 `ability_assessments`，分析端直接取 `grades`；若写入 `ability_assessments`，也应使用 `score=0..5`，`max_score=5`，`related_id=assignment.id`（便于按课程筛选）。

注意：为保证课程筛选有效，`ability_assessments.related_id` 必须写入对应的 `assignment.id`。分析 SQL 通过 `aa.related_id = assignments.id` 关联以应用 `courseId` 条件。此前写入 `submission.id` 或 `grade.id` 将导致无法匹配课程，造成雷达四维为 0 的问题。

## 实体与表结构
- `ability_dimensions` 新增 `code` 列（唯一），用于稳定映射：
  - `MORAL_COGNITION` / `LEARNING_ATTITUDE` / `LEARNING_ABILITY` / `LEARNING_METHOD` / `ACADEMIC_GRADE`
- 前端 `AbilityDimension` 类型补充 `code` 字段。

## 前端渲染
- 雷达图组件 `RadarChart.vue` 以 `indicators.max=100` 与 `series` 百分制值渲染；支持 Tooltip。
- 教师端 `AnalyticsView.vue`、学生端 `AnalysisView.vue` 从接口 `dimensions` 数组构建指标，顺序保持与后端一致。

### 新增：中英图例说明（v1.2）

- 共享组件：`frontend/src/shared/views/AbilityRadarLegend.vue`
  - 数据源：i18n 键 `shared.radarLegend.dimensions.{CODE}`
  - 展示内容：
    - `what`（考查内容 / What it examines）
    - `abilities`（展示能力 / Abilities shown）
    - `scoring`（评分逻辑 / Scoring logic）
  - 集成位置：
    - 教师端：`features/teacher/views/AnalyticsView.vue` 雷达图下方
    - 学生端：`features/student/views/AbilityView.vue` 雷达图下方

### i18n 键位

- `frontend/src/locales/zh-CN/shared.json` 与 `en-US/shared.json` 新增 `shared.radarLegend.*`：
  - `labels.title/expandAll/collapseAll/what/abilities/scoring`
  - `dimensions.MORAL_COGNITION | LEARNING_ATTITUDE | LEARNING_ABILITY | LEARNING_METHOD | ACADEMIC_GRADE`


## 兼容性与历史数据
- 历史写入若使用了 `submission.id` 或 `grade.id`，课程过滤下不会计入四维均分。可视需要通过一次性脚本迁移 `ability_assessments.related_id` → 对应的 `assignment.id`。

## 五维能力雷达数据规范（v1.1）

- 维度顺序（固定）：道德认知（MORAL_COGNITION）、学习态度（LEARNING_ATTITUDE）、学习能力（LEARNING_ABILITY）、学习方法（LEARNING_METHOD）、学习成绩（ACADEMIC_GRADE）。
- 量纲：聚合输出统一为百分制（0–100）；教师写入时四维/成绩采用 0–5 刻度写入，后端保存 `max_score=5`，聚合时统一归一化为百分制。
- 写入口径：
  - 四维：来源于 AI 规范化结果 `overall.dimension_averages`（0–5），assessmentType='assignment'，relatedId=assignmentId。
  - 学业成绩：按 grade 百分比换算为 0–5（score5 = round((percentage/20), 1)），assessmentType='assignment'，relatedId=gradeId。
  - assessed_at：所有写入均设置为当前时间；历史为空时以 `created_at` 回填。
- 分析口径：
  - 后端 `AbilityAnalyticsServiceImpl` 聚合：维度表均取 `ability_assessments` 平均分并按 `max_score` 归一化为百分制；“学习成绩”维度单独取 `grades` 表均值。
  - 课程权重：`course_ability_weights` 按 `dimension_code` 读取，默认等权。
- 前端渲染：
  - `RadarChart.vue` 接收 `indicators` 与 `series`；教师端/学生端统一本地化维度名。
  - 老数据修复：确保四维可见需回填 `assessed_at`。

### 数据库迁移（不含 IF NOT EXISTS）

参考存储过程：`migrate_radar_dimensions_v1`（新增 code 列/唯一索引并回填）。

### 故障排查

- 仅显示“学习成绩”无四维：
  1) 检查 `ability_assessments.assessed_at` 是否为空；为空则回填。
  2) 检查四维写入的 `related_id` 是否为 assignmentId；如为 submissionId 可能导致课程聚合无法命中。
  3) 检查 `assessment_type` 是否在允许范围（assignment/project/exam/peer/self/system）。
  4) 确认当前分析时间范围覆盖写入的 `assessed_at`。


