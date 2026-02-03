package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByPlannerId(Long plannerId);

    /** 해당 플래너들에 대한 피드백 목록 조회 (주차별 요약용) */
    List<Feedback> findByPlanner_IdIn(Collection<Long> plannerIds);
}
