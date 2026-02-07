package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class WeeklyAiSummaryRequest {
    private LocalDate weekStartDate; // 해당 주의 일요일
}
