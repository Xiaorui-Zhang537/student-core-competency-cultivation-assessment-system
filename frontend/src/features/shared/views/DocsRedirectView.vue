<template>
  <div class="p-6 text-sm text-gray-600 dark:text-gray-300">
    Redirecting to documentation...
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  const fallback = 'http://localhost:4174'
  const target = (import.meta as any).env?.VITE_DOCS_URL || fallback
  try {
    window.location.href = String(target)
  } catch {
    window.location.href = fallback
  }
})
</script>


