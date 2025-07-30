-- 能力评估模块数据库表结构
-- 基于前端API设计，简化但完整的能力评估系统

-- 1. 能力维度表 (ability_dimensions)
-- 定义可评估的能力维度，如编程能力、沟通能力、创新能力等
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
-- 记录每个学生在各个维度上的当前能力水平
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
-- 记录具体的评估活动和结果
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
-- 学生设定的能力发展目标
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
-- 基于能力评估结果生成的个性化学习建议
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
-- 定期生成的综合能力分析报告
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

-- 插入默认能力维度数据
INSERT INTO ability_dimensions (name, description, category, weight, icon, color, sort_order) VALUES
('编程能力', '代码编写、调试和优化能力', 'technical', 1.0, 'code', '#10B981', 1),
('问题解决', '分析问题和找到有效解决方案的能力', 'technical', 0.9, 'puzzle', '#F59E0B', 2),
('沟通协作', '与他人有效交流和团队合作的能力', 'soft', 0.8, 'chat', '#3B82F6', 3),
('创新思维', '创造性思考和提出新想法的能力', 'creative', 0.7, 'lightbulb', '#8B5CF6', 4),
('学习能力', '快速学习新知识和技能的能力', 'academic', 0.9, 'book', '#EF4444', 5),
('领导力', '引导和激励他人的能力', 'soft', 0.6, 'crown', '#F97316', 6),
('时间管理', '有效规划和利用时间的能力', 'soft', 0.7, 'clock', '#06B6D4', 7),
('批判思维', '客观分析和评估信息的能力', 'academic', 0.8, 'shield-check', '#84CC16', 8); 