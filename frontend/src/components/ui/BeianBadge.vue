<template>
  <div class="fixed left-4 bottom-4 z-30">
    <liquid-glass :radius="28" :frost="0.04" class="px-4 py-2" containerClass="rounded-full shadow-lg">
      <a
        href="https://beian.mps.gov.cn/#/query/webSearch?code=21021702000814"
        target="_blank"
        rel="noreferrer"
        aria-label="公安备案与ICP备案"
        class="inline-flex items-center gap-3 text-xs sm:text-sm text-subtle hover:opacity-90"
      >
        <span class="inline-flex items-center gap-1">
          <img src="/brand/police-badge.png" alt="公安备案" class="w-4 h-4" />
          <span class="whitespace-nowrap">辽公网安备21021702000814号</span>
        </span>
        <span class="whitespace-nowrap">辽ICP备2025066070号-1</span>
      </a>
    </liquid-glass>
  </div>
  
</template>

<script setup lang="ts">
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
</script>

<style scoped>
</style>



