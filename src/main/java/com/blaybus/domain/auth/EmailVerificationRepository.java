package com.blaybus.domain.auth;

import java.util.List;
import java.util.Optional;

/**
 * EmailVerification Repository 인터페이스
 */
public interface EmailVerificationRepository {

    // 이메일 인증 저장
    EmailVerification save(EmailVerification verification);

    // ID로 이메일 인증 조회
    Optional<EmailVerification> findById(Long id);

    // 이메일과 코드로 조회
    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    // 이메일로 가장 최근 인증 조회
    Optional<EmailVerification> findTopByEmailOrderByCreateDtDesc(String email);

    // 이메일로 인증 목록 조회
    List<EmailVerification> findByEmail(String email);

    // 이메일 인증 삭제
    void delete(EmailVerification verification);
}
