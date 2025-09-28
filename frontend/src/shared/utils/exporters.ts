import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

export const EXPORT_FALLBACK_STYLE = `
  :root, html[data-theme], body {
    --color-base-100: #ffffff; --color-base-200: #f3f4f6; --color-base-300: #e5e7eb; --color-base-content: #111827;
    --color-primary: #3b82f6; --color-primary-content: #ffffff; --color-secondary: #a78bfa; --color-accent: #22d3ee;
    --color-neutral: #6b7280; --color-info: #60a5fa; --color-success: #10b981; --color-warning: #f59e0b; --color-error: #ef4444;
    --glass-bg-rgb: 255 255 255; --glass-border-color: rgba(0,0,0,0.08);
  }
  .glass-ultraThin, .glass-thin, .glass-regular, .glass-thick, .glass-ultraThick, .glass-interactive::after {
    background: rgba(255,255,255,0.9) !important;
    background-image: none !important;
  }
  /* 关键：防止 color-mix/oklab/oklch 被解析失败，统一文本/边框为可导出颜色 */
  *, *::before, *::after {
    color: #111827 !important;
    border-color: rgba(0,0,0,0.12) !important;
    outline-color: rgba(0,0,0,0.12) !important;
    box-shadow: none !important;
    text-shadow: none !important;
    filter: none !important;
    mix-blend-mode: normal !important;
    background-image: none !important;
  }
`

export const GRADIENTS = {
  overall: 'linear-gradient(90deg, #34d399, #10b981)',
  dimension: 'linear-gradient(90deg, #60a5fa, #3b82f6)',
  templateSky: 'linear-gradient(90deg, #38bdf8, #2563eb)'
} as const

// 固定导出主题：尽量接近“历史页”的清爽风格
export const EXPORT_THEME_CSS = `
  [data-export-root="1"] { font-family: Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif !important; color: #111827 !important; background: #fff !important; }
  [data-export-root="1"] h1,[data-export-root="1"] h2,[data-export-root="1"] h3,[data-export-root="1"] h4 { color:#0f172a !important; margin: 0 0 8px 0 !important; font-weight:600 !important; }
  [data-export-root="1"] .export-card { border:1px solid rgba(0,0,0,0.08) !important; border-radius:12px !important; padding:12px !important; background:#fff !important; }
  [data-export-root="1"] .export-muted { color:#6b7280 !important; }
  [data-export-root="1"] .export-badge { display:inline-flex; align-items:center; gap:6px; padding:2px 8px; border-radius:9999px; border:1px solid rgba(0,0,0,0.08); background:#f8fafc; color:#0f172a; font-size:12px; }
  [data-export-root="1"] .export-row { display:flex; align-items:center; gap:12px; }
  [data-export-root="1"] .export-bar { height:8px; width:256px; border-radius:6px; overflow:hidden; border:1px solid rgba(0,0,0,0.12); background:#e5e7eb; }
  [data-export-root="1"] .export-fill.overall { background: linear-gradient(90deg, #34d399, #10b981); height:100%; }
  [data-export-root="1"] .export-fill.dimension { background: linear-gradient(90deg, #60a5fa, #3b82f6); height:100%; }
  /* 兼容历史页与其它页面：凡是进度条容器内的 data-gradient 子元素，都强制着色 */
  [data-export-root="1"] [data-gradient="overall"],
  [data-export-root="1"] .export-bar [data-gradient="overall"],
  [data-export-root="1"] .h-2 [data-gradient="overall"],
  [data-export-root="1"] .h-full[data-gradient="overall"] { background: linear-gradient(90deg, #34d399, #10b981) !important; background-color:#10b981 !important; }
  [data-export-root="1"] [data-gradient="dimension"],
  [data-export-root="1"] .export-bar [data-gradient="dimension"],
  [data-export-root="1"] .h-2 [data-gradient="dimension"],
  [data-export-root="1"] .h-full[data-gradient="dimension"] { background: linear-gradient(90deg, #60a5fa, #3b82f6) !important; background-color:#60a5fa !important; }
`

