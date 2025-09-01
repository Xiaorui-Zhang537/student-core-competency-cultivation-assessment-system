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
