package com.blaybus.application.openai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DALL-E 이미지 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DalleGenerateRequest {

    /**
     * 이미지 생성 프롬프트 (필수)
     */
    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(max = 4000, message = "프롬프트는 최대 4000자까지 입력 가능합니다")
    private String prompt;

    /**
     * DALL-E 모델 (기본값: dall-e-3)
     * - dall-e-2: 이전 버전
     * - dall-e-3: 최신 버전 (높은 품질)
     */
    @Builder.Default
    private String model = "dall-e-3";

    /**
     * 이미지 크기 (기본값: 1024x1024)
     * DALL-E 2: 256x256, 512x512, 1024x1024
     * DALL-E 3: 1024x1024, 1792x1024, 1024x1792
     */
    @Builder.Default
    private String size = "1024x1024";

    /**
     * 품질 (DALL-E 3 전용, 기본값: standard)
     * - standard: 표준 품질
     * - hd: 고화질
     */
    @Builder.Default
    private String quality = "standard";

    /**
     * 스타일 (DALL-E 3 전용, 기본값: vivid)
     * - vivid: 생동감 있고 극적인
     * - natural: 자연스럽고 사실적인
     */
    @Builder.Default
    private String style = "vivid";

    /**
     * 시드 값 (재현성, 선택)
     */
    private Long seed;

    /**
     * 요청한 사용자 ID (선택)
     */
    private Long userId;

    /**
     * 추가 메타데이터 (JSON 형식, 선택)
     */
    private String metadata;
}