package com.blaybus.infrastructure.jpa.auth;

import com.blaybus.domain.auth.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * EmailVerification JPA Repository
 */
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    Optional<EmailVerification> findTopByEmailOrderByCreateDtDesc(String email);

    List<EmailVerification> findByEmail(String email);
}
