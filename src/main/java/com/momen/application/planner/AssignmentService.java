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

    /** 학습 점검하기: 파일·텍스트(메모) 제출 후 해당 Todo를 학습 완료 처리 */
    @Transactional
    public Long submitAssignment(Long userId, Long todoId, SubmissionRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        boolean hasFile = request.getFileUrl() != null && !request.getFileUrl().isBlank();
        boolean hasMemo = request.getMemo() != null && !request.getMemo().isBlank();
        if (!hasFile && !hasMemo) {
            throw new IllegalArgumentException("파일 또는 학습 메모 중 하나 이상 입력해 주세요");
        }

        String fileUrl = hasFile ? request.getFileUrl() : null;
        String fileName = (request.getFileName() != null) ? request.getFileName() : null;
        String memo = hasMemo ? request.getMemo() : null;

        AssignmentSubmission submission = new AssignmentSubmission(todo, fileUrl, fileName, memo);
        submissionRepository.save(submission);

        if (hasFile) {
            analyzeSubmissionAsync(submission.getId(), request.getFileUrl());
        }

        todo.complete();
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
