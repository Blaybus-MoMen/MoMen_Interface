package com.momen.infrastructure.jpa.user;

import com.momen.domain.user.OAuthProvider;
import com.momen.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User JPA Repository
 */
public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);
}
