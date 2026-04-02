package com.josequintero.taskflow.service;

import com.josequintero.taskflow.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TareaTemporalServiceTest {

    private final TareaTemporalService service = new TareaTemporalService(
            Clock.fixed(Instant.parse("2026-04-02T10:15:00Z"), ZoneId.of("Europe/Madrid"))
    );

    @Test
    void parseFechaLimiteAcceptsDateTimeWithMinutes() {
        LocalDateTime result = service.parseFechaLimite("2026-04-04T18:30");

        assertEquals(LocalDateTime.of(2026, 4, 4, 18, 30), result);
    }

    @Test
    void parseFechaLimiteMapsLegacyDateOnlyToEndOfDay() {
        LocalDateTime result = service.parseFechaLimite("2026-04-04");

        assertEquals(LocalDateTime.of(2026, 4, 4, 23, 59), result);
    }

    @Test
    void parseFiltroDesdeUsesStartOfDay() {
        LocalDateTime result = service.parseFiltroDesde("2026-04-04");

        assertEquals(LocalDateTime.of(2026, 4, 4, 0, 0), result);
    }

    @Test
    void parseFechaLimiteRejectsUnsupportedFormat() {
        assertThrows(BusinessException.class, () -> service.parseFechaLimite("04/04/2026 18:30"));
    }
}
