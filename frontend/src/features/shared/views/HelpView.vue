<template>
  <div class="min-h-screen relative overflow-hidden">
    <div class="absolute inset-x-0 top-0 -z-0 h-[24rem] overflow-hidden pointer-events-none">
      <div class="absolute left-[-6rem] top-[-5rem] h-56 w-56 rounded-full bg-emerald-400/18 blur-3xl"></div>
      <div class="absolute right-[-4rem] top-12 h-64 w-64 rounded-full bg-cyan-400/16 blur-3xl"></div>
      <div class="absolute left-1/3 top-20 h-40 w-40 rounded-full bg-amber-300/14 blur-3xl"></div>
    </div>

    <div class="relative z-10 max-w-6xl mx-auto p-6">
      <!-- 页面标题 -->
      <div class="mb-6">
        <page-header :title="t('shared.help.title')" :subtitle="t('shared.help.subtitle')" />
      </div>

      <!-- 顶部指标（StartCard 四卡） -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
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

      <!-- 顶部入口区 -->
      <div class="mb-12 relative overflow-hidden rounded-[2rem] p-6 glass-thin glass-tint-primary border border-white/20 dark:border-white/10 shadow-md">
        <div class="absolute inset-y-0 right-0 w-2/5 bg-gradient-to-l from-cyan-400/16 via-transparent to-transparent pointer-events-none"></div>

        <div class="relative grid grid-cols-1 xl:grid-cols-[minmax(0,1.25fr)_minmax(280px,0.75fr)] gap-6">
          <div>
            <div class="max-w-3xl">
              <h2 class="text-2xl md:text-3xl font-semibold text-base-content leading-tight">
                {{ isZh ? '先定位答案 再进入正确入口' : 'Find answers first then use the right path' }}
              </h2>
              <p class="mt-3 text-sm md:text-base text-subtle leading-7">
                {{ isZh
                  ? '参考 GitHub Docs、Shopify Help Center、Slack Help 等主流帮助中心的做法，这里优先提供搜索、自助排查、角色指南与统一支持单，减少来回沟通。'
                  : 'Borrowing patterns from mainstream help centers such as GitHub Docs, Shopify Help Center, and Slack Help, this page prioritizes search, self-service, role-based guidance, and one unified ticket flow.' }}
              </p>
            </div>

            <div class="mt-5 rounded-3xl px-4 py-4 glass-thin glass-tint-primary border border-white/20 dark:border-white/10 shadow-sm">
              <glass-search-input
                v-model="searchQuery"
                :placeholder="t('shared.help.searchPlaceholder') || '搜索帮助内容...'"
                size="md"
                tint="info"
              />

              <div class="mt-4 flex flex-wrap gap-2">
                <button
                  v-for="keyword in searchSuggestions"
                  :key="keyword"
                  type="button"
                  class="rounded-full border border-white/20 dark:border-white/10 glass-ultraThin glass-tint-secondary px-3 py-1.5 text-xs font-medium text-base-content transition hover:border-cyan-300/45 hover:text-cyan-900 dark:hover:text-cyan-100"
                  @click="applySearchSuggestion(keyword)"
                >
                  {{ keyword }}
                </button>
              </div>
            </div>

            <div class="mt-5 grid grid-cols-1 md:grid-cols-3 gap-3">
              <button
                v-for="entry in quickEntryCards"
                :key="entry.id"
                type="button"
                class="group rounded-3xl p-4 text-left transition-all duration-200 hover:-translate-y-0.5 border border-white/20 dark:border-white/10 shadow-sm"
                :class="entry.surfaceClass"
                @click="entry.action"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="h-11 w-11 rounded-2xl flex items-center justify-center shrink-0"
                       :class="entry.iconWrapClass">
                    <component :is="entry.icon" class="w-5 h-5" />
                  </div>
                  <span class="text-xs font-semibold uppercase tracking-[0.18em] text-subtle">
                    {{ entry.tag }}
                  </span>
                </div>
                <div class="mt-4">
                  <div class="font-semibold text-base-content group-hover:text-emerald-700 dark:group-hover:text-emerald-200">
                    {{ entry.title }}
                  </div>
                  <p class="mt-1 text-sm text-subtle leading-6">
                    {{ entry.description }}
                  </p>
                </div>
              </button>
            </div>

            <div class="mt-5 grid grid-cols-1 md:grid-cols-3 gap-3">
              <div
                v-for="promise in servicePromises"
                :key="promise.title"
                class="rounded-2xl p-5 border border-white/20 dark:border-white/10 shadow-sm glass-ultraThin min-h-[10.5rem] flex flex-col"
                :class="promise.surfaceClass"
              >
                <div class="flex items-start justify-between gap-3">
                  <div class="h-11 w-11 rounded-2xl flex items-center justify-center shadow-sm glass-ultraThin shrink-0"
                       :class="promise.iconWrapClass">
                    <component :is="promise.icon" class="w-5 h-5" />
                  </div>
                  <span class="text-[11px] font-semibold uppercase tracking-[0.18em] text-subtle">
                    {{ isZh ? '说明' : 'Note' }}
                  </span>
                </div>
                <div class="mt-4 text-sm font-medium text-base-content">{{ promise.title }}</div>
                <p class="mt-2 text-sm text-subtle leading-6">
                  {{ promise.description }}
                </p>
              </div>
            </div>
          </div>

          <div class="space-y-5">
            <div class="rounded-2xl p-4 glass-ultraThin glass-tint-info border border-white/20 dark:border-white/10 shadow-sm">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <div class="text-xs font-semibold uppercase tracking-[0.18em] text-subtle">
                    {{ roleLanding.badge }}
                  </div>
                  <div class="mt-2 text-base font-semibold text-base-content">{{ roleLanding.title }}</div>
                </div>
                <div class="h-11 w-11 rounded-2xl flex items-center justify-center glass-ultraThin"
                     :class="roleLanding.iconWrapClass">
                  <component :is="roleLanding.icon" class="w-5 h-5" />
                </div>
              </div>
              <p class="mt-3 text-sm text-subtle leading-6">
                {{ roleLanding.description }}
              </p>
              <div class="mt-4 space-y-2">
                <div
                  v-for="item in roleFocusItems"
                  :key="item"
                  class="flex items-start gap-2 text-sm text-base-content"
                >
                  <span class="mt-1.5 h-1.5 w-1.5 rounded-full bg-cyan-400 shrink-0"></span>
                  <span>{{ item }}</span>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between gap-3">
              <div>
                <div class="text-xs font-semibold uppercase tracking-[0.18em] text-subtle">
                  {{ isZh ? '推荐处理路径' : 'Recommended path' }}
                </div>
                <p class="mt-2 text-sm text-subtle leading-6">
                  {{ isZh ? '主流帮助中心普遍先做内容自助，其次给明确下一步，最后再进入人工支持。' : 'Most help centers guide users through self-service first, then a clear next step, then human support when needed.' }}
                </p>
              </div>
              <div class="h-12 w-12 rounded-2xl bg-cyan-500/12 text-cyan-600 dark:text-cyan-300 flex items-center justify-center">
                <QuestionMarkCircleIcon class="w-6 h-6" />
              </div>
            </div>

            <div class="mt-5 space-y-4">
              <div
                v-for="item in serviceJourney"
                :key="item.step"
                class="rounded-2xl p-4 border border-white/20 dark:border-white/10 shadow-sm glass-ultraThin"
                :class="item.surfaceClass"
              >
                <div class="flex items-start gap-3">
                  <div class="h-8 w-8 rounded-2xl glass-ultraThin text-sm font-semibold shrink-0 flex items-center justify-center"
                       :class="item.badgeClass">
                    {{ item.step }}
                  </div>
                  <div>
                    <div class="font-medium text-base-content">{{ item.title }}</div>
                    <p class="mt-1 text-sm text-subtle leading-6">{{ item.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      

      <!-- 主要内容区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <!-- 左侧导航 -->
        <div class="lg:col-span-1">
          <card padding="lg" tint="info" class="sticky top-6 pt-2 shadow-sm">
            <template #header>
              <h2 class="text-lg font-semibold text-base-content">{{ t('shared.help.nav') || '导航' }}</h2>
            </template>
            <nav class="space-y-2 pb-4">
              <button
                v-for="section in sections"
                :key="section.id"
                @click="activeSection = section.id"
                class="w-full text-left px-3 py-3 rounded-2xl text-sm transition-all duration-200"
                :class="activeSection === section.id
                  ? 'bg-emerald-500/15 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-200 ring-1 ring-emerald-500/20 shadow-sm'
                  : 'text-gray-600 dark:text-gray-400 hover:bg-white/10 dark:hover:bg-white/5'"
              >
                {{ section.title }}
              </button>
              <button
                @click="router.push('/docs')"
                class="w-full text-left px-3 py-3 rounded-2xl text-sm transition-all duration-200 text-gray-600 dark:text-gray-400 hover:bg-white/10 dark:hover:bg-white/5"
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
            <card padding="lg" tint="primary" class="shadow-sm">
              <template #header>
                <div class="flex items-center justify-between gap-3 flex-wrap">
                  <div>
                    <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.articles') || '帮助文章' }}</h2>
                    <p class="mt-1 text-sm text-subtle leading-6">
                      {{ isZh ? '优先浏览高频问题和操作指引，减少重复提问。' : 'Start with common issues and guided workflows before opening a ticket.' }}
                    </p>
                  </div>
                  <div class="flex items-center gap-2">
                    <Button size="sm" :variant="sortMode==='latest' ? 'primary' : 'outline'" @click="changeSort('latest')">{{ t('shared.common.latest') || '最新' }}</Button>
                    <Button size="sm" :variant="sortMode==='hot' ? 'primary' : 'outline'" @click="changeSort('hot')">{{ t('shared.common.hot') || '最热' }}</Button>
                  </div>
                </div>
              </template>

              <div class="mb-5 rounded-3xl p-4 glass-thin glass-tint-primary border border-white/20 dark:border-white/10 shadow-sm">
                <glass-search-input
                  v-model="searchQuery"
                  :placeholder="t('shared.help.searchPlaceholder') || '搜索帮助内容...'"
                  size="md"
                  tint="info"
                />
              </div>

              <div
                class="mb-5 rounded-2xl p-4 border border-white/20 dark:border-white/10 shadow-sm glass-ultraThin"
                :class="hasSearchQuery ? 'glass-tint-info' : 'glass-tint-secondary'"
              >
                <div class="text-sm font-medium text-base-content">{{ searchResultSummary }}</div>
                <p class="mt-1 text-xs text-subtle">
                  {{ hasSearchQuery
                    ? (isZh ? '命中词会在标题与摘要中高亮显示，方便快速扫读。' : 'Matched terms are highlighted in titles and excerpts for faster scanning.')
                    : (isZh ? '可按关键词、分类和热度快速筛选帮助内容。' : 'Use keywords, categories, and sort options to narrow the help content quickly.') }}
                </p>
              </div>

              <!-- 分类筛选 -->
              <div class="flex flex-wrap gap-2 mb-5">
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
                <card
                  v-for="a in helpStore.articles"
                  :key="a.id"
                  padding="lg"
                  hoverable
                  class="cursor-pointer group shadow-sm glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10"
                  @click="openArticle(a.slug)"
                >
                  <div class="flex items-center justify-between gap-3 mb-3">
                    <Badge size="sm" variant="info">
                      {{ articleCategoryName(a.categoryId) }}
                    </Badge>
                    <span class="text-xs text-subtle">{{ articleUpdatedAt(a) }}</span>
                  </div>
                  <h3 class="font-medium text-base-content mb-2 transition-colors group-hover:text-cyan-900 dark:group-hover:text-cyan-100" v-html="highlightedArticleTitle(a)"></h3>
                  <p class="text-sm text-subtle line-clamp-3 min-h-[4.5rem]" v-html="highlightedArticleExcerpt(a)"></p>
                  <div class="mt-4 pt-4 border-t border-white/15 dark:border-white/10 text-xs text-subtle flex items-center justify-between">
                    <span>{{ (a.views || 0) }} {{ t('shared.common.views') || '浏览' }}</span>
                    <span>{{ articleFeedbackSummary(a) }}</span>
                  </div>
                </card>
              </div>

              <div
                v-if="!helpStore.loading && helpStore.articles.length===0"
                class="rounded-3xl glass-ultraThin glass-tint-warning border border-dashed border-white/20 dark:border-white/10 px-6 py-8 text-center shadow-sm"
              >
                <div class="text-sm font-medium text-base-content">
                  {{ isZh ? '暂时没有匹配的帮助内容' : 'No matching help content yet' }}
                </div>
                <p class="mt-2 text-sm text-subtle">
                  {{ isZh ? '可以换一个关键词，或者直接查看常见问题/提交支持单。' : 'Try a different keyword, or switch to FAQs or the ticket form.' }}
                </p>
                <div class="mt-4 flex flex-wrap justify-center gap-2">
                  <Button size="sm" variant="outline" @click="activeSection = 'faq'">
                    {{ isZh ? '查看常见问题' : 'Open FAQs' }}
                  </Button>
                  <Button size="sm" variant="primary" @click="activeSection = 'support'">
                    {{ isZh ? '提交支持单' : 'Open support' }}
                  </Button>
                </div>
              </div>
            </card>
          </section>

          <!-- 文章详情 -->
          <section v-if="activeSection === 'articleDetail' && helpStore.article" id="articleDetail">
            <card padding="lg" tint="primary" class="shadow-sm">
              <template #header>
                <div class="flex items-center justify-between gap-3 flex-wrap">
                  <div>
                    <div class="flex items-center gap-2 flex-wrap">
                      <Badge size="sm" variant="info">
                        {{ articleCategoryName(helpStore.article?.categoryId) }}
                      </Badge>
                      <span class="text-xs text-subtle">{{ articleUpdatedAt(helpStore.article) }}</span>
                    </div>
                    <h2 class="mt-2 text-xl font-semibold text-base-content">{{ helpStore.article?.title }}</h2>
                    <div class="mt-3 flex items-center gap-4 flex-wrap text-xs text-subtle">
                      <span>{{ helpStore.article?.views || 0 }} {{ t('shared.common.views') || '浏览' }}</span>
                      <span>{{ helpStore.article?.upVotes || 0 }} {{ isZh ? '有帮助' : 'Helpful' }}</span>
                      <span>{{ helpStore.article?.downVotes || 0 }} {{ isZh ? '没帮助' : 'Not helpful' }}</span>
                    </div>
                  </div>
                  <div class="inline-flex items-center gap-2 flex-nowrap whitespace-nowrap">
                    <Button size="sm" variant="outline" @click="activeSection='articles'">{{ t('shared.common.back') || '返回' }}</Button>
                    <Button size="sm" :loading="isVoting" @click="voteArticle(true)">{{ t('shared.help.actions.useful') || '有帮助' }}</Button>
                    <Button size="sm" variant="outline" :loading="isVoting" @click="voteArticle(false)">{{ t('shared.help.actions.notUseful') || '没帮助' }}</Button>
                  </div>
                </div>
              </template>

              <div class="prose prose-sm md:prose-base dark:prose-invert max-w-none help-md" v-html="renderedArticleContent"></div>

              <div class="mt-6 rounded-3xl px-5 py-4 glass-ultraThin glass-tint-info border border-white/20 dark:border-white/10 shadow-sm">
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                  <div>
                    <div class="font-medium text-base-content">
                      {{ isZh ? '看完仍未解决？' : 'Still not solved?' }}
                    </div>
                    <p class="mt-1 text-sm text-subtle">
                      {{ isZh ? '可以直接进入支持单，并带上你已尝试过的步骤，减少重复说明。' : 'Open a ticket and include what you already tried to avoid repeating context.' }}
                    </p>
                  </div>
                  <Button size="sm" variant="primary" @click="activeSection = 'support'">
                    {{ isZh ? '去提交支持单' : 'Open support ticket' }}
                  </Button>
                </div>
              </div>
            </card>
          </section>
          <!-- 常见问题（daisyUI Accordion with plus/minus） -->
          <section v-if="activeSection === 'faq'" id="faq">
            <card padding="lg" tint="accent" class="shadow-sm">
              <template #header>
                <div>
                  <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.faq') || '常见问题' }}</h2>
                  <p class="mt-1 text-sm text-subtle leading-6">
                    {{ isZh ? '先覆盖登录、课程、作业提交和浏览器问题这几类高频场景。' : 'This section covers the most common scenarios: login, courses, assignment submission, and browser issues.' }}
                  </p>
                </div>
              </template>
              
              <div class="space-y-8">
                <div
                  v-for="category in faqCategories"
                  :key="category.id"
                  class="last:pb-0 rounded-3xl p-5 glass-ultraThin glass-tint-secondary border border-white/20 dark:border-white/10 shadow-sm"
                >
                  <h3 class="text-lg font-medium text-base-content mb-4">{{ category.name }}</h3>
                  <div class="join join-vertical w-full rounded-2xl overflow-hidden border border-white/15 dark:border-white/10">
                    <div
                      v-for="(faq, idx) in category.faqs"
                      :key="faq.id"
                      class="collapse collapse-plus join-item glass-ultraThin glass-tint-info border-white/20 dark:border-white/10 border"
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
            <card padding="lg" tint="success" class="shadow-sm">
              <template #header>
                <div>
                  <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.guide') || '使用指南' }}</h2>
                  <p class="mt-1 text-sm text-subtle leading-6">
                    {{ isZh ? '按照当前登录角色给出最常用的上手路径，尽量少绕路。' : 'This guide shows the fastest common path based on the current role.' }}
                  </p>
                </div>
              </template>
              
              <div class="space-y-8">
                <div v-for="guide in userGuidesFiltered" :key="guide.id">
                  <div class="mb-4 flex items-center justify-between gap-3 flex-wrap">
                    <h3 class="text-lg font-medium text-base-content flex items-center">
                      <component :is="guide.icon" class="w-5 h-5 mr-2 text-emerald-600 dark:text-emerald-400" />
                      {{ guide.title }}
                    </h3>
                    <span class="rounded-full border border-white/15 dark:border-white/10 px-3 py-1 text-xs font-medium text-subtle">
                      {{ isZh ? '推荐路径' : 'Recommended flow' }}
                    </span>
                  </div>
                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div
                      v-for="step in guide.steps"
                      :key="step.id"
                      class="rounded-3xl p-5 transition-all duration-200 hover:-translate-y-0.5 hover:border-emerald-400/25 glass-ultraThin glass-tint-success border border-white/20 dark:border-white/10 shadow-sm"
                    >
                      <div class="flex items-center mb-4">
                        <span class="w-8 h-8 bg-emerald-600 dark:bg-emerald-500 text-white rounded-2xl flex items-center justify-center text-sm font-medium mr-3">
                          {{ step.step }}
                        </span>
                        <h4 class="font-medium text-base-content">{{ step.title }}</h4>
                      </div>
                      <p class="text-sm text-subtle leading-6 min-h-[4.5rem]">{{ step.description }}</p>
                      <Button
                        v-if="step.action"
                        variant="outline"
                        size="sm"
                        class="mt-4"
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
              <card padding="lg" tint="info" class="shadow-sm">
                <template #header>
                  <div>
                    <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.support') || '联系技术支持' }}</h2>
                    <p class="mt-1 text-sm text-subtle leading-6">
                      {{ isZh ? '适合无法自助解决的问题。先看联系方式，再决定是否立即提交支持单。' : 'Use this when self-service is not enough. Check contact options first, then decide whether to submit a ticket.' }}
                    </p>
                  </div>
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

                <div class="mt-5 rounded-3xl p-4 glass-thin glass-tint-accent border border-white/20 dark:border-white/10 shadow-sm">
                  <div class="text-sm font-medium text-base-content">
                    {{ isZh ? '填写建议' : 'Tips for faster handling' }}
                  </div>
                  <div class="mt-3 space-y-2">
                    <div
                      v-for="tip in supportChecklist"
                      :key="tip"
                      class="flex items-start gap-2 text-sm text-subtle"
                    >
                      <span class="mt-1 h-1.5 w-1.5 rounded-full bg-emerald-500 shrink-0"></span>
                      <span>{{ tip }}</span>
                    </div>
                  </div>
                </div>

                <div
                  class="mt-5 rounded-2xl p-4 glass-ultraThin border border-white/20 dark:border-white/10 shadow-sm"
                  :class="ticketRoutingHint.surfaceClass"
                >
                  <div class="text-sm font-medium text-base-content">{{ ticketRoutingHint.title }}</div>
                  <p class="mt-1 text-xs text-subtle leading-5">{{ ticketRoutingHint.description }}</p>
                </div>
              </card>

              <!-- 提交工单 -->
              <card padding="lg" tint="accent" class="shadow-sm">
                <template #header>
                  <div>
                    <h2 class="text-xl font-semibold text-base-content">{{ t('shared.help.sections.ticket') || '提交支持单' }}</h2>
                    <p class="mt-1 text-sm text-subtle leading-6">
                      {{ isZh ? '统一记录问题、补充信息和管理员回复，避免多处重复沟通。' : 'Keep the issue, follow-up details, and admin replies in one place.' }}
                    </p>
                  </div>
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

                  <p class="text-xs text-subtle text-center">
                    {{ isZh ? '提交后可在“我的工单”中继续补充说明，管理员回复也会集中展示。' : 'After submission, continue the thread in “My Tickets” and review admin replies there.' }}
                  </p>
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
            <card padding="lg" tint="warning" class="shadow-sm">
              <template #header>
                <div>
                  <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ t('shared.help.sections.feedback') || '意见反馈' }}</h2>
                  <p class="mt-1 text-sm text-subtle leading-6">
                    {{ isZh ? '产品建议、体验问题和错误报告统一归档到支持单，方便跟踪处理进度。' : 'Feature ideas, UX issues, and bug reports are all tracked through the same ticket system.' }}
                  </p>
                </div>
              </template>

              <div class="space-y-5">
                <p class="text-sm text-subtle">
                  {{ isZh
                    ? '选择合适的反馈类型后，会自动带入支持单表单。这样你后续补充细节、查看处理状态都会更集中。'
                    : 'Choose a feedback type and we will prefill the unified ticket form. This keeps later updates and status tracking in one place.' }}
                </p>

                <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <button
                    v-for="type in feedbackTypeOptions"
                    :key="type.value"
                    type="button"
                    class="rounded-3xl p-4 text-left cursor-pointer transition-all duration-200 hover:-translate-y-0.5 hover:border-emerald-400/25 glass-ultraThin glass-tint-warning border border-white/20 dark:border-white/10 shadow-sm"
                    @click="startFeedbackTicket(type.value)"
                  >
                    <div class="h-10 w-10 rounded-2xl bg-white/70 dark:bg-white/10 text-emerald-600 dark:text-emerald-300 flex items-center justify-center">
                      <component :is="type.icon" class="w-5 h-5" />
                    </div>
                    <div class="mt-3">
                      <div class="text-sm font-medium text-base-content">{{ type.label }}</div>
                      <p class="mt-1 text-xs text-subtle leading-5">{{ type.description }}</p>
                    </div>
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
import GlassSearchInput from '@/components/ui/inputs/GlassSearchInput.vue'
import GlassTextarea from '@/components/ui/inputs/GlassTextarea.vue'
// 动态背景已移除
import { useI18n } from 'vue-i18n'
const { t, locale } = useI18n()
import { useHelpStore } from '@/stores/help'
import { useAuthStore } from '@/stores/auth'
import type { HelpArticle } from '@/types/help'
import { installCodeCopyHandler, renderMarkdown } from '@/shared/utils/markdownRenderer'
import GlassPopoverSelect from '@/components/ui/filters/GlassPopoverSelect.vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowPathIcon,
  EyeIcon,
  PaperAirplaneIcon,
  QuestionMarkCircleIcon,
  BookOpenIcon,
  ChatBubbleLeftIcon,
  ClockIcon,
  EnvelopeIcon,
  LightBulbIcon,
  HeartIcon,
  BugAntIcon,
  AcademicCapIcon,
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
const activeSection = ref('articles')
const searchQuery = ref('')
const isSubmittingTicket = ref(false)
const isReplyingTicket = ref(false)
const selectedCategoryId = ref<number | null>(null)
const sortMode = ref<'latest' | 'hot'>('hot')
const isVoting = ref(false)
const ticketReply = ref('')
const myTicketsPanelRef = ref<HTMLElement | null>(null)
const lastHandledRouteTicketId = ref<number | null>(null)
let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null

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

