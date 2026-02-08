package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class StudyTimeStatsResponse {
    private String totalHours;
    private String totalMinutes;
    private String totalSeconds;
    private Map<String, StudyTimeDetail> subjectStudyTime;

    @Getter
    @Builder
    public static class StudyTimeDetail {
        private String hours;
        private String minutes;
        private String seconds;

        public static StudyTimeDetail fromSeconds(int totalSec) {
            return StudyTimeDetail.builder()
                    .hours(fmt(totalSec / 3600))
                    .minutes(fmt((totalSec % 3600) / 60))
                    .seconds(fmt(totalSec % 60))
                    .build();
        }

        private static String fmt(int v) { return String.format("%02d", v); }
    }

    private static String fmt(int v) { return String.format("%02d", v); }
}
