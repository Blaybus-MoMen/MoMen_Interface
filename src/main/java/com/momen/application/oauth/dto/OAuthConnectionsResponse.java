package com.momen.application.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 소셜 연동 상태 조회 응답 DTO
 * 설정 화면에서 "카카오 연동됨" 등 표시용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "소셜 연동 상태 조회 응답")
public class OAuthConnectionsResponse {

    @Schema(description = "제공자별 연동 정보 (kakao, google, naver)")
    private Map<String, ProviderConnection> connections;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "제공자별 연동 정보")
    public static class ProviderConnection {
        @Schema(description = "연동 여부")
        private boolean connected;

        @Schema(description = "연동 시각 (연동된 경우)")
        private LocalDateTime connectedAt;
    }
}