const categoryNameMap = computed<Record<number, string>>(() =>
  Object.fromEntries((helpStore.categories || []).map(category => [category.id, category.name]))
)

const isTeacherRole = computed(() => authStore.userRole === 'TEACHER')
const hasSearchQuery = computed(() => searchQuery.value.trim().length > 0)
const searchTerms = computed(() =>
  Array.from(new Set(
    searchQuery.value
      .split(/\s+/)
      .map(term => term.trim())
      .filter(Boolean)
  )).slice(0, 4)
)

const searchSuggestions = computed(() => {
  const common = isZh.value
    ? ['登录与账户', '作业提交', '学习进度', '浏览器兼容']
    : ['Login & account', 'Assignment submission', 'Learning progress', 'Browser support']

  if (isTeacherRole.value) {
    return [
      ...common,
      isZh.value ? '课程管理' : 'Course management',
      isZh.value ? 'AI批改' : 'AI grading'
    ]
  }

  return [
    ...common,
    isZh.value ? '成绩查看' : 'Grade review',
    isZh.value ? '学习助手' : 'Learning assistant'
  ]
})

const roleLanding = computed(() => (
  isTeacherRole.value
    ? {
        badge: isZh.value ? '教师支持台' : 'Teacher desk',
        title: isZh.value ? '按教学任务快速进入排查与处理流程' : 'Jump straight into teaching-side troubleshooting and support flows',
        description: isZh.value ? '优先处理课程管理、学生管理、AI 批改和教学分析相关问题。' : 'Prioritize course operations, student management, AI grading, and teaching analytics issues.',
        icon: UserGroupIcon,
        iconWrapClass: 'glass-tint-info text-cyan-900 dark:text-cyan-100'
      }
    : {
        badge: isZh.value ? '学生支持台' : 'Student desk',
        title: isZh.value ? '围绕学习过程快速定位答案与下一步动作' : 'Find learning-side answers and the next best action quickly',
        description: isZh.value ? '优先覆盖课程学习、作业提交、成绩查看和学习助手相关问题。' : 'Focus on course learning, assignment submission, grade review, and learning-assistant issues.',
        icon: AcademicCapIcon,
        iconWrapClass: 'glass-tint-success text-emerald-900 dark:text-emerald-100'
      }
))

