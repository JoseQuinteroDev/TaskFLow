package com.josequintero.taskflow.service;

import com.josequintero.taskflow.dto.admin.AdminDashboardSummaryDto;
import com.josequintero.taskflow.dto.admin.AdminReminderFailureResponseDto;

import java.util.List;

public interface AdminMonitoringService {

    AdminDashboardSummaryDto getDashboardSummary();

    List<AdminReminderFailureResponseDto> getRecentReminderFailures(int limit);
}
