package com.momen.application.notification.dto;

import com.momen.domain.notification.Notification;
import com.momen.domain.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private NotificationType type;
    private Boolean isRead;
    private Long referenceId;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreateDt())
                .build();
    }
}
