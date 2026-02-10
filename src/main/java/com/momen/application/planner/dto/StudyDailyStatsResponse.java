package com.momen.application.planner.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 당일(또는 지정 날짜) 학습 통계.
 * 홈 화면 프로그레스바(총 학습 / 완료된 학습 / 남은 학습)용.
 */
@Getter
@Builder
public class StudyDailyStatsResponse {
    private LocalDate date;
    private int total;
    private int completed;
    private int remaining;
    private int completionRatePercent;
    private String message;

  public static String messageFor(int percent) {
    if (percent == 0) return "오늘 도전을 시작해요! ";
    if (percent < 30) return "차근차근 시작해요! ";
    if (percent < 50) return "좋은 흐름이에요, 계속 가요! ";
    if (percent < 80) return "절반 넘었어요, 조금만 더! ";
    if (percent < 100) return "거의 다 왔어요, 마지막 스퍼트! ";
    return "오늘 미션 완수! ";
  }
}
