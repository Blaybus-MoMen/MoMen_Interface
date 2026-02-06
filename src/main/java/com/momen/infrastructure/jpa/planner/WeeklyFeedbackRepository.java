package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.WeeklyFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyFeedbackRepository extends JpaRepository<WeeklyFeedback, Long> {

    Optional<WeeklyFeedback> findByMenteeIdAndWeekStartDate(Long menteeId, LocalDate weekStartDate);

    List<WeeklyFeedback> findByMenteeIdOrderByWeekStartDateDesc(Long menteeId);

    // 해당 월 달력에 보이는 주간 피드백 조회 (크로스월 포함)
    List<WeeklyFeedback> findByMenteeIdAndWeekStartDateBetweenOrderByWeekStartDate(
            Long menteeId, LocalDate from, LocalDate to);
}
