package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiVoiceSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiVoiceSessionMapper {
    int insert(AiVoiceSession session);

    AiVoiceSession selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    List<AiVoiceSession> listByUser(@Param("userId") Long userId,
                                    @Param("q") String q,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    int updateTitle(@Param("id") Long id, @Param("userId") Long userId, @Param("title") String title);

    int updatePinned(@Param("id") Long id, @Param("userId") Long userId, @Param("pinned") Boolean pinned);

    int softDelete(@Param("id") Long id, @Param("userId") Long userId);
}

