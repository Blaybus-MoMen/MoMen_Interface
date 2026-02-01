package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class PlannerCreateRequest {
    private LocalDate date;
    private String studentComment;
}
