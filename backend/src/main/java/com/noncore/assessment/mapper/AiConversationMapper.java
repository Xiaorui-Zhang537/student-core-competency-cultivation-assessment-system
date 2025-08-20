package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AiConversationMapper {
    int insert(AiConversation conversation);
    AiConversation selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
    List<AiConversation> listByUser(@Param("userId") Long userId,
                                    @Param("q") String q,
                                    @Param("pinned") Boolean pinned,
                                    @Param("archived") Boolean archived);
    int update(AiConversation conversation);
    int updateLastMessageAt(@Param("id") Long id, @Param("ts") LocalDateTime ts);
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}


