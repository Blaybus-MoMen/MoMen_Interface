package com.momen.application.planner;

import com.momen.application.planner.dto.WeeklyAiSummaryRequest;
import com.momen.application.planner.dto.WeeklyFeedbackRequest;
import com.momen.application.planner.dto.WeeklyFeedbackResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.WeeklyFeedback;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.WeeklyFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyFeedbackService {

    private final WeeklyFeedbackRepository weeklyFeedbackRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;

    // AI 요약 생성 (저장 X, 텍스트만 반환)
    public String generateAiSummary(WeeklyAiSummaryRequest request) {
        String prompt = buildWeeklySummaryPrompt(
                request.getOverallReview(),
                request.getWellDone(),
                request.getToImprove()
        );
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

    private String buildWeeklySummaryPrompt(String overallReview, String wellDone, String toImprove) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음은 멘토가 작성한 주간 피드백입니다. 이 내용을 학생이 이해하기 쉽게 2-3문장으로 요약해주세요.\n\n");
        sb.append("=== 멘토 총평 ===\n").append(overallReview != null ? overallReview : "없음").append("\n\n");
        sb.append("=== 이번 주 잘한 점 ===\n").append(wellDone != null ? wellDone : "없음").append("\n\n");
        sb.append("=== 다음 주 보완할 점 ===\n").append(toImprove != null ? toImprove : "없음").append("\n\n");
        sb.append("위 내용을 바탕으로 학생에게 전달할 격려와 조언이 담긴 요약을 작성해주세요.");
        return sb.toString();
    }
}
