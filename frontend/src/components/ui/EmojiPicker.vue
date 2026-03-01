<template>
  <div class="relative inline-block" ref="btnRef">
    <Button type="button" :size="size" :variant="btnVariant" :class="['flex items-center', buttonClass]" @click="toggle">
      <face-smile-icon class="w-5 h-5 mr-1" />
      <span :class="[hideLabelOnSmall ? 'hidden sm:inline' : '', 'whitespace-nowrap']">
        {{ t('shared.emojiPicker.button') }}
      </span>
    </Button>
  </div>
  <teleport to="body">
    <div
      v-if="open"
      ref="panelRef"
      class="fixed z-[20050] p-2 w-60 max-h-56 overflow-auto rounded-2xl grid grid-cols-8 gap-1 no-scrollbar ui-popover-menu"
      :class="tintClass"
      v-glass="{ strength: 'thin', interactive: false }"
      :style="{ left: `${pos.left}px`, top: `${pos.top}px` }"
    >
      <button v-for="(e, idx) in emojis" :key="idx" type="button" class="text-xl rounded hover:bg-white/20 dark:hover:bg-white/10 transition-colors" @click="pick(e)">{{ e }}</button>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import { FaceSmileIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

const emit = defineEmits<{ (e: 'emoji', emoji: string): void; (e: 'select', emoji: string): void }>()

const props = withDefaults(defineProps<{ size?: 'xs'|'sm'|'md'; variant?: 'ghost'|'outline'|'secondary'|'primary'; tint?: 'primary'|'secondary'|'accent'|'info'|'success'|'warning'|'danger'|null; buttonClass?: string; hideLabelOnSmall?: boolean }>(), {
  size: 'sm',
  variant: 'ghost',
  tint: 'primary',
  buttonClass: '',
  hideLabelOnSmall: false
})

const open = ref(false)
const btnRef = ref<HTMLElement | null>(null)
const panelRef = ref<HTMLElement | null>(null)
const pos = ref({ left: 0, top: 0 })
const { t } = useI18n()
const hideLabelOnSmall = computed(() => !!props.hideLabelOnSmall)
const btnVariant = computed(() => props.variant || 'ghost')
const tintClass = computed(() => props.tint ? `glass-tint-${props.tint}` : '')
const emojis = [
  // Smileys & Emotion
  '😀','😁','😂','🤣','😃','😄','😅','😆','😉','😊','😋','😎','😍','😘','😗','😙','😚','🙂','🤗','🤩','🥳','😏','😒','🙄','😬','😌','😔','😪','🤤','😴','🤒','🤕','🤢','🤮','🤧','😷','🥵','🥶','🥴','😵','🤯','🤠','🥹','😭','😤','😠','😡','🤬','😇','🤔','🤨','🫤','🫠','😐','😑','😶','😮‍💨','😮','😲','😳','🥺','😱','😨','😰','😥','😓','🤥','🤫','🤭','🫢','🫣','🤐','😈','👿','💀','☠️','👻','👽','🤖',
  // Gestures & Body
  '👍','👎','👌','✌️','🤞','🤟','🤘','🤙','👋','🤚','✋','🖖','🙏','👏','🙌','🫶','💪','🫳','🫴','🫷','🫸','🖐️','✊','🤛','🤜','🫵','☝️','👆','👇','👈','👉',
  // Hearts & Symbols
  '❤️','🧡','💛','💚','💙','💜','🖤','🤍','🤎','💘','💝','💖','💗','💓','💞','💕','💟','💔','💯','✨','🌟','⭐','🔥','⚡','💫','🫶',
  // Animals & Nature
  '🐶','🐱','🐭','🐹','🐰','🦊','🐻','🐼','🐨','🐯','🦁','🐮','🐷','🐸','🐵','🐔','🐧','🐦','🦆','🦅','🦉','🦇','🐺','🐗','🐴','🦄','🐝','🪲','🦋','🐞','🐢','🐍','🦎','🦖','🐙','🦑','🦐','🦞','🦀','🐡','🐠','🐟','🐬','🐳','🐋','🦈','🐊','🦓','🦒','🦬','🐘','🦏','🦛','🦘','🐇','🐿️','🦔','🦦','🦥','🦨','🦫','🕊️','🌸','🌼','🌻','🌺','🌹','💐','🌷','🍀','🍃','🍂','🍁','🌈','☀️','🌤️','⛅','🌧️','⛈️','🌨️','🌙','⭐',
  // Food & Drink
  '🍎','🍏','🍐','🍊','🍋','🍌','🍉','🍇','🍓','🫐','🍈','🍒','🍑','🥭','🍍','🥥','🥝','🍅','🍆','🥑','🥦','🥬','🥒','🌶️','🫑','🥕','🧄','🧅','🥔','🍠','🍞','🥐','🥖','🥨','🧀','🥚','🍳','🥞','🧇','🥓','🥩','🍗','🍖','🌭','🍔','🍟','🍕','🫓','🥪','🥙','🌮','🌯','🫔','🥗','🍝','🍜','🍲','🥘','🍛','🍣','🍤','🍱','🍚','🍙','🍘','🥟','🥠','🥡','🍥','🍢','🍧','🍨','🍦','🍰','🎂','🧁','🍮','🍭','🍬','🍫','🍿','🧋','🧃','🥤','🧉','🍺','🍻','🍷','🍸','🍹','🥂','☕','🍵',
  // Activities & Objects
  '⚽','🏀','🏈','⚾','🎾','🏐','🏉','🎱','🏓','🏸','🥅','🏒','🏑','🥍','🥏','🎣','🥊','🥋','🎽','🛹','🛼','⛸️','🎿','⛷️','🏂','🏋️','🤼','🤸','⛹️','🤺','🎯','🎮','🕹️','🎲','♟️','🎳','🎻','🎸','🎺','🎷','🎹','🥁','🪘','🪗','📷','🎥','🎬','📱','💻','⌚','📚','📝','✏️','📌','📎','🧷','🔒','🔑',
  // Travel & Places
  '🚗','🚕','🚙','🚌','🚎','🏎️','🚓','🚑','🚒','🚐','🚚','🚛','🚜','🛵','🏍️','🚲','🛴','✈️','🛫','🛬','🚀','🛸','⛵','🚢','⚓','🗺️','🗽','🗼','🏰','🏯','🏝️','🏜️','🏞️','🌋','🗻','🏔️','⛺'
]

const PANEL_WIDTH = 240 // tailwind w-60 ≈ 15rem ≈ 240px
const PANEL_ESTIMATED_HEIGHT = 224 // 14rem 近似，便于上下方位判断
const clamp = (v: number, min: number, max: number) => Math.max(min, Math.min(v, max))
const computePos = () => {
  const el = btnRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const vw = window.innerWidth || document.documentElement.clientWidth || 0
  const vh = window.innerHeight || document.documentElement.clientHeight || 0
  const left = clamp(rect.left, 8, vw - PANEL_WIDTH - 8)
  const preferDownTop = rect.bottom + 6
  const preferUpTop = Math.max(8, rect.top - PANEL_ESTIMATED_HEIGHT - 6)
  const top = (preferDownTop + PANEL_ESTIMATED_HEIGHT <= vh - 8) ? preferDownTop : preferUpTop
  pos.value = { left, top }
}

const toggle = async () => {
  if (!open.value) {
    computePos()
    open.value = true
    await nextTick()
    // 打开后再精确一次（获取到实际高度时）
    computePos()
    attachScrollParents()
  } else {
    open.value = false
    detachScrollParents()
  }
}

const pick = (e: string) => {
  emit('emoji', e)
  emit('select', e)
  open.value = false
  detachScrollParents()
}

const onResize = () => { if (open.value) computePos() }

// 关闭：点击窗口外部
const onDocClick = (ev: MouseEvent) => {
  if (!open.value) return
  const target = ev.target as Node
  const anchor = btnRef.value
  const panel = panelRef.value
  if (anchor && anchor.contains(target)) return
  if (panel && panel.contains(target)) return
  open.value = false
  detachScrollParents()
}

// 关闭：Esc
const onKeydown = (ev: KeyboardEvent) => {
  if (ev.key === 'Escape' && open.value) {
    open.value = false
    detachScrollParents()
  }
}

// 跟随滚动：监听可滚动祖先
let scrollParents: HTMLElement[] = []
const isScrollable = (v: string) => v === 'auto' || v === 'scroll'
const collectScrollParents = (el: HTMLElement | null) => {
  const arr: HTMLElement[] = []
  let cur: HTMLElement | null = el
  while (cur && cur !== document.body) {
    const style = window.getComputedStyle(cur)
    if (isScrollable(style.overflowY) || isScrollable(style.overflowX)) arr.push(cur)
    cur = cur.parentElement
  }
  const docEl = (document.scrollingElement || document.documentElement) as HTMLElement
  if (docEl && !arr.includes(docEl)) arr.push(docEl)
  return arr
}
const attachScrollParents = () => {
  detachScrollParents()
  scrollParents = collectScrollParents(btnRef.value as HTMLElement)
  scrollParents.forEach(p => p.addEventListener('scroll', onResize, { passive: true }))
}
const detachScrollParents = () => {
  if (!scrollParents.length) return
  scrollParents.forEach(p => p.removeEventListener('scroll', onResize))
  scrollParents = []
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  window.addEventListener('scroll', onResize, true)
  document.addEventListener('click', onDocClick)
  document.addEventListener('keydown', onKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', onResize, true)
  document.removeEventListener('click', onDocClick)
  document.removeEventListener('keydown', onKeydown)
  detachScrollParents()
})
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
