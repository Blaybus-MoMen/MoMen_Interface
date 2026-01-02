package com.blaybus.application.runway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Runway Gen-3 비디오 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunwayGenerateRequest {

    /**
     * 비디오 생성 프롬프트 (최대 1000자)
     */
    @NotBlank(message = "프롬프트는 필수입니다")
    @Size(max = 1000, message = "프롬프트는 최대 1000자까지 입력 가능합니다")
    private String promptText;

    /**
     * 모델 선택 (기본값: veo3.1_fast)
     * - veo3.1_fast: Veo 3.1 Fast (빠른 속도)
     * - veo3.1: Veo 3.1 (높은 품질)
     * - veo3: Veo 3 (이전 버전)
     */
    @Builder.Default
    private String model = "veo3.1_fast";

    /**
     * 화면 비율 (픽셀 해상도)
     * - 1280:720 (16:9 가로)
     * - 720:1280 (9:16 세로)
     * - 1080:1920 (9:16 세로 HD)
     * - 1920:1080 (16:9 가로 HD)
     */
    @Builder.Default
    private String ratio = "1280:720";

    /**
     * ratio 값 정규화
     * 16:9 -> 1280:720 형식으로 변환
     */
    public void normalizeRatio() {
        if (ratio == null) {
            ratio = "1280:720";
            return;
        }

        // 이미 올바른 형식이면 그대로 사용
        if (ratio.matches("\\d{3,4}:\\d{3,4}")) {
            return;
        }

        // 비율 형식(16:9 등)을 픽셀 해상도로 변환
        switch (ratio) {
            case "16:9":
                ratio = "1280:720";
                break;
            case "9:16":
                ratio = "720:1280";
                break;
            default:
                // 기본값 사용
                ratio = "1280:720";
                break;
        }
    }

    /**
     * 비디오 길이 (초)
     * 4, 6, 8초 중 선택 가능 (기본값: 6초)
     * Runway API는 4, 6, 8초만 지원하며, 8초가 최대입니다.
     */
    @Builder.Default
    private Integer duration = 8;

    /**
     * 오디오 생성 여부
     */
    @Builder.Default
    private Boolean audio = true;

    /**
     * 요청한 사용자 ID (선택)
     */
    private Long userId;
}
