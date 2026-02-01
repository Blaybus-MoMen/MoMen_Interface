package com.momen.presentation.planner;

import com.momen.application.planner.PlannerService;
import com.momen.application.planner.dto.PlannerCreateRequest;
import com.momen.application.planner.dto.TodoCreateRequest;
import com.momen.application.planner.dto.PlannerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/planners")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;

    // 플래너 생성 (오늘 공부 시작!)
    @PostMapping
    public ResponseEntity<Long> createPlanner(@RequestAttribute("userId") Long userId, @RequestBody PlannerCreateRequest request) {
        Long plannerId = plannerService.createPlanner(userId, request);
        return ResponseEntity.ok(plannerId);
    }

    // 특정 날짜의 플래너 조회
    @GetMapping
    public ResponseEntity<PlannerResponse> getPlanner(@RequestAttribute("userId") Long userId,
                                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PlannerResponse response = plannerService.getPlanner(userId, date);
        return ResponseEntity.ok(response);
    }

    // 할 일 추가
    @PostMapping("/{plannerId}/todos")
    public ResponseEntity<Long> addTodo(@RequestAttribute("userId") Long userId, @PathVariable Long plannerId,
                                        @RequestBody TodoCreateRequest request) {
        Long todoId = plannerService.addTodo(userId, plannerId, request);
        return ResponseEntity.ok(todoId);
    }
}
