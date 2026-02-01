package com.momen.infrastructure.jpa.openai;

import com.momen.domain.openai.DalleGenerationLog;
import com.momen.domain.openai.DalleGenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DALL-E 이미지 생성 로그 리포지토리
 */
@Repository
public interface DalleGenerationLogRepository extends JpaRepository<DalleGenerationLog, Long> {

    Optional<DalleGenerationLog> findByJobId(String jobId);

    List<DalleGenerationLog> findByUserId(Long userId);

    List<DalleGenerationLog> findByStatus(DalleGenerationStatus status);

    List<DalleGenerationLog> findByUserIdAndStatus(Long userId, DalleGenerationStatus status);
}
