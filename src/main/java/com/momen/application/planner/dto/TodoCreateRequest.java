package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class TodoCreateRequest {
    private String title;
    private String subject; // KOREAN, MATH, ENGLISH
    private String goalDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> repeatDays; // ["MONDAY","WEDNESDAY"] - 반복 일정 시 사용, null이면 단건
    private List<MaterialInfo> materials; // 멘토가 업로드한 자료들

    @Getter
    @NoArgsConstructor
    public static class MaterialInfo {
        private String fileUrl;
        private String fileName;
    }
}
