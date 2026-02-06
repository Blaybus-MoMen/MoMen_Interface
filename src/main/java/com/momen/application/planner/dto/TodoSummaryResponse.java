package com.momen.application.planner.dto;

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
    private String repeatDays;
    private Boolean mentorConfirmed;
    private boolean hasFeedback;

    public static TodoSummaryResponse from(Todo todo, boolean hasFeedback) {
        return TodoSummaryResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .repeatDays(todo.getRepeatDays())
                .mentorConfirmed(todo.getMentorConfirmed())
                .hasFeedback(hasFeedback)
                .build();
    }
}
