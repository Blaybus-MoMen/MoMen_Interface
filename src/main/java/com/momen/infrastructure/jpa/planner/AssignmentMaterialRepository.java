package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.AssignmentMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentMaterialRepository extends JpaRepository<AssignmentMaterial, Long> {
    List<AssignmentMaterial> findByTodoId(Long todoId);
    void deleteByTodoId(Long todoId);
}
