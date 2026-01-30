package com.momen.domain.openai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OpenAI ChatGPT 대화 로그 리포지토리
 */
@Repository
public interface OpenAIChatLogRepository extends JpaRepository<OpenAIChatLog, Long> {

    // Job ID로 조회
    Optional<OpenAIChatLog> findByJobId(String jobId);

    // 사용자 ID로 조회
    List<OpenAIChatLog> findByUserId(Long userId);

    // 특정 상태의 로그들 조회
    List<OpenAIChatLog> findByStatus(OpenAIChatStatus status);

    // 사용자 ID와 상태로 조회
    List<OpenAIChatLog> findByUserIdAndStatus(Long userId, OpenAIChatStatus status);
}
