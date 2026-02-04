package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MenteeCalendarResponse {
    private String yearMonth;
    private Long menteeId;
    private List<DayEntry> days;

    @Getter
    @Builder
    public static class DayEntry {
        private LocalDate date;
        private Long plannerId;
        private List<TodoResponse> todos;
    }
}
