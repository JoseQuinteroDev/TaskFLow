package com.josequintero.taskflow.service;

import com.josequintero.taskflow.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TareaTemporalServiceTest {

    private final TareaTemporalService service = new TareaTemporalService(
            Clock.fixed(Instant.parse("2026-04-02T10:15:00Z"), ZoneId.of("Europe/Madrid")),
            "Europe/Madrid"
    );

    @Test
    void parseFechaLimiteAcceptsDateTimeWithMinutes() {
        Instant result = service.parseFechaLimite("2026-04-04T18:30", "Europe/Madrid");

        assertEquals(Instant.parse("2026-04-04T16:30:00Z"), result);
    }

    @Test
    void parseFechaLimiteMapsLegacyDateOnlyToEndOfDay() {
        Instant result = service.parseFechaLimite("2026-04-04", "Europe/Madrid");

        assertEquals(Instant.parse("2026-04-04T21:59:00Z"), result);
    }

    @Test
    void parseFiltroDesdeUsesStartOfDay() {
        Instant result = service.parseFiltroDesde("2026-04-04", "Europe/Madrid");

        assertEquals(Instant.parse("2026-04-03T22:00:00Z"), result);
    }

    @Test
    void parseFechaLimiteRejectsUnsupportedFormat() {
        assertThrows(BusinessException.class, () -> service.parseFechaLimite("04/04/2026 18:30", "Europe/Madrid"));
    }
}
