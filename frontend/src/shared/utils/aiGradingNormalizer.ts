/**
 * AI 评分数据标准化工具
 * 从 GradeAssignmentView 和 TeacherAIGradingView 中提取的公共逻辑
 * 支持多种 LLM 输出格式的统一转换
 */

/** 计算数组平均值（保留一位小数） */
export function avg(nums: number[]): number {
  const arr = nums.filter(n => Number.isFinite(n))
  if (!arr.length) return 0
  return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10
}

/** 汇总维度均分与建议，构造整体反馈 */
export function buildHolisticFeedback(out: any): string {
  try {
    const parts: string[] = []
    const dimAvg = out?.overall?.dimension_averages || {}
    const finalScore = Number(out?.overall?.final_score ?? 0)
    parts.push(`Averages — Moral: ${dimAvg.moral_reasoning ?? 0}, Attitude: ${dimAvg.attitude ?? 0}, Ability: ${dimAvg.ability ?? 0}, Strategy: ${dimAvg.strategy ?? 0}. Final: ${finalScore}/5.`)
    const allSuggestions: string[] = []
    const pushFromGroup = (grp: any) => {
      if (!grp) return
      for (const key of Object.keys(grp)) {
        const arr = Array.isArray(grp[key]?.suggestions) ? grp[key].suggestions : []
        for (const s of arr) { if (s && allSuggestions.length < 6) allSuggestions.push(String(s)) }
      }
    }
    pushFromGroup(out.moral_reasoning)
    pushFromGroup(out.attitude_development)
    pushFromGroup(out.ability_growth)
    pushFromGroup(out.strategy_optimization)
    if (allSuggestions.length) {
      parts.push('Key suggestions:')
      for (const s of allSuggestions) parts.push(`- ${s}`)
    }
    return parts.join('\n')
  } catch { return '' }
}

/** 计算四维均分并生成 overall */
export function computeOverall(out: any): void {
  const mrAvg = avg([
    Number(out.moral_reasoning?.stage_level?.score),
    Number(out.moral_reasoning?.foundations_balance?.score),
    Number(out.moral_reasoning?.argument_chain?.score)
  ])
  const adAvg = avg([
    Number(out.attitude_development?.emotional_engagement?.score),
    Number(out.attitude_development?.resilience?.score),
    Number(out.attitude_development?.focus_flow?.score)
  ])
  const agAvg = avg([
    Number(out.ability_growth?.blooms_level?.score),
    Number(out.ability_growth?.metacognition?.score),
    Number(out.ability_growth?.transfer?.score)
  ])
  const soAvg = avg([
    Number(out.strategy_optimization?.diversity?.score),
    Number(out.strategy_optimization?.depth?.score),
    Number(out.strategy_optimization?.self_regulation?.score)
  ])
  const finalScore = avg([mrAvg, adAvg, agAvg, soAvg])
  out.overall = {
    dimension_averages: { moral_reasoning: mrAvg, attitude: adAvg, ability: agAvg, strategy: soAvg },
    final_score: finalScore,
    holistic_feedback: ''
  }
  out.overall.holistic_feedback = buildHolisticFeedback(out)
}

/** 将 evidence 字符串解析为结构化数组 */
export function parseEvidenceString(s: string): any[] {
  const text = String(s || '')
  const m = text.match(/Quote:\s*([\s\S]*?)\n\s*Reasoning:\s*([\s\S]*?)\n\s*Conclusion:\s*([\s\S]*)/i)
  if (m) return [{ quote: m[1]?.trim() || '', reasoning: m[2]?.trim() || '', conclusion: m[3]?.trim() || '' }]
  const mq = text.match(/Quote:\s*([\s\S]*)/i)
  if (mq) return [{ quote: mq[1]?.trim() || '', reasoning: '', conclusion: '' }]
  return []
}

