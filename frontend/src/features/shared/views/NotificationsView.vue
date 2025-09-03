<template>
  <div class="py-6">
    <div class="max-w-7xl mx-auto">
      <PageHeader :title="t('shared.notifications.title') || t('layout.notifications') || '通知中心'" :subtitle="t('shared.notifications.subtitle') || ''" />
      <NotificationCenter />
    </div>
  </div>
  </template>

<script setup lang="ts">
import NotificationCenter from '@/components/notifications/NotificationCenter.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>


