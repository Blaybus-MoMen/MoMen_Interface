package com.blaybus.application.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 토큰 응답 DTO
 */
@Getter
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private String role;

    public TokenResponse(String accessToken, String refreshToken, Long userId, String email, String name, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
