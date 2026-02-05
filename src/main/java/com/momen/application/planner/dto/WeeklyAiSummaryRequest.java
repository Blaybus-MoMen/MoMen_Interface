package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyAiSummaryRequest {
    private String overallReview;  // 멘토 총평
    private String wellDone;       // 이번주 잘한점
    private String toImprove;      // 다음주 보완할점
}
