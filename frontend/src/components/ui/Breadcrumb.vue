<template>
  <nav class="relative z-10" :aria-label="ariaLabel">
    <ol class="flex items-center space-x-2 text-sm">
      <li v-for="(item, index) in items" :key="`${item.label}-${index}`" class="min-w-0">
        <span
          v-if="index > 0"
          class="mr-2 text-[color-mix(in_oklab,var(--color-base-content)_45%,transparent)]"
          aria-hidden="true"
        >
          &gt;
        </span>
        <router-link
          v-if="item.to && index < items.length - 1"
          :to="item.to"
          class="text-[var(--color-base-content)] hover:text-[var(--color-primary)]"
        >
          {{ item.label }}
        </router-link>
        <span
          v-else
          class="font-medium text-[var(--color-base-content)] truncate inline-block max-w-full align-bottom"
          :aria-current="index === items.length - 1 ? 'page' : undefined"
        >
          {{ item.label }}
        </span>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
type BreadcrumbItem = {
  label: string
  to?: string
}

withDefaults(defineProps<{
  items: BreadcrumbItem[]
  ariaLabel?: string
}>(), {
  ariaLabel: 'Breadcrumb'
})
</script>
