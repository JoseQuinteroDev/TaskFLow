package com.josequintero.taskflow.service;

import java.time.Duration;

public final class RecordatorioTareaRules {

    public static final int MIN_MINUTOS_ANTES = 5;
    public static final int MAX_MINUTOS_ANTES = 10080;

    private RecordatorioTareaRules() {
    }

    public static Duration maxLeadTime() {
        return Duration.ofMinutes(MAX_MINUTOS_ANTES);
    }
}
