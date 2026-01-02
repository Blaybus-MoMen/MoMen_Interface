package com.blaybus.presentation.auth;

import com.blaybus.application.auth.EmailVerificationService;
import com.blaybus.core.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Email Verification", description = "이메일 인증 API")
@RestController
@RequestMapping("/api/v1/email-verification")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "인증 코드 발송", description = "이메일로 6자리 인증 코드를 발송합니다")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody SendCodeRequest request) {
        emailVerificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "인증 코드가 발송되었습니다"));
    }

    @Operation(summary = "인증 코드 확인", description = "발송된 인증 코드를 확인하고 이메일 인증을 완료합니다")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@RequestBody VerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증이 완료되었습니다"));
    }

    @Operation(summary = "인증 코드 재발송", description = "인증 코드를 재발송합니다 (1분 간격 제한)")
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(@RequestBody SendCodeRequest request) {
        emailVerificationService.resendVerificationCode(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "인증 코드가 재발송되었습니다"));
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendCodeRequest {
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyCodeRequest {
        private String email;
        private String code;
    }
}
