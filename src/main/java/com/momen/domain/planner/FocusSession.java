package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "focus_sessions")
public class FocusSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 감지된 부정적 행동 횟수
    @Column(name = "drowsiness_count")
    private Integer drowsinessCount = 0; // 졸음 감지 횟수

    @Column(name = "phone_use_count")
    private Integer phoneUseCount = 0; // 스마트폰 사용 감지 횟수

    @Column(name = "focus_score")
    private Integer focusScore; // 100점 만점 환산 점수

    public FocusSession(Mentee mentee) {
        this.mentee = mentee;
        this.startTime = LocalDateTime.now();
    }

    public void endSession(Integer drowsiness, Integer phoneUse) {
        this.endTime = LocalDateTime.now();
        this.drowsinessCount = drowsiness;
        this.phoneUseCount = phoneUse;
        this.focusScore = calculateScore(drowsiness, phoneUse);
    }

    private Integer calculateScore(int drowsy, int phone) {
        // 간단한 로직: 100점에서 감점
        int score = 100 - (drowsy * 2) - (phone * 5);
        return Math.max(0, score);
    }
}
