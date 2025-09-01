<template>
  <div class="line-chart-container">
    <div 
      ref="chartRef" 
      :style="{ width: width, height: height }"
      class="line-chart"
    ></div>
    
    <!-- 加载状态 -->
    <div 
      v-if="loading" 
      class="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-gray-800/80 rounded-lg"
    >
      <div class="text-center">
        <div class="w-8 h-8 border-2 border-primary-600 border-t-transparent rounded-full animate-spin mb-2"></div>
        <p class="text-sm text-gray-600 dark:text-gray-400">加载中...</p>
      </div>
    </div>
    
    <!-- 错误状态 -->
    <div 
      v-if="error" 
      class="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-gray-800/80 rounded-lg"
    >
      <div class="text-center">
        <exclamation-triangle-icon class="w-12 h-12 text-red-500 mx-auto mb-2" />
        <p class="text-sm text-gray-600 dark:text-gray-400">图表加载失败</p>
        <button 
          @click="initChart" 
          class="mt-2 text-xs text-primary-600 hover:text-primary-700"
        >
          重试
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'

// Props
interface ChartData {
  name: string
  data: number[]
  color?: string
  type?: 'line' | 'bar'
  smooth?: boolean
}

interface Props {
  data: ChartData[]
  xAxisData: string[]
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
  grid?: object
  legend?: object
  tooltip?: object
  animation?: boolean
  backgroundColor?: string
  loading?: boolean
  empty?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  xAxisData: () => [],
  width: '100%',
  height: '400px',
  theme: 'auto',
  animation: true,
  backgroundColor: 'transparent',
  loading: false,
  empty: false
})

// 状态
const chartRef = ref<HTMLElement>()
const chartInstance = ref<echarts.ECharts>()
const error = ref(false)

// 等待容器可用且有尺寸
const waitForContainer = async (maxTries = 10): Promise<boolean> => {
  for (let i = 0; i < maxTries; i++) {
    await nextTick()
    if (chartRef.value && chartRef.value.offsetWidth > 0 && chartRef.value.offsetHeight > 0) return true
    await new Promise(r => requestAnimationFrame(() => r(null)))
  }
  return !!chartRef.value
}

