<template>
  <div ref="exportRoot" class="p-8" data-export-root="1">
    <PageHeader :title="pageTitle" :subtitle="pageSubtitle">
      <template #actions>
        <div class="flex items-center gap-2">
          <button variant="outline" @click="goBack">{{ t('common.back') || '返回' }}</button>
          <button variant="outline" :disabled="loading" @click="onExportPdf">{{ t('admin.tools.exportPdf') || '导出PDF' }}</button>
          <button variant="outline" :disabled="!studentId" @click="openChatWithStudent">{{ t('shared.chat.open') || '聊天' }}</button>
          <button variant="outline" :disabled="!studentId" @click="openAudit">{{ t('admin.student360.auditAiVoice') || 'AI/口语审计' }}</button>
        </div>
      </template>
    </PageHeader>

    <loading-overlay v-if="loading" class="mt-4" :text="String(t('common.loading') || '加载中…')" />
    <error-state
      v-else-if="error"
      class="mt-4"
      :title="String(t('common.error') || '加载失败')"
      :message="error"
      :actionText="String(t('common.retry') || '重试')"
      @action="reload"
    />

    <div v-else class="mt-4 space-y-8">
      <!-- 顶部摘要 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <card padding="md" tint="info" class="lg:col-span-2">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <div class="text-xs text-subtle mb-2">{{ t('admin.student360.profile') || '学生画像' }}</div>
              <div class="text-2xl font-semibold truncate">{{ displayName }}</div>
              <div class="text-sm text-subtle">{{ student?.email || '-' }}</div>
              <div v-if="student?.studentNo" class="text-xs text-subtle mt-1">No: {{ student.studentNo }}</div>
              <div class="text-xs text-subtle mt-1">Status: {{ student?.status || '-' }}</div>
              <div v-if="resolvedCourseId" class="text-xs text-subtle mt-2">
                {{ t('admin.student360.courseContext') || '课程上下文' }}: #{{ resolvedCourseId }}
              </div>
            </div>
            <div class="flex flex-col gap-2">
              <div class="glass-badge px-3 py-2 justify-between">
                <span class="text-subtle">{{ t('admin.student360.enrolledCourses') || '选课数' }}</span>
                <span class="font-semibold">{{ data?.enrolledCourses ?? 0 }}</span>
              </div>
              <div class="glass-badge px-3 py-2 justify-between">
                <span class="text-subtle">{{ t('admin.student360.avgGrade') || '平均分' }}</span>
                <span class="font-semibold">{{ data?.avgGradePercentage ?? '-' }}</span>
              </div>
              <div class="glass-badge px-3 py-2 justify-between">
                <span class="text-subtle">{{ t('admin.student360.abilityReports') || '报告数' }}</span>
                <span class="font-semibold">{{ data?.abilityReports ?? 0 }}</span>
              </div>
            </div>
          </div>
        </card>

        <card padding="md" tint="accent">
          <div class="text-xs text-subtle mb-2">{{ t('admin.student360.panels') || '面板' }}</div>
          <div class="flex flex-col gap-2">
            <button size="sm" :variant="panel==='grades' ? 'solid' : 'outline'" @click="panel='grades'">{{ t('admin.student360.tabs.grades') || '成绩' }}</button>
            <button size="sm" :variant="panel==='ability' ? 'solid' : 'outline'" @click="panel='ability'">{{ t('admin.student360.tabs.ability') || '雷达与报告' }}</button>
            <button size="sm" :variant="panel==='insights' ? 'solid' : 'outline'" @click="panel='insights'">{{ t('admin.student360.tabs.insights') || '行为洞察' }}</button>
            <button size="sm" :variant="panel==='ai' ? 'solid' : 'outline'" @click="panel='ai'">{{ t('admin.student360.tabs.aiChat') || 'AI问答' }}</button>
            <button size="sm" :variant="panel==='voice' ? 'solid' : 'outline'" @click="panel='voice'">{{ t('admin.student360.tabs.voice') || '口语训练' }}</button>
          </div>
        </card>
      </div>

      <!-- 成绩 -->
      <div v-if="panel==='grades'" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <card padding="md" tint="secondary" class="lg:col-span-2">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.gradesTitle') || '作业成绩' }}</div>
            <div class="flex items-center gap-2">
              <button size="sm" variant="outline" :disabled="gradesLoading" @click="reloadGrades">{{ t('common.refresh') || '刷新' }}</button>
              <button size="sm" variant="outline" :disabled="gradesLoading || grades.length===0" @click="exportGradesCsv">{{ t('admin.tools.exportCsv') || '导出CSV' }}</button>
            </div>
          </div>
          <loading-overlay v-if="gradesLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="gradesError" :title="String(t('common.error') || '加载失败')" :message="gradesError" :actionText="String(t('common.retry') || '重试')" @action="reloadGrades" />

          <div v-else>
            <glass-table>
              <template #head>
                <tr>
                  <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('common.columns.id') || 'ID' }}</th>
                  <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.student360.assignment') || '作业' }}</th>
                  <th class="px-6 py-3 text-center text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide whitespace-nowrap">{{ t('admin.student360.score') || '分数' }}</th>
                  <th class="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-200 tracking-wide">{{ t('admin.student360.feedback') || '评语' }}</th>
                </tr>
              </template>
              <template #body>
                <tr v-for="g in grades" :key="g.id" class="hover:bg-white/10 transition-colors duration-150">
                  <td class="px-6 py-3 text-center font-mono text-xs">{{ g.id }}</td>
                  <td class="px-6 py-3 text-sm">
                    <div class="font-medium">{{ g.assignmentTitle || `#${g.assignmentId}` }}</div>
                    <div class="text-xs text-subtle">{{ g.courseTitle || '' }}</div>
                  </td>
                  <td class="px-6 py-3 text-sm text-center">{{ g.score ?? '-' }} / {{ g.maxScore ?? '-' }}</td>
                  <td class="px-6 py-3 text-xs text-subtle line-clamp-2">{{ g.feedback || '-' }}</td>
                </tr>
                <tr v-if="grades.length === 0">
                  <td colspan="4" class="px-6 py-6">
                    <empty-state :title="String(t('common.empty') || '暂无数据')" />
                  </td>
                </tr>
              </template>
            </glass-table>
          </div>

          <pagination-bar
            class="mt-2"
            :page="gradesPage"
            :page-size="gradesPageSize"
            :total-items="gradesTotal"
            :total-pages="gradesTotalPages"
            :disabled="gradesLoading"
            @update:page="(v: number) => { gradesPage = v; reloadGrades() }"
            @update:pageSize="(v: number) => { gradesPageSize = v; gradesPage = 1; reloadGrades() }"
          />
        </card>

        <card padding="md" tint="warning">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.trendTitle') || '成绩趋势' }}</div>
            <button size="sm" variant="outline" :disabled="trendLoading" @click="reloadTrend">{{ t('common.refresh') || '刷新' }}</button>
          </div>
          <trend-area-chart :series="trendSeries" height="320px" :loading="trendLoading" />
        </card>
      </div>

      <!-- 雷达与报告 -->
      <div v-if="panel==='ability'" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <card padding="md" tint="secondary" class="lg:col-span-2">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.radarTitle') || '五维能力雷达' }}</div>
            <div class="flex items-center gap-2">
              <button size="sm" variant="outline" :disabled="radarLoading" @click="reloadRadar">{{ t('common.refresh') || '刷新' }}</button>
              <button size="sm" variant="outline" :disabled="!radarOk" @click="exportRadarCsv">{{ t('admin.tools.exportCsv') || '导出CSV' }}</button>
            </div>
          </div>
          <div v-if="!resolvedCourseId" class="text-sm text-subtle">
            {{ t('admin.student360.needCourseContext') || '提示：雷达图需要课程上下文。请从课程详情页进入，或在地址中携带 courseId。' }}
          </div>
          <loading-overlay v-else-if="radarLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="radarError" :title="String(t('common.error') || '加载失败')" :message="radarError" :actionText="String(t('common.retry') || '重试')" @action="reloadRadar" />
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <radar-chart
              v-if="radarOk"
              ref="radarRef"
              :indicators="radarIndicators"
              :series="radarSeries"
              height="420px"
              :show-legend="true"
            />
            <ability-radar-legend :dimensions="legendDimensions" class="md:pt-3" />
          </div>
        </card>

        <card padding="md" tint="accent">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.reportsTitle') || '能力报告' }}</div>
            <button size="sm" variant="outline" :disabled="reportsLoading" @click="reloadReports">{{ t('common.refresh') || '刷新' }}</button>
          </div>
          <loading-overlay v-if="reportsLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="reportsError" :title="String(t('common.error') || '加载失败')" :message="reportsError" :actionText="String(t('common.retry') || '重试')" @action="reloadReports" />
          <div v-else class="space-y-2">
            <div
              v-for="r in reports"
              :key="r.id"
              class="p-3 rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 transition-colors cursor-pointer"
              @click="openReportDetail(Number(r.id))"
            >
              <div class="font-medium line-clamp-1">{{ r.title }}</div>
              <div class="text-xs text-subtle">{{ r.reportType }} · {{ r.createdAt || '-' }}</div>
            </div>
            <empty-state v-if="reports.length === 0" :title="String(t('common.empty') || '暂无数据')" />
          </div>
          <div class="mt-3">
            <button size="sm" variant="outline" :disabled="reports.length===0" @click="exportReportsCsv">{{ t('admin.tools.exportCsv') || '导出CSV' }}</button>
          </div>
        </card>
      </div>

      <glass-modal
        v-if="showReportDetail"
        :title="String(t('admin.student360.reportsTitle') || '能力报告')"
        size="lg"
        heightVariant="tall"
        @close="closeReportDetail"
      >
        <loading-overlay v-if="reportDetailLoading" :text="String(t('common.loading') || '加载中…')" />
        <error-state
          v-else-if="reportDetailError"
          :title="String(t('common.error') || '加载失败')"
          :message="reportDetailError"
          :actionText="String(t('common.retry') || '重试')"
          @action="() => selectedReportId && openReportDetail(selectedReportId)"
        />
        <ability-report-viewer v-else-if="reportDetail" :report="reportDetail" />
        <empty-state v-else :title="String(t('common.empty') || '暂无数据')" />
      </glass-modal>

      <!-- 行为洞察 -->
      <card v-if="panel==='insights'" padding="md" tint="secondary">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm font-medium">{{ t('admin.student360.insightsTitle') || 'AI行为洞察' }}</div>
          <div class="flex items-center gap-2">
            <button size="sm" variant="outline" :disabled="insightLoading" @click="reloadInsight">{{ t('common.refresh') || '刷新' }}</button>
            <button size="sm" variant="outline" :disabled="insightLoading" @click="generateInsight">{{ t('admin.student360.generate') || '生成' }}</button>
          </div>
        </div>
        <loading-overlay v-if="insightLoading" :text="String(t('common.loading') || '加载中…')" />
        <error-state v-else-if="insightError" :title="String(t('common.error') || '加载失败')" :message="insightError" :actionText="String(t('common.retry') || '重试')" @action="reloadInsight" />
        <div v-else class="space-y-4">
          <div class="text-xs text-subtle">{{ insight?.meta?.generatedAt || '' }}</div>
          <div v-if="Array.isArray(insight?.riskAlerts) && insight.riskAlerts.length" class="space-y-2">
            <div class="text-sm font-medium">{{ t('admin.student360.risks') || '风险预警' }}</div>
            <div v-for="(a, idx) in insight.riskAlerts" :key="idx" class="p-3 rounded-xl border border-white/10 bg-white/5">
              <div class="font-medium">{{ a.title || '-' }}</div>
              <div class="text-xs text-subtle">{{ a.message || '' }}</div>
            </div>
          </div>
          <div v-if="Array.isArray(insight?.actionRecommendations) && insight.actionRecommendations.length" class="space-y-2">
            <div class="text-sm font-medium">{{ t('admin.student360.recommendations') || '行动建议' }}</div>
            <div v-for="(a, idx) in insight.actionRecommendations" :key="idx" class="p-3 rounded-xl border border-white/10 bg-white/5">
              <div class="font-medium">{{ a.title || '-' }}</div>
              <div class="text-xs text-subtle">{{ a.description || '' }}</div>
              <div v-if="Array.isArray(a.nextActions) && a.nextActions.length" class="mt-2 text-xs">
                <div v-for="(s, i) in a.nextActions" :key="i" class="text-subtle">- {{ s }}</div>
              </div>
            </div>
          </div>
          <empty-state v-if="!insight" :title="String(t('common.empty') || '暂无数据')" />
        </div>
      </card>

      <!-- AI 问答审计 -->
      <div v-if="panel==='ai'" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <card padding="md" tint="secondary">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.aiConversations') || '会话列表' }}</div>
            <button size="sm" variant="outline" :disabled="aiLoading" @click="reloadAi">{{ t('common.refresh') || '刷新' }}</button>
          </div>
          <loading-overlay v-if="aiLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="aiError" :title="String(t('common.error') || '加载失败')" :message="aiError" :actionText="String(t('common.retry') || '重试')" @action="reloadAi" />
          <div v-else class="space-y-2">
            <button
              v-for="c in aiConversations"
              :key="c.id"
              class="w-full text-left p-3 rounded-xl border border-white/10 hover:bg-white/5"
              :class="selectedConversationId === c.id ? 'bg-white/10' : 'bg-white/0'"
              @click="selectConversation(c.id)"
            >
              <div class="font-medium line-clamp-1">{{ c.title || `#${c.id}` }}</div>
              <div class="text-xs text-subtle">{{ c.model || '-' }} · {{ c.lastMessageAt || '' }}</div>
            </button>
            <empty-state v-if="aiConversations.length === 0" :title="String(t('common.empty') || '暂无数据')" />
          </div>
          <div class="mt-3 flex gap-2">
            <button size="sm" variant="outline" :disabled="aiConversations.length===0" @click="exportAiCsv">{{ t('admin.tools.exportCsv') || '导出CSV' }}</button>
            <button size="sm" variant="outline" :disabled="!selectedConversationId" @click="exportConversationZip">{{ t('admin.tools.exportZip') || '导出ZIP' }}</button>
          </div>
        </card>

        <card padding="md" tint="secondary" class="lg:col-span-2">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.aiMessages') || '消息' }}</div>
            <button size="sm" variant="outline" :disabled="aiMsgLoading || !selectedConversationId" @click="reloadMessages">{{ t('common.refresh') || '刷新' }}</button>
          </div>
          <loading-overlay v-if="aiMsgLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="aiMsgError" :title="String(t('common.error') || '加载失败')" :message="aiMsgError" :actionText="String(t('common.retry') || '重试')" @action="reloadMessages" />
          <div v-else class="space-y-3">
            <div v-for="m in aiMessages" :key="m.id" class="p-3 rounded-xl border border-white/10 bg-white/5">
              <div class="text-xs text-subtle mb-1">{{ m.role }} · {{ m.createdAt || '' }}</div>
              <div class="text-sm whitespace-pre-wrap">{{ m.content || '' }}</div>
            </div>
            <empty-state v-if="!selectedConversationId" :title="String(t('admin.student360.selectConversation') || '请选择会话')" />
            <empty-state v-else-if="aiMessages.length === 0" :title="String(t('common.empty') || '暂无数据')" />
          </div>
        </card>
      </div>

      <!-- 口语训练 -->
      <div v-if="panel==='voice'" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <card padding="md" tint="secondary">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.voiceSessions') || '会话列表' }}</div>
            <button size="sm" variant="outline" :disabled="voiceLoading" @click="reloadVoice">{{ t('common.refresh') || '刷新' }}</button>
          </div>
          <loading-overlay v-if="voiceLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="voiceError" :title="String(t('common.error') || '加载失败')" :message="voiceError" :actionText="String(t('common.retry') || '重试')" @action="reloadVoice" />
          <div v-else class="space-y-2">
            <button
              v-for="s in voiceSessions"
              :key="s.id"
              class="w-full text-left p-3 rounded-xl border border-white/10 hover:bg-white/5"
              :class="selectedVoiceSessionId === s.id ? 'bg-white/10' : 'bg-white/0'"
              @click="selectVoiceSession(s.id)"
            >
              <div class="font-medium line-clamp-1">{{ s.title || `#${s.id}` }}</div>
              <div class="text-xs text-subtle">{{ s.model || '-' }} · {{ s.createdAt || '' }}</div>
            </button>
            <empty-state v-if="voiceSessions.length === 0" :title="String(t('common.empty') || '暂无数据')" />
          </div>
          <div class="mt-3 flex gap-2">
            <button size="sm" variant="outline" :disabled="voiceSessions.length===0" @click="exportVoiceCsv">{{ t('admin.tools.exportCsv') || '导出CSV' }}</button>
            <button size="sm" variant="outline" :disabled="!selectedVoiceSessionId" @click="exportVoiceZip">{{ t('admin.tools.exportZip') || '导出ZIP' }}</button>
          </div>
        </card>

        <card padding="md" tint="secondary" class="lg:col-span-2">
          <div class="flex items-center justify-between mb-3">
            <div class="text-sm font-medium">{{ t('admin.student360.voiceTurns') || '回合' }}</div>
            <button size="sm" variant="outline" :disabled="voiceTurnsLoading || !selectedVoiceSessionId" @click="reloadVoiceTurns">{{ t('common.refresh') || '刷新' }}</button>
          </div>
          <loading-overlay v-if="voiceTurnsLoading" :text="String(t('common.loading') || '加载中…')" />
          <error-state v-else-if="voiceTurnsError" :title="String(t('common.error') || '加载失败')" :message="voiceTurnsError" :actionText="String(t('common.retry') || '重试')" @action="reloadVoiceTurns" />
          <div v-else class="space-y-3">
            <div v-for="t0 in voiceTurns" :key="t0.id" class="p-3 rounded-xl border border-white/10 bg-white/5">
              <div class="text-xs text-subtle mb-1">{{ t0.createdAt || '' }}</div>
              <div class="text-sm">
                <div class="font-medium">{{ t('admin.student360.voiceUser') || '学生' }}</div>
                <div class="text-xs text-subtle whitespace-pre-wrap">{{ t0.userTranscript || '-' }}</div>
              </div>
              <div class="mt-2 text-sm">
                <div class="font-medium">{{ t('admin.student360.voiceAssistant') || 'AI' }}</div>
                <div class="text-xs text-subtle whitespace-pre-wrap">{{ t0.assistantText || '-' }}</div>
              </div>
            </div>
            <empty-state v-if="!selectedVoiceSessionId" :title="String(t('admin.student360.selectVoiceSession') || '请选择会话')" />
            <empty-state v-else-if="voiceTurns.length === 0" :title="String(t('common.empty') || '暂无数据')" />
          </div>
        </card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import ErrorState from '@/components/ui/ErrorState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import PaginationBar from '@/components/ui/PaginationBar.vue'
