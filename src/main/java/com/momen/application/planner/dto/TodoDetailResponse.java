package com.momen.application.planner.dto;

import com.momen.domain.planner.AssignmentMaterial;
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
    private String repeatDays;
    private String repeatGroupId;
    private Boolean mentorConfirmed;
    private Boolean isCompleted;
    private Integer studyTime;
    private Long createdBy;
    private String mentorFeedback;
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

    public static TodoDetailResponse from(Todo todo, List<AssignmentMaterial> materials) {
        return TodoDetailResponse.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .subject(todo.getSubject())
                .goalDescription(todo.getGoalDescription())
                .startDate(todo.getStartDate())
                .endDate(todo.getEndDate())
                .repeatDays(todo.getRepeatDays())
                .repeatGroupId(todo.getRepeatGroupId())
                .mentorConfirmed(todo.getMentorConfirmed())
                .isCompleted(todo.getIsCompleted())
                .studyTime(todo.getStudyTime())
                .createdBy(todo.getCreatedBy())
                .mentorFeedback(todo.getMentorFeedback())
                .materials(materials.stream()
                        .map(MaterialInfo::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
