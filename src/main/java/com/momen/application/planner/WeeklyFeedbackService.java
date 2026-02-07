package com.momen.application.planner;

import com.momen.application.notification.NotificationService;
import com.momen.application.planner.dto.WeeklyAiSummaryRequest;
import com.momen.application.planner.dto.WeeklyFeedbackRequest;
import com.momen.application.planner.dto.WeeklyFeedbackResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.notification.NotificationType;
import com.momen.domain.planner.Todo;
import com.momen.domain.planner.TodoFeedback;
import com.momen.domain.planner.WeeklyFeedback;
import com.momen.domain.user.User;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.TodoFeedbackRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import com.momen.infrastructure.jpa.planner.WeeklyFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyFeedbackService {

    private final WeeklyFeedbackRepository weeklyFeedbackRepository;
    private final TodoRepository todoRepository;
    private final TodoFeedbackRepository todoFeedbackRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;
    private final NotificationService notificationService;

    // AI 요약 생성 (해당 주차 Todo 피드백들을 DB에서 조회 → AI 요약)
    public String generateAiSummary(Long mentorUserId, Long menteeId, WeeklyAiSummaryRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        LocalDate weekStart = request.getWeekStartDate();
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Todo> todos = todoRepository.findByMenteeIdAndMonth(menteeId, weekStart, weekEnd);
        if (todos.isEmpty()) {
            return null;
        }

        List<Long> todoIds = todos.stream().map(Todo::getId).toList();
        List<TodoFeedback> feedbacks = todoFeedbackRepository.findByTodoIdIn(todoIds);
        if (feedbacks.isEmpty()) {
            return null;
        }

        String prompt = buildWeeklySummaryPrompt(feedbacks, weekStart);
        return aiClient.generateText(prompt);
    }

    // 주간 피드백 저장
    @Transactional
    public WeeklyFeedbackResponse saveFeedback(Long mentorUserId, Long menteeId, WeeklyFeedbackRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        WeeklyFeedback feedback = weeklyFeedbackRepository
                .findByMenteeIdAndWeekStartDate(menteeId, request.getWeekStartDate())
                .orElseGet(() -> weeklyFeedbackRepository.save(
                        new WeeklyFeedback(mentee, mentor, request.getWeekStartDate())
                ));

        feedback.update(
                request.getOverallReview(),
                request.getWellDone(),
                request.getToImprove(),
                request.getAiSummary()
        );

        // 멘티에게 주간 피드백 알림 전송
        User menteeUser = mentee.getUser();
        LocalDate weekEnd = request.getWeekStartDate().plusDays(6);
        String message = String.format("%d월 %d일 ~ %d월 %d일 주간 피드백이 등록되었습니다.",
                request.getWeekStartDate().getMonthValue(), request.getWeekStartDate().getDayOfMonth(),
                weekEnd.getMonthValue(), weekEnd.getDayOfMonth());
        notificationService.createAndPush(menteeUser, message, NotificationType.WEEKLY_FEEDBACK, feedback.getId());

        return WeeklyFeedbackResponse.from(feedback);
    }

    // 주간 피드백 단건 조회
    public WeeklyFeedbackResponse getFeedback(Long feedbackId) {
        WeeklyFeedback feedback = weeklyFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return WeeklyFeedbackResponse.from(feedback);
    }

    // 멘티의 주간 피드백 목록 조회 (필터: yearMonth, weekStartDate)
    public List<WeeklyFeedbackResponse> getFeedbackList(Long menteeId, String yearMonth, LocalDate weekStartDate) {
        if (weekStartDate != null) {
            // 특정 주 단건 조회
            return weeklyFeedbackRepository.findByMenteeIdAndWeekStartDate(menteeId, weekStartDate)
                    .map(WeeklyFeedbackResponse::from)
                    .map(List::of)
                    .orElse(List.of());
        } else if (yearMonth != null) {
            // 해당 월 달력에 보이는 주간 피드백 조회
            YearMonth ym = YearMonth.parse(yearMonth);
            LocalDate calendarStart = getCalendarStartDate(ym);
            LocalDate lastOfMonth = ym.atEndOfMonth();
            return weeklyFeedbackRepository
                    .findByMenteeIdAndWeekStartDateBetweenOrderByWeekStartDate(menteeId, calendarStart, lastOfMonth)
                    .stream()
                    .map(WeeklyFeedbackResponse::from)
                    .toList();
        } else {
            // 전체 목록
            return weeklyFeedbackRepository.findByMenteeIdOrderByWeekStartDateDesc(menteeId)
                    .stream()
                    .map(WeeklyFeedbackResponse::from)
                    .toList();
        }
    }

    // 해당 월 달력의 첫 번째 일요일 계산
    private LocalDate getCalendarStartDate(YearMonth ym) {
        LocalDate firstOfMonth = ym.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // 일=0, 월=1, ..., 토=6
        return firstOfMonth.minusDays(dayOfWeek);
    }

    private String buildWeeklySummaryPrompt(List<TodoFeedback> feedbacks, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        StringBuilder sb = new StringBuilder();
        sb.append(weekStart).append(" ~ ").append(weekEnd).append(" 주간의 ");
        sb.append("Todo 피드백들을 종합하여 주간 학습 요약을 2-3문장으로 작성해주세요.\n\n");

        for (TodoFeedback tf : feedbacks) {
            Todo todo = tf.getTodo();
            sb.append("=== ").append(todo.getTitle()).append(" (").append(todo.getSubject()).append(") ===\n");
            sb.append("멘토피드백: ").append(tf.getMentorComment() != null ? tf.getMentorComment() : "없음").append("\n");
            sb.append("멘티질문: ").append(tf.getQuestion() != null ? tf.getQuestion() : "없음").append("\n");
            sb.append("멘토답변: ").append(tf.getAnswer() != null ? tf.getAnswer() : "없음").append("\n\n");
        }

        sb.append("위 피드백들을 바탕으로 이번 주 학습 성과와 개선점을 포함한 요약을 작성해주세요.");
        return sb.toString();
    }
}
