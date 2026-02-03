package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedbacks")
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id", nullable = false)
    private Planner planner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    // [NEW] AI Co-pilot
    @Column(name = "ai_generated_draft", columnDefinition = "TEXT")
    private String aiGeneratedDraft; // AI가 써준 초안

    @Column(name = "is_ai_adopted")
    private Boolean isAiAdopted = false; // AI 초안 채택 여부

    @Column(name = "korean_summary", columnDefinition = "TEXT")
    private String koreanSummary;

    @Column(name = "math_summary", columnDefinition = "TEXT")
    private String mathSummary;

    @Column(name = "english_summary", columnDefinition = "TEXT")
    private String englishSummary;

    @Column(name = "science_summary", columnDefinition = "TEXT")
    private String scienceSummary;

    @Column(name = "total_review", columnDefinition = "TEXT")
    private String totalReview;

    public Feedback(Planner planner, Mentor mentor) {
        this.planner = planner;
        this.mentor = mentor;
    }

    public void updateSummaries(String korean, String math, String english, String science, String total) {
        this.koreanSummary = korean;
        this.mathSummary = math;
        this.englishSummary = english;
        this.scienceSummary = science;
        this.totalReview = total;
    }

    public void saveAiDraft(String draft) {
        this.aiGeneratedDraft = draft;
    }

    public void adoptAiDraft(boolean adopted) {
        this.isAiAdopted = adopted;
    }
}
