package com.momen.presentation.mentoring;

import com.momen.application.mentoring.MentoringChatService;
import com.momen.application.mentoring.MentoringService;
import com.momen.application.mentoring.dto.MenteeResponse;
import com.momen.application.planner.FeedbackService;
import com.momen.application.planner.PlannerService;
import com.momen.application.planner.dto.*;
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
    private final PlannerService plannerService;

    @Operation(summary = "담당 멘티 목록 조회", description = "멘토가 담당하는 멘티 목록을 조회합니다")
    @GetMapping("/mentees")
    public ResponseEntity<List<MenteeResponse>> getMenteeList(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(mentoringService.getMenteeList(userId));
    }

    @Operation(summary = "멘티 플래너 조회", description = "멘토가 특정 멘티의 특정 날짜 플래너를 조회합니다")
    @GetMapping("/mentees/{menteeId}/planners")
    public ResponseEntity<PlannerResponse> getMenteePlanner(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(plannerService.getPlannerForMentee(userId, menteeId, date));
    }

    @Operation(summary = "멘티에게 할 일 등록", description = "멘토가 특정 멘티에게 할 일을 등록합니다")
    @PostMapping("/mentees/{menteeId}/todos")
    public ResponseEntity<Long> addTodoForMentee(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(plannerService.addTodoForMentee(userId, menteeId, date, request));
    }

    @Operation(summary = "AI 피드백 초안 생성", description = "멘토가 특정 멘티의 기간별 AI 피드백 초안을 생성합니다")
    @PostMapping("/mentees/{menteeId}/feedback/draft")
    public ResponseEntity<String> generateFeedbackDraft(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "피드백 유형 (WEEKLY/MONTHLY)") @RequestParam FeedbackType type,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(feedbackService.generateFeedbackDraft(userId, menteeId, type, startDate, endDate));
    }

    @Operation(summary = "피드백 단건 조회", description = "특정 멘티의 기간별 피드백을 조회합니다")
    @GetMapping("/mentees/{menteeId}/feedback")
    public ResponseEntity<FeedbackResponse> getFeedback(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "피드백 유형 (WEEKLY/MONTHLY)") @RequestParam FeedbackType type,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return ResponseEntity.ok(feedbackService.getFeedback(menteeId, type, startDate));
    }

    @Operation(summary = "피드백 목록 조회", description = "특정 멘티의 피드백 목록을 조회합니다")
    @GetMapping("/mentees/{menteeId}/feedback/list")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackList(
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "피드백 유형 (WEEKLY/MONTHLY)") @RequestParam FeedbackType type) {
        return ResponseEntity.ok(feedbackService.getFeedbackList(menteeId, type));
    }

    @Operation(summary = "피드백 작성/수정", description = "멘토가 피드백을 작성하거나 수정합니다")
    @PostMapping("/mentees/{menteeId}/feedback")
    public ResponseEntity<FeedbackResponse> saveFeedback(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.saveFeedback(userId, menteeId, request));
    }

    @Operation(summary = "주차별 피드백 AI 요약", description = "해당 월의 멘토 피드백을 주차별·항목별(국어/수학/영어/총평)로 AI 요약합니다")
    @GetMapping("/mentees/{menteeId}/feedback/weekly-summary")
    public ResponseEntity<MonthlyFeedbackSummaryResponse> getWeeklyFeedbackSummary(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "멘티 ID") @PathVariable Long menteeId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam String yearMonth) {
        return ResponseEntity.ok(feedbackService.generateWeeklySummaries(userId, menteeId, yearMonth));
    }

    @Operation(summary = "AI 튜터 채팅", description = "멘티가 AI 튜터와 채팅합니다")
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithAi(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        return ResponseEntity.ok(chatService.chatWithAiTutor(userId, message));
    }
}
