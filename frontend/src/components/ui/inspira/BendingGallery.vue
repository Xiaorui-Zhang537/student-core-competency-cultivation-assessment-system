<template>
  <div
    ref="containerRef"
    class="w-full h-full overflow-hidden cursor-grab active:cursor-grabbing pointer-events-auto"
  />
  
</template>

<script setup lang="ts">
/* eslint-disable @typescript-eslint/no-explicit-any  */

import { Camera, Mesh, Plane, Program, Renderer, Texture, Transform } from 'ogl'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

type GL = Renderer['gl']

interface BendingGalleryItem { image: string; text: string }

interface BendingGalleryProps {
  items?: BendingGalleryItem[]
  bend?: number
  textColor?: string
  borderRadius?: number
  font?: string
  aspectRatio?: number // width / height, e.g., 16/9
  dpr?: number // device pixel ratio for renderer
  anisotropy?: number // texture anisotropic filtering level
  wobble?: number // image surface wobble amplitude (0 = disable)
  wheelScroll?: boolean // whether mouse wheel scroll drives the gallery
  dragWithinContainer?: boolean // limit events to container only
  textGap?: number // world units gap between image bottom and text baseline
}

const props = withDefaults(defineProps<BendingGalleryProps>(), {
  bend: 3,
  textColor: 'var(--color-base-content)',
  borderRadius: 0.05,
  font: "400 28px 'PingFang SC', -apple-system, BlinkMacSystemFont, 'Hiragino Sans GB', 'Microsoft YaHei', 'Noto Sans CJK SC', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
  aspectRatio: 16 / 9,
  dpr: 2,
  anisotropy: 8,
  wobble: 0.02,
  wheelScroll: false,
  textGap: 0.12,
  dragWithinContainer: true,
})

const containerRef = ref<HTMLDivElement | null>(null)
let app: App | null = null

function debounce<T extends (...args: any[]) => void>(func: T, wait: number) {
  let timeout: number
  return function (this: any, ...args: Parameters<T>) {
    window.clearTimeout(timeout)
    timeout = window.setTimeout(() => func.apply(this, args), wait)
  }
}

function lerp(p1: number, p2: number, t: number): number { return p1 + (p2 - p1) * t }
function autoBind(instance: any): void {
  const proto = Object.getPrototypeOf(instance)
  Object.getOwnPropertyNames(proto).forEach((key) => {
    if (key !== 'constructor' && typeof instance[key] === 'function') {
      instance[key] = instance[key].bind(instance)
    }
  })
}
function getFontSize(font: string): number {
  const match = font.match(/(\d+)px/)
  return match ? Number.parseInt(match[1], 10) : 30
}
function resolveCssColor(color: string): string {
  let resolved = color
  try {
    if (color && color.startsWith('var(')) {
      const varName = color.slice(4, -1).trim()
      const rs = getComputedStyle(document.documentElement)
      const v = rs.getPropertyValue(varName).trim()
      if (v) resolved = v
      // Fallbacks when CSS var not defined
      if (!v || v.length === 0) {
        const candidates = ['--color-base-content', '--bc', '--color-content', '--color-info', '--in', '--color-theme-primary', '--p', '--color-primary']
        for (const cand of candidates) {
          const alt = rs.getPropertyValue(cand).trim()
          if (alt) { resolved = alt; break }
        }
        if (!resolved || resolved.startsWith('var(')) {
          resolved = '#22d3ee'
        }
      }
    }
  } catch {}
  return resolved
}
function parseCssColorToRgbFloat(input: string): [number, number, number] {
  const v = (input || '').trim()
  if (!v) return [1, 1, 1]
  // #rrggbb or #rgb
  if (v.startsWith('#')) {
    const hex = v.slice(1)
    if (hex.length === 3) {
      const r = parseInt(hex[0] + hex[0], 16)
      const g = parseInt(hex[1] + hex[1], 16)
      const b = parseInt(hex[2] + hex[2], 16)
      return [r / 255, g / 255, b / 255]
    }
    if (hex.length === 6) {
      const r = parseInt(hex.slice(0, 2), 16)
      const g = parseInt(hex.slice(2, 4), 16)
      const b = parseInt(hex.slice(4, 6), 16)
      return [r / 255, g / 255, b / 255]
    }
  }
  // rgb(r,g,b)
  const m = v.match(/rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)/i)
  if (m) {
    const r = Number(m[1]) / 255
    const g = Number(m[2]) / 255
    const b = Number(m[3]) / 255
    return [r, g, b]
  }
  return [1, 1, 1]
}
function createTextTexture(
  gl: GL,
  text: string,
  font: string = 'bold 30px monospace',
  color: string = 'black',
): { texture: Texture; width: number; height: number } {
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  if (!context) throw new Error('Could not get 2d context')

  context.font = font
  const metrics = context.measureText(text)
  const textWidth = Math.ceil(metrics.width)
  const fontSize = getFontSize(font)
  const textHeight = Math.ceil(fontSize * 1.2)

  canvas.width = textWidth + 20
  canvas.height = textHeight + 20

  context.font = font
  // Resolve CSS variable colors to computed value with theme fallbacks
  const resolvedColor = resolveCssColor(color)
  context.fillStyle = resolvedColor
  context.textBaseline = 'middle'
  context.textAlign = 'center'
  context.clearRect(0, 0, canvas.width, canvas.height)
  context.fillText(text, canvas.width / 2, canvas.height / 2)

  const texture = new Texture(gl, { generateMipmaps: false })
  texture.image = canvas
  return { texture, width: canvas.width, height: canvas.height }
}

