package com.josequintero.taskflow.controller;

import com.josequintero.taskflow.dto.admin.AdminDashboardSummaryDto;
import com.josequintero.taskflow.service.AdminMonitoringService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminMonitoringService adminMonitoringService;

    public AdminDashboardController(AdminMonitoringService adminMonitoringService) {
        this.adminMonitoringService = adminMonitoringService;
    }

    @GetMapping("/summary")
    public AdminDashboardSummaryDto getSummary() {
        return adminMonitoringService.getDashboardSummary();
    }
}
