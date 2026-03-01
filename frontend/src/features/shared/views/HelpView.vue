<template>
  <div class="min-h-screen relative">
    <div class="relative z-10 max-w-6xl mx-auto p-6">
      <!-- 页面标题 -->
      <div class="mb-8">
        <page-header :title="t('shared.help.title')" :subtitle="t('shared.help.subtitle')" />
      </div>

      

      <!-- 顶部指标（StartCard 四卡） -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-4">
        <div class="cursor-pointer" @click="activeSection = 'articles'">
          <start-card :label="t('shared.help.cards.categories') || '分类总数'"
                     :value="categoriesCount"
                     tone="blue"
                     :icon="BookOpenIcon" />
            </div>
        <div class="cursor-pointer" @click="activeSection = 'articles'">
          <start-card :label="t('shared.help.cards.hotArticles') || '热门文章'"
                     :value="hotArticlesCount"
                     tone="emerald"
                     :icon="ChartBarIcon" />
          </div>
        <div class="cursor-pointer" @click="activeSection = 'support'">
          <start-card :label="t('shared.help.cards.myTickets') || '我的工单'"
                     :value="myTicketsCount"
                     tone="violet"
                     :icon="ChatBubbleLeftIcon" />
        </div>
        <div class="cursor-pointer" @click="activeSection = 'articles'">
          <start-card :label="t('shared.help.cards.new7d') || '近7日新增'"
                     :value="newArticles7dCount"
                     tone="amber"
                     :icon="LightBulbIcon" />
        </div>
      </div>

      

      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧导航 -->
        <div class="lg:col-span-1">
          <card padding="lg" tint="secondary" class="sticky top-6 pt-2">
            <template #header>
              <h2 class="text-lg font-semibold text-base-content">{{ t('shared.help.nav') || '导航' }}</h2>
            </template>
            <nav class="space-y-2 pb-4">
              <button
                v-for="section in sections"
                :key="section.id"
                @click="activeSection = section.id"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                :class="activeSection === section.id
                  ? 'bg-emerald-500/15 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-200 ring-1 ring-emerald-500/20'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700'"
              >
                {{ section.title }}
              </button>
              <button
                @click="router.push('/docs')"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                {{ t('shared.help.sections.docs') || '文档' }}
              </button>
            </nav>
          </card>
        </div>

        <!-- 右侧内容 -->
        <div class="lg:col-span-3 space-y-8">
          <!-- 文章列表 -->
          <section v-if="activeSection === 'articles'" id="articles">
            <card padding="lg" tint="secondary">
              <template #header>
                <div class="flex items-center justify-between gap-3 flex-wrap">
                  <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.articles') || '帮助文章' }}</h2>
                  <div class="flex items-center gap-2">
                    <Button size="sm" :variant="sortMode==='latest' ? 'primary' : 'outline'" @click="changeSort('latest')">{{ t('shared.common.latest') || '最新' }}</Button>
                    <Button size="sm" :variant="sortMode==='hot' ? 'primary' : 'outline'" @click="changeSort('hot')">{{ t('shared.common.hot') || '最热' }}</Button>
                  </div>
                </div>
              </template>

              <!-- 分类筛选 -->
              <div class="flex flex-wrap gap-2 mb-4">
                <Button
                  size="sm"
                  :variant="selectedCategoryId===null ? 'primary' : 'outline'"
                  @click="pickCategory(null)">
                  {{ t('shared.common.all') || '全部' }}
                </Button>
                <Button
                  v-for="c in helpStore.categories"
                  :key="c.id"
                  size="sm"
                  :variant="selectedCategoryId===c.id ? 'primary' : 'outline'"
                  @click="pickCategory(c.id)">
                  {{ c.name }}
                </Button>
              </div>

              <!-- 文章卡片列表 -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <card v-for="a in helpStore.articles" :key="a.id" padding="lg" hoverable class="cursor-pointer" @click="openArticle(a.slug)">
                  <h3 class="font-medium text-base-content mb-2">{{ a.title }}</h3>
                  <p class="text-sm text-subtle line-clamp-3" v-if="a.contentMd">{{ a.contentMd.slice(0, 120) }}...</p>
                  <div class="mt-3 text-xs text-subtle flex items-center justify-between">
                    <span>{{ (a.views || 0) }} {{ t('shared.common.views') || '浏览' }}</span>
                    <span>{{ (a.upVotes || 0) - (a.downVotes || 0) }} {{ t('shared.common.score') || '评分' }}</span>
                  </div>
                </card>
              </div>

              <div v-if="!helpStore.loading && helpStore.articles.length===0" class="text-sm text-subtle py-6 text-center">
                {{ t('shared.common.empty') || '暂无内容' }}
              </div>
            </card>
          </section>

          <!-- 文章详情 -->
          <section v-if="activeSection === 'articleDetail' && helpStore.article" id="articleDetail">
            <card padding="lg" tint="secondary">
              <template #header>
                <div class="flex items-center gap-3 flex-wrap">
                  <h2 class="text-xl font-semibold text-base-content">{{ helpStore.article?.title }}</h2>
                  <div class="inline-flex items-center gap-2 flex-nowrap whitespace-nowrap">
                    <Button size="sm" variant="outline" @click="activeSection='articles'">{{ t('shared.common.back') || '返回' }}</Button>
                    <Button size="sm" :loading="isVoting" @click="voteArticle(true)">{{ t('shared.help.actions.useful') || '有帮助' }}</Button>
                    <Button size="sm" variant="outline" :loading="isVoting" @click="voteArticle(false)">{{ t('shared.help.actions.notUseful') || '没帮助' }}</Button>
                  </div>
                </div>
              </template>

              <div class="prose dark:prose-invert max-w-none" v-html="helpStore.article?.contentHtml || ''"></div>
            </card>
          </section>
          <!-- 常见问题（daisyUI Accordion with plus/minus） -->
          <section v-if="activeSection === 'faq'" id="faq">
            <card padding="lg" tint="secondary">
              <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.faq') || '常见问题' }}</h2>
              </template>
              
              <div class="space-y-8">
                <div
                  v-for="category in faqCategories"
                  :key="category.id"
                  class="last:pb-0"
                >
                  <h3 class="text-lg font-medium text-base-content mb-4">{{ category.name }}</h3>
                  <div class="join join-vertical w-full rounded-2xl overflow-hidden border border-base-300">
                    <div
                      v-for="(faq, idx) in category.faqs"
                      :key="faq.id"
                      class="collapse collapse-plus join-item bg-base-100 border-base-300 border"
                    >
                      <input type="radio" :name="`faq-accordion-${category.id}`" :checked="idx === 0" />
                      <div class="collapse-title font-medium text-base-content">{{ faq.question }}</div>
                      <div class="collapse-content text-sm text-subtle">
                        <div v-html="faq.answer"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </card>
          </section>

          <!-- 使用指南 -->
          <section v-if="activeSection === 'guide'" id="guide">
            <card padding="lg" tint="secondary">
              <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.guide') || '使用指南' }}</h2>
              </template>
              
              <div class="space-y-8">
                <div v-for="guide in userGuidesFiltered" :key="guide.id">
                  <h3 class="text-lg font-medium text-base-content mb-4 flex items-center">
                    <component :is="guide.icon" class="w-5 h-5 mr-2 text-emerald-600 dark:text-emerald-400" />
                    {{ guide.title }}
                  </h3>
                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div
                      v-for="step in guide.steps"
                      :key="step.id"
                      class="border border-gray-200 dark:border-gray-600 rounded-lg p-4"
                    >
                      <div class="flex items-center mb-3">
                        <span class="w-6 h-6 bg-emerald-600 dark:bg-emerald-500 text-white rounded-full flex items-center justify-center text-sm font-medium mr-3">
                          {{ step.step }}
                        </span>
                        <h4 class="font-medium text-base-content">{{ step.title }}</h4>
                      </div>
                      <p class="text-sm text-subtle">{{ step.description }}</p>
                      <Button
                        v-if="step.action"
                        variant="outline"
                        size="sm"
                        class="mt-3"
                        @click="step.action"
                      >
                        {{ step.actionText }}
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            </card>
          </section>

          

          <!-- 技术支持 -->
          <section v-if="activeSection === 'support'" id="support">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- 联系方式 -->
              <card padding="lg" tint="secondary">
                <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.support') || '联系技术支持' }}</h2>
                </template>
                
                <div class="space-y-6">
                  <div v-for="contact in supportContacts" :key="contact.type">
                    <div class="flex items-start space-x-3">
                      <div class="flex-shrink-0">
                        <div class="w-10 h-10 rounded-lg flex items-center justify-center"
                             :class="contact.iconBg">
                          <component :is="contact.icon" class="w-5 h-5 text-white" />
                        </div>
                      </div>
                      <div class="flex-1">
                        <h3 class="font-medium text-base-content">{{ contact.title }}</h3>
                        <p class="text-sm text-subtle mb-2">{{ contact.description }}</p>
                        <div class="text-sm">
                          <p class="text-base-content font-medium">{{ contact.value }}</p>
                          <p class="text-subtle">{{ contact.time }}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </card>

              <!-- 提交工单 -->
              <card padding="lg" tint="secondary">
                <template #header>
                <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.ticket') || '提交支持单' }}</h2>
                </template>
                
                <form @submit.prevent="submitTicket" class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">诉求分类</label>
                    <glass-popover-select
                      v-model="ticketForm.channel as any"
                      tint="info"
                      :options="[
                        { label: '技术支持', value: 'support' },
                        { label: '意见反馈', value: 'feedback' }
                      ]"
                      stacked
                    />
                  </div>

                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.type') || '问题类型' }}</label>
                    <glass-popover-select
                      v-model="ticketForm.type as any"
                      tint="accent"
                      :options="[
                        { label: t('shared.help.form.selectType') || '选择问题类型', value: '', disabled: true },
                        { label: t('shared.help.form.typeTechnical') || '技术问题', value: 'technical' },
                        { label: t('shared.help.form.typeAccount') || '账户问题', value: 'account' },
                        { label: t('shared.help.form.typeFeature') || '功能建议', value: 'feature' },
                        { label: t('shared.help.form.typeBug') || '错误报告', value: 'bug' },
                        { label: '使用体验', value: 'experience' },
                        { label: '意见反馈', value: 'feedback' },
                        { label: t('shared.help.form.typeOther') || '其他', value: 'other' }
                      ]"
                      :placeholder="t('shared.help.form.selectType') || '选择问题类型'"
                      stacked
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.title') || '问题标题' }}</label>
                    <glass-input
                      v-model="ticketForm.title"
                      type="text"
                      tint="secondary"
                      :placeholder="t('shared.help.form.titlePh') || '简要描述您的问题'"
                    />
                  </div>
                  
                  <div>
                    <div class="flex items-center justify-between gap-3 mb-1">
                      <label class="block text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('shared.help.form.description') || '问题详情' }}</label>
                      <emoji-picker
                        size="sm"
                        variant="primary"
                        tint="accent"
                        :hideLabelOnSmall="true"
                        :buttonClass="'!text-white whitespace-nowrap shrink-0'"
                        @select="onTicketDescriptionEmojiSelect"
                      />
                    </div>
                    <glass-textarea
                      v-model="ticketForm.description"
                      tint="secondary"
                      :rows="4"
                      :placeholder="t('shared.help.form.descriptionPh') || '请详细描述您遇到的问题...'"
                    />
                  </div>

                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.contact') || '联系方式 (可选)' }}</label>
                    <glass-input
                      v-model="ticketForm.contact"
                      type="text"
                      tint="secondary"
                      :placeholder="t('shared.help.form.contactPh') || '如需回复，请留下您的邮箱或电话'"
                    />
                  </div>
                  
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">{{ t('shared.help.form.priority') || '优先级' }}</label>
                    <glass-popover-select
                      v-model="ticketForm.priority as any"
                      tint="secondary"
                      :options="[
                        { label: t('shared.help.form.priLow') || '低', value: 'low' },
                        { label: t('shared.help.form.priMedium') || '中', value: 'medium' },
                        { label: t('shared.help.form.priHigh') || '高', value: 'high' },
                        { label: t('shared.help.form.priUrgent') || '紧急', value: 'urgent' }
                      ]"
                      stacked
                    />
                  </div>

                  <Button
                    type="submit"
                    variant="primary"
                    class="w-full"
                    :loading="isSubmittingTicket"
                  >
                    <PaperAirplaneIcon class="w-4 h-4 mr-2" />
                    {{ t('shared.help.actions.submitTicket') || '提交工单' }}
                  </Button>
                </form>
              </card>
              
              <!-- 我的工单（仅登录后显示） -->
              <div v-if="authStore.isAuthenticated" ref="myTicketsPanelRef" class="md:col-span-2">
                <card padding="lg" tint="secondary">
                  <template #header>
                    <div class="flex items-center justify-between gap-3 flex-wrap">
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-2xl bg-indigo-500/15 text-indigo-600 dark:text-indigo-300 flex items-center justify-center">
                          <TicketIcon class="w-5 h-5" />
                        </div>
                        <div>
                          <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.myTickets') || '我的工单' }}</h2>
                          <p class="text-xs text-subtle">
                            {{ isZh ? '查看管理员回复，并在同一条支持单继续补充信息' : 'Review admin replies and continue the conversation in one support ticket' }}
                          </p>
                        </div>
                      </div>
                      <Button size="sm" variant="info" @click="helpStore.fetchMyTickets()">
                        <ArrowPathIcon class="w-4 h-4 mr-2" />
                        {{ t('shared.common.latest') || '最新' }}
                      </Button>
                    </div>
                  </template>

                  <div v-if="helpStore.tickets.length === 0" class="text-sm text-subtle">
                    {{ t('shared.common.empty') || '暂无内容' }}
                  </div>

                  <div v-else class="space-y-3">
                    <div
                      v-for="ticket in helpStore.tickets"
                      :key="ticket.id"
                      class="rounded-2xl p-4 transition glass-ultraThin border border-white/20 dark:border-white/10 shadow-sm"
                      :class="isCurrentTicket(ticket.id)
                        ? 'glass-tint-info border-cyan-300/50'
                        : 'glass-tint-secondary hover:bg-white/10'"
                    >
                      <div class="flex items-start justify-between gap-3 flex-wrap">
                        <div class="min-w-0 flex-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="font-medium text-base-content">{{ ticket.title }}</div>
                            <Badge size="sm" :variant="ticketStatusVariant(ticket.status)">
                              {{ ticketStatusText(ticket.status) }}
                            </Badge>
                          </div>

                          <div class="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 text-xs">
                            <div>
                              <span class="text-subtle">{{ isZh ? '工单ID' : 'Ticket ID' }}:</span>
                              <span class="text-base-content ml-1">#{{ ticket.id }}</span>
                            </div>
                            <div>
                              <span class="text-subtle">{{ isZh ? '分类' : 'Channel' }}:</span>
                              <span class="text-base-content ml-1">{{ ticketChannelText(ticket.channel) }}</span>
                            </div>
                            <div>
                              <span class="text-subtle">{{ isZh ? '类型' : 'Type' }}:</span>
                              <span class="text-base-content ml-1">{{ ticketTypeText(ticket.ticketType) }}</span>
                            </div>
                            <div>
                              <span class="text-subtle">{{ isZh ? '创建时间' : 'Created' }}:</span>
                              <span class="text-base-content ml-1">{{ formatTicketTime(ticket.createdAt) }}</span>
                            </div>
                          </div>
                        </div>

                        <div class="inline-flex items-center gap-2 flex-nowrap whitespace-nowrap">
                          <Button size="sm" variant="primary" class="whitespace-nowrap" @click="openTicketDetail(ticket.id)">
                            <EyeIcon class="w-4 h-4 mr-1.5" />
                            {{ isZh ? '查看工单' : 'Open' }}
                          </Button>
                          <Button size="sm" variant="danger" class="whitespace-nowrap" @click="deleteTicket(ticket.id)">
                            <TrashIcon class="w-4 h-4 mr-1.5" />
                            {{ t('shared.common.delete') || '删除' }}
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div v-if="helpStore.ticketDetail?.ticket" class="mt-6 pt-6 border-t border-white/15 dark:border-white/10">
                    <div class="rounded-3xl p-5 glass-thin glass-tint-accent border border-white/20 dark:border-white/10 shadow-sm">
                      <div class="flex items-start justify-between gap-3 flex-wrap">
                        <div class="min-w-0">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="font-semibold text-base-content">{{ helpStore.ticketDetail.ticket.title }}</div>
                            <Badge size="sm" :variant="ticketStatusVariant(helpStore.ticketDetail.ticket.status)">
                              {{ ticketStatusText(helpStore.ticketDetail.ticket.status) }}
                            </Badge>
                          </div>
                          <div class="mt-3 grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-x-4 gap-y-1 text-xs">
                            <div>
                              <span class="text-subtle">{{ isZh ? '工单ID' : 'Ticket ID' }}:</span>
                              <span class="text-base-content ml-1">#{{ helpStore.ticketDetail.ticket.id }}</span>
                            </div>
                            <div>
                              <span class="text-subtle">{{ isZh ? '分类' : 'Channel' }}:</span>
                              <span class="text-base-content ml-1">{{ ticketChannelText(helpStore.ticketDetail.ticket.channel) }}</span>
                            </div>
                            <div>
                              <span class="text-subtle">{{ isZh ? '类型' : 'Type' }}:</span>
                              <span class="text-base-content ml-1">{{ ticketTypeText(helpStore.ticketDetail.ticket.ticketType) }}</span>
                            </div>
                            <div>
                              <span class="text-subtle">{{ isZh ? '最近回复' : 'Last reply' }}:</span>
                              <span class="text-base-content ml-1">{{ formatTicketTime(helpStore.ticketDetail.ticket.lastReplyAt || helpStore.ticketDetail.ticket.updatedAt || helpStore.ticketDetail.ticket.createdAt) }}</span>
                            </div>
                          </div>
                        </div>
                        <Button size="sm" variant="info" @click="openTicketDetail(helpStore.ticketDetail.ticket.id)">
                          <ArrowPathIcon class="w-4 h-4 mr-1.5" />
                          {{ isZh ? '刷新详情' : 'Refresh' }}
                        </Button>
                      </div>

                      <div class="mt-5 space-y-3 max-h-80 overflow-auto pr-1">
                        <div
                          v-for="message in helpStore.ticketDetail.messages"
                          :key="message.id"
                          class="rounded-2xl px-4 py-3 border glass-ultraThin"
                          :class="message.senderSide === 'admin'
                            ? 'glass-tint-success border-emerald-300/30'
                            : 'glass-tint-info border-white/15 dark:border-white/10'"
                        >
                          <div class="flex items-center gap-2 flex-wrap text-xs mb-2">
                            <span class="inline-flex items-center gap-1 text-subtle">
                              <ClockIcon class="w-3.5 h-3.5" />
                              {{ formatTicketTime(message.createdAt) }}
                            </span>
                            <span class="text-subtle">·</span>
                            <span class="text-base-content">
                              {{ message.senderSide === 'admin'
                                ? (isZh ? '管理员回复' : 'Admin reply')
                                : (isZh ? '我的补充' : 'My reply') }}
                            </span>
                            <span v-if="message.senderName" class="text-subtle">· {{ message.senderName }}</span>
                          </div>
                          <div class="text-sm text-base-content whitespace-pre-line">{{ message.content }}</div>
                        </div>
                      </div>

                      <form class="mt-5 space-y-3" @submit.prevent="replyToCurrentTicket">
                        <div class="flex items-center justify-between gap-3">
                          <div class="text-sm font-medium text-base-content">
                            {{ isZh ? '继续补充信息' : 'Add more details' }}
                          </div>
                          <emoji-picker
                            size="sm"
                            variant="primary"
                            tint="accent"
                            :hideLabelOnSmall="true"
                            :buttonClass="'!text-white whitespace-nowrap shrink-0'"
                            @select="onTicketReplyEmojiSelect"
                          />
                        </div>
                        <glass-textarea
                          v-model="ticketReply"
                          tint="accent"
                          :rows="3"
                          :placeholder="isZh ? '继续补充问题，或回复管理员' : 'Add more details or reply to the admin'"
                        />
                        <div class="flex justify-end">
                          <Button type="submit" size="sm" variant="primary" :loading="isReplyingTicket" :disabled="!ticketReply.trim()">
                            <PaperAirplaneIcon class="w-4 h-4 mr-1.5" />
                            {{ isZh ? '发送回复' : 'Send reply' }}
                          </Button>
                        </div>
                      </form>
                    </div>
                  </div>
                </card>
              </div>
            </div>
          </section>

          

          <!-- 反馈 -->
          <section v-if="activeSection === 'feedback'" id="feedback">
            <card padding="lg" tint="secondary">
              <template #header>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ t('shared.help.sections.feedback') || '意见反馈' }}</h2>
              </template>

              <div class="space-y-5">
                <p class="text-sm text-subtle">
                  意见反馈已合并到统一的支持单表单中。先选择反馈类型，再跳转到“技术支持”页面填写内容，管理员会在同一条单据里处理和回复。
                </p>

                <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <button
                    v-for="type in feedbackTypeOptions"
                    :key="type.value"
                    type="button"
                    class="flex items-center p-3 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
                    @click="startFeedbackTicket(type.value)"
                  >
                    <component :is="type.icon" class="w-5 h-5 mr-2 text-gray-600" />
                    <span class="text-sm font-medium">{{ type.label }}</span>
                  </button>
                </div>

                <div class="flex justify-end">
                  <Button type="button" variant="primary" @click="startFeedbackTicket('feedback')">
                    去填写统一支持单
                  </Button>
                </div>
              </div>
            </card>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useUIStore } from '@/stores/ui'
