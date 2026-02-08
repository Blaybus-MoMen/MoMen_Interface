package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByMenteeId(Long menteeId);

    // 일별 조회
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :date AND t.endDate >= :date ORDER BY t.startDate ASC")
    List<Todo> findByMenteeIdAndDate(@Param("menteeId") Long menteeId, @Param("date") LocalDate date);

    // 월별/주별 조회
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :endOfMonth AND t.endDate >= :startOfMonth ORDER BY t.startDate ASC")
    List<Todo> findByMenteeIdAndMonth(@Param("menteeId") Long menteeId,
                                       @Param("startOfMonth") LocalDate startOfMonth,
                                       @Param("endOfMonth") LocalDate endOfMonth);

    // 일별 조회 + 과목 필터
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :date AND t.endDate >= :date AND t.subject IN :subjects ORDER BY t.startDate ASC")
    List<Todo> findByMenteeIdAndDateAndSubjects(@Param("menteeId") Long menteeId, @Param("date") LocalDate date, @Param("subjects") List<String> subjects);

    // 월별/주별 조회 + 과목 필터
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :endOfMonth AND t.endDate >= :startOfMonth AND t.subject IN :subjects ORDER BY t.startDate ASC")
    List<Todo> findByMenteeIdAndMonthAndSubjects(@Param("menteeId") Long menteeId,
                                                  @Param("startOfMonth") LocalDate startOfMonth,
                                                  @Param("endOfMonth") LocalDate endOfMonth,
                                                  @Param("subjects") List<String> subjects);

    // 특정 날짜의 미완료 Todo 조회 (멘티, 유저 정보 함께 로딩)
    @Query("SELECT t FROM Todo t JOIN FETCH t.mentee m JOIN FETCH m.user " +
           "WHERE t.startDate <= :date AND t.endDate >= :date AND t.isCompleted = false")
    List<Todo> findIncompleteByDateWithMenteeAndUser(@Param("date") LocalDate date);

    // 주별 조회: 해당 주와 겹치는 todo
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :endOfWeek AND t.endDate >= :startOfWeek ORDER BY t.startDate ASC")
    List<Todo> findByMenteeIdAndWeek(@Param("menteeId") Long menteeId,
                                      @Param("startOfWeek") LocalDate startOfWeek,
                                      @Param("endOfWeek") LocalDate endOfWeek);

    // 주별 조회 + 과목 필터
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :endOfWeek AND t.endDate >= :startOfWeek AND t.subject IN :subjects ORDER BY t.startDate ASC")
    List<Todo> findByMenteeIdAndWeekAndSubjects(@Param("menteeId") Long menteeId,
                                                 @Param("startOfWeek") LocalDate startOfWeek,
                                                 @Param("endOfWeek") LocalDate endOfWeek,
                                                 @Param("subjects") List<String> subjects);
}
