package com.blaybus.application.oauth;

import com.blaybus.application.oauth.dto.KakaoLoginRequest;
import com.blaybus.application.oauth.dto.KakaoTokenResponse;
import com.blaybus.application.oauth.dto.KakaoUserInfo;
import com.blaybus.application.oauth.dto.OAuthLoginResponse;
import com.blaybus.core.exception.BusinessException;
import com.blaybus.core.error.enums.ErrorCode;
import com.blaybus.domain.user.OAuthProvider;
import com.blaybus.domain.user.User;
import com.blaybus.domain.user.UserRepository;
import com.blaybus.infrastructure.redis.TokenRedisService;
import com.blaybus.infrastructure.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

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

    private WebClient webClient;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret:}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String defaultRedirectUri;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * 카카오 로그인 처리
     */
    @Transactional
    public OAuthLoginResponse login(KakaoLoginRequest request) {
        // 1. 인증 코드로 카카오 액세스 토큰 획득
        String redirectUri = request.getRedirectUri() != null ? request.getRedirectUri() : defaultRedirectUri;
        KakaoTokenResponse tokenResponse = getKakaoToken(request.getCode(), redirectUri);

        // 2. 액세스 토큰으로 사용자 정보 조회
        KakaoUserInfo userInfo = getKakaoUserInfo(tokenResponse.getAccessToken());

        // 3. 사용자 조회 또는 생성
        boolean isNewUser = false;
        User user = findOrCreateUser(userInfo);
        if (user.getId() == null) {
            isNewUser = true;
            user = userRepository.save(user);
        }

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // 5. Refresh Token Redis에 저장
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
     * 카카오 액세스 토큰 획득
     */
    private KakaoTokenResponse getKakaoToken(String code, String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }

        try {
            KakaoTokenResponse response = webClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null) {
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_ERROR);
            }

            return response;
        } catch (Exception e) {
            log.error("카카오 토큰 획득 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_ERROR);
        }
    }

    /**
     * 카카오 사용자 정보 조회
     */
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        try {
            KakaoUserInfo userInfo = webClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfo.class)
                    .block();

            if (userInfo == null || userInfo.getId() == null) {
                throw new BusinessException(ErrorCode.OAUTH_USER_INFO_ERROR);
            }

            return userInfo;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_ERROR);
        }
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
     * 카카오 로그인 URL 생성
     */
    public String getKakaoLoginUrl(String redirectUri) {
        String uri = redirectUri != null ? redirectUri : defaultRedirectUri;
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                clientId, uri
        );
    }
}
