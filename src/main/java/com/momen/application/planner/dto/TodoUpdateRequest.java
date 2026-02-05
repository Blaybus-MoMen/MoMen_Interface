package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TodoUpdateRequest {
    private String title;
    private String subject;
    private String goalDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCompleted;
    private Integer studyTime;
}
