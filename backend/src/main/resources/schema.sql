create table if not exists student_assessment_system.abilities
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

create table if not exists student_assessment_system.ability_assessments
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
    deleted         tinyint(1)                                                       default 0                 not null
)
    comment '能力评估记录表' charset = utf8mb4;

create index idx_assessed_at
    on student_assessment_system.ability_assessments (assessed_at);

create index idx_assessment_type
    on student_assessment_system.ability_assessments (assessment_type);

create index idx_assessor_id
    on student_assessment_system.ability_assessments (assessor_id);

create index idx_dimension_id
    on student_assessment_system.ability_assessments (dimension_id);

create index idx_related
    on student_assessment_system.ability_assessments (related_id);

create index idx_student_id
    on student_assessment_system.ability_assessments (student_id);

create table if not exists student_assessment_system.ability_dimensions
(
    id          bigint auto_increment comment '维度ID'
        primary key,
    name        varchar(100)                            not null comment '维度名称',
    code        varchar(50)                             not null comment '维度编码（如：MORAL_COGNITION）',
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
    comment '能力维度表' charset = utf8mb4;

alter table student_assessment_system.ability_assessments
    add constraint ability_assessments_ibfk_2
        foreign key (dimension_id) references student_assessment_system.ability_dimensions (id)
            on delete cascade;

create index idx_category
    on student_assessment_system.ability_dimensions (category);

create index idx_is_active
    on student_assessment_system.ability_dimensions (is_active);

create index idx_sort_order
    on student_assessment_system.ability_dimensions (sort_order);

alter table student_assessment_system.ability_dimensions
    add constraint uk_code
        unique (code);

create table if not exists student_assessment_system.ability_goals
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
    updated_at    timestamp                                          default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '能力发展目标表' charset = utf8mb4;

create index idx_dimension_id
    on student_assessment_system.ability_goals (dimension_id);

create index idx_status
    on student_assessment_system.ability_goals (status);

create index idx_student_id
    on student_assessment_system.ability_goals (student_id);

create index idx_target_date
    on student_assessment_system.ability_goals (target_date);

alter table student_assessment_system.ability_goals
    add constraint ability_goals_ibfk_2
        foreign key (dimension_id) references student_assessment_system.ability_dimensions (id)
            on delete cascade;

create table if not exists student_assessment_system.ability_reports
(
    id                  bigint auto_increment comment '报告ID'
        primary key,
    student_id          bigint                                                                           not null comment '学生ID',
    report_type         enum ('monthly', 'semester', 'annual', 'custom', 'ai') default 'monthly'         null comment '报告类型',
    title               varchar(200)                                                                     not null comment '报告标题',
    overall_score       decimal(5, 2)                                                                    not null comment '综合得分',
    report_period_start date                                                                             not null comment '报告周期开始',
    report_period_end   date                                                                             not null comment '报告周期结束',
    dimension_scores    json                                                                             null comment '各维度得分（JSON格式）',
    trends_analysis     text                                                                             null comment '趋势分析',
    achievements        json                                                                             null comment '成就列表（JSON格式）',
    improvement_areas   json                                                                             null comment '待改进领域（JSON格式）',
    recommendations     text                                                                             null comment '综合建议',
    generated_by        enum ('system', 'teacher')                             default 'system'          null comment '生成方式',
    is_published        tinyint(1)                                             default 0                 null comment '是否发布',
    created_at          timestamp                                              default CURRENT_TIMESTAMP null comment '创建时间',
    published_at        timestamp                                                                        null comment '发布时间',
    updated_at          datetime                                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    course_id           bigint                                                                           null comment '课程ID',
    assignment_id       bigint                                                                           null comment '作业ID',
    submission_id       bigint                                                                           null comment '提交ID',
    ai_history_id       bigint                                                                           null comment 'AI批改历史ID'
)
    comment '能力报告表' charset = utf8mb4;

create index idx_ability_report_aihist
    on student_assessment_system.ability_reports (ai_history_id);

create index idx_ability_report_ctx
    on student_assessment_system.ability_reports (student_id, course_id, assignment_id, submission_id);

create index idx_created_at
    on student_assessment_system.ability_reports (created_at);

create index idx_period
    on student_assessment_system.ability_reports (report_period_start, report_period_end);

create index idx_report_type
    on student_assessment_system.ability_reports (report_type);

create index idx_student_id
    on student_assessment_system.ability_reports (student_id);

create table if not exists student_assessment_system.ai_conversations
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
    updated_at      timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'AI 会话表（用户私有）' charset = utf8mb4;

create index idx_archived
    on student_assessment_system.ai_conversations (archived);

create index idx_last_message_at
    on student_assessment_system.ai_conversations (last_message_at);

create index idx_pinned
    on student_assessment_system.ai_conversations (pinned);

create index idx_user_id
    on student_assessment_system.ai_conversations (user_id);

create table if not exists student_assessment_system.ai_grading_history
(
    id          bigint auto_increment
        primary key,
    teacher_id  bigint       not null,
    file_id     bigint       null,
    file_name   varchar(255) null,
    model       varchar(128) null,
    final_score double       null,
    raw_json    longtext     not null,
    created_at  datetime     not null
)
    charset = utf8mb4;

create index idx_ai_grading_history_teacher
    on student_assessment_system.ai_grading_history (teacher_id, created_at);

create table if not exists student_assessment_system.ai_memories
(
    id         bigint auto_increment comment '主键'
        primary key,
    user_id    bigint                               not null comment '用户ID',
    enabled    tinyint(1) default 1                 not null comment '是否启用记忆',
    content    text                                 null comment '记忆内容',
    updated_at timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'AI 用户长期记忆' charset = utf8mb4;

alter table student_assessment_system.ai_memories
    add constraint uniq_user_id
        unique (user_id);

create table if not exists student_assessment_system.ai_messages
(
    id              bigint auto_increment comment '消息ID'
        primary key,
    conversation_id bigint                              not null comment '会话ID',
    user_id         bigint                              not null comment '所属用户ID（冗余，用于权限隔离）',
    role            varchar(20)                         not null comment '角色：user/assistant/system',
    content         text                                not null comment '消息文本内容',
    attachments     json                                null comment '附件文件ID数组（如启用多模态）',
    created_at      timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment 'AI 会话消息表' charset = utf8mb4;

create index idx_conv
    on student_assessment_system.ai_messages (conversation_id);

create index idx_user_conv
    on student_assessment_system.ai_messages (user_id, conversation_id);

alter table student_assessment_system.ai_messages
    add constraint fk_ai_messages_conversation
        foreign key (conversation_id) references student_assessment_system.ai_conversations (id)
            on delete cascade;

create table if not exists student_assessment_system.analytics_cache
(
    cache_key    varchar(255) not null comment '缓存主键',
    cache_value  text         null comment '缓存内容',
    generated_at datetime     null comment '生成时间'
)
    comment '分析缓存表' charset = utf8mb4;

alter table student_assessment_system.analytics_cache
    add primary key (cache_key);

create table if not exists student_assessment_system.assignments
(
    id               bigint auto_increment comment '作业ID'
        primary key,
    course_id        bigint                                                                       not null comment '课程ID',
    teacher_id       bigint                                                                       not null comment '发布教师ID',
    title            varchar(200)                                                                 not null comment '作业标题',
    description      text                                                                         null comment '作业描述',
    requirements     text                                                                         null comment '作业要求',
    max_score        decimal(5, 2)                                      default 100.00            null comment '总分',
    due_date         timestamp                                                                    null comment '截止时间',
    allow_late       tinyint(1)                                         default 0                 null comment '是否允许迟交',
    max_attempts     int                                                default 1                 null comment '最大提交次数',
    file_types       varchar(200)                                                                 null comment '允许的文件类型',
    max_file_size    int                                                default 10                null comment '最大文件大小（MB）',
    status           enum ('draft', 'scheduled', 'published', 'closed') default 'draft'           not null comment '作业状态',
    publish_at       timestamp                                                                    null comment '定时发布时间（为空表示不定时）',
    assignment_type  enum ('normal', 'course_bound')                    default 'normal'          not null comment '作业类型',
    created_at       timestamp                                          default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       timestamp                                          default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted          tinyint(1)                                         default 0                 null comment '是否删除',
    submission_count int                                                default 0                 null comment '提交数量',
    lesson_id        bigint                                                                       null comment '绑定的节次ID（仅course_bound可用)'
)
    comment '作业表' charset = utf8mb4;

create index idx_assignment_course_status
    on student_assessment_system.assignments (course_id, status);

create index idx_assignment_due_date
    on student_assessment_system.assignments (due_date);

create index idx_assignment_type
    on student_assessment_system.assignments (assignment_type);

create index idx_assignments_course_status
    on student_assessment_system.assignments (course_id, status);

create index idx_assignments_lesson_id
    on student_assessment_system.assignments (lesson_id);

create index idx_course_id
    on student_assessment_system.assignments (course_id);

create index idx_deleted
    on student_assessment_system.assignments (deleted);

create index idx_due_date
    on student_assessment_system.assignments (due_date);

create index idx_lesson_id
    on student_assessment_system.assignments (lesson_id);

create index idx_publish_at
    on student_assessment_system.assignments (publish_at);

create index idx_status
    on student_assessment_system.assignments (status);

create index idx_teacher_id
    on student_assessment_system.assignments (teacher_id);

create table if not exists student_assessment_system.chapters
(
    id          bigint auto_increment
        primary key,
    course_id   bigint                               not null,
    title       varchar(255)                         not null,
    description text                                 null,
    order_index int        default 0                 null,
    deleted     tinyint(1) default 0                 null,
    created_at  datetime   default CURRENT_TIMESTAMP null,
    updated_at  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    charset = utf8mb4;

create index idx_chapters_course_id
    on student_assessment_system.chapters (course_id);

create table if not exists student_assessment_system.chat_conversation_members
(
    id                   bigint auto_increment comment '主键'
        primary key,
    conversation_id      bigint                               not null comment '会话ID',
    user_id              bigint                               not null comment '成员用户ID',
    unread_count         int        default 0                 not null comment '未读数',
    pinned               tinyint(1) default 0                 not null comment '是否置顶',
    archived             tinyint(1) default 0                 not null comment '是否归档',
    last_read_message_id bigint                               null comment '最后已读消息ID',
    last_read_at         datetime                             null comment '最后已读时间',
    created_at           datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天会话成员表' charset = utf8mb4;

create index idx_member_conv
    on student_assessment_system.chat_conversation_members (conversation_id);

create index idx_member_user
    on student_assessment_system.chat_conversation_members (user_id, pinned, archived);

alter table student_assessment_system.chat_conversation_members
    add constraint uk_conv_member
        unique (conversation_id, user_id);

create table if not exists student_assessment_system.chat_conversations
(
    id              bigint auto_increment comment '会话ID'
        primary key,
    type            varchar(20) default 'direct'          not null comment '会话类型：direct(点对点)；预留群聊',
    peer_a_id       bigint                                not null comment '成员A（较小的用户ID）',
    peer_b_id       bigint                                not null comment '成员B（较大的用户ID）',
    course_id       bigint      default 0                 not null comment '课程ID（0表示无课程上下文）',
    last_message_id bigint                                null comment '最后一条消息ID（notifications.id）',
    last_message_at datetime                              null comment '最后一条消息时间',
    created_at      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天会话表' charset = utf8mb4;

alter table student_assessment_system.chat_conversation_members
    add constraint fk_members_conv
        foreign key (conversation_id) references student_assessment_system.chat_conversations (id)
            on delete cascade;

create index idx_conv_last
    on student_assessment_system.chat_conversations (last_message_at);

create index idx_conv_peers
    on student_assessment_system.chat_conversations (peer_a_id, peer_b_id);

alter table student_assessment_system.chat_conversations
    add constraint uk_conv_direct
        unique (peer_a_id, peer_b_id, course_id);

create table if not exists student_assessment_system.chat_message_attachments
(
    id              bigint auto_increment comment '主键'
        primary key,
    notification_id bigint                             not null comment '消息ID（notifications.id）',
    file_id         bigint                             not null comment '文件ID（file_records.id）',
    uploader_id     bigint                             not null comment '上传者用户ID',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '聊天消息附件表' charset = utf8mb4;

create index idx_file
    on student_assessment_system.chat_message_attachments (file_id);

alter table student_assessment_system.chat_message_attachments
    add constraint uk_msg_file
        unique (notification_id, file_id);

create table if not exists student_assessment_system.comment_likes
(
    id         bigint auto_increment comment '点赞ID'
        primary key,
    comment_id bigint                              not null comment '评论ID',
    user_id    bigint                              not null comment '用户ID',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '评论点赞表' charset = utf8mb4;

create index idx_comment_id
    on student_assessment_system.comment_likes (comment_id);

create index idx_user_id
    on student_assessment_system.comment_likes (user_id);

alter table student_assessment_system.comment_likes
    add constraint uk_comment_user
        unique (comment_id, user_id);

create table if not exists student_assessment_system.course_ability_weights
(
    id             bigint auto_increment comment '主键'
        primary key,
    course_id      bigint                                  not null comment '课程ID',
    dimension_code varchar(50)                             not null comment '维度编码：MORAL_COGNITION/LEARNING_ATTITUDE/LEARNING_ABILITY/LEARNING_METHOD/ACADEMIC_GRADE',
    weight         decimal(5, 2) default 1.00              not null comment '权重',
    updated_at     timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '课程能力权重表' charset = utf8mb4;

create index idx_course_id
    on student_assessment_system.course_ability_weights (course_id);

alter table student_assessment_system.course_ability_weights
    add constraint uk_course_dimension
        unique (course_id, dimension_code);

create table if not exists student_assessment_system.courses
(
    id                 bigint auto_increment comment '课程ID'
        primary key,
    title              varchar(200)                                                            not null comment '课程标题',
    description        text                                                                    null comment '课程描述',
    content            text                                                                    null,
    cover_image        varchar(500)                                                            null comment '课程封面图',
    teacher_id         bigint                                                                  not null comment '授课教师ID',
    difficulty         enum ('beginner', 'intermediate', 'advanced') default 'beginner'        null comment '课程难度',
    duration           int                                                                     null comment '课程时长（分钟）',
    max_students       int                                           default 50                null comment '最大学生数',
    price              decimal(10, 2)                                default 0.00              null comment '课程价格',
    status             enum ('draft', 'published', 'archived')       default 'draft'           null comment '课程状态',
    start_date         date                                                                    null comment '开课日期',
    end_date           date                                                                    null comment '结课日期',
    tags               varchar(500)                                                            null comment '课程标签（JSON格式）',
    objectives         text                                                                    null comment '学习目标',
    requirements       text                                                                    null comment '先修要求',
    require_enroll_key tinyint(1)                                    default 0                 not null comment '是否需要入课密钥',
    enroll_key_hash    varchar(255)                                                            null comment '入课密钥哈希',
    created_at         timestamp                                     default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at         timestamp                                     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted            tinyint(1)                                    default 0                 null comment '是否删除',
    enrollment_count   int                                           default 0                 null comment '选课人数',
    rating             decimal(2, 1)                                 default 0.0               null comment '课程评分',
    review_count       int                                           default 0                 null comment '评价数量',
    category           varchar(100)                                                            null comment '课程分类'
)
    comment '课程表' charset = utf8mb4;

alter table student_assessment_system.assignments
    add constraint assignments_ibfk_1
        foreign key (course_id) references student_assessment_system.courses (id)
            on delete cascade;

alter table student_assessment_system.chapters
    add constraint fk_chapters_course
        foreign key (course_id) references student_assessment_system.courses (id)
            on delete cascade;

alter table student_assessment_system.course_ability_weights
    add constraint fk_course_weights_course
        foreign key (course_id) references student_assessment_system.courses (id)
            on delete cascade;

create index idx_deleted
    on student_assessment_system.courses (deleted);

create index idx_start_date
    on student_assessment_system.courses (start_date);

create index idx_status
    on student_assessment_system.courses (status);

create index idx_teacher_id
    on student_assessment_system.courses (teacher_id);

create table if not exists student_assessment_system.enrollments
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
    last_access_time datetime                                                          null comment '最后一次访问课程的时间'
)
    comment '选课关系表' charset = utf8mb4;

create index idx_course_id
    on student_assessment_system.enrollments (course_id);

create index idx_enrollment_course_student
    on student_assessment_system.enrollments (course_id, student_id);

create index idx_enrollment_student_course
    on student_assessment_system.enrollments (student_id, course_id);

create index idx_enrollments_student_course_status
    on student_assessment_system.enrollments (student_id, course_id, status);

create index idx_status
    on student_assessment_system.enrollments (status);

create index idx_student_id
    on student_assessment_system.enrollments (student_id);

alter table student_assessment_system.enrollments
    add constraint uk_student_course
        unique (student_id, course_id);

alter table student_assessment_system.enrollments
    add constraint enrollments_ibfk_2
        foreign key (course_id) references student_assessment_system.courses (id)
            on delete cascade;

create table if not exists student_assessment_system.file_records
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
    created_at     timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '文件记录表' charset = utf8mb4;

alter table student_assessment_system.chat_message_attachments
    add constraint fk_cma_file
        foreign key (file_id) references student_assessment_system.file_records (id)
            on delete cascade;

create index idx_file_type
    on student_assessment_system.file_records (file_type);

create index idx_related
    on student_assessment_system.file_records (related_type, related_id);

create index idx_uploader_id
    on student_assessment_system.file_records (uploader_id);

create table if not exists student_assessment_system.grades
(
    id             bigint auto_increment comment '成绩ID'
        primary key,
    student_id     bigint                                                            not null comment '学生ID',
    assignment_id  bigint                                                            not null comment '作业ID',
    score          decimal(5, 2)                                                     not null comment '得分',
    max_score      decimal(5, 2)                                                     not null comment '满分',
    percentage     decimal(5, 2)                                                     null comment '百分比分数',
    grade_level    varchar(2)                                                        null comment '等级(A,B,C,D,F)',
    feedback       text                                                              null comment '评语',
    strengths      text                                                              null,
    improvements   text                                                              null,
    allow_resubmit tinyint(1)                              default 0                 not null,
    rubric_json    json                                                              null,
    regrade_reason text                                                              null,
    status         enum ('draft', 'published', 'returned') default 'draft'           not null,
    published_at   timestamp                                                         null,
    deleted        tinyint(1)                              default 0                 null comment '是否删除',
    created_at     timestamp                               default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     timestamp                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    submission_id  bigint                                                            null comment '提交ID',
    grader_id      bigint                                                            null comment '评分人ID',
    resubmit_until timestamp                                                         null
)
    comment '成绩表' charset = utf8mb4;

create index idx_assignment_id
    on student_assessment_system.grades (assignment_id);

create index idx_grade_assignment
    on student_assessment_system.grades (assignment_id);

create index idx_grade_status
    on student_assessment_system.grades (status);

create index idx_grade_student_created
    on student_assessment_system.grades (student_id, created_at);

create index idx_grades_created_at
    on student_assessment_system.grades (created_at);

create index idx_score
    on student_assessment_system.grades (score);

create index idx_status
    on student_assessment_system.grades (status);

create index idx_student_id
    on student_assessment_system.grades (student_id);

alter table student_assessment_system.grades
    add constraint grades_ibfk_2
        foreign key (assignment_id) references student_assessment_system.assignments (id)
            on delete cascade;

create table if not exists student_assessment_system.help_article
(
    id           bigint auto_increment comment '文章ID'
        primary key,
    category_id  bigint                               not null comment '分类ID',
    title        varchar(200)                         not null comment '标题',
    slug         varchar(200)                         not null comment '标识',
    content_md   mediumtext                           null comment 'Markdown内容',
    content_html mediumtext                           null comment '渲染HTML',
    tags         varchar(500)                         null comment '标签(逗号分隔)',
    views        int        default 0                 null comment '浏览数',
    up_votes     int        default 0                 null comment '有用票',
    down_votes   int        default 0                 null comment '无用票',
    published    tinyint(1) default 1                 null comment '是否发布',
    created_at   timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at   timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '帮助中心文章表' charset = utf8mb4;

create index idx_help_article_cat
    on student_assessment_system.help_article (category_id);

alter table student_assessment_system.help_article
    add constraint uk_help_article_slug
        unique (slug);

create table if not exists student_assessment_system.help_article_feedback
(
    id         bigint auto_increment comment '主键'
        primary key,
    article_id bigint                              not null comment '文章ID',
    user_id    bigint                              null comment '用户ID(可空表示匿名)',
    helpful    tinyint(1)                          null comment '是否有帮助(可空表示仅文本反馈)',
    content    text                                null comment '反馈内容',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '帮助文章反馈/投票表' charset = utf8mb4;

create index idx_help_article_feedback_article
    on student_assessment_system.help_article_feedback (article_id);

create index idx_help_article_feedback_user
    on student_assessment_system.help_article_feedback (user_id);

alter table student_assessment_system.help_article_feedback
    add constraint fk_help_feedback_article
        foreign key (article_id) references student_assessment_system.help_article (id)
            on delete cascade;

create table if not exists student_assessment_system.help_category
(
    id         bigint auto_increment comment '分类ID'
        primary key,
    name       varchar(100)                        not null comment '分类名称',
    slug       varchar(100)                        not null comment '分类标识',
    sort       int       default 0                 null comment '排序',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '帮助中心分类表' charset = utf8mb4;

alter table student_assessment_system.help_article
    add constraint fk_help_article_category
        foreign key (category_id) references student_assessment_system.help_category (id)
            on delete cascade;

alter table student_assessment_system.help_category
    add constraint uk_help_category_slug
        unique (slug);

create table if not exists student_assessment_system.help_ticket
(
    id          bigint auto_increment comment '工单ID'
        primary key,
    user_id     bigint                                                                       not null comment '提交用户ID',
    title       varchar(200)                                                                 not null comment '标题',
    description text                                                                         not null comment '描述',
    status      enum ('open', 'in_progress', 'resolved', 'closed') default 'open'            null comment '状态',
    created_at  timestamp                                          default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  timestamp                                          default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '技术支持工单表' charset = utf8mb4;

create index idx_help_ticket_status
    on student_assessment_system.help_ticket (status);

create index idx_help_ticket_user
    on student_assessment_system.help_ticket (user_id);

create table if not exists student_assessment_system.learning_recommendations
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
    expires_at          timestamp                                                                    null comment '过期时间'
)
    comment '学习建议表' charset = utf8mb4;

create index idx_dimension_id
    on student_assessment_system.learning_recommendations (dimension_id);

create index idx_is_read
    on student_assessment_system.learning_recommendations (is_read);

create index idx_priority
    on student_assessment_system.learning_recommendations (priority_score);

create index idx_student_id
    on student_assessment_system.learning_recommendations (student_id);

create index idx_type
    on student_assessment_system.learning_recommendations (recommendation_type);

alter table student_assessment_system.learning_recommendations
    add constraint learning_recommendations_ibfk_2
        foreign key (dimension_id) references student_assessment_system.ability_dimensions (id)
            on delete cascade;

create table if not exists student_assessment_system.lesson_materials
(
    lesson_id  bigint                             not null,
    file_id    bigint                             not null,
    created_at datetime default CURRENT_TIMESTAMP null
)
    comment '节次-资料 关联表' charset = utf8mb4;

create index idx_lm_file
    on student_assessment_system.lesson_materials (file_id);

alter table student_assessment_system.lesson_materials
    add primary key (lesson_id, file_id);

alter table student_assessment_system.lesson_materials
    add constraint fk_lm_file
        foreign key (file_id) references student_assessment_system.file_records (id);

create table if not exists student_assessment_system.lesson_progress
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
    study_time      int           default 0                 null
)
    comment '学习进度表' charset = utf8mb4;

create index idx_completed
    on student_assessment_system.lesson_progress (completed);

create index idx_lesson_id
    on student_assessment_system.lesson_progress (lesson_id);

create index idx_lesson_progress_last_studied
    on student_assessment_system.lesson_progress (student_id, last_studied_at);

create index idx_lesson_progress_student_course
    on student_assessment_system.lesson_progress (student_id, course_id);

create index idx_lesson_progress_student_lesson
    on student_assessment_system.lesson_progress (student_id, lesson_id);

create index idx_lp_last_studied
    on student_assessment_system.lesson_progress (student_id, last_studied_at);

create index idx_lp_student_course
    on student_assessment_system.lesson_progress (student_id, course_id);

create index idx_lp_student_lesson
    on student_assessment_system.lesson_progress (student_id, lesson_id);

create index idx_student_id
    on student_assessment_system.lesson_progress (student_id);

alter table student_assessment_system.lesson_progress
    add constraint uk_student_lesson
        unique (student_id, lesson_id);

create table if not exists student_assessment_system.lessons
(
    id                 bigint auto_increment comment '章节ID'
        primary key,
    course_id          bigint                                  not null comment '课程ID',
    title              varchar(200)                            not null comment '章节标题',
    description        text                                    null comment '章节描述',
    content            text                                    null comment '章节内容',
    video_url          varchar(500)                            null comment '视频URL',
    allow_scrubbing    tinyint(1)    default 1                 not null comment '允许拖动进度条',
    allow_speed_change tinyint(1)    default 1                 not null comment '允许倍速',
    duration           int                                     null comment '章节时长（分钟）',
    order_index        int                                     not null comment '排序索引',
    is_free            tinyint(1)    default 0                 null comment '是否免费',
    created_at         timestamp     default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at         timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted            tinyint(1)    default 0                 null comment '是否删除',
    weight             decimal(5, 2) default 1.00              not null comment '章节权重（用于课程加权进度）',
    chapter_id         bigint                                  null
)
    comment '课程章节表' charset = utf8mb4;

alter table student_assessment_system.assignments
    add constraint assignments_ibfk_3
        foreign key (lesson_id) references student_assessment_system.lessons (id)
            on delete set null;

alter table student_assessment_system.assignments
    add constraint fk_assignments_lesson
        foreign key (lesson_id) references student_assessment_system.lessons (id)
            on delete set null;

alter table student_assessment_system.lesson_materials
    add constraint fk_lm_lesson
        foreign key (lesson_id) references student_assessment_system.lessons (id);

alter table student_assessment_system.lesson_progress
    add constraint lesson_progress_ibfk_2
        foreign key (lesson_id) references student_assessment_system.lessons (id)
            on delete cascade;

create index idx_course_id
    on student_assessment_system.lessons (course_id);

create index idx_deleted
    on student_assessment_system.lessons (deleted);

create index idx_lessons_chapter_id
    on student_assessment_system.lessons (chapter_id);

create index idx_order_index
    on student_assessment_system.lessons (order_index);

alter table student_assessment_system.lessons
    add constraint fk_lessons_chapter
        foreign key (chapter_id) references student_assessment_system.chapters (id)
            on delete set null;

alter table student_assessment_system.lessons
    add constraint lessons_ibfk_1
        foreign key (course_id) references student_assessment_system.courses (id)
            on delete cascade;

create table if not exists student_assessment_system.notifications
(
    id              bigint auto_increment comment '通知ID'
        primary key,
    user_id         bigint                                                                                        not null comment '用户ID',
    sender_id       bigint                                                                                        null,
    title           varchar(200)                                                                                  not null comment '通知标题',
    content         text                                                                                          null comment '通知内容',
    type            enum ('system', 'assignment', 'grade', 'course', 'message', 'post') default 'system'          not null,
    category        varchar(50)                                                                                   null comment '通知分类，如学术/系统/活动',
    priority        enum ('low', 'normal', 'high', 'urgent')                            default 'normal'          null comment '优先级',
    is_read         tinyint(1)                                                          default 0                 null comment '是否已读',
    read_at         datetime                                                                                      null comment '已读时间',
    related_type    varchar(50)                                                                                   null comment '关联类型',
    related_id      bigint                                                                                        null comment '关联ID',
    conversation_id bigint                                                                                        null,
    expired_at      timestamp                                                                                     null comment '过期时间',
    created_at      timestamp                                                           default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      datetime                                                                                      null comment '更新时间',
    deleted         tinyint(1)                                                          default 0                 null comment '软删除标志',
    data            text                                                                                          null comment '附加数据(JSON)',
    icon            varchar(50)                                                                                   null comment '通知图标',
    action_url      varchar(255)                                                                                  null comment '操作跳转URL'
)
    comment '通知表' charset = utf8mb4;

alter table student_assessment_system.chat_message_attachments
    add constraint fk_cma_notification
        foreign key (notification_id) references student_assessment_system.notifications (id)
            on delete cascade;

create index idx_created_at
    on student_assessment_system.notifications (created_at);

create index idx_is_read
    on student_assessment_system.notifications (is_read);

create index idx_notifications_conv
    on student_assessment_system.notifications (conversation_id, created_at);

create index idx_sender
    on student_assessment_system.notifications (sender_id);

create index idx_sender_id
    on student_assessment_system.notifications (sender_id);

create index idx_type
    on student_assessment_system.notifications (type);

create index idx_user_created_at
    on student_assessment_system.notifications (user_id, created_at);

create index idx_user_id
    on student_assessment_system.notifications (user_id);

create index idx_user_sender_created
    on student_assessment_system.notifications (user_id, sender_id, created_at);

create index idx_user_sender_created_at
    on student_assessment_system.notifications (user_id, sender_id, created_at);

create table if not exists student_assessment_system.post_comments
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
    deleted     tinyint(1)  default 0                 null comment '是否删除'
)
    comment '帖子评论表' charset = utf8mb4;

alter table student_assessment_system.comment_likes
    add constraint comment_likes_ibfk_1
        foreign key (comment_id) references student_assessment_system.post_comments (id)
            on delete cascade;

create index idx_author_id
    on student_assessment_system.post_comments (author_id);

create index idx_created_at
    on student_assessment_system.post_comments (created_at);

create index idx_parent_id
    on student_assessment_system.post_comments (parent_id);

create index idx_post_id
    on student_assessment_system.post_comments (post_id);

alter table student_assessment_system.post_comments
    add constraint post_comments_ibfk_3
        foreign key (parent_id) references student_assessment_system.post_comments (id)
            on delete cascade;

create table if not exists student_assessment_system.post_likes
(
    id         bigint auto_increment comment '点赞ID'
        primary key,
    post_id    bigint                              not null comment '帖子ID',
    user_id    bigint                              not null comment '用户ID',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '帖子点赞表' charset = utf8mb4;

create index idx_post_id
    on student_assessment_system.post_likes (post_id);

create index idx_user_id
    on student_assessment_system.post_likes (user_id);

alter table student_assessment_system.post_likes
    add constraint uk_post_user
        unique (post_id, user_id);

create table if not exists student_assessment_system.post_tags
(
    id         bigint auto_increment comment '关联ID'
        primary key,
    post_id    bigint                              not null comment '帖子ID',
    tag_id     bigint                              not null comment '标签ID',
    created_at timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '帖子标签关联表' charset = utf8mb4;

create index idx_post_id
    on student_assessment_system.post_tags (post_id);

create index idx_tag_id
    on student_assessment_system.post_tags (tag_id);

alter table student_assessment_system.post_tags
    add constraint uk_post_tag
        unique (post_id, tag_id);

create table if not exists student_assessment_system.posts
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
    deleted        tinyint(1)  default 0                 null comment '是否删除'
)
    comment '帖子表' charset = utf8mb4;

alter table student_assessment_system.post_comments
    add constraint post_comments_ibfk_1
        foreign key (post_id) references student_assessment_system.posts (id)
            on delete cascade;

alter table student_assessment_system.post_likes
    add constraint post_likes_ibfk_1
        foreign key (post_id) references student_assessment_system.posts (id)
            on delete cascade;

alter table student_assessment_system.post_tags
    add constraint post_tags_ibfk_1
        foreign key (post_id) references student_assessment_system.posts (id)
            on delete cascade;

create index idx_author_id
    on student_assessment_system.posts (author_id);

create index idx_category
    on student_assessment_system.posts (category);

create index idx_created_at
    on student_assessment_system.posts (created_at);

create index idx_pinned
    on student_assessment_system.posts (pinned);

create index idx_status
    on student_assessment_system.posts (status);

create table if not exists student_assessment_system.student_abilities
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
    updated_at         timestamp                                               default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '学生能力记录表' charset = utf8mb4;

create index idx_dimension_id
    on student_assessment_system.student_abilities (dimension_id);

create index idx_level
    on student_assessment_system.student_abilities (level);

create index idx_student_id
    on student_assessment_system.student_abilities (student_id);

alter table student_assessment_system.student_abilities
    add constraint uk_student_dimension
        unique (student_id, dimension_id);

alter table student_assessment_system.student_abilities
    add constraint student_abilities_ibfk_2
        foreign key (dimension_id) references student_assessment_system.ability_dimensions (id)
            on delete cascade;

create table if not exists student_assessment_system.submissions
(
    id               bigint auto_increment comment '提交ID'
        primary key,
    assignment_id    bigint                                                                                 not null comment '作业ID',
    student_id       bigint                                                                                 not null comment '学生ID',
    content          text                                                                                   null comment '作业内容',
    file_path        varchar(500)                                                                           null comment '文件路径',
    file_name        varchar(200)                                                                           null comment '文件名',
    file_size        int                                                                                    null comment '文件大小（字节）',
    submission_count int                                                          default 1                 null comment '提交次数',
    status           enum ('draft', 'submitted', 'graded', 'late', 'rejected', 'returned') default 'submitted'       not null,
    submitted_at     timestamp                                                                              null comment '提交时间',
    is_late          tinyint(1)                                                   default 0                 null comment '是否迟交',
    created_at       timestamp                                                    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       timestamp                                                    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '作业提交表' charset = utf8mb4;

create index idx_assignment_id
    on student_assessment_system.submissions (assignment_id);

create index idx_status
    on student_assessment_system.submissions (status);

create index idx_student_id
    on student_assessment_system.submissions (student_id);

create index idx_submission_assignment_student
    on student_assessment_system.submissions (assignment_id, student_id);

create index idx_submission_status
    on student_assessment_system.submissions (status);

create index idx_submission_student_submitted_at
    on student_assessment_system.submissions (student_id, submitted_at);

create index idx_submitted_at
    on student_assessment_system.submissions (submitted_at);

alter table student_assessment_system.submissions
    add constraint uk_assignment_student
        unique (assignment_id, student_id);

alter table student_assessment_system.submissions
    add constraint submissions_ibfk_1
        foreign key (assignment_id) references student_assessment_system.assignments (id)
            on delete cascade;

create table if not exists student_assessment_system.tags
(
    id          bigint auto_increment comment '标签ID'
        primary key,
    name        varchar(50)                          not null comment '标签名称',
    description varchar(200)                         null comment '标签描述',
    color       varchar(7) default '#3B82F6'         null comment '标签颜色',
    posts_count int        default 0                 null comment '使用此标签的帖子数',
    created_at  timestamp  default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '标签表' charset = utf8mb4;

alter table student_assessment_system.post_tags
    add constraint post_tags_ibfk_2
        foreign key (tag_id) references student_assessment_system.tags (id)
            on delete cascade;

create index idx_name
    on student_assessment_system.tags (name);

create index idx_posts_count
    on student_assessment_system.tags (posts_count);

alter table student_assessment_system.tags
    add constraint name
        unique (name);

create table if not exists student_assessment_system.users
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
    mbti           varchar(4)                                                     null comment 'MBTI类型(16种：ISTJ,ISFJ,INFJ,INTJ,ISTP,ISFP,INFP,INTP,ESTP,ESFP,ENFP,ENTP,ESTJ,ESFJ,ENFJ,ENTJ)',
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
    teacher_no     varchar(50)                                                    null
)
    comment '用户表' charset = utf8mb4;

alter table student_assessment_system.ability_assessments
    add constraint ability_assessments_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.ability_assessments
    add constraint ability_assessments_ibfk_3
        foreign key (assessor_id) references student_assessment_system.users (id)
            on delete set null;

alter table student_assessment_system.ability_goals
    add constraint ability_goals_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.ability_reports
    add constraint ability_reports_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.ai_conversations
    add constraint fk_ai_conversations_user
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.ai_memories
    add constraint fk_ai_memories_user
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.ai_messages
    add constraint fk_ai_messages_user
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.assignments
    add constraint assignments_ibfk_2
        foreign key (teacher_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.comment_likes
    add constraint comment_likes_ibfk_2
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.courses
    add constraint courses_ibfk_1
        foreign key (teacher_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.enrollments
    add constraint enrollments_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.file_records
    add constraint file_records_ibfk_1
        foreign key (uploader_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.grades
    add constraint grades_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.help_article_feedback
    add constraint fk_help_feedback_user
        foreign key (user_id) references student_assessment_system.users (id)
            on delete set null;

alter table student_assessment_system.help_ticket
    add constraint fk_help_ticket_user
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.learning_recommendations
    add constraint learning_recommendations_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.lesson_progress
    add constraint lesson_progress_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.notifications
    add constraint notifications_ibfk_1
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.post_comments
    add constraint post_comments_ibfk_2
        foreign key (author_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.post_likes
    add constraint post_likes_ibfk_2
        foreign key (user_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.posts
    add constraint posts_ibfk_1
        foreign key (author_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.student_abilities
    add constraint student_abilities_ibfk_1
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

alter table student_assessment_system.submissions
    add constraint submissions_ibfk_2
        foreign key (student_id) references student_assessment_system.users (id)
            on delete cascade;

create index idx_deleted
    on student_assessment_system.users (deleted);

create index idx_email
    on student_assessment_system.users (email);

create index idx_role
    on student_assessment_system.users (role);

create index idx_username
    on student_assessment_system.users (username);

alter table student_assessment_system.users
    add constraint email
        unique (email);

alter table student_assessment_system.users
    add constraint uk_student_no
        unique (student_no);

alter table student_assessment_system.users
    add constraint uk_teacher_no
        unique (teacher_no);

alter table student_assessment_system.users
    add constraint username
        unique (username);


