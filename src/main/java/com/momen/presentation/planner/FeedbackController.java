package com.momen.presentation.planner;

import com.momen.application.planner.MonthlyFeedbackService;
import com.momen.application.planner.TodoFeedbackService;
import com.momen.application.planner.WeeklyFeedbackService;
import com.momen.application.planner.dto.*;
import com.momen.core.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Feedback", description = "피드백 API (Todo/주간/월간)")
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {

    private final TodoFeedbackService todoFeedbackService;
    private final WeeklyFeedbackService weeklyFeedbackService;
    private final MonthlyFeedbackService monthlyFeedbackService;

    // ==================== Todo 피드백 ====================

    @Operation(summary = "Todo 피드백 작성/수정 (멘토)", description = "멘토가 Todo에 대한 피드백을 작성합니다")
    @PostMapping("/todo/{todoId}")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> saveTodoFeedback(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "Todo ID") @PathVariable Long todoId,
            @RequestBody TodoFeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.saveFeedbackByMentor(userId, todoId, request)));
    }

    @Operation(summary = "Todo 피드백 Q&A 수정 (멘티)", description = "멘티가 Q&A 섹션을 수정합니다")
    @PatchMapping("/todo/{todoId}/qna")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> updateTodoQna(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "Todo ID") @PathVariable Long todoId,
            @RequestBody Map<String, String> request) {
        String qna = request.get("qna");
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.updateQnaByMentee(userId, todoId, qna)));
    }

    @Operation(summary = "Todo 피드백 조회", description = "Todo의 피드백을 조회합니다")
    @GetMapping("/todo/{todoId}")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> getTodoFeedback(
            @Parameter(description = "Todo ID") @PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.getFeedback(todoId)));
    }

    // ==================== 주간 피드백 ====================

    @Operation(summary = "주간 피드백 AI 요약 생성", description = "멘토가 작성한 내용을 기반으로 AI 요약을 생성합니다 (저장X)")
    @PostMapping("/weekly/ai-summary")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateWeeklyAiSummary(
            @RequestBody WeeklyAiSummaryRequest request) {
        String aiSummary = weeklyFeedbackService.generateAiSummary(request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("aiSummary", aiSummary)));
    }

    @Operation(summary = "주간 피드백 저장", description = "주간 피드백을 저장합니다")
    @PostMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyFeedbackResponse>> saveWeeklyFeedback(
            @RequestAttribute("userId") Long userId,
            @RequestBody WeeklyFeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.saveFeedback(userId, request)));
    }

    @Operation(summary = "주간 피드백 단건 조회", description = "주간 피드백을 조회합니다")
    @GetMapping("/weekly/{feedbackId}")
    public ResponseEntity<ApiResponse<WeeklyFeedbackResponse>> getWeeklyFeedback(
            @Parameter(description = "피드백 ID") @PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.getFeedback(feedbackId)));
    }

    @Operation(summary = "주간 피드백 목록 조회", description = "멘티의 주간 피드백 목록을 조회합니다")
    @GetMapping("/weekly/list/{menteeId}")
    public ResponseEntity<ApiResponse<List<WeeklyFeedbackResponse>>> getWeeklyFeedbackList(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.getFeedbackList(menteeId)));
    }

    // ==================== 월간 피드백 ====================

    @Operation(summary = "월간 피드백 AI 요약 생성", description = "해당 월의 주간피드백들을 기반으로 AI 요약을 생성합니다 (저장X)")
    @PostMapping("/monthly/ai-summary")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateMonthlyAiSummary(
            @RequestAttribute("userId") Long userId,
            @RequestBody MonthlyAiSummaryRequest request) {
        String aiSummary = monthlyFeedbackService.generateAiSummary(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("aiSummary", aiSummary)));
    }

    @Operation(summary = "월간 피드백 저장", description = "월간 피드백을 저장합니다")
    @PostMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyFeedbackResponse>> saveMonthlyFeedback(
            @RequestAttribute("userId") Long userId,
            @RequestBody MonthlyFeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.saveFeedback(userId, request)));
    }

    @Operation(summary = "월간 피드백 단건 조회", description = "월간 피드백을 조회합니다")
    @GetMapping("/monthly/{feedbackId}")
    public ResponseEntity<ApiResponse<MonthlyFeedbackResponse>> getMonthlyFeedback(
            @Parameter(description = "피드백 ID") @PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.getFeedback(feedbackId)));
    }

    @Operation(summary = "월간 피드백 목록 조회", description = "멘티의 월간 피드백 목록을 조회합니다")
    @GetMapping("/monthly/list/{menteeId}")
    public ResponseEntity<ApiResponse<List<MonthlyFeedbackResponse>>> getMonthlyFeedbackList(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.getFeedbackList(menteeId)));
    }
}
