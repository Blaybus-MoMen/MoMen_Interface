package com.momen.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {

    private static final long TIMEOUT = 60 * 60 * 1000L; // 1시간
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        // 기존 연결이 있으면 제거
        SseEmitter old = emitters.remove(userId);
        if (old != null) {
            old.complete();
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for userId={}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE connection timed out for userId={}", userId);
            emitters.remove(userId);
            emitter.complete();
        });
        emitter.onError(e -> {
            log.debug("SSE connection error for userId={}: {}", userId, e.getMessage());
            emitters.remove(userId);
        });

        emitters.put(userId, emitter);

        // 연결 즉시 connect 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            log.warn("Failed to send connect event to userId={}", userId);
            emitters.remove(userId);
        }

        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("Failed to send SSE event to userId={}, removing emitter", userId);
                emitters.remove(userId);
            }
        }
    }
}
