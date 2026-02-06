package com.momen.application.planner;

import com.momen.application.planner.dto.WeeklyAiSummaryRequest;
import com.momen.application.planner.dto.WeeklyFeedbackRequest;
import com.momen.application.planner.dto.WeeklyFeedbackResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.Todo;
import com.momen.domain.planner.TodoFeedback;
import com.momen.domain.planner.WeeklyFeedback;
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

    // AI 요약 생성 (해당 주차 Todo 피드백들을 DB에서 조회 → AI 요약)
    public String generateAiSummary(Long mentorUserId, Long menteeId, WeeklyAiSummaryRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        LocalDate weekStart = getWeekStartDate(request.getYear(), request.getMonth(), request.getWeek());
        LocalDate weekEnd = getWeekEndDate(request.getYear(), request.getMonth(), request.getWeek());

        List<Todo> todos = todoRepository.findByMenteeIdAndMonth(menteeId, weekStart, weekEnd);
        if (todos.isEmpty()) {
            throw new IllegalArgumentException("해당 주차에 등록된 Todo가 없습니다");
        }

        List<Long> todoIds = todos.stream().map(Todo::getId).toList();
        List<TodoFeedback> feedbacks = todoFeedbackRepository.findByTodoIdIn(todoIds);
        if (feedbacks.isEmpty()) {
            throw new IllegalArgumentException("해당 주차 Todo에 작성된 피드백이 없습니다");
        }

        String prompt = buildWeeklySummaryPrompt(todos, feedbacks, request.getYear(), request.getMonth(), request.getWeek());
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
                .findByMenteeIdAndYearAndMonthAndWeek(menteeId, request.getYear(), request.getMonth(), request.getWeek())
                .orElseGet(() -> weeklyFeedbackRepository.save(
                        new WeeklyFeedback(mentee, mentor, request.getYear(), request.getMonth(), request.getWeek())
                ));

        feedback.update(
                request.getOverallReview(),
                request.getWellDone(),
                request.getToImprove(),
                request.getAiSummary()
        );

        return WeeklyFeedbackResponse.from(feedback);
    }

    // 주간 피드백 단건 조회
    public WeeklyFeedbackResponse getFeedback(Long feedbackId) {
        WeeklyFeedback feedback = weeklyFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return WeeklyFeedbackResponse.from(feedback);
    }

    // 멘티의 주간 피드백 목록 조회 (필터 조건: year, month, week)
    public List<WeeklyFeedbackResponse> getFeedbackList(Long menteeId, Integer year, Integer month, Integer week) {
        if (year != null && month != null && week != null) {
            return weeklyFeedbackRepository.findByMenteeIdAndYearAndMonthAndWeek(menteeId, year, month, week)
                    .map(WeeklyFeedbackResponse::from)
                    .map(List::of)
                    .orElse(List.of());
        } else if (year != null && month != null) {
            return weeklyFeedbackRepository.findByMenteeIdAndYearAndMonthOrderByWeek(menteeId, year, month)
                    .stream()
                    .map(WeeklyFeedbackResponse::from)
                    .toList();
        } else {
            return weeklyFeedbackRepository.findByMenteeIdOrderByYearDescMonthDescWeekDesc(menteeId)
                    .stream()
                    .map(WeeklyFeedbackResponse::from)
                    .toList();
        }
    }

    // 주차 → 시작일 계산 (1주차: 1일, 2주차: 8일, 3주차: 15일, 4주차: 22일)
    private LocalDate getWeekStartDate(int year, int month, int week) {
        int day = (week - 1) * 7 + 1;
        return LocalDate.of(year, month, day);
    }

    // 주차 → 종료일 계산 (1주차: 7일, 2주차: 14일, 3주차: 21일, 4주차: 말일)
    private LocalDate getWeekEndDate(int year, int month, int week) {
        if (week == 4) {
            return YearMonth.of(year, month).atEndOfMonth();
        }
        return LocalDate.of(year, month, week * 7);
    }

    private String buildWeeklySummaryPrompt(List<Todo> todos, List<TodoFeedback> feedbacks, int year, int month, int week) {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("년 ").append(month).append("월 ").append(week).append("주차의 ");
        sb.append("Todo 피드백들을 종합하여 주간 학습 요약을 2-3문장으로 작성해주세요.\n\n");

        for (TodoFeedback tf : feedbacks) {
            Todo todo = tf.getTodo();
            sb.append("=== ").append(todo.getTitle()).append(" (").append(todo.getSubject()).append(") ===\n");
            sb.append("학습점검: ").append(tf.getStudyCheck() != null ? tf.getStudyCheck() : "없음").append("\n");
            sb.append("멘토피드백: ").append(tf.getMentorComment() != null ? tf.getMentorComment() : "없음").append("\n");
            sb.append("Q&A: ").append(tf.getQna() != null ? tf.getQna() : "없음").append("\n\n");
        }

        sb.append("위 피드백들을 바탕으로 이번 주 학습 성과와 개선점을 포함한 요약을 작성해주세요.");
        return sb.toString();
    }
}
