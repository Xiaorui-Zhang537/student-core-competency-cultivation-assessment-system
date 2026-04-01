-- Migration script
-- from: e591f825fd5b9226ea3962a20a831061ad09cf58
-- to  : 5a6a75536071151996a1b1ef56887375f2fe7f0b
-- database: MySQL 8.x

USE student_assessment_system;

-- =========================
-- 1) 结构清理（移除已废弃对象）
-- =========================
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS analytics_cache;
DROP TABLE IF EXISTS abilities;

SET @drop_ability_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'ability_assessments'
              AND COLUMN_NAME = 'ability_id'
        ),
        'ALTER TABLE ability_assessments DROP COLUMN ability_id',
        'SELECT ''ability_assessments.ability_id already removed'''
    )
);

PREPARE stmt FROM @drop_ability_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- 2) 现有表增量字段
-- 兼容 MySQL 5.7 / 8.0.29 之前（不支持 ADD COLUMN IF NOT EXISTS）
-- =========================

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'status'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''active'' COMMENT ''状态：active,disabled'' AFTER role',
    'SELECT ''users.status already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE users
SET status = 'active'
WHERE status IS NULL OR status = '';

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'channel'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN channel VARCHAR(32) DEFAULT ''support'' NULL COMMENT ''渠道：support/feedback''',
    'SELECT ''help_ticket.channel already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'ticket_type'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN ticket_type VARCHAR(64) DEFAULT ''other'' NULL COMMENT ''类型''',
    'SELECT ''help_ticket.ticket_type already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'priority'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN priority VARCHAR(16) DEFAULT ''medium'' NULL COMMENT ''优先级''',
    'SELECT ''help_ticket.priority already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'source_role'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN source_role VARCHAR(32) DEFAULT ''student'' NULL COMMENT ''来源角色''',
    'SELECT ''help_ticket.source_role already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'contact'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN contact VARCHAR(255) NULL COMMENT ''联系方式''',
    'SELECT ''help_ticket.contact already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'is_anonymous'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN is_anonymous TINYINT(1) DEFAULT 0 NULL COMMENT ''是否匿名展示''',
    'SELECT ''help_ticket.is_anonymous already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'assignee_admin_id'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN assignee_admin_id BIGINT NULL COMMENT ''处理管理员ID''',
    'SELECT ''help_ticket.assignee_admin_id already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'last_reply_at'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN last_reply_at TIMESTAMP NULL COMMENT ''最近回复时间''',
    'SELECT ''help_ticket.last_reply_at already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'help_ticket'
      AND COLUMN_NAME = 'closed_at'
);

SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE help_ticket ADD COLUMN closed_at TIMESTAMP NULL COMMENT ''关闭时间''',
    'SELECT ''help_ticket.closed_at already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE help_ticket
SET channel = COALESCE(channel, 'support'),
    ticket_type = COALESCE(ticket_type, 'other'),
    priority = COALESCE(priority, 'medium'),
    source_role = COALESCE(source_role, 'student'),
    is_anonymous = COALESCE(is_anonymous, 0)
WHERE channel IS NULL
   OR ticket_type IS NULL
   OR priority IS NULL
   OR source_role IS NULL
   OR is_anonymous IS NULL;

-- =========================
-- 3) 新增表（管理员审计 / 帮助中心消息 / 行为洞察 / 口语训练）
-- =========================

CREATE TABLE IF NOT EXISTS admin_audit_logs
(
    id          BIGINT AUTO_INCREMENT COMMENT '审计ID'
        PRIMARY KEY,
    actor_id    BIGINT                              NOT NULL COMMENT '操作人ID',
    action      VARCHAR(80)                         NOT NULL COMMENT '动作标识',
    target_type VARCHAR(50)                         NULL COMMENT '目标类型',
    target_id   BIGINT                              NULL COMMENT '目标ID',
    detail_json JSON                                NULL COMMENT '详情(JSON)',
    ip          VARCHAR(45)                         NULL COMMENT 'IP',
    user_agent  VARCHAR(255)                        NULL COMMENT 'User-Agent',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    KEY idx_action (action),
    KEY idx_actor_id (actor_id),
    KEY idx_created_at (created_at),
    KEY idx_target (target_type, target_id)
)
    COMMENT '管理员审计日志' CHARSET = utf8mb4;

SET @fk_admin_audit_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'admin_audit_logs'
      AND CONSTRAINT_NAME = 'admin_audit_logs_ibfk_1'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_admin_audit_sql := IF(
    @fk_admin_audit_exists = 0,
    'ALTER TABLE admin_audit_logs ADD CONSTRAINT admin_audit_logs_ibfk_1 FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE CASCADE',
    'SELECT ''admin_audit_logs_ibfk_1 already exists'''
);

