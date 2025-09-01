# 新人上手手册（导航）

欢迎来到“学生非核心能力发展评估系统”。本手册面向完全新手：不需要预先了解项目或相关技术，按顺序阅读并动手实践，你将能在本地跑起来并逐步理解系统。

## 建议阅读路线
1. `quickstart-beginner.md`：零基础快速上手，先跑通项目（包含环境安装、数据库初始化、前后端启动、Swagger 验证）
2. `architecture-overview.md`：端到端架构与一次请求的生命周期（含鉴权/数据流时序图）
3. `file-walkthrough.md`：逐文件导览（关键文件逐条说明与调用链）
4. `e2e-examples.md`：端到端样例（登录/课程/AI/通知等时序图）
5. 深入：`backend-deep-dive.md`、`frontend-deep-dive.md`
6. 参考：API 参考、模型与配置、安全配置、Cookbook、FAQ、术语、团队约定
7. `learning-path.md`：1–2 周成长路线（配实践任务）

## 角色路线
- 学生端路线（Beta）：`quickstart` → `frontend-deep-dive` → `student.api.ts` → 完成 Cookbook 任务A/C
- 教师端路线（稳定）：`quickstart` → `backend-deep-dive` → `teacher.api.ts` → 完成 任务B/E/D

## 先决条件清单
- 已安装：Java 17、Maven、Node.js 18+、MySQL 8.x（Redis 可选）
- 数据库：已创建并导入 `schema.sql`
- 后端已启动：`http://localhost:8080/api`
- 前端已启动：`http://localhost:5173`（或自定义端口）
- 文档站点（可选）：`http://localhost:4174`

## 计划（Planning）建议
- 明确变更目标：新接口/页面/报表/图表
- 列出相关文件：Controller/Service/Mapper/Entity/DTO 与前端 API/Store/视图/类型
- 画出链路草图（Mermaid）确定数据流向与边界条件
- 拆分任务并估时：后端/前端/联调/文档/测试
- 风险评估：权限/性能/一致性/可扩展性

## 常用入口
- Swagger：`http://localhost:8080/api/swagger-ui.html`
- 前端开发：`http://localhost:5173`
- 文档站点（本地）：`http://localhost:4174`

## 相关技术官方链接
- Java：`https://dev.java/`
- Spring Boot：`https://spring.io/projects/spring-boot`
- MyBatis：`https://mybatis.org/`
- Spring Security：`https://spring.io/projects/spring-security`
- SpringDoc OpenAPI：`https://springdoc.org/`
- Vue 3：`https://vuejs.org/`
- TypeScript：`https://www.typescriptlang.org/`
- Vite：`https://vitejs.dev/`
- Pinia：`https://pinia.vuejs.org/`
- Tailwind CSS：`https://tailwindcss.com/`
- Axios：`https://axios-http.com/`
- ECharts：`https://echarts.apache.org/`

> 提示：本手册中的命令默认在 macOS / Linux 环境下执行；Windows 可用 PowerShell 等价命令。