import Card from '@/components/ui/Card.vue'
import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StartCard from '@/components/ui/StartCard.vue'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
// 动态背景已移除
import { useI18n } from 'vue-i18n'
const { t, locale } = useI18n()
import { useHelpStore } from '@/stores/help'
import { useAuthStore } from '@/stores/auth'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowPathIcon,
  EyeIcon,
  MagnifyingGlassIcon,
  PaperAirplaneIcon,
  QuestionMarkCircleIcon,
  BookOpenIcon,
  PhoneIcon,
  ChatBubbleLeftIcon,
  ClockIcon,
  EnvelopeIcon,
  ChevronDownIcon,
  ExclamationTriangleIcon,
  LightBulbIcon,
  HeartIcon,
  BugAntIcon,
  AcademicCapIcon,
  CogIcon,
  TicketIcon,
  TrashIcon,
  UserGroupIcon,
  ChartBarIcon
} from '@heroicons/vue/24/outline'


// Stores
const uiStore = useUIStore()
const helpStore = useHelpStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const isZh = computed(() => String(locale.value || '').toLowerCase().startsWith('zh'))

// 状态
const activeSection = ref('faq')
const searchQuery = ref('')
const expandedFaqs = ref<string[]>([])
const isSubmittingTicket = ref(false)
const isReplyingTicket = ref(false)
const selectedCategoryId = ref<number | null>(null)
const sortMode = ref<'latest' | 'hot'>('hot')
const isVoting = ref(false)
const ticketReply = ref('')
const myTicketsPanelRef = ref<HTMLElement | null>(null)
const lastHandledRouteTicketId = ref<number | null>(null)

