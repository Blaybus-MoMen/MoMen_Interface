package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByTodoId(Long todoId);
}
