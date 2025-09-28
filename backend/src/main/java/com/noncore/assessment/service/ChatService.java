package com.noncore.assessment.service;

import com.noncore.assessment.util.PageResult;

import java.util.Map;

public interface ChatService {
    PageResult<Map<String, Object>> listMyConversations(Long userId, Boolean pinned, Boolean archived, Integer page, Integer size);
    Map<String, Object> markConversationRead(Long userId, Long conversationId);
    int setArchived(Long userId, Long conversationId, boolean archived);
    Integer getUnreadTotal(Long userId);
}


