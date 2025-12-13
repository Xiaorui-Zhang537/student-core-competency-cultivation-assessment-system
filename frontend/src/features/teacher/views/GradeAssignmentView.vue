<template>
  <div class="min-h-screen p-6">
    <div class="max-w-7xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-8">
        <nav class="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400 mb-2">
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/courses')">{{ t('teacher.courses.breadcrumb') }}</span>
          <chevron-right-icon class="w-4 h-4" />
          <span v-if="assignment.courseName || assignmentCourseId" class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="goCourse()">{{ assignment.courseName || `#${assignmentCourseId}` }}</span>
          <chevron-right-icon class="w-4 h-4" />
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push('/teacher/assignments')">{{ t('teacher.submissions.breadcrumb.assignments') }}</span>
          <chevron-right-icon class="w-4 h-4" />
          <span class="hover:text-gray-700 dark:hover:text-gray-200 cursor-pointer" @click="router.push(`/teacher/assignments/${assignment.id}/submissions`)">{{ t('teacher.submissions.breadcrumb.self') }}</span>
          <chevron-right-icon class="w-4 h-4" />
          <span>{{ submission.studentName || submission.studentId }}</span>
        </nav>
        <page-header :title="t('teacher.grading.title', { name: submission.studentName || submission.studentId })" :subtitle="pageSubtitle">
          <template #actions>
            <badge :variant="getSubmissionStatusVariant(submission.status)">
              {{ getSubmissionStatusText(submission.status) }}
            </badge>
          </template>
        </page-header>
      </div>

      <!-- 加载状态 -->
      <div v-if="isLoading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        <p class="mt-2 text-gray-600 dark:text-gray-400">{{ t('teacher.grading.loading') }}</p>
      </div>

      <div v-else class="grid grid-cols-1 xl:grid-cols-3 gap-8">
        <!-- 左侧主要内容 -->
        <div class="xl:col-span-2 space-y-8">
          <!-- 作业信息 -->
          <card padding="lg" tint="info">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.assignment.requirements') }}</h2>
            </template>
            
            <div class="space-y-4">
              <div>
                <h3 class="font-medium text-gray-900 dark:text-white mb-2">{{ assignment.title }}</h3>
                <p class="text-gray-600 dark:text-gray-400 whitespace-pre-line break-words prose-readable">{{ assignment.description }}</p>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.assignment.due') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ formatDate(assignment.dueDate) }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.assignment.totalScore') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white">{{ assignment.totalScore }}{{ t('teacher.grading.history.scoreSuffix') }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.assignment.submitStatus') }}</span>
                  <span class="font-medium" :class="submitStatusClass">
                    {{ submitStatusText }}
                  </span>
                </div>
              </div>
            </div>
          </card>

          <!-- 学生提交内容 -->
          <card padding="lg" tint="accent" v-if="showSubmissionContent">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.submission.content') }}</h2>
            </template>
            
            <div class="space-y-6">
              <!-- 文字内容 -->
              <div v-if="submission.content">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.submission.answer') }}</h3>
                <div class="rounded-xl glass-ultraThin p-4" v-glass="{ strength: 'ultraThin', interactive: false }">
                  <div class="prose max-w-none dark:prose-invert">
                    <p class="whitespace-pre-line">{{ submission.content }}</p>
                  </div>
                </div>
              </div>

              <!-- 附件（单文件） -->
              <div v-if="submission.fileName">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">{{ t('teacher.grading.submission.attachment') }}</h3>
                <div class="flex items-center justify-between p-3 rounded-xl glass-ultraThin border border-white/20 dark:border-white/10" v-glass="{ strength: 'ultraThin', interactive: true }">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <document-icon class="w-6 h-6 text-gray-400" />
                    </div>
                    <div>
                      <p class="text-sm font-medium text-gray-900 dark:text-white">{{ submission.fileName }}</p>
                    </div>
                  </div>
                  <div class="flex items-center space-x-2">
                    <Button variant="ghost" size="sm" @click="previewSingleFile" :disabled="!canPreviewSubmission">
                      <eye-icon class="w-4 h-4" />
                    </Button>
                    <Button variant="ghost" size="sm" @click="downloadSingleFile">
                      <arrow-down-tray-icon class="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </div>

              <!-- 学生自评 -->
              <div v-if="submission.selfEvaluation">
                <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.submission.selfEvaluation') }}</h3>
                <div class="rounded-xl glass-ultraThin p-4" v-glass="{ strength: 'ultraThin', interactive: false }">
                  <p class="text-sm text-gray-700 dark:text-gray-300">{{ submission.selfEvaluation }}</p>
                </div>
              </div>
            </div>
          </card>
          <card padding="lg" tint="accent" v-else>
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.submission.content') }}</h2>
            </template>
            <div class="text-sm text-gray-600 dark:text-gray-300">
              作业已退回，原提交内容不再展示，等待学生重新提交。
            </div>
          </card>

          

          <!-- AI评分建议 -->
          <card padding="lg" tint="warning" v-if="aiSuggestion">
            <template #header>
              <div class="flex items-center justify-between">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.ai.title') }}</h2>
                <div class="flex items-center gap-2">
                  <Button variant="outline" size="sm" @click="applyAiSuggestion">
                    <check-icon class="w-4 h-4 mr-1" />
                    {{ t('teacher.grading.ai.applySuggestion') || '一键应用' }}
                  </Button>
                  
                  <Button v-if="aiSuggestion" variant="indigo" size="sm" @click="viewAiDetail">
                    <eye-icon class="w-4 h-4 mr-1" />
                    {{ t('teacher.grading.ai.viewDetail') || '查看详情' }}
                  </Button>
                  <Button size="sm" variant="primary" class="ml-1" :loading="aiLoading" @click="openAiModal">
                    <sparkles-icon class="w-4 h-4 mr-1" />
                    {{ t('teacher.grading.ai.open') || 'AI 批改' }}
                  </Button>
                </div>
              </div>
            </template>
            
            <div class="space-y-4">
              <div class="flex items-start gap-6">
                <div class="min-w-[160px] text-center mt-6 md:mt-8">
                  <div class="text-xs text-gray-500 mb-1">{{ t('teacher.grading.ai.suggestedScore') }}</div>
                  <div class="text-3xl font-extrabold text-theme-primary">{{ aiSuggestion.suggestedScore }}</div>
                  </div>
                <div class="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div class="space-y-2">
                    <div class="text-xs text-gray-500">{{ t('teacher.aiGrading.render.moral_reasoning') }}</div>
                    <Progress :value="(aiSuggestion.dims?.moral ?? 0) * 20" size="sm" />
                    <div class="text-xs text-right text-gray-500">{{ (aiSuggestion.dims?.moral ?? 0).toFixed(1) }}/5</div>
                </div>
                  <div class="space-y-2">
                    <div class="text-xs text-gray-500">{{ t('teacher.aiGrading.render.attitude_development') }}</div>
                    <Progress :value="(aiSuggestion.dims?.attitude ?? 0) * 20" size="sm" />
                    <div class="text-xs text-right text-gray-500">{{ (aiSuggestion.dims?.attitude ?? 0).toFixed(1) }}/5</div>
                    </div>
                  <div class="space-y-2">
                    <div class="text-xs text-gray-500">{{ t('teacher.aiGrading.render.ability_growth') }}</div>
                    <Progress :value="(aiSuggestion.dims?.ability ?? 0) * 20" size="sm" />
                    <div class="text-xs text-right text-gray-500">{{ (aiSuggestion.dims?.ability ?? 0).toFixed(1) }}/5</div>
                    </div>
                  <div class="space-y-2">
                    <div class="text-xs text-gray-500">{{ t('teacher.aiGrading.render.strategy_optimization') }}</div>
                    <Progress :value="(aiSuggestion.dims?.strategy ?? 0) * 20" size="sm" />
                    <div class="text-xs text-right text-gray-500">{{ (aiSuggestion.dims?.strategy ?? 0).toFixed(1) }}/5</div>
                    </div>
                  </div>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.analysis') }}</h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                  <li v-for="(line, idx) in analysisLines" :key="idx" class="flex items-start">
                    <span class="whitespace-pre-line">{{ line }}</span>
                  </li>
                </ul>
              </div>
              
              <div>
                <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.improvements') }}</h4>
                <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                  <li v-for="suggestion in aiSuggestion.improvements" :key="suggestion" class="flex items-start">
                    <span class="text-theme-primary mr-2">•</span>{{ suggestion }}
                  </li>
                </ul>
              </div>
            </div>
          </card>

          <card padding="lg" tint="secondary" v-else>
            <template #header>
              <div class="flex items-center justify-between">
                <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.ai.title') }}</h2>
                <badge variant="secondary" class="flex items-center">
                  <sparkles-icon class="w-3 h-3 mr-1" />
                  {{ t('teacher.grading.ai.badge') }}
                </badge>
              </div>
            </template>
            <div class="flex items-center justify-between">
              <div class="text-sm text-gray-600 dark:text-gray-300">{{ t('teacher.grading.ai.hint') || '点击下方按钮，基于本次提交自动生成AI评分建议。' }}</div>
              <Button size="sm" variant="primary" :loading="aiLoading" @click="openAiModal">
                <sparkles-icon class="w-4 h-4 mr-1" />
                {{ t('teacher.grading.ai.open') || 'AI 批改' }}
              </Button>
            </div>
          </card>

          <!-- AI 批改弹窗（选择附件或文本 + 模型 + 进度） -->
           <glass-modal v-if="aiModalOpen" :title="t('teacher.aiGrading.picker.title') as string" size="sm" heightVariant="tall" solidBody @close="aiModalOpen=false">
            <div class="space-y-3">
            <div class="flex items-center gap-3 flex-nowrap">
              <div class="flex items-center gap-2">
                <span class="text-xs text-gray-600 dark:text-gray-300 whitespace-nowrap">{{ t('teacher.ai.model.label') || '模型' }}</span>
                  <glass-popover-select v-model="gradingModel" :options="gradingModelOptions" size="sm" />
                </div>
                <segmented-pills
                  class="ml-2"
                  v-model="aiSource"
                  :options="[{ label: '文本', value: 'text' }, { label: '附件', value: 'files' }]"
                  size="sm"
                />
                <div class="flex items-center gap-2 ml-auto">
                  <Button size="sm" variant="primary" :disabled="hasOngoing || !canStartAi" @click="startAiGradingFromModal">
                    <sparkles-icon class="w-4 h-4 mr-1" />{{ t('teacher.aiGrading.start') || '开始批改' }}
                  </Button>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <Card padding="sm" tint="secondary">
                  <div class="text-xs text-gray-600 dark:text-gray-300 mb-2">{{ t('teacher.aiGrading.picker.text') || '文本内容' }}</div>
                  <div v-if="aiPicker.previewText" class="bg-white/40 dark:bg-white/5 border border-white/30 dark:border-white/10 rounded p-2 text-xs max-h-40 overflow-auto no-scrollbar whitespace-pre-wrap">
                    {{ aiPicker.previewText }}
                  </div>
                  <div v-else class="text-xs text-gray-500">{{ t('common.empty') || '无内容' }}</div>
                </Card>
                <Card padding="sm" tint="secondary">
                  <div class="flex items-center justify-between mb-2">
                    <div class="text-xs text-gray-600 dark:text-gray-300">{{ t('teacher.aiGrading.picker.attachments') || '附件' }}</div>
                  </div>
                  <ul class="divide-y divide-gray-200 dark:divide-white/10 max-h-40 overflow-auto no-scrollbar">
                    <li v-for="f in aiPicker.files" :key="String(f.id)" class="py-1 flex items-center justify-between">
                      <label class="flex items-center gap-2 cursor-pointer text-xs">
                        <input type="checkbox" :value="Number(f.id)" v-model="aiPicker.selectedFileIds" :disabled="aiSource!=='files'" />
                        <span :title="f.originalName || f.fileName || f.name">{{ f.originalName || f.fileName || f.name }}</span>
                      </label>
                      <span class="text-[10px] text-gray-500">{{ f.size ? (Math.round(f.size/102.4)/10 + ' KB') : '' }}</span>
                    </li>
                    <li v-if="!aiPicker.files.length" class="py-2 text-xs text-gray-500">{{ t('common.empty') || '无内容' }}</li>
                  </ul>
                </Card>
              </div>

              <Card v-if="aiProgress.items.length" padding="sm" tint="warning">
                <div class="text-xs text-gray-600 dark:text-gray-300 mb-2">{{ t('teacher.aiGrading.progress.label') || '进度' }}</div>
                <ul class="space-y-1 text-xs">
                  <li v-for="it in aiProgress.items" :key="it.id" class="flex items-center justify-between">
                    <div class="flex items-center gap-2 min-w-0">
                      <svg v-if="it.status==='grading'" class="h-3 w-3 text-indigo-500 animate-spin" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                      </svg>
                      <span class="truncate max-w-[60%]" :title="it.name">{{ it.name }}</span>
                    </div>
                    <span class="ml-2" :class="it.status==='error' ? 'text-red-600' : (it.status==='done' ? 'text-green-600' : 'text-gray-600')">{{ t('teacher.aiGrading.status.' + it.status) }}</span>
                  </li>
                </ul>
              </Card>
            </div>
            <template #footer>
              <div class="flex items-center justify-between w-full">
                <span class="text-xs text-gray-500 dark:text-gray-400">{{ t('teacher.aiGrading.picker.hintBackgroundRunning') }}</span>
                <Button size="sm" variant="secondary" @click="aiModalOpen=false">{{ t('teacher.aiGrading.picker.close') || '关闭' }}</Button>
              </div>
            </template>
          </glass-modal>

      

          <!-- 评分历史 -->
          <card padding="lg" tint="info" v-if="gradingHistory.length > 0">
            <template #header>
              <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.history.title') }}</h2>
            </template>
            
            <div class="space-y-4">
              <div v-for="history in gradingHistory" 
                   :key="history.id"
                   class="flex items-start space-x-4 p-4 rounded-xl glass-ultraThin" v-glass="{ strength: 'ultraThin', interactive: false }">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 rounded-full glass-ultraThin flex items-center justify-center" v-glass="{ strength: 'ultraThin', interactive: false }">
                    <academic-cap-icon class="w-4 h-4 text-theme-primary" />
                  </div>
                </div>
                <div class="flex-1">
                  <div class="flex flex-wrap items-center gap-3 mb-1">
                    <span v-if="history.graderName" class="font-medium text-gray-900 dark:text-white">{{ history.graderName }}</span>
                    <span class="text-sm text-gray-500">{{ formatDate(history.gradedAt) }}</span>
                    <badge variant="secondary" size="sm">{{ history.score }}{{ t('teacher.grading.history.scoreSuffix') }}</badge>
                    <badge v-if="history.gradeLevel" variant="primary" size="sm">{{ history.gradeLevel }}</badge>
                    <badge v-if="history.status" :variant="history.status === 'published' ? 'success' : 'secondary'" size="sm">{{ history.status }}</badge>
                  </div>
                  <div class="space-y-2">
                    <p v-if="history.feedback" class="text-sm text-gray-700 dark:text-gray-300">{{ history.feedback }}</p>
                    <div v-if="history.strengths" class="text-sm">
                      <span class="font-medium text-gray-900 dark:text-white mr-2">{{ t('teacher.grading.form.strengths') }}:</span>
                      <span class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ history.strengths }}</span>
                    </div>
                    <div v-if="history.improvements" class="text-sm">
                      <span class="font-medium text-gray-900 dark:text-white mr-2">{{ t('teacher.grading.form.improvements') }}:</span>
                      <span class="text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ history.improvements }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </card>
        </div>

        <!-- 右侧评分面板 -->
        <div class="space-y-6">
          <!-- 评分表单 -->
          <card padding="lg" tint="primary">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.form.title') }}</h3>
            </template>
            
            <form @submit.prevent="submitGrade" class="space-y-6">
              <!-- 分数输入 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ t('teacher.grading.form.score') }} <span class="text-red-500">*</span>
                </label>
                <div class="flex items-center space-x-2">
                  <glass-input
                    v-model="gradeForm.score as any"
                    type="number"
                    :min="0"
                    :max="assignment.totalScore"
                    step="0.5"
                    class="flex-1"
                    :class="{ 'border-red-500': errors.score }"
                    @blur="() => { validateScore(); validateGrade(); }"
                  />
                  <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('teacher.grading.form.ofTotal', { total: assignment.totalScore }) }}</span>
                </div>
                <p v-if="errors.score" class="mt-1 text-sm text-red-600">{{ errors.score }}</p>
                
                <!-- 动画分数展示（玻璃超薄风格） -->
                <div class="mt-4 glass-ultraThin rounded-xl p-4" v-glass="{ strength: 'ultraThin', interactive: true }">
                  <div class="flex items-center justify-between">
                    <div class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ animatedScore.toFixed(1) }}</div>
                    <div class="text-sm text-gray-500 dark:text-gray-400">/ {{ assignment.totalScore }}{{ t('teacher.grading.history.scoreSuffix') }}</div>
                  </div>
                  <div class="mt-2 h-2 rounded-full bg-gray-200/60 dark:bg-gray-700/60 overflow-hidden">
                    <div class="h-full rounded-full bg-primary-500/70 backdrop-blur-sm transition-all duration-300" :style="{ width: scorePercent + '%' }"></div>
                  </div>
                </div>
              </div>

              <!-- 等级评定 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ t('teacher.grading.form.grade') }} <span class="text-red-500">*</span>
                </label>
                <glass-popover-select
                  v-model="gradeForm.grade"
                  :options="gradeOptions"
                  size="md"
                  :disabled="true"
                />
                <p v-if="errors.grade" class="mt-1 text-sm text-red-600">{{ errors.grade }}</p>
              </div>

              <!-- 反馈内容 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  {{ t('teacher.grading.form.feedback') }} <span class="text-red-500">*</span>
                </label>
                <glass-textarea
                  v-model="gradeForm.feedback"
                  :rows="6"
                  :placeholder="t('teacher.grading.form.feedbackPlaceholder') as string"
                  :class="{ 'border-red-500': errors.feedback }"
                  @blur="validateFeedback"
                />
                <p v-if="errors.feedback" class="mt-1 text-sm text-red-600">{{ errors.feedback }}</p>
              </div>

              <!-- 优点和改进点 -->
              <div class="grid grid-cols-1 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    {{ t('teacher.grading.form.strengths') }}
                  </label>
                  <glass-textarea
                    v-model="gradeForm.strengths"
                    :rows="3"
                    :placeholder="t('teacher.grading.form.strengthsPlaceholder') as string"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    {{ t('teacher.grading.form.improvements') }}
                  </label>
                  <glass-textarea
                    v-model="gradeForm.improvements"
                    :rows="3"
                    :placeholder="t('teacher.grading.form.improvementsPlaceholder') as string"
                  />
                </div>
              </div>

              <!-- 评分选项 -->
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.allowResubmit') }}</span>
                  <glass-switch v-model="(gradeForm.allowResubmit as any)" size="sm" />
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.sendNotification') }}</span>
                  <glass-switch v-model="(gradeForm.sendNotification as any)" size="sm" />
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.publishImmediately') }}</span>
                  <glass-switch v-model="(gradeForm.publishImmediately as any)" size="sm" />
                </div>
                <div class="flex items-center justify-between" v-if="gradeForm.allowResubmit">
                  <span class="text-sm text-gray-700 dark:text-gray-300">{{ t('teacher.grading.form.resubmitUntil') || '重交截止时间' }}</span>
                  <glass-date-time-picker v-model="gradeForm.resubmitUntil" size="md" :minute-step="5" tint="info" />
                </div>
              </div>

              <!-- 提交按钮 -->
              <div class="space-y-3">
                <Button
                  type="submit"
                  variant="success"
                  size="lg"
                  class="w-full"
                  :loading="isSubmitting"
                  :disabled="!isFormValid"
                >
                   <check-icon class="w-4 h-4 mr-2" />
                   {{ t('teacher.grading.form.submit') }}
                </Button>
                
                <Button
                  type="button"
                  variant="outline"
                  size="lg"
                  class="w-full"
                  @click="saveDraft"
                  :loading="isDraftSaving"
                >
                   <document-duplicate-icon class="w-4 h-4 mr-2" />
                   {{ t('teacher.grading.form.saveDraft') }}
                </Button>
                <Button
                  type="button"
                  variant="danger"
                  size="lg"
                  class="w-full"
                  @click="openReturnModal"
                >
                   <exclamation-triangle-icon class="w-4 h-4 mr-2" />
                   {{ t('teacher.grading.actions.returnForResubmit') || '打回重做' }}
                </Button>
              </div>
            </form>
          </card>
      <glass-modal v-if="showReturn" :title="(t('teacher.grading.actions.returnForResubmit') as string) || '打回重做'" size="lg" heightVariant="tall" solidBody @close="showReturn=false">
        <div class="space-y-3">
          <label class="text-sm">{{ t('teacher.grading.form.reason') || '原因' }}</label>
          <glass-textarea v-model="returnForm.reason" :rows="4" />
          <label class="text-sm">{{ t('teacher.grading.form.resubmitUntil') || '重交截止时间' }}</label>
          <glass-date-time-picker v-model="returnForm.resubmitUntil" size="md" :minute-step="5" tint="info" />
        </div>
        <template #footer>
          <Button variant="secondary" size="sm" @click="showReturn=false">{{ t('common.cancel') || '取消' }}</Button>
          <Button variant="danger" size="sm" @click="confirmReturn">{{ t('common.confirm') || '确定' }}</Button>
        </template>
      </glass-modal>

      <!-- AI 报告详情弹窗（与历史页一致的样式与渲染） -->
      <glass-modal v-if="aiDetailOpen" :title="assignment.title || (t('teacher.aiGrading.viewDetail') as string) || 'AI 能力报告'" size="xl" :hideScrollbar="true" heightVariant="max" solidBody @close="aiDetailOpen=false">
        <div v-if="aiDetailParsed" ref="aiDetailRef" data-export-root="1" class="space-y-4">
          <Card padding="sm" tint="secondary">
            <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.overall') }}</h4>
            <div>
              <div class="text-sm mb-2 flex items-center gap-3" v-if="getOverall(aiDetailParsed)?.final_score != null">
                <span>{{ t('teacher.aiGrading.render.final_score') }}: {{ overallScore(aiDetailParsed) }}</span>
                  <div class="h-2 w-64 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                    <div class="h-full" data-gradient="overall" :style="{ width: ((Number(overallScore(aiDetailParsed))*20) || 0) + '%' }"></div>
                  </div>
              </div>
              <div class="space-y-2 mb-2" v-if="dimensionBars(aiDetailParsed)">
                <div class="text-sm font-medium">{{ t('teacher.aiGrading.render.dimension_averages') }}</div>
                <div v-for="row in dimensionBars(aiDetailParsed)" :key="row.key" class="flex items-center gap-3">
                  <div class="w-40 text-xs text-gray-700 dark:text-gray-300">{{ row.label }}: {{ row.value }}</div>
                 <div class="h-2 flex-1 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner">
                   <div class="h-full" :data-gradient="dimGradient(row.key)" :style="{ width: ((row.value*20) || 0) + '%' }"></div>
                 </div>
                </div>
              </div>
              <div class="text-sm whitespace-pre-wrap">{{ t('teacher.aiGrading.render.holistic_feedback') }}: {{ overallFeedback(aiDetailParsed) || (t('common.empty') || '无内容') }}</div>
            </div>
          </Card>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card padding="sm" tint="warning" v-if="aiDetailParsed?.moral_reasoning">
            <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.moral_reasoning') }}</h4>
             <div v-html="renderCriterion(aiDetailParsed.moral_reasoning, 'dimension_moral')"></div>
            </Card>
            <Card padding="sm" tint="accent" v-if="aiDetailParsed?.attitude_development">
            <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.attitude_development') }}</h4>
             <div v-html="renderCriterion(aiDetailParsed.attitude_development, 'dimension_attitude')"></div>
            </Card>
            <Card padding="sm" tint="info" v-if="aiDetailParsed?.ability_growth">
            <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.ability_growth') }}</h4>
             <div v-html="renderCriterion(aiDetailParsed.ability_growth, 'dimension_ability')"></div>
            </Card>
            <Card padding="sm" tint="success" v-if="aiDetailParsed?.strategy_optimization">
            <h4 class="font-semibold mb-2">{{ t('teacher.aiGrading.render.strategy_optimization') }}</h4>
             <div v-html="renderCriterion(aiDetailParsed.strategy_optimization, 'dimension_strategy')"></div>
            </Card>
          </div>
        </div>
        <pre v-else class="bg-black/70 text-green-100 p-3 rounded overflow-auto text-xs max-h-[60vh]">{{ pretty(aiRawJson) }}</pre>
        <template #footer>
          <Button size="sm" variant="primary" @click="exportAiDetailAsText" :disabled="!aiDetailParsed">{{ t('teacher.aiGrading.exportText') || '导出文本' }}</Button>
          <Button size="sm" variant="success" @click="exportAiDetailAsPng" :disabled="!aiDetailParsed">{{ t('teacher.aiGrading.exportPng') || '导出 PNG' }}</Button>
          <Button size="sm" variant="purple" @click="exportAiDetailAsPdf" :disabled="!aiDetailParsed">{{ t('teacher.aiGrading.exportPdf') || '导出 PDF' }}</Button>
          <Button size="sm" variant="secondary" @click="aiDetailOpen=false">{{ t('teacher.aiGrading.picker.close') || '关闭' }}</Button>
        </template>
      </glass-modal>

          <!-- 学生信息 -->
          <card padding="lg" tint="success">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.student.title') }}</h3>
            </template>
            
            <div class="space-y-4">
              <div class="flex items-center space-x-3">
                <div class="w-12 h-12">
                  <user-avatar :avatar="submission.avatar" :size="48">
                    <div class="w-12 h-12 rounded-full" :style="{ backgroundColor: 'color-mix(in oklab, var(--color-primary) 15%, transparent)' }">
                      <user-icon class="w-6 h-6 text-theme-primary" />
                    </div>
                  </user-avatar>
                </div>
                <div>
                  <h4 class="font-medium text-gray-900 dark:text-white">{{ submission.studentName }}</h4>
                  <Badge v-if="submission.mbti" size="sm" :variant="mbtiVariant">MBTI · {{ submission.mbti }}</Badge>
                </div>
              </div>
              
              <div class="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.course') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ assignment.courseName }}</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.avg') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.averageScore ?? '--' }}/100</span>
                </div>
                <div>
                  <span class="text-gray-500 dark:text-gray-400">{{ t('teacher.grading.student.rank') }}</span>
                  <span class="font-medium text-gray-900 dark:text-white block">{{ submission.rank ?? '--' }}/{{ submission.totalStudents ?? '--' }}</span>
                </div>
              </div>
              
              <div class="flex justify-center">
                <Button variant="primary" size="lg" @click="viewStudentProfile">
                  <user-icon class="w-4 h-4 mr-2" />
                  {{ t('teacher.grading.sidebar.viewProfile') || '查看学生档案' }}
                </Button>
              </div>
            </div>
          </card>

          <!-- 快速操作 -->
          <card padding="lg" tint="success">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">{{ t('teacher.grading.quick.title') }}</h3>
            </template>
            
            <div class="space-y-3">
              <Button variant="outline" class="w-full justify-start" @click="contactStudent">
                <chat-bubble-left-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.sidebar.contact') || '联系学生' }}
              </Button>
              <Button variant="outline" class="w-full justify-start" @click="viewOtherSubmissions">
                <document-text-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.sidebar.otherSubmissions') || '查看其它提交' }}
              </Button>
              <Button variant="outline" class="w-full justify-start" @click="exportSubmission">
                <arrow-down-tray-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.sidebar.export') || '导出提交' }}
              </Button>
              <Button variant="outline" class="w-full justify-start" @click="reportPlagiarism">
                <exclamation-triangle-icon class="w-4 h-4 mr-3" />
                {{ t('teacher.grading.sidebar.report') || '举报疑似抄袭' }}
              </Button>
            </div>
          </card>
        </div>
      </div>
      <!-- 举报弹窗 (GlassModal) -->
      <glass-modal v-if="showReport" :title="t('teacher.grading.quick.report') as string" size="sm" heightVariant="compact" solidBody @close="showReport=false">
        <div class="space-y-2">
          <label class="text-sm">{{ t('teacher.studentDetail.table.reason') || '原因' }}</label>
          <glass-input v-model="reportForm.reason" />
          <label class="text-sm">{{ t('teacher.studentDetail.table.details') || '详情' }}</label>
          <glass-textarea v-model="reportForm.details" :rows="4" />
        </div>
        <template #footer>
          <Button variant="secondary" size="sm" @click="showReport=false">{{ t('common.cancel') || '取消' }}</Button>
          <Button variant="primary" size="sm" @click="submitReport">{{ t('common.submit') || '提交' }}</Button>
        </template>
      </glass-modal>

      <!-- AI 历史详情弹窗 -->
      <glass-modal v-if="aiHistoryDetailOpen" :title="t('teacher.aiGrading.historyDetail') as string || 'AI 批改详情'" size="2xl" heightVariant="max" solidBody @close="aiHistoryDetailOpen=false">
        <div class="space-y-3">
          <div class="flex flex-wrap items-center gap-3 text-sm">
            <badge variant="secondary">{{ aiHistoryDetail?.model || '-' }}</badge>
            <span class="text-gray-600 dark:text-gray-300">{{ formatDate(aiHistoryDetail?.createdAt) }}</span>
            <span class="text-gray-900 dark:text-white font-medium">{{ aiHistoryDetail?.fileName || t('common.unknown') || '未知文件' }}</span>
            <span class="ml-auto text-gray-700 dark:text-gray-200">{{ t('teacher.grading.ai.suggestedScore') }}: <b>{{ aiHistoryDetailView?.suggestedScore ?? '-' }}</b></span>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <Card padding="sm" tint="secondary">
              <div class="text-xs text-gray-600 dark:text-gray-300 mb-2">{{ t('teacher.grading.ai.title') }}</div>
              <div class="grid grid-cols-4 gap-2 text-xs">
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ (aiHistoryDetailView?.dims?.moral ?? 0).toFixed(1) }}/5</div>
                  <p class="text-gray-500">{{ t('teacher.aiGrading.render.moral_reasoning') }}</p>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ (aiHistoryDetailView?.dims?.attitude ?? 0).toFixed(1) }}/5</div>
                  <p class="text-gray-500">{{ t('teacher.aiGrading.render.attitude_development') }}</p>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ (aiHistoryDetailView?.dims?.ability ?? 0).toFixed(1) }}/5</div>
                  <p class="text-gray-500">{{ t('teacher.aiGrading.render.ability_growth') }}</p>
                </div>
                <div class="text-center">
                  <div class="font-semibold text-gray-900 dark:text-white">{{ (aiHistoryDetailView?.dims?.strategy ?? 0).toFixed(1) }}/5</div>
                  <p class="text-gray-500">{{ t('teacher.aiGrading.render.strategy_optimization') }}</p>
                </div>
              </div>
              <div class="mt-3 text-xs text-gray-700 dark:text-gray-300 whitespace-pre-wrap">{{ aiHistoryDetailView?.analysis || '' }}</div>
            </Card>
            <Card padding="sm" tint="secondary">
              <div class="text-xs text-gray-600 dark:text-gray-300 mb-2">{{ t('common.details') || '详情' }}</div>
              <pre class="text-[11px] leading-4 max-h-64 overflow-auto no-scrollbar bg-white/40 dark:bg-white/5 border border-white/30 dark:border-white/10 rounded p-2">{{ aiHistoryDetailPretty }}</pre>
            </Card>
          </div>
        </div>
        <template #footer>
          <Button size="sm" variant="secondary" @click="aiHistoryDetailOpen=false">{{ t('common.close') || '关闭' }}</Button>
        </template>
      </glass-modal>

      <!-- 改为调用全局抽屉：删除本地 Teleport -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Progress from '@/components/ui/Progress.vue'
