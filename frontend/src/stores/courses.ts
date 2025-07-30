import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Course, Lesson, CourseFilter, CourseStats } from '@/types/course'
import { coursesAPI } from '@/api/courses.api'

export const useCoursesStore = defineStore('courses', () => {
  // 状态
  const courses = ref<Course[]>([])
  const currentCourse = ref<Course | null>(null)
  const lessons = ref<Lesson[]>([])
  const stats = ref<CourseStats | null>(null)
  const loading = ref(false)
  const filter = ref<CourseFilter>({})

  // 计算属性
  const filteredCourses = computed(() => {
    let result = courses.value

    if (filter.value.search) {
      const search = filter.value.search.toLowerCase()
      result = result.filter(course => 
        course.title.toLowerCase().includes(search) ||
        course.description.toLowerCase().includes(search)
      )
    }

    if (filter.value.category) {
      result = result.filter(course => course.category === filter.value.category)
    }

    if (filter.value.level) {
      result = result.filter(course => course.level === filter.value.level)
    }

    return result
  })

  const enrolledCourses = computed(() => 
    courses.value.filter(course => course.isEnrolled)
  )

  const completedCourses = computed(() => 
    enrolledCourses.value.filter(course => course.progress === 100)
  )

  const inProgressCourses = computed(() => 
    enrolledCourses.value.filter(course => course.progress > 0 && course.progress < 100)
  )

  // 方法
  const fetchCourses = async (params?: any) => {
    loading.value = true
    try {
      const response = await coursesAPI.getCourses(params)
      courses.value = response.data.items
      return response
    } finally {
      loading.value = false
    }
  }

  const fetchMyCourses = async () => {
    loading.value = true
    try {
      const response = await coursesAPI.getMyCourses()
      courses.value = response.data
      return response
    } finally {
      loading.value = false
    }
  }

  const fetchCourse = async (id: string) => {
    loading.value = true
    try {
      const response = await coursesAPI.getCourse(id)
      currentCourse.value = response.data
      return response
    } finally {
      loading.value = false
    }
  }

  const fetchStats = async () => {
    const response = await coursesAPI.getCourseStats()
    stats.value = response.data
    return response
  }

  const enrollCourse = async (courseId: string) => {
    const response = await coursesAPI.enrollCourse(courseId)
    // 更新本地状态
    const course = courses.value.find(c => c.id === courseId)
    if (course) {
      course.isEnrolled = true
    }
    return response
  }

  const unenrollCourse = async (courseId: string) => {
    const response = await coursesAPI.unenrollCourse(courseId)
    
    // 更新本地课程状态
    const courseIndex = courses.value.findIndex(c => c.id === courseId)
    if (courseIndex !== -1) {
      courses.value[courseIndex].isEnrolled = false
      courses.value[courseIndex].progress = 0
    }
    
    return response
  }

  const fetchLessons = async (courseId: string) => {
    loading.value = true
    try {
      const response = await coursesAPI.getLessons(courseId)
      lessons.value = response.data
      return response
    } finally {
      loading.value = false
    }
  }

  const completeLesson = async (courseId: string, lessonId: string) => {
    const response = await coursesAPI.completeLesson(courseId, lessonId)
    
    // 更新本地课时状态
    const lessonIndex = lessons.value.findIndex(l => l.id === lessonId)
    if (lessonIndex !== -1) {
      lessons.value[lessonIndex].isCompleted = true
    }
    
    // 更新课程进度
    const course = courses.value.find(c => c.id === courseId)
    if (course && lessons.value.length > 0) {
      const completedLessons = lessons.value.filter(l => l.isCompleted).length
      course.progress = (completedLessons / lessons.value.length) * 100
    }
    
    return response
  }

  const updateFilter = (newFilter: Partial<CourseFilter>) => {
    filter.value = { ...filter.value, ...newFilter }
  }

  const clearFilter = () => {
    filter.value = {}
  }

  const searchCourses = async (query: string) => {
    loading.value = true
    try {
      const response = await coursesAPI.searchCourses(query)
      courses.value = response.data
      return response
    } finally {
      loading.value = false
    }
  }

  return {
    // 状态
    courses,
    currentCourse,
    lessons,
    stats,
    loading,
    filter,
    // 计算属性
    filteredCourses,
    enrolledCourses,
    completedCourses,
    inProgressCourses,
    // 方法
    fetchCourses,
    fetchMyCourses,
    fetchCourse,
    fetchStats,
    enrollCourse,
    unenrollCourse,
    fetchLessons,
    completeLesson,
    updateFilter,
    clearFilter,
    searchCourses
  }
}) 