const roleFocusItems = computed(() => (
  isTeacherRole.value
    ? [
        isZh.value ? '课程创建、排课和学生名单问题优先看教师指南。' : 'Start with the teacher guide for course setup, scheduling, and roster issues.',
        isZh.value ? 'AI 批改和数据分析异常建议附上具体作业或课程名称。' : 'For AI grading or analytics issues, include the course or assignment name.'
      ]
    : [
        isZh.value ? '登录、选课、提交作业和成绩查看都可以先走自助内容。' : 'Login, enrollment, assignment submission, and grades are all covered by self-service content first.',
        isZh.value ? '学习助手或社区问题建议带上触发步骤，便于复现。' : 'For assistant or community issues, include the exact trigger steps so they can be reproduced.'
      ]
))

const quickEntryCards = computed(() => ([
  {
    id: 'articles',
    tag: isZh.value ? '高频解答' : 'Answers',
    title: isZh.value ? '先看帮助文章' : 'Start with articles',
    description: isZh.value ? '适合快速查找流程说明、常见报错和操作路径。' : 'Use this for quick guidance, common errors, and operational steps.',
    icon: BookOpenIcon,
    surfaceClass: 'glass-ultraThin glass-tint-info',
    iconWrapClass: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-300',
    action: () => {
      activeSection.value = 'articles'
    }
  },
  {
    id: 'guide',
    tag: isZh.value ? '角色引导' : 'Role guide',
    title: authStore.userRole === 'TEACHER'
      ? (isZh.value ? '查看教师流程' : 'Open teacher flow')
      : (isZh.value ? '查看学生流程' : 'Open student flow'),
    description: isZh.value ? '按照当前身份给出常见任务的最短路径，减少摸索成本。' : 'See the fastest path for common tasks based on your current role.',
    icon: ChartBarIcon,
    surfaceClass: 'glass-ultraThin glass-tint-success',
    iconWrapClass: 'bg-cyan-500/15 text-cyan-600 dark:text-cyan-300',
    action: () => {
      activeSection.value = 'guide'
    }
  },
  {
    id: 'support',
    tag: isZh.value ? '继续跟进' : 'Escalate',
    title: isZh.value ? '统一支持单' : 'Unified support ticket',
    description: isZh.value ? '无法自助解决时，集中提交、补充和跟踪管理员回复。' : 'Submit, follow up, and track admin replies in one thread.',
    icon: TicketIcon,
    surfaceClass: 'glass-ultraThin glass-tint-accent',
    iconWrapClass: 'bg-amber-400/15 text-amber-600 dark:text-amber-200',
    action: () => {
      activeSection.value = 'support'
    }
  }
]))