import {
  ChevronRightIcon,
  DocumentIcon,
  EyeIcon,
  ChartBarIcon,
  ArrowDownTrayIcon,
  ArrowUturnLeftIcon,
  SparklesIcon,
  AcademicCapIcon,
  UserIcon,
  CheckIcon,
  DocumentDuplicateIcon,
  ChatBubbleLeftIcon,
  DocumentTextIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useLocale } from '@/i18n/useLocale'
import PageHeader from '@/components/ui/PageHeader.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import GlassModal from '@/components/ui/GlassModal.vue'
import GlassSwitch from '@/components/ui/inputs/GlassSwitch.vue'
import GlassDateTimePicker from '@/components/ui/inputs/GlassDateTimePicker.vue'
import SegmentedPills from '@/components/ui/SegmentedPills.vue'
import { exportNodeAsPng, exportNodeAsPdf, applyExportGradientsInline } from '@/shared/utils/exporters'
import { getMbtiVariant } from '@/shared/utils/badgeColor'
import { userApi } from '@/api/user.api'

// Router and Stores
const route = useRoute()
const router = useRouter()
const uiStore = useUIStore()
const { t } = useLocale()

// 状态
const isLoading = ref(true)
const isSubmitting = ref(false)
const isDraftSaving = ref(false)
const aiLoading = ref(false)
const aiModalOpen = ref(false)

