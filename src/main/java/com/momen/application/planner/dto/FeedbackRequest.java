package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedbackRequest {
    private String koreanSummary;
    private String mathSummary;
    private String englishSummary;
    private String scienceSummary;
    private String totalReview;
    private Boolean adoptAiDraft;
}
