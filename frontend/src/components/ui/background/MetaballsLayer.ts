// Three.js Metaballs rendering layer
// Exposes a uniform API for mount/unmount/update/resize/theme/tier/emphasis

export type ThemeMode = 'light' | 'dark'

export interface MetaballsOptions {
  palette: string[]
  theme: ThemeMode
  tier: 'high' | 'medium' | 'low' | 'low-static'
  emphasis: boolean
}

export interface LayerHandle {
  mount: () => void
  unmount: () => void
  update: (dt: number) => void
  resize: (w: number, h: number) => void
  setTheme: (theme: ThemeMode, palette: string[]) => void
  setTier: (tier: MetaballsOptions['tier']) => void
  setEmphasis: (emphasis: boolean) => void
  setPointer: (xNdc: number, yNdc: number) => void
}

export function create(containerEl: HTMLElement, opts: MetaballsOptions): LayerHandle {
  // Lazy imports to avoid heavy cost on non-3D tiers
  let three: any = null
  let renderer: any = null
  let scene: any = null
  let camera: any = null
  let mesh: any = null
  let raf = 0
  let width = 1
  let height = 1
  let palette = opts.palette.slice()
  let theme: ThemeMode = opts.theme
  let tier = opts.tier
  let emphasis = opts.emphasis
  const pointer = { x: 0, y: 0 }

  const state = {
    ready: false,
    mode: 'none' as 'raymarch' | 'marching' | 'none'
  }

  function chooseMode() {
    if (tier === 'low-static' || tier === 'low') return 'none'
    // Prefer raymarch on medium/high for visible dynamics
    if (tier === 'high' || tier === 'medium') return 'raymarch'
    return 'marching'
  }

  async function init() {
    try {
      three = await import('three')
    } catch {
      return
    }
    const { WebGLRenderer, Scene, PerspectiveCamera, Mesh, PlaneGeometry, ShaderMaterial, Color, DirectionalLight, AmbientLight } = three as any
    renderer = new WebGLRenderer({ antialias: true, alpha: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.setSize(width, height)
    renderer.domElement.style.width = '100%'
    renderer.domElement.style.height = '100%'
    containerEl.appendChild(renderer.domElement)

    scene = new Scene()
    scene.background = null
    camera = new PerspectiveCamera(45, width / height, 0.1, 100)
    camera.position.set(0, 0, 6)

    const mode = chooseMode()
    state.mode = mode

    if (mode === 'raymarch') {
      const geo = new PlaneGeometry(2, 2)
      const mat = new ShaderMaterial({
        transparent: true,
        uniforms: {
          uTime: { value: 0 },
          uResolution: { value: new three.Vector2(width, height) },
          uTheme: { value: theme === 'dark' ? 1 : 0 },
          uPointer: { value: new three.Vector2(0, 0) },
          uPalette: { value: paletteToVec3ArrayLocal(palette) }
        },
        vertexShader: `
          varying vec2 vUv;
          void main() {
            vUv = uv;
            gl_Position = vec4(position, 1.0);
          }
        `,
        fragmentShader: `
          precision highp float;
          varying vec2 vUv;
          uniform float uTime;
          uniform vec2 uResolution;
          uniform int uTheme;
          uniform vec2 uPointer;
          uniform vec3 uPalette[6];

          // Simple SDF metaballs in screen space + fake glow
          float sdSphere(vec3 p, float r) { return length(p) - r; }

          float map(vec3 p) {
            float t = uTime * 0.7;
            // Place a few moving spheres
            vec3 c0 = vec3( sin(t*0.7)*0.7,  cos(t*0.9)*0.5, 0.0 );
            vec3 c1 = vec3( cos(t*0.6)*0.4,  sin(t*1.1)*0.6, 0.0 );
            vec3 c2 = vec3( sin(t*0.8+1.0)*0.5,  cos(t*0.5+1.2)*0.4, 0.0 );
            float d = 1e9;
            d = min(d, sdSphere(p - c0, 0.55));
            d = min(d, sdSphere(p - c1, 0.45));
            d = min(d, sdSphere(p - c2, 0.5));
            // Pointer field attraction
            vec3 pc = vec3(uPointer*1.2, 0.0);
            d = min(d, sdSphere(p - pc, 0.4));
            return d;
          }

          vec3 palette(float x) {
            // Blend across 6 colors
            float i = floor(x*5.0);
            float f = fract(x*5.0);
            int idx = int(i);
            vec3 a = uPalette[idx];
            vec3 b = uPalette[min(idx+1, 5)];
            return mix(a, b, f);
          }

          void main() {
            // Raymarch in a small cube space
            vec2 uv = (vUv*2.0 - 1.0);
            uv.x *= uResolution.x / uResolution.y;
            vec3 ro = vec3(0.0, 0.0, 2.0);
            vec3 rd = normalize(vec3(uv, -1.4));
            float t = 0.0;
            float hit = -1.0;
            for (int i=0; i<64; i++) {
              vec3 p = ro + rd * t;
              float d = map(p);
              if (d < 0.001) { hit = t; break; }
              t += d * 0.7; // conservative steps
              if (t>6.0) break;
            }
            vec3 col = vec3(0.0);
            if (hit > 0.0) {
              float shade = 1.0 - clamp(hit/6.0, 0.0, 1.0);
              col = palette(shade);
              // Theme-aware brightness
              col *= (uTheme==1 ? 1.2 : 1.0);
            }
            // Soft vignette / glow veil
            float r = length(uv);
            float vignette = smoothstep(1.2, 0.2, r);
            col += vignette * 0.06;
            gl_FragColor = vec4(col, 0.9);
          }
        `
      })
      mesh = new Mesh(geo, mat)
      scene.add(mesh)
      // Subtle lighting to match scene characteristics if extended later
      const key = new DirectionalLight(new Color(0x88aaff), 0.6)
      key.position.set(2, 3, 4)
      scene.add(key)
      const amb = new AmbientLight(new Color(0xffffff), 0.2)
      scene.add(amb)
    } else if (mode === 'marching') {
      // Placeholder marching cubes: simple plane with standard material for now
      const { MeshStandardMaterial, PlaneGeometry, Mesh, DirectionalLight, AmbientLight, Color } = three as any
      const geo = new PlaneGeometry(18, 10, 64, 32)
      const mat = new MeshStandardMaterial({ color: new Color(0x2a3b5f), metalness: 0.2, roughness: 0.85 })
      mesh = new Mesh(geo, mat)
      scene.add(mesh)
      const key = new DirectionalLight(new Color(0x88aaff), 0.5)
      key.position.set(2, 3, 4)
      scene.add(key)
      const amb = new AmbientLight(new Color(0xffffff), 0.25)
      scene.add(amb)
    } else {
      // none
      return
    }

    state.ready = true
    raf = requestAnimationFrame(loop)
  }

  function loop() {
    raf = requestAnimationFrame(loop)
    if (!state.ready) return
    const t = performance.now() * 0.001
    if (state.mode === 'raymarch' && mesh && mesh.material && mesh.material.uniforms) {
      mesh.material.uniforms.uTime.value = t
      mesh.material.uniforms.uPointer.value.set(pointer.x, pointer.y)
    }
    renderer.render(scene, camera)
  }

  function hexToLinear(hex: string) {
    const c = parseInt(hex.slice(1), 16)
    return {
      r: ((c >> 16) & 255) / 255,
      g: ((c >> 8) & 255) / 255,
      b: (c & 255) / 255
    }
  }

  function paletteToVec3ArrayLocal(cols: string[]) {
    const out: any[] = []
    const src = cols.length >= 6 ? cols.slice(0, 6) : cols.slice()
    while (src.length < 6) src.push(cols[src.length % cols.length] || '#ffffff')
    for (let i = 0; i < 6; i++) {
      const { r, g, b } = hexToLinear(src[i])
      out.push(new three.Vector3(r, g, b))
    }
    return out
  }

  return {
    mount() {
      const rect = containerEl.getBoundingClientRect()
      width = Math.max(1, Math.floor(rect.width))
      height = Math.max(1, Math.floor(rect.height))
      init()
    },
    unmount() {
      cancelAnimationFrame(raf)
      try {
        if (renderer) renderer.dispose?.()
        if (scene) scene.traverse?.((obj: any) => {
          if (obj.geometry) obj.geometry.dispose?.()
          if (obj.material) {
            if (Array.isArray(obj.material)) obj.material.forEach((m: any) => m.dispose?.())
            else obj.material.dispose?.()
          }
        })
        if (containerEl && renderer?.domElement) containerEl.removeChild(renderer.domElement)
      } catch {}
      renderer = null; scene = null; camera = null; mesh = null; three = null
      state.ready = false
    },
    update(_dt: number) {
      // rendering loop is RAF-driven inside
    },
    resize(w: number, h: number) {
      width = w; height = h
      if (renderer) renderer.setSize(w, h)
      if (camera) { camera.aspect = w / h; camera.updateProjectionMatrix() }
      if (state.mode === 'raymarch' && mesh && mesh.material && mesh.material.uniforms) {
        mesh.material.uniforms.uResolution.value.set(w, h)
      }
    },
    setTheme(nextTheme: ThemeMode, nextPalette: string[]) {
      theme = nextTheme
      palette = nextPalette.slice()
      if (state.mode === 'raymarch' && mesh && mesh.material && mesh.material.uniforms) {
        mesh.material.uniforms.uTheme.value = theme === 'dark' ? 1 : 0
        mesh.material.uniforms.uPalette.value = paletteToVec3ArrayLocal(palette)
      }
    },
    setTier(next: MetaballsOptions['tier']) {
      if (tier === next) return
      tier = next
      // Recreate pipeline for mode change
      this.unmount()
      this.mount()
    },
    setEmphasis(on: boolean) {
      emphasis = on
      // Could adjust internal intensities in shaders/materials when needed
    },
    setPointer(xNdc: number, yNdc: number) {
      pointer.x = xNdc
      pointer.y = yNdc
    }
  }
}

// Removed legacy paletteToVec3Array to avoid optional chaining in new-expression


