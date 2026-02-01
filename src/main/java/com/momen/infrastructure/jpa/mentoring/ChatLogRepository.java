package com.momen.infrastructure.jpa.mentoring;

import com.momen.domain.mentoring.MentoringChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatLogRepository extends JpaRepository<MentoringChatLog, Long> {
    List<MentoringChatLog> findByMenteeIdOrderByCreateDtDesc(Long menteeId);
}
