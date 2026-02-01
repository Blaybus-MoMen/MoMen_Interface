package com.momen.application.planner;

import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.MistakeNote;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.external.ai.AiClient;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.MistakeNoteRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MistakeNoteService {

    private final MistakeNoteRepository mistakeNoteRepository;
    private final MenteeRepository menteeRepository;
    private final TodoRepository todoRepository;
    private final AiClient aiClient;

    @Transactional
    public Long createMistakeNote(Long userId, Long todoId, String imageUrl) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        MistakeNote note = new MistakeNote(mentee, todo, imageUrl);
        mistakeNoteRepository.save(note);

        // 비동기 변형 문제 생성
        generateTwinProblemAsync(note.getId());

        return note.getId();
    }

    @Async
    @Transactional
    public void generateTwinProblemAsync(Long noteId) {
        MistakeNote note = mistakeNoteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        // 프롬프트 생성 (이미지 URL은 멀티모달 모델이 처리한다고 가정)
        String prompt = "Create a similar math problem (twin problem) based on this mistake image: " + note.getQuestionImageUrl();
        
        String aiQuestion = aiClient.generateText(prompt);
        
        note.updateAiQuestion(aiQuestion);
    }
}
