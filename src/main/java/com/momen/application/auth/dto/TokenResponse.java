package com.momen.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String name;
    private String role;
}
