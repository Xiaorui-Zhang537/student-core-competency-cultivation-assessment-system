export function buildAbilityReportText(parsed: any): string {
  const lines: string[] = []
  const pushSec = (title: string, sec: any) => {
    if (!sec) return
    for (const [k, v] of Object.entries(sec)) {
      const s = v as any
      lines.push(`${title} - ${String(k)}: ${Number(s?.score ?? 0)}/5`)
      const ev = Array.isArray(s?.evidence) ? s.evidence : []
      if (ev.length) {
        const quotes = ev.map((e: any) => String(e?.quote || '')).filter((q: string) => q.trim())
        if (quotes.length) {
          lines.push('证据:')
          quotes.forEach((q: string) => lines.push(`- ${q}`))
        }
        const reasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning
        const conclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion
        const explanation = (ev.find((e: any) => e && String(e.explanation || '').trim()) || {}).explanation
        if (reasoning) lines.push(`推理: ${String(reasoning)}`)
        if (conclusion) lines.push(`结论: ${String(conclusion)}`)
        if (explanation) lines.push(`解释: ${String(explanation)}`)
      }
      const sug = Array.isArray(s?.suggestions) ? s.suggestions : []
      if (sug.length) {
        lines.push('建议:')
        sug.forEach((x: any, idx: number) => lines.push(`${idx + 1}. ${String(x)}`))
      }
      lines.push('')
    }
  }

  const ov = (parsed as any)?.overall || parsed
  if (ov) {
    lines.push(`总体评分: ${Number((ov as any)?.final_score ?? 0)}/5`)
    if ((ov as any)?.holistic_feedback) lines.push(`总体评价: ${String((ov as any).holistic_feedback)}`)
    lines.push('')
  }
  pushSec('道德推理', parsed?.moral_reasoning)
  pushSec('学习态度', parsed?.attitude_development)
  pushSec('学习能力', parsed?.ability_growth)
  pushSec('学习策略', parsed?.strategy_optimization)
  return lines.join('\n')
}
