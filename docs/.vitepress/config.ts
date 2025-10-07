import { defineConfig } from 'vitepress'

const appUrl = process.env.VITE_APP_URL || 'https://stucoreai.space'

export default defineConfig({
  lang: 'zh-CN',
  title: '学生核心能力培养评估系统',
  description: '从0开始的完整上手与参考文档',
  base: '/',
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
          { text: '页面级时序图', link: '/page-sequences' }
        ]
      },
      {
        text: 'API 参考',
        items: [
          {
            text: '后端 API（按控制器）',
            link: '/backend/api/index',
            items: [
              { text: '认证与用户', link: '/backend/api/auth' },
              { text: '课程与课时', link: '/backend/api/course' },
              { text: '作业', link: '/backend/api/assignment' },
              { text: '提交', link: '/backend/api/submission' },
              { text: '评分', link: '/backend/api/grade' },
              { text: '学生端', link: '/backend/api/student' },
              { text: '教师端', link: '/backend/api/teacher' },
              { text: '社区', link: '/backend/api/community' },
              { text: '通知', link: '/backend/api/notification' },
              { text: '文件', link: '/backend/api/file' },
              { text: '报表/举报', link: '/backend/api/report' },
              { text: 'AI 聊天', link: '/backend/api/ai' }
            ]
          },
          {
            text: '前端 API（按文件）',
            link: '/frontend/api/index',
            items: [
              { text: 'auth.api.ts', link: '/frontend/api/auth.api' },
              { text: 'user.api.ts', link: '/frontend/api/user.api' },
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
              { text: 'ability.api.ts', link: '/frontend/api/ability.api' },
              { text: 'chapter.api.ts', link: '/frontend/api/chapter.api' },
              { text: 'lesson.api.ts', link: '/frontend/api/lesson.api' },
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
          { text: '安全与配置（JWT/CORS/Env）', link: '/security-config' }
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
          { text: '团队约定', link: '/conventions' }
        ]
      }
    ],
    outline: [2, 3],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Xiaorui-Zhang537/student-core-competency-cultivation-assessment-system' }
    ]
  }
})
