package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StudyTimeStatsResponse {
    private int totalHours;
    private int totalMinutes;
    private int totalSeconds;
    private Map<String, StudyTimeDetail> subjectStudyTime;

    @Getter
    @Builder
    public static class StudyTimeDetail {
        private int hours;
        private int minutes;
        private int seconds;

        public static StudyTimeDetail fromSeconds(int totalSec) {
            return StudyTimeDetail.builder()
                    .hours(totalSec / 3600)
                    .minutes((totalSec % 3600) / 60)
                    .seconds(totalSec % 60)
                    .build();
        }
    }
}
