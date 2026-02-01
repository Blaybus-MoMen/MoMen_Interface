package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Planner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PlannerRepository extends JpaRepository<Planner, Long> {
    Optional<Planner> findByMenteeIdAndPlannerDate(Long menteeId, LocalDate date);
}
