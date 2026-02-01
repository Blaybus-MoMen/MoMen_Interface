package com.momen.application.planner;

import com.momen.domain.planner.AssignmentSubmission;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.planner.AssignmentSubmissionRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final TodoRepository todoRepository;
    private final AiClient aiClient;

    // 1. 과제 제출 (DB 저장 후 비동기 분석 요청)
    @Transactional
    public Long submitAssignment(Long userId, Long todoId, String fileUrl) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        AssignmentSubmission submission = new AssignmentSubmission(todo, fileUrl);
        submissionRepository.save(submission);

        // 비동기 AI 분석 호출 (결과 처리는 별도 트랜잭션)
        analyzeSubmissionAsync(submission.getId(), fileUrl);

        return submission.getId();
    }

    // 2. [Async] AI 분석 로직
    @Async
    @Transactional
    public void analyzeSubmissionAsync(Long submissionId, String imageUrl) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        // AI 클라이언트 호출
        AiClient.AiVisionResult result = aiClient.analyzeImage(imageUrl);

        // 결과 업데이트
        submission.updateAiAnalysis(result.status(), result.densityScore(), result.comment());
        
        // TODO: 만약 점수가 너무 낮으면(예: < 30) 멘토에게 알림 발송 가능
    }
}
