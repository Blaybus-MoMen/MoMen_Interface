package com.blaybus.application.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DALL-E 이미지 생성 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DalleGenerateResponse {

    /**
     * 작업 ID (UUID)
     */
    private String jobId;

    /**
     * 생성된 이미지 URL
     */
    private String imageUrl;

    /**
     * DALL-E가 수정한 프롬프트 (DALL-E 3 전용)
     */
    private String revisedPrompt;

    /**
     * 작업 상태
     */
    private String status;

    /**
     * Base64 인코딩된 이미지 (선택적)
     */
    private String b64Json;

    /**
     * 저작권 관련 플래그
     */
    private Boolean copyrightFlag;

    /**
     * 안전 필터 작동 여부
     */
    private Boolean safetyFilterTriggered;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 에러 코드 (실패 시)
     */
    private String errorCode;

    /**
     * 성공 응답
     */
    public static DalleGenerateResponse success(String jobId, String imageUrl, String revisedPrompt, String b64Json) {
        return DalleGenerateResponse.builder()
                .jobId(jobId)
                .imageUrl(imageUrl)
                .revisedPrompt(revisedPrompt)
                .b64Json(b64Json)
                .status("COMPLETED")
                .copyrightFlag(false)
                .safetyFilterTriggered(false)
                .build();
    }

    /**
     * 대기 중 응답
     */
    public static DalleGenerateResponse pending(String jobId) {
        return DalleGenerateResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .build();
    }

    /**
     * 실패 응답
     */
    public static DalleGenerateResponse failed(String jobId, String errorCode, String errorMessage) {
        return DalleGenerateResponse.builder()
                .jobId(jobId)
                .status("FAILED")
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
