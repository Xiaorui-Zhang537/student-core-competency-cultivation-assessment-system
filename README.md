# **学生非核心能力发展评估系统**

> **最后更新：2025‑08‑01 | 当前版本：v1.0.1**

------



## **📚 项目概述**

本系统是一个基于 **AI** 的学生非核心能力发展评估与学习平台，旨在帮助学生发展批判性思维、创新能力、协作能力等 21 世纪核心素养。平台通过多维度评估、个性化学习路径推荐和智能分析，为教育工作者和学生提供 **全栈式能力发展支持**。

------



## **🏗️ 系统架构**

### **技术栈**

| **层次** | **技术**                                                     | **说明**                  |
| -------- | ------------------------------------------------------------ | ------------------------- |
| 后端     | Spring Boot 3.2.1 · Java 21 · MyBatis · Spring Security + JWT · Redis | RESTful API、鉴权、缓存   |
| 前端     | Vue 3.5.18 · TypeScript · Vite 5 · Tailwind CSS · Pinia · Element Plus | SPA 界面、状态管理        |
| 数据库   | MySQL 9.0.1                                                  | 22 张核心表，主从分离预留 |
| DevOps   | Maven 3.x · npm · GitHub Actions                             | CI/CD、代码质量扫描       |

------



## **🧩 核心功能模块**

1. **用户管理** – 学生 / 教师 / 管理员的注册、登录、RBAC 授权
2. **课程管理** – 课程发布、选课、章节与资源管理
3. **作业系统** – 多模态作业提交（文档 / 音视频 / PPT 等）、AI 评分
4. **能力评估** – 雷达图、趋势分析、学习建议
5. **学习路径** – 基于画像的个性化路径推荐
6. **社交学习** – 论坛、讨论区、@邀请、点赞
7. **数据分析** – 实时可视化面板、关键指标跟踪
8. **通知系统** – 邮件与 WebSocket 实时推送

------



## **🚀 快速开始（本地开发）**

### **环境要求**

- **Java 21+**
- **Node.js 20.18.0+**
- **MySQL 8.0+**
- **Redis 6.0+**（可选，用于黑名单、会话）
- **Maven 3.6+**

### **数据库初始化**

```
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
# 导入结构 & 初始数据
mysql -u root -p student_assessment_system < backend/src/main/resources/schema.sql
mysql -u root -p student_assessment_system < backend/src/main/resources/data.sql
```

### **启动后端**

```
cd backend
mvn spring-boot:run
```

后端默认监听 http://localhost:8080。

### **启动前端**

```
cd frontend
npm install
npm run dev
```

前端默认监听 http://localhost:5173。

> 访问 http://localhost:8080/api/swagger-ui.html 可查看在线 API 文档。

------



## **📦 运行与部署（生产环境）**

**打包后端** 

```
cd backend
mvn clean package -DskipTests
# 生成 target/student-assessment-system.jar
java -jar target/student-assessment-system.jar --spring.profiles.active=prod
```

**构建前端静态文件** 

```
cd frontend
npm run build
# 输出到 dist/
```

**Nginx 示例配置**（可选）

```
server {
  listen 80;
  server_name assessment.example.com;

  location / {
    root   /var/www/assessment/dist;
    try_files $uri $uri/ /index.html;
  }

  location /api/ {
    proxy_pass http://127.0.0.1:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

------



## **📈 系统状态**

- **后端服务**：✅ Spring Boot 启动成功（8080）
- **前端服务**：✅ Vite 开发服务器正常（5173）
- **数据库**：✅ 22 张表结构完整
- **缓存服务**：✅ Redis 连接正常

------



## **🗂️ 文件详解**

| **路径 / 文件**                      | **类型** | **说明**                                        |
| ------------------------------------ | -------- | ----------------------------------------------- |
| **backend/**                         | 目录     | Spring Boot 服务端代码与配置                    |
| ├── **pom.xml**                      | 配置     | 后端依赖与构建脚本                              |
| ├── **src/main/java/**               | 源码     | Controller / Service / Mapper / Entity 分层结构 |
| ├── **src/main/resources/**          | 资源     | application.yml、schema.sql、mapper/*.xml 等    |
| **frontend/**                        | 目录     | Vue 3 单页应用代码                              |
| ├── **package.json**                 | 配置     | Node 依赖与脚本命令                             |
| ├── **src/**                         | 源码     | 组件、页面、Pinia store、路由等                 |
| ├── **public/**                      | 资源     | 静态资源（favicon、图标等）                     |
| **API_CRITICAL_ISSUES_SUMMARY.md**   | 文档     | 关键 API 问题追踪与总结                         |
| **API_FIX_COMPLETION_REPORT.md**     | 文档     | 已修复缺陷清单                                  |
| **API_INTERFACE_TEST_REPORT.md**     | 文档     | 接口测试覆盖与结果                              |
| **ENV_CONFIG_EXAMPLE.md**            | 文档     | 环境变量示例（本地 / CI）                       |
| **ESLINT_CLEANUP_GUIDE.md**          | 文档     | 前端 ESLint 警告修复指北                        |
| **SECURITY_FIXES_SUMMARY.md**        | 文档     | 安全漏洞修复记录                                |
| **UNIMPLEMENTED_FEATURES_REPORT.md** | 文档     | 待实现功能及优先级                              |
| **pom.xml** / **package.json**       | 配置     | 根级别聚合 Maven & npm 脚本                     |

> 如需深入了解后端 / 前端目录下的包结构，请阅读各自模块内的 README.md 或查看源码注释。

------



## **🛡️ 测试与质量保障**

| **范围**      | **工具**                                 | **命令**                              |
| ------------- | ---------------------------------------- | ------------------------------------- |
| 后端单元测试  | JUnit 5 · Spring Boot Test               | mvn test                              |
| 前端单元测试  | Vitest                                   | npm run test                          |
| 静态代码扫描  | SonarLint (IDE) · SonarQube (CI)         | GitHub Actions 自动触发               |
| 格式化 & Lint | Spotless (Java) · ESLint + Prettier (TS) | mvn spotless:apply / npm run lint:fix |
| 安全检查      | OWASP Dependency‑Check                   | CI 自动执行                           |

持续集成流程存放在 .github/workflows/，包括 **测试 → 构建 → 静态扫描 → 部署** 四阶段步骤。

------



## **🤝 贡献指南**

1. Fork → 创建分支 (feat/xxx) → 提交 PR
2. Commit 遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范
3. 在 PR 描述中关联 Issue，并附带测试或截图

------



## **🔮 未来计划**

- **v1.1**：移动端 H5、离线数据同步、多语言 i18n
- **v2.0**：AI 推荐算法优化、实时协作编辑、直播授课
- **v3.0**：云原生微服务拆分、大数据分析平台、元宇宙学习空间

------



## **📝 许可证**

项目遵循 **MIT License**，详见 LICENSE 文件。

------



## **📬 支持与联系**

- Issues: https://github.com/Xiaorui-Zhang537/student-assessment-system/issues
- Discussions: https://github.com/Xiaorui-Zhang537/student-assessment-system/discussions
- 邮箱: **xiaorui537537@Gmail.com**

------



> *Made with ❤️ by Student Assessment Development Team*