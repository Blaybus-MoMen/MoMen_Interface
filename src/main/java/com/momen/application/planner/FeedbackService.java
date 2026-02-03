package com.momen.application.planner;

import com.momen.application.planner.dto.FeedbackRequest;
import com.momen.application.planner.dto.FeedbackResponse;
import com.momen.application.planner.dto.MonthlyFeedbackSummaryItem;
import com.momen.application.planner.dto.MonthlyFeedbackSummaryResponse;
import com.momen.application.planner.dto.WeeklyFeedbackSummaryItem;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.Feedback;
import com.momen.domain.planner.FeedbackType;
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

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // AI 피드백 초안 생성
    @Transactional
    public String generateFeedbackDraft(Long userId, Long menteeId, FeedbackType feedbackType,
                                        LocalDate startDate, LocalDate endDate) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Feedback feedback = feedbackRepository
                .findByMenteeIdAndFeedbackTypeAndStartDate(menteeId, feedbackType, startDate)
                .orElseGet(() -> new Feedback(mentee, mentor, feedbackType, startDate, endDate));

        List<Planner> planners = plannerRepository.findByMenteeIdAndPlannerDateBetween(menteeId, startDate, endDate);
        List<Todo> todos = planners.isEmpty()
                ? List.of()
                : todoRepository.findByPlannerIdIn(planners.stream().map(Planner::getId).toList());

        String prompt = buildPrompt(feedbackType, startDate, endDate, planners, todos);
        String aiDraft = aiClient.generateText(prompt);

        feedback.saveAiDraft(aiDraft);
        feedbackRepository.save(feedback);
        return aiDraft;
    }

    // 피드백 단건 조회
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long menteeId, FeedbackType feedbackType, LocalDate startDate) {
        Feedback feedback = feedbackRepository
                .findByMenteeIdAndFeedbackTypeAndStartDate(menteeId, feedbackType, startDate)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return FeedbackResponse.from(feedback);
    }

    // 피드백 목록 조회
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackList(Long menteeId, FeedbackType feedbackType) {
        return feedbackRepository
                .findByMenteeIdAndFeedbackTypeOrderByStartDateDesc(menteeId, feedbackType)
                .stream()
                .map(FeedbackResponse::from)
                .toList();
    }

    // 피드백 작성/수정
    @Transactional
    public FeedbackResponse saveFeedback(Long userId, Long menteeId, FeedbackRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Feedback feedback = feedbackRepository
                .findByMenteeIdAndFeedbackTypeAndStartDate(menteeId, request.getFeedbackType(), request.getStartDate())
                .orElseGet(() -> feedbackRepository.save(
                        new Feedback(mentee, mentor, request.getFeedbackType(), request.getStartDate(), request.getEndDate())
                ));

        feedback.updateSummaries(
                request.getKoreanSummary(),
                request.getMathSummary(),
                request.getEnglishSummary(),
                request.getScienceSummary(),
                request.getTotalReview()
        );

        feedback.updateWeeklyReview(
                request.getOverallReview(),
                request.getWellDone(),
                request.getToImprove()
        );

        feedback.updateMentorComment(request.getMentorComment());

        if (request.getAdoptAiDraft() != null) {
            feedback.adoptAiDraft(request.getAdoptAiDraft());
        }

        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    // 주차별 피드백 AI 요약 생성
    @Transactional(readOnly = true)
    public MonthlyFeedbackSummaryResponse generateWeeklySummaries(Long userId, Long menteeId, String yearMonthStr) {
        mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        YearMonth ym = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        List<Feedback> weeklyFeedbacks = feedbackRepository
                .findByMenteeIdAndStartDateBetween(menteeId, monthStart, monthEnd)
                .stream()
                .filter(f -> f.getFeedbackType() == FeedbackType.WEEKLY)
                .toList();

        List<WeeklyFeedbackSummaryItem> weeks = new ArrayList<>();
        StringBuilder allKorean = new StringBuilder();
        StringBuilder allMath = new StringBuilder();
        StringBuilder allEnglish = new StringBuilder();
        StringBuilder allScience = new StringBuilder();
        StringBuilder allTotal = new StringBuilder();

        for (Feedback fb : weeklyFeedbacks) {
            int weekNumber = fb.getStartDate().get(ChronoField.ALIGNED_WEEK_OF_MONTH);

            String kSummary = fb.getKoreanSummary() != null
                    ? aiClient.generateText(FeedbackSummaryPromptTemplates.korean(weekNumber, fb.getKoreanSummary())) : null;
            String mSummary = fb.getMathSummary() != null
                    ? aiClient.generateText(FeedbackSummaryPromptTemplates.math(weekNumber, fb.getMathSummary())) : null;
            String eSummary = fb.getEnglishSummary() != null
                    ? aiClient.generateText(FeedbackSummaryPromptTemplates.english(weekNumber, fb.getEnglishSummary())) : null;
            String sSummary = fb.getScienceSummary() != null
                    ? aiClient.generateText(FeedbackSummaryPromptTemplates.science(weekNumber, fb.getScienceSummary())) : null;
            String tSummary = fb.getTotalReview() != null
                    ? aiClient.generateText(FeedbackSummaryPromptTemplates.total(weekNumber, fb.getTotalReview())) : null;

            weeks.add(WeeklyFeedbackSummaryItem.builder()
                    .weekNumber(weekNumber)
                    .koreanSummary(kSummary)
                    .mathSummary(mSummary)
                    .englishSummary(eSummary)
                    .scienceSummary(sSummary)
                    .totalSummary(tSummary)
                    .build());

            if (fb.getKoreanSummary() != null) allKorean.append(fb.getKoreanSummary()).append("\n");
            if (fb.getMathSummary() != null) allMath.append(fb.getMathSummary()).append("\n");
            if (fb.getEnglishSummary() != null) allEnglish.append(fb.getEnglishSummary()).append("\n");
            if (fb.getScienceSummary() != null) allScience.append(fb.getScienceSummary()).append("\n");
            if (fb.getTotalReview() != null) allTotal.append(fb.getTotalReview()).append("\n");
        }

        String ymLabel = yearMonthStr;
        MonthlyFeedbackSummaryItem monthlySummary = MonthlyFeedbackSummaryItem.builder()
                .koreanSummary(allKorean.length() > 0
                        ? aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyKorean(ymLabel, allKorean.toString())) : null)
                .mathSummary(allMath.length() > 0
                        ? aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyMath(ymLabel, allMath.toString())) : null)
                .englishSummary(allEnglish.length() > 0
                        ? aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyEnglish(ymLabel, allEnglish.toString())) : null)
                .scienceSummary(allScience.length() > 0
                        ? aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyScience(ymLabel, allScience.toString())) : null)
                .totalSummary(allTotal.length() > 0
                        ? aiClient.generateText(FeedbackSummaryPromptTemplates.monthlyTotal(ymLabel, allTotal.toString())) : null)
                .build();

        return MonthlyFeedbackSummaryResponse.builder()
                .yearMonth(yearMonthStr)
                .weeks(weeks)
                .monthlySummary(monthlySummary)
                .build();
    }

    private String buildPrompt(FeedbackType feedbackType, LocalDate startDate, LocalDate endDate,
                               List<Planner> planners, List<Todo> todos) {
        // 과목별 통계 집계
        Map<String, Long> totalBySubject = todos.stream()
                .collect(Collectors.groupingBy(Todo::getSubject, Collectors.counting()));
        Map<String, Long> completedBySubject = todos.stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsCompleted()))
                .collect(Collectors.groupingBy(Todo::getSubject, Collectors.counting()));

        // 학생 코멘트 집계
        List<String> studentComments = planners.stream()
                .filter(p -> p.getStudentComment() != null && !p.getStudentComment().isBlank())
                .map(p -> p.getPlannerDate() + ": " + p.getStudentComment())
                .toList();

        String periodLabel = feedbackType == FeedbackType.WEEKLY ? "주간" : "월간";

        StringBuilder sb = new StringBuilder();
        sb.append("Role: You are a warm and encouraging study mentor.\n");
        sb.append("Task: Write a ").append(periodLabel).append(" feedback for a student.\n");
        sb.append("Period: ").append(startDate).append(" ~ ").append(endDate).append("\n");
        sb.append("Total planners: ").append(planners.size()).append(" days\n\n");

        sb.append("=== Subject Statistics ===\n");
        for (String subject : totalBySubject.keySet()) {
            long total = totalBySubject.get(subject);
            long completed = completedBySubject.getOrDefault(subject, 0L);
            sb.append(subject).append(": ").append(completed).append("/").append(total).append(" completed\n");
        }

        if (!studentComments.isEmpty()) {
            sb.append("\n=== Student Comments ===\n");
            for (String comment : studentComments) {
                sb.append(comment).append("\n");
            }
        }

        sb.append("\nPlease write a ").append(periodLabel)
                .append(" feedback in Korean, covering each subject's progress and overall encouragement.");
        return sb.toString();
    }
}
