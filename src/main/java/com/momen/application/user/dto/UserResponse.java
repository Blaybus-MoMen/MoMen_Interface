package com.momen.application.user.dto;

import com.momen.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 * Entity -> DTO 변환은 core.mapper.UserMapper 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private Boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private UserRole role;
    private String profileImageUrl;
    private LocalDateTime termsAgreedAt;
}
