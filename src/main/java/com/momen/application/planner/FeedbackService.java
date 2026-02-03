package com.momen.application.planner;

import com.momen.application.planner.dto.FeedbackRequest;
import com.momen.application.planner.dto.FeedbackResponse;
import com.momen.application.planner.dto.MonthlyFeedbackSummaryItem;
import com.momen.application.planner.dto.MonthlyFeedbackSummaryResponse;
import com.momen.application.planner.dto.WeeklyFeedbackSummaryItem;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.Feedback;
import com.momen.domain.planner.Planner;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.FeedbackRepository;
import com.momen.infrastructure.jpa.planner.PlannerRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PlannerRepository plannerRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final TodoRepository todoRepository;
    private final AiClient aiClient;

    @Transactional
    public String generateFeedbackDraft(Long userId, Long plannerId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));

        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseGet(() -> new Feedback(planner, mentor));

        String prompt = buildPrompt(planner);
        String aiDraft = aiClient.generateText(prompt);

        feedback.saveAiDraft(aiDraft);
        feedbackRepository.save(feedback);
        return aiDraft;
    }

    // 피드백 조회
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long plannerId) {
        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return FeedbackResponse.from(feedback);
    }

    // 피드백 작성/수정
    @Transactional
    public FeedbackResponse saveFeedback(Long userId, Long plannerId, FeedbackRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));

        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseGet(() -> feedbackRepository.save(new Feedback(planner, mentor)));

        feedback.updateSummaries(
                request.getKoreanSummary(),
                request.getMathSummary(),
                request.getEnglishSummary(),
                request.getScienceSummary(),
                request.getTotalReview()
        );

        if (request.getAdoptAiDraft() != null) {
            feedback.adoptAiDraft(request.getAdoptAiDraft());
        }

        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    /**
     * 주차별 피드백 AI 요약 생성.
     * 해당 월의 멘토 피드백을 주차(1~4)별로 묶고, 항목별(국어/수학/영어/총평) 프롬프트 템플릿으로 AI 요약 생성.
     */
    @Transactional(readOnly = true)
    public MonthlyFeedbackSummaryResponse generateWeeklySummaries(Long mentorUserId, Long menteeId, String yearMonth) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        var mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        var start = ym.atDay(1);
        var end = ym.atEndOfMonth();

        List<Planner> planners = plannerRepository.findByMenteeIdAndPlannerDateBetween(mentee.getId(), start, end);
        if (planners.isEmpty()) {
            return MonthlyFeedbackSummaryResponse.builder()
                    .yearMonth(yearMonth)
                    .weeks(List.of())
                    .monthlySummary(null)
                    .build();
        }

        List<Long> plannerIds = planners.stream().map(Planner::getId).collect(Collectors.toList());
        List<Feedback> feedbacks = feedbackRepository.findByPlanner_IdIn(plannerIds);

        // 주차 = (일자 - 1) / 7 + 1 (1~7 -> 1, 8~14 -> 2, ...)
        var feedbacksByWeek = new ArrayList<List<Feedback>>();
        for (int w = 1; w <= 4; w++) {
            final int week = w;
            List<Feedback> inWeek = feedbacks.stream()
                    .filter(f -> {
                        int day = f.getPlanner().getPlannerDate().getDayOfMonth();
                        int wk = (day - 1) / 7 + 1;
                        return wk == week;
                    })
                    .toList();
            feedbacksByWeek.add(inWeek);
        }

        List<WeeklyFeedbackSummaryItem> weeks = new ArrayList<>();
        for (int w = 1; w <= 4; w++) {
            List<Feedback> weekFeedbacks = feedbacksByWeek.get(w - 1);
            if (weekFeedbacks.isEmpty()) {
                continue;
            }

            String koreanContent = concatNonEmpty(weekFeedbacks, Feedback::getKoreanSummary);
            String mathContent = concatNonEmpty(weekFeedbacks, Feedback::getMathSummary);
            String englishContent = concatNonEmpty(weekFeedbacks, Feedback::getEnglishSummary);
            String scienceContent = concatNonEmpty(weekFeedbacks, Feedback::getScienceSummary);
            String totalContent = concatNonEmpty(weekFeedbacks, Feedback::getTotalReview);

            String koreanSummary = koreanContent.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.korean(w, koreanContent));
            String mathSummary = mathContent.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.math(w, mathContent));
            String englishSummary = englishContent.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.english(w, englishContent));
            String scienceSummary = scienceContent.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.science(w, scienceContent));
            String totalSummary = totalContent.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.total(w, totalContent));

            weeks.add(WeeklyFeedbackSummaryItem.builder()
                    .weekNumber(w)
                    .koreanSummary(koreanSummary)
                    .mathSummary(mathSummary)
                    .englishSummary(englishSummary)
                    .scienceSummary(scienceSummary)
                    .totalSummary(totalSummary)
                    .build());
        }

        // 월별 요약: 해당 월 전체 피드백을 항목별로 묶어 AI 요약
        String yearMonthLabel = ym.getYear() + "년 " + ym.getMonthValue() + "월";
        String koreanContentAll = concatNonEmpty(feedbacks, Feedback::getKoreanSummary);
        String mathContentAll = concatNonEmpty(feedbacks, Feedback::getMathSummary);
        String englishContentAll = concatNonEmpty(feedbacks, Feedback::getEnglishSummary);
        String scienceContentAll = concatNonEmpty(feedbacks, Feedback::getScienceSummary);
        String totalContentAll = concatNonEmpty(feedbacks, Feedback::getTotalReview);

        MonthlyFeedbackSummaryItem monthlySummary = MonthlyFeedbackSummaryItem.builder()
                .koreanSummary(koreanContentAll.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyKorean(yearMonthLabel, koreanContentAll)))
                .mathSummary(mathContentAll.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyMath(yearMonthLabel, mathContentAll)))
                .englishSummary(englishContentAll.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyEnglish(yearMonthLabel, englishContentAll)))
                .scienceSummary(scienceContentAll.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyScience(yearMonthLabel, scienceContentAll)))
                .totalSummary(totalContentAll.isBlank() ? null : aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyTotal(yearMonthLabel, totalContentAll)))
                .build();

        return MonthlyFeedbackSummaryResponse.builder()
                .yearMonth(yearMonth)
                .weeks(weeks)
                .monthlySummary(monthlySummary)
                .build();
    }

    private static String concatNonEmpty(List<Feedback> feedbacks, java.util.function.Function<Feedback, String> getter) {
        return feedbacks.stream()
                .map(getter)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining("\n\n"));
    }

    private String buildPrompt(Planner planner) {
        List<Todo> todos = todoRepository.findByPlannerId(planner.getId());
        long totalTodos = todos.size();
        long completedTodos = todos.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("Role: You are a warm and encouraging study mentor.\n");
        sb.append("Task: Write a daily feedback for a student.\n");
        sb.append("Date: ").append(planner.getPlannerDate()).append("\n");
        sb.append("Student Comment: ").append(planner.getStudentComment()).append("\n");
        if (planner.getMoodEmoji() != null) {
            sb.append("Student Mood: ").append(planner.getMoodEmoji()).append("\n");
        }
        sb.append("Todo completion: ").append(completedTodos).append("/").append(totalTodos).append("\n");
        sb.append("Please write a short, motivating feedback in Korean.");
        return sb.toString();
    }
}
