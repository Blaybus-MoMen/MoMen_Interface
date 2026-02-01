package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "oral_tests")
public class OralTest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(nullable = false)
    private String topic; // 예: "미분계수의 정의에 대해 설명해보세요"

    @Column(name = "audio_url", length = 500)
    private String audioUrl; // 녹음 파일

    @Column(columnDefinition = "TEXT")
    private String transcription; // STT 변환 텍스트

    @Column(name = "ai_accuracy_score")
    private Integer accuracyScore; // 설명 정확도 (0~100)

    @Column(name = "ai_feedback_comment", columnDefinition = "TEXT")
    private String aiFeedbackComment;

    public OralTest(Mentee mentee, String topic, String audioUrl) {
        this.mentee = mentee;
        this.topic = topic;
        this.audioUrl = audioUrl;
    }

    public void updateResult(String text, Integer score, String feedback) {
        this.transcription = text;
        this.accuracyScore = score;
        this.aiFeedbackComment = feedback;
    }
}
