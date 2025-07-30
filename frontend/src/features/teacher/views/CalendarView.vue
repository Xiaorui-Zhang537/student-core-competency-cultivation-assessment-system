<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">教学日历</h1>
            <p class="text-gray-600 dark:text-gray-400">管理课程安排、作业截止和重要事件</p>
          </div>
          <div class="flex items-center space-x-3">
            <select v-model="currentView" class="input">
              <option value="month">月视图</option>
              <option value="week">周视图</option>
              <option value="day">日视图</option>
            </select>
            <button variant="outline" @click="goToToday">
              <calendar-icon class="w-4 h-4 mr-2" />
              今天
            </button>
            <button variant="primary" @click="showAddEventModal = true">
              <plus-icon class="w-4 h-4 mr-2" />
              添加事件
            </button>
          </div>
        </div>
      </div>

      <!-- 日历导航 -->
      <card padding="lg" class="mb-8">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <button variant="ghost" @click="previousPeriod">
                <chevron-left-icon class="w-4 h-4" />
              </button>
              <button variant="ghost" @click="nextPeriod">
                <chevron-right-icon class="w-4 h-4" />
              </button>
            </div>
            <h2 class="text-xl font-semibold text-gray-900 dark:text-white">
              {{ formatCurrentPeriod() }}
            </h2>
          </div>
          
          <!-- 快速筛选 -->
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-600 dark:text-gray-400">显示:</span>
              <label class="flex items-center">
                <input
                  v-model="filters.courses"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-1 text-sm text-gray-700 dark:text-gray-300">课程</span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="filters.assignments"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-1 text-sm text-gray-700 dark:text-gray-300">作业</span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="filters.events"
                  type="checkbox"
                  class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                />
                <span class="ml-1 text-sm text-gray-700 dark:text-gray-300">事件</span>
              </label>
            </div>
          </div>
        </div>
      </card>

      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧日历 -->
        <div class="lg:col-span-3">
          <card padding="lg">
            <!-- 月视图 -->
            <div v-if="currentView === 'month'" class="space-y-4">
              <!-- 星期标题 -->
              <div class="grid grid-cols-7 gap-1">
                <div
                  v-for="day in weekDays"
                  :key="day"
                  class="p-2 text-center text-sm font-medium text-gray-700 dark:text-gray-300"
                >
                  {{ day }}
                </div>
              </div>
              
              <!-- 日期网格 -->
              <div class="grid grid-cols-7 gap-1">
                <div
                  v-for="date in calendarDates"
                  :key="date.dateString"
                  @click="selectDate(date)"
                  class="min-h-24 p-1 border border-gray-200 dark:border-gray-600 rounded cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                  :class="{
                    'bg-gray-100 dark:bg-gray-700': !date.isCurrentMonth,
                    'bg-primary-50 dark:bg-primary-900/20 border-primary-200 dark:border-primary-700': date.isToday,
                    'bg-blue-50 dark:bg-blue-900/20': selectedDate === date.dateString
                  }"
                >
                  <div class="flex justify-between items-start mb-1">
                    <span
                      class="text-sm font-medium"
                      :class="{
                        'text-gray-400': !date.isCurrentMonth,
                        'text-primary-600 dark:text-primary-400': date.isToday,
                        'text-gray-900 dark:text-white': date.isCurrentMonth && !date.isToday
                      }"
                    >
                      {{ date.day }}
                    </span>
                    <div v-if="date.events.length > 3" class="text-xs text-gray-500">
                      +{{ date.events.length - 3 }}
                    </div>
                  </div>
                  
                  <!-- 事件列表 -->
                  <div class="space-y-1">
                    <div
                      v-for="event in date.events.slice(0, 3)"
                      :key="event.id"
                      @click.stop="viewEvent(event)"
                      class="px-1 py-0.5 rounded text-xs truncate cursor-pointer"
                      :class="getEventClass(event.type)"
                    >
                      {{ event.title }}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 周视图 -->
            <div v-else-if="currentView === 'week'" class="space-y-4">
              <!-- 时间轴周视图 -->
              <div class="grid grid-cols-8 gap-1">
                <!-- 时间列 -->
                <div class="space-y-12">
                  <div class="h-8"></div> <!-- 标题空间 -->
                  <div
                    v-for="hour in 24"
                    :key="hour"
                    class="h-12 text-xs text-gray-500 text-right pr-2"
                  >
                    {{ hour - 1 }}:00
                  </div>
                </div>
                
                <!-- 日期列 -->
                <div
                  v-for="date in weekDates"
                  :key="date.dateString"
                  class="space-y-1"
                >
                  <!-- 日期标题 -->
                  <div class="h-8 text-center border-b border-gray-200 dark:border-gray-600">
                    <div
                      class="text-sm font-medium"
                      :class="date.isToday ? 'text-primary-600' : 'text-gray-900 dark:text-white'"
                    >
                      {{ date.weekDay }}
                    </div>
                    <div
                      class="text-xs"
                      :class="date.isToday ? 'text-primary-500' : 'text-gray-500'"
                    >
                      {{ date.day }}
                    </div>
                  </div>
                  
                  <!-- 时间槽 -->
                  <div class="relative">
                    <div
                      v-for="hour in 24"
                      :key="hour"
                      class="h-12 border-t border-gray-100 dark:border-gray-700"
                    ></div>
                    
                    <!-- 事件块 -->
                    <div
                      v-for="event in getDateEvents(date)"
                      :key="event.id"
                      @click="viewEvent(event)"
                      class="absolute left-0 right-0 mx-1 px-2 py-1 rounded cursor-pointer"
                      :class="getEventClass(event.type)"
                      :style="getEventStyle(event)"
                    >
                      <div class="text-xs font-medium truncate">{{ event.title }}</div>
                      <div class="text-xs opacity-75">{{ event.time }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 日视图 -->
            <div v-else class="space-y-4">
              <div class="text-center">
                <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
                  {{ formatSelectedDate() }}
                </h3>
              </div>
              
              <div class="space-y-2">
                <div
                  v-for="event in selectedDateEvents"
                  :key="event.id"
                  @click="viewEvent(event)"
                  class="p-4 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
                >
                  <div class="flex items-start justify-between">
                    <div class="flex-1">
                      <h4 class="font-medium text-gray-900 dark:text-white">{{ event.title }}</h4>
                      <p class="text-sm text-gray-600 dark:text-gray-400">{{ event.description }}</p>
                      <div class="flex items-center space-x-4 mt-2 text-xs text-gray-500">
                        <span>{{ event.time }}</span>
                        <span>{{ event.location }}</span>
                      </div>
                    </div>
                    <badge :variant="getEventVariant(event.type)">
                      {{ getEventTypeText(event.type) }}
                    </badge>
                  </div>
                </div>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧边栏 -->
        <div class="lg:col-span-1 space-y-6">
          <!-- 今日议程 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">今日议程</h3>
            </template>
            
            <div class="space-y-3">
              <div
                v-for="event in todayEvents"
                :key="event.id"
                class="p-3 border border-gray-200 dark:border-gray-600 rounded-lg"
              >
                <h4 class="font-medium text-gray-900 dark:text-white text-sm">{{ event.title }}</h4>
                <p class="text-xs text-gray-500 mt-1">{{ event.time }}</p>
                <badge :variant="getEventVariant(event.type)" size="sm" class="mt-2">
                  {{ getEventTypeText(event.type) }}
                </badge>
              </div>
              
              <div v-if="todayEvents.length === 0" class="text-center py-4">
                <p class="text-sm text-gray-500">今天没有安排</p>
              </div>
            </div>
          </card>

          <!-- 即将到来的截止日期 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">即将截止</h3>
            </template>
            
            <div class="space-y-3">
              <div
                v-for="deadline in upcomingDeadlines"
                :key="deadline.id"
                class="flex items-center justify-between p-3 border border-gray-200 dark:border-gray-600 rounded-lg"
              >
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-white text-sm">{{ deadline.title }}</h4>
                  <p class="text-xs text-gray-500">{{ deadline.course }}</p>
                </div>
                <div class="text-right">
                  <p class="text-xs font-medium" :class="getDeadlineColor(deadline.daysLeft)">
                    {{ deadline.daysLeft === 0 ? '今天' : `${deadline.daysLeft}天后` }}
                  </p>
                </div>
              </div>
            </div>
          </card>

          <!-- 快速操作 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">快速操作</h3>
            </template>
            
            <div class="space-y-2">
              <button variant="outline" size="sm" class="w-full justify-start" @click="addCourseSchedule">
                <book-open-icon class="w-4 h-4 mr-2" />
                安排课程
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="addAssignmentDeadline">
                <document-text-icon class="w-4 h-4 mr-2" />
                设置作业截止
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="addExamSchedule">
                <academic-cap-icon class="w-4 h-4 mr-2" />
                安排考试
              </button>
              <button variant="outline" size="sm" class="w-full justify-start" @click="addMeeting">
                <users-icon class="w-4 h-4 mr-2" />
                安排会议
              </button>
            </div>
          </card>

          <!-- 事件统计 -->
          <card padding="lg">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">本月统计</h3>
            </template>
            
            <div class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">课程数:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ monthStats.courses }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">作业截止:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ monthStats.assignments }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">考试安排:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ monthStats.exams }}</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-gray-600 dark:text-gray-400">会议:</span>
                <span class="font-medium text-gray-900 dark:text-white">{{ monthStats.meetings }}</span>
              </div>
            </div>
          </card>
        </div>
      </div>

      <!-- 添加事件弹窗 -->
      <div v-if="showAddEventModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">添加新事件</h3>
          <form @submit.prevent="createEvent" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                事件标题
              </label>
              <input
                v-model="newEvent.title"
                type="text"
                placeholder="输入事件标题"
                class="input"
                required
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                事件类型
              </label>
              <select v-model="newEvent.type" class="input">
                <option value="course">课程</option>
                <option value="assignment">作业截止</option>
                <option value="exam">考试</option>
                <option value="meeting">会议</option>
                <option value="event">其他事件</option>
              </select>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  日期
                </label>
                <input
                  v-model="newEvent.date"
                  type="date"
                  class="input"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  时间
                </label>
                <input
                  v-model="newEvent.time"
                  type="time"
                  class="input"
                />
              </div>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                地点
              </label>
              <input
                v-model="newEvent.location"
                type="text"
                placeholder="输入地点"
                class="input"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                描述
              </label>
              <textarea
                v-model="newEvent.description"
                rows="3"
                placeholder="输入事件描述"
                class="input"
              ></textarea>
            </div>
            
            <div class="flex justify-end space-x-3 pt-4">
              <button variant="outline" @click="showAddEventModal = false">
                取消
              </button>
              <button type="submit" variant="primary" :loading="isCreatingEvent">
                创建事件
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 查看事件弹窗 -->
      <div v-if="showEventModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ selectedEvent?.title }}</h3>
            <div class="flex items-center space-x-2">
              <button variant="ghost" size="sm" @click="editEvent">
                <pencil-icon class="w-4 h-4" />
              </button>
              <button variant="ghost" size="sm" @click="deleteEvent" class="text-red-600">
                <trash-icon class="w-4 h-4" />
              </button>
            </div>
          </div>
          
          <div class="space-y-4">
            <div>
              <badge :variant="getEventVariant(selectedEvent?.type || 'event')">
                {{ getEventTypeText(selectedEvent?.type || 'event') }}
              </badge>
            </div>
            
            <div class="space-y-2 text-sm">
              <div class="flex items-center space-x-2">
                <calendar-icon class="w-4 h-4 text-gray-500" />
                <span>{{ formatEventDate(selectedEvent?.date) }}</span>
              </div>
              <div v-if="selectedEvent?.time" class="flex items-center space-x-2">
                <clock-icon class="w-4 h-4 text-gray-500" />
                <span>{{ selectedEvent.time }}</span>
              </div>
              <div v-if="selectedEvent?.location" class="flex items-center space-x-2">
                <map-pin-icon class="w-4 h-4 text-gray-500" />
                <span>{{ selectedEvent.location }}</span>
              </div>
            </div>
            
            <div v-if="selectedEvent?.description">
              <p class="text-sm text-gray-600 dark:text-gray-400">{{ selectedEvent.description }}</p>
            </div>
          </div>
          
          <div class="mt-6 flex justify-end">
            <button variant="outline" @click="showEventModal = false">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  CalendarIcon,
  PlusIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  BookOpenIcon,
  DocumentTextIcon,
  AcademicCapIcon,
  UsersIcon,
  ClockIcon,
  MapPinIcon,
  PencilIcon,
  TrashIcon
} from '@heroicons/vue/24/outline'

