package com.blaybus.domain.auth;

import com.blaybus.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 인증 엔티티
 */
@Entity
@Table(name = "tbl_email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VERIFICATION_ID")
    private Long id;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "CODE", nullable = false, length = 6)
    private String code;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "VERIFIED", nullable = false)
    private Boolean verified;

    @Column(name = "VERIFIED_AT")
    private LocalDateTime verifiedAt;

    @Column(name = "ATTEMPT_COUNT", nullable = false)
    private Integer attemptCount;

    @Builder
    public EmailVerification(String email, String code, LocalDateTime expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.attemptCount = 0;
    }

    /**
     * 인증 시도 증가
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }

    /**
     * 인증 완료 처리
     */
    public void verify() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 인증 코드 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 인증 시도 횟수 초과 여부 (최대 5회)
     */
    public boolean isAttemptsExceeded() {
        return this.attemptCount >= 5;
    }

    /**
     * 코드 일치 여부 확인
     */
    public boolean isCodeMatch(String inputCode) {
        return this.code.equals(inputCode);
    }
}
