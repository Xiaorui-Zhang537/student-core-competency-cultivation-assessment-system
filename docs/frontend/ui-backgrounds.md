### 背景选择与主题说明（前端）

功能：在学生端、教师端、登录首页的右上角新增“背景”按钮，打开弹窗可分别为明亮/暗黑模式选择背景。

- 明亮模式：无 / 极光（Aurora）/ 俄罗斯方块（Tetris）
- 暗黑模式：无 / Neural Background / 流星（Meteors）

实现要点：
- Store：`src/stores/ui.ts` 增加 `backgroundLight`、`backgroundDark`，本地持久化。
- 背景层：`src/components/ui/BackgroundLayer.vue` 根据明暗模式与 store 值渲染对应背景。
- 选择弹窗：`src/components/ui/BackgroundPickerModal.vue` 提供选项并即时更新 store。
- 布局接入：`StudentLayout.vue`、`TeacherLayout.vue`、`AuthLayout.vue`、`PublicLayout.vue` 顶部加入按钮并挂载 `BackgroundLayer`。

注意：背景仅为装饰层，指针事件穿透；主题切换仍使用原有按钮与逻辑。


