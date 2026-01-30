package com.momen.application.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 로그인 요청 DTO
 * 클라이언트에서 받은 authorization code를 서버로 전달
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카카오 로그인 요청")
public class KakaoLoginRequest {

    @NotBlank(message = "인증 코드는 필수입니다")
    @Schema(description = "카카오 인증 코드", example = "authorization_code_from_kakao")
    private String code;

    @NotBlank(message = "state는 필수입니다 (CSRF 방지)")
    @Schema(description = "로그인 URL 발급 시 받은 state (일회성)", example = "uuid-from-login-url")
    private String state;

    @Schema(description = "리다이렉트 URI (로그인 URL 요청 시 사용한 값과 동일해야 함)", example = "http://localhost:3000/oauth/kakao/callback")
    private String redirectUri;
}
