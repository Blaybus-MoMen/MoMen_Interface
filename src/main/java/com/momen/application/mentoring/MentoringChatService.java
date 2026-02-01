package com.momen.application.mentoring;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.MentoringChatLog;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.ChatLogRepository;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoringChatService {

    private final ChatLogRepository chatLogRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;

    @Transactional
    public String chatWithAiTutor(Long userId, String userMessage) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        // 유저 질문 저장
        chatLogRepository.save(new MentoringChatLog(mentee, "USER", userMessage, null));

        // AI 답변 생성 (System Prompt 설정)
        String systemRole = "You are a helpful study tutor. Do not give the answer directly, but provide hints using the Socratic method.";
        String aiResponse = aiClient.chat(systemRole, userMessage);

        // AI 답변 저장
        chatLogRepository.save(new MentoringChatLog(mentee, "ASSISTANT", aiResponse, null));

        return aiResponse;
    }
}
