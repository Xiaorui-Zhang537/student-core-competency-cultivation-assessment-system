// Particle Field Layer: CPU-updated Points following a vector field with mouse/click forces

export type ThemeMode = 'light' | 'dark'

export interface ParticleFieldOptions {
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
  setTier: (tier: ParticleFieldOptions['tier']) => void
  setEmphasis: (emphasis: boolean) => void
  setPointer: (xNdc: number, yNdc: number) => void
  addClickImpulse: (xNdc: number, yNdc: number) => void
  isReady: () => boolean
  fallback2D: () => void
}

export function create(containerEl: HTMLElement, opts: ParticleFieldOptions): LayerHandle {
  let three: any = null
  let renderer: any = null
  let scene: any = null
  let camera: any = null
  let points: any = null
  let geometry: any = null
  let material: any = null
  let raf = 0
  let width = 1
  let height = 1
  let theme: ThemeMode = opts.theme
  let palette = opts.palette.slice()
  let tier = opts.tier
  let emphasis = opts.emphasis

  // Particle buffers (pixel-space coordinates)
  let positions: Float32Array
  let velocities: Float32Array
  let colors: Float32Array
  let charges: Float32Array
  let particleCount = 0

  // Pointer in pixels
  const pointer = { x: 0, y: 0 }
  // Magnetic pulses (Bz) at click points
  const impulses: Array<{ x: number; y: number; power: number; t: number; sign: number }> = []
  let lastTime = 0
  let pulseToggle = 1
  let ready = false
  // 2D fallback
  let c2dCanvas: HTMLCanvasElement | null = null
  let c2dCtx: CanvasRenderingContext2D | null = null
  let c2dRaf = 0
  let c2dParticles: Array<{ x: number; y: number; vx: number; vy: number; color: string }> = []

  function chooseCount() {
    // Always render some particles to avoid blank background
    if (tier === 'low-static') return 800
    if (tier === 'low') return 800
    if (tier === 'medium') return 1600
    return 2600
  }

  async function init() {
    try { three = await import('three') } catch { return }
    const { WebGLRenderer, Scene, OrthographicCamera, BufferGeometry, Float32BufferAttribute, Points, PointsMaterial, Color, AdditiveBlending } = three as any
    renderer = new WebGLRenderer({ antialias: true, alpha: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.setSize(width, height)
    renderer.setClearColor(0x000000, 0)
    renderer.domElement.style.width = '100%'
    renderer.domElement.style.height = '100%'
    renderer.domElement.style.pointerEvents = 'none'
    ;(renderer.domElement.style as any).zIndex = '999'
    document.body.appendChild(renderer.domElement)

    scene = new Scene()
    scene.background = null
    camera = new OrthographicCamera(-width / 2, width / 2, height / 2, -height / 2, -10, 10)
    camera.position.set(0, 0, 5)

    particleCount = chooseCount()
    if (particleCount <= 0) return

    // Allocate buffers
    positions = new Float32Array(particleCount * 3)
    velocities = new Float32Array(particleCount * 2)
    colors = new Float32Array(particleCount * 3)
    charges = new Float32Array(particleCount)

    const brighten = (hex: string) => new Color(hex).lerp(new Color(0xffffff), theme === 'dark' ? 0.28 : 0.2)
    const c1 = brighten(palette[0] || '#4F8BFE')
    const c2 = brighten(palette[1] || '#60A5FA')
    const c3 = brighten(palette[2] || '#22D3EE')
    const c4 = brighten(palette[3] || '#2AFEB7')
    const c5 = brighten(palette[4] || '#A6FF6E')
    const colorsPool = [c1, c2, c3, c4, c5]
    for (let i = 0; i < particleCount; i++) {
      // spawn from screen edges with inward initial velocity (px/s)
      const edge = Math.floor(Math.random() * 4)
      let x = 0, y = 0
      const margin = 8
      if (edge === 0) { x = -width / 2 - margin; y = (Math.random() - 0.5) * height }
      else if (edge === 1) { x = width / 2 + margin; y = (Math.random() - 0.5) * height }
      else if (edge === 2) { x = (Math.random() - 0.5) * width; y = height / 2 + margin }
      else { x = (Math.random() - 0.5) * width; y = -height / 2 - margin }
      positions[i * 3 + 0] = x
      positions[i * 3 + 1] = y
      positions[i * 3 + 2] = 0

      const dirX = -x, dirY = -y
      const len = Math.hypot(dirX, dirY) || 1
      const nx = dirX / len, ny = dirY / len
      const speed = 40 + Math.random() * 60
      const jitter = (Math.random() - 0.5) * 0.8
      const cj = Math.cos(jitter), sj = Math.sin(jitter)
      const jx = nx * cj - ny * sj
      const jy = nx * sj + ny * cj
      velocities[i * 2 + 0] = jx * speed
      velocities[i * 2 + 1] = jy * speed

      const q = Math.random() < 0.5 ? 1 : -1
      charges[i] = q
      const col = colorsPool[i % colorsPool.length]
      colors[i * 3 + 0] = col.r
      colors[i * 3 + 1] = col.g
      colors[i * 3 + 2] = col.b
    }

    geometry = new BufferGeometry()
    geometry.setAttribute('position', new Float32BufferAttribute(positions, 3))
    geometry.setAttribute('color', new Float32BufferAttribute(colors, 3))

    material = new PointsMaterial({ size: 3.2, sizeAttenuation: true, vertexColors: true, transparent: true, opacity: theme === 'dark' ? 0.95 : 0.9, blending: AdditiveBlending, depthWrite: false })

    points = new Points(geometry, material)
    scene.add(points)

    raf = requestAnimationFrame(loop)
    ready = true
  }

  function startFallback2D() {
    // Cleanup WebGL if any
    try {
      cancelAnimationFrame(raf)
      if (renderer) renderer.dispose?.()
      if (renderer?.domElement && renderer.domElement.parentElement) renderer.domElement.parentElement.removeChild(renderer.domElement)
    } catch {}
    renderer = null; scene = null; camera = null; points = null; geometry = null; material = null

    // Create 2D canvas (use window size to avoid zero rect issues)
    c2dCanvas = document.createElement('canvas')
    c2dCanvas.id = 'particle-fallback'
    const DPR = Math.min(window.devicePixelRatio || 1, 2)
    width = window.innerWidth
    height = window.innerHeight
    c2dCanvas.width = Math.max(1, Math.floor(width * DPR))
    c2dCanvas.height = Math.max(1, Math.floor(height * DPR))
    c2dCanvas.style.position = 'fixed'
    c2dCanvas.style.left = '0'
    c2dCanvas.style.top = '0'
    c2dCanvas.style.width = '100%'
    c2dCanvas.style.height = '100%'
    c2dCanvas.style.zIndex = '2147483647'
    c2dCanvas.style.pointerEvents = 'none'
    c2dCanvas.style.opacity = '1'
    c2dCanvas.style.mixBlendMode = 'normal'
    document.body.appendChild(c2dCanvas)
    c2dCtx = c2dCanvas.getContext('2d')
    if (c2dCtx) c2dCtx.setTransform(DPR, 0, 0, DPR, 0, 0)
    console.warn('[Particle2D] fallback started', { width, height, DPR })
    // Sanity paint to ensure canvas visible
    if (c2dCtx) {
      c2dCtx.fillStyle = 'rgba(255,255,255,0.06)'
      c2dCtx.fillRect(0, 0, width, height)
      c2dCtx.fillStyle = 'rgba(0,255,255,0.2)'
      c2dCtx.fillRect(20, 20, 120, 8)
    }
    // Init particles (edge spawn, inward velocity)
    const cols = ['#7cc1ff', '#5de2ff', '#3cf6c8', '#a6ff6e', '#fff36b']
    const n = 1600
    c2dParticles = []
    for (let i = 0; i < n; i++) {
      const edge = Math.floor(Math.random() * 4)
      let x = 0, y = 0
      const margin = 8
      if (edge === 0) { x = -margin; y = Math.random() * height }
      else if (edge === 1) { x = width + margin; y = Math.random() * height }
      else if (edge === 2) { x = Math.random() * width; y = -margin }
      else { x = Math.random() * width; y = height + margin }
      const dirX = (width / 2 - x)
      const dirY = (height / 2 - y)
      const len = Math.hypot(dirX, dirY) || 1
      const nx = dirX / len, ny = dirY / len
      const sp = 40 + Math.random() * 60
      const jitter = (Math.random() - 0.5) * 0.8
      const cj = Math.cos(jitter), sj = Math.sin(jitter)
      const jx = nx * cj - ny * sj
      const jy = nx * sj + ny * cj
      const q = Math.random() < 0.5 ? 1 : -1
      c2dParticles.push({ x, y, vx: jx * sp, vy: jy * sp, q, color: cols[i % cols.length] })
    }
    const last = { t: performance.now() * 0.001 }
    const loop2d = () => {
      c2dRaf = requestAnimationFrame(loop2d)
      if (!c2dCtx || !c2dCanvas) return
      const now = performance.now() * 0.001
      const dt = Math.min(0.033, now - last.t)
      last.t = now
      c2dCtx.clearRect(0, 0, width, height)
      // DEBUG pattern to ensure visibility
      c2dCtx.fillStyle = '#0ff'
      c2dCtx.fillRect(12, 12, 180, 10)
      c2dCtx.fillStyle = '#fff'
      c2dCtx.fillRect(12, 28, 80, 6)
      c2dCtx.globalCompositeOperation = 'lighter'
      // Global electric field
      const Ex = (theme === 'dark' ? 18 : 14) * (emphasis ? 1.1 : 1)
      const Ey = (theme === 'dark' ? 8 : 6) * (emphasis ? 1.1 : 1)
      // Decay pulses
      for (let k = impulses.length - 1; k >= 0; k--) { impulses[k].t -= dt; if (impulses[k].t <= 0) impulses.splice(k, 1) }
      for (const p of c2dParticles) {
        // Electric
        p.vx += p.q * Ex * dt
        p.vy += p.q * Ey * dt
        // Magnetic pulses (v x B)
        let Bz = 0
        for (let j = 0; j < impulses.length; j++) {
          const imp = impulses[j]
          const dx = p.x - imp.x
          const dy = p.y - imp.y
          const r2 = dx*dx + dy*dy
          const g = Math.exp(-r2 / 2200) * (imp.t) * imp.sign
          Bz += g * imp.power
        }
        const axB = p.q * (p.vy * Bz)
        const ayB = p.q * (-p.vx * Bz)
        p.vx += axB * dt
        p.vy += ayB * dt
        // safety clamp + damping
        if (!Number.isFinite(p.vx) || Math.abs(p.vx) > 2000) p.vx = (Math.random() - 0.5) * 200
        if (!Number.isFinite(p.vy) || Math.abs(p.vy) > 2000) p.vy = (Math.random() - 0.5) * 200
        p.vx *= 0.992; p.vy *= 0.992
        p.x += p.vx * dt
        p.y += p.vy * dt
        // wrap
        const margin = 12
        if (p.x < -margin) p.x = width + margin
        if (p.x > width + margin) p.x = -margin
        if (p.y < -margin) p.y = height + margin
        if (p.y > height + margin) p.y = -margin
        // draw
        c2dCtx.fillStyle = p.color
        c2dCtx.beginPath(); c2dCtx.arc(p.x, p.y, 3.0, 0, Math.PI * 2); c2dCtx.fill()
      }
    }
    loop2d()
  }

  function loop() {
    raf = requestAnimationFrame(loop)
    const now = performance.now() * 0.001
    const dt = Math.min(0.033, lastTime ? now - lastTime : 1 / 60)
    lastTime = now
    step(now, dt)
    if (renderer) renderer.render(scene, camera)
  }

  function step(time: number, dt: number) {
    if (!positions || !velocities) return
    // Global uniform electric field E (stable)
    const Ex = (theme === 'dark' ? 24 : 18) * (emphasis ? 1.2 : 1)
    const Ey = (theme === 'dark' ? 10 : 8) * (emphasis ? 1.2 : 1)
    const damping = 0.988
    const bounds = { x: width / 2, y: height / 2 }

    // Decay magnetic pulses
    for (let k = impulses.length - 1; k >= 0; k--) {
      impulses[k].t -= dt
      if (impulses[k].t <= 0) impulses.splice(k, 1)
    }

    for (let i = 0; i < particleCount; i++) {
      const idx3 = i * 3
      const idx2 = i * 2
      let x = positions[idx3]
      let y = positions[idx3 + 1]
      let vx = velocities[idx2]
      let vy = velocities[idx2 + 1]
      const q = charges[i] || 1

      // Electric field acceleration a = q * E
      vx += q * Ex * dt
      vy += q * Ey * dt

      // Magnetic pulses: sum Bz and apply Lorentz curvature a = q (v x B)
      let Bz = 0
      for (let j = 0; j < impulses.length; j++) {
        const imp = impulses[j]
        const dx = x - imp.x
        const dy = y - imp.y
        const r2 = dx*dx + dy*dy
        const sigma2 = 2200
        const g = Math.exp(-r2 / sigma2) * (imp.t) * imp.sign
        Bz += g * imp.power
      }
      const axB = q * (vy * Bz)
      const ayB = q * (-vx * Bz)
      vx += axB * dt
      vy += ayB * dt

      // Slight randomization to avoid locking
      vx += (Math.random() - 0.5) * 2.0
      vy += (Math.random() - 0.5) * 2.0

      vx *= damping
      vy *= damping
      x += vx * dt
      y += vy * dt

      // Respawn when far out
      const margin = 24
      if (x > bounds.x + margin || x < -bounds.x - margin || y > bounds.y + margin || y < -bounds.y - margin) {
        const edge = Math.floor(Math.random() * 4)
        if (edge === 0) { x = -bounds.x - margin; y = (Math.random() - 0.5) * height }
        else if (edge === 1) { x = bounds.x + margin; y = (Math.random() - 0.5) * height }
        else if (edge === 2) { x = (Math.random() - 0.5) * width; y = bounds.y + margin }
        else { x = (Math.random() - 0.5) * width; y = -bounds.y - margin }
        const dirX = -x, dirY = -y
        const len = Math.hypot(dirX, dirY) || 1
        const nx = dirX / len, ny = dirY / len
        const speed = 40 + Math.random() * 60
        const jitter = (Math.random() - 0.5) * 0.8
        const cj = Math.cos(jitter), sj = Math.sin(jitter)
        const jx = nx * cj - ny * sj
        const jy = nx * sj + ny * cj
        vx = jx * speed
        vy = jy * speed
        charges[i] = Math.random() < 0.5 ? 1 : -1
      }

      positions[idx3] = x
      positions[idx3 + 1] = y
      velocities[idx2] = vx
      velocities[idx2 + 1] = vy
    }

    geometry.attributes.position.needsUpdate = true
  }

  function ndcToPixels(xNdc: number, yNdc: number) {
    const x = (xNdc * 0.5 + 0.5) * width - width / 2
    const y = (yNdc * 0.5 + 0.5) * height - height / 2
    return { x, y }
  }

  return {
    mount() {
      const rect = containerEl.getBoundingClientRect()
      width = Math.max(1, Math.floor(rect.width || window.innerWidth))
      height = Math.max(1, Math.floor(rect.height || window.innerHeight))
      init()
    },
    unmount() {
      cancelAnimationFrame(raf)
      cancelAnimationFrame(c2dRaf)
      try {
        if (renderer) renderer.dispose?.()
        if (scene) scene.traverse?.((obj: any) => {
          if (obj.geometry) obj.geometry.dispose?.()
          if (obj.material) {
            if (Array.isArray(obj.material)) obj.material.forEach((m: any) => m.dispose?.())
            else obj.material.dispose?.()
          }
        })
        if (renderer?.domElement && renderer.domElement.parentElement) renderer.domElement.parentElement.removeChild(renderer.domElement)
        if (c2dCanvas && c2dCanvas.parentElement) c2dCanvas.parentElement.removeChild(c2dCanvas)
      } catch {}
      renderer = null; scene = null; camera = null; points = null; geometry = null; material = null; three = null
      c2dCanvas = null; c2dCtx = null; c2dParticles = []
    },
    update(_dt: number) {},
    resize(w: number, h: number) {
      width = w; height = h
      if (renderer) renderer.setSize(w, h)
      if (camera) {
        camera.left = -w / 2
        camera.right = w / 2
        camera.top = h / 2
        camera.bottom = -h / 2
        camera.updateProjectionMatrix()
      }
    },
    setTheme(nextTheme: ThemeMode, nextPalette: string[]) {
      theme = nextTheme
      palette = nextPalette.slice()
      if (material) {
        material.opacity = theme === 'dark' ? 0.9 : 0.8
      }
    },
    setTier(next: ParticleFieldOptions['tier']) {
      if (tier === next) return
      tier = next
      this.unmount()
      this.mount()
    },
    setEmphasis(on: boolean) {
      emphasis = on
    },
    setPointer(xNdc: number, yNdc: number) {
      const p = ndcToPixels(xNdc, yNdc)
      pointer.x = p.x
      pointer.y = p.y
    },
    addClickImpulse(xNdc: number, yNdc: number) {
      const p = ndcToPixels(xNdc, yNdc)
      const sign = pulseToggle
      pulseToggle = -pulseToggle
      impulses.push({ x: p.x, y: p.y, power: theme === 'dark' ? 22 : 18, t: 1.0, sign })
    },
    isReady() { return ready },
    fallback2D() { startFallback2D() }
  }
}


