---
title: "`voicePractice.api.ts`"
description: AI 口语训练（语音会话/回合/复听上报）
outline: [2, 3]
---

# `voicePractice.api.ts`（AI 口语训练）

对应后端 `/api/ai/voice/**` 与 `POST /api/ai/voice/turns`。

## 1. 数据结构（摘要）

- `VoiceSession`：会话元信息（`title/model/mode/locale/scenario/pinned/...`）
- `VoiceTurn`：一次回合（转写文本 + 音频 fileId 引用）
- `VoiceReplayReportRequest`：复听/回放增量上报（秒）

## 2. 方法清单

- `createSession(data)`
- `listSessions(params?)`
- `getSession(sessionId)`
- `updateSession(sessionId, data)`
- `deleteSession(sessionId)`
- `listTurns(sessionId, params?)`
- `saveTurn(data)`：写入一次回合（转写 + 音频附件）
- `reportReplay(data)`：上报复听/回放时长（行为证据事实记录）

## 3. 最小调用示例

```ts
import { voicePracticeApi } from '@/api/voicePractice.api'

// 创建会话
const created: any = await voicePracticeApi.createSession({ scenario: 'Interview', locale: 'zh-CN' })
const sessionId = created.sessionId ?? created.id

// 写入回合（音频需先上传得到 fileId）
await voicePracticeApi.saveTurn({
  sessionId,
  userTranscript: 'Hello, my name is...',
  assistantText: 'Nice to meet you...',
  userAudioFileId: 1001,
  assistantAudioFileId: 1002,
  scenario: 'Interview',
  locale: 'zh-CN'
})

// 复听上报（只记录事实，不代表评价/分数）
await voicePracticeApi.reportReplay({
  sessionId,
  audioRole: 'user',
  deltaSeconds: 12,
  fileId: 1001
})
```

## 4. 注意事项

- `reportReplay` 是行为证据事实记录：不算分、不输出评价。
- `deltaSeconds` 为“增量秒数”，建议前端做节流/合并上报，避免高频请求。

