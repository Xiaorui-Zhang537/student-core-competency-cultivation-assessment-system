<template>
  <div class="p-6 space-y-6">
    <page-header
      :title="t('admin.sidebar.tools') || '通知中心'"
      :subtitle="copy.subtitle"
    />

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      <start-card
        :label="copy.summary.pendingLabel"
        :value="pendingTicketCount"
        :description="copy.summary.pendingDesc"
        tone="amber"
        :icon="InboxStackIcon"
      />
      <start-card
        :label="copy.summary.inProgressLabel"
        :value="inProgressTicketCount"
        :description="copy.summary.inProgressDesc"
        tone="blue"
        :icon="ClockIcon"
      />
      <start-card
        :label="copy.summary.resolvedLabel"
        :value="resolvedTicketCount"
        :description="copy.summary.resolvedDesc"
        tone="emerald"
        :icon="CheckCircleIcon"
      />
      <start-card
        :label="copy.summary.audienceLabel"
        :value="broadcastAudienceValue"
        :description="broadcastAudienceDesc"
        tone="violet"
        :icon="BellAlertIcon"
      />
    </div>

    <div class="grid grid-cols-1 gap-6 items-start">
      <card padding="lg" tint="accent" class="overflow-hidden">
        <template #header>
          <div class="flex items-center justify-between gap-3 flex-wrap">
            <div class="flex items-center gap-3">
              <div class="w-11 h-11 rounded-2xl bg-amber-500/15 text-amber-600 dark:text-amber-300 flex items-center justify-center">
                <BellAlertIcon class="w-5 h-5" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-base-content">{{ copy.broadcast.panelTitle }}</h2>
                <p class="text-xs text-subtle mt-1">{{ copy.broadcast.panelDesc }}</p>
              </div>
            </div>
            <Button size="sm" variant="warning" @click="resetBroadcastForm">
              <ArrowPathIcon class="w-4 h-4 mr-2" />
              {{ copy.broadcast.reset }}
            </Button>
          </div>
        </template>

        <div class="space-y-4">
          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-warning border border-white/20 dark:border-white/10 shadow-sm">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div class="rounded-xl px-4 py-3 bg-white/10 border border-white/10">
                <div class="text-xs text-subtle">{{ copy.broadcast.overviewAudience }}</div>
                <div class="text-sm font-semibold text-base-content mt-1">{{ broadcastAudienceValue }}</div>
              </div>
              <div class="rounded-xl px-4 py-3 bg-white/10 border border-white/10">
                <div class="text-xs text-subtle">{{ copy.broadcast.overviewType }}</div>
                <div class="text-sm font-semibold text-base-content mt-1">{{ broadcastTypeLabel }}</div>
              </div>
              <div class="rounded-xl px-4 py-3 bg-white/10 border border-white/10">
                <div class="text-xs text-subtle">{{ copy.broadcast.overviewPriority }}</div>
                <div class="text-sm font-semibold text-base-content mt-1">{{ broadcastPriorityLabel }}</div>
              </div>
            </div>
          </div>

          <div class="rounded-2xl p-5 space-y-4 glass-ultraThin glass-tint-accent border border-white/20 dark:border-white/10 shadow-sm">
            <div class="grid grid-cols-1 xl:grid-cols-5 gap-4 items-start">
              <div class="xl:col-span-2">
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.titleLabel }}</div>
                <glass-input v-model="broadcastTitle" :placeholder="copy.broadcast.titlePlaceholder" />
              </div>

              <div class="xl:col-span-3">
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.contentLabel }}</div>
                <div class="flex items-start gap-3">
                  <div class="flex-1 min-w-0">
                    <glass-textarea v-model="broadcastContent" :rows="5" :placeholder="copy.broadcast.contentPlaceholder" />
                  </div>
                  <emoji-picker
                    size="sm"
                    variant="primary"
                    tint="warning"
                    :hideLabelOnSmall="true"
                    :buttonClass="'!text-white whitespace-nowrap shrink-0'"
                    @select="appendBroadcastEmoji"
                  />
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-3">
              <div>
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.typeLabel }}</div>
                <glass-popover-select v-model="broadcastType" :options="broadcastTypeOptions" size="sm" tint="warning" />
              </div>
              <div>
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.priorityLabel }}</div>
                <glass-popover-select v-model="broadcastPriority" :options="broadcastPriorityOptions" size="sm" tint="accent" />
              </div>
              <div>
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.targetLabel }}</div>
                <glass-popover-select v-model="broadcastTargetType" :options="broadcastTargetOptions" size="sm" tint="info" />
              </div>
              <div v-if="broadcastTargetType === 'role'">
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.roleLabel }}</div>
                <glass-popover-select v-model="broadcastRole" :options="broadcastRoleOptions" size="sm" tint="secondary" />
              </div>
              <div v-else-if="broadcastTargetType === 'specific'">
                <div class="text-xs text-subtle mb-1">{{ copy.broadcast.specificLabel }}</div>
                <glass-search-input v-model="recipientKeyword" :placeholder="copy.broadcast.searchPlaceholder" size="sm" tint="info" />
              </div>
            </div>

            <div v-if="broadcastTargetType === 'specific'" class="space-y-3">
              <div v-if="searchingRecipients" class="text-xs text-subtle px-1">{{ copy.broadcast.searching }}</div>
              <div v-if="recipientResults.length > 0" class="space-y-2 max-h-52 overflow-auto pr-1">
                <button
                  v-for="user in recipientResults"
                  :key="user.id"
                  type="button"
                  class="w-full text-left rounded-2xl px-3 py-3 border border-white/20 dark:border-white/10 bg-white/10 hover:bg-white/15 transition"
                  @click="toggleRecipient(user)"
                >
                  <div class="text-sm font-medium text-base-content">{{ formatRecipientName(user) }}</div>
                  <div class="mt-2 grid grid-cols-1 md:grid-cols-2 gap-x-4 gap-y-1 text-xs">
                    <div>
                      <span class="text-subtle">{{ copy.recipient.userId }}:</span>
                      <span class="text-base-content ml-1">#{{ user.id }}</span>
                    </div>
                    <div>
                      <span class="text-subtle">{{ copy.recipient.role }}:</span>
                      <span class="text-base-content ml-1">{{ roleText(user.role) }}</span>
                    </div>
                    <div>
                      <span class="text-subtle">{{ copy.recipient.username }}:</span>
                      <span class="text-base-content ml-1">{{ user.username || copy.common.notAvailable }}</span>
                    </div>
                    <div>
                      <span class="text-subtle">{{ copy.recipient.email }}:</span>
                      <span class="text-base-content ml-1 break-all">{{ user.email || copy.common.notAvailable }}</span>
                    </div>
                    <div v-if="recipientSecondaryIdValue(user)">
                      <span class="text-subtle">{{ recipientSecondaryIdLabel(user) }}:</span>
                      <span class="text-base-content ml-1">{{ recipientSecondaryIdValue(user) }}</span>
                    </div>
                  </div>
                </button>
              </div>
              <div v-else-if="recipientKeyword.trim() && !searchingRecipients" class="text-xs text-subtle px-1">
                {{ copy.broadcast.empty }}
              </div>

              <div v-if="selectedRecipients.length > 0" class="space-y-2">
                <div class="text-xs text-subtle px-1">{{ copy.broadcast.selectedMembers }}: {{ selectedRecipients.length }}</div>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="user in selectedRecipients"
                    :key="user.id"
                    type="button"
                    class="px-3 py-1 rounded-full text-xs bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300"
                    @click="toggleRecipient(user)"
                  >
                    {{ formatRecipientName(user) }} ×
                  </button>
                </div>
              </div>
            </div>

            <div class="flex flex-wrap gap-3 pt-2">
              <Button
                variant="warning"
                size="sm"
                :disabled="sending || !canSendBroadcast"
                :loading="sending"
                @click="sendBroadcast"
              >
                <PaperAirplaneIcon class="w-4 h-4 mr-2" />
                {{ copy.broadcast.send }}
              </Button>
              <Button
                variant="secondary"
                size="sm"
                :disabled="sending"
                @click="resetBroadcastForm"
              >
                <ArrowPathIcon class="w-4 h-4 mr-2" />
                {{ copy.broadcast.clear }}
              </Button>
            </div>
          </div>
        </div>
      </card>

      <card padding="lg" tint="info" class="overflow-hidden">
        <template #header>
          <div class="flex items-center justify-between gap-3 flex-wrap">
            <div class="flex items-center gap-3">
              <div class="w-11 h-11 rounded-2xl bg-cyan-500/15 text-cyan-600 dark:text-cyan-300 flex items-center justify-center">
                <ChatBubbleLeftRightIcon class="w-5 h-5" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-base-content">{{ copy.support.panelTitle }}</h2>
                <p class="text-xs text-subtle mt-1">{{ copy.support.panelDesc }}</p>
              </div>
            </div>
            <Button size="sm" variant="info" :loading="ticketLoading" @click="fetchTickets(true)">
              <ArrowPathIcon class="w-4 h-4 mr-2" />
              {{ copy.support.refresh }}
            </Button>
          </div>
        </template>

        <div class="space-y-4">
          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-info border border-white/20 dark:border-white/10 shadow-sm">
            <filter-bar tint="secondary" align="center" :dense="false" class="rounded-3xl">
              <template #left>
                <div class="flex items-start gap-3 flex-wrap w-full">
                  <div class="w-full sm:w-auto flex flex-col gap-1">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.support.filters.status }}</span>
                    <div class="w-full sm:w-40">
                      <glass-popover-select
                        v-model="ticketFilters.status"
                        :options="ticketStatusFilterOptions"
                        size="sm"
                        tint="secondary"
                      />
                    </div>
                  </div>
                  <div class="w-full sm:w-auto flex flex-col gap-1">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.support.filters.channel }}</span>
                    <div class="w-full sm:w-40">
                      <glass-popover-select
                        v-model="ticketFilters.channel"
                        :options="ticketChannelFilterOptions"
                        size="sm"
                        tint="secondary"
                      />
                    </div>
                  </div>
                  <div class="w-full sm:w-auto flex flex-col gap-1">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.support.filters.priority }}</span>
                    <div class="w-full sm:w-40">
                      <glass-popover-select
                        v-model="ticketFilters.priority"
                        :options="ticketPriorityFilterOptions"
                        size="sm"
                        tint="secondary"
                      />
                    </div>
                  </div>
                  <div class="w-full lg:flex-1 flex flex-col gap-1 min-w-[16rem]">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.support.filters.search }}</span>
                    <glass-search-input
                      v-model="ticketFilters.q"
                      :placeholder="copy.support.filters.searchPlaceholder"
                      size="sm"
                      tint="info"
                    />
                  </div>
                </div>
              </template>
            </filter-bar>
          </div>

          <div class="rounded-2xl p-5 glass-ultraThin glass-tint-primary border border-white/20 dark:border-white/10 shadow-sm">
            <div class="grid grid-cols-1 xl:grid-cols-2 gap-5">
              <div class="space-y-3 max-h-[40rem] overflow-auto pr-1">
                <div v-if="ticketLoading && tickets.length === 0" class="text-sm text-subtle py-8 text-center">{{ copy.support.loading }}</div>
                <div v-else-if="tickets.length === 0" class="text-sm text-subtle py-8 text-center">{{ copy.support.empty }}</div>

                <button
                  v-for="ticket in tickets"
                  :key="ticket.id"
                  type="button"
                  class="w-full text-left rounded-3xl px-4 py-4 border transition"
                  :class="selectedTicketId === ticket.id
                    ? 'border-cyan-300 bg-cyan-500/10 shadow-sm'
                    : 'border-white/20 dark:border-white/10 bg-white/5 hover:bg-white/10'"
                  @click="openTicket(ticket.id)"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0">
                      <div class="font-medium text-base-content truncate">{{ ticket.title }}</div>
                      <div class="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 text-xs">
                        <div>
                          <span class="text-subtle">{{ copy.support.ticketId }}:</span>
                          <span class="text-base-content ml-1">#{{ ticket.id }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.account }}:</span>
                          <span class="text-base-content ml-1">{{ ticket.username || copy.common.notAvailable }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.submitterRole }}:</span>
                          <span class="text-base-content ml-1">{{ roleText(ticket.userRole) }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.channel }}:</span>
                          <span class="text-base-content ml-1">{{ ticketChannelText(ticket.channel) }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.type }}:</span>
                          <span class="text-base-content ml-1">{{ ticketTypeText(ticket.ticketType) }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.priority }}:</span>
                          <span class="text-base-content ml-1">{{ ticketPriorityText(ticket.priority) }}</span>
                        </div>
                      </div>
                    </div>
                    <Badge size="sm" :variant="ticketStatusVariant(ticket.status)">
                      {{ ticketStatusText(ticket.status) }}
                    </Badge>
                  </div>
                  <div class="text-xs text-subtle mt-3">
                    {{ copy.support.lastUpdated }}:
                    <span class="text-base-content ml-1">{{ formatDateTime(ticket.lastReplyAt || ticket.updatedAt || ticket.createdAt) }}</span>
                  </div>
                </button>
              </div>

              <div class="min-h-[32rem] rounded-3xl border border-white/20 dark:border-white/10 p-4 bg-white/5">
                <div v-if="ticketDetailLoading" class="text-sm text-subtle py-12 text-center">{{ copy.support.detailLoading }}</div>
                <div v-else-if="!selectedTicketDetail" class="text-sm text-subtle py-12 text-center">{{ copy.support.detailEmpty }}</div>

                <div v-else class="space-y-4">
                  <div class="rounded-3xl border border-white/20 dark:border-white/10 p-4 bg-white/5">
                    <div class="flex items-start justify-between gap-3 flex-wrap">
                      <div>
                        <div class="text-lg font-semibold text-base-content">{{ selectedTicketDetail.ticket.title }}</div>
                        <div class="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-x-4 gap-y-1 text-xs">
                          <div>
                            <span class="text-subtle">{{ copy.support.ticketId }}:</span>
                            <span class="text-base-content ml-1">#{{ selectedTicketDetail.ticket.id }}</span>
                          </div>
                          <div>
                            <span class="text-subtle">{{ copy.support.account }}:</span>
                            <span class="text-base-content ml-1">{{ selectedTicketDetail.ticket.username || copy.common.notAvailable }}</span>
                          </div>
                          <div>
                            <span class="text-subtle">{{ copy.support.submitterRole }}:</span>
                            <span class="text-base-content ml-1">{{ roleText(selectedTicketDetail.ticket.userRole) }}</span>
                          </div>
                          <div>
                            <span class="text-subtle">{{ copy.support.contact }}:</span>
                            <span class="text-base-content ml-1">{{ selectedTicketDetail.ticket.contact || copy.common.notAvailable }}</span>
                          </div>
                        </div>
                      </div>

                      <glass-popover-select
                        v-model="detailStatus"
                        :options="ticketStatusActionOptions"
                        size="sm"
                        tint="info"
                        @update:modelValue="changeTicketStatus"
                      />
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4 text-xs">
                      <div>
                        <span class="text-subtle">{{ copy.support.channel }}:</span>
                        <span class="text-base-content ml-1">{{ ticketChannelText(selectedTicketDetail.ticket.channel) }}</span>
                      </div>
                      <div>
                        <span class="text-subtle">{{ copy.support.type }}:</span>
                        <span class="text-base-content ml-1">{{ ticketTypeText(selectedTicketDetail.ticket.ticketType) }}</span>
                      </div>
                      <div>
                        <span class="text-subtle">{{ copy.support.priority }}:</span>
                        <span class="text-base-content ml-1">{{ ticketPriorityText(selectedTicketDetail.ticket.priority) }}</span>
                      </div>
                      <div>
                        <span class="text-subtle">{{ copy.support.assignee }}:</span>
                        <span class="text-base-content ml-1">{{ selectedTicketDetail.ticket.assigneeName || copy.support.unassigned }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="rounded-3xl border border-white/20 dark:border-white/10 p-4 max-h-80 overflow-auto space-y-3 bg-white/5">
                    <div
                      v-for="message in selectedTicketDetail.messages"
                      :key="message.id"
                      class="rounded-2xl px-4 py-3"
                      :class="message.senderSide === 'admin'
                        ? 'bg-emerald-500/10 border border-emerald-500/20'
                        : 'bg-white/5 border border-white/10'"
                    >
                      <div class="grid grid-cols-1 sm:grid-cols-3 gap-1 text-xs mb-2">
                        <div>
                          <span class="text-subtle">{{ copy.support.messageSender }}:</span>
                          <span class="text-base-content ml-1">{{ senderSideText(message.senderSide) }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.messageName }}:</span>
                          <span class="text-base-content ml-1">{{ message.senderName || copy.common.notAvailable }}</span>
                        </div>
                        <div>
                          <span class="text-subtle">{{ copy.support.messageTime }}:</span>
                          <span class="text-base-content ml-1">{{ formatDateTime(message.createdAt) }}</span>
                        </div>
                      </div>
                      <div class="text-sm text-base-content whitespace-pre-line">{{ message.content }}</div>
                    </div>
                  </div>

                  <form class="space-y-3" @submit.prevent="replyTicket">
                    <div class="flex items-center justify-between gap-3">
                      <div class="text-xs text-subtle">{{ isZh ? '回复内容' : 'Reply message' }}</div>
                      <emoji-picker
                        size="sm"
                        variant="primary"
                        tint="info"
                        :hideLabelOnSmall="true"
                        :buttonClass="'!text-white whitespace-nowrap shrink-0'"
                        @select="appendTicketReplyEmoji"
                      />
                    </div>
                    <glass-textarea
                      v-model="ticketReply"
                      :rows="4"
                      :placeholder="copy.support.replyPlaceholder"
                    />
                    <div class="flex justify-end">
                      <Button
                        type="submit"
                        size="sm"
                        variant="success"
                        :disabled="!ticketReply.trim() || replyingTicket"
                        :loading="replyingTicket"
                      >
                        <PaperAirplaneIcon class="w-4 h-4 mr-2" />
                        {{ copy.support.sendReply }}
                      </Button>
                    </div>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </card>

      <card padding="lg" tint="success" class="overflow-hidden">
        <template #header>
          <div class="flex items-center justify-between gap-3 flex-wrap">
            <div class="flex items-center gap-3">
              <div class="w-11 h-11 rounded-2xl bg-emerald-500/15 text-emerald-600 dark:text-emerald-300 flex items-center justify-center">
                <BookOpenIcon class="w-5 h-5" />
              </div>
              <div>
                <h2 class="text-lg font-semibold text-base-content">{{ copy.helpDocs.panelTitle }}</h2>
                <p class="text-xs text-subtle mt-1">{{ copy.helpDocs.panelDesc }}</p>
              </div>
            </div>
            <Button size="sm" variant="success" :loading="helpArticleLoading" @click="fetchAdminHelpArticles">
              <ArrowPathIcon class="w-4 h-4 mr-2" />
              {{ copy.helpDocs.refresh }}
            </Button>
          </div>
        </template>

        <div class="space-y-4">
          <div class="rounded-2xl p-5 glass-ultraThin glass-tint-success border border-white/20 dark:border-white/10 shadow-sm space-y-4">
            <div class="flex items-center justify-between gap-3 flex-wrap">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-2xl bg-emerald-500/15 text-emerald-700 dark:text-emerald-200 flex items-center justify-center">
                  <DocumentTextIcon class="w-5 h-5" />
                </div>
                <div>
                  <div class="text-sm font-semibold text-base-content">
                    {{ editingHelpArticleId ? copy.helpDocs.editingTitle : copy.helpDocs.createTitle }}
                  </div>
                  <div class="text-xs text-subtle mt-1">
                    {{ editingHelpArticleId ? copy.helpDocs.editingDesc : copy.helpDocs.createDesc }}
                  </div>
                </div>
              </div>

              <Button size="sm" variant="secondary" @click="resetHelpArticleForm">
                <ArrowPathIcon class="w-4 h-4 mr-2" />
                {{ copy.helpDocs.reset }}
              </Button>
            </div>

            <div class="rounded-2xl border border-white/15 bg-white/5 p-4 space-y-3">
              <div class="flex items-center justify-between gap-3 flex-wrap">
                <div class="text-xs font-medium text-subtle">{{ copy.helpDocs.categoryCreateLabel }}</div>
                <div v-if="helpCategories.length === 0" class="text-xs text-amber-700 dark:text-amber-300">
                  {{ copy.helpDocs.noCategoriesHint }}
                </div>
              </div>
              <div class="flex flex-col sm:flex-row gap-3">
                <div class="flex-1 min-w-0">
                  <glass-input
                    v-model="newHelpCategoryName"
                    :placeholder="copy.helpDocs.categoryCreatePlaceholder"
                  />
                </div>
                <Button
                  size="sm"
                  variant="success"
                  class="sm:self-end"
                  :loading="creatingHelpCategory"
                  :disabled="creatingHelpCategory"
                  @click="saveHelpCategory"
                >
                  <BookOpenIcon class="w-4 h-4 mr-2" />
                  {{ copy.helpDocs.categoryCreateButton }}
                </Button>
              </div>
            </div>

            <div class="grid grid-cols-1 xl:grid-cols-4 gap-4">
              <div>
                <div class="text-xs text-subtle mb-1">{{ copy.helpDocs.categoryLabel }}</div>
                <glass-popover-select
                  v-model="helpArticleForm.categoryId"
                  :options="helpCategoryFormOptions"
                  size="sm"
                  tint="success"
                />
              </div>
              <div class="xl:col-span-2">
                <div class="text-xs text-subtle mb-1">{{ copy.helpDocs.titleLabel }}</div>
                <glass-input
                  v-model="helpArticleForm.title"
                  :placeholder="copy.helpDocs.titlePlaceholder"
                />
              </div>
              <div>
                <div class="text-xs text-subtle mb-1">{{ copy.helpDocs.publishedLabel }}</div>
                <glass-popover-select
                  v-model="helpArticleForm.publishState"
                  :options="helpArticlePublishOptions"
                  size="sm"
                  tint="success"
                />
              </div>
            </div>

            <div class="grid grid-cols-1 xl:grid-cols-3 gap-4">
              <div>
                <div class="text-xs text-subtle mb-1">{{ copy.helpDocs.slugLabel }}</div>
                <glass-input
                  v-model="helpArticleForm.slug"
                  :placeholder="copy.helpDocs.slugPlaceholder"
                />
              </div>
              <div class="xl:col-span-2">
                <div class="text-xs text-subtle mb-1">{{ copy.helpDocs.tagsLabel }}</div>
                <glass-input
                  v-model="helpArticleForm.tags"
                  :placeholder="copy.helpDocs.tagsPlaceholder"
                />
              </div>
            </div>

            <div>
              <div class="text-xs text-subtle mb-1">{{ copy.helpDocs.contentLabel }}</div>
              <glass-textarea
                v-model="helpArticleForm.contentMd"
                :rows="8"
                :placeholder="copy.helpDocs.contentPlaceholder"
              />
            </div>

            <div class="flex flex-wrap gap-3 pt-2">
              <Button
                size="sm"
                variant="success"
                :loading="savingHelpArticle"
                :disabled="savingHelpArticle"
                @click="saveHelpArticle"
              >
                <DocumentTextIcon class="w-4 h-4 mr-2" />
                {{ editingHelpArticleId ? copy.helpDocs.update : copy.helpDocs.create }}
              </Button>
              <Button
                size="sm"
                variant="secondary"
                :disabled="savingHelpArticle"
                @click="resetHelpArticleForm"
              >
                <ArrowPathIcon class="w-4 h-4 mr-2" />
                {{ copy.helpDocs.reset }}
              </Button>
            </div>
          </div>

          <div class="rounded-2xl p-4 glass-ultraThin glass-tint-primary border border-white/20 dark:border-white/10 shadow-sm">
            <filter-bar tint="secondary" align="center" :dense="false" class="rounded-3xl">
              <template #left>
                <div class="flex items-start gap-3 flex-wrap w-full">
                  <div class="w-full sm:w-auto flex flex-col gap-1">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.helpDocs.categoryLabel }}</span>
                    <div class="w-full sm:w-44">
                      <glass-popover-select
                        v-model="helpArticleFilters.categoryId"
                        :options="helpCategoryFilterOptions"
                        size="sm"
                        tint="secondary"
                      />
                    </div>
                  </div>
                  <div class="w-full sm:w-auto flex flex-col gap-1">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.helpDocs.publishedLabel }}</span>
                    <div class="w-full sm:w-40">
                      <glass-popover-select
                        v-model="helpArticleFilters.published"
                        :options="helpArticleStatusFilterOptions"
                        size="sm"
                        tint="secondary"
                      />
                    </div>
                  </div>
                  <div class="w-full lg:flex-1 flex flex-col gap-1 min-w-[16rem]">
                    <span class="text-xs font-medium leading-tight text-subtle">{{ copy.helpDocs.searchLabel }}</span>
                    <glass-search-input
                      v-model="helpArticleFilters.q"
                      :placeholder="copy.helpDocs.searchPlaceholder"
                      size="sm"
                      tint="success"
                    />
                  </div>
                </div>
              </template>
            </filter-bar>
          </div>

          <div class="rounded-2xl p-5 glass-ultraThin glass-tint-accent border border-white/20 dark:border-white/10 shadow-sm space-y-3">
            <div class="flex items-center justify-between gap-3 flex-wrap">
              <div class="text-sm font-semibold text-base-content">{{ copy.helpDocs.listTitle }}</div>
              <div class="text-xs text-subtle">{{ helpArticleListSummary }}</div>
            </div>

            <div v-if="helpArticleLoading && adminHelpArticles.length === 0" class="text-sm text-subtle py-8 text-center">
              {{ copy.support.loading }}
            </div>
            <div v-else-if="adminHelpArticles.length === 0" class="text-sm text-subtle py-8 text-center">
              {{ copy.helpDocs.empty }}
            </div>

            <div v-else class="space-y-3">
              <div
                v-for="article in adminHelpArticles"
                :key="article.id"
                class="rounded-3xl border border-white/20 dark:border-white/10 bg-white/5 p-4"
              >
                <div class="flex items-start justify-between gap-3 flex-wrap">
                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2 flex-wrap">
                      <div class="font-semibold text-base-content break-all">{{ article.title }}</div>
                      <Badge size="sm" :variant="article.published ? 'success' : 'secondary'">
                        {{ article.published ? copy.helpDocs.published : copy.helpDocs.draft }}
                      </Badge>
                    </div>
                    <div class="mt-2 flex items-center gap-2 flex-wrap text-xs text-subtle">
                      <span>#{{ article.id }}</span>
                      <span>{{ helpCategoryName(article.categoryId) }}</span>
                      <span>{{ article.slug || '-' }}</span>
                    </div>
                    <div class="mt-2 flex items-center gap-3 flex-wrap text-xs text-subtle">
                      <span>{{ article.views || 0 }} {{ isZh ? '浏览' : 'views' }}</span>
                      <span>{{ article.upVotes || 0 }} {{ isZh ? '有帮助' : 'helpful' }}</span>
                      <span>{{ article.downVotes || 0 }} {{ isZh ? '没帮助' : 'not helpful' }}</span>
                    </div>
                    <div v-if="article.tags" class="mt-2 text-xs text-subtle break-all">{{ article.tags }}</div>
                    <div class="mt-3 text-sm text-base-content/90 whitespace-pre-line">
                      {{ helpArticlePreview(article) }}
                    </div>
                    <div class="mt-3 text-xs text-subtle">
                      {{ copy.helpDocs.updatedAt }}:
                      <span class="text-base-content ml-1">{{ formatDateTime(article.updatedAt || article.createdAt) }}</span>
                    </div>
                  </div>

                  <div class="flex items-center gap-2 shrink-0">
                    <Button
                      size="sm"
                      variant="secondary"
                      @click="editHelpArticle(article)"
                    >
                      <PencilSquareIcon class="w-4 h-4 mr-2" />
                      {{ copy.helpDocs.edit }}
                    </Button>
                    <Button
                      size="sm"
                      variant="danger"
                      :loading="deletingHelpArticleId === article.id"
                      :disabled="deletingHelpArticleId === article.id"
                      @click="deleteHelpArticle(article)"
                    >
                      <TrashIcon class="w-4 h-4 mr-2" />
                      {{ copy.helpDocs.delete }}
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import PageHeader from '@/components/ui/PageHeader.vue'
import Badge from '@/components/ui/Badge.vue'
import Card from '@/components/ui/Card.vue'
import StartCard from '@/components/ui/StartCard.vue'
import Button from '@/components/ui/Button.vue'
import FilterBar from '@/components/ui/filters/FilterBar.vue'
import GlassInput from '@/components/ui/inputs/GlassInput.vue'
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
import EmojiPicker from '@/components/ui/EmojiPicker.vue'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { notificationAPI } from '@/api/notification.api'
import { adminApi, type AdminUserListItem } from '@/api/admin.api'
import { helpApi } from '@/api/help.api'
import { useUIStore } from '@/stores/ui'
import type { HelpArticle, HelpArticleUpsertRequest, HelpCategory, HelpCategoryCreateRequest, HelpTicket, HelpTicketDetail } from '@/types/help'
import {
  ArrowPathIcon,
  BellAlertIcon,
  BookOpenIcon,
  ChatBubbleLeftRightIcon,
  CheckCircleIcon,
  ClockIcon,
  DocumentTextIcon,
  InboxStackIcon,
  PaperAirplaneIcon,
  PencilSquareIcon,
  TrashIcon,
} from '@heroicons/vue/24/outline'

