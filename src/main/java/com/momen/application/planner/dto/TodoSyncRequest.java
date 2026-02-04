package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class TodoSyncRequest {

    private LocalDate syncDate; // 삭제 판단 기준 날짜
    private List<TodoSyncItem> todos;

    @Getter
    @NoArgsConstructor
    public static class TodoSyncItem {
        private Long todoId; // null = 새로 생성
        private LocalDate date; // 해당 todo의 실제 날짜
        private String title;
        private String subject;
        private String goalDescription;
        private String worksheetFileUrl;
        private Boolean mentorConfirmed;
        private String repeatDays; // 신규 생성 시에만 사용, "MONDAY,WEDNESDAY"
    }
}
