USE student_assessment_system;

-- 兼容“已经导入过旧版 schema”的场景。
-- 功能：
-- 1) 删除历史遗留的 abilities 表；
-- 2) 删除 ability_assessments 中已弃用的 ability_id 列。

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS student_assessment_system.abilities;

SET @drop_ability_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = 'student_assessment_system'
              AND TABLE_NAME = 'ability_assessments'
              AND COLUMN_NAME = 'ability_id'
        ),
        'ALTER TABLE student_assessment_system.ability_assessments DROP COLUMN ability_id',
        'SELECT ''ability_assessments.ability_id already removed'''
    )
);

PREPARE stmt FROM @drop_ability_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
