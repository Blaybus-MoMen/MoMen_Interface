package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.AssignmentMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AssignmentMaterialRepository extends JpaRepository<AssignmentMaterial, Long> {
    List<AssignmentMaterial> findByTodoId(Long todoId);
    List<AssignmentMaterial> findByTodoIdIn(Collection<Long> todoIds); // 멘티화면 리스트 보여주는거
    void deleteByTodoId(Long todoId);
}
