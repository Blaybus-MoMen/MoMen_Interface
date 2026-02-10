package com.momen.application.planner;

import com.momen.application.notification.NotificationService;
import com.momen.application.planner.dto.TodoFeedbackRequest;
import com.momen.application.planner.dto.TodoFeedbackResponse;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.notification.NotificationType;
import com.momen.domain.planner.AssignmentSubmission;
import com.momen.domain.planner.SubmissionFile;
import com.momen.domain.planner.Todo;
import com.momen.domain.planner.TodoFeedback;
import com.momen.domain.user.User;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.AssignmentSubmissionRepository;
import com.momen.infrastructure.jpa.planner.SubmissionFileRepository;
import com.momen.infrastructure.jpa.planner.TodoFeedbackRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoFeedbackService {

    private final TodoFeedbackRepository todoFeedbackRepository;
    private final TodoRepository todoRepository;
    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final SubmissionFileRepository fileRepository;
    private final NotificationService notificationService;

    // 멘토: Todo 피드백 작성/수정
    @Transactional
    public TodoFeedbackResponse saveFeedbackByMentor(Long mentorUserId, Long todoId, TodoFeedbackRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        TodoFeedback feedback = todoFeedbackRepository.findByTodoId(todoId)
                .orElseGet(() -> todoFeedbackRepository.save(new TodoFeedback(todo)));

        feedback.updateByMentor(request.getMentorComment(), request.getAnswer());

        // 멘티에게 피드백 알림 전송
        User menteeUser = todo.getMentee().getUser();
        String message = "'" + todo.getTitle() + "' 과제에 멘토 피드백이 등록되었습니다.";
        notificationService.createAndPush(menteeUser, message, NotificationType.TODO_FEEDBACK, todoId);

        return buildResponseWithSubmission(feedback, todoId);
    }

    // 멘티: 질문 수정
    @Transactional
    public TodoFeedbackResponse updateQuestionByMentee(Long menteeUserId, Long todoId, String question) {
        Mentee mentee = menteeRepository.findByUserId(menteeUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        TodoFeedback feedback = todoFeedbackRepository.findByTodoId(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        feedback.updateQuestionByMentee(question);
        return buildResponseWithSubmission(feedback, todoId);
    }

    // Todo 피드백 조회
    public TodoFeedbackResponse getFeedback(Long todoId) {
        return todoFeedbackRepository.findByTodoId(todoId)
                .map(feedback -> buildResponseWithSubmission(feedback, todoId))
                .orElse(null);
    }

    private TodoFeedbackResponse buildResponseWithSubmission(TodoFeedback feedback, Long todoId) {
        AssignmentSubmission submission = submissionRepository.findByTodoId(todoId).orElse(null);
        List<SubmissionFile> files = submission != null
                ? fileRepository.findBySubmissionId(submission.getId())
                : List.of();
        return TodoFeedbackResponse.from(feedback, submission, files);
    }
}