// 表单数据
const ticketForm = reactive({
  channel: 'support',
  type: '',
  title: '',
  description: '',
  contact: '',
  priority: 'medium'
})

// 顶部 StartCard 统计
const categoriesCount = computed(() => (helpStore.categories?.length || 0))
const hotArticlesCount = computed(() => (helpStore.articles?.length || 0))
const myTicketsCount = computed(() => (authStore.isAuthenticated ? (helpStore.tickets?.length || 0) : 0))
const newArticles7dCount = computed(() => {
  const list = helpStore.articles || []
  const now = Date.now()
  const sevenDays = 7 * 24 * 60 * 60 * 1000
  let cnt = 0
  for (const a of list) {
    const ts = a?.createdAt ? Date.parse(String(a.createdAt)) : NaN
    if (!isNaN(ts) && (now - ts) <= sevenDays) cnt++
  }
  return cnt
})

// 导航菜单（随语言切换动态更新）
const sections = computed(() => ([
  { id: 'faq', title: t('shared.help.sections.faq') || '常见问题' },
  { id: 'articles', title: t('shared.help.sections.articles') || '帮助文章' },
  { id: 'guide', title: t('shared.help.sections.guide') || '使用指南' },
  { id: 'support', title: t('shared.help.sections.support') || '技术支持' },
  { id: 'feedback', title: t('shared.help.sections.feedback') || '意见反馈' }
]))

