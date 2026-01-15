package com.blaybus.infrastructure.jpa.user;

import com.blaybus.domain.user.OAuthProvider;
import com.blaybus.domain.user.User;
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
