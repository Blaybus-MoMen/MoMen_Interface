package com.momen.presentation.notification;

import com.momen.application.notification.NotificationService;
import com.momen.application.notification.dto.NotificationResponse;
import com.momen.core.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "SSE 구독", description = "실시간 알림을 위한 SSE 연결 (query param token 사용)")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestAttribute("userId") Long userId) {
        return notificationService.subscribe(userId);
    }

    @Operation(summary = "알림 목록 조회", description = "전체 알림 목록을 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getNotifications(userId)));
    }

    @Operation(summary = "읽지 않은 알림 수 조회", description = "읽지 않은 알림 개수를 조회합니다")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnreadCount(userId)));
    }

    @Operation(summary = "전체 읽음 처리", description = "모든 알림을 읽음 처리합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "단건 읽음 처리", description = "특정 알림을 읽음 처리합니다")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
