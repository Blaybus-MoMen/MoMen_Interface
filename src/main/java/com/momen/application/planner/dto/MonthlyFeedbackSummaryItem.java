package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 해당 월 전체에 대한 피드백 AI 요약 (항목별).
 * 주차별 요약과 함께 한 응답에서 제공.
 */
@Getter
@Builder
public class MonthlyFeedbackSummaryItem {
    /** 국어 피드백 요약 */
    private String koreanSummary;
    /** 수학 피드백 요약 */
    private String mathSummary;
    /** 영어 피드백 요약 */
    private String englishSummary;
    /** 과학 피드백 요약 */
    private String scienceSummary;
    /** 총평 피드백 요약 */
    private String totalSummary;
}
