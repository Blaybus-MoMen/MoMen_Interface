package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StudyTimeStatsResponse {
    private int totalStudyMinutes;
    private Map<String, Integer> subjectStudyMinutes; // 과목별 학습시간 (분)
}
