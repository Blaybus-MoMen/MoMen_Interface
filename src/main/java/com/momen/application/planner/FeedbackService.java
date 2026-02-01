package com.momen.application.planner;

import com.momen.application.planner.dto.FeedbackRequest;
import com.momen.application.planner.dto.FeedbackResponse;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.Feedback;
import com.momen.domain.planner.Planner;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.FeedbackRepository;
import com.momen.infrastructure.jpa.planner.PlannerRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PlannerRepository plannerRepository;
    private final MentorRepository mentorRepository;
    private final TodoRepository todoRepository;
    private final AiClient aiClient;

    @Transactional
    public String generateFeedbackDraft(Long userId, Long plannerId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));

        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseGet(() -> new Feedback(planner, mentor));

        String prompt = buildPrompt(planner);
        String aiDraft = aiClient.generateText(prompt);

        feedback.saveAiDraft(aiDraft);
        feedbackRepository.save(feedback);
        return aiDraft;
    }

    // 피드백 조회
    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(Long plannerId) {
        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return FeedbackResponse.from(feedback);
    }

    // 피드백 작성/수정
    @Transactional
    public FeedbackResponse saveFeedback(Long userId, Long plannerId, FeedbackRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));

        Feedback feedback = feedbackRepository.findByPlannerId(plannerId)
                .orElseGet(() -> feedbackRepository.save(new Feedback(planner, mentor)));

        feedback.updateSummaries(
                request.getKoreanSummary(),
                request.getMathSummary(),
                request.getEnglishSummary(),
                request.getTotalReview()
        );

        if (request.getAdoptAiDraft() != null) {
            feedback.adoptAiDraft(request.getAdoptAiDraft());
        }

        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    private String buildPrompt(Planner planner) {
        List<Todo> todos = todoRepository.findByPlannerId(planner.getId());
        long totalTodos = todos.size();
        long completedTodos = todos.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();

        StringBuilder sb = new StringBuilder();
        sb.append("Role: You are a warm and encouraging study mentor.\n");
        sb.append("Task: Write a daily feedback for a student.\n");
        sb.append("Date: ").append(planner.getPlannerDate()).append("\n");
        sb.append("Student Comment: ").append(planner.getStudentComment()).append("\n");
        if (planner.getMoodEmoji() != null) {
            sb.append("Student Mood: ").append(planner.getMoodEmoji()).append("\n");
        }
        sb.append("Todo completion: ").append(completedTodos).append("/").append(totalTodos).append("\n");
        sb.append("Please write a short, motivating feedback in Korean.");
        return sb.toString();
    }
}
