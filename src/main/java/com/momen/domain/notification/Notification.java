package com.momen.domain.notification;

import com.momen.core.entity.BaseTimeEntity;
import com.momen.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "reference_id")
    private Long referenceId;

    public Notification(User user, String message, NotificationType type, Long referenceId) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
