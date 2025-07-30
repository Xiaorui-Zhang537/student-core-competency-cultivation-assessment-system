package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.LoginRequest;
import com.noncore.assessment.dto.request.RegisterRequest;
import com.noncore.assessment.dto.response.AuthResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.AuthService;
import com.noncore.assessment.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

/**
 * 认证服务实现类
 * 实现用户认证相关的业务逻辑
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("用户登录尝试: {}", loginRequest.getUsername());

        // 验证输入参数
        if (!StringUtils.hasText(loginRequest.getUsername()) || !StringUtils.hasText(loginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户名和密码不能为空");
        }

        // 查找用户（支持用户名或邮箱登录）
        User user = findUserByUsernameOrEmail(loginRequest.getUsername());
        if (user == null) {
            logger.warn("登录失败: 用户不存在 - {}", loginRequest.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("登录失败: 密码错误 - {}", loginRequest.getUsername());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());

        // 生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        logger.info("用户登录成功: {} ({})", user.getUsername(), user.getRole());

        // 清除敏感信息
        user.setPassword(null);

        return AuthResponse.of(user, accessToken, refreshToken, jwtUtil.getExpiration());
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("用户注册尝试: {}", registerRequest.getUsername());

        // 验证输入参数
        validateRegisterRequest(registerRequest);

        // 检查用户名是否已存在
        if (userMapper.checkUsernameExists(registerRequest.getUsername(), null) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 检查邮箱是否已存在
        if (userMapper.checkEmailExists(registerRequest.getEmail(), null) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 验证密码一致性
        if (!registerRequest.isPasswordMatch()) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 创建新用户
        User newUser = createUserFromRegisterRequest(registerRequest);

        // 加密密码
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // 设置创建时间
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setDeleted(false);

        // 插入数据库
        int result = userMapper.insertUser(newUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "用户注册失败");
        }

        logger.info("用户注册成功: {} ({})", newUser.getUsername(), newUser.getRole());

        // 生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(newUser.getId(), newUser.getUsername(), newUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(newUser.getId(), newUser.getUsername());

        // 清除敏感信息
        newUser.setPassword(null);

        return AuthResponse.of(newUser, accessToken, refreshToken, jwtUtil.getExpiration());
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("刷新令牌请求");

        // 验证刷新令牌
        if (!StringUtils.hasText(refreshToken) || !jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "无效的刷新令牌");
        }

        // 检查是否为刷新令牌
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "令牌类型错误");
        }

        // 从令牌中获取用户信息
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 查找用户
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        logger.info("令牌刷新成功: {}", username);

        return AuthResponse.tokenRefresh(newAccessToken, jwtUtil.getExpiration());
    }

    @Override
    public void logout(String token) {
        try {
            logger.info("用户登出，令牌加入黑名单");
            
            // 从token中提取过期时间
            Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
            long ttl = expirationDate.getTime() - System.currentTimeMillis();
            
            // 如果令牌还未过期，将其加入黑名单
            if (ttl > 0) {
                String blacklistKey = "token_blacklist:" + token;
                // 存储到Redis，设置过期时间与JWT令牌一致
                redisTemplate.opsForValue().set(blacklistKey, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                logger.info("令牌已加入黑名单，过期时间: {} 毫秒", ttl);
            }
            
        } catch (Exception e) {
            logger.error("添加令牌到黑名单失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "登出失败: " + e.getMessage());
        }
    }

    /**
     * 检查令牌是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = "token_blacklist:" + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            logger.error("检查令牌黑名单状态失败", e);
            return false;
        }
    }

    /**
     * 获取当前用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    public User getCurrentUser(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 更新用户资料
     * 
     * @param username 用户名
     * @param userInfo 用户信息
     * @return 更新后的用户信息
     */
    public User updateProfile(String username, User userInfo) {
        logger.info("更新用户资料: {}", username);

        // 查找当前用户
        User currentUser = userMapper.selectUserByUsername(username);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(userInfo.getEmail()) && 
            !userInfo.getEmail().equals(currentUser.getEmail())) {
            if (userMapper.checkEmailExists(userInfo.getEmail(), currentUser.getId()) > 0) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS, "邮箱已被其他用户使用");
            }
        }

        // 更新用户信息
        updateUserInfo(currentUser, userInfo);
        currentUser.setUpdatedAt(LocalDateTime.now());

        int result = userMapper.updateUser(currentUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户资料失败");
        }

        logger.info("用户资料更新成功: {}", username);

        // 清除敏感信息
        currentUser.setPassword(null);
        return currentUser;
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        logger.info("修改密码: {}", username);

        // 查找用户
        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "当前密码错误");
        }

        // 验证新密码
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "新密码长度至少6个字符");
        }

        // 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        int result = userMapper.updatePassword(user.getId(), encodedPassword);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "修改密码失败");
        }

        logger.info("密码修改成功: {}", username);
    }

    @Override
    public void forgotPassword(String email) {
        try {
            logger.info("处理忘记密码请求: {}", email);
            
            // 检查用户是否存在
            User user = userMapper.selectByEmail(email);
            if (user == null) {
                throw new BusinessException(ErrorCode.EMAIL_NOT_REGISTERED, "该邮箱未注册");
            }
            
            // 生成重置密码令牌
            String resetToken = generateResetToken(user.getId());
            
            // 存储重置令牌到Redis，有效期15分钟
            String resetKey = "password_reset:" + resetToken;
            redisTemplate.opsForValue().set(resetKey, user.getId(), 15, TimeUnit.MINUTES);
            
            // 发送重置密码邮件
            sendPasswordResetEmail(email, resetToken);
            
            logger.info("重置密码邮件已发送到: {}", email);
            
        } catch (Exception e) {
            logger.error("处理忘记密码请求失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "发送重置密码邮件失败: " + e.getMessage());
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        try {
            logger.info("处理重置密码请求");
            
            // 验证重置令牌
            String resetKey = "password_reset:" + token;
            Object userIdObj = redisTemplate.opsForValue().get(resetKey);
            
            if (userIdObj == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "重置令牌无效或已过期");
            }
            
            Long userId = (Long) userIdObj;
            
            // 加密新密码
            String encodedPassword = passwordEncoder.encode(newPassword);
            
            // 更新用户密码
            User user = new User();
            user.setId(userId);
            user.setPassword(encodedPassword);
            user.updateTimestamp();
            
            int result = userMapper.updateUser(user);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新密码失败");
            }
            
            // 删除已使用的重置令牌
            redisTemplate.delete(resetKey);
            
            logger.info("用户密码重置成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("重置密码失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "重置密码失败: " + e.getMessage());
        }
    }

    @Override
    public void verifyEmail(String token) {
        try {
            logger.info("处理邮箱验证请求");
            
            // 验证邮箱验证令牌
            String verifyKey = "email_verify:" + token;
            Object userIdObj = redisTemplate.opsForValue().get(verifyKey);
            
            if (userIdObj == null) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "验证令牌无效或已过期");
            }
            
            Long userId = (Long) userIdObj;
            
            // 更新用户邮箱验证状态
            User user = new User();
            user.setId(userId);
            user.setEmailVerified(true);
            user.updateTimestamp();
            
            int result = userMapper.updateUser(user);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新邮箱验证状态失败");
            }
            
            // 删除已使用的验证令牌
            redisTemplate.delete(verifyKey);
            
            logger.info("邮箱验证成功，用户ID: {}", userId);
            
        } catch (Exception e) {
            logger.error("邮箱验证失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "邮箱验证失败: " + e.getMessage());
        }
    }

    @Override
    public void resendVerification(String username) {
        try {
            logger.info("重新发送验证邮件: {}", username);
            
            // 获取用户信息
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }
            
            if (user.getEmailVerified() != null && user.getEmailVerified()) {
                throw new BusinessException(ErrorCode.EMAIL_VERIFIED, "邮箱已验证，无需重复验证");
            }
            
            // 生成新的验证令牌
            String verifyToken = generateVerifyToken(user.getId());
            
            // 存储验证令牌到Redis，有效期24小时
            String verifyKey = "email_verify:" + verifyToken;
            redisTemplate.opsForValue().set(verifyKey, user.getId(), 24, TimeUnit.HOURS);
            
            // 发送验证邮件
            sendVerificationEmail(user.getEmail(), verifyToken);
            
            logger.info("验证邮件已重新发送到: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("重新发送验证邮件失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "重新发送验证邮件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userMapper.checkUsernameExists(username, null) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userMapper.checkEmailExists(email, null) > 0;
    }

    @Override
    public User getUserProfile(String username) {
        logger.info("获取用户资料: {}", username);
        
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 清除敏感信息
        user.setPassword(null);
        
        return user;
    }

    @Override
    public void updateUserProfile(User user) {
        logger.info("更新用户资料: {}", user.getUsername());
        
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 检查邮箱是否被其他用户使用
        if (!existingUser.getEmail().equals(user.getEmail())) {
            User emailUser = userMapper.selectByEmail(user.getEmail());
            if (emailUser != null && !emailUser.getId().equals(existingUser.getId())) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS, "邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息
        user.setId(existingUser.getId());
        user.setUpdatedAt(LocalDateTime.now());
        
        int result = userMapper.updateUser(user);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户资料失败");
        }
        
        logger.info("用户资料更新成功: {}", user.getUsername());
    }

    /**
     * 根据用户名或邮箱查找用户
     */
    private User findUserByUsernameOrEmail(String identifier) {
        // 首先按用户名查找
        User user = userMapper.selectUserByUsername(identifier);
        if (user == null) {
            // 如果包含@符号，按邮箱查找
            if (identifier.contains("@")) {
                user = userMapper.selectUserByEmail(identifier);
            }
        }
        return user;
    }

    /**
     * 验证注册请求参数
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户名不能为空");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "邮箱不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "密码不能为空");
        }
        if (!StringUtils.hasText(request.getRole())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户角色不能为空");
        }
        if (!"student".equals(request.getRole()) && !"teacher".equals(request.getRole())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "用户角色只能是student或teacher");
        }
    }

    /**
     * 从注册请求创建用户对象
     */
    private User createUserFromRegisterRequest(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDisplayName(request.getDisplayName());
        user.setGrade(request.getGrade());
        user.setSubject(request.getSubject());
        user.setSchool(request.getSchool());
        
        // 设置默认头像
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + request.getUsername());
        
        return user;
    }

    /**
     * 更新用户信息
     */
    private void updateUserInfo(User currentUser, User userInfo) {
        if (StringUtils.hasText(userInfo.getEmail())) {
            currentUser.setEmail(userInfo.getEmail());
        }
        if (StringUtils.hasText(userInfo.getFirstName())) {
            currentUser.setFirstName(userInfo.getFirstName());
        }
        if (StringUtils.hasText(userInfo.getLastName())) {
            currentUser.setLastName(userInfo.getLastName());
        }
        if (StringUtils.hasText(userInfo.getDisplayName())) {
            currentUser.setDisplayName(userInfo.getDisplayName());
        }
        if (StringUtils.hasText(userInfo.getBio())) {
            currentUser.setBio(userInfo.getBio());
        }
        if (StringUtils.hasText(userInfo.getGrade())) {
            currentUser.setGrade(userInfo.getGrade());
        }
        if (StringUtils.hasText(userInfo.getSubject())) {
            currentUser.setSubject(userInfo.getSubject());
        }
        if (StringUtils.hasText(userInfo.getSchool())) {
            currentUser.setSchool(userInfo.getSchool());
        }
        if (StringUtils.hasText(userInfo.getPhone())) {
            currentUser.setPhone(userInfo.getPhone());
        }
        if (StringUtils.hasText(userInfo.getAvatar())) {
            currentUser.setAvatar(userInfo.getAvatar());
        }
    }

    /**
     * 生成重置密码令牌
     */
    private String generateResetToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_" + userId;
    }

    /**
     * 生成邮箱验证令牌
     */
    private String generateVerifyToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_verify_" + userId;
    }

    /**
     * 发送重置密码邮件
     */
    private void sendPasswordResetEmail(String email, String resetToken) {
        // 构建重置密码链接
        String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken;
        
        // 邮件内容
        String subject = "【学生非核心能力发展评估系统】重置密码";
        String content = String.format("""
            <html>
            <body>
                <h2>重置密码</h2>
                <p>您好，</p>
                <p>您请求重置密码。请点击下面的链接来重置您的密码：</p>
                <p><a href="%s" style="color: #007bff;">重置密码</a></p>
                <p>此链接将在15分钟后失效。</p>
                <p>如果您没有请求重置密码，请忽略此邮件。</p>
                <br>
                <p>学生非核心能力发展评估系统</p>
            </body>
            </html>
            """, resetUrl);
        
        // 发送邮件（实际实现需要配置邮件服务）
        logger.info("模拟发送重置密码邮件到: {}, 主题: {}, 内容长度: {} 字符, 重置链接: {}", 
                   email, subject, content.length(), resetUrl);
    }

    /**
     * 发送邮箱验证邮件
     */
    private void sendVerificationEmail(String email, String verifyToken) {
        // 构建验证链接
        String verifyUrl = "http://localhost:5173/verify-email?token=" + verifyToken;
        
        // 邮件内容
        String subject = "【学生非核心能力发展评估系统】邮箱验证";
        String content = String.format("""
            <html>
            <body>
                <h2>邮箱验证</h2>
                <p>您好，</p>
                <p>欢迎注册学生非核心能力发展评估系统！请点击下面的链接验证您的邮箱：</p>
                <p><a href="%s" style="color: #007bff;">验证邮箱</a></p>
                <p>此链接将在24小时后失效。</p>
                <p>如果您没有注册账号，请忽略此邮件。</p>
                <br>
                <p>学生非核心能力发展评估系统</p>
            </body>
            </html>
            """, verifyUrl);
        
        // 发送邮件（实际实现需要配置邮件服务）
        logger.info("模拟发送验证邮件到: {}, 主题: {}, 内容长度: {} 字符, 验证链接: {}", 
                   email, subject, content.length(), verifyUrl);
    }
} 