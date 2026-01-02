package com.blaybus.application.user.dto;

import com.blaybus.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
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

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .emailVerified(user.getEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .isActive(user.getIsActive())
                .createdAt(user.getCreateDt())
                .build();
    }
}
