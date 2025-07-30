# 学生非核心能力发展评估系统 - 后端服务

## 1. 项目概览

本项目是**学生非核心能力发展评估系统**的后端服务。它基于 `Java 17` 和 `Spring Boot 3.2.1` 构建，旨在为前端应用提供一套完整、稳定、安全的RESTful API接口。

该系统通过多维度、数据驱动的方式，对学生的非核心能力（如沟通协作、创新思维、问题解决等）进行评估、跟踪和分析，并提供个性化的学习建议，从而帮助学生全面发展。

### 核心功能

*   **多维能力评估**：支持自定义能力维度，记录和分析学生在不同维度上的表现。
*   **课程与作业管理**：提供课程、章节、作业的创建、发布和管理功能。
*   **认证与授权**：基于 `Spring Security` 和 `JWT` 实现安全的用户认证和角色权限控制。
*   **社区互动**：提供帖子发布、评论、点赞等基本社区功能。
*   **个性化推荐**：根据学生的能力评估结果，生成针对性的学习建议。
*   **数据统计与报告**：为学生和教师提供多维度的数据看板和能力报告。

## 2. 技术栈

| 技术类别         | 技术名称                                   | 版本          | 描述                                       |
|------------------|--------------------------------------------|---------------|--------------------------------------------|
| **核心框架**     | Spring Boot                                | 3.2.1         | 快速构建生产级应用的基础框架。             |
| **开发语言**     | Java                                       | 17            | 项目的主要编程语言。                       |
| **构建工具**     | Maven                                      | 3.9+          | 项目管理和构建工具。                       |
| **Web开发**      | Spring MVC                                 | 6.1.2         | 实现RESTful API接口。                      |
| **安全框架**     | Spring Security                            | 6.2.1         | 提供认证、授权和安全防护。                 |
| **认证协议**     | JSON Web Token (JWT)                       | 0.12.3        | 用于生成和验证无状态的认证令牌。           |
| **数据库**       | MySQL                                      | 8.0+          | 主要的关系型数据库。                       |
| **数据持久化**   | MyBatis                                    | 3.0.3         | ORM框架，简化数据库操作。                  |
| **数据库连接池** | HikariCP                                   | -             | 高性能的JDBC连接池。                       |
| **分页插件**     | PageHelper                                 | 2.1.0         | MyBatis的物理分页插件。                    |
| **缓存**         | Redis                                      | 6.0+          | 用于缓存常用数据，提高系统性能。           |
| **API文档**      | SpringDoc OpenAPI (Swagger)                | 2.3.0         | 自动生成并展示API文档。                    |
| **参数验证**     | Spring Boot Validation (JSR-303)           | -             | 提供标准化的数据验证机制。                 |
| **JSON处理**     | Jackson                                    | -             | 高性能的JSON处理库。                       |
| **文件处理**     | Apache Commons IO / FileUpload             | 2.11.0 / 1.5  | 提供强大的文件操作和上传功能。             |
| **测试框架**     | JUnit 5, Mockito                           | -             | 用于编写单元测试和集成测试。               |

## 3. 项目结构

```
backend/
├── pom.xml                   # Maven项目配置文件，定义依赖和构建规则
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/noncore/assessment
│   │   │       ├── AssessmentApplication.java  # Spring Boot应用启动类
│   │   │       ├── config                # 应用配置类
│   │   │       ├── controller            # RESTful API控制器
│   │   │       ├── dto                   # 数据传输对象 (Request/Response)
│   │   │       ├── entity                # 数据库实体类
│   │   │       ├── exception             # 全局异常处理
│   │   │       ├── mapper                # MyBatis数据访问接口
│   │   │       ├── service               # 业务逻辑服务接口与实现
│   │   │       └── util                  # 工具类
│   │   └── resources
│   │       ├── mapper                # MyBatis XML映射文件
│   │       ├── static                # 静态资源 (此处为空)
│   │       ├── templates             # 模板文件 (此处为空)
│   │       ├── ability_schema.sql    # 能力评估模块的数据库表结构
│   │       ├── application.yml       # 全局配置文件
│   │       ├── application-dev.yml   # 开发环境配置文件
│   │       ├── application-prod.yml  # 生产环境配置文件
│   │       ├── data.sql              # 数据库初始数据
│   │       └── schema.sql            # 主数据库表结构
│   └── test
│       └── java
│           └── com/noncore/assessment
│               └── controller
│                   └── ApiIntegrationTest.java # API集成测试类
└── 数据库连接指南.md         # 数据库连接的详细说明文档
```

