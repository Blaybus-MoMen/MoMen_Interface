package com.blaybus.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 이메일 인증번호 Redis 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationRedisService {

    private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:";
    private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
    private static final String EMAIL_ATTEMPT_PREFIX = "email:attempt:";
    private static final int VERIFICATION_CODE_TTL_MINUTES = 10; // 10분
    private static final int VERIFIED_FLAG_TTL_HOURS = 24; // 24시간 (인증 완료 플래그 유지)
    private static final int MAX_ATTEMPTS = 5; // 최대 시도 횟수
    private static final int ATTEMPT_RESET_TTL_MINUTES = 1; // 1분 이내 재발송 방지

    private final StringRedisTemplate redisTemplate;

    // 이메일 인증번호 저장
    public void saveVerificationCode(String email, String code) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(VERIFICATION_CODE_TTL_MINUTES));
    }

    // 이메일 인증번호 조회
    public String getVerificationCode(String email) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }


    // 이메일 인증번호 삭제
    public void deleteVerificationCode(String email) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        redisTemplate.delete(key);
    }

    // 인증번호 존재 여부 확인
    public boolean existsVerificationCode(String email) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 이메일 인증 완료 플래그 설정
    public void setVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(key, "true", Duration.ofHours(VERIFIED_FLAG_TTL_HOURS));
    }

    // 이메일 인증 완료 여부 확인
    public boolean isVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 이메일 인증 완료 플래그 삭제
    public void deleteVerified(String email) {
        String key = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.delete(key);
    }

    // 인증 시도 횟수 증가
    public int incrementAttempt(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            // 첫 시도 시 TTL 설정 (인증번호와 동일하게 10분)
            redisTemplate.expire(key, Duration.ofMinutes(VERIFICATION_CODE_TTL_MINUTES));
        }
        return count != null ? count.intValue() : 0;
    }

    // 인증 시도 횟수 조회
    public int getAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    // 인증 시도 횟수 초과 여부 확인
    public boolean isAttemptsExceeded(String email) {
        return getAttemptCount(email) >= MAX_ATTEMPTS;
    }

    //  인증 시도 횟수 초기화 (인증 성공 시)
    public void resetAttempt(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        redisTemplate.delete(key);
    }

    //  최근 발송 시간 확인 (1분 이내 재발송 방지)
    public boolean isRecentlySent(String email) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        // TTL이 9분 이상이면 1분 이내에 발송된 것
        return ttl != null && ttl > (VERIFICATION_CODE_TTL_MINUTES - 1) * 60;
    }

    //  인증번호 검증
    public boolean verifyCode(String email, String code) {
        String storedCode = getVerificationCode(email);
        if (storedCode == null) {
            return false;
        }
        return storedCode.equals(code);
    }
}
