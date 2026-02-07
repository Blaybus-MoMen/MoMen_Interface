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

    public static TodoSummaryResponse from(Todo todo) {
        return TodoSummaryResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .mentorConfirmed(todo.getMentorConfirmed())
                .creatorType(todo.getCreatorType())
                .build();
    }

    public static TodoSummaryResponse from(Todo todo, boolean hasFeedback) {
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
                .build();
    }
}
