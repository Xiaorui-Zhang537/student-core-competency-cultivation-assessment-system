<template>
  <div class="shrink-0 w-80">
    <liquid-glass :radius="16" :frost="0.05" :border="0.07" :alpha="0.93" :blur="11"
                 :container-class="['glass-regular','glass-interactive', tintClass, 'rounded-2xl'].join(' ')">
      <div class="p-4">
        <div class="flex items-center gap-3 mb-3">
          <user-avatar :avatar="img" :size="36" :rounded="true" />
          <div class="min-w-0">
            <div class="text-sm font-medium text-base-content truncate">{{ name }}</div>
            <div class="text-xs text-muted truncate">{{ username }}</div>
          </div>
          <span v-if="mbti" class="ml-auto px-2 py-0.5 text-xs rounded-full border" :class="mbtiBadgeClass">
            {{ mbti }}
          </span>
        </div>
        <div class="text-sm text-gray-700 dark:text-gray-300 leading-relaxed">
          {{ body }}
        </div>
      </div>
    </liquid-glass>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import UserAvatar from '@/components/ui/UserAvatar.vue'

interface Props {
  img: string
  name: string
  username: string
  body: string
  mbti?: string
  tint?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'info' | null
}

const props = withDefaults(defineProps<Props>(), {
  tint: 'info',
})

const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')

// MBTI 徽章颜色映射（Analysts=紫, Sentinels=蓝, Diplomats=绿, Explorers=黄）
const mbtiBadgeClass = computed(() => {
  if (!props.mbti) return 'bg-white/10 text-base-content/70 border-white/20'
  const code = props.mbti.toUpperCase().replace(/[^A-Z]/g, '')
  if (code.length < 4) return 'bg-white/10 text-base-content/70 border-white/20'
  const nOrS = code[1]
  const tOrF = code[2]
  const jOrP = code[3]
  // Analysts: N + T → purple
  if (nOrS === 'N' && tOrF === 'T') return 'bg-purple-500/15 text-purple-600 dark:text-purple-300 border-purple-400/30'
  // Diplomats: N + F → green
  if (nOrS === 'N' && tOrF === 'F') return 'bg-green-500/15 text-green-600 dark:text-green-300 border-green-400/30'
  // Sentinels: S + J → blue
  if (nOrS === 'S' && jOrP === 'J') return 'bg-blue-500/15 text-blue-600 dark:text-blue-300 border-blue-400/30'
  // Explorers: S + P → yellow
  if (nOrS === 'S' && jOrP === 'P') return 'bg-yellow-400/20 text-yellow-700 dark:text-yellow-300 border-yellow-400/40'
  return 'bg-white/10 text-base-content/70 border-white/20'
})
</script>

<style scoped>
</style>


