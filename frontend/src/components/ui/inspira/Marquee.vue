<template>
  <div class="overflow-hidden">
    <div class="whitespace-nowrap animate-marquee will-change-transform" :style="{ animationDuration: duration+'s' }">
      <span v-for="(it, i) in loopItems" :key="i" class="inline-flex items-center gap-2 mr-10">
        <span class="font-medium">{{ renderTitle(it) }}</span>
        <span class="text-subtle">— {{ renderDesc(it) }}</span>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
const props = withDefaults(defineProps<{ items: Array<any>; duration?: number }>(), { duration: 18 })
const { t } = useI18n()
const loopItems = computed(() => props.items.concat(props.items))
function renderTitle(it: any) { return it.titleKey ? t(it.titleKey) : (it.title || '') }
function renderDesc(it: any) { return it.descKey ? t(it.descKey) : (it.desc || '') }
</script>

<style scoped>
@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
.animate-marquee { animation-name: marquee; animation-timing-function: linear; animation-iteration-count: infinite; }
</style>


