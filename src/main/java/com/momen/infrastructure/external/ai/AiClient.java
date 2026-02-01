package com.momen.infrastructure.external.ai;

import com.momen.domain.planner.AnalysisStatus;

public interface AiClient {
    // Vision: 필기 밀도 및 코멘트 분석
    AiVisionResult analyzeImage(String imageUrl);

    // LLM: 텍스트 생성 (피드백 초안, 변형 문제 등)
    String generateText(String prompt);

    // Chat: 대화형 챗봇
    String chat(String systemRole, String userMessage);

    // STT & Eval: 음성 텍스트 변환 및 평가
    AiOralResult analyzeSpeech(String audioUrl, String topic);

    // DTOs for Result
    record AiVisionResult(AnalysisStatus status, int densityScore, String comment) {}
    record AiOralResult(String transcription, int accuracyScore, String feedback) {}
}
