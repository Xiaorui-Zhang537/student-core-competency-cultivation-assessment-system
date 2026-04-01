USE student_assessment_system;

-- 1) 优先更新已存在 admin 账号密码
UPDATE users
SET password = '$2a$10$fKoNHkjprIIZ4HNJFTkrBOqq1DXbw/j2.HX53waOvNjmP1X4NVcoK',
    updated_at = NOW()
WHERE username = 'admin';

-- 2) 若 admin 不存在则插入（可重复执行）
INSERT INTO users (
    username, email, password, role, status, deleted, email_verified, created_at, updated_at
)
SELECT
    'admin',
    'admin+seed-20260303@local.test',
    '$2a$10$fKoNHkjprIIZ4HNJFTkrBOqq1DXbw/j2.HX53waOvNjmP1X4NVcoK',
    'admin',
    'active',
    0,
    1,
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

