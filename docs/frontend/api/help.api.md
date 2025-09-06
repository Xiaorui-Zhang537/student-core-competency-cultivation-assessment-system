# 前端 Help API 使用说明（中文）

从 `src/api/help.api.ts` 引入：

```ts
import { helpApi } from '@/api/help.api'
```

可用方法：
- `helpApi.listCategories()` → 返回 `HelpCategory[]`
- `helpApi.listArticles({ q?, categoryId?, tag?, sort? })` → 返回 `HelpArticle[]`
- `helpApi.getArticle(slug, inc=true)` → 返回 `HelpArticle`
- `helpApi.submitArticleFeedback(articleId, { helpful?, content? })` → 返回 void（统一响应包装）
- `helpApi.createTicket(title, description)` → 返回 `HelpTicket`
- `helpApi.myTickets()` → 返回 `HelpTicket[]`

类型定义位于：`src/types/help.ts`。
