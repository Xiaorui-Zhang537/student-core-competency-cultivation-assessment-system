import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { abilityApi } from '@/api/ability.api';
import type { AbilityDashboardData, AbilityTrendData, AbilityRecommendation } from '@/types/ability';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

export const useAbilityStore = defineStore('ability', () => {
  const uiStore = useUIStore();

  // State
  const dashboardData = ref<AbilityDashboardData | null>(null);
  const trendsData = ref<AbilityTrendData | null>(null);
  const recommendations = ref<AbilityRecommendation[]>([]);
  const loading = computed(() => uiStore.loading);

  // Actions
  const fetchDashboard = async () => {
    const response = await handleApiCall(
      abilityApi.getStudentDashboard,
      uiStore,
      '获取能力仪表盘数据失败'
    );
    if (response) {
      dashboardData.value = response.data;
    }
  };
  
  const fetchTrends = async () => {
    const response = await handleApiCall(
      abilityApi.getStudentTrends,
      uiStore,
      '获取能力趋势数据失败'
    );
    if (response) {
      trendsData.value = response.data;
    }
  };
  
  const fetchRecommendations = async () => {
    const response = await handleApiCall(
      abilityApi.getStudentRecommendations,
      uiStore,
      '获取能力建议失败'
    );
    if (response) {
      recommendations.value = response.data;
    }
  };

  return {
    dashboardData,
    trendsData,
    recommendations,
    loading,
    fetchDashboard,
    fetchTrends,
    fetchRecommendations,
  };
});
