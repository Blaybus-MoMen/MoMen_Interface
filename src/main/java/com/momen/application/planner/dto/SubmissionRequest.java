package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SubmissionRequest {
    private List<FileInfo> files; // 제출 파일 목록
    private String memo;          // 학습 점검 메모

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FileInfo {
        private String fileUrl;
        private String fileName;
    }
}
