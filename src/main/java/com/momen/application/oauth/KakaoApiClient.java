package com.momen.application.oauth;

import com.momen.application.oauth.dto.KakaoTokenResponse;
import com.momen.application.oauth.dto.KakaoUserInfo;
import com.momen.core.exception.BusinessException;
import com.momen.core.error.enums.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 카카오 OAuth API HTTP 클라이언트
 * Resilience4j @Retry, @CircuitBreaker 적용 (외부 API 장애·일시 오류 대응)
 */
@Slf4j
@Component
public class KakaoApiClient {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final int READ_TIMEOUT_SEC = 10;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret:}")
    private String clientSecret;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Retry(name = "kakaoApi")
    @CircuitBreaker(name = "kakaoApi", fallbackMethod = "getTokenFallback")
    public KakaoTokenResponse getToken(String code, String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }

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
    }

    @SuppressWarnings("unused")
    public KakaoTokenResponse getTokenFallback(String code, String redirectUri, Exception e) {
        log.error("카카오 토큰 API 실패 (fallback): {}", e.getMessage());
        throw new BusinessException(ErrorCode.OAUTH_TOKEN_ERROR);
    }

    @Retry(name = "kakaoApi")
    @CircuitBreaker(name = "kakaoApi", fallbackMethod = "getUserInfoFallback")
    public KakaoUserInfo getUserInfo(String accessToken) {
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
    }

    @SuppressWarnings("unused")
    public KakaoUserInfo getUserInfoFallback(String accessToken, Exception e) {
        log.error("카카오 사용자 정보 API 실패 (fallback): {}", e.getMessage());
        throw new BusinessException(ErrorCode.OAUTH_USER_INFO_ERROR);
    }
}
