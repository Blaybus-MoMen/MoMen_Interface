package com.momen.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWT 토큰 Redis 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_REFRESH_TOKEN_PREFIX = "token:user:refresh:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMilliseconds;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMilliseconds;

    // ==================== Refresh Token 관리 ====================

    /**
     * Refresh Token 저장
     * @param userId 사용자 ID
     * @param refreshToken Refresh Token
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        // 사용자별 Refresh Token 저장 (한 사용자당 하나만 유지)
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMillis(refreshTokenValidityInMilliseconds));
        
        // Refresh Token -> UserId 매핑 저장 (토큰으로 사용자 조회용)
        String tokenKey = USER_REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(tokenKey, userId.toString(), Duration.ofMillis(refreshTokenValidityInMilliseconds));
    }

    // Refresh Token 조회
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    // Refresh Token으로 사용자 ID 조회
    public Long getUserIdByRefreshToken(String refreshToken) {
        String key = USER_REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(key);
        return userId != null ? Long.parseLong(userId) : null;
    }

    // Refresh Token 존재 여부 확인
    public boolean existsRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Refresh Token으로 존재 여부 확인
    public boolean existsRefreshTokenByToken(String refreshToken) {
        String key = USER_REFRESH_TOKEN_PREFIX + refreshToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Refresh Token 삭제
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String refreshToken = redisTemplate.opsForValue().get(key);
        
        if (refreshToken != null) {
            // 사용자 ID로 저장된 토큰 삭제
            redisTemplate.delete(key);
            
            // 토큰으로 저장된 사용자 ID 매핑도 삭제
            String tokenKey = USER_REFRESH_TOKEN_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);
        }
    }

    // Refresh Token 삭제 (토큰으로)
    public void deleteRefreshTokenByToken(String refreshToken) {
        String tokenKey = USER_REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(tokenKey);
        
        if (userId != null) {
            // 토큰으로 저장된 사용자 ID 매핑 삭제
            redisTemplate.delete(tokenKey);
            
            // 사용자 ID로 저장된 토큰도 삭제
            String key = REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.delete(key);
        }
    }

    // ==================== Access Token 블랙리스트 관리 ====================

    // Access Token을 블랙리스트에 추가 (로그아웃 시 사용)
    public void addToBlacklist(String accessToken, long expirationTime) {
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
        long ttl = expirationTime - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofMillis(ttl));
        }
    }

    // Access Token 블랙리스트 여부 확인
    public boolean isBlacklisted(String accessToken) {
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Access Token 블랙리스트에서 제거
    public void removeFromBlacklist(String accessToken) {
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken;
        redisTemplate.delete(key);
    }
}

