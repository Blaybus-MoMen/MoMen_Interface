package com.blaybus.application.runway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Runway 비디오 생성 요청 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunwayGenerateResponse {

    /**
     * 작업 ID (UUID)
     */
    private String taskId;

    /**
     * 작업 상태
     */
    private String status;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 생성 성공 응답
     */
    public static RunwayGenerateResponse pending(String taskId) {
        return RunwayGenerateResponse.builder()
                .taskId(taskId)
                .status("PENDING")
                .build();
    }

    /**
     * 생성 실패 응답
     */
    public static RunwayGenerateResponse failed(String errorMessage) {
        return RunwayGenerateResponse.builder()
                .status("FAILED")
                .errorMessage(errorMessage)
                .build();
    }
}
