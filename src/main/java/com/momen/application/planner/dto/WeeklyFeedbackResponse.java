package com.momen.application.planner.dto;

import com.momen.domain.planner.WeeklyFeedback;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class WeeklyFeedbackResponse {
    private Long feedbackId;
    private Long menteeId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate; // weekStartDate + 6
    private String overallReview;
    private String wellDone;
    private String toImprove;
    private String aiSummary;

    public static WeeklyFeedbackResponse from(WeeklyFeedback feedback) {
        return WeeklyFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .menteeId(feedback.getMentee().getId())
                .weekStartDate(feedback.getWeekStartDate())
                .weekEndDate(feedback.getWeekStartDate().plusDays(6))
                .overallReview(feedback.getOverallReview())
                .wellDone(feedback.getWellDone())
                .toImprove(feedback.getToImprove())
                .aiSummary(feedback.getAiSummary())
                .build();
    }
}
