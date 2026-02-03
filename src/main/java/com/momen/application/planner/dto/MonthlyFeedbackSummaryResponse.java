package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 월별·주차별 피드백 AI 요약 응답.
 * 주차별(weeks) + 해당 월 전체(monthlySummary)를 한 번에 제공.
 */
@Getter
@Builder
public class MonthlyFeedbackSummaryResponse {
    /** 연월 (yyyy-MM) */
    private String yearMonth;
    /** 주차별 요약 목록 (1주차, 2주차, ...) */
    private List<WeeklyFeedbackSummaryItem> weeks;
    /** 해당 월 전체 요약 (항목별) */
    private MonthlyFeedbackSummaryItem monthlySummary;
}
