package com.momen.application.planner.dto;

import com.momen.domain.planner.CreatorType;
import com.momen.domain.planner.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TodoSummaryResponse {
    private Long todoId;
    private String title;
    private String subject;
    private String goalDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean mentorConfirmed;
    private CreatorType creatorType;
    private Boolean isCompleted;
    private boolean hasFeedback;
    private String studyTimeHours;
    private String studyTimeMinutes;
    private String studyTimeSeconds;

    private static String fmt(int v) { return String.format("%02d", v); }

    public static TodoSummaryResponse from(Todo todo) {
        int s = todo.getStudyTime() != null ? todo.getStudyTime() : 0;
        return TodoSummaryResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .mentorConfirmed(todo.getMentorConfirmed())
                .creatorType(todo.getCreatorType())
                .isCompleted(todo.getIsCompleted())
                .studyTimeHours(fmt(s / 3600))
                .studyTimeMinutes(fmt((s % 3600) / 60))
                .studyTimeSeconds(fmt(s % 60))
                .build();
    }

    public static TodoSummaryResponse from(Todo todo, boolean hasFeedback) {
        int s = todo.getStudyTime() != null ? todo.getStudyTime() : 0;
        return TodoSummaryResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .mentorConfirmed(todo.getMentorConfirmed())
                .creatorType(todo.getCreatorType())
                .isCompleted(todo.getIsCompleted())
                .hasFeedback(hasFeedback)
                .studyTimeHours(fmt(s / 3600))
                .studyTimeMinutes(fmt((s % 3600) / 60))
                .studyTimeSeconds(fmt(s % 60))
                .build();
    }
}
