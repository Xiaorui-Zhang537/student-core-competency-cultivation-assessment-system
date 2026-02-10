# AI 助理：模型说明、限额与长期记忆

本文面向学生与教师解释：可选模型能力差异、学生端使用限额、附件支持范围，以及“长期记忆”如何生效与推荐写法。

## 1. 可用模型与推荐场景

### 1.1 Gemini 2.5 Pro（`google/gemini-2.5-pro`）
- **优势**：复杂问题推理、长文本总结、图像理解更强。
- **附件**：支持 **图片 + 文档**（pdf/doc/docx/txt）。
  - 文档会先在后端抽取为文本再发送给模型。
  - 图片以安全的内联方式发送（不依赖外网 URL）。
- **适合**：看图解释、图表/截图提问、带材料的写作/阅读任务、综合分析。
 - **参考**：Gemini API `generateContent` 支持 `inlineData` 图像输入与 `usageMetadata`（token 统计）等元信息，详见 Google 官方文档（`https://ai.google.dev/gemini-api/docs`）。

### 1.2 GLM-4.6（`glm-4.6`）
- **优势**：文本对话质量高，适合日常问答、文本分析。
- **附件**：**不支持**图片/文件输入（文本模型）。
- **适合**：纯文字问题、概念解释、写作润色、学习方法建议。
 - **参考**：智谱开放平台文档（`https://docs.bigmodel.cn/`）对 GLM 文本模型与视觉模型（如 `glm-4.6v`）做了区分。

### 1.3 GLM-4.5 Air（`glm-4.5-air`）
- **优势**：响应更快，更轻量。
- **附件**：**不支持**图片/文件输入（文本模型）。
- **适合**：快速问答、短文本改写、要点提炼。

## 2. 学生端使用限额（每周）

> 计次口径：仅当后端成功写入一条 `assistant` 回复消息时才计 1 次；上游报错/网络异常/空返回不会写入 `assistant`，因此不计次。

- **Gemini**：每周最多 **10 次**
- **GLM**：每周最多 **20 次**

## 3. 长期记忆（Memory）

### 3.1 记忆是什么
- 你可以把“长期稳定的偏好/背景/约束”写进长期记忆，例如：
  - 希望输出语言（中文/英文）、语气（简洁/鼓励/严格）
  - 希望答案格式（先结论后步骤/用表格/给示例）
  - 当前学习目标与薄弱点（例如写作结构、口语表达）
  - 需要避免的内容（例如不要使用太复杂术语）
### 3.2 记忆如何生效
- 后端在每次聊天生成前读取 `/api/ai/memory` 对应内容，并把它作为 **system prompt** 注入到模型请求里。
- 若你的当前问题与记忆冲突，系统会优先满足“当前问题”。
- 记忆内容有长度上限，过长会被截断（避免挤占上下文）。
### 3.3 推荐记忆模板（复制即用）

模板 A（学习目标 + 输出风格）
```text
我希望你用中文回答，语气简洁、分点。
我的学习目标是：__________。
当我问学习问题时，请按“结论 -> 解释 -> 例子 -> 练习题”输出。
```

模板 B（写作/表达导向）
```text
我在训练写作与表达能力。
请优先指出结构问题，再给出可执行的修改建议；必要时给一个简短示例。
```

模板 C（配合附件）
```text
当我上传图片/文档时，请先用 3~5 条要点总结材料内容，再回答我的问题。
如果材料与问题无关，请直接说明并建议我如何补充信息。
```

## 4. 数据模型（开发者）

### 4.1 会话（Conversation）
- 字段（示例）：`id/userId/title/model/provider/pinned/archived/createdAt`

### 4.2 消息（Message）
- 字段（示例）：`id/conversationId/role/content/attachments/createdAt`
- 角色：`user/assistant/system`

### 4.3 记忆（Memory）
- 字段（示例）：`userId/enabled/content/updatedAt`

## 5. 相关代码位置（开发者）
- 控制器：`backend/src/main/java/com/noncore/assessment/controller/AiController.java`
- 服务：`backend/src/main/java/com/noncore/assessment/service/impl/AiServiceImpl.java`、`AiConversationService`、`AiMemoryService`
- Gemini 客户端：`backend/src/main/java/com/noncore/assessment/service/llm/GeminiClient.java`
- 前端：`frontend/src/api/ai.api.ts`、`frontend/src/stores/ai.ts`、`frontend/src/features/shared/views/AIAssistantView.vue`
