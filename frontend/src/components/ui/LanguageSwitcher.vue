<template>
  <div class="relative z-[60]" ref="host" v-click-outside="() => (open=false)">
    <ripple-button pill :class="buttonClassMerged" @click="toggleOpen" :title="t('app.language.title') || 'Language'">
      {{ currentShortLabel }}
    </ripple-button>
    <teleport to="body">
      <liquid-glass
        v-if="open"
        :style="styleObj"
        containerClass="fixed z-[1000] rounded-2xl border border-white/20 overflow-hidden"
        :radius="16"
        :frost="0.05"
        @click.stop
      >
        <div class="px-3 py-2 text-xs text-subtle">
          <div class="font-medium mb-1">{{ t('app.language.title') }}</div>
          <div class="opacity-90">{{ t('app.language.note') }}</div>
        </div>
        <div class="border-t border-white/10 my-1"></div>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="change('zh-CN')">
          <span>{{ t('app.language.zhCN') }}</span>
          <span v-if="isCurrent('zh-CN')" class="text-theme-primary">✓</span>
        </button>
        <button class="w-full text-left px-3 py-2 rounded-2xl hover:bg-white/10 text-sm flex items-center justify-between" @click="change('en-US')">
          <span>{{ t('app.language.enUS') }}</span>
          <span v-if="isCurrent('en-US')" class="text-theme-primary">✓</span>
        </button>
      </liquid-glass>
    </teleport>
  </div>
 </template>

<script setup lang="ts">
import { ref, computed, nextTick, watchEffect } from 'vue'
import { useLocale } from '@/i18n/useLocale'
import { loadLocaleMessages, REQUIRED_NAMESPACES } from '@/i18n'
import { useI18n } from 'vue-i18n'
import RippleButton from '@/components/ui/RippleButton.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

const open = ref(false)
function toggleOpen() {
  // 已打开时，二次点击应直接关闭（不要再广播全局关闭事件，否则会出现“关了又开”的感觉）
  if (open.value) {
    open.value = false
    return
  }
  try { window.dispatchEvent(new CustomEvent('ui:close-topbar-popovers')) } catch {}
  open.value = true
}
const host = ref<HTMLElement | null>(null)
const styleObj = ref<Record<string, string>>({})
const { locale, setLocale } = useLocale()
const { t } = useI18n()
const props = withDefaults(defineProps<{ buttonClass?: string }>(), { buttonClass: '' })
const baseBtn = [
  'px-4 h-11 flex items-center rounded-full text-sm bg-transparent',
  // hover 仅“轻微加深底色”，不要浮起/阴影（避免突出来）
  'hover:bg-black/5 active:bg-black/10',
  'dark:hover:bg-white/10 dark:active:bg-white/15',
  'focus-visible:shadow-[0_0_0_2px_rgba(59,130,246,0.35)]',
].join(' ')
const buttonClassMerged = computed(() => [baseBtn, props.buttonClass].filter(Boolean).join(' '))

const currentShortLabel = computed(() => {
  const v = String(locale.value || '').toLowerCase().replace('_','-')
  return (v === 'zh' || v === 'zh-cn') ? t('app.language.short.zh') : t('app.language.short.en')
})

function isCurrent(code: 'zh-CN' | 'en-US') {
  const v = String(locale.value || '').toLowerCase().replace('_','-')
  if (code === 'zh-CN') return v === 'zh-cn' || v === 'zh'
  // code === 'en-US'
  return v === 'en-us' || v === 'en'
}

// click-outside handled by directive

async function change(l: 'zh-CN' | 'en-US') {
  await loadLocaleMessages(l, [...REQUIRED_NAMESPACES])
  await setLocale(l)
  open.value = false
}

watchEffect(async () => {
  if (!open.value) return
  await nextTick()
  try {
    const el = host.value as HTMLElement
    const rect = el.getBoundingClientRect()
    styleObj.value = {
      position: 'fixed',
      top: `${rect.bottom + 10}px`,
      left: `${Math.max(8, rect.right - 160)}px`,
      width: '10rem',
      zIndex: '1000'
    }
  } catch {}
})

// 统一响应“关闭顶栏弹层”事件
try { window.addEventListener('ui:close-topbar-popovers', () => (open.value = false)) } catch {}
</script>


