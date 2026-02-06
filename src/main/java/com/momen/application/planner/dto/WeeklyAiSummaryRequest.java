package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeeklyAiSummaryRequest {
    private Integer year;
    private Integer month;
    private Integer week; // 1~4주차
}
