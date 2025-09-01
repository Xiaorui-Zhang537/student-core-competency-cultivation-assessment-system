# 数据模型总览（表 / Entity / DTO / TS Types）

- 表结构（Tables）：来源于 `schema.sql`、`ability_schema.sql`
- 实体（Entity）：`src/main/java/com/noncore/assessment/entity/*`
- DTO：`src/main/java/com/noncore/assessment/dto/*`
- TS Types：`frontend/src/types/*`

每个模型条目建议包含：字段名、中英文释义、类型、非空/默认、约束/枚举，以及“变更影响面”（会影响哪些 DTO、Mapper、前端类型）。

子页索引：
- 课程/作业/提交/评分：`/models/courses-assignments`
- 用户与认证：`/models/users-auth`
- 能力评估：`/models/ability`
- 社区与通知：`/models/community-notifications`
- AI 会话/消息/记忆：`/models/ai`
