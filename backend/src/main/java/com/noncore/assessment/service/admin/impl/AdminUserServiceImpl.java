package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.dto.request.admin.AdminUserCreateRequest;
import com.noncore.assessment.dto.response.admin.AdminUserListItemResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AdminUserMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminUserService;
import com.noncore.assessment.util.PageResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员用户管理服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AdminUserServiceImpl(AdminUserMapper adminUserMapper,
                                UserMapper userMapper,
                                PasswordEncoder passwordEncoder,
                                UserService userService) {
        this.adminUserMapper = adminUserMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AdminUserListItemResponse> pageUsers(int page, int size, String keyword, String role, String status, boolean includeDeleted) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = (p - 1) * s;

        List<AdminUserListItemResponse> items = adminUserMapper.pageUsers(keyword, role, status, includeDeleted, offset, s);
        long total = safe(adminUserMapper.countUsers(keyword, role, status, includeDeleted));
        int totalPages = (int) Math.ceil(total / (double) s);
        return PageResult.of(items, p, s, total, totalPages);
    }

    @Override
    public User createUser(AdminUserCreateRequest request) {
        // 基础唯一性校验（用户名/邮箱）
        if (userMapper.selectUserByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userMapper.selectUserByEmail(request.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        User user = new User();
        user.initialize();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole()));
        user.setStatus(normalizeStatus(request.getStatus()));

        user.setNickname(request.getNickname());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStudentNo(request.getStudentNo());
        user.setTeacherNo(request.getTeacherNo());
        user.setSchool(request.getSchool());
        user.setSubject(request.getSubject());
        user.setGrade(request.getGrade());

        int inserted = userMapper.insertUser(user);
        if (inserted <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建用户失败");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUserRole(Long id, String role) {
        String r = normalizeRole(role);
        int updated = adminUserMapper.updateUserRole(id, r);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户不存在或更新失败");
        }
    }

    @Override
    public void updateUserStatus(Long id, String status) {
        String s = normalizeStatus(status);
        int updated = adminUserMapper.updateUserStatus(id, s);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户不存在或更新失败");
        }
    }

    @Override
    public void sendPasswordResetEmail(Long id, String lang) {
        User u = userMapper.selectUserById(id);
        if (u == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        // 复用现有忘记密码流程：发送重置邮件
        userService.forgotPassword(u.getEmail());
        // lang 目前由 forgotPassword 内部决定（如后续需要可扩展接口参数）
    }

    private long safe(Long v) {
        return v == null ? 0L : v;
    }

    private String normalizeRole(String role) {
        if (role == null) throw new BusinessException(ErrorCode.INVALID_PARAMETER, "角色不能为空");
        String r = role.trim().toLowerCase();
        if (!("student".equals(r) || "teacher".equals(r) || "admin".equals(r))) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "非法角色: " + role);
        }
        return r;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "active";
        String s = status.trim().toLowerCase();
        if (!("active".equals(s) || "disabled".equals(s))) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "非法状态: " + status);
        }
        return s;
    }
}