## 4. 文件详解

### 4.1 根目录文件

*   `pom.xml`: 定义了项目的基本信息、依赖库、插件和构建过程。是项目的核心配置文件。
*   `数据库连接指南.md`: 提供了连接和配置本地开发数据库的详细步骤。

### 4.2 `src/main/java` - Java源代码

#### `com.noncore.assessment.AssessmentApplication.java`

*   **用途**: Spring Boot应用的主启动类。
*   **核心注解**:
    *   `@SpringBootApplication`: 标志这是一个Spring Boot应用，并启用自动配置。
    *   `@EnableCaching`: 启用Spring的缓存支持。
    *   `@MapperScan`: 指定MyBatis的Mapper接口所在的包路径，让MyBatis能够扫描并创建代理实现。

#### `config` - 配置类

*   `JwtAccessDeniedHandler.java`: 处理已认证用户访问无权限资源时的403错误。
*   `JwtAuthenticationEntryPoint.java`: 处理匿名用户访问受保护资源时的401错误。
*   `JwtAuthenticationFilter.java`: JWT核心过滤器，在每个请求中验证JWT令牌的有效性，并设置Spring Security的认证上下文。
*   `RedisConfig.java`: 配置Redis连接和序列化方式，使应用能够使用Redis进行缓存。
*   `SecurityConfig.java`: Spring Security的核心配置，定义了安全策略、密码编码器、公开访问路径和JWT过滤器的集成。
*   `SwaggerConfig.java`: 配置SpringDoc OpenAPI（Swagger），用于生成和展示API文档。

#### `controller` - API控制器

*   **职责**: 接收HTTP请求，调用相应的`Service`处理业务逻辑，并返回JSON格式的响应。
*   `AbilityController.java`: 处理与能力评估相关的API请求。
*   `AssignmentController.java`: 处理作业管理相关的API请求。
*   `AuthController.java`: 处理用户认证（注册、登录、密码重置）相关的API请求。
*   `CommunityController.java`: 处理社区功能（帖子、评论）相关的API请求。
*   `CourseController.java`: 处理课程管理相关的API请求。
*   `FileController.java`: 处理文件上传和下载的API请求。
*   `GradeController.java`: 处理成绩管理相关的API请求。
*   `LessonController.java`: 处理课程章节相关的API请求。
*   `NotificationController.java`: 处理通知相关的API请求。
*   `StudentController.java`: 处理学生相关的API请求。
*   `TeacherController.java`: 处理教师相关的API请求。

#### `dto` - 数据传输对象

*   **职责**: 用于在不同层（如Controller和Service）之间传递数据，避免直接暴露数据库实体。
*   `request/`: 包含所有从客户端接收的请求数据模型。
    *   `ChangePasswordRequest.java`: 修改密码请求。
    *   `LoginRequest.java`: 用户登录请求。
    *   `RegisterRequest.java`: 用户注册请求。
    *   `ResetPasswordRequest.java`: 重置密码请求。
*   `response/`: 包含所有向客户端发送的响应数据模型。
    *   `AuthResponse.java`: 认证成功后的响应，通常包含JWT。
    *   `StudentDashboardResponse.java`: 学生仪表板数据响应。
    *   `TeacherDashboardResponse.java`: 教师仪表板数据响应。

#### `entity` - 数据库实体

