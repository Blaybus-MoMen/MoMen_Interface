package com.momen.application.openai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ChatGPT 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTRequest {

    /**
     * 사용자 프롬프트 (필수)
     */
    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(max = 10000, message = "프롬프트는 최대 10000자까지 입력 가능합니다")
    private String userPrompt;

    /**
     * 시스템 프롬프트 (선택)
     */
    @Size(max = 5000, message = "시스템 프롬프트는 최대 5000자까지 입력 가능합니다")
    private String systemPrompt;

    /**
     * ChatGPT 모델 (기본값: gpt-4)
     * - gpt-3.5-turbo: 빠르고 저렴
     * - gpt-4: 높은 품질
     * - gpt-4-turbo: 빠른 GPT-4
     */
    @Builder.Default
    private String model = "gpt-4";

    /**
     * 온도 설정 (0.0-2.0, 기본값: 0.7)
     * 높을수록 더 창의적이고 무작위적
     */
    @Builder.Default
    private BigDecimal temperature = new BigDecimal("0.7");

    /**
     * 최대 토큰 수 (기본값: 1000)
     */
    @Builder.Default
    private Integer maxTokens = 1000;

    /**
     * 요청한 사용자 ID (선택)
     */
    private Long userId;

    /**
     * 추가 메타데이터 (JSON 형식, 선택)
     */
    private String metadata;
}