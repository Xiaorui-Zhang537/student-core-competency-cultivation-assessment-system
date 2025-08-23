import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const isOpen = ref(false)
  const peerId = ref<string | number | null>(null)
  const peerName = ref<string | null>(null)
  const courseId = ref<string | number | null>(null)

  function openChat(id: string | number, name: string | null = null, cId: string | number | null = null) {
    peerId.value = id
    peerName.value = name
    courseId.value = cId
    isOpen.value = true
  }

  function closeChat() {
    isOpen.value = false
    peerId.value = null
    peerName.value = null
    courseId.value = null
  }

  return { isOpen, peerId, peerName, courseId, openChat, closeChat }
})


