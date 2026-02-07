package com.momen.application.planner.dto;

import com.momen.domain.planner.AnalysisStatus;
import com.momen.domain.planner.AssignmentSubmission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AssignmentSubmissionResponse {
    private Long submissionId;
    private Long todoId;
    private String fileUrl;
    private String fileName;
    private String memo;
    private LocalDateTime submittedAt;
    private AnalysisStatus aiAnalysisStatus;
    private Integer studyDensityScore;
    private String aiCheckComment;

    public static AssignmentSubmissionResponse from(AssignmentSubmission submission) {
        return AssignmentSubmissionResponse.builder()
                .submissionId(submission.getId())
                .todoId(submission.getTodo().getId())
                .fileUrl(submission.getFileUrl())
                .fileName(submission.getFileName())
                .memo(submission.getMemo())
                .submittedAt(submission.getSubmittedAt())
                .aiAnalysisStatus(submission.getAiAnalysisStatus())
                .studyDensityScore(submission.getStudyDensityScore())
                .aiCheckComment(submission.getAiCheckComment())
                .build();
    }
}
