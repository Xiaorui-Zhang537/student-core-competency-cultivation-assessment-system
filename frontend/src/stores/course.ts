import { defineStore } from 'pinia';
import { courseApi } from '@/api/course.api';
import type { Course, CourseCategory, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
import type { PaginatedResponse, PaginationParams } from '@/types/api';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';
import { ref } from 'vue';

export const useCourseStore = defineStore('course', {
  state: () => ({
    courses: [] as Course[],
    pagination: {
      page: 1,
      size: 10,
      total: 0,
      totalPages: 0,
    },
    currentCourse: null as Course | null,
    loading: false,

    // For Course Discovery
    popularCourses: [] as Course[],
    recommendedCourses: [] as Course[],
    categories: [] as CourseCategory[],
    categoryCourses: [] as Course[],
    categoryPagination: {
      page: 1,
      size: 9,
      total: 0,
      totalPages: 0,
    },
    discoveryLoading: {
        popular: false,
        recommended: false,
        categories: false,
        categoryCourses: false,
    }
  }),
  actions: {
    async createCourse(courseData: CourseCreationRequest) {
      const uiStore = useUIStore();
      const response = await handleApiCall(() => courseApi.createCourse(courseData), uiStore, '课程创建失败');
      return response;
    },

    async fetchCourses(params: PaginationParams) {
        this.loading = true;
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.getCourses(params), uiStore, '获取课程列表失败');
        if (response) {
            this.courses = response.items;
            this.pagination = {
                page: response.page,
                size: response.size,
                total: response.total,
                totalPages: response.totalPages,
            };
        }
        this.loading = false;
    },

    async fetchCourseById(id: string) {
      this.loading = true;
      const uiStore = useUIStore();
      const response = await handleApiCall(() => courseApi.getCourseById(id), uiStore, '获取课程详情失败');
      if (response) {
        this.currentCourse = response;
      }
      this.loading = false;
      return this.currentCourse;
    },
    
    async updateCourse(id: string, courseData: CourseUpdateRequest) {
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.updateCourse(id, courseData), uiStore, '课程更新失败');
        return response;
    },

    async deleteCourse(id: string) {
        const uiStore = useUIStore();
        await handleApiCall(() => courseApi.deleteCourse(id), uiStore, '删除课程失败', { successMessage: '课程已删除' });
        // Optionally refetch courses
        await this.fetchCourses({ page: this.pagination.page, size: this.pagination.size });
    },

    async publishCourse(id: string) {
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.publishCourse(id), uiStore, '发布课程失败', { successMessage: '课程已发布' });
        if (response && this.currentCourse) {
            this.currentCourse.published = true;
        }
    },

    async enrollInCourse(courseId: string) {
        const uiStore = useUIStore();
        await handleApiCall(() => courseApi.enrollInCourse(courseId), uiStore, '课程报名失败', { successMessage: '报名成功！' });
    },

    // Actions for Course Discovery
    async fetchPopularCourses(limit: number = 8) {
        const uiStore = useUIStore();
        await handleApiCall(() => courseApi.getPopularCourses({ limit }), uiStore, '获取热门课程失败', { loadingRef: ref(this.discoveryLoading.popular) });
        if (response) {
            this.popularCourses = response;
        }
    },

    async fetchRecommendedCourses(limit: number = 6) {
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.getRecommendedCourses({ limit }), uiStore, '获取推荐课程失败', { loadingRef: ref(this.discoveryLoading.recommended) });
        if (response) {
            this.recommendedCourses = response;
        }
    },

    async fetchCategories() {
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.getCategories(), uiStore, '获取课程分类失败', { loadingRef: ref(this.discoveryLoading.categories) });
        if(response) {
            this.categories = response;
        }
    },
    
    async fetchCoursesByCategory(categoryId: string, params?: PaginationParams) {
        const uiStore = useUIStore();
        const queryParams = { ...params, page: this.categoryPagination.page, size: this.categoryPagination.size, ...params };
        const response = await handleApiCall(() => courseApi.getCoursesByCategory(categoryId, queryParams), uiStore, '获取分类课程失败', { loadingRef: ref(this.discoveryLoading.categoryCourses) });

        if (response) {
            const data = response as PaginatedResponse<Course>;
            this.categoryCourses = data.items;
            this.categoryPagination = {
                page: data.page,
                size: data.size,
                total: data.total,
                totalPages: data.totalPages,
            };
        }
    },

    async changeCategoryPage(categoryId: string, page: number) {
        this.categoryPagination.page = page;
        await this.fetchCoursesByCategory(categoryId);
    }
  },
});
