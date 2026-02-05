package com.momen.application.planner.dto;

import com.momen.domain.planner.TodoFeedback;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoFeedbackResponse {
    private Long feedbackId;
    private Long todoId;
    private String studyCheck;
    private String mentorComment;
    private String qna;

    public static TodoFeedbackResponse from(TodoFeedback feedback) {
        return TodoFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .todoId(feedback.getTodo().getId())
                .studyCheck(feedback.getStudyCheck())
                .mentorComment(feedback.getMentorComment())
                .qna(feedback.getQna())
                .build();
    }
}
