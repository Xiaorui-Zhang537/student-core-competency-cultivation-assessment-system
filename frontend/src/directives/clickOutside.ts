// A Vue 3 global directive to detect clicks outside of an element
// Usage: v-click-outside="() => closeFn()"
import type { DirectiveBinding, ObjectDirective } from 'vue'

type HTMLElementWithCo = HTMLElement & {
  __clickOutsideHandler__?: (e: MouseEvent) => void
}

const ClickOutsideDirective: ObjectDirective = {
  beforeMount(el: HTMLElementWithCo, binding: DirectiveBinding) {
    const handler = (e: MouseEvent) => {
      // If click is inside the element, ignore
      if (el === e.target || el.contains(e.target as Node)) return
      // Invoke bound function if provided
      const fn = binding.value
      if (typeof fn === 'function') {
        try {
          fn(e)
        } catch {
          // no-op to avoid breaking UI due to handler errors
        }
      }
    }
    el.__clickOutsideHandler__ = handler
    // Use capture to ensure we run early and reliably
    document.addEventListener('click', handler, true)
  },
  updated(el: HTMLElementWithCo, binding: DirectiveBinding) {
    // no special handling needed; binding.value is read at call time
  },
  unmounted(el: HTMLElementWithCo) {
    if (el.__clickOutsideHandler__) {
      document.removeEventListener('click', el.__clickOutsideHandler__, true)
      delete el.__clickOutsideHandler__
    }
  }
}

export default ClickOutsideDirective


