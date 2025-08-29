<template>
  <div class="relative inline-block" ref="btnRef">
    <Button :size="size" :variant="variant" class="flex items-center" @click="toggle">
      <FaceSmileIcon class="w-5 h-5 mr-1" />
      {{ t('shared.emojiPicker.button') }}
    </Button>
  </div>
  <teleport to="body">
    <div
      v-if="open"
      class="fixed z-[9999] p-2 w-60 max-h-56 overflow-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow grid grid-cols-8 gap-1 no-scrollbar"
      :style="{ left: `${pos.left}px`, top: `${pos.top}px` }"
    >
      <button v-for="(e, idx) in emojis" :key="idx" type="button" class="text-xl hover:bg-gray-100 dark:hover:bg-gray-700 rounded" @click="pick(e)">{{ e }}</button>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { FaceSmileIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'

const emit = defineEmits<{ (e: 'emoji', emoji: string): void; (e: 'select', emoji: string): void }>()

const props = withDefaults(defineProps<{ size?: 'xs'|'sm'|'md'; variant?: 'ghost'|'outline'|'secondary' }>(), {
  size: 'sm',
  variant: 'ghost'
})

const open = ref(false)
const btnRef = ref<HTMLElement | null>(null)
const pos = ref({ left: 0, top: 0 })
const { t } = useI18n()
const emojis = [
  '😀','😁','😂','🤣','😊','😇','🙂','😉','😍','😘','😗','😄','😅','😆','😌','🤗',
  '🤔','🤨','😐','😑','😶','🙄','😏','😴','😪','😷','🤒','🤕','🤧','🥳','🤩','🥰',
  '👍','👎','🙏','👏','🙌','💪','🤝','👌','✌️','🤘','👋','🤙','🫶','❤️','🧡','💛',
  '💚','💙','💜','🖤','🤍','🤎','💯','🔥','✨','🌟','🎉','🎊','🍀','🌈','🍻','☕'
]

const computePos = () => {
  const el = btnRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  pos.value = { left: rect.left, top: rect.bottom + 6 }
}

const toggle = () => {
  if (!open.value) computePos()
  open.value = !open.value
}

const pick = (e: string) => {
  emit('emoji', e)
  emit('select', e)
  open.value = false
}

const onResize = () => { if (open.value) computePos() }

onMounted(() => {
  window.addEventListener('resize', onResize)
  window.addEventListener('scroll', onResize, true)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('scroll', onResize, true)
})
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>

