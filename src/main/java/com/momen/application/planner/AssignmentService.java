package com.momen.application.planner;

import com.momen.application.planner.dto.AssignmentSubmissionResponse;
import com.momen.application.planner.dto.SubmissionRequest;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.AssignmentSubmission;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.AssignmentSubmissionRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final TodoRepository todoRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;

    // 멘티 과제 제출
    @Transactional
    public Long submitAssignment(Long userId, Long todoId, SubmissionRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        AssignmentSubmission submission = new AssignmentSubmission(todo, request.getFileUrl(), request.getFileName());
        submissionRepository.save(submission);

        analyzeSubmissionAsync(submission.getId(), request.getFileUrl());
        return submission.getId();
    }

    // 제출 상세 조회
    @Transactional(readOnly = true)
    public AssignmentSubmissionResponse getSubmission(Long submissionId) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        return AssignmentSubmissionResponse.from(submission);
    }

    // Todo별 제출 목록 조회
    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponse> getSubmissionsByTodo(Long todoId) {
        return submissionRepository.findByTodoId(todoId).stream()
                .map(AssignmentSubmissionResponse::from)
                .collect(Collectors.toList());
    }

    @Async
    @Transactional
    public void analyzeSubmissionAsync(Long submissionId, String imageUrl) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        AiClient.AiVisionResult result = aiClient.analyzeImage(imageUrl);
        submission.updateAiAnalysis(result.status(), result.densityScore(), result.comment());
    }
}
