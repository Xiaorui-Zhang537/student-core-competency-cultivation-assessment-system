USE student_assessment_system;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE ability_assessments;
TRUNCATE TABLE ability_dimensions;
TRUNCATE TABLE course_ability_weights;
TRUNCATE TABLE ability_goals;
TRUNCATE TABLE ability_reports;
TRUNCATE TABLE abilities;
TRUNCATE TABLE analytics_cache;
TRUNCATE TABLE assignments;
TRUNCATE TABLE comment_likes;
TRUNCATE TABLE courses;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE file_records;
TRUNCATE TABLE grades;
TRUNCATE TABLE learning_recommendations;
TRUNCATE TABLE lesson_progress;
TRUNCATE TABLE lessons;
TRUNCATE TABLE notifications;
TRUNCATE TABLE post_comments;
TRUNCATE TABLE post_likes;
TRUNCATE TABLE post_tags;
TRUNCATE TABLE posts;
TRUNCATE TABLE student_abilities;
TRUNCATE TABLE submissions;
TRUNCATE TABLE tags;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 用户表：存储所有用户（学生、教师、管理员）
-- =========================
-- 初始登录密码（开发/演示数据）：12345678
INSERT INTO users (id, username, email, password, role, status, avatar, nickname, first_name, last_name, bio, grade, subject, school, phone, created_at, updated_at, deleted, email_verified)
VALUES
    (1, 'admin', 'admin+seed-20260214@local.test', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'admin', 'active', '', '系统管理员', '系统', '管理员', '负责系统管理', NULL, NULL, '信息中心', '13800000000', NOW(), NOW(), 0, 1),
    (2, 'teacher', 'teacher@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'teacher', 'active', '', '张老师', '张', '老师', '专注教学', NULL, '计算机', '软件学院', '13800000001', NOW(), NOW(), 0, 1),
    (3, 'student1', 'student1@example.com', '$2y$10$w/WC3WahuiIUzyg/uJEEU.JvfEIGdWqy7BZvQwsDTSvoL4yssEJ.y', 'student', 'active', '', '李雷', '李', '雷', '2023级学生', '2023级', '软件工程', '软件学院', '13800000002', NOW(), NOW(), 0, 1);

-- =========================
-- 能力维度表：定义能力的维度类型
-- =========================
-- 固定四个非成绩维度：道德认知、学习态度、学习能力、学习方法
INSERT INTO ability_dimensions (id, name, code, description, category, weight, max_score, icon, color, sort_order, is_active, created_at, updated_at)
VALUES
    (1, '道德认知', 'MORAL_COGNITION', '价值观与道德判断', 'soft', 1.00, 100, 'scale', '#EF4444', 1, 1, NOW(), NOW()),
    (2, '学习态度', 'LEARNING_ATTITUDE', '积极性、坚持性与自律', 'soft', 1.00, 100, 'heart', '#F59E0B', 2, 1, NOW(), NOW()),
    (3, '学习能力', 'LEARNING_ABILITY', '理解、分析与迁移应用', 'academic', 1.00, 100, 'sparkles', '#3B82F6', 3, 1, NOW(), NOW()),
    (4, '学习方法', 'LEARNING_METHOD', '策略、反思与资源利用', 'academic', 1.00, 100, 'book-open', '#10B981', 4, 1, NOW(), NOW());

-- =========================
-- 能力库表：所有能力项（挂靠维度）
-- =========================
-- abilities 表可按需保留，当前与雷达图无直接耦合；示例数据移除


-- =========================
-- 课程表：存储课程基本信息
-- =========================
INSERT INTO courses (id, title, description, cover_image, teacher_id, category, difficulty, duration, max_students, price, status, start_date, end_date, tags, objectives, requirements, created_at, updated_at, deleted)
VALUES
    (1, 'Java程序设计', '学习Java基本语法与面向对象思想', '', 2, '编程', 'beginner', 4800, 50, 0, 'published', '2024-09-01', '2024-12-31', NULL, '掌握Java基础', '无', NOW(), NOW(), 0),
    (2, '数据库基础', '掌握关系型数据库和SQL语句', '', 2, '数据库', 'intermediate', 3600, 40, 0, 'published', '2024-09-15', '2024-12-15', NULL, '掌握SQL基础', 'Java基础', NOW(), NOW(), 0),
    (3, '沟通训练', '提升沟通表达能力', '', 2, '软技能', 'beginner', 1200, 30, 0, 'published', '2024-09-20', '2024-10-30', NULL, '提高表达协作', '无', NOW(), NOW(), 0);

-- =========================
-- 作业表：课程作业和考核任务
-- =========================
INSERT INTO assignments (id, course_id, teacher_id, title, description, requirements, max_score, due_date, allow_late, max_attempts, file_types, max_file_size, status, created_at, updated_at, deleted)
VALUES
    (1, 1, 2, 'Java基础练习', '完成基础语法题', '代码需加注释', 100, '2024-10-10 23:59:59', 1, 3, 'java,txt', 10, 'published', NOW(), NOW(), 0),
    (2, 2, 2, 'SQL练习', '编写SQL完成查询', '附带SQL文件', 100, '2024-11-01 23:59:59', 1, 2, 'sql,txt', 10, 'published', NOW(), NOW(), 0),
    (3, 3, 2, '沟通案例分析', '分析沟通案例', '提交报告', 100, '2024-10-20 23:59:59', 0, 1, 'doc,txt', 10, 'published', NOW(), NOW(), 0);

-- =========================
-- 选课表：学生选修课程的关系
-- =========================
INSERT INTO enrollments (id, student_id, course_id, enrolled_at, progress, status, grade, completion_date, created_at, updated_at, last_access_time)
VALUES
    (1, 3, 1, NOW(), 75.0, 'active', 85.0, NULL, NOW(), NOW(), NULL),
    (2, 3, 2, NOW(), 60.0, 'active', NULL, NULL, NOW(), NOW(), NULL),
    (3, 3, 3, NOW(), 90.0, 'completed', 92.0, NOW(), NOW(), NOW(), NOW());

-- =========================
-- 课程章节表：每门课的章节和小节内容
-- =========================
INSERT INTO lessons (id, course_id, title, description, content, video_url, duration, order_index, is_free, created_at, updated_at, deleted)
VALUES
    (1, 1, 'Java开发环境', '环境搭建', '安装JDK和IDE', NULL, 45, 1, 1, NOW(), NOW(), 0),
    (2, 1, 'Java语法基础', '变量、语句', '基础语法知识', NULL, 60, 2, 1, NOW(), NOW(), 0),
    (3, 2, 'SQL基础', '数据库操作', '基础SQL语句', NULL, 50, 1, 1, NOW(), NOW(), 0);

-- =========================
-- 章节学习进度表：学生每章的学习进度记录
-- =========================
INSERT INTO lesson_progress (id, student_id, lesson_id, progress, completed, watch_time, completed_at, created_at, updated_at, last_studied_at, course_id, view_count, last_position, status, notes, rating, started_at)
VALUES
    (1, 3, 1, 100.0, 1, 2700, NOW(), NOW(), NOW(), NOW(), 1, 1, 0, 'finished', NULL, 5, NOW()),
    (2, 3, 2, 80.0, 0, 2200, NULL, NOW(), NOW(), NOW(), 1, 1, 0, 'studying', NULL, 4, NOW()),
    (3, 3, 3, 100.0, 1, 3000, NOW(), NOW(), NOW(), NOW(), 2, 1, 0, 'finished', NULL, 5, NOW());

-- =========================
-- 作业提交表：学生作业上传和提交记录
-- =========================
INSERT INTO submissions (id, assignment_id, student_id, content, file_path, file_name, file_size, submission_count, status, submitted_at, is_late, created_at, updated_at)
VALUES
    (1, 1, 3, '完成Java代码', '/upload/Java1.java', 'Java1.java', 2000, 1, 'graded', '2024-10-09 10:00:00', 0, NOW(), NOW()),
    (2, 2, 3, '完成SQL语句', '/upload/SQL1.sql', 'SQL1.sql', 1500, 1, 'submitted', '2024-11-01 19:00:00', 0, NOW(), NOW()),
    (3, 3, 3, '沟通案例分析报告', '/upload/report.doc', 'report.doc', 1200, 1, 'graded', '2024-10-21 14:00:00', 0, NOW(), NOW());

-- =========================
-- 成绩表：记录每次作业的分数及反馈
-- =========================
INSERT INTO grades (id, student_id, assignment_id, score, max_score, percentage, grade_level, feedback, status, deleted, created_at, updated_at, submission_id, grader_id)
VALUES
    (1, 3, 1, 88.0, 100.0, 88.0, 'B', '代码规范良好', 'published', 0, NOW(), NOW(), 1, 2),
    (2, 3, 2, 95.0, 100.0, 95.0, 'A', 'SQL用法全面', 'published', 0, NOW(), NOW(), 2, 2),
    (3, 3, 3, 90.0, 100.0, 90.0, 'A', '分析透彻', 'published', 0, NOW(), NOW(), 3, 2);

-- =========================
-- 标签表：社区话题标签
-- =========================
INSERT INTO tags (id, name, description, color, posts_count, created_at)
VALUES
    (1, 'Java', 'Java编程相关', '#3B82F6', 2, NOW()),
    (2, 'SQL', 'SQL数据库话题', '#10B981', 1, NOW()),
    (3, '沟通技巧', '沟通表达讨论', '#F59E0B', 1, NOW());

-- =========================
-- 帖子表：论坛与问答社区的帖子
-- =========================
INSERT INTO posts (id, title, content, category, author_id, pinned, anonymous, allow_comments, views, likes_count, comments_count, status, created_at, updated_at, deleted)
VALUES
    (1, 'Java循环结构求助', 'for循环中遇到死循环，求解！', 'homework', 3, 0, 0, 1, 50, 2, 2, 'published', NOW(), NOW(), 0),
    (2, 'SQL连接查询经验', '多表关联查询有哪些注意点？', 'study', 3, 0, 0, 1, 30, 1, 1, 'published', NOW(), NOW(), 0),
    (3, '沟通小组招募', '组建沟通训练小组，欢迎加入！', 'chat', 3, 0, 0, 1, 10, 0, 0, 'published', NOW(), NOW(), 0);

-- =========================
-- 帖子评论表：论坛帖子的评论
-- =========================
INSERT INTO post_comments (id, post_id, author_id, parent_id, content, likes_count, status, created_at, updated_at, deleted)
VALUES
    (1, 1, 2, NULL, '可以检查循环条件设置。', 3, 'published', NOW(), NOW(), 0),
    (2, 2, 3, NULL, '建议用EXISTS子句。', 1, 'published', NOW(), NOW(), 0),
    (3, 3, 2, NULL, '沟通小组有什么活动？', 2, 'published', NOW(), NOW(), 0);

-- =========================
-- 帖子点赞表：记录谁点赞了哪些帖子
-- =========================
INSERT INTO post_likes (id, post_id, user_id, created_at)
VALUES
    (1, 1, 3, NOW()),
    (2, 2, 2, NOW()),
    (3, 3, 3, NOW());

-- =========================
-- 评论点赞表：记录谁点赞了哪些评论
-- =========================
INSERT INTO comment_likes (id, comment_id, user_id, created_at)
VALUES
    (1, 1, 3, NOW()),
    (2, 2, 2, NOW()),
    (3, 3, 3, NOW());

-- =========================
-- 帖子标签关联表：帖子与标签多对多关系
-- =========================
INSERT INTO post_tags (id, post_id, tag_id, created_at)
VALUES
    (1, 1, 1, NOW()),
    (2, 2, 2, NOW()),
    (3, 3, 3, NOW());

-- =========================
-- 通知表：系统和课程的消息通知
-- =========================
INSERT INTO notifications (id, user_id, title, content, type, category, priority, is_read, read_at, related_type, related_id, expired_at, created_at, updated_at, deleted, data, icon, action_url)
VALUES
    (1, 3, '新作业发布', '请完成Java基础练习', 'assignment', NULL, 'normal', 0, NULL, 'assignment', 1, NULL, NOW(), NOW(), 0, NULL, NULL, NULL),
    (2, 3, '成绩已公布', '您的SQL练习成绩已发布', 'grade', NULL, 'normal', 1, NOW(), 'assignment', 2, NULL, NOW(), NOW(), 0, NULL, NULL, NULL),
    (3, 3, '小组通知', '沟通小组有新活动', 'system', NULL, 'normal', 0, NULL, NULL, NULL, NULL, NOW(), NOW(), 0, NULL, NULL, NULL);

-- =========================
-- 分析缓存表：缓存统计分析结果
-- =========================
INSERT INTO analytics_cache (cache_key, cache_value, generated_at)
VALUES
    ('user:3:summary', '{"score":88,"rank":2}', NOW()),
    ('course:1:analysis', '{"avg":90,"top":100}', NOW()),
    ('system:notice', '{"msg":"本周六停机维护"}', NOW());

-- =========================
-- 能力评估表：学生各能力维度的评估记录
-- =========================
INSERT INTO ability_assessments (id, student_id, dimension_id, ability_id, assessor_id, assessment_type, related_id, score, max_score, ability_level, confidence, evidence, improvement, status, assessed_at, created_at, updated_at, deleted)
VALUES
    (1, 3, 1, NULL, 2, 'assignment', 1, 78, 100, 'beginner', 0.95, '课堂表现与作业', '加强案例讨论', 'completed', NOW(), NOW(), NOW(), 0),
    (2, 3, 2, NULL, 2, 'project', 2, 82, 100, 'intermediate', 0.90, '小组项目协作', '提升时间管理', 'completed', NOW(), NOW(), NOW(), 0),
    (3, 3, 3, NULL, 2, 'exam', 3, 88, 100, 'advanced', 0.98, '阶段测评', '加强迁移训练', 'completed', NOW(), NOW(), NOW(), 0),
    (4, 3, 4, NULL, 2, 'assignment', 1, 75, 100, 'intermediate', 0.92, '学习反思', '优化方法结构', 'completed', NOW(), NOW(), NOW(), 0);

-- =========================
-- 能力发展目标表：学生针对维度设置的发展目标
-- =========================
INSERT INTO ability_goals (id, student_id, dimension_id, title, description, target_score, current_score, target_date, priority, status, achieved_at, created_at, updated_at)
VALUES
    (1, 3, 1, '提升编程', '期末前编程能力90分', 90, 80, '2024-12-31', 'high', 'active', NULL, NOW(), NOW()),
    (2, 3, 2, '突破沟通', '提升沟通表达能力', 85, 70, '2024-12-15', 'medium', 'active', NULL, NOW(), NOW()),
    (3, 3, 3, '创新挑战', '获得创新大赛奖', 95, 88, '2024-11-30', 'high', 'active', NULL, NOW(), NOW());

-- =========================
-- 能力报告表：每学期/月/年生成的能力分析报告
-- =========================
INSERT INTO ability_reports (id, student_id, report_type, title, overall_score, report_period_start, report_period_end, dimension_scores, trends_analysis, achievements, improvement_areas, recommendations, generated_by, is_published, created_at, published_at, updated_at)
VALUES
    (1, 3, 'monthly', '10月报告', 82, '2024-10-01', '2024-10-31', '{"编程":80,"沟通":70}', '稳步提升', '["完成Java作业"]', '["沟通表达"]', '多参加交流', 'system', 1, NOW(), NOW(), NOW()),
    (2, 3, 'semester', '上学期总结', 85, '2024-09-01', '2024-12-31', '{"创新":88}', '有波动', '["创新项目"]', '["团队协作"]', '加强团队活动', 'teacher', 1, NOW(), NOW(), NOW()),
    (3, 3, 'monthly', '11月报告', 79, '2024-11-01', '2024-11-30', '{"创新":76}', '上升中', '["创新获奖"]', '["理论基础"]', '加强理论', 'system', 1, NOW(), NOW(), NOW());

-- =========================
-- 学生能力记录表：每位学生当前各维度能力总览
-- =========================
INSERT INTO student_abilities (id, student_id, dimension_id, current_score, level, last_assessment_at, assessment_count, trend, created_at, updated_at)
VALUES
    (1, 3, 1, 78, 'beginner', NOW(), 3, 'rising', NOW(), NOW()),
    (2, 3, 2, 82, 'intermediate', NOW(), 2, 'stable', NOW(), NOW()),
    (3, 3, 3, 88, 'advanced', NOW(), 4, 'rising', NOW(), NOW()),
    (4, 3, 4, 75, 'intermediate', NOW(), 2, 'rising', NOW(), NOW());

-- 为课程初始化默认权重（等权 1.00），包含学习成绩维度
INSERT INTO course_ability_weights (course_id, dimension_code, weight, updated_at) VALUES
  (1, 'MORAL_COGNITION', 1.00, NOW()),
  (1, 'LEARNING_ATTITUDE', 1.00, NOW()),
  (1, 'LEARNING_ABILITY', 1.00, NOW()),
  (1, 'LEARNING_METHOD', 1.00, NOW()),
  (1, 'ACADEMIC_GRADE', 1.00, NOW());

-- =========================
-- 文件记录表：上传文件的管理
-- =========================
INSERT INTO file_records (id, original_name, saved_name, file_path, file_size, file_type, mime_type, uploader_id, related_type, related_id, download_count, created_at)
VALUES
    (1, 'Java1.java', 'Java1.java', '/upload/Java1.java', 2000, 'java', 'text/x-java-source', 3, 'assignment', 1, 0, NOW()),
    (2, 'SQL1.sql', 'SQL1.sql', '/upload/SQL1.sql', 1500, 'sql', 'text/x-sql', 3, 'assignment', 2, 0, NOW()),
    (3, 'report.doc', 'report.doc', '/upload/report.doc', 1200, 'doc', 'application/msword', 3, 'assignment', 3, 0, NOW());

-- =========================
-- 学习建议表：个性化学习推荐与建议
-- =========================
INSERT INTO learning_recommendations (id, student_id, dimension_id, title, description, recommendation_type, resource_url, difficulty_level, estimated_time, priority_score, is_read, is_accepted, created_at, expires_at)
VALUES
    (1, 3, 1, '多做LeetCode', '提升编程能力', 'course', NULL, 'intermediate', '10小时', 1, 0, 0, NOW(), NULL),
    (2, 3, 2, '参加演讲比赛', '提升沟通能力', 'practice', NULL, 'beginner', '2小时', 1, 0, 0, NOW(), NULL),
    (3, 3, 3, '参与创新项目', '提升创新实践', 'project', NULL, 'advanced', '30小时', 1, 0, 0, NOW(), NULL);