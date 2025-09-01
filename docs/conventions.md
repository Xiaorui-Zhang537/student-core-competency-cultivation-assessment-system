# 团队约定（Conventions）

## 代码与提交
- 命名：使用有意义的英文单词；类/接口 PascalCase，方法/变量 camelCase
- 提交规范：Conventional Commits（示例：`feat(api): add batch publish for grades`）
- 分支：`feat/*`、`fix/*`、`docs/*`、`refactor/*`、`chore/*`
- 提交粒度：一次提交完成一类变更，信息完整可回溯

## PR 与评审清单
- 代码通过构建/类型检查与 Lint
- 新增/修改接口已更新 `docs/` 对应页面
- 文案支持中英文（i18n）
- 核心路径增加必要单测/集成测试用例说明
- 关联 Issue/任务编号，描述影响范围与回滚策略

## 后端约定
- Controller 仅做参数校验与转发；业务在 Service
- 统一返回体 `ApiResponse<T>`；分页 `PageResult<T>`
- 异常：抛业务异常，由全局处理器转换为标准响应
- 安全：严禁硬编码密钥与连接串，使用环境变量；JWT 鉴权与角色校验到位
- 事务：在 Service 层使用 `@Transactional`，保持边界清晰

## 前端约定
- “后端即真理”：仅基于已存在 API 开发
- API 封装统一在 `src/api`；状态在 `src/stores`
- 鉴权：401 统一跳登录；403 友好提示，不自动跳转
- i18n：新增文案全部进入语言包；组件中使用 `t()`
- 可视化：ECharts 配置复用，风格统一（雷达/饼/趋势）

## 安全与合规
- 输入校验：后端 JSR-303；前端基本校验 + 防抖
- 防护：参数化查询防 SQL 注入；输出转义防 XSS；开启 CORS 控制
- 日志：不记录敏感信息；生产使用最小必要日志级别

## 文档与流程
- 新增模块/接口后，更新 `docs/` 中相关章节与侧边栏
- README/Quickstart 保持可用；FAQ 与术语表同步扩展
- 发布前：完成端到端自测，确保主要流程与回滚路径明确
