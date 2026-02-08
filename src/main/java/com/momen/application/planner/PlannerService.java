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
import java.time.temporal.ChronoUnit;
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
        int overallRate = totalTodos > 0 ? (int) Math.round((double) completedTodos / totalTodos * 100.0) : 0;

        // 과목별 성취율 (정수 반올림)
        Map<String, Integer> subjectRates = allTodos.stream()
                .collect(Collectors.groupingBy(Todo::getSubject))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            long total = e.getValue().size();
                            long completed = e.getValue().stream().filter(t -> Boolean.TRUE.equals(t.getIsCompleted())).count();
                            return total > 0 ? (int) Math.round((double) completed / total * 100.0) : 0;
                        }
                ));

        int totalSec = allTodos.stream()
                .filter(t -> t.getStudyTime() != null)
                .mapToInt(Todo::getStudyTime)
                .sum();

        String mentorName = mentee.getMentor() != null ? mentee.getMentor().getUser().getName() : null;

        return MypageResponse.builder()
                .name(mentee.getUser().getName())
                .profileImageUrl(mentee.getUser().getProfileImageUrl())
                .grade(mentee.getGrade())
                .mentorName(mentorName)
                .cards(mentee.getCards())
                .totalTodos(totalTodos)
                .completedTodos(completedTodos)
                .overallCompletionRate(overallRate)
                .subjectCompletionRates(subjectRates)
                .totalStudyHours(totalSec / 3600)
                .totalStudyMinutes((totalSec % 3600) / 60)
                .totalStudySeconds(totalSec % 60)
                .daysWithUs(ChronoUnit.DAYS.between(mentee.getCreateDt().toLocalDate(), LocalDate.now()) + 1)
                .build();
    }
}
