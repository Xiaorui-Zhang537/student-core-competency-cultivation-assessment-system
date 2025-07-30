<template>
  <div class="pie-chart-container">
    <div 
      ref="chartRef" 
      :style="{ width: width, height: height }"
      class="pie-chart"
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
    
    <!-- 空数据状态 -->
    <div 
      v-if="!loading && (!data || data.length === 0)" 
      class="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-gray-800/80 rounded-lg"
    >
      <div class="text-center">
        <chart-pie-icon class="w-12 h-12 text-gray-400 mx-auto mb-2" />
        <p class="text-sm text-gray-600 dark:text-gray-400">暂无数据</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ChartPieIcon } from '@heroicons/vue/24/outline'

// Props
interface PieData {
  name: string
  value: number
  color?: string
}

interface Props {
  data: PieData[]
  title?: string
  width?: string
  height?: string
  theme?: 'light' | 'dark' | 'auto'
  radius?: string | [string, string]
  center?: [string, string]
  legend?: object
  tooltip?: object
  animation?: boolean
  backgroundColor?: string
  loading?: boolean
  showLabel?: boolean
  labelPosition?: 'inside' | 'outside'
  roseType?: boolean | 'radius' | 'area'
}

const props = withDefaults(defineProps<Props>(), {
  width: '100%',
  height: '400px',
  theme: 'auto',
  radius: () => ['0%', '70%'] as [string, string],
  center: () => ['50%', '50%'] as [string, string],
  animation: true,
  backgroundColor: 'transparent',
  loading: false,
  showLabel: true,
  labelPosition: 'outside',
  roseType: false
})

// 状态
const chartRef = ref<HTMLElement>()
const chartInstance = ref<echarts.ECharts>()

// 默认颜色
const defaultColors = [
  '#3b82f6', '#ef4444', '#10b981', '#f59e0b',
  '#8b5cf6', '#06b6d4', '#84cc16', '#f97316',
  '#ec4899', '#6366f1'
]

// 初始化图表
const initChart = async () => {
  if (!chartRef.value || !props.data || props.data.length === 0) return
  
  try {
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
    chartInstance.value = echarts.init(chartRef.value, theme)
    
    // 处理数据，添加默认颜色
    const processedData = props.data.map((item, index) => ({
      ...item,
      itemStyle: {
        color: item.color || defaultColors[index % defaultColors.length]
      }
    }))
    
    // 配置选项
    const option = {
      backgroundColor: props.backgroundColor,
      title: props.title ? {
        text: props.title,
        left: 'center',
        top: '5%',
        textStyle: {
          color: theme === 'dark' ? '#ffffff' : '#333333',
          fontSize: 16,
          fontWeight: 'bold'
        }
      } : undefined,
      
      tooltip: {
        trigger: 'item',
        backgroundColor: theme === 'dark' ? '#374151' : '#ffffff',
        borderColor: theme === 'dark' ? '#4b5563' : '#e5e7eb',
        textStyle: {
          color: theme === 'dark' ? '#ffffff' : '#333333'
        },
        formatter: '{a} <br/>{b}: {c} ({d}%)',
        ...props.tooltip
      },
      
      legend: {
        type: 'scroll',
        orient: 'vertical',
        right: 10,
        top: 20,
        bottom: 20,
        textStyle: {
          color: theme === 'dark' ? '#ffffff' : '#333333'
        },
        ...props.legend
      },
      
      series: [
        {
          name: props.title || '数据分布',
          type: 'pie',
          radius: props.radius,
          center: props.center,
          data: processedData,
          roseType: props.roseType,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            show: props.showLabel,
            position: props.labelPosition,
            color: theme === 'dark' ? '#ffffff' : '#333333',
            formatter: '{b}: {d}%'
          },
          labelLine: {
            show: props.showLabel && props.labelPosition === 'outside'
          },
          animationType: 'scale',
          animationEasing: 'elasticOut',
          animationDelay: function (idx: number) {
            return Math.random() * 200
          },
          animationDuration: props.animation ? 1000 : 0
        }
      ]
    }
    
    // 设置选项
    chartInstance.value.setOption(option)
    
    // 响应式处理
    const resizeObserver = new ResizeObserver(() => {
      chartInstance.value?.resize()
    })
    resizeObserver.observe(chartRef.value)
    
  } catch (err) {
    console.error('饼图初始化失败:', err)
  }
}

// 更新图表
const updateChart = () => {
  if (!chartInstance.value) return
  
  const processedData = props.data.map((item, index) => ({
    ...item,
    itemStyle: {
      color: item.color || defaultColors[index % defaultColors.length]
    }
  }))
  
  const option = {
    series: [
      {
        data: processedData
      }
    ]
  }
  
  chartInstance.value.setOption(option)
}

// 监听数据变化
watch(
  () => [props.data, props.theme],
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
  
  return chartInstance.value.getDataURL({
    type,
    pixelRatio: 2,
    backgroundColor: '#fff'
  })
}

const downloadChart = (filename: string = 'pie-chart', type: 'png' | 'jpeg' = 'png') => {
  const dataURL = exportChart(type)
  if (!dataURL) return
  
  const link = document.createElement('a')
  link.download = `${filename}.${type}`
  link.href = dataURL
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 高亮显示特定数据
const highlightData = (dataIndex: number) => {
  if (!chartInstance.value) return
  
  chartInstance.value.dispatchAction({
    type: 'highlight',
    seriesIndex: 0,
    dataIndex
  })
}

// 取消高亮
const downplayData = (dataIndex: number) => {
  if (!chartInstance.value) return
  
  chartInstance.value.dispatchAction({
    type: 'downplay',
    seriesIndex: 0,
    dataIndex
  })
}

// 暴露方法
defineExpose({
  exportChart,
  downloadChart,
  highlightData,
  downplayData,
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
.pie-chart-container {
  position: relative;
  display: flex;
  flex-direction: column;
}

.pie-chart {
  flex: 1;
  min-height: 200px;
}
</style> 