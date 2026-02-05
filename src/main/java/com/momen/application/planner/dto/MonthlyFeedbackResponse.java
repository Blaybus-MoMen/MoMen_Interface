package com.momen.application.planner.dto;

import com.momen.domain.planner.MonthlyFeedback;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyFeedbackResponse {
    private Long feedbackId;
    private Long menteeId;
    private Integer year;
    private Integer month;
    private String aiSummary;
    private String mentorComment;

    public static MonthlyFeedbackResponse from(MonthlyFeedback feedback) {
        return MonthlyFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .menteeId(feedback.getMentee().getId())
                .year(feedback.getYear())
                .month(feedback.getMonth())
                .aiSummary(feedback.getAiSummary())
                .mentorComment(feedback.getMentorComment())
                .build();
    }
}
