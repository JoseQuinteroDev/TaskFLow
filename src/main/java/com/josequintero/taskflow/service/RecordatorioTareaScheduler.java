package com.josequintero.taskflow.service;

import com.josequintero.taskflow.config.ReminderProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.notifications.reminders", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RecordatorioTareaScheduler {

    private final RecordatorioTareaService recordatorioTareaService;
    private final ReminderProperties reminderProperties;

    public RecordatorioTareaScheduler(
            RecordatorioTareaService recordatorioTareaService,
            ReminderProperties reminderProperties
    ) {
        this.recordatorioTareaService = recordatorioTareaService;
        this.reminderProperties = reminderProperties;
    }

    @Scheduled(cron = "${app.notifications.reminders.cron:0 */15 * * * *}")
    public void run() {
        if (!reminderProperties.isEnabled()) {
            return;
        }

        recordatorioTareaService.procesarRecordatoriosPendientes();
    }
}
