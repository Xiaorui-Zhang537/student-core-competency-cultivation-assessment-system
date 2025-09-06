package com.noncore.assessment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMapper {
    List<Map<String, Object>> listConversations(@Param("userId") Long userId,
                                                @Param("pinned") Boolean pinned,
                                                @Param("archived") Boolean archived,
                                                @Param("offset") Integer offset,
                                                @Param("size") Integer size);

    long countConversations(@Param("userId") Long userId,
                            @Param("pinned") Boolean pinned,
                            @Param("archived") Boolean archived);

    int markConversationRead(@Param("userId") Long userId,
                             @Param("conversationId") Long conversationId);

    int recalcUnread(@Param("userId") Long userId,
                     @Param("conversationId") Long conversationId);
}


