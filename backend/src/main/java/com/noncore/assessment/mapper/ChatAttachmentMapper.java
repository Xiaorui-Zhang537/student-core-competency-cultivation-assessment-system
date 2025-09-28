package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.ChatMessageAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatAttachmentMapper {

    int insert(ChatMessageAttachment att);

    int batchInsert(@Param("notificationId") Long notificationId,
                    @Param("fileIds") List<Long> fileIds,
                    @Param("uploaderId") Long uploaderId);

    List<ChatMessageAttachment> selectByNotificationIds(@Param("ids") List<Long> notificationIds);

    /**
     * 判断指定用户是否对某个文件拥有聊天会话访问权限（作为发送者或接收者）。
     */
    int existsUserAccessForFile(@Param("fileId") Long fileId,
                                @Param("userId") Long userId);
}