const { t, locale } = useI18n()
const route = useRoute()
const ui = useUIStore()

const isZh = computed(() => String(locale.value || '').toLowerCase().startsWith('zh'))
const copy = computed(() => (isZh.value
  ? {
      subtitle: '管理员通知发送与支持单处理工作台',
      common: {
        notAvailable: '-',
        unknown: '未知',
        errorTitle: '错误'
      },
      roles: {
        student: '学生',
        teacher: '教师',
        admin: '管理员'
      },
      summary: {
        pendingLabel: '待处理支持单',
        pendingDesc: '等待管理员首次响应',
        inProgressLabel: '处理中',
        inProgressDesc: '已接手，正在跟进',
        resolvedLabel: '已解决',
        resolvedDesc: '当前筛选结果中的已闭环单据',
        audienceLabel: '通知对象'
      },
      broadcast: {
        panelTitle: '通知派发台',
        panelDesc: '统一面向全体、角色分组或指定成员发通知',
        reset: '重置表单',
        clear: '清空',
        send: '发送通知',
        overviewAudience: '发送范围',
        overviewType: '通知类型',
        overviewPriority: '优先级',
        titleLabel: '标题',
        titlePlaceholder: '输入通知标题',
        contentLabel: '内容',
        contentPlaceholder: '输入通知正文',
        typeLabel: '通知类型',
        priorityLabel: '优先级',
        targetLabel: '发送对象',
        roleLabel: '角色',
        specificLabel: '指定成员',
        searchPlaceholder: '搜索姓名、学号、工号、用户名或邮箱',
        searching: '正在搜索成员…',
        empty: '暂无匹配成员',
        selectedMembers: '已选成员',
        allAudienceValue: '全体',
        audienceSelectedCount: '已选成员数量',
        audiencePickMembers: '请先搜索并选择成员',
        audienceByRole: '按单一角色分组推送',
        audienceAll: '向平台全部成员推送',
        sentTitle: '已发送',
        searchError: '搜索用户失败',
        sendError: '发送通知失败'
      },
      targets: {
        all: '全体成员',
        role: '按角色',
        specific: '指定成员'
      },
      notificationTypes: {
        system: '系统通知',
        assignment: '作业通知',
        grade: '成绩通知',
        course: '课程通知',
        message: '消息提醒'
      },
      priorities: {
        low: '低',
        normal: '普通',
        medium: '中',
        high: '高',
        urgent: '紧急'
      },
      statuses: {
        open: '待处理',
        in_progress: '处理中',
        resolved: '已解决',
        closed: '已关闭'
      },
      channels: {
        support: '技术支持',
        feedback: '意见反馈'
      },
      ticketTypes: {
        technical: '技术问题',
        account: '账户问题',
        feature: '功能建议',
        bug: '错误报告',
        feedback: '意见反馈',
        other: '其他'
      },
      support: {
        panelTitle: '支持单处理台',
        panelDesc: '统一查看学生和教师的支持单、反馈单，并直接回复',
        refresh: '刷新队列',
        filters: {
          status: '状态',
          channel: '渠道',
          priority: '优先级',
          search: '搜索',
          searchPlaceholder: '搜索标题、账号或内容'
        },
        loading: '加载中…',
        empty: '暂无支持单',
        detailLoading: '正在加载详情…',
        detailEmpty: '选择左侧支持单查看详情',
        lastUpdated: '最近更新时间',
        ticketId: '工单ID',
        account: '账号',
        submitterRole: '角色',
        channel: '渠道',
        type: '类型',
        priority: '优先级',
        contact: '联系方式',
        assignee: '处理人',
        unassigned: '未指派',
        messageSender: '发送方',
        messageName: '昵称',
        messageTime: '时间',
        messageAdmin: '管理员',
        messageUser: '用户',
        replyPlaceholder: '输入管理员回复内容',
        sendReply: '发送回复',
        replySuccessTitle: '已回复',
        replySuccessMessage: '回复内容已发送给提交人',
        replyError: '回复失败',
        statusSuccessTitle: '已更新',
        statusSuccessMessage: '支持单状态已更新',
        statusError: '更新状态失败',
        loadError: '加载支持单失败',
        detailError: '加载详情失败'
      },
      helpDocs: {
        panelTitle: '帮助文档管理',
        panelDesc: '创建、编辑并发布帮助文章，前台帮助中心会自动展示已发布内容',
        refresh: '刷新文章',
        listTitle: '已发布与草稿',
        createTitle: '新建帮助文章',
        createDesc: '写好内容后即可保存为草稿或直接发布',
        editingTitle: '编辑帮助文章',
        editingDesc: '更新后会同步到帮助中心，已发布内容会直接生效',
        categoryCreateLabel: '快速新增分类',
        categoryCreatePlaceholder: '输入分类名称',
        categoryCreateButton: '新增分类',
        noCategoriesHint: '当前还没有帮助分类，请先新增一个分类再创建文章',
        searchLabel: '搜索',
        searchPlaceholder: '搜索标题、slug 或正文',
        categoryLabel: '分类',
        allCategories: '全部分类',
        selectCategory: '选择分类',
        titleLabel: '标题',
        titlePlaceholder: '输入帮助文章标题',
        slugLabel: 'Slug',
        slugPlaceholder: '留空则按标题生成',
        tagsLabel: '标签',
        tagsPlaceholder: '多个标签请用逗号分隔',
        contentLabel: '内容',
        contentPlaceholder: '输入帮助说明，支持纯文本或 Markdown',
        publishedLabel: '发布状态',
        allStatus: '全部状态',
        published: '已发布',
        draft: '草稿',
        create: '新增文章',
        update: '保存修改',
        reset: '新建一篇',
        edit: '编辑',
        delete: '删除',
        empty: '暂无帮助文章',
        createSuccess: '帮助文章已创建',
        updateSuccess: '帮助文章已更新',
        deleteSuccess: '帮助文章已删除',
        categoryCreateSuccess: '帮助分类已创建',
        updatedAt: '最近更新',
        listSummary: '当前文章数',
        saveError: '保存帮助文章失败',
        categoryCreateError: '新增帮助分类失败',
        deleteError: '删除帮助文章失败',
        loadError: '加载帮助文章失败',
        loadCategoriesError: '加载帮助分类失败',
        requiredError: '请先填写分类、标题和内容',
        categoryRequiredError: '请先新增或选择帮助分类'
      },
      recipient: {
        userId: '用户ID',
        username: '用户名',
        email: '邮箱',
        role: '角色',
        studentNo: '学号',
        teacherNo: '工号'
      }
    }
  : {
      subtitle: 'Admin notification dispatch and support ticket workspace',
      common: {
        notAvailable: '-',
        unknown: 'Unknown',
        errorTitle: 'Error'
      },
      roles: {
        student: 'Student',
        teacher: 'Teacher',
        admin: 'Admin'
      },
      summary: {
        pendingLabel: 'Pending tickets',
        pendingDesc: 'Waiting for the first admin response',
        inProgressLabel: 'In progress',
        inProgressDesc: 'Already assigned and being followed up',
        resolvedLabel: 'Resolved',
        resolvedDesc: 'Closed-loop items in the current filtered set',
        audienceLabel: 'Audience'
      },
      broadcast: {
        panelTitle: 'Notification dispatch',
        panelDesc: 'Send notices to everyone, by role, or to selected members',
        reset: 'Reset form',
        clear: 'Clear',
        send: 'Send notification',
        overviewAudience: 'Audience',
        overviewType: 'Type',
        overviewPriority: 'Priority',
        titleLabel: 'Title',
        titlePlaceholder: 'Enter notification title',
        contentLabel: 'Content',
        contentPlaceholder: 'Enter notification message',
        typeLabel: 'Notification type',
        priorityLabel: 'Priority',
        targetLabel: 'Audience type',
        roleLabel: 'Role',
        specificLabel: 'Specific members',
        searchPlaceholder: 'Search by name, student no, staff no, username, or email',
        searching: 'Searching members…',
        empty: 'No matching members',
        selectedMembers: 'Selected members',
        allAudienceValue: 'All',
        audienceSelectedCount: 'Selected member count',
        audiencePickMembers: 'Search and select members first',
        audienceByRole: 'Send to one role group',
        audienceAll: 'Send to everyone on the platform',
        sentTitle: 'Sent',
        searchError: 'Failed to search users',
        sendError: 'Failed to send notification'
      },
      targets: {
        all: 'All members',
        role: 'By role',
        specific: 'Specific members'
      },
      notificationTypes: {
        system: 'System',
        assignment: 'Assignment',
        grade: 'Grade',
        course: 'Course',
        message: 'Message'
      },
      priorities: {
        low: 'Low',
        normal: 'Normal',
        medium: 'Medium',
        high: 'High',
        urgent: 'Urgent'
      },
      statuses: {
        open: 'Open',
        in_progress: 'In progress',
        resolved: 'Resolved',
        closed: 'Closed'
      },
      channels: {
        support: 'Support',
        feedback: 'Feedback'
      },
      ticketTypes: {
        technical: 'Technical',
        account: 'Account',
        feature: 'Feature request',
        bug: 'Bug report',
        feedback: 'Feedback',
        other: 'Other'
      },
      support: {
        panelTitle: 'Support desk',
        panelDesc: 'Review tickets from students and teachers and reply directly',
        refresh: 'Refresh queue',
        filters: {
          status: 'Status',
          channel: 'Channel',
          priority: 'Priority',
          search: 'Search',
          searchPlaceholder: 'Search title, account, or content'
        },
        loading: 'Loading…',
        empty: 'No tickets',
        detailLoading: 'Loading details…',
        detailEmpty: 'Select a ticket from the left to view details',
        lastUpdated: 'Last updated',
        ticketId: 'Ticket ID',
        account: 'Account',
        submitterRole: 'Role',
        channel: 'Channel',
        type: 'Type',
        priority: 'Priority',
        contact: 'Contact',
        assignee: 'Assignee',
        unassigned: 'Unassigned',
        messageSender: 'Sender',
        messageName: 'Name',
        messageTime: 'Time',
        messageAdmin: 'Admin',
        messageUser: 'User',
        replyPlaceholder: 'Enter your reply',
        sendReply: 'Send reply',
        replySuccessTitle: 'Replied',
        replySuccessMessage: 'Your reply has been sent to the submitter',
        replyError: 'Failed to reply',
        statusSuccessTitle: 'Updated',
        statusSuccessMessage: 'Ticket status updated',
        statusError: 'Failed to update status',
        loadError: 'Failed to load tickets',
        detailError: 'Failed to load details'
      },
      helpDocs: {
        panelTitle: 'Help article management',
        panelDesc: 'Create, edit, and publish help articles. Published items appear in the public Help Center automatically.',
        refresh: 'Refresh articles',
        listTitle: 'Published and draft articles',
        createTitle: 'Create help article',
        createDesc: 'Write the content, then save it as a draft or publish it directly.',
        editingTitle: 'Edit help article',
        editingDesc: 'Updates sync to the Help Center, and published changes go live immediately.',
        categoryCreateLabel: 'Quick category',
        categoryCreatePlaceholder: 'Enter a category name',
        categoryCreateButton: 'Add category',
        noCategoriesHint: 'No help categories yet. Add one before creating the first article.',
        searchLabel: 'Search',
        searchPlaceholder: 'Search title, slug, or content',
        categoryLabel: 'Category',
        allCategories: 'All categories',
        selectCategory: 'Select category',
        titleLabel: 'Title',
        titlePlaceholder: 'Enter the help article title',
        slugLabel: 'Slug',
        slugPlaceholder: 'Leave blank to generate from the title',
        tagsLabel: 'Tags',
        tagsPlaceholder: 'Separate multiple tags with commas',
        contentLabel: 'Content',
        contentPlaceholder: 'Enter help content in plain text or Markdown',
        publishedLabel: 'Publish status',
        allStatus: 'All statuses',
        published: 'Published',
        draft: 'Draft',
        create: 'Create article',
        update: 'Save changes',
        reset: 'New article',
        edit: 'Edit',
        delete: 'Delete',
        empty: 'No help articles yet',
        createSuccess: 'Help article created',
        updateSuccess: 'Help article updated',
        deleteSuccess: 'Help article deleted',
        categoryCreateSuccess: 'Help category created',
        updatedAt: 'Updated',
        listSummary: 'Articles',
        saveError: 'Failed to save the help article',
        categoryCreateError: 'Failed to create the help category',
        deleteError: 'Failed to delete the help article',
        loadError: 'Failed to load help articles',
        loadCategoriesError: 'Failed to load help categories',
        requiredError: 'Please fill in the category, title, and content first',
        categoryRequiredError: 'Create or select a help category first'
      },
      recipient: {
        userId: 'User ID',
        username: 'Username',
        email: 'Email',
        role: 'Role',
        studentNo: 'Student No.',
        teacherNo: 'Staff No.'
      }
    }
))

