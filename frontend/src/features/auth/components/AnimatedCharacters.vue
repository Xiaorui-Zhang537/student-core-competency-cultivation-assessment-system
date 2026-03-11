<template>
  <div class="relative pointer-events-auto select-none" style="width: 550px; height: 400px; isolation: isolate">
    <!-- Purple tall rectangle character (back layer) -->
    <div
      ref="purpleRef"
      class="character character-purple absolute bottom-0 cursor-pointer character-hit"
      :class="[{ 'monster-bounce': bounceFlags.purple }, expressionClass('purple')]"
      :style="purpleStyle"
      @click.stop="onCharacterClick('purple')"
    >
      <div class="absolute flex gap-8" :style="purpleEyesWrapperStyle">
        <div ref="purpleLeftEyeRef" class="rounded-full flex items-center justify-center" :style="purpleLeftEyeStyle">
          <div v-if="!shouldHidePupil('purple', 'left')" class="rounded-full eye-pupil" :style="purpleLeftPupilStyle"></div>
        </div>
        <div ref="purpleRightEyeRef" class="rounded-full flex items-center justify-center" :style="purpleRightEyeStyle">
          <div v-if="!shouldHidePupil('purple', 'right')" class="rounded-full eye-pupil" :style="purpleRightPupilStyle"></div>
        </div>
      </div>
    </div>

    <!-- Black tall rectangle character (middle layer) -->
    <div
      ref="blackRef"
      class="character character-black absolute bottom-0 cursor-pointer character-hit"
      :class="[{ 'monster-bounce': bounceFlags.black }, expressionClass('black')]"
      :style="blackStyle"
      @click.stop="onCharacterClick('black')"
    >
      <div class="absolute flex gap-6" :style="blackEyesWrapperStyle">
        <div ref="blackLeftEyeRef" class="rounded-full flex items-center justify-center" :style="blackLeftEyeStyle">
          <div v-if="!shouldHidePupil('black', 'left')" class="rounded-full eye-pupil" :style="blackLeftPupilStyle"></div>
        </div>
        <div ref="blackRightEyeRef" class="rounded-full flex items-center justify-center" :style="blackRightEyeStyle">
          <div v-if="!shouldHidePupil('black', 'right')" class="rounded-full eye-pupil" :style="blackRightPupilStyle"></div>
        </div>
      </div>
    </div>

    <!-- Orange semi-circle character (front-left) -->
    <div
      ref="orangeRef"
      class="character character-orange absolute bottom-0 cursor-pointer character-hit"
      :class="[{ 'monster-bounce': bounceFlags.orange }, expressionClass('orange')]"
      :style="orangeStyle"
      @click.stop="onCharacterClick('orange')"
    >
      <div class="absolute flex gap-8" :style="orangePupilsWrapperStyle">
        <div ref="orangeLeftEyeRef" class="rounded-full eye-pupil" :style="orangeLeftPupilStyle"></div>
        <div ref="orangeRightEyeRef" class="rounded-full eye-pupil" :style="orangeRightPupilStyle"></div>
      </div>
      <div class="absolute rounded-full orange-mouth" :style="orangeMouthStyle"></div>
    </div>

    <!-- Yellow rounded rectangle character (front-right) -->
    <div
      ref="yellowRef"
      class="character character-yellow absolute bottom-0 cursor-pointer character-hit"
      :class="[{ 'monster-bounce': bounceFlags.yellow }, expressionClass('yellow')]"
      :style="yellowStyle"
      @click.stop="onCharacterClick('yellow')"
    >
      <div class="absolute flex gap-6" :style="yellowPupilsWrapperStyle">
        <div ref="yellowLeftEyeRef" class="rounded-full eye-pupil" :style="yellowLeftPupilStyle"></div>
        <div ref="yellowRightEyeRef" class="rounded-full eye-pupil" :style="yellowRightPupilStyle"></div>
      </div>
      <div class="absolute rounded-full yellow-mouth" :style="yellowMouthStyle"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

