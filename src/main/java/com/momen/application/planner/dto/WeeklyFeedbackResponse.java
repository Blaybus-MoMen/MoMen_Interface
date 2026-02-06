package com.momen.application.planner.dto;

import com.momen.domain.planner.WeeklyFeedback;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyFeedbackResponse {
    private Long feedbackId;
    private Long menteeId;
    private Integer year;
    private Integer month;
    private Integer week;
    private String overallReview;
    private String wellDone;
    private String toImprove;
    private String aiSummary;

    public static WeeklyFeedbackResponse from(WeeklyFeedback feedback) {
        return WeeklyFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .menteeId(feedback.getMentee().getId())
                .year(feedback.getYear())
                .month(feedback.getMonth())
                .week(feedback.getWeek())
                .overallReview(feedback.getOverallReview())
                .wellDone(feedback.getWellDone())
                .toImprove(feedback.getToImprove())
                .aiSummary(feedback.getAiSummary())
                .build();
    }
}
