package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MonthlyFeedbackRequest {
    private Integer year;
    private Integer month;
    private String aiSummary;      // AI 요약 (프론트에서 받은 값)
    private String mentorComment;  // 멘토 보충설명
}
