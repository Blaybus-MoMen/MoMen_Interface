package com.momen.application.planner;

import com.momen.application.planner.dto.MonthlyAiSummaryRequest;
import com.momen.application.planner.dto.MonthlyFeedbackRequest;
import com.momen.application.planner.dto.MonthlyFeedbackResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.MonthlyFeedback;
import com.momen.domain.planner.WeeklyFeedback;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.MonthlyFeedbackRepository;
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
public class MonthlyFeedbackService {

    private final MonthlyFeedbackRepository monthlyFeedbackRepository;
    private final WeeklyFeedbackRepository weeklyFeedbackRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;

    // AI 요약 생성 (해당 월 달력의 주간피드백들 조회 → AI 요약)
    public String generateAiSummary(Long mentorUserId, Long menteeId, MonthlyAiSummaryRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
        LocalDate calendarStart = getCalendarStartDate(ym);
        LocalDate lastOfMonth = ym.atEndOfMonth();

        List<WeeklyFeedback> weeklyFeedbacks = weeklyFeedbackRepository
                .findByMenteeIdAndWeekStartDateBetweenOrderByWeekStartDate(menteeId, calendarStart, lastOfMonth);

        if (weeklyFeedbacks.isEmpty()) {
            return null;
        }

        String prompt = buildMonthlySummaryPrompt(weeklyFeedbacks, request.getYear(), request.getMonth());
        return aiClient.generateText(prompt);
    }

    // 월간 피드백 저장
    @Transactional
    public MonthlyFeedbackResponse saveFeedback(Long mentorUserId, Long menteeId, MonthlyFeedbackRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        MonthlyFeedback feedback = monthlyFeedbackRepository
                .findByMenteeIdAndYearAndMonth(menteeId, request.getYear(), request.getMonth())
                .orElseGet(() -> monthlyFeedbackRepository.save(
                        new MonthlyFeedback(mentee, mentor, request.getYear(), request.getMonth())
                ));

        feedback.update(request.getAiSummary(), request.getMentorComment());
        return MonthlyFeedbackResponse.from(feedback);
    }

    // 월간 피드백 단건 조회
    public MonthlyFeedbackResponse getFeedback(Long feedbackId) {
        MonthlyFeedback feedback = monthlyFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return MonthlyFeedbackResponse.from(feedback);
    }

    // 멘티의 월간 피드백 목록 조회 (필터: yearMonth 또는 year)
    public List<MonthlyFeedbackResponse> getFeedbackList(Long menteeId, String yearMonth, Integer year) {
        if (yearMonth != null) {
            YearMonth ym = YearMonth.parse(yearMonth);
            return monthlyFeedbackRepository.findByMenteeIdAndYearAndMonth(menteeId, ym.getYear(), ym.getMonthValue())
                    .map(MonthlyFeedbackResponse::from)
                    .map(List::of)
                    .orElse(List.of());
        } else if (year != null) {
            return monthlyFeedbackRepository.findByMenteeIdAndYearOrderByMonthDesc(menteeId, year)
                    .stream()
                    .map(MonthlyFeedbackResponse::from)
                    .toList();
        } else {
            return monthlyFeedbackRepository.findByMenteeIdOrderByYearDescMonthDesc(menteeId)
                    .stream()
                    .map(MonthlyFeedbackResponse::from)
                    .toList();
        }
    }

    // 해당 월 달력의 첫 번째 일요일 계산
    private LocalDate getCalendarStartDate(YearMonth ym) {
        LocalDate firstOfMonth = ym.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // 일=0, 월=1, ..., 토=6
        return firstOfMonth.minusDays(dayOfWeek);
    }

    private String buildMonthlySummaryPrompt(List<WeeklyFeedback> weeklyFeedbacks, int year, int month) {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("년 ").append(month).append("월의 주간 피드백들을 종합하여 ");
        sb.append("한 달간의 학습 성과를 3-4문장으로 요약해주세요.\n\n");

        int weekNum = 1;
        for (WeeklyFeedback wf : weeklyFeedbacks) {
            LocalDate weekEnd = wf.getWeekStartDate().plusDays(6);
            sb.append("=== ").append(weekNum++).append("주차 (")
              .append(wf.getWeekStartDate()).append(" ~ ").append(weekEnd).append(") ===\n");
            sb.append("총평: ").append(wf.getOverallReview() != null ? wf.getOverallReview() : "없음").append("\n");
            sb.append("잘한점: ").append(wf.getWellDone() != null ? wf.getWellDone() : "없음").append("\n");
            sb.append("보완점: ").append(wf.getToImprove() != null ? wf.getToImprove() : "없음").append("\n\n");
        }

        sb.append("위 주간 피드백들을 바탕으로 한 달간의 학습 성과와 성장 포인트, ");
        sb.append("그리고 다음 달에 집중해야 할 부분을 포함한 종합 요약을 작성해주세요.");
        return sb.toString();
    }
}
