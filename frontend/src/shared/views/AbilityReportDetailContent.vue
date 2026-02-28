<template>
  <div v-if="parsedAi" class="space-y-4">
    <card padding="sm" tint="secondary">
      <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.overall', '总体') }}</h4>
      <div>
        <div class="text-sm mb-2 flex items-center gap-3" v-if="getOverall(parsedAi)?.final_score != null">
          <span>{{ i18nText('teacher.aiGrading.render.final_score', '最终分') }}: {{ overallScore(parsedAi) }}</span>
          <div class="h-2 w-64 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
            <div class="h-full" data-gradient="overall" :style="{ width: ((Number(overallScore(parsedAi)) * 20) || 0) + '%' }"></div>
          </div>
        </div>
        <div class="space-y-2 mb-2" v-if="dimensionBars(parsedAi)">
          <div class="text-sm font-medium">{{ i18nText('teacher.aiGrading.render.dimension_averages', '维度均分') }}</div>
          <div v-for="row in dimensionBars(parsedAi)" :key="row.key" class="flex items-center gap-3">
            <div class="w-40 text-xs text-gray-700 dark:text-gray-300">{{ row.label }}: {{ row.value }}</div>
            <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
              <div class="h-full" :data-gradient="dimGradient(row.key)" :style="{ width: ((row.value * 20) || 0) + '%' }"></div>
            </div>
          </div>
        </div>
        <div class="text-sm whitespace-pre-wrap">{{ i18nText('teacher.aiGrading.render.holistic_feedback', '整体反馈') }}: {{ overallFeedback(parsedAi) || (i18nText('common.empty', '无内容')) }}</div>
      </div>
    </card>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <card padding="sm" tint="warning" v-if="parsedAi?.moral_reasoning">
        <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.moral_reasoning', '道德推理') }}</h4>
        <div v-html="renderCriterion(parsedAi.moral_reasoning, 'dimension_moral')"></div>
      </card>
      <card padding="sm" tint="accent" v-if="parsedAi?.attitude_development">
        <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.attitude_development', '学习态度') }}</h4>
        <div v-html="renderCriterion(parsedAi.attitude_development, 'dimension_attitude')"></div>
      </card>
      <card padding="sm" tint="info" v-if="parsedAi?.ability_growth">
        <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.ability_growth', '学习能力') }}</h4>
        <div v-html="renderCriterion(parsedAi.ability_growth, 'dimension_ability')"></div>
      </card>
      <card padding="sm" tint="success" v-if="parsedAi?.strategy_optimization">
        <h4 class="font-semibold mb-2">{{ i18nText('teacher.aiGrading.render.strategy_optimization', '学习策略') }}</h4>
        <div v-html="renderCriterion(parsedAi.strategy_optimization, 'dimension_strategy')"></div>
      </card>
    </div>
  </div>
  <pre v-else class="bg-black/70 text-green-100 p-3 rounded overflow-auto text-xs max-h-[60vh]">{{ pretty(rawAiJson) }}</pre>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import { parseAbilityReport, resolveReportRawJson } from '@/shared/utils/abilityReportData'

const props = defineProps<{ report: any }>()
const { t } = useI18n()

function i18nText(key: string, fallback: string): string {
  try {
    const msg = (t(key) as any) as string
    return (msg && msg !== key) ? msg : fallback
  } catch {
    return fallback
  }
}

const rawAiJson = computed(() => resolveReportRawJson(props.report))
const parsedAi = computed(() => parseAbilityReport(props.report))

function pretty(v: any) {
  try { return JSON.stringify(typeof v === 'string' ? JSON.parse(v) : v, null, 2) } catch { return String(v || '') }
}

