package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.AiVoiceTurn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiVoiceTurnMapper {
    int insert(AiVoiceTurn turn);

    List<AiVoiceTurn> listBySession(@Param("sessionId") Long sessionId,
                                    @Param("userId") Long userId,
                                    @Param("offset") int offset,
                                    @Param("size") int size);
}

