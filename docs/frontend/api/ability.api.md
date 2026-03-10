---
title: "`ability.api.ts`"
description: 能力评估（学生目标/雷达/报告）API 封装
outline: [2, 3]
---

# 前端 API：ability.api.ts

## 方法签名
- `getAbilityDimensions()` → `GET /ability/dimensions`
- `getStudentGoals()` → `GET /ability/student/goals`
- `createStudentGoal(payload)` → `POST /ability/student/goals`
- `updateStudentGoal(goalId, payload)` → `PUT /ability/student/goals/{goalId}`
- `deleteStudentGoal(goalId)` → `DELETE /ability/student/goals/{goalId}`
- `getStudentRadar(params)` → `GET /ability/student/radar`
- `postStudentRadarCompare(body)` → `POST /ability/student/radar/compare`
- `postStudentDimensionInsights(body)` → `POST /ability/student/dimension-insights`
- `getStudentLatestReport()` → `GET /ability/student/report/latest`
- `getStudentLatestReportByContext(params)` → `GET /ability/student/report/latest-by-context`
- `getTeacherLatestReportOfStudent(params)` → `GET /ability/teacher/report/latest`
- `createReportFromAi(payload)` → `POST /ability/teacher/report/from-ai`
- `recordTeacherAssessment(payload)` → `POST /ability/teacher/assessment`（`x-www-form-urlencoded`）

## 最小示例
```ts
import { abilityApi } from '@/api/ability.api'

const dims = await abilityApi.getAbilityDimensions()
const goals = await abilityApi.getStudentGoals()
const radar = await abilityApi.getStudentRadar({ courseId: 1001 })
const insights = await abilityApi.postStudentDimensionInsights({ courseId: 1001 })
```

## 注意事项
- 当前前端已不直接封装 `student/dashboard|trends|recommendations` 老接口，统一走目标+雷达+报告链路
- `recordTeacherAssessment` 使用 `@RequestParam`，必须按表单方式提交

## 参数与返回
- 维度：`AbilityDimension[]`
- 目标：`AbilityGoal[]`
- 雷达/对比/维度解析：后端聚合对象（`any`，由页面按字段读取）
- 报告：`ability_reports` 聚合对象（`any`）

## 错误处理
- 401：未登录
- 403：无课程访问权限（教师/学生）
