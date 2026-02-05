package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.TodoFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoFeedbackRepository extends JpaRepository<TodoFeedback, Long> {
    Optional<TodoFeedback> findByTodoId(Long todoId);
}