// FAQ数据（i18n 驱动）
const faqCategories = computed(() => ([
  {
    id: 'account',
    name: t('shared.help.faq.account.title') || '账户相关',
    faqs: [
      {
        id: 'login',
        question: t('shared.help.faq.account.login.q') || '忘记密码怎么办？',
        answer: t('shared.help.faq.account.login.a') || '您可以在登录页面点击"忘记密码"链接…'
      },
      {
        id: 'profile',
        question: t('shared.help.faq.account.profile.q') || '如何修改个人信息？',
        answer: t('shared.help.faq.account.profile.a') || '登录后前往个人资料页面进行修改…'
      },
      {
        id: 'security',
        question: t('shared.help.faq.account.security.q') || '如何确保账户安全？',
        answer: t('shared.help.faq.account.security.a') || '建议使用强密码、定期更换、勿在公共设备保存登录信息…'
      },
      {
        id: 'support',
        question: t('shared.help.faq.account.support.q') || '如何联系技术支持？',
        answer: t('shared.help.faq.account.support.a') || '您可以通过页面的“技术支持”模块使用邮箱或微信联系。'
      }
    ]
  },
  {
    id: 'courses',
    name: t('shared.help.faq.courses.title') || '课程相关',
    faqs: [
      {
        id: 'enrollment',
        question: t('shared.help.faq.courses.enrollment.q') || '如何报名课程？',
        answer: t('shared.help.faq.courses.enrollment.a') || '在课程列表页面选择课程并报名…'
      },
      {
        id: 'progress',
        question: t('shared.help.faq.courses.progress.q') || '学习进度如何计算？',
        answer: t('shared.help.faq.courses.progress.a') || '按已完成课时统计，并在课程详情页查看…'
      },
      {
        id: 'submit',
        question: t('shared.help.faq.courses.submit.q') || '如何提交作业？',
        answer: t('shared.help.faq.courses.submit.a') || '前往“作业”页面，在待提交列表中进入作业详情，按要求填写/上传后提交。'
      }
    ]
  },
  {
    id: 'technical',
    name: t('shared.help.faq.technical.title') || '技术问题',
    faqs: [
      {
        id: 'browser',
        question: t('shared.help.faq.technical.browser.q') || '支持哪些浏览器？',
        answer: t('shared.help.faq.technical.browser.a') || '为获得最完整体验，请使用 Chrome 最新版本。其他浏览器也可访问，但体验可能受限。'
      },
      {
        id: 'cache',
        question: t('shared.help.faq.technical.cache.q') || '遇到异常如何清除缓存？',
        answer: t('shared.help.faq.technical.cache.a') || '在浏览器设置中清除站点缓存与 Cookie，然后重新登录再试。'
      },
      {
        id: 'uploads',
        question: t('shared.help.faq.technical.uploads.q') || '上传失败怎么办？',
        answer: t('shared.help.faq.technical.uploads.a') || '请检查文件格式与大小是否符合页面提示，确保网络稳定后重试。若仍失败，请联系技术支持。'
      }
    ]
  }
]))