// 作业和提交数据
const assignment = reactive({
  id: String(route.params.assignmentId || ''),
  title: '',
  description: '',
  dueDate: '',
  totalScore: 100,
  courseName: '',
  courseId: ''
})

const submission = reactive<any>({
  id: String(route.params.submissionId || ''),
  studentId: String(route.params.studentId || ''),
  studentName: '',
  mbti: '',
  className: '',
  submittedAt: '',
  isLate: false,
  status: '',
  content: '',
  fileName: '',
  filePath: ''
})

const mbtiVariant = computed(() => getMbtiVariant((submission as any).mbti))

// AI评分建议
const aiSuggestion = ref<any | null>(null)
const lastAiNormalized = ref<any | null>(null)
const lastAiHistoryId = ref<any>(null)
const aiHistory = ref<any[]>([])
const analysisLines = computed(() => {
  const txt = String(aiSuggestion.value?.analysis || '').trim()
  if (!txt) return []
  // 按行拆分，去空行
  return txt.replace(/\r/g, '\n').split(/\n+/).map(s => s.trim()).filter(Boolean)
})
const aiHistoryDetailOpen = ref(false)
const aiHistoryDetail = ref<any | null>(null)
const aiHistoryDetailView = ref<any | null>(null)
const aiHistoryDetailPretty = computed(() => { try { return JSON.stringify(aiHistoryDetail.value?.parsed || aiHistoryDetail.value, null, 2) } catch { return '' } })

// 评分页本地复用：AI 报告详情弹窗（与历史页一致）
const aiDetailOpen = ref(false)
const aiDetailRef = ref<HTMLElement | null>(null)
const aiRawJson = ref<any>(null)
const aiDetailParsed = computed(() => {
  try {
    if (!aiRawJson.value) return null
    // lastAiNormalized 已是标准schema，兼容直接传对象/字符串
    const raw = typeof aiRawJson.value === 'string' ? aiRawJson.value : JSON.stringify(aiRawJson.value)
    const obj = (function(s: string){
      try { return JSON.parse(s) } catch {
        try {
          let t = String(s || '')
          const m = t.match(/```(?:json)?\s*([\s\S]*?)```/i)
          if (m && m[1]) t = m[1]
          t = t.replace(/,\s*}/g, '}').replace(/,\s*]/g, ']')
          return JSON.parse(t)
        } catch { return null }
      }
    })(raw)
    return obj ? normalizeAssessment(obj) : null
  } catch { return null }
})
const pretty = (v: any) => { try { return JSON.stringify(typeof v === 'string' ? JSON.parse(v) : v, null, 2) } catch { return String(v || '') } }

// （移除重复定义，统一使用 aiDetailParsed）
function getOverall(obj: any): any { if (!obj || typeof obj !== 'object') return null; if ((obj as any).overall) return (obj as any).overall; if ((obj as any).final_score || (obj as any).holistic_feedback || (obj as any).dimension_averages) return obj; return null }
function overallScore(obj: any): number { const ov = getOverall(obj); const n = Number(ov?.final_score); return Number.isFinite(n) ? n : 0 }
function overallFeedback(obj: any): string { const ov = getOverall(obj); return String(ov?.holistic_feedback || '') }
function dimensionBars(obj: any): Array<{ key: string; label: string; value: number }> | null {
  try {
    const ov = getOverall(obj)
    const dm = ov?.dimension_averages
    if (!dm) return null
    return [
      { key: 'moral_reasoning', label: t('teacher.aiGrading.render.moral_reasoning') as string, value: Number(dm.moral_reasoning ?? 0) },
      { key: 'attitude', label: t('teacher.aiGrading.render.attitude_development') as string, value: Number(dm.attitude ?? 0) },
      { key: 'ability', label: t('teacher.aiGrading.render.ability_growth') as string, value: Number(dm.ability ?? 0) },
      { key: 'strategy', label: t('teacher.aiGrading.render.strategy_optimization') as string, value: Number(dm.strategy ?? 0) }
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
      const bar = typeof score === 'number' ? `<div class=\"h-2 w-40 rounded-md overflow-hidden border border-gray-300/70 dark:border-white/10 bg-gray-200/60 dark:bg-white/10 shadow-inner\"><div class=\"h-full\" data-gradient=\"${which}\" style=\"width:${score*20}%\"></div></div>` : ''
      const firstReasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning || ''
      const firstConclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion || ''
      const groupReasoning = String(firstReasoning || (sec?.reasoning || ((sec && !Array.isArray(sec?.evidence) && (sec as any).evidence?.reasoning) || '')) || '')
      const groupConclusion = String(firstConclusion || (sec?.conclusion || ((sec && !Array.isArray(sec?.evidence) && (sec as any).evidence?.conclusion) || '')) || '')
      const evid = ev
        .filter((e: any) => (e && (e.quote || e.explanation)))
        .map((e: any) => `<li class=\"mb-1\">\n            <div class=\"text-xs text-gray-600 dark:text-gray-300\">${e.quote ? '\"'+escapeHtml(e.quote)+'\"' : ''}</div>\n            ${e.explanation?`<div class=\\\"text-[11px] text-gray-500\\\">${escapeHtml(e.explanation)}</div>`:''}\n          </li>`) 
        .join('')
      const reasoningBlock = groupReasoning ? `<div class=\"text-xs\"><span class=\"font-semibold\">Reasoning:</span> ${escapeHtml(groupReasoning)}</div>` : ''
      const conclusionBlock = groupConclusion ? `<div class=\"text-xs italic text-gray-600 dark:text-gray-300\"><span class=\"not-italic font-semibold\">Conclusion:</span> ${escapeHtml(groupConclusion)}</div>` : ''
      const sugg = sug.map((s: any) => `<li class=\"mb-1 text-xs\">${escapeHtml(String(s))}</li>`).join('')
      sections.push(`<div class=\"space-y-2\"><div class=\"text-sm font-medium\">${escapeHtml(String(k))} ${score!=null?`(${score}/5)`:''}</div>${bar}<div><div class=\"text-xs font-semibold mt-2\">Evidence</div><ul>${evid}</ul>${reasoningBlock}${conclusionBlock}</div><div><div class=\"text-xs font-semibold mt-2\">Suggestions</div><ul>${sugg}</ul></div></div>`)
    }
    return sections.join('')
  } catch { return `<pre class=\"text-xs\">${escapeHtml(JSON.stringify(block, null, 2))}</pre>` }
}
// 维度条显示用主题映射（仅显示，不影响导出模板固定皮肤）
function dimGradient(key: string): 'dimension' | 'overall' | 'dimension_moral' | 'dimension_attitude' | 'dimension_ability' | 'dimension_strategy' {
  const k = String(key || '').toLowerCase()
  if (k.includes('moral') || k.includes('stage') || k.includes('argument') || k.includes('foundation')) return 'dimension_moral'
  if (k.includes('attitude') || k.includes('emotion') || k.includes('resilience') || k.includes('flow')) return 'dimension_attitude'
  if (k.includes('ability') || k.includes('bloom') || k.includes('metacognition') || k.includes('transfer')) return 'dimension_ability'
  if (k.includes('strategy') || k.includes('diversity') || k.includes('depth') || k.includes('regulation')) return 'dimension_strategy'
  return 'dimension'
}
function escapeHtml(s: string) { return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;').replace(/'/g,'&#39;') }


