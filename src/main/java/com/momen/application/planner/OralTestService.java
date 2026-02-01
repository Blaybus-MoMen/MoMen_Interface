package com.momen.application.planner;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.OralTest;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.OralTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OralTestService {

    private final OralTestRepository oralTestRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;

    @Transactional
    public Long submitOralTest(Long userId, String topic, String audioUrl) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        OralTest test = new OralTest(mentee, topic, audioUrl);
        oralTestRepository.save(test);

        // 비동기 채점
        evaluateSpeechAsync(test.getId(), audioUrl, topic);

        return test.getId();
    }

    @Async
    @Transactional
    public void evaluateSpeechAsync(Long testId, String audioUrl, String topic) {
        OralTest test = oralTestRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found"));

        // AI 분석 (STT + Eval)
        AiClient.AiOralResult result = aiClient.analyzeSpeech(audioUrl, topic);

        test.updateResult(result.transcription(), result.accuracyScore(), result.feedback());
    }
}
