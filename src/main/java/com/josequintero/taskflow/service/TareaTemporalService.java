package com.josequintero.taskflow.service;

import com.josequintero.taskflow.exception.BusinessException;
import com.josequintero.taskflow.model.Tarea;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public TareaTemporalService(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    public boolean estaVencida(Tarea tarea) {
        return tarea != null && tarea.estaVencida(ahora());
    }

    public LocalDateTime parseFechaLimite(String valor) {
        return parse(valor, false);
    }

    public LocalDateTime parseFiltroDesde(String valor) {
        return parse(valor, true);
    }

    public LocalDateTime parseFiltroHasta(String valor) {
        return parse(valor, false);
    }

    public String formatForEmail(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return "Sin fecha límite";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' HH:mm", EMAIL_LOCALE);
        return fechaHora.format(formatter);
    }

    private LocalDateTime parse(String valor, boolean inicioDelDia) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }

        String normalizado = valor.trim();

        try {
            return LocalDateTime.parse(normalizado, DATE_TIME_SECONDS);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(normalizado, DATE_TIME_MINUTES);
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDate date = LocalDate.parse(normalizado, DATE_ONLY);
            return inicioDelDia ? date.atStartOfDay() : date.atTime(LocalTime.of(23, 59));
        } catch (DateTimeParseException ex) {
            throw new BusinessException("La fecha límite debe tener formato ISO, por ejemplo 2026-04-02T18:30");
        }
    }
}