// 评分历史
const gradingHistory = ref<any[]>([])
const assignmentCourseId = computed(() => String(assignment.courseId || ''))

const showSubmissionContent = computed(() => {
  const st = String(submission.status || '')
  if (st === 'returned') return false
  return true
})

// 顶部副标题：未提交显示"未提交"，否则显示提交时间
const pageSubtitle = computed(() => {
  if (!submission) return ''
  const st = String(submission.status || '')
  if (st === 'unsubmitted') return t('teacher.submissions.status.unsubmitted') as any
  if (st === 'returned') return t('teacher.submissions.status.returned') as any
  if (st === 'graded') return t('teacher.submissions.status.graded') as any
  if (!submission.submittedAt) return t('teacher.submissions.status.unsubmitted') as any
  return t('teacher.submissions.submittedAt', { time: formatDate(submission.submittedAt) }) as any
})

// 评分表单
const gradeForm = reactive({
  score: 0,
  grade: '',
  feedback: '',
  strengths: '',
  improvements: '',
  allowResubmit: false,
  sendNotification: true,
  publishImmediately: true,
  resubmitUntil: '' as any
})

// 错误状态
const errors = reactive({
  score: '',
  feedback: '',
  grade: ''
})

// 计算属性
const isFormValid = computed(() => {
  return gradeForm.score >= 0 &&
    gradeForm.score <= assignment.totalScore &&
    gradeForm.grade.trim() !== '' &&
    gradeForm.feedback.trim() !== '' &&
    Object.values(errors).every(error => error === '')
})

// 方法
import { assignmentApi } from '@/api/assignment.api'
import { gradeApi } from '@/api/grade.api'
import { baseURL } from '@/api/config'
import { teacherStudentApi } from '@/api/teacher-student.api'
import { reportApi } from '@/api/report.api'
import { useChatStore } from '@/stores/chat'
import { submissionApi } from '@/api/submission.api'
import { notificationAPI } from '@/api/notification.api'
import { aiGradingApi } from '@/api/aiGrading.api'
import { fileApi } from '@/api/file.api'
import { abilityApi } from '@/api/ability.api'
import { onMounted as vueOnMounted } from 'vue'

const loadSubmission = async () => {
  const pickData = (v: any) => (v && v.data !== undefined) ? v.data : v
  const toMs = (v: any): number => {
    if (!v) return NaN
    if (typeof v === 'number') return v
    const s = String(v)
    const d = new Date(s.includes(' ') && !s.includes('T') ? s.replace(' ', 'T') : s)
    return d.getTime()
  }
  try {
    const submissionId = String(route.params.submissionId || '')
    const assignmentId = String(route.params.assignmentId || '')
    if (submissionId) {
      try {
  const sRes = await submissionApi.getSubmissionById(submissionId)
        const s = pickData(sRes)
    Object.assign(submission, s)
        if (!submission.status) submission.status = 'submitted'
      } catch (e) {
        // 容错：提交不存在时也允许进入评分（视为未提交）
        submission.status = 'unsubmitted'
      }
    } else {
      // 无提交场景：从路由参数得到 studentId，并拉取学生基本信息用于展示
      submission.status = 'unsubmitted'
      if (submission.studentId) {
        try {
          const prof: any = await teacherStudentApi.getStudentProfile(String(submission.studentId))
          const pdata: any = pickData(prof)
          const name = pdata?.nickname || pdata?.name || pdata?.username
          submission.studentName = name || String(submission.studentId)
          submission.avatar = pdata?.avatar
          submission.mbti = pdata?.mbti || pdata?.MBTI || pdata?.profile?.mbti || pdata?.profile?.MBTI || ''
          if (!submission.mbti) {
            try {
              const ures: any = await userApi.getProfileById(String(submission.studentId))
              const udata: any = (ures?.data?.data) ?? (ures?.data) ?? ures
              const resolvedMbti = udata?.mbti || udata?.MBTI || udata?.profile?.mbti || udata?.profile?.MBTI || ''
              if (resolvedMbti) submission.mbti = String(resolvedMbti)
            } catch {}
          }
        } catch {}
      }
    }
  const aRes = await assignmentApi.getAssignmentById(assignmentId)
    const a: any = pickData(aRes)
    assignment.title = a.title
    assignment.description = a.description
    assignment.dueDate = a.dueDate
    assignment.totalScore = Number((a.maxScore ?? a.totalScore ?? 100))
    assignment.courseName = a.courseName || ''
    assignment.courseId = String(a.courseId || '')
    assignment.courseId = String(a.courseId || '')
    const maybeGradeId: any = (submission as any)?.gradeId
    if (maybeGradeId) {
      try {
        const hist = await gradeApi.getGradeHistory(String(maybeGradeId))
        gradingHistory.value = pickData(hist) || []
      } catch {}
    } 
    // 若无提交或无 gradeId，尝试通过 学生+作业 获取当前成绩
    if (!gradingHistory.value.length) {
      // 兜底：通过提交ID查询一次成绩摘要，取到 grade_id 再查历史
      try {
        const sgRaw = submissionId ? await submissionApi.getSubmissionGrade(submissionId) : await gradeApi.getGradeForStudentAssignment(String(submission.studentId), String(assignment.id))
        const sg: any = pickData(sgRaw)
        // 若获取到成绩，按成绩真实状态映射展示态（无论是否有 submissionId）
        if (sg && (sg.id || sg.grade_id)) {
          const gStatus = String((sg.status || sg.grade_status || '')).toLowerCase()
          if (gStatus === 'returned') {
            // 若在打回之后已有重新提交 → 显示已提交；否则按是否超期显示未提交/已退回
            const untilMs = toMs(sg.resubmitUntil || sg.resubmit_until)
            const nowMs = Date.now()
            const updatedMs = toMs(sg.updatedAt || sg.updated_at || sg.gradedAt || sg.graded_at)
            const submittedMs = toMs((submission as any).submittedAt)
            const resubmittedAfterReturn = Number.isFinite(submittedMs) && Number.isFinite(updatedMs) && submittedMs > updatedMs
            if (resubmittedAfterReturn) {
              submission.status = 'submitted'
            } else if (Number.isFinite(untilMs) && nowMs > untilMs) {
              submission.status = 'unsubmitted'
            } else {
              submission.status = 'returned'
            }
          } else if (gStatus === 'published') {
            submission.status = 'graded'
          } else if (!submissionId) {
            submission.status = 'unsubmitted'
          }
        }
        const gid = sg?.grade_id
        // 兼容平均分、总人数、排名（注意后端字段名）
        const avgVal = sg?.averageScore ?? sg?.avg_score
        submission.averageScore = (avgVal === null || avgVal === undefined) ? undefined : Number(avgVal)
        const totalVal = sg?.totalStudents ?? sg?.total_students
        submission.totalStudents = (totalVal === null || totalVal === undefined) ? undefined : Number(totalVal)
        const rankVal = sg?.rank ?? sg?.student_rank
        submission.rank = (rankVal === null || rankVal === undefined) ? undefined : Number(rankVal)
        const targetGid = gid || sg?.id
        if (targetGid) {
          const hist = await gradeApi.getGradeHistory(String(targetGid))
          gradingHistory.value = pickData(hist) || []
        }
      } catch {}
    }
    // 兜底头像
    if ((!submission.avatar || !submission.mbti) && submission.studentId) {
      try {
        const prof: any = await teacherStudentApi.getStudentProfile(String(submission.studentId))
        const pdata: any = pickData(prof)
        if (pdata?.avatar) submission.avatar = pdata.avatar
        if (!submission.mbti) submission.mbti = pdata?.mbti || pdata?.MBTI || pdata?.profile?.mbti || pdata?.profile?.MBTI || ''
      } catch {}
      if (!submission.mbti) {
        try {
          const ures: any = await userApi.getProfileById(String(submission.studentId))
          const udata: any = (ures?.data?.data) ?? (ures?.data) ?? ures
          const resolvedMbti = udata?.mbti || udata?.MBTI || udata?.profile?.mbti || udata?.profile?.MBTI || ''
          if (resolvedMbti) submission.mbti = String(resolvedMbti)
        } catch {}
      }
    }
  } catch (error) {
    // 仅在确实没有 studentId 和 assignmentId 时提示失败；未提交路径不提示
    const hasKey = !!(submission.studentId) && !!(assignment.id)
    if (!hasKey) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.loadFailed'),
      message: t('teacher.grading.notify.loadFailedMsg')
    })
    }
  } finally {
    isLoading.value = false
  }
}

// 载入学生最近一次AI能力报告（用于持久化展示）
async function loadLatestAiReportForStudent() {
  try {
    if (!submission.studentId) return
    const resp: any = await abilityApi.getTeacherLatestReportOfStudent({
      studentId: String(submission.studentId),
      courseId: assignment.courseId || undefined,
      assignmentId: assignment.id || undefined,
      submissionId: submission.id || undefined
    })
    const rep = resp?.data || resp
    if (!rep) return
    // 统一以"报告中完整规范化JSON"为准，全面解析维度、最终分与Key Suggestions
    const total = Number(assignment.totalScore || 100)
    const raw = String(rep.trendsAnalysis || rep.trends_analysis || rep.normalizedJson || rep.rawJson || rep.raw_json || '')
    const obj = (function(s: string){
      try { return JSON.parse(s) } catch {
        try {
          let t = String(s || '')
          const m = t.match(/```(?:json)?\s*([\s\S]*?)```/i)
          if (m && m[1]) t = m[1]
          t = t.replace(/,\s*}/g, '}').replace(/,\s*]/g, ']')
          return JSON.parse(t)
        } catch { return {} }
      }
    })(raw)
        const normalized = normalizeAssessment(obj)
    const ov = getOverall(normalized) || {}
    const nd = (ov?.dimension_averages || {}) as any
    const final5 = Number(ov?.final_score || 0)
    const suggested = Math.round(((final5 / 5) * total) * 10) / 10

    // Key suggestions: 优先从 holistic_feedback 的"Key suggestions"段按行抽取；若无则聚合四组 suggestions
    const improvementsArr: string[] = []
    const holistic = String(ov?.holistic_feedback || '')
    if (holistic) {
      const lines = holistic.replace(/\r/g, '\n').split(/\n+/)
      let inKeys = false
      for (const l of lines) {
        const ln = String(l || '').trim()
        if (!ln) continue
        if (!inKeys) { if (ln.toLowerCase().includes('key suggestions')) { inKeys = true } continue }
        let item = ln
        if (item.startsWith('- ')) item = item.substring(2).trim()
        if (item.startsWith('• ')) item = item.substring(2).trim()
        if (item) improvementsArr.push(item)
        if (improvementsArr.length >= 12) break
      }
    }
    if (!improvementsArr.length) {
        const collect = (grp: any) => {
          if (!grp) return
          for (const k of Object.keys(grp)) {
            const sec = (grp as any)[k]
            const arr = Array.isArray(sec?.suggestions) ? sec.suggestions : []
          for (const s of arr) { const v = String(s || '').trim(); if (v) improvementsArr.push(v); if (improvementsArr.length >= 12) break }
              if (improvementsArr.length >= 12) break
            }
      }
      collect(normalized?.moral_reasoning); collect(normalized?.attitude_development); collect(normalized?.ability_growth); collect(normalized?.strategy_optimization)
    }

    aiSuggestion.value = {
      suggestedScore: suggested,
      dims: {
        moral: Number(nd.moral_reasoning || 0),
        attitude: Number(nd.attitude || 0),
        ability: Number(nd.ability || 0),
        strategy: Number(nd.strategy || 0)
      },
      analysis: holistic || String(rep.recommendations || '').trim(),
      improvements: improvementsArr
    }
  } catch {}
}

