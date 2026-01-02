package com.blaybus.domain.user;

import java.util.List;
import java.util.Optional;

/**
 * User Repository 인터페이스
 * Domain Layer에서는 인터페이스만 정의
 * 구현체는 Infrastructure Layer에서 제공
 */
public interface UserRepository {

    // 사용자 저장
    User save(User user);

    // ID로 사용자 조회
    Optional<User> findById(Long id);

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 사용자 삭제
    void delete(User user);

    // 모든 사용자 조회
    List<User> findAll();
}
