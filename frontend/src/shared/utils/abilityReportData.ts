import { normalizeAssessment, safeJsonParse } from '@/shared/utils/aiGradingNormalizer'

function hasCoreStructure(v: any): boolean {
  return !!(v && (v.overall || v.moral_reasoning || v.attitude_development || v.ability_growth || v.strategy_optimization))
}

function toFlatScores(fromA: any, fromB: any): Record<string, number> {
  const src = (fromA && typeof fromA === 'object') ? fromA : ((fromB && typeof fromB === 'object') ? fromB : {})
  const out: Record<string, number> = {}
  const pairs: Array<[string, string[]]> = [
    ['MORAL_COGNITION', ['MORAL_COGNITION', 'moral', 'moral_cognition']],
    ['LEARNING_ATTITUDE', ['LEARNING_ATTITUDE', 'attitude', 'learning_attitude']],
    ['LEARNING_ABILITY', ['LEARNING_ABILITY', 'ability', 'learning_ability']],
    ['LEARNING_METHOD', ['LEARNING_METHOD', 'strategy', 'learning_method']],
  ]
  for (const [key, aliases] of pairs) {
    for (const alias of aliases) {
      const n = Number((src as any)?.[alias])
      if (Number.isFinite(n)) {
        out[key] = n
        break
      }
    }
  }
  return out
}

function score5(v: number): number {
  if (!Number.isFinite(v)) return 0
  if (v > 5) return Math.max(0, Math.min(5, v / 20))
  return Math.max(0, Math.min(5, v))
}

function buildFallbackFromFlatScores(report: any, normalized: any, rawObj: any): any | null {
  const flat = toFlatScores(normalized, rawObj)
  const keys = Object.keys(flat)
  if (!keys.length) return null
  const mr = score5(Number(flat.MORAL_COGNITION || 0))
  const ad = score5(Number(flat.LEARNING_ATTITUDE || 0))
  const ag = score5(Number(flat.LEARNING_ABILITY || 0))
  const so = score5(Number(flat.LEARNING_METHOD || 0))
  const mk = (score: number) => ({ score, evidence: [], suggestions: [] })
  const finalScore = [mr, ad, ag, so].reduce((a, b) => a + b, 0) / 4
  return {
    overall: {
      dimension_averages: { moral_reasoning: mr, attitude: ad, ability: ag, strategy: so },
      final_score: Math.round(finalScore * 10) / 10,
      holistic_feedback: String((report as any)?.recommendations || ''),
    },
    moral_reasoning: { stage_level: mk(mr), foundations_balance: mk(mr), argument_chain: mk(mr) },
    attitude_development: { emotional_engagement: mk(ad), resilience: mk(ad), focus_flow: mk(ad) },
    ability_growth: { blooms_level: mk(ag), metacognition: mk(ag), transfer: mk(ag) },
    strategy_optimization: { diversity: mk(so), depth: mk(so), self_regulation: mk(so) },
  }
}

export function resolveReportRawJson(report: any): any {
  if (!report) return null
  return (
    (report as any).trendsAnalysis ||
    (report as any).trends_analysis ||
    (report as any).normalizedJson ||
    (report as any).rawJson ||
    (report as any).raw_json ||
    (report as any).dimensionScores ||
    null
  )
}

export function parseAbilityReport(report: any): any | null {
  const raw = resolveReportRawJson(report)
  if (!raw) return null
  try {
    const obj = typeof raw === 'string' ? safeJsonParse(raw) : raw
    if (!obj) return null
    const normalized = normalizeAssessment(obj)
    const output = hasCoreStructure(normalized) ? normalized : buildFallbackFromFlatScores(report, normalized, obj)
    if (!output) return null
    const feedback = String((report as any)?.recommendations || '').trim()
    if (feedback) {
      if ((output as any).overall && typeof (output as any).overall === 'object') {
        if (!String((output as any).overall.holistic_feedback || '').trim()) (output as any).overall.holistic_feedback = feedback
      } else {
        ;(output as any).holistic_feedback = feedback
      }
    }
    return output
  } catch {
    return buildFallbackFromFlatScores(report, null, null)
  }
}