// 使用指南（中英文切换、按角色展示）
const userGuides = computed(() => ([
  {
    id: 'student',
    title: t('shared.help.guide.student.title') || '学生使用指南',
    icon: AcademicCapIcon,
    steps: [
      {
        id: 1,
        step: 1,
        title: t('shared.help.guide.student.steps.browseCourses.title') || '浏览课程',
        description: t('shared.help.guide.student.steps.browseCourses.desc') || '在课程库中检索并预览适合你的课程。',
        actionText: t('shared.help.guide.student.steps.browseCourses.action') || '去浏览',
        action: () => router.push({ name: 'StudentCourses' })
      },
      {
        id: 2,
        step: 2,
        title: t('shared.help.guide.student.steps.submitAssignment.title') || '提交作业',
        description: t('shared.help.guide.student.steps.submitAssignment.desc') || '在作业中心查看待交作业并按要求上传提交。',
        actionText: t('shared.help.guide.student.steps.submitAssignment.action') || '去提交',
        action: () => router.push({ name: 'StudentAssignments' })
      },
      {
        id: 3,
        step: 3,
        title: t('shared.help.guide.student.steps.viewGrades.title') || '浏览成绩',
        description: t('shared.help.guide.student.steps.viewGrades.desc') || '在成绩中心查看每门课程的成绩与评语。',
        actionText: t('shared.help.guide.student.steps.viewGrades.action') || '去查看',
        action: () => router.push({ name: 'StudentAnalysis' })
      },
      {
        id: 4,
        step: 4,
        title: t('shared.help.guide.student.steps.askAI.title') || '询问AI',
        description: t('shared.help.guide.student.steps.askAI.desc') || '打开学习助手，针对课程/作业提出问题并获得建议。',
        actionText: t('shared.help.guide.student.steps.askAI.action') || '去提问',
        action: () => router.push({ name: 'StudentAssistant' })
      },
      {
        id: 5,
        step: 5,
        title: t('shared.help.guide.student.steps.communityQA.title') || '社区答疑',
        description: t('shared.help.guide.student.steps.communityQA.desc') || '在学习社区向同学与老师求助，互相解答问题。',
        actionText: t('shared.help.guide.student.steps.communityQA.action') || '去交流',
        action: () => router.push({ name: 'StudentCommunity' })
      }
    ]
  },
  {
    id: 'teacher',
    title: t('shared.help.guide.teacher.title') || '教师使用指南',
    icon: UserGroupIcon,
    steps: [
      {
        id: 1,
        step: 1,
        title: t('shared.help.guide.teacher.steps.createCourse.title') || '创建课程',
        description: t('shared.help.guide.teacher.steps.createCourse.desc') || '在课程管理中创建并完善课程结构与内容。',
        actionText: t('shared.help.guide.teacher.steps.createCourse.action') || '去创建',
        action: () => router.push({ name: 'TeacherCourseManagement', query: { create: '1' } })
      },
      {
        id: 2,
        step: 2,
        title: t('shared.help.guide.teacher.steps.manageStudents.title') || '管理学生',
        description: t('shared.help.guide.teacher.steps.manageStudents.desc') || '查看学生学习情况，邀请/移除学生并导出数据。',
        actionText: t('shared.help.guide.teacher.steps.manageStudents.action') || '去管理',
        action: async () => {
          try {
            if (authStore.userRole === 'TEACHER') {
              const { useCourseStore } = await import('@/stores/course')
              const cs = useCourseStore()
              if (!Array.isArray(cs.courses) || cs.courses.length === 0) {
                const teacherId = authStore.user?.id
                if (teacherId) {
                  await cs.fetchCourses({ page: 1, size: 10, teacherId: String(teacherId) })
                }
              }
              const first = Array.isArray(cs.courses) ? cs.courses[0] : null
              if (first && (first as any).id) {
                return router.push({ name: 'TeacherCourseStudents', params: { id: (first as any).id } })
              }
            }
          } catch {}
          return router.push({ name: 'TeacherCourseManagement' })
        }
      },
      {
        id: 3,
        step: 3,
        title: t('shared.help.guide.teacher.steps.gradeAssignments.title') || '批改作业',
        description: t('shared.help.guide.teacher.steps.gradeAssignments.desc') || '在作业管理中查看提交并进行评分与反馈。',
        actionText: t('shared.help.guide.teacher.steps.gradeAssignments.action') || '去批改',
        action: () => router.push({ name: 'TeacherAssignments' })
      },
      {
        id: 4,
        step: 4,
        title: t('shared.help.guide.teacher.steps.aiGrading.title') || 'AI批改',
        description: t('shared.help.guide.teacher.steps.aiGrading.desc') || '使用 AI 辅助批改，提升批改效率与一致性。',
        actionText: t('shared.help.guide.teacher.steps.aiGrading.action') || '去体验',
        action: () => router.push({ name: 'TeacherAIGrading' })
      },
      {
        id: 5,
        step: 5,
        title: t('shared.help.guide.teacher.steps.analytics.title') || '数据分析',
        description: t('shared.help.guide.teacher.steps.analytics.desc') || '查看课程关键指标与学生表现，辅助教学决策。',
        actionText: t('shared.help.guide.teacher.steps.analytics.action') || '去分析',
        action: () => router.push({ name: 'TeacherAnalytics' })
      },
      {
        id: 6,
        step: 6,
        title: t('shared.help.guide.teacher.steps.askAI.title') || '询问AI',
        description: t('shared.help.guide.teacher.steps.askAI.desc') || '打开教学助手，撰写内容、出题与答疑辅助。',
        actionText: t('shared.help.guide.teacher.steps.askAI.action') || '去咨询',
        action: () => router.push({ name: 'TeacherAssistant' })
      },
      {
        id: 7,
        step: 7,
        title: t('shared.help.guide.teacher.steps.communityQA.title') || '社区答疑',
        description: t('shared.help.guide.teacher.steps.communityQA.desc') || '在社区与学生互动，答疑解惑并分享教学经验。',
        actionText: t('shared.help.guide.teacher.steps.communityQA.action') || '去交流',
        action: () => router.push({ name: 'TeacherCommunity' })
      }
    ]
  }
]))