// 加载教师端AI批改历史（最近N条）
async function loadAiHistory() {
  try {
    const resp: any = await aiGradingApi.listHistory({ page: 1, size: 5 })
    const data = resp?.data || resp
    const items = data?.items || data?.list || data?.records || []
    aiHistory.value = Array.isArray(items) ? items : []
  } catch {
    aiHistory.value = []
  }
}

function parseNormalized(obj: any) {
  try {
    if (!obj) return null
    const normalized = normalizeAssessment(obj)
    const ov = getOverall(normalized)
    const final5 = Number(ov?.final_score ?? 0)
    const dim = (ov?.dimension_averages || {}) as any
    const suggested = Math.round(((final5 / 5) * Number(assignment.totalScore || 100)) * 10) / 10
    return {
      suggestedScore: suggested,
      dims: {
        moral: Number(dim.moral_reasoning || 0),
        attitude: Number(dim.attitude || 0),
        ability: Number(dim.ability || 0),
        strategy: Number(dim.strategy || 0)
      },
      analysis: String(ov?.holistic_feedback || '')
    }
  } catch { return null }
}

async function openAiHistoryDetail(h: any) {
  try {
    const resp: any = await aiGradingApi.getHistoryDetail(String(h.id))
    const rec = resp?.data || resp
    aiHistoryDetail.value = rec
    let parsed: any = null
    try { parsed = JSON.parse(String(rec.rawJson || '{}')) } catch {}
    aiHistoryDetail.value.parsed = parsed
    aiHistoryDetailView.value = parseNormalized(parsed)
    aiHistoryDetailOpen.value = true
  } catch {
    uiStore.showNotification({ type: 'error', title: t('teacher.aiGrading.historyDetail') || 'AI 批改详情', message: t('teacher.grading.notify.loadFailed') })
  }
}

const getSubmissionStatusVariant = (status: string) => {
  const variantMap: Record<string, string> = {
    submitted: 'success',
    graded: 'secondary',
    returned: 'warning',
    late: 'danger',
    unsubmitted: 'warning'
  }
  return variantMap[status] || 'secondary'
}

const getSubmissionStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    submitted: t('teacher.submissions.status.submitted'),
    graded: t('teacher.submissions.status.graded'),
    returned: t('teacher.submissions.status.returned'),
    late: t('teacher.submissions.status.late'),
    unsubmitted: t('teacher.submissions.status.unsubmitted')
  }
  return textMap[status] || status
}

