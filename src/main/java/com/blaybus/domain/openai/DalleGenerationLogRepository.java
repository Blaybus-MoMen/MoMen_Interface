package com.blaybus.domain.openai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DALL-E 이미지 생성 로그 리포지토리
 */
@Repository
public interface DalleGenerationLogRepository extends JpaRepository<DalleGenerationLog, Long> {

    // Job ID로 조회
    Optional<DalleGenerationLog> findByJobId(String jobId);

    // 사용자 ID로 조회
    List<DalleGenerationLog> findByUserId(Long userId);

    // 특정 상태의 로그들 조회
    List<DalleGenerationLog> findByStatus(DalleGenerationStatus status);

    // 사용자 ID와 상태로 조회
    List<DalleGenerationLog> findByUserIdAndStatus(Long userId, DalleGenerationStatus status);
}
