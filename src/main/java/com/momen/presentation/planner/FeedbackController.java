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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @Operation(summary = "Todo 피드백 질문 수정 (멘티)", description = "멘티가 질문을 수정합니다")
    @PatchMapping("/todo/{todoId}/question")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> updateTodoQuestion(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "Todo ID") @PathVariable Long todoId,
            @RequestBody Map<String, String> request) {
        String question = request.get("question");
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.updateQuestionByMentee(userId, todoId, question)));
    }

    @Operation(summary = "Todo 피드백 조회", description = "Todo의 피드백을 조회합니다")
    @GetMapping("/todo/{todoId}")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> getTodoFeedback(
            @Parameter(description = "Todo ID") @PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.getFeedback(todoId)));
    }

    // ==================== 주간 피드백 ====================

    @Operation(summary = "주간 피드백 AI 요약 생성", description = "해당 주차의 Todo 피드백들을 기반으로 AI 요약을 생성합니다 (저장X)")
    @PostMapping("/mentees/{menteeId}/weekly/ai-summary")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateWeeklyAiSummary(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @RequestBody WeeklyAiSummaryRequest request) {
        String aiSummary = weeklyFeedbackService.generateAiSummary(userId, menteeId, request);
        if (aiSummary == null) {
            aiSummary = "이번 주에 등록된 피드백이 없습니다.";
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("aiSummary", aiSummary)));
    }

    @Operation(summary = "주간 피드백 저장", description = "주간 피드백을 저장합니다")
    @PostMapping("/mentees/{menteeId}/weekly")
    public ResponseEntity<ApiResponse<WeeklyFeedbackResponse>> saveWeeklyFeedback(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @RequestBody WeeklyFeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.saveFeedback(userId, menteeId, request)));
    }

    @Operation(summary = "주간 피드백 단건 조회", description = "주간 피드백을 조회합니다")
    @GetMapping("/weekly/{feedbackId}")
    public ResponseEntity<ApiResponse<WeeklyFeedbackResponse>> getWeeklyFeedback(
            @Parameter(description = "피드백 ID") @PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.getFeedback(feedbackId)));
    }

    @Operation(summary = "주간 피드백 목록 조회", description = "멘티의 주간 피드백을 조회합니다. yearMonth로 월별 달력 조회, weekStartDate로 특정 주 조회 가능")
    @GetMapping("/mentees/{menteeId}/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyFeedbackResponse>>> getWeeklyFeedbackList(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam(required = false) String yearMonth,
            @Parameter(description = "주 시작일-일요일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.getFeedbackList(menteeId, yearMonth, weekStartDate)));
    }

    // ==================== 월간 피드백 ====================

    @Operation(summary = "월간 피드백 AI 요약 생성", description = "해당 월의 주간피드백들을 기반으로 AI 요약을 생성합니다")
    @PostMapping("/mentees/{menteeId}/monthly/ai-summary")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateMonthlyAiSummary(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @RequestBody MonthlyAiSummaryRequest request) {
        String aiSummary = monthlyFeedbackService.generateAiSummary(userId, menteeId, request);
        if (aiSummary == null) {
            aiSummary = "이번 달에 작성된 주간 피드백이 없습니다.";
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("aiSummary", aiSummary)));
    }

    @Operation(summary = "월간 피드백 저장", description = "월간 피드백을 저장합니다")
    @PostMapping("/mentees/{menteeId}/monthly")
    public ResponseEntity<ApiResponse<MonthlyFeedbackResponse>> saveMonthlyFeedback(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long menteeId,
            @RequestBody MonthlyFeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.saveFeedback(userId, menteeId, request)));
    }

    @Operation(summary = "월간 피드백 단건 조회", description = "월간 피드백을 조회합니다")
    @GetMapping("/monthly/{feedbackId}")
    public ResponseEntity<ApiResponse<MonthlyFeedbackResponse>> getMonthlyFeedback(
            @Parameter(description = "피드백 ID") @PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.getFeedback(feedbackId)));
    }

    @Operation(summary = "월간 피드백 목록 조회", description = "멘티의 월간 피드백을 조회합니다. yearMonth로 특정 월, year로 연도별 조회 가능")
    @GetMapping("/mentees/{menteeId}/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyFeedbackResponse>>> getMonthlyFeedbackList(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam(required = false) String yearMonth,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.getFeedbackList(menteeId, yearMonth, year)));
    }
}
