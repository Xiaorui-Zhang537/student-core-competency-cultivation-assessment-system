USE student_assessment_system;

-- 开发/演示/联调种子数据。
-- 设计原则：
-- 1) 固定主键，便于前后端联调与截图复现；
-- 2) 可重复执行，先清理再插入；
-- 3) 只覆盖当前仍在使用的核心业务链路；
-- 4) AI 报告与结构化能力评估同时提供，避免页面长期依赖 JSON 兜底。

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM admin_audit_logs;
DELETE FROM ai_voice_turns;
DELETE FROM ai_voice_sessions;
DELETE FROM ai_messages;
DELETE FROM ai_conversations;
DELETE FROM ai_memories;
DELETE FROM ai_grading_history;
DELETE FROM behavior_insights;
DELETE FROM behavior_summary_snapshots;
DELETE FROM behavior_events;
DELETE FROM chat_message_attachments;
DELETE FROM chat_conversation_members;
DELETE FROM chat_conversations;
DELETE FROM comment_likes;
DELETE FROM post_likes;
DELETE FROM post_comments;
DELETE FROM post_tags;
DELETE FROM posts;
DELETE FROM tags;
DELETE FROM notifications;
DELETE FROM learning_recommendations;
DELETE FROM ability_goals;
DELETE FROM ability_reports;
DELETE FROM ability_assessments;
DELETE FROM student_abilities;
DELETE FROM grades;
DELETE FROM submissions;
DELETE FROM lesson_materials;
DELETE FROM lesson_progress;
DELETE FROM lessons;
DELETE FROM chapters;
DELETE FROM assignments;
DELETE FROM enrollments;
DELETE FROM course_ability_weights;
DELETE FROM file_records;
DELETE FROM help_ticket_message;
DELETE FROM help_ticket;
DELETE FROM help_article_feedback;
DELETE FROM help_article;
DELETE FROM help_category;
DELETE FROM courses;
DELETE FROM ability_dimensions;
DELETE FROM users;
SET FOREIGN_KEY_CHECKS = 1;

