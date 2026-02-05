package com.momen.application.planner;

import com.momen.application.planner.dto.TodoFeedbackRequest;
import com.momen.application.planner.dto.TodoFeedbackResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.Todo;
import com.momen.domain.planner.TodoFeedback;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.TodoFeedbackRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoFeedbackService {

    private final TodoFeedbackRepository todoFeedbackRepository;
    private final TodoRepository todoRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;

    // 멘토: Todo 피드백 작성/수정
    @Transactional
    public TodoFeedbackResponse saveFeedbackByMentor(Long mentorUserId, Long todoId, TodoFeedbackRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        TodoFeedback feedback = todoFeedbackRepository.findByTodoId(todoId)
                .orElseGet(() -> todoFeedbackRepository.save(new TodoFeedback(todo)));

        feedback.updateByMentor(request.getStudyCheck(), request.getMentorComment(), request.getQna());
        return TodoFeedbackResponse.from(feedback);
    }

    // 멘티: Q&A 수정
    @Transactional
    public TodoFeedbackResponse updateQnaByMentee(Long menteeUserId, Long todoId, String qna) {
        Mentee mentee = menteeRepository.findByUserId(menteeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        TodoFeedback feedback = todoFeedbackRepository.findByTodoId(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        feedback.updateQnaByMentee(qna);
        return TodoFeedbackResponse.from(feedback);
    }

    // Todo 피드백 조회
    public TodoFeedbackResponse getFeedback(Long todoId) {
        TodoFeedback feedback = todoFeedbackRepository.findByTodoId(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        return TodoFeedbackResponse.from(feedback);
    }
}
