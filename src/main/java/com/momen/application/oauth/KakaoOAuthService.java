package com.momen.application.oauth;

import com.momen.application.oauth.dto.KakaoLoginRequest;
import com.momen.application.oauth.dto.KakaoLoginUrlResponse;
import com.momen.application.oauth.dto.KakaoTokenResponse;
import com.momen.application.oauth.dto.KakaoUserInfo;
import com.momen.application.oauth.dto.OAuthLoginResponse;
import com.momen.core.exception.BusinessException;
import com.momen.core.error.enums.ErrorCode;
import com.momen.domain.user.OAuthProvider;
import com.momen.domain.user.User;
import com.momen.domain.user.UserRepository;
import com.momen.infrastructure.redis.OAuthStateRedisService;
import com.momen.infrastructure.redis.TokenRedisService;
import com.momen.infrastructure.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 카카오 OAuth 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final OAuthStateRedisService oauthStateRedisService;
    private final KakaoApiClient kakaoApiClient;

    private List<String> allowedRedirectUris;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret:}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String defaultRedirectUri;

    @Value("${oauth.kakao.scope:profile_nickname,profile_image,account_email}")
    private String scope;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${oauth.kakao.redirect-uris:}")
    private String redirectUrisComma;

    @PostConstruct
    public void init() {
        this.allowedRedirectUris = buildAllowedRedirectUris();
    }

    private List<String> buildAllowedRedirectUris() {
        if (StringUtils.hasText(redirectUrisComma)) {
            return Arrays.stream(redirectUrisComma.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
        }
        return List.of(defaultRedirectUri);
    }

    private String resolveRedirectUri(String requestRedirectUri) {
        if (StringUtils.hasText(requestRedirectUri)) {
            validateRedirectUri(requestRedirectUri);
            return requestRedirectUri;
        }
        return defaultRedirectUri;
    }

    private void validateRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isBlank()) {
            return;
        }
        String normalized = redirectUri.trim();
        boolean allowed = allowedRedirectUris.stream()
                .anyMatch(uri -> uri.equals(normalized));
        if (!allowed) {
            log.warn("허용되지 않은 redirect_uri 요청: {}", normalized);
            throw new BusinessException(ErrorCode.OAUTH_INVALID_REDIRECT_URI);
        }
    }

    /**
     * 카카오 로그인 처리
     */
    @Transactional
    public OAuthLoginResponse login(KakaoLoginRequest request) {
        // 0. State 검증 (CSRF 방지, 일회성 사용)
        if (!oauthStateRedisService.consumeState(request.getState())) {
            log.warn("유효하지 않거나 만료된 state: {}", request.getState());
            throw new BusinessException(ErrorCode.OAUTH_INVALID_STATE);
        }
        // 1. Redirect URI 검증 및 결정
        String redirectUri = resolveRedirectUri(request.getRedirectUri());
        // 2. 인증 코드로 카카오 액세스 토큰 획득 (Resilience4j Retry/CircuitBreaker 적용)
        KakaoTokenResponse tokenResponse = kakaoApiClient.getToken(request.getCode(), redirectUri);

        // 3. 액세스 토큰으로 사용자 정보 조회 (Resilience4j Retry/CircuitBreaker 적용)
        KakaoUserInfo userInfo = kakaoApiClient.getUserInfo(tokenResponse.getAccessToken());

        // 4. 사용자 조회 또는 생성
        boolean isNewUser = false;
        User user = findOrCreateUser(userInfo);
        if (user.getId() == null) {
            isNewUser = true;
            user = userRepository.save(user);
        }

        // 5. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // 6. Refresh Token Redis에 저장
        tokenRedisService.saveRefreshToken(user.getId(), refreshToken);

        log.info("카카오 로그인 성공: userId={}, email={}, isNewUser={}",
                user.getId(), user.getEmail(), isNewUser);

        return OAuthLoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenValidity)
                .isNewUser(isNewUser)
                .provider(OAuthProvider.KAKAO.getProviderName())
                .build();
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User findOrCreateUser(KakaoUserInfo userInfo) {
        String oauthId = userInfo.getOAuthId();

        // 1. OAuth ID로 기존 사용자 조회
        Optional<User> existingUser = userRepository.findByOauthProviderAndOauthId(
                OAuthProvider.KAKAO, oauthId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 프로필 정보 업데이트
            if (userInfo.getProfileImageUrl() != null) {
                user.updateProfileImageUrl(userInfo.getProfileImageUrl());
            }
            return userRepository.save(user);
        }

        // 2. 이메일로 기존 사용자 조회 (이메일이 있는 경우)
        String email = userInfo.getEmail();
        if (email != null && !email.isEmpty()) {
            Optional<User> userByEmail = userRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                User user = userByEmail.get();
                // 기존 계정에 카카오 연결
                user.connectOAuth(OAuthProvider.KAKAO, oauthId, userInfo.getProfileImageUrl());
                return userRepository.save(user);
            }
        }

        // 3. 신규 사용자 생성
        return User.createOAuthUser(
                email,
                userInfo.getName(),
                OAuthProvider.KAKAO,
                oauthId,
                userInfo.getProfileImageUrl()
        );
    }

    /**
     * 카카오 로그인 URL 생성 (state 포함, CSRF 방지)
     * 클라이언트는 반환된 state를 콜백 후 POST /oauth/kakao/login 시 함께 전달해야 합니다.
     */
    public KakaoLoginUrlResponse getKakaoLoginUrl(String redirectUri) {
        String uri = resolveRedirectUri(redirectUri != null ? redirectUri.trim() : null);
        if (!StringUtils.hasText(uri)) {
            uri = defaultRedirectUri;
        }
        String state = UUID.randomUUID().toString();
        oauthStateRedisService.saveState(state);
        String loginUrl = String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s",
                clientId, uri, scope, state
        );
        return KakaoLoginUrlResponse.builder()
                .loginUrl(loginUrl)
                .state(state)
                .build();
    }

    /**
     * 카카오 연동 해제 (우리 서비스 DB에서만 해제, 카카오 계정 설정은 사용자가 직접 처리)
     */
    @Transactional
    public void unlink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.isOAuthUser() || user.getOauthProvider() != OAuthProvider.KAKAO) {
            throw new BusinessException(ErrorCode.OAUTH_NOT_CONNECTED);
        }
        user.disconnectOAuth();
        userRepository.save(user);
        log.info("카카오 연동 해제: userId={}", userId);
    }
}