interface TitleProps {
  gl: GL
  plane: Mesh
  renderer: Renderer
  text: string
  textColor?: string
  font?: string
}
class Title {
  gl: GL
  plane: Mesh
  renderer: Renderer
  text: string
  textColor: string
  font: string
  mesh!: Mesh
  aspect!: number
  currentResolvedColor: string = ''
  constructor({ gl, plane, renderer, text, textColor = '#545050', font = '30px sans-serif' }: TitleProps) {
    autoBind(this)
    this.gl = gl
    this.plane = plane
    this.renderer = renderer
    this.text = text
    this.textColor = textColor
    this.font = font
    this.createMesh()
  }
  createMesh() {
    const { texture, width, height } = createTextTexture(this.gl, this.text, this.font, this.textColor)
    const geometry = new Plane(this.gl)
    const program = new Program(this.gl, {
      vertex: `
          attribute vec3 position;
          attribute vec2 uv;
          uniform mat4 modelViewMatrix;
          uniform mat4 projectionMatrix;
          varying vec2 vUv;
          void main() {
            vUv = uv;
            gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
          }
        `,
      fragment: `
          precision highp float;
          uniform sampler2D tMap;
          // simple soft shadow around glyphs to improve readability
          vec4 sample(vec2 uv, float dx, float dy) {
            return texture2D(tMap, uv + vec2(dx, dy));
          }
          varying vec2 vUv;
          void main() {
            vec4 base = texture2D(tMap, vUv);
            float s = 1.0/1024.0; // shadow spread based on texture size approx
            vec4 shadow = (
              sample(vUv, -2.0*s, 0.0) + sample(vUv, 2.0*s, 0.0) +
              sample(vUv, 0.0, -2.0*s) + sample(vUv, 0.0, 2.0*s) +
              sample(vUv, -s, -s) + sample(vUv, s, s) +
              sample(vUv, -s, s) + sample(vUv, s, -s)
            ) * 0.08;
            vec4 color = max(base, shadow);
            if (color.a < 0.1) discard;
            gl_FragColor = color;
          }
        `,
      uniforms: { tMap: { value: texture } },
      transparent: true,
    })
    this.mesh = new Mesh(this.gl, { geometry, program })
    this.aspect = width / height
    this.currentResolvedColor = resolveCssColor(this.textColor)
    // 初次根据父缩放设置，后续在 onResize 中同步
    const parentScaleX = this.plane.scale.x || 1
    const parentScaleY = this.plane.scale.y || 1
    const worldTextH = (parentScaleY * 0.15)
    const worldTextW = worldTextH * this.aspect
    this.mesh.scale.set(worldTextW / parentScaleX, worldTextH / parentScaleY, 1)
    this.mesh.position.y = (-parentScaleY * 0.5 - worldTextH * 0.5 - 0.05) / parentScaleY
    this.mesh.setParent(this.plane)
  }

  updateForParentScale(parentScaleX: number, parentScaleY: number) {
    const worldTextH = (parentScaleY * 0.15)
    const worldTextW = worldTextH * this.aspect
    this.mesh.scale.set(worldTextW / (parentScaleX || 1), worldTextH / (parentScaleY || 1), 1)
    const offset = Math.max(0.02, (props.textGap ?? 0.12))
    const localY = (-parentScaleY * 0.5 - worldTextH * 0.5 - offset) / (parentScaleY || 1)
    this.mesh.position.y = localY
  }