/** 通用 section 解析：score + evidence + suggestions */
function toSection(raw: any): { score: number; evidence: any[]; suggestions: string[] } {
  if (!raw || typeof raw !== 'object') return { score: 0, evidence: [], suggestions: [] }
  const score = Number(raw?.score ?? 0)
  const ev = raw?.evidence
  let evidence: any[]
  if (Array.isArray(ev?.quotes)) {
    evidence = ev.quotes.map((q: any) => ({ quote: String(q), reasoning: String(ev?.reasoning || ''), conclusion: String(ev?.conclusion || '') }))
  } else if (Array.isArray(ev)) {
    evidence = ev.map((e: any) => typeof e === 'object' ? e : { quote: '', reasoning: String(e ?? ''), conclusion: '' })
  } else if (ev) {
    const parsed = parseEvidenceString(String(ev))
    evidence = parsed.length ? parsed : [{ quote: '', reasoning: String(ev), conclusion: '' }]
  } else {
    evidence = [{ quote: '', reasoning: '', conclusion: '' }]
  }
  const sraw = raw?.suggestions
  const suggestions = Array.isArray(sraw) ? sraw.map(String) : (sraw ? [String(sraw)] : [])
  return { score, evidence, suggestions }
}

/** 从 evaluation_result 数组转换 */
export function normalizeFromEvaluationArray(arr: any[]): any {
  const out: any = { moral_reasoning: {}, attitude_development: {}, ability_growth: {}, strategy_optimization: {} }
  const mapDim = (name: string) => {
    const n = (name || '').toLowerCase()
    if (n.includes('moral')) return 'moral_reasoning'
    if (n.includes('attitude')) return 'attitude_development'
    if (n.includes('ability')) return 'ability_growth'
    if (n.includes('strategy')) return 'strategy_optimization'
    return ''
  }
  const mapId: Record<string, Record<string, string>> = {
    moral_reasoning: { '1A': 'stage_level', '1B': 'foundations_balance', '1C': 'argument_chain' },
    attitude_development: { '2A': 'emotional_engagement', '2B': 'resilience', '2C': 'focus_flow' },
    ability_growth: { '3A': 'blooms_level', '3B': 'metacognition', '3C': 'transfer' },
    strategy_optimization: { '4A': 'diversity', '4B': 'depth', '4C': 'self_regulation' }
  }
  for (const g of (arr || [])) {
    const dimKey = mapDim(g?.dimension || '')
    if (!dimKey) continue
    for (const it of (Array.isArray(g?.sub_criteria) ? g.sub_criteria : [])) {
      const secKey = mapId[dimKey]?.[String(it?.id || '').toUpperCase()] || ''
      if (secKey) out[dimKey][secKey] = toSection(it)
    }
  }
  computeOverall(out)
  return out
}

/** 从 evaluation 对象（含 "1) ..." 键）转换 */
export function normalizeFromEvaluationObject(evaluation: any): any {
  const norm = (s: string) => String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '')
  const pickSub = (group: any, ids: string[], hints: string[]): any => {
    if (!group || typeof group !== 'object') return undefined
    const keys = Object.keys(group)
    const idNorms = ids.map(id => norm(id))
    const hintNorms = hints.map(h => norm(h))
    for (const k of keys) { const nk = norm(k); if (idNorms.some(id => nk.startsWith(id))) return group[k] }
    for (const k of keys) { const nk = norm(k); if (hintNorms.every(h => nk.includes(h))) return group[k] }
    return undefined
  }
  const findGroup = (obj: any, hint: string) => { const k = Object.keys(obj || {}).find(k => k.toLowerCase().includes(hint)); return k ? obj[k] : undefined }

  const moral = evaluation["1) Moral Reasoning Maturity"] || findGroup(evaluation, 'moral reasoning') || {}
  const attitude = evaluation["2) Learning Attitude Development"] || findGroup(evaluation, 'attitude') || {}
  const ability = evaluation["3) Learning Ability Growth"] || findGroup(evaluation, 'ability') || {}
  const strategy = evaluation["4) Learning Strategy Optimization"] || findGroup(evaluation, 'strategy') || {}

  const out: any = {
    moral_reasoning: { stage_level: toSection(pickSub(moral, ['1a'], ['stage', 'level']) || {}), foundations_balance: toSection(pickSub(moral, ['1b'], ['foundation']) || {}), argument_chain: toSection(pickSub(moral, ['1c'], ['argument']) || pickSub(moral, ['1c'], ['counter']) || {}) },
    attitude_development: { emotional_engagement: toSection(pickSub(attitude, ['2a'], ['emotional']) || {}), resilience: toSection(pickSub(attitude, ['2b'], ['resilience']) || pickSub(attitude, ['2b'], ['persistence']) || {}), focus_flow: toSection(pickSub(attitude, ['2c'], ['task', 'flow']) || pickSub(attitude, ['2c'], ['focus']) || {}) },
    ability_growth: { blooms_level: toSection(pickSub(ability, ['3a'], ['bloom', 'taxonomy']) || {}), metacognition: toSection(pickSub(ability, ['3b'], ['metacognition']) || {}), transfer: toSection(pickSub(ability, ['3c'], ['transfer']) || {}) },
    strategy_optimization: { diversity: toSection(pickSub(strategy, ['4a'], ['diversity']) || {}), depth: toSection(pickSub(strategy, ['4b'], ['depth']) || {}), self_regulation: toSection(pickSub(strategy, ['4c'], ['self', 'regulation']) || {}) },
  }
  computeOverall(out)
  return out
}

