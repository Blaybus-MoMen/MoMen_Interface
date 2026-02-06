package com.momen.application.planner;

import com.momen.application.planner.dto.*;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.AssignmentMaterial;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.AssignmentMaterialRepository;
import com.momen.infrastructure.jpa.planner.TodoFeedbackRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoFeedbackRepository todoFeedbackRepository;
    private final AssignmentMaterialRepository materialRepository;
    private final MenteeRepository menteeRepository;
    private final MentorRepository mentorRepository;

    // ==================== 멘토용 API ====================

    // Todo 생성 (단건 or 반복)
    @Transactional
    public List<Long> createTodo(Long mentorUserId, Long menteeId, TodoCreateRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Long> createdIds = new ArrayList<>();

        if (request.getRepeatDays() != null && !request.getRepeatDays().isBlank()) {
            // 반복 일정 생성
            createdIds.addAll(createRepeatingTodos(mentee, mentor, request));
        } else {
            // 단건 생성
            Todo todo = new Todo(
                    mentee,
                    request.getTitle(),
                    request.getSubject(),
                    request.getGoalDescription(),
                    request.getStartDate(),
                    request.getEndDate(),
                    mentor.getUser().getId()
            );
            todo = todoRepository.save(todo);
            saveMaterials(todo, request.getMaterials());
            createdIds.add(todo.getId());
        }

        return createdIds;
    }

    private List<Long> createRepeatingTodos(Mentee mentee, Mentor mentor, TodoCreateRequest request) {
        Set<DayOfWeek> dayOfWeeks = Arrays.stream(request.getRepeatDays().split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        String groupId = UUID.randomUUID().toString();
        List<Long> ids = new ArrayList<>();

        LocalDate current = request.getStartDate();
        LocalDate end = request.getEndDate();

        while (!current.isAfter(end)) {
            if (dayOfWeeks.contains(current.getDayOfWeek())) {
                Todo todo = new Todo(
                        mentee,
                        request.getTitle(),
                        request.getSubject(),
                        request.getGoalDescription(),
                        current, // 반복 일정은 해당일 단일
                        current,
                        mentor.getUser().getId()
                );
                todo.assignRepeatGroup(groupId, request.getRepeatDays());
                todo = todoRepository.save(todo);
                saveMaterials(todo, request.getMaterials());
                ids.add(todo.getId());
            }
            current = current.plusDays(1);
        }

        return ids;
    }

    private void saveMaterials(Todo todo, List<TodoCreateRequest.MaterialInfo> materials) {
        if (materials == null || materials.isEmpty()) return;
        for (TodoCreateRequest.MaterialInfo m : materials) {
            materialRepository.save(new AssignmentMaterial(todo, m.getFileUrl(), m.getFileName()));
        }
    }

    // Todo 수정 (멘토용)
    @Transactional
    public void updateTodoByMentor(Long mentorUserId, Long todoId, TodoUpdateRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        // 반복 그룹에서 분리
        if (todo.getRepeatGroupId() != null) {
            todo.detachFromRepeatGroup();
        }

        if (request.getTitle() != null) {
            todo.updateContent(
                    request.getTitle(),
                    request.getSubject(),
                    request.getGoalDescription(),
                    request.getStartDate(),
                    request.getEndDate()
            );
        }
    }

    // Todo 삭제
    @Transactional
    public void deleteTodo(Long mentorUserId, Long todoId) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        materialRepository.deleteByTodoId(todoId);
        todoRepository.delete(todo);
    }

    // Todo 확인 (멘토 확인 토글)
    @Transactional
    public void confirmTodo(Long mentorUserId, Long todoId, boolean confirmed) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        todo.setMentorConfirmed(confirmed);
    }

    // 멘티의 일별 Todo 조회 (멘토용)
    public List<TodoSummaryResponse> getTodosForMenteeByDate(Long mentorUserId, Long menteeId, LocalDate date) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        List<Todo> todos = todoRepository.findByMenteeIdAndDate(menteeId, date);
        return toSummaryWithFeedbackFlag(todos);
    }

    // 멘티의 월별 Todo 조회 (멘토용)
    public List<TodoSummaryResponse> getTodosForMenteeByMonth(Long mentorUserId, Long menteeId, String yearMonth) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Todo> todos = todoRepository.findByMenteeIdAndMonth(menteeId, start, end);
        return toSummaryWithFeedbackFlag(todos);
    }

    // Todo 상세 조회
    public TodoDetailResponse getTodoDetail(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        List<AssignmentMaterial> materials = materialRepository.findByTodoId(todoId);
        boolean hasFeedback = todoFeedbackRepository.existsByTodoId(todoId);
        return TodoDetailResponse.from(todo, materials, hasFeedback);
    }

    // ==================== 멘티용 API ====================

    // 본인 일별 Todo 조회
    public List<TodoSummaryResponse> getMyTodosByDate(Long userId, LocalDate date) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Todo> todos = todoRepository.findByMenteeIdAndDate(mentee.getId(), date);
        return toSummaryWithFeedbackFlag(todos);
    }

    // 본인 월별 Todo 조회
    public List<TodoSummaryResponse> getMyTodosByMonth(Long userId, String yearMonth) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Todo> todos = todoRepository.findByMenteeIdAndMonth(mentee.getId(), start, end);
        return toSummaryWithFeedbackFlag(todos);
    }

    // Todo 완료 처리 / 공부 시간 기록 (멘티용)
    @Transactional
    public void updateTodoByMentee(Long userId, Long todoId, TodoUpdateRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        if (request.getIsCompleted() != null) {
            if (Boolean.TRUE.equals(request.getIsCompleted())) {
                todo.complete();
            } else {
                todo.uncomplete();
            }
        }

        if (request.getStudyTime() != null) {
            todo.updateStudyTime(request.getStudyTime());
        }
    }

    // ==================== 공통 ====================

    private List<TodoSummaryResponse> toSummaryWithFeedbackFlag(List<Todo> todos) {
        if (todos.isEmpty()) return List.of();

        List<Long> todoIds = todos.stream().map(Todo::getId).toList();
        Set<Long> feedbackTodoIds = new HashSet<>(todoFeedbackRepository.findTodoIdsWithFeedback(todoIds));

        return todos.stream()
                .map(todo -> TodoSummaryResponse.from(todo, feedbackTodoIds.contains(todo.getId())))
                .collect(Collectors.toList());
    }
}