function getOverall(obj: any): any {
  if (!obj || typeof obj !== 'object') return null
  if ((obj as any).overall) return (obj as any).overall
  if ((obj as any).final_score || (obj as any).holistic_feedback || (obj as any).dimension_averages) return obj
  return null
}
function overallScore(obj: any): number {
  const ov = getOverall(obj)
  const n = Number(ov?.final_score)
  return Number.isFinite(n) ? n : 0
}
function overallFeedback(obj: any): string {
  const ov = getOverall(obj)
  return String(ov?.holistic_feedback || '')
}
function dimensionBars(obj: any): Array<{ key: string; label: string; value: number }> | null {
  try {
    const ov = getOverall(obj)
    const dm = ov?.dimension_averages
    if (!dm) return null
    return [
      { key: 'moral_reasoning', label: i18nText('teacher.aiGrading.render.moral_reasoning', '道德推理'), value: Number(dm.moral_reasoning ?? 0) },
      { key: 'attitude', label: i18nText('teacher.aiGrading.render.attitude_development', '学习态度'), value: Number(dm.attitude ?? 0) },
      { key: 'ability', label: i18nText('teacher.aiGrading.render.ability_growth', '学习能力'), value: Number(dm.ability ?? 0) },
      { key: 'strategy', label: i18nText('teacher.aiGrading.render.strategy_optimization', '学习策略'), value: Number(dm.strategy ?? 0) },
    ]
  } catch { return null }
}
function renderCriterion(block: any, barKind?: 'dimension' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy') {
  try {
    const sections: string[] = []
    for (const [k, v] of Object.entries(block || {})) {
      const sec = v as any
      const score = sec?.score
      const ev = Array.isArray(sec?.evidence) ? sec.evidence : []
      const sug = Array.isArray(sec?.suggestions) ? sec.suggestions : []
      const which = barKind || 'dimension'
      const labelInline = (txt: string) =>
        `<span class="inline-flex items-center gap-2 font-semibold mr-1">
          <span class="inline-block w-2 h-2 rounded-full" data-gradient="${which}"></span>
          <span>${escapeHtml(txt)}:</span>
        </span>`
      const labelBlock = (txt: string) =>
        `<div class="mt-2 mb-1 inline-flex items-center gap-2 text-xs font-semibold">
          <span class="inline-block w-2 h-2 rounded-full" data-gradient="${which}"></span>
          <span>${escapeHtml(txt)}</span>
        </div>`
      const bar = typeof score === 'number' ? `<div class=\"h-2 w-40 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner\"><div class=\"h-full\" data-gradient=\"${which}\" style=\"width:${score * 20}%\"></div></div>` : ''
      const firstReasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning || ''
      const firstConclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion || ''
      const evid = ev
        .filter((e: any) => (e && (e.quote || e.explanation)))
        .map((e: any) => `<li class="mb-1">
            <div class="text-xs text-gray-600 dark:text-gray-300">${e.quote ? '“' + escapeHtml(e.quote) + '”' : ''}</div>
            ${e.explanation ? `<div class="text-[11px] text-gray-500">${escapeHtml(e.explanation)}</div>` : ''}</li>`)
        .join('')
      const reasoningBlock = firstReasoning ? `<div class="text-xs mt-1">${labelInline('Reasoning')} ${escapeHtml(firstReasoning)}</div>` : ''
      const conclusionBlock = firstConclusion ? `<div class="text-xs italic text-gray-600 dark:text-gray-300 mt-1"><span class="not-italic">${labelInline('Conclusion')}</span> ${escapeHtml(firstConclusion)}</div>` : ''
      const sugg = sug.map((s: any) => `<li class="mb-1 text-xs">${escapeHtml(String(s))}</li>`).join('')
      sections.push(`<div class="space-y-2"><div class="text-sm font-medium">${escapeHtml(String(k))} ${score != null ? `(${score}/5)` : ''}</div>${bar}<div>${labelBlock('Evidence')}<ul>${evid}</ul>${reasoningBlock}${conclusionBlock}</div><div>${labelBlock('Suggestions')}<ul>${sugg}</ul></div></div>`)
    }
    return sections.join('')
  } catch { return `<pre class="text-xs">${escapeHtml(pretty(block))}</pre>` }
}
function dimGradient(key: string): 'dimension' | 'overall' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy' {
  const k = String(key || '').toLowerCase()
  if (k.includes('moral') || k.includes('stage') || k.includes('argument') || k.includes('foundation')) return 'dimension_moral'
  if (k.includes('attitude') || k.includes('emotion') || k.includes('resilience') || k.includes('flow')) return 'dimension_attitude'
  if (k.includes('ability') || k.includes('bloom') || k.includes('metacognition') || k.includes('transfer')) return 'dimension_ability'
  if (k.includes('strategy') || k.includes('diversity') || k.includes('depth') || k.includes('regulation')) return 'dimension_strategy'
  return 'dimension'
}
function escapeHtml(s: string) {
  return String(s)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}
</script>
