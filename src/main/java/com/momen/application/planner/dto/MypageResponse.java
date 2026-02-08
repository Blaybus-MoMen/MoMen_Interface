package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MypageResponse {
    private String name;
    private String profileImageUrl;
    private String grade;
    private String mentorName;
    private List<String> cards;
    private int totalTodos;
    private int completedTodos;
    private int overallCompletionRate;
    private Map<String, Integer> subjectCompletionRates;
    private String totalStudyHours;
    private String totalStudyMinutes;
    private String totalStudySeconds;
    private long daysWithUs;
}
