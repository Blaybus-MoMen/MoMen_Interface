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
    private boolean isCompleted;
    private boolean isFixed;

    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .isCompleted(todo.getIsCompleted())
                .isFixed(todo.getIsFixed())
                .build();
    }
}
