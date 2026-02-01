package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "planners", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mentee_id", "planner_date"})
})
public class Planner extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planner_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(name = "planner_date", nullable = false)
    private LocalDate plannerDate;

    @Column(name = "student_comment", columnDefinition = "TEXT")
    private String studentComment; // 멘티의 오늘의 한마디

    // [NEW] 멘탈 케어 필드
    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1.0(부정) ~ 1.0(긍정)

    @Column(name = "mood_emoji", length = 10)
    private String moodEmoji; // 학생이 선택한 기분 이모지 (😊, 🤯, 😭)

    public Planner(Mentee mentee, LocalDate plannerDate) {
        this.mentee = mentee;
        this.plannerDate = plannerDate;
    }

    public void updateStudentComment(String studentComment, String moodEmoji) {
        this.studentComment = studentComment;
        this.moodEmoji = moodEmoji;
    }

    public void updateSentiment(Double score) {
        this.sentimentScore = score;
    }
}
