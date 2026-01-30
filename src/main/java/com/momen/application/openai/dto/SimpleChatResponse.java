package com.momen.application.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 간단한 ChatGPT 테스트 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleChatResponse {

    /**
     * AI 응답
     */
    private String response;

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
     * 성공 여부
     */
    private boolean success;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 성공 응답 생성
     */
    public static SimpleChatResponse success(String response, Integer tokensUsed, Integer promptTokens, Integer completionTokens) {
        return SimpleChatResponse.builder()
                .response(response)
                .tokensUsed(tokensUsed)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .success(true)
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static SimpleChatResponse failed(String errorMessage) {
        return SimpleChatResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}