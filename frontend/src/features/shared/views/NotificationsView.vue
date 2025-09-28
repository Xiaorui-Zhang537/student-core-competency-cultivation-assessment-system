<template>
  <div class="py-6">
    <div class="max-w-7xl mx-auto px-4">
      <notification-center />
    </div>
  </div>
  </template>

<script setup lang="ts">
import NotificationCenter from '@/components/notifications/NotificationCenter.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>


