
-- AI 批改历史表
CREATE TABLE ai_grading_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  file_id BIGINT NULL,
  file_name VARCHAR(255) NULL,
  model VARCHAR(128) NULL,
  final_score DOUBLE NULL,
  raw_json LONGTEXT NOT NULL,
  created_at DATETIME NOT NULL
);
CREATE INDEX idx_ai_grading_history_teacher ON ai_grading_history(teacher_id, created_at);
-- -----------------------------------------------------
-- 节次-资料 关联表（方案A）：资料不再移动，仅建立引用关系
-- -----------------------------------------------------
CREATE TABLE lesson_materials (
  lesson_id BIGINT NOT NULL,
  file_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (lesson_id, file_id),
  KEY idx_lm_file (file_id),
  CONSTRAINT fk_lm_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id),
  CONSTRAINT fk_lm_file FOREIGN KEY (file_id) REFERENCES file_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节次-资料 关联表';

-- 迁移历史数据：把已标记为 lesson_material 的文件记录建立关联（不改变原记录）
INSERT INTO lesson_materials (lesson_id, file_id, created_at)
SELECT fr.related_id, fr.id, NOW()
FROM file_records fr
WHERE fr.related_type = 'lesson_material';

-- RESOLVED: merge artifact start removed
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

-- ================== course_ability_weights：课程能力权重表 ==================
-- 每门课程针对五维（含学习成绩）配置的权重，默认等权 1.00
CREATE TABLE IF NOT EXISTS `course_ability_weights` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `dimension_code` varchar(50) NOT NULL COMMENT '维度编码：MORAL_COGNITION/LEARNING_ATTITUDE/LEARNING_ABILITY/LEARNING_METHOD/ACADEMIC_GRADE',
  `weight` decimal(5,2) NOT NULL DEFAULT '1.00' COMMENT '权重',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_dimension` (`course_id`,`dimension_code`),
  KEY `idx_course_id` (`course_id`),
  CONSTRAINT `fk_course_weights_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程能力权重表';

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
                               `status` enum('draft','scheduled','published','closed') COLLATE utf8mb4_unicode_ci DEFAULT 'draft' COMMENT '作业状态',
                               `publish_at` timestamp NULL DEFAULT NULL COMMENT '定时发布时间（为空表示不定时）',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
                               `submission_count` int DEFAULT '0' COMMENT '提交数量',
                               PRIMARY KEY (`id`),
                               KEY `idx_course_id` (`course_id`),
                               KEY `idx_teacher_id` (`teacher_id`),
                               KEY `idx_due_date` (`due_date`),
                               KEY `idx_status` (`status`),
                               KEY `idx_publish_at` (`publish_at`),
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
                           `content` text COLLATE utf8mb4_unicode_ci COMMENT '课程内容',
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
                           `require_enroll_key` tinyint(1) DEFAULT '0' COMMENT '是否需要入课密钥',
                           `enroll_key_hash` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '入课密钥哈希',
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
                           `allow_scrubbing` tinyint(1) DEFAULT '1' COMMENT '允许拖动进度条',
                           `allow_speed_change` tinyint(1) DEFAULT '1' COMMENT '允许倍速',
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
                                 `type` enum('system','assignment','grade','course','message','post') COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT '通知类型',
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

