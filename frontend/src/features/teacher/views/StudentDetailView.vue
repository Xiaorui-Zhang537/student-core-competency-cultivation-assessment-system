<template>
  <div class="p-6">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <nav class="text-sm mb-2">
          <router-link to="/teacher/student-analytics" class="text-gray-500 hover:text-blue-600">学生进度概览</router-link>
          <span class="mx-2">/</span>
          <span class="font-medium">{{ studentName }}</span>
            </nav>
        <h1 class="text-3xl font-bold">学生详情: {{ studentName }}</h1>
      </div>

      <div v-if="gradeStore.loading" class="text-center card p-8">
          <p>正在加载学生成绩数据...</p>
            </div>
      
      <div v-else class="space-y-8">
          <!-- Stats -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="card p-4 text-center">
                  <h3 class="text-2xl font-bold">{{ gradedAssignmentsCount }}</h3>
                  <p class="text-sm text-gray-500">已评分作业</p>
              </div>
              <div class="card p-4 text-center">
                  <h3 class="text-2xl font-bold">{{ averageScore.toFixed(1) }}</h3>
                  <p class="text-sm text-gray-500">平均分</p>
              </div>
              <div class="card p-4 text-center">
                  <h3 class="text-2xl font-bold">{{ involvedCourses.length }}</h3>
                  <p class="text-sm text-gray-500">相关课程</p>
              </div>
            </div>
          
          <!-- Grades Table -->
          <div class="card overflow-x-auto">
              <div class="card-header">
                  <h2 class="text-xl font-semibold">成绩记录</h2>
              </div>
              <table class="w-full text-left">
                  <thead>
                      <tr class="border-b">
                          <th class="p-4">作业标题</th>
                          <th class="p-4">所属课程</th>
                          <th class="p-4">分数</th>
                          <th class="p-4">评分教师</th>
                          <th class="p-4">评分日期</th>
                          <th class="p-4">状态</th>
                    </tr>
                  </thead>
                  <tbody>
                      <tr v-for="grade in grades" :key="grade.id" class="border-b hover:bg-gray-50">
                          <td class="p-4 font-medium">{{ grade.assignmentTitle }}</td>
                          <td class="p-4">{{ grade.courseTitle }}</td>
                          <td class="p-4 font-bold">{{ grade.score }}</td>
                          <td class="p-4">{{ grade.teacherName }}</td>
                          <td class="p-4">{{ new Date(grade.gradedAt).toLocaleDateString() }}</td>
                          <td class="p-4">
                              <span class="badge" :class="grade.isPublished ? 'badge-success' : 'badge-warning'">
                                  {{ grade.isPublished ? '已发布' : '未发布' }}
                              </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              <div v-if="grades.length === 0" class="text-center p-8">
                  <p>该学生暂无成绩记录。</p>
              </div>
              <div class="p-4 flex items-center justify-between">
                <div class="flex items-center space-x-2">
                  <span class="text-sm text-gray-700">每页</span>
                  <select class="input input-sm w-20" v-model.number="pageSize" @change="fetchPage(1)">
                    <option :value="10">10</option>
                    <option :value="20">20</option>
                    <option :value="50">50</option>
                  </select>
                  <span class="text-sm text-gray-700">条</span>
                </div>
                <div class="flex items-center space-x-2">
                  <button class="btn btn-sm btn-outline" :disabled="currentPage===1" @click="fetchPage(currentPage-1)">上一页</button>
                  <span class="text-sm">第 {{ currentPage }} / {{ totalPages }} 页</span>
                  <button class="btn btn-sm btn-outline" :disabled="currentPage>=totalPages" @click="fetchPage(currentPage+1)">下一页</button>
                </div>
              </div>
          </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useGradeStore } from '@/stores/grade';

const route = useRoute();
const gradeStore = useGradeStore();

const studentId = ref<string | null>(null);
const studentName = ref(route.query.name as string || '学生');

const grades = computed(() => gradeStore.grades);

const gradedAssignmentsCount = computed(() => grades.value.length);

const averageScore = computed(() => {
    if (grades.value.length === 0) return 0;
    const total = grades.value.reduce((sum, grade) => sum + grade.score, 0);
    return total / grades.value.length;
});

const involvedCourses = computed(() => {
    const courseTitles = new Set(grades.value.map(g => g.courseTitle));
    return Array.from(courseTitles);
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = computed(() => gradeStore.totalGrades);
const totalPages = computed(() => Math.max(1, Math.ceil((total.value || 0) / pageSize.value)));

async function fetchPage(page: number) {
  if (!studentId.value) return;
  currentPage.value = page;
  await gradeStore.fetchGradesByStudent(studentId.value, { page: currentPage.value, size: pageSize.value });
}

onMounted(() => {
    const id = route.params.id as string;
    if (id) {
        studentId.value = id;
        fetchPage(1);
    }
});
</script>
