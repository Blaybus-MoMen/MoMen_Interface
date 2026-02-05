package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MonthlyAiSummaryRequest {
    private Long menteeId;
    private Integer year;
    private Integer month;
}