const serviceJourney = computed(() => ([
  {
    step: '01',
    title: isZh.value ? '先搜索答案' : 'Search first',
    description: isZh.value ? '优先通过文章、FAQ 找到现成答案，缩短处理时间。' : 'Check articles and FAQs first to resolve the issue immediately when possible.',
    surfaceClass: 'glass-tint-info',
    badgeClass: 'glass-tint-info text-cyan-900 dark:text-cyan-100'
  },
  {
    step: '02',
    title: isZh.value ? '按角色走指南' : 'Follow role-based guides',
    description: isZh.value ? '学生和教师看到不同的快捷路径，直接进入对应功能。' : 'Students and teachers get different shortcuts so they can jump straight into the right flow.',
    surfaceClass: 'glass-tint-success',
    badgeClass: 'glass-tint-success text-emerald-900 dark:text-emerald-100'
  },
  {
    step: '03',
    title: isZh.value ? '统一支持单继续处理' : 'Continue in one ticket',
    description: isZh.value ? '仍未解决时，用同一条支持单补充信息、查看回复和追踪状态。' : 'If the issue remains, keep the context, replies, and status updates in one shared ticket thread.',
    surfaceClass: 'glass-tint-accent',
    badgeClass: 'glass-tint-accent text-violet-900 dark:text-violet-100'
  }
]))

