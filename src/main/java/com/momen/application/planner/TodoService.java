package com.momen.application.planner;

import com.momen.application.planner.dto.*;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.AssignmentMaterial;
import com.momen.domain.planner.CreatorType;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.AssignmentMaterialRepository;
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

        // 멘티의 수강과목 검증
        if (!mentee.getSubjects().isEmpty() && !mentee.getSubjects().contains(request.getSubject())) {
            throw new IllegalArgumentException("멘티의 수강 과목이 아닙니다: " + request.getSubject());
        }

        List<Long> createdIds = new ArrayList<>();

        if (request.getRepeatDays() != null && !request.getRepeatDays().isEmpty()) {
            // 반복 일정 생성
            createdIds.addAll(createRepeatingTodos(mentee, mentor.getUser().getId(), CreatorType.MENTOR, request));
        } else {
            // 단건 생성
            Todo todo = new Todo(
                    mentee,
                    request.getTitle(),
                    request.getSubject(),
                    request.getGoalDescription(),
                    request.getStartDate(),
                    request.getEndDate(),
                    mentor.getUser().getId(),
                    CreatorType.MENTOR
            );
            todo = todoRepository.save(todo);
            saveMaterials(todo, request.getMaterials());
            createdIds.add(todo.getId());
        }

        return createdIds;
    }

    private List<Long> createRepeatingTodos(Mentee mentee, Long createdByUserId, CreatorType creatorType, TodoCreateRequest request) {
        Set<DayOfWeek> dayOfWeeks = request.getRepeatDays().stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

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
                        current,
                        current,
                        createdByUserId,
                        creatorType
                );
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

    // Todo 수정 (멘토용 - 멘토가 생성한 할일만)
    @Transactional
    public void updateTodoByMentor(Long mentorUserId, Long todoId, TodoUpdateRequest request) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (todo.getCreatorType() != CreatorType.MENTOR) {
            throw new IllegalArgumentException("멘토가 생성한 할일만 수정할 수 있습니다");
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

        if (request.getMaterials() != null) {
            materialRepository.deleteByTodoId(todoId);
            for (TodoUpdateRequest.MaterialInfo m : request.getMaterials()) {
                materialRepository.save(new AssignmentMaterial(todo, m.getFileUrl(), m.getFileName()));
            }
        }
    }

    // Todo 삭제 (멘토용 - 멘토가 생성한 할일만)
    @Transactional
    public void deleteTodo(Long mentorUserId, Long todoId) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (todo.getCreatorType() != CreatorType.MENTOR) {
            throw new IllegalArgumentException("멘토가 생성한 할일만 삭제할 수 있습니다");
        }

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
    public List<TodoSummaryResponse> getTodosForMenteeByDate(Long mentorUserId, Long menteeId, LocalDate date, List<String> subjects) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndDateAndSubjects(menteeId, date, subjects)
                : todoRepository.findByMenteeIdAndDate(menteeId, date);
        return todos.stream().map(todo -> TodoSummaryResponse.from(todo, false)).collect(Collectors.toList());
    }

    // 멘티의 월별 Todo 조회 (멘토용)
    public List<TodoSummaryResponse> getTodosForMenteeByMonth(Long mentorUserId, Long menteeId, String yearMonth, List<String> subjects) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndMonthAndSubjects(menteeId, start, end, subjects)
                : todoRepository.findByMenteeIdAndMonth(menteeId, start, end);
        return todos.stream().map(todo -> TodoSummaryResponse.from(todo, false)).collect(Collectors.toList());
    }

    // 멘티의 주별 Todo 조회 (멘토용)
    public List<TodoSummaryResponse> getTodosForMenteeByWeek(Long mentorUserId, Long menteeId, LocalDate weekStartDate, List<String> subjects) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        LocalDate end = weekStartDate.plusDays(6);
        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndWeekAndSubjects(menteeId, weekStartDate, end, subjects)
                : todoRepository.findByMenteeIdAndWeek(menteeId, weekStartDate, end);
        return todos.stream().map(todo -> TodoSummaryResponse.from(todo, false)).collect(Collectors.toList());
    }

    // Todo 상세 조회
    public TodoDetailResponse getTodoDetail(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        List<AssignmentMaterial> materials = materialRepository.findByTodoId(todoId);
        return TodoDetailResponse.from(todo, materials, false);
    }

    // ==================== 멘티용 API ====================

    // 본인 일별 Todo 조회
    public List<TodoSummaryResponse> getMyTodosByDate(Long userId, LocalDate date, List<String> subjects) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndDateAndSubjects(mentee.getId(), date, subjects)
                : todoRepository.findByMenteeIdAndDate(mentee.getId(), date);
        return todos.stream().map(todo -> TodoSummaryResponse.from(todo, false)).collect(Collectors.toList());
    }

    // 본인 월별 Todo 조회
    public List<TodoSummaryResponse> getMyTodosByMonth(Long userId, String yearMonth, List<String> subjects) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndMonthAndSubjects(mentee.getId(), start, end, subjects)
                : todoRepository.findByMenteeIdAndMonth(mentee.getId(), start, end);
        return todos.stream().map(todo -> TodoSummaryResponse.from(todo, false)).collect(Collectors.toList());
    }

    // 본인 주별 Todo 조회
    public List<TodoSummaryResponse> getMyTodosByWeek(Long userId, LocalDate weekStartDate, List<String> subjects) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        LocalDate weekEnd = weekStartDate.plusDays(6);

        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndWeekAndSubjects(mentee.getId(), weekStartDate, weekEnd, subjects)
                : todoRepository.findByMenteeIdAndWeek(mentee.getId(), weekStartDate, weekEnd);
        return todos.stream().map(todo -> TodoSummaryResponse.from(todo, false)).collect(Collectors.toList());
    }

    // 카드 UI용: 일별 Todo 배열 (진행상태, 학습지 포함)
    public List<TodoDetailResponse> getMyTodoCardsByDate(Long userId, LocalDate date, List<String> subjects) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndDateAndSubjects(mentee.getId(), date, subjects)
                : todoRepository.findByMenteeIdAndDate(mentee.getId(), date);
        return toDetailResponseList(todos);
    }

    /** 당일(또는 지정 날짜) 학습 통계: 총 학습, 완료된 학습, 남은 학습. 홈 프로그레스바용 */
    public StudyDailyStatsResponse getDailyStats(Long userId, LocalDate date) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Todo> todos = mentee.getSubjects().isEmpty()
                ? todoRepository.findByMenteeIdAndDate(mentee.getId(), date)
                : todoRepository.findByMenteeIdAndDateAndSubjects(mentee.getId(), date, mentee.getSubjects());
        int total = todos.size();
        int completed = (int) todos.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        int remaining = total - completed;
        int rate = total > 0 ? (int) Math.round((double) completed / total * 100.0) : 0;

        return StudyDailyStatsResponse.builder()
                .date(date)
                .total(total)
                .completed(completed)
                .remaining(remaining)
                .completionRatePercent(rate)
                .message(StudyDailyStatsResponse.messageFor(rate))
                .build();
    }

    /** 카드 UI용: 월별 Todo 배열 (진행상태, 학습지 포함) */
    public List<TodoDetailResponse> getMyTodoCardsByMonth(Long userId, String yearMonth, List<String> subjects) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Todo> todos = hasSubjects(subjects)
                ? todoRepository.findByMenteeIdAndMonthAndSubjects(mentee.getId(), start, end, subjects)
                : todoRepository.findByMenteeIdAndMonth(mentee.getId(), start, end);
        return toDetailResponseList(todos);
    }

    private List<TodoDetailResponse> toDetailResponseList(List<Todo> todos) {
        if (todos.isEmpty()) {
            return List.of();
        }
        List<Long> todoIds = todos.stream().map(Todo::getId).toList();
        List<AssignmentMaterial> allMaterials = materialRepository.findByTodoIdIn(todoIds);
        Map<Long, List<AssignmentMaterial>> materialsByTodoId = allMaterials.stream()
                .collect(Collectors.groupingBy(m -> m.getTodo().getId()));

        return todos.stream()
                .map(todo -> TodoDetailResponse.from(todo, materialsByTodoId.getOrDefault(todo.getId(), List.of()), false))
                .collect(Collectors.toList());
    }

    // Todo 생성 (멘티용)
    @Transactional
    public List<Long> createTodoByMentee(Long userId, TodoCreateRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        // 수강과목 검증
        if (!mentee.getSubjects().isEmpty() && !mentee.getSubjects().contains(request.getSubject())) {
            throw new IllegalArgumentException("수강 과목이 아닙니다: " + request.getSubject());
        }

        List<Long> createdIds = new ArrayList<>();

        if (request.getRepeatDays() != null && !request.getRepeatDays().isEmpty()) {
            createdIds.addAll(createRepeatingTodos(mentee, userId, CreatorType.MENTEE, request));
        } else {
            Todo todo = new Todo(
                    mentee,
                    request.getTitle(),
                    request.getSubject(),
                    request.getGoalDescription(),
                    request.getStartDate(),
                    request.getEndDate(),
                    userId,
                    CreatorType.MENTEE
            );
            todo = todoRepository.save(todo);
            saveMaterials(todo, request.getMaterials());
            createdIds.add(todo.getId());
        }

        return createdIds;
    }

    // Todo 내용 수정 (멘티용 - 멘티가 생성한 할일만)
    @Transactional
    public void updateTodoContentByMentee(Long userId, Long todoId, TodoUpdateRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }
        if (todo.getCreatorType() != CreatorType.MENTEE) {
            throw new IllegalArgumentException("멘티가 생성한 할일만 수정할 수 있습니다");
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

        if (request.getMaterials() != null) {
            materialRepository.deleteByTodoId(todoId);
            for (TodoUpdateRequest.MaterialInfo m : request.getMaterials()) {
                materialRepository.save(new AssignmentMaterial(todo, m.getFileUrl(), m.getFileName()));
            }
        }
    }

    // Todo 삭제 (멘티용 - 멘티가 생성한 할일만)
    @Transactional
    public void deleteTodoByMentee(Long userId, Long todoId) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }
        if (todo.getCreatorType() != CreatorType.MENTEE) {
            throw new IllegalArgumentException("멘티가 생성한 할일만 삭제할 수 있습니다");
        }

        materialRepository.deleteByTodoId(todoId);
        todoRepository.delete(todo);
    }

    // Todo별 학습 시간 추가 (타이머 측정값 누적)
    @Transactional
    public void addStudyTime(Long userId, Long todoId, int seconds) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

        if (!todo.getMentee().getId().equals(mentee.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        todo.addStudyTime(seconds);
    }

    // 학습시간 통계 조회 (일별)
    public StudyTimeStatsResponse getStudyTimeStatsByDate(Long userId, LocalDate date) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Todo> todos = mentee.getSubjects().isEmpty()
                ? todoRepository.findByMenteeIdAndDate(mentee.getId(), date)
                : todoRepository.findByMenteeIdAndDateAndSubjects(mentee.getId(), date, mentee.getSubjects());

        return buildStudyTimeStats(todos);
    }

    // 학습시간 통계 조회 (주별)
    public StudyTimeStatsResponse getStudyTimeStatsByWeek(Long userId, LocalDate weekStartDate) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        LocalDate weekEnd = weekStartDate.plusDays(6);

        List<Todo> todos = mentee.getSubjects().isEmpty()
                ? todoRepository.findByMenteeIdAndMonth(mentee.getId(), weekStartDate, weekEnd)
                : todoRepository.findByMenteeIdAndMonthAndSubjects(mentee.getId(), weekStartDate, weekEnd, mentee.getSubjects());

        return buildStudyTimeStats(todos);
    }

    // 학습시간 통계 조회 (월별)
    public StudyTimeStatsResponse getStudyTimeStatsByMonth(Long userId, String yearMonth) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Todo> todos = mentee.getSubjects().isEmpty()
                ? todoRepository.findByMenteeIdAndMonth(mentee.getId(), start, end)
                : todoRepository.findByMenteeIdAndMonthAndSubjects(mentee.getId(), start, end, mentee.getSubjects());

        return buildStudyTimeStats(todos);
    }

    private StudyTimeStatsResponse buildStudyTimeStats(List<Todo> todos) {
        int totalSec = todos.stream()
                .filter(t -> t.getStudyTime() != null)
                .mapToInt(Todo::getStudyTime)
                .sum();

        Map<String, StudyTimeStatsResponse.StudyTimeDetail> subjectStudyTime = todos.stream()
                .filter(t -> t.getStudyTime() != null)
                .collect(Collectors.groupingBy(
                        Todo::getSubject,
                        Collectors.summingInt(Todo::getStudyTime)
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> StudyTimeStatsResponse.StudyTimeDetail.fromSeconds(e.getValue())
                ));

        return StudyTimeStatsResponse.builder()
                .totalHours(String.format("%02d", totalSec / 3600))
                .totalMinutes(String.format("%02d", (totalSec % 3600) / 60))
                .totalSeconds(String.format("%02d", totalSec % 60))
                .subjectStudyTime(subjectStudyTime)
                .build();
    }

    private boolean hasSubjects(List<String> subjects) {
        return subjects != null && !subjects.isEmpty();
    }
}
