<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold">教师仪表盘</h1>
      <p class="text-gray-500">选择一门课程以查看其数据分析</p>
    </div>

    <!-- Course Selector -->
    <div class="mb-6 max-w-sm">
        <label for="course-select" class="block text-sm font-medium mb-1">选择课程</label>
        <select id="course-select" v-model="selectedCourseId" @change="onCourseSelect" class="input">
            <option :value="null" disabled>请选择一门课程</option>
            <option v-for="course in courseStore.courses" :key="course.id" :value="course.id">
                {{ course.title }}
            </option>
        </select>
    </div>

    <!-- Loading State -->
    <div v-if="teacherStore.loading" class="text-center py-12">
      <p>正在加载分析数据...</p>
    </div>

    <!-- Analytics Content -->
    <div v-else-if="selectedCourseId && courseAnalytics" class="space-y-8">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div class="card p-4 text-center">
                <h3 class="text-2xl font-bold">{{ courseAnalytics.enrollmentCount }}</h3>
                <p class="text-sm text-gray-500">学生人数</p>
            </div>
            <div class="card p-4 text-center">
                <h3 class="text-2xl font-bold">{{ (courseAnalytics.averageCompletionRate || 0).toFixed(1) }}%</h3>
                <p class="text-sm text-gray-500">平均完成率</p>
            </div>
            <div class="card p-4 text-center">
                <h3 class="text-2xl font-bold">{{ (courseAnalytics.averageScore || 0).toFixed(1) }}</h3>
                <p class="text-sm text-gray-500">平均分</p>
            </div>
             <div class="card p-4 text-center">
                <h3 class="text-2xl font-bold">{{ courseAnalytics.assignmentCount }}</h3>
                <p class="text-sm text-gray-500">作业数</p>
            </div>
        </div>

        <!-- Class Performance Chart -->
        <div class="card p-6">
            <h2 class="text-xl font-semibold mb-4">班级成绩分布</h2>
            <div ref="chartRef" class="h-96 w-full"></div>
        </div>
    </div>

     <!-- Empty State -->
    <div v-else class="text-center py-12 card">
        <h3 class="text-lg font-medium">请选择一门课程</h3>
        <p class="text-gray-500">选择一门课程后，将在此处显示相关的统计数据和图表。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, onUnmounted } from 'vue';
import { useTeacherStore } from '@/stores/teacher';
import { useCourseStore } from '@/stores/course';
import * as echarts from 'echarts';

const teacherStore = useTeacherStore();
const courseStore = useCourseStore();

const selectedCourseId = ref<string | null>(null);
const chartRef = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

const courseAnalytics = computed(() => teacherStore.courseAnalytics);
const classPerformance = computed(() => teacherStore.classPerformance);

const onCourseSelect = () => {
    if (selectedCourseId.value) {
        teacherStore.fetchCourseAnalytics(selectedCourseId.value);
        teacherStore.fetchClassPerformance(selectedCourseId.value);
    }
};

const initChart = () => {
    if (!chartRef.value || !classPerformance.value || !Array.isArray(classPerformance.value.scoreDistribution)) return;
    chart = echarts.init(chartRef.value);

    const option = {
        tooltip: { trigger: 'item' },
        xAxis: {
            type: 'category',
            data: classPerformance.value.scoreDistribution.map(d => d.range),
        },
        yAxis: { type: 'value' },
        series: [{
            name: '学生人数',
            type: 'bar',
            data: classPerformance.value.scoreDistribution.map(d => d.count),
        }]
    };
    chart.setOption(option);
};

const resizeChart = () => {
    chart?.resize();
};

watch(classPerformance, (newData) => {
    if (newData) {
        nextTick(initChart);
    }
}, { deep: true });

onMounted(async () => {
  await courseStore.fetchCourses({ page: 1, size: 100 }); // Fetch all courses for selector
  if (courseStore.courses.length > 0) {
      selectedCourseId.value = String(courseStore.courses[0].id);
      onCourseSelect();
  }
  window.addEventListener('resize', resizeChart);
});

onUnmounted(() => {
    chart?.dispose();
    window.removeEventListener('resize', resizeChart);
});
</script>
