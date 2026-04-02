package com.josequintero.taskflow.service.email;

public record EmailMessage(
        String to,
        String subject,
        String textBody,
        String htmlBody
) {
}
