/**
 * AI 对话导出工具
 * 支持导出为 Markdown 和 JSON 格式
 */

export interface ExportMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt?: string
}

/**
 * 导出对话为 Markdown 格式
 */
export function exportAsMarkdown(title: string, messages: ExportMessage[], model?: string): string {
  const lines: string[] = []
  lines.push(`# ${title || '对话记录'}`)
  lines.push('')
  if (model) lines.push(`> 模型: ${model}`)
  lines.push(`> 导出时间: ${new Date().toLocaleString('zh-CN')}`)
  lines.push('')
  lines.push('---')
  lines.push('')

  for (const m of messages) {
    const label = m.role === 'user' ? '**用户**' : m.role === 'assistant' ? '**AI**' : '**系统**'
    const time = m.createdAt ? ` _(${new Date(m.createdAt).toLocaleString('zh-CN')})_` : ''
    lines.push(`### ${label}${time}`)
    lines.push('')
    lines.push(m.content)
    lines.push('')
  }

  return lines.join('\n')
}

/**
 * 导出对话为 JSON 格式
 */
export function exportAsJson(title: string, messages: ExportMessage[], model?: string): string {
  return JSON.stringify({
    title,
    model,
    exportedAt: new Date().toISOString(),
    messages: messages.map(m => ({
      role: m.role,
      content: m.content,
      createdAt: m.createdAt,
    })),
  }, null, 2)
}

/**
 * 触发浏览器下载
 */
export function downloadText(content: string, filename: string, mimeType = 'text/plain') {
  const blob = new Blob([content], { type: `${mimeType};charset=utf-8` })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

/**
 * 一键导出为 Markdown 文件
 */
export function downloadAsMarkdown(title: string, messages: ExportMessage[], model?: string) {
  const md = exportAsMarkdown(title, messages, model)
  const safeName = (title || 'conversation').replace(/[^a-zA-Z0-9\u4e00-\u9fa5_-]/g, '_')
  downloadText(md, `${safeName}.md`, 'text/markdown')
}

/**
 * 一键导出为 JSON 文件
 */
export function downloadAsJson(title: string, messages: ExportMessage[], model?: string) {
  const json = exportAsJson(title, messages, model)
  const safeName = (title || 'conversation').replace(/[^a-zA-Z0-9\u4e00-\u9fa5_-]/g, '_')
  downloadText(json, `${safeName}.json`, 'application/json')
}
