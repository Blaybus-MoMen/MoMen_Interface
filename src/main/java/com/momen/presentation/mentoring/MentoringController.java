package com.momen.presentation.mentoring;

import com.momen.application.mentoring.MentoringChatService;
import com.momen.application.mentoring.MentoringService;
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
import java.util.Map;

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
    public ResponseEntity<ApiResponse<List<MenteeResponse>>> getMenteeList(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(mentoringService.getMenteeList(userId)));
    }

    @Operation(summary = "멘티 단건 조회", description = "멘토가 담당 멘티 한 명의 정보를 조회합니다")
    @GetMapping("/mentees/{menteeId}")
    public ResponseEntity<ApiResponse<MenteeResponse>> getMentee(
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

    @Operation(summary = "멘티 일별 Todo 조회", description = "멘토가 특정 멘티의 특정 날짜 할일을 조회합니다")
    @GetMapping(value = "/mentees/{menteeId}/todos", params = "date")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getTodosByDate(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getTodosForMenteeByDate(userId, menteeId, date)));
    }

    @Operation(summary = "멘티 월별 Todo 조회", description = "멘토가 특정 멘티의 월별 할일을 조회합니다")
    @GetMapping(value = "/mentees/{menteeId}/todos", params = "yearMonth")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getTodosByMonth(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getTodosForMenteeByMonth(userId, menteeId, yearMonth)));
    }

    @Operation(summary = "멘티 주별 Todo 조회", description = "멘토가 특정 멘티의 특정 주차 할일을 조회합니다 (일요일~토요일)")
    @GetMapping(value = "/mentees/{menteeId}/todos", params = "weekStartDate")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getTodosByWeek(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @Parameter(description = "주 시작일-일요일 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getTodosForMenteeByWeek(userId, menteeId, weekStartDate)));
    }

    @Operation(summary = "Todo 상세 조회", description = "할일의 상세 정보를 조회합니다 (자료파일 포함)")
    @GetMapping("/todos/{todoId}")
    public ResponseEntity<ApiResponse<TodoDetailResponse>> getTodoDetail(@PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getTodoDetail(todoId)));
    }

    @Operation(summary = "Todo별 제출물 조회", description = "특정 할일에 제출된 과제를 조회합니다")
    @GetMapping("/todos/{todoId}/submissions")
    public ResponseEntity<ApiResponse<List<AssignmentSubmissionResponse>>> getSubmissions(@PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getSubmissionsByTodo(todoId)));
    }

    // ==================== AI 튜터 ====================

    @Operation(summary = "AI 튜터 채팅", description = "멘티가 AI 튜터와 채팅합니다")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chatWithAi(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        return ResponseEntity.ok(ApiResponse.ok(chatService.chatWithAiTutor(userId, message)));
    }
}
