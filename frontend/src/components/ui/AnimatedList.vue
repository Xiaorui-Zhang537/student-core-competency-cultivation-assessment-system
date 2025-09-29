<template>
  <div class="animated-list-container">
    <transition-group
      name="animated-list"
      tag="div"
      class="animated-list"
    >
      <div
        v-for="item in displayItems"
        :key="item.__key"
        class="animated-list-item"
      >
        <slot name="item" :item="item.payload" />
      </div>
    </transition-group>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, type PropType } from 'vue'

interface AnimatedItem<T = any> {
  __key: string
  payload: T
}

const props = defineProps({
  items: { type: Array as PropType<any[]>, required: true },
  delay: { type: Number, default: 120 }, // 每项添加间隔（毫秒）
  reverse: { type: Boolean, default: true }, // 新项置顶
})

const displayItems = ref<AnimatedItem[]>([])
let timer: number | null = null

const clearTimer = () => {
  if (timer !== null) {
    window.clearInterval(timer)
    timer = null
  }
}

const enqueueAll = () => {
  clearTimer()
  const queue = (props.items || []).map((p, idx) => ({ __key: `${Date.now()}-${idx}-${Math.random().toString(36).slice(2)}`, payload: p }))
  let i = 0
  if (queue.length === 0) { displayItems.value = []; return }
  // 逐个加入，形成顺序入场效果
  timer = window.setInterval(() => {
    const next = queue[i]
    if (!next) { clearTimer(); return }
    if (props.reverse) displayItems.value = [next, ...displayItems.value]
    else displayItems.value = [...displayItems.value, next]
    i++
    if (i >= queue.length) clearTimer()
  }, Math.max(16, props.delay)) as unknown as number
}

watch(() => props.items, () => {
  // 当 items 变化，重放入场队列
  enqueueAll()
}, { deep: true, immediate: true })

onMounted(() => {
  enqueueAll()
})

onUnmounted(() => {
  clearTimer()
})
</script>

<style scoped>
.animated-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.animated-list-item {
  will-change: transform, opacity;
}

/* 进入动画（从右侧滑入并带有轻微弹性） */
.animated-list-enter-active {
  transition: transform 320ms cubic-bezier(0.22, 1, 0.36, 1), opacity 260ms ease-out;
}
.animated-list-leave-active {
  transition: transform 220ms ease-in, opacity 200ms ease-in;
}
.animated-list-enter-from {
  opacity: 0;
  transform: translateY(6px) scale(0.98);
}
.animated-list-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
}
.animated-list-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
}
.animated-list-leave-to {
  opacity: 0;
  transform: translateY(6px) scale(0.98);
}

/* 移动过渡（顺序变化） */
.animated-list-move {
  transition: transform 260ms ease;
}
</style>


