<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">能力成长分析</h1>
      <p class="text-gray-500">可视化您的能力发展轨迹</p>
    </div>

    <!-- Loading State -->
    <div v-if="abilityStore.loading" class="text-center py-12">
      <p>正在加载能力数据...</p>
    </div>

    <!-- Main Content -->
    <div v-else class="space-y-8">
      <!-- Radar Chart & Recommendations -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 card p-6">
          <h2 class="text-xl font-semibold mb-4">能力雷达图</h2>
          <div ref="radarChartRef" class="h-96 w-full"></div>
        </div>
        <div class="card p-6">
          <h2 class="text-xl font-semibold mb-4">提升建议</h2>
          <div v-if="abilityStore.recommendations.length > 0" class="space-y-3 max-h-96 overflow-y-auto">
            <div v-for="rec in abilityStore.recommendations" :key="rec.id" class="p-3 bg-gray-50 rounded">
              <h4 class="font-medium">{{ rec.dimension }}</h4>
              <p class="text-sm text-gray-600">{{ rec.recommendationText }}</p>
            </div>
          </div>
          <p v-else class="text-gray-500">暂无建议。</p>
        </div>
      </div>

      <!-- Trends Chart -->
      <div class="card p-6">
        <h2 class="text-xl font-semibold mb-4">能力发展趋势</h2>
        <div ref="trendChartRef" class="h-96 w-full"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useAbilityStore } from '@/stores/ability';
import * as echarts from 'echarts';

const abilityStore = useAbilityStore();

const radarChartRef = ref<HTMLElement | null>(null);
const trendChartRef = ref<HTMLElement | null>(null);
let radarChart: echarts.ECharts | null = null;
let trendChart: echarts.ECharts | null = null;

const initRadarChart = () => {
  if (!radarChartRef.value || !abilityStore.dashboardData) return;
  radarChart = echarts.init(radarChartRef.value);
  
  const option = {
    radar: {
      indicator: abilityStore.dashboardData.radarChartData.labels.map(name => ({ name, max: 100 })),
    },
    series: [{
      type: 'radar',
      data: [{
        value: abilityStore.dashboardData.radarChartData.scores,
        name: '能力评分'
      }]
    }]
  };
  radarChart.setOption(option);
};

const initTrendChart = () => {
  if (!trendChartRef.value || !abilityStore.trendsData) return;
  trendChart = echarts.init(trendChartRef.value);

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: abilityStore.trendsData.dates },
    yAxis: { type: 'value' },
    series: abilityStore.trendsData.dimensions.map(dim => ({
      name: dim.name,
      type: 'line',
      data: dim.scores,
      smooth: true,
    }))
  };
  trendChart.setOption(option);
};

const resizeCharts = () => {
  radarChart?.resize();
  trendChart?.resize();
};

watch(() => abilityStore.dashboardData, (newData) => {
  if (newData) nextTick(initRadarChart);
}, { deep: true });

watch(() => abilityStore.trendsData, (newData) => {
  if (newData) nextTick(initTrendChart);
}, { deep: true });

onMounted(async () => {
  await Promise.all([
    abilityStore.fetchDashboard(),
    abilityStore.fetchTrends(),
    abilityStore.fetchRecommendations(),
  ]);
  window.addEventListener('resize', resizeCharts);
});

onUnmounted(() => {
  radarChart?.dispose();
  trendChart?.dispose();
  window.removeEventListener('resize', resizeCharts);
});
</script>