  updateColorIfChanged() {
    const next = resolveCssColor(this.textColor)
    if (next === this.currentResolvedColor) return
    this.currentResolvedColor = next
    // Recreate text texture with new color
    const { texture } = createTextTexture(this.gl, this.text, this.font, this.textColor)
    ;(this.mesh.program as any).uniforms.tMap.value = texture
  }
}

interface ScreenSize { width: number; height: number }
interface Viewport { width: number; height: number }

interface MediaProps {
  geometry: Plane
  gl: GL
  image: string
  index: number
  length: number
  renderer: Renderer
  scene: Transform
  screen: ScreenSize
  text: string
  viewport: Viewport
  bend: number
  textColor: string
  borderRadius?: number
  font?: string
}

class Media {
  extra: number = 0
  geometry: Plane
  gl: GL
  image: string
  index: number
  length: number
  renderer: Renderer
  scene: Transform
  screen: ScreenSize
  text: string
  viewport: Viewport
  bend: number
  textColor: string
  borderRadius: number
  font?: string
  program!: Program
  plane!: Mesh
  title!: Title
  scale!: number
  padding!: number
  width!: number
  widthTotal!: number
  x!: number
  speed: number = 0
  isBefore: boolean = false
  isAfter: boolean = false
  colorCheckCounter: number = 0
  constructor({ geometry, gl, image, index, length, renderer, scene, screen, text, viewport, bend, textColor, borderRadius = 0, font, }: MediaProps) {
    this.geometry = geometry
    this.gl = gl
    this.image = image
    this.index = index
    this.length = length
    this.renderer = renderer
    this.scene = scene
    this.screen = screen
    this.text = text
    this.viewport = viewport
    this.bend = bend
    this.textColor = textColor
    this.borderRadius = borderRadius
    this.font = font
    this.createShader()
    this.createMesh()
    this.createTitle()
    this.onResize()
  }
  createShader() {
    const texture = new Texture(this.gl, { generateMipmaps: false })
    this.program = new Program(this.gl, {
      depthTest: false,
      depthWrite: false,
      vertex: `
          precision highp float;
          attribute vec3 position;
          attribute vec2 uv;
          uniform mat4 modelViewMatrix;
          uniform mat4 projectionMatrix;
          uniform float uTime;
          uniform float uSpeed;
          uniform float uWobble;
          varying vec2 vUv;
          void main() {
            vUv = uv;
            vec3 p = position;
            float wobble = uWobble + abs(uSpeed) * 0.1;
            p.z = (sin(p.x * 4.0 + uTime) * 1.5 + cos(p.y * 2.0 + uTime) * 1.5) * wobble;
            gl_Position = projectionMatrix * modelViewMatrix * vec4(p, 1.0);
          }
        `,
      fragment: `
          precision highp float;
          uniform vec2 uImageSizes;
          uniform vec2 uPlaneSizes;
          uniform sampler2D tMap;
          uniform float uBorderRadius;
          varying vec2 vUv;
          float roundedBoxSDF(vec2 p, vec2 b, float r) {
            vec2 d = abs(p) - b;
            return length(max(d, vec2(0.0))) + min(max(d.x, d.y), 0.0) - r;
          }
          void main() {
            vec2 ratio = vec2(
              min((uPlaneSizes.x / uPlaneSizes.y) / (uImageSizes.x / uImageSizes.y), 1.0),
              min((uPlaneSizes.y / uPlaneSizes.x) / (uImageSizes.y / uImageSizes.x), 1.0)
            );
            vec2 uv = vec2(
              vUv.x * ratio.x + (1.0 - ratio.x) * 0.5,
              vUv.y * ratio.y + (1.0 - ratio.y) * 0.5
            );
            vec4 color = texture2D(tMap, uv);
            float d = roundedBoxSDF(vUv - 0.5, vec2(0.5 - uBorderRadius), uBorderRadius);
            if(d > 0.0) { discard; }
            gl_FragColor = vec4(color.rgb, 1.0);
          }
        `,
      uniforms: {
        tMap: { value: texture },
        uPlaneSizes: { value: [0, 0] },
        uImageSizes: { value: [0, 0] },
        uSpeed: { value: 0 },
        uTime: { value: 100 * Math.random() },
        uBorderRadius: { value: this.borderRadius },
        uWobble: { value: Math.max(0.0, props.wobble ?? 0.0) },
      },
      transparent: true,
    })
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.src = this.image
    img.onload = () => {
      texture.image = img
      // Improve sampling quality
      try {
        const gl = this.gl as WebGLRenderingContext & { MAX_TEXTURE_MAX_ANISOTROPY_EXT?: number }
        texture.minFilter = gl.LINEAR
        texture.magFilter = gl.LINEAR
        // @ts-ignore: OGL Texture supports anisotropy if extension is present
        texture.anisotropy = Math.max(1, props.anisotropy || 0)
      } catch {}
      this.program.uniforms.uImageSizes.value = [img.naturalWidth, img.naturalHeight]
    }
  }
  createMesh() { this.plane = new Mesh(this.gl, { geometry: this.geometry, program: this.program }); this.plane.setParent(this.scene) }
  createTitle() { this.title = new Title({ gl: this.gl, plane: this.plane, renderer: this.renderer, text: this.text, textColor: this.textColor, font: this.font }) }
  update(scroll: { current: number; last: number }, direction: 'right' | 'left') {
    this.plane.position.x = this.x - scroll.current - this.extra
    const x = this.plane.position.x
    const H = this.viewport.width / 2
    if (this.bend === 0) { this.plane.position.y = 0; this.plane.rotation.z = 0 } else {
      const B_abs = Math.abs(this.bend)
      const R = (H * H + B_abs * B_abs) / (2 * B_abs)
      const effectiveX = Math.min(Math.abs(x), H)
      const arc = R - Math.sqrt(R * R - effectiveX * effectiveX)
      if (this.bend > 0) { this.plane.position.y = -arc; this.plane.rotation.z = -Math.sign(x) * Math.asin(effectiveX / R) }
      else { this.plane.position.y = arc; this.plane.rotation.z = Math.sign(x) * Math.asin(effectiveX / R) }
    }
    this.speed = scroll.current - scroll.last
    this.program.uniforms.uTime.value += 0.04
    this.program.uniforms.uSpeed.value = this.speed
    // Throttled color update to reflect theme changes
    if (this.title) {
      this.colorCheckCounter = (this.colorCheckCounter + 1) % 30
      if (this.colorCheckCounter === 0) this.title.updateColorIfChanged()
    }
    const planeOffset = this.plane.scale.x / 2
    const viewportOffset = this.viewport.width / 2
    this.isBefore = this.plane.position.x + planeOffset < -viewportOffset
    this.isAfter = this.plane.position.x - planeOffset > viewportOffset
    if (direction === 'right' && this.isBefore) { this.extra -= this.widthTotal; this.isBefore = this.isAfter = false }
    if (direction === 'left' && this.isAfter) { this.extra += this.widthTotal; this.isBefore = this.isAfter = false }
  }
  onResize({ screen, viewport }: { screen?: ScreenSize; viewport?: Viewport } = {}) {
    if (screen) this.screen = screen
    if (viewport) { this.viewport = viewport; if (this.plane.program.uniforms.uViewportSizes) { this.plane.program.uniforms.uViewportSizes.value = [ this.viewport.width, this.viewport.height ] } }
    this.scale = this.screen.height / 1500
    // Force media plane to target aspect ratio (width / height)
    const targetAspect = (props.aspectRatio || (16/9))
    const baseHeight = (this.viewport.height * (900 * this.scale)) / this.screen.height
    const baseWidth = baseHeight * targetAspect
    this.plane.scale.y = baseHeight
    this.plane.scale.x = baseWidth
    this.plane.program.uniforms.uPlaneSizes.value = [this.plane.scale.x, this.plane.scale.y]
    this.padding = 2
    this.width = this.plane.scale.x + this.padding
    this.widthTotal = this.width * this.length
    this.x = this.width * this.index
    if (this.title) {
      this.title.updateForParentScale(this.plane.scale.x || 1, this.plane.scale.y || 1)
    }
  }
}

