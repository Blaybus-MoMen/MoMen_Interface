package com.momen.presentation.planner;

import com.momen.application.planner.AssignmentService;
import com.momen.application.planner.MistakeNoteService;
import com.momen.application.planner.PlannerService;
import com.momen.application.planner.TodoService;
import com.momen.application.planner.dto.*;
import com.momen.core.dto.response.ApiResponse;
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

@Tag(name = "Study", description = "학습 과제 및 오답노트 API (멘티용)")
@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudyController {

    private final AssignmentService assignmentService;
    private final MistakeNoteService mistakeNoteService;
    private final TodoService todoService;
    private final PlannerService plannerService;

    // ==================== Todo 조회 (멘티용) ====================

    @Operation(summary = "일별 Todo 조회", description = "멘티 본인의 특정 날짜 할일을 조회합니다")
    @GetMapping(value = "/todos", params = "date")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getMyTodosByDate(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getMyTodosByDate(userId, date)));
    }

    @Operation(summary = "월별 Todo 조회", description = "멘티 본인의 월별 할일을 조회합니다")
    @GetMapping(value = "/todos", params = "yearMonth")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getMyTodosByMonth(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getMyTodosByMonth(userId, yearMonth)));
    }

    @Operation(summary = "Todo 상세 조회", description = "할일의 상세 정보를 조회합니다 (자료파일 포함)")
    @GetMapping("/todos/{todoId}")
    public ResponseEntity<ApiResponse<TodoDetailResponse>> getTodoDetail(@PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getTodoDetail(todoId)));
    }

    @Operation(summary = "학습 카드 목록 조회 (일별)", description = "해당 날짜의 학습 할일을 카드 표시용 배열로 조회합니다 (진행상태, 학습지 포함)")
    @GetMapping(value = "/todos/cards", params = "date")
    public ResponseEntity<ApiResponse<List<TodoDetailResponse>>> getMyTodoCardsByDate(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getMyTodoCardsByDate(userId, date)));
    }

    @Operation(summary = "학습 카드 목록 조회 (월별)", description = "해당 월의 학습 할일을 카드 표시용 배열로 조회합니다 (진행상태, 학습지 포함)")
    @GetMapping(value = "/todos/cards", params = "yearMonth")
    public ResponseEntity<ApiResponse<List<TodoDetailResponse>>> getMyTodoCardsByMonth(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getMyTodoCardsByMonth(userId, yearMonth)));
    }

    @Operation(summary = "Todo 완료/공부시간 기록", description = "멘티가 할일 완료 처리 및 공부 시간을 기록합니다")
    @PatchMapping("/todos/{todoId}")
    public ResponseEntity<ApiResponse<Void>> updateMyTodo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long todoId,
            @RequestBody TodoUpdateRequest request) {
        todoService.updateTodoByMentee(userId, todoId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== 과제 제출 ====================

    @Operation(summary = "학습 점검하기 (과제 제출 + 학습 완료)", description = "파일·텍스트(메모)를 제출하면 해당 할일이 학습 완료로 표시됩니다. 파일 첨부 시 AI Vision 자동 검수가 진행됩니다.")
    @PostMapping("/todos/{todoId}/submit")
    public ResponseEntity<ApiResponse<Long>> submitAssignment(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "할 일 ID") @PathVariable Long todoId,
            @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.submitAssignment(userId, todoId, request)));
    }

    @Operation(summary = "제출물 상세 조회", description = "과제 제출물의 상세 정보와 AI 분석 결과를 조회합니다")
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> getSubmission(
            @Parameter(description = "제출물 ID") @PathVariable Long submissionId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getSubmission(submissionId)));
    }

    @Operation(summary = "Todo별 제출 목록 조회", description = "특정 할일에 대한 제출 목록을 조회합니다")
    @GetMapping("/todos/{todoId}/submissions")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionResponse>>> getSubmissionsByTodo(
            @Parameter(description = "할 일 ID") @PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getSubmissionsByTodo(todoId)));
    }

    // ==================== 오답노트 ====================

    @Operation(summary = "오답노트 생성", description = "오답노트를 생성하고 AI 변형 문제 생성을 시작합니다")
    @PostMapping("/mistake-notes")
    public ResponseEntity<ApiResponse<Long>> createMistakeNote(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "할 일 ID") @RequestParam Long todoId,
            @Parameter(description = "오답 이미지 URL") @RequestParam String imageUrl) {
        return ResponseEntity.ok(ApiResponse.ok(mistakeNoteService.createMistakeNote(userId, todoId, imageUrl)));
    }

    // ==================== 마이페이지 ====================

    @Operation(summary = "마이페이지 조회", description = "멘티의 프로필, 성취율, 과목별 완료율을 조회합니다")
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MypageResponse>> getMypage(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(plannerService.getMypage(userId)));
    }
}