/** 从 evaluation_results 风格转换 */
export function normalizeFromEvaluationResults(er: any): any {
  const lcKeys = Object.keys(er || {})
  const findKey = (includes: string[]) => lcKeys.find(k => includes.every(t => k.toLowerCase().includes(t)))
  const getSec = (group: any, hints: string[]) => {
    if (!group || typeof group !== 'object') return { score: 0, evidence: [], suggestions: [] }
    const target = Object.keys(group).find(k => hints.every(h => k.toLowerCase().includes(h)))
    return target ? toSection(group[target]) : { score: 0, evidence: [], suggestions: [] }
  }
  const moral = findKey(['moral', 'reasoning']) ? er[findKey(['moral', 'reasoning'])!] : undefined
  const attitude = (findKey(['attitude']) ? er[findKey(['attitude'])!] : undefined)
  const ability = (findKey(['ability', 'growth']) || findKey(['learning', 'ability'])) ? er[(findKey(['ability', 'growth']) || findKey(['learning', 'ability']))!] : undefined
  const strategy = (findKey(['strategy'])) ? er[findKey(['strategy'])!] : undefined

  const out: any = {
    moral_reasoning: { stage_level: getSec(moral, ['stage', 'level']), foundations_balance: getSec(moral, ['foundation']), argument_chain: getSec(moral, ['argument']) },
    attitude_development: { emotional_engagement: getSec(attitude, ['emotional']), resilience: getSec(attitude, ['resilience']), focus_flow: getSec(attitude, ['flow']) },
    ability_growth: { blooms_level: getSec(ability, ['bloom']), metacognition: getSec(ability, ['metacognition']), transfer: getSec(ability, ['transfer']) },
    strategy_optimization: { diversity: getSec(strategy, ['diversity']), depth: getSec(strategy, ['depth']), self_regulation: getSec(strategy, ['self', 'regulation']) },
  }
  computeOverall(out)
  return out
}

/** 从蛇形命名结构转换 */
export function normalizeFromSnakeDimensions(obj: any): any {
  const mr = obj.moral_reasoning_maturity || {}
  const ad = obj.learning_attitude_development || {}
  const ag = obj.learning_ability_growth || {}
  const so = obj.learning_strategy_optimization || {}
  const out: any = {
    moral_reasoning: { stage_level: toSection(mr['1A_stage_level_identification'] || mr['1a'] || {}), foundations_balance: toSection(mr['1B_breadth_of_moral_foundations'] || mr['1b'] || {}), argument_chain: toSection(mr['1C_argument_chains_and_counter_arguments'] || mr['1c'] || {}) },
    attitude_development: { emotional_engagement: toSection(ad['2A_emotional_engagement'] || ad['2a'] || {}), resilience: toSection(ad['2B_persistence_resilience'] || ad['2b'] || {}), focus_flow: toSection(ad['2C_task_focus_flow'] || ad['2c'] || {}) },
    ability_growth: { blooms_level: toSection(ag['3A_bloom_s_taxonomy_progression'] || ag['3a'] || {}), metacognition: toSection(ag['3B_metacognition_plan_monitor_revise'] || ag['3b'] || {}), transfer: toSection(ag['3C_knowledge_transfer'] || ag['3c'] || {}) },
    strategy_optimization: { diversity: toSection(so['4A_strategy_diversity'] || so['4a'] || {}), depth: toSection(so['4B_depth_of_processing'] || so['4b'] || {}), self_regulation: toSection(so['4C_self_regulation'] || so['4c'] || {}) },
  }
  computeOverall(out)
  return out
}

