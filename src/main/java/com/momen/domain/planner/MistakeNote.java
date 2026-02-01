package com.momen.domain.planner;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.mentoring.Mentee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mistake_notes")
public class MistakeNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    private Todo todo; // 어떤 문제에서 틀렸는지 연결

    @Column(name = "question_image_url", length = 500)
    private String questionImageUrl; // 틀린 문제 사진

    // [NEW] AI가 생성한 변형 문제
    @Column(name = "ai_generated_question", columnDefinition = "TEXT")
    private String aiGeneratedQuestion;

    @Column(name = "is_solved")
    private Boolean isSolved = false;

    public MistakeNote(Mentee mentee, Todo todo, String questionImageUrl) {
        this.mentee = mentee;
        this.todo = todo;
        this.questionImageUrl = questionImageUrl;
    }

    public void updateAiQuestion(String question) {
        this.aiGeneratedQuestion = question;
    }

    public void solve() {
        this.isSolved = true;
    }
}
