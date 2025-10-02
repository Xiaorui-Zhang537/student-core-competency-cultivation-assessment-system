<template>
  <component
    :is="tag"
    :class="[
      'inline-flex items-center justify-center rounded-full font-medium transition-all duration-200 outline-none ring-0 focus:outline-none focus-visible:outline-none focus:ring-0 focus:ring-offset-0 focus-visible:ring-0 focus-visible:ring-offset-0 disabled:opacity-50 disabled:cursor-not-allowed',
      sizeClasses,
      variantClasses,
      contentGapClass,
      $attrs.class
    ]"
    :disabled="disabled || loading"
    v-bind="$attrs"
    @click="handleClick"
  >
    <liquid-glass
      v-if="isGlassVariant"
      containerClass="absolute inset-0 rounded-full pointer-events-none"
      class="rounded-full"
      :radius="999"
      :frost="0.05"
      :border="0.07"
      :lightness="50"
      :alpha="0.93"
      :blur="11"
      :scale="-180"
      :rOffset="0"
      :gOffset="10"
      :bOffset="20"
    />
    <svg
      v-if="loading"
      class="animate-spin -ml-1 mr-2 h-4 w-4"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    <span v-if="iconOnLeft" class="inline-flex items-center">
      <slot name="icon">
        <span v-if="icon" aria-hidden="true" class="inline-flex">
          <svg class="h-4 w-4" v-bind="iconSvgAttrs" v-html="renderIcon(icon)"></svg>
        </span>
      </slot>
    </span>
    <slot />
    <span v-if="iconOnRight || $slots.suffix" class="inline-flex items-center">
      <slot name="suffix">
        <span v-if="icon && iconPosition==='right'" aria-hidden="true" class="inline-flex">
          <svg class="h-4 w-4" v-bind="iconSvgAttrs" v-html="renderIcon(icon)"></svg>
        </span>
      </slot>
    </span>
  </component>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'

interface Props {
  variant?: 'glass' | 'glass-ghost' | 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'success' | 'warning' | 'indigo' | 'purple' | 'teal'
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  loading?: boolean
  disabled?: boolean
  as?: 'button' | 'a'
  icon?: 'plus' | 'edit' | 'delete' | 'save' | 'confirm' | 'cancel' | 'download' | 'arrow-left' | 'arrow-right' | 'close' | 'search' | 'key' | 'user-plus'
  iconPosition?: 'left' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  as: 'button',
  icon: undefined,
  iconPosition: 'left'
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

const slots = useSlots()

const sizeClasses = computed(() => {
  // Unified fixed-height system (width grows with content)
  const sizes = {
    xs: 'h-7 px-2 text-xs',      // ~28px
    sm: 'h-8 px-3 text-sm',      // ~32px
    md: 'h-9 px-4 text-sm',      // ~36px (default)
    lg: 'h-11 px-5 text-base',   // ~44px
    xl: 'h-12 px-6 text-lg'      // ~48px
  }
  return sizes[props.size]
})

const contentGapClass = computed(() => {
  return (props.loading || slots.icon || slots.suffix) ? 'gap-2' : ''
})

const tag = computed(() => props.as || 'button')

const iconPosition = computed(() => props.iconPosition || 'left')
const iconOnLeft = computed(() => !!(slots.icon || (props.icon && iconPosition.value==='left')))
const iconOnRight = computed(() => !!(props.icon && iconPosition.value==='right'))

// 统一控制图标 SVG 属性：删除图标使用描边版（与 AI 助手一致），其余保持填充版
const iconSvgAttrs = computed<Record<string, string | undefined>>(() => {
  if (props.icon === 'delete' || props.icon === 'download') {
    return {
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '1.6',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }
  }
  return { viewBox: '0 0 20 20', fill: 'currentColor' }
})

function renderIcon(name?: Props['icon']): string {
  switch (name) {
    case 'plus': return '<path d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" />'
    case 'edit': return '<path d="M4 13.5V16h2.5L14 8.5l-2.5-2.5L4 13.5z"/><path d="M13.5 3a1 1 0 011.4 0l1.6 1.6a1 1 0 010 1.4l-1.1 1.1-3-3L13.5 3z" />'
    case 'delete': return '<polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>'
    case 'save': return '<path d="M17 3H3v14h14V3zM5 5h10v10H5V5zm2 2h6v2H7V7z" />'
    case 'confirm': return '<path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414L8.414 15l-4.121-4.121a1 1 0 111.414-1.414L8.414 12.172l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />'
    case 'cancel': return '<path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />'
    case 'download': return '<path d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5" /><path d="M7.5 9l4.5 4.5L16.5 9" /><path d="M12 3v10.5" />'
    case 'arrow-left': return '<path d="M12 5l-5 5 5 5V5z" />'
    case 'arrow-right': return '<path d="M8 5l5 5-5 5V5z" />'
    case 'close': return '<path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />'
    case 'search': return '<path fill-rule="evenodd" d="M12.9 14.32a8 8 0 111.414-1.414l4.387 4.387-1.414 1.414-4.387-4.387zM14 8a6 6 0 11-12 0 6 6 0 0112 0z" clip-rule="evenodd" />'
    case 'key': return '<path d="M14 8a4 4 0 10-7.465 1.94L2 14.475V17h2.525l1.535-1.535A4 4 0 0014 8zm-6 0a2 2 0 113.999.001A2 2 0 018 8z" />'
    case 'user-plus': return '<path d="M8 9a3 3 0 100-6 3 3 0 000 6z"/><path d="M2 16a6 6 0 1112 0v1H2v-1zM16 7h2v2h2v2h-2v2h-2v-2h-2V9h2V7z" />'
    default: return ''
  }
}

const isGlassVariant = computed(() => props.variant === 'glass' || props.variant === 'glass-ghost')

const variantClasses = computed(() => {
  const baseGlass = 'relative overflow-hidden text-base-content hover:bg-white/5'
  const baseGhost = 'relative overflow-hidden text-base-content hover:bg-white/5'
  const variants: Record<string, string> = {
    glass: baseGlass,
    'glass-ghost': baseGhost,
    primary: 'btn-variant-primary',
    secondary: 'btn-variant-secondary',
    outline: 'btn-variant-outline',
    ghost: 'btn-variant-ghost',
    danger: 'btn-variant-danger',
    success: 'btn-variant-success',
    warning: 'btn-variant-warning',
    info: 'btn-variant-info',
    indigo: 'btn-variant-info',
    purple: 'btn-variant-info',
    teal: 'btn-variant-info',
    menu: 'relative overflow-hidden text-base-content hover:bg-white/10 justify-start items-center text-left !px-0 outline-none ring-0 focus:outline-none focus-visible:outline-none focus:ring-0 focus:ring-offset-0 focus-visible:ring-0 focus-visible:ring-offset-0 active:ring-0'
  }
  return variants[props.variant]
})

const handleClick = (event: MouseEvent) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script> 