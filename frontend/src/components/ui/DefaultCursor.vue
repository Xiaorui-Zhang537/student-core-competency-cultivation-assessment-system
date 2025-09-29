<template>
  <div class="cursor-default text-theme-primary">
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="32"
      height="32"
      viewBox="0 0 32 32"
      aria-hidden="true"
    >
      <path
        fill="currentColor"
        d="M9.391 2.32C8.42 1.56 7 2.253 7 3.486V28.41c0 1.538 1.966 2.18 2.874.938l6.225-8.523a2 2 0 0 1 1.615-.82h9.69c1.512 0 2.17-1.912.978-2.844z"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.cursor-default {
  position: absolute;
  left: 0;
  top: 0;
  pointer-events: none;
  color: var(--color-theme-primary, var(--color-primary, var(--color-base-content, #111)));
}
</style>

