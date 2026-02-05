package com.momen.presentation.mentoring;

import com.momen.application.mentoring.MentoringChatService;
import com.momen.application.mentoring.MentoringService;
import com.momen.application.mentoring.dto.MenteeResponse;
import com.momen.application.planner.AssignmentService;
import com.momen.application.planner.FeedbackService;
import com.momen.application.planner.TodoService;
import com.momen.application.planner.dto.*;
import com.momen.core.dto.response.ApiResponse;
import com.momen.domain.planner.FeedbackType;
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

    private final FeedbackService feedbackService;
    private final MentoringChatService chatService;
    private final MentoringService mentoringService;
    private final TodoService todoService;
    private final AssignmentService assignmentService;

    @Operation(summary = "담당 멘티 목록 조회", description = "멘토가 담당하는 멘티 목록을 조회합니다")
    @GetMapping("/mentees")
    public ResponseEntity<ApiResponse<List<MenteeResponse>>> getMenteeList(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(mentoringService.getMenteeList(userId)));
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

    @Operation(summary = "Todo 피드백 작성", description = "멘토가 할일에 피드백을 작성합니다")
    @PostMapping("/mentees/{menteeId}/todos/{todoId}/feedback")
    public ResponseEntity<ApiResponse<Void>> writeTodoFeedback(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @PathVariable Long todoId,
            @RequestBody TodoFeedbackRequest request) {
        todoService.writeFeedback(userId, todoId, request);
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

    // ==================== 피드백 ====================

    @Operation(summary = "AI 피드백 초안 생성", description = "멘토가 특정 멘티의 기간별 AI 피드백 초안을 생성합니다")
    @PostMapping("/mentees/{menteeId}/feedback/draft")
    public ResponseEntity<ApiResponse<String>> generateFeedbackDraft(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "피드백 유형 (WEEKLY/MONTHLY)") @RequestParam FeedbackType type,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.generateFeedbackDraft(userId, menteeId, type, startDate, endDate)));
    }

    @Operation(summary = "피드백 단건 조회", description = "특정 멘티의 기간별 피드백을 조회합니다")
    @GetMapping("/mentees/{menteeId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "피드백 유형 (WEEKLY/MONTHLY)") @RequestParam FeedbackType type,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getFeedback(menteeId, type, startDate)));
    }

    @Operation(summary = "피드백 목록 조회", description = "특정 멘티의 피드백 목록을 조회합니다")
    @GetMapping("/mentees/{menteeId}/feedback/list")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getFeedbackList(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "피드백 유형 (WEEKLY/MONTHLY)") @RequestParam FeedbackType type) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.getFeedbackList(menteeId, type)));
    }

    @Operation(summary = "피드백 작성/수정", description = "멘토가 피드백을 작성하거나 수정합니다")
    @PostMapping("/mentees/{menteeId}/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse>> saveFeedback(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.saveFeedback(userId, menteeId, request)));
    }

    @Operation(summary = "주차별 피드백 AI 요약", description = "해당 월의 멘토 피드백을 주차별·항목별(국어/수학/영어/총평)로 AI 요약합니다")
    @GetMapping("/mentees/{menteeId}/feedback/weekly-summary")
    public ResponseEntity<ApiResponse<MonthlyFeedbackSummaryResponse>> getWeeklyFeedbackSummary(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(feedbackService.generateWeeklySummaries(userId, menteeId, yearMonth)));
    }

    @Operation(summary = "AI 튜터 채팅", description = "멘티가 AI 튜터와 채팅합니다")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chatWithAi(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        return ResponseEntity.ok(ApiResponse.ok(chatService.chatWithAiTutor(userId, message)));
    }
}
