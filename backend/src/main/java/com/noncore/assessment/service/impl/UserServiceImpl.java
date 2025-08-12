package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.EmailService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.dto.request.UpdateProfileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    @Value("${application.base-url:http://localhost:5173}")
    private String applicationBaseUrl;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, RedisTemplate<String, Object> redisTemplate, EmailService emailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserProfile(String username) {
        logger.info("获取用户资料: {}", username);
        User user = userMapper.selectUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPassword(null); // 清除敏感信息
        return user;
    }

    @Override
    public User updateUserProfile(Long userId, UpdateProfileRequest request) {
        logger.info("更新用户资料: {}", userId);
        User existingUser = userMapper.selectUserById(userId);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 仅更新DTO中提供的字段
        if (StringUtils.hasText(request.getNickname())) {
            existingUser.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            String av = request.getAvatar().trim();
            existingUser.setAvatar(av.isEmpty() ? null : av);
        }
        if (request.getGender() != null) {
            existingUser.setGender(request.getGender());
        }
        if (request.getBio() != null) {
            existingUser.setBio(request.getBio());
        }
        if (StringUtils.hasText(request.getPhone())) {
            existingUser.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getCountry())) {
            existingUser.setCountry(request.getCountry());
        }
        if (StringUtils.hasText(request.getProvince())) {
            existingUser.setProvince(request.getProvince());
        }
        if (StringUtils.hasText(request.getCity())) {
            existingUser.setCity(request.getCity());
        }
        if (StringUtils.hasText(request.getBirthday())) {
            try {
                java.util.Date bd = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(request.getBirthday());
                existingUser.setBirthday(bd);
            } catch (Exception ignored) {}
        }

        // 新增：first/last/school/subject
        if (StringUtils.hasText(request.getFirstName())) {
            existingUser.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            existingUser.setLastName(request.getLastName());
        }
        if (request.getSchool() != null) {
            existingUser.setSchool(request.getSchool());
        }
        if (request.getSubject() != null) {
            existingUser.setSubject(request.getSubject());
        }

        // 学号/工号唯一性校验（根据角色判定允许编辑哪一个，或者两者都校验）
        if (StringUtils.hasText(request.getStudentNo())) {
            int exists = userMapper.checkStudentNoExists(request.getStudentNo(), userId);
            if (exists > 0) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学号已存在");
            }
            existingUser.setStudentNo(request.getStudentNo());
        }
        if (StringUtils.hasText(request.getTeacherNo())) {
            int exists = userMapper.checkTeacherNoExists(request.getTeacherNo(), userId);
            if (exists > 0) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "工号已存在");
            }
            existingUser.setTeacherNo(request.getTeacherNo());
        }

        existingUser.setUpdatedAt(new java.util.Date());
        int result = userMapper.updateUser(existingUser);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户资料失败");
        }
        logger.info("用户资料更新成功: {}", existingUser.getUsername());
        return existingUser;
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("修改密码: {}", userId);
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "当前密码错误");
        }

        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "新密码长度至少6个字符");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        int result = userMapper.updatePassword(user.getId(), encodedPassword);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "修改密码失败");
        }
        logger.info("密码修改成功: {}", user.getUsername());
    }

    @Override
    public void forgotPassword(String email) {
        logger.info("处理忘记密码请求: {}", email);
        User user = userMapper.selectUserByEmail(email);
        if (user == null) {
            // 为防止邮箱枚举攻击，即使邮箱不存在也返回成功提示，但后台记录日志
            logger.warn("请求重置密码的邮箱不存在: {}", email);
            return;
        }

        String resetToken = generateResetToken(user.getId());
        String resetKey = "password_reset:" + resetToken;
        redisTemplate.opsForValue().set(resetKey, user.getId(), 15, TimeUnit.MINUTES);

        sendPasswordResetEmail(user.getEmail(), resetToken);
        logger.info("密码重置邮件已发送至: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        logger.info("处理重置密码请求");
        String resetKey = "password_reset:" + token;
        Object userIdObj = redisTemplate.opsForValue().get(resetKey);

        if (userIdObj == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "重置令牌无效或已过期");
        }

        Long userId = Long.parseLong(userIdObj.toString());
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
        redisTemplate.delete(resetKey);
        logger.info("用户密码重置成功，用户ID: {}", userId);
    }

    @Override
    public void verifyEmail(String token) {
        logger.info("处理邮箱验证请求");
        String verifyKey = "email_verify:" + token;
        Object userIdObj = redisTemplate.opsForValue().get(verifyKey);

        if (userIdObj == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "验证令牌无效或已过期");
        }

        Long userId = Long.parseLong(userIdObj.toString());
        userMapper.updateEmailVerified(userId, true);
        redisTemplate.delete(verifyKey);
        logger.info("邮箱验证成功，用户ID: {}", userId);
    }

    @Override
    public void resendVerification(Long userId) {
        logger.info("重新发送验证邮件: {}", userId);
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        String verifyToken = generateVerifyToken(user.getId());
        String verifyKey = "email_verify:" + verifyToken;
        redisTemplate.opsForValue().set(verifyKey, user.getId(), 24, TimeUnit.HOURS);
        sendVerificationEmail(user.getEmail(), verifyToken);
        logger.info("验证邮件已重新发送至: {}", user.getEmail());
    }

    private String generateResetToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_" + userId;
    }

    private String generateVerifyToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_verify_" + userId;
    }

    private void sendPasswordResetEmail(String email, String resetToken) {
        // TODO: 考虑将邮件模板外部化
        String resetUrl = applicationBaseUrl + "/reset-password?token=" + resetToken;
        String subject = "【学生非核心能力发展评估系统】重置密码";
        String content = String.format("请点击此链接重置您的密码（15分钟内有效）: <a href=\"%s\">重置密码</a>", resetUrl);
        emailService.sendSimpleMessage(email, subject, content);
    }

    private void sendVerificationEmail(String email, String verifyToken) {
        // TODO: 考虑将邮件模板外部化
        String verifyUrl = applicationBaseUrl + "/verify-email?token=" + verifyToken;
        String subject = "【学生非核心能力发展评估系统】邮箱验证";
        String content = String.format("请点击此链接验证您的邮箱（24小时内有效）: <a href=\"%s\">验证邮箱</a>", verifyUrl);
        emailService.sendSimpleMessage(email, subject, content);
    }
} 