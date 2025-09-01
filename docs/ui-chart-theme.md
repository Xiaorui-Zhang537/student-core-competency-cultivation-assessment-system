# 图表主题与统一配色规范

目标：让雷达图、饼图、折线/柱状图在全站风格保持一致，减少认知负担。

## 1. 基础主题（ECharts）
```ts
export const baseChartTheme = {
  color: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'],
  textStyle: { color: '#1f2937' },
  tooltip: { backgroundColor: 'rgba(31,41,55,0.9)', textStyle: { color: '#fff' } },
  grid: { left: 24, right: 24, top: 24, bottom: 24, containLabel: true },
}
```

## 2. 雷达图规范
- 指标顺序固定，避免各页不一致
- 量纲标准化至 0-100
- 区域填充透明度 `0.15-0.25`

```ts
const radarOption = {
  ...baseChartTheme,
  radar: { indicator: radarDims.map((n) => ({ name: n, max: 100 })) },
  series: [{ type: 'radar', areaStyle: { opacity: 0.2 } }]
}
```

## 3. 饼图规范
- 展示前 N 大 + 其它合并
- 标签显示：名称 + 百分比（1 位小数）

```ts
const pieOption = {
  ...baseChartTheme,
  series: [{
    type: 'pie', radius: ['40%', '70%'],
    label: { formatter: '{b}: {d}%'}
  }]
}
```

## 4. 折线/柱状规范
- 时间轴按日/周/月统一粒度
- 缺失点显示为 null，线段不断裂时用 `connectNulls: true`
- 平滑曲线 `smooth: 0.1`

```ts
const lineOption = {
  ...baseChartTheme,
  xAxis: { type: 'category' }, yAxis: { type: 'value' },
  series: [{ type: 'line', smooth: 0.1, connectNulls: true }]
}
```

## 5. 深浅色适配
- 跟随应用主题切换：浅色使用上面色板；深色将文本/网格反色
- 组件外部以 `isDark` 控制主题注入

## 6. 可访问性与导出
- 提供数据表格视图切换
- 导出图像使用白底，字体≥12px

## 7. 验收清单
- 颜色顺序一致、图例不换位
- 标签不重叠（必要时隐藏尾部标签）
- 工具提示包含单位/量纲；下载导出可用

