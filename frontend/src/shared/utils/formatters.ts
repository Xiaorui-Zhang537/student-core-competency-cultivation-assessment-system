/**
 * 通用格式化工具函数
 * 从 StudentDetailView、CourseStudentsView、AnalyticsView 等多个视图中
 * 提取的重复定义的日期格式化、徽章颜色映射等函数
 */

/**
 * 格式化日期为本地化字符串（仅日期部分）
 */
export function formatDate(input: any, locale?: string): string {
  if (!input) return '-'
  try {
    const d = new Date(input)
    if (isNaN(d.getTime())) return String(input)
    const loc = locale || 'zh-CN'
    return d.toLocaleDateString(loc, { year: 'numeric', month: '2-digit', day: '2-digit' })
  } catch {
    return String(input)
  }
}

/**
 * 格式化日期时间为本地化字符串（含时间部分）
 */
export function formatDateTime(input: any, locale?: string): string {
  if (!input) return '-'
  try {
    const d = new Date(input)
    if (isNaN(d.getTime())) return String(input)
    const loc = locale || 'zh-CN'
    return d.toLocaleString(loc, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return String(input)
  }
}

/**
 * 相对时间格式化（如 "3 分钟前"）
 */
export function formatRelativeTime(input: any, locale?: string): string {
  if (!input) return '-'
  try {
    const d = new Date(input)
    if (isNaN(d.getTime())) return String(input)
    const now = Date.now()
    const diff = now - d.getTime()
    const isZh = (locale || 'zh-CN').startsWith('zh')

    if (diff < 60000) return isZh ? '刚刚' : 'just now'
    if (diff < 3600000) {
      const m = Math.floor(diff / 60000)
      return isZh ? `${m} 分钟前` : `${m}m ago`
    }
    if (diff < 86400000) {
      const h = Math.floor(diff / 3600000)
      return isZh ? `${h} 小时前` : `${h}h ago`
    }
    if (diff < 604800000) {
      const days = Math.floor(diff / 86400000)
      return isZh ? `${days} 天前` : `${days}d ago`
    }
    return formatDate(input, locale)
  } catch {
    return String(input)
  }
}

/**
 * 成绩徽章颜色变体映射
 */
export function getGradeBadgeVariant(score: number): string {
  if (score >= 90) return 'success'
  if (score >= 80) return 'info'
  if (score >= 70) return 'warning'
  if (score >= 60) return 'default'
  return 'danger'
}

/**
 * 雷达图/能力维度徽章颜色映射（0-5 标尺）
 */
export function getRadarBadgeVariant(score: number): string {
  if (score >= 4) return 'success'
  if (score >= 3) return 'info'
  if (score >= 2) return 'warning'
  return 'danger'
}

/**
 * 分类本地化函数（用于 CourseDetailView 等）
 */
export function localizeCategory(
  category: string,
  categoryMap: Record<string, { zh: string; en: string }>,
  locale?: string
): string {
  const isZh = (locale || 'zh-CN').startsWith('zh')
  const mapped = categoryMap[category]
  if (mapped) return isZh ? mapped.zh : mapped.en
  return category || '-'
}

/**
 * 数字格式化（如 12345 → "12,345"）
 */
export function formatNumber(val: number | string, locale?: string): string {
  const num = Number(val)
  if (!Number.isFinite(num)) return String(val)
  return num.toLocaleString(locale || 'zh-CN')
}

/**
 * 百分比格式化
 */
export function formatPercent(val: number, decimals = 1): string {
  if (!Number.isFinite(val)) return '0%'
  return `${(val * 100).toFixed(decimals)}%`
}