// Stores
const uiStore = useUIStore()

// 状态
const currentView = ref<'month' | 'week' | 'day'>('month')
const currentDate = ref(new Date())
const selectedDate = ref('')
const showAddEventModal = ref(false)
const showEventModal = ref(false)
const isCreatingEvent = ref(false)
const selectedEvent = ref<any>(null)

// 筛选器
const filters = reactive({
  courses: true,
  assignments: true,
  events: true
})

// 新事件表单
const newEvent = reactive({
  title: '',
  type: 'course',
  date: '',
  time: '',
  location: '',
  description: ''
})

// 星期名称
const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 模拟事件数据
const events = ref([
  {
    id: '1',
    title: '高等数学课程',
    type: 'course',
    date: '2024-01-15',
    time: '09:00',
    location: '教学楼A-101',
    description: '第三章：导数与微分',
    course: '高等数学'
  },
  {
    id: '2',
    title: '期中作业截止',
    type: 'assignment',
    date: '2024-01-16',
    time: '23:59',
    location: '在线提交',
    description: '微积分练习题',
    course: '高等数学'
  },
  {
    id: '3',
    title: '期末考试',
    type: 'exam',
    date: '2024-01-20',
    time: '14:00',
    location: '考试中心',
    description: '高等数学期末考试',
    course: '高等数学'
  },
  {
    id: '4',
    title: '教学会议',
    type: 'meeting',
    date: '2024-01-18',
    time: '10:00',
    location: '会议室B-203',
    description: '讨论下学期教学计划',
    course: ''
  }
])

