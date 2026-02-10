/**
 * 统一的 Markdown 渲染器
 * 使用 marked + highlight.js 提供代码高亮、表格、列表等完整 GFM 支持
 * 供 AI 助手、聊天等模块复用
 */
import { Marked } from 'marked'
import hljs from 'highlight.js/lib/core'
// 引入 highlight.js 主题样式（GitHub 风格，支持亮暗模式）
import 'highlight.js/styles/github.css'

// 按需注册常用语言（减少打包体积）
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import css from 'highlight.js/lib/languages/css'
import xml from 'highlight.js/lib/languages/xml'
import json from 'highlight.js/lib/languages/json'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import markdown from 'highlight.js/lib/languages/markdown'
import cpp from 'highlight.js/lib/languages/cpp'
import csharp from 'highlight.js/lib/languages/csharp'
import go from 'highlight.js/lib/languages/go'
import rust from 'highlight.js/lib/languages/rust'
import php from 'highlight.js/lib/languages/php'
import ruby from 'highlight.js/lib/languages/ruby'
import swift from 'highlight.js/lib/languages/swift'
import kotlin from 'highlight.js/lib/languages/kotlin'
import yaml from 'highlight.js/lib/languages/yaml'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('py', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('css', css)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('json', json)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('shell', bash)
hljs.registerLanguage('sh', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('markdown', markdown)
hljs.registerLanguage('md', markdown)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c', cpp)
hljs.registerLanguage('csharp', csharp)
hljs.registerLanguage('cs', csharp)
hljs.registerLanguage('go', go)
hljs.registerLanguage('rust', rust)
hljs.registerLanguage('php', php)
hljs.registerLanguage('ruby', ruby)
hljs.registerLanguage('swift', swift)
hljs.registerLanguage('kotlin', kotlin)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('yml', yaml)

/** 创建配置好的 Marked 实例 */
function createMarkedInstance(): Marked {
  const marked = new Marked()

  marked.setOptions({
    gfm: true,
    breaks: true,
  })

  // 自定义渲染器
  const renderer = {
    code(this: any, token: { text: string; lang?: string }) {
      const code = token.text || ''
      const lang = (token.lang || '').trim().toLowerCase()
      let highlighted: string
      if (lang && hljs.getLanguage(lang)) {
        try {
          highlighted = hljs.highlight(code, { language: lang, ignoreIllegals: true }).value
        } catch {
          highlighted = escapeHtml(code)
        }
      } else {
        // 尝试自动检测
        try {
          highlighted = hljs.highlightAuto(code).value
        } catch {
          highlighted = escapeHtml(code)
        }
      }
      const langLabel = lang ? `<span class="code-lang-label">${escapeHtml(lang)}</span>` : ''
      return `<div class="code-block-wrapper"><div class="code-block-header">${langLabel}<button class="code-copy-btn" onclick="window.__copyCodeBlock(this)" title="复制">`
        + `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>`
        + `<span class="copy-text">复制</span></button></div><pre><code class="hljs${lang ? ` language-${escapeHtml(lang)}` : ''}">${highlighted}</code></pre></div>`
    },
    link(this: any, token: { href: string; title?: string; tokens?: any[] }) {
      const href = token.href || ''
      const title = token.title ? ` title="${escapeHtml(token.title)}"` : ''
      // 手动渲染子 token 获取文本
      const text = token.tokens ? (this as any).parser.parseInline(token.tokens) : escapeHtml(href)
      return `<a href="${escapeHtml(href)}" target="_blank" rel="noopener noreferrer"${title}>${text}</a>`
    },
    table(this: any, token: { header: any[]; rows: any[] }) {
      const headerCells = token.header.map((cell: any) => {
        const align = cell.align ? ` style="text-align:${cell.align}"` : ''
        const content = (this as any).parser.parseInline(cell.tokens)
        return `<th${align}>${content}</th>`
      }).join('')
      const bodyRows = token.rows.map((row: any[]) => {
        const cells = row.map((cell: any) => {
          const align = cell.align ? ` style="text-align:${cell.align}"` : ''
          const content = (this as any).parser.parseInline(cell.tokens)
          return `<td${align}>${content}</td>`
        }).join('')
        return `<tr>${cells}</tr>`
      }).join('')
      return `<table class="ai-table"><thead><tr>${headerCells}</tr></thead><tbody>${bodyRows}</tbody></table>`
    },
  }

  marked.use({ renderer })
  return marked
}

/** HTML 转义 */
function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

// 单例
let _marked: Marked | null = null
function getMarked(): Marked {
  if (!_marked) _marked = createMarkedInstance()
  return _marked
}

/**
 * 渲染 Markdown 字符串为 HTML
 * @param raw 原始 markdown 文本
 * @returns 安全的 HTML 字符串
 */
export function renderMarkdown(raw: string): string {
  if (!raw) return ''
  try {
    const result = getMarked().parse(raw)
    // marked.parse 可能返回 string | Promise<string>，同步模式下为 string
    return typeof result === 'string' ? result : ''
  } catch {
    // 回退：简单转义
    return escapeHtml(raw).replace(/\n/g, '<br/>')
  }
}

/**
 * 全局代码块复制处理函数
 * 挂载到 window 上供内联 onclick 调用
 */
export function installCodeCopyHandler(): void {
  if (typeof window === 'undefined') return
  ;(window as any).__copyCodeBlock = (btn: HTMLButtonElement) => {
    const wrapper = btn.closest('.code-block-wrapper')
    if (!wrapper) return
    const codeEl = wrapper.querySelector('code')
    if (!codeEl) return
    const text = codeEl.textContent || ''
    navigator.clipboard.writeText(text).then(() => {
      const textSpan = btn.querySelector('.copy-text')
      if (textSpan) {
        const orig = textSpan.textContent
        textSpan.textContent = '已复制'
        btn.classList.add('copied')
        setTimeout(() => {
          textSpan.textContent = orig
          btn.classList.remove('copied')
        }, 2000)
      }
    }).catch(() => {})
  }
}
