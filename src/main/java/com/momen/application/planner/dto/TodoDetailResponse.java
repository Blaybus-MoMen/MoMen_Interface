package com.momen.application.planner.dto;

import com.momen.domain.planner.AssignmentMaterial;
import com.momen.domain.planner.CreatorType;
import com.momen.domain.planner.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class TodoDetailResponse {
    private Long todoId;
    private String title;
    private String subject;
    private String goalDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean mentorConfirmed;
    private CreatorType creatorType;
    private Boolean isCompleted;
    private boolean hasFeedback;
    private String studyTimeHours;
    private String studyTimeMinutes;
    private String studyTimeSeconds;
    private List<MaterialInfo> materials;

    @Getter
    @Builder
    public static class MaterialInfo {
        private Long materialId;
        private String fileUrl;
        private String fileName;

        public static MaterialInfo from(AssignmentMaterial material) {
            return MaterialInfo.builder()
                    .materialId(material.getId())
                    .fileUrl(material.getFileUrl())
                    .fileName(material.getFileName())
                    .build();
        }
    }

    private static String fmt(int v) { return String.format("%02d", v); }

    public static TodoDetailResponse from(Todo todo, List<AssignmentMaterial> materials) {
        return from(todo, materials, false);
    }

    public static TodoDetailResponse from(Todo todo, List<AssignmentMaterial> materials, boolean hasFeedback) {
        int s = todo.getStudyTime() != null ? todo.getStudyTime() : 0;
        return TodoDetailResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .mentorConfirmed(todo.getMentorConfirmed())
                .creatorType(todo.getCreatorType())
                .isCompleted(todo.getIsCompleted())
                .hasFeedback(hasFeedback)
                .studyTimeHours(fmt(s / 3600))
                .studyTimeMinutes(fmt((s % 3600) / 60))
                .studyTimeSeconds(fmt(s % 60))
                .materials(materials.stream()
                        .map(MaterialInfo::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
