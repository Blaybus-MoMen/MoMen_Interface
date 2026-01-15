package com.blaybus.application.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OAuth 로그인 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth 로그인 응답")
public class OAuthLoginResponse {

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "사용자 이메일")
    private String email;

    @Schema(description = "사용자 이름")
    private String name;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    @Schema(description = "Access Token")
    private String accessToken;

    @Schema(description = "Refresh Token")
    private String refreshToken;

    @Schema(description = "Access Token 만료 시간 (밀리초)")
    private Long accessTokenExpiresIn;

    @Schema(description = "신규 가입 여부")
    private Boolean isNewUser;

    @Schema(description = "OAuth 제공자")
    private String provider;
}
