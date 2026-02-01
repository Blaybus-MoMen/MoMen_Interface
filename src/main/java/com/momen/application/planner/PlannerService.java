package com.momen.application.planner;

import com.momen.application.planner.dto.PlannerCreateRequest;
import com.momen.application.planner.dto.TodoCreateRequest;
import com.momen.application.planner.dto.PlannerResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.Planner;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.PlannerRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final TodoRepository todoRepository;
    private final MenteeRepository menteeRepository;

    // 플래너 생성 (또는 조회)
    @Transactional
    public Long createPlanner(Long userId, PlannerCreateRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        // 이미 있으면 그 ID 반환 (중복 생성 방지)
        return plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), request.getDate())
                .map(Planner::getId)
                .orElseGet(() -> {
                    Planner newPlanner = new Planner(mentee, request.getDate());
                    newPlanner.updateStudentComment(request.getStudentComment(), null);
                    return plannerRepository.save(newPlanner).getId();
                });
    }

    // 날짜별 플래너 조회 (Planner + Todos)
    public PlannerResponse getPlanner(Long userId, LocalDate date) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Planner planner = plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), date)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found for date: " + date));

        List<Todo> todos = todoRepository.findByPlannerId(planner.getId());

        return PlannerResponse.from(planner, todos);
    }

    // 할 일(Todo) 추가
    @Transactional
    public Long addTodo(Long userId, Long plannerId, TodoCreateRequest request) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));
        
        // TODO: 권한 체크 (본인 플래너인지)

        Todo todo = new Todo(
                planner,
                request.getTitle(),
                request.getSubject(),
                request.getGoalDescription(),
                request.getIsFixed(),
                userId // createdBy
        );
        return todoRepository.save(todo).getId();
    }
}