// 初始化图表
const initChart = async () => {
  const ok = await waitForContainer()
  if (!ok || !chartRef.value) return
  
  try {
    error.value = false
    
    // 销毁已存在的实例
    if (chartInstance.value) {
      chartInstance.value.dispose()
    }
    
    await nextTick()
    
    // 确定主题
    const theme = props.theme === 'auto' 
      ? (document.documentElement.classList.contains('dark') ? 'dark' : 'light')
      : props.theme
    
    // 创建图表实例
    chartInstance.value = echarts.init(chartRef.value as HTMLDivElement, theme)
    
    // 配置选项
    const palette = theme === 'dark'
      ? ['#93c5fd', '#22d3ee', '#f472b6', '#f59e0b', '#34d399']
      : ['#3b82f6', '#06b6d4', '#ec4899', '#f59e0b', '#10b981']

    const normalizedSeries = (props.data || []).filter((s: any) => s && Array.isArray(s.data))
    const hasBar = normalizedSeries.some(s => (s.type || 'line') === 'bar')

    const option: any = {
      backgroundColor: theme === 'dark' ? '#0b0f1a' : '#ffffff',
      title: props.title ? {
        text: props.title,
        left: 'center',
        textStyle: {
          color: theme === 'dark' ? '#ffffff' : '#333333',
          fontSize: 16,
          fontWeight: 'bold'
        }
      } : undefined,
      
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
        ...props.grid
      },
      
      legend: {
        top: 'bottom',
        textStyle: {
          color: theme === 'dark' ? '#ffffff' : '#333333'
        },
        ...props.legend
      },
      
      tooltip: {
        trigger: 'axis',
        backgroundColor: theme === 'dark' ? '#374151' : '#ffffff',
        borderColor: theme === 'dark' ? '#4b5563' : '#e5e7eb',
        textStyle: {
          color: theme === 'dark' ? '#ffffff' : '#333333'
        },
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: theme === 'dark' ? '#6b7280' : '#6b7280'
          }
        },
        ...props.tooltip
      },
      
      xAxis: {
        type: 'category',
        boundaryGap: hasBar ? true : false,
        data: Array.isArray(props.xAxisData) ? props.xAxisData : [],
        axisLine: {
          lineStyle: {
            color: theme === 'dark' ? '#4b5563' : '#d1d5db'
          }
        },
        axisLabel: {
          color: theme === 'dark' ? '#9ca3af' : '#6b7280'
        }
      },
      
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: theme === 'dark' ? '#6b7280' : '#d1d5db'
          }
        },
        axisLabel: {
          color: theme === 'dark' ? '#9ca3af' : '#6b7280'
        },
        splitLine: {
          lineStyle: {
            color: theme === 'dark' ? '#4b5563' : '#e5e7eb'
          }
        }
      },
      
      series: normalizedSeries.map((item, idx) => {
        const isBar = (item.type || 'line') === 'bar'
        const baseColor = item.color || palette[idx % palette.length]
        return {
          name: item.name,
          type: (item.type || 'line') as 'line' | 'bar',
          smooth: item.smooth !== false,
          data: Array.isArray(item.data) ? item.data : [],
          symbol: 'circle',
          symbolSize: theme === 'dark' ? 5 : 4,
          // 为柱状图设置每个柱子不同颜色；折线图保持系列颜色
          itemStyle: isBar
            ? {
                color: (params: any) => palette[params.dataIndex % palette.length]
              }
            : {
                color: baseColor
              },
          lineStyle: !isBar
            ? {
                color: baseColor,
                width: theme === 'dark' ? 3 : 2
              }
            : undefined,
          areaStyle: !isBar
            ? {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: baseColor + (theme === 'dark' ? '40' : '30') },
                  { offset: 1, color: baseColor + (theme === 'dark' ? '10' : '05') }
                ])
              }
            : undefined,
          barMaxWidth: isBar ? 32 : undefined,
          emphasis: {
            focus: 'series'
          },
          animationDuration: props.animation ? 1000 : 0
        }
      })
    }
    if (normalizedSeries.length === 0 || !Array.isArray(props.xAxisData)) {
      option.series = []
      option.xAxis.data = []
    }
    
    // 设置选项（完全替换以避免保留无效旧系列）
    chartInstance.value.clear()
    chartInstance.value.setOption(option, true)
    
    // 响应式处理
    const resizeObserver = new ResizeObserver(() => {
      if (!chartInstance.value) return
      requestAnimationFrame(() => chartInstance.value && chartInstance.value.resize())
    })
    resizeObserver.observe(chartRef.value)
    
  } catch (err) {
    console.error('图表初始化失败:', err)
    error.value = true
  }
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) return

  const isDark = (props.theme === 'auto')
    ? document.documentElement.classList.contains('dark')
    : props.theme === 'dark'
  const palette = isDark
    ? ['#93c5fd', '#22d3ee', '#f472b6', '#f59e0b', '#34d399']
    : ['#3b82f6', '#06b6d4', '#ec4899', '#f59e0b', '#10b981']

  const normalizedSeries = (props.data || []).filter((s: any) => s && Array.isArray(s.data))
  const hasBar = normalizedSeries.some(s => (s.type || 'line') === 'bar')

  const option: any = {
    xAxis: {
      data: Array.isArray(props.xAxisData) ? props.xAxisData : [],
      boundaryGap: hasBar ? true : false
    },
    series: normalizedSeries.map((item, idx) => {
      const isBar = (item.type || 'line') === 'bar'
      const baseColor = item.color || palette[idx % palette.length]
      return {
        name: item.name,
        type: (item.type || 'line') as 'line' | 'bar',
        data: Array.isArray(item.data) ? item.data : [],
        smooth: item.smooth !== false,
        symbol: 'circle',
        symbolSize: isDark ? 5 : 4,
        itemStyle: isBar
          ? { color: (params: any) => palette[params.dataIndex % palette.length] }
          : { color: baseColor },
        lineStyle: !isBar ? { color: baseColor, width: isDark ? 3 : 2 } : undefined,
        areaStyle: !isBar
          ? { color: new (echarts as any).graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: baseColor + (isDark ? '40' : '30') },
              { offset: 1, color: baseColor + (isDark ? '10' : '05') }
            ]) }
          : undefined,
        barMaxWidth: isBar ? 32 : undefined
      }
    })
  }
  if (normalizedSeries.length === 0) {
    option.series = []
  }

  chartInstance.value.clear()
  chartInstance.value.setOption(option, true)
}

// 监听数据变化
watch(
  () => [props.data, props.xAxisData, props.theme],
  () => {
    if (chartInstance.value) {
      updateChart()
    }
  },
  { deep: true }
)

// 监听主题变化
watch(
  () => document.documentElement.classList.contains('dark'),
  () => {
    if (props.theme === 'auto') {
      initChart()
    }
  }
)

// 导出方法
const exportChart = (type: 'png' | 'jpeg' = 'png') => {
  if (!chartInstance.value) return null
  const isDark = document.documentElement.classList.contains('dark')
  const bg = isDark ? '#0b0f1a' : '#ffffff'
  return chartInstance.value.getDataURL({ type, pixelRatio: 2, backgroundColor: bg })
}

const downloadChart = (filename: string = 'chart', type: 'png' | 'jpeg' = 'png') => {
  const dataURL = exportChart(type)
  if (!dataURL) return
  
  const link = document.createElement('a')
  link.download = `${filename}.${type}`
  link.href = dataURL
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 暴露方法
defineExpose({
  exportChart,
  downloadChart,
  getChartInstance: () => chartInstance.value
})

// 生命周期
onMounted(() => {
  initChart()
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
  }
})
</script>

<style scoped lang="postcss">
.line-chart-container {
  position: relative;
  display: flex;
  flex-direction: column;
}

.line-chart {
  flex: 1;
  min-height: 200px;
}
</style> 