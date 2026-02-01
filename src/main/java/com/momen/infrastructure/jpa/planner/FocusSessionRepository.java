package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    List<FocusSession> findByMenteeIdOrderByStartTimeDesc(Long menteeId);
}
