package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByPlannerId(Long plannerId);

    List<Todo> findByPlannerIdIn(List<Long> plannerIds);

    void deleteByPlannerId(Long plannerId);
}
