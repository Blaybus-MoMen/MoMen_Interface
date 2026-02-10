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

    @Scheduled(cron = "0 */3 * * * *", zone = "Asia/Seoul") // TODO: 테스트 후 "0 0 0 * * *"로 복구
    public void notifyIncompleteTodos() {
        try {
            LocalDate today = LocalDate.now();
            List<Todo> incompleteTodos = todoRepository.findAllPastIncompleteWithMenteeAndUser(today);

            if (incompleteTodos.isEmpty()) {
                log.debug("No past incomplete todos found");
                return;
            }

            // 멘티(User) 기준으로 그룹핑 → 각 멘티 내에서 endDate별로 다시 그룹핑
            Map<User, Map<LocalDate, List<Todo>>> grouped = incompleteTodos.stream()
                    .collect(Collectors.groupingBy(
                            todo -> todo.getMentee().getUser(),
                            Collectors.groupingBy(Todo::getEndDate)
                    ));

            for (Map.Entry<User, Map<LocalDate, List<Todo>>> userEntry : grouped.entrySet()) {
                try {
                    User user = userEntry.getKey();
                    for (Map.Entry<LocalDate, List<Todo>> dateEntry : userEntry.getValue().entrySet()) {
                        LocalDate date = dateEntry.getKey();
                        int count = dateEntry.getValue().size();
                        String message = String.format("%d월 %d일 %d개의 과제가 완료되지 않았습니다.",
                                date.getMonthValue(), date.getDayOfMonth(), count);

                        notificationService.createAndPush(user, message, NotificationType.TODO_INCOMPLETE, null);
                    }
                    log.debug("Sent incomplete todo notifications to userId={}", user.getId());
                } catch (Exception e) {
                    log.error("Failed to send notification to userId={}: {}", userEntry.getKey().getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to execute notifyIncompleteTodos scheduler: {}", e.getMessage(), e);
        }
    }
}
