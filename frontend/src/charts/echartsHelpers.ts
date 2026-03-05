import { echarts, type ECharts } from '@/charts/echartsCore'

/**
 * ECharts 通用工具（教师端多页面复用）
 * - 避免重复 init/clear
 * - 统一 tooltip 残留隐藏策略（showTip/hideTip/globalout）
 * - 统一全局主题切换事件绑定
 */

export function getOrInitChart(el: HTMLElement | undefined | null, theme?: any): ECharts | null {
  if (!el) return null
  let inst = echarts.getInstanceByDom(el as any)
  if (!inst) {
    inst = echarts.init(el as HTMLDivElement, theme as any)
  } else {
    try { inst.clear() } catch {}
  }
  return inst
}

function tooltipSelector(scopeClass?: string): string {
  if (!scopeClass) return '.echarts-tooltip, .echarts-glass-tooltip'
  const normalized = String(scopeClass)
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .join('.')
  return normalized ? `.echarts-glass-tooltip.${normalized}` : '.echarts-tooltip, .echarts-glass-tooltip'
}

function tooltipElements(scopeClass?: string): HTMLElement[] {
  return Array.from(document.querySelectorAll(tooltipSelector(scopeClass))) as HTMLElement[]
}

function allTooltipElements(): HTMLElement[] {
  return Array.from(document.querySelectorAll('.echarts-tooltip, .echarts-glass-tooltip')) as HTMLElement[]
}

export function setTooltipVisible(visible: boolean, scopeClass?: string) {
  const v = visible ? 'visible' : 'hidden'
  const scoped = tooltipElements(scopeClass)
  if (visible && scopeClass) {
    const scopedSet = new Set(scoped)
    allTooltipElements().forEach(el => {
      if (!scopedSet.has(el)) {
        el.style.visibility = 'hidden'
      }
    })
  }
  scoped.forEach(el => {
    el.style.visibility = v
    if (visible) el.style.opacity = '1'
  })
}

export function bindTooltipVisibility(inst: ECharts | null, scopeClass?: string) {
  if (!inst) return
  try { (inst as any).off && (inst as any).off('showTip') } catch {}
  try { (inst as any).off && (inst as any).off('hideTip') } catch {}

  // 初始化默认隐藏：避免左上角出现空白 tooltip 容器
  try { setTooltipVisible(false, scopeClass) } catch {}

  try {
    inst.on('showTip', () => setTooltipVisible(true, scopeClass))
    inst.on('hideTip', () => setTooltipVisible(false, scopeClass))
  } catch {}
}

export function bindHideTipOnGlobalOut(inst: ECharts | null, scopeClass?: string) {
  if (!inst) return
  try { (inst as any).off && (inst as any).off('globalout') } catch {}
  try {
    inst.on('globalout', () => {
      try { inst.dispatchAction({ type: 'hideTip' } as any) } catch {}
      try { setTooltipVisible(false, scopeClass) } catch {}
    })
  } catch {}
}

export function bindThemeChangeEvents(handlers: { onChanging?: () => void; onChanged: () => void }) {
  const onChanging = handlers.onChanging || (() => {})
  const onChanged = handlers.onChanged
  try { window.addEventListener('theme:changing', onChanging as any) } catch {}
  try { window.addEventListener('theme:changed', onChanged as any) } catch {}
  return {
    disconnect() {
      try { window.removeEventListener('theme:changing', onChanging as any) } catch {}
      try { window.removeEventListener('theme:changed', onChanged as any) } catch {}
    }
  }
}
