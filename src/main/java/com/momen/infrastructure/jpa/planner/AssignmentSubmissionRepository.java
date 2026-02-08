package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByTodoId(Long todoId);
}
