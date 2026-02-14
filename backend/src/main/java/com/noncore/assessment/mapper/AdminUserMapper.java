package com.noncore.assessment.mapper;

import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员用户管理 Mapper。
 *
 * @author System
 * @since 2026-02-14
 */
@Mapper
public interface AdminUserMapper {

    List<AdminUserListItemResponse> pageUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("status") String status,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("offset") int offset,
            @Param("size") int size
    );

    Long countUsers(
            @Param("keyword") String keyword,
            @Param("role") String role,
            @Param("status") String status,
            @Param("includeDeleted") boolean includeDeleted
    );

    int updateUserRole(@Param("id") Long id, @Param("role") String role);

    int updateUserStatus(@Param("id") Long id, @Param("status") String status);
}

