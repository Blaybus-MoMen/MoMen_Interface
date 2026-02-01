package com.momen.application.planner.dto;

import com.momen.domain.planner.Feedback;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedbackResponse {
    private Long feedbackId;
    private Long plannerId;
    private String koreanSummary;
    private String mathSummary;
    private String englishSummary;
    private String totalReview;
    private String aiGeneratedDraft;
    private Boolean isAiAdopted;

    public static FeedbackResponse from(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .plannerId(feedback.getPlanner().getId())
                .koreanSummary(feedback.getKoreanSummary())
                .mathSummary(feedback.getMathSummary())
                .englishSummary(feedback.getEnglishSummary())
                .totalReview(feedback.getTotalReview())
                .aiGeneratedDraft(feedback.getAiGeneratedDraft())
                .isAiAdopted(feedback.getIsAiAdopted())
                .build();
    }
}
