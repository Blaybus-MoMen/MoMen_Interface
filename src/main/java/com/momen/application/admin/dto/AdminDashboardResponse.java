package com.momen.application.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalMentors;
    private long totalMentees;
    private long todayFocusSessions;
    private long totalAiUsage;
}
