package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoFeedbackRequest {
    private String mentorComment; // 멘토 피드백
    private String answer;        // 멘토 답변
}
