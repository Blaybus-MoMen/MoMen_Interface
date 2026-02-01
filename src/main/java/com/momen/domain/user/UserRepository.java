package com.momen.domain.user;

import java.util.List;
import java.util.Optional;

/**
 * User Repository 인터페이스
 * Domain Layer에서는 인터페이스만 정의
 * 구현체는 Infrastructure Layer에서 제공
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void delete(User user);

    List<User> findAll();
}
