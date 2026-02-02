package com.noncore.assessment.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 口语训练回合入库请求。
 * <p>
 * 该接口用于把一次“用户发声 -> AI 回复”的回合持久化到 AI 会话（AiConversation/AiMessage）中，\n
 * 同时把音频文件（通过 /files/upload 上传）以附件形式绑定到消息上。\n
 *
 * @author System
 * @since 2026-02-02
 */
@Data
public class AiVoiceTurnRequest {

    /**
     * 语音会话ID（必须存在且归属当前用户）。
     *
     * 兼容：历史字段名 conversationId（旧实现曾把语音写入 AI 聊天会话）
     */
    @NotNull
    @JsonAlias({"conversationId"})
    private Long sessionId;

    /**
     * 兼容字段（已废弃）：旧前端会传 conversationId。
     * 新前端应使用 sessionId。
     */
    @Deprecated
    private Long conversationId;

    /**
     * 期望使用的模型（可选）。若提供，会更新会话的 model 字段（保持单会话单模型）。
     * 示例：google/gemini-2.5-pro
     */
    private String model;

    /**
     * 用户输入转写文本（建议提供；若为空则仍可入库音频附件，但不利于检索）。
     */
    private String userTranscript;

    /**
     * AI 回复的文字内容（可选；若为音频回复，建议同时传入输出转写以便回放/检索）。
     */
    private String assistantText;

    /**
     * 用户音频文件ID（通过文件上传接口获得）。
     */
    private Long userAudioFileId;

    /**
     * AI 音频文件ID（通过文件上传接口获得）。
     */
    private Long assistantAudioFileId;

    /**
     * 训练场景（可选，用于前端展示/检索；当前版本仅透传，不参与模型调用）。
     */
    private String scenario;

    /**
     * 语言偏好（可选，如 en-US / zh-CN）。
     */
    private String locale;
}

