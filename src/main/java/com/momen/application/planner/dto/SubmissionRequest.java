package com.momen.application.planner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmissionRequest {
    /** 업로드된 파일 URL (파일 업로드 API 호출 후 받은 URL). 파일 없이 텍스트만 제출 가능 */
    private String fileUrl;
    /** 원본 파일명 */
    private String fileName;
    /** 학습 점검 메모 (텍스트). 파일 없이 텍스트만 제출 시 사용 */
    private String memo;
}
