package com.momen.presentation.planner;

import com.momen.application.planner.AssignmentService;
import com.momen.application.planner.MistakeNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
public class StudyController {

    private final AssignmentService assignmentService;
    private final MistakeNoteService mistakeNoteService;

    // 과제 제출 (AI Vision 자동 검수 시작)
    @PostMapping("/todos/{todoId}/submit")
    public ResponseEntity<Long> submitAssignment(@RequestAttribute("userId") Long userId, @PathVariable Long todoId, @RequestParam String fileUrl) {
        Long submissionId = assignmentService.submitAssignment(userId, todoId, fileUrl);
        return ResponseEntity.ok(submissionId);
    }

    // 오답노트 생성 (AI 변형 문제 생성 시작)
    @PostMapping("/mistake-notes")
    public ResponseEntity<Long> createMistakeNote(@RequestAttribute("userId") Long userId, @RequestParam Long todoId, @RequestParam String imageUrl) {
        Long noteId = mistakeNoteService.createMistakeNote(userId, todoId, imageUrl);
        return ResponseEntity.ok(noteId);
    }
}
