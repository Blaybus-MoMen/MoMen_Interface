package com.momen.domain.openai;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OpenAI ChatGPT 대화 로그 엔티티
 */
@Entity
@Table(name = "tbl_openai_chat_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenAIChatLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_LOG_ID")
    private Long id;

    // 고유한 작업 ID (UUID)
    @Column(name = "JOB_ID", unique = true, nullable = false, length = 36)
    private String jobId;

    // 요청한 사용자 (nullable - 공개 API 지원)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    // ChatGPT 모델 (gpt-3.5-turbo, gpt-4, gpt-4-turbo 등)
    @Column(name = "MODEL", nullable = false, length = 50)
    private String model;

    // 시스템 프롬프트
    @Column(name = "SYSTEM_PROMPT", columnDefinition = "TEXT")
    private String systemPrompt;

    // 사용자 프롬프트
    @Column(name = "USER_PROMPT", nullable = false, columnDefinition = "TEXT")
    private String userPrompt;

    // AI 응답
    @Column(name = "ASSISTANT_RESPONSE", columnDefinition = "TEXT")
    private String assistantResponse;

    // 온도 설정 (0.0-2.0)
    @Column(name = "TEMPERATURE", precision = 3, scale = 2)
    private BigDecimal temperature;

    // 최대 토큰 수
    @Column(name = "MAX_TOKENS")
    private Integer maxTokens;

    // 사용된 총 토큰 수
    @Column(name = "TOKENS_USED")
    private Integer tokensUsed;

    // 프롬프트 토큰 수
    @Column(name = "PROMPT_TOKENS")
    private Integer promptTokens;

    // 완료 토큰 수
    @Column(name = "COMPLETION_TOKENS")
    private Integer completionTokens;

    // 작업 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private OpenAIChatStatus status;

    // 에러 메시지 (실패 시)
    @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
    private String errorMessage;

    // 에러 코드 (실패 시)
    @Column(name = "ERROR_CODE", length = 50)
    private String errorCode;

    // 추가 메타데이터 (JSON)
    @Column(name = "METADATA", columnDefinition = "JSON")
    private String metadata;

    @Builder
    public OpenAIChatLog(User user,
                         String model,
                         String systemPrompt,
                         String userPrompt,
                         BigDecimal temperature,
                         Integer maxTokens,
                         String metadata) {
        this.jobId = UUID.randomUUID().toString();
        this.user = user;
        this.model = model != null ? model : "gpt-4";
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.temperature = temperature != null ? temperature : new BigDecimal("0.7");
        this.maxTokens = maxTokens != null ? maxTokens : 1000;
        this.metadata = metadata;
        this.status = OpenAIChatStatus.PENDING;
    }

    // 응답 설정 (완료 시)
    public void setResponse(String assistantResponse, Integer tokensUsed, Integer promptTokens, Integer completionTokens) {
        this.assistantResponse = assistantResponse;
        this.tokensUsed = tokensUsed;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.status = OpenAIChatStatus.COMPLETED;
    }

    // 에러 설정 (실패 시)
    public void setError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = OpenAIChatStatus.FAILED;
    }

    // 상태 업데이트
    public void updateStatus(OpenAIChatStatus status) {
        this.status = status;
    }
}