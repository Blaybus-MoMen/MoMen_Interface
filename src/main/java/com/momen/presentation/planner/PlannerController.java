package com.momen.presentation.planner;

import com.momen.application.planner.PlannerService;
import com.momen.application.planner.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Planner", description = "학습 플래너 API")
@RestController
@RequestMapping("/api/v1/planners")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PlannerController {

    private final PlannerService plannerService;

    @Operation(summary = "플래너 생성", description = "새로운 학습 플래너를 생성합니다")
    @PostMapping
    public ResponseEntity<Long> createPlanner(@RequestAttribute("userId") Long userId, @RequestBody PlannerCreateRequest request) {
        return ResponseEntity.ok(plannerService.createPlanner(userId, request));
    }

    @Operation(summary = "플래너 조회", description = "특정 날짜의 학습 플래너를 조회합니다")
    @GetMapping
    public ResponseEntity<PlannerResponse> getPlanner(@RequestAttribute("userId") Long userId, @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(plannerService.getPlanner(userId, date));
    }

    @Operation(summary = "할 일 추가", description = "플래너에 새로운 할 일을 추가합니다")
    @PostMapping("/{plannerId}/todos")
    public ResponseEntity<Long> addTodo(@RequestAttribute("userId") Long userId,
                                        @Parameter(description = "플래너 ID") @PathVariable Long plannerId,
                                        @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(plannerService.addTodo(userId, plannerId, request));
    }

    @Operation(summary = "할 일 수정", description = "할 일의 완료 여부, 공부 시간을 수정합니다")
    @PatchMapping("/todos/{todoId}")
    public ResponseEntity<Void> updateTodo(@RequestAttribute("userId") Long userId,
                                           @Parameter(description = "할 일 ID") @PathVariable Long todoId,
                                           @RequestBody TodoUpdateRequest request) {
        plannerService.updateTodo(userId, todoId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "플래너 코멘트 수정", description = "오늘의 한마디와 기분 이모지를 수정합니다")
    @PatchMapping("/{plannerId}/comment")
    public ResponseEntity<Void> updateComment(@RequestAttribute("userId") Long userId, @Parameter(description = "플래너 ID") @PathVariable Long plannerId, @RequestBody PlannerCommentUpdateRequest request) {
        plannerService.updatePlannerComment(userId, plannerId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "캘린더 조회", description = "기간별 플래너 캘린더를 조회합니다 (일별 완료율 포함)")
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarDayResponse>> getCalendar(@RequestAttribute("userId") Long userId,
                                                                 @Parameter(description = "시작일 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                 @Parameter(description = "종료일 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(plannerService.getCalendar(userId, startDate, endDate));
    }

    @Operation(summary = "마이페이지 조회", description = "멘티의 프로필, 성취율, 과목별 완료율을 조회합니다")
    @GetMapping("/mypage")
    public ResponseEntity<MypageResponse> getMypage(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(plannerService.getMypage(userId));
    }
}
