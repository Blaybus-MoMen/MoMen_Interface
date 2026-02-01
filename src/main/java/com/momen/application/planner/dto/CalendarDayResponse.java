package com.momen.application.planner.dto;

import com.momen.domain.planner.Planner;
import com.momen.domain.planner.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CalendarDayResponse {
    private Long plannerId;
    private LocalDate date;
    private String moodEmoji;
    private int totalTodos;
    private int completedTodos;
    private double completionRate;

    public static CalendarDayResponse from(Planner planner, List<Todo> todos) {
        int total = todos.size();
        int completed = (int) todos.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        double rate = total > 0 ? (double) completed / total * 100.0 : 0.0;
        return CalendarDayResponse.builder()
                .plannerId(planner.getId())
                .date(planner.getPlannerDate())
                .moodEmoji(planner.getMoodEmoji())
                .totalTodos(total)
                .completedTodos(completed)
                .completionRate(Math.round(rate * 10.0) / 10.0)
                .build();
    }
}
