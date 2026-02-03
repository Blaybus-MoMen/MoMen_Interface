package com.momen.application.planner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 요일 선택 시 해당 월 전체 주차에 동일 할일을 반복 등록하는 요청.
 * 예: 2025년 2월의 매주 월/수/금에 같은 할일 템플릿으로 Todo 생성.
 */
@Getter
@NoArgsConstructor
public class TodoRepeatByWeekdaysRequest {

    /**
     * 연월 (yyyy-MM)
     */
    @NotNull(message = "연월은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "연월은 yyyy-MM 형식이어야 합니다")
    private String yearMonth;

    /**
     * 적용할 요일 (예: MONDAY, WEDNESDAY, FRIDAY)
     * java.time.DayOfWeek 이름과 동일.
     */
    @NotEmpty(message = "요일 목록은 1개 이상이어야 합니다")
    private List<String> weekdays;

    @NotNull(message = "할일 템플릿은 필수입니다")
    @Valid
    private TodoCreateRequest todoTemplate;

    public TodoRepeatByWeekdaysRequest(String yearMonth, List<String> weekdays, TodoCreateRequest todoTemplate) {
        this.yearMonth = yearMonth;
        this.weekdays = weekdays;
        this.todoTemplate = todoTemplate;
    }
}
