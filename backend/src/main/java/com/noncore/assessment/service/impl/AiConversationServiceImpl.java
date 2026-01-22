package com.noncore.assessment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.config.AiConfigProperties;
import com.noncore.assessment.entity.AiMessage;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AiConversationMapper;
import com.noncore.assessment.mapper.AiMessageMapper;
import com.noncore.assessment.service.AiConversationService;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.util.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final FileStorageService fileStorageService;
    private final com.noncore.assessment.config.AiConfigProperties aiConfigProperties;

    @Override
    public AiConversation createConversation(Long userId, String title, String model, String provider) {
        String normalizedModel = normalizeModel(model);
        String normalizedProvider = (provider == null || provider.isBlank()) ? aiConfigProperties.getDefaultProvider() : provider.trim();
        AiConversation c = AiConversation.builder()
                .userId(userId)
                .title(title == null || title.isBlank() ? "新对话" : title.trim())
                .model(normalizedModel)
                .provider(normalizedProvider)
                .pinned(false)
                .archived(false)
                .deleted(false)
                .lastMessageAt(null)
                .build();
        conversationMapper.insert(c);
        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AiConversation> listConversations(Long userId, String q, Boolean pinned, Boolean archived, Integer page, Integer size) {
        PageHelper.startPage(page == null ? 1 : page, size == null ? 50 : size);
        List<AiConversation> list = conversationMapper.listByUser(userId, q, pinned, archived);
        PageInfo<AiConversation> pi = new PageInfo<>(list);
        return PageResult.of(pi.getList(), pi.getPageNum(), pi.getPageSize(), pi.getTotal(), pi.getPages());
    }

    @Override
    @Transactional(readOnly = true)
    public AiConversation getConversation(Long userId, Long conversationId) {
        AiConversation c = conversationMapper.selectByIdAndUser(conversationId, userId);
        if (c == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "会话不存在");
        String normalized = normalizeModel(c.getModel());
        if (!normalized.equals(c.getModel())) {
            c.setModel(normalized);
            conversationMapper.update(c);
        }
        return c;
    }

    @Override
    public void updateConversationModel(Long userId, Long conversationId, String model) {
        AiConversation c = getConversation(userId, conversationId);
        String normalizedModel = normalizeModel(model);
        c.setModel(normalizedModel);
        if (normalizedModel != null && normalizedModel.startsWith("google/")) {
            c.setProvider(aiConfigProperties.getDefaultProvider());
        }
        conversationMapper.update(c);
    }

    @Override
    public void updateConversation(Long userId, Long conversationId, String title, Boolean pinned, Boolean archived) {
        AiConversation c = getConversation(userId, conversationId);
        if (title != null) c.setTitle(title.trim());
        if (pinned != null) c.setPinned(pinned);
        if (archived != null) c.setArchived(archived);
        conversationMapper.update(c);
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        // 级联清理附件
        try { fileStorageService.cleanupRelatedFiles("ai_chat", conversationId); } catch (Exception ignored) {}
        // 会话与消息外键已设 ON DELETE CASCADE，此处直接删除会话
        int r = conversationMapper.deleteByIdAndUser(conversationId, userId);
        if (r <= 0) throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除会话失败");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AiMessage> listMessages(Long userId, Long conversationId, Integer page, Integer size) {
        getConversation(userId, conversationId); // 所有权校验
        int sz = size == null ? 100 : Math.min(100, size);
        int pg = page == null ? 1 : page;
        int offset = (pg - 1) * sz;
        List<AiMessage> list = messageMapper.listByConversation(conversationId, offset, sz);
        long total = messageMapper.countByConversation(conversationId);
        int pages = (int) Math.ceil(total / (double) sz);
        return PageResult.of(list, pg, sz, total, pages);
    }

    @Override
    public AiMessage appendMessage(Long userId, Long conversationId, String role, String content, List<Long> attachments) {
        getConversation(userId, conversationId); // 所有权校验
        String attachmentsJson = null;
        if (attachments != null && !attachments.isEmpty()) {
            attachmentsJson = attachments.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",", "[", "]"));
        }
        AiMessage m = AiMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(role)
                .content(content)
                .attachments(attachmentsJson)
                .build();
        messageMapper.insert(m);
        conversationMapper.updateLastMessageAt(conversationId, LocalDateTime.now());
        // 裁剪为最新100条
        try { messageMapper.deleteOldestExceeding(conversationId, 100); } catch (Exception ignored) {}
        return m;
    }
    

    @Override
    public long countAssistantMessagesByModelSince(Long userId, String modelPrefix, LocalDateTime since) {
        return messageMapper.countAssistantMessagesByModelSince(userId, modelPrefix, since);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMessages(Long userId, Long conversationId) {
        if (userId == null || conversationId == null) return 0L;
        getConversation(userId, conversationId); // 所有权校验
        return messageMapper.countByConversation(conversationId);
    }

    @Override
    public String normalizeModel(String model) {
        String defaultModel = aiConfigProperties.getDeepseek().getModel();
        if (defaultModel == null || defaultModel.isBlank()) {
            defaultModel = "google/gemini-2.5-pro";
        }
        if (model == null || model.isBlank()) return defaultModel;

        String trimmed = model.trim();
        Map<String, String> aliases = new java.util.HashMap<>();
        aliases.put("z-ai/glm-4.5-air", "glm-4.5-air");
        aliases.put("z-ai/glm-4.5-air:free", "glm-4.5-air");
        aliases.put("gemini-3-pro", "google/gemini-2.5-pro");
        aliases.put("google/gemini-3-pro", "google/gemini-2.5-pro");
        aliases.put("gemini-2.5-pro", "google/gemini-2.5-pro");
        aliases.put("gemini-2.5-flash", "google/gemini-2.5-flash");
        aliases.put("gemini-2.5-flash-lite", "google/gemini-2.5-flash-lite");
        if (aliases.containsKey(trimmed)) {
            trimmed = aliases.get(trimmed);
        }
        if (trimmed.startsWith("gemini-")) {
            trimmed = "google/" + trimmed;
        }
        boolean allowedGoogle = trimmed.startsWith("google/");
        Set<String> allowed = Set.of(
                "glm-4.6",
                "glm-4.5-air",
                "glm-4.6v"
        );
        return allowedGoogle || allowed.contains(trimmed) ? trimmed : defaultModel;
    }
}


