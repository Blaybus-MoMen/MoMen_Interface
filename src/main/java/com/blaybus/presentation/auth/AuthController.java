package com.blaybus.presentation.auth;

import com.blaybus.application.auth.AuthService;
import com.blaybus.application.auth.dto.LoginRequest;
import com.blaybus.application.auth.dto.RefreshTokenRequest;
import com.blaybus.application.auth.dto.SignupRequest;
import com.blaybus.application.auth.dto.TokenResponse;
import com.blaybus.core.controller.BaseController;
import com.blaybus.core.dto.response.ApiResponse;
import com.blaybus.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API Controller
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    // 회원가입
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        TokenResponse response = authService.signup(request);

        return ApiResponse.success(response, "회원가입이 완료되었습니다");
    }

    // 로그인
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);

        return ApiResponse.success(response, "로그인 성공");
    }

    // Refresh Token을 사용하여 Access Token 재발급
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refresh(request.getRefreshToken());

        return ApiResponse.success(response, "토큰 재발급 성공");
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "로그아웃하여 Refresh Token을 삭제하고 Access Token을 블랙리스트에 추가합니다")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestHeader(value = "Authorization", required = false) String authorization) {
        String accessToken = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        authService.logout(userDetails.getUserId(), accessToken);

        return ApiResponse.success(null, "로그아웃되었습니다");
    }
}
