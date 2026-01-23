# 错误码与空态展示规范（UI）

面向组件与页面的统一展示规范，确保一致、可预期、可本地化。

## 1. 显示策略总览
- **成功（code=200）**：正常渲染数据；空数据进入空态
- **4xx（校验/资源/权限）**：展示具体 message；403 不跳转，仅提示
- **401（未认证）**：清理会话并跳转登录
- **429（限流）**：提示稍后重试，带倒计时或退避说明
- **5xx/网络错误**：兜底错误提示 + “重试”按钮

## 2. 空态规范
- 必含元素：图标/插画（可选）、标题（简短）、说明（可选）、主行动（创建/刷新/返回）
- 列表空态：提供筛选/清除条件/新建入口
- 搜索空态：给出搜索建议

示例（伪代码）：
```vue
<Empty v-if="!loading && items.length===0" icon="i-ep-search">
  <template #title>{{ t('empty.noData') }}</template>
  <template #description>{{ t('empty.tryAdjustFilters') }}</template>
  <NButton @click="onCreate">{{ t('common.create') }}</NButton>
</Empty>
```

## 3. 错误展示模式
- 表单校验错误：逐字段提示 + 顶部汇总（可选）
- 列表/详情错误：全局通知或页面级提示条
- 上传错误：就近提示（文件项旁）

### 3.1 右上角通知弹窗（Toast）去重与关闭
- **去重规则**：同一通知按 `type + title + message` 生成 key，在 **3 秒窗口内只展示 1 条**（多入口重复触发也只会出现一次）。实现收口在 `frontend/src/stores/ui.ts` 的 `showNotification()`。\n+- **关闭规则**：每条通知卡片右上角都有“×”关闭按钮，点击仅移除该条（不影响其他通知）。UI 渲染在 `frontend/src/App.vue`。\n+- **建议**：业务层尽量只在一个层级弹错（页面/封装/全局择一），但即使重复调用，UIStore 会兜底去重，避免右上角出现多条同样报错。\n+
Axios 统一处理示例：
```ts
instance.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.message || 'Request failed'
    if (status === 401) redirectToLogin()
    else showToast(msg)
    return Promise.reject(err)
  }
)
```

## 4. 重试与退避
- 建议指数退避：1s → 2s → 5s → 10s（上限）
- 支持“手动重试”按钮；自动重试需明确用户感知

示例（SSE/网络）：
```ts
let delay = 1000
async function retry(fn: () => Promise<void>) {
  for (let i = 0; i < 5; i++) {
    try { await fn(); return } catch { await wait(delay); delay = Math.min(delay*2, 10000) }
  }
}
```

## 5. 国际化与可访问性
- 文案从 i18n 词典获取；不要硬编码
- 提示文本简短、指向性强；配合 ARIA 属性（如 `aria-live`）

## 6. 组件约定
- 列表组件暴露 `loading/error/retry` 属性或插槽
- 图表组件在空数据时展示空态占位，而非空白

## 7. 验收清单
- 异常路径均有反馈（401/403/404/409/429/5xx）
- 空态具备操作入口（创建/刷新/清筛）
- 文案可本地化；无硬编码中文
- 支持重试；重要交互具备兜底提示
