package com.momen.presentation.oauth;

import com.momen.application.oauth.KakaoOAuthService;
import com.momen.application.oauth.OAuthConnectionService;
import com.momen.application.oauth.dto.KakaoLoginRequest;
import com.momen.application.oauth.dto.KakaoLoginUrlResponse;
import com.momen.application.oauth.dto.OAuthConnectionsResponse;
import com.momen.application.oauth.dto.OAuthLoginResponse;
import com.momen.core.dto.response.ApiResponse;
import com.momen.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    private final OAuthConnectionService oauthConnectionService;

    // 소셜 연동 상태 조회 (인증 필요)
    @GetMapping("/connections")
    @Operation(summary = "소셜 연동 상태 조회", description = "현재 계정에 연결된 소셜 로그인(카카오, Google, Naver) 연동 상태를 조회합니다. 설정 화면용.")
    public ResponseEntity<ApiResponse<OAuthConnectionsResponse>> getConnections(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        OAuthConnectionsResponse response = oauthConnectionService.getConnections(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

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
    @Operation(summary = "카카오 로그인 URL 조회", description = "카카오 로그인 페이지 URL과 state를 반환합니다. 콜백 후 POST /kakao/login 시 state를 함께 전달해야 합니다.")
    public ResponseEntity<ApiResponse<KakaoLoginUrlResponse>> getKakaoLoginUrl(@Parameter(description = "리다이렉트 URI (허용 목록에 등록된 URI만 가능)") @RequestParam(required = false) String redirectUri) {
        KakaoLoginUrlResponse response = kakaoOAuthService.getKakaoLoginUrl(redirectUri);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 카카오 콜백 (프론트엔드 없이 테스트용)
    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 콜백 (테스트용)", description = "카카오 OAuth 콜백을 처리합니다. code와 state는 카카오가 리다이렉트 시 쿼리로 전달합니다.")
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> kakaoCallback(@Parameter(description = "카카오 인증 코드") @RequestParam String code,
                                                                         @Parameter(description = "CSRF 방지용 state (로그인 URL 발급 시 받은 값)") @RequestParam String state,
                                                                         @Parameter(description = "에러 (있는 경우)") @RequestParam(required = false) String error,
                                                                         @Parameter(description = "에러 설명 (있는 경우)") @RequestParam(required = false, name = "error_description") String errorDescription) {
        // 카카오 로그인 에러 처리
        if (error != null) {
            log.error("카카오 로그인 에러: {} - {}", error, errorDescription);
            return ResponseEntity.badRequest().body(ApiResponse.error(error + ": " + errorDescription, "OAUTH_ERROR"));
        }

        log.info("카카오 콜백 수신: code={}", code.substring(0, Math.min(10, code.length())) + "...");

        KakaoLoginRequest request = new KakaoLoginRequest(code, state, null);
        OAuthLoginResponse response = kakaoOAuthService.login(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 카카오 연동 해제 (인증 필요)
    @PostMapping("/kakao/unlink")
    @Operation(summary = "카카오 연동 해제", description = "현재 계정에서 카카오 로그인 연동을 해제합니다. 인증이 필요합니다.")
    public ResponseEntity<ApiResponse<Void>> kakaoUnlink(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        kakaoOAuthService.unlink(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