const servicePromises = computed(() => ([
  {
    title: isZh.value ? '统一入口' : 'Single intake',
    description: isZh.value ? '帮助、反馈和技术支持不再分散，降低重复沟通。' : 'Help, feedback, and support are routed through one consistent intake flow.',
    surfaceClass: 'glass-tint-primary',
    icon: BookOpenIcon,
    iconWrapClass: 'glass-tint-info text-cyan-900 dark:text-cyan-100'
  },
  {
    title: isZh.value ? '对话留痕' : 'Threaded follow-up',
    description: isZh.value ? '支持单保留历史上下文，方便持续补充。' : 'Each ticket keeps the full conversation so you can continue without restarting.',
    surfaceClass: 'glass-tint-info',
    icon: ChatBubbleLeftIcon,
    iconWrapClass: 'glass-tint-accent text-violet-900 dark:text-violet-100'
  },
  {
    title: isZh.value ? '角色化指引' : 'Role-aware guidance',
    description: isZh.value ? '学生与教师展示不同内容，减少无关信息。' : 'Students and teachers see different guidance to keep the page focused.',
    surfaceClass: 'glass-tint-success',
    icon: UserGroupIcon,
    iconWrapClass: 'glass-tint-success text-emerald-900 dark:text-emerald-100'
  }
]))