// 统计数据
const monthStats = reactive({
  courses: 45,
  assignments: 23,
  exams: 8,
  meetings: 12
})

// 计算属性
const calendarDates = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const firstDayOfWeek = firstDay.getDay()
  const daysInMonth = lastDay.getDate()
  
  const dates = []
  
  // 添加上个月的日期
  const prevMonth = new Date(year, month - 1, 0)
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const date = new Date(year, month - 1, prevMonth.getDate() - i)
    dates.push({
      date,
      day: date.getDate(),
      dateString: formatDateString(date),
      isCurrentMonth: false,
      isToday: isToday(date),
      events: getEventsForDate(date)
    })
  }
  
  // 添加当月日期
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day)
    dates.push({
      date,
      day,
      dateString: formatDateString(date),
      isCurrentMonth: true,
      isToday: isToday(date),
      events: getEventsForDate(date)
    })
  }
  
  // 添加下个月的日期
  const remainingCells = 42 - dates.length
  for (let day = 1; day <= remainingCells; day++) {
    const date = new Date(year, month + 1, day)
    dates.push({
      date,
      day,
      dateString: formatDateString(date),
      isCurrentMonth: false,
      isToday: isToday(date),
      events: getEventsForDate(date)
    })
  }
  
  return dates
})

const weekDates = computed(() => {
  const dates = []
  const startOfWeek = getStartOfWeek(currentDate.value)
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)
    dates.push({
      date,
      day: date.getDate(),
      weekDay: weekDays[i],
      dateString: formatDateString(date),
      isToday: isToday(date),
      events: getEventsForDate(date)
    })
  }
  
  return dates
})

