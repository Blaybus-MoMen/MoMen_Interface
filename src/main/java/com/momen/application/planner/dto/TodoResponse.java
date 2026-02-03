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
    private String dayOfWeek;
    private String worksheetFileUrl;
    private Boolean mentorConfirmed;
    private Long parentTodoId;

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
                .dayOfWeek(todo.getDayOfWeek())
                .worksheetFileUrl(todo.getWorksheetFileUrl())
                .mentorConfirmed(todo.getMentorConfirmed())
                .parentTodoId(todo.getParentTodo() != null ? todo.getParentTodo().getId() : null)
                .build();
    }
}