const sending = ref(false)
const ticketLoading = ref(false)
const ticketDetailLoading = ref(false)
const searchingRecipients = ref(false)
const replyingTicket = ref(false)
const helpArticleLoading = ref(false)
const savingHelpArticle = ref(false)
const deletingHelpArticleId = ref<number | null>(null)
const creatingHelpCategory = ref(false)

const broadcastTitle = ref('')
const broadcastContent = ref('')
const broadcastType = ref<'system' | 'assignment' | 'grade' | 'course' | 'message'>('system')
const broadcastPriority = ref<'low' | 'normal' | 'high' | 'urgent'>('normal')
const broadcastTargetType = ref<'all' | 'role' | 'specific'>('all')
const broadcastRole = ref<'student' | 'teacher' | 'admin'>('student')
const recipientKeyword = ref('')
const recipientResults = ref<AdminUserListItem[]>([])
const selectedRecipients = ref<AdminUserListItem[]>([])

const tickets = ref<HelpTicket[]>([])
const selectedTicketId = ref<number | null>(null)
const selectedTicketDetail = ref<HelpTicketDetail | null>(null)
const ticketReply = ref('')
const detailStatus = ref<'open' | 'in_progress' | 'resolved' | 'closed'>('open')
const helpCategories = ref<HelpCategory[]>([])
const adminHelpArticles = ref<HelpArticle[]>([])
const editingHelpArticleId = ref<number | null>(null)
const newHelpCategoryName = ref('')
const ticketFilters = reactive({
  status: '',
  channel: '',
  priority: '',
  q: ''
})
const helpArticleFilters = reactive<{
  categoryId: number
  published: '' | 'published' | 'draft'
  q: string
}>({
  categoryId: 0,
  published: '',
  q: ''
})
const helpArticleForm = reactive<{
  categoryId: number
  title: string
  slug: string
  tags: string
  contentMd: string
  publishState: 'published' | 'draft'
}>({
  categoryId: 0,
  title: '',
  slug: '',
  tags: '',
  contentMd: '',
  publishState: 'published'
})
let recipientSearchTimer: ReturnType<typeof setTimeout> | null = null
let ticketFilterTimer: ReturnType<typeof setTimeout> | null = null
let helpArticleFilterTimer: ReturnType<typeof setTimeout> | null = null

