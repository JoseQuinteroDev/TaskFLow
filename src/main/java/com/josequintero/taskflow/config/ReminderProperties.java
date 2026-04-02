package com.josequintero.taskflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.notifications.reminders")
public class ReminderProperties {

    private boolean enabled = true;
    private String cron = "0 */15 * * * *";
    private Duration soonThreshold = Duration.ofHours(24);
    private Duration overdueWindow = Duration.ofMinutes(20);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Duration getSoonThreshold() {
        return soonThreshold;
    }

    public void setSoonThreshold(Duration soonThreshold) {
        this.soonThreshold = soonThreshold;
    }

    public Duration getOverdueWindow() {
        return overdueWindow;
    }

    public void setOverdueWindow(Duration overdueWindow) {
        this.overdueWindow = overdueWindow;
    }
}
