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
    private boolean hasFeedback;
    private int studyTimeHours;
    private int studyTimeMinutes;
    private int studyTimeSeconds;

    public static TodoSummaryResponse from(Todo todo) {
        int totalSec = todo.getStudyTime() != null ? todo.getStudyTime() : 0;
        return TodoSummaryResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .mentorConfirmed(todo.getMentorConfirmed())
                .creatorType(todo.getCreatorType())
                .studyTimeHours(totalSec / 3600)
                .studyTimeMinutes((totalSec % 3600) / 60)
                .studyTimeSeconds(totalSec % 60)
                .build();
    }

    public static TodoSummaryResponse from(Todo todo, boolean hasFeedback) {
        int totalSec = todo.getStudyTime() != null ? todo.getStudyTime() : 0;
        return TodoSummaryResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .mentorConfirmed(todo.getMentorConfirmed())
                .creatorType(todo.getCreatorType())
                .hasFeedback(hasFeedback)
                .studyTimeHours(totalSec / 3600)
                .studyTimeMinutes((totalSec % 3600) / 60)
                .studyTimeSeconds(totalSec % 60)
                .build();
    }
}