const broadcastTargetOptions = computed(() => [
  { label: copy.value.targets.all, value: 'all' },
  { label: copy.value.targets.role, value: 'role' },
  { label: copy.value.targets.specific, value: 'specific' }
])

const broadcastRoleOptions = computed(() => [
  { label: copy.value.roles.student, value: 'student' },
  { label: copy.value.roles.teacher, value: 'teacher' },
  { label: copy.value.roles.admin, value: 'admin' }
])

const broadcastTypeOptions = computed(() => [
  { label: copy.value.notificationTypes.system, value: 'system' },
  { label: copy.value.notificationTypes.assignment, value: 'assignment' },
  { label: copy.value.notificationTypes.grade, value: 'grade' },
  { label: copy.value.notificationTypes.course, value: 'course' },
  { label: copy.value.notificationTypes.message, value: 'message' }
])

const broadcastPriorityOptions = computed(() => [
  { label: copy.value.priorities.low, value: 'low' },
  { label: copy.value.priorities.normal, value: 'normal' },
  { label: copy.value.priorities.high, value: 'high' },
  { label: copy.value.priorities.urgent, value: 'urgent' }
])

const ticketStatusFilterOptions = computed(() => [
  { label: isZh.value ? '全部状态' : 'All statuses', value: '' },
  { label: copy.value.statuses.open, value: 'open' },
  { label: copy.value.statuses.in_progress, value: 'in_progress' },
  { label: copy.value.statuses.resolved, value: 'resolved' },
  { label: copy.value.statuses.closed, value: 'closed' }
])