// 按角色过滤显示对应指南（随语言切换自动更新）
const userGuidesFiltered = computed(() => {
  const all = userGuides.value
  const role = authStore.userRole
  if (role === 'TEACHER') return all.filter(g => g.id === 'teacher')
  return all.filter(g => g.id === 'student')
})

// 移除视频教程（无后端支持）

// 技术支持联系方式（i18n 驱动，仅邮箱 + 微信）
const supportContacts = computed(() => ([
  {
    type: 'email',
    title: t('shared.help.support.email.title') || '邮件支持',
    description: t('shared.help.support.email.desc') || '非紧急问题请发送邮件',
    value: 'xiaorui537537@gmail.com',
    time: t('shared.help.support.email.time') || '24小时内回复',
    icon: EnvelopeIcon,
    iconBg: 'bg-blue-500'
  },
  {
    type: 'wechat',
    title: t('shared.help.support.wechat.title') || '微信 WeChat',
    description: t('shared.help.support.wechat.desc') || '添加微信获取支持',
    value: 'Xiao_Rui537',
    time: t('shared.help.support.wechat.time') || '工作日 9:00-18:00',
    icon: ChatBubbleLeftIcon,
    iconBg: 'bg-emerald-500'
  }
]))

// 移除系统状态（无后端支持）

