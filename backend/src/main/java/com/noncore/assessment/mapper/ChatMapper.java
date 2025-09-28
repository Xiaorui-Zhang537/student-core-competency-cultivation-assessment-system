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

    Long findConversationId(@Param("peerA") Long peerA,
                            @Param("peerB") Long peerB,
                            @Param("courseId") Long courseId);

    int insertConversation(@Param("peerA") Long peerA,
                           @Param("peerB") Long peerB,
                           @Param("courseId") Long courseId,
                           @Param("lastMessageId") Long lastMessageId,
                           @Param("lastMessageAt") java.time.LocalDateTime lastMessageAt);

    int updateConversationLast(@Param("conversationId") Long conversationId,
                               @Param("lastMessageId") Long lastMessageId,
                               @Param("lastMessageAt") java.time.LocalDateTime lastMessageAt);

    int ensureMember(@Param("conversationId") Long conversationId,
                     @Param("userId") Long userId);

    int incrementUnread(@Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

    int setArchived(@Param("conversationId") Long conversationId,
                    @Param("userId") Long userId,
                    @Param("archived") boolean archived);

    int resetUnread(@Param("conversationId") Long conversationId,
                    @Param("userId") Long userId);

    Integer getUnreadTotal(@Param("userId") Long userId);
}


