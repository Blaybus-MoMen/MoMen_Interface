package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weekly_feedbacks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentee_id", "year", "month", "week"})
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

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer week; // 1~4주차

    @Column(name = "overall_review", columnDefinition = "TEXT")
    private String overallReview; // 멘토 총평

    @Column(name = "well_done", columnDefinition = "TEXT")
    private String wellDone; // 이번주 잘한점

    @Column(name = "to_improve", columnDefinition = "TEXT")
    private String toImprove; // 다음주 보완할점

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary; // AI 요약

    public WeeklyFeedback(Mentee mentee, Mentor mentor, Integer year, Integer month, Integer week) {
        this.mentee = mentee;
        this.mentor = mentor;
        this.year = year;
        this.month = month;
        this.week = week;
    }

    public void update(String overallReview, String wellDone, String toImprove, String aiSummary) {
        this.overallReview = overallReview;
        this.wellDone = wellDone;
        this.toImprove = toImprove;
        this.aiSummary = aiSummary;
    }
}
