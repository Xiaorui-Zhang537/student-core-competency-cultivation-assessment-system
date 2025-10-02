<template>
  <canvas
    ref="canvasRef"
    :class="cn('absolute inset-0 w-full h-full pointer-events-none opacity-95', props.class)"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, type HTMLAttributes } from 'vue'
import { Renderer, Camera, Transform, Program, Mesh, Plane } from 'ogl'
import { cn } from '@/lib/utils'

interface Props {
  hue?: number
  saturation?: number
  chroma?: number
  class?: HTMLAttributes['class']
}

const props = withDefaults(defineProps<Props>(), {
  hue: 200,
  saturation: 0.8,
  chroma: 0.6,
})

const canvasRef = ref<HTMLCanvasElement | null>(null)
const animationRef = ref<number | null>(null)
const rendererRef = ref<Renderer | null>(null)
const sceneRef = ref<Transform | null>(null)
const meshRef = ref<Mesh | null>(null)
const cameraRef = ref<Camera | null>(null)

const pointerRef = ref({ x: 0, y: 0, tX: 0, tY: 0 })

const vertexShader = `
  precision mediump float;
  attribute vec2 position;
  attribute vec2 uv;
  varying vec2 vUv;
  void main() {
      vUv = uv;
      gl_Position = vec4(position, 0.0, 1.0);
  }
`

const fragmentShader = `
  precision mediump float;
  varying vec2 vUv;
  uniform float u_time;
  uniform float u_ratio;
  uniform vec2 u_pointer_position;
  uniform float u_scroll_progress;
  uniform float u_hue;
  uniform float u_saturation;
  uniform float u_chroma;

  vec2 rotate(vec2 uv, float th) {
      return mat2(cos(th), sin(th), -sin(th), cos(th)) * uv;
  }

  float neuro_shape(vec2 uv, float t, float p) {
      vec2 sine_acc = vec2(0.);
      vec2 res = vec2(0.);
      float scale = 8.;
      for (int j = 0; j < 15; j++) {
          uv = rotate(uv, 1.);
          sine_acc = rotate(sine_acc, 1.);
          vec2 layer = uv * scale + float(j) + sine_acc - t;
          sine_acc += sin(layer) + 2.4 * p;
          res += (.5 + .5 * cos(layer)) / scale;
          scale *= (1.2);
      }
      return res.x + res.y;
  }

  vec3 hsl2rgb(vec3 c) {
      vec3 rgb = clamp(abs(mod(c.x*6.0+vec3(0.0,4.0,2.0),6.0)-3.0)-1.0, 0.0, 1.0);
      return c.z + c.y * (rgb - 0.5) * (1.0 - abs(2.0 * c.z - 1.0));
  }

  void main() {
      vec2 uv = .5 * vUv;
      uv.x *= u_ratio;
      vec2 pointer = vUv - u_pointer_position;
      pointer.x *= u_ratio;
      float p = clamp(length(pointer), 0., 1.);
      p = .5 * pow(1. - p, 2.);
      float t = .001 * u_time;
      vec3 color = vec3(0.);
      float noise = neuro_shape(uv, t, p);
      noise = 1.2 * pow(noise, 3.);
      noise += pow(noise, 10.);
      noise = max(.0, noise - .5);
      noise *= (1. - length(vUv - .5));
      float normalizedHue = u_hue / 360.0;
      vec3 hsl = vec3(
          normalizedHue + 0.1 * sin(3.0 * u_scroll_progress + 1.5),
          u_saturation,
          u_chroma * 0.5 + 0.2 * sin(2.0 * u_scroll_progress)
      );
      color = hsl2rgb(hsl);
      color = color * noise;
      gl_FragColor = vec4(color, noise);
  }
`

