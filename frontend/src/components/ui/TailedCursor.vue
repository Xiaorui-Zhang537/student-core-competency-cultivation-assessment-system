<template>
  <div
    ref="containerRef"
    class="pointer-events-none fixed inset-0 z-50"
  />
</template>

<script setup lang="ts">
import { Color, Polyline, Renderer, Transform, Vec3 } from 'ogl'
import { computed, watch as vueWatch } from 'vue'
import { useUIStore } from '@/stores/ui'
import { getThemeCoreColors } from '@/utils/theme'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

interface RibbonsProps {
  colors?: string[]
  baseSpring?: number
  baseFriction?: number
  baseThickness?: number
  offsetFactor?: number
  maxAge?: number
  pointCount?: number
  speedMultiplier?: number
  enableFade?: boolean
  enableShaderEffect?: boolean
  effectAmplitude?: number
  backgroundColor?: number[]
}

const props = withDefaults(defineProps<RibbonsProps>(), {
  colors: () => [],
  baseSpring: 0.02,
  baseFriction: 0.91,
  baseThickness: 12,
  offsetFactor: 0.04,
  maxAge: 380,
  pointCount: 36,
  speedMultiplier: 0.5,
  enableFade: false,
  enableShaderEffect: false,
  effectAmplitude: 2,
  backgroundColor: () => [0, 0, 0, 0],
})

const containerRef = ref<HTMLDivElement | null>(null)
const ui = useUIStore()
const themePrimary = computed(() => {
  // 与 Smooth 一致：使用主题工具获取主色（已考虑明暗与可读性）
  void ui.themeName; void ui.isDarkMode
  return getThemeCoreColors().primary
})

// 将各种颜色表示解析为 OGL Color 可用的 [r,g,b] (0-1)
function parseColorToRGB01(input: string): [number, number, number] {
  const c = (input || '').trim()
  // #rrggbb
  if (c.startsWith('#')) {
    const s = c.slice(1)
    const n = parseInt(s, 16)
    const r = (n >> 16) & 255
    const g = (n >> 8) & 255
    const b = n & 255
    return [r / 255, g / 255, b / 255]
  }
  // rgb()/rgba()/hsl()/hsla() 或 css 变量引用，交给浏览器解析
  const el = document.createElement('span')
  el.style.color = c
  document.body.appendChild(el)
  const cs = getComputedStyle(el).color // e.g. "rgb(34, 211, 238)"
  document.body.removeChild(el)
  const m = cs.match(/rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)/i)
  if (m) {
    const r = parseInt(m[1], 10)
    const g = parseInt(m[2], 10)
    const b = parseInt(m[3], 10)
    return [r / 255, g / 255, b / 255]
  }
  // 兜底青色
  return [34 / 255, 211 / 255, 238 / 255]
}

