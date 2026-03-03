USE student_assessment_system;

-- 将历史 AI 能力报告中的四维结果回填到结构化表。
-- 约束：
-- 1) 只处理已经被老师发布过成绩的作业（grades.status = 'published'）；
-- 2) 同一学生同一作业只取最新一份 AI 报告；
-- 3) 保留 ability_reports 全量历史，不删除任何历史报告；
-- 4) 回填后同步刷新 student_abilities 快照。

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
