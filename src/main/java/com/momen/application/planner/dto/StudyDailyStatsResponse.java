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
        if (percent <= 25) return "첫 걸음을 뗐어요. 시작이 제일 어려워요!";
        if (percent <= 50) return "지금 반이나 했어요! 흐름 좋아요.";
        if (percent <= 75) return "후반부에 들어왔어요. 끝이 보여요!";
        return "마지막 스퍼트예요! 거의 완료!";
    }
}
