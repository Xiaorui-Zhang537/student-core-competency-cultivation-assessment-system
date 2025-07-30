-- 学生非核心能力发展评估系统初始化数据
-- 创建时间: 2024-12-28
-- 作者: System

USE student_assessment_system;

-- 清空现有数据（开发环境使用）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE lesson_progress;
TRUNCATE TABLE notifications;
TRUNCATE TABLE file_records;
TRUNCATE TABLE grades;
TRUNCATE TABLE submissions;
TRUNCATE TABLE assignments;
TRUNCATE TABLE lessons;
TRUNCATE TABLE enrollments;
TRUNCATE TABLE courses;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 插入测试用户数据
-- 密码: 123456 (BCrypt加密后的值)
INSERT INTO users (id, username, email, password, role, first_name, last_name, display_name, avatar, bio, grade, subject, school, phone) VALUES
-- 管理员账户
(1, 'admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin', '系统', '管理员', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '系统管理员账户', NULL, NULL, '系统管理部', '13800000000'),

-- 教师账户
(2, 'teacher', 'teacher@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'teacher', '张', '老师', '张老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher', '专注于学生能力发展评估研究', NULL, '计算机科学', '计算机学院', '13800138001'),

(3, 'teacher2', 'wang@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'teacher', '王', '教授', '王教授', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wang', '数据库系统与大数据技术专家', NULL, '数据库技术', '计算机学院', '13800138002'),

-- 学生账户
(4, 'student', 'student@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student', '李', '同学', '李同学', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student', '积极参与各项能力发展活动的学生', '2023级', '软件工程', '软件工程学院', '13800138003'),

(5, 'student2', 'liu@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student', '刘', '小明', '刘小明', 'https://api.dicebear.com/7.x/avataaars/svg?seed=liu', '热爱编程，喜欢挑战新技术', '2023级', '计算机科学', '计算机学院', '13800138004'),

(6, 'student3', 'chen@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'student', '陈', '小红', '陈小红', 'https://api.dicebear.com/7.x/avataaars/svg?seed=chen', '对人工智能和机器学习很感兴趣', '2024级', '人工智能', '计算机学院', '13800138005');

-- 2. 插入课程数据
INSERT INTO courses (id, title, description, cover_image, teacher_id, category, difficulty, duration, max_students, status, start_date, end_date, objectives, requirements) VALUES
(1, 'Java程序设计基础', '零基础学习Java编程，掌握面向对象编程思想，为后续课程打下坚实基础。', 'https://via.placeholder.com/400x200/4CAF50/FFFFFF?text=Java', 2, '编程基础', 'beginner', 4800, 50, 'published', '2024-09-01', '2024-12-31', 'Java语法基础;面向对象编程;集合框架;异常处理', '无编程基础要求'),

(2, '数据库系统原理', '深入学习关系型数据库理论，掌握SQL语言，理解数据库设计原则。', 'https://via.placeholder.com/400x200/2196F3/FFFFFF?text=Database', 3, '数据库', 'intermediate', 3600, 40, 'published', '2024-09-15', '2024-12-15', '关系型数据库理论;SQL语言;数据库设计;性能优化', 'Java程序设计基础'),

(3, 'Web开发技术', '全栈Web开发技术，包括前端HTML/CSS/JavaScript和后端Spring Boot。', 'https://via.placeholder.com/400x200/FF9800/FFFFFF?text=Web', 2, 'Web开发', 'intermediate', 5400, 35, 'published', '2024-10-01', '2025-01-31', '前端开发技术;后端框架;数据库集成;项目实战', 'Java程序设计基础;数据库系统原理'),

(4, '软件工程导论', '软件开发生命周期、项目管理、团队协作等软件工程核心概念。', 'https://via.placeholder.com/400x200/9C27B0/FFFFFF?text=Software', 2, '软件工程', 'beginner', 2400, 60, 'published', '2024-09-10', '2024-11-30', '软件开发流程;项目管理;需求分析;测试方法', '无特殊要求'),

(5, '人工智能基础', '人工智能基础理论，机器学习算法，深度学习入门。', 'https://via.placeholder.com/400x200/E91E63/FFFFFF?text=AI', 3, '人工智能', 'advanced', 4200, 30, 'draft', '2025-02-01', '2025-05-31', '机器学习算法;深度学习;神经网络;实际应用', 'Python编程基础;高等数学');

-- 3. 插入选课数据
INSERT INTO enrollments (student_id, course_id, progress, status, grade) VALUES
(4, 1, 85.5, 'active', 88.5),
(4, 2, 72.3, 'active', NULL),
(4, 4, 100.0, 'completed', 92.0),
(5, 1, 95.2, 'completed', 95.5),
(5, 2, 68.7, 'active', NULL),
(5, 3, 45.8, 'active', NULL),
(6, 1, 23.4, 'active', NULL),
(6, 4, 78.9, 'active', 85.0);

-- 4. 插入课程章节数据
INSERT INTO lessons (course_id, title, description, content, duration, order_index, is_free) VALUES
-- Java课程章节
(1, 'Java开发环境搭建', '学习如何安装JDK和IDE，配置开发环境', 'Java开发环境的安装和配置详细步骤...', 45, 1, true),
(1, 'Java基础语法', '变量、数据类型、运算符、控制结构', 'Java语言的基础语法介绍...', 60, 2, true),
(1, '面向对象编程（一）', '类、对象、封装、继承', '面向对象编程的核心概念...', 75, 3, false),
(1, '面向对象编程（二）', '多态、抽象类、接口', '高级面向对象编程特性...', 80, 4, false),
(1, '集合框架', 'List、Set、Map及其实现类', 'Java集合框架的使用...', 70, 5, false),

-- 数据库课程章节
(2, '关系型数据库概述', '数据库基本概念、关系模型', '数据库系统的基础理论...', 50, 1, true),
(2, 'SQL基础语法', 'DDL、DML、DQL语句', 'SQL语言的基础操作...', 65, 2, false),
(2, '数据库设计', '范式理论、ER模型', '如何设计一个好的数据库...', 70, 3, false),
(2, '索引与性能优化', '索引原理、查询优化', '数据库性能优化技巧...', 60, 4, false),

-- Web开发课程章节
(3, 'HTML与CSS基础', '网页结构与样式', 'Web前端基础技术...', 90, 1, true),
(3, 'JavaScript编程', 'JS语法、DOM操作、事件处理', 'JavaScript编程基础...', 100, 2, false),
(3, 'Spring Boot入门', '微服务框架基础', 'Spring Boot开发入门...', 120, 3, false),
(3, '前后端交互', 'AJAX、RESTful API', '前后端数据交互...', 85, 4, false);

-- 5. 插入作业数据
INSERT INTO assignments (course_id, teacher_id, title, description, requirements, max_score, due_date, allow_late, max_attempts, file_types, status) VALUES
(1, 2, 'Java基础编程练习', '完成基础语法练习题，包括变量定义、循环结构、条件判断等', '请提交完整的Java源代码文件，代码需要有适当注释', 100.0, '2024-10-15 23:59:59', true, 3, 'java,txt', 'published'),

(1, 2, '面向对象程序设计', '设计并实现一个简单的学生管理系统', '使用面向对象思想，包含Student、Course、Manager等类', 100.0, '2024-11-30 23:59:59', true, 2, 'java,zip', 'published'),

(2, 3, '数据库设计作业', '为图书管理系统设计数据库表结构', '提交ER图和建表SQL语句，需要符合第三范式', 100.0, '2024-11-20 23:59:59', false, 1, 'sql,pdf,doc,docx', 'published'),

(3, 2, 'Web项目实战', '开发一个完整的Web应用', '包含前端页面和后端API，实现用户注册登录功能', 100.0, '2025-01-15 23:59:59', true, 1, 'zip,rar', 'published');

-- 6. 插入作业提交数据
INSERT INTO submissions (assignment_id, student_id, content, file_name, submission_count, status, submitted_at, is_late) VALUES
(1, 4, '完成了所有的基础语法练习，包括循环、条件判断等', 'JavaBasics.java', 1, 'graded', '2024-10-14 16:30:00', false),
(1, 5, '基础语法练习代码，已通过本地测试', 'Exercise1.java', 2, 'graded', '2024-10-15 22:45:00', false),
(2, 4, '学生管理系统，实现了学生信息的增删改查功能', 'StudentManager.zip', 1, 'submitted', '2024-11-28 20:15:00', false),
(3, 5, '图书管理系统数据库设计，包含图书、读者、借阅等表', 'LibraryDB.sql', 1, 'graded', '2024-11-19 14:20:00', false);

-- 7. 插入成绩数据
INSERT INTO grades (submission_id, teacher_id, score, max_score, feedback, graded_at) VALUES
(1, 2, 88.0, 100.0, '代码结构清晰，逻辑正确。建议在变量命名上更加规范，增加更多注释。', '2024-10-16 09:30:00'),
(2, 2, 92.0, 100.0, '完成质量很高，代码风格良好。对面向对象的理解比较到位。', '2024-10-16 10:15:00'),
(4, 3, 95.0, 100.0, '数据库设计合理，符合范式要求。ER图绘制规范，SQL语句正确。', '2024-11-20 11:00:00');

-- 8. 插入学习进度数据
INSERT INTO lesson_progress (student_id, lesson_id, progress, completed, watch_time, completed_at) VALUES
-- 学生4的学习进度
(4, 1, 100.0, true, 2700, '2024-09-05 14:30:00'),
(4, 2, 100.0, true, 3600, '2024-09-08 16:20:00'),
(4, 3, 100.0, true, 4500, '2024-09-15 19:45:00'),
(4, 4, 85.0, false, 4080, NULL),
(4, 6, 100.0, true, 3000, '2024-09-20 10:30:00'),
(4, 7, 75.0, false, 2925, NULL),

-- 学生5的学习进度
(5, 1, 100.0, true, 2700, '2024-09-03 13:15:00'),
(5, 2, 100.0, true, 3600, '2024-09-06 15:30:00'),
(5, 3, 100.0, true, 4500, '2024-09-12 18:20:00'),
(5, 4, 100.0, true, 4800, '2024-09-18 20:10:00'),
(5, 5, 100.0, true, 4200, '2024-09-25 16:45:00'),
(5, 6, 100.0, true, 3000, '2024-09-28 11:20:00'),
(5, 7, 45.0, false, 1755, NULL);

-- 9. 插入通知数据
INSERT INTO notifications (user_id, title, content, type, priority, is_read) VALUES
(4, '新作业发布', 'Java基础编程练习已发布，请及时完成', 'assignment', 'normal', true),
(4, '成绩已公布', '您的Java基础编程练习成绩已公布，得分88分', 'grade', 'normal', false),
(5, '课程更新', 'Web开发技术课程新增了前端框架内容', 'course', 'low', true),
(5, '作业提醒', '数据库设计作业截止日期临近，请抓紧时间完成', 'assignment', 'high', false),
(6, '欢迎加入', '欢迎加入学生非核心能力发展评估系统！', 'system', 'normal', true);

-- 重置自增ID
ALTER TABLE users AUTO_INCREMENT = 7;
ALTER TABLE courses AUTO_INCREMENT = 6;
ALTER TABLE lessons AUTO_INCREMENT = 13;
ALTER TABLE assignments AUTO_INCREMENT = 5;
ALTER TABLE submissions AUTO_INCREMENT = 5;
ALTER TABLE grades AUTO_INCREMENT = 4;
ALTER TABLE enrollments AUTO_INCREMENT = 9;
ALTER TABLE lesson_progress AUTO_INCREMENT = 13;
ALTER TABLE notifications AUTO_INCREMENT = 6;

-- 显示插入结果统计
SELECT 
    '用户' as 表名, COUNT(*) as 记录数 FROM users
UNION ALL SELECT 
    '课程', COUNT(*) FROM courses
UNION ALL SELECT 
    '选课', COUNT(*) FROM enrollments  
UNION ALL SELECT 
    '章节', COUNT(*) FROM lessons
UNION ALL SELECT 
    '作业', COUNT(*) FROM assignments
UNION ALL SELECT 
    '提交', COUNT(*) FROM submissions
UNION ALL SELECT 
    '成绩', COUNT(*) FROM grades
UNION ALL SELECT 
    '进度', COUNT(*) FROM lesson_progress
UNION ALL SELECT 
    '通知', COUNT(*) FROM notifications; 

-- ===================================
-- 社区功能测试数据
-- ===================================

-- 插入标签数据
INSERT INTO tags (id, name, description, color, posts_count) VALUES
(1, '微积分', '高等数学微积分相关讨论', '#3B82F6', 15),
(2, '线性代数', '线性代数学习交流', '#10B981', 12),
(3, '期中考试', '期中考试相关话题', '#F59E0B', 25),
(4, '编程作业', '编程类作业讨论', '#8B5CF6', 18),
(5, '学习方法', '学习技巧和方法分享', '#EF4444', 20),
(6, '导数', '导数计算和应用', '#06B6D4', 8),
(7, '链式法则', '导数链式法则', '#84CC16', 5),
(8, '复习计划', '考试复习规划', '#F97316', 10),
(9, '矩阵', '矩阵运算相关', '#EC4899', 7),
(10, '时间管理', '学习时间管理技巧', '#6366F1', 6);

-- 插入帖子数据
INSERT INTO posts (id, title, content, category, author_id, pinned, anonymous, allow_comments, views, likes_count, comments_count, status, created_at) VALUES
(1, '求助：微积分第三章习题解答', '大家好，我在做微积分第三章的习题时遇到了困难，特别是关于导数的链式法则部分。有没有同学可以帮忙解答一下？我已经尝试了多种方法，但是总是在复合函数的求导上出错。希望有经验的同学能够指点一下，最好能提供一些具体的例题解析。', 'homework', 3, FALSE, FALSE, TRUE, 156, 8, 12, 'published', '2024-01-15 10:30:00'),
(2, '分享：我的高数学习心得', '经过一学期的学习，我总结了一些高等数学的学习方法和技巧，希望对大家有帮助。首先是要理解概念的本质，不要死记硬背公式。其次是要多做练习题，通过练习来加深理解。最后是要及时复习，巩固所学知识。我发现制作思维导图特别有用，可以帮助梳理知识点之间的关系。另外，组建学习小组互相讨论也很有效果。', 'share', 4, TRUE, FALSE, TRUE, 432, 67, 25, 'published', '2024-01-14 14:15:00'),
(3, '讨论：期中考试复习计划', '期中考试快到了，大家都是怎么安排复习计划的？我感觉时间有点紧张，不知道该从哪里开始复习。有没有学长学姐可以分享一下复习经验？特别是如何在有限的时间内高效复习多门课程。我现在主要担心的是数学和物理这两门课，感觉知识点太多了。', 'study', 5, FALSE, FALSE, TRUE, 289, 23, 18, 'published', '2024-01-13 16:45:00'),
(4, '问答：线性代数矩阵运算', '请问矩阵的逆运算有什么简便的计算方法吗？课本上的方法感觉比较复杂。我看到有同学用行变换的方法，但是我总是算错。是不是有什么技巧可以避免计算错误？另外，什么情况下矩阵没有逆矩阵？希望有大神能够详细解答一下。', 'qa', 6, FALSE, FALSE, TRUE, 178, 15, 9, 'published', '2024-01-12 11:20:00'),
(5, '学习小组招募：高等数学互助', '我想组建一个高等数学学习小组，大家一起学习，互相帮助。我们可以定期聚会讨论难题，分享学习资源，还可以一起刷题。目前我们已经有3个人了，希望再招募2-3个志同道合的同学。如果你对高数学习有热情，欢迎加入我们！', 'study', 2, FALSE, FALSE, TRUE, 95, 12, 6, 'published', '2024-01-11 09:30:00'),
(6, '吐槽：这次作业也太难了吧', '不知道是不是只有我觉得这次的编程作业特别难？题目要求实现一个复杂的算法，但是老师讲得不够详细。我已经花了一整个周末在这个作业上了，还是没有头绪。有同学已经做出来了吗？可以给点提示吗？', 'chat', 3, FALSE, TRUE, TRUE, 67, 8, 14, 'published', '2024-01-10 20:15:00');

-- 插入帖子标签关联数据
INSERT INTO post_tags (post_id, tag_id) VALUES
(1, 1), (1, 6), (1, 7),  -- 微积分、导数、链式法则
(2, 1), (2, 5),          -- 微积分、学习方法
(3, 3), (3, 8), (3, 10), -- 期中考试、复习计划、时间管理
(4, 2), (4, 9),          -- 线性代数、矩阵
(5, 1), (5, 5),          -- 微积分、学习方法
(6, 4);                  -- 编程作业

-- 插入帖子评论数据
INSERT INTO post_comments (id, post_id, author_id, parent_id, content, likes_count, status, created_at) VALUES
(1, 1, 4, NULL, '我也遇到了同样的问题，期待有人解答！', 3, 'published', '2024-01-15 11:00:00'),
(2, 1, 5, NULL, '链式法则的关键是要分清楚复合函数的结构，建议多练习几道题目。', 8, 'published', '2024-01-15 14:30:00'),
(3, 1, 2, 2, '说得对，我当时也是通过大量练习才掌握的。推荐做一下教材第三章的练习题。', 2, 'published', '2024-01-15 16:20:00'),
(4, 2, 3, NULL, '谢谢分享！这些方法很实用，特别是思维导图的建议。', 5, 'published', '2024-01-14 15:00:00'),
(5, 2, 6, NULL, '学习小组的想法很好，我们班也组织了类似的活动，效果确实不错。', 4, 'published', '2024-01-14 17:30:00'),
(6, 3, 2, NULL, '我建议先做个时间表，把每门课的重点内容列出来，然后按优先级复习。', 6, 'published', '2024-01-13 18:00:00'),
(7, 3, 4, 6, '同意，而且要留出时间做模拟题，这样能更好地检验复习效果。', 3, 'published', '2024-01-14 10:20:00'),
(8, 4, 2, NULL, '可以试试用伴随矩阵的方法，对于3x3以下的矩阵比较简单。', 7, 'published', '2024-01-12 14:45:00'),
(9, 4, 5, NULL, '矩阵没有逆的充要条件是行列式为0，这个要记住。', 4, 'published', '2024-01-13 08:30:00'),
(10, 5, 3, NULL, '我很有兴趣加入！什么时候开始？', 2, 'published', '2024-01-11 10:15:00'),
(11, 5, 6, NULL, '我也想加入，我的高数成绩还不错，可以帮助其他同学。', 3, 'published', '2024-01-11 15:30:00'),
(12, 6, 4, NULL, '我也觉得这次作业很难，建议大家可以先看看相关的算法资料。', 5, 'published', '2024-01-10 21:00:00'),
(13, 6, 2, NULL, '可以参考一下GitHub上的开源实现，但是不要直接抄袭哦。', 3, 'published', '2024-01-11 09:45:00'),
(14, 6, 5, 12, '同意，我找到了一些不错的教程，可以私信分享给你。', 2, 'published', '2024-01-11 12:20:00');

-- 插入帖子点赞数据
INSERT INTO post_likes (post_id, user_id, created_at) VALUES
(1, 2, '2024-01-15 11:30:00'), (1, 4, '2024-01-15 12:00:00'), (1, 5, '2024-01-15 13:15:00'), (1, 6, '2024-01-15 14:45:00'), (1, 1, '2024-01-15 15:30:00'),
(2, 1, '2024-01-14 15:30:00'), (2, 3, '2024-01-14 16:00:00'), (2, 4, '2024-01-14 16:30:00'), (2, 5, '2024-01-14 17:00:00'), (2, 6, '2024-01-14 17:30:00'),
(3, 2, '2024-01-13 17:30:00'), (3, 4, '2024-01-13 18:00:00'), (3, 6, '2024-01-13 19:15:00'),
(4, 2, '2024-01-12 12:00:00'), (4, 3, '2024-01-12 13:30:00'), (4, 5, '2024-01-12 15:00:00'),
(5, 3, '2024-01-11 10:00:00'), (5, 4, '2024-01-11 11:30:00'), (5, 6, '2024-01-11 14:00:00'),
(6, 2, '2024-01-10 20:30:00'), (6, 4, '2024-01-10 21:30:00'), (6, 5, '2024-01-11 08:00:00');

-- 插入评论点赞数据
INSERT INTO comment_likes (comment_id, user_id, created_at) VALUES
(2, 1, '2024-01-15 14:45:00'), (2, 3, '2024-01-15 15:00:00'), (2, 4, '2024-01-15 15:30:00'),
(4, 2, '2024-01-14 15:15:00'), (4, 5, '2024-01-14 16:00:00'),
(6, 3, '2024-01-13 18:15:00'), (6, 4, '2024-01-13 19:00:00'), (6, 5, '2024-01-13 20:30:00'),
(8, 3, '2024-01-12 15:00:00'), (8, 4, '2024-01-12 16:30:00'), (8, 5, '2024-01-12 17:45:00'),
(12, 2, '2024-01-10 21:15:00'), (12, 3, '2024-01-10 22:00:00'), (12, 5, '2024-01-11 08:30:00'); 