package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoFeedbackRequest {
    private String studyCheck;    // 학습 점검
    private String mentorComment; // 멘토 피드백
    private String qna;           // Q&A
}
