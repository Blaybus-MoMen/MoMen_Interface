package com.momen.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * OAuth State(CSRF 방지) Redis 저장 서비스
 * State는 일회성으로 검증 후 삭제됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthStateRedisService {

    private static final String OAUTH_STATE_PREFIX = "oauth:state:";

    private final StringRedisTemplate redisTemplate;

    @Value("${oauth.kakao.state-validity-seconds:300}")
    private long stateValiditySeconds;

    /**
     * State 저장 (TTL 적용)
     */
    public void saveState(String state) {
        String key = OAUTH_STATE_PREFIX + state;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(stateValiditySeconds));
    }

    /**
     * State 검증 후 삭제 (일회성 사용)
     * @return true면 유효한 state였고 삭제됨, false면 없거나 이미 사용됨
     */
    public boolean consumeState(String state) {
        if (state == null || state.isBlank()) {
            return false;
        }
        String key = OAUTH_STATE_PREFIX + state;
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }
}
