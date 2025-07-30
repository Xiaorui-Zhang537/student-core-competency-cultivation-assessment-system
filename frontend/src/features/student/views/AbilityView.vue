<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 p-6">
    <!-- 页面标题 -->
    <div class="mb-8">
      <h1 class="text-4xl font-bold text-gray-900 dark:text-white mb-2">能力成长分析</h1>
      <p class="text-lg text-gray-600 dark:text-gray-400">全面了解您的能力发展轨迹，制定个性化成长计划</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="abilityStore.loading" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-primary-500"></div>
    </div>

    <!-- 主要内容 -->
    <div v-else class="space-y-8">
      <!-- 能力概览卡片 -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <card class="lg:col-span-1" padding="lg">
          <div class="text-center">
            <div class="text-3xl font-bold text-primary-600 dark:text-primary-400 mb-2">
              {{ abilityStore.overallProgress }}%
            </div>
            <div class="text-sm text-gray-600 dark:text-gray-400 mb-4">综合能力</div>
            <progress :value="abilityStore.overallProgress" color="primary" size="lg" :show-label="false" />
            <div class="mt-4 flex items-center justify-center">
              <badge 
                :variant="progressTrendColor" 
                :class="['flex items-center space-x-1']"
              >
                <component :is="progressTrendIcon" class="w-4 h-4" />
                <span>{{ progressTrendText }}</span>
              </badge>
            </div>
          </div>
        </card>

        <card class="lg:col-span-3" padding="lg">
          <template #header>
            <div class="flex justify-between items-center">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">能力雷达图</h3>
              <div class="flex space-x-2">
                <button
                  v-for="range in timeRanges"
                  :key="range.value"
                  :variant="abilityStore.selectedTimeRange === range.value ? 'primary' : 'outline'"
                  size="sm"
                  @click="abilityStore.setTimeRange(range.value)"
                >
                  {{ range.label }}
                </button>
              </div>
            </div>
          </template>
          <div ref="radarChartRef" class="h-80 w-full"></div>
        </card>
      </div>

      <!-- 趋势分析和学习建议 -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-8">
        <!-- 能力发展趋势 -->
        <card padding="lg">
          <template #header>
            <div class="flex justify-between items-center">
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">能力发展趋势</h3>
              <select 
                v-model="selectedDimensionForTrend"
                class="input input-sm max-w-xs"
                @change="onDimensionChange"
              >
                <option value="">全部维度</option>
                <option 
                  v-for="dimension in abilityStore.dimensions" 
                  :key="dimension.id" 
                  :value="dimension.id"
                >
                  {{ dimension.name }}
                </option>
              </select>
            </div>
          </template>
          <div ref="trendChartRef" class="h-80 w-full"></div>
        </card>

        <!-- 学习建议 -->
        <card padding="lg">
          <template #header>
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">智能学习建议</h3>
          </template>
          <div class="space-y-4 max-h-80 overflow-y-auto">
            <div 
              v-for="recommendation in abilityStore.recommendations.slice(0, 5)" 
              :key="recommendation.id"
              class="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors cursor-pointer"
              @click="openRecommendation(recommendation)"
            >
              <div class="flex justify-between items-start mb-2">
                <h4 class="font-medium text-gray-900 dark:text-white text-sm">{{ recommendation.title }}</h4>
                <div class="flex space-x-2">
                  <badge :variant="getPriorityColor(recommendation.priority)" size="sm">
                    {{ getPriorityText(recommendation.priority) }}
                  </badge>
                  <badge variant="secondary" size="sm">
                    {{ recommendation.estimatedTime }}分钟
                  </badge>
                </div>
              </div>
              <p class="text-sm text-gray-600 dark:text-gray-400 line-clamp-2">{{ recommendation.description }}</p>
            </div>
            <div v-if="abilityStore.recommendations.length === 0" class="text-center py-8 text-gray-500 dark:text-gray-400">
              暂无学习建议
            </div>
          </div>
        </card>
      </div>

      <!-- 学习目标管理 -->
      <card padding="lg">
        <template #header>
          <div class="flex justify-between items-center">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">学习目标管理</h3>
            <button @click="showCreateGoalModal = true" variant="primary" size="sm">
              <plus-icon class="w-4 h-4 mr-2" />
              新建目标
            </button>
          </div>
        </template>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            v-for="goal in abilityStore.activeGoals" 
            :key="goal.id"
            class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg hover:shadow-md transition-shadow"
          >
            <div class="flex justify-between items-start mb-3">
              <h4 class="font-medium text-gray-900 dark:text-white">{{ getDimensionName(goal.dimensionId) }}</h4>
              <badge :variant="getGoalStatusColor(goal.status)" size="sm">
                {{ getGoalStatusText(goal.status) }}
              </badge>
            </div>
            
            <div class="space-y-2 mb-4">
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">当前分数</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ goal.currentScore }}</span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">目标分数</span>
                <span class="font-medium text-primary-600 dark:text-primary-400">{{ goal.targetScore }}</span>
              </div>
              <progress 
                :value="(goal.currentScore / goal.targetScore) * 100" 
                color="primary" 
                size="sm"
                class="mt-2"
              />
            </div>
            
            <div class="flex justify-between items-center text-sm text-gray-500 dark:text-gray-400">
              <span>截止：{{ formatDate(goal.deadline) }}</span>
              <div class="flex space-x-2">
                <button 
                  @click="editGoal(goal)"
                  class="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                >
                  编辑
                </button>
                <button 
                  @click="deleteGoal(goal.id)"
                  class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                >
                  删除
                </button>
              </div>
            </div>
          </div>
          
          <div 
            v-if="abilityStore.activeGoals.length === 0" 
            class="col-span-full text-center py-12 text-gray-500 dark:text-gray-400"
          >
            <chart-bar-icon class="w-12 h-12 mx-auto mb-4 opacity-50" />
            <p>还没有设置学习目标</p>
            <p class="text-sm">设置目标来跟踪您的能力发展进度</p>
          </div>
        </div>
      </card>

      <!-- 能力详细分析 -->
      <card padding="lg">
        <template #header>
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white">能力详细分析</h3>
        </template>
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- 优势能力 -->
          <div>
            <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4 flex items-center">
                              <arrow-trending-up-icon class="w-5 h-5 text-green-500 mr-2" />
              优势能力
            </h4>
            <div class="space-y-3">
              <div 
                v-for="dimension in abilityStore.strongestDimensions" 
                :key="dimension.dimensionId"
                class="flex items-center justify-between p-3 bg-green-50 dark:bg-green-900/20 rounded-lg"
              >
                <div>
                  <div class="font-medium text-gray-900 dark:text-white">{{ dimension.dimensionName }}</div>
                  <div class="text-sm text-gray-600 dark:text-gray-400">
                    水平：{{ getLevelText(dimension.level) }} · 排名前{{ dimension.percentile }}%
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-lg font-bold text-green-600 dark:text-green-400">{{ dimension.score }}</div>
                  <div class="text-xs text-gray-500">/ {{ dimension.maxScore }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 提升空间 -->
          <div>
            <h4 class="text-md font-medium text-gray-900 dark:text-white mb-4 flex items-center">
                              <arrow-trending-down-icon class="w-5 h-5 text-orange-500 mr-2" />
              提升空间
            </h4>
            <div class="space-y-3">
              <div 
                v-for="dimension in abilityStore.weakestDimensions" 
                :key="dimension.dimensionId"
                class="flex items-center justify-between p-3 bg-orange-50 dark:bg-orange-900/20 rounded-lg"
              >
                <div>
                  <div class="font-medium text-gray-900 dark:text-white">{{ dimension.dimensionName }}</div>
                  <div class="text-sm text-gray-600 dark:text-gray-400">
                    水平：{{ getLevelText(dimension.level) }} · 排名前{{ dimension.percentile }}%
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-lg font-bold text-orange-600 dark:text-orange-400">{{ dimension.score }}</div>
                  <div class="text-xs text-gray-500">/ {{ dimension.maxScore }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </card>
    </div>

    <!-- 创建目标弹窗 -->
    <div 
      v-if="showCreateGoalModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showCreateGoalModal = false"
    >
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">创建学习目标</h3>
        
        <form @submit.prevent="createGoal" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">能力维度</label>
            <select v-model="goalForm.dimensionId" class="input w-full" required>
              <option value="">请选择能力维度</option>
              <option 
                v-for="dimension in abilityStore.dimensions" 
                :key="dimension.id" 
                :value="dimension.id"
              >
                {{ dimension.name }}
              </option>
            </select>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">目标分数</label>
            <input 
              v-model.number="goalForm.targetScore" 
              type="number" 
              min="0" 
              max="100" 
              class="input w-full" 
              required 
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">截止日期</label>
            <input 
              v-model="goalForm.deadline" 
              type="date" 
              class="input w-full" 
              required 
            />
          </div>
          
          <div class="flex justify-end space-x-3 pt-4">
            <button type="button" variant="outline" @click="showCreateGoalModal = false">
              取消
            </button>
            <button type="submit" variant="primary" :loading="creatingGoal">
              创建目标
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { useAbilityStore } from '@/stores/ability'
import { useAuthStore } from '@/stores/auth'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import * as echarts from 'echarts'
import type { AbilityGoal, LearningRecommendation } from '@/types/ability'
import {
  PlusIcon,
  ChartBarIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  MinusIcon
} from '@heroicons/vue/24/outline'

// Stores
const abilityStore = useAbilityStore()
const authStore = useAuthStore()

// Chart refs
const radarChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
let radarChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

// 时间范围选项
const timeRanges = [
  { label: '1个月', value: '1month' as const },
  { label: '3个月', value: '3months' as const },
  { label: '6个月', value: '6months' as const },
  { label: '1年', value: '1year' as const }
]

// 状态
const selectedDimensionForTrend = ref('')
const showCreateGoalModal = ref(false)
const creatingGoal = ref(false)

// 目标表单
const goalForm = ref({
  dimensionId: '',
  targetScore: 0,
  deadline: ''
})

// 计算属性
const progressTrendColor = computed(() => {
  switch (abilityStore.progressTrend) {
    case 'improving': return 'success'
    case 'declining': return 'warning'
    default: return 'secondary'
  }
})

const progressTrendIcon = computed(() => {
  switch (abilityStore.progressTrend) {
    case 'improving': return ArrowTrendingUpIcon
    case 'declining': return ArrowTrendingDownIcon
    default: return MinusIcon
  }
})

const progressTrendText = computed(() => {
  switch (abilityStore.progressTrend) {
    case 'improving': return '持续提升'
    case 'declining': return '需要关注'
    default: return '保持稳定'
  }
})

// 方法
const initRadarChart = () => {
  if (!radarChartRef.value) return
  
  radarChart = echarts.init(radarChartRef.value)
  
  const dimensions = abilityStore.latestScores.map(score => ({
    name: score.dimensionName,
    max: score.maxScore
  }))
  
  const data = abilityStore.latestScores.map(score => score.score)
  
  const option = {
    title: {
      show: false
    },
    radar: {
      indicator: dimensions,
      radius: '60%',
      splitNumber: 5,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      splitLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      splitArea: {
        areaStyle: {
          color: ['transparent', 'rgba(99, 102, 241, 0.05)']
        }
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: data,
        name: '当前能力',
        areaStyle: {
          color: 'rgba(99, 102, 241, 0.2)'
        },
        lineStyle: {
          color: '#6366f1',
          width: 2
        },
        itemStyle: {
          color: '#6366f1'
        }
      }]
    }],
    grid: {
      left: 20,
      right: 20,
      top: 20,
      bottom: 20
    }
  }
  
  radarChart.setOption(option)
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  const trendData = abilityStore.trends.length > 0 ? abilityStore.trends[0] : null
  
  if (!trendData) {
    const option = {
      title: {
        text: '暂无趋势数据',
        left: 'center',
        top: 'middle',
        textStyle: {
          color: '#9ca3af',
          fontSize: 16
        }
      }
    }
    trendChart.setOption(option)
    return
  }
  
  const dates = trendData.data.map(item => item.date)
  const scores = trendData.data.map(item => item.score)
  
  const option = {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${data.axisValue}<br/>${data.seriesName}: ${data.value}分`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280'
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6'
        }
      }
    },
    series: [{
      name: trendData.dimensionName,
      type: 'line',
      data: scores,
      smooth: true,
      lineStyle: {
        color: '#6366f1',
        width: 3
      },
      itemStyle: {
        color: '#6366f1'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(99, 102, 241, 0.3)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0.05)' }
          ]
        }
      }
    }],
    grid: {
      left: 50,
      right: 20,
      top: 20,
      bottom: 40
    }
  }
  
  trendChart.setOption(option)
}

const onDimensionChange = () => {
  abilityStore.setSelectedDimension(selectedDimensionForTrend.value || null)
}

const getDimensionName = (dimensionId: string) => {
  const dimension = abilityStore.dimensions.find(d => d.id === dimensionId)
  return dimension?.name || '未知维度'
}

const getPriorityColor = (priority: string) => {
  switch (priority) {
    case 'high': return 'danger'
    case 'medium': return 'warning'
    default: return 'secondary'
  }
}

const getPriorityText = (priority: string) => {
  switch (priority) {
    case 'high': return '高优先级'
    case 'medium': return '中优先级'
    default: return '低优先级'
  }
}

const getGoalStatusColor = (status: string) => {
  switch (status) {
    case 'completed': return 'success'
    case 'active': return 'primary'
    default: return 'secondary'
  }
}

const getGoalStatusText = (status: string) => {
  switch (status) {
    case 'completed': return '已完成'
    case 'active': return '进行中'
    case 'paused': return '已暂停'
    default: return '未知'
  }
}

const getLevelText = (level: string) => {
  switch (level) {
    case 'expert': return '专家'
    case 'advanced': return '高级'
    case 'intermediate': return '中级'
    default: return '初级'
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const createGoal = async () => {
  if (!goalForm.value.dimensionId || !goalForm.value.targetScore || !goalForm.value.deadline) {
    return
  }
  
  creatingGoal.value = true
  try {
    await abilityStore.createGoal({
      studentId: authStore.user?.id || '',
      dimensionId: goalForm.value.dimensionId,
      targetScore: goalForm.value.targetScore,
      currentScore: getCurrentScore(goalForm.value.dimensionId),
      deadline: goalForm.value.deadline,
      status: 'active'
    })
    
    showCreateGoalModal.value = false
    goalForm.value = {
      dimensionId: '',
      targetScore: 0,
      deadline: ''
    }
  } catch (error) {
    console.error('创建目标失败:', error)
  } finally {
    creatingGoal.value = false
  }
}

const getCurrentScore = (dimensionId: string) => {
  const dimension = abilityStore.latestScores.find(s => s.dimensionId === dimensionId)
  return dimension?.score || 0
}

const editGoal = (goal: AbilityGoal) => {
  // TODO: 实现编辑目标功能
  console.log('编辑目标:', goal)
}

const deleteGoal = async (goalId: string) => {
  if (confirm('确定要删除这个学习目标吗？')) {
    try {
      await abilityStore.deleteGoal(goalId)
    } catch (error) {
      console.error('删除目标失败:', error)
    }
  }
}

const openRecommendation = (recommendation: LearningRecommendation) => {
  // TODO: 实现打开学习建议详情
  console.log('打开学习建议:', recommendation)
}

const resizeCharts = () => {
  radarChart?.resize()
  trendChart?.resize()
}

// 监听数据变化并更新图表
watch(() => abilityStore.latestScores, () => {
  nextTick(initRadarChart)
}, { deep: true })

watch(() => abilityStore.trends, () => {
  nextTick(initTrendChart)
}, { deep: true })

// 生命周期
onMounted(async () => {
  await abilityStore.initAbilityData()
  
  nextTick(() => {
    initRadarChart()
    initTrendChart()
  })
  
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  radarChart?.dispose()
  trendChart?.dispose()
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 