-- 默认密码（开发/演示数据）：12345678
INSERT INTO users (id, username, email, password, role, status, avatar, nickname, first_name, last_name, bio, grade, subject, school, phone, created_at, updated_at, deleted, email_verified)
VALUES
    (1, 'admin', 'admin+seed-20260303@local.test', '$2a$10$fKoNHkjprIIZ4HNJFTkrBOqq1DXbw/j2.HX53waOvNjmP1X4NVcoK', 'admin', 'active', '', '系统管理员', '系统', '管理员', '负责系统治理与数据审计', NULL, NULL, '信息中心', '13800000000', NOW(), NOW(), 0, 1),
    (2, 'teacher_java', 'teacher.java@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'teacher', 'active', '', '罗老师', '罗', '老师', '负责工程实践与数据库课程', NULL, '计算机', '软件学院', '13800000001', NOW(), NOW(), 0, 1),
    (3, 'teacher_soft', 'teacher.soft@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'teacher', 'active', '', '周老师', '周', '老师', '负责表达训练与协作课程', NULL, '通识教育', '人文学院', '13800000002', NOW(), NOW(), 0, 1),
    (11, 'student_lin', 'student.lin@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'student', 'active', '', '林一', '林', '一', '偏向工程实现，提交稳定', '2024级', '软件工程', '软件学院', '13800000011', NOW(), NOW(), 0, 1),
    (12, 'student_wang', 'student.wang@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'student', 'active', '', '王可', '王', '可', '作业提交积极，待提高方法性', '2024级', '软件工程', '软件学院', '13800000012', NOW(), NOW(), 0, 1),
    (13, 'student_chen', 'student.chen@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'student', 'active', '', '陈曦', '陈', '曦', '表达与分析均衡发展', '2024级', '数据科学', '软件学院', '13800000013', NOW(), NOW(), 0, 1),
    (14, 'student_zhao', 'student.zhao@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'student', 'active', '', '赵宁', '赵', '宁', '有 AI 报告历史，适合验证回填脚本', '2024级', '数字媒体', '人文学院', '13800000014', NOW(), NOW(), 0, 1);

-- 能力维度字典：仅维护四个非成绩维度。学习成绩仍由 grades 独立计算。
INSERT INTO ability_dimensions (id, name, code, description, category, weight, max_score, icon, color, sort_order, is_active, created_at, updated_at)
VALUES
    (1, '道德认知', 'MORAL_COGNITION', '价值判断、责任意识与规则理解', 'soft', 1.00, 100, 'scale', '#EF4444', 1, 1, NOW(), NOW()),
    (2, '学习态度', 'LEARNING_ATTITUDE', '投入度、自律性与持续性', 'soft', 1.00, 100, 'heart', '#F59E0B', 2, 1, NOW(), NOW()),
    (3, '学习能力', 'LEARNING_ABILITY', '理解、分析、迁移与解决问题', 'academic', 1.00, 100, 'sparkles', '#3B82F6', 3, 1, NOW(), NOW()),
    (4, '学习方法', 'LEARNING_METHOD', '计划、反思与资源使用策略', 'academic', 1.00, 100, 'book-open', '#10B981', 4, 1, NOW(), NOW());

INSERT INTO courses (id, title, description, cover_image, teacher_id, category, difficulty, duration, max_students, price, status, start_date, end_date, tags, objectives, requirements, created_at, updated_at, deleted)
VALUES
    (101, 'Java Web 工程实践', '以课程项目为主线，完成接口、鉴权与部署实践', '', 2, '编程', 'intermediate', 5400, 60, 0, 'published', '2026-03-02', '2026-06-30', 'java,spring,backend', '完成一个可运行的后端服务', '具备 Java 基础语法能力', NOW(), NOW(), 0),
    (102, '数据建模与 SQL 实战', '围绕 MySQL 建模、索引和统计查询开展训练', '', 2, '数据库', 'intermediate', 4800, 50, 0, 'published', '2026-03-03', '2026-06-20', 'mysql,sql,data', '掌握关系建模与核心查询模式', '具备基本编程能力', NOW(), NOW(), 0),
    (103, '学术表达与协作', '通过口头表达、书面表达和协作反馈提升综合素养', '', 3, '软技能', 'beginner', 3600, 45, 0, 'published', '2026-03-05', '2026-05-30', 'communication,teamwork', '提升表达与协作质量', '无', NOW(), NOW(), 0);

INSERT INTO assignments (id, course_id, teacher_id, title, description, requirements, max_score, due_date, allow_late, max_attempts, file_types, max_file_size, status, created_at, updated_at, deleted)
VALUES
    (1001, 101, 2, '接口鉴权练习', '完成登录与鉴权拦截器实现', '提交代码与设计说明', 100, '2026-03-20 23:59:59', 1, 3, 'zip,md,pdf', 20, 'published', NOW(), NOW(), 0),
    (1002, 101, 2, '课程项目一期', '补全用户与课程两个核心模块', '提交仓库链接与运行截图', 100, '2026-04-05 23:59:59', 1, 3, 'zip,md', 50, 'published', NOW(), NOW(), 0),
    (1003, 102, 2, '索引设计实验', '为给定业务查询补充索引设计', '提交 SQL 脚本与分析说明', 100, '2026-03-25 23:59:59', 1, 2, 'sql,md,pdf', 10, 'published', NOW(), NOW(), 0),
    (1004, 102, 2, '统计查询实战', '实现课程成绩与能力雷达统计查询', '提交 SQL 与结果截图', 100, '2026-04-10 23:59:59', 1, 2, 'sql,zip', 20, 'published', NOW(), NOW(), 0),
    (1005, 103, 3, '案例复盘汇报', '围绕协作冲突案例完成复盘展示', '提交演示稿与复盘文档', 100, '2026-03-28 23:59:59', 0, 1, 'ppt,pdf,docx', 20, 'published', NOW(), NOW(), 0);

INSERT INTO enrollments (id, student_id, course_id, enrolled_at, progress, status, grade, completion_date, created_at, updated_at, last_access_time)
VALUES
    (2001, 11, 101, NOW(), 78.0, 'active', 88.0, NULL, NOW(), NOW(), NOW()),
    (2002, 11, 102, NOW(), 65.0, 'active', 91.0, NULL, NOW(), NOW(), NOW()),
    (2003, 12, 101, NOW(), 52.0, 'active', NULL, NULL, NOW(), NOW(), NOW()),
    (2004, 12, 103, NOW(), 34.0, 'active', NULL, NULL, NOW(), NOW(), NOW()),
    (2005, 13, 102, NOW(), 84.0, 'active', 89.0, NULL, NOW(), NOW(), NOW()),
    (2006, 13, 103, NOW(), 76.0, 'active', 93.0, NULL, NOW(), NOW(), NOW()),
    (2007, 14, 102, NOW(), 58.0, 'active', NULL, NULL, NOW(), NOW(), NOW()),
    (2008, 14, 103, NOW(), 71.0, 'active', 86.0, NULL, NOW(), NOW(), NOW());

INSERT INTO lessons (id, course_id, title, description, content, video_url, duration, order_index, is_free, created_at, updated_at, deleted)
VALUES
    (3001, 101, '认证方案选型', '比较 Session、JWT 与网关拦截方案', '聚焦后端鉴权设计', NULL, 45, 1, 1, NOW(), NOW(), 0),
    (3002, 101, '接口分层实践', 'Controller、Service、Mapper 职责划分', '示例代码与重构练习', NULL, 60, 2, 0, NOW(), NOW(), 0),
    (3003, 102, '索引与查询计划', '结合 EXPLAIN 分析执行计划', '多种典型查询案例', NULL, 55, 1, 1, NOW(), NOW(), 0),
    (3004, 102, '聚合统计与窗口思路', '面向统计报表的 SQL 组织方式', '课程级聚合练习', NULL, 50, 2, 0, NOW(), NOW(), 0),
    (3005, 103, '复盘表达结构', '构建问题、证据、结论三段式表达', '适合课堂演练', NULL, 40, 1, 1, NOW(), NOW(), 0);

INSERT INTO lesson_progress (id, student_id, lesson_id, progress, completed, watch_time, completed_at, created_at, updated_at, last_studied_at, course_id, view_count, last_position, status, notes, rating, started_at)
VALUES
    (4001, 11, 3001, 100.0, 1, 2700, NOW(), NOW(), NOW(), NOW(), 101, 2, 0, 'finished', '已完成鉴权方案对比笔记', 5, NOW()),
    (4002, 11, 3003, 82.0, 0, 2500, NULL, NOW(), NOW(), NOW(), 102, 1, 830, 'studying', '正在补充索引实验', 4, NOW()),
    (4003, 12, 3001, 64.0, 0, 1900, NULL, NOW(), NOW(), NOW(), 101, 1, 640, 'studying', '需要再次回看 JWT 部分', 4, NOW()),
    (4004, 13, 3004, 100.0, 1, 3000, NOW(), NOW(), NOW(), NOW(), 102, 2, 0, 'finished', '已完成统计查询实战', 5, NOW()),
    (4005, 13, 3005, 88.0, 0, 2100, NULL, NOW(), NOW(), NOW(), 103, 1, 420, 'studying', '准备复盘演讲提纲', 5, NOW()),
    (4006, 14, 3005, 100.0, 1, 2400, NOW(), NOW(), NOW(), NOW(), 103, 2, 0, 'finished', '已完成复盘案例演练', 4, NOW());

INSERT INTO submissions (id, assignment_id, student_id, content, file_path, file_name, file_size, submission_count, status, submitted_at, is_late, created_at, updated_at)
VALUES
    (5001, 1001, 11, '完成登录接口、JWT 过滤器与鉴权测试', '/upload/seed/auth-service-lin.zip', 'auth-service-lin.zip', 48231, 1, 'graded', '2026-03-18 20:30:00', 0, NOW(), NOW()),
    (5002, 1001, 12, '完成接口鉴权主体，但异常处理仍需完善', '/upload/seed/auth-service-wang.zip', 'auth-service-wang.zip', 45112, 1, 'submitted', '2026-03-19 21:10:00', 0, NOW(), NOW()),
    (5003, 1003, 11, '补充索引脚本与执行计划截图', '/upload/seed/index-design-lin.sql', 'index-design-lin.sql', 8120, 1, 'graded', '2026-03-22 19:45:00', 0, NOW(), NOW()),
    (5004, 1004, 13, '完成聚合查询、分页与雷达统计 SQL', '/upload/seed/radar-report-chen.sql', 'radar-report-chen.sql', 12650, 1, 'graded', '2026-04-08 18:40:00', 0, NOW(), NOW()),
    (5005, 1005, 14, '提交复盘汇报与案例总结', '/upload/seed/retro-zhao.pdf', 'retro-zhao.pdf', 23112, 1, 'graded', '2026-03-27 16:20:00', 0, NOW(), NOW()),
    (5006, 1005, 12, '已上传初稿，等待教师反馈', '/upload/seed/retro-wang.pdf', 'retro-wang.pdf', 19876, 1, 'submitted', '2026-03-27 22:05:00', 0, NOW(), NOW());

INSERT INTO grades (id, student_id, assignment_id, score, max_score, percentage, grade_level, feedback, status, deleted, created_at, updated_at, submission_id, grader_id)
VALUES
    (6001, 11, 1001, 92.0, 100.0, 92.0, 'A', '鉴权链路完整，异常处理和日志设计清晰。', 'published', 0, NOW(), NOW(), 5001, 2),
    (6002, 11, 1003, 95.0, 100.0, 95.0, 'A', '索引设计合理，执行计划分析充分。', 'published', 0, NOW(), NOW(), 5003, 2),
    (6003, 13, 1004, 89.0, 100.0, 89.0, 'B', '统计查询覆盖完整，边界条件仍可加强。', 'published', 0, NOW(), NOW(), 5004, 2),
    (6004, 14, 1005, 86.0, 100.0, 86.0, 'B', '复盘结构完整，表达节奏较稳定。', 'published', 0, NOW(), NOW(), 5005, 3);

INSERT INTO ability_assessments (id, student_id, dimension_id, assessor_id, assessment_type, related_id, score, max_score, ability_level, confidence, evidence, improvement, status, assessed_at, created_at, updated_at, deleted)
VALUES
    (7001, 11, 1, 2, 'assignment', 1001, 4.1, 5.0, 'advanced', 0.94, 'AI 报告与教师点评均指向规则意识稳定。', '继续补充高风险接口的权限边界说明。', 'completed', '2026-03-18 21:00:00', NOW(), NOW(), 0),
    (7002, 11, 2, 2, 'assignment', 1001, 4.4, 5.0, 'advanced', 0.96, '按期提交且日志与注释完整。', '维持持续复盘习惯。', 'completed', '2026-03-18 21:00:00', NOW(), NOW(), 0),
    (7003, 11, 3, 2, 'assignment', 1001, 4.6, 5.0, 'advanced', 0.97, '能独立实现鉴权链路并完成测试。', '继续提高边界条件覆盖。', 'completed', '2026-03-18 21:00:00', NOW(), NOW(), 0),
    (7004, 11, 4, 2, 'assignment', 1001, 4.0, 5.0, 'advanced', 0.93, '提交内容结构清晰，文件组织合理。', '进一步规范说明文档模板。', 'completed', '2026-03-18 21:00:00', NOW(), NOW(), 0),
    (7005, 13, 1, 2, 'assignment', 1004, 4.0, 5.0, 'intermediate', 0.92, '分析过程较完整，能解释统计口径。', '在结论中增加更明确的约束边界。', 'completed', '2026-04-08 19:00:00', NOW(), NOW(), 0),
    (7006, 13, 2, 2, 'assignment', 1004, 4.2, 5.0, 'intermediate', 0.94, '按节点提交，能主动补齐缺失字段。', '继续保持对细节的复查。', 'completed', '2026-04-08 19:00:00', NOW(), NOW(), 0),
    (7007, 13, 3, 2, 'assignment', 1004, 4.5, 5.0, 'advanced', 0.95, '统计 SQL 能覆盖课程、班级、学生多口径。', '加强异常样本与空值分支处理。', 'completed', '2026-04-08 19:00:00', NOW(), NOW(), 0),
    (7008, 13, 4, 2, 'assignment', 1004, 4.1, 5.0, 'intermediate', 0.93, '查询结构较清晰，复盘条理性较好。', '进一步提炼可复用模板。', 'completed', '2026-04-08 19:00:00', NOW(), NOW(), 0);

INSERT INTO ability_goals (id, student_id, dimension_id, title, description, target_score, current_score, target_date, priority, status, achieved_at, created_at, updated_at)
VALUES
    (8001, 11, 4, '统一接口设计模板', '在课程项目二期前完成接口说明模板沉淀', 4.60, 4.00, '2026-04-20', 'medium', 'active', NULL, NOW(), NOW()),
    (8002, 12, 3, '补齐调试与排错能力', '针对鉴权作业中的异常场景做系统化排查', 4.25, 3.40, '2026-04-15', 'high', 'active', NULL, NOW(), NOW()),
    (8003, 13, 1, '提升论证完整度', '在汇报中更明确地区分事实、推论和建议', 4.40, 4.00, '2026-04-18', 'medium', 'active', NULL, NOW(), NOW());

INSERT INTO student_abilities (id, student_id, dimension_id, current_score, level, last_assessment_at, assessment_count, trend, created_at, updated_at)
VALUES
    (9001, 11, 1, 4.10, 'advanced', '2026-03-18 21:00:00', 3, 'rising', NOW(), NOW()),
    (9002, 11, 2, 4.40, 'advanced', '2026-03-18 21:00:00', 3, 'rising', NOW(), NOW()),
    (9003, 11, 3, 4.55, 'expert', '2026-03-18 21:00:00', 4, 'rising', NOW(), NOW()),
    (9004, 11, 4, 4.00, 'advanced', '2026-03-18 21:00:00', 3, 'stable', NOW(), NOW()),
    (9005, 12, 1, 3.50, 'intermediate', '2026-03-19 21:10:00', 1, 'stable', NOW(), NOW()),
    (9006, 12, 2, 3.80, 'advanced', '2026-03-19 21:10:00', 1, 'rising', NOW(), NOW()),
    (9007, 12, 3, 3.40, 'intermediate', '2026-03-19 21:10:00', 1, 'stable', NOW(), NOW()),
    (9008, 12, 4, 3.30, 'intermediate', '2026-03-19 21:10:00', 1, 'stable', NOW(), NOW()),
    (9009, 13, 1, 4.00, 'advanced', '2026-04-08 19:00:00', 2, 'rising', NOW(), NOW()),
    (9010, 13, 2, 4.20, 'advanced', '2026-04-08 19:00:00', 2, 'rising', NOW(), NOW()),
    (9011, 13, 3, 4.50, 'expert', '2026-04-08 19:00:00', 3, 'rising', NOW(), NOW()),
    (9012, 13, 4, 4.10, 'advanced', '2026-04-08 19:00:00', 2, 'rising', NOW(), NOW()),
    (9013, 14, 1, 3.90, 'advanced', '2026-03-27 17:00:00', 1, 'stable', NOW(), NOW()),
    (9014, 14, 2, 4.15, 'advanced', '2026-03-27 17:00:00', 1, 'rising', NOW(), NOW()),
    (9015, 14, 3, 3.95, 'advanced', '2026-03-27 17:00:00', 1, 'stable', NOW(), NOW()),
    (9016, 14, 4, 4.05, 'advanced', '2026-03-27 17:00:00', 1, 'rising', NOW(), NOW());

INSERT INTO ability_reports (id, student_id, report_type, title, overall_score, report_period_start, report_period_end, dimension_scores, trends_analysis, achievements, improvement_areas, recommendations, generated_by, is_published, created_at, published_at, updated_at, course_id, assignment_id, submission_id, ai_history_id)
VALUES
    (10001, 11, 'ai', '接口鉴权练习 AI 报告', 88.0, '2026-03-18', '2026-03-18', '{"moral_reasoning":4.1,"attitude":4.4,"ability":4.6,"strategy":4.0}', '{"overall":{"final_score":4.4,"dimension_averages":{"moral_reasoning":4.1,"attitude":4.4,"ability":4.6,"strategy":4.0},"holistic_feedback":"接口设计完整，鉴权逻辑稳定，建议继续补齐异常边界。"}}', '["完成 JWT 过滤器","补齐接口测试"]', '["异常边界说明","统一接口文档"]', '保持当前实现质量，继续增强边界测试。', 'teacher', 1, NOW(), NOW(), NOW(), 101, 1001, 5001, NULL),
    (10002, 13, 'ai', '统计查询实战 AI 报告', 86.0, '2026-04-08', '2026-04-08', '{"moral_reasoning":4.0,"attitude":4.2,"ability":4.5,"strategy":4.1}', '{"overall":{"final_score":4.3,"dimension_averages":{"moral_reasoning":4.0,"attitude":4.2,"ability":4.5,"strategy":4.1},"holistic_feedback":"统计查询覆盖完整，建议进一步校验空值和极端样本。"}}', '["完成多口径统计 SQL","补齐分页查询"]', '["空值样本处理","异常数据说明"]', '加强边界样本测试，避免统计口径漂移。', 'teacher', 1, NOW(), NOW(), NOW(), 102, 1004, 5004, NULL),
    (10003, 14, 'ai', '案例复盘汇报 AI 报告', 84.0, '2026-03-27', '2026-03-27', '{"moral_reasoning":3.9,"attitude":4.2,"ability":4.0,"strategy":4.1}', '{"overall":{"final_score":4.2,"dimension_averages":{"moral_reasoning":3.9,"attitude":4.2,"ability":4.0,"strategy":4.1},"holistic_feedback":"复盘结构清晰，建议在证据引用和结论收束上再加强。"}}', '["完成课堂复盘","表达节奏稳定"]', '["证据引用密度","结论收束"]', '适合用于验证 AI 报告回填结构化评估。', 'teacher', 1, NOW(), NOW(), NOW(), 103, 1005, 5005, NULL);

-- 课程权重：显式写入，避免不同环境出现默认权重漂移。
INSERT INTO course_ability_weights (course_id, dimension_code, weight, updated_at)
VALUES
    (101, 'MORAL_COGNITION', 0.90, NOW()),
    (101, 'LEARNING_ATTITUDE', 1.00, NOW()),
    (101, 'LEARNING_ABILITY', 1.20, NOW()),
    (101, 'LEARNING_METHOD', 1.00, NOW()),
    (101, 'ACADEMIC_GRADE', 1.10, NOW()),
    (102, 'MORAL_COGNITION', 0.90, NOW()),
    (102, 'LEARNING_ATTITUDE', 0.95, NOW()),
    (102, 'LEARNING_ABILITY', 1.25, NOW()),
    (102, 'LEARNING_METHOD', 1.10, NOW()),
    (102, 'ACADEMIC_GRADE', 1.15, NOW()),
    (103, 'MORAL_COGNITION', 1.10, NOW()),
    (103, 'LEARNING_ATTITUDE', 1.15, NOW()),
    (103, 'LEARNING_ABILITY', 0.95, NOW()),
    (103, 'LEARNING_METHOD', 1.05, NOW()),
    (103, 'ACADEMIC_GRADE', 0.75, NOW());

INSERT INTO file_records (id, original_name, saved_name, file_path, file_size, file_type, mime_type, uploader_id, related_type, related_id, download_count, created_at)
VALUES
    (11001, 'auth-service-lin.zip', 'auth-service-lin.zip', '/upload/seed/auth-service-lin.zip', 48231, 'zip', 'application/zip', 11, 'submission', 5001, 3, NOW()),
    (11002, 'index-design-lin.sql', 'index-design-lin.sql', '/upload/seed/index-design-lin.sql', 8120, 'sql', 'text/plain', 11, 'submission', 5003, 2, NOW()),
    (11003, 'radar-report-chen.sql', 'radar-report-chen.sql', '/upload/seed/radar-report-chen.sql', 12650, 'sql', 'text/plain', 13, 'submission', 5004, 1, NOW()),
    (11004, 'retro-zhao.pdf', 'retro-zhao.pdf', '/upload/seed/retro-zhao.pdf', 23112, 'pdf', 'application/pdf', 14, 'submission', 5005, 1, NOW());

INSERT INTO learning_recommendations (id, student_id, dimension_id, title, description, recommendation_type, resource_url, difficulty_level, estimated_time, priority_score, is_read, is_accepted, created_at, expires_at)
VALUES
    (12001, 11, 4, '整理接口文档模板', '把当前作业中的接口说明抽取成可复用模板', 'practice', NULL, 'intermediate', '2小时', 0.95, 0, 0, NOW(), '2026-04-10 23:59:59'),
    (12002, 12, 3, '补做异常链路测试', '围绕登录失败、Token 失效、权限不足补齐测试样例', 'practice', NULL, 'beginner', '3小时', 0.98, 0, 0, NOW(), '2026-04-05 23:59:59'),
    (12003, 13, 4, '沉淀统计查询模板', '把课程、班级、学生三类统计口径沉淀为统一 SQL 模板', 'project', NULL, 'advanced', '4小时', 0.92, 1, 0, NOW(), '2026-04-15 23:59:59'),
    (12004, 14, 1, '补充复盘证据链', '在汇报中明确区分客观事实、判断依据与行动建议', 'practice', NULL, 'intermediate', '1.5小时', 0.90, 0, 0, NOW(), '2026-04-08 23:59:59');

INSERT INTO tags (id, name, description, color, posts_count, created_at)
VALUES
    (13001, 'Java', 'Java 与 Spring 相关讨论', '#3B82F6', 1, NOW()),
    (13002, 'SQL', 'MySQL 与数据建模话题', '#10B981', 1, NOW()),
    (13003, '协作复盘', '表达、复盘与团队协作交流', '#F59E0B', 1, NOW());

INSERT INTO posts (id, title, content, category, author_id, pinned, anonymous, allow_comments, views, likes_count, comments_count, status, created_at, updated_at, deleted)
VALUES
    (14001, 'JWT 鉴权链路里，刷新令牌应该放在哪一层？', '我在课程项目里把刷新令牌放在 AuthService，但拦截器里也要判断过期，想听听大家的分层建议。', 'study', 11, 1, 0, 1, 86, 2, 2, 'published', NOW(), NOW(), 0),
    (14002, '统计查询里如何处理空值样本更稳妥？', '在做课程雷达统计时，AVG 和 COUNT 的口径不一致，大家通常怎么约束空值和无提交样本？', 'homework', 13, 0, 0, 1, 54, 1, 1, 'published', NOW(), NOW(), 0),
    (14003, '复盘汇报如何避免只有结论没有证据？', '我发现自己总结时容易直接下结论，缺少证据链，想请教大家怎么组织表达。', 'chat', 14, 0, 0, 1, 31, 1, 1, 'published', NOW(), NOW(), 0);

INSERT INTO post_comments (id, post_id, author_id, parent_id, content, likes_count, status, created_at, updated_at, deleted)
VALUES
    (15001, 14001, 2, NULL, '刷新令牌策略可以放在 AuthService，拦截器只负责识别与转发异常。', 2, 'published', NOW(), NOW(), 0),
    (15002, 14001, 12, NULL, '我这次是把异常码统一收口在 Filter 后面，前端提示会更稳。', 1, 'published', NOW(), NOW(), 0),
    (15003, 14002, 2, NULL, '先定义“有效样本”，再分别统计缺失样本数量，避免一个指标吞掉全部场景。', 1, 'published', NOW(), NOW(), 0),
    (15004, 14003, 3, NULL, '可以用“事实-解释-行动”三段式，先把证据写出来再下结论。', 1, 'published', NOW(), NOW(), 0);

INSERT INTO post_likes (id, post_id, user_id, created_at)
VALUES
    (16001, 14001, 12, NOW()),
    (16002, 14001, 2, NOW()),
    (16003, 14002, 11, NOW()),
    (16004, 14003, 13, NOW());

INSERT INTO comment_likes (id, comment_id, user_id, created_at)
VALUES
    (17001, 15001, 11, NOW()),
    (17002, 15002, 2, NOW()),
    (17003, 15003, 13, NOW()),
    (17004, 15004, 14, NOW());

INSERT INTO post_tags (id, post_id, tag_id, created_at)
VALUES
    (18001, 14001, 13001, NOW()),
    (18002, 14002, 13002, NOW()),
    (18003, 14003, 13003, NOW());

INSERT INTO notifications (id, user_id, title, content, type, category, priority, is_read, read_at, related_type, related_id, expired_at, created_at, updated_at, deleted, data, icon, action_url)
VALUES
    (19001, 11, '成绩已发布：接口鉴权练习', '你的《接口鉴权练习》成绩与四维能力分析已生成。', 'grade', 'course', 'normal', 1, NOW(), 'assignment', 1001, NULL, NOW(), NOW(), 0, '{"score":92}', 'award', '/student/assignments/1001'),
    (19002, 12, '待处理：接口鉴权练习', '你已提交作业，教师尚未完成批改，可先根据建议补充异常场景测试。', 'assignment', 'course', 'normal', 0, NULL, 'assignment', 1001, NULL, NOW(), NOW(), 0, '{"submissionId":5002}', 'clipboard-list', '/student/submissions/5002'),
    (19003, 13, '新建议：沉淀统计查询模板', '系统已根据你的统计查询表现生成新的学习建议。', 'system', 'ability', 'normal', 0, NULL, 'recommendation', 12003, NULL, NOW(), NOW(), 0, '{"dimension":"LEARNING_METHOD"}', 'light-bulb', '/student/ability'),
    (19004, 14, 'AI 报告可回填验证', '你的案例复盘 AI 报告已生成，当前可用回填脚本补齐结构化四维记录。', 'system', 'ability', 'normal', 0, NULL, 'report', 10003, NULL, NOW(), NOW(), 0, '{"reportId":10003}', 'sparkles', '/student/reports/10003');
