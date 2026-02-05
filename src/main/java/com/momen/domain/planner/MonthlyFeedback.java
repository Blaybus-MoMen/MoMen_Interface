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
@Table(name = "monthly_feedbacks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentee_id", "year", "month"})
})
public class MonthlyFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary; // 주간피드백들의 AI 요약

    @Column(name = "mentor_comment", columnDefinition = "TEXT")
    private String mentorComment; // 멘토 보충설명

    public MonthlyFeedback(Mentee mentee, Mentor mentor, Integer year, Integer month) {
        this.mentee = mentee;
        this.mentor = mentor;
        this.year = year;
        this.month = month;
    }

    public void update(String aiSummary, String mentorComment) {
        this.aiSummary = aiSummary;
        this.mentorComment = mentorComment;
    }
}