type CharacterKey = 'purple' | 'black' | 'orange' | 'yellow'
type ExpressionState = 'idle' | 'blink' | 'frown' | 'shy'
type EyeSide = 'left' | 'right'

const props = withDefaults(defineProps<{
  isTyping?: boolean
  isPasswordFocused?: boolean
  showPassword?: boolean
  passwordLength?: number
}>(), {
  isTyping: false,
  isPasswordFocused: false,
  showPassword: false,
  passwordLength: 0,
})

const mouseX = ref(0)
const mouseY = ref(0)
const bodyMouseX = ref(0)
const bodyMouseY = ref(0)
const targetMouseX = ref(0)
const targetMouseY = ref(0)

const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const expressionStates = ref<Record<CharacterKey, ExpressionState>>({
  purple: 'idle',
  black: 'idle',
  orange: 'idle',
  yellow: 'idle',
})
const blinkSides = ref<Record<CharacterKey, EyeSide>>({
  purple: 'left',
  black: 'right',
  orange: 'left',
  yellow: 'right',
})
const bounceFlags = ref<Record<CharacterKey, boolean>>({
  purple: false,
  black: false,
  orange: false,
  yellow: false,
})

const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)

const purpleLeftEyeRef = ref<HTMLElement | null>(null)
const purpleRightEyeRef = ref<HTMLElement | null>(null)
const blackLeftEyeRef = ref<HTMLElement | null>(null)
const blackRightEyeRef = ref<HTMLElement | null>(null)
const orangeLeftEyeRef = ref<HTMLElement | null>(null)
const orangeRightEyeRef = ref<HTMLElement | null>(null)
const yellowLeftEyeRef = ref<HTMLElement | null>(null)
const yellowRightEyeRef = ref<HTMLElement | null>(null)

let purpleBlinkTimer: ReturnType<typeof window.setTimeout> | null = null
let purpleBlinkEndTimer: ReturnType<typeof window.setTimeout> | null = null
let blackBlinkTimer: ReturnType<typeof window.setTimeout> | null = null
let blackBlinkEndTimer: ReturnType<typeof window.setTimeout> | null = null
let mouseRafId: number | null = null
const clickTransitionCursor: Record<CharacterKey, number> = {
  purple: 0,
  black: 0,
  orange: 0,
  yellow: 0,
}
const bounceTimers: Record<CharacterKey, ReturnType<typeof window.setTimeout> | null> = {
  purple: null,
  black: null,
  orange: null,
  yellow: null,
}
const expressionTimers: Record<CharacterKey, ReturnType<typeof window.setTimeout> | null> = {
  purple: null,
  black: null,
  orange: null,
  yellow: null,
}

const hasPassword = computed(() => Number(props.passwordLength || 0) > 0)
const isUsernameFocused = computed(() => !!props.isTyping)
const isPasswordFieldFocused = computed(() => !!props.isPasswordFocused)
const isShowingPassword = computed(() => hasPassword.value && !!props.showPassword)
const shouldPurplePeekWhenPasswordFocus = computed(() => isPasswordFieldFocused.value && !isShowingPassword.value)
const usernamePose = ref(0)
const passwordPose = ref(0)
const showPose = ref(0)

function clearTimer(t: ReturnType<typeof window.setTimeout> | null) {
  if (t) window.clearTimeout(t)
}

function randomBlinkDelay() {
  return Math.random() * 4000 + 3000
}

function schedulePurpleBlink() {
  clearTimer(purpleBlinkTimer)
  purpleBlinkTimer = window.setTimeout(() => {
    isPurpleBlinking.value = true
    clearTimer(purpleBlinkEndTimer)
    purpleBlinkEndTimer = window.setTimeout(() => {
      isPurpleBlinking.value = false
      schedulePurpleBlink()
    }, 150)
  }, randomBlinkDelay())
}

