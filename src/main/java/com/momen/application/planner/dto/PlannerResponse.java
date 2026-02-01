package com.momen.application.planner.dto;

import com.momen.domain.planner.Planner;
import com.momen.domain.planner.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PlannerResponse {
    private Long plannerId;
    private LocalDate date;
    private String studentComment;
    private Double sentimentScore;
    private String moodEmoji;
    private List<TodoResponse> todos;

    public static PlannerResponse from(Planner planner, List<Todo> todos) {
        return PlannerResponse.builder()
                .plannerId(planner.getId())
                .date(planner.getPlannerDate())
                .studentComment(planner.getStudentComment())
                .sentimentScore(planner.getSentimentScore())
                .moodEmoji(planner.getMoodEmoji())
                .todos(todos.stream().map(TodoResponse::from).collect(Collectors.toList()))
                .build();
    }
}
