package com.momen.presentation.admin;

import com.momen.application.admin.AdminService;
import com.momen.application.admin.dto.AdminDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        // TODO: Add Security Check (Admin Role Only)
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}
