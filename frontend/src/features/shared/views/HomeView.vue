<template>
  <div>
    <!-- 概览（Aurora + TextScrollReveal + LiquidLogo + AppleCardCarousel + Meteors） -->
    <section id="overview" class="scroll-mt-24">
      <div class="relative">
        

        <section class="py-10 md:py-16">
          <div class="grid md:grid-cols-12 gap-8 items-center">
            <div class="md:col-span-6 min-w-0">
              <div class="mb-8 md:mb-10">
                <img src="/brand/logo.png" alt="System Logo" class="h-28 md:h-36 w-auto select-none pointer-events-none" />
              </div>
              <LetterPullup
                :words="t('app.home.hero.title') as string"
                align="left"
                class="text-left text-5xl md:text-7xl font-extrabold leading-tight mb-6 text-[color:var(--color-base-content)]"
              />
              <TextGenerateEffect :words="t('app.home.hero.subtitle') as string" :duration="0.7" :delay="100" :filter="true" class="text-left text-lg md:text-2xl leading-relaxed text-subtle" />
              
            </div>
            <div class="md:col-span-6">
              <LiquidGlass :radius="16" :frost="0.06" :scale="-100" :tint="true" :tint-from="'var(--color-theme-primary)'" :tint-to="'var(--color-theme-accent)'" :container-class="'relative rounded-2xl w-full aspect-[16/10] overflow-hidden'">
                <div class="relative w-full h-full">
                  <BendingGallery class="absolute inset-0" :bend="3" :border-radius="0.06" :items="galleryItems" :aspect-ratio="16/9" :dpr="2" :anisotropy="8" :wobble="0.015" />
                </div>
              </LiquidGlass>
            </div>
          </div>
        </section>

        <section class="mt-6 md:mt-10">
          <div class="relative">
            <div class="absolute inset-0 opacity-60 pointer-events-none">
              <Meteors :count="18" />
            </div>
            <div class="relative">
              <AppleCardCarousel :items="carouselItems" />
            </div>
          </div>
        </section>
      </div>
    </section>

    <!-- Apple 风格卡片轮播（6 张） -->
    <section id="apps" class="mt-4 md:mt-6 scroll-mt-24">
      <AppleCardCarousel>
        <AppleCarouselItem v-for="(c, idx) in appCards" :key="idx" :index="idx">
          <AppleCard :card="c" :index="idx" :layout="true">
            <div class="prose prose-sm max-w-none dark:prose-invert">
              <h4 v-if="idx !== 0 && idx !== 1 && idx !== 2 && idx !== 3 && idx !== 4">{{ c.title }}</h4>
              <p v-if="idx !== 0 && idx !== 2 && idx !== 1 && idx !== 3 && idx !== 4">{{ c.desc }}</p>
              <ul v-if="idx !== 2 && idx !== 0 && idx !== 1 && idx !== 3 && idx !== 4">
                <li v-for="p in c.points" :key="p">{{ p }}</li>
              </ul>
              <!-- 多维评估（第一张卡片）追加内容：两张图 + 五维介绍 -->
              <template v-if="idx === 0">
                <div class="mt-4 space-y-4">
                  <p class="text-xl md:text-2xl text-subtle -mt-4 md:-mt-6">{{ t('app.home.cards.assessment.rich.subtitle') }}</p>
                  <img :src="'/home/apps/assessment-1.png'" :alt="t('app.home.cards.assessment.img1Alt') as string" class="rounded-xl w-full h-auto object-cover" />
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold">{{ t('app.home.cards.assessment.rich.h1.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.assessment.rich.h1.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.assessment.rich.h1.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold">{{ t('app.home.cards.assessment.rich.h2.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.assessment.rich.h2.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.assessment.rich.h2.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold">{{ t('app.home.cards.assessment.rich.h3.title') }}</h5>
                    <ul class="mt-2 list-disc pl-5">
                      <li>{{ t('app.home.cards.assessment.rich.h3.b1') }}</li>
                      <li>{{ t('app.home.cards.assessment.rich.h3.b2') }}</li>
                    </ul>
                    <p class="mt-1">{{ t('app.home.cards.assessment.rich.h3.outro') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold">{{ t('app.home.cards.assessment.rich.h4.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.assessment.rich.h4.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.assessment.rich.h4.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.assessment.rich.closing.title') }}</h5>
                    <p class="mt-0">{{ t('app.home.cards.assessment.rich.closing.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.assessment.rich.closing.p2') }}</p>
                  </div>
                  <!-- 第二张图：放在底部 -->
                  <img :src="'/home/apps/assessment-2.png'" :alt="t('app.home.cards.assessment.img2Alt') as string" class="rounded-xl w-full h-auto object-cover" />
                </div>
                <div class="mt-6">
                  <h5 class="text-base font-semibold">{{ t('app.home.cards.assessment.dimensions.title') }}</h5>
                  <div class="mt-3 space-y-3">
                    <div>
                      <div class="font-medium">
                        {{ t('app.home.cards.assessment.dimensions.moral.title') }}
                        <span class="text-xs text-subtle">({{ t('app.home.cards.assessment.dimensions.moral.key') }})</span>
                      </div>
                      <p class="text-sm">{{ t('app.home.cards.assessment.dimensions.moral.desc') }}</p>
                      <p class="text-xs text-subtle">{{ t('app.home.cards.assessment.dimensions.moral.logic') }}</p>
                    </div>
                    <div>
                      <div class="font-medium">
                        {{ t('app.home.cards.assessment.dimensions.attitude.title') }}
                        <span class="text-xs text-subtle">({{ t('app.home.cards.assessment.dimensions.attitude.key') }})</span>
                      </div>
                      <p class="text-sm">{{ t('app.home.cards.assessment.dimensions.attitude.desc') }}</p>
                      <p class="text-xs text-subtle">{{ t('app.home.cards.assessment.dimensions.attitude.logic') }}</p>
                    </div>
                    <div>
                      <div class="font-medium">
                        {{ t('app.home.cards.assessment.dimensions.ability.title') }}
                        <span class="text-xs text-subtle">({{ t('app.home.cards.assessment.dimensions.ability.key') }})</span>
                      </div>
                      <p class="text-sm">{{ t('app.home.cards.assessment.dimensions.ability.desc') }}</p>
                      <p class="text-xs text-subtle">{{ t('app.home.cards.assessment.dimensions.ability.logic') }}</p>
                    </div>
                    <div>
                      <div class="font-medium">
                        {{ t('app.home.cards.assessment.dimensions.method.title') }}
                        <span class="text-xs text-subtle">({{ t('app.home.cards.assessment.dimensions.method.key') }})</span>
                      </div>
                      <p class="text-sm">{{ t('app.home.cards.assessment.dimensions.method.desc') }}</p>
                      <p class="text-xs text-subtle">{{ t('app.home.cards.assessment.dimensions.method.logic') }}</p>
                    </div>
                    <div>
                      <div class="font-medium">
                        {{ t('app.home.cards.assessment.dimensions.grade.title') }}
                        <span class="text-xs text-subtle">({{ t('app.home.cards.assessment.dimensions.grade.key') }})</span>
                      </div>
                      <p class="text-sm">{{ t('app.home.cards.assessment.dimensions.grade.desc') }}</p>
                      <p class="text-xs text-subtle">{{ t('app.home.cards.assessment.dimensions.grade.logic') }}</p>
                    </div>
                  </div>
                </div>
              </template>
              
              <!-- 聊天与通知（第五张卡片）追加内容：丰富介绍模块 -->
              <template v-if="idx === 4">
                <div class="mt-5 space-y-4">
                  <p class="text-xl md:text-2xl text-subtle -mt-4 md:-mt-6">{{ t('app.home.cards.chat.rich.subtitle') }}</p>
                  <!-- 概览图：副标题下方、正文上方 -->
                  <img :src="'/home/apps/chat-overview.png'" :alt="t('app.home.cards.chat.rich.imgAlt') as string" class="rounded-xl w-full h-auto object-cover" />
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.chat.rich.h1.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.chat.rich.h1.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.chat.rich.h1.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.chat.rich.h2.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.chat.rich.h2.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.chat.rich.h2.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.chat.rich.h3.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.chat.rich.h3.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.chat.rich.h3.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.chat.rich.h4.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.chat.rich.h4.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.chat.rich.h4.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.chat.rich.closing.title') }}</h5>
                    <p class="mt-0">{{ t('app.home.cards.chat.rich.closing.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.chat.rich.closing.p2') }}</p>
                  </div>
                  <!-- 底部图：放在结语之后 -->
                  <img :src="'/home/apps/chat-detail.png'" :alt="t('app.home.cards.chat.rich.imgAltBottom') as string" class="mt-5 rounded-xl w-full h-auto object-cover" />
                </div>
              </template>

              <!-- AI 批改（第三张卡片）追加内容：丰富介绍模块 -->
              <template v-if="idx === 2">
                <div class="mt-5 space-y-4">
                  <p class="text-xl md:text-2xl text-subtle -mt-4 md:-mt-6">{{ t('app.home.cards.ai.rich.subtitle') }}</p>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.ai.rich.h1.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.ai.rich.h1.intro') }}</p>
                    <ul class="mt-2 list-disc pl-5">
                      <li>{{ t('app.home.cards.ai.rich.h1.b1') }}</li>
                      <li>{{ t('app.home.cards.ai.rich.h1.b2') }}</li>
                      <li>{{ t('app.home.cards.ai.rich.h1.b3') }}</li>
                    </ul>
                    <p class="mt-1 text-sm text-subtle">{{ t('app.home.cards.ai.rich.h1.outro') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.ai.rich.h2.title') }}</h5>
                    <ul class="mt-0 list-disc pl-5">
                      <li>{{ t('app.home.cards.ai.rich.h2.b1') }}</li>
                      <li>{{ t('app.home.cards.ai.rich.h2.b2') }}</li>
                      <li>{{ t('app.home.cards.ai.rich.h2.b3') }}</li>
                      <li>{{ t('app.home.cards.ai.rich.h2.b4') }}</li>
                    </ul>
                    <p class="mt-0 text-sm text-subtle">{{ t('app.home.cards.ai.rich.h2.outro') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.ai.rich.h3.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.ai.rich.h3.text') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.ai.rich.closing.title') }}</h5>
                    <p class="mt-0">{{ t('app.home.cards.ai.rich.closing.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.ai.rich.closing.p2') }}</p>
                  </div>
                  <!-- AI 概览图：移至底部展示 -->
              <img :src="'/home/apps/ai-overview.png'" :alt="t('app.home.cards.ai.rich.imgAlt') as string" class="mt-5 rounded-xl w-full h-auto object-cover" />
                </div>
              </template>

              <!-- 学习社区（第四张卡片）追加内容：丰富介绍模块 -->
              <template v-if="idx === 3">
                <div class="mt-5 space-y-4">
                  <p class="text-xl md:text-2xl text-subtle -mt-4 md:-mt-6">{{ t('app.home.cards.community.rich.subtitle') }}</p>
                  <!-- 社区概览图：副标题下方、正文上方 -->
                  <img :src="'/home/apps/community-overview.png'" :alt="t('app.home.cards.community.rich.imgAlt') as string" class="rounded-xl w-full h-auto object-cover" />
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.community.rich.h1.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.community.rich.h1.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.community.rich.h1.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.community.rich.h2.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.community.rich.h2.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.community.rich.h2.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.community.rich.h3.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.community.rich.h3.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.community.rich.h3.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.community.rich.h4.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.community.rich.h4.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.community.rich.h4.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.community.rich.closing.title') }}</h5>
                    <p class="mt-0">{{ t('app.home.cards.community.rich.closing.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.community.rich.closing.p2') }}</p>
                  </div>
                </div>
              </template>
              <!-- 课程与作业（第二张卡片）追加内容：丰富介绍模块 -->
              <template v-if="idx === 1">
                <div class="mt-5 space-y-4">
                  <p class="text-xl md:text-2xl text-subtle -mt-4 md:-mt-6">{{ t('app.home.cards.courses.rich.subtitle') }}</p>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.courses.rich.h1.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.courses.rich.h1.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.courses.rich.h1.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.courses.rich.h2.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.courses.rich.h2.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.courses.rich.h2.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.courses.rich.h3.title') }}</h5>
                    <p class="mt-3">{{ t('app.home.cards.courses.rich.h3.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.courses.rich.h3.p2') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.courses.rich.h4.title') }}</h5>
                    <ul class="mt-3 list-disc pl-5">
                      <li>{{ t('app.home.cards.courses.rich.h4.b1') }}</li>
                      <li>{{ t('app.home.cards.courses.rich.h4.b2') }}</li>
                      <li>{{ t('app.home.cards.courses.rich.h4.b3') }}</li>
                    </ul>
                    <p class="mt-0">{{ t('app.home.cards.courses.rich.h4.outro') }}</p>
                  </div>
                  <div class="opacity-60 select-none">⸻</div>
                  <div>
                    <h5 class="text-base font-semibold mb-3 md:mb-4">{{ t('app.home.cards.courses.rich.closing.title') }}</h5>
                    <p class="mt-0">{{ t('app.home.cards.courses.rich.closing.p1') }}</p>
                    <p class="-mt-1 md:-mt-2">{{ t('app.home.cards.courses.rich.closing.p2') }}</p>
                  </div>
                  <!-- 课程与作业概览图：放在底部 -->
                  <img :src="'/home/apps/courses-overview.png'" :alt="t('app.home.cards.courses.rich.imgAlt') as string" class="mt-5 rounded-xl w-full h-auto object-cover" />
                </div>
              </template>
            </div>
          </AppleCard>
        </AppleCarouselItem>
      </AppleCardCarousel>
    </section>

    <!-- 对比（Compare） -->
    <section id="compare" class="mt-14 scroll-mt-24">
      <div class="space-y-20">
        <!-- 明暗主题对比场景：统一 sticky 行，左右同步 -->
        <div id="scene-lightdark" class="relative h-[260vh]">
          <div class="sticky top-0 grid md:grid-cols-2 items-center gap-8 h-screen">
            <div class="px-2">
              <SafariMockup class="w-full aspect-[1203/753]" url="student-competency.com">
                <div class="absolute inset-0 overflow-hidden">
                  <CompareSlider
                    first-image="/home/compare/theme-light.png"
                    second-image="/home/compare/theme-dark.png"
                    :first-image-alt="t('app.home.compare.light')"
                    :second-image-alt="t('app.home.compare.dark')"
                    first-content-class="object-cover object-center scale-[1.02]"
                    second-content-class="object-cover object-center scale-[1.02]"
                    class="w-full h-full"
                    slide-mode="hover"
                    :autoplay="true"
                    :autoplay-duration="5000"
                  />
                </div>
              </SafariMockup>
            </div>
            <div class="px-2">
              <TextScrollReveal
                :text="t('app.home.compareDesc.lightDark') as string"
                :sticky="false"
                progress-container-id="scene-lightdark"
                :reveal-portion="0.5"
              />
            </div>
          </div>
        </div>

        <!-- 中英文对比场景：统一 sticky 行，左右同步 -->
        <div id="scene-zhen" class="relative h-[260vh]">
          <div class="sticky top-0 grid md:grid-cols-2 items-center gap-8 h-screen">
            <div class="px-2">
              <SafariMockup class="w-full aspect-[1203/753]" url="student-competency.com">
                <div class="absolute inset-0 overflow-hidden">
                  <CompareSlider
                    :first-image="langZhImg"
                    :second-image="langEnImg"
                    :first-image-alt="t('app.home.compare.zh')"
                    :second-image-alt="t('app.home.compare.en')"
                    first-content-class="object-cover object-center scale-[1.02]"
                    second-content-class="object-cover object-center scale-[1.02]"
                    class="w-full h-full"
                    slide-mode="hover"
                    :autoplay="true"
                    :autoplay-duration="5000"
                  />
                </div>
              </SafariMockup>
            </div>
            <div class="px-2">
              <TextScrollReveal
                :text="t('app.home.compareDesc.zhEn') as string"
                :sticky="false"
                progress-container-id="scene-zhen"
                :reveal-portion="0.5"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    

    <!-- 项目结构（分为 docs/frontend/backend 三个容器，液态玻璃） -->
    <section id="structure" class="mt-28 scroll-mt-24">
      <div id="scene-structure" class="relative h-[260vh]">
        <div class="sticky top-[14vh]">
      <div class="mb-8">
            <TextScrollReveal :text="t('app.home.structureDesc') as string" :sticky="false" progress-container-id="scene-structure" :reveal-portion="0.5" />
      </div>
      <div class="grid md:grid-cols-3 gap-8">
        <!-- docs 容器 -->
        <LiquidGlass
          :radius="16"
          :frost="0.06"
          :tint="true"
          :tint-from="'var(--color-info)'"
          :tint-to="'var(--color-accent)'"
          :container-class="'relative rounded-2xl w-[92%] mx-auto overflow-hidden'"
        >
          <div class="relative h-[700px] pt-5 px-5 pb-3">
            <div
              class="absolute inset-0 rounded-2xl pointer-events-none"
              style="background: linear-gradient(135deg, var(--color-info), color-mix(in oklch, var(--color-accent) 72%, transparent)); opacity: 0.16;"
            ></div>
            <Tree :initial-expanded-items="expandedDocs">
              <RenderElements :elements="docsNode ? [docsNode] : []" prefix="root" />
            </Tree>
          </div>
        </LiquidGlass>

        <!-- frontend 容器 -->
        <LiquidGlass
          :radius="16"
          :frost="0.06"
          :tint="true"
          :tint-from="'var(--color-primary)'"
          :tint-to="'var(--color-accent)'"
          :container-class="'relative rounded-2xl w-[92%] mx-auto overflow-hidden'"
        >
          <div class="relative h-[700px] pt-5 px-5 pb-3">
            <div
              class="absolute inset-0 rounded-2xl pointer-events-none"
              style="background: linear-gradient(135deg, var(--color-primary), color-mix(in oklch, var(--color-accent) 68%, transparent)); opacity: 0.15;"
            ></div>
            <Tree :initial-expanded-items="expandedFrontend">
              <RenderElements :elements="frontendNode ? [frontendNode] : []" prefix="root" />
            </Tree>
          </div>
        </LiquidGlass>

        <!-- backend 容器 -->
        <LiquidGlass
          :radius="16"
          :frost="0.06"
          :tint="true"
          :tint-from="'var(--color-success)'"
          :tint-to="'var(--color-secondary)'"
          :container-class="'relative rounded-2xl w-[92%] mx-auto overflow-hidden'"
        >
          <div class="relative h-[700px] pt-5 px-5 pb-3">
            <div
              class="absolute inset-0 rounded-2xl pointer-events-none"
              style="background: linear-gradient(135deg, var(--color-success), color-mix(in oklch, var(--color-secondary) 70%, transparent)); opacity: 0.14;"
            ></div>
            <Tree :initial-expanded-items="expandedBackend">
              <RenderElements :elements="backendNode ? [backendNode] : []" prefix="root" />
            </Tree>
          </div>
        </LiquidGlass>
          </div>
        </div>
      </div>
    </section>

    <!-- 走马灯（Marquee） -->
    <section id="help" class="mt-20 scroll-mt-24">
      <div id="scene-help" class="relative h-[260vh]">
        <div class="sticky top-[8vh]">
          <ContainerScroll>
            <template #title>
              <TextScrollReveal :text="t('app.home.helpDesc') as string" :sticky="false" progress-container-id="scene-help" :reveal-portion="0.5" />
            </template>
            <template #card>
              <img :src="helpImageSrc" alt="Help Overview" class="w-full h-full object-cover rounded-xl" @error="onHelpImgError" />
            </template>
          </ContainerScroll>
        </div>
      </div>
    </section>

    <!-- 学生反馈与系统成效展示（Tracing Beam 区块，占位） -->
    <section id="feedback" class="mt-20 scroll-mt-24">
      <div id="scene-feedback" class="relative h-[200vh]">
        <div class="sticky top-[8vh]">
          <div class="mb-4">
            <TextScrollReveal :text="t('app.home.feedbackDesc') as string" :sticky="false" progress-container-id="scene-feedback" :reveal-portion="0.35" />
          </div>
          <div class="w-full items-center justify-center px-4 md:px-8">
            <TracingBeam progress-container-id="scene-feedback" class="px-6">
              <div class="relative mx-auto max-w-2xl pt-4 antialiased">
                <div v-for="(item, index) in feedbackItems" :key="`fb-${index}`" class="mb-6">
                  <span class="ui-chip mb-2 inline-block text-base md:text-lg" :class="[item.variant, 'is-active']">{{ item.badge }}</span>
                  <div class="prose-base dark:prose-invert md:prose-lg prose-p:my-2 md:prose-p:my-3 prose-li:my-1 prose-p:leading-relaxed">
        <div>
                      <p v-for="(paragraph, idx) in item.description" :key="`fbp-${idx}`" v-html="paragraph"></p>
                    </div>
                  </div>
                </div>
                <!-- 备注：单独一项，淡色显示 -->
                <div class="mb-6">
                  <p class="text-subtle opacity-60 text-sm leading-relaxed">{{ t('app.home.feedbackSections.remark') }}</p>
                </div>
              </div>
            </TracingBeam>
        </div>
        </div>
      </div>
    </section>

    <!-- Ready CTA 滚动文本 + 按钮（自动触发彩带） -->
    <section id="ready" class="mt-28 md:mt-36 scroll-mt-24">
      <div id="scene-ready" class="relative h-[180vh]">
        <div class="sticky top-[16vh]">
          <div class="flex flex-col items-center gap-6">
            <TextScrollReveal
              :text="t('app.cta.ready') as string"
              :sticky="false"
              progress-container-id="scene-ready"
              :reveal-portion="0.5"
            />
            <div class="flex items-center gap-4">
              <Button
                variant="primary"
                size="xl"
                class="px-7 min-w-[170px]"
                @click="goLogin"
              >
                <template #icon>
                  <SparklesIcon class="w-5 h-5" />
                </template>
                {{ t('app.home.cta.login') }}
              </Button>
              <Button
                variant="info"
                size="xl"
                class="px-7 min-w-[170px]"
                @click="goDocs"
              >
                <template #icon>
                  <BookOpenIcon class="w-5 h-5" />
                </template>
                {{ t('app.home.cta.docs') }}
              </Button>
            </div>

            <!-- 复用登录页走马灯（两行，评价卡片） -->
            <div class="mt-8 w-full max-w-5xl mx-auto px-4">
              <Marquee pauseOnHover class="[--duration:22s] [--gap:1.25rem]">
                <ReviewCard v-for="r in homeMarqueeRow1" :key="r.username" :img="r.img" :name="r.name" :username="r.username" :mbti="r.mbti" :body="r.body" :tint="r.tint" />
              </Marquee>
              <Marquee reverse pauseOnHover class="[--duration:24s] [--gap:1.25rem] mt-4">
                <ReviewCard v-for="r in homeMarqueeRow2" :key="r.username" :img="r.img" :name="r.name" :username="r.username" :mbti="r.mbti" :body="r.body" :tint="r.tint" />
              </Marquee>
            </div>
          </div>
        </div>
        <!-- 进入该区块下方时触发彩带 -->
        <div ref="confettiSentinel" class="absolute bottom-24 left-0 right-0 h-8"></div>
      </div>
    </section>

    <section id="marquee" class="mt-14 scroll-mt-24">
      <Marquee :items="marqueeItems" />
    </section>

    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useUIStore } from '@/stores/ui'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import Button from '@/components/ui/Button.vue'
import { SparklesIcon, BookOpenIcon } from '@heroicons/vue/24/solid'
import LiquidLogo from '@/components/ui/LiquidLogo.vue'
// 按钮已移除：不再需要相关图标

// Inspira 适配组件
// 副标题改为 TextGenerateEffect
import TextGenerateEffect from '@/components/ui/inspira/TextGenerateEffect.vue'
import Meteors from '@/components/ui/inspira/Meteors.vue'
// 极光背景已移除
import AppleCardCarousel from '@/components/ui/inspira/AppleCardCarousel.vue'
import AppleCarouselItem from '@/components/ui/inspira/AppleCarouselItem.vue'
import AppleCard from '@/components/ui/inspira/AppleCard.vue'
import BendingGallery from '@/components/ui/inspira/BendingGallery.vue'
import LiquidGlass from '@/components/ui/LiquidGlass.vue'
import CompareSlider from '@/components/ui/inspira/CompareSlider.vue'
import TextScrollReveal from '@/components/ui/inspira/TextScrollReveal.vue'
import ContainerScroll from '@/components/ui/inspira/ContainerScroll.vue'
import TracingBeam from '@/components/ui/inspira/TracingBeam.vue'
import SafariMockup from '@/components/ui/inspira/SafariMockup.vue'
import { Tree, LetterPullup } from '@/components/ui/inspira'
import RenderElements from '@/components/ui/inspira/RenderElements.vue'
import { getProjectTree, type ProjectTreeNode } from '@/api/project.api'
import confetti from 'canvas-confetti'
import Marquee from '@/components/ui/inspira/Marquee.vue'
import ReviewCard from '@/components/ui/inspira/ReviewCard.vue'
import { DEFAULT_AVATARS } from '@/shared/utils/avatars'

const router = useRouter()
const { t } = useI18n()
const uiStore = useUIStore()
const { themeName } = storeToRefs(uiStore)
const isDarkMode = computed(() => {
  try { return (uiStore as any).isDarkMode || document.documentElement.classList.contains('dark') } catch { return document.documentElement.classList.contains('dark') }
})

// 语言对比图片（根据暗黑模式切换到同目录 -dark 资源）
const langZhImg = computed(() => isDarkMode.value ? '/home/compare/lang-zh-dark.png' : '/home/compare/lang-zh.png')
const langEnImg = computed(() => isDarkMode.value ? '/home/compare/lang-en-dark.png' : '/home/compare/lang-en.png')

const carouselItems = [
  { titleKey: 'app.home.cards.assessment.title', descKey: 'app.home.cards.assessment.desc' },
  { titleKey: 'app.home.cards.courses.title', descKey: 'app.home.cards.courses.desc' },
  { titleKey: 'app.home.cards.ai.title', descKey: 'app.home.cards.ai.desc' },
  { titleKey: 'app.home.cards.analytics.title', descKey: 'app.home.cards.analytics.desc' },
  { titleKey: 'app.home.cards.community.title', descKey: 'app.home.cards.community.desc' },
  { titleKey: 'app.home.cards.chat.title', descKey: 'app.home.cards.chat.desc' },
]

// 帮助区图片资源路径（请将图片放置于 frontend/public/home/help/overview.png）
const helpImage = '/home/help/overview.png'
const helpImageDark = '/home/help/overview-dark.png'
const helpImageSrc = computed(() => (isDarkMode.value ? helpImageDark : helpImage))
function onHelpImgError(e: Event) {
  const target = e.target as HTMLImageElement
  // 回退到占位图
  target.src = '/brand/logo.png'
}

// Ready CTA 按钮行为
function goLogin() {
  router.push('/auth/login')
}
function goDocs() {
  const url = (import.meta as any).env.VITE_DOCS_URL || 'http://localhost:4174'
  try { window.location.href = url } catch { window.open(url, '_self') }
}
// 画廊图片路径固定，标题走 i18n
const galleryItems = computed(() => {
  // 读取主题，浅色用 1-6，深色用 7-12
  const dark = isDarkMode.value
  // 引用 themeName 以确保主题切换时触发依赖更新
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  themeName?.value
  const numbers = dark ? [7, 8, 9, 10, 11, 12] : [1, 2, 3, 4, 5, 6]
  const labels = [
    t('app.home.gallery.login'),
    t('app.home.gallery.analytics'),
    t('app.home.gallery.students'),
    t('app.home.gallery.aiReports'),
    t('app.home.gallery.assistant'),
    t('app.home.gallery.community')
  ]
  return numbers.map((n, idx) => ({ image: `/home/gallery/${n}.png`, text: labels[idx] }))
})

const featureCards = [
  { titleKey: 'app.home.cards.assessment.title', descKey: 'app.home.cards.assessment.desc' },
  { titleKey: 'app.home.cards.courses.title', descKey: 'app.home.cards.courses.desc' },
  { titleKey: 'app.home.cards.ai.title', descKey: 'app.home.cards.ai.desc' },
  { titleKey: 'app.home.cards.analytics.title', descKey: 'app.home.cards.analytics.desc' },
  { titleKey: 'app.home.cards.community.title', descKey: 'app.home.cards.community.desc' },
  { titleKey: 'app.home.cards.chat.title', descKey: 'app.home.cards.chat.desc' },
]

const marqueeItems = [
  { titleKey: 'app.home.pages.studentDashboard.title', descKey: 'app.home.pages.studentDashboard.desc' },
  { titleKey: 'app.home.pages.assignments.title', descKey: 'app.home.pages.assignments.desc' },
  { titleKey: 'app.home.pages.courses.title', descKey: 'app.home.pages.courses.desc' },
  { titleKey: 'app.home.pages.analysis.title', descKey: 'app.home.pages.analysis.desc' },
  { titleKey: 'app.home.pages.community.title', descKey: 'app.home.pages.community.desc' },
  { titleKey: 'app.home.pages.notifications.title', descKey: 'app.home.pages.notifications.desc' },
]
// Apple 卡片数据（图片请放置至 frontend/public/home/apps/1-6.png）
const appCards = computed(() => [
  {
    src: '/home/apps/1.png',
    title: t('app.home.cards.assessment.title'),
    category: t('app.home.cards.category'),
    desc: t('app.home.cards.assessment.desc'),
    points: [t('app.home.cards.assessment.p1'), t('app.home.cards.assessment.p2'), t('app.home.cards.assessment.p3')]
  },
  {
    src: '/home/apps/2.png',
    title: t('app.home.cards.courses.title'),
    category: t('app.home.cards.category'),
    desc: t('app.home.cards.courses.desc'),
    points: [t('app.home.cards.courses.p1'), t('app.home.cards.courses.p2'), t('app.home.cards.courses.p3')]
  },
  {
    src: '/home/apps/3.png',
    title: t('app.home.cards.ai.title'),
    category: t('app.home.cards.category'),
    desc: t('app.home.cards.ai.desc'),
    points: [t('app.home.cards.ai.p1'), t('app.home.cards.ai.p2'), t('app.home.cards.ai.p3')]
  },
  {
    src: '/home/apps/5.png',
    title: t('app.home.cards.community.title'),
    category: t('app.home.cards.category'),
    desc: t('app.home.cards.community.desc'),
    points: [t('app.home.cards.community.p1'), t('app.home.cards.community.p2'), t('app.home.cards.community.p3')]
  },
  {
    src: '/home/apps/6.png',
    title: t('app.home.cards.chat.title'),
    category: t('app.home.cards.category'),
    desc: t('app.home.cards.chat.desc'),
    points: [t('app.home.cards.chat.p1'), t('app.home.cards.chat.p2'), t('app.home.cards.chat.p3')]
  }
])


const tree = ref<ProjectTreeNode[]>([])
// 拆分三个根节点
const docsNode = computed(() => tree.value.find(n => n.name === 'docs'))
const frontendNode = computed(() => tree.value.find(n => n.name === 'frontend'))
const backendNode = computed(() => tree.value.find(n => n.name === 'backend'))
// 默认展开各自根
const expandedDocs = computed(() => docsNode.value ? [ 'root/docs' ] : [])
const expandedFrontend = computed(() => frontendNode.value ? [ 'root/frontend' ] : [])
const expandedBackend = computed(() => backendNode.value ? [ 'root/backend' ] : [])



onMounted(async () => {
  try {
    tree.value = await getProjectTree({ depth: 0 })
    // 容器内各自 Tree 控制展开
  } catch (e) {
    // 失败则回退到精简静态
    tree.value = [
      { name: 'backend', directory: true, children: [ { name: 'src', directory: true }, { name: 'pom.xml', directory: false } ] },
      { name: 'frontend', directory: true, children: [ { name: 'src', directory: true }, { name: 'package.json', directory: false } ] },
      { name: 'docs', directory: true, children: [ { name: 'README.md', directory: false } ] },
    ]
  }
  // 观察滚动触发彩带
  try {
    if (confettiObserver) confettiObserver.disconnect()
    confettiObserver = new IntersectionObserver((entries) => {
      const entry = entries[0]
      if (!entry) return
      // 进入可视区域：触发（若尚未触发）
      if (entry.isIntersecting && !confettiFired.value) {
        confettiFired.value = true
        triggerFireworks()
      }
      // 离开可视区域：复位，允许再次触发
      if (!entry.isIntersecting) {
        confettiFired.value = false
      }
    }, { threshold: 0.35 })
    const el = confettiSentinel.value
    if (el) confettiObserver.observe(el)
  } catch {}
})

onUnmounted(() => {
  try { if (confettiObserver) confettiObserver.disconnect() } catch {}
})

// 彩带：左右两侧喷射的烟花效果
const confettiSentinel = ref<HTMLElement | null>(null)
const confettiFired = ref(false)
let confettiObserver: IntersectionObserver | null = null

function randomInRange(min: number, max: number) {
  return Math.random() * (max - min) + min
}

// 走马灯（复用登录页的评价卡）
const avatars = (DEFAULT_AVATARS || []) as string[]
const baseReviews = [
  { name: '王同学', username: '@wang', mbti: 'INTJ', tint: 'primary', bodyKey: 'layout.auth.reviews.r1' },
  { name: 'Li', username: '@li', mbti: 'ENFP', tint: 'info', bodyKey: 'layout.auth.reviews.r2' },
  { name: 'Zhang', username: '@zhang', mbti: 'ISTP', tint: 'success', bodyKey: 'layout.auth.reviews.r3' },
  { name: 'Chen', username: '@chen', mbti: 'ENTJ', tint: 'warning', bodyKey: 'layout.auth.reviews.r4' },
  { name: 'Liu', username: '@liu', mbti: 'INFJ', tint: 'secondary', bodyKey: 'layout.auth.reviews.r5' },
  { name: 'Zhao', username: '@zhao', mbti: 'ISTJ', tint: 'accent', bodyKey: 'layout.auth.reviews.r6' }
]
const homeMarqueeAll = computed(() => baseReviews.map((r, idx) => ({
  ...r,
  img: avatars.length ? avatars[idx % avatars.length] : '/brand/logo.png',
  body: t(r.bodyKey as any) as string
})))
const homeMarqueeRow1 = computed(() => homeMarqueeAll.value.slice(0, Math.ceil(homeMarqueeAll.value.length / 2)))
const homeMarqueeRow2 = computed(() => homeMarqueeAll.value.slice(Math.ceil(homeMarqueeAll.value.length / 2)))

// Feedback items（根据 i18n 组装为示例样式）
const feedbackItems = computed(() => {
  // 为每个分点分配不同颜色变体：primary/info/success/secondary
  const variants = ['pill-variant-primary', 'pill-variant-info', 'pill-variant-warning', 'pill-variant-secondary']
  const items = [
    {
      badge: t('app.home.feedbackSections.s1.badge') as string,
      title: t('app.home.feedbackSections.s1.title') as string,
      description: [
        t('app.home.feedbackSections.s1.p1') as string,
        `“${t('app.home.feedbackSections.s1.q1') as string}”`,
        `“${t('app.home.feedbackSections.s1.q2') as string}”`
      ]
    },
    {
      badge: t('app.home.feedbackSections.s2.badge') as string,
      title: t('app.home.feedbackSections.s2.title') as string,
      description: [
        t('app.home.feedbackSections.s2.p1') as string,
        t('app.home.feedbackSections.s2.p2') as string,
        t('app.home.feedbackSections.s2.p3') as string,
        t('app.home.feedbackSections.s2.p4') as string
      ]
    },
    {
      badge: t('app.home.feedbackSections.s3.badge') as string,
      title: t('app.home.feedbackSections.s3.title') as string,
      description: [
        t('app.home.feedbackSections.s3.p1') as string,
        t('app.home.feedbackSections.s3.p2') as string,
        t('app.home.feedbackSections.s3.p3') as string,
        t('app.home.feedbackSections.s3.p4') as string
      ]
    },
    {
      badge: t('app.home.feedbackSections.s5.badge') as string,
      title: t('app.home.feedbackSections.s5.title') as string,
      description: [
        `“${t('app.home.feedbackSections.s5.q1') as string}”`,
        t('app.home.feedbackSections.s5.p1') as string
      ]
    }
  ]
  return items.map((it, idx) => ({ ...it, variant: variants[idx % variants.length] }))
})

function triggerFireworks() {
  const duration = 4000
  const animationEnd = Date.now() + duration
  const defaults = { startVelocity: 30, spread: 360, ticks: 60, zIndex: 1000, useWorker: true }
  const interval = window.setInterval(() => {
    const timeLeft = animationEnd - Date.now()
    if (timeLeft <= 0) {
      clearInterval(interval)
      return
    }
    const particleCount = Math.max(20, 50 * (timeLeft / duration))
    confetti({
      ...defaults,
      particleCount: particleCount as number,
      origin: { x: randomInRange(0.1, 0.3), y: Math.random() - 0.2 }
    })
    confetti({
      ...defaults,
      particleCount: particleCount as number,
      origin: { x: randomInRange(0.7, 0.9), y: Math.random() - 0.2 }
    })
  }, 250)
}
</script>

<style scoped>
</style>


