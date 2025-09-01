# 模型对照：能力评估（Ability）

> 以 `ability_schema.sql`、`Ability*` DTO 与前端 `types/ability.ts` 为准。

## 能力维度与记录
- 维度表：`ability_dimension`（维度名称、权重、描述）
- 记录表：`ability_record`（学生、课程/作业、维度得分、时间）

## 前端类型（参考）
- `AbilityRadarData { invest, quality, mastery, stability, growth }`
- 趋势数组：`Array<{ x: string; y: number }>`

## 变更影响
- 教师端：雷达与权重更新接口
- 学生端：分析聚合展示
- 报表：维度导出与对比