const ticketStatusActionOptions = computed(() => [
  { label: copy.value.statuses.open, value: 'open' },
  { label: copy.value.statuses.in_progress, value: 'in_progress' },
  { label: copy.value.statuses.resolved, value: 'resolved' },
  { label: copy.value.statuses.closed, value: 'closed' }
])

const ticketChannelFilterOptions = computed(() => [
  { label: isZh.value ? '全部渠道' : 'All channels', value: '' },
  { label: copy.value.channels.support, value: 'support' },
  { label: copy.value.channels.feedback, value: 'feedback' }
])

const ticketPriorityFilterOptions = computed(() => [
  { label: isZh.value ? '全部优先级' : 'All priorities', value: '' },
  { label: copy.value.priorities.low, value: 'low' },
  { label: copy.value.priorities.medium, value: 'medium' },
  { label: copy.value.priorities.high, value: 'high' },
  { label: copy.value.priorities.urgent, value: 'urgent' }
])

const helpCategoryFilterOptions = computed(() => [
  { label: copy.value.helpDocs.allCategories, value: 0 },
  ...helpCategories.value.map(item => ({
    label: item.name,
    value: item.id
  }))
])

const helpCategoryFormOptions = computed(() => [
  { label: copy.value.helpDocs.selectCategory, value: 0 },
  ...helpCategories.value.map(item => ({
    label: item.name,
    value: item.id
  }))
])