const searchResultSummary = computed(() => {
  if (!hasSearchQuery.value) {
    return isZh.value
      ? `当前展示 ${helpStore.articles.length} 篇帮助内容，可直接按关键词或分类缩小范围。`
      : `Showing ${helpStore.articles.length} help articles. Use keywords or categories to narrow the list.`
  }

  return isZh.value
    ? `已按“${searchQuery.value.trim()}”筛选，当前命中 ${helpStore.articles.length} 篇内容。`
    : `Filtered by "${searchQuery.value.trim()}". ${helpStore.articles.length} articles matched.`
})

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

const supportChecklist = computed(() => ([
  isZh.value ? '标题写清楚现象和影响范围，例如“学生端上传作业后页面一直转圈”。' : 'Make the title specific, for example: “Student upload keeps spinning after submission”.',
  isZh.value ? '描述里补充发生时间、操作步骤、账号角色和你已经尝试过的处理方式。' : 'Include when it happened, what you clicked, your role, and what you already tried.',
  isZh.value ? '如果是间歇性问题，尽量说明出现频率，方便排查。' : 'If the issue is intermittent, mention how often it happens.'
]))

const ticketRoutingHint = computed(() => {
  const isFeedback = ticketForm.channel === 'feedback'
  const isUrgent = ticketForm.priority === 'urgent' || ticketForm.priority === 'high'
  const currentType = ticketTypeText(ticketForm.type || (isFeedback ? 'feedback' : 'technical'))

  if (isFeedback) {
    return {
      title: isZh.value ? `当前将按“${currentType}”归档` : `This will be tracked as "${currentType}"`,
      description: isZh.value
        ? '适合产品建议、体验问题和错误反馈。提交后建议在同一工单里持续补充上下文。'
        : 'Use this for product ideas, UX issues, and bug feedback. Keep follow-up details in the same ticket thread.',
      surfaceClass: 'glass-tint-warning'
    }
  }

  if (isUrgent) {
    return {
      title: isZh.value ? `当前优先级较高：${currentType}` : `Higher priority selected: ${currentType}`,
      description: isZh.value
        ? '建议在描述中写明影响范围、受影响角色和复现频率，便于优先判断。'
        : 'Include impact scope, affected roles, and how often it occurs so triage can happen faster.',
      surfaceClass: 'glass-tint-warning'
    }
  }

  return {
    title: isZh.value ? `当前将按“${currentType}”进入支持流程` : `This will enter the support flow as "${currentType}"`,
    description: isZh.value
      ? '提交后可在“我的工单”持续补充信息，管理员回复也会集中在同一条记录。'
      : 'After submission, continue adding details in My Tickets and review admin replies in the same thread.',
    surfaceClass: 'glass-tint-info'
  }
})