PREPARE stmt FROM @add_fk_admin_audit_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS help_ticket_message
(
    id          BIGINT AUTO_INCREMENT COMMENT '消息ID'
        PRIMARY KEY,
    ticket_id   BIGINT                              NOT NULL COMMENT '工单ID',
    sender_id   BIGINT                              NOT NULL COMMENT '发送者ID',
    sender_role VARCHAR(32)                         NULL COMMENT '发送者角色',
    sender_side VARCHAR(16)                         NOT NULL COMMENT '发送侧：user/admin',
    content     TEXT                                NOT NULL COMMENT '消息内容',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    KEY idx_help_ticket_message_ticket (ticket_id)
)
    COMMENT '技术支持工单消息表' CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS behavior_events
(
    id           BIGINT AUTO_INCREMENT COMMENT '事件ID'
        PRIMARY KEY,
    student_id   BIGINT                               NOT NULL COMMENT '学生ID（行为主体）',
    course_id    BIGINT                               NULL COMMENT '课程ID（可选）',
    event_type   VARCHAR(64)                          NOT NULL COMMENT '事件类型code（ai_question/assignment_submit/...)',
    related_type VARCHAR(64)                          NULL COMMENT '关联对象类型（assignment/submission/post/...）',
    related_id   BIGINT                               NULL COMMENT '关联对象ID',
    metadata     JSON                                 NULL COMMENT '事件元数据(JSON)',
    occurred_at  DATETIME                             NOT NULL COMMENT '事件发生时间',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL COMMENT '记录创建时间',
    KEY idx_behavior_events_student_time (student_id, occurred_at),
    KEY idx_behavior_events_course_time (course_id, occurred_at),
    KEY idx_behavior_events_type_time (event_type, occurred_at)
)
    COMMENT '行为事件表（事实记录，append-only）' CHARSET = utf8mb4;

SET @fk_behavior_events_student_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_events'
      AND CONSTRAINT_NAME = 'fk_behavior_events_student'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_events_student_sql := IF(
    @fk_behavior_events_student_exists = 0,
    'ALTER TABLE behavior_events ADD CONSTRAINT fk_behavior_events_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE',
    'SELECT ''fk_behavior_events_student already exists'''
);

PREPARE stmt FROM @add_fk_behavior_events_student_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_behavior_events_course_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_events'
      AND CONSTRAINT_NAME = 'fk_behavior_events_course'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_events_course_sql := IF(
    @fk_behavior_events_course_exists = 0,
    'ALTER TABLE behavior_events ADD CONSTRAINT fk_behavior_events_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE SET NULL',
    'SELECT ''fk_behavior_events_course already exists'''
);

PREPARE stmt FROM @add_fk_behavior_events_course_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS behavior_summary_snapshots
(
    id                   BIGINT AUTO_INCREMENT COMMENT '摘要快照ID'
        PRIMARY KEY,
    schema_version       VARCHAR(64)                          NOT NULL COMMENT '摘要schema版本（behavior_summary.v1）',
    student_id           BIGINT                               NOT NULL COMMENT '学生ID',
    course_id            BIGINT                               NULL COMMENT '课程ID（可选）',
    range_key            VARCHAR(32)                          NOT NULL COMMENT '时间窗标识（7d/custom）',
    period_from          DATETIME                             NOT NULL COMMENT '时间窗起始（包含）',
    period_to            DATETIME                             NOT NULL COMMENT '时间窗结束（不包含）',
    input_event_count    INT                                  NOT NULL COMMENT '输入事件数量',
    event_types_included JSON                                 NULL COMMENT '包含的事件类型code列表(JSON数组)',
    summary_json         LONGTEXT                             NOT NULL COMMENT '阶段一摘要JSON（纯代码生成）',
    generated_at         DATETIME                             NOT NULL COMMENT '生成时间',
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL COMMENT '入库时间',
    KEY idx_behavior_snapshots_student_range_period (student_id, range_key, period_from, period_to, generated_at),
    KEY idx_behavior_snapshots_course (course_id, generated_at)
)
    COMMENT '行为摘要快照表（阶段一产物，禁止AI）' CHARSET = utf8mb4;

