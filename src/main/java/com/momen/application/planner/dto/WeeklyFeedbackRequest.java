package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class WeeklyFeedbackRequest {
    private LocalDate weekStartDate; // 해당 주의 일요일
    private String overallReview;    // 멘토 총평
    private String wellDone;         // 이번주 잘한점
    private String toImprove;        // 다음주 보완할점
    private String aiSummary;        // AI 요약 (프론트에서 받은 값)
}
