package com.momen.application.planner.dto;

import com.momen.domain.planner.AssignmentSubmission;
import com.momen.domain.planner.SubmissionFile;
import com.momen.domain.planner.TodoFeedback;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodoFeedbackResponse {
    private Long feedbackId;
    private Long todoId;
    private String mentorComment;
    private String question;
    private String answer;
    private AssignmentSubmissionResponse submission;

    public static TodoFeedbackResponse from(TodoFeedback feedback) {
        return from(feedback, null, null);
    }

    public static TodoFeedbackResponse from(TodoFeedback feedback, AssignmentSubmission submission, List<SubmissionFile> files) {
        return TodoFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .todoId(feedback.getTodo().getId())
                .mentorComment(feedback.getMentorComment())
                .question(feedback.getQuestion())
                .answer(feedback.getAnswer())
                .submission(submission != null ? AssignmentSubmissionResponse.from(submission, files != null ? files : List.of()) : null)
                .build();
    }
}