const todayEvents = computed(() => {
  const today = new Date()
  return getEventsForDate(today)
})

const selectedDateEvents = computed(() => {
  if (!selectedDate.value) return []
  const date = new Date(selectedDate.value)
  return getEventsForDate(date)
})

const upcomingDeadlines = computed(() => {
  const today = new Date()
  return events.value
    .filter(event => event.type === 'assignment' || event.type === 'exam')
    .map(event => {
      const eventDate = new Date(event.date)
      const diffTime = eventDate.getTime() - today.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      return {
        ...event,
        daysLeft: Math.max(0, diffDays)
      }
    })
    .filter(event => event.daysLeft <= 7)
    .sort((a, b) => a.daysLeft - b.daysLeft)
})

// 方法
const formatDateString = (date: Date) => {
  return date.toISOString().split('T')[0]
}

const isToday = (date: Date) => {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

const getStartOfWeek = (date: Date) => {
  const d = new Date(date)
  const day = d.getDay()
  const diff = d.getDate() - day
  return new Date(d.setDate(diff))
}

const getEventsForDate = (date: Date) => {
  const dateString = formatDateString(date)
  return events.value.filter(event => {
    if (!filters.courses && event.type === 'course') return false
    if (!filters.assignments && event.type === 'assignment') return false
    if (!filters.events && ['exam', 'meeting', 'event'].includes(event.type)) return false
    return event.date === dateString
  })
}

const getDateEvents = (dateObj: any) => {
  return dateObj.events
}

const formatCurrentPeriod = () => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  if (currentView.value === 'month') {
    return `${year}年${month + 1}月`
  } else if (currentView.value === 'week') {
    const startOfWeek = getStartOfWeek(currentDate.value)
    const endOfWeek = new Date(startOfWeek)
    endOfWeek.setDate(startOfWeek.getDate() + 6)
    return `${startOfWeek.getMonth() + 1}月${startOfWeek.getDate()}日 - ${endOfWeek.getMonth() + 1}月${endOfWeek.getDate()}日`
  } else {
    return `${year}年${month + 1}月${currentDate.value.getDate()}日`
  }
}

