package com.josequintero.taskflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private String provider = "log";
    private String from = "noreply@taskflow.local";
    private final SendGrid sendgrid = new SendGrid();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public SendGrid getSendgrid() {
        return sendgrid;
    }

    public static class SendGrid {
        private String apiKey;
        private String baseUrl = "https://api.sendgrid.com/v3";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
