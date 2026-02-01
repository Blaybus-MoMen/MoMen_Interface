package com.momen.presentation.planner;

import com.momen.application.planner.PlannerService;
import com.momen.application.planner.dto.PlannerCreateRequest;
import com.momen.application.planner.dto.TodoCreateRequest;
import com.momen.application.planner.dto.PlannerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Planner", description = "학습 플래너 API")
@RestController
@RequestMapping("/api/v1/planners")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PlannerController {

    private final PlannerService plannerService;

    @Operation(summary = "플래너 생성", description = "새로운 학습 플래너를 생성합니다")
    @PostMapping
    public ResponseEntity<Long> createPlanner(
            @RequestAttribute("userId") Long userId,
            @RequestBody PlannerCreateRequest request) {
        Long plannerId = plannerService.createPlanner(userId, request);
        return ResponseEntity.ok(plannerId);
    }

    @Operation(summary = "플래너 조회", description = "특정 날짜의 학습 플래너를 조회합니다")
    @GetMapping
    public ResponseEntity<PlannerResponse> getPlanner(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PlannerResponse response = plannerService.getPlanner(userId, date);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할 일 추가", description = "플래너에 새로운 할 일을 추가합니다")
    @PostMapping("/{plannerId}/todos")
    public ResponseEntity<Long> addTodo(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "플래너 ID") @PathVariable Long plannerId,
            @RequestBody TodoCreateRequest request) {
        Long todoId = plannerService.addTodo(userId, plannerId, request);
        return ResponseEntity.ok(todoId);
    }
}
