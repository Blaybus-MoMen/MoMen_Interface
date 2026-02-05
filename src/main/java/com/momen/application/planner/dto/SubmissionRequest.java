package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmissionRequest {
    private String fileUrl;
    private String fileName;
}
