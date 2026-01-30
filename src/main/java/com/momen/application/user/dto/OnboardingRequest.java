package com.momen.application.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신규 OAuth 사용자 추가 정보 입력 요청 DTO
 * isNewUser == true 인 경우 전화번호·약관 동의 등을 한 번 받을 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "온보딩(추가 정보) 입력 요청")
public class OnboardingRequest {

    @Schema(description = "전화번호 (선택)")
    private String phone;

    @NotNull(message = "서비스 이용약관 동의는 필수입니다")
    @Schema(description = "서비스 이용약관 동의 여부", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean termsAgreed;

    @Schema(description = "마케팅 수신 동의 여부 (선택)")
    private Boolean marketingAgreed;
}
