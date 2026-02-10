/**
 * 文件下载工具
 * - 兼容后端返回 Blob / ArrayBuffer / string / AxiosResponse({ data })
 * - 统一 createObjectURL + a.click + revokeObjectURL 流程
 */
export type BlobSourceLike =
  | Blob
  | ArrayBuffer
  | Uint8Array
  | string
  | { data?: any }

function toBlob(input: BlobSourceLike, mimeType?: string): Blob {
  if (input instanceof Blob) return input

  const data = (input as any)?.data ?? input
  if (data instanceof Blob) return data

  const type = mimeType || 'application/octet-stream'
  // string 直接作为文本写入；ArrayBuffer/Uint8Array 直接作为二进制写入
  if (typeof data === 'string') return new Blob([data], { type })
  if (data instanceof ArrayBuffer) return new Blob([data], { type })
  if (data instanceof Uint8Array) return new Blob([data], { type })

  // 兜底：尽量 stringify（避免传入对象时直接变成 [object Object]）
  try {
    return new Blob([JSON.stringify(data, null, 2)], { type: 'application/json;charset=utf-8' })
  } catch {
    return new Blob([String(data)], { type })
  }
}

export function downloadBlobAsFile(input: BlobSourceLike, filename: string, mimeType?: string) {
  const blob = toBlob(input, mimeType)
  const url = window.URL.createObjectURL(blob)
  try {
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.rel = 'noopener'
    a.click()
  } finally {
    // 让浏览器有机会开始读取 blob
    setTimeout(() => {
      try { window.URL.revokeObjectURL(url) } catch {}
    }, 0)
  }
}

export function downloadCsv(input: BlobSourceLike, filename: string) {
  downloadBlobAsFile(input, filename, 'text/csv;charset=utf-8;')
}

