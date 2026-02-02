# AI 实时语音（口语训练）API（Gemini Live）

> 以 Swagger 与后端实现为准：`http://localhost:8080/api/swagger-ui.html`  
> 说明：实时语音使用 **WebSocket**（双工），并通过后端代理连接 Gemini Live API。前端 **不得**直连 Gemini（避免暴露密钥）。

## 1. 入口与用途

- **入口页面**：AI 助理页右上角「语音练习」按钮
  - 学生端：`/student/assistant/voice`
  - 教师端：`/teacher/assistant/voice`

- **核心能力**：
  - 浏览器实时采集麦克风音频 → 后端 WS → Gemini Live
  - 实时返回：输入转写、AI 文本增量、AI 音频分片（可选）
  - 结束后：上传用户音频与 AI 音频，并写入会话消息用于回放

> 重要限制：Gemini Live 的 `responseModalities` 单次只能选择 **TEXT** 或 **AUDIO**，不能同时 TEXT+AUDIO。  
> 因此本文的 `mode=both（音频+文字）` 实现方式是：**AUDIO 输出 + 开启 `outputAudioTranscription`**（获得文字转写），从而达到“能听到音频 + 看到文字”的效果。

## 2. WebSocket：`/api/ai/live/ws`

### 2.1 鉴权

- WS 地址：`/api/ai/live/ws?token=JWT_ACCESS_TOKEN`
- 鉴权规则与通知 SSE 一致：必须是 **access token**，并通过后端校验。

### 2.2 客户端消息（Client → Server）

#### start

```json
{
  "type": "start",
  "conversationId": 123,
  "model": "google/gemini-2.5-pro",
  "mode": "text|audio|both",
  "locale": "en-US",
  "scenario": "面试自我介绍"
}
```

说明：
- `mode=text`：后端向 Gemini 设置 `responseModalities=["TEXT"]`
- `mode=audio`：后端向 Gemini 设置 `responseModalities=["AUDIO"]`
- `mode=both`：后端向 Gemini 设置 `responseModalities=["AUDIO"]` 并开启 `outputAudioTranscription`（返回音频 + 文字转写）

#### audio_chunk

```json
{
  "type": "audio_chunk",
  "seq": 1738500000000,
  "pcm16Base64": "BASE64_PCM16LE",
  "sampleRate": 16000
}
```

> PCM 要求：16-bit little-endian PCM。建议 16kHz 单声道。

#### activity_start（开始本轮，可选）

用于“按回合”的口语训练交互：标记用户开始说话。

```json
{ "type": "activity_start" }
```

#### activity_end（结束本轮，触发模型回复）

用于“按回合”的口语训练交互：标记用户结束说话，并触发模型开始生成回复。

```json
{ "type": "activity_end" }
```

#### stop

```json
{ "type": "stop" }
```

#### ping（可选）

```json
{ "type": "ping", "ts": 1738500000000 }
```

### 2.3 服务端消息（Server → Client）

#### ready（握手后立即返回）

```json
{ "type": "ready", "userId": 1, "role": "STUDENT" }
```

#### session_ready（Gemini setup 完成）

```json
{ "type": "session_ready" }
```

> 说明：后端已在 Gemini setup 中禁用自动 VAD（`realtimeInputConfig.automaticActivityDetection.disabled=true`），建议客户端在 `session_ready` 后再发送 `activity_start/audio_chunk/activity_end`。

#### transcript（输入音频转写）

```json
{ "type": "transcript", "isFinal": true, "text": "..." }
```

#### assistant_text（AI 文本增量）

```json
{ "type": "assistant_text", "delta": "..." }
```

#### assistant_audio（AI 音频分片，base64）

```json
{
  "type": "assistant_audio",
  "pcm16Base64": "BASE64_PCM16LE",
  "mimeType": "audio/pcm;rate=24000"
}
```

#### error / warn

```json
{ "type": "error", "code": "CONNECT_FAILED", "message": "..." }
```

```json
{ "type": "warn", "code": "THROTTLED", "message": "..." }
```

## 3. 回合入库：`POST /api/ai/voice/turns`

用于把一次口语训练回合写入 AI 会话消息中（`AiMessage.attachments` 保存音频文件ID）。

### 请求

```json
{
  "conversationId": 123,
  "model": "google/gemini-2.5-pro",
  "userTranscript": "Hello, my name is ...",
  "assistantText": "Nice to meet you. ...",
  "userAudioFileId": 10001,
  "assistantAudioFileId": 10002,
  "scenario": "面试自我介绍",
  "locale": "en-US"
}
```

### 响应

```json
{
  "code": 200,
  "data": {
    "conversationId": 123,
    "userMessageId": 20001,
    "assistantMessageId": 20002
  }
}
```

## 4. 音频文件上传与回放

- 上传：`POST /api/files/upload`（purpose=ai_voice, relatedId=conversationId）
- 回放：推荐使用 `GET /api/files/{fileId}/stream?token=...`（支持 Range，浏览器 `audio` 标签更稳定）

## 5. Prompt

- 口语训练系统 Prompt：`backend/src/main/resources/prompts/speaking_practice_system_prompt.v1.txt`
- Live 会话 setup 时会注入 systemInstruction（并附加 locale/scenario 提示）

