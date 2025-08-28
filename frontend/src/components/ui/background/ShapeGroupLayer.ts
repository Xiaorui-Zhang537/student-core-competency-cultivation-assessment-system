// Shape Group Layer: multiple small 3D primitives with mixed materials.
// Cursor steers a subtle group tilt; click triggers burst impulses.

export type ThemeMode = 'light' | 'dark'

export interface ShapeGroupOptions {
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
  setTier: (tier: ShapeGroupOptions['tier']) => void
  setEmphasis: (emphasis: boolean) => void
  setPointer: (xNdc: number, yNdc: number) => void
  burst: (xNdc: number, yNdc: number) => void
}

export function create(containerEl: HTMLElement, opts: ShapeGroupOptions): LayerHandle {
  let three: any = null
  let renderer: any = null
  let scene: any = null
  let camera: any = null
  let group: any = null
  let composer: any = null
  let RenderPass: any = null
  let EffectComposer: any = null
  let UnrealBloomPass: any = null
  let BokehPass: any = null
  let raf = 0
  let width = 1
  let height = 1
  let theme: ThemeMode = opts.theme
  let palette = opts.palette.slice()
  let tier = opts.tier
  let emphasis = opts.emphasis

  const pointer = { x: 0, y: 0 }
  let targetRot = { x: 0, y: 0 }

  const shapes: any[] = []
  const spins: Array<{ rx: number; ry: number; rz: number; sx: number; life: number }> = []

  function colorAt(i: number) {
    const col = palette[i % palette.length]
    return new three.Color(col)
  }

  async function init() {
    try { three = await import('three') } catch { return }
    const { WebGLRenderer, Scene, PerspectiveCamera, Group, DirectionalLight, AmbientLight, Color, ConeGeometry, CylinderGeometry, MeshStandardMaterial, MeshPhysicalMaterial, Mesh } = three as any
    try {
      const modEC = await import('three/examples/jsm/postprocessing/EffectComposer.js')
      const modRP = await import('three/examples/jsm/postprocessing/RenderPass.js')
      const modBloom = await import('three/examples/jsm/postprocessing/UnrealBloomPass.js')
      const modBokeh = await import('three/examples/jsm/postprocessing/BokehPass.js')
      EffectComposer = modEC.EffectComposer
      RenderPass = modRP.RenderPass
      UnrealBloomPass = modBloom.UnrealBloomPass
      BokehPass = modBokeh.BokehPass
    } catch {}

    renderer = new WebGLRenderer({ antialias: true, alpha: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.setSize(width, height)
    renderer.setClearColor(0x000000, 0)
    renderer.domElement.style.width = '100%'
    renderer.domElement.style.height = '100%'
    renderer.domElement.style.pointerEvents = 'none'
    containerEl.appendChild(renderer.domElement)

    scene = new Scene()
    scene.background = null
    camera = new PerspectiveCamera(45, width / height, 0.1, 100)
    camera.position.set(0, 0, 10)

    const key = new DirectionalLight(new Color(0xffffff), theme === 'dark' ? 1.0 : 0.9)
    key.position.set(3, 4, 6)
    scene.add(key)
    const rim = new DirectionalLight(new Color(0x88aaff), theme === 'dark' ? 0.6 : 0.4)
    rim.position.set(-3, -2, -4)
    scene.add(rim)
    const amb = new AmbientLight(new Color(0xffffff), theme === 'dark' ? 0.25 : 0.18)
    scene.add(amb)

    group = new Group()
    scene.add(group)

    // Materials: a mix of metal and glass-like
    const metal = (i: number) => new MeshStandardMaterial({ color: colorAt(i), metalness: 0.85, roughness: 0.25 })
    const glass = (i: number) => new MeshPhysicalMaterial({ color: colorAt(i).multiplyScalar(theme === 'dark' ? 0.85 : 0.78), metalness: 0, roughness: 0.08, transmission: 0.48, transparent: true, opacity: 0.92, ior: 1.12, thickness: 0.35 })

    // Geometries: 四角用四种基础几何
    const geoPyramid = new ConeGeometry(0.35, 0.6, 4) // 四棱锥
    const geoCylinder = new CylinderGeometry(0.28, 0.28, 0.7, 24)
    const geoCone = new ConeGeometry(0.3, 0.7, 24)
    const geoTriPrism = new CylinderGeometry(0.33, 0.33, 0.66, 3) // 近似三棱柱

    const geos = [geoPyramid, geoCylinder, geoCone, geoTriPrism]
    const mats = [metal(0), glass(1), metal(2), glass(3)]

    // 固定四角
    const positions = [
      { x: -width*0.35, y:  height*0.28, z: -1.2 }, // 左上
      { x:  width*0.35, y:  height*0.28, z: -1.2 }, // 右上
      { x: -width*0.35, y: -height*0.28, z: -1.2 }, // 左下
      { x:  width*0.35, y: -height*0.28, z: -1.2 }  // 右下
    ]
    const scale = Math.min(width, height) * 0.0009
    for (let i = 0; i < 4; i++) {
      const mesh = new Mesh(geos[i], mats[i])
      mesh.position.set(positions[i].x, positions[i].y, positions[i].z)
      mesh.scale.setScalar(scale)
      group.add(mesh)
      shapes.push(mesh)
      spins.push({ rx: 0, ry: 0, rz: 0, sx: 0, life: 0 })
    }

    // Postprocessing (high: bloom + bokeh, medium: bloom only)
    if (EffectComposer && RenderPass) {
      composer = new EffectComposer(renderer)
      const rp = new RenderPass(scene, camera)
      ;(rp as any).clearAlpha = 0
      composer.addPass(rp)
      const enableBloom = tier === 'high' || tier === 'medium'
      const enableBokeh = tier === 'high'
      if (enableBloom && UnrealBloomPass) {
        const size = new three.Vector2(width, height)
        const bloom = new UnrealBloomPass(size, theme === 'dark' ? 0.55 : 0.38, 0.8, 0.85)
        composer.addPass(bloom)
      }
      if (enableBokeh && BokehPass) {
        const bokeh = new BokehPass(scene, camera, {
          focus: 9.5,
          aperture: 0.00012,
          maxblur: 0.007
        })
        composer.addPass(bokeh)
      }
    }

    raf = requestAnimationFrame(loop)
  }

  function loop() {
    raf = requestAnimationFrame(loop)
    step(1 / 60)
    if (composer) composer.render()
    else if (renderer) renderer.render(scene, camera)
  }

  function step(dt: number) {
    // Group不再整体旋转，仅各角朝向
    for (let i = 0; i < shapes.length; i++) {
      const mesh = shapes[i]
      // 朝向指针的方向（看向屏幕内的某点）
      const look = new three.Vector3(pointer.x * 5, pointer.y * 3, 5)
      mesh.lookAt(look)
    }
  }

  function ndcToWorldDir(xNdc: number, yNdc: number) {
    const v = new three.Vector3(xNdc, yNdc, 0.5).unproject(camera).sub(camera.position).normalize()
    return v
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
      renderer = null; scene = null; camera = null; group = null; three = null
    },
    update(_dt: number) {},
    resize(w: number, h: number) {
      width = w; height = h
      if (renderer) renderer.setSize(w, h)
      if (camera) { camera.aspect = w / h; camera.updateProjectionMatrix() }
      if (composer) composer.setSize(w, h)
    },
    setTheme(nextTheme: ThemeMode, nextPalette: string[]) {
      theme = nextTheme
      palette = nextPalette.slice()
    },
    setTier(next: ShapeGroupOptions['tier']) {
      if (tier === next) return
      tier = next
      this.unmount(); this.mount()
    },
    setEmphasis(on: boolean) { emphasis = on },
    setPointer(xNdc: number, yNdc: number) { pointer.x = xNdc; pointer.y = yNdc },
    burst(_x: number, _y: number) { /* no-op: 角标不爆裂 */ }
  }
}


