package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.LoginRequest;
import com.noncore.assessment.dto.request.RegisterRequest;
import com.noncore.assessment.dto.response.AuthResponse;
import com.noncore.assessment.dto.response.UserDto;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.AuthService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.StudentService;
import com.noncore.assessment.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StudentService studentService;
    private final UserService userService;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate, StudentService studentService, UserService userService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.studentService = studentService;
        this.userService = userService;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("用户登录尝试: {}", loginRequest.getUsername());

        if (!StringUtils.hasText(loginRequest.getUsername()) || !StringUtils.hasText(loginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户名和密码不能为空");
        }

        User user = findUserByUsernameOrEmail(loginRequest.getUsername());
        if (user == null) {
            logger.warn("登录失败: 用户不存在 - {}", loginRequest.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("登录失败: 密码错误 - {}", loginRequest.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED, "邮箱未验证，请先完成邮箱验证");
        }
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        logger.info("用户登录成功: {} ({})", user.getUsername(), user.getRole());

        user.setPassword(null);

        return AuthResponse.of(UserDto.fromEntity(user), accessToken, refreshToken, jwtUtil.getExpiration());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (!StringUtils.hasText(registerRequest.getUsername()) || !StringUtils.hasText(registerRequest.getEmail()) || !StringUtils.hasText(registerRequest.getPassword()) || !StringUtils.hasText(registerRequest.getRole())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户名、邮箱、密码和角色均不能为空");
        }

        final String role = registerRequest.getRole().toLowerCase();
        if (!"student".equals(role) && !"teacher".equals(role)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户角色只能是student或teacher");
        }

        if (userMapper.selectUserByUsername(registerRequest.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userMapper.selectUserByEmail(registerRequest.getEmail()) != null) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        // 与数据库列约束和前端保持一致：存储为 STUDENT / TEACHER
        user.setRole("student".equals(role) ? "STUDENT" : "TEACHER");
        if (StringUtils.hasText(registerRequest.getNickname())) {
            user.setNickname(registerRequest.getNickname());
        }
        if (registerRequest.getAvatar() != null && StringUtils.hasText(registerRequest.getAvatar())) {
            user.setAvatar(registerRequest.getAvatar().trim());
        }
        user.initialize();
        
        userMapper.insertUser(user);

        if ("student".equals(role)) {
            studentService.createStudentProfile(user);
        }

        logger.info("用户注册成功: {} ({})，准备发送验证邮件", user.getUsername(), user.getRole());

        // 生成并发送邮箱验证
        // 复用 UserServiceImpl 的逻辑：生成 token -> Redis 保存 -> 发送验证邮件
        // 优先使用注册请求中的语言，如果未提供则由 UserServiceImpl 使用默认语言
        try {
            String reqLang = null;
            try { reqLang = registerRequest.getLang(); } catch (Exception ignored) {}
            if (reqLang != null && !reqLang.isBlank()) {
                userService.resendVerificationByEmail(user.getEmail(), reqLang);
            } else {
                userService.resendVerification(user.getId());
            }
        } catch (Exception e) {
            logger.warn("发送验证邮件时出现非致命错误: {}", e.getMessage());
        }

        user.setPassword(null);
        // 注册后不返回令牌
        return AuthResponse.userOnly(UserDto.fromEntity(user));
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("刷新令牌请求");

        if (!StringUtils.hasText(refreshToken) || !jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "无效的刷新令牌");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "令牌类型错误");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        logger.info("令牌刷新成功: {}", user.getUsername());

        return AuthResponse.tokenRefresh(newAccessToken, jwtUtil.getExpiration());
    }

    @Override
    public void logout(String token) {
        try {
            logger.info("用户登出，令牌加入黑名单");
            Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
            long ttl = expirationDate.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String blacklistKey = "token_blacklist:" + token;
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                logger.info("令牌已加入黑名单，过期时间: {} 毫秒", ttl);
            }
        } catch (DataAccessException e) {
            logger.error("添加令牌到黑名单失败 - Redis操作失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "登出失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = "token_blacklist:" + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (DataAccessException e) {
            logger.error("检查令牌黑名单状态失败 - Redis操作失败。为安全起见，将令牌视为无效。", e);
            return true;
        }
    }
    
    private User findUserByUsernameOrEmail(String identifier) {
        User user = userMapper.selectUserByUsername(identifier);
        if (user == null && identifier.contains("@")) {
            user = userMapper.selectUserByEmail(identifier);
        }
        return user;
    }
} 