<template>
  <div class="py-6">
    <div class="max-w-7xl mx-auto p-4 glass-regular glass-tint-info rounded-2xl" v-glass>
      <notification-center />
    </div>
  </div>
  </template>

<script setup lang="ts">
import NotificationCenter from '@/components/notifications/NotificationCenter.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>


