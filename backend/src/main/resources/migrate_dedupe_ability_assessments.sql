USE student_assessment_system;

SET FOREIGN_KEY_CHECKS = 0;

-- Remove duplicate ability assessment rows and keep the newest record in each logical group.
DELETE a1
FROM ability_assessments a1
JOIN ability_assessments a2
  ON a1.student_id = a2.student_id
 AND a1.dimension_id = a2.dimension_id
 AND a1.assessment_type = a2.assessment_type
 AND COALESCE(a1.related_id, -1) = COALESCE(a2.related_id, -1)
 AND a1.id < a2.id
WHERE a1.deleted = FALSE
  AND a2.deleted = FALSE;

-- Prevent the same student/dimension/context from being inserted twice again.
SET @index_exists := (
  SELECT COUNT(1)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = 'student_assessment_system'
    AND TABLE_NAME = 'ability_assessments'
    AND INDEX_NAME = 'uk_student_dimension_related'
);

SET @ddl := IF(
  @index_exists = 0,
  'ALTER TABLE student_assessment_system.ability_assessments ADD UNIQUE KEY uk_student_dimension_related (student_id, dimension_id, assessment_type, related_id)',
  'SELECT ''uk_student_dimension_related already exists'''
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