/** 从 evaluation.dimensions 风格转换 */
export function normalizeFromEvaluationDimensions(evaluation: any): any {
  const dims = evaluation?.dimensions || {}
  const findDim = (names: string[]) => { const keys = Object.keys(dims); for (const k of keys) { const kl = k.toLowerCase(); for (const n of names) { if (kl.includes(n.toLowerCase())) return dims[k] } }; return undefined }
  const moral = findDim(['moral reasoning'])
  const attitude = findDim(['learning attitude'])
  const ability = findDim(['learning ability'])
  const strategy = findDim(['strategy'])
  const out: any = {
    moral_reasoning: { stage_level: toSection(moral?.['1A']), foundations_balance: toSection(moral?.['1B']), argument_chain: toSection(moral?.['1C']) },
    attitude_development: { emotional_engagement: toSection(attitude?.['2A']), resilience: toSection(attitude?.['2B']), focus_flow: toSection(attitude?.['2C']) },
    ability_growth: { blooms_level: toSection(ability?.['3A']), metacognition: toSection(ability?.['3B']), transfer: toSection(ability?.['3C']) },
    strategy_optimization: { diversity: toSection(strategy?.['4A']), depth: toSection(strategy?.['4B']), self_regulation: toSection(strategy?.['4C']) },
  }
  computeOverall(out)
  return out
}

/**
 * 统一入口：自动检测格式并标准化 AI 评分结果
 */
export function normalizeAssessment(obj: any): any {
  try {
    if (obj == null || typeof obj === 'string') return obj
    if (Array.isArray(obj.evaluation_result)) return normalizeFromEvaluationArray(obj.evaluation_result)
    if (obj.evaluation_results && typeof obj.evaluation_results === 'object') return normalizeFromEvaluationResults(obj.evaluation_results)
    if (obj.evaluation && typeof obj.evaluation === 'object') {
      if (obj.evaluation.dimensions) return normalizeFromEvaluationDimensions(obj.evaluation)
      return normalizeFromEvaluationObject(obj.evaluation)
    }
    if (obj.moral_reasoning_maturity || obj.learning_attitude_development || obj.learning_ability_growth || obj.learning_strategy_optimization) {
      return normalizeFromSnakeDimensions(obj)
    }
    const hasCore = (o: any) => !!(o && (o.moral_reasoning || o.attitude_development || o.ability_growth || o.strategy_optimization || o.overall))
    if (hasCore(obj)) return obj
    return obj
  } catch { return obj }
}

/**
 * 前端宽松 JSON 解析
 */
export function safeJsonParse(raw: string): any {
  let s = String(raw || '').replace(/^\uFEFF/, '').trim()
  const fence = /```(?:json)?\s*([\s\S]*?)```/i
  const m = s.match(fence)
  if (m && m[1]) s = m[1].trim()
  const first = s.indexOf('{')
  const last = s.lastIndexOf('}')
  if (first >= 0 && last > first) s = s.substring(first, last + 1)
  s = s.replace(/,\s*}/g, '}').replace(/,\s*]/g, ']')
  return JSON.parse(s)
}

/**
 * 渲染单个评估标准为 HTML 片段
 */
export function renderCriterion(block: any, barKind?: string): string {
  if (!block || typeof block !== 'object') return ''
  const score = Number(block.score ?? 0)
  const pct = Math.max(0, Math.min(100, (score / 5) * 100))
  const colorClass = barKind ? `bar-${barKind}` : ''
  let html = `<div class="flex items-center gap-2 mb-1"><div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2"><div class="h-2 rounded-full ${colorClass}" style="width:${pct}%"></div></div><span class="text-xs font-bold whitespace-nowrap">${score.toFixed(1)}/5</span></div>`
  const evidence = Array.isArray(block.evidence) ? block.evidence : []
  for (const ev of evidence) {
    if (ev?.quote) html += `<div class="text-xs italic text-gray-500 dark:text-gray-400 mt-1">"${String(ev.quote).slice(0, 200)}"</div>`
    if (ev?.reasoning) html += `<div class="text-xs text-gray-600 dark:text-gray-300 mt-0.5">${String(ev.reasoning).slice(0, 300)}</div>`
  }
  const suggestions = Array.isArray(block.suggestions) ? block.suggestions : []
  if (suggestions.length) {
    html += '<ul class="text-xs text-blue-600 dark:text-blue-400 mt-1 list-disc pl-4">'
    for (const s of suggestions) html += `<li>${String(s).slice(0, 200)}</li>`
    html += '</ul>'
  }
  return html
}
