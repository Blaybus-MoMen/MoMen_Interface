package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.WeeklyFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyFeedbackRepository extends JpaRepository<WeeklyFeedback, Long> {

    Optional<WeeklyFeedback> findByMenteeIdAndWeekStartDate(Long menteeId, LocalDate weekStartDate);

    List<WeeklyFeedback> findByMenteeIdOrderByWeekStartDateDesc(Long menteeId);

    @Query("SELECT wf FROM WeeklyFeedback wf WHERE wf.mentee.id = :menteeId " +
           "AND YEAR(wf.weekStartDate) = :year AND MONTH(wf.weekStartDate) = :month " +
           "ORDER BY wf.weekStartDate")
    List<WeeklyFeedback> findByMenteeIdAndYearMonth(@Param("menteeId") Long menteeId,
                                                     @Param("year") int year,
                                                     @Param("month") int month);
}
