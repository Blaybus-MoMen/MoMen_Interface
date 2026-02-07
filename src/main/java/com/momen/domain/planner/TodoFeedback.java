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

    @Column(name = "mentor_comment", columnDefinition = "TEXT")
    private String mentorComment; // 멘토 피드백

    @Column(name = "question", columnDefinition = "TEXT")
    private String question; // 멘티 질문

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer; // 멘토 답변

    public TodoFeedback(Todo todo) {
        this.todo = todo;
    }

    public void updateByMentor(String mentorComment, String answer) {
        this.mentorComment = mentorComment;
        this.answer = answer;
    }

    public void updateQuestionByMentee(String question) {
        this.question = question;
    }
}