import GlassTable from '@/components/ui/tables/GlassTable.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import { adminApi } from '@/api/admin.api'
import { gradeApi } from '@/api/grade.api'
import TrendAreaChart from '@/components/charts/TrendAreaChart.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import AbilityRadarLegend from '@/shared/views/AbilityRadarLegend.vue'
import AbilityReportViewer from '@/shared/views/AbilityReportViewer.vue'
import { exportNodeAsPdf } from '@/shared/utils/exporters'
import { downloadCsv, downloadBlobAsFile } from '@/utils/download'
import JSZip from 'jszip'
import { buildFileUrl, fileApi } from '@/api/file.api'
import { useChatStore } from '@/stores/chat'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const chat = useChatStore()
const studentId = computed(() => String((route.params as any).studentId || route.params.id || ''))
const resolvedCourseId = computed(() => String((route.params as any).courseId || (route.query as any)?.courseId || ''))

const loading = ref(false)
const error = ref<string | null>(null)
const data = ref<any>(null)
const exportRoot = ref<HTMLElement | null>(null)

const student = computed(() => data.value?.student || null)
const displayName = computed(() => student.value?.nickname || student.value?.username || `#${studentId.value}`)
const pageTitle = computed(() => t('admin.sidebar.students') || '学生')
const pageSubtitle = computed(() => `#${studentId.value}`)

