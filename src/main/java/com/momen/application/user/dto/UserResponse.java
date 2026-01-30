package com.momen.application.user.dto;

import com.momen.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 * Entity → DTO 변환은 core.mapper.UserMapper 사용
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

    // OAuth·프로필
    private String oauthProvider;       // "kakao", "google", "naver" 또는 null
    private LocalDateTime oauthConnectedAt;
    private String profileImageUrl;
    private Boolean oauthConnected;      // 소셜 연동 여부

    // 온보딩·약관 (신규 OAuth 사용자 추가 정보 여부 판단용)
    private LocalDateTime termsAgreedAt;
}
