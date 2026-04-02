package com.josequintero.taskflow.service;

import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.model.Tarea;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
public class TareaTemporalService {

    private static final DateTimeFormatter DATE_TIME_MINUTES = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECONDS = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Locale EMAIL_LOCALE = Locale.forLanguageTag("es-ES");

    private final Clock clock;
    private final ZoneId defaultZoneId;

    public TareaTemporalService(
            Clock clock,
            @Value("${app.timezone:Europe/Madrid}") String defaultZoneId
    ) {
        this.clock = clock;
        this.defaultZoneId = ZoneId.of(defaultZoneId);
    }

    public Instant ahora() {
        return clock.instant();
    }

    public boolean estaVencida(Tarea tarea) {
        return tarea != null && tarea.estaVencida(ahora());
    }

    public String normalizeTimezone(String zoneId, String fallbackZoneId) {
        return resolveZone(zoneId, fallbackZoneId).getId();
    }

    public Instant parseFechaLimite(String valor, String timezone) {
        return parse(valor, false, timezone);
    }

    public Instant parseFiltroDesde(String valor, String timezone) {
        return parse(valor, true, timezone);
    }

    public Instant parseFiltroHasta(String valor, String timezone) {
        return parse(valor, false, timezone);
    }

    public LocalDateTime toUtcLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public String formatForEmail(Instant fechaHora, String timezone) {
        if (fechaHora == null) {
            return "Sin fecha limite";
        }

        ZonedDateTime zonedDateTime = fechaHora.atZone(resolveZone(timezone, null));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' HH:mm z", EMAIL_LOCALE);
        return zonedDateTime.format(formatter);
    }

    private Instant parse(String valor, boolean inicioDelDia, String timezone) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }

        String normalizado = valor.trim();
        ZoneId zoneId = resolveZone(timezone, null);

        try {
            return Instant.parse(normalizado);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return OffsetDateTime.parse(normalizado).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(normalizado, DATE_TIME_SECONDS)
                    .atZone(zoneId)
                    .toInstant();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(normalizado, DATE_TIME_MINUTES)
                    .atZone(zoneId)
                    .toInstant();
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate date = LocalDate.parse(normalizado, DATE_ONLY);
            LocalDateTime localDateTime = inicioDelDia ? date.atStartOfDay() : date.atTime(LocalTime.of(23, 59));
            return localDateTime.atZone(zoneId).toInstant();
        } catch (DateTimeParseException ex) {
            throw new BusinessException("La fecha limite debe tener formato ISO, por ejemplo 2026-04-02T18:30 o 2026-04-02T16:30:00Z");
        }
    }

    private ZoneId resolveZone(String zoneId, String fallbackZoneId) {
        if (StringUtils.hasText(zoneId)) {
            try {
                return ZoneId.of(zoneId.trim());
            } catch (DateTimeException ex) {
                throw new BusinessException("La zona horaria indicada no es valida");
            }
        }

        if (StringUtils.hasText(fallbackZoneId)) {
            try {
                return ZoneId.of(fallbackZoneId.trim());
            } catch (DateTimeException ex) {
                throw new BusinessException("La zona horaria indicada no es valida");
            }
        }

        return defaultZoneId;
    }
}
