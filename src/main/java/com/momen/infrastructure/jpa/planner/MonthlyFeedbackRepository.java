package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.MonthlyFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyFeedbackRepository extends JpaRepository<MonthlyFeedback, Long> {

    Optional<MonthlyFeedback> findByMenteeIdAndYearAndMonth(Long menteeId, Integer year, Integer month);

    List<MonthlyFeedback> findByMenteeIdOrderByYearDescMonthDesc(Long menteeId);

    List<MonthlyFeedback> findByMenteeIdAndYearOrderByMonthDesc(Long menteeId, Integer year);
}