SET @fk_behavior_snapshots_student_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_summary_snapshots'
      AND CONSTRAINT_NAME = 'fk_behavior_snapshots_student'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_snapshots_student_sql := IF(
    @fk_behavior_snapshots_student_exists = 0,
    'ALTER TABLE behavior_summary_snapshots ADD CONSTRAINT fk_behavior_snapshots_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE',
    'SELECT ''fk_behavior_snapshots_student already exists'''
);

PREPARE stmt FROM @add_fk_behavior_snapshots_student_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_behavior_snapshots_course_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_summary_snapshots'
      AND CONSTRAINT_NAME = 'fk_behavior_snapshots_course'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_snapshots_course_sql := IF(
    @fk_behavior_snapshots_course_exists = 0,
    'ALTER TABLE behavior_summary_snapshots ADD CONSTRAINT fk_behavior_snapshots_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE SET NULL',
    'SELECT ''fk_behavior_snapshots_course already exists'''
);

PREPARE stmt FROM @add_fk_behavior_snapshots_course_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS behavior_insights
(
    id             BIGINT AUTO_INCREMENT COMMENT '洞察ID'
        PRIMARY KEY,
    schema_version VARCHAR(64)                          NOT NULL COMMENT '洞察schema版本（behavior_insight.v1）',
    snapshot_id    BIGINT                               NOT NULL COMMENT '关联摘要快照ID',
    student_id     BIGINT                               NOT NULL COMMENT '学生ID',
    course_id      BIGINT                               NULL COMMENT '课程ID（可选）',
    model          VARCHAR(128)                         NULL COMMENT '模型标识（审计用）',
    prompt_version VARCHAR(64)                          NULL COMMENT 'prompt版本（审计用）',
    status         VARCHAR(16)                          NOT NULL COMMENT '生成状态(success/failed/partial)',
    insight_json   LONGTEXT                             NOT NULL COMMENT '阶段二AI输出JSON（不得含新分数）',
    error_message  VARCHAR(500)                         NULL COMMENT '失败原因（可选）',
    generated_at   DATETIME                             NOT NULL COMMENT '生成时间',
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL COMMENT '入库时间',
    KEY idx_behavior_insights_snapshot (snapshot_id, generated_at),
    KEY idx_behavior_insights_student_course (student_id, course_id, generated_at)
)
    COMMENT '行为洞察表（阶段二AI解读结果，仅解释总结）' CHARSET = utf8mb4;

SET @fk_behavior_insights_snapshot_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_insights'
      AND CONSTRAINT_NAME = 'fk_behavior_insights_snapshot'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_insights_snapshot_sql := IF(
    @fk_behavior_insights_snapshot_exists = 0,
    'ALTER TABLE behavior_insights ADD CONSTRAINT fk_behavior_insights_snapshot FOREIGN KEY (snapshot_id) REFERENCES behavior_summary_snapshots (id) ON DELETE CASCADE',
    'SELECT ''fk_behavior_insights_snapshot already exists'''
);

PREPARE stmt FROM @add_fk_behavior_insights_snapshot_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_behavior_insights_student_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_insights'
      AND CONSTRAINT_NAME = 'fk_behavior_insights_student'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_insights_student_sql := IF(
    @fk_behavior_insights_student_exists = 0,
    'ALTER TABLE behavior_insights ADD CONSTRAINT fk_behavior_insights_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE',
    'SELECT ''fk_behavior_insights_student already exists'''
);

PREPARE stmt FROM @add_fk_behavior_insights_student_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_behavior_insights_course_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'behavior_insights'
      AND CONSTRAINT_NAME = 'fk_behavior_insights_course'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_behavior_insights_course_sql := IF(
    @fk_behavior_insights_course_exists = 0,
    'ALTER TABLE behavior_insights ADD CONSTRAINT fk_behavior_insights_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE SET NULL',
    'SELECT ''fk_behavior_insights_course already exists'''
);