const formatDate = (dateString: string) => {
  try {
    if (!dateString) return '-'
    const d = new Date(dateString)
    if (isNaN(d.getTime())) return '-'
    return d.toLocaleString('zh-CN')
  } catch { return '-' }
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileType = (filename: string) => {
  const extension = filename.split('.').pop()?.toLowerCase()
  const typeMap: Record<string, string> = {
    pdf: 'PDF文档',
    doc: 'Word文档',
    docx: 'Word文档',
    jpg: '图片',
    jpeg: '图片',
    png: '图片',
    txt: '文本文件'
  }
  return typeMap[extension || ''] || '未知类型'
}

const validateScore = () => {
  if (gradeForm.score < 0) {
    errors.score = t('teacher.grading.errors.scoreTooLow')
  } else if (gradeForm.score > assignment.totalScore) {
    errors.score = t('teacher.grading.errors.scoreTooHigh', { max: assignment.totalScore }) as string
  } else {
    errors.score = ''
  }
}

const validateGrade = () => {
  if (!gradeForm.grade || gradeForm.grade.trim() === '') {
    errors.grade = t('teacher.grading.errors.gradeRequired') as string
  } else {
    errors.grade = ''
  }
}

const validateFeedback = () => {
  if (gradeForm.feedback.trim() === '') {
    errors.feedback = t('teacher.grading.errors.feedbackRequired')
  } else {
    errors.feedback = ''
  }
}

const applyAiSuggestion = () => {
  if (!aiSuggestion.value) return
  gradeForm.score = aiSuggestion.value.suggestedScore
  gradeForm.feedback = aiSuggestion.value.analysis
  gradeForm.improvements = (aiSuggestion.value.improvements || []).join('\n')
  
  uiStore.showNotification({
    type: 'success',
    title: t('teacher.grading.notify.aiApplied'),
    message: t('teacher.grading.notify.aiAppliedMsg')
  })
}

// 根据分数自动选择等级（百分制区间，可按需调整）
const pickGradeByScore = (score: number, max: number) => {
  const pct = max > 0 ? (score / max) * 100 : 0
  if (pct >= 95) return 'A+'
  if (pct >= 90) return 'A'
  if (pct >= 85) return 'B+'
  if (pct >= 80) return 'B'
  if (pct >= 70) return 'C+'
  if (pct >= 60) return 'C'
  if (pct >= 50) return 'D'
  return 'F'
}

// 监听分数变动自动同步等级
watch(() => gradeForm.score, (val) => {
  const s = Number(val || 0)
  gradeForm.grade = pickGradeByScore(s, Number(assignment.totalScore || 100))
  validateGrade()
})

const gradeOptions = [
  { label: 'A+ (优秀+)', value: 'A+' },
  { label: 'A (优秀)', value: 'A' },
  { label: 'B+ (良好+)', value: 'B+' },
  { label: 'B (良好)', value: 'B' },
  { label: 'C+ (中等+)', value: 'C+' },
  { label: 'C (中等)', value: 'C' },
  { label: 'D (及格)', value: 'D' },
  { label: 'F (不及格)', value: 'F' },
]

// 作业要求区域：提交状态文本与样式
const submitStatusText = computed(() => {
  const st = String(submission.status || '')
  if (st === 'unsubmitted') return t('teacher.submissions.status.unsubmitted') as any
  if (st === 'returned') return t('teacher.submissions.status.returned') as any
  if (st === 'graded') return t('teacher.submissions.status.graded') as any
  return submission.isLate ? t('teacher.grading.assignment.late') : t('teacher.grading.assignment.onTime')
})
const submitStatusClass = computed(() => {
  const st = String(submission.status || '')
  if (st === 'unsubmitted') return 'text-gray-500 dark:text-gray-400'
  if (st === 'returned') return 'text-yellow-600'
  if (st === 'graded') return 'text-green-600'
  return submission.isLate ? 'text-red-600' : 'text-green-600'
})

const previewFile = async (file: any) => {
  const fid = String(file?.id || file?.fileId || '')
  const filename = String(file?.originalName || file?.fileName || `file_${fid}`)
  if (!fid) return
  // 1) 先根据文件名后缀/元信息判断是否可在线预览，避免无谓的 /preview 400
  const isPreviewExt = (name: string) => {
    const n = String(name || '').toLowerCase()
    return ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.pdf'].some(ext => n.endsWith(ext))
  }
  try {
    let previewable = isPreviewExt(filename)
    if (!previewable) {
      try {
        const info: any = await fileApi.getFileInfo(fid)
        const ct = String(info?.contentType || info?.mimeType || '')
        if (ct && (ct.startsWith('image/') || ct === 'application/pdf')) previewable = true
      } catch {}
    }
    if (!previewable) {
      await fileApi.downloadFile(fid, filename)
      return
    }
    // 2) 仅在推断可预览时才调用 preview；失败则回退下载
    try {
      const blob: Blob = await fileApi.getPreview(fid)
      if (!blob || blob.size === 0) {
        await fileApi.downloadFile(fid, filename)
        return
      }
      const type = String((blob as any)?.type || '')
      if (!(type.startsWith('image/') || type === 'application/pdf')) {
        await fileApi.downloadFile(fid, filename)
        return
      }
      const url = URL.createObjectURL(blob)
      window.open(url, '_blank')
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (e: any) {
      await fileApi.downloadFile(fid, filename)
    }
  } catch (err: any) {
    const status = err?.response?.status
    if (status === 401) {
      uiStore.showNotification({ type: 'error', title: t('error.unauthorized') as string || '未授权', message: t('auth.login.required') as string || '未授权访问，请先登录' })
      return
    }
    await fileApi.downloadFile(fid, filename)
  }
}

const downloadFile = (file: any) => {
  // 实现文件下载
  uiStore.showNotification({
    type: 'success',
    title: t('teacher.grading.notify.downloading'),
    message: `${t('teacher.grading.notify.downloading')}: ${file.name}`
  })
}

const previewSingleFile = async () => {
  try {
    // 优先读取与该提交关联的文件列表
    const res: any = await fileApi.getRelatedFiles('submission', String(submission.id))
    const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
    if (list.length > 0) {
      return await previewFile(list[0])
    }
    // 无附件则尝试预览文本内容（已在页面显示）；这里给出提示
    if (String(submission.content || '').trim()) {
      uiStore.showNotification({ type: 'info', title: t('teacher.grading.submission.content') as string, message: t('teacher.grading.notify.textShown') as string || '该提交无附件，已显示文本内容。' })
      return
    }
    uiStore.showNotification({ type: 'warning', title: t('common.empty') as string || '无内容', message: t('teacher.grading.notify.noPreview') as string || '没有可预览的内容或附件。' })
  } catch (err: any) {
    const status = err?.response?.status
    if (status === 401) {
      uiStore.showNotification({ type: 'error', title: t('error.unauthorized') as string || '未授权', message: t('auth.login.required') as string || '未授权访问，请先登录' })
    } else {
      uiStore.showNotification({ type: 'error', title: t('teacher.grading.notify.filePreview') as string, message: (err?.message || '预览失败') })
    }
  }
}

// ---- AI 弹窗逻辑 ----
const gradingModel = ref('google/gemini-2.5-pro')
const gradingModelOptions = [
  { label: 'Gemini 2.5 Pro', value: 'google/gemini-2.5-pro' },
  { label: 'GLM-4.6', value: 'glm-4.6' },
  { label: 'GLM-4.6v', value: 'glm-4.6v' }
]
const aiSource = ref<'text'|'files'>('text')
const aiPicker = reactive({ previewText: '', files: [] as any[], selectedFileIds: [] as number[] })
const aiProgress = reactive<{ items: Array<{ id: number|string; name: string; status: 'pending'|'extracting'|'grading'|'done'|'error'; result?: any; error?: string }> }>({ items: [] })
const hasOngoing = computed(() => aiProgress.items.some(it => it.status === 'grading' || it.status === 'extracting' || it.status === 'pending'))

function openAiModal() {
  aiPicker.previewText = String(submission.content || '').trim()
  aiSource.value = 'text'
  aiPicker.selectedFileIds = []
  aiPicker.files = []
  ;(async () => {
    try {
      const res: any = await fileApi.getRelatedFiles('submission', String(submission.id))
      const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
      aiPicker.files = list
    } catch { aiPicker.files = [] }
  })()
  aiModalOpen.value = true
}

const canStartAi = computed(() => {
  return aiSource.value === 'text' ? !!aiPicker.previewText : (Array.isArray(aiPicker.selectedFileIds) && aiPicker.selectedFileIds.length > 0)
})

function selectAllAiFiles() {
  aiPicker.selectedFileIds = (aiPicker.files || []).map((f: any) => Number(f?.id)).filter((n: any) => Number.isFinite(n))
}
function clearSelectedAiFiles() { aiPicker.selectedFileIds = [] }

// 可预览性：依据文件名/后端返回的 contentType 判断（图片/PDF）
const canPreviewSubmission = computed(() => {
  try {
    const name = String(submission.fileName || '')
    const lower = name.toLowerCase()
    if (['.png','.jpg','.jpeg','.gif','.webp','.pdf'].some(ext => lower.endsWith(ext))) return true
    return false
  } catch { return false }
})

async function startAiGradingFromModal() {
  if (!canStartAi.value) return
  aiLoading.value = true
  aiProgress.items = []
  try {
    if (aiSource.value === 'text') {
      // 仅文本
      const tempId = Date.now()
      aiProgress.items.push({ id: tempId, name: 'text_item', status: 'grading' })
      try {
        const payload = { messages: [{ role: 'user', content: String(aiPicker.previewText) }], model: gradingModel.value, jsonOnly: true, useGradingPrompt: true }
      const resp: any = await aiGradingApi.gradeEssay(payload as any)
        const result = (resp?.data?.results?.[0]?.result) ?? (resp?.data ?? resp)
      const historyId = (resp?.data?.historyId ?? resp?.historyId)
        const target = aiProgress.items.find(it => it.id === tempId)
      if (target) { target.status = 'done'; target.result = result; (target as any).historyId = historyId }
      } catch (e: any) {
        const target = aiProgress.items.find(it => it.id === tempId)
        if (target) { target.status = 'error'; target.error = e?.message || 'Failed' }
      }
    } else if (aiSource.value === 'files') {
      // 仅附件
      const ids = aiPicker.selectedFileIds.slice()
      aiProgress.items.push(...ids.map(id => {
        const info = (aiPicker.files || []).find((x: any) => Number(x.id) === Number(id))
        const name = info?.originalName || info?.fileName || info?.name || `file_${id}`
        return { id, name, status: 'grading' as const }
      }))
      try {
        const resp: any = await aiGradingApi.gradeFiles({ fileIds: ids as number[], model: gradingModel.value, jsonOnly: true, useGradingPrompt: true })
        const results = (resp?.data?.results || resp?.results || []) as any[]
        for (const r of results) {
          const target = aiProgress.items.find(it => Number(it.id) === Number(r.fileId))
          if (!target) continue
          if (r.error) { target.status = 'error'; target.error = r.error || (resp?.message) || (resp?.data?.message) || t('common.unknownError') }
          else {
            // 接口可能包裹 { result: { evaluation: {...} } }
            const result = (r?.result?.evaluation ? r.result : (r?.result ?? r))
            target.status = 'done'; target.result = result; (target as any).historyId = (r?.historyId ?? resp?.data?.historyId)
          }
        }
      } catch (err: any) {
        const errMsg = err?.response?.data?.message || err?.message || t('common.unknownError')
        aiProgress.items.forEach(it => { if (ids.includes(Number(it.id))) { it.status = 'error'; it.error = errMsg } })
        throw err
      }
    }

    // 使用首个成功结果作为当前界面展示依据（不立即保存到历史/报告）
    const firstOk = aiProgress.items.find(it => it.status === 'done' && it.result)
    if (firstOk?.result) {
      try {
        const rawRes: any = firstOk.result
        const v = rawRes?.evaluation ? { evaluation: rawRes.evaluation } : rawRes
        lastAiNormalized.value = normalizeAssessment(v)
      } catch { lastAiNormalized.value = null }
      try { lastAiHistoryId.value = (firstOk as any)?.historyId || null } catch { lastAiHistoryId.value = null }
      // 解析维度、最终分、Key suggestions → 用于"AI评分建议"展示
      try {
        const ov = getOverall(lastAiNormalized.value)
      const dim = (ov?.dimension_averages || {}) as any
        const final5 = Number(ov?.final_score ?? 0)
      const suggested = Math.round(((final5 / 5) * Number(assignment.totalScore || 100)) * 10) / 10
      const improvements: string[] = []
        const holistic = String(ov?.holistic_feedback || '')
        if (holistic) {
          const lines = holistic.replace(/\r/g, '\n').split(/\n+/)
          let inKeys = false
          for (const l of lines) {
            const ln = String(l || '').trim()
            if (!ln) continue
            if (!inKeys) { if (ln.toLowerCase().includes('key suggestions')) { inKeys = true } continue }
            let item = ln
            if (item.startsWith('- ')) item = item.substring(2).trim()
            if (item.startsWith('• ')) item = item.substring(2).trim()
            if (item) improvements.push(item)
            if (improvements.length >= 12) break
          }
        }
        if (!improvements.length) {
          const collect = (grp: any) => {
        if (!grp) return
        for (const k of Object.keys(grp)) {
          const sec = (grp as any)[k]
          const arr = Array.isArray(sec?.suggestions) ? sec.suggestions : []
              for (const s of arr) { const v = String(s || '').trim(); if (v) improvements.push(v); if (improvements.length >= 12) break }
              if (improvements.length >= 12) break
        }
      }
          collect(lastAiNormalized.value?.moral_reasoning); collect(lastAiNormalized.value?.attitude_development); collect(lastAiNormalized.value?.ability_growth); collect(lastAiNormalized.value?.strategy_optimization)
        }
      aiSuggestion.value = {
        suggestedScore: suggested,
        dims: {
          moral: Number(dim.moral_reasoning || 0),
          attitude: Number(dim.attitude || 0),
          ability: Number(dim.ability || 0),
          strategy: Number(dim.strategy || 0)
        },
          analysis: holistic,
        improvements
        }
      } catch {}
    }

    uiStore.showNotification({ type: 'success', title: t('teacher.grading.ai.title'), message: t('teacher.grading.notify.aiApplied') })
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.ai.title'), message: e?.message || t('teacher.grading.notify.loadFailed') })
  } finally {
    aiLoading.value = false
  }
}

// 跳转到AI历史并打开本次报告（基于最近历史ID或最新一条）
async function viewAiDetail() {
  // 优先展示本次新生成；若无，则回退读取最近一次已保存的 ability_report
  try {
    if (lastAiNormalized.value) {
      aiRawJson.value = JSON.stringify(lastAiNormalized.value)
    } else {
      const resp: any = await abilityApi.getTeacherLatestReportOfStudent({
        studentId: String(submission.studentId),
        courseId: assignment.courseId || undefined,
        assignmentId: assignment.id || undefined,
        submissionId: submission.id || undefined
      })
      const rep = resp?.data || resp
      const raw = String(
        rep?.normalizedJson ||
        rep?.normalized_json ||
        rep?.trendsAnalysis ||
        rep?.trends_analysis ||
        rep?.rawJson ||
        rep?.raw_json ||
        ''
      )
      aiRawJson.value = raw
    }
  } catch {
    aiRawJson.value = ''
  }
  aiDetailOpen.value = true
  setTimeout(() => {
    const el = document.querySelector('[data-export-root="1"]') as HTMLElement | null
    if (el) aiDetailRef.value = el
  }, 0)
}

// 导出（与历史页一致的实现）
function exportAiDetailAsText() {
  if (!aiDetailParsed.value) return
  const res: any = aiDetailParsed.value
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
        let reasoning = (ev.find((e: any) => e && String(e.reasoning || '').trim()) || {}).reasoning as string | undefined
        let conclusion = (ev.find((e: any) => e && String(e.conclusion || '').trim()) || {}).conclusion as string | undefined
        const explanation = (ev.find((e: any) => e && String(e.explanation || '').trim()) || {}).explanation
        if (!reasoning) reasoning = String((s as any)?.reasoning || ((s && !Array.isArray(s?.evidence) && (s as any).evidence?.reasoning) || '')) || undefined
        if (!conclusion) conclusion = String((s as any)?.conclusion || ((s && !Array.isArray(s?.evidence) && (s as any).evidence?.conclusion) || '')) || undefined
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
  const ov = getOverall(res) as any
  if (ov) {
    lines.push(`总体评分: ${Number(ov?.final_score ?? 0)}/5`)
    if (ov?.holistic_feedback) lines.push(`总体评价: ${String(ov.holistic_feedback)}`)
    lines.push('')
  }
  pushSec('道德推理', res?.moral_reasoning)
  pushSec('学习态度', res?.attitude_development)
  pushSec('学习能力', res?.ability_growth)
  pushSec('学习策略', res?.strategy_optimization)
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${(assignment.title || 'grading').toString().replace(/\s+/g,'_')}.txt`
  a.click()
  URL.revokeObjectURL(a.href)
}
async function exportAiDetailAsPng() {
  if (!aiDetailRef.value) return
  applyExportGradientsInline(aiDetailRef.value)
  const fileBase = (assignment.title || 'grading').toString().replace(/\s+/g, '_')
  await exportNodeAsPng(aiDetailRef.value, fileBase)
}
async function exportAiDetailAsPdf() {
  if (!aiDetailRef.value) return
  const node = aiDetailRef.value
  const cloned = node.cloneNode(true) as HTMLElement
  const wrapper = document.createElement('div')
  wrapper.style.position = 'fixed'
  wrapper.style.left = '-99999px'
  wrapper.style.top = '0'
  wrapper.style.width = `${node.clientWidth}px`
  wrapper.style.background = '#ffffff'
  cloned.style.maxHeight = 'none'
  cloned.style.overflow = 'visible'
  applyExportGradientsInline(cloned)
  wrapper.appendChild(cloned)
  document.body.appendChild(wrapper)
  // 关键：强制移除 glass 容器的 backdrop-filter，避免 html2canvas 解析 oklab/oklch
  try {
    cloned.querySelectorAll('[v-glass], .glass-ultraThin, .glass-thin, .glass-regular, .glass-thick, .glass-ultraThick').forEach((n) => {
      const s = (n as HTMLElement).style
      s.backdropFilter = 'none'
      s.setProperty('-webkit-backdrop-filter', 'none')
    })
  } catch {}
  await exportNodeAsPdf(cloned, (assignment.title || 'grading').toString().replace(/\s+/g, '_'))
  document.body.removeChild(wrapper)
}
const downloadSingleFile = async () => {
  const name = submission.fileName || 'attachment'
  try {
    // 1) 优先：submission.fileId
    const fid = String((submission as any).fileId || '')
    if (fid) { await fileApi.downloadFile(fid, name); return }

    // 2) 兼容：从后端查询与该 submission 关联的文件列表
    if (submission.id) {
      try {
        const res: any = await fileApi.getRelatedFiles('submission', String(submission.id))
        const list: any[] = (res?.data || res || []) as any[]
        const first = (Array.isArray(list) ? list : []).find((f: any) => f && (f.id || f.fileId))
        const rid = String(first?.id || first?.fileId || '')
        if (rid) { await fileApi.downloadFile(rid, first?.originalName || first?.fileName || name); return }
      } catch {}
    }

    // 3) 兼容：从 filePath 中提取 /files/{id}/...
    const path = String(submission.filePath || '')
    const m = path.match(/\/files\/(\d+)\/(?:download|preview|stream)/)
    if (m && m[1]) { await fileApi.downloadFile(m[1], name); return }

    // 4) 兜底：直链（可能 401，尽量避免）
    if (!path) return
    const url = path.startsWith('http') ? path : `${baseURL}${path}`
    const a = document.createElement('a')
    a.href = url
    a.download = name
    document.body.appendChild(a)
    a.click()
    a.remove()
  } catch {}
}

// 动画分数：在分数变化时平滑过渡
const animatedScore = ref(0)
const scorePercent = computed(() => {
  const max = Number(assignment.totalScore || 100)
  const val = Math.max(0, Math.min(max, Number(animatedScore.value || 0)))
  return max > 0 ? (val / max) * 100 : 0
})

let animRaf: number | null = null
watch(() => gradeForm.score, (target) => {
  const start = Number(animatedScore.value || 0)
  const end = Number(target || 0)
  const duration = 300
  const startTs = performance.now()
  if (animRaf) cancelAnimationFrame(animRaf)
  const tick = (now: number) => {
    const p = Math.min(1, (now - startTs) / duration)
    animatedScore.value = start + (end - start) * p
    if (p < 1) animRaf = requestAnimationFrame(tick)
  }
  animRaf = requestAnimationFrame(tick)
})

// 初始化 animatedScore
onMounted(() => { animatedScore.value = Number(gradeForm.score || 0) })

const saveDraft = async () => {
  isDraftSaving.value = true
  try {
    const payload: any = {
      submissionId: String(submission.id),
      studentId: String(submission.studentId),
      assignmentId: String(assignment.id),
      score: Number(gradeForm.score || 0),
      maxScore: Number(assignment.totalScore || 100),
      feedback: gradeForm.feedback || '',
      strengths: gradeForm.strengths || '',
      improvements: gradeForm.improvements || '',
      allowResubmit: !!gradeForm.allowResubmit,
      status: 'draft' as const,
      publishImmediately: false
    }
    await gradeApi.gradeSubmission(payload)
    uiStore.showNotification({
      type: 'success',
      title: t('teacher.grading.notify.draftSaved'),
      message: t('teacher.grading.notify.draftSavedMsg')
    })
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.saveFailed'),
      message: t('teacher.grading.notify.saveFailedMsg')
    })
  } finally {
    isDraftSaving.value = false
  }
}

const submitGrade = async () => {
  validateScore()
  validateFeedback()
  
  if (!isFormValid.value) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.formInvalid'),
      message: t('teacher.grading.notify.formInvalidMsg')
    })
    return
  }

  isSubmitting.value = true
  try {
    const payload: any = {
      studentId: String(submission.studentId),
      assignmentId: String(assignment.id),
      score: Number(gradeForm.score),
      maxScore: Number(assignment.totalScore || 100),
      feedback: gradeForm.feedback || '',
      strengths: gradeForm.strengths || '',
      improvements: gradeForm.improvements || '',
      allowResubmit: !!gradeForm.allowResubmit,
      publishImmediately: !!gradeForm.publishImmediately,
      status: (gradeForm.publishImmediately ? 'published' : 'draft') as 'published' | 'draft'
    }
    if (submission.id) payload.submissionId = String(submission.id)
    if (gradeForm.allowResubmit && gradeForm.resubmitUntil) payload.resubmitUntil = String(gradeForm.resubmitUntil).replace('T',' ') + ':00'
    const gr = await gradeApi.gradeSubmission(payload)
    const gradeId = (gr as any)?.id
    if (gradeForm.publishImmediately && gradeId) {
      // 仅调用发布接口，通知改为由后端自动触发
      await gradeApi.publishGrade(String(gradeId))
      try {
        // 随提交评分：优先使用本次会话的 AI 结果；否则回退读取最近AI报告
        if (lastAiNormalized.value) {
          const ov: any = getOverall(lastAiNormalized.value)
          const dm: any = (ov && ov.dimension_averages) ? ov.dimension_averages : {}
        const dimResp: any = await abilityApi.getAbilityDimensions()
        const dims: any[] = Array.isArray(dimResp?.data) ? dimResp.data : (Array.isArray(dimResp) ? dimResp : [])
          const codeToId = new Map<string, any>()
          for (const d of dims) {
            const code = String((d?.code || '').toString() || '')
            if (code && d?.id != null) codeToId.set(code, d.id)
          }
          const pairs: Array<{ code: string; value: number }> = [
            { code: 'LEARNING_ABILITY', value: Number(dm.ability || 0) },
            { code: 'LEARNING_ATTITUDE', value: Number(dm.attitude || 0) },
            { code: 'LEARNING_METHOD', value: Number(dm.strategy || 0) },
            { code: 'MORAL_COGNITION', value: Number(dm.moral_reasoning || 0) }
          ]
          const evidence = String(ov?.holistic_feedback || '')
          await Promise.all(pairs.map(p => {
            const did = codeToId.get(p.code); if (!did) return Promise.resolve()
            const score5 = Math.max(0, Math.min(5, Math.round((Number(p.value || 0)) * 10) / 10))
            // 归档口径：与后端统计口径对齐，relatedId 使用 assignmentId 以便通过 assignments 关联 courseId
            return abilityApi.recordTeacherAssessment({ studentId: submission.studentId, dimensionId: did, score: score5, assessmentType: 'assignment', relatedId: assignment.id, evidence }) as any
          }))
          // 提交评分后：保存完整归一化报告到 ability_reports（学生端可见）
          await abilityApi.createReportFromAi({
            studentId: submission.studentId,
            normalizedJson: JSON.stringify(lastAiNormalized.value),
            title: `${assignment.title || '作业'} · AI批改报告`,
            courseId: assignment.courseId || undefined,
            assignmentId: assignment.id || undefined,
            submissionId: submission.id || undefined,
            aiHistoryId: lastAiHistoryId.value || undefined
          } as any)
        } else {
          // 回退：拉取最近AI报告解析四维并写入
          try {
            const repResp: any = await abilityApi.getTeacherLatestReportOfStudent({
              studentId: String(submission.studentId),
              courseId: assignment.courseId || undefined,
              assignmentId: assignment.id || undefined,
              submissionId: submission.id || undefined
            })
            const rep = repResp?.data || repResp
            // 从 trendsAnalysis / normalizedJson 读取完整规范化 JSON
            const raw = String(
              rep?.normalizedJson || rep?.normalized_json ||
              rep?.trendsAnalysis || rep?.trends_analysis ||
              rep?.rawJson || rep?.raw_json || ''
            )
            if (raw) {
              let obj: any = null
              try { obj = JSON.parse(raw) } catch { obj = null }
              if (obj) {
                const ov: any = getOverall(obj)
                const dm: any = (ov && ov.dimension_averages) ? ov.dimension_averages : {}
                const dimResp: any = await abilityApi.getAbilityDimensions()
                const dims: any[] = Array.isArray(dimResp?.data) ? dimResp.data : (Array.isArray(dimResp) ? dimResp : [])
                const codeToId = new Map<string, any>()
                for (const d of dims) {
                  const code = String((d?.code || '').toString() || '')
                  if (code && d?.id != null) codeToId.set(code, d.id)
                }
                const pairs: Array<{ code: string; value: number }> = [
                  { code: 'LEARNING_ABILITY', value: Number(dm.ability || 0) },
                  { code: 'LEARNING_ATTITUDE', value: Number(dm.attitude || 0) },
                  { code: 'LEARNING_METHOD', value: Number(dm.strategy || 0) },
                  { code: 'MORAL_COGNITION', value: Number(dm.moral_reasoning || 0) }
                ]
                const evidence = String(ov?.holistic_feedback || '')
                await Promise.all(pairs.map(p => {
                  const did = codeToId.get(p.code); if (!did) return Promise.resolve()
                  const score5 = Math.max(0, Math.min(5, Math.round((Number(p.value || 0)) * 10) / 10))
                  return abilityApi.recordTeacherAssessment({ studentId: submission.studentId, dimensionId: did, score: score5, assessmentType: 'assignment', relatedId: assignment.id, evidence }) as any
                }))
              }
            }
          } catch {}
        }
        // 无论是否有AI结果，都将学业成绩写入第五维（ACADEMIC_GRADE）
        const dimResp2: any = await abilityApi.getAbilityDimensions()
        const dims2: any[] = Array.isArray(dimResp2?.data) ? dimResp2.data : (Array.isArray(dimResp2) ? dimResp2 : [])
        const target = (dims2 || []).find((d: any) => String(d?.code || '') === 'ACADEMIC_GRADE')
        if (target && target.id != null) {
          const pct = Number(assignment.totalScore || 0) > 0 ? (Number(payload.score) / Number(assignment.totalScore)) * 100 : 0
          const score5 = Math.max(0, Math.min(5, Math.round((pct / 20) * 10) / 10))
          await abilityApi.recordTeacherAssessment({
            studentId: submission.studentId,
            dimensionId: target.id,
            score: score5,
            assessmentType: 'assignment',
            relatedId: assignment.id,
            evidence: String(payload.feedback || '')
          })
        }
      } catch {}
    }
    
    uiStore.showNotification({
      type: 'success',
      title: t('teacher.grading.notify.submitSuccess'),
      message: gradeForm.publishImmediately ? t('teacher.grading.notify.submitPublished') : t('teacher.grading.notify.submitSaved')
    })
    // 本页 UI 状态更新：将状态改为 graded，副标题与徽章即时反映
    submission.status = 'graded'
    // 跳转回该作业的提交列表，便于继续批改
    router.push(`/teacher/assignments/${assignment.id}/submissions`)
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('teacher.grading.notify.submitFailed'),
      message: t('teacher.grading.notify.submitFailedMsg')
    })
  } finally {
    isSubmitting.value = false
  }
}

const viewStudentProfile = () => {
  const sid = String(submission.studentId || '')
  const cid = assignment?.courseId != null ? String(assignment.courseId) : ''
  const ctitle = assignment?.courseName || ''
  const sname = submission?.studentName || ''
  router.push({
    path: `/teacher/students/${sid}`,
    query: {
      courseId: cid || undefined,
      courseTitle: ctitle || undefined,
      name: sname || undefined
    }
  })
}

const chat = useChatStore()
const contactStudent = () => {
  if (!submission.studentId) return
  chat.openChat(String(submission.studentId), String(submission.studentName || ''), String(assignment.courseId || ''))
}

const viewOtherSubmissions = () => {
  router.push(`/teacher/assignments/${assignment.id}/submissions?highlightStudentId=${submission.studentId}`)
}

const exportSubmission = async () => {
  if (!submission.id) return
  try {
    const blob = await submissionApi.exportSubmission(String(submission.id))
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `submission_${submission.id}.zip`
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (e) {
    uiStore.showNotification({ type: 'error', title: t('teacher.analytics.messages.exportFailed') || 'Export failed', message: t('teacher.analytics.messages.exportFailedMsg') || 'Please try again later' })
  }
}

// 生成 AI 建议：优先使用附件，否则使用文本内容
async function generateAiSuggestion() {
  if (!submission.id) return
  aiLoading.value = true
  try {
    // 1) 取附件ID（purpose=submission）
    let fileIds: number[] = []
    try {
      const res: any = await fileApi.getRelatedFiles('submission', String(submission.id))
      const list: any[] = Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : [])
      fileIds = list.map((f: any) => Number(f?.id)).filter((n: any) => Number.isFinite(n))
    } catch {}

    // 2) 调用 AI 批改
    let normalized: any = null
    if (fileIds.length > 0) {
      const resp: any = await aiGradingApi.gradeFiles({ fileIds, jsonOnly: true, useGradingPrompt: true })
      const results = (resp?.data?.results || resp?.results || []) as any[]
      const ok = results.find(r => !r?.error) || results[0]
      const raw = ok?.result
      normalized = normalizeAssessment(raw)
    } else if (String(submission.content || '').trim()) {
      const payload = { messages: [{ role: 'user', content: String(submission.content) }], jsonOnly: true, useGradingPrompt: true }
      const resp: any = await aiGradingApi.gradeEssay(payload as any)
      const raw = resp?.data ?? resp
      normalized = normalizeAssessment(raw)
    } else {
      throw new Error('无可用于AI评估的文本或附件')
    }

    if (!normalized) throw new Error('AI 未返回有效结果')
    const ov = getOverall(normalized)
    const final5 = Number(ov?.final_score ?? 0)
    const dim = (ov?.dimension_averages || {}) as any
    // 0-5 → 总分制（以作业总分换算）
    const suggested = Math.round(((final5 / 5) * Number(assignment.totalScore || 100)) * 10) / 10

    const improvements: string[] = []
    const pushSugs = (grp: any) => {
      if (!grp) return
      for (const k of Object.keys(grp)) {
        const sec = (grp as any)[k]
        const arr = Array.isArray(sec?.suggestions) ? sec.suggestions : []
        for (const s of arr) { const v = String(s || '').trim(); if (v) improvements.push(v); if (improvements.length >= 8) break }
        if (improvements.length >= 8) break
      }
    }
    pushSugs(normalized?.moral_reasoning)
    pushSugs(normalized?.attitude_development)
    pushSugs(normalized?.ability_growth)
    pushSugs(normalized?.strategy_optimization)

    // 统一以 overall.dimension_averages 显示四维（确保包含 blooms_level 等子项贡献）
    aiSuggestion.value = {
      suggestedScore: suggested,
      dims: {
        moral: Number(dim.moral_reasoning || 0),
        attitude: Number(dim.attitude || 0),
        ability: Number(dim.ability || 0),
        strategy: Number(dim.strategy || 0)
      },
      analysis: String(ov?.holistic_feedback || ''),
      improvements
    }

    uiStore.showNotification({ type: 'success', title: t('teacher.grading.ai.title'), message: t('teacher.grading.notify.aiApplied') })
    // 刷新：以最新能力报告作为展示来源（若AI已生成过报告或此前已有报告）
    try { await loadLatestAiReportForStudent() } catch {}
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.ai.title'), message: e?.message || t('teacher.grading.notify.loadFailed') })
  } finally {
    aiLoading.value = false
  }
}

// writeRadarFromAi 逻辑已取消（自动写入改为随提交评分进行）

// 注意：全局仅保留一处 getOverall 定义，避免重复

function normalizeAssessment(obj: any) {
  try {
    if (!obj || typeof obj !== 'object') return obj
    if ((obj as any).overall || (obj as any).final_score || (obj as any).dimension_averages) return obj
    if (Array.isArray((obj as any).evaluation_result)) return normalizeFromEvaluationArray((obj as any).evaluation_result)
    if ((obj as any).evaluation && typeof (obj as any).evaluation === 'object') return normalizeFromEvaluationObject((obj as any).evaluation)
    return obj
  } catch { return obj }
}

// 生成整体反馈（与历史/学生页一致的规则）
function buildHolisticFeedback(out: any): string {
  try {
    const parts: string[] = []
    const avg = out?.overall?.dimension_averages || {}
    const finalScore = Number(out?.overall?.final_score ?? 0)
    const avgLine = `Averages — Moral: ${avg.moral_reasoning ?? 0}, Attitude: ${avg.attitude ?? 0}, Ability: ${avg.ability ?? 0}, Strategy: ${avg.strategy ?? 0}. Final: ${finalScore}/5.`
    parts.push(avgLine)
    const allSuggestions: string[] = []
    const pushFromGroup = (grp: any) => {
      if (!grp) return
      for (const key of Object.keys(grp)) {
        const sec = (grp as any)[key]
        const arr = Array.isArray(sec?.suggestions) ? sec.suggestions : []
        for (const s of arr) {
          const v = String(s || '').trim()
          if (v && allSuggestions.length < 6) allSuggestions.push(v)
        }
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

function normalizeFromEvaluationArray(arr: any[]) {
  const out: any = { moral_reasoning: {}, attitude_development: {}, ability_growth: {}, strategy_optimization: {} }
  const getSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    const ev = raw?.evidence
    let evidence: any[] = []
    if (Array.isArray(ev?.quotes)) {
      const explanations = Array.isArray(ev?.explanations) ? ev.explanations : undefined
      evidence = ev.quotes.map((q: any, idx: number) => ({
        quote: String(q),
        reasoning: String(ev?.reasoning || ''),
        conclusion: String(ev?.conclusion || ''),
        explanation: explanations && explanations[idx] != null ? String(explanations[idx]) : String(ev?.explanation || ev?.analysis || '')
      }))
    }
    const suggestions = Array.isArray(raw?.suggestions) ? raw.suggestions : []
    return { score, evidence, suggestions }
  }
  const mapDim = (name: string) => {
    const n = (name || '').toLowerCase()
    if (n.includes('moral')) return 'moral_reasoning'
    if (n.includes('attitude')) return 'attitude_development'
    if (n.includes('ability')) return 'ability_growth'
    if (n.includes('strategy')) return 'strategy_optimization'
    return ''
  }
  const mapId = (dimKey: string, id: string) => {
    const i = (id || '').toUpperCase()
    const tbl: Record<string, Record<string, string>> = {
      moral_reasoning: { '1A': 'stage_level', '1B': 'foundations_balance', '1C': 'argument_chain' },
      attitude_development: { '2A': 'emotional_engagement', '2B': 'resilience', '2C': 'focus_flow' },
      ability_growth: { '3A': 'blooms_level', '3B': 'metacognition', '3C': 'transfer' },
      strategy_optimization: { '4A': 'diversity', '4B': 'depth', '4C': 'self_regulation' }
    }
    return tbl[dimKey]?.[i] || ''
  }
  for (const g of (arr || [])) {
    const dimKey = mapDim(g?.dimension || '')
    if (!dimKey) continue
    const items = Array.isArray(g?.sub_criteria) ? g.sub_criteria : []
    for (const it of items) {
      const secKey = mapId(dimKey, String(it?.id || ''))
      if (!secKey) continue
      ;(out as any)[dimKey][secKey] = getSec(it)
    }
  }
  const avg = (nums: number[]) => { const arrv = nums.filter(n => Number.isFinite(n)); if (!arrv.length) return 0; return Math.round((arrv.reduce((a, b) => a + b, 0) / arrv.length) * 10) / 10 }
  const mr = avg([ Number(out.moral_reasoning?.stage_level?.score), Number(out.moral_reasoning?.foundations_balance?.score), Number(out.moral_reasoning?.argument_chain?.score) ])
  const ad = avg([ Number(out.attitude_development?.emotional_engagement?.score), Number(out.attitude_development?.resilience?.score), Number(out.attitude_development?.focus_flow?.score) ])
  const ag = avg([ Number(out.ability_growth?.blooms_level?.score), Number(out.ability_growth?.metacognition?.score), Number(out.ability_growth?.transfer?.score) ])
  const so = avg([ Number(out.strategy_optimization?.diversity?.score), Number(out.strategy_optimization?.depth?.score), Number(out.strategy_optimization?.self_regulation?.score) ])
  out.overall = { dimension_averages: { moral_reasoning: mr, attitude: ad, ability: ag, strategy: so }, final_score: avg([mr, ad, ag, so]), holistic_feedback: '' }
  if (!out.overall.holistic_feedback) {
    out.overall.holistic_feedback = buildHolisticFeedback(out)
  }
  return out
}

function normalizeFromEvaluationObject(evaluation: any) {
  const toSec = (raw: any) => {
    const score = Number(raw?.score ?? 0)
    let evidence: any[] = []
    const ev = raw?.evidence
    if (Array.isArray(ev?.quotes)) {
      const explanations = Array.isArray(ev?.explanations) ? ev.explanations : undefined
      evidence = ev.quotes.map((q: any, idx: number) => ({
        quote: String(q),
        reasoning: String(raw?.reasoning || ev?.reasoning || ''),
        conclusion: String(raw?.conclusion || ev?.conclusion || ''),
        explanation: explanations && explanations[idx] != null ? String(explanations[idx]) : String(raw?.explanation || ev?.explanation || raw?.analysis || ev?.analysis || '')
      }))
    }
    const suggestions = Array.isArray(raw?.suggestions) ? raw.suggestions : []
    return { score, evidence, suggestions }
  }
  const findGroup = (obj: any, hint: string) => { const k = Object.keys(obj || {}).find(k => k.toLowerCase().includes(hint)); return k ? obj[k] : undefined }
  const findSubKey = (obj: any, hints: string[]) => {
    const keys = Object.keys(obj || {})
    const lowerHints = hints.map(h => String(h).toLowerCase())
    const hit = keys.find(k => lowerHints.every(h => k.toLowerCase().includes(h)))
    return hit ? obj[hit] : undefined
  }
  const moral = evaluation["1) Moral Reasoning Maturity"] || findGroup(evaluation, 'moral reasoning') || {}
  const attitude = evaluation["2) Learning Attitude Development"] || findGroup(evaluation, 'attitude') || {}
  const ability = evaluation["3) Learning Ability Growth"] || findGroup(evaluation, 'ability') || {}
  const strategy = evaluation["4) Learning Strategy Optimization"] || findGroup(evaluation, 'strategy') || {}
  const out: any = {}
  out.moral_reasoning = { stage_level: toSec(moral['1A. Stage Level Identification'] || moral['1A']), foundations_balance: toSec(moral['1B. Breadth of Moral Foundations'] || moral['1B']), argument_chain: toSec(moral['1C. Argument Chains and Counter-arguments'] || moral['1C']) }
  out.attitude_development = { emotional_engagement: toSec(attitude['2A. Emotional Engagement'] || attitude['2A']), resilience: toSec(attitude['2B. Persistence/Resilience'] || attitude['2B']), focus_flow: toSec(attitude['2C. Task Focus / Flow'] || attitude['2C']) }
  out.ability_growth = {
    blooms_level: toSec(
      ability['3A. Bloom\'s Taxonomy Progression'] ||
      findSubKey(ability, ['3a', 'bloom']) ||
      ability['3A']
    ),
    metacognition: toSec(ability['3B. Metacognition (Plan–Monitor–Revise)'] || ability['3B']),
    transfer: toSec(ability['3C. Knowledge Transfer'] || ability['3C'])
  }
  out.strategy_optimization = { diversity: toSec(strategy['4A. Strategy Diversity'] || strategy['4A']), depth: toSec(strategy['4B. Depth of Processing'] || strategy['4B']), self_regulation: toSec(strategy['4C. Self-Regulation'] || strategy['4C']) }
  const avg = (nums: number[]) => { const arr = nums.filter(n => Number.isFinite(n)); if (!arr.length) return 0; return Math.round((arr.reduce((a, b) => a + b, 0) / arr.length) * 10) / 10 }
  const mr = avg([ Number(out.moral_reasoning?.stage_level?.score), Number(out.moral_reasoning?.foundations_balance?.score), Number(out.moral_reasoning?.argument_chain?.score) ])
  const ad = avg([ Number(out.attitude_development?.emotional_engagement?.score), Number(out.attitude_development?.resilience?.score), Number(out.attitude_development?.focus_flow?.score) ])
  const ag = avg([ Number(out.ability_growth?.blooms_level?.score), Number(out.ability_growth?.metacognition?.score), Number(out.ability_growth?.transfer?.score) ])
  const so = avg([ Number(out.strategy_optimization?.diversity?.score), Number(out.strategy_optimization?.depth?.score), Number(out.strategy_optimization?.self_regulation?.score) ])
  out.overall = { dimension_averages: { moral_reasoning: mr, attitude: ad, ability: ag, strategy: so }, final_score: avg([mr, ad, ag, so]), holistic_feedback: '' }
  if (!out.overall.holistic_feedback) out.overall.holistic_feedback = (function(o: any){
    try {
      const parts: string[] = []
      const avgm = o?.overall?.dimension_averages || {}
      const finalScore = Number(o?.overall?.final_score ?? 0)
      const avgLine = `Averages — Moral: ${avgm.moral_reasoning ?? 0}, Attitude: ${avgm.attitude ?? 0}, Ability: ${avgm.ability ?? 0}, Strategy: ${avgm.strategy ?? 0}. Final: ${finalScore}/5.`
      parts.push(avgLine)
      const all: string[] = []
      const pushGrp = (grp: any) => {
        if (!grp) return
        for (const key of Object.keys(grp)) {
          const sec = (grp as any)[key]
          const arr = Array.isArray(sec?.suggestions) ? sec.suggestions : []
          for (const s of arr) { const v = String(s||'').trim(); if (v && all.length < 6) all.push(v) }
        }
      }
      pushGrp(o.moral_reasoning); pushGrp(o.attitude_development); pushGrp(o.ability_growth); pushGrp(o.strategy_optimization)
      if (all.length) { parts.push('Key suggestions:'); for (const s of all) parts.push(`- ${s}`) }
      return parts.join('\n')
    } catch { return '' }
  })(out)
  return out
}

function goCourse() {
  if (assignmentCourseId.value) {
    router.push(`/teacher/courses/${assignmentCourseId.value}`)
  }
}

const showReport = ref(false)
const showReturn = ref(false)
const reportForm = reactive({ reason: '', details: '' })
const returnForm = reactive({ reason: '', resubmitUntil: '' as any })
const openReturnModal = () => { showReturn.value = true }
const confirmReturn = async () => {
  try {
    if (!returnForm.reason || !returnForm.reason.trim()) {
      uiStore.showNotification({ type: 'error', title: t('teacher.grading.actions.returnForResubmit') as any, message: (t('teacher.grading.errors.feedbackRequired') as any) || '请填写打回原因' })
      return
    }
    if (!returnForm.resubmitUntil) {
      uiStore.showNotification({ type: 'error', title: t('teacher.grading.actions.returnForResubmit') as any, message: (t('teacher.grading.errors.resubmitUntilRequired') as any) || '请选择重交截止时间' })
      return
    }
    let gid = (gradingHistory.value?.[0] as any)?.id || (await (async ()=>{
      try {
        const g: any = await gradeApi.getGradeForStudentAssignment(String(submission.studentId), String(assignment.id))
        return (g as any)?.id
      } catch { return null }
    })())
    // 若尚无成绩记录，则先创建一条“草稿”成绩作为载体
    if (!gid) {
      const payload: any = {
        studentId: String(submission.studentId),
        assignmentId: String(assignment.id),
        submissionId: String(submission.id || ''),
        score: 0,
        maxScore: Number(assignment.totalScore || 100),
        feedback: returnForm.reason || '',
        allowResubmit: true,
        status: 'draft',
        publishImmediately: false
      }
      if (returnForm.resubmitUntil) payload.resubmitUntil = String(returnForm.resubmitUntil).replace('T',' ') + ':00'
      try {
        const gr: any = await gradeApi.gradeSubmission(payload)
        gid = (gr as any)?.id || (gr as any)?.data?.id || null
      } catch {}
    }
    if (!gid) throw new Error('no gradeId')
    const data: any = { reason: returnForm.reason }
    if (returnForm.resubmitUntil) data.resubmitUntil = String(returnForm.resubmitUntil).replace('T',' ') + ':00'
    await gradeApi.returnForResubmission(String(gid), data)
    submission.status = 'returned'
    uiStore.showNotification({ type: 'success', title: t('teacher.grading.actions.returnForResubmit') as any, message: t('teacher.grading.notify.submitSaved') as any })
    showReturn.value = false
  } catch (e: any) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.actions.returnForResubmit') as any, message: e?.message || 'Failed' })
  }
}
const reportPlagiarism = () => { showReport.value = true }
const submitReport = async () => {
  if (!reportForm.reason.trim()) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.quick.report'), message: t('teacher.grading.errors.feedbackRequired') })
    return
  }
  try {
    await reportApi.createReport({
      reportedStudentId: String(submission.studentId || ''),
      courseId: String(assignment.courseId || ''),
      assignmentId: String(assignment.id || ''),
      submissionId: String(submission.id || ''),
      reason: reportForm.reason,
      details: reportForm.details || ''
    })
    uiStore.showNotification({ type: 'success', title: t('teacher.grading.quick.report'), message: t('teacher.grading.notify.submitSuccess') })
    showReport.value = false
    reportForm.reason = ''
    reportForm.details = ''
  } catch (e) {
    uiStore.showNotification({ type: 'error', title: t('teacher.grading.quick.report'), message: t('teacher.grading.notify.submitFailed') })
  }
}

// 生命周期：先加载提交与作业，再按上下文加载最新AI报告
onMounted(async () => {
  await loadSubmission()
  await loadLatestAiReportForStudent()
  // 可选：如需保留历史入口可继续加载
  // await loadAiHistory()
})
</script> 