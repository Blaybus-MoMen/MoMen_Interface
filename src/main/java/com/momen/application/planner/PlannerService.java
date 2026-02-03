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
                userId
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

        Planner planner = plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), date)
                .orElseGet(() -> plannerRepository.save(new Planner(mentee, date)));

        Todo todo = new Todo(
                planner,
                request.getTitle(),
                request.getSubject(),
                request.getGoalDescription(),
                true, // isFixed = true (멘토 지정)
                mentor.getUser().getId()
        );
        return todoRepository.save(todo).getId();
    }

    /** 할일 일괄 생성: 한 플래너에 여러 할일을 한 번에 등록 */
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
                    userId
            );
            ids.add(todoRepository.save(todo).getId());
        }
        return ids;
    }

    /** 멘토가 멘티의 특정 날짜에 할일 일괄 등록 */
    @Transactional
    public List<Long> addTodoBatchForMentee(Long mentorUserId, Long menteeId, LocalDate date, TodoBatchCreateRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));
        Planner planner = plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), date)
                .orElseGet(() -> plannerRepository.save(new Planner(mentee, date)));
        List<Long> ids = new ArrayList<>();
        for (TodoCreateRequest item : request.getItems()) {
            Todo todo = new Todo(
                    planner,
                    item.getTitle(),
                    item.getSubject(),
                    item.getGoalDescription(),
                    true,
                    mentor.getUser().getId()
            );
            ids.add(todoRepository.save(todo).getId());
        }
        return ids;
    }

    /** 요일 선택 시 해당 월 전체 주차에 동일 할일 반복 등록 (멘토 → 멘티) */
    @Transactional
    public List<Long> addTodosForMonthByWeekdays(Long mentorUserId, Long menteeId, TodoRepeatByWeekdaysRequest request) {
        Mentor mentor = mentorRepository.findByUserId(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"));
        Mentee mentee = menteeRepository.findById(menteeId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        YearMonth ym = YearMonth.parse(request.getYearMonth());
        Set<DayOfWeek> dayOfWeeks = request.getWeekdays().stream()
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());

        List<Long> ids = new ArrayList<>();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (!dayOfWeeks.contains(d.getDayOfWeek())) {
                continue;
            }
            final LocalDate date = d;
            Planner planner = plannerRepository.findByMenteeIdAndPlannerDate(mentee.getId(), date)
                    .orElseGet(() -> plannerRepository.save(new Planner(mentee, date)));
            TodoCreateRequest t = request.getTodoTemplate();
            Todo todo = new Todo(
                    planner,
                    t.getTitle(),
                    t.getSubject(),
                    t.getGoalDescription(),
                    true,
                    mentor.getUser().getId()
            );
            ids.add(todoRepository.save(todo).getId());
        }
        return ids;
    }

    /** 할일 일괄 수정 */
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
