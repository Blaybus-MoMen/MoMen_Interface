package com.momen.domain.notification;

public enum NotificationType {
    TODO_INCOMPLETE,   // 자정 스케줄러: 미완료 과제
    TODO_FEEDBACK,     // 멘토 Todo 피드백 등록/수정
    WEEKLY_FEEDBACK,   // 멘토 주간 피드백 등록/수정
    MONTHLY_FEEDBACK   // 멘토 월간 피드백 등록/수정
}
