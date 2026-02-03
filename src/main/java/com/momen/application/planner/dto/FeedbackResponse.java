package com.momen.application.planner.dto;

import com.momen.domain.planner.Feedback;
import com.momen.domain.planner.FeedbackType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FeedbackResponse {
    private Long feedbackId;
    private Long menteeId;
    private FeedbackType feedbackType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String koreanSummary;
    private String mathSummary;
    private String englishSummary;
    private String scienceSummary;
    private String totalReview;
    private String overallReview;
    private String wellDone;
    private String toImprove;
    private String mentorComment;
    private String aiGeneratedDraft;
    private Boolean isAiAdopted;

    public static FeedbackResponse from(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .menteeId(feedback.getMentee().getId())
                .feedbackType(feedback.getFeedbackType())
                .startDate(feedback.getStartDate())
                .endDate(feedback.getEndDate())
                .koreanSummary(feedback.getKoreanSummary())
                .mathSummary(feedback.getMathSummary())
                .englishSummary(feedback.getEnglishSummary())
                .scienceSummary(feedback.getScienceSummary())
                .totalReview(feedback.getTotalReview())
                .overallReview(feedback.getOverallReview())
                .wellDone(feedback.getWellDone())
                .toImprove(feedback.getToImprove())
                .mentorComment(feedback.getMentorComment())
                .aiGeneratedDraft(feedback.getAiGeneratedDraft())
                .isAiAdopted(feedback.getIsAiAdopted())
                .build();
    }
}
