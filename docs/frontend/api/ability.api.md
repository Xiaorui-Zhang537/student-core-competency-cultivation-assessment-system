# 前端 API：ability.api.ts

## 方法签名
- `getAbilityDimensions()` → `GET /ability/dimensions`
- `getStudentDashboard()` → `GET /ability/student/dashboard`
- `getStudentTrends()` → `GET /ability/student/trends`
- `getStudentRecommendations()` → `GET /ability/student/recommendations`

### 类型（节选）
```ts
import type { AbilityDimension, AbilityDashboardData, AbilityTrendData, AbilityRecommendation } from '@/types/ability'
```

## 最小示例
```ts
import { abilityApi } from '@/api/ability.api'

const dims = await abilityApi.getAbilityDimensions()
const dashboard = await abilityApi.getStudentDashboard()
const trends = await abilityApi.getStudentTrends()
const recs = await abilityApi.getStudentRecommendations()
```

## 注意事项
- 趋势图与推荐需要前端 ECharts 组件与 UI 进行可视化渲染
- 接口为学生端常用，返回结构请参考 `@/types/ability`

## 参数与返回
- 维度：`AbilityDimension[]`
- 仪表盘：`AbilityDashboardData`
- 趋势：`AbilityTrendData`
- 推荐：`AbilityRecommendation[]`

## 示例
```ts
// 维度+仪表盘
const dims = await abilityApi.getAbilityDimensions()
const dashboard = await abilityApi.getStudentDashboard()

// 趋势与建议
const trends = await abilityApi.getStudentTrends()
const recs = await abilityApi.getStudentRecommendations()
```

## 错误处理
- 401：未登录
- 404：能力数据未初始化
