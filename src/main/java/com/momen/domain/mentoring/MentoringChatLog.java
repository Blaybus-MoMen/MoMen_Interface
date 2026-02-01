package com.momen.domain.mentoring;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.planner.Todo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_logs")
public class MentoringChatLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private Mentee mentee;

    @Column(nullable = false, length = 20)
    private String role; // 'USER', 'ASSISTANT'

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_todo_id")
    private Todo relatedTodo; // 특정 문제에 대한 질문일 경우 연결

    public MentoringChatLog(Mentee mentee, String role, String messageContent, Todo relatedTodo) {
        this.mentee = mentee;
        this.role = role;
        this.messageContent = messageContent;
        this.relatedTodo = relatedTodo;
    }
}
