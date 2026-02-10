package com.momen.application.notification;

import com.momen.domain.notification.NotificationType;
import com.momen.domain.planner.Todo;
import com.momen.domain.user.User;
import com.momen.infrastructure.jpa.planner.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TodoRepository todoRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void notifyIncompleteTodos() {
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            List<Todo> incompleteTodos = todoRepository.findIncompleteByDateWithMenteeAndUser(yesterday);

            if (incompleteTodos.isEmpty()) {
                log.debug("No incomplete todos found for {}", yesterday);
                return;
            }

            // 멘티(User) 기준으로 그룹핑
            Map<User, List<Todo>> grouped = incompleteTodos.stream()
                    .collect(Collectors.groupingBy(todo -> todo.getMentee().getUser()));

            for (Map.Entry<User, List<Todo>> entry : grouped.entrySet()) {
                try {
                    User user = entry.getKey();
                    int count = entry.getValue().size();
                    String message = String.format("%d월 %d일 %d개의 과제가 완료되지 않았습니다.",
                            yesterday.getMonthValue(), yesterday.getDayOfMonth(), count);

                    notificationService.createAndPush(user, message, NotificationType.TODO_INCOMPLETE, null);
                    log.debug("Sent incomplete todo notification to userId={}, count={}", user.getId(), count);
                } catch (Exception e) {
                    log.error("Failed to send notification to userId={}: {}", entry.getKey().getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute notifyIncompleteTodos scheduler: {}", e.getMessage(), e);
        }
    }
}