// 反馈类型（随语言切换动态更新）
const feedbackTypeOptions = computed(() => ([
  { value: 'bug', label: t('shared.help.form.typeBug') || '错误报告', icon: BugAntIcon },
  { value: 'feature', label: t('shared.help.form.typeFeature') || '功能建议', icon: LightBulbIcon },
  { value: 'experience', label: t('shared.help.form.typeExperience') || '用户体验', icon: HeartIcon },
  { value: 'other', label: t('shared.help.form.typeOther') || '其他反馈', icon: ChatBubbleLeftIcon }
]))

// 方法
const scrollToSection = (sectionId: string) => {
  activeSection.value = sectionId
}
function pickCategory(id: number | null) {
  selectedCategoryId.value = id
  helpStore.searchArticles(searchQuery.value || undefined, id === null ? undefined : id, undefined, sortMode.value)
}

function changeSort(s: 'latest' | 'hot') {
  sortMode.value = s
  helpStore.searchArticles(searchQuery.value || undefined, selectedCategoryId.value === null ? undefined : selectedCategoryId.value, undefined, sortMode.value)
}

async function openArticle(slug: string) {
  await helpStore.fetchArticle(slug, true)
  activeSection.value = 'articleDetail'
}

async function voteArticle(helpful: boolean) {
  if (!helpStore.article) return
  isVoting.value = true
  try {
    await helpStore.voteOrFeedback(helpStore.article.id, { helpful })
    uiStore.showNotification({ type: 'success', title: t('shared.help.feedback.thanks') || '感谢反馈', message: '' })
  } finally {
    isVoting.value = false
  }
}

async function deleteTicket(id: number) {
  if (!confirm(t('shared.common.confirm') || '确认删除？')) return
  await helpStore.deleteTicket(id)
  uiStore.showNotification({ type: 'success', title: t('shared.common.deleted') || '已删除', message: '' })
}

async function openTicketDetail(id: number) {
  try {
    ticketReply.value = ''
    await helpStore.fetchTicketDetail(id)
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: isZh.value ? '错误' : 'Error',
      message: isZh.value ? '加载单据详情失败' : 'Failed to load ticket details'
    })
  }
}

async function focusTicketFromRoute() {
  const rawTicketId = Number((route.query as any)?.ticketId || 0)
  if (!rawTicketId || !authStore.isAuthenticated) {
    lastHandledRouteTicketId.value = null
    return
  }

  const shouldHandle = lastHandledRouteTicketId.value !== rawTicketId
    || !isCurrentTicket(rawTicketId)

  if (!shouldHandle) return

  lastHandledRouteTicketId.value = rawTicketId
  activeSection.value = 'support'

  try {
    await helpStore.fetchMyTickets()
    await openTicketDetail(rawTicketId)
    await nextTick()
    myTicketsPanelRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } catch {}
}

const searchHelp = () => {
  helpStore.searchArticles(searchQuery.value || undefined)
}

const toggleFaq = (faqId: string) => {
  const index = expandedFaqs.value.indexOf(faqId)
  if (index > -1) {
    expandedFaqs.value.splice(index, 1)
  } else {
    expandedFaqs.value.push(faqId)
  }
}

// 移除视频/状态相关方法

const ticketStatusText = (s: string) => {
  switch (s) {
    case 'open': return t('shared.help.ticket.open') || '已创建'
    case 'in_progress': return t('shared.help.ticket.inProgress') || '处理中'
    case 'resolved': return t('shared.help.ticket.resolved') || '已解决'
    case 'closed': return t('shared.help.ticket.closed') || '已关闭'
    default: return s
  }
}

const ticketStatusVariant = (s?: string): 'warning' | 'info' | 'success' | 'secondary' => {
  switch (s) {
    case 'open': return 'warning'
    case 'in_progress': return 'info'
    case 'resolved': return 'success'
    case 'closed': return 'secondary'
    default: return 'secondary'
  }
}

const ticketChannelText = (channel?: string) => {
  if (channel === 'feedback') return t('shared.help.sections.feedback') || (isZh.value ? '反馈' : 'Feedback')
  if (channel === 'support') return t('shared.help.sections.support') || (isZh.value ? '支持' : 'Support')
  return channel || (isZh.value ? '未知' : 'Unknown')
}

