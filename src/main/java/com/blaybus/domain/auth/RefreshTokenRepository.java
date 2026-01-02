package com.blaybus.domain.auth;

import com.blaybus.domain.user.User;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken token);

    void deleteByUser(User user);
}


