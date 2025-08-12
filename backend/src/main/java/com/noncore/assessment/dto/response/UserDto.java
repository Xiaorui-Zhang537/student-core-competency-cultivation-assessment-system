package com.noncore.assessment.dto.response;

import com.noncore.assessment.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;
    private String firstName;
    private String lastName;
    private String studentNo;
    private String teacherNo;
    private String bio;
    private String grade;
    private String subject;
    private String school;
    private String phone;
    private Date createdAt;
    private Date updatedAt;
    private boolean emailVerified;

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .studentNo(user.getStudentNo())
                .teacherNo(user.getTeacherNo())
                .bio(user.getBio())
                .grade(user.getGrade())
                .subject(user.getSubject())
                .school(user.getSchool())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .emailVerified(user.isEmailVerified())
                .build();
    }
} 