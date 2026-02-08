package com.momen.application.planner;

import com.momen.application.planner.dto.AssignmentSubmissionResponse;
import com.momen.application.planner.dto.SubmissionRequest;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.AssignmentSubmission;
import com.momen.domain.planner.SubmissionFile;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.AssignmentSubmissionRepository;
import com.momen.infrastructure.jpa.planner.SubmissionFileRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final SubmissionFileRepository fileRepository;
    private final TodoRepository todoRepository;
    private final MenteeRepository menteeRepository;
    private final AiClient aiClient;

    /** 과제 제출 (생성 또는 수정) - Todo당 1건만 존재 */
    @Transactional
    public AssignmentSubmissionResponse submitAssignment(Long userId, Long todoId, SubmissionRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        boolean hasFiles = request.getFiles() != null && !request.getFiles().isEmpty();
        boolean hasMemo = request.getMemo() != null && !request.getMemo().isBlank();
        if (!hasFiles && !hasMemo) {
            throw new IllegalArgumentException("파일 또는 학습 메모 중 하나 이상 입력해 주세요");
        }

        // 기존 제출이 있으면 수정, 없으면 새로 생성
        Optional<AssignmentSubmission> existing = submissionRepository.findByTodoId(todoId);
        AssignmentSubmission submission;

        if (existing.isPresent()) {
            submission = existing.get();
            submission.updateContent(request.getMemo());
            // 기존 파일 삭제 후 새 파일 저장
            fileRepository.deleteBySubmissionId(submission.getId());
        } else {
            submission = new AssignmentSubmission(todo, request.getMemo());
            submissionRepository.save(submission);
        }

        // 파일 저장
        if (hasFiles) {
            for (SubmissionRequest.FileInfo f : request.getFiles()) {
                fileRepository.save(new SubmissionFile(submission, f.getFileUrl(), f.getFileName()));
            }
        }

        todo.complete();

        // 파일이 있으면 첫 번째 파일로 AI 분석
        if (hasFiles) {
            analyzeSubmissionAsync(submission.getId(), request.getFiles().get(0).getFileUrl());
        }

        List<SubmissionFile> files = fileRepository.findBySubmissionId(submission.getId());
        return AssignmentSubmissionResponse.from(submission, files);
    }

    /** Todo별 제출물 조회 */
    @Transactional(readOnly = true)
    public AssignmentSubmissionResponse getSubmissionByTodo(Long todoId) {
        AssignmentSubmission submission = submissionRepository.findByTodoId(todoId)
                .orElseThrow(() -> new IllegalArgumentException("제출물이 없습니다"));

        List<SubmissionFile> files = fileRepository.findBySubmissionId(submission.getId());
        return AssignmentSubmissionResponse.from(submission, files);
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