interface AppConfig { items?: BendingGalleryItem[]; bend?: number; textColor?: string; borderRadius?: number; font?: string }
class App {
  container: HTMLElement
  scroll: { ease: number; current: number; target: number; last: number; position?: number }
  onCheckDebounce: (...args: any[]) => void
  renderer!: Renderer
  gl!: GL
  camera!: Camera
  scene!: Transform
  planeGeometry!: Plane
  medias: Media[] = []
  mediasImages: BendingGalleryItem[] = []
  screen!: { width: number; height: number }
  viewport!: { width: number; height: number }
  raf: number = 0
  boundOnResize!: () => void
  boundOnWheel!: (e: WheelEvent) => void
  boundOnTouchDown!: (e: MouseEvent | TouchEvent) => void
  boundOnTouchMove!: (e: MouseEvent | TouchEvent) => void
  boundOnTouchUp!: (e?: MouseEvent | TouchEvent) => void
  isDown: boolean = false
  start: number = 0
  constructor(container: HTMLElement, { items, bend = 1, textColor = '#ffffff', borderRadius = 0, font = 'bold 30px DM Sans' }: AppConfig) {
    this.container = container
    this.scroll = { ease: 0.05, current: 0, target: 0, last: 0 }
    this.onCheckDebounce = debounce(this.onCheck.bind(this), 200)
    this.createRenderer(); this.createCamera(); this.createScene(); this.onResize(); this.createGeometry(); this.createMedias(items, bend, textColor, borderRadius, font); this.update(); this.addEventListeners()
  }
  createRenderer() {
    const desiredDpr = Math.min(Math.max(props.dpr || (typeof window !== 'undefined' ? window.devicePixelRatio || 1 : 1), 1), 3)
    this.renderer = new Renderer({ alpha: true, dpr: desiredDpr as any })
    this.gl = this.renderer.gl
    this.gl.clearColor(0, 0, 0, 0)
    this.container.appendChild(this.renderer.gl.canvas as HTMLCanvasElement)
  }
  createCamera() { this.camera = new Camera(this.gl); this.camera.fov = 45; this.camera.position.z = 20 }
  createScene() { this.scene = new Transform() }
  createGeometry() { this.planeGeometry = new Plane(this.gl, { heightSegments: 50, widthSegments: 100 }) }
  createMedias(items: BendingGalleryItem[] | undefined, bend: number = 1, textColor: string, borderRadius: number, font: string) {
    const defaultItems: BendingGalleryItem[] = [
      { image: '/home/gallery/1.png', text: 'Image 1' },
      { image: '/home/gallery/2.png', text: 'Image 2' },
      { image: '/home/gallery/3.png', text: 'Image 3' },
      { image: '/home/gallery/4.png', text: 'Image 4' },
      { image: '/home/gallery/5.png', text: 'Image 5' },
      { image: '/home/gallery/6.png', text: 'Image 6' },
    ]
    const galleryItems = items && items.length ? items : defaultItems
    this.mediasImages = galleryItems.concat(galleryItems)
    this.medias = this.mediasImages.map((data, index) => new Media({ geometry: this.planeGeometry, gl: this.gl, image: data.image, index, length: this.mediasImages.length, renderer: this.renderer, scene: this.scene, screen: this.screen, text: data.text, viewport: this.viewport, bend, textColor, borderRadius, font }))
  }
  onTouchDown(e: MouseEvent | TouchEvent) {
    this.isDown = true
    this.scroll.position = this.scroll.current
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
    this.start = clientX
  }
  onTouchMove(e: MouseEvent | TouchEvent) {
    if (!this.isDown) return
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
    const distance = (this.start - clientX) * 0.05
    this.scroll.target = (this.scroll.position ?? 0) + distance
  }
  onTouchUp() { this.isDown = false; this.onCheck() }
  onWheel(e: WheelEvent) {
    if (props.wheelScroll !== true) return
    // only react when wheel happens over the container
    const target = e.target as HTMLElement
    if (!this.container.contains(target)) return
    this.scroll.target += 2
    this.onCheckDebounce()
  }
  onCheck() { if (!this.medias || !this.medias[0]) return; const width = this.medias[0].width; const itemIndex = Math.round(Math.abs(this.scroll.target) / width); const item = width * itemIndex; this.scroll.target = this.scroll.target < 0 ? -item : item }
  onResize() {
    this.screen = { width: this.container.clientWidth, height: this.container.clientHeight }
    this.renderer.setSize(this.screen.width, this.screen.height)
    this.camera.perspective({ aspect: this.screen.width / this.screen.height })
    const fov = (this.camera.fov * Math.PI) / 180
    const height = 2 * Math.tan(fov / 2) * this.camera.position.z
    const width = height * this.camera.aspect
    this.viewport = { width, height }
    if (this.medias) { this.medias.forEach((media) => media.onResize({ screen: this.screen, viewport: this.viewport })) }
  }
  update() { this.scroll.current = lerp(this.scroll.current, this.scroll.target, this.scroll.ease); const direction = this.scroll.current > this.scroll.last ? 'right' : 'left'; if (this.medias) { this.medias.forEach((media) => media.update(this.scroll, direction)) } this.renderer.render({ scene: this.scene, camera: this.camera }); this.scroll.last = this.scroll.current; this.raf = window.requestAnimationFrame(this.update.bind(this)) }
  addEventListeners() {
    this.boundOnResize = this.onResize.bind(this)
    this.boundOnWheel = this.onWheel.bind(this)
    this.boundOnTouchDown = this.onTouchDown.bind(this)
    this.boundOnTouchMove = this.onTouchMove.bind(this)
    this.boundOnTouchUp = this.onTouchUp.bind(this)
    window.addEventListener('resize', this.boundOnResize)
    // wheel: use window but check target containment to avoid global effect
    window.addEventListener('wheel', this.boundOnWheel, { passive: true } as any)
    // pointer/touch: bind to container only if configured
    const el = this.container
    if (props.dragWithinContainer) {
      el.addEventListener('mousedown', this.boundOnTouchDown)
      el.addEventListener('mousemove', this.boundOnTouchMove)
      el.addEventListener('mouseup', this.boundOnTouchUp)
      el.addEventListener('mouseleave', this.boundOnTouchUp)
      el.addEventListener('touchstart', this.boundOnTouchDown, { passive: true } as any)
      el.addEventListener('touchmove', this.boundOnTouchMove, { passive: true } as any)
      el.addEventListener('touchend', this.boundOnTouchUp)
      el.addEventListener('touchcancel', this.boundOnTouchUp)
    } else {
      window.addEventListener('mousedown', this.boundOnTouchDown)
      window.addEventListener('mousemove', this.boundOnTouchMove)
      window.addEventListener('mouseup', this.boundOnTouchUp)
      window.addEventListener('touchstart', this.boundOnTouchDown)
      window.addEventListener('touchmove', this.boundOnTouchMove)
      window.addEventListener('touchend', this.boundOnTouchUp)
    }
  }
  destroy() {
    window.cancelAnimationFrame(this.raf)
    window.removeEventListener('resize', this.boundOnResize)
    window.removeEventListener('wheel', this.boundOnWheel)
    const el = this.container
    el.removeEventListener('mousedown', this.boundOnTouchDown)
    el.removeEventListener('mousemove', this.boundOnTouchMove)
    el.removeEventListener('mouseup', this.boundOnTouchUp)
    el.removeEventListener('mouseleave', this.boundOnTouchUp)
    el.removeEventListener('touchstart', this.boundOnTouchDown as any)
    el.removeEventListener('touchmove', this.boundOnTouchMove as any)
    el.removeEventListener('touchend', this.boundOnTouchUp)
    el.removeEventListener('touchcancel', this.boundOnTouchUp)
    if (this.renderer && this.renderer.gl && this.renderer.gl.canvas.parentNode) {
      this.renderer.gl.canvas.parentNode.removeChild(this.renderer.gl.canvas as HTMLCanvasElement)
    }
  }
}

onMounted(() => {
  if (!containerRef.value) return
  app = new App(containerRef.value, { items: props.items, bend: props.bend, textColor: props.textColor, borderRadius: props.borderRadius, font: props.font })
})

watch([() => props.bend, () => props.textColor, () => props.borderRadius, () => props.font], () => {
  if (app) { app.destroy(); app = null }
  if (containerRef.value) { app = new App(containerRef.value, { items: props.items, bend: props.bend, textColor: props.textColor, borderRadius: props.borderRadius, font: props.font }) }
})

watch(() => props.items, () => {
  if (app) { app.destroy(); app = null }
  if (containerRef.value) { app = new App(containerRef.value, { items: props.items, bend: props.bend, textColor: props.textColor, borderRadius: props.borderRadius, font: props.font }) }
}, { deep: true })

onBeforeUnmount(() => { if (app) { app.destroy(); app = null } })
</script>

<style scoped>
</style>


