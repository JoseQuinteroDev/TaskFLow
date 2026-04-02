package com.josequintero.taskflow.service.email;

import com.josequintero.taskflow.config.EmailProperties;
import com.josequintero.taskflow.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "provider", havingValue = "sendgrid")
public class SendGridEmailSender implements EmailSender {

    private final EmailProperties emailProperties;
    private final RestClient restClient;

    public SendGridEmailSender(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
        this.restClient = RestClient.create();
    }

    @Override
    public void send(EmailMessage message) {
        if (!StringUtils.hasText(emailProperties.getSendgrid().getApiKey())) {
            throw new BusinessException("Falta la API key de SendGrid en la configuración del backend");
        }

        Map<String, Object> payload = Map.of(
                "personalizations", List.of(
                        Map.of("to", List.of(Map.of("email", message.to())))
                ),
                "from", Map.of("email", emailProperties.getFrom()),
                "subject", message.subject(),
                "content", List.of(
                        Map.of("type", MediaType.TEXT_PLAIN_VALUE, "value", message.textBody()),
                        Map.of("type", MediaType.TEXT_HTML_VALUE, "value", message.htmlBody())
                )
        );

        restClient.post()
                .uri(emailProperties.getSendgrid().getBaseUrl() + "/mail/send")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + emailProperties.getSendgrid().getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
