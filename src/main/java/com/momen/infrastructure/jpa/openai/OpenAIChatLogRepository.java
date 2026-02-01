package com.momen.infrastructure.jpa.openai;

import com.momen.domain.openai.OpenAIChatLog;
import com.momen.domain.openai.OpenAIChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OpenAI ChatGPT 대화 로그 리포지토리
 */
@Repository
public interface OpenAIChatLogRepository extends JpaRepository<OpenAIChatLog, Long> {

    Optional<OpenAIChatLog> findByJobId(String jobId);

    List<OpenAIChatLog> findByUserId(Long userId);

    List<OpenAIChatLog> findByStatus(OpenAIChatStatus status);

    List<OpenAIChatLog> findByUserIdAndStatus(Long userId, OpenAIChatStatus status);
}
