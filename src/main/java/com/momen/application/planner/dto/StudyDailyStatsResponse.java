package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 당일(또는 지정 날짜) 학습 통계.
 * 홈 화면 프로그레스바(총 학습 / 완료된 학습 / 남은 학습)용.
 */
@Getter
@Builder
public class StudyDailyStatsResponse {
    private LocalDate date;
    private int total;
    private int completed;
    private int remaining;
    private double completionRatePercent;
}
