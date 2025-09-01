# 学习路线（Learning Path, 1–2 周）

## 第1阶段：跑通与验证（1–2天）
- 目标：本地可跑，能访问 Swagger 与主页面
- 任务：完成 Quickstart、整理本地 `.env` 与数据库初始化脚本
- 验收：
  - [ ] Swagger 正常
  - [ ] 前端正常、登录跳转明确
  - [ ] API 命中 `/api`，无 CORS 报错

## 第2阶段：理解链路（2–3天）
- 目标：能从“页面交互 → Store → API → 后端 → DB → 渲染”完整定位问题
- 任务：阅读 `architecture-overview.md`、`file-walkthrough.md`
- 验收：
  - [ ] 能画出登录/课程列表的时序图
  - [ ] 能说清 Controller/Service/Mapper 边界

## 第3阶段：动手实践（3–4天）
- 目标：独立完成功能改造并提交 PR
- 任务：完成 Cookbook 至少 2 个任务（建议：通知中心会话模式 + AI 会话管理）
- 验收：
  - [ ] 通过评审（Conventional Commits、变更范围清晰）
  - [ ] 文档同步更新（API/FAQ/术语/Conventions）

## 第4阶段：深入与优化（2–3天）
- 目标：具备调优与守护能力
- 任务：阅读 `backend-deep-dive.md` 与 `frontend-deep-dive.md`，并在实际页面上优化日志、错误处理、类型约束
- 验收：
  - [ ] 补充 1 个端到端图示
  - [ ] 完成 1 个性能/体验优化点（分页/节流/缓存/图表复用）

## 实践映射（任务 → 文档）
- 登录/课程列表 → `e2e-examples.md`、`auth.api.md`、`course.api.md`
- 通知中心会话模式 → `backend/api/notification.md`、`useNotificationStream.ts`
- AI 会话管理 → `backend/api/ai.md`、`ai.api.md`
- 文件上传/预览 → `backend/api/file.md`、`file.api.md`

完成后，你应能：
- 独立新增后端接口与前端页面
- 熟练定位链路问题（401/404/500）
- 维护与扩展文档，使后来者可快速上手
