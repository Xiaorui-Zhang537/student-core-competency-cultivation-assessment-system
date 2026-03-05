type TranslateFn = (key: string) => string

export const formatRadarArea = (area?: number): string => {
  if (!Number.isFinite(Number(area))) return '--'
  return `${Number(area).toFixed(1)}%`
}

export const getRadarBadgeVariant = (classification?: string): 'success' | 'warning' | 'info' | 'danger' => {
  switch ((classification || '').toUpperCase()) {
    case 'A': return 'success'
    case 'B': return 'warning'
    case 'C': return 'info'
    default: return 'danger'
  }
}

export const formatRadarBadgeLabel = (t: TranslateFn, classification?: string): string => {
  const key = (classification || 'D').toUpperCase() as 'A' | 'B' | 'C' | 'D'
  const desc = t(`teacher.students.radar.classification.${key}`)
  return `${key} · ${desc}`
}

export const formatDimensionLabel = (t: TranslateFn, code: string): string => {
  const label = t(`shared.radarLegend.dimensions.${code}.title`)
  return label || code
}

export const formatRadarValue = (value?: number): string => {
  if (!Number.isFinite(Number(value))) return '--'
  return Number(value).toFixed(1)
}