function initOGL(): boolean {
  const canvas = canvasRef.value
  if (!canvas) return false
  try {
    const renderer = new Renderer({
      canvas,
      width: canvas.clientWidth,
      height: canvas.clientHeight,
      dpr: Math.min(window.devicePixelRatio || 1, 2),
    })
    const camera = new Camera(renderer.gl)
    const scene = new Transform()
    const geometry = new Plane(renderer.gl, { width: 2, height: 2 })
    const program = new Program(renderer.gl, {
      vertex: vertexShader,
      fragment: fragmentShader,
      uniforms: {
        u_time: { value: 0 },
        u_ratio: { value: window.innerWidth / window.innerHeight },
        u_pointer_position: { value: [0, 0] },
        u_scroll_progress: { value: 0 },
        u_hue: { value: props.hue },
        u_saturation: { value: props.saturation },
        u_chroma: { value: props.chroma },
      },
    })
    const mesh = new Mesh(renderer.gl, { geometry, program })
    mesh.setParent(scene)
    rendererRef.value = renderer
    cameraRef.value = camera
    sceneRef.value = scene
    meshRef.value = mesh
    return true
  } catch (e) {
    console.error('[NeuralBackground] init error:', e)
    return false
  }
}

function resizeCanvas() {
  const renderer = rendererRef.value
  const mesh = meshRef.value
  const canvas = canvasRef.value
  if (!renderer || !mesh || !canvas) return
  const width = canvas.clientWidth
  const height = canvas.clientHeight
  renderer.setSize(width, height)
  if ((mesh as any).program?.uniforms?.u_ratio) {
    ;(mesh as any).program.uniforms.u_ratio.value = width / height
  }
}

function render() {
  const renderer = rendererRef.value
  const scene = sceneRef.value
  const camera = cameraRef.value
  const mesh = meshRef.value
  const pointer = pointerRef.value
  if (!renderer || !scene || !camera || !mesh) return
  const currentTime = performance.now()
  pointer.x += (pointer.tX - pointer.x) * 0.2
  pointer.y += (pointer.tY - pointer.y) * 0.2
  const uniforms = (mesh as any).program?.uniforms
  if (uniforms) {
    if (uniforms.u_time) uniforms.u_time.value = currentTime
    if (uniforms.u_pointer_position) {
      uniforms.u_pointer_position.value = [
        pointer.x / (window.innerWidth || 1),
        1 - pointer.y / (window.innerHeight || 1),
      ]
    }
    if (uniforms.u_scroll_progress) {
      uniforms.u_scroll_progress.value = window.pageYOffset / (2 * (window.innerHeight || 1))
    }
  }
  renderer.render({ scene, camera })
  animationRef.value = requestAnimationFrame(render)
}

function updateMousePosition(x: number, y: number) {
  pointerRef.value.tX = x
  pointerRef.value.tY = y
}

function handlePointerMove(e: PointerEvent) { updateMousePosition(e.clientX, e.clientY) }
function handleTouchMove(e: TouchEvent) { updateMousePosition(e.touches[0].clientX, e.touches[0].clientY) }
function handleClick(e: MouseEvent) { updateMousePosition(e.clientX, e.clientY) }

watch(() => props.hue, (v) => { const m = meshRef.value as any; if (m?.program?.uniforms?.u_hue) m.program.uniforms.u_hue.value = v })
watch(() => props.saturation, (v) => { const m = meshRef.value as any; if (m?.program?.uniforms?.u_saturation) m.program.uniforms.u_saturation.value = v })
watch(() => props.chroma, (v) => { const m = meshRef.value as any; if (m?.program?.uniforms?.u_chroma) m.program.uniforms.u_chroma.value = v })

onMounted(() => {
  if (initOGL()) {
    resizeCanvas()
    render()
    window.addEventListener('resize', resizeCanvas)
    window.addEventListener('pointermove', handlePointerMove)
    window.addEventListener('touchmove', handleTouchMove)
    window.addEventListener('click', handleClick)
  }
})

onUnmounted(() => {
  if (animationRef.value) cancelAnimationFrame(animationRef.value)
  window.removeEventListener('resize', resizeCanvas)
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('touchmove', handleTouchMove)
  window.removeEventListener('click', handleClick)
  rendererRef.value = null
  sceneRef.value = null
  meshRef.value = null
  cameraRef.value = null
})
</script>

<style scoped>
</style>


