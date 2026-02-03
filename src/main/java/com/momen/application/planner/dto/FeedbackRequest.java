package com.momen.application.planner.dto;

import com.momen.domain.planner.FeedbackType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class FeedbackRequest {
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
    private Boolean adoptAiDraft;
}
