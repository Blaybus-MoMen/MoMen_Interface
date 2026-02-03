package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Feedback;
import com.momen.domain.planner.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByMenteeIdAndFeedbackTypeAndStartDate(Long menteeId, FeedbackType feedbackType, LocalDate startDate);

    List<Feedback> findByMenteeIdAndFeedbackTypeOrderByStartDateDesc(Long menteeId, FeedbackType feedbackType);

    List<Feedback> findByMenteeIdAndStartDateBetween(Long menteeId, LocalDate startDate, LocalDate endDate);
}
