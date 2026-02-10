<template>
  <div v-if="container" :class="wrapperClass">
    <div class="flex items-center justify-between">
      <div class="min-w-0">
        <h1 class="text-2xl md:text-3xl font-bold truncate" style="color: var(--color-base-content)">{{ title }}</h1>
        <p v-if="subtitle" class="mt-1 text-muted whitespace-pre-line break-words prose-readable">{{ subtitle }}</p>
      </div>
      <div class="shrink-0">
        <slot name="actions" />
      </div>
    </div>
  </div>
  <template v-else>
    <div class="flex items-center justify-between">
      <div class="min-w-0">
        <h1 class="text-2xl md:text-3xl font-bold truncate" style="color: var(--color-base-content)">{{ title }}</h1>
        <p v-if="subtitle" class="mt-1 text-muted whitespace-pre-line break-words prose-readable">{{ subtitle }}</p>
      </div>
      <div class="shrink-0">
        <slot name="actions" />
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
/**
 * PageHeader
 * - 统一页面标题与子标题的排版
 * - 默认自带 `max-w-7xl mx-auto mb-6` 容器；在需要更高层级的布局（如 PageScaffold）时可关闭容器
 */
const props = withDefaults(defineProps<{
  title: string
  subtitle?: string
  /**
   * 是否渲染外层容器（用于控制 max-width 与间距）。
   * 默认 true 保持现有页面不受影响。
   */
  container?: boolean
  /**
   * 外层容器 class（仅 container=true 时生效）。
   */
  containerClass?: string
}>(), {
  container: true,
  containerClass: 'max-w-7xl mx-auto mb-6 relative z-10',
})

const wrapperClass = props.containerClass
</script>

<style scoped>
</style>


