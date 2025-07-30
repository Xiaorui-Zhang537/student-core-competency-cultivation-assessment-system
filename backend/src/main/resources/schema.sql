-- 学生非核心能力发展评估系统数据库建表脚本
-- 数据库: student_assessment_system
-- 创建时间: 2024-12-28
-- 作者: System

-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS student_assessment_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE student_assessment_system;

-- 1. 用户表 (users)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    role ENUM('student', 'teacher', 'admin') NOT NULL DEFAULT 'student' COMMENT '用户角色',
    avatar VARCHAR(500) COMMENT '头像URL',
    first_name VARCHAR(50) COMMENT '姓氏',
    last_name VARCHAR(50) COMMENT '名字',
    display_name VARCHAR(100) COMMENT '显示名称',
    bio TEXT COMMENT '个人简介',
    grade VARCHAR(20) COMMENT '年级（学生）',
    subject VARCHAR(100) COMMENT '专业/科目',
    school VARCHAR(200) COMMENT '学校/学院',
    phone VARCHAR(20) COMMENT '联系电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 课程表 (courses)
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '课程标题',
    description TEXT COMMENT '课程描述',
    cover_image VARCHAR(500) COMMENT '课程封面图',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    category VARCHAR(100) COMMENT '课程分类',
    difficulty ENUM('beginner', 'intermediate', 'advanced') DEFAULT 'beginner' COMMENT '课程难度',
    duration INTEGER COMMENT '课程时长（分钟）',
    max_students INTEGER DEFAULT 50 COMMENT '最大学生数',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '课程价格',
    status ENUM('draft', 'published', 'archived') DEFAULT 'draft' COMMENT '课程状态',
    start_date DATE COMMENT '开课日期',
    end_date DATE COMMENT '结课日期',
    tags VARCHAR(500) COMMENT '课程标签（JSON格式）',
    objectives TEXT COMMENT '学习目标',
    requirements TEXT COMMENT '先修要求',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_start_date (start_date),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 3. 选课关系表 (enrollments)
CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '选课ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '学习进度（百分比）',
    status ENUM('active', 'completed', 'dropped') DEFAULT 'active' COMMENT '选课状态',
    grade DECIMAL(5,2) COMMENT '课程成绩',
    completion_date TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_course (student_id, course_id),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课关系表';

-- 4. 课程章节表 (lessons)
CREATE TABLE IF NOT EXISTS lessons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    description TEXT COMMENT '章节描述',
    content TEXT COMMENT '章节内容',
    video_url VARCHAR(500) COMMENT '视频URL',
    duration INTEGER COMMENT '章节时长（分钟）',
    order_index INTEGER NOT NULL COMMENT '排序索引',
    is_free BOOLEAN DEFAULT FALSE COMMENT '是否免费',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course_id (course_id),
    INDEX idx_order_index (order_index),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章节表';

-- 5. 作业表 (assignments)
CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '作业ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '发布教师ID',
    title VARCHAR(200) NOT NULL COMMENT '作业标题',
    description TEXT COMMENT '作业描述',
    requirements TEXT COMMENT '作业要求',
    max_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '总分',
    due_date TIMESTAMP NOT NULL COMMENT '截止时间',
    allow_late BOOLEAN DEFAULT FALSE COMMENT '是否允许迟交',
    max_attempts INTEGER DEFAULT 1 COMMENT '最大提交次数',
    file_types VARCHAR(200) COMMENT '允许的文件类型',
    max_file_size INTEGER DEFAULT 10 COMMENT '最大文件大小（MB）',
    status ENUM('draft', 'published', 'closed') DEFAULT 'draft' COMMENT '作业状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_course_id (course_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- 6. 作业提交表 (submissions)
CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
    assignment_id BIGINT NOT NULL COMMENT '作业ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    content TEXT COMMENT '作业内容',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_name VARCHAR(200) COMMENT '文件名',
    file_size INTEGER COMMENT '文件大小（字节）',
    submission_count INTEGER DEFAULT 1 COMMENT '提交次数',
    status ENUM('draft', 'submitted', 'graded') DEFAULT 'draft' COMMENT '提交状态',
    submitted_at TIMESTAMP NULL COMMENT '提交时间',
    is_late BOOLEAN DEFAULT FALSE COMMENT '是否迟交',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_assignment_student (assignment_id, student_id),
    INDEX idx_assignment_id (assignment_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status),
    INDEX idx_submitted_at (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

-- 7. 成绩表 (grades)
CREATE TABLE IF NOT EXISTS grades (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩ID',
    submission_id BIGINT NOT NULL COMMENT '提交ID',
    teacher_id BIGINT NOT NULL COMMENT '评分教师ID',
    score DECIMAL(5,2) NOT NULL COMMENT '得分',
    max_score DECIMAL(5,2) NOT NULL COMMENT '满分',
    feedback TEXT COMMENT '评语',
    grading_criteria TEXT COMMENT '评分标准',
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_submission_id (submission_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_score (score),
    INDEX idx_graded_at (graded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- 8. 文件记录表 (file_records)
CREATE TABLE IF NOT EXISTS file_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    saved_name VARCHAR(255) NOT NULL COMMENT '保存文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_type VARCHAR(100) COMMENT '文件类型',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    related_type VARCHAR(50) COMMENT '关联类型（assignment, submission, profile等）',
    related_id BIGINT COMMENT '关联ID',
    download_count INTEGER DEFAULT 0 COMMENT '下载次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_uploader_id (uploader_id),
    INDEX idx_related (related_type, related_id),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- 9. 通知表 (notifications)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type ENUM('system', 'assignment', 'grade', 'course', 'message') DEFAULT 'system' COMMENT '通知类型',
    priority ENUM('low', 'normal', 'high', 'urgent') DEFAULT 'normal' COMMENT '优先级',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    related_type VARCHAR(50) COMMENT '关联类型',
    related_id BIGINT COMMENT '关联ID',
    expired_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 10. 学习进度表 (lesson_progress)
CREATE TABLE IF NOT EXISTS lesson_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    lesson_id BIGINT NOT NULL COMMENT '章节ID',
    progress DECIMAL(5,2) DEFAULT 0.00 COMMENT '学习进度（百分比）',
    completed BOOLEAN DEFAULT FALSE COMMENT '是否完成',
    watch_time INTEGER DEFAULT 0 COMMENT '观看时长（秒）',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_lesson (student_id, lesson_id),
    INDEX idx_student_id (student_id),
    INDEX idx_lesson_id (lesson_id),
    INDEX idx_completed (completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表'; 

-- 社区功能相关表

-- 帖子表
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    category VARCHAR(50) NOT NULL DEFAULT 'study' COMMENT '帖子分类：study,homework,share,qa,chat',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名发布',
    allow_comments BOOLEAN DEFAULT TRUE COMMENT '是否允许评论',
    views INT DEFAULT 0 COMMENT '浏览次数',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    comments_count INT DEFAULT 0 COMMENT '评论数',
    status VARCHAR(20) DEFAULT 'published' COMMENT '状态：draft,published,deleted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_author_id (author_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status),
    INDEX idx_pinned (pinned),
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
    description VARCHAR(200) COMMENT '标签描述',
    color VARCHAR(7) DEFAULT '#3B82F6' COMMENT '标签颜色',
    posts_count INT DEFAULT 0 COMMENT '使用此标签的帖子数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_name (name),
    INDEX idx_posts_count (posts_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 帖子标签关联表
CREATE TABLE IF NOT EXISTS post_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_post_tag (post_id, tag_id),
    INDEX idx_post_id (post_id),
    INDEX idx_tag_id (tag_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- 帖子评论表
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    author_id BIGINT NOT NULL COMMENT '评论者ID',
    parent_id BIGINT NULL COMMENT '父评论ID（用于回复）',
    content TEXT NOT NULL COMMENT '评论内容',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    status VARCHAR(20) DEFAULT 'published' COMMENT '状态：published,deleted',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_post_id (post_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES post_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论表';

-- 帖子点赞表
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- 评论点赞表
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_comment_user (comment_id, user_id),
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (comment_id) REFERENCES post_comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表'; 

-- =====================================================
-- 能力评估模块表结构
-- =====================================================

-- 1. 能力维度表 (ability_dimensions)
CREATE TABLE IF NOT EXISTS ability_dimensions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维度ID',
    name VARCHAR(100) NOT NULL COMMENT '维度名称',
    description TEXT COMMENT '维度描述',
    category VARCHAR(50) NOT NULL COMMENT '分类：technical,soft,academic,creative',
    weight DECIMAL(3,2) DEFAULT 1.00 COMMENT '权重系数',
    max_score INT DEFAULT 100 COMMENT '最高分数',
    icon VARCHAR(100) COMMENT '图标标识',
    color VARCHAR(7) DEFAULT '#3B82F6' COMMENT '显示颜色',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_category (category),
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力维度表';

-- 2. 学生能力记录表 (student_abilities)
CREATE TABLE IF NOT EXISTS student_abilities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    dimension_id BIGINT NOT NULL COMMENT '能力维度ID',
    current_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '当前得分',
    level ENUM('beginner', 'intermediate', 'advanced', 'expert') DEFAULT 'beginner' COMMENT '能力等级',
    last_assessment_at TIMESTAMP NULL COMMENT '最后评估时间',
    assessment_count INT DEFAULT 0 COMMENT '评估次数',
    trend ENUM('rising', 'stable', 'declining') DEFAULT 'stable' COMMENT '发展趋势',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_student_dimension (student_id, dimension_id),
    INDEX idx_student_id (student_id),
    INDEX idx_dimension_id (dimension_id),
    INDEX idx_level (level),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (dimension_id) REFERENCES ability_dimensions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生能力记录表';

-- 3. 能力评估记录表 (ability_assessments)
CREATE TABLE IF NOT EXISTS ability_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评估ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    dimension_id BIGINT NOT NULL COMMENT '能力维度ID',
    assessor_id BIGINT NULL COMMENT '评估者ID（教师/系统）',
    assessment_type ENUM('assignment', 'project', 'exam', 'peer', 'self', 'system') DEFAULT 'assignment' COMMENT '评估类型',
    related_type VARCHAR(50) COMMENT '关联类型（assignment/course/etc）',
    related_id BIGINT COMMENT '关联对象ID',
    score DECIMAL(5,2) NOT NULL COMMENT '得分',
    max_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '满分',
    confidence DECIMAL(3,2) DEFAULT 1.00 COMMENT '置信度',
    evidence TEXT COMMENT '评估依据',
    feedback TEXT COMMENT '反馈意见',
    improvement_suggestions TEXT COMMENT '改进建议',
    status ENUM('draft', 'completed', 'reviewed') DEFAULT 'completed' COMMENT '评估状态',
    assessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评估时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_student_id (student_id),
    INDEX idx_dimension_id (dimension_id),
    INDEX idx_assessor_id (assessor_id),
    INDEX idx_assessment_type (assessment_type),
    INDEX idx_related (related_type, related_id),
    INDEX idx_assessed_at (assessed_at),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (dimension_id) REFERENCES ability_dimensions(id) ON DELETE CASCADE,
    FOREIGN KEY (assessor_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力评估记录表';

-- 4. 能力发展目标表 (ability_goals)
CREATE TABLE IF NOT EXISTS ability_goals (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '目标ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    dimension_id BIGINT NOT NULL COMMENT '能力维度ID',
    title VARCHAR(200) NOT NULL COMMENT '目标标题',
    description TEXT COMMENT '目标描述',
    target_score DECIMAL(5,2) NOT NULL COMMENT '目标分数',
    current_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '当前分数',
    target_date DATE NOT NULL COMMENT '目标达成日期',
    priority ENUM('low', 'medium', 'high') DEFAULT 'medium' COMMENT '优先级',
    status ENUM('active', 'achieved', 'paused', 'cancelled') DEFAULT 'active' COMMENT '目标状态',
    achieved_at TIMESTAMP NULL COMMENT '达成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_student_id (student_id),
    INDEX idx_dimension_id (dimension_id),
    INDEX idx_status (status),
    INDEX idx_target_date (target_date),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (dimension_id) REFERENCES ability_dimensions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力发展目标表';

-- 5. 学习建议表 (learning_recommendations)
CREATE TABLE IF NOT EXISTS learning_recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '建议ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    dimension_id BIGINT NOT NULL COMMENT '能力维度ID',
    title VARCHAR(200) NOT NULL COMMENT '建议标题',
    description TEXT NOT NULL COMMENT '建议内容',
    recommendation_type ENUM('course', 'resource', 'practice', 'project') DEFAULT 'course' COMMENT '建议类型',
    resource_url VARCHAR(500) COMMENT '资源链接',
    difficulty_level ENUM('beginner', 'intermediate', 'advanced') DEFAULT 'intermediate' COMMENT '难度等级',
    estimated_time VARCHAR(50) COMMENT '预估学习时间',
    priority_score DECIMAL(3,2) DEFAULT 1.00 COMMENT '优先级分数',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    is_accepted BOOLEAN DEFAULT FALSE COMMENT '是否已采纳',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    
    INDEX idx_student_id (student_id),
    INDEX idx_dimension_id (dimension_id),
    INDEX idx_type (recommendation_type),
    INDEX idx_priority (priority_score),
    INDEX idx_is_read (is_read),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (dimension_id) REFERENCES ability_dimensions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习建议表';

-- 6. 能力报告表 (ability_reports)
CREATE TABLE IF NOT EXISTS ability_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    report_type ENUM('monthly', 'semester', 'annual', 'custom') DEFAULT 'monthly' COMMENT '报告类型',
    title VARCHAR(200) NOT NULL COMMENT '报告标题',
    overall_score DECIMAL(5,2) NOT NULL COMMENT '综合得分',
    report_period_start DATE NOT NULL COMMENT '报告周期开始',
    report_period_end DATE NOT NULL COMMENT '报告周期结束',
    dimension_scores JSON COMMENT '各维度得分（JSON格式）',
    trends_analysis TEXT COMMENT '趋势分析',
    achievements JSON COMMENT '成就列表（JSON格式）',
    improvement_areas JSON COMMENT '待改进领域（JSON格式）',
    recommendations TEXT COMMENT '综合建议',
    generated_by ENUM('system', 'teacher') DEFAULT 'system' COMMENT '生成方式',
    is_published BOOLEAN DEFAULT FALSE COMMENT '是否发布',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    
    INDEX idx_student_id (student_id),
    INDEX idx_report_type (report_type),
    INDEX idx_period (report_period_start, report_period_end),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力报告表'; 