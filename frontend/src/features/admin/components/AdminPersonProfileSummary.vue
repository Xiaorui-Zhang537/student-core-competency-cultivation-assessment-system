<template>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <card padding="md" tint="info" class="lg:col-span-2">
      <div class="flex items-start justify-between gap-4">
        <div class="min-w-0">
          <div class="text-xs text-subtle mb-2">{{ title }}</div>
          <div class="text-2xl font-semibold truncate">{{ displayName }}</div>
          <div class="text-sm text-subtle">{{ person?.email || '-' }}</div>
          <div v-if="primaryIdValue" class="text-xs text-subtle mt-1">{{ primaryIdLabel }}: {{ primaryIdValue }}</div>
          <div class="text-xs text-subtle mt-1">{{ t('common.columns.status') || '状态' }}: {{ person?.status || '-' }}</div>
        </div>
        <div class="flex flex-col gap-2 min-w-[12rem]">
          <div
            v-for="(s, idx) in stats"
            :key="`${s.label}-${idx}`"
            class="glass-badge px-3 py-2 justify-between"
          >
            <span class="text-subtle">{{ s.label }}</span>
            <span class="font-semibold">{{ s.value ?? '-' }}</span>
          </div>
        </div>
      </div>
    </card>

    <card padding="md" tint="secondary">
      <div class="text-sm font-medium mb-3">{{ t('shared.profile.section.profileInfo') || '个人信息' }}</div>
      <div class="grid grid-cols-1 gap-2 text-sm">
        <div><span class="text-subtle">{{ t('shared.profile.fields.username') || '用户名' }}:</span> {{ person?.username || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.nickname') || '昵称' }}:</span> {{ person?.nickname || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.firstName') || '名' }}:</span> {{ person?.firstName || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.lastName') || '姓' }}:</span> {{ person?.lastName || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.gender') || '性别' }}:</span> {{ person?.gender || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.mbti') || 'MBTI' }}:</span> {{ person?.mbti || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.phone') || '电话' }}:</span> {{ person?.phone || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.birthday') || '生日' }}:</span> {{ person?.birthday || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.school') || '学校' }}:</span> {{ person?.school || '-' }}</div>
        <div><span class="text-subtle">{{ t('shared.profile.fields.subject') || '专业/科目' }}:</span> {{ person?.subject || '-' }}</div>
        <div v-if="person?.grade"><span class="text-subtle">{{ t('shared.profile.fields.grade') || '年级' }}:</span> {{ person?.grade }}</div>
        <div>
          <span class="text-subtle">{{ t('shared.profile.fields.country') || '国家' }}:</span>
          {{ [person?.country, person?.province, person?.city].filter(Boolean).join(' / ') || '-' }}
        </div>
        <div class="line-clamp-3"><span class="text-subtle">{{ t('shared.profile.fields.bio') || '简介' }}:</span> {{ person?.bio || '-' }}</div>
      </div>
    </card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'

type StatItem = { label: string; value: string | number | null | undefined }

const props = withDefaults(defineProps<{
  title?: string
  person?: any
  primaryIdLabel?: string
  primaryIdValue?: string
  stats?: StatItem[]
}>(), {
  title: '',
  person: null,
  primaryIdLabel: 'ID',
  primaryIdValue: '',
  stats: () => [],
})

const { t } = useI18n()

const displayName = computed(() => {
  const p = props.person || {}
  return p.nickname || p.username || '#'
})
</script>

