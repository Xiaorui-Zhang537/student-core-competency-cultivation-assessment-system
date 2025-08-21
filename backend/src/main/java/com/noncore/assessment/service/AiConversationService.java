package com.noncore.assessment.service;

import com.noncore.assessment.entity.AiConversation;
import com.noncore.assessment.entity.AiMessage;
import com.noncore.assessment.util.PageResult;

import java.util.List;

public interface AiConversationService {
    AiConversation createConversation(Long userId, String title, String model, String provider);
    PageResult<AiConversation> listConversations(Long userId, String q, Boolean pinned, Boolean archived, Integer page, Integer size);
    AiConversation getConversation(Long userId, Long conversationId);
    void updateConversation(Long userId, Long conversationId, String title, Boolean pinned, Boolean archived);
    void deleteConversation(Long userId, Long conversationId);

    PageResult<AiMessage> listMessages(Long userId, Long conversationId, Integer page, Integer size);
    AiMessage appendMessage(Long userId, Long conversationId, String role, String content, List<Long> attachments);
}