// 反馈类型（随语言切换动态更新）
const feedbackTypeOptions = computed(() => ([
  {
    value: 'bug',
    label: t('shared.help.form.typeBug') || '错误报告',
    description: isZh.value ? '报错、异常行为、无法完成操作' : 'Errors, unexpected behavior, or broken flows',
    icon: BugAntIcon
  },
  {
    value: 'feature',
    label: t('shared.help.form.typeFeature') || '功能建议',
    description: isZh.value ? '你希望新增的能力或优化方向' : 'New capabilities or workflow improvements you want',
    icon: LightBulbIcon
  },
  {
    value: 'experience',
    label: t('shared.help.form.typeExperience') || '用户体验',
    description: isZh.value ? '信息难找、流程绕、提示不清晰' : 'Confusing copy, hard-to-find actions, or clunky flows',
    icon: HeartIcon
  },
  {
    value: 'other',
    label: t('shared.help.form.typeOther') || '其他反馈',
    description: isZh.value ? '不属于以上分类，但值得记录' : 'Anything else worth tracking',
    icon: ChatBubbleLeftIcon
  }
]))

// 方法
const escapeHtml = (value: string) => value
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

const escapeRegExp = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

const highlightText = (value?: string | null) => {
  let output = escapeHtml(String(value || ''))
  if (!searchTerms.value.length) return output

  for (const term of searchTerms.value) {
    const escapedTerm = escapeRegExp(escapeHtml(term))
    output = output.replace(
      new RegExp(`(${escapedTerm})`, 'ig'),
      '<mark class="rounded-md px-1 py-0.5 bg-amber-300/75 text-slate-900">$1</mark>'
    )
  }

  return output
}

const articleCategoryName = (categoryId?: number | null) =>
  categoryNameMap.value[Number(categoryId || 0)] || (isZh.value ? '通用帮助' : 'General')

const articleExcerpt = (article?: HelpArticle | null) => {
  const source = String(article?.contentMd || article?.contentHtml || '')
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  if (!source) {
    return isZh.value ? '查看这篇文章了解更完整的操作说明与解决思路。' : 'Open this article for the full workflow and troubleshooting details.'
  }

  return source.length > 120 ? `${source.slice(0, 120)}...` : source
}

const articleUpdatedAt = (article?: HelpArticle | null) =>
  formatTicketTime(article?.updatedAt || article?.createdAt)

const highlightedArticleTitle = (article?: HelpArticle | null) =>
  highlightText(article?.title || '')

const highlightedArticleExcerpt = (article?: HelpArticle | null) =>
  highlightText(articleExcerpt(article))