PREPARE stmt FROM @add_fk_behavior_insights_course_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS ai_voice_sessions
(
    id         BIGINT AUTO_INCREMENT COMMENT '语音会话ID'
        PRIMARY KEY,
    user_id    BIGINT                               NOT NULL COMMENT '用户ID',
    title      VARCHAR(255)                         NULL COMMENT '会话标题（可选）',
    model      VARCHAR(128)                         NULL COMMENT '模型（审计用）',
    mode       VARCHAR(16)                          NULL COMMENT '模式(text/audio/both)',
    locale     VARCHAR(32)                          NULL COMMENT '语言偏好（en-US/zh-CN）',
    scenario   VARCHAR(255)                         NULL COMMENT '训练场景（可选）',
    pinned     TINYINT(1) DEFAULT 0                 NOT NULL COMMENT '是否置顶',
    deleted    TINYINT(1) DEFAULT 0                 NOT NULL COMMENT '是否删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_ai_voice_sessions_user_created (user_id, created_at),
    KEY idx_ai_voice_sessions_user_pinned_updated (user_id, pinned, updated_at)
)
    COMMENT 'AI 口语训练会话表' CHARSET = utf8mb4;

SET @fk_ai_voice_sessions_user_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_voice_sessions'
      AND CONSTRAINT_NAME = 'fk_ai_voice_sessions_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_ai_voice_sessions_user_sql := IF(
    @fk_ai_voice_sessions_user_exists = 0,
    'ALTER TABLE ai_voice_sessions ADD CONSTRAINT fk_ai_voice_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE',
    'SELECT ''fk_ai_voice_sessions_user already exists'''
);

PREPARE stmt FROM @add_fk_ai_voice_sessions_user_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS ai_voice_turns
(
    id                      BIGINT AUTO_INCREMENT COMMENT '语音回合ID'
        PRIMARY KEY,
    session_id              BIGINT                               NOT NULL COMMENT '语音会话ID',
    user_id                 BIGINT                               NOT NULL COMMENT '用户ID',
    user_transcript         TEXT                                 NULL COMMENT '用户转写文本',
    assistant_text          TEXT                                 NULL COMMENT 'AI 回复文本/转写',
    user_audio_file_id      BIGINT                               NULL COMMENT '用户音频文件ID',
    assistant_audio_file_id BIGINT                               NULL COMMENT 'AI 音频文件ID',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL COMMENT '创建时间',
    KEY idx_ai_voice_turns_session_created (session_id, created_at),
    KEY idx_ai_voice_turns_user_created (user_id, created_at)
)
    COMMENT 'AI 口语训练回合表' CHARSET = utf8mb4;

SET @fk_ai_voice_turns_session_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_voice_turns'
      AND CONSTRAINT_NAME = 'fk_ai_voice_turns_session'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_ai_voice_turns_session_sql := IF(
    @fk_ai_voice_turns_session_exists = 0,
    'ALTER TABLE ai_voice_turns ADD CONSTRAINT fk_ai_voice_turns_session FOREIGN KEY (session_id) REFERENCES ai_voice_sessions (id) ON DELETE CASCADE',
    'SELECT ''fk_ai_voice_turns_session already exists'''
);

PREPARE stmt FROM @add_fk_ai_voice_turns_session_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_ai_voice_turns_user_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_voice_turns'
      AND CONSTRAINT_NAME = 'fk_ai_voice_turns_user'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);

SET @add_fk_ai_voice_turns_user_sql := IF(
    @fk_ai_voice_turns_user_exists = 0,
    'ALTER TABLE ai_voice_turns ADD CONSTRAINT fk_ai_voice_turns_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE',
    'SELECT ''fk_ai_voice_turns_user already exists'''
);

PREPARE stmt FROM @add_fk_ai_voice_turns_user_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =========================
-- 4) 历史数据回填（可选）
--    目的：将历史 AI 能力报告回填到 ability_assessments / student_abilities。
--    若不需要回填，可跳过本节。
-- =========================

DROP TEMPORARY TABLE IF EXISTS tmp_backfill_ability_seed;

