import tailwindcss from 'tailwindcss'
import autoprefixer from 'autoprefixer'
import { createRequire } from 'module'

const require = createRequire(import.meta.url)

let nesting = null
try {
  // 可选加载，未安装时自动跳过
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  const mod = require('postcss-nesting')
  nesting = typeof mod === 'function' ? mod : (mod?.default || null)
} catch {}

export default {
  plugins: [
    ...(nesting ? [nesting()] : []),
    tailwindcss(),
    autoprefixer(),
  ]
}