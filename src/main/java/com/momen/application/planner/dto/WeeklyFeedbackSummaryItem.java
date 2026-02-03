package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 한 주차에 대한 피드백 AI 요약 (항목별).
 * 프롬프트 템플릿으로 국어/수학/영어/총평 각각 생성.
 */
@Getter
@Builder
public class WeeklyFeedbackSummaryItem {
    /** 주차 (1~4, 해당 월 내) */
    private int weekNumber;
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
