<template>
  <div
    :class="[
      'relative h-16 w-full text-center font-sans leading-none [filter:url(#threshold)_blur(0.6px)]',
      props.class
    ]"
  >
    <span ref="text1Ref" class="absolute inset-x-0 top-0 m-auto inline-block w-full" />
    <span ref="text2Ref" class="absolute inset-x-0 top-0 m-auto inline-block w-full" />

    <svg id="filters" class="fixed size-0" preserveAspectRatio="xMidYMid slice">
      <defs>
        <filter id="threshold">
          <feColorMatrix in="SourceGraphic" type="matrix" values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 255 -140" />
        </filter>
      </defs>
    </svg>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'

interface Props {
  class?: string
  text: string
  morphTime?: number
  coolDownTime?: number
}
const props = withDefaults(defineProps<Props>(), {
  morphTime: 1.2,
  coolDownTime: 0.4
})

const text1Ref = ref<HTMLSpanElement>()
const text2Ref = ref<HTMLSpanElement>()

const previousText = ref(props.text)
const currentText = ref(props.text)
const morph = ref(0)
const coolDown = ref(0)
const time = ref(new Date())
let animationFrameId = 0

function setStyles(fraction: number) {
  if (!text1Ref.value || !text2Ref.value) return
  text2Ref.value.textContent = currentText.value
  text1Ref.value.textContent = previousText.value
  text2Ref.value.style.filter = `blur(${Math.min(8 / fraction - 8, 100)}px)`
  text2Ref.value.style.opacity = `${Math.pow(fraction, 0.4) * 100}%`
  const inv = 1 - fraction
  text1Ref.value.style.filter = `blur(${Math.min(8 / inv - 8, 100)}px)`
  text1Ref.value.style.opacity = `${Math.pow(inv, 0.4) * 100}%`
}

function doMorph() {
  morph.value -= coolDown.value
  coolDown.value = 0
  let fraction = morph.value / props.morphTime
  if (fraction > 1) { coolDown.value = props.coolDownTime; fraction = 1 }
  setStyles(fraction)
  if (fraction === 1) previousText.value = currentText.value
}

function doCoolDown() {
  morph.value = 0
  if (text1Ref.value && text2Ref.value) {
    text2Ref.value.style.filter = 'none'
    text2Ref.value.style.opacity = '100%'
    text1Ref.value.style.filter = 'none'
    text1Ref.value.style.opacity = '0%'
  }
}

function animate() {
  animationFrameId = requestAnimationFrame(animate)
  const newTime = new Date()
  const dt = (newTime.getTime() - time.value.getTime()) / 1000
  time.value = newTime
  coolDown.value -= dt
  if (coolDown.value <= 0) {
    doMorph()
  } else {
    doCoolDown()
  }
}

watch(() => props.text, (newText) => {
  if (newText !== currentText.value) {
    previousText.value = currentText.value
    currentText.value = newText
    morph.value = 0
    coolDown.value = 0
    time.value = new Date()
  }
})

onMounted(() => { animate() })
onUnmounted(() => { cancelAnimationFrame(animationFrameId) })
</script>

