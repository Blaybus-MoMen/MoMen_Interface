package com.momen.presentation.planner;

import com.momen.application.planner.AssignmentService;
import com.momen.application.planner.MistakeNoteService;
import com.momen.application.planner.dto.AssignmentSubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Study", description = "학습 과제 및 오답노트 API")
@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudyController {

    private final AssignmentService assignmentService;
    private final MistakeNoteService mistakeNoteService;

    @Operation(summary = "과제 제출", description = "할 일에 대한 과제를 제출합니다. AI Vision 자동 검수가 시작됩니다")
    @PostMapping("/todos/{todoId}/submit")
    public ResponseEntity<Long> submitAssignment(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "할 일 ID") @PathVariable Long todoId,
            @Parameter(description = "제출 파일 URL") @RequestParam String fileUrl) {
        return ResponseEntity.ok(assignmentService.submitAssignment(userId, todoId, fileUrl));
    }

    @Operation(summary = "제출물 상세 조회", description = "과제 제출물의 상세 정보와 AI 분석 결과를 조회합니다")
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<AssignmentSubmissionResponse> getSubmission(
            @Parameter(description = "제출물 ID") @PathVariable Long submissionId) {
        return ResponseEntity.ok(assignmentService.getSubmission(submissionId));
    }

    @Operation(summary = "Todo별 제출 목록 조회", description = "특정 할 일에 대한 제출 목록을 조회합니다")
    @GetMapping("/todos/{todoId}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponse>> getSubmissionsByTodo(
            @Parameter(description = "할 일 ID") @PathVariable Long todoId) {
        return ResponseEntity.ok(assignmentService.getSubmissionsByTodo(todoId));
    }

    @Operation(summary = "오답노트 생성", description = "오답노트를 생성하고 AI 변형 문제 생성을 시작합니다")
    @PostMapping("/mistake-notes")
    public ResponseEntity<Long> createMistakeNote(
            @RequestAttribute("userId") Long userId,
            @Parameter(description = "할 일 ID") @RequestParam Long todoId,
            @Parameter(description = "오답 이미지 URL") @RequestParam String imageUrl) {
        return ResponseEntity.ok(mistakeNoteService.createMistakeNote(userId, todoId, imageUrl));
    }
}