const ticketTypeText = (type?: string) => {
  switch (String(type || '')) {
    case 'technical': return t('shared.help.form.typeTechnical') || (isZh.value ? '技术问题' : 'Technical')
    case 'account': return t('shared.help.form.typeAccount') || (isZh.value ? '账户问题' : 'Account')
    case 'feature': return t('shared.help.form.typeFeature') || (isZh.value ? '功能建议' : 'Feature')
    case 'bug': return t('shared.help.form.typeBug') || (isZh.value ? '错误报告' : 'Bug')
    case 'experience': return t('shared.help.form.typeExperience') || (isZh.value ? '用户体验' : 'Experience')
    case 'feedback': return t('shared.help.sections.feedback') || (isZh.value ? '意见反馈' : 'Feedback')
    case 'other': return t('shared.help.form.typeOther') || (isZh.value ? '其他' : 'Other')
    default: return type || (isZh.value ? '未分类' : 'Uncategorized')
  }
}

const formatTicketTime = (value?: string) => {
  if (!value) return isZh.value ? '暂无' : 'N/A'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(locale.value || (isZh.value ? 'zh-CN' : 'en-US'), {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: !isZh.value
  }).format(date)
}

const isCurrentTicket = (id?: number) => Number(helpStore.ticketDetail?.ticket?.id || 0) === Number(id || 0)

const onTicketDescriptionEmojiSelect = (emoji: string) => {
  ticketForm.description = `${ticketForm.description || ''}${emoji}`
}

const onTicketReplyEmojiSelect = (emoji: string) => {
  ticketReply.value = `${ticketReply.value || ''}${emoji}`
}

const startFeedbackTicket = (type: string) => {
  ticketForm.channel = 'feedback'
  ticketForm.type = type
  if (!ticketForm.title.trim()) {
    ticketForm.title = '意见反馈'
  }
  activeSection.value = 'support'
}

const submitTicket = async () => {
  if (!authStore.isAuthenticated) {
    uiStore.showNotification({ type: 'warning', title: t('shared.common.tip') || '提示', message: t('auth.loginRequired') || '请先登录再提交工单' })
    return router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
  }
  if (!ticketForm.type || !ticketForm.title || !ticketForm.description) {
    uiStore.showNotification({
      type: 'warning',
      title: t('shared.help.validation.incomplete') || '表单不完整',
      message: t('shared.help.validation.fillRequired') || '请填写所有必填字段'
    })
    return
  }

  isSubmittingTicket.value = true
  try {
    await helpStore.submitTicket({
      title: ticketForm.title.trim(),
      content: ticketForm.description.trim(),
      channel: ticketForm.channel as 'support' | 'feedback',
      ticketType: ticketForm.type,
      priority: ticketForm.priority as 'low' | 'medium' | 'high' | 'urgent',
      contact: ticketForm.contact.trim() || undefined
    })
    uiStore.showNotification({
      type: 'success',
      title: t('shared.help.notify.ticketSuccessTitle') || '工单提交成功',
      message: t('shared.help.notify.ticketSuccessMsg') || '我们会尽快处理您的问题并与您联系'
    })

    // 重置表单
    Object.assign(ticketForm, {
      channel: 'support',
      type: '',
      title: '',
      description: '',
      contact: '',
      priority: 'medium'
    })
    await helpStore.fetchMyTickets()
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('shared.help.notify.submitFailTitle') || '提交失败',
      message: t('shared.help.notify.ticketFailMsg') || '提交工单时发生错误，请稍后重试'
    })
  } finally {
    isSubmittingTicket.value = false
  }
}

const replyToCurrentTicket = async () => {
  const currentId = helpStore.ticketDetail?.ticket?.id
  if (!currentId || !ticketReply.value.trim()) return
  isReplyingTicket.value = true
  try {
    await helpStore.replyTicket(currentId, ticketReply.value.trim())
    ticketReply.value = ''
    uiStore.showNotification({
      type: 'success',
      title: isZh.value ? '已发送' : 'Sent',
      message: isZh.value ? '已追加到当前支持单' : 'Your reply has been added to this ticket'
    })
    await helpStore.fetchMyTickets()
  } catch (error) {
    uiStore.showNotification({
      type: 'error',
      title: t('shared.help.notify.submitFailTitle') || '提交失败',
      message: isZh.value ? '追加回复失败，请稍后重试' : 'Failed to add your reply. Please try again later'
    })
  } finally {
    isReplyingTicket.value = false
  }
}

// 生命周期
onMounted(() => {
  helpStore.fetchCategories().catch(() => {})
  // 初始加载热门文章
  helpStore.searchArticles(undefined, undefined, undefined, 'hot').catch(() => {})
  if (authStore.isAuthenticated) {
    helpStore.fetchMyTickets().catch(() => {})
  }
  // 恢复分区：优先 query.section，其次 hash
  try {
    const sectionFromQuery = String((route.query as any)?.section || '').trim()
    const sectionFromHash = String((route.hash || '').replace(/^#/, '')).trim()
    const candidate = sectionFromQuery || sectionFromHash
    const allowed = new Set(['faq','articles','guide','support','feedback','articleDetail'])
    if (candidate && allowed.has(candidate)) {
      activeSection.value = candidate
    }
  } catch {}
  focusTicketFromRoute().catch(() => {})
})

// 分区变化时写回到 URL，避免主题/语言切换导致重置
watch(activeSection, (s) => {
  try {
    const q = { ...(route.query as any), section: s }
    router.replace({ query: q })
  } catch {}
})

watch(
  () => [route.query.ticketId, authStore.isAuthenticated],
  () => {
    focusTicketFromRoute().catch(() => {})
  }
)
</script> 