CREATE TEMPORARY TABLE tmp_backfill_ability_seed AS
SELECT
    grade_ctx.student_id,
    dim.id AS dimension_id,
    grade_ctx.assignment_id AS related_id,
    latest_report.id AS report_id,
    COALESCE(grade_ctx.grade_published_at, latest_report.published_at, latest_report.created_at, NOW()) AS assessed_at,
    CAST(
        CASE dim.code
            WHEN 'MORAL_COGNITION' THEN COALESCE(
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.moral_reasoning'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.MORAL_COGNITION'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.道德认知'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.overall.dimension_averages.moral_reasoning'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.dimension_averages.moral_reasoning'), NULL)), 'null')
            )
            WHEN 'LEARNING_ATTITUDE' THEN COALESCE(
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.attitude'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.LEARNING_ATTITUDE'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.学习态度'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.overall.dimension_averages.attitude'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.dimension_averages.attitude'), NULL)), 'null')
            )
            WHEN 'LEARNING_ABILITY' THEN COALESCE(
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.ability'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.LEARNING_ABILITY'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.学习能力'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.overall.dimension_averages.ability'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.dimension_averages.ability'), NULL)), 'null')
            )
            WHEN 'LEARNING_METHOD' THEN COALESCE(
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.strategy'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.LEARNING_METHOD'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.dimension_scores), JSON_EXTRACT(latest_report.dimension_scores, '$.学习方法'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.overall.dimension_averages.strategy'), NULL)), 'null'),
                NULLIF(JSON_UNQUOTE(IF(JSON_VALID(latest_report.trends_analysis), JSON_EXTRACT(latest_report.trends_analysis, '$.dimension_averages.strategy'), NULL)), 'null')
            )
            ELSE NULL
        END AS DECIMAL(5, 2)
    ) AS score
FROM (
    SELECT
        g.student_id,
        g.assignment_id,
        MAX(COALESCE(g.published_at, g.updated_at, g.created_at)) AS grade_published_at
    FROM grades g
    WHERE g.status = 'published'
      AND COALESCE(g.deleted, 0) = 0
      AND g.student_id IS NOT NULL
      AND g.assignment_id IS NOT NULL
    GROUP BY g.student_id, g.assignment_id
) AS grade_ctx
INNER JOIN (
    SELECT
        r1.id,
        r1.student_id,
        r1.assignment_id,
        r1.dimension_scores,
        r1.trends_analysis,
        r1.published_at,
        r1.created_at
    FROM ability_reports r1
    WHERE r1.report_type = 'ai'
      AND r1.student_id IS NOT NULL
      AND r1.assignment_id IS NOT NULL
      AND NOT EXISTS (
        SELECT 1
        FROM ability_reports r2
        WHERE r2.report_type = 'ai'
          AND r2.student_id = r1.student_id
          AND r2.assignment_id = r1.assignment_id
          AND (
            COALESCE(r2.created_at, '1970-01-01 00:00:00') > COALESCE(r1.created_at, '1970-01-01 00:00:00')
            OR (
              COALESCE(r2.created_at, '1970-01-01 00:00:00') = COALESCE(r1.created_at, '1970-01-01 00:00:00')
              AND r2.id > r1.id
            )
          )
      )
) AS latest_report
    ON latest_report.student_id = grade_ctx.student_id
   AND latest_report.assignment_id = grade_ctx.assignment_id
INNER JOIN ability_dimensions dim
    ON dim.code IN ('MORAL_COGNITION', 'LEARNING_ATTITUDE', 'LEARNING_ABILITY', 'LEARNING_METHOD')
   AND COALESCE(dim.is_active, 1) = 1;

DELETE FROM tmp_backfill_ability_seed
WHERE score IS NULL
   OR score <= 0;

UPDATE ability_assessments a
INNER JOIN tmp_backfill_ability_seed seed
    ON a.student_id = seed.student_id
   AND a.dimension_id = seed.dimension_id
   AND a.assessment_type = 'assignment'
   AND a.related_id = seed.related_id
   AND a.deleted = 0
SET a.score = seed.score,
    a.max_score = 5.00,
    a.confidence = 0.90,
    a.evidence = CONCAT('历史 AI 报告回填（按已发布成绩取最新report），reportId=', seed.report_id),
    a.status = 'completed',
    a.assessed_at = seed.assessed_at,
    a.updated_at = NOW();

INSERT INTO ability_assessments (
    student_id,
    dimension_id,
    assessor_id,
    assessment_type,
    related_id,
    score,
    max_score,
    confidence,
    evidence,
    status,
    assessed_at,
    created_at,
    updated_at,
    deleted
)
SELECT
    seed.student_id,
    seed.dimension_id,
    NULL,
    'assignment',
    seed.related_id,
    seed.score,
    5.00,
    0.90,
    CONCAT('历史 AI 报告回填（按已发布成绩取最新report），reportId=', seed.report_id),
    'completed',
    seed.assessed_at,
    NOW(),
    NOW(),
    0
FROM tmp_backfill_ability_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM ability_assessments a
    WHERE a.student_id = seed.student_id
      AND a.dimension_id = seed.dimension_id
      AND a.assessment_type = 'assignment'
      AND a.related_id = seed.related_id
      AND a.deleted = 0
);

