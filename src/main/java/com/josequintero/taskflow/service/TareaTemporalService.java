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

    public Instant parseFechaInicio(String valor, String timezone) {
        return parseRequiredDateTime(
                valor,
                timezone,
                "La fecha de inicio es obligatoria",
                "La fecha de inicio debe tener formato ISO con fecha y hora, por ejemplo 2026-04-07T12:00 o 2026-04-07T10:00:00Z"
        );
    }

    public Instant parseFechaLimite(String valor, String timezone) {
        return parseOptionalDateTime(
                valor,
                timezone,
                "La fecha límite debe tener formato ISO con fecha y hora, por ejemplo 2026-04-07T18:00 o 2026-04-07T16:00:00Z"
        );
    }

    public Instant parseFiltroDesde(String valor, String timezone) {
        return parseFlexibleDateTime(
                valor,
                true,
                timezone,
                "La fecha de filtro debe tener formato ISO, por ejemplo 2026-04-07 o 2026-04-07T12:00"
        );
    }

    public Instant parseFiltroHasta(String valor, String timezone) {
        return parseFlexibleDateTime(
                valor,
                false,
                timezone,
                "La fecha de filtro debe tener formato ISO, por ejemplo 2026-04-07 o 2026-04-07T18:00"
        );
    }

    public LocalDateTime toUtcLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public String formatForEmail(Instant fechaHora, String timezone) {
        if (fechaHora == null) {
            return "Sin fecha límite";
        }

        ZonedDateTime zonedDateTime = fechaHora.atZone(resolveZone(timezone, null));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' HH:mm z", EMAIL_LOCALE);
        return zonedDateTime.format(formatter);
    }

    public Instant calcularMomentoRecordatorio(Instant fechaInicio, Integer minutosAntes) {
        if (fechaInicio == null || minutosAntes == null) {
            return null;
        }

        return fechaInicio.minusSeconds((long) minutosAntes * 60);
    }

    private Instant parseRequiredDateTime(
            String valor,
            String timezone,
            String emptyMessage,
            String invalidMessage
    ) {
        if (!StringUtils.hasText(valor)) {
            throw new BusinessException(emptyMessage);
        }

        return parseDateTimeValue(valor, timezone, invalidMessage);
    }

    private Instant parseOptionalDateTime(
            String valor,
            String timezone,
            String invalidMessage
    ) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }

        return parseDateTimeValue(valor, timezone, invalidMessage);
    }

    private Instant parseFlexibleDateTime(
            String valor,
            boolean inicioDelDia,
            String timezone,
            String invalidMessage
    ) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }

        String normalizado = valor.trim();
        ZoneId zoneId = resolveZone(timezone, null);

        Instant exactDateTime = tryParseExactDateTime(normalizado, zoneId);
        if (exactDateTime != null) {
            return exactDateTime;
        }

        try {
            LocalDate date = LocalDate.parse(normalizado, DATE_ONLY);
            LocalDateTime localDateTime = inicioDelDia ? date.atStartOfDay() : date.atTime(LocalTime.of(23, 59));
            return localDateTime.atZone(zoneId).toInstant();
        } catch (DateTimeParseException ex) {
            throw new BusinessException(invalidMessage);
        }
    }

    private Instant parseDateTimeValue(String valor, String timezone, String invalidMessage) {
        String normalizado = valor.trim();
        ZoneId zoneId = resolveZone(timezone, null);
        Instant parsed = tryParseExactDateTime(normalizado, zoneId);

        if (parsed != null) {
            return parsed;
        }

        throw new BusinessException(invalidMessage);
    }

    private Instant tryParseExactDateTime(String normalizado, ZoneId zoneId) {
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

        return null;
    }

    private ZoneId resolveZone(String zoneId, String fallbackZoneId) {
        if (StringUtils.hasText(zoneId)) {
            try {
                return ZoneId.of(zoneId.trim());
            } catch (DateTimeException ex) {
                throw new BusinessException("La zona horaria indicada no es válida");
            }
        }

        if (StringUtils.hasText(fallbackZoneId)) {
            try {
                return ZoneId.of(fallbackZoneId.trim());
            } catch (DateTimeException ex) {
                throw new BusinessException("La zona horaria indicada no es válida");
            }
        }

        return defaultZoneId;
    }
}
