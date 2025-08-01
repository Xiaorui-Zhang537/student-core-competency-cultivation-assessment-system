-- MySQL dump 10.13  Distrib 9.0.1, for macos15.1 (arm64)
--
-- Host: 127.0.0.1    Database: student_assessment_system
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ================== abilities：能力库表 ==================
-- 存储所有能力（如Java基础、沟通表达等）的基本信息，可层级管理
CREATE TABLE `abilities` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '能力ID',
                             `name` varchar(255) NOT NULL COMMENT '能力名称',
                             `description` text COMMENT '能力描述',
                             `category` varchar(64) DEFAULT NULL COMMENT '能力分类',
                             `level` varchar(32) DEFAULT NULL COMMENT '能力等级',
                             `weight` decimal(5,2) DEFAULT '1.00' COMMENT '权重',
                             `parent_id` bigint DEFAULT NULL COMMENT '父能力ID',
                             `sort_order` int DEFAULT '0' COMMENT '排序顺序',
                             `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
                             `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='能力表';

-- ================== ability_assessments：能力评估记录表 ==================
-- 存储针对学生各能力维度的评估详情（如老师、系统、同伴、自评等）
CREATE TABLE `ability_assessments` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评估ID',
                                       `student_id` bigint NOT NULL COMMENT '学生ID',
                                       `dimension_id` bigint NOT NULL COMMENT '能力维度ID',
                                       `ability_id` bigint DEFAULT NULL COMMENT '能力ID',
                                       `assessor_id` bigint DEFAULT NULL COMMENT '评估者ID（教师/系统）',
                                       `assessment_type` enum('assignment','project','exam','peer','self','system') COLLATE utf8mb4_unicode_ci DEFAULT 'assignment' COMMENT '评估类型',
                                       `related_id` bigint DEFAULT NULL COMMENT '关联对象ID',
                                       `score` decimal(5,2) NOT NULL COMMENT '得分',
                                       `max_score` decimal(10,2) DEFAULT NULL COMMENT '满分',
                                       `ability_level` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '能力等级',
                                       `confidence` decimal(5,2) DEFAULT NULL COMMENT '置信度',
                                       `evidence` text COLLATE utf8mb4_unicode_ci COMMENT '评估依据',
                                       `improvement` text COLLATE utf8mb4_unicode_ci COMMENT '改进建议',
                                       `status` enum('draft','completed','reviewed') COLLATE utf8mb4_unicode_ci DEFAULT 'completed' COMMENT '评估状态',
                                       `assessed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评估时间',
                                       `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_student_id` (`student_id`),
                                       KEY `idx_dimension_id` (`dimension_id`),
                                       KEY `idx_assessor_id` (`assessor_id`),
                                       KEY `idx_assessment_type` (`assessment_type`),
                                       KEY `idx_related` (`related_id`),
                                       KEY `idx_assessed_at` (`assessed_at`),
                                       CONSTRAINT `ability_assessments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                       CONSTRAINT `ability_assessments_ibfk_2` FOREIGN KEY (`dimension_id`) REFERENCES `ability_dimensions` (`id`) ON DELETE CASCADE,
                                       CONSTRAINT `ability_assessments_ibfk_3` FOREIGN KEY (`assessor_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力评估记录表';

-- ================== ability_dimensions：能力维度表 ==================
-- 定义所有评估的能力维度（如编程能力、沟通协作、创新思维等）
CREATE TABLE `ability_dimensions` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '维度ID',
                                      `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '维度名称',
                                      `description` text COLLATE utf8mb4_unicode_ci COMMENT '维度描述',
                                      `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类：technical,soft,academic,creative',
                                      `weight` decimal(3,2) DEFAULT '1.00' COMMENT '权重系数',
                                      `max_score` int DEFAULT '100' COMMENT '最高分数',
                                      `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标标识',
                                      `color` varchar(7) COLLATE utf8mb4_unicode_ci DEFAULT '#3B82F6' COMMENT '显示颜色',
                                      `sort_order` int DEFAULT '0' COMMENT '排序顺序',
                                      `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_category` (`category`),
                                      KEY `idx_is_active` (`is_active`),
                                      KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力维度表';

-- ================== ability_goals：能力目标表 ==================
-- 存储每个学生在某个能力维度下设定的成长目标（如“编程能力提升至90分”）
CREATE TABLE `ability_goals` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '目标ID',
                                 `student_id` bigint NOT NULL COMMENT '学生ID',
                                 `dimension_id` bigint NOT NULL COMMENT '能力维度ID',
                                 `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标标题',
                                 `description` text COLLATE utf8mb4_unicode_ci COMMENT '目标描述',
                                 `target_score` decimal(5,2) NOT NULL COMMENT '目标分数',
                                 `current_score` decimal(5,2) DEFAULT '0.00' COMMENT '当前分数',
                                 `target_date` date NOT NULL COMMENT '目标达成日期',
                                 `priority` enum('low','medium','high') COLLATE utf8mb4_unicode_ci DEFAULT 'medium' COMMENT '优先级',
                                 `status` enum('active','achieved','paused','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '目标状态',
                                 `achieved_at` timestamp NULL DEFAULT NULL COMMENT '达成时间',
                                 `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_student_id` (`student_id`),
                                 KEY `idx_dimension_id` (`dimension_id`),
                                 KEY `idx_status` (`status`),
                                 KEY `idx_target_date` (`target_date`),
                                 CONSTRAINT `ability_goals_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `ability_goals_ibfk_2` FOREIGN KEY (`dimension_id`) REFERENCES `ability_dimensions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力发展目标表';

-- ================== ability_reports：能力报告表 ==================
-- 存储学生的各周期能力分析报告（含总分、各维度分、趋势、建议等）
CREATE TABLE `ability_reports` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID',
                                   `student_id` bigint NOT NULL COMMENT '学生ID',
                                   `report_type` enum('monthly','semester','annual','custom') COLLATE utf8mb4_unicode_ci DEFAULT 'monthly' COMMENT '报告类型',
                                   `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报告标题',
                                   `overall_score` decimal(5,2) NOT NULL COMMENT '综合得分',
                                   `report_period_start` date NOT NULL COMMENT '报告周期开始',
                                   `report_period_end` date NOT NULL COMMENT '报告周期结束',
                                   `dimension_scores` json DEFAULT NULL COMMENT '各维度得分（JSON格式）',
                                   `trends_analysis` text COLLATE utf8mb4_unicode_ci COMMENT '趋势分析',
                                   `achievements` json DEFAULT NULL COMMENT '成就列表（JSON格式）',
                                   `improvement_areas` json DEFAULT NULL COMMENT '待改进领域（JSON格式）',
                                   `recommendations` text COLLATE utf8mb4_unicode_ci COMMENT '综合建议',
                                   `generated_by` enum('system','teacher') COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT '生成方式',
                                   `is_published` tinyint(1) DEFAULT '0' COMMENT '是否发布',
                                   `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `published_at` timestamp NULL DEFAULT NULL COMMENT '发布时间',
                                   `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_student_id` (`student_id`),
                                   KEY `idx_report_type` (`report_type`),
                                   KEY `idx_period` (`report_period_start`,`report_period_end`),
                                   KEY `idx_created_at` (`created_at`),
                                   CONSTRAINT `ability_reports_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力报告表';

-- ================== analytics_cache：分析缓存表 ==================
-- 用于缓存分析结果（如统计报表、数据分析等），提升查询速度
CREATE TABLE `analytics_cache` (
                                   `cache_key` varchar(255) NOT NULL COMMENT '缓存主键',
                                   `cache_value` text COMMENT '缓存内容',
                                   `generated_at` datetime DEFAULT NULL COMMENT '生成时间',
                                   PRIMARY KEY (`cache_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分析缓存表';

-- ================== assignments：作业表 ==================
-- 存储课程布置的所有作业，包括题目、描述、截止时间等
CREATE TABLE `assignments` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作业ID',
                               `course_id` bigint NOT NULL COMMENT '课程ID',
                               `teacher_id` bigint NOT NULL COMMENT '发布教师ID',
                               `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作业标题',
                               `description` text COLLATE utf8mb4_unicode_ci COMMENT '作业描述',
                               `requirements` text COLLATE utf8mb4_unicode_ci COMMENT '作业要求',
                               `max_score` decimal(5,2) DEFAULT '100.00' COMMENT '总分',
                               `due_date` timestamp NOT NULL COMMENT '截止时间',
                               `allow_late` tinyint(1) DEFAULT '0' COMMENT '是否允许迟交',
                               `max_attempts` int DEFAULT '1' COMMENT '最大提交次数',
                               `file_types` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '允许的文件类型',
                               `max_file_size` int DEFAULT '10' COMMENT '最大文件大小（MB）',
                               `status` enum('draft','published','closed') COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '作业状态',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                               `submission_count` int DEFAULT '0' COMMENT '提交数量',
                               PRIMARY KEY (`id`),
                               KEY `idx_course_id` (`course_id`),
                               KEY `idx_teacher_id` (`teacher_id`),
                               KEY `idx_due_date` (`due_date`),
                               KEY `idx_status` (`status`),
                               KEY `idx_deleted` (`deleted`),
                               CONSTRAINT `assignments_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `assignments_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- ================== comment_likes：评论点赞表 ==================
-- 存储用户对帖子评论的点赞记录
CREATE TABLE `comment_likes` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
                                 `comment_id` bigint NOT NULL COMMENT '评论ID',
                                 `user_id` bigint NOT NULL COMMENT '用户ID',
                                 `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
                                 KEY `idx_comment_id` (`comment_id`),
                                 KEY `idx_user_id` (`user_id`),
                                 CONSTRAINT `comment_likes_ibfk_1` FOREIGN KEY (`comment_id`) REFERENCES `post_comments` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `comment_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ================== courses：课程表 ==================
-- 存储所有课程的基本信息，包括教师、分类、难度、时间等
CREATE TABLE `courses` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
                           `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程标题',
                           `description` text COLLATE utf8mb4_unicode_ci COMMENT '课程描述',
                           `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程封面图',
                           `teacher_id` bigint NOT NULL COMMENT '授课教师ID',
                           `category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程分类',
                           `difficulty` enum('beginner','intermediate','advanced') COLLATE utf8mb4_unicode_ci DEFAULT 'beginner' COMMENT '课程难度',
                           `duration` int DEFAULT NULL COMMENT '课程时长（分钟）',
                           `max_students` int DEFAULT '50' COMMENT '最大学生数',
                           `price` decimal(10,2) DEFAULT '0.00' COMMENT '课程价格',
                           `status` enum('draft','published','archived') COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '课程状态',
                           `start_date` date DEFAULT NULL COMMENT '开课日期',
                           `end_date` date DEFAULT NULL COMMENT '结课日期',
                           `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程标签（JSON格式）',
                           `objectives` text COLLATE utf8mb4_unicode_ci COMMENT '学习目标',
                           `requirements` text COLLATE utf8mb4_unicode_ci COMMENT '先修要求',
                           `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                           `enrollment_count` int DEFAULT '0' COMMENT '选课人数',
                           `rating` decimal(2,1) DEFAULT '0.0' COMMENT '课程评分',
                           `review_count` int DEFAULT '0' COMMENT '评价数量',
                           PRIMARY KEY (`id`),
                           KEY `idx_teacher_id` (`teacher_id`),
                           KEY `idx_category` (`category`),
                           KEY `idx_status` (`status`),
                           KEY `idx_start_date` (`start_date`),
                           KEY `idx_deleted` (`deleted`),
                           CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ================== enrollments：选课表 ==================
-- 记录学生选修课程的关系、学习进度与成绩
CREATE TABLE `enrollments` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '选课ID',
                               `student_id` bigint NOT NULL COMMENT '学生ID',
                               `course_id` bigint NOT NULL COMMENT '课程ID',
                               `enrolled_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
                               `progress` decimal(5,2) DEFAULT '0.00' COMMENT '学习进度（百分比）',
                               `status` enum('active','completed','dropped') COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '选课状态',
                               `grade` decimal(5,2) DEFAULT NULL COMMENT '课程成绩',
                               `completion_date` timestamp NULL DEFAULT NULL COMMENT '完成时间',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `last_access_time` datetime DEFAULT NULL COMMENT '最后一次访问课程的时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_student_course` (`student_id`,`course_id`),
                               KEY `idx_student_id` (`student_id`),
                               KEY `idx_course_id` (`course_id`),
                               KEY `idx_status` (`status`),
                               CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选课关系表';

-- ================== file_records：文件记录表 ==================
-- 存储系统所有文件的元数据，包括作业上传、课件、个人资料等
CREATE TABLE `file_records` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
                                `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
                                `saved_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '保存文件名',
                                `file_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
                                `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
                                `file_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
                                `mime_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MIME类型',
                                `uploader_id` bigint NOT NULL COMMENT '上传者ID',
                                `related_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联类型（assignment, submission, profile等）',
                                `related_id` bigint DEFAULT NULL COMMENT '关联ID',
                                `download_count` int DEFAULT '0' COMMENT '下载次数',
                                `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_uploader_id` (`uploader_id`),
                                KEY `idx_related` (`related_type`,`related_id`),
                                KEY `idx_file_type` (`file_type`),
                                CONSTRAINT `file_records_ibfk_1` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- ================== grades：成绩表 ==================
-- 存储学生的作业成绩信息及评语
CREATE TABLE `grades` (
                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
                          `student_id` bigint NOT NULL COMMENT '学生ID',
                          `assignment_id` bigint NOT NULL COMMENT '作业ID',
                          `score` decimal(5,2) NOT NULL COMMENT '得分',
                          `max_score` decimal(5,2) NOT NULL COMMENT '满分',
                          `percentage` decimal(5,2) DEFAULT NULL COMMENT '百分比分数',
                          `grade_level` varchar(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '等级(A,B,C,D,F)',
                          `feedback` text COLLATE utf8mb4_unicode_ci COMMENT '评语',
                          `status` enum('draft','published') COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '状态',
                          `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                          `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          `submission_id` bigint DEFAULT NULL COMMENT '提交ID',
                          `grader_id` bigint DEFAULT NULL COMMENT '评分人ID',
                          PRIMARY KEY (`id`),
                          KEY `idx_student_id` (`student_id`),
                          KEY `idx_assignment_id` (`assignment_id`),
                          KEY `idx_score` (`score`),
                          KEY `idx_status` (`status`),
                          CONSTRAINT `grades_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                          CONSTRAINT `grades_ibfk_2` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- ================== learning_recommendations：学习建议表 ==================
-- 存储针对学生能力提升推送的个性化学习建议
CREATE TABLE `learning_recommendations` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '建议ID',
                                            `student_id` bigint NOT NULL COMMENT '学生ID',
                                            `dimension_id` bigint NOT NULL COMMENT '能力维度ID',
                                            `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建议标题',
                                            `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建议内容',
                                            `recommendation_type` enum('course','resource','practice','project') COLLATE utf8mb4_unicode_ci DEFAULT 'course' COMMENT '建议类型',
                                            `resource_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源链接',
                                            `difficulty_level` enum('beginner','intermediate','advanced') COLLATE utf8mb4_unicode_ci DEFAULT 'intermediate' COMMENT '难度等级',
                                            `estimated_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预估学习时间',
                                            `priority_score` decimal(3,2) DEFAULT '1.00' COMMENT '优先级分数',
                                            `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
                                            `is_accepted` tinyint(1) DEFAULT '0' COMMENT '是否已采纳',
                                            `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `expires_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
                                            PRIMARY KEY (`id`),
                                            KEY `idx_student_id` (`student_id`),
                                            KEY `idx_dimension_id` (`dimension_id`),
                                            KEY `idx_type` (`recommendation_type`),
                                            KEY `idx_priority` (`priority_score`),
                                            KEY `idx_is_read` (`is_read`),
                                            CONSTRAINT `learning_recommendations_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                            CONSTRAINT `learning_recommendations_ibfk_2` FOREIGN KEY (`dimension_id`) REFERENCES `ability_dimensions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习建议表';

-- ================== lesson_progress：课程章节学习进度表 ==================
-- 记录学生在课程每一章节的学习进度、观看时长等
CREATE TABLE `lesson_progress` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '进度ID',
                                   `student_id` bigint NOT NULL COMMENT '学生ID',
                                   `lesson_id` bigint NOT NULL COMMENT '章节ID',
                                   `progress` decimal(5,2) DEFAULT '0.00' COMMENT '学习进度（百分比）',
                                   `completed` tinyint(1) DEFAULT '0' COMMENT '是否完成',
                                   `watch_time` int DEFAULT '0' COMMENT '观看时长（秒）',
                                   `completed_at` timestamp NULL DEFAULT NULL COMMENT '完成时间',
                                   `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `last_studied_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次学习时间',
                                   `course_id` bigint DEFAULT NULL COMMENT '课程ID',
                                   `view_count` int DEFAULT '0' COMMENT '观看次数',
                                   `last_position` int DEFAULT '0' COMMENT '最后观看位置(秒)',
                                   `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'not_started' COMMENT '学习状态',
                                   `notes` text COLLATE utf8mb4_unicode_ci COMMENT '学习笔记',
                                   `rating` int DEFAULT '0' COMMENT '学习评分',
                                   `started_at` datetime DEFAULT NULL COMMENT '开始学习时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_student_lesson` (`student_id`,`lesson_id`),
                                   KEY `idx_student_id` (`student_id`),
                                   KEY `idx_lesson_id` (`lesson_id`),
                                   KEY `idx_completed` (`completed`),
                                   CONSTRAINT `lesson_progress_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `lesson_progress_ibfk_2` FOREIGN KEY (`lesson_id`) REFERENCES `lessons` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习进度表';

-- ================== lessons：课程章节表 ==================
-- 记录课程下所有的章节信息，包括章节内容和视频等
CREATE TABLE `lessons` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '章节ID',
                           `course_id` bigint NOT NULL COMMENT '课程ID',
                           `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '章节标题',
                           `description` text COLLATE utf8mb4_unicode_ci COMMENT '章节描述',
                           `content` text COLLATE utf8mb4_unicode_ci COMMENT '章节内容',
                           `video_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频URL',
                           `duration` int DEFAULT NULL COMMENT '章节时长（分钟）',
                           `order_index` int NOT NULL COMMENT '排序索引',
                           `is_free` tinyint(1) DEFAULT '0' COMMENT '是否免费',
                           `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                           PRIMARY KEY (`id`),
                           KEY `idx_course_id` (`course_id`),
                           KEY `idx_order_index` (`order_index`),
                           KEY `idx_deleted` (`deleted`),
                           CONSTRAINT `lessons_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程章节表';

-- ================== notifications：通知表 ==================
-- 系统、课程、作业等相关通知，推送给用户
CREATE TABLE `notifications` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                                 `user_id` bigint NOT NULL COMMENT '用户ID',
                                 `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
                                 `content` text COLLATE utf8mb4_unicode_ci COMMENT '通知内容',
                                 `type` enum('system','assignment','grade','course','message') COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT '通知类型',
                                 `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知分类，如学术/系统/活动',
                                 `priority` enum('low','normal','high','urgent') COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT '优先级',
                                 `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
                                 `read_at` datetime DEFAULT NULL COMMENT '已读时间',
                                 `related_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联类型',
                                 `related_id` bigint DEFAULT NULL COMMENT '关联ID',
                                 `expired_at` timestamp NULL DEFAULT NULL COMMENT '过期时间',
                                 `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
                                 `deleted` tinyint(1) DEFAULT '0' COMMENT '软删除标志',
                                 `data` text COLLATE utf8mb4_unicode_ci COMMENT '附加数据(JSON)',
                                 `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '通知图标',
                                 `action_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作跳转URL',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_user_id` (`user_id`),
                                 KEY `idx_is_read` (`is_read`),
                                 KEY `idx_type` (`type`),
                                 KEY `idx_created_at` (`created_at`),
                                 CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ================== post_comments：帖子评论表 ==================
-- 记录所有帖子下的评论、回复、点赞数等
CREATE TABLE `post_comments` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
                                 `post_id` bigint NOT NULL COMMENT '帖子ID',
                                 `author_id` bigint NOT NULL COMMENT '评论者ID',
                                 `parent_id` bigint DEFAULT NULL COMMENT '父评论ID（用于回复）',
                                 `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
                                 `likes_count` int DEFAULT '0' COMMENT '点赞数',
                                 `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'published' COMMENT '状态：published,deleted',
                                 `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_post_id` (`post_id`),
                                 KEY `idx_author_id` (`author_id`),
                                 KEY `idx_parent_id` (`parent_id`),
                                 KEY `idx_created_at` (`created_at`),
                                 CONSTRAINT `post_comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `post_comments_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                 CONSTRAINT `post_comments_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `post_comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论表';

-- ================== post_likes：帖子点赞表 ==================
-- 记录用户对每条帖子的点赞行为
CREATE TABLE `post_likes` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
                              `post_id` bigint NOT NULL COMMENT '帖子ID',
                              `user_id` bigint NOT NULL COMMENT '用户ID',
                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_post_user` (`post_id`,`user_id`),
                              KEY `idx_post_id` (`post_id`),
                              KEY `idx_user_id` (`user_id`),
                              CONSTRAINT `post_likes_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                              CONSTRAINT `post_likes_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- ================== post_tags：帖子标签关联表 ==================
-- 记录帖子与标签的多对多关系
CREATE TABLE `post_tags` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                             `post_id` bigint NOT NULL COMMENT '帖子ID',
                             `tag_id` bigint NOT NULL COMMENT '标签ID',
                             `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_post_tag` (`post_id`,`tag_id`),
                             KEY `idx_post_id` (`post_id`),
                             KEY `idx_tag_id` (`tag_id`),
                             CONSTRAINT `post_tags_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `post_tags_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- ================== posts：帖子表 ==================
-- 论坛社区的主贴表，包含标题、内容、分类、作者等
CREATE TABLE `posts` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
                         `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
                         `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子内容',
                         `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'study' COMMENT '帖子分类：study,homework,share,qa,chat',
                         `author_id` bigint NOT NULL COMMENT '作者ID',
                         `pinned` tinyint(1) DEFAULT '0' COMMENT '是否置顶',
                         `anonymous` tinyint(1) DEFAULT '0' COMMENT '是否匿名发布',
                         `allow_comments` tinyint(1) DEFAULT '1' COMMENT '是否允许评论',
                         `views` int DEFAULT '0' COMMENT '浏览次数',
                         `likes_count` int DEFAULT '0' COMMENT '点赞数',
                         `comments_count` int DEFAULT '0' COMMENT '评论数',
                         `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'published' COMMENT '状态：draft,published,deleted',
                         `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                         PRIMARY KEY (`id`),
                         KEY `idx_author_id` (`author_id`),
                         KEY `idx_category` (`category`),
                         KEY `idx_created_at` (`created_at`),
                         KEY `idx_status` (`status`),
                         KEY `idx_pinned` (`pinned`),
                         CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ================== student_abilities：学生能力记录表 ==================
-- 每个学生在各能力维度的分数与成长状态
CREATE TABLE `student_abilities` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                     `student_id` bigint NOT NULL COMMENT '学生ID',
                                     `dimension_id` bigint NOT NULL COMMENT '能力维度ID',
                                     `current_score` decimal(5,2) DEFAULT '0.00' COMMENT '当前得分',
                                     `level` enum('beginner','intermediate','advanced','expert') COLLATE utf8mb4_unicode_ci DEFAULT 'beginner' COMMENT '能力等级',
                                     `last_assessment_at` timestamp NULL DEFAULT NULL COMMENT '最后评估时间',
                                     `assessment_count` int DEFAULT '0' COMMENT '评估次数',
                                     `trend` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分数趋势(up/down/steady)',
                                     `achievements` json DEFAULT NULL COMMENT '能力成就（JSON）',
                                     `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_student_dimension` (`student_id`, `dimension_id`),
                                     KEY `idx_student_id` (`student_id`),
                                     KEY `idx_dimension_id` (`dimension_id`),
                                     CONSTRAINT `student_abilities_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `student_abilities_ibfk_2` FOREIGN KEY (`dimension_id`) REFERENCES `ability_dimensions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生能力记录表';

-- ================== submissions：作业提交表 ==================
-- 学生作业提交记录，包括文件、提交时间、批改状态等
CREATE TABLE `submissions` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
                               `assignment_id` bigint NOT NULL COMMENT '作业ID',
                               `student_id` bigint NOT NULL COMMENT '学生ID',
                               `submit_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                               `file_id` bigint DEFAULT NULL COMMENT '关联文件ID',
                               `content` text COLLATE utf8mb4_unicode_ci COMMENT '文本答案（如有）',
                               `status` enum('submitted','graded','late','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'submitted' COMMENT '提交状态',
                               `grade` decimal(5,2) DEFAULT NULL COMMENT '分数',
                               `feedback` text COLLATE utf8mb4_unicode_ci COMMENT '教师评语',
                               `grader_id` bigint DEFAULT NULL COMMENT '批改教师ID',
                               `graded_at` timestamp NULL DEFAULT NULL COMMENT '批改时间',
                               `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_assignment_id` (`assignment_id`),
                               KEY `idx_student_id` (`student_id`),
                               KEY `idx_status` (`status`),
                               KEY `idx_file_id` (`file_id`),
                               CONSTRAINT `submissions_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `submissions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `submissions_ibfk_3` FOREIGN KEY (`file_id`) REFERENCES `file_records` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

-- ================== tags：标签表 ==================
-- 论坛与问答社区的所有标签定义
CREATE TABLE `tags` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
                        `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
                        `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
                        `color` varchar(7) COLLATE utf8mb4_unicode_ci DEFAULT '#3B82F6' COMMENT '标签颜色',
                        `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签图标',
                        `usage_count` int DEFAULT '0' COMMENT '使用次数',
                        `is_active` tinyint(1) DEFAULT '1' COMMENT '是否激活',
                        `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_tag_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ================== users：用户表 ==================
-- 存储所有用户信息，包括学生、教师和管理员
CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                         `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
                         `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
                         `role` enum('student','teacher','admin') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'student' COMMENT '用户角色',
                         `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
                         `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                         `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
                         `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码Hash',
                         `status` enum('active','inactive','banned') COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '账号状态',
                         `gender` enum('male','female','other') COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别',
                         `school` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学校名称',
                         `student_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学号',
                         `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级/班级',
                         `bio` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简介',
                         `register_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                         `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
                         `is_verified` tinyint(1) DEFAULT '0' COMMENT '邮箱是否已验证',
                         `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                         `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_username` (`username`),
                         UNIQUE KEY `uk_email` (`email`),
                         KEY `idx_role` (`role`),
                         KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';