onMounted(() => {
  const container = containerRef.value
  if (!container) return

  const renderer = new Renderer({ dpr: window.devicePixelRatio || 2, alpha: true })
  const gl = renderer.gl
  if (Array.isArray(props.backgroundColor) && props.backgroundColor.length === 4) {
    gl.clearColor(
      props.backgroundColor[0],
      props.backgroundColor[1],
      props.backgroundColor[2],
      props.backgroundColor[3],
    )
  } else {
    gl.clearColor(0, 0, 0, 0)
  }

  gl.canvas.style.position = 'absolute'
  gl.canvas.style.top = '0'
  gl.canvas.style.left = '0'
  gl.canvas.style.width = '100%'
  gl.canvas.style.height = '100%'
  container.appendChild(gl.canvas)

  const scene = new Transform()
  const lines: Array<{
    spring: number
    friction: number
    mouseVelocity: Vec3
    mouseOffset: Vec3
    points: Vec3[]
    polyline: Polyline
  }> = []

  const vertex = `
    precision highp float;
    attribute vec3 position;
    attribute vec3 next;
    attribute vec3 prev;
    attribute vec2 uv;
    attribute float side;
    uniform vec2 uResolution;
    uniform float uDPR;
    uniform float uThickness;
    uniform float uTime;
    uniform float uEnableShaderEffect;
    uniform float uEffectAmplitude;
    varying vec2 vUV;
    vec4 getPosition() {
      vec4 current = vec4(position, 1.0);
      vec2 aspect = vec2(uResolution.x / uResolution.y, 1.0);
      vec2 nextScreen = next.xy * aspect;
      vec2 prevScreen = prev.xy * aspect;
      vec2 tangent = normalize(nextScreen - prevScreen);
      vec2 normal = vec2(-tangent.y, tangent.x);
      normal /= aspect;
      normal *= mix(1.0, 0.1, pow(abs(uv.y - 0.5) * 2.0, 2.0));
      float dist = length(nextScreen - prevScreen);
      normal *= smoothstep(0.0, 0.02, dist);
      float pixelWidthRatio = 1.0 / (uResolution.y / uDPR);
      float pixelWidth = current.w * pixelWidthRatio;
      normal *= pixelWidth * uThickness;
      current.xy -= normal * side;
      if(uEnableShaderEffect > 0.5) {
        current.xy += normal * sin(uTime + current.x * 10.0) * uEffectAmplitude;
      }
      return current;
    }
    void main() { vUV = uv; gl_Position = getPosition(); }
  `

  const fragment = `
    precision highp float;
    uniform vec3 uColor;
    uniform float uOpacity;
    uniform float uEnableFade;
    varying vec2 vUV;
    void main() {
      float fadeFactor = 1.0;
      if(uEnableFade > 0.5) { fadeFactor = 1.0 - smoothstep(0.0, 1.0, vUV.y); }
      gl_FragColor = vec4(uColor, uOpacity * fadeFactor);
    }
  `

  function resize() {
    if (!container) return
    const width = container.clientWidth
    const height = container.clientHeight
    renderer.setSize(width, height)
    lines.forEach((line) => line.polyline.resize())
  }
  window.addEventListener('resize', resize)

  const dynamicColors = (() => {
    // 单色：严格跟随主题主色，降低干扰
    const base = themePrimary.value || '#22d3ee'
    return [base]
  })()

  const baseColors = (props.colors && props.colors.length) ? props.colors : dynamicColors
  const center = (baseColors.length - 1) / 2
  baseColors.forEach((color, index) => {
    const spring = props.baseSpring + (Math.random() - 0.5) * 0.05
    const friction = props.baseFriction + (Math.random() - 0.5) * 0.05
    const thickness = props.baseThickness + (Math.random() - 0.5) * 3
    const mouseOffset = new Vec3(
      (index - center) * props.offsetFactor + (Math.random() - 0.5) * 0.01,
      (Math.random() - 0.5) * 0.1,
      0,
    )

    const line = {
      spring,
      friction,
      mouseVelocity: new Vec3(),
      mouseOffset,
      points: [] as Vec3[],
      polyline: {} as Polyline,
    }

    const count = props.pointCount
    const points: Vec3[] = []
    for (let i = 0; i < count; i++) points.push(new Vec3())
    line.points = points

    const rgb01 = parseColorToRGB01(color)
    line.polyline = new Polyline(gl, {
      points,
      vertex,
      fragment,
      uniforms: {
        uColor: { value: new Color(rgb01[0], rgb01[1], rgb01[2]) },
        uThickness: { value: thickness },
        uOpacity: { value: 0.7 },
        uTime: { value: 0.0 },
        uEnableShaderEffect: { value: props.enableShaderEffect ? 1.0 : 0.0 },
        uEffectAmplitude: { value: props.effectAmplitude },
        uEnableFade: { value: props.enableFade ? 1.0 : 0.0 },
      },
    })
    line.polyline.mesh.setParent(scene)
    lines.push(line)
  })

  resize()

  const mouse = new Vec3()
  function updateMouse(e: MouseEvent | TouchEvent) {
    let x: number, y: number
    const rect = (container?.getBoundingClientRect?.() || ({ left: 0, top: 0, width: window.innerWidth, height: window.innerHeight } as any))
    if ('changedTouches' in e && (e as TouchEvent).changedTouches?.length) {
      const t = (e as TouchEvent).changedTouches[0]
      x = t.clientX - rect.left
      y = t.clientY - rect.top
    } else if ('clientX' in e) {
      const m = e as MouseEvent
      x = m.clientX - rect.left
      y = m.clientY - rect.top
    } else { x = 0; y = 0 }
    const width = (container?.clientWidth || window.innerWidth)
    const height = (container?.clientHeight || window.innerHeight)
    mouse.set((x / width) * 2 - 1, (y / height) * -2 + 1, 0)
  }
  // 监听挂到 window，避免容器 pointer-events:none 无法触发事件
  window.addEventListener('mousemove', updateMouse)
  window.addEventListener('touchstart', updateMouse)
  window.addEventListener('touchmove', updateMouse)

  const tmp = new Vec3()
  let frameId = 0
  let lastTime = performance.now()
  function update() {
    frameId = requestAnimationFrame(update)
    const currentTime = performance.now()
    const dt = currentTime - lastTime
    lastTime = currentTime

    lines.forEach((line) => {
      tmp.copy(mouse).add(line.mouseOffset).sub(line.points[0]).multiply(line.spring)
      line.mouseVelocity.add(tmp).multiply(line.friction)
      line.points[0].add(line.mouseVelocity)

      for (let i = 1; i < line.points.length; i++) {
        if (isFinite(props.maxAge) && props.maxAge > 0) {
          const segmentDelay = props.maxAge / (line.points.length - 1)
          const alpha = Math.min(1, (dt * props.speedMultiplier) / segmentDelay)
          line.points[i].lerp(line.points[i - 1], alpha)
        } else {
          line.points[i].lerp(line.points[i - 1], 0.9)
        }
      }
      if ((line.polyline.mesh.program.uniforms as any).uTime) {
        ;(line.polyline.mesh.program.uniforms as any).uTime.value = currentTime * 0.001
      }
      line.polyline.updateGeometry()
    })

    renderer.render({ scene })
  }
  update()

  watch(() => props.enableShaderEffect, (newValue) => {
    lines.forEach((line) => {
      const u = (line.polyline.mesh.program.uniforms as any)
      if (u.uEnableShaderEffect) u.uEnableShaderEffect.value = newValue ? 1.0 : 0.0
    })
  })

  watch(() => props.enableFade, (newValue) => {
    lines.forEach((line) => {
      const u = (line.polyline.mesh.program.uniforms as any)
      if (u.uEnableFade) u.uEnableFade.value = newValue ? 1.0 : 0.0
    })
  })

  watch(() => props.effectAmplitude, (newValue) => {
    lines.forEach((line) => {
      const u = (line.polyline.mesh.program.uniforms as any)
      if (u.uEffectAmplitude) u.uEffectAmplitude.value = newValue
    })
  })

  // 主题变化时，动态更新颜色
  vueWatch(themePrimary, (v) => {
    const [r, g, b] = parseColorToRGB01(v)
    lines.forEach((line) => {
      const u = (line.polyline.mesh.program.uniforms as any)
      if (u.uColor) u.uColor.value = new Color(r, g, b)
    })
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    window.removeEventListener('mousemove', updateMouse)
    window.removeEventListener('touchstart', updateMouse)
    window.removeEventListener('touchmove', updateMouse)
    cancelAnimationFrame(frameId)
    if (gl.canvas && container && gl.canvas.parentNode === container) {
      container.removeChild(gl.canvas)
    }
  })
})
</script>

<style scoped>
</style>


