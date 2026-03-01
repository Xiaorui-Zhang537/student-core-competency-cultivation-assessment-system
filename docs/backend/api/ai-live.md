---
title: AI 实时语音（Gemini Live）API
description: WebSocket 实时语音通道（浏览器 <-> 后端 <-> Gemini Live）
outline: [2, 3]
---

# AI 实时语音（Gemini Live）API

> 以 Swagger 与后端实现为准：`http://localhost:8080/api/swagger-ui.html`  
> 说明：实时语音使用 WebSocket（双工），由后端代理连接 Gemini Live API，前端不直连 Gemini（避免暴露密钥）。

本项目后端 `context-path=/api`，因此 WS 完整路径为 `/api/ai/live/ws`。

## 1. 用途与限制

用途：

- 浏览器实时采集麦克风音频，通过后端 WS 转发到 Gemini Live
- 实时返回：用户输入转写、AI 文本增量、AI 音频分片

限制：

- Gemini Live 的 `responseModalities` 单次只能选择 `TEXT` 或 `AUDIO`，不能同时 `TEXT+AUDIO`。
- 本项目的 `mode=both` 实现方式是：选择 `AUDIO` 输出，并开启 `outputAudioTranscription`（得到音频 + 文字转写）。
- Live 通道本身不自动“落库”，如需保存练习回合，请走“口语训练回合入库”接口（见本文第 4 节与 [ai-voice-practice.md](./ai-voice-practice)）。

## 2. WebSocket：`/api/ai/live/ws`

### 2.1 鉴权

后端支持 3 种方式传 token（优先级从高到低）：

- Query：`/api/ai/live/ws?token=<access_jwt>`
- Header：`Authorization: Bearer <access_jwt>`
- Header：`X-Auth-Token: <access_jwt>`

要求：必须是合法的 access token。

### 2.2 客户端消息（Client -> Server）

#### start

用于启动一段 Live 会话，并触发后端连接 Gemini Live + 发送 setup。

```json
{
  "type": "start",
  "model": "gemini-2.5-flash-native-audio-preview-12-2025",
  "mode": "text|audio|both",
  "locale": "en-US",
  "scenario": "面试自我介绍"
}
```

说明：

- `model` 可省略。后端会按 `mode` 选择默认模型：
  - `mode=text` -> `gemini-live-2.5-flash-preview`
  - `mode=audio|both` -> `gemini-2.5-flash-native-audio-preview-12-2025`
- 仅支持 Gemini 模型：`google/*` 或 `gemini-*` 或 `models/*`。非 Gemini 会返回 `INVALID_MODEL`。

#### audio_chunk

用于实时推送音频分片（PCM16 base64）。

```json
{
  "type": "audio_chunk",
  "seq": 1738500000000,
  "pcm16Base64": "BASE64_PCM16LE",
  "sampleRate": 16000
}
```

说明：

- PCM 要求：16-bit little-endian PCM（单声道）。
- `sampleRate` 可选，缺省视为 16000。

#### activity_start

开始本轮说话（按回合交互）。

```json
{ "type": "activity_start" }
```

#### activity_end

结束本轮说话，并触发模型开始生成回复（按回合交互）。

```json
{ "type": "activity_end" }
```

#### stop

停止会话（关闭后端到 Gemini 的 WS）。

```json
{ "type": "stop" }
```

#### ping（可选）

```json
{ "type": "ping", "ts": 1738500000000 }
```

### 2.3 服务端消息（Server -> Client）

#### ready（握手成功）

```json
{ "type": "ready", "userId": 1, "role": "STUDENT" }
```

#### started / stopped / pong（本地 ACK）

```json
{ "type": "started", "mode": "both", "model": "gemini-2.5-flash-native-audio-preview-12-2025" }
```

```json
{ "type": "stopped" }
```

```json
{ "type": "pong", "ts": 1738500000000 }
```

#### session_ready（Gemini setup 完成）

```json
{ "type": "session_ready" }
```

说明：后端在 setup 中禁用了自动 VAD（`automaticActivityDetection.disabled=true`），因此推荐客户端在 `session_ready` 之后再发送 `activity_start/audio_chunk/activity_end`。

#### transcript（用户输入转写）

```json
{ "type": "transcript", "isFinal": true, "text": "..." }
```

#### assistant_transcript（AI 音频的文字转写）

```json
{ "type": "assistant_transcript", "isFinal": true, "text": "..." }
```

#### assistant_text（AI 文本增量）

```json
{ "type": "assistant_text", "delta": "...", "isFinal": false }
```

#### assistant_audio（AI 音频分片）

```json
{
  "type": "assistant_audio",
  "pcm16Base64": "BASE64_PCM16LE",
  "mimeType": "audio/pcm;rate=24000"
}
```

#### turn_complete / generation_complete / interrupted

```json
{ "type": "turn_complete" }
```

```json
{ "type": "generation_complete" }
```

```json
{ "type": "interrupted" }
```

#### warn / error / closed

```json
{ "type": "warn", "code": "THROTTLED", "message": "发送过快，已触发节流，请放慢语速或稍后重试" }
```

```json
{ "type": "error", "code": "CONNECT_FAILED", "message": "Google Gemini API Key 未配置" }
```

```json
{ "type": "closed", "code": 1000, "reason": "client_closed" }
```

## 3. Prompt

后端在 Live setup 中注入 `systemInstruction`，并把 `locale/scenario` 追加到提示词中（用于口语训练引导）。

## 4. 回合入库：`POST /api/ai/voice/turns`

用途：把一次口语训练回合（转写 + 音频附件）写入“口语训练”独立记录（不写入 AI 聊天会话）。

创建 `sessionId` 请先调用语音会话接口（见 [ai-voice-practice.md](./ai-voice-practice) 的 `POST /ai/voice/sessions`）。

请求（示例）：

```json
{
  "sessionId": 123,
  "model": "google/gemini-2.5-pro",
  "userTranscript": "Hello, my name is ...",
  "assistantText": "Nice to meet you. ...",
  "userAudioFileId": 10001,
  "assistantAudioFileId": 10002,
  "scenario": "面试自我介绍",
  "locale": "en-US"
}
```

说明：兼容历史字段名 `conversationId`，会被当作 `sessionId` 使用。

响应（示例）：

```json
{
  "code": 200,
  "data": {
    "sessionId": 123,
    "conversationId": 123,
    "turnId": 20001
  }
}
```

## 5. 音频文件上传与回放

- 上传：`POST /api/files/upload`（先拿到 `fileId`，再填 `userAudioFileId/assistantAudioFileId`）
- 回放：可使用 `GET /api/files/{fileId}/stream?token=...`（支持 Range）
