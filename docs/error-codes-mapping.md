# 错误码映射表（后端 code → HTTP → UI 动作）

规范化错误的表达与处理，让前后端与 UI 行为一致。

## 1. 基础映射
- 200 → 200 OK → 正常渲染或提示成功
- 400 → 400 Bad Request → 表单/参数错误提示；定位字段
- 401 → 401 Unauthorized → 清理会话并跳转登录
- 403 → 403 Forbidden → Toast 提示“无权限”，不跳转
- 404 → 404 Not Found → 空态/返回上一页
- 409 → 409 Conflict → 显示冲突原因（状态已变更/重复操作）
- 429 → 429 Too Many Requests → 退避重试提示
- 5xx → 500+ → 兜底错误提示 + 重试

## 2. 统一响应体
```json
{ "code": 401, "message": "Invalid token", "data": null }
```
前端以 `code` 为准，若无 `code` 则回退到 `HTTP status`。

## 3. UI 处理策略（伪代码）
```ts
function handleError(resp: { code?: number; http: number; message?: string }) {
  const c = resp.code ?? resp.http
  switch (c) {
    case 401: logoutAndRedirect(); break
    case 403: toast(t('error.forbidden')); break
    case 404: showEmpty(t('empty.noData')); break
    case 409: toast(resp.message ?? t('error.conflict')); break
    case 429: showRetryWithBackoff(); break
    default: toast(resp.message ?? t('error.server'))
  }
}
```

## 4. 领域特定映射示例
- 课程发布：`409`（已发布/非法迁移）→ 显示“状态冲突”并刷新详情
- 作业截止：`409`（已关闭）→ 禁用提交按钮
- 上传：`400`（类型/大小）→ 就近提示；`413` → 提示增大限制

## 5. 验收清单
- 所有异常路径都有明确 UI 行动
- 错误信息可本地化（对齐 i18n）
- 重试路径具备退避策略
