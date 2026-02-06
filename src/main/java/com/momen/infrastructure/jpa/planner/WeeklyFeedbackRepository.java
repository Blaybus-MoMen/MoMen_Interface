package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.WeeklyFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeeklyFeedbackRepository extends JpaRepository<WeeklyFeedback, Long> {

    Optional<WeeklyFeedback> findByMenteeIdAndYearAndMonthAndWeek(Long menteeId, Integer year, Integer month, Integer week);

    List<WeeklyFeedback> findByMenteeIdOrderByYearDescMonthDescWeekDesc(Long menteeId);

    List<WeeklyFeedback> findByMenteeIdAndYearAndMonthOrderByWeek(Long menteeId, Integer year, Integer month);
}