-- ================== reports：举报表 ==================
-- 存储教师/管理员对可疑提交的举报记录
CREATE TABLE IF NOT EXISTS `reports` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `reported_student_id` bigint NOT NULL COMMENT '被举报学生ID',
  `course_id` bigint DEFAULT NULL COMMENT '课程ID',
  `assignment_id` bigint DEFAULT NULL COMMENT '作业ID',
  `submission_id` bigint DEFAULT NULL COMMENT '提交ID',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报原因',
  `details` text COLLATE utf8mb4_unicode_ci COMMENT '详细说明',
  `evidence_file_id` bigint DEFAULT NULL COMMENT '证据文件ID',
  `status` enum('pending','in_review','resolved','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '处理状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_student_id` (`reported_student_id`),
  KEY `idx_submission_id` (`submission_id`),
  CONSTRAINT `reports_ibfk_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reports_ibfk_student` FOREIGN KEY (`reported_student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

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

-- 存储所有用户信息，包括学生、教师和管理员
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码Hash',
  `role` enum('student','teacher','admin') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'student' COMMENT '用户角色',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `student_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学号',
  `teacher_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工号',
  `first_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '名',
  `last_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `gender` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别',
  `bio` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简介',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `country` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `province` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份/州',
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `school` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学校名称',
  `subject` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业/科目',
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级/班级',
  `email_verified` tinyint(1) DEFAULT '0' COMMENT '邮箱是否已验证',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
-- RESOLVED: merge artifact separator removed
create table abilities
(
    id          bigint auto_increment comment '能力ID'
        primary key,
    name        varchar(255)                            not null comment '能力名称',
    description text                                    null comment '能力描述',
    category    varchar(64)                             null comment '能力分类',
    level       varchar(32)                             null comment '能力等级',
    weight      decimal(5, 2) default 1.00              null comment '权重',
    parent_id   bigint                                  null comment '父能力ID',
    sort_order  int           default 0                 null comment '排序顺序',
    is_active   tinyint(1)    default 1                 null comment '是否激活',
    created_at  datetime      default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  datetime      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint(1)    default 0                 null comment '是否删除'
)
    comment '能力表' charset = utf8mb4;

create table ability_dimensions
(
    id          bigint auto_increment comment '维度ID'
        primary key,
    name        varchar(100)                            not null comment '维度名称',
    description text                                    null comment '维度描述',
    category    varchar(50)                             not null comment '分类：technical,soft,academic,creative',
    weight      decimal(3, 2) default 1.00              null comment '权重系数',
    max_score   int           default 100               null comment '最高分数',
    icon        varchar(100)                            null comment '图标标识',
    color       varchar(7)    default '#3B82F6'         null comment '显示颜色',
    sort_order  int           default 0                 null comment '排序顺序',
    is_active   tinyint(1)    default 1                 null comment '是否激活',
    created_at  timestamp     default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '能力维度表';

create index idx_category
    on ability_dimensions (category);

create index idx_is_active
    on ability_dimensions (is_active);

create index idx_sort_order
    on ability_dimensions (sort_order);

create table analytics_cache
(
    cache_key    varchar(255) not null comment '缓存主键'
        primary key,
    cache_value  text         null comment '缓存内容',
    generated_at datetime     null comment '生成时间'
)
    comment '分析缓存表' charset = utf8mb4;

create table tags
(
    id          bigint auto_increment comment '标签ID'
        primary key,
    name        varchar(50)                          not null comment '标签名称',
    description varchar(200)                         null comment '标签描述',
    color       varchar(7) default '#3B82F6'         null comment '标签颜色',
    posts_count int        default 0                 null comment '使用此标签的帖子数',
    created_at  timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    constraint name
        unique (name)
)
    comment '标签表';

create index idx_name
    on tags (name);

create index idx_posts_count
    on tags (posts_count);

create table users
(
    id             bigint auto_increment comment '用户ID'
        primary key,
    username       varchar(50)                                                    not null comment '用户名',
    email          varchar(100)                                                   not null comment '邮箱',
    password       varchar(255)                                                   not null comment '密码（BCrypt加密）',
    role           enum ('student', 'teacher', 'admin') default 'student'         not null comment '用户角色',
    avatar         varchar(500)                                                   null comment '头像URL',
    nickname       varchar(50)                                                    null,
    first_name     varchar(50)                                                    null comment '姓氏',
    last_name      varchar(50)                                                    null comment '名字',
    gender         varchar(20)                                                    null,
    bio            text                                                           null comment '个人简介',
    grade          varchar(20)                                                    null comment '年级（学生）',
    subject        varchar(100)                                                   null comment '专业/科目',
    school         varchar(200)                                                   null comment '学校/学院',
    phone          varchar(20)                                                    null comment '联系电话',
    birthday       date                                                           null,
    country        varchar(64)                                                    null,
    province       varchar(64)                                                    null,
    city           varchar(64)                                                    null,
    created_at     timestamp                            default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     timestamp                            default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)                           default 0                 null comment '是否删除',
    email_verified tinyint(1)                           default 0                 null comment '邮箱是否已验证',
    student_no     varchar(50)                                                    null,
    teacher_no     varchar(50)                                                    null,
    constraint email
        unique (email),
    constraint uk_student_no
        unique (student_no),
    constraint uk_teacher_no
        unique (teacher_no),
    constraint username
        unique (username)
)
    comment '用户表';

create table ability_assessments
(
    id              bigint auto_increment comment '评估ID'
        primary key,
    student_id      bigint                                                                                     not null comment '学生ID',
    dimension_id    bigint                                                                                     not null comment '能力维度ID',
    ability_id      bigint                                                                                     null,
    assessor_id     bigint                                                                                     null comment '评估者ID（教师/系统）',
    assessment_type enum ('assignment', 'project', 'exam', 'peer', 'self', 'system') default 'assignment'      null comment '评估类型',
    related_id      bigint                                                                                     null comment '关联对象ID',
    score           decimal(5, 2)                                                                              not null comment '得分',
    max_score       decimal(10, 2)                                                                             null,
    ability_level   varchar(16)                                                                                null,
    confidence      decimal(5, 2)                                                                              null,
    evidence        text                                                                                       null comment '评估依据',
    improvement     text                                                                                       null,
    status          enum ('draft', 'completed', 'reviewed')                          default 'completed'       null comment '评估状态',
    assessed_at     timestamp                                                        default CURRENT_TIMESTAMP null comment '评估时间',
    created_at      timestamp                                                        default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      timestamp                                                        default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         tinyint(1)                                                       default 0                 not null,
    constraint ability_assessments_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint ability_assessments_ibfk_2
        foreign key (dimension_id) references ability_dimensions (id)
            on delete cascade,
    constraint ability_assessments_ibfk_3
        foreign key (assessor_id) references users (id)
            on delete set null
)
    comment '能力评估记录表';

create index idx_assessed_at
    on ability_assessments (assessed_at);

create index idx_assessment_type
    on ability_assessments (assessment_type);

create index idx_assessor_id
    on ability_assessments (assessor_id);

create index idx_dimension_id
    on ability_assessments (dimension_id);

create index idx_related
    on ability_assessments (related_id);

create index idx_student_id
    on ability_assessments (student_id);

create table ability_goals
(
    id            bigint auto_increment comment '目标ID'
        primary key,
    student_id    bigint                                                                       not null comment '学生ID',
    dimension_id  bigint                                                                       not null comment '能力维度ID',
    title         varchar(200)                                                                 not null comment '目标标题',
    description   text                                                                         null comment '目标描述',
    target_score  decimal(5, 2)                                                                not null comment '目标分数',
    current_score decimal(5, 2)                                      default 0.00              null comment '当前分数',
    target_date   date                                                                         not null comment '目标达成日期',
    priority      enum ('low', 'medium', 'high')                     default 'medium'          null comment '优先级',
    status        enum ('active', 'achieved', 'paused', 'cancelled') default 'active'          null comment '目标状态',
    achieved_at   timestamp                                                                    null comment '达成时间',
    created_at    timestamp                                          default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    timestamp                                          default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint ability_goals_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint ability_goals_ibfk_2
        foreign key (dimension_id) references ability_dimensions (id)
            on delete cascade
)
    comment '能力发展目标表';

create index idx_dimension_id
    on ability_goals (dimension_id);

create index idx_status
    on ability_goals (status);

create index idx_student_id
    on ability_goals (student_id);

create index idx_target_date
    on ability_goals (target_date);

create table ability_reports
(
    id                  bigint auto_increment comment '报告ID'
        primary key,
    student_id          bigint                                                                     not null comment '学生ID',
    report_type         enum ('monthly', 'semester', 'annual', 'custom') default 'monthly'         null comment '报告类型',
    title               varchar(200)                                                               not null comment '报告标题',
    overall_score       decimal(5, 2)                                                              not null comment '综合得分',
    report_period_start date                                                                       not null comment '报告周期开始',
    report_period_end   date                                                                       not null comment '报告周期结束',
    dimension_scores    json                                                                       null comment '各维度得分（JSON格式）',
    trends_analysis     text                                                                       null comment '趋势分析',
    achievements        json                                                                       null comment '成就列表（JSON格式）',
    improvement_areas   json                                                                       null comment '待改进领域（JSON格式）',
    recommendations     text                                                                       null comment '综合建议',
    generated_by        enum ('system', 'teacher')                       default 'system'          null comment '生成方式',
    is_published        tinyint(1)                                       default 0                 null comment '是否发布',
    created_at          timestamp                                        default CURRENT_TIMESTAMP null comment '创建时间',
    published_at        timestamp                                                                  null comment '发布时间',
    updated_at          datetime                                         default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint ability_reports_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade
)
    comment '能力报告表';

create index idx_created_at
    on ability_reports (created_at);

create index idx_period
    on ability_reports (report_period_start, report_period_end);

create index idx_report_type
    on ability_reports (report_type);

create index idx_student_id
    on ability_reports (student_id);

create table ai_conversations
(
    id              bigint auto_increment comment '会话ID'
        primary key,
    user_id         bigint                                 not null comment '所属用户ID（私有会话）',
    title           varchar(200) default '新对话'          not null comment '会话标题',
    model           varchar(100)                           null comment '模型标识（可选）',
    provider        varchar(50)                            null comment '服务商（可选）',
    pinned          tinyint(1)   default 0                 null comment '是否置顶',
    archived        tinyint(1)   default 0                 null comment '是否归档',
    deleted         tinyint(1)   default 0                 null comment '是否删除',
    last_message_at timestamp                              null comment '最后一条消息时间',
    created_at      timestamp    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint fk_ai_conversations_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment 'AI 会话表（用户私有）';

create index idx_archived
    on ai_conversations (archived);

create index idx_last_message_at
    on ai_conversations (last_message_at);

create index idx_pinned
    on ai_conversations (pinned);

create index idx_user_id
    on ai_conversations (user_id);

create table ai_memories
(
    id         bigint auto_increment comment '主键'
        primary key,
    user_id    bigint                               not null comment '用户ID',
    enabled    tinyint(1) default 1                 not null comment '是否启用记忆',
    content    text                                 null comment '记忆内容',
    updated_at timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uniq_user_id
        unique (user_id),
    constraint fk_ai_memories_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment 'AI 用户长期记忆';

create table ai_messages
(
    id              bigint auto_increment comment '消息ID'
        primary key,
    conversation_id bigint                              not null comment '会话ID',
    user_id         bigint                              not null comment '所属用户ID（冗余，用于权限隔离）',
    role            varchar(20)                         not null comment '角色：user/assistant/system',
    content         text                                not null comment '消息文本内容',
    attachments     json                                null comment '附件文件ID数组（如启用多模态）',
    created_at      timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    constraint fk_ai_messages_conversation
        foreign key (conversation_id) references ai_conversations (id)
            on delete cascade,
    constraint fk_ai_messages_user
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment 'AI 会话消息表';

create index idx_conv
    on ai_messages (conversation_id);

create index idx_user_conv
    on ai_messages (user_id, conversation_id);

create table courses
(
    id               bigint auto_increment comment '课程ID'
        primary key,
    title            varchar(200)                                                            not null comment '课程标题',
    description      text                                                                    null comment '课程描述',
    content          text                                                                    null,
    cover_image      varchar(500)                                                            null comment '课程封面图',
    teacher_id       bigint                                                                  not null comment '授课教师ID',
    category         varchar(100)                                                            null comment '课程分类',
    difficulty       enum ('beginner', 'intermediate', 'advanced') default 'beginner'        null comment '课程难度',
    duration         int                                                                     null comment '课程时长（分钟）',
    max_students     int                                           default 50                null comment '最大学生数',
    price            decimal(10, 2)                                default 0.00              null comment '课程价格',
    status           enum ('draft', 'published', 'archived')       default 'draft'           null comment '课程状态',
    start_date       date                                                                    null comment '开课日期',
    end_date         date                                                                    null comment '结课日期',
    tags             varchar(500)                                                            null comment '课程标签（JSON格式）',
    objectives       text                                                                    null comment '学习目标',
    requirements     text                                                                    null comment '先修要求',
    require_enroll_key tinyint(1) default 0                 null comment '是否需要入课密钥',
    enroll_key_hash   varchar(255)                               null comment '入课密钥哈希',
    created_at       timestamp                                     default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       timestamp                                     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          tinyint(1)                                    default 0                 null comment '是否删除',
    enrollment_count int                                           default 0                 null comment '选课人数',
    rating           decimal(2, 1)                                 default 0.0               null comment '课程评分',
    review_count     int                                           default 0                 null comment '评价数量',
    constraint courses_ibfk_1
        foreign key (teacher_id) references users (id)
            on delete cascade
)
    comment '课程表';

create table assignments
(
    id               bigint auto_increment comment '作业ID'
        primary key,
    course_id        bigint                                                          not null comment '课程ID',
    teacher_id       bigint                                                          not null comment '发布教师ID',
    title            varchar(200)                                                    not null comment '作业标题',
    description      text                                                            null comment '作业描述',
    requirements     text                                                            null comment '作业要求',
    max_score        decimal(5, 2)                         default 100.00            null comment '总分',
    due_date         timestamp                                                       not null comment '截止时间',
    allow_late       tinyint(1)                            default 0                 null comment '是否允许迟交',
    max_attempts     int                                   default 1                 null comment '最大提交次数',
    file_types       varchar(200)                                                    null comment '允许的文件类型',
    max_file_size    int                                   default 10                null comment '最大文件大小（MB）',
    status           enum ('draft', 'published', 'closed') default 'draft'           null comment '作业状态',
    created_at       timestamp                             default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       timestamp                             default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          tinyint(1)                            default 0                 null comment '是否删除',
    submission_count int                                   default 0                 null comment '提交数量',
    constraint assignments_ibfk_1
        foreign key (course_id) references courses (id)
            on delete cascade,
    constraint assignments_ibfk_2
        foreign key (teacher_id) references users (id)
            on delete cascade
)
    comment '作业表';

create index idx_assignment_course_status
    on assignments (course_id, status);

create index idx_assignment_due_date
    on assignments (due_date);

create index idx_assignments_course_status
    on assignments (course_id, status);

create index idx_course_id
    on assignments (course_id);

create index idx_deleted
    on assignments (deleted);

create index idx_due_date
    on assignments (due_date);

create index idx_status
    on assignments (status);

create index idx_teacher_id
    on assignments (teacher_id);

create table course_ability_weights
(
    id             bigint auto_increment comment '主键'
        primary key,
    course_id      bigint                                  not null comment '课程ID',
    dimension_code varchar(50)                             not null comment '维度编码：MORAL_COGNITION/LEARNING_ATTITUDE/LEARNING_ABILITY/LEARNING_METHOD/ACADEMIC_GRADE',
    weight         decimal(5, 2) default 1.00              not null comment '权重',
    updated_at     timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_course_dimension
        unique (course_id, dimension_code),
    constraint fk_course_weights_course
        foreign key (course_id) references courses (id)
            on delete cascade
)
    comment '课程能力权重表';

create index idx_course_id
    on course_ability_weights (course_id);

create index idx_category
    on courses (category);

create index idx_deleted
    on courses (deleted);

create index idx_start_date
    on courses (start_date);

create index idx_status
    on courses (status);

create index idx_teacher_id
    on courses (teacher_id);

create table enrollments
(
    id               bigint auto_increment comment '选课ID'
        primary key,
    student_id       bigint                                                            not null comment '学生ID',
    course_id        bigint                                                            not null comment '课程ID',
    enrolled_at      timestamp                               default CURRENT_TIMESTAMP null comment '选课时间',
    progress         decimal(5, 2)                           default 0.00              null comment '学习进度（百分比）',
    status           enum ('active', 'completed', 'dropped') default 'active'          null comment '选课状态',
    grade            decimal(5, 2)                                                     null comment '课程成绩',
    completion_date  timestamp                                                         null comment '完成时间',
    created_at       timestamp                               default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       timestamp                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    last_access_time datetime                                                          null comment '最后一次访问课程的时间',
    constraint uk_student_course
        unique (student_id, course_id),
    constraint enrollments_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint enrollments_ibfk_2
        foreign key (course_id) references courses (id)
            on delete cascade
)
    comment '选课关系表';

create index idx_course_id
    on enrollments (course_id);

create index idx_enrollment_course_student
    on enrollments (course_id, student_id);

create index idx_enrollment_student_course
    on enrollments (student_id, course_id);

create index idx_enrollments_student_course_status
    on enrollments (student_id, course_id, status);

create index idx_status
    on enrollments (status);

create index idx_student_id
    on enrollments (student_id);

create table file_records
(
    id             bigint auto_increment comment '文件ID'
        primary key,
    original_name  varchar(255)                        not null comment '原始文件名',
    saved_name     varchar(255)                        not null comment '保存文件名',
    file_path      varchar(500)                        not null comment '文件路径',
    file_size      bigint                              not null comment '文件大小（字节）',
    file_type      varchar(100)                        null comment '文件类型',
    mime_type      varchar(100)                        null comment 'MIME类型',
    uploader_id    bigint                              not null comment '上传者ID',
    related_type   varchar(50)                         null comment '关联类型（assignment, submission, profile等）',
    related_id     bigint                              null comment '关联ID',
    download_count int       default 0                 null comment '下载次数',
    created_at     timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    constraint file_records_ibfk_1
        foreign key (uploader_id) references users (id)
            on delete cascade
)
    comment '文件记录表';

create index idx_file_type
    on file_records (file_type);

create index idx_related
    on file_records (related_type, related_id);

create index idx_uploader_id
    on file_records (uploader_id);

create table grades
(
    id             bigint auto_increment comment '成绩ID'
        primary key,
    student_id     bigint                                                not null comment '学生ID',
    assignment_id  bigint                                                not null comment '作业ID',
    score          decimal(5, 2)                                         not null comment '得分',
    max_score      decimal(5, 2)                                         not null comment '满分',
    percentage     decimal(5, 2)                                         null comment '百分比分数',
    grade_level    varchar(2)                                            null comment '等级(A,B,C,D,F)',
    feedback       text                                                  null comment '评语',
    strengths      text                                                  null,
    improvements   text                                                  null,
    allow_resubmit tinyint(1)                  default 0                 not null,
    rubric_json    json                                                  null,
    regrade_reason text                                                  null,
    status         enum ('draft', 'published') default 'draft'           null comment '状态',
    published_at   timestamp                                             null,
    deleted        tinyint(1)                  default 0                 null comment '是否删除',
    created_at     timestamp                   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     timestamp                   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    submission_id  bigint                                                null comment '提交ID',
    grader_id      bigint                                                null comment '评分人ID',
    constraint grades_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint grades_ibfk_2
        foreign key (assignment_id) references assignments (id)
            on delete cascade
)
    comment '成绩表';

create index idx_assignment_id
    on grades (assignment_id);

create index idx_grade_assignment
    on grades (assignment_id);

create index idx_grade_status
    on grades (status);

create index idx_grade_student_created
    on grades (student_id, created_at);

create index idx_grades_created_at
    on grades (created_at);

create index idx_score
    on grades (score);

create index idx_status
    on grades (status);

create index idx_student_id
    on grades (student_id);

create table learning_recommendations
(
    id                  bigint auto_increment comment '建议ID'
        primary key,
    student_id          bigint                                                                       not null comment '学生ID',
    dimension_id        bigint                                                                       not null comment '能力维度ID',
    title               varchar(200)                                                                 not null comment '建议标题',
    description         text                                                                         not null comment '建议内容',
    recommendation_type enum ('course', 'resource', 'practice', 'project') default 'course'          null comment '建议类型',
    resource_url        varchar(500)                                                                 null comment '资源链接',
    difficulty_level    enum ('beginner', 'intermediate', 'advanced')      default 'intermediate'    null comment '难度等级',
    estimated_time      varchar(50)                                                                  null comment '预估学习时间',
    priority_score      decimal(3, 2)                                      default 1.00              null comment '优先级分数',
    is_read             tinyint(1)                                         default 0                 null comment '是否已读',
    is_accepted         tinyint(1)                                         default 0                 null comment '是否已采纳',
    created_at          timestamp                                          default CURRENT_TIMESTAMP null comment '创建时间',
    expires_at          timestamp                                                                    null comment '过期时间',
    constraint learning_recommendations_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint learning_recommendations_ibfk_2
        foreign key (dimension_id) references ability_dimensions (id)
            on delete cascade
)
    comment '学习建议表';

create index idx_dimension_id
    on learning_recommendations (dimension_id);

create index idx_is_read
    on learning_recommendations (is_read);

create index idx_priority
    on learning_recommendations (priority_score);

create index idx_student_id
    on learning_recommendations (student_id);

create index idx_type
    on learning_recommendations (recommendation_type);

create table lessons
(
    id          bigint auto_increment comment '章节ID'
        primary key,
    course_id   bigint                               not null comment '课程ID',
    title       varchar(200)                         not null comment '章节标题',
    description text                                 null comment '章节描述',
    content     text                                 null comment '章节内容',
    video_url   varchar(500)                         null comment '视频URL',
    allow_scrubbing tinyint(1) default 1             null comment '允许拖动进度条',
    allow_speed_change tinyint(1) default 1          null comment '允许倍速',
    duration    int                                  null comment '章节时长（分钟）',
    order_index int                                  not null comment '排序索引',
    is_free     tinyint(1) default 0                 null comment '是否免费',
    chapter_id  bigint                               null comment '所属章节ID',
    weight      decimal(4, 2) default 1.00           null comment '章节权重',
    created_at  timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint(1) default 0                 null comment '是否删除',
    constraint lessons_ibfk_1
        foreign key (course_id) references courses (id)
            on delete cascade
)
    comment '课程章节表';

create table lesson_progress
(
    id              bigint auto_increment comment '进度ID'
        primary key,
    student_id      bigint                                  not null comment '学生ID',
    lesson_id       bigint                                  not null comment '章节ID',
    progress        decimal(5, 2) default 0.00              null comment '学习进度（百分比）',
    completed       tinyint(1)    default 0                 null comment '是否完成',
    watch_time      int           default 0                 null comment '观看时长（秒）',
    completed_at    timestamp                               null comment '完成时间',
    created_at      timestamp     default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    last_studied_at datetime      default CURRENT_TIMESTAMP null,
    course_id       bigint                                  null comment '课程ID',
    view_count      int           default 0                 null comment '观看次数',
    last_position   int           default 0                 null comment '最后观看位置(秒)',
    status          varchar(32)   default 'not_started'     null comment '学习状态',
    notes           text                                    null comment '学习笔记',
    rating          int           default 0                 null comment '学习评分',
    started_at      datetime                                null comment '开始学习时间',
    study_time      int           default 0                 null,
    constraint uk_student_lesson
        unique (student_id, lesson_id),
    constraint lesson_progress_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint lesson_progress_ibfk_2
        foreign key (lesson_id) references lessons (id)
            on delete cascade
)
    comment '学习进度表';

create index idx_completed
    on lesson_progress (completed);

create index idx_lesson_id
    on lesson_progress (lesson_id);

create index idx_lesson_progress_last_studied
    on lesson_progress (student_id, last_studied_at);

create index idx_lesson_progress_student_course
    on lesson_progress (student_id, course_id);

create index idx_lesson_progress_student_lesson
    on lesson_progress (student_id, lesson_id);

create index idx_lp_last_studied
    on lesson_progress (student_id, last_studied_at);

create index idx_lp_student_course
    on lesson_progress (student_id, course_id);

create index idx_lp_student_lesson
    on lesson_progress (student_id, lesson_id);

create index idx_student_id
    on lesson_progress (student_id);

create index idx_course_id
    on lessons (course_id);

create index idx_deleted
    on lessons (deleted);

create index idx_order_index
    on lessons (order_index);

create table notifications
(
    id           bigint auto_increment comment '通知ID'
        primary key,
    user_id      bigint                                                                                        not null comment '用户ID',
    sender_id    bigint                                                                                        null,
    title        varchar(200)                                                                                  not null comment '通知标题',
    content      text                                                                                          null comment '通知内容',
    type         enum ('system', 'assignment', 'grade', 'course', 'message', 'post') default 'system'          not null,
    category     varchar(50)                                                                                   null comment '通知分类，如学术/系统/活动',
    priority     enum ('low', 'normal', 'high', 'urgent')                            default 'normal'          null comment '优先级',
    is_read      tinyint(1)                                                          default 0                 null comment '是否已读',
    read_at      datetime                                                                                      null comment '已读时间',
    related_type varchar(50)                                                                                   null comment '关联类型',
    related_id   bigint                                                                                        null comment '关联ID',
    expired_at   timestamp                                                                                     null comment '过期时间',
    created_at   timestamp                                                           default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at   datetime                                                                                      null comment '更新时间',
    deleted      tinyint(1)                                                          default 0                 null comment '软删除标志',
    data         text                                                                                          null comment '附加数据(JSON)',
    icon         varchar(50)                                                                                   null comment '通知图标',
    action_url   varchar(255)                                                                                  null comment '操作跳转URL',
    constraint notifications_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '通知表';

create index idx_created_at
    on notifications (created_at);

create index idx_is_read
    on notifications (is_read);

create index idx_sender
    on notifications (sender_id);

create index idx_sender_id
    on notifications (sender_id);

create index idx_type
    on notifications (type);

create index idx_user_created_at
    on notifications (user_id, created_at);

create index idx_user_id
    on notifications (user_id);

create index idx_user_sender_created
    on notifications (user_id, sender_id, created_at);

create index idx_user_sender_created_at
    on notifications (user_id, sender_id, created_at);

create table posts
(
    id             bigint auto_increment comment '帖子ID'
        primary key,
    title          varchar(200)                          not null comment '帖子标题',
    content        text                                  not null comment '帖子内容',
    category       varchar(50) default 'study'           not null comment '帖子分类：study,homework,share,qa,chat',
    author_id      bigint                                not null comment '作者ID',
    pinned         tinyint(1)  default 0                 null comment '是否置顶',
    anonymous      tinyint(1)  default 0                 null comment '是否匿名发布',
    allow_comments tinyint(1)  default 1                 null comment '是否允许评论',
    views          int         default 0                 null comment '浏览次数',
    likes_count    int         default 0                 null comment '点赞数',
    comments_count int         default 0                 null comment '评论数',
    status         varchar(20) default 'published'       null comment '状态：draft,published,deleted',
    created_at     timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted        tinyint(1)  default 0                 null comment '是否删除',
    constraint posts_ibfk_1
        foreign key (author_id) references users (id)
            on delete cascade
)
    comment '帖子表';

create table post_comments
(
    id          bigint auto_increment comment '评论ID'
        primary key,
    post_id     bigint                                not null comment '帖子ID',
    author_id   bigint                                not null comment '评论者ID',
    parent_id   bigint                                null comment '父评论ID（用于回复）',
    content     text                                  not null comment '评论内容',
    likes_count int         default 0                 null comment '点赞数',
    status      varchar(20) default 'published'       null comment '状态：published,deleted',
    created_at  timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint(1)  default 0                 null comment '是否删除',
    constraint post_comments_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint post_comments_ibfk_2
        foreign key (author_id) references users (id)
            on delete cascade,
    constraint post_comments_ibfk_3
        foreign key (parent_id) references post_comments (id)
            on delete cascade
)
    comment '帖子评论表';

create table comment_likes
(
    id         bigint auto_increment comment '点赞ID'
        primary key,
    comment_id bigint                              not null comment '评论ID',
    user_id    bigint                              not null comment '用户ID',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_comment_user
        unique (comment_id, user_id),
    constraint comment_likes_ibfk_1
        foreign key (comment_id) references post_comments (id)
            on delete cascade,
    constraint comment_likes_ibfk_2
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '评论点赞表';

create index idx_comment_id
    on comment_likes (comment_id);

create index idx_user_id
    on comment_likes (user_id);

create index idx_author_id
    on post_comments (author_id);

create index idx_created_at
    on post_comments (created_at);

create index idx_parent_id
    on post_comments (parent_id);

create index idx_post_id
    on post_comments (post_id);

create table post_likes
(
    id         bigint auto_increment comment '点赞ID'
        primary key,
    post_id    bigint                              not null comment '帖子ID',
    user_id    bigint                              not null comment '用户ID',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_post_user
        unique (post_id, user_id),
    constraint post_likes_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint post_likes_ibfk_2
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '帖子点赞表';

create index idx_post_id
    on post_likes (post_id);

create index idx_user_id
    on post_likes (user_id);

create table post_tags
(
    id         bigint auto_increment comment '关联ID'
        primary key,
    post_id    bigint                              not null comment '帖子ID',
    tag_id     bigint                              not null comment '标签ID',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_post_tag
        unique (post_id, tag_id),
    constraint post_tags_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint post_tags_ibfk_2
        foreign key (tag_id) references tags (id)
            on delete cascade
)
    comment '帖子标签关联表';

create index idx_post_id
    on post_tags (post_id);

create index idx_tag_id
    on post_tags (tag_id);

create index idx_author_id
    on posts (author_id);

create index idx_category
    on posts (category);

create index idx_created_at
    on posts (created_at);

create index idx_pinned
    on posts (pinned);

create index idx_status
    on posts (status);

create table student_abilities
(
    id                 bigint auto_increment comment '记录ID'
        primary key,
    student_id         bigint                                                                            not null comment '学生ID',
    dimension_id       bigint                                                                            not null comment '能力维度ID',
    current_score      decimal(5, 2)                                           default 0.00              null comment '当前得分',
    level              enum ('beginner', 'intermediate', 'advanced', 'expert') default 'beginner'        null comment '能力等级',
    last_assessment_at timestamp                                                                         null comment '最后评估时间',
    assessment_count   int                                                     default 0                 null comment '评估次数',
    trend              enum ('rising', 'stable', 'declining')                  default 'stable'          null comment '发展趋势',
    created_at         timestamp                                               default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at         timestamp                                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_student_dimension
        unique (student_id, dimension_id),
    constraint student_abilities_ibfk_1
        foreign key (student_id) references users (id)
            on delete cascade,
    constraint student_abilities_ibfk_2
        foreign key (dimension_id) references ability_dimensions (id)
            on delete cascade
)
    comment '学生能力记录表';

create index idx_dimension_id
    on student_abilities (dimension_id);

create index idx_level
    on student_abilities (level);

create index idx_student_id
    on student_abilities (student_id);

create table submissions
(
    id               bigint auto_increment comment '提交ID'
        primary key,
    assignment_id    bigint                                                          not null comment '作业ID',
    student_id       bigint                                                          not null comment '学生ID',
    content          text                                                            null comment '作业内容',
    file_path        varchar(500)                                                    null comment '文件路径',
    file_name        varchar(200)                                                    null comment '文件名',
    file_size        int                                                             null comment '文件大小（字节）',
    submission_count int                                   default 1                 null comment '提交次数',
    status           enum ('draft', 'submitted', 'graded') default 'draft'           null comment '提交状态',
    submitted_at     timestamp                                                       null comment '提交时间',
    is_late          tinyint(1)                            default 0                 null comment '是否迟交',
    created_at       timestamp                             default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       timestamp                             default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_assignment_student
        unique (assignment_id, student_id),
    constraint submissions_ibfk_1
        foreign key (assignment_id) references assignments (id)
            on delete cascade,
    constraint submissions_ibfk_2
        foreign key (student_id) references users (id)
            on delete cascade
)
    comment '作业提交表';

create index idx_assignment_id
    on submissions (assignment_id);

create index idx_status
    on submissions (status);

create index idx_student_id
    on submissions (student_id);

create index idx_submission_assignment_student
    on submissions (assignment_id, student_id);

create index idx_submission_status
    on submissions (status);

create index idx_submission_student_submitted_at
    on submissions (student_id, submitted_at);

create index idx_submitted_at
    on submissions (submitted_at);

create index idx_deleted
    on users (deleted);

create index idx_email
    on users (email);

create index idx_role
    on users (role);

create index idx_username
    on users (username);

create
    definer = root@localhost procedure add_column_if_not_exists(IN db_name varchar(64), IN tbl_name varchar(64),
                                                                IN col_name varchar(64), IN col_def varchar(512))
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = db_name
       AND TABLE_NAME   = tbl_name
       AND COLUMN_NAME  = col_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', db_name, '`.`', tbl_name, '` ADD COLUMN `', col_name, '` ', col_def);
    PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END;

create
    definer = root@localhost procedure add_index_if_not_exists(IN db_name varchar(64), IN tbl_name varchar(64),
                                                               IN idx_name varchar(64), IN idx_cols varchar(512))
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = db_name
       AND TABLE_NAME   = tbl_name
       AND INDEX_NAME   = idx_name
  ) THEN
    SET @ddl = CONCAT('CREATE INDEX `', idx_name, '` ON `', db_name, '`.`', tbl_name, '` ', idx_cols);
    PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;
END;


-- RESOLVED: merge artifact end removed

-- ================== help_category：帮助分类表 ==================
CREATE TABLE IF NOT EXISTS `help_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类标识',
  `sort` int DEFAULT 0 COMMENT '排序',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_help_category_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助中心分类表';

-- ================== help_article：帮助文章表 ==================
CREATE TABLE IF NOT EXISTS `help_article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `slug` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标识',
  `content_md` mediumtext COLLATE utf8mb4_unicode_ci COMMENT 'Markdown内容',
  `content_html` mediumtext COLLATE utf8mb4_unicode_ci COMMENT '渲染HTML',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签(逗号分隔)',
  `views` int DEFAULT 0 COMMENT '浏览数',
  `up_votes` int DEFAULT 0 COMMENT '有用票',
  `down_votes` int DEFAULT 0 COMMENT '无用票',
  `published` tinyint(1) DEFAULT 1 COMMENT '是否发布',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_help_article_slug` (`slug`),
  KEY `idx_help_article_cat` (`category_id`),
  CONSTRAINT `fk_help_article_category` FOREIGN KEY (`category_id`) REFERENCES `help_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助中心文章表';

-- ================== help_article_feedback：文章反馈/有用性投票 ==================
CREATE TABLE IF NOT EXISTS `help_article_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID(可空表示匿名)',
  `helpful` tinyint(1) DEFAULT NULL COMMENT '是否有帮助(可空表示仅文本反馈)',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '反馈内容',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_help_article_feedback_article` (`article_id`),
  KEY `idx_help_article_feedback_user` (`user_id`),
  CONSTRAINT `fk_help_feedback_article` FOREIGN KEY (`article_id`) REFERENCES `help_article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_help_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助文章反馈/投票表';

-- ================== help_ticket：技术支持工单 ==================
CREATE TABLE IF NOT EXISTS `help_ticket` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `user_id` bigint NOT NULL COMMENT '提交用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `status` enum('open','in_progress','resolved','closed') COLLATE utf8mb4_unicode_ci DEFAULT 'open' COMMENT '状态',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_help_ticket_user` (`user_id`),
  KEY `idx_help_ticket_status` (`status`),
  CONSTRAINT `fk_help_ticket_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术支持工单表';

/* ================================
   Chat Conversations Schema & Data Backfill
   ================================ */

-- A) 会话表（服务端统一“最近会话”）
CREATE TABLE IF NOT EXISTS chat_conversations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
  type VARCHAR(20) NOT NULL DEFAULT 'direct' COMMENT '会话类型：direct(点对点)；预留群聊',
  peer_a_id BIGINT NOT NULL COMMENT '成员A（较小的用户ID）',
  peer_b_id BIGINT NOT NULL COMMENT '成员B（较大的用户ID）',
  course_id BIGINT NOT NULL DEFAULT 0 COMMENT '课程ID（0表示无课程上下文）',
  last_message_id BIGINT NULL COMMENT '最后一条消息ID（notifications.id）',
  last_message_at DATETIME NULL COMMENT '最后一条消息时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_conv_direct (peer_a_id, peer_b_id, course_id),
  KEY idx_conv_last (last_message_at),
  KEY idx_conv_peers (peer_a_id, peer_b_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS chat_conversation_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  conversation_id BIGINT NOT NULL COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '成员用户ID',
  unread_count INT NOT NULL DEFAULT 0 COMMENT '未读数',
  pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
  archived TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否归档',
  last_read_message_id BIGINT NULL COMMENT '最后已读消息ID',
  last_read_at DATETIME NULL COMMENT '最后已读时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_conv_member (conversation_id, user_id),
  KEY idx_member_user (user_id, pinned, archived),
  KEY idx_member_conv (conversation_id),
  CONSTRAINT fk_members_conv FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话成员表';

-- B) notifications 新增列与索引（兼容写法：信息模式判断后动态执行）
-- 添加 conversation_id 列（若不存在）
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notifications'
    AND COLUMN_NAME = 'conversation_id'
);

SET @has_related := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notifications'
    AND COLUMN_NAME = 'related_id'
);

SET @ddl := IF(@col_exists = 0,
  IF(@has_related = 1,
    'ALTER TABLE notifications ADD COLUMN conversation_id BIGINT NULL AFTER related_id',
    'ALTER TABLE notifications ADD COLUMN conversation_id BIGINT NULL'
  ),
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加索引 idx_notifications_conv（若不存在）
SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'notifications'
    AND INDEX_NAME = 'idx_notifications_conv'
);
SET @ddl := IF(@idx_exists = 0,
  'ALTER TABLE notifications ADD INDEX idx_notifications_conv (conversation_id, created_at)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- C) 历史消息回填（健壮版；仅首次部署需要）
-- 说明：
-- - 仅处理 type='message' 的记录
-- - user_id 为接收者列，sender_id 为发送者列
-- - 会话键 = (LEAST(sender_id,user_id), GREATEST(sender_id,user_id), COALESCE(related_id,0))

-- C.1 有效消息明细（过滤 sender/user 为空或相等）
DROP TEMPORARY TABLE IF EXISTS tmp_chat_msgs;
CREATE TEMPORARY TABLE tmp_chat_msgs
SELECT
  n.id,
  LEAST(n.sender_id, n.user_id)       AS peer_a_id,
  GREATEST(n.sender_id, n.user_id)    AS peer_b_id,
  COALESCE(n.related_id, 0)           AS course_id,
  n.sender_id,
  n.user_id,
  n.created_at
FROM notifications n
WHERE n.type = 'message'
  AND n.sender_id IS NOT NULL
  AND n.user_id   IS NOT NULL
  AND n.sender_id <> n.user_id;

ALTER TABLE tmp_chat_msgs
  ADD INDEX idx_tmm_key (peer_a_id, peer_b_id, course_id),
  ADD INDEX idx_tmm_created (created_at);

-- C.2 每个会话键的最后时间
DROP TEMPORARY TABLE IF EXISTS tmp_conv_keys;
CREATE TEMPORARY TABLE tmp_conv_keys
SELECT
  t.peer_a_id,
  t.peer_b_id,
  t.course_id,
  MAX(t.created_at) AS max_created
FROM tmp_chat_msgs t
GROUP BY t.peer_a_id, t.peer_b_id, t.course_id;

ALTER TABLE tmp_conv_keys
  ADD INDEX idx_tck_key (peer_a_id, peer_b_id, course_id),
  ADD INDEX idx_tck_created (max_created);

-- C.3 插入/更新 chat_conversations（last_message_at）
INSERT INTO chat_conversations (
  peer_a_id, peer_b_id, course_id, last_message_at, created_at, updated_at
)
SELECT
  k.peer_a_id,
  k.peer_b_id,
  k.course_id,
  k.max_created AS last_message_at,
  NOW(), NOW()
FROM tmp_conv_keys k
ON DUPLICATE KEY UPDATE
  last_message_at = GREATEST(VALUES(last_message_at), chat_conversations.last_message_at),
  updated_at      = NOW();

-- C.4 每个会话键的最后一条消息ID
DROP TEMPORARY TABLE IF EXISTS tmp_last_ids;
CREATE TEMPORARY TABLE tmp_last_ids
SELECT
  k.peer_a_id,
  k.peer_b_id,
  k.course_id,
  n1.id AS last_id
FROM tmp_conv_keys k
JOIN notifications n1
  ON LEAST(n1.sender_id, n1.user_id) = k.peer_a_id
 AND GREATEST(n1.sender_id, n1.user_id) = k.peer_b_id
 AND COALESCE(n1.related_id, 0) = k.course_id
 AND n1.type = 'message'
 AND n1.created_at = k.max_created;

ALTER TABLE tmp_last_ids
  ADD INDEX idx_tli_key (peer_a_id, peer_b_id, course_id);

-- C.5 回填 chat_conversations.last_message_id
UPDATE chat_conversations c
JOIN tmp_last_ids tl
  ON tl.peer_a_id = c.peer_a_id
 AND tl.peer_b_id = c.peer_b_id
 AND tl.course_id = c.course_id
SET c.last_message_id = tl.last_id,
    c.updated_at      = NOW();

-- C.6 回写 notifications.conversation_id（建立消息与会话关系）
UPDATE notifications n
JOIN tmp_chat_msgs m
  ON m.id = n.id
JOIN chat_conversations c
  ON c.peer_a_id = m.peer_a_id
 AND c.peer_b_id = m.peer_b_id
 AND c.course_id = m.course_id
SET n.conversation_id = c.id
WHERE n.type = 'message';

-- C.7 创建/更新会话成员（两个成员），计算未读（接收者 user_id）
INSERT INTO chat_conversation_members (
  conversation_id, user_id, unread_count, pinned, archived, last_read_message_id, last_read_at, created_at, updated_at
)
SELECT
  c.id AS conversation_id,
  c.peer_a_id AS user_id,
  (
    SELECT COUNT(*)
    FROM notifications n
    WHERE n.conversation_id = c.id
      AND n.type = 'message'
      AND n.user_id = c.peer_a_id
      AND (n.is_read = 0 OR n.is_read IS NULL)
  ) AS unread_count,
  0, 0, NULL, NULL, NOW(), NOW()
FROM chat_conversations c
ON DUPLICATE KEY UPDATE
  unread_count = VALUES(unread_count),
  updated_at   = NOW();

INSERT INTO chat_conversation_members (
  conversation_id, user_id, unread_count, pinned, archived, last_read_message_id, last_read_at, created_at, updated_at
)
SELECT
  c.id AS conversation_id,
  c.peer_b_id AS user_id,
  (
    SELECT COUNT(*)
    FROM notifications n
    WHERE n.conversation_id = c.id
      AND n.type = 'message'
      AND n.user_id = c.peer_b_id
      AND (n.is_read = 0 OR n.is_read IS NULL)
  ) AS unread_count,
  0, 0, NULL, NULL, NOW(), NOW()
FROM chat_conversations c
ON DUPLICATE KEY UPDATE
  unread_count = VALUES(unread_count),
  updated_at   = NOW();

-- C.8 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_last_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_conv_keys;
DROP TEMPORARY TABLE IF EXISTS tmp_chat_msgs;