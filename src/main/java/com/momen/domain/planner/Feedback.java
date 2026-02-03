package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feedbacks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentee_id", "feedback_type", "start_date"})
})
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 10)
    private FeedbackType feedbackType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

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

    @Column(name = "overall_review", columnDefinition = "TEXT")
    private String overallReview;

    @Column(name = "well_done", columnDefinition = "TEXT")
    private String wellDone;

    @Column(name = "to_improve", columnDefinition = "TEXT")
    private String toImprove;

    @Column(name = "mentor_comment", columnDefinition = "TEXT")
    private String mentorComment;

    public Feedback(Mentee mentee, Mentor mentor, FeedbackType feedbackType, LocalDate startDate, LocalDate endDate) {
        this.mentee = mentee;
        this.mentor = mentor;
        this.feedbackType = feedbackType;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public void updateWeeklyReview(String overallReview, String wellDone, String toImprove) {
        this.overallReview = overallReview;
        this.wellDone = wellDone;
        this.toImprove = toImprove;
    }

    public void updateMentorComment(String mentorComment) {
        this.mentorComment = mentorComment;
    }
}
