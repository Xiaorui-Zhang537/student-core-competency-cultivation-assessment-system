<template>
  <div class="relative inline-block">
    <button type="button" class="btn btn-ghost btn-sm flex items-center" @click="toggle">
      <FaceSmileIcon class="w-5 h-5 mr-1" />
      {{ t('shared.emojiPicker.button') }}
    </button>
    <div v-if="open" class="absolute z-50 mt-2 p-2 w-60 max-h-56 overflow-auto bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded shadow grid grid-cols-8 gap-1">
      <button v-for="(e, idx) in emojis" :key="idx" type="button" class="text-xl hover:bg-gray-100 dark:hover:bg-gray-700 rounded" @click="pick(e)">{{ e }}</button>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { FaceSmileIcon } from '@heroicons/vue/24/outline'
// @ts-ignore shim for vue-i18n types in this project
import { useI18n } from 'vue-i18n'

const emit = defineEmits<{ (e: 'select', emoji: string): void }>()

const open = ref(false)
const { t } = useI18n()
const emojis = [
  '😀','😁','😂','🤣','😊','😇','🙂','😉','😍','😘','😗','😄','😅','😆','😌','🤗',
  '🤔','🤨','😐','😑','😶','🙄','😏','😴','😪','😷','🤒','🤕','🤧','🥳','🤩','🥰',
  '👍','👎','🙏','👏','🙌','💪','🤝','👌','✌️','🤘','👋','🤙','🫶','❤️','🧡','💛',
  '💚','💙','💜','🖤','🤍','🤎','💯','🔥','✨','🌟','🎉','🎊','🍀','🌈','🍻','☕'
]

const toggle = () => {
  open.value = !open.value
}

const pick = (e: string) => {
  emit('select', e)
  open.value = false
}
</script>

<style scoped>
</style>

