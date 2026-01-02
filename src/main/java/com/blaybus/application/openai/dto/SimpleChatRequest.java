package com.blaybus.application.openai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 간단한 ChatGPT 테스트 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleChatRequest {

    /**
     * 사용자 프롬프트 (필수)
     */
    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(max = 10000, message = "프롬프트는 최대 10000자까지 입력 가능합니다")
    private String prompt;

    /**
     * ChatGPT 모델 (선택, 기본값: gpt-4)
     */
    @Builder.Default
    private String model = "gpt-4";

    /**
     * 온도 설정 (선택, 기본값: 0.7)
     */
    @Builder.Default
    private BigDecimal temperature = new BigDecimal("0.7");

    /**
     * 최대 토큰 수 (선택, 기본값: 1000)
     */
    @Builder.Default
    private Integer maxTokens = 1000;
}