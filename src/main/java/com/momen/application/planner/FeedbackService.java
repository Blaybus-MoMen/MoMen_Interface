package com.momen.application.planner;

import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.Feedback;
import com.momen.domain.planner.Planner;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.FeedbackRepository;
import com.momen.infrastructure.jpa.planner.PlannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PlannerRepository plannerRepository;
    private final MentorRepository mentorRepository;
    private final AiClient aiClient;

    @Transactional
    public String generateFeedbackDraft(Long userId, Long plannerId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));

        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseGet(() -> new Feedback(planner, mentor));

        // 1. 프롬프트 생성 (학생 데이터 취합)
        String prompt = buildPrompt(planner);
        
        // 2. AI 호출
        String aiDraft = aiClient.generateText(prompt);
        
        // 3. 결과 저장
        feedback.saveAiDraft(aiDraft);
        feedbackRepository.save(feedback);

        return aiDraft;
    }

    private String buildPrompt(Planner planner) {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: You are a warm and encouraging study mentor.\n");
        sb.append("Task: Write a daily feedback for a student.\n");
        sb.append("Date: ").append(planner.getPlannerDate()).append("\n");
        sb.append("Student Comment: ").append(planner.getStudentComment()).append("\n");
        if(planner.getMoodEmoji() != null) {
            sb.append("Student Mood: ").append(planner.getMoodEmoji()).append("\n");
        }
        // TODO: Add Todo completion rates here
        sb.append("Please write a short, motivating feedback in Korean.");
        return sb.toString();
    }
}
