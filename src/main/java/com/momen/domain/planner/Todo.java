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
@Table(name = "todos")
public class Todo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 20)
    private String subject; // KOREAN, MATH, ENGLISH, ETC

    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "study_time")
    private Integer studyTime; // 공부 시간 (분)

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 작성자 ID (userId)

    @Enumerated(EnumType.STRING)
    @Column(name = "creator_type", nullable = false, length = 10)
    private CreatorType creatorType = CreatorType.MENTOR;

    @Column(name = "mentor_confirmed")
    private Boolean mentorConfirmed = false; // 멘토 확인 여부

    public Todo(Mentee mentee, String title, String subject, String goalDescription,
                LocalDate startDate, LocalDate endDate, Long createdBy, CreatorType creatorType) {
        this.mentee = mentee;
        this.title = title;
        this.subject = subject;
        this.goalDescription = goalDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.creatorType = creatorType;
    }

    public void complete() {
        this.isCompleted = true;
    }

    public void uncomplete() {
        this.isCompleted = false;
    }

    public void updateStudyTime(Integer studyTime) {
        this.studyTime = studyTime;
    }

    public void updateContent(String title, String subject, String goalDescription,
                              LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.subject = subject;
        this.goalDescription = goalDescription;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setMentorConfirmed(boolean confirmed) {
        this.mentorConfirmed = confirmed;
    }
}
