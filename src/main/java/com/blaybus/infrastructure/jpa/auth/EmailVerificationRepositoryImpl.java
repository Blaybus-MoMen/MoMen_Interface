package com.blaybus.infrastructure.jpa.auth;

import com.blaybus.domain.auth.EmailVerification;
import com.blaybus.domain.auth.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;

    @Override
    public EmailVerification save(EmailVerification verification) {
        return jpaRepository.save(verification);
    }

    @Override
    public Optional<EmailVerification> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<EmailVerification> findByEmailAndCode(String email, String code) {
        return jpaRepository.findByEmailAndCode(email, code);
    }

    @Override
    public Optional<EmailVerification> findTopByEmailOrderByCreateDtDesc(String email) {
        return jpaRepository.findTopByEmailOrderByCreateDtDesc(email);
    }

    @Override
    public List<EmailVerification> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public void delete(EmailVerification verification) {
        jpaRepository.delete(verification);
    }
}
