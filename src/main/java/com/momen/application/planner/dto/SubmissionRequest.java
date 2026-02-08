package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SubmissionRequest {
    private List<FileInfo> files; // 제출 파일 목록
    private String memo;          // 학습 점검 메모

    @Getter
    @NoArgsConstructor
    public static class FileInfo {
        private String fileUrl;
        private String fileName;
    }
}