const panel = ref<'grades' | 'ability' | 'insights' | 'ai' | 'voice'>('grades')

async function reload() {
  loading.value = true
  error.value = null
  try {
    data.value = await adminApi.getStudentDetail(studentId.value)
  } catch (e: any) {
    error.value = e?.message || 'Failed to load'
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (resolvedCourseId.value) {
    router.push(`/admin/courses/${resolvedCourseId.value}`)
    return
  }
  router.push('/admin/people?tab=students')
}

async function onExportPdf() {
  if (!exportRoot.value) return
  await exportNodeAsPdf(exportRoot.value, `admin_student_${studentId.value}`)
}

/**
 * 打开与当前学生的一对一聊天抽屉（管理员视角）。
 */
function openChatWithStudent() {
  const id = String(studentId.value || '')
  if (!id) return
  chat.openChat(id, displayName.value || id, resolvedCourseId.value || null)
}

function openAudit() {
  const id = String(studentId.value || '')
  if (!id) return
  router.push({ path: `/admin/audit/${id}`, query: { name: displayName.value || undefined } })
}

// grades list
const gradesLoading = ref(false)
const gradesError = ref<string | null>(null)
const grades = ref<any[]>([])
const gradesPage = ref(1)
const gradesPageSize = ref(10)
const gradesTotal = ref(0)
const gradesTotalPages = ref(1)

async function reloadGrades() {
  gradesLoading.value = true
  gradesError.value = null
  try {
    const res: any = await gradeApi.getGradesByStudent(studentId.value, {
      page: gradesPage.value,
      size: gradesPageSize.value,
      courseId: resolvedCourseId.value || undefined
    })
    const pageRes = (res as any) || {}
    grades.value = pageRes?.items || []
    gradesTotal.value = Number(pageRes?.total || 0)
    gradesTotalPages.value = Number(pageRes?.totalPages || 1)
  } catch (e: any) {
    gradesError.value = e?.message || 'Failed to load'
  } finally {
    gradesLoading.value = false
  }
}

function exportGradesCsv() {
  const header = ['gradeId', 'assignmentId', 'assignmentTitle', 'courseId', 'courseTitle', 'score', 'maxScore', 'percentage', 'feedback', 'status', 'gradedAt']
  const rows = grades.value.map((g: any) => ([
    g?.id ?? '',
    g?.assignmentId ?? '',
    JSON.stringify(String(g?.assignmentTitle ?? '')),
    g?.courseId ?? '',
    JSON.stringify(String(g?.courseTitle ?? '')),
    g?.score ?? '',
    g?.maxScore ?? '',
    g?.percentage ?? '',
    JSON.stringify(String(g?.feedback ?? '')),
    g?.status ?? '',
    g?.gradedAt ?? '',
  ]).join(','))
  downloadCsv(header.join(',') + '\n' + rows.join('\n'), `student_${studentId.value}_grades.csv`)
}

// trend
const trendLoading = ref(false)
const trendData = ref<any[]>([])
const trendSeries = computed(() => ([
  {
    name: t('admin.student360.score') || '分数',
    data: (trendData.value || []).map((p: any) => ({ x: p?.date || p?.x || '', y: Number(p?.value ?? p?.y ?? 0) }))
  }
]))

async function reloadTrend() {
  trendLoading.value = true
  try {
    trendData.value = await gradeApi.getGradeTrend(studentId.value, { courseId: resolvedCourseId.value || undefined, days: 30 }) as any
  } finally {
    trendLoading.value = false
  }
}

// radar
const radarLoading = ref(false)
const radarError = ref<string | null>(null)
const radar = ref<any>(null)
const radarRef = ref<any>(null)
const radarOk = computed(() => !!radar.value && Array.isArray(radar.value?.dimensions) && Array.isArray(radar.value?.studentScores))
const radarIndicators = computed(() => (radar.value?.dimensions || []).map((d: any) => ({ name: String(d), max: 100 })))
const radarSeries = computed(() => ([
  { name: t('admin.student360.student') || '学生', values: (radar.value?.studentScores || []).map((n: any) => Number(n || 0)) },
  { name: t('admin.student360.classAvg') || '均值', values: (radar.value?.classAvgScores || []).map((n: any) => Number(n || 0)), color: 'var(--color-info)' }
]))

async function reloadRadar() {
  if (!resolvedCourseId.value) return
  radarLoading.value = true
  radarError.value = null
  try {
    radar.value = await adminApi.getAbilityRadar({ studentId: studentId.value, courseId: resolvedCourseId.value })
  } catch (e: any) {
    radarError.value = e?.message || 'Failed to load'
  } finally {
    radarLoading.value = false
  }
}

const legendDimensions = computed(() => {
  const dims = radar.value?.dimensions
  if (Array.isArray(dims) && dims.length) return dims.map((x: any) => String(x))
  // 兜底：当雷达尚未加载时，仍提供默认维度避免 Legend 报错
  return ['道德认知', '学习态度', '学习能力', '学习方法', '学习成绩']
})

function exportRadarCsv() {
  if (!radarOk.value) return
  const dims = radar.value?.dimensions || []
  const s = radar.value?.studentScores || []
  const c = radar.value?.classAvgScores || []
  const header = ['dimension', 'student', 'classAvg']
  const rows = dims.map((d: any, idx: number) => [JSON.stringify(String(d)), s[idx] ?? '', c[idx] ?? ''].join(','))
  downloadCsv(header.join(',') + '\n' + rows.join('\n'), `student_${studentId.value}_radar_course_${resolvedCourseId.value}.csv`)
}

// reports list
const reportsLoading = ref(false)
const reportsError = ref<string | null>(null)
const reports = ref<any[]>([])

// report detail modal
const showReportDetail = ref(false)
const selectedReportId = ref<number | null>(null)
const reportDetailLoading = ref(false)
const reportDetailError = ref<string | null>(null)
const reportDetail = ref<any | null>(null)

async function reloadReports() {
  reportsLoading.value = true
  reportsError.value = null
  try {
    const res: any = await adminApi.pageAbilityReports({
      page: 1,
      size: 10,
      studentId: Number(studentId.value),
      courseId: resolvedCourseId.value ? Number(resolvedCourseId.value) : undefined,
    })
    reports.value = res?.items || []
  } catch (e: any) {
    reportsError.value = e?.message || 'Failed to load'
  } finally {
    reportsLoading.value = false
  }
}

async function openReportDetail(id: number) {
  showReportDetail.value = true
  selectedReportId.value = id
  reportDetailLoading.value = true
  reportDetailError.value = null
  reportDetail.value = null
  try {
    reportDetail.value = await adminApi.getAbilityReport(id)
  } catch (e: any) {
    reportDetailError.value = e?.message || String(t('common.error') || '加载失败')
  } finally {
    reportDetailLoading.value = false
  }
}

function closeReportDetail() {
  showReportDetail.value = false
  selectedReportId.value = null
  reportDetailLoading.value = false
  reportDetailError.value = null
  reportDetail.value = null
}

async function exportReportsCsv() {
  try {
    const blob = await adminApi.exportAbilityReportsCsv({
      studentId: Number(studentId.value),
      courseId: resolvedCourseId.value ? Number(resolvedCourseId.value) : undefined,
    })
    downloadCsv(blob as any, `student_${studentId.value}_ability_reports.csv`)
  } catch (e: any) {
    console.warn(e)
  }
}

// insight
const insightLoading = ref(false)
const insightError = ref<string | null>(null)
const insight = ref<any>(null)

async function reloadInsight() {
  insightLoading.value = true
  insightError.value = null
  try {
    insight.value = await adminApi.getBehaviorInsightLatest({
      studentId: studentId.value,
      courseId: resolvedCourseId.value || undefined,
      range: '7d'
    })
  } catch (e: any) {
    insightError.value = e?.message || 'Failed to load'
  } finally {
    insightLoading.value = false
  }
}

async function generateInsight() {
  insightLoading.value = true
  insightError.value = null
  try {
    insight.value = await adminApi.generateBehaviorInsight({
      studentId: studentId.value,
      courseId: resolvedCourseId.value || undefined,
      range: '7d',
      force: false
    })
  } catch (e: any) {
    insightError.value = e?.message || 'Failed to generate'
  } finally {
    insightLoading.value = false
  }
}

// AI conversations
const aiLoading = ref(false)
const aiError = ref<string | null>(null)
const aiConversations = ref<any[]>([])
const selectedConversationId = ref<number | null>(null)

const aiMsgLoading = ref(false)
const aiMsgError = ref<string | null>(null)
const aiMessages = ref<any[]>([])

async function reloadAi() {
  aiLoading.value = true
  aiError.value = null
  try {
    const res: any = await adminApi.listAiConversations({ studentId: studentId.value, page: 1, size: 50 })
    aiConversations.value = res?.items || []
  } catch (e: any) {
    aiError.value = e?.message || 'Failed to load'
  } finally {
    aiLoading.value = false
  }
}

async function selectConversation(id: number) {
  selectedConversationId.value = id
  await reloadMessages()
}

async function reloadMessages() {
  if (!selectedConversationId.value) return
  aiMsgLoading.value = true
  aiMsgError.value = null
  try {
    const res: any = await adminApi.listAiMessages(selectedConversationId.value, { studentId: studentId.value, page: 1, size: 100 })
    aiMessages.value = res?.items || []
  } catch (e: any) {
    aiMsgError.value = e?.message || 'Failed to load'
  } finally {
    aiMsgLoading.value = false
  }
}

async function exportAiCsv() {
  try {
    const blob = await adminApi.exportAiConversationsCsv({ studentId: studentId.value })
    downloadCsv(blob as any, `student_${studentId.value}_ai_conversations.csv`)
  } catch (e: any) {
    console.warn(e)
  }
}

async function exportConversationZip() {
  if (!selectedConversationId.value) return
  const zip = new JSZip()
  zip.file('conversation.json', JSON.stringify({ conversationId: selectedConversationId.value, studentId: studentId.value }, null, 2))
  zip.file('messages.json', JSON.stringify(aiMessages.value || [], null, 2))
  const md = (aiMessages.value || []).map((m: any) => `### ${m.role}\n\n${m.content || ''}\n`).join('\n')
  zip.file('messages.md', md)

  // 尝试打包附件（若存在）
  const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
  const parseIds = (v: any): number[] => {
    if (!v) return []
    if (Array.isArray(v)) return v.map((x) => Number(x)).filter((n) => Number.isFinite(n))
    if (typeof v === 'string') {
      const s = v.trim()
      if (!s) return []
      try {
        const j = JSON.parse(s)
        if (Array.isArray(j)) return j.map((x) => Number(x)).filter((n) => Number.isFinite(n))
      } catch {}
      return s.split(',').map((x) => Number(String(x).trim())).filter((n) => Number.isFinite(n))
    }
    return []
  }
  const attachmentIds = Array.from(new Set((aiMessages.value || []).flatMap((m: any) => parseIds(m.attachments))))
  const limited = attachmentIds.slice(0, 50) // 保护：避免一次性打包过大
  for (const fid of limited) {
    try {
      let name = `file_${fid}`
      try {
        const info: any = await fileApi.getFileInfo(String(fid))
        const original = info?.originalName || info?.storedName
        if (original) name = String(original)
      } catch {}
      const url = buildFileUrl(`/files/${encodeURIComponent(String(fid))}/download`)
      const res = await fetch(url, {
        method: 'GET',
        headers: {
          Accept: 'application/octet-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: 'include',
      })
      if (!res.ok) continue
      const blob = await res.blob()
      zip.file(`attachments/${fid}_${name}`, blob)
    } catch {
      // 忽略单个附件失败
    }
  }

  const blob = await zip.generateAsync({ type: 'blob' })
  downloadBlobAsFile(blob, `student_${studentId.value}_conversation_${selectedConversationId.value}.zip`)
}

// Voice sessions
const voiceLoading = ref(false)
const voiceError = ref<string | null>(null)
const voiceSessions = ref<any[]>([])
const selectedVoiceSessionId = ref<number | null>(null)

const voiceTurnsLoading = ref(false)
const voiceTurnsError = ref<string | null>(null)
const voiceTurns = ref<any[]>([])

async function reloadVoice() {
  voiceLoading.value = true
  voiceError.value = null
  try {
    voiceSessions.value = await adminApi.listVoiceSessions({ studentId: studentId.value, page: 1, size: 50 }) as any
  } catch (e: any) {
    voiceError.value = e?.message || 'Failed to load'
  } finally {
    voiceLoading.value = false
  }
}

async function selectVoiceSession(id: number) {
  selectedVoiceSessionId.value = id
  await reloadVoiceTurns()
}

async function reloadVoiceTurns() {
  if (!selectedVoiceSessionId.value) return
  voiceTurnsLoading.value = true
  voiceTurnsError.value = null
  try {
    voiceTurns.value = await adminApi.listVoiceTurns(selectedVoiceSessionId.value, { studentId: studentId.value, page: 1, size: 200 }) as any
  } catch (e: any) {
    voiceTurnsError.value = e?.message || 'Failed to load'
  } finally {
    voiceTurnsLoading.value = false
  }
}

async function exportVoiceCsv() {
  try {
    const blob = await adminApi.exportVoiceSessionsCsv({ studentId: studentId.value })
    downloadCsv(blob as any, `student_${studentId.value}_voice_sessions.csv`)
  } catch (e: any) {
    console.warn(e)
  }
}

async function exportVoiceZip() {
  if (!selectedVoiceSessionId.value) return
  const zip = new JSZip()
  zip.file('session.json', JSON.stringify({ sessionId: selectedVoiceSessionId.value, studentId: studentId.value }, null, 2))
  zip.file('turns.json', JSON.stringify(voiceTurns.value || [], null, 2))

  // 尝试打包语音附件（学生录音/AI 回复音频）
  const token = (() => { try { return localStorage.getItem('token') } catch { return null } })()
  const audioIds = Array.from(new Set((voiceTurns.value || []).flatMap((t0: any) => [t0?.userAudioFileId, t0?.assistantAudioFileId]).filter(Boolean)))
  const limited = audioIds.slice(0, 50)
  for (const fid of limited) {
    try {
      let name = `audio_${fid}`
      try {
        const info: any = await fileApi.getFileInfo(String(fid))
        const original = info?.originalName || info?.storedName
        if (original) name = String(original)
      } catch {}
      const url = buildFileUrl(`/files/${encodeURIComponent(String(fid))}/download`)
      const res = await fetch(url, {
        method: 'GET',
        headers: {
          Accept: 'application/octet-stream',
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        credentials: 'include',
      })
      if (!res.ok) continue
      const blob = await res.blob()
      zip.file(`audio/${fid}_${name}`, blob)
    } catch {}
  }

  const blob = await zip.generateAsync({ type: 'blob' })
  downloadBlobAsFile(blob, `student_${studentId.value}_voice_${selectedVoiceSessionId.value}.zip`)
}

watch(panel, (p) => {
  if (p === 'grades') {
    if (!gradesLoading.value && grades.value.length === 0) reloadGrades()
    if (!trendLoading.value && trendData.value.length === 0) reloadTrend()
  }
  if (p === 'ability') {
    if (!reportsLoading.value && reports.value.length === 0) reloadReports()
    if (resolvedCourseId.value && !radarLoading.value && !radar.value) reloadRadar()
  }
  if (p === 'insights') {
    if (!insightLoading.value && !insight.value) reloadInsight()
  }
  if (p === 'ai') {
    if (!aiLoading.value && aiConversations.value.length === 0) reloadAi()
  }
  if (p === 'voice') {
    if (!voiceLoading.value && voiceSessions.value.length === 0) reloadVoice()
  }
})

onMounted(async () => {
  await reload()
  await reloadGrades()
  await reloadTrend()
})
</script>

