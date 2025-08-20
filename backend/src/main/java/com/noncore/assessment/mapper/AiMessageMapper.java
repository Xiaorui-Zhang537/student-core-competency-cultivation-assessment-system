package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiMessageMapper {
    int insert(AiMessage message);
    List<AiMessage> listByConversation(@Param("conversationId") Long conversationId,
                                       @Param("offset") int offset,
                                       @Param("size") int size);
    long countByConversation(@Param("conversationId") Long conversationId);
    int deleteOldestExceeding(@Param("conversationId") Long conversationId,
                              @Param("keep") int keep);
    int deleteByConversation(@Param("conversationId") Long conversationId);
}


