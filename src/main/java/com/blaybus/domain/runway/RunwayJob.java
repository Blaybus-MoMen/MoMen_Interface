package com.blaybus.domain.runway;

import com.blaybus.core.entity.BaseTimeEntity;
import com.blaybus.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Runway 비디오 생성 작업 엔티티
 */
@Entity
@Table(name = "tbl_runway_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunwayJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RUNWAY_JOB_ID")
    private Long id;

    // 고유한 작업 ID (UUID) - 내부 사용
    @Column(name = "JOB_ID", unique = true, nullable = false, length = 36)
    private String jobId;

    // Runway API의 task ID
    @Column(name = "TASK_ID", unique = true, nullable = false, length = 36)
    private String taskId;

    // 요청한 사용자 (nullable - 공개 API 지원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    // 프롬프트
    @Column(name = "PROMPT", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    // 작업 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private RunwayJobStatus status;

    // 진행률 (0-100)
    @Column(name = "PROGRESS")
    private Integer progress;

    // 생성된 비디오 URL (완료 시)
    @Column(name = "VIDEO_URL", length = 2000)
    private String videoUrl;

    // 에러 정보 (실패 시)
    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    // 에러 코드 (실패 시)
    @Column(name = "ERROR_CODE", length = 50)
    private String errorCode;

    @Builder
    public RunwayJob(User user,
                     String prompt,
                     String taskId) {
        this.jobId = UUID.randomUUID().toString();
        this.taskId = taskId;
        this.user = user;
        this.prompt = prompt;
        this.status = RunwayJobStatus.PENDING;
    }

    public void updateStatus(RunwayJobStatus status) {
        this.status = status;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
        this.status = RunwayJobStatus.SUCCEEDED;
    }

    public void setError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = RunwayJobStatus.FAILED;
    }

    public void markAsRunning() {
        this.status = RunwayJobStatus.RUNNING;
    }
}

