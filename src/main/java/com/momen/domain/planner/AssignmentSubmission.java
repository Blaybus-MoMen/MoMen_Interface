package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "assignment_submissions")
public class AssignmentSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // [NEW] AI Vision 분석 결과
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_analysis_status", length = 20)
    private AnalysisStatus aiAnalysisStatus = AnalysisStatus.PENDING;

    @Column(name = "study_density_score")
    private Integer studyDensityScore; // 필기 밀도 (0~100)

    @Column(name = "ai_check_comment", columnDefinition = "TEXT")
    private String aiCheckComment; // AI 자동 검수 코멘트

    public AssignmentSubmission(Todo todo, String fileUrl, String fileName) {
        this.todo = todo;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.submittedAt = LocalDateTime.now();
    }

    public void updateAiAnalysis(AnalysisStatus status, Integer density, String comment) {
        this.aiAnalysisStatus = status;
        this.studyDensityScore = density;
        this.aiCheckComment = comment;
    }
}
