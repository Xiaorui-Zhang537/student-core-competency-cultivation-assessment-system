import { use, init, getInstanceByDom, graphic } from 'echarts/core'
import type { EChartsType, EChartsCoreOption } from 'echarts/core'
import { LineChart, BarChart, PieChart, GaugeChart, RadarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  RadarComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 统一按需注册，避免全量打包 echarts
use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent,
  RadarComponent,
  LineChart,
  BarChart,
  PieChart,
  GaugeChart,
  RadarChart,
  CanvasRenderer
])

export const echarts = {
  init,
  getInstanceByDom,
  graphic
}

export type ECharts = EChartsType
export type { EChartsCoreOption }
