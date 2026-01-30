package com.momen.domain.auth;

import com.momen.domain.user.User;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken token);

    void deleteByUser(User user);
}