DROP TEMPORARY TABLE IF EXISTS tmp_backfill_student_ability_snapshot;

CREATE TEMPORARY TABLE tmp_backfill_student_ability_snapshot AS
SELECT
    latest.student_id,
    latest.dimension_id,
    latest.score,
    latest.assessed_at,
    counts.assessment_count
FROM (
    SELECT
        a.student_id,
        a.dimension_id,
        a.score,
        a.assessed_at,
        a.id
    FROM ability_assessments a
    WHERE a.assessment_type = 'assignment'
      AND a.deleted = 0
      AND NOT EXISTS (
        SELECT 1
        FROM ability_assessments newer
        WHERE newer.student_id = a.student_id
          AND newer.dimension_id = a.dimension_id
          AND newer.assessment_type = 'assignment'
          AND newer.deleted = 0
          AND (
            COALESCE(newer.assessed_at, '1970-01-01 00:00:00') > COALESCE(a.assessed_at, '1970-01-01 00:00:00')
            OR (
              COALESCE(newer.assessed_at, '1970-01-01 00:00:00') = COALESCE(a.assessed_at, '1970-01-01 00:00:00')
              AND newer.id > a.id
            )
          )
      )
) AS latest
INNER JOIN (
    SELECT
        student_id,
        dimension_id,
        COUNT(1) AS assessment_count
    FROM ability_assessments
    WHERE assessment_type = 'assignment'
      AND deleted = 0
    GROUP BY student_id, dimension_id
) AS counts
    ON counts.student_id = latest.student_id
   AND counts.dimension_id = latest.dimension_id;

UPDATE student_abilities sa
INNER JOIN tmp_backfill_student_ability_snapshot snap
    ON sa.student_id = snap.student_id
   AND sa.dimension_id = snap.dimension_id
SET sa.current_score = snap.score,
    sa.level = CASE
        WHEN snap.score >= 4.50 THEN 'expert'
        WHEN snap.score >= 3.75 THEN 'advanced'
        WHEN snap.score >= 3.00 THEN 'intermediate'
        ELSE 'beginner'
    END,
    sa.last_assessment_at = snap.assessed_at,
    sa.assessment_count = snap.assessment_count,
    sa.trend = COALESCE(sa.trend, 'stable'),
    sa.updated_at = NOW();

INSERT INTO student_abilities (
    student_id,
    dimension_id,
    current_score,
    level,
    last_assessment_at,
    assessment_count,
    trend,
    created_at,
    updated_at
)
SELECT
    snap.student_id,
    snap.dimension_id,
    snap.score,
    CASE
        WHEN snap.score >= 4.50 THEN 'expert'
        WHEN snap.score >= 3.75 THEN 'advanced'
        WHEN snap.score >= 3.00 THEN 'intermediate'
        ELSE 'beginner'
    END,
    snap.assessed_at,
    snap.assessment_count,
    'stable',
    NOW(),
    NOW()
FROM tmp_backfill_student_ability_snapshot snap
WHERE NOT EXISTS (
    SELECT 1
    FROM student_abilities sa
    WHERE sa.student_id = snap.student_id
      AND sa.dimension_id = snap.dimension_id
);

DROP TEMPORARY TABLE IF EXISTS tmp_backfill_student_ability_snapshot;
DROP TEMPORARY TABLE IF EXISTS tmp_backfill_ability_seed;

-- =========================
-- 5) 去重并添加唯一索引（建议在回填后执行）
-- =========================

DELETE a1
FROM ability_assessments a1
JOIN ability_assessments a2
  ON a1.student_id = a2.student_id
 AND a1.dimension_id = a2.dimension_id
 AND a1.assessment_type = a2.assessment_type
 AND COALESCE(a1.related_id, -1) = COALESCE(a2.related_id, -1)
 AND a1.id < a2.id
WHERE a1.deleted = 0
  AND a2.deleted = 0;

SET @uk_exists := (
    SELECT COUNT(1)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ability_assessments'
      AND INDEX_NAME = 'uk_student_dimension_related'
);

SET @add_uk_sql := IF(
    @uk_exists = 0,
    'ALTER TABLE ability_assessments ADD UNIQUE KEY uk_student_dimension_related (student_id, dimension_id, assessment_type, related_id)',
    'SELECT ''uk_student_dimension_related already exists'''
);

PREPARE stmt FROM @add_uk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
