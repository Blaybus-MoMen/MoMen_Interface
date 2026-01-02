package com.blaybus.infrastructure.jpa.auth;

import com.blaybus.domain.auth.RefreshToken;
import com.blaybus.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}


