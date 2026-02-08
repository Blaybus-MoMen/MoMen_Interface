package com.momen.application.planner.dto;

import com.momen.domain.planner.TodoFeedback;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoFeedbackResponse {
    private Long feedbackId;
    private Long todoId;
    private String mentorComment;
    private String question;
    private String answer;

    public static TodoFeedbackResponse from(TodoFeedback feedback) {
        return TodoFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .todoId(feedback.getTodo().getId())
                .mentorComment(feedback.getMentorComment())
                .question(feedback.getQuestion())
                .answer(feedback.getAnswer())
                .build();
    }
}
