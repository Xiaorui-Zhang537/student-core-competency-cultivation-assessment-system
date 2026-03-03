import { defineStore } from 'pinia';
import { courseApi } from '@/api/course.api';
import type { Course, CourseCreationRequest, CourseUpdateRequest } from '@/types/course';
import type { PaginatedResponse } from '@/types/api';
import { useUIStore } from './ui';
import { handleApiCall } from '@/utils/api-handler';

// ◇ Utility to handle possible AxiosResponse wrappers
function unwrap<T>(res: any): T {
  return res && res.data !== undefined ? res.data : res;
}

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
  }),
  actions: {
    async createCourse(courseData: CourseCreationRequest) {
      const uiStore = useUIStore();
      const response = await handleApiCall(() => courseApi.createCourse(courseData), uiStore, '课程创建失败');
      return response;
    },

    async fetchCourses(params: { page?: number; size?: number; query?: string; status?: string; teacherId?: string; }) {
        this.loading = true;
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.getCourses(params), uiStore, '获取课程列表失败');
        if (response) {
            const data = unwrap<PaginatedResponse<Course>>(response);
            this.courses = data.items;
            this.pagination = {
                page: data.page,
                size: data.size,
                total: data.total,
                totalPages: data.totalPages,
            };
        }
        this.loading = false;
    },

    async fetchCourseById(id: string) {
      this.loading = true;
      const uiStore = useUIStore();
      const response = await handleApiCall(() => courseApi.getCourseById(id), uiStore, '获取课程详情失败');
      if (response) {
        this.currentCourse = unwrap<Course>(response);
      }
      this.loading = false;
      return this.currentCourse;
    },
    
    async updateCourse(id: string, courseData: CourseUpdateRequest) {
        const uiStore = useUIStore();
        const response = await handleApiCall(() => courseApi.updateCourse(id, courseData), uiStore, '课程更新失败');
        return response;
    },

    async deleteCourse(id: string, opts?: { notify?: boolean }) {
        const uiStore = useUIStore();
        if (opts?.notify === false) {
            await handleApiCall(() => courseApi.deleteCourse(id), uiStore, '删除课程失败');
        } else {
            await handleApiCall(() => courseApi.deleteCourse(id), uiStore, '删除课程失败', { successMessage: '课程已删除' });
        }
        // Optionally refetch courses
        await this.fetchCourses({ page: this.pagination.page, size: this.pagination.size });
    },

    async publishCourse(id: string, opts?: { notify?: boolean }) {
        const uiStore = useUIStore();
        uiStore.setLoading(true)
        try {
            await courseApi.publishCourse(Number(id) as any)
            if (this.currentCourse) this.currentCourse.isPublished = true;
            return true
        } catch (e: any) {
            const msg = String(e?.message || '发布课程失败')
            // 幂等：后端提示“已发布”时视为成功，避免弹出错误影响体验
            if (/已发布/.test(msg)) {
                if (this.currentCourse) this.currentCourse.isPublished = true;
                return true
            }
            uiStore.showNotification({
                type: 'error',
                title: '发布失败',
                message: msg
            })
            return false
        } finally {
            uiStore.setLoading(false)
        }
    },

    async enrollInCourse(courseId: string, enrollKey?: string) {
        const uiStore = useUIStore();
        await handleApiCall(() => courseApi.enrollInCourse(courseId, enrollKey), uiStore, '课程报名失败', { successMessage: '报名成功！' });
    },

    async unenrollFromCourse(courseId: string) {
        const uiStore = useUIStore();
        await handleApiCall(() => courseApi.unenrollFromCourse(courseId), uiStore, '退课失败', { successMessage: '已退课' });
    }
  },
});
