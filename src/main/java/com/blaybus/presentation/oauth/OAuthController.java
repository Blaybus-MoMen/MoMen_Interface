package com.blaybus.presentation.oauth;

import com.blaybus.application.oauth.KakaoOAuthService;
import com.blaybus.application.oauth.dto.KakaoLoginRequest;
import com.blaybus.application.oauth.dto.OAuthLoginResponse;
import com.blaybus.core.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OAuth 소셜 로그인 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth", description = "소셜 로그인 API")
public class OAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    // 카카오 로그인
    @PostMapping("/kakao/login")
    @Operation(summary = "카카오 로그인", description = "카카오 인증 코드로 로그인합니다")
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {

        log.info("카카오 로그인 요청");
        OAuthLoginResponse response = kakaoOAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 카카오 로그인 URL 조회
    @GetMapping("/kakao/login-url")
    @Operation(summary = "카카오 로그인 URL 조회", description = "카카오 로그인 페이지 URL을 반환합니다")
    public ResponseEntity<ApiResponse<Map<String, String>>> getKakaoLoginUrl(@Parameter(description = "리다이렉트 URI (선택)") @RequestParam(required = false) String redirectUri) {
        String loginUrl = kakaoOAuthService.getKakaoLoginUrl(redirectUri);
        return ResponseEntity.ok(ApiResponse.success(Map.of("loginUrl", loginUrl)));
    }

    // 카카오 콜백 (프론트엔드 없이 테스트용)
    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 콜백 (테스트용)", description = "카카오 OAuth 콜백을 처리합니다")
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> kakaoCallback(@Parameter(description = "카카오 인증 코드") @RequestParam String code,
                                                                         @Parameter(description = "에러 (있는 경우)") @RequestParam(required = false) String error,
                                                                         @Parameter(description = "에러 설명 (있는 경우)") @RequestParam(required = false, name = "error_description") String errorDescription) {
        // 카카오 로그인 에러 처리
        if (error != null) {
            log.error("카카오 로그인 에러: {} - {}", error, errorDescription);
            return ResponseEntity.badRequest().body(ApiResponse.error(error + ": " + errorDescription, "OAUTH_ERROR"));
        }

        log.info("카카오 콜백 수신: code={}", code.substring(0, Math.min(10, code.length())) + "...");

        KakaoLoginRequest request = new KakaoLoginRequest(code, null);
        OAuthLoginResponse response = kakaoOAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