*   **职责**: 定义了与数据库表结构一一对应的Java对象。
*   `Ability.java`: 能力实体。
*   `AbilityAssessment.java`: 能力评估记录实体。
*   `AbilityDimension.java`: 能力维度实体。
*   `AbilityGoal.java`: 能力目标实体。
*   `AbilityReport.java`: 能力报告实体。
*   `Assignment.java`: 作业实体。
*   `Course.java`: 课程实体。
*   `Enrollment.java`: 选课记录实体。
*   `FileRecord.java`: 文件上传记录实体。
*   `Grade.java`: 成绩实体。
*   `LearningRecommendation.java`: 学习建议实体。
*   `Lesson.java`: 课程章节实体。
*   `LessonProgress.java`: 章节学习进度实体。
*   `Notification.java`: 通知实体。
*   `Post.java`: 社区帖子实体。
*   `PostComment.java`: 帖子评论实体。
*   `PostLike.java`: 帖子点赞实体。
*   `StudentAbility.java`: 学生能力记录实体。
*   `Submission.java`: 作业提交记录实体。
*   `Tag.java`: 标签实体。
*   `User.java`: 用户实体。

#### `exception` - 异常处理

*   `BusinessException.java`: 自定义业务异常类，用于处理可预见的业务逻辑错误。
*   `ErrorCode.java`: 定义了标准化的错误码和错误信息枚举。
*   `GlobalExceptionHandler.java`: 全局异常处理器，捕获并统一处理应用中抛出的各类异常，返回标准格式的错误响应。

#### `mapper` - MyBatis数据访问接口

*   **职责**: 定义操作数据库的抽象方法，通过MyBatis框架与XML文件中的SQL语句绑定。
*   `AbilityAssessmentMapper.java`, `CourseMapper.java`, `UserMapper.java`, 等: 每个`Mapper`接口对应一个实体的数据访问操作。

#### `service` - 业务逻辑服务

*   **职责**: 实现核心业务逻辑，通常会调用一个或多个`Mapper`接口来完成数据库操作。
*   `impl/`: 存放服务接口的实现类。
*   `AbilityService.java`, `AuthService.java`, `CourseService.java`, 等: 每个`Service`接口定义了一组相关的业务操作。

#### `util` - 工具类

*   `ApiResponse.java`: 定义了标准的API响应格式，包含状态码、消息和数据。
*   `JwtUtil.java`: 提供了生成、解析和验证JWT令牌的静态方法。
*   `PageResult.java`: 定义了分页查询结果的标准化格式。

### 4.3 `src/main/resources` - 资源文件

#### `mapper/` - MyBatis XML映射文件

*   **职责**: 存放与`Mapper`接口对应的SQL语句。
*   `CourseMapper.xml`, `UserMapper.xml`, 等: 每个XML文件包含了一组针对特定实体的SQL查询（增删改查）。

#### 根目录资源文件

*   `ability_schema.sql`: 包含了能力评估模块相关数据表的创建语句（DDL）。
*   `application.yml`: 应用的主配置文件，定义了通用的配置项，并通过`spring.profiles.active`指定当前激活的环境。
*   `application-dev.yml`: **开发环境**的专属配置文件，会覆盖`application.yml`中的同名配置。
*   `application-prod.yml`: **生产环境**的专属配置文件。
*   `data.sql`: 包含插入数据库的初始数据（DML），如默认的用户角色、能力维度等。
*   `schema.sql`: 包含项目核心数据表（除能力评估外）的创建语句（DDL）。

### 4.4 `src/test/java` - 测试代码

*   `ApiIntegrationTest.java`: API集成测试类，使用`MockMvc`对主要的API端点进行自动化测试，验证其功能、性能和错误处理是否符合预期。

## 5. 核心功能模块

### 5.1 认证与授权 (`AuthController`, `AuthService`)
*   **注册**: 提供用户注册功能，支持学生和教师两种角色。
*   **登录**: 用户通过用户名和密码登录，成功后返回JWT。
*   **密码管理**: 支持修改密码和忘记密码后的重置流程。
*   **权限控制**: 基于Spring Security和JWT，通过`@PreAuthorize`注解对API接口进行精细化的权限控制。

