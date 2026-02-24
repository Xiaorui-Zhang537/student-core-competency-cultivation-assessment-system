<template>
  <div :class="['relative z-10', wrapperClass]">
    <div :class="containerClass">
      <breadcrumb
        v-if="hasBreadcrumb"
        class="mb-2"
        :items="breadcrumbItems || []"
        :aria-label="breadcrumbAriaLabel"
      />
      <page-header :title="title" :subtitle="subtitle" :container="false">
        <template #actions>
          <slot name="actions" />
        </template>
      </page-header>
    </div>
    <div v-if="$slots.default" :class="containerClass">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Breadcrumb from '@/components/ui/Breadcrumb.vue'
import PageHeader from '@/components/ui/PageHeader.vue'

type BreadcrumbItem = {
  label: string
  to?: string
}

/**
 * PageScaffold
 * - 将 Breadcrumb + PageHeader + 内容容器统一成一个骨架，保证全站标题区布局一致
 * - 不引入任何业务逻辑，只负责布局与插槽
 */
const props = withDefaults(defineProps<{
  title: string
  subtitle?: string
  breadcrumbItems?: BreadcrumbItem[]
  breadcrumbAriaLabel?: string
  /**
   * 统一 max-width 容器 class；默认与现有 PageHeader 视觉对齐。
   */
  containerClass?: string
  /**
   * 外层 wrapper 的额外 class（用于页面级 margin/padding 控制）。
   */
  wrapperClass?: string
}>(), {
  breadcrumbAriaLabel: 'Breadcrumb',
  containerClass: 'max-w-7xl mx-auto',
  wrapperClass: '',
})

const hasBreadcrumb = computed(() => Array.isArray(props.breadcrumbItems) && props.breadcrumbItems.length > 0)
</script>

