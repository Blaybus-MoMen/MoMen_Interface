package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class MypageResponse {
    private String name;
    private String grade;
    private String mentorName;
    private int totalTodos;
    private int completedTodos;
    private double overallCompletionRate;
    private Map<String, Double> subjectCompletionRates;
    private int totalStudyMinutes;
}
