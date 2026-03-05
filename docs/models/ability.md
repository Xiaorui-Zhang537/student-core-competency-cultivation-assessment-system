# 模型对照：能力评估（Ability）

> 以 `backend/src/main/resources/schema.sql`、`Ability*` 实体/DTO 与前端 `types/ability.ts` 为准。

## 当前核心表
- `ability_dimensions`
  - 四个非成绩维度的字典表：`MORAL_COGNITION`、`LEARNING_ATTITUDE`、`LEARNING_ABILITY`、`LEARNING_METHOD`
  - 用途：提供稳定的维度编码与名称映射
- `ability_assessments`
  - 结构化评估明细表
  - 用途：保存学生在某个课程/作业上下文下的四维评分，是雷达与对比分析的主要数据源
- `student_abilities`
  - 学生当前能力快照
  - 用途：当时间区间内缺少明细时，作为雷达展示的第一层兜底
- `ability_reports`
  - AI/阶段性能力报告归档
  - 用途：保存完整 AI 规范化结果、维度均分、建议等；主要用于报告展示与历史追溯
- `course_ability_weights`
  - 课程维度权重配置
  - 用途：能力雷达综合分计算时读取（为空时默认等权）
  - 现状：后端 `GET/PUT /teachers/ability/weights` 仍可用，但前端暂未提供独立“权重编辑”入口，属于“后端保留能力”
- `ability_goals`
  - 学生维度目标
  - 用途：学生自定义维度提升目标；按 `student_abilities.current_score` 同口径（0-5）跟踪进度，达标后自动标记完成
  - 语义：到期未达成时不会强制改写 `status`，而是按派生字段 `overdue=true` 展示和聚合，便于继续追踪且不丢失原目标
  - 联动：系统每天早上会预同步“即将到期 / 已逾期”提醒；通知中心读取时还会再按需兜底同步（同一学生带 `60s` 短时节流）。行为摘要会附带目标达成率、逾期后恢复次数、目标关联分数变化，以及最近截止/最近动作/当前聚焦等派生信号
  - 跳转：目标提醒在通知详情页点击“查看详情”后，会直接跳到学生端 `/student/analysis` 并滚动到“能力目标”区块
- `learning_recommendations`
  - 个性化建议
  - 用途：承接能力分析后的行动建议；后端会在读取建议时按学生能力快照兜底生成
  - 现状：后端学生/教师建议端点仍在；当前前端主流程未直接渲染该表数据，属于“可复用但前端弱链路”

## 数据流约定
- AI 生成后，完整结果先归档到 `ability_reports`
- 成绩发布后，应尽量将四维结果写入 `ability_assessments`
- 教师端/学生端雷达优先读取 `ability_assessments`
- 若结构化明细缺失，则先回退到 `student_abilities`，最后才使用 `ability_reports` 中的 JSON 兜底
- `ability_reports` 保留完整历史，用于管理员/学生查看报告追溯；结构化回填不会删除历史报告
- `ability_goals` 的创建、调整、达成会写入 `BehaviorEvent`，并进入行为证据/行为洞察摘要；因此 AI summary 可以引用“当前聚焦的目标维度”和“已达成目标”
- 教师端班级统计当前通过 `enrollments(active)` 选出课程学生，再聚合这些学生在 `student_abilities` 中的最新快照；不再依赖不存在的“按课程分表的维度快照”

## 清理说明
- 旧的 `abilities` 表已移除
- `ability_assessments.ability_id` 冗余列已移除
- 历史数据如仅保留 AI 报告，可执行 `backfill_ability_assessments_from_reports.sql`：只按 `published` 成绩回填，并且每个学生每个作业只取最新一份 AI 报告，同时同步 `student_abilities`

## 前端类型（参考）
- `AbilityRadarData { invest, quality, mastery, stability, growth }`
- 趋势数组：`Array<{ x: string; y: number }>`

## 变更影响
- 教师端：雷达、对比分析、课程权重调整
- 学生端：能力看板、能力报告历史、学习建议
- 管理员端：能力导出、能力报告汇总
