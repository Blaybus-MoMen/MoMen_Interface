package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByPlannerId(Long plannerId);
}
