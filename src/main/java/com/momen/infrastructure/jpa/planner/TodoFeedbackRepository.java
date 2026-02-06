package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.TodoFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TodoFeedbackRepository extends JpaRepository<TodoFeedback, Long> {
    Optional<TodoFeedback> findByTodoId(Long todoId);

    boolean existsByTodoId(Long todoId);

    List<TodoFeedback> findByTodoIdIn(List<Long> todoIds);

    @Query("SELECT tf.todo.id FROM TodoFeedback tf WHERE tf.todo.id IN :todoIds")
    List<Long> findTodoIdsWithFeedback(@Param("todoIds") List<Long> todoIds);
}
