<template>
  <transition name="success-pop">
    <div v-if="visible" class="fixed inset-0 z-[9999] flex items-center justify-center pointer-events-none">
      <div class="success-circle">
        <svg class="success-check" viewBox="0 0 52 52">
          <circle class="success-circle-bg" cx="26" cy="26" r="25" fill="none"/>
          <path class="success-check-path" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
        </svg>
        <div v-if="message" class="success-message">{{ message }}</div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  show: boolean
  message?: string
  duration?: number
}>()

const emit = defineEmits<{ (e: 'done'): void }>()
const visible = ref(false)

watch(() => props.show, (v) => {
  if (v) {
    visible.value = true
    setTimeout(() => {
      visible.value = false
      emit('done')
    }, props.duration || 1500)
  }
})
</script>

<style scoped>
.success-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}
.success-check {
  width: 72px;
  height: 72px;
  filter: drop-shadow(0 4px 20px rgba(16, 185, 129, 0.4));
}
.success-circle-bg {
  stroke: #10b981;
  stroke-width: 2;
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  animation: circle-draw 0.6s ease forwards;
}
.success-check-path {
  stroke: #10b981;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  animation: check-draw 0.3s ease 0.4s forwards;
}
.success-message {
  font-size: 0.875rem;
  font-weight: 600;
  color: #10b981;
  text-shadow: 0 1px 4px rgba(0,0,0,0.1);
}
@keyframes circle-draw {
  to { stroke-dashoffset: 0; }
}
@keyframes check-draw {
  to { stroke-dashoffset: 0; }
}
.success-pop-enter-active { animation: pop-in 0.3s ease; }
.success-pop-leave-active { animation: pop-out 0.2s ease; }
@keyframes pop-in { 0% { opacity: 0; transform: scale(0.5); } 100% { opacity: 1; transform: scale(1); } }
@keyframes pop-out { 0% { opacity: 1; transform: scale(1); } 100% { opacity: 0; transform: scale(0.5); } }
</style>
