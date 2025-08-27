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

    @Value("${application.email.default-language:zh-CN}")
    private String defaultEmailLanguage;

    private final com.noncore.assessment.mapper.FileRecordMapper fileRecordMapper;
    private final com.noncore.assessment.service.FileStorageService fileStorageService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, RedisTemplate<String, Object> redisTemplate, EmailService emailService,
                           com.noncore.assessment.mapper.FileRecordMapper fileRecordMapper,
                           com.noncore.assessment.service.FileStorageService fileStorageService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.fileRecordMapper = fileRecordMapper;
        this.fileStorageService = fileStorageService;
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

        sendPasswordResetEmail(user.getEmail(), resetToken, defaultEmailLanguage);
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
        sendVerificationEmail(user.getEmail(), verifyToken, defaultEmailLanguage);
        logger.info("验证邮件已重新发送至: {}", user.getEmail());
    }

    @Override
    public void resendVerificationByEmail(String email, String lang) {
        logger.info("通过邮箱重发验证邮件: {}", email);
        User user = userMapper.selectUserByEmail(email);
        if (user == null) {
            logger.warn("重发验证邮件的邮箱不存在: {}", email);
            return;
        }
        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }
        String verifyToken = generateVerifyToken(user.getId());
        String verifyKey = "email_verify:" + verifyToken;
        redisTemplate.opsForValue().set(verifyKey, user.getId(), 24, TimeUnit.HOURS);
        String langToUse = (lang != null && !lang.isBlank()) ? lang : defaultEmailLanguage;
        sendVerificationEmail(user.getEmail(), verifyToken, langToUse);
        logger.info("验证邮件已发送至: {}", email);
    }

    @Override
    public void initiateChangeEmail(Long userId, String newEmail, String lang) {
        logger.info("发起更换邮箱: userId={}, newEmail={}", userId, newEmail);
        if (!StringUtils.hasText(newEmail)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "新邮箱不能为空");
        }
        int exists = userMapper.checkEmailExists(newEmail, userId);
        if (exists > 0) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        String token = UUID.randomUUID().toString().replace("-", "") + "_change_" + userId;
        String key = "email_change:" + token;
        redisTemplate.opsForValue().set(key, newEmail, 24, TimeUnit.HOURS);
        sendChangeEmailConfirm(newEmail, token);
    }

    @Override
    public void confirmChangeEmail(String token) {
        logger.info("确认更换邮箱");
        String key = "email_change:" + token;
        Object newEmailObj = redisTemplate.opsForValue().get(key);
        if (newEmailObj == null) {
            throw new BusinessException(ErrorCode.EMAIL_CHANGE_TOKEN_INVALID);
        }
        String newEmail = String.valueOf(newEmailObj);
        try {
            String[] parts = token.split("_change_");
            String left = parts[1];
            Long userId = Long.parseLong(left);
            User user = userMapper.selectUserById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
            user.setEmail(newEmail);
            user.setEmailVerified(true);
            user.setUpdatedAt(new java.util.Date());
            userMapper.updateUser(user);
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_CHANGE_TOKEN_INVALID);
        }
    }

    @Override
    public void updateAvatar(Long userId, Long fileId) {
        logger.info("更新头像 userId={}, fileId={}", userId, fileId);
        User user = userMapper.selectUserById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        com.noncore.assessment.entity.FileRecord fr = fileRecordMapper.selectFileRecordById(fileId);
        if (fr == null) throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        if (!"avatar".equals(fr.getRelatedType()) && !"avatar".equals(fr.getUploadPurpose())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "文件用途不是头像");
        }
        if (!java.util.Objects.equals(fr.getUploaderId(), userId)) {
            throw new BusinessException(ErrorCode.FILE_PERMISSION_DENIED, "不能使用他人文件作为头像");
        }

        // 更新用户头像URL（可用统一下载接口路径，亦可直接存文件路径，这里使用下载接口）
        String avatarUrl = "/api/files/" + fileId + "/download";
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(new java.util.Date());
        userMapper.updateUser(user);

        // 清理旧头像：删除该用户所有 avatar 文件中除当前 fileId 之外的文件
        try {
            java.util.List<com.noncore.assessment.entity.FileRecord> avatars = fileRecordMapper.selectByUploaderIdAndRelatedType(userId, "avatar");
            if (avatars != null) {
                for (com.noncore.assessment.entity.FileRecord a : avatars) {
                    if (!java.util.Objects.equals(a.getId(), fileId)) {
                        try { fileStorageService.deleteFile(a.getId(), userId); } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("清理旧头像失败 userId={}", userId, e);
        }
    }

    private String generateResetToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_" + userId;
    }

    private String generateVerifyToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_verify_" + userId;
    }

    private void sendPasswordResetEmail(String email, String resetToken, String lang) {
        String language = (lang != null && lang.toLowerCase().startsWith("en")) ? "en-US" : "zh-CN";
        String resetUrl = applicationBaseUrl + "/reset-password?token=" + resetToken + "&lang=" + language;
        java.util.Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("appName", "学生核心能力培养系统");
        vars.put("actionUrl", resetUrl);
        vars.put("expireHours", 0.25);
        vars.put("year", java.time.Year.now().getValue());
        String template = language.equals("en-US") ? "password_reset_en" : "password_reset_zh";
        String subject = language.equals("en-US") ? "[Student Core Competence System] Reset Password" : "【学生核心能力培养系统】重置密码";
        emailService.sendTemplate(email, subject, template, vars, language);
    }

    private void sendVerificationEmail(String email, String verifyToken, String lang) {
        String language = (lang != null && lang.toLowerCase().startsWith("en")) ? "en-US" : "zh-CN";
        String verifyUrl = applicationBaseUrl + "/verify-email?token=" + verifyToken + "&lang=" + language;
        java.util.Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("appName", "学生核心能力培养系统");
        vars.put("actionUrl", verifyUrl);
        vars.put("expireHours", 24);
        vars.put("year", java.time.Year.now().getValue());
        String template = language.equals("en-US") ? "verify_email_en" : "verify_email_zh";
        String subject = language.equals("en-US") ? "[Student Core Competence System] Email Verification" : "【学生核心能力培养系统】邮箱验证";
        emailService.sendTemplate(email, subject, template, vars, language);
    }

    private void sendChangeEmailConfirm(String email, String token) {
        String confirmUrl = applicationBaseUrl + "/email-change/confirm?token=" + token;
        java.util.Map<String, Object> vars = new java.util.HashMap<>();
        vars.put("appName", "学生核心能力培养系统");
        vars.put("actionUrl", confirmUrl);
        vars.put("expireHours", 24);
        vars.put("year", java.time.Year.now().getValue());
        emailService.sendTemplate(email, "【学生核心能力培养系统】确认更换邮箱", "change_email_zh", vars, "zh-CN");
    }
} 