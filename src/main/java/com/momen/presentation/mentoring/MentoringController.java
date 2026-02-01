package com.momen.presentation.mentoring;

import com.momen.application.mentoring.MentoringChatService;
import com.momen.application.planner.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Mentoring", description = "멘토링 API")
@RestController
@RequestMapping("/api/v1/mentoring")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MentoringController {

    private final FeedbackService feedbackService;
    private final MentoringChatService chatService;

    @Operation(summary = "AI 피드백 초안 생성", description = "멘토가 특정 플래너에 대한 AI 피드백 초안을 생성합니다")
    @PostMapping("/planners/{plannerId}/feedback/draft")
    public ResponseEntity<String> generateFeedbackDraft(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "플래너 ID") @PathVariable Long plannerId) {
        String draft = feedbackService.generateFeedbackDraft(userId, plannerId);
        return ResponseEntity.ok(draft);
    }

    @Operation(summary = "AI 튜터 채팅", description = "멘티가 AI 튜터와 채팅합니다")
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithAi(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = chatService.chatWithAiTutor(userId, message);
        return ResponseEntity.ok(response);
    }
}
