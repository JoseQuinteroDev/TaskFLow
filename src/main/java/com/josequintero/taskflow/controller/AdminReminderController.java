package com.josequintero.taskflow.controller;

import com.josequintero.taskflow.dto.admin.AdminReminderFailureResponseDto;
import com.josequintero.taskflow.service.AdminMonitoringService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/admin/reminders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReminderController {

    private final AdminMonitoringService adminMonitoringService;

    public AdminReminderController(AdminMonitoringService adminMonitoringService) {
        this.adminMonitoringService = adminMonitoringService;
    }

    @GetMapping("/failures")
    public List<AdminReminderFailureResponseDto> getFailures(
            @RequestParam(defaultValue = "8")
            @Min(value = 1, message = "El limite minimo es 1")
            @Max(value = 20, message = "El limite maximo es 20")
            int limit
    ) {
        return adminMonitoringService.getRecentReminderFailures(limit);
    }
}
