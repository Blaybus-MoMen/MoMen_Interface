package com.blaybus.application.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ChatGPT 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTResponse {

    /**
     * 작업 ID (UUID)
     */
    private String jobId;

    /**
     * AI 응답
     */
    private String response;

    /**
     * 작업 상태
     */
    private String status;

    /**
     * 사용된 총 토큰 수
     */
    private Integer tokensUsed;

    /**
     * 프롬프트 토큰 수
     */
    private Integer promptTokens;

    /**
     * 완료 토큰 수
     */
    private Integer completionTokens;

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
    public static ChatGPTResponse success(String jobId, String response, Integer tokensUsed, Integer promptTokens, Integer completionTokens) {
        return ChatGPTResponse.builder()
                .jobId(jobId)
                .response(response)
                .status("COMPLETED")
                .tokensUsed(tokensUsed)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .build();
    }

    /**
     * 대기 중 응답
     */
    public static ChatGPTResponse pending(String jobId) {
        return ChatGPTResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .build();
    }

    /**
     * 실패 응답
     */
    public static ChatGPTResponse failed(String jobId, String errorCode, String errorMessage) {
        return ChatGPTResponse.builder()
                .jobId(jobId)
                .status("FAILED")
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}