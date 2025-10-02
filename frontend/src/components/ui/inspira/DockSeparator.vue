<template>
  <div :class="['relative block bg-primary/50', orientation === 'vertical' ? 'w-4/5 h-0.5' : 'h-4/5 w-0.5']"></div>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import { ORIENTATION_INJECTION_KEY } from './injectionKeys'
const orientation = inject(ORIENTATION_INJECTION_KEY, 'horizontal')
</script>

<style scoped>
</style>


