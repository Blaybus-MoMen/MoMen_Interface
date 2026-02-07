package com.momen.application.notification;

import com.momen.application.notification.dto.NotificationResponse;
import com.momen.domain.notification.Notification;
import com.momen.domain.notification.NotificationType;
import com.momen.domain.user.User;
import com.momen.infrastructure.jpa.notification.NotificationRepository;
import com.momen.infrastructure.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterManager sseEmitterManager;

    public SseEmitter subscribe(Long userId) {
        return sseEmitterManager.createEmitter(userId);
    }

    @Transactional
    public void createAndPush(User user, String message, NotificationType type, Long referenceId) {
        Notification notification = new Notification(user, message, type, referenceId);
        notificationRepository.save(notification);

        NotificationResponse response = NotificationResponse.from(notification);
        sseEmitterManager.sendToUser(user.getId(), "notification", response);
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreateDtDesc(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        notification.markAsRead();
    }
}
