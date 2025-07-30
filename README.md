# 学生非核心能力发展评估系统

## 📋 项目概述

本系统是一个基于AI的学生非核心能力发展评估与学习平台，旨在帮助学生发展批判性思维、创新能力、协作能力等21世纪核心素养。通过多维度评估、个性化学习路径推荐和智能分析，为教育工作者和学生提供全面的能力发展支持。

## 🏗️ 系统架构

### 技术栈
- **后端**: Spring Boot 3.2.1 + Java 21 + MyBatis + MySQL + Redis + Spring Security
- **前端**: Vue 3.5.18 + TypeScript + Vite 5 + Tailwind CSS + Pinia + Element Plus
- **数据库**: MySQL 9.0.1 (22个核心表)
- **缓存**: Redis (JWT黑名单、邮件验证、会话管理)
- **构建工具**: Maven 3.x (后端) + Vite 5.x (前端)

### 核心功能模块
1. **用户管理**: 学生、教师、管理员角色管理，JWT认证授权
2. **课程管理**: 课程创建、发布、选课、内容管理
3. **作业系统**: 作业发布、多模态提交、智能评分
4. **能力评估**: 多维度能力评估、AI分析、学习建议
5. **学习路径**: 个性化学习路径推荐和进度跟踪
6. **社交学习**: 论坛、讨论、协作学习功能
7. **数据分析**: 学习数据可视化、能力发展趋势分析
8. **通知系统**: 实时消息推送、邮件通知

## 🚀 快速开始

### 环境要求
- **Java**: JDK 21+
- **Node.js**: 20.18.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选，用于缓存和JWT管理)
- **Maven**: 3.6+

### 数据库初始化
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入表结构和初始数据
mysql -u root -p student_assessment_system < backend/src/main/resources/schema.sql
mysql -u root -p student_assessment_system < backend/src/main/resources/data.sql
```

### 后端启动
```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # macOS
mvn clean compile
mvn spring-boot:run
```
后端服务将在 `http://localhost:8080` 启动

### 前端启动
```bash
cd frontend
npm install
npm run dev
```
前端服务将在 `http://localhost:5173` 启动

### 验证安装
访问以下地址确认系统正常运行：
- **前端应用**: http://localhost:5173
- **后端API文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/actuator/health

## 📊 系统状态

### ✅ 完全正常运行
- **后端服务**: ✅ Spring Boot 完全启动成功 (端口: 8080)
- **前端服务**: ✅ Vue 3 开发服务器正常 (端口: 5173)
- **数据库**: ✅ MySQL 连接正常，22个表结构完整
- **缓存服务**: ✅ Redis 配置正常，JWT黑名单功能正常
- **API功能**: ✅ 用户注册、登录、认证功能验证通过
- **编译构建**: ✅ 前后端代码编译零错误

### 📈 质量指标
- **后端**: 96个Java源文件，遵循Spring Boot最佳实践
- **前端**: TypeScript编译0错误，ESLint警告91个(不影响功能)
- **测试覆盖**: 核心API功能测试通过
- **性能**: 后端启动时间 < 6秒，前端编译时间 < 3秒

### 🔧 已解决的技术问题
- ✅ MyBatis XML映射文件JDBC类型配置问题
- ✅ Redis配置和依赖注入问题  
- ✅ UserMapper参数名不匹配问题
- ✅ 前端TypeScript类型错误和组件问题
- ✅ Spring Security JWT认证配置
- ✅ 数据库连接池和事务管理

## 📚 API文档

### 认证模块 (已验证)
- `POST /api/auth/login` - 用户登录 ✅
- `POST /api/auth/register` - 用户注册 ✅  
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/check-username` - 检查用户名是否存在 ✅

### 课程模块
- `GET /api/courses` - 获取课程列表
- `POST /api/courses` - 创建课程
- `GET /api/courses/{id}` - 获取课程详情
- `PUT /api/courses/{id}` - 更新课程
- `DELETE /api/courses/{id}` - 删除课程

### 作业模块
- `GET /api/assignments` - 获取作业列表
- `POST /api/assignments` - 创建作业
- `GET /api/assignments/{id}` - 获取作业详情
- `POST /api/submissions` - 提交作业

### 能力评估模块
- `GET /api/abilities/dashboard` - 能力仪表板
- `POST /api/abilities/assessments` - 记录评估
- `GET /api/abilities/reports` - 获取能力报告

### 用户管理模块
- `GET /api/students` - 学生列表
- `GET /api/teachers` - 教师列表
- `PUT /api/users/{id}` - 更新用户信息

详细API文档请访问: http://localhost:8080/api/swagger-ui.html

## 🔐 默认账户

系统提供以下测试账户（密码均为: `123456`）：

| 角色 | 用户名 | 邮箱 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin@example.com | 系统管理员 |
| 教师 | teacher | teacher@example.com | 张老师 |
| 教师 | teacher2 | wang@example.com | 王教授 |
| 学生 | student | student@example.com | 李同学 |
| 学生 | student2 | liu@example.com | 刘小明 |
| 学生 | student3 | chen@example.com | 陈小红 |

## 🔧 开发指南

### 代码规范
- **Java**: 遵循Google Java Style Guide
- **TypeScript**: 使用严格模式，ESLint + Prettier
- **数据库**: 统一命名规范，软删除机制
- **API**: RESTful设计，统一响应格式

### 错误处理
- **后端**: BusinessException + ErrorCode枚举
- **前端**: 全局错误拦截器
- **数据库**: 事务回滚机制
- **日志**: 分级日志记录

### 安全机制
- **认证**: JWT令牌 + Redis黑名单
- **授权**: RBAC角色权限控制
- **输入验证**: JSR-303 Bean Validation
- **SQL注入防护**: MyBatis参数化查询

## 📁 项目结构

```
Student Non-Core Competence Development Assessment System/
├── backend/                 # Spring Boot后端
│   ├── src/main/java/      # Java源代码
│   ├── src/main/resources/ # 配置文件和静态资源
│   └── pom.xml            # Maven配置
├── frontend/               # Vue 3前端
│   ├── src/               # 前端源代码
│   ├── public/            # 静态资源
│   └── package.json       # npm配置
├── docs/                  # 项目文档
└── README.md             # 项目说明
```

详细信息请查看：
- [后端开发指南](./backend/README.md)
- [前端开发指南](./frontend/README.md)

## 🤝 贡献指南

1. Fork 项目到个人仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 提交规范
- `feat`: 新功能
- `fix`: 错误修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关

## 📈 未来计划

### 近期计划 (v1.1)
- [ ] 移动端应用开发
- [ ] 离线数据同步
- [ ] 批量数据导入导出
- [ ] 多语言国际化支持

### 中期计划 (v2.0)
- [ ] AI智能推荐算法优化
- [ ] 实时协作功能
- [ ] 视频直播授课
- [ ] 区块链证书认证

### 长期计划 (v3.0)
- [ ] 云原生架构迁移
- [ ] 微服务拆分
- [ ] 大数据分析平台
- [ ] 元宇宙学习环境

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 支持与联系

- **项目仓库**: [GitHub Repository](https://github.com/your-org/student-assessment-system)
- **问题反馈**: [Issues](https://github.com/your-org/student-assessment-system/issues)
- **技术讨论**: [Discussions](https://github.com/your-org/student-assessment-system/discussions)
- **邮箱支持**: support@assessment-system.com

---

**开发团队**: Student Assessment Development Team  
**最后更新**: 2025-01-29  
**当前版本**: v1.0.0  
**系统状态**: 🟢 完全正常运行 