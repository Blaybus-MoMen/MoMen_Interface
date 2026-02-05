package com.momen.application.planner;

import com.momen.application.planner.dto.*;
import com.momen.domain.mentoring.Mentee;
import com.momen.domain.planner.Todo;
import com.momen.infrastructure.jpa.mentoring.MenteeRepository;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlannerService {

    private final TodoRepository todoRepository;
    private final MenteeRepository menteeRepository;

    // 마이페이지 (성취율 등)
    public MypageResponse getMypage(Long userId) {
        Mentee mentee = menteeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Mentee not found"));

        // 최근 30일 Todo 조회
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<Todo> allTodos = todoRepository.findByMenteeIdAndMonth(mentee.getId(), startDate, endDate);

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
