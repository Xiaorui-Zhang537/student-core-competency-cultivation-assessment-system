# 数据模型总览（表 / Entity / DTO / TS Types）

- 表结构（Tables）：以 `backend/src/main/resources/schema.sql` 为唯一准源
- `ability_schema.sql`：历史参考脚本，可用于理解能力域演进，但不再作为当前结构准源
- 实体（Entity）：`src/main/java/com/noncore/assessment/entity/*`
- DTO：`src/main/java/com/noncore/assessment/dto/*`
- TS Types：`frontend/src/types/*`

审计备注（2026-03）：
- 旧 `reports`：违规举报/申诉工单链路已下线，表与后端 CRUD 已移除；能力报告历史统一保留在 `ability_reports`
- `analytics_cache`：已确认不在主流程中，相关表与孤立服务已移除
- 学生能力趋势已统一改为基于 `ability_assessments.assessed_at` 聚合；`student_abilities` 上旧的月趋势查询已清理

每个模型条目建议包含：字段名、中英文释义、类型、非空/默认、约束/枚举，以及“变更影响面”（会影响哪些 DTO、Mapper、前端类型）。

子页索引：
- 课程/作业/提交/评分：`/models/courses-assignments`
- 用户与认证：`/models/users-auth`
- 能力评估：`/models/ability`
- 社区与通知：`/models/community-notifications`
- AI 会话/消息/记忆：`/models/ai`