const helpArticleStatusFilterOptions = computed(() => [
  { label: copy.value.helpDocs.allStatus, value: '' },
  { label: copy.value.helpDocs.published, value: 'published' },
  { label: copy.value.helpDocs.draft, value: 'draft' }
])

const helpArticlePublishOptions = computed(() => [
  { label: copy.value.helpDocs.published, value: 'published' },
  { label: copy.value.helpDocs.draft, value: 'draft' }
])

const pendingTicketCount = computed(() => tickets.value.filter(item => item.status === 'open').length)
const inProgressTicketCount = computed(() => tickets.value.filter(item => item.status === 'in_progress').length)
const resolvedTicketCount = computed(() => tickets.value.filter(item => item.status === 'resolved' || item.status === 'closed').length)
const helpArticleListSummary = computed(() => `${copy.value.helpDocs.listSummary}: ${adminHelpArticles.value.length}`)

const broadcastAudienceValue = computed(() => {
  if (broadcastTargetType.value === 'all') return copy.value.broadcast.allAudienceValue
  if (broadcastTargetType.value === 'role') {
    const found = broadcastRoleOptions.value.find(item => item.value === broadcastRole.value)
    return found?.label || broadcastRole.value
  }
  return String(selectedRecipients.value.length)
})

const broadcastAudienceDesc = computed(() => {
  if (broadcastTargetType.value === 'specific') {
    return selectedRecipients.value.length > 0
      ? copy.value.broadcast.audienceSelectedCount
      : copy.value.broadcast.audiencePickMembers
  }
  if (broadcastTargetType.value === 'role') return copy.value.broadcast.audienceByRole
  return copy.value.broadcast.audienceAll
})

