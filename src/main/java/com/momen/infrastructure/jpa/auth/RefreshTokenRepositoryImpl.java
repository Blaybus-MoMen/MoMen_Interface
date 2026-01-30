package com.momen.infrastructure.jpa.auth;

import com.momen.domain.auth.RefreshToken;
import com.momen.domain.auth.RefreshTokenRepository;
import com.momen.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpaRepository.save(token);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token);
    }

    @Override
    public void delete(RefreshToken token) {
        jpaRepository.delete(token);
    }

    @Override
    public void deleteByUser(User user) {
        jpaRepository.deleteByUser(user);
    }
}


