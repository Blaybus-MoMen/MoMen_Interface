package com.momen.presentation.mentoring;

import com.momen.application.mentoring.MentoringChatService;
import com.momen.application.planner.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/mentoring")
@RequiredArgsConstructor
public class MentoringController {

    private final FeedbackService feedbackService;
    private final MentoringChatService chatService;

    // 멘토: AI 피드백 초안 생성 요청
    @PostMapping("/planners/{plannerId}/feedback/draft")
    public ResponseEntity<String> generateFeedbackDraft(@RequestAttribute("userId") Long userId, @PathVariable Long plannerId) {
        String draft = feedbackService.generateFeedbackDraft(userId, plannerId);
        return ResponseEntity.ok(draft);
    }

    // 멘티: AI 튜터와 채팅
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithAi(@RequestAttribute("userId") Long userId, @RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = chatService.chatWithAiTutor(userId, message);
        return ResponseEntity.ok(response);
    }
}
