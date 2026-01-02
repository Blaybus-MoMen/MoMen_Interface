package com.blaybus.domain.openai;

import com.blaybus.core.entity.BaseTimeEntity;
import com.blaybus.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * OpenAI DALL-E 이미지 생성 로그 엔티티
 */
@Entity
@Table(name = "tbl_dalle_generation_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DalleGenerationLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DALLE_LOG_ID")
    private Long id;

    // 고유한 작업 ID (UUID)
    @Column(name = "JOB_ID", unique = true, nullable = false, length = 36)
    private String jobId;

    // 요청한 사용자 (nullable - 공개 API 지원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    // DALL-E 모델 (dall-e-2, dall-e-3)
    @Column(name = "MODEL", nullable = false, length = 50)
    private String model;

    // 이미지 생성 프롬프트
    @Column(name = "PROMPT", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    // DALL-E가 수정한 프롬프트 (dall-e-3 전용)
    @Column(name = "REVISED_PROMPT", columnDefinition = "TEXT")
    private String revisedPrompt;

    // 이미지 크기 (256x256, 512x512, 1024x1024, 1792x1024, 1024x1792)
    @Column(name = "SIZE", length = 20)
    private String size;

    // 품질 (standard, hd - dall-e-3 전용)
    @Column(name = "QUALITY", length = 20)
    private String quality;

    // 스타일 (vivid, natural - dall-e-3 전용)
    @Column(name = "STYLE", length = 20)
    private String style;

    // 시드 값 (재현성)
    @Column(name = "SEED")
    private Long seed;

    // 생성된 이미지 URL
    @Column(name = "IMAGE_URL", length = 1000)
    private String imageUrl;

    // Base64 인코딩된 이미지 (선택적)
    @Column(name = "B64_JSON", columnDefinition = "TEXT")
    private String b64Json;

    // 작업 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private DalleGenerationStatus status;

    // 에러 메시지 (실패 시)
    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    // 에러 코드 (실패 시)
    @Column(name = "ERROR_CODE", length = 50)
    private String errorCode;

    // 저작권 관련 플래그
    @Column(name = "COPYRIGHT_FLAG")
    private Boolean copyrightFlag;

    // 안전 필터 작동 여부
    @Column(name = "SAFETY_FILTER_TRIGGERED")
    private Boolean safetyFilterTriggered;

    // 추가 메타데이터 (JSON)
    @Column(name = "METADATA", columnDefinition = "JSON")
    private String metadata;

    @Builder
    public DalleGenerationLog(User user,
                              String model,
                              String prompt,
                              String size,
                              String quality,
                              String style,
                              Long seed,
                              String metadata) {
        this.jobId = UUID.randomUUID().toString();
        this.user = user;
        this.model = model != null ? model : "dall-e-3";
        this.prompt = prompt;
        this.size = size != null ? size : "1024x1024";
        this.quality = quality != null ? quality : "standard";
        this.style = style != null ? style : "vivid";
        this.seed = seed;
        this.metadata = metadata;
        this.status = DalleGenerationStatus.PENDING;
        this.copyrightFlag = false;
        this.safetyFilterTriggered = false;
    }

    // 이미지 생성 완료 설정
    public void setImageGenerated(String imageUrl, String revisedPrompt, String b64Json) {
        this.imageUrl = imageUrl;
        this.revisedPrompt = revisedPrompt;
        this.b64Json = b64Json;
        this.status = DalleGenerationStatus.COMPLETED;
    }

    // 에러 설정 (실패 시)
    public void setError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = DalleGenerationStatus.FAILED;
    }

    // 저작권 플래그 설정
    public void setCopyrightFlag(Boolean copyrightFlag) {
        this.copyrightFlag = copyrightFlag;
    }

    // 안전 필터 트리거 설정
    public void setSafetyFilterTriggered(Boolean safetyFilterTriggered) {
        this.safetyFilterTriggered = safetyFilterTriggered;
    }

    // 상태 업데이트
    public void updateStatus(DalleGenerationStatus status) {
        this.status = status;
    }
}
