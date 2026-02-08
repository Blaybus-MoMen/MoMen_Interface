package com.momen.infrastructure.jpa.planner;

import com.momen.domain.planner.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByMenteeId(Long menteeId);

    // 일별 조회: 해당 날짜가 startDate ~ endDate 범위에 포함되는 todo
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :date AND t.endDate >= :date")
    List<Todo> findByMenteeIdAndDate(@Param("menteeId") Long menteeId, @Param("date") LocalDate date);

    // 월별 조회: 해당 월과 겹치는 todo (startDate가 월말 이전 AND endDate가 월초 이후)
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :endOfMonth AND t.endDate >= :startOfMonth")
    List<Todo> findByMenteeIdAndMonth(@Param("menteeId") Long menteeId,
                                       @Param("startOfMonth") LocalDate startOfMonth,
                                       @Param("endOfMonth") LocalDate endOfMonth);

    // 주별 조회: 해당 주와 겹치는 todo
    @Query("SELECT t FROM Todo t WHERE t.mentee.id = :menteeId AND t.startDate <= :endOfWeek AND t.endDate >= :startOfWeek")
    List<Todo> findByMenteeIdAndWeek(@Param("menteeId") Long menteeId,
                                      @Param("startOfWeek") LocalDate startOfWeek,
                                      @Param("endOfWeek") LocalDate endOfWeek);
}
