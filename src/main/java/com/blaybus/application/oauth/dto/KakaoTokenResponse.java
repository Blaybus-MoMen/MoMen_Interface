package com.blaybus.application.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카카오 토큰 응답 DTO
 * 카카오 OAuth 서버로부터 받는 토큰 정보
 */
@Getter
@Setter
@NoArgsConstructor
public class KakaoTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;

    @JsonProperty("scope")
    private String scope;
}
