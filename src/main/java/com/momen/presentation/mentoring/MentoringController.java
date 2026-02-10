package com.momen.presentation.mentoring;

import com.momen.application.mentoring.MentoringChatService;
import com.momen.application.mentoring.MentoringService;
import com.momen.application.mentoring.dto.ChatRequest;
import com.momen.application.mentoring.dto.MenteeResponse;
import com.momen.application.planner.AssignmentService;
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

@Tag(name = "Mentoring", description = "멘토링 API")
@RestController
@RequestMapping("/api/v1/mentoring")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MentoringController {

    private final MentoringChatService chatService;
    private final MentoringService mentoringService;
    private final TodoService todoService;
    private final AssignmentService assignmentService;

    @Operation(summary = "담당 멘티 목록 조회", description = "멘토가 담당하는 멘티 목록을 조회합니다")
    @GetMapping("/mentees")
    public ResponseEntity<ApiResponse<List<MenteeResponse.MenteeForMentorResponse>>> getMenteeList(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(mentoringService.getMenteeList(userId)));
    }

    @Operation(summary = "멘티 단건 조회", description = "멘토가 담당 멘티 한 명의 정보를 조회합니다")
    @GetMapping("/mentees/{menteeId}")
    public ResponseEntity<ApiResponse<MenteeResponse.MenteeForMentorResponse>> getMentee(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId) {
        return ResponseEntity.ok(ApiResponse.ok(mentoringService.getMentee(userId, menteeId)));
    }

    // ==================== Todo CRUD ====================

    @Operation(summary = "Todo 생성", description = "멘토가 멘티에게 할일을 등록합니다 (단건/반복)")
    @PostMapping("/mentees/{menteeId}/todos")
    public ResponseEntity<ApiResponse<List<Long>>> createTodo(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.createTodo(userId, menteeId, request)));
    }

    @Operation(summary = "Todo 수정", description = "멘토가 할일을 수정합니다")
    @PatchMapping("/mentees/{menteeId}/todos/{todoId}")
    public ResponseEntity<ApiResponse<Void>> updateTodo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @PathVariable Long todoId,
            @RequestBody TodoUpdateRequest request) {
        todoService.updateTodoByMentor(userId, todoId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Todo 삭제", description = "멘토가 할일을 삭제합니다")
    @DeleteMapping("/mentees/{menteeId}/todos/{todoId}")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @PathVariable Long todoId) {
        todoService.deleteTodo(userId, todoId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Todo 확인 토글", description = "멘토가 할일의 확인 상태를 변경합니다 (on/off)")
    @PatchMapping("/mentees/{menteeId}/todos/{todoId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmTodo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @PathVariable Long todoId,
            @RequestBody TodoConfirmRequest request) {
        todoService.confirmTodo(userId, todoId, request.getConfirmed());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== Todo 조회 ====================

    @Operation(summary = "멘티 Todo 조회", description = "멘토가 특정 멘티의 할일을 조회합니다. date(일별), weekStartDate(주별), yearMonth(월별) 중 하나를 선택하여 조회합니다. 우선순위: date > weekStartDate > yearMonth")
    @GetMapping(value = "/mentees/{menteeId}/todos")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getTodos(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "주 시작일-일요일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam(required = false) String yearMonth,
            @Parameter(description = "과목 필터") @RequestParam(required = false) List<String> subjects) {
        if (date != null) {
            return ResponseEntity.ok(ApiResponse.ok(todoService.getTodosForMenteeByDate(userId, menteeId, date, subjects)));
        }
        if (weekStartDate != null) {
            return ResponseEntity.ok(ApiResponse.ok(todoService.getTodosForMenteeByWeek(userId, menteeId, weekStartDate, subjects)));
        }
        if (yearMonth != null) {
            return ResponseEntity.ok(ApiResponse.ok(todoService.getTodosForMenteeByMonth(userId, menteeId, yearMonth, subjects)));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("date, weekStartDate, yearMonth 중 하나는 필수입니다", "MISSING_PARAM"));
    }

    @Operation(summary = "Todo 상세 조회", description = "할일의 상세 정보를 조회합니다 (자료파일 포함)")
    @GetMapping("/todos/{todoId}")
    public ResponseEntity<ApiResponse<TodoDetailResponse>> getTodoDetail(@PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getTodoDetail(todoId)));
    }

    @Operation(summary = "Todo별 제출물 조회", description = "특정 할일에 제출된 과제를 조회합니다")
    @GetMapping("/todos/{todoId}/submission")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> getSubmission(@PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getSubmissionByTodo(todoId)));
    }

    // ==================== AI 튜터 ====================

    @Operation(summary = "AI 튜터 채팅", description = "멘티가 AI 튜터와 채팅합니다")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chatWithAi(
            @RequestAttribute("userId") Long userId,
            @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.chatWithAiTutor(userId, request.getMessage())));
    }
}
