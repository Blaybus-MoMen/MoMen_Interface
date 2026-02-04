package com.momen.application.planner;

import com.momen.application.planner.dto.*;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.mentoring.Mentor;
import com.momen.domain.planner.Planner;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.mentoring.MentorRepository;
import com.momen.infrastructure.jpa.planner.PlannerRepository;
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
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final TodoRepository todoRepository;
    private final MenteeRepository menteeRepository;
    private final MentorRepository mentorRepository;

    @Transactional
    public Long createPlanner(Long userId, PlannerCreateRequest request) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        return plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), request.getDate())
                .map(Planner::getId)
                .orElseGet(() -> {
                    Planner newPlanner = new Planner(mentee, request.getDate());
                    newPlanner.updateStudentComment(request.getStudentComment(), null);
                    return plannerRepository.save(newPlanner).getId();
                });
    }

    public PlannerResponse getPlanner(Long userId, LocalDate date) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Planner planner = plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), date)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found for date: " + date));

        List<Todo> todos = todoRepository.findByPlannerId(planner.getId());
        return PlannerResponse.from(planner, todos);
    }

    // 멘토가 멘티의 플래너 조회
    public PlannerResponse getPlannerForMentee(Long mentorUserId, Long menteeId, LocalDate date) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        Planner planner = plannerRepository.findByMenteeIdAndPlannerDate(menteeId, date)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found for date: " + date));

        List<Todo> todos = todoRepository.findByPlannerId(planner.getId());
        return PlannerResponse.from(planner, todos);
    }

    @Transactional
    public Long addTodo(Long userId, Long plannerId, TodoCreateRequest request) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));

        Todo todo = new Todo(
                planner,
                request.getTitle(),
                request.getSubject(),
                request.getGoalDescription(),
                request.getIsFixed(),
                userId,
                request.getWorksheetFileUrl()
        );
        return todoRepository.save(todo).getId();
    }

    // 멘토가 멘티에게 할 일 등록
    @Transactional
    public Long addTodoForMentee(Long mentorUserId, Long menteeId, LocalDate date, TodoCreateRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        Planner planner = getOrCreatePlanner(mentee, date);

        Todo todo = new Todo(
                planner,
                request.getTitle(),
                request.getSubject(),
                request.getGoalDescription(),
                true,
                mentor.getUser().getId(),
                request.getWorksheetFileUrl()
        );
        return todoRepository.save(todo).getId();
    }

    // 할일 일괄 생성: 한 플래너에 여러 할일을 한 번에 등록
    @Transactional
    public List<Long> addTodoBatch(Long userId, Long plannerId, TodoBatchCreateRequest request) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));
        List<Long> ids = new ArrayList<>();
        for (TodoCreateRequest item : request.getItems()) {
            Todo todo = new Todo(
                    planner,
                    item.getTitle(),
                    item.getSubject(),
                    item.getGoalDescription(),
                    item.getIsFixed() != null ? item.getIsFixed() : false,
                    userId,
                    item.getWorksheetFileUrl()
            );
            ids.add(todoRepository.save(todo).getId());
        }
        return ids;
    }

    // 멘토가 멘티의 특정 날짜에 할일 일괄 등록
    @Transactional
    public List<Long> addTodoBatchForMentee(Long mentorUserId, Long menteeId, LocalDate date, TodoBatchCreateRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        Planner planner = getOrCreatePlanner(mentee, date);
        List<Long> ids = new ArrayList<>();
        for (TodoCreateRequest item : request.getItems()) {
            Todo todo = new Todo(
                    planner,
                    item.getTitle(),
                    item.getSubject(),
                    item.getGoalDescription(),
                    true,
                    mentor.getUser().getId(),
                    item.getWorksheetFileUrl()
            );
            ids.add(todoRepository.save(todo).getId());
        }
        return ids;
    }

    // 할일 일괄 수정
    @Transactional
    public void updateTodoBatch(Long userId, TodoBatchUpdateRequest request) {
        for (TodoBatchUpdateRequest.TodoBatchUpdateItem item : request.getItems()) {
            updateTodo(userId, item.getTodoId(), item.getPatch());
        }
    }

    // Todo 업데이트 (완료 여부, 공부 시간)
    @Transactional
    public void updateTodo(Long userId, Long todoId, TodoUpdateRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found"));

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

    // 플래너 코멘트 수정
    @Transactional
    public void updatePlannerComment(Long userId, Long plannerId, PlannerCommentUpdateRequest request) {
        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new IllegalArgumentException("Planner not found"));
        planner.updateStudentComment(request.getStudentComment(), request.getMoodEmoji());
    }

    // 캘린더 조회 (기간별)
    public List<CalendarDayResponse> getCalendar(Long userId, LocalDate startDate, LocalDate endDate) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        List<Planner> planners = plannerRepository.findByMenteeIdAndPlannerDateBetween(mentee.getId(), startDate, endDate);

        return planners.stream().map(planner -> {
            List<Todo> todos = todoRepository.findByPlannerId(planner.getId());
            return CalendarDayResponse.from(planner, todos);
        }).collect(Collectors.toList());
    }

    // 멘토가 멘티의 월별 캘린더 조회 (date별 todo 묶음)
    public MenteeCalendarResponse getCalendarForMentee(Long mentorUserId, Long menteeId, String yearMonth) {
        mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Planner> planners = plannerRepository.findByMenteeIdAndPlannerDateBetween(menteeId, start, end);

        List<MenteeCalendarResponse.DayEntry> days = planners.stream()
                .map(planner -> {
                    List<TodoResponse> todos = todoRepository.findByPlannerId(planner.getId()).stream()
                            .map(TodoResponse::from)
                            .collect(Collectors.toList());
                    return MenteeCalendarResponse.DayEntry.builder()
                            .date(planner.getPlannerDate())
                            .plannerId(planner.getId())
                            .todos(todos)
                            .build();
                })
                .collect(Collectors.toList());

        return MenteeCalendarResponse.builder()
                .yearMonth(yearMonth)
                .menteeId(menteeId)
                .days(days)
                .build();
    }

    // 멘토가 멘티의 할일을 Full Sync (추가/수정/삭제 한번에 처리)
    @Transactional
    public TodoSyncResponse syncTodosForMentee(Long mentorUserId, Long menteeId, TodoSyncRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        LocalDate syncDate = request.getSyncDate();
        Planner syncPlanner = getOrCreatePlanner(mentee, syncDate);

        // DELETE: syncDate 기준 기존 todo 중 요청 목록에 없는 것 삭제
        List<Todo> existingTodos = todoRepository.findByPlannerId(syncPlanner.getId());
        Set<Long> incomingIds = request.getTodos().stream()
                .map(TodoSyncRequest.TodoSyncItem::getTodoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Todo> toDelete = existingTodos.stream()
                .filter(t -> !incomingIds.contains(t.getId()))
                .collect(Collectors.toList());
        int deletedCount = toDelete.size();
        todoRepository.deleteAll(toDelete);

        int createdCount = 0;
        int updatedCount = 0;
        List<Todo> resultTodos = new ArrayList<>();

        for (TodoSyncRequest.TodoSyncItem item : request.getTodos()) {
            if (item.getTodoId() == null) {
                // CREATE
                if (item.getRepeatDays() != null && !item.getRepeatDays().isBlank()) {
                    // 반복 생성: 오늘 ~ 월말
                    List<Todo> repeated = createRecurringTodos(mentee, mentor, item);
                    resultTodos.addAll(repeated);
                    createdCount += repeated.size();
                } else {
                    // 단건 생성
                    LocalDate todoDate = item.getDate() != null ? item.getDate() : syncDate;
                    Planner planner = getOrCreatePlanner(mentee, todoDate);
                    Todo newTodo = new Todo(
                            planner,
                            item.getTitle(),
                            item.getSubject(),
                            item.getGoalDescription(),
                            true,
                            mentor.getUser().getId(),
                            item.getWorksheetFileUrl()
                    );
                    if (Boolean.TRUE.equals(item.getMentorConfirmed())) {
                        newTodo.updateContent(item.getTitle(), item.getSubject(),
                                item.getGoalDescription(), item.getWorksheetFileUrl(), true);
                    }
                    newTodo = todoRepository.save(newTodo);
                    resultTodos.add(newTodo);
                    createdCount++;
                }
            } else {
                // UPDATE
                Todo existing = todoRepository.findById(item.getTodoId())
                        .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + item.getTodoId()));

                // 변경 감지 → 반복그룹 분리
                boolean changed = hasContentChanged(existing, item);
                if (changed && existing.getRepeatGroupId() != null) {
                    existing.detachFromRepeatGroup();
                }

                // 날짜 변경 시 planner 이동
                if (item.getDate() != null && !item.getDate().equals(existing.getPlanner().getPlannerDate())) {
                    Planner newPlanner = getOrCreatePlanner(mentee, item.getDate());
                    existing.reassignPlanner(newPlanner);
                }

                existing.updateContent(
                        item.getTitle(),
                        item.getSubject(),
                        item.getGoalDescription(),
                        item.getWorksheetFileUrl(),
                        item.getMentorConfirmed() != null ? item.getMentorConfirmed() : false
                );

                resultTodos.add(existing);
                updatedCount++;
            }
        }

        List<TodoResponse> todoResponses = resultTodos.stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());

        return TodoSyncResponse.builder()
                .plannerId(syncPlanner.getId())
                .created(createdCount)
                .updated(updatedCount)
                .deleted(deletedCount)
                .todos(todoResponses)
                .build();
    }

    // 반복 일정 생성: 오늘 ~ 월말의 해당 요일에 todo 생성
    private List<Todo> createRecurringTodos(Mentee mentee, Mentor mentor, TodoSyncRequest.TodoSyncItem item) {
        String repeatDays = item.getRepeatDays();
        Set<DayOfWeek> dayOfWeeks = Arrays.stream(repeatDays.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        String groupId = UUID.randomUUID().toString();
        LocalDate today = LocalDate.now();
        LocalDate end = YearMonth.from(today).atEndOfMonth();

        List<Todo> created = new ArrayList<>();
        for (LocalDate d = today; !d.isAfter(end); d = d.plusDays(1)) {
            if (!dayOfWeeks.contains(d.getDayOfWeek())) {
                continue;
            }
            Planner planner = getOrCreatePlanner(mentee, d);
            Todo todo = new Todo(
                    planner,
                    item.getTitle(),
                    item.getSubject(),
                    item.getGoalDescription(),
                    true,
                    mentor.getUser().getId(),
                    item.getWorksheetFileUrl()
            );
            todo.assignRepeatGroup(groupId, repeatDays);
            created.add(todoRepository.save(todo));
        }
        return created;
    }

    // 내용 변경 여부 확인
    private boolean hasContentChanged(Todo existing, TodoSyncRequest.TodoSyncItem item) {
        return !Objects.equals(existing.getTitle(), item.getTitle())
                || !Objects.equals(existing.getSubject(), item.getSubject())
                || !Objects.equals(existing.getGoalDescription(), item.getGoalDescription())
                || !Objects.equals(existing.getWorksheetFileUrl(), item.getWorksheetFileUrl())
                || (item.getDate() != null && !item.getDate().equals(existing.getPlanner().getPlannerDate()));
    }

    private Planner getOrCreatePlanner(Mentee mentee, LocalDate date) {
        return plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), date)
                .orElseGet(() -> plannerRepository.save(new Planner(mentee, date)));
    }

    // 마이페이지 (성취율 등)
    public MypageResponse getMypage(Long userId) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        // 최근 30일 플래너 조회
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<Planner> planners = plannerRepository.findByMenteeIdAndPlannerDateBetween(mentee.getId(), startDate, endDate);

        List<Todo> allTodos = new ArrayList<>();
        for (Planner p : planners) {
            allTodos.addAll(todoRepository.findByPlannerId(p.getId()));
        }

        int totalTodos = allTodos.size();
        int completedTodos = (int) allTodos.stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
        double overallRate = totalTodos > 0 ? (double) completedTodos / totalTodos * 100.0 : 0.0;

        // 과목별 성취율
        Map<String, Double> subjectRates = allTodos.stream()
                .collect(Collectors.groupingBy(Todo::getSubject))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            long total = e.getValue().size();
                            long completed = e.getValue().stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
                            return total > 0 ? Math.round((double) completed / total * 1000.0) / 10.0 : 0.0;
                        }
                ));

        int totalMinutes = allTodos.stream()
                .filter(t -> t.getStudyTime() != null)
                .mapToInt(Todo::getStudyTime)
                .sum();

        String mentorName = mentee.getMentor() != null ? mentee.getMentor().getUser().getName() : null;

        return MypageResponse.builder()
                .name(mentee.getUser().getName())
                .grade(mentee.getGrade())
                .targetUniversity(mentee.getTargetUniversity())
                .mentorName(mentorName)
                .totalTodos(totalTodos)
                .completedTodos(completedTodos)
                .overallCompletionRate(Math.round(overallRate * 10.0) / 10.0)
                .subjectCompletionRates(subjectRates)
                .totalStudyMinutes(totalMinutes)
                .build();
    }
}
