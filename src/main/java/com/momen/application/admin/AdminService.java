package com.momen.application.admin;

import com.momen.application.admin.dto.AdminDashboardResponse;
import com.momen.domain.user.UserRole;
import com.momen.infrastructure.jpa.planner.AssignmentSubmissionRepository;
import com.momen.infrastructure.jpa.planner.FocusSessionRepository;
import com.momen.infrastructure.jpa.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserJpaRepository userJpaRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userJpaRepository.count();
        long totalMentors = userJpaRepository.countByRole(UserRole.ADMIN);
        long totalMentees = userJpaRepository.countByRole(UserRole.STUDENT);

        long todaySessions = focusSessionRepository.count();
        long aiUsageCount = submissionRepository.count();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalMentors(totalMentors)
                .totalMentees(totalMentees)
                .todayFocusSessions(todaySessions)
                .totalAiUsage(aiUsageCount)
                .build();
    }
}
