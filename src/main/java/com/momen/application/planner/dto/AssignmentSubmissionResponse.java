package com.momen.application.planner.dto;

import com.momen.domain.planner.AnalysisStatus;
import com.momen.domain.planner.AssignmentSubmission;
import com.momen.domain.planner.SubmissionFile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class AssignmentSubmissionResponse {
    private Long submissionId;
    private Long todoId;
    private String memo;
    private List<FileResponse> files;
    private LocalDateTime submittedAt;
    private AnalysisStatus aiAnalysisStatus;
    private Integer studyDensityScore;
    private String aiCheckComment;

    @Getter
    @Builder
    public static class FileResponse {
        private Long fileId;
        private String fileUrl;
        private String fileName;

        public static FileResponse from(SubmissionFile file) {
            return FileResponse.builder()
                    .fileId(file.getId())
                    .fileUrl(file.getFileUrl())
                    .fileName(file.getFileName())
                    .build();
        }
    }

    public static AssignmentSubmissionResponse from(AssignmentSubmission submission, List<SubmissionFile> files) {
        return AssignmentSubmissionResponse.builder()
                .submissionId(submission.getId())
                .todoId(submission.getTodo().getId())
                .memo(submission.getMemo())
                .files(files.stream().map(FileResponse::from).collect(Collectors.toList()))
                .submittedAt(submission.getSubmittedAt())
                .aiAnalysisStatus(submission.getAiAnalysisStatus())
                .studyDensityScore(submission.getStudyDensityScore())
                .aiCheckComment(submission.getAiCheckComment())
                .build();
    }
}