const broadcastTypeLabel = computed(
  () => broadcastTypeOptions.value.find(item => item.value === broadcastType.value)?.label || broadcastType.value
)

const broadcastPriorityLabel = computed(
  () => broadcastPriorityOptions.value.find(item => item.value === broadcastPriority.value)?.label || broadcastPriority.value
)

const canSendBroadcast = computed(() => {
  if (!broadcastTitle.value.trim() || !broadcastContent.value.trim()) return false
  if (broadcastTargetType.value === 'specific') return selectedRecipients.value.length > 0
  if (broadcastTargetType.value === 'role') return Boolean(broadcastRole.value)
  return true
})

const ticketStatusText = (status?: string) => {
  switch (String(status || '')) {
    case 'open': return copy.value.statuses.open
    case 'in_progress': return copy.value.statuses.in_progress
    case 'resolved': return copy.value.statuses.resolved
    case 'closed': return copy.value.statuses.closed
    default: return status || copy.value.common.unknown
  }
}

const ticketStatusVariant = (status?: string): 'warning' | 'info' | 'success' | 'secondary' => {
  switch (String(status || '')) {
    case 'open': return 'warning'
    case 'in_progress': return 'info'
    case 'resolved': return 'success'
    case 'closed': return 'secondary'
    default: return 'secondary'
  }
}

const toggleRecipient = (user: AdminUserListItem) => {
  const idx = selectedRecipients.value.findIndex(item => item.id === user.id)
  if (idx >= 0) {
    selectedRecipients.value.splice(idx, 1)
    return
  }
  selectedRecipients.value.push(user)
}

const formatRecipientName = (user: AdminUserListItem) => {
  const fullName = isZh.value
    ? `${user.lastName || ''}${user.firstName || ''}`.trim()
    : [user.firstName, user.lastName].filter(Boolean).join(' ').trim()
  return fullName || user.nickname || user.username
}

const roleText = (role?: string) => {
  switch (String(role || '')) {
    case 'student': return copy.value.roles.student
    case 'teacher': return copy.value.roles.teacher
    case 'admin': return copy.value.roles.admin
    default: return role || copy.value.common.notAvailable
  }
}

const recipientSecondaryIdLabel = (user: AdminUserListItem) => {
  if (user.role === 'student' && user.studentNo) return copy.value.recipient.studentNo
  if (user.role === 'teacher' && user.teacherNo) return copy.value.recipient.teacherNo
  return ''
}

const recipientSecondaryIdValue = (user: AdminUserListItem) => {
  if (user.role === 'student') return user.studentNo || ''
  if (user.role === 'teacher') return user.teacherNo || ''
  return ''
}

const ticketChannelText = (channel?: string) => {
  switch (String(channel || '')) {
    case 'feedback': return copy.value.channels.feedback
    case 'support': return copy.value.channels.support
    default: return channel || copy.value.common.notAvailable
  }
}

const ticketPriorityText = (priority?: string) => {
  switch (String(priority || '')) {
    case 'low': return copy.value.priorities.low
    case 'normal': return copy.value.priorities.normal
    case 'medium': return copy.value.priorities.medium
    case 'high': return copy.value.priorities.high
    case 'urgent': return copy.value.priorities.urgent
    default: return priority || copy.value.common.notAvailable
  }
}

const ticketTypeText = (ticketType?: string) => {
  switch (String(ticketType || '')) {
    case 'technical': return copy.value.ticketTypes.technical
    case 'account': return copy.value.ticketTypes.account
    case 'feature': return copy.value.ticketTypes.feature
    case 'bug': return copy.value.ticketTypes.bug
    case 'feedback': return copy.value.ticketTypes.feedback
    case 'other': return copy.value.ticketTypes.other
    default: return ticketType || copy.value.common.notAvailable
  }
}

const senderSideText = (senderSide?: string) => {
  return senderSide === 'admin'
    ? copy.value.support.messageAdmin
    : copy.value.support.messageUser
}

const formatDateTime = (value?: string | null) => {
  if (!value) return copy.value.common.notAvailable
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat(isZh.value ? 'zh-CN' : 'en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: !isZh.value
  }).format(date)
}

const broadcastSentMessage = (successCount: number, failCount: number) => {
  return isZh.value
    ? `成功 ${successCount} 条，失败 ${failCount} 条`
    : `${successCount} succeeded, ${failCount} failed`
}

const appendBroadcastEmoji = (emoji: string) => {
  broadcastContent.value = `${broadcastContent.value || ''}${emoji}`
}

const appendTicketReplyEmoji = (emoji: string) => {
  ticketReply.value = `${ticketReply.value || ''}${emoji}`
}

const defaultHelpCategoryId = () => helpCategories.value[0]?.id || 0

const resetHelpArticleForm = () => {
  editingHelpArticleId.value = null
  helpArticleForm.categoryId = defaultHelpCategoryId()
  helpArticleForm.title = ''
  helpArticleForm.slug = ''
  helpArticleForm.tags = ''
  helpArticleForm.contentMd = ''
  helpArticleForm.publishState = 'published'
}

const helpCategoryName = (categoryId?: number) => {
  if (!categoryId) return copy.value.common.notAvailable
  return helpCategories.value.find(item => item.id === categoryId)?.name || `#${categoryId}`
}

const helpArticlePreview = (article: HelpArticle) => {
  const raw = String(article.contentMd || article.contentHtml || '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  if (!raw) return copy.value.common.notAvailable
  return raw.length > 120 ? `${raw.slice(0, 120)}...` : raw
}

const fetchHelpCategories = async () => {
  try {
    helpCategories.value = await helpApi.listCategories()
    if (!helpArticleForm.categoryId) {
      helpArticleForm.categoryId = defaultHelpCategoryId()
    }
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.helpDocs.loadCategoriesError
    })
  }
}

const saveHelpCategory = async () => {
  const name = newHelpCategoryName.value.trim()
  if (!name) {
    ui.showNotification({
      type: 'warning',
      title: copy.value.common.errorTitle,
      message: copy.value.helpDocs.categoryCreatePlaceholder
    })
    return
  }

  const payload: HelpCategoryCreateRequest = { name }
  creatingHelpCategory.value = true
  try {
    const created = await helpApi.adminCreateCategory(payload)
    await fetchHelpCategories()
    helpArticleForm.categoryId = created.id
    newHelpCategoryName.value = ''
    ui.showNotification({
      type: 'success',
      title: copy.value.helpDocs.categoryCreateSuccess,
      message: created.name
    })
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.helpDocs.categoryCreateError
    })
  } finally {
    creatingHelpCategory.value = false
  }
}

const fetchAdminHelpArticles = async () => {
  helpArticleLoading.value = true
  try {
    adminHelpArticles.value = await helpApi.adminListArticles({
      q: helpArticleFilters.q.trim() || undefined,
      categoryId: helpArticleFilters.categoryId || undefined,
      published: helpArticleFilters.published === 'published'
        ? true
        : (helpArticleFilters.published === 'draft' ? false : undefined)
    })
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.helpDocs.loadError
    })
  } finally {
    helpArticleLoading.value = false
  }
}

const editHelpArticle = (article: HelpArticle) => {
  editingHelpArticleId.value = article.id
  helpArticleForm.categoryId = article.categoryId || defaultHelpCategoryId()
  helpArticleForm.title = article.title || ''
  helpArticleForm.slug = article.slug || ''
  helpArticleForm.tags = article.tags || ''
  helpArticleForm.contentMd = article.contentMd || article.contentHtml || ''
  helpArticleForm.publishState = article.published ? 'published' : 'draft'
}

