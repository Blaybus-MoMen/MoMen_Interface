package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class TodoSyncRequest {

    private LocalDate date;
    private List<TodoSyncItem> todos;

    @Getter
    @NoArgsConstructor
    public static class TodoSyncItem {
        private Long todoId; // null = 새로 생성
        private String title;
        private String subject;
        private String goalDescription;
        private String dayOfWeek;
        private String worksheetFileUrl;
        private Boolean mentorConfirmed;
    }
}