export async function exportNodeAsPng(node: HTMLElement, fileBase: string) {
  const canvas = await html2canvas(node, {
    backgroundColor: '#ffffff',
    scale: 2,
    useCORS: true,
    logging: false,
    onclone: (doc) => {
      try {
        const style = doc.createElement('style')
        style.setAttribute('data-export-fallback', 'oklch')
        style.innerHTML = EXPORT_FALLBACK_STYLE + EXPORT_THEME_CSS
        doc.head.appendChild(style)
        const root = doc.querySelector('[data-export-root="1"]') as HTMLElement | null
        if (root) root.setAttribute('data-export-skin', 'history-v1')
        // 移除内联样式中的 oklch/oklab/color-mix，修正 SVG 颜色属性
        const fixColor = (s: string) => s
          .replace(/color-mix\([^)]*\)/gi, '#111827')
          .replace(/oklch\([^)]*\)/gi, '#111827')
          .replace(/oklab\([^)]*\)/gi, '#111827')
        ;(doc.querySelectorAll('*') as NodeListOf<HTMLElement>).forEach(el => {
          const st = el.getAttribute('style')
          if (st && (/oklch|oklab|color-mix/i.test(st))) el.setAttribute('style', fixColor(st))
        })
        ;(['fill','stroke','stop-color'] as const).forEach(attr => {
          (doc.querySelectorAll(`svg *[${attr}]`) as NodeListOf<HTMLElement>).forEach(el => {
            const v = el.getAttribute(attr) || ''
            if (/oklch|oklab|color-mix|var\(/i.test(v)) el.setAttribute(attr, '#111827')
          })
        })
        // 统一为未标注的进度条填充维度色（overall 已由 data-gradient 覆盖）
        const fills = doc.querySelectorAll('[data-export-root="1"] .h-2 > div, [data-export-root="1"] .export-bar > div, [data-export-root="1"] .h-2 div.h-full') as NodeListOf<HTMLElement>
        fills.forEach(f => {
          const st = f.getAttribute('style') || ''
          const hasWidth = /width\s*:\s*\d/.test(st)
          if (!hasWidth) return
          const mark = (f.getAttribute('data-gradient') || '').toLowerCase()
          if (mark === 'overall') {
            f.style.backgroundColor = '#10b981'
            return
          }
          f.style.backgroundColor = '#60a5fa'
        })
      } catch {}
    }
  })
  const url = canvas.toDataURL('image/png')
  const a = document.createElement('a')
  a.href = url
  a.download = `${fileBase}.png`
  a.click()
}

export async function exportNodeAsPdf(node: HTMLElement, fileBase: string) {
  const canvas = await html2canvas(node, {
    backgroundColor: '#ffffff',
    scale: 2,
    useCORS: true,
    logging: false,
    onclone: (doc) => {
      try {
        const style = doc.createElement('style')
        style.setAttribute('data-export-fallback', 'oklch')
        style.innerHTML = EXPORT_FALLBACK_STYLE + EXPORT_THEME_CSS
        doc.head.appendChild(style)
        const root = doc.querySelector('[data-export-root="1"]') as HTMLElement | null
        if (root) root.setAttribute('data-export-skin', 'history-v1')
        // 移除内联样式中的 oklch/oklab/color-mix，修正 SVG 颜色属性
        const fixColor = (s: string) => s
          .replace(/color-mix\([^)]*\)/gi, '#111827')
          .replace(/oklch\([^)]*\)/gi, '#111827')
          .replace(/oklab\([^)]*\)/gi, '#111827')
        ;(doc.querySelectorAll('*') as NodeListOf<HTMLElement>).forEach(el => {
          const st = el.getAttribute('style')
          if (st && (/oklch|oklab|color-mix/i.test(st))) el.setAttribute('style', fixColor(st))
        })
        ;(['fill','stroke','stop-color'] as const).forEach(attr => {
          (doc.querySelectorAll(`svg *[${attr}]`) as NodeListOf<HTMLElement>).forEach(el => {
            const v = el.getAttribute(attr) || ''
            if (/oklch|oklab|color-mix|var\(/i.test(v)) el.setAttribute(attr, '#111827')
          })
        })
        const fills = doc.querySelectorAll('[data-export-root="1"] .h-2 > div, [data-export-root="1"] .export-bar > div, [data-export-root="1"] .h-2 div.h-full') as NodeListOf<HTMLElement>
        fills.forEach(f => {
          const st = f.getAttribute('style') || ''
          const hasWidth = /width\s*:\s*\d/.test(st)
          if (!hasWidth) return
          const mark = (f.getAttribute('data-gradient') || '').toLowerCase()
          if (mark === 'overall') {
            f.style.backgroundColor = '#10b981'
            return
          }
          f.style.backgroundColor = '#60a5fa'
        })
      } catch {}
    }
  })
  const pdf = new jsPDF({ orientation: 'p', unit: 'px', format: [canvas.width, canvas.height], compress: true })
  pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, canvas.width, canvas.height, undefined, 'FAST')
  pdf.save(`${fileBase}.pdf`)
}

export function applyExportGradientsInline(el: HTMLElement) {
  try {
    el.querySelectorAll('[data-gradient="overall"]').forEach((n) => (n as HTMLElement).style.background = GRADIENTS.overall)
    el.querySelectorAll('[data-gradient="dimension"]').forEach((n) => (n as HTMLElement).style.background = GRADIENTS.dimension)
    el.querySelectorAll('[data-gradient="templateSky"]').forEach((n) => (n as HTMLElement).style.background = GRADIENTS.templateSky)
  } catch {}
}