const saveHelpArticle = async () => {
  if (!helpArticleForm.categoryId) {
    ui.showNotification({
      type: 'warning',
      title: copy.value.common.errorTitle,
      message: copy.value.helpDocs.categoryRequiredError
    })
    return
  }

  if (!helpArticleForm.title.trim() || !helpArticleForm.contentMd.trim()) {
    ui.showNotification({
      type: 'warning',
      title: copy.value.common.errorTitle,
      message: copy.value.helpDocs.requiredError
    })
    return
  }

  const payload: HelpArticleUpsertRequest = {
    categoryId: helpArticleForm.categoryId,
    title: helpArticleForm.title.trim(),
    slug: helpArticleForm.slug.trim() || undefined,
    contentMd: helpArticleForm.contentMd.trim(),
    tags: helpArticleForm.tags.trim() || undefined,
    published: helpArticleForm.publishState === 'published'
  }

  savingHelpArticle.value = true
  try {
    if (editingHelpArticleId.value) {
      await helpApi.adminUpdateArticle(editingHelpArticleId.value, payload)
    } else {
      await helpApi.adminCreateArticle(payload)
    }
    await fetchAdminHelpArticles()
    ui.showNotification({
      type: 'success',
      title: editingHelpArticleId.value ? copy.value.helpDocs.updateSuccess : copy.value.helpDocs.createSuccess,
      message: helpArticleForm.title.trim()
    })
    resetHelpArticleForm()
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.helpDocs.saveError
    })
  } finally {
    savingHelpArticle.value = false
  }
}

const deleteHelpArticle = async (article: HelpArticle) => {
  deletingHelpArticleId.value = article.id
  try {
    await helpApi.adminDeleteArticle(article.id)
    if (editingHelpArticleId.value === article.id) {
      resetHelpArticleForm()
    }
    await fetchAdminHelpArticles()
    ui.showNotification({
      type: 'success',
      title: copy.value.helpDocs.deleteSuccess,
      message: article.title
    })
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.helpDocs.deleteError
    })
  } finally {
    deletingHelpArticleId.value = null
  }
}

const searchRecipients = async () => {
  if (!recipientKeyword.value.trim()) {
    recipientResults.value = []
    return
  }
  searchingRecipients.value = true
  try {
    const res = await adminApi.pageUsers({
      page: 1,
      size: 10,
      keyword: recipientKeyword.value.trim(),
      role: '',
      status: '',
      includeDeleted: false
    })
    recipientResults.value = res.items || []
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.broadcast.searchError
    })
  } finally {
    searchingRecipients.value = false
  }
}

const resetBroadcastForm = () => {
  broadcastTitle.value = ''
  broadcastContent.value = ''
  broadcastType.value = 'system'
  broadcastPriority.value = 'normal'
  broadcastTargetType.value = 'all'
  broadcastRole.value = 'student'
  recipientKeyword.value = ''
  recipientResults.value = []
  selectedRecipients.value = []
}

const sendBroadcast = async () => {
  if (!canSendBroadcast.value) return
  sending.value = true
  try {
    const result: any = await notificationAPI.adminBroadcast({
      title: broadcastTitle.value.trim(),
      content: broadcastContent.value.trim(),
      type: broadcastType.value,
      category: broadcastType.value,
      priority: broadcastPriority.value,
      targetType: broadcastTargetType.value,
      role: broadcastTargetType.value === 'role' ? broadcastRole.value : undefined,
      targetIds: broadcastTargetType.value === 'specific'
        ? selectedRecipients.value.map(item => item.id)
        : undefined
    })
    ui.showNotification({
      type: 'success',
      title: copy.value.broadcast.sentTitle,
      message: broadcastSentMessage(result?.successCount ?? 0, result?.failCount ?? 0)
    })
    resetBroadcastForm()
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.broadcast.sendError
    })
  } finally {
    sending.value = false
  }
}

const fetchTickets = async (resetSelection = false) => {
  ticketLoading.value = true
  try {
    const page = await helpApi.adminPageTickets({
      page: 1,
      size: 20,
      status: ticketFilters.status || undefined,
      channel: ticketFilters.channel || undefined,
      priority: ticketFilters.priority || undefined,
      q: ticketFilters.q || undefined
    })
    tickets.value = page.items || []

    const routeTicketId = Number(route.query.ticketId || 0)
    const nextId = resetSelection
      ? (routeTicketId || tickets.value[0]?.id || null)
      : (selectedTicketId.value && tickets.value.some(item => item.id === selectedTicketId.value)
        ? selectedTicketId.value
        : (routeTicketId || tickets.value[0]?.id || null))

    if (nextId) {
      await openTicket(nextId)
    } else {
      selectedTicketId.value = null
      selectedTicketDetail.value = null
    }
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.support.loadError
    })
  } finally {
    ticketLoading.value = false
  }
}

const openTicket = async (id: number) => {
  selectedTicketId.value = id
  ticketDetailLoading.value = true
  try {
    selectedTicketDetail.value = await helpApi.adminGetTicket(id)
    detailStatus.value = selectedTicketDetail.value.ticket.status
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.support.detailError
    })
  } finally {
    ticketDetailLoading.value = false
  }
}

const replyTicket = async () => {
  if (!selectedTicketId.value || !ticketReply.value.trim()) return
  replyingTicket.value = true
  try {
    selectedTicketDetail.value = await helpApi.adminReplyTicket(selectedTicketId.value, ticketReply.value.trim())
    detailStatus.value = selectedTicketDetail.value.ticket.status
    ticketReply.value = ''
    await fetchTickets()
    ui.showNotification({
      type: 'success',
      title: copy.value.support.replySuccessTitle,
      message: copy.value.support.replySuccessMessage
    })
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.support.replyError
    })
  } finally {
    replyingTicket.value = false
  }
}

const changeTicketStatus = async () => {
  if (!selectedTicketId.value) return
  try {
    const updated = await helpApi.adminUpdateTicketStatus(selectedTicketId.value, detailStatus.value)
    if (selectedTicketDetail.value) {
      selectedTicketDetail.value = {
        ...selectedTicketDetail.value,
        ticket: {
          ...selectedTicketDetail.value.ticket,
          ...updated
        }
      }
    }
    await fetchTickets()
    ui.showNotification({
      type: 'success',
      title: copy.value.support.statusSuccessTitle,
      message: copy.value.support.statusSuccessMessage
    })
  } catch (error: any) {
    ui.showNotification({
      type: 'error',
      title: copy.value.common.errorTitle,
      message: error?.message || copy.value.support.statusError
    })
  }
}

onMounted(async () => {
  await Promise.all([
    fetchTickets(true),
    fetchHelpCategories()
  ])
  await fetchAdminHelpArticles()
})

watch(
  () => recipientKeyword.value,
  (value) => {
    if (recipientSearchTimer) clearTimeout(recipientSearchTimer)
    if (broadcastTargetType.value !== 'specific') return
    recipientSearchTimer = setTimeout(() => {
      if (!String(value || '').trim()) {
        recipientResults.value = []
        return
      }
      searchRecipients()
    }, 220)
  }
)

watch(
  () => broadcastTargetType.value,
  (value) => {
    if (value !== 'specific') {
      recipientKeyword.value = ''
      recipientResults.value = []
      return
    }
    if (recipientKeyword.value.trim()) {
      searchRecipients()
    }
  }
)

watch(
  () => [ticketFilters.status, ticketFilters.channel, ticketFilters.priority, ticketFilters.q],
  () => {
    if (ticketFilterTimer) clearTimeout(ticketFilterTimer)
    ticketFilterTimer = setTimeout(() => {
      fetchTickets(true)
    }, 220)
  }
)

watch(
  () => [helpArticleFilters.categoryId, helpArticleFilters.published, helpArticleFilters.q],
  () => {
    if (helpArticleFilterTimer) clearTimeout(helpArticleFilterTimer)
    helpArticleFilterTimer = setTimeout(() => {
      fetchAdminHelpArticles()
    }, 220)
  }
)
</script>
