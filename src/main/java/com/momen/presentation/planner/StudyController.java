package com.momen.presentation.planner;

import com.momen.application.mentoring.MentoringService;
import com.momen.application.mentoring.dto.MenteeResponse;
import com.momen.application.planner.*;
import com.momen.application.planner.dto.*;
import com.momen.domain.mentoring.Mentee;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
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

import static java.time.LocalDate.now;

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
    private final MentoringService mentoringService;
    private final TodoFeedbackService todoFeedbackService;
    private final WeeklyFeedbackService weeklyFeedbackService;
    private final MonthlyFeedbackService monthlyFeedbackService;
    private final MenteeRepository menteeRepository;

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

    @Operation(summary = "주별 Todo 조회", description = "멘티 본인의 특정 주차 할일을 조회합니다 (일요일~토요일)")
    @GetMapping(value = "/todos", params = "weekStartDate")
    public ResponseEntity<ApiResponse<List<TodoSummaryResponse>>> getMyTodosByWeek(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "주 시작일-일요일 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getMyTodosByWeek(userId, weekStartDate)));
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

    // ==================== Todo CRUD (멘티용) ====================

    @Operation(summary = "Todo 생성 (멘티)", description = "멘티가 본인의 할일을 생성합니다 (단건/반복)")
    @PostMapping("/todos")
    public ResponseEntity<ApiResponse<List<Long>>> createTodo(
            @RequestAttribute("userId") Long userId,
            @RequestBody TodoCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.createTodoByMentee(userId, request)));
    }

    @Operation(summary = "Todo 내용 수정 (멘티)", description = "멘티가 본인이 생성한 할일의 내용을 수정합니다")
    @PatchMapping("/todos/{todoId}/content")
    public ResponseEntity<ApiResponse<Void>> updateTodoContent(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long todoId,
            @RequestBody TodoUpdateRequest request) {
        todoService.updateTodoContentByMentee(userId, todoId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Todo 삭제 (멘티)", description = "멘티가 본인이 생성한 할일을 삭제합니다")
    @DeleteMapping("/todos/{todoId}")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long todoId) {
        todoService.deleteTodoByMentee(userId, todoId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "Todo 학습 시간 추가", description = "타이머로 측정한 시간(초)을 기존 학습시간에 누적합니다.")
    @PatchMapping("/todos/{todoId}/study-time")
    public ResponseEntity<ApiResponse<Void>> addStudyTime(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long todoId,
            @RequestBody Map<String, Integer> request) {
        todoService.addStudyTime(userId, todoId, request.get("studyTime"));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== 학습시간 통계 ====================

    @Operation(summary = "학습시간 통계 (일별)", description = "특정 날짜의 총 학습시간과 과목별 학습시간을 조회합니다")
    @GetMapping(value = "/study-time", params = "date")
    public ResponseEntity<ApiResponse<StudyTimeStatsResponse>> getStudyTimeByDate(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getStudyTimeStatsByDate(userId, date)));
    }

    @Operation(summary = "학습시간 통계 (주별)", description = "특정 주의 총 학습시간과 과목별 학습시간을 조회합니다")
    @GetMapping(value = "/study-time", params = "weekStartDate")
    public ResponseEntity<ApiResponse<StudyTimeStatsResponse>> getStudyTimeByWeek(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "주 시작일-일요일 (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getStudyTimeStatsByWeek(userId, weekStartDate)));
    }

    @Operation(summary = "학습시간 통계 (월별)", description = "특정 월의 총 학습시간과 과목별 학습시간을 조회합니다")
    @GetMapping(value = "/study-time", params = "yearMonth")
    public ResponseEntity<ApiResponse<StudyTimeStatsResponse>> getStudyTimeByMonth(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam String yearMonth) {
        return ResponseEntity.ok(ApiResponse.ok(todoService.getStudyTimeStatsByMonth(userId, yearMonth)));
    }

    @Operation(summary = "당일 학습 통계", description = "오늘(또는 지정 날짜) 하루의 총 학습·완료·남은 개수와 완료율을 조회합니다. 홈 프로그레스바용.")
    @GetMapping("/daily-stats")
    public ResponseEntity<ApiResponse<StudyDailyStatsResponse>> getDailyStats(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "조회할 날짜 (미입력 시 오늘)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : now();
        return ResponseEntity.ok(ApiResponse.ok(todoService.getDailyStats(userId, targetDate)));
    }

    // ==================== 과제 제출 ====================

    @Operation(summary = "과제 제출/수정", description = "파일·텍스트(메모)를 제출합니다. 기존 제출이 있으면 수정됩니다. Todo당 1건만 존재합니다.")
    @PostMapping("/todos/{todoId}/submit")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> submitAssignment(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "할 일 ID") @PathVariable Long todoId,
            @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.submitAssignment(userId, todoId, request)));
    }

    @Operation(summary = "Todo별 제출물 조회", description = "특정 할일의 제출물과 첨부파일 목록을 조회합니다")
    @GetMapping("/todos/{todoId}/submission")
    public ResponseEntity<ApiResponse<AssignmentSubmissionResponse>> getSubmissionByTodo(
            @Parameter(description = "할 일 ID") @PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getSubmissionByTodo(todoId)));
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

    // ==================== 멘티 정보 ====================

    @Operation(summary = "멘티 본인 정보 조회", description = "멘티 본인의 프로필사진, 수강과목, 응원메세지 등 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MenteeResponse>> getMyInfo(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(mentoringService.getMyMenteeInfo(userId)));
    }

    @Operation(summary = "응원 메세지 수정", description = "멘티가 본인의 응원 메세지를 수정합니다")
    @PatchMapping("/me/cheer-message")
    public ResponseEntity<ApiResponse<Void>> updateCheerMessage(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        mentoringService.updateCheerMessage(userId, request.get("cheerMessage"));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== 마이페이지 ====================

    @Operation(summary = "마이페이지 조회", description = "멘티의 프로필, 성취율, 과목별 완료율을 조회합니다")
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MypageResponse>> getMypage(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(plannerService.getMypage(userId)));
    }

    // ==================== 피드백 조회 (멘티용 - 토큰 기반) ====================

    @Operation(summary = "Todo 피드백 조회", description = "특정 Todo의 피드백을 조회합니다")
    @GetMapping("/feedback/todo/{todoId}")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> getMyTodoFeedback(
            @PathVariable Long todoId) {
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.getFeedback(todoId)));
    }

    @Operation(summary = "Todo 피드백 질문 수정", description = "멘티가 질문을 수정합니다")
    @PatchMapping("/feedback/todo/{todoId}/question")
    public ResponseEntity<ApiResponse<TodoFeedbackResponse>> updateMyTodoQuestion(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long todoId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.ok(todoFeedbackService.updateQuestionByMentee(userId, todoId, request.get("question"))));
    }

    @Operation(summary = "주간 피드백 목록 조회", description = "본인의 주간 피드백을 조회합니다. yearMonth 또는 weekStartDate로 필터링 가능")
    @GetMapping("/feedback/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyFeedbackResponse>>> getMyWeeklyFeedbackList(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam(required = false) String yearMonth,
            @Parameter(description = "주 시작일-일요일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        Long menteeId = getMenteeId(userId);
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.getFeedbackList(menteeId, yearMonth, weekStartDate)));
    }

    @Operation(summary = "주간 피드백 단건 조회", description = "주간 피드백을 조회합니다")
    @GetMapping("/feedback/weekly/{feedbackId}")
    public ResponseEntity<ApiResponse<WeeklyFeedbackResponse>> getMyWeeklyFeedback(
            @PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResponse.ok(weeklyFeedbackService.getFeedback(feedbackId)));
    }

    @Operation(summary = "월간 피드백 목록 조회", description = "본인의 월간 피드백을 조회합니다. yearMonth 또는 year로 필터링 가능")
    @GetMapping("/feedback/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyFeedbackResponse>>> getMyMonthlyFeedbackList(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "연월 (yyyy-MM)") @RequestParam(required = false) String yearMonth,
            @Parameter(description = "연도") @RequestParam(required = false) Integer year) {
        Long menteeId = getMenteeId(userId);
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.getFeedbackList(menteeId, yearMonth, year)));
    }

    @Operation(summary = "월간 피드백 단건 조회", description = "월간 피드백을 조회합니다")
    @GetMapping("/feedback/monthly/{feedbackId}")
    public ResponseEntity<ApiResponse<MonthlyFeedbackResponse>> getMyMonthlyFeedback(
            @PathVariable Long feedbackId) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyFeedbackService.getFeedback(feedbackId)));
    }

    private Long getMenteeId(Long userId) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        return mentee.getId();
    }
}
