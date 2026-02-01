package com.momen.application.planner.dto;

import com.momen.domain.planner.Todo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoResponse {
    private Long todoId;
    private String title;
    private String subject;
    private String goalDescription;
    private Integer studyTime;
    private boolean isCompleted;
    private boolean isFixed;
    private Long createdBy;

    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .studyTime(todo.getStudyTime())
                .isCompleted(todo.getIsCompleted())
                .isFixed(todo.getIsFixed())
                .createdBy(todo.getCreatedBy())
                .build();
    }
}