### 5.2 课程与学习 (`CourseController`, `LessonController`)
*   **课程管理**: 教师可以创建、编辑和发布课程。
*   **课程学习**: 学生可以浏览、选择课程，并查看课程详情和章节列表。
*   **学习进度**: 系统会记录学生在每个章节的学习进度。

### 5.3 作业与成绩 (`AssignmentController`, `GradeController`)
*   **作业管理**: 教师可以为课程创建和发布作业。
*   **作业提交**: 学生可以查看作业要求并提交作业。
*   **成绩评定**: 教师可以为学生提交的作业打分，并发布成绩。

### 5.4 能力评估 (`AbilityController`, `AbilityService`)
*   **多维评估**: 系统根据学生在课程、作业等方面的表现，从多个能力维度进行综合评估。
*   **数据看板**: 为学生提供个人能力雷达图和发展趋势分析。
*   **报告生成**: 定期生成学生个人能力发展报告。
*   **学习建议**: 基于能力短板，为学生提供个性化的学习资源或课程建议。

### 5.5 社区互动 (`CommunityController`)
*   **帖子交流**: 支持用户发布帖子、查看帖子列表和详情。
*   **评论与点赞**: 用户可以对帖子进行评论和点赞。

## 6. API文档

本项目的API遵循RESTful设计规范，并使用SpringDoc OpenAPI 3自动生成交互式API文档。

启动项目后，您可以访问以下地址查看和测试所有API接口：

*   **Swagger UI**: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)

## 7. 运行与部署

### 7.1 环境要求

*   **Java**: JDK 17 或更高版本
*   **Maven**: 3.8 或更高版本
*   **MySQL**: 8.0 或更高版本
*   **Redis**: 6.0 或更高版本 (可选，用于缓存)

### 7.2 本地运行步骤

1.  **克隆项目**:
    ```bash
    git clone <repository_url>
    cd student-assessment-system/backend
    ```

2.  **配置数据库**:
    *   在本地MySQL中创建一个新的数据库，例如 `student_assessment_system`。
    *   修改 `src/main/resources/application-dev.yml` 文件，更新数据库的 `url`, `username`, 和 `password`。
    *   或者，通过设置环境变量 `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` 来进行配置。

3.  **初始化数据库**:
    *   启动应用时，Spring Boot会自动执行 `schema.sql`, `ability_schema.sql` 和 `data.sql` 来创建表和插入初始数据。
    *   **注意**: `schema.sql` 和 `ability_schema.sql` 是幂等的（使用 `CREATE TABLE IF NOT EXISTS`），因此可以安全地重复执行。

4.  **运行应用**:
    ```bash
    # 使用Maven Spring Boot插件运行
    mvn spring-boot:run
    ```

5.  **验证启动**:
    *   应用成功启动后，您可以在控制台看到 "学生非核心能力发展评估系统后端启动成功！" 的消息。
    *   访问 [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html) 确认API文档是否可用。

### 7.3 打包与部署

1.  **打包应用**:
    ```bash
    # 跳过测试进行打包
    mvn clean package -DskipTests
    ```
    该命令会在 `target/` 目录下生成一个可执行的JAR文件，例如 `student-assessment-system-1.0.0.jar`。

2.  **部署运行**:
    ```bash
    # 在服务器上运行JAR包
    # 确保已设置好生产环境的数据库和Redis环境变量
    java -jar target/student-assessment-system-1.0.0.jar --spring.profiles.active=prod
    ```

## 8. 测试指南

本项目包含一套API集成测试，用于验证核心功能的正确性。

### 8.1 运行测试

您可以通过以下命令运行所有测试：

```bash
mvn test
```

### 8.2 测试说明

*   **测试配置**: 测试运行时会激活 `test` profile，但由于项目中未提供 `application-test.yml`，它将回退使用 `application-dev.yml` 的配置。请确保开发环境的数据库可用。
*   **测试范围**: `ApiIntegrationTest.java` 覆盖了认证、课程、作业、成绩等主要API的创建、查询和基本错误处理流程。
*   **模拟请求**: 测试使用 `MockMvc` 来模拟HTTP请求，并验证响应的状态码和内容是否符合预期。