function scheduleBlackBlink() {
  clearTimer(blackBlinkTimer)
  blackBlinkTimer = window.setTimeout(() => {
    isBlackBlinking.value = true
    clearTimer(blackBlinkEndTimer)
    blackBlinkEndTimer = window.setTimeout(() => {
      isBlackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, randomBlinkDelay())
}

function handleMouseMove(event: MouseEvent) {
  targetMouseX.value = event.clientX
  targetMouseY.value = event.clientY
}

function lerp(current: number, target: number, easing: number) {
  const next = current + (target - current) * easing
  if (Math.abs(next - target) < 0.001) return target
  return next
}

function startMouseSmoothing() {
  const mouseEasing = 0.12
  const bodyEasing = 0.075
  const usernameEasing = 0.14
  const passwordEnterEasing = 0.09
  const passwordExitEasing = 0.14
  const showEasing = 0.18
  const tick = () => {
    const deltaX = targetMouseX.value - mouseX.value
    const deltaY = targetMouseY.value - mouseY.value
    mouseX.value += deltaX * mouseEasing
    mouseY.value += deltaY * mouseEasing
    if (Math.abs(deltaX) < 0.02) mouseX.value = targetMouseX.value
    if (Math.abs(deltaY) < 0.02) mouseY.value = targetMouseY.value
    bodyMouseX.value = lerp(bodyMouseX.value, targetMouseX.value, bodyEasing)
    bodyMouseY.value = lerp(bodyMouseY.value, targetMouseY.value, bodyEasing)

    const usernameTarget = isUsernameFocused.value ? 1 : 0
    const passwordTarget = shouldPurplePeekWhenPasswordFocus.value ? 1 : 0
    const passwordEasing = passwordTarget > passwordPose.value ? passwordEnterEasing : passwordExitEasing
    usernamePose.value = lerp(usernamePose.value, usernameTarget, usernameEasing)
    passwordPose.value = lerp(passwordPose.value, passwordTarget, passwordEasing)
    showPose.value = lerp(showPose.value, isShowingPassword.value ? 1 : 0, showEasing)

    mouseRafId = window.requestAnimationFrame(tick)
  }
  if (mouseRafId) window.cancelAnimationFrame(mouseRafId)
  mouseRafId = window.requestAnimationFrame(tick)
}

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(max, value))
}

function calculateCharacterPosition(el: HTMLElement | null) {
  if (!el) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const rect = el.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  const deltaX = bodyMouseX.value - centerX
  const deltaY = bodyMouseY.value - centerY
  return {
    faceX: clamp(deltaX / 20, -15, 15),
    faceY: clamp(deltaY / 30, -10, 10),
    bodySkew: clamp(-deltaX / 120, -6, 6),
  }
}

function calculateEyeLook(
  el: HTMLElement | null,
  maxDistance: number,
  forceX?: number,
  forceY?: number
) {
  if (typeof forceX === 'number' && typeof forceY === 'number') {
    return { x: forceX, y: forceY }
  }
  if (!el) return { x: 0, y: 0 }
  const rect = el.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY
  const distance = Math.min(Math.hypot(deltaX, deltaY), maxDistance)
  const angle = Math.atan2(deltaY, deltaX)
  return {
    x: Math.cos(angle) * distance,
    y: Math.sin(angle) * distance,
  }
}

const purplePosition = computed(() => calculateCharacterPosition(purpleRef.value))
const blackPosition = computed(() => calculateCharacterPosition(blackRef.value))
const yellowPosition = computed(() => calculateCharacterPosition(yellowRef.value))
const orangePosition = computed(() => calculateCharacterPosition(orangeRef.value))

const purpleForceLook = computed(() => {
  if (showPose.value < 0.02) return null
  return { x: -4 * showPose.value, y: -2 * showPose.value }
})

const blackForceLook = computed(() => {
  if (showPose.value < 0.02) return null
  return { x: -4 * showPose.value, y: -2 * showPose.value }
})

const orangeYellowForceLook = computed(() => {
  if (showPose.value < 0.02) return null
  return { x: -5 * showPose.value, y: -2 * showPose.value }
})

function px(value: number) {
  return `${value}px`
}

const expressionTransitions: Record<ExpressionState, ExpressionState[]> = {
  idle: ['blink', 'frown', 'shy'],
  blink: ['frown', 'shy', 'idle'],
  frown: ['shy', 'blink', 'idle'],
  shy: ['blink', 'frown', 'idle'],
}

const expressionDurations: Record<ExpressionState, number> = {
  idle: 0,
  blink: 360,
  frown: 900,
  shy: 1000,
}

function expressionClass(key: CharacterKey) {
  return `exp-${expressionStates.value[key]}`
}

function clearExpressionTimer(key: CharacterKey) {
  clearTimer(expressionTimers[key])
  expressionTimers[key] = null
}

function setRandomBlinkSide(key: CharacterKey) {
  blinkSides.value[key] = Math.random() > 0.5 ? 'left' : 'right'
}

function pickClickExpression(key: CharacterKey): ExpressionState {
  const current = expressionStates.value[key]
  const candidates = expressionTransitions[current].filter((state) => state !== 'idle')
  const cursor = clickTransitionCursor[key]
  const next = candidates[cursor % candidates.length] || 'blink'
  clickTransitionCursor[key] = cursor + 1
  return next
}

function activateExpression(
  key: CharacterKey,
  state: ExpressionState,
  options: { duration?: number; delay?: number } = {},
) {
  clearExpressionTimer(key)
  const current = expressionStates.value[key]

  const applyState = () => {
    if (state === 'blink') setRandomBlinkSide(key)
    expressionStates.value[key] = state
    if (state === 'idle') {
      expressionTimers[key] = null
      return
    }
    const duration = options.duration ?? expressionDurations[state]
    expressionTimers[key] = window.setTimeout(() => {
      expressionStates.value[key] = 'idle'
      expressionTimers[key] = null
    }, duration)
  }

  const delay = options.delay ?? (current !== 'idle' && current !== state ? 110 : 90)
  expressionTimers[key] = window.setTimeout(() => {
    applyState()
  }, delay)
}

function onCharacterClick(key: CharacterKey) {
  clearTimer(bounceTimers[key])
  bounceFlags.value[key] = false
  window.requestAnimationFrame(() => {
    bounceFlags.value[key] = true
  })
  bounceTimers[key] = window.setTimeout(() => {
    bounceFlags.value[key] = false
    bounceTimers[key] = null
  }, 680)

  activateExpression(key, pickClickExpression(key))
}

const purpleStyle = computed(() => {
  const skew = purplePosition.value.bodySkew
  const username = usernamePose.value
  const password = passwordPose.value
  const showing = showPose.value
  const hiddenSkew = skew - (2 * username + 5.2 * password)
  const hiddenTranslate = 14 * password
  const mixedSkew = hiddenSkew * (1 - showing)
  const mixedTranslate = hiddenTranslate * (1 - showing)
  const hiddenHeight = 400 + 16 * username + 22 * password
  const mixedHeight = hiddenHeight * (1 - showing) + 400 * showing
  return {
    left: '70px',
    width: '180px',
    height: px(mixedHeight),
    backgroundColor: '#6C3FF5',
    borderRadius: '10px 10px 0 0',
    transform: `skewX(${mixedSkew}deg) translateX(${mixedTranslate}px)`,
    transformOrigin: 'bottom center',
  }
})

const blackStyle = computed(() => {
  const skew = blackPosition.value.bodySkew
  const password = passwordPose.value
  const showing = showPose.value
  const hiddenSkew = skew * (1 + 0.08 * password) + 1.2 * password
  const hiddenTranslate = 2 * password
  const mixedSkew = hiddenSkew * (1 - showing)
  const mixedTranslate = hiddenTranslate * (1 - showing)
  return {
    left: '240px',
    width: '120px',
    height: '310px',
    backgroundColor: '#2D2D2D',
    borderRadius: '8px 8px 0 0',
    transform: `skewX(${mixedSkew}deg) translateX(${mixedTranslate}px)`,
    transformOrigin: 'bottom center',
  }
})

const orangeStyle = computed(() => ({
  left: '0px',
  width: '240px',
  height: '200px',
  backgroundColor: '#FF9B6B',
  borderRadius: '120px 120px 0 0',
  transform: `skewX(${orangePosition.value.bodySkew * (1 - showPose.value)}deg)`,
  transformOrigin: 'bottom center',
}))

const yellowStyle = computed(() => ({
  left: '310px',
  width: '140px',
  height: '230px',
  backgroundColor: '#E8D754',
  borderRadius: '70px 70px 0 0',
  transform: `skewX(${yellowPosition.value.bodySkew * (1 - showPose.value)}deg)`,
  transformOrigin: 'bottom center',
}))

const purpleEyesWrapperStyle = computed(() => {
  const username = usernamePose.value
  const password = passwordPose.value
  const showing = showPose.value
  const normalLeft = 45 + purplePosition.value.faceX + (5 * username) + (10 * password)
  const normalTop = 40 + purplePosition.value.faceY + (4 * username) + (18 * password)
  return {
    left: px(normalLeft * (1 - showing) + 20 * showing),
    top: px(normalTop * (1 - showing) + 35 * showing),
  }
})

const blackEyesWrapperStyle = computed(() => {
  const password = passwordPose.value
  const showing = showPose.value
  const normalLeft = 26 + blackPosition.value.faceX + (4 * password)
  const normalTop = 32 + blackPosition.value.faceY - (2 * password)
  return {
    left: px(normalLeft * (1 - showing) + 10 * showing),
    top: px(normalTop * (1 - showing) + 28 * showing),
  }
})

const orangePupilsWrapperStyle = computed(() => {
  const showing = showPose.value
  const normalLeft = 82 + orangePosition.value.faceX
  const normalTop = 90 + orangePosition.value.faceY
  return {
    left: px(normalLeft * (1 - showing) + 50 * showing),
    top: px(normalTop * (1 - showing) + 85 * showing),
  }
})

const yellowPupilsWrapperStyle = computed(() => {
  const showing = showPose.value
  const normalLeft = 52 + yellowPosition.value.faceX
  const normalTop = 40 + yellowPosition.value.faceY
  return {
    left: px(normalLeft * (1 - showing) + 20 * showing),
    top: px(normalTop * (1 - showing) + 35 * showing),
  }
})

const yellowMouthStyle = computed(() => {
  const state = expressionStates.value.yellow
  const isFrown = state === 'frown'
  const isShy = state === 'shy'
  return {
    width: isFrown ? '68px' : isShy ? '56px' : '80px',
    height: isFrown ? '12px' : isShy ? '8px' : '4px',
    left: px((40 + yellowPosition.value.faceX) * (1 - showPose.value) + (10 * showPose.value)),
    top: px((88 + yellowPosition.value.faceY) * (1 - showPose.value) + (88 * showPose.value)),
    borderBottom: isFrown ? '3px solid #2D2D2D' : isShy ? '2px solid #2D2D2D' : '0',
    borderRadius: isFrown ? '0 0 60px 60px' : isShy ? '0 0 36px 36px' : '999px',
    backgroundColor: isFrown || isShy ? 'transparent' : '#2D2D2D',
    transform: `translateY(${isShy ? 2 : isFrown ? -1 : 0}px)`,
    transition: 'all 260ms cubic-bezier(0.22, 1, 0.36, 1)',
  }
})

const orangeMouthStyle = computed(() => {
  const state = expressionStates.value.orange
  const isFrown = state === 'frown'
  const isShy = state === 'shy'
  return {
    width: isFrown ? '76px' : isShy ? '58px' : '66px',
    height: isFrown ? '12px' : isShy ? '8px' : '4px',
    left: px((102 + orangePosition.value.faceX) * (1 - showPose.value) + (76 * showPose.value)),
    top: px((142 + orangePosition.value.faceY) * (1 - showPose.value) + (136 * showPose.value)),
    borderBottom: isFrown ? '3px solid #2D2D2D' : isShy ? '2px solid #2D2D2D' : '0',
    borderRadius: isFrown ? '0 0 60px 60px' : isShy ? '0 0 36px 36px' : '999px',
    backgroundColor: isFrown || isShy ? 'transparent' : '#2D2D2D',
    transform: `translateY(${isShy ? 2 : isFrown ? -1 : 0}px)`,
    transition: 'all 260ms cubic-bezier(0.22, 1, 0.36, 1)',
  }
})

function shouldHidePupil(key: CharacterKey, side: EyeSide) {
  if (key === 'purple' && isPurpleBlinking.value) return true
  if (key === 'black' && isBlackBlinking.value) return true
  return expressionStates.value[key] === 'blink' && blinkSides.value[key] === side
}

function expressionPupilAdjust(key: CharacterKey, side: EyeSide) {
  const state = expressionStates.value[key]
  if (state === 'frown') {
    return { x: side === 'left' ? -1.6 : 1.6, y: -1.2, scale: 1.05 }
  }
  if (state === 'shy') {
    return { x: side === 'left' ? 1.3 : -1.3, y: 1.8, scale: 0.86 }
  }
  return { x: 0, y: 0, scale: 1 }
}

function eyeContainerStyle(
  key: CharacterKey,
  side: EyeSide,
  size: number,
  isAutoBlinking: boolean,
) {
  const state = expressionStates.value[key]
  const isExpressionBlink = state === 'blink' && blinkSides.value[key] === side
  const closed = isAutoBlinking || isExpressionBlink
  let height = closed ? 2 : size
  if (!closed && state === 'shy') height = Math.max(4, Math.round(size * 0.72))
  if (!closed && state === 'frown') height = Math.max(4, Math.round(size * 0.84))
  const translateY = state === 'shy' ? 2 : state === 'frown' ? -1 : 0
  return {
    width: `${size}px`,
    height: `${height}px`,
    backgroundColor: 'white',
    overflow: 'hidden',
    transform: `translateY(${translateY}px)`,
    transition: 'height 240ms cubic-bezier(0.22, 1, 0.36, 1), transform 240ms cubic-bezier(0.22, 1, 0.36, 1)',
  }
}

const purpleLeftEyeStyle = computed(() => eyeContainerStyle('purple', 'left', 18, isPurpleBlinking.value))
const purpleRightEyeStyle = computed(() => eyeContainerStyle('purple', 'right', 18, isPurpleBlinking.value))
const blackLeftEyeStyle = computed(() => eyeContainerStyle('black', 'left', 16, isBlackBlinking.value))
const blackRightEyeStyle = computed(() => eyeContainerStyle('black', 'right', 16, isBlackBlinking.value))

function eyePupilStyle(
  eyeEl: HTMLElement | null,
  key: CharacterKey,
  side: EyeSide,
  size: number,
  maxDistance: number,
  force: { x: number; y: number } | null,
) {
  const look = calculateEyeLook(eyeEl, maxDistance, force?.x, force?.y)
  const adjust = expressionPupilAdjust(key, side)
  return {
    width: `${size}px`,
    height: `${size}px`,
    backgroundColor: '#2D2D2D',
    transform: `translate(${look.x + adjust.x}px, ${look.y + adjust.y}px) scale(${adjust.scale})`,
    transition: 'transform 280ms cubic-bezier(0.22, 1, 0.36, 1)',
  }
}

const purpleLeftPupilStyle = computed(() => eyePupilStyle(purpleLeftEyeRef.value, 'purple', 'left', 7, 5, purpleForceLook.value))
const purpleRightPupilStyle = computed(() => eyePupilStyle(purpleRightEyeRef.value, 'purple', 'right', 7, 5, purpleForceLook.value))
const blackLeftPupilStyle = computed(() => eyePupilStyle(blackLeftEyeRef.value, 'black', 'left', 6, 4, blackForceLook.value))
const blackRightPupilStyle = computed(() => eyePupilStyle(blackRightEyeRef.value, 'black', 'right', 6, 4, blackForceLook.value))

function pupilOnlyStyle(
  eyeEl: HTMLElement | null,
  key: CharacterKey,
  side: EyeSide,
  maxDistance: number,
  force: { x: number; y: number } | null,
) {
  const look = calculateEyeLook(eyeEl, maxDistance, force?.x, force?.y)
  const adjust = expressionPupilAdjust(key, side)
  const isExpressionBlink = expressionStates.value[key] === 'blink' && blinkSides.value[key] === side
  const width = isExpressionBlink ? 12 : 12 * adjust.scale
  const height = isExpressionBlink ? 3 : 12 * adjust.scale
  const extraY = isExpressionBlink ? 4 : 0
  return {
    width: `${width}px`,
    height: `${height}px`,
    borderRadius: '999px',
    backgroundColor: '#2D2D2D',
    transform: `translate(${look.x + adjust.x}px, ${look.y + adjust.y + extraY}px)`,
    transition: 'all 280ms cubic-bezier(0.22, 1, 0.36, 1)',
  }
}

const orangeLeftPupilStyle = computed(() => pupilOnlyStyle(orangeLeftEyeRef.value, 'orange', 'left', 5, orangeYellowForceLook.value))
const orangeRightPupilStyle = computed(() => pupilOnlyStyle(orangeRightEyeRef.value, 'orange', 'right', 5, orangeYellowForceLook.value))
const yellowLeftPupilStyle = computed(() => pupilOnlyStyle(yellowLeftEyeRef.value, 'yellow', 'left', 5, orangeYellowForceLook.value))
const yellowRightPupilStyle = computed(() => pupilOnlyStyle(yellowRightEyeRef.value, 'yellow', 'right', 5, orangeYellowForceLook.value))

onMounted(() => {
  targetMouseX.value = window.innerWidth / 2
  targetMouseY.value = window.innerHeight / 2
  mouseX.value = targetMouseX.value
  mouseY.value = targetMouseY.value
  bodyMouseX.value = targetMouseX.value
  bodyMouseY.value = targetMouseY.value
  usernamePose.value = isUsernameFocused.value ? 1 : 0
  passwordPose.value = shouldPurplePeekWhenPasswordFocus.value ? 1 : 0
  showPose.value = isShowingPassword.value ? 1 : 0
  window.addEventListener('mousemove', handleMouseMove, { passive: true })
  startMouseSmoothing()
  schedulePurpleBlink()
  scheduleBlackBlink()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  if (mouseRafId) window.cancelAnimationFrame(mouseRafId)
  clearTimer(purpleBlinkTimer)
  clearTimer(purpleBlinkEndTimer)
  clearTimer(blackBlinkTimer)
  clearTimer(blackBlinkEndTimer)
  clearTimer(bounceTimers.purple)
  clearTimer(bounceTimers.black)
  clearTimer(bounceTimers.orange)
  clearTimer(bounceTimers.yellow)
  clearExpressionTimer('purple')
  clearExpressionTimer('black')
  clearExpressionTimer('orange')
  clearExpressionTimer('yellow')
})
</script>

<style scoped>
.character {
  transition:
    scale 240ms cubic-bezier(0.22, 1, 0.36, 1),
    filter 220ms cubic-bezier(0.22, 1, 0.36, 1);
}

.character-purple {
  z-index: 10;
}

.character-black {
  z-index: 20;
}

.character-orange {
  z-index: 30;
}

.character-yellow {
  z-index: 40;
}

.character-hit {
  touch-action: manipulation;
  will-change: transform, scale, filter;
}

.eye-pupil {
  transform-origin: center;
  will-change: transform;
}

.character.exp-blink {
  scale: 1.008 0.992;
}

.character.exp-frown {
  scale: 1.014 0.986;
  filter: saturate(1.06) brightness(0.98);
}

.character.exp-shy {
  scale: 0.992 1.012;
  filter: saturate(0.96) brightness(1.02);
}

.monster-bounce {
  animation: monster-jelly 680ms cubic-bezier(0.22, 1, 0.36, 1);
}

@keyframes monster-jelly {
  0% {
    scale: 1;
  }
  14% {
    scale: 1.09 0.9;
  }
  32% {
    scale: 0.92 1.11;
  }
  48% {
    scale: 1.045 0.965;
  }
  66% {
    scale: 0.985 1.02;
  }
  84% {
    scale: 1.01 0.992;
  }
  100% {
    scale: 1;
  }
}
</style>
