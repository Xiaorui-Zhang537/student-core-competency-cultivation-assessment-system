<template>
  <div
    class="flex w-full max-w-lg flex-col gap-6 overflow-hidden rounded-3xl border p-8"
    :style="{ backgroundColor: 'rgb(var(--glass-bg-rgb) / var(--glass-alpha-regular))', borderColor: 'var(--glass-border-color)' }"
  >
    <text-morph :text="calendarData[activeIndex].day" class="w-fit font-bold text-[28px]" :morph-time="0.5" :cool-down-time="0.1" />

    <motion
      v-if="calendarData[activeIndex].events"
      :key="'event-container' + activeIndex"
      class="flex flex-col gap-4"
      :initial="{ x: 10, opacity: 0 }"
      :animate="{ x: 0, opacity: 1 }"
    >
      <motion class="flex items-center gap-2" :initial="{ x: 10, opacity: 0 }" :animate="{ x: 0, opacity: 1 }">
        <span class="inline-flex w-5 h-5 rounded bg-[color-mix(in_oklab,var(--color-primary)_70%,transparent)]"></span>
        <span class="font-medium text-base-content">Upcoming Events</span>
      </motion>

      <div class="flex flex-wrap gap-4">
        <motion
          v-for="event in calendarData[activeIndex].events"
          :key="event.title + event.time"
          class="w-full max-w-44 rounded-lg border p-3"
          :style="{ borderColor: 'var(--glass-border-color)' }"
          :initial="{ x: 10, opacity: 0 }"
          :animate="{ x: 0, opacity: 1 }"
        >
          <p class="text-sm font-medium text-base-content">{{ event.title }}</p>
          <p class="text-xs text-subtle">{{ event.day }}, {{ event.time }}</p>
        </motion>
      </div>
    </motion>

    <div class="flex flex-wrap gap-3">
      <motion
        v-for="(day, index) in calendarData"
        :key="day.date + '-' + index"
        class="flex flex-col rounded-2xl border p-3 text-center opacity-100 duration-200 hover:bg-[color-mix(in_oklab,var(--color-base-content)_6%,transparent)] hover:scale-[1.04] active:scale-95"
        :style="{ borderColor: 'var(--glass-border-color)' }"
        :class="activeIndex === index ? 'bg-[color-mix(in_oklab,var(--color-info)_12%,transparent)]' : ''"
        :initial="{ scale: 1 }"
        :animate="{ scale: 1 }"
        @click="setActive(index)"
      >
        <span class="text-[10px] font-medium uppercase text-subtle">{{ day.month }}</span>
        <span class="font-semibold text-base-content">{{ day.date }}</span>
        <span class="text-[10px] font-medium uppercase" :style="{ color: 'var(--color-primary)' }">{{ day.day }}</span>
      </motion>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Motion } from '@motionone/vue'
import TextMorph from '@/components/ui/TextMorph.vue'

interface CalendarEvent { title: string; day: string; time: string }
interface CalendarDay { month: string; date: number; day: string; events?: CalendarEvent[] }

const props = defineProps<{ calendarData: CalendarDay[]; initialIndex?: number }>()
const emit = defineEmits<{ (e: 'update:activeIndex', value: number): void }>()
const activeIndex = ref(props.initialIndex ?? 0)
function setActive(index: number) { activeIndex.value = index; emit('update:activeIndex', index) }
</script>

