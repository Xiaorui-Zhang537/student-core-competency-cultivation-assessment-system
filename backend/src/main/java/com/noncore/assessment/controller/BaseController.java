package com.noncore.assessment.controller;

import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {

    private final UserService userService;

    public BaseController(UserService userService) {
        this.userService = userService;
    }

    protected User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || "anonymousUser".equals(username)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户未登录");
        }
        User user = userService.getUserProfile(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "无法获取当前用户信息");
        }
        return user;
    }

    protected Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
    
    protected Long getOptionalUserId() {
        try {
            return getCurrentUserId();
        } catch (BusinessException e) {
            // 用户未登录或无法获取，安全地返回null
            return null;
        }
    }

    protected boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }
} 