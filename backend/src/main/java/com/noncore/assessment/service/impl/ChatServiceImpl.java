package com.noncore.assessment.service.impl;

import com.noncore.assessment.mapper.ChatMapper;
import com.noncore.assessment.service.ChatService;
import com.noncore.assessment.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMapper chatMapper;

    public ChatServiceImpl(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    @Override
    public PageResult<Map<String, Object>> listMyConversations(Long userId, Boolean pinned, Boolean archived, Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 20 : size;
        int offset = (p - 1) * s;
        List<Map<String, Object>> items = chatMapper.listConversations(userId, pinned, archived, offset, s);
        long total = chatMapper.countConversations(userId, pinned, archived);
        return PageResult.of(items, p, s, total, (int) Math.ceil(total / (double) s));
    }

    @Override
    public Map<String, Object> markConversationRead(Long userId, Long conversationId) {
        // 将该会话中“我作为接收者”的未读通知标记已读，并重算未读
        int marked = chatMapper.markConversationRead(userId, conversationId);
        int unread = chatMapper.recalcUnread(userId, conversationId);
        Map<String, Object> res = new HashMap<>();
        res.put("marked", marked);
        res.put("unread", unread);
        return res;
    }

    @Override
    public int setArchived(Long userId, Long conversationId, boolean archived) {
        return chatMapper.setArchived(conversationId, userId, archived);
    }

    @Override
    public Integer getUnreadTotal(Long userId) {
        try {
            Integer v = chatMapper.getUnreadTotal(userId);
            return v == null ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }
}


