package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class TodoUpdateRequest {
    private String title;
    private String subject;
    private String goalDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCompleted;
    private Integer studyTime;
    private List<MaterialInfo> materials; // null이면 변경 없음, 빈 배열이면 전체 삭제

    @Getter
    @NoArgsConstructor
    public static class MaterialInfo {
        private String fileUrl;
        private String fileName;
    }
}
