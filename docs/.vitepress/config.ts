import { defineConfig } from 'vitepress'

const appUrl = process.env.VITE_APP_URL || 'http://stucoreai.space'

export default defineConfig({
  lang: 'zh-CN',
  title: '学生核心能力培养评估系统',
  description: '从0开始的完整上手与参考文档',
  base: '/',
  // docs/ 下存在依赖与构建产物时，必须显式排除，否则会被当作内容扫描，导致 build 极慢甚至卡住。
  srcExclude: ['**/node_modules/**', '**/.vitepress/cache/**', '**/.vitepress/dist/**'],
  themeConfig: {
    nav: [
      { text: '返回系统', link: appUrl },
      { text: '入门', link: '/quickstart-beginner' },
      { text: '架构', link: '/architecture-overview' },
      { text: '逐文件', link: '/file-walkthrough' },
      {
        text: 'API',
        items: [
          { text: '后端 API', link: '/backend/api/index' },
          { text: '前端 API', link: '/frontend/api/index' }
        ]
      },
      { text: '模型', link: '/models/index' },
      { text: '端到端', link: '/e2e-examples' },
      { text: 'Cookbook', link: '/cookbook' },
      { text: 'FAQ', link: '/faq' }
    ],
    sidebar: [
      {
        text: '上手',
        items: [
          { text: '导航', link: '/' },
          { text: '新人上手手册', link: '/README' },
          { text: '零基础快速上手', link: '/quickstart-beginner' },
          { text: '学习路线', link: '/learning-path' }
        ]
      },
      {
        text: '理解系统',
        items: [
          { text: '架构总览', link: '/architecture-overview' },
          { text: '逐文件导览', link: '/file-walkthrough' }
        ]
      },
      {
        text: '深入',
        items: [
          { text: '后端深度讲解', link: '/backend-deep-dive' },
          { text: '前端深度讲解', link: '/frontend-deep-dive' },
          { text: '组件-Store-API 交互案例', link: '/frontend-interaction-cases' },
          { text: '端到端样例', link: '/e2e-examples' },
          { text: '页面级时序图', link: '/page-sequences' },
          { text: '五维雷达与能力口径', link: '/radar-five-dimensions' }
        ]
      },
      {
        text: '前端专题',
        items: [
          { text: '前端文档索引', link: '/frontend/index' },
          { text: '帮助中心', link: '/frontend/help-center' },
          { text: '前端适配与首页交互', link: '/frontend/responsive-guides' },
          { text: '实时语音口语训练', link: '/frontend/voice-practice' },
          { text: '前端页面序列（路由串联）', link: '/frontend/page-sequences' },
          { text: '管理员端 UI 重构说明', link: '/frontend/admin-ui-redesign' },
          { text: 'UI 统一规范', link: '/frontend/ui-unification' },
          { text: 'LiquidGlass 材质说明', link: '/frontend/ui-liquid-glass' },
          { text: 'Legacy 背景指南', link: '/frontend/ui-backgrounds' },
          { text: '前端图表主题（ECharts）', link: '/frontend/ui-chart-theme' },
          { text: '全站标题子标题规范', link: '/frontend/subtitle-style-guide' }
        ]
      },
      {
        text: 'API 参考',
        items: [
          {
            text: '后端 API（按控制器）',
            link: '/backend/api/index',
            items: [
              { text: '通用约定', link: '/backend/api/conventions' },
              { text: '认证', link: '/backend/api/auth' },
              { text: '用户资料', link: '/backend/api/user' },
              { text: '课程与课时', link: '/backend/api/course' },
              { text: '章节（Chapter）', link: '/backend/api/chapter' },
              { text: '课时（Lesson）', link: '/backend/api/lesson' },
              { text: '作业', link: '/backend/api/assignment' },
              { text: '提交', link: '/backend/api/submission' },
              { text: '评分', link: '/backend/api/grade' },
              { text: '能力评估', link: '/backend/api/ability' },
              { text: '学生端', link: '/backend/api/student' },
              { text: '教师端', link: '/backend/api/teacher' },
              { text: '社区', link: '/backend/api/community' },
              { text: '通知', link: '/backend/api/notification' },
              { text: '文件', link: '/backend/api/file' },
              { text: '报表/举报', link: '/backend/api/report' },
              { text: '帮助中心', link: '/backend/api/help' },
              { text: '聊天会话', link: '/backend/api/chat' },
              { text: '项目/工作台', link: '/backend/api/project' },
              { text: '行为证据评价', link: '/backend/api/behavior' },
              { text: '管理员', link: '/backend/api/admin' },
              { text: 'AI 聊天', link: '/backend/api/ai' },
              { text: 'AI 批改', link: '/backend/api/ai-grading' },
              { text: 'AI 实时语音（WS）', link: '/backend/api/ai-live' },
              { text: 'AI 口语训练', link: '/backend/api/ai-voice-practice' }
            ]
          },
          {
            text: '前端 API（按文件）',
            link: '/frontend/api/index',
            items: [
              { text: '通用约定', link: '/frontend/api/conventions' },
              { text: 'auth.api.ts', link: '/frontend/api/auth.api' },
              { text: 'user.api.ts', link: '/frontend/api/user.api' },
              { text: 'admin.api.ts', link: '/frontend/api/admin.api' },
              { text: 'course.api.ts', link: '/frontend/api/course.api' },
              { text: 'assignment.api.ts', link: '/frontend/api/assignment.api' },
              { text: 'submission.api.ts', link: '/frontend/api/submission.api' },
              { text: 'grade.api.ts', link: '/frontend/api/grade.api' },
              { text: 'student.api.ts', link: '/frontend/api/student.api' },
              { text: 'teacher.api.ts', link: '/frontend/api/teacher.api' },
              { text: 'community.api.ts', link: '/frontend/api/community.api' },
              { text: 'notification.api.ts', link: '/frontend/api/notification.api' },
              { text: 'file.api.ts', link: '/frontend/api/file.api' },
              { text: 'report.api.ts', link: '/frontend/api/report.api' },
              { text: 'ai.api.ts', link: '/frontend/api/ai.api' },
              { text: 'aiGrading.api.ts', link: '/frontend/api/aiGrading.api' },
              { text: 'voicePractice.api.ts', link: '/frontend/api/voicePractice.api' },
              { text: 'ability.api.ts', link: '/frontend/api/ability.api' },
              { text: 'behaviorEvidence.api.ts', link: '/frontend/api/behaviorEvidence.api' },
              { text: 'behaviorInsight.api.ts', link: '/frontend/api/behaviorInsight.api' },
              { text: 'chapter.api.ts', link: '/frontend/api/chapter.api' },
              { text: 'lesson.api.ts', link: '/frontend/api/lesson.api' },
              { text: 'chat.api.ts', link: '/frontend/api/chat.api' },
              { text: 'help.api.ts', link: '/frontend/api/help.api' },
              { text: 'project.api.ts', link: '/frontend/api/project.api' },
              { text: 'teacher-student.api.ts', link: '/frontend/api/teacher-student.api' }
            ]
          }
        ]
      },
      {
        text: '模型与配置',
        items: [
          {
            text: '数据模型总览',
            link: '/models/index',
            items: [
              { text: '课程/作业/提交/评分', link: '/models/courses-assignments' },
              { text: '用户与认证', link: '/models/users-auth' },
              { text: '能力评估', link: '/models/ability' },
              { text: '社区与通知', link: '/models/community-notifications' },
              { text: 'AI 会话/消息/记忆', link: '/models/ai' }
            ]
          },
          { text: '安全与配置（JWT/CORS/Env）', link: '/security-config' },
          { text: '主题与 UI（全站）', link: '/ui-theming' }
        ]
      },
      {
        text: '业务规格',
        items: [
          { text: '行为证据评价：数据契约', link: '/行为证据评价-数据契约' },
          { text: '管理员端：深度数据中心', link: '/管理员端-深度数据中心' },
          { text: '管理员端：课程详情查看与导出', link: '/管理员端-课程详情-查看与导出' },
          { text: '教师端：学生管理导出字段', link: '/教师端-学生管理-导出字段' },
          { text: '教师端：学生详情导出报告', link: '/教师端-学生详情-导出报告' }
        ]
      },
      {
        text: '运维与发布',
        items: [
          { text: '生产部署指南', link: '/deploy-production' },
          { text: '变更记录（清理与修复）', link: '/CHANGELOG-cleanup-20251006' }
        ]
      },
      {
        text: '参考',
        items: [
          { text: '实战食谱', link: '/cookbook' },
          { text: '图表主题与统一配色', link: '/ui-chart-theme' },
          { text: '错误码映射表', link: '/error-codes-mapping' },
          { text: '错误码与空态展示规范', link: '/ui-error-empty-spec' },
          { text: 'FAQ', link: '/faq' },
          { text: 'i18n 文案示例集', link: '/i18n-examples' },
          { text: '调试处方集', link: '/debug-recipes' },
          { text: '术语表', link: '/glossary' },
          { text: '团队约定', link: '/conventions' },
          { text: '文档与实现差异清单', link: '/docs-quality-backlog' },
          { text: '文档写作规范', link: '/docs-authoring' }
        ]
      }
    ],
    outline: [2, 3],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Xiaorui-Zhang537/student-core-competency-cultivation-assessment-system' }
    ]
  }
})
