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

let selectorParser = null
try {
  const mod = require('postcss-selector-parser')
  selectorParser = typeof mod === 'function' ? mod : (mod?.default || null)
} catch {}

const stripUnusedDaisyImportantSelectors = selectorParser
  ? {
      postcssPlugin: 'strip-unused-daisy-important-selectors',
      Rule(rule) {
        const selector = String(rule.selector || '')
        if (!selector || (!selector.includes('.\\!btn') && !selector.includes('.\\!input'))) return
        try {
          const kept = []
          selectorParser((selectors) => {
            selectors.each((node) => {
              const text = String(node).trim()
              if (!text.includes('.\\!btn') && !text.includes('.\\!input')) {
                kept.push(text)
              }
            })
          }).processSync(selector)
          if (kept.length === 0) rule.remove()
          else rule.selector = kept.join(', ')
        } catch {
          rule.remove()
        }
      }
    }
  : null

export default {
  plugins: [
    ...(nesting ? [nesting()] : []),
    tailwindcss(),
    ...(stripUnusedDaisyImportantSelectors ? [stripUnusedDaisyImportantSelectors] : []),
    autoprefixer(),
  ]
}
