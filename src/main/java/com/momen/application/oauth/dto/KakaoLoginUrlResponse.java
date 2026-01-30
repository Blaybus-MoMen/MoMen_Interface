package com.momen.application.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 로그인 URL 응답 DTO
 * CSRF 방지를 위한 state를 함께 반환합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카카오 로그인 URL 응답")
public class KakaoLoginUrlResponse {

    @Schema(description = "카카오 로그인 페이지 URL (state 포함)")
    private String loginUrl;

    @Schema(description = "CSRF 방지용 state (콜백 시 서버로 전달 필수)")
    private String state;
}
