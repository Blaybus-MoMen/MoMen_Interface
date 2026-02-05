package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "todo_feedbacks")
public class TodoFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_feedback_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false, unique = true)
    private Todo todo;

    @Column(name = "study_check", columnDefinition = "TEXT")
    private String studyCheck; // 학습 점검

    @Column(name = "mentor_comment", columnDefinition = "TEXT")
    private String mentorComment; // 멘토 피드백

    @Column(name = "qna", columnDefinition = "TEXT")
    private String qna; // Q&A (멘토+멘티 공동 작성)

    public TodoFeedback(Todo todo) {
        this.todo = todo;
    }

    public void updateByMentor(String studyCheck, String mentorComment, String qna) {
        this.studyCheck = studyCheck;
        this.mentorComment = mentorComment;
        this.qna = qna;
    }

    public void updateQnaByMentee(String qna) {
        this.qna = qna;
    }
}
