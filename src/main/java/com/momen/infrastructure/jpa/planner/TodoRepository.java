package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByPlannerId(Long plannerId);

    List<Todo> findByPlannerIdIn(List<Long> plannerIds);

    List<Todo> findByParentTodoId(Long parentTodoId);

    @Query("SELECT t FROM Todo t WHERE t.parentTodo.id = :parentId OR t.id = :parentId")
    List<Todo> findRecurringGroup(@Param("parentId") Long parentId);

    void deleteByPlannerId(Long plannerId);
}
