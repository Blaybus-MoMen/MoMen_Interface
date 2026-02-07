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
@Table(name = "weekly_feedbacks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentee_id", "week_start_date"})
})
public class WeeklyFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate; // 해당 주의 일요일

    @Column(name = "overall_review", columnDefinition = "TEXT")
    private String overallReview; // 멘토 총평

    @Column(name = "well_done", columnDefinition = "TEXT")
    private String wellDone; // 이번주 잘한점

    @Column(name = "to_improve", columnDefinition = "TEXT")
    private String toImprove; // 다음주 보완할점

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary; // AI 요약

    public WeeklyFeedback(Mentee mentee, Mentor mentor, LocalDate weekStartDate) {
        this.mentee = mentee;
        this.mentor = mentor;
        this.weekStartDate = weekStartDate;
    }

    public void update(String overallReview, String wellDone, String toImprove, String aiSummary) {
        this.overallReview = overallReview;
        this.wellDone = wellDone;
        this.toImprove = toImprove;
        this.aiSummary = aiSummary;
    }
}