const articleFeedbackSummary = (article?: HelpArticle | null) => {
  const upVotes = Number(article?.upVotes || 0)
  const downVotes = Number(article?.downVotes || 0)
  const totalVotes = upVotes + downVotes

  if (!totalVotes) {
    return isZh.value ? '暂无反馈' : 'No feedback yet'
  }

  if (!downVotes) {
    return isZh.value ? `${upVotes} 人觉得有帮助` : `${upVotes} found this helpful`
  }

  return isZh.value
    ? `${upVotes}/${totalVotes} 觉得有帮助`
    : `${upVotes}/${totalVotes} found this helpful`
}

const renderedArticleContent = computed(() => {
  const article = helpStore.article
  if (!article) return ''

  const markdown = String(article.contentMd || '').trim()
  if (markdown) {
    return renderMarkdown(markdown)
  }

  return String(article.contentHtml || '')
})

const clearSearch = () => {
  const changed = searchQuery.value !== ''
  searchQuery.value = ''
  if (!changed) searchHelp()
}

const applySearchSuggestion = (keyword: string) => {
  const changed = searchQuery.value !== keyword
  searchQuery.value = keyword
  if (!changed) searchHelp()
}

function pickCategory(id: number | null) {
  selectedCategoryId.value = id
  activeSection.value = 'articles'
  helpStore.searchArticles(searchQuery.value || undefined, id === null ? undefined : id, undefined, sortMode.value)
}

function changeSort(s: 'latest' | 'hot') {
  sortMode.value = s
  activeSection.value = 'articles'
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
  activeSection.value = 'articles'
  helpStore.searchArticles(
    searchQuery.value || undefined,
    selectedCategoryId.value === null ? undefined : selectedCategoryId.value,
    undefined,
    sortMode.value
  )
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
  if (!ticketForm.title.trim() || ticketForm.title.trim() === '意见反馈' || ticketForm.title.trim() === 'Feedback') {
    ticketForm.title = type === 'feedback'
      ? (isZh.value ? '意见反馈' : 'Feedback')
      : (isZh.value ? `${ticketTypeText(type)}反馈` : `${ticketTypeText(type)}`)
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
  installCodeCopyHandler()
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

watch(searchQuery, (value, oldValue) => {
  if (value === oldValue) return
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
  searchDebounceTimer = setTimeout(() => {
    searchHelp()
  }, 220)
})

watch(
  () => [route.query.ticketId, authStore.isAuthenticated],
  () => {
    focusTicketFromRoute().catch(() => {})
  }
)
</script> 

<style scoped>
.help-md :deep(pre) {
  margin: 0.75rem 0;
  border-radius: 1rem;
  overflow-x: auto;
}

.help-md :deep(code) {
  background-color: color-mix(in oklab, var(--color-base-200) 58%, transparent);
  padding: 0.125rem 0.3rem;
  border-radius: 0.4rem;
  font-size: 0.85em;
}

.help-md :deep(pre code) {
  display: block;
  padding: 0.9rem 1rem;
  background: transparent;
}

.help-md :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
  text-underline-offset: 0.18em;
}

.help-md :deep(table.ai-table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0.75rem 0;
  overflow: hidden;
  border-radius: 1rem;
}

.help-md :deep(table.ai-table th),
.help-md :deep(table.ai-table td) {
  border: 1px solid var(--glass-border-color, rgba(128, 128, 128, 0.3));
  padding: 0.6rem 0.8rem;
}

.help-md :deep(table.ai-table thead th) {
  background-color: color-mix(in oklab, var(--color-base-200) 45%, transparent);
}

.dark .help-md :deep(table.ai-table thead th) {
  background-color: color-mix(in oklab, var(--color-base-200) 25%, transparent);
}

.help-md :deep(ul),
.help-md :deep(ol) {
  padding-left: 1.5em;
  margin: 0.45rem 0;
}

.help-md :deep(li) {
  margin: 0.2rem 0;
}

.help-md :deep(blockquote) {
  border-left: 3px solid var(--color-primary, #3b82f6);
  padding-left: 0.9rem;
  margin: 0.75rem 0;
  opacity: 0.9;
}

.help-md :deep(h1),
.help-md :deep(h2),
.help-md :deep(h3),
.help-md :deep(h4) {
  margin: 0.9rem 0 0.35rem;
  font-weight: 700;
}

.help-md :deep(hr) {
  border-color: var(--glass-border-color, rgba(128, 128, 128, 0.2));
  margin: 0.9rem 0;
}

.help-md :deep(.code-block-wrapper) {
  margin: 0.8rem 0;
  border: 1px solid var(--glass-border-color, rgba(128, 128, 128, 0.25));
  border-radius: 1rem;
  overflow: hidden;
  background: color-mix(in oklab, var(--color-base-100) 82%, transparent);
}

.help-md :deep(.code-block-header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.55rem 0.8rem;
  font-size: 0.75rem;
  color: var(--color-base-content);
  opacity: 0.8;
  border-bottom: 1px solid var(--glass-border-color, rgba(128, 128, 128, 0.18));
}

.help-md :deep(.code-lang-label) {
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.help-md :deep(.code-copy-btn) {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: 0;
  background: transparent;
  color: inherit;
  cursor: pointer;
  padding: 0.2rem 0.35rem;
  border-radius: 999px;
  opacity: 0.82;
  transition: background-color 0.2s ease, opacity 0.2s ease, color 0.2s ease;
}

.help-md :deep(.code-copy-btn:hover) {
  opacity: 1;
  background: color-mix(in oklab, var(--color-base-200) 50%, transparent);
}

.help-md :deep(.code-copy-btn.copied) {
  color: #10b981;
}
</style>