const formatSelectedDate = () => {
  if (!selectedDate.value) return ''
  const date = new Date(selectedDate.value)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

const formatEventDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

const getEventClass = (type: string) => {
  switch (type) {
    case 'course':
      return 'bg-blue-100 dark:bg-blue-900 text-blue-800 dark:text-blue-200'
    case 'assignment':
      return 'bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200'
    case 'exam':
      return 'bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200'
    case 'meeting':
      return 'bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200'
    default:
      return 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200'
  }
}

const getEventVariant = (type: string) => {
  switch (type) {
    case 'course': return 'primary'
    case 'assignment': return 'warning'
    case 'exam': return 'danger'
    case 'meeting': return 'success'
    default: return 'secondary'
  }
}

const getEventTypeText = (type: string) => {
  switch (type) {
    case 'course': return '课程'
    case 'assignment': return '作业'
    case 'exam': return '考试'
    case 'meeting': return '会议'
    default: return '事件'
  }
}

const getEventStyle = (event: any) => {
  if (!event.time) return {}
  
  const [hours, minutes] = event.time.split(':').map(Number)
  const top = (hours * 48) + (minutes * 0.8) // 每小时48px高度
  
  return {
    top: `${top}px`,
    height: '40px' // 默认事件高度
  }
}

const getDeadlineColor = (daysLeft: number) => {
  if (daysLeft === 0) return 'text-red-600'
  if (daysLeft <= 2) return 'text-orange-600'
  return 'text-gray-600'
}

const previousPeriod = () => {
  const newDate = new Date(currentDate.value)
  if (currentView.value === 'month') {
    newDate.setMonth(newDate.getMonth() - 1)
  } else if (currentView.value === 'week') {
    newDate.setDate(newDate.getDate() - 7)
  } else {
    newDate.setDate(newDate.getDate() - 1)
  }
  currentDate.value = newDate
}

const nextPeriod = () => {
  const newDate = new Date(currentDate.value)
  if (currentView.value === 'month') {
    newDate.setMonth(newDate.getMonth() + 1)
  } else if (currentView.value === 'week') {
    newDate.setDate(newDate.getDate() + 7)
  } else {
    newDate.setDate(newDate.getDate() + 1)
  }
  currentDate.value = newDate
}

const goToToday = () => {
  currentDate.value = new Date()
  selectedDate.value = formatDateString(new Date())
}

const selectDate = (date: any) => {
  selectedDate.value = date.dateString
  if (currentView.value !== 'day') {
    currentView.value = 'day'
    currentDate.value = date.date
  }
}

const viewEvent = (event: any) => {
  selectedEvent.value = event
  showEventModal.value = true
}

const createEvent = async () => {
  if (!newEvent.title.trim() || !newEvent.date) return

  isCreatingEvent.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))

    const event = {
      id: Date.now().toString(),
      title: newEvent.title,
      type: newEvent.type,
      date: newEvent.date,
      time: newEvent.time,
      location: newEvent.location,
      description: newEvent.description,
      course: newEvent.type === 'course' ? newEvent.title : ''
    }

    events.value.push(event)

    uiStore.showNotification({
      type: 'success',
      title: '事件创建成功',
      message: `${event.title} 已添加到日历`
    })

    showAddEventModal.value = false
    Object.assign(newEvent, {
      title: '',
      type: 'course',
      date: '',
      time: '',
      location: '',
      description: ''
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: '创建失败',
      message: '创建事件时发生错误'
    })
  } finally {
    isCreatingEvent.value = false
  }
}

const editEvent = () => {
  uiStore.showNotification({
    type: 'info',
    title: '编辑事件',
    message: '事件编辑功能开发中...'
  })
}

const deleteEvent = () => {
  if (confirm('确定要删除这个事件吗？')) {
    const index = events.value.findIndex(e => e.id === selectedEvent.value?.id)
    if (index > -1) {
      events.value.splice(index, 1)
      showEventModal.value = false
      uiStore.showNotification({
        type: 'success',
        title: '删除成功',
        message: '事件已删除'
      })
    }
  }
}

const addCourseSchedule = () => {
  newEvent.type = 'course'
  showAddEventModal.value = true
}

const addAssignmentDeadline = () => {
  newEvent.type = 'assignment'
  showAddEventModal.value = true
}

const addExamSchedule = () => {
  newEvent.type = 'exam'
  showAddEventModal.value = true
}

const addMeeting = () => {
  newEvent.type = 'meeting'
  showAddEventModal.value = true
}

// 生命周期
onMounted(() => {
  selectedDate.value = formatDateString(new Date())
  newEvent.date = formatDateString(new Date())
})
</script> 