package com.josequintero.taskflow.service;

import com.josequintero.taskflow.config.ReminderProperties;
import com.josequintero.taskflow.model.RecordatorioTarea;
import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.Usuario;
import com.josequintero.taskflow.model.enums.CanalNotificacion;
import com.josequintero.taskflow.model.enums.EstadoEnvioNotificacion;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import com.josequintero.taskflow.model.enums.TipoRecordatorioTarea;
import com.josequintero.taskflow.repositories.RecordatorioTareaRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import com.josequintero.taskflow.service.email.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordatorioTareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private RecordatorioTareaRepository recordatorioTareaRepository;

    @Mock
    private EmailSender emailSender;

    private RecordatorioTareaService service;

    @BeforeEach
    void setUp() {
        ReminderProperties reminderProperties = new ReminderProperties();
        reminderProperties.setEnabled(true);
        reminderProperties.setSoonThreshold(Duration.ofHours(24));
        reminderProperties.setOverdueWindow(Duration.ofMinutes(20));

        TareaTemporalService tareaTemporalService = new TareaTemporalService(
                Clock.fixed(Instant.parse("2026-04-02T10:00:00Z"), ZoneId.of("Europe/Madrid")),
                "Europe/Madrid"
        );

        service = new RecordatorioTareaService(
                tareaRepository,
                recordatorioTareaRepository,
                reminderProperties,
                emailSender,
                tareaTemporalService
        );
    }

    @Test
    void sendsDueSoonReminderAndPersistsIt() {
        Tarea tarea = buildTask(Instant.parse("2026-04-02T10:10:00Z"), Instant.parse("2026-04-02T18:00:00Z"), 15);

        when(tareaRepository.findByEstadoNotAndRecordatorioActivoTrueAndFechaInicioBetweenOrderByFechaInicioAsc(
                eq(EstadoTarea.COMPLETADA),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(List.of(tarea), List.of());
        when(recordatorioTareaRepository.findByTareaIdAndTipoAndCanal(
                tarea.getId(),
                TipoRecordatorioTarea.ANTES_DE_INICIO,
                CanalNotificacion.EMAIL
        )).thenReturn(Optional.empty());

        service.procesarRecordatoriosPendientes();

        ArgumentCaptor<RecordatorioTarea> captor = ArgumentCaptor.forClass(RecordatorioTarea.class);
        verify(recordatorioTareaRepository).save(captor.capture());
        verify(emailSender).send(any());

        RecordatorioTarea savedReminder = captor.getValue();
        assertEquals(EstadoEnvioNotificacion.ENVIADO, savedReminder.getEstado());
        assertEquals(TipoRecordatorioTarea.ANTES_DE_INICIO, savedReminder.getTipo());
        assertEquals("user@taskflow.dev", savedReminder.getDestinatario());
        assertEquals(LocalDateTime.of(2026, 4, 2, 9, 55), savedReminder.getFechaProgramada());
        assertTrue(savedReminder.getFechaEnvio() != null);
    }

    @Test
    void doesNotResendReminderAlreadyMarkedAsSent() {
        Tarea tarea = buildTask(Instant.parse("2026-04-02T10:10:00Z"), Instant.parse("2026-04-02T18:00:00Z"), 15);
        RecordatorioTarea existingReminder = RecordatorioTarea.builder()
                .id(20L)
                .tarea(tarea)
                .tipo(TipoRecordatorioTarea.ANTES_DE_INICIO)
                .canal(CanalNotificacion.EMAIL)
                .estado(EstadoEnvioNotificacion.ENVIADO)
                .destinatario("user@taskflow.dev")
                .fechaProgramada(LocalDateTime.of(2026, 4, 2, 12, 0))
                .build();

        when(tareaRepository.findByEstadoNotAndRecordatorioActivoTrueAndFechaInicioBetweenOrderByFechaInicioAsc(
                eq(EstadoTarea.COMPLETADA),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(List.of(tarea), List.of());
        when(recordatorioTareaRepository.findByTareaIdAndTipoAndCanal(
                tarea.getId(),
                TipoRecordatorioTarea.ANTES_DE_INICIO,
                CanalNotificacion.EMAIL
        )).thenReturn(Optional.of(existingReminder));

        service.procesarRecordatoriosPendientes();

        verify(emailSender, never()).send(any());
        verify(recordatorioTareaRepository, never()).save(any());
    }

    @Test
    void searchesUsingTheMaximumReminderLeadTimeWindow() {
        when(tareaRepository.findByEstadoNotAndRecordatorioActivoTrueAndFechaInicioBetweenOrderByFechaInicioAsc(
                eq(EstadoTarea.COMPLETADA),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(List.of());

        service.procesarRecordatoriosPendientes();

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);

        verify(tareaRepository).findByEstadoNotAndRecordatorioActivoTrueAndFechaInicioBetweenOrderByFechaInicioAsc(
                eq(EstadoTarea.COMPLETADA),
                fromCaptor.capture(),
                toCaptor.capture()
        );

        assertEquals(Instant.parse("2026-04-02T09:40:00Z"), fromCaptor.getValue());
        assertEquals(Instant.parse("2026-04-09T10:00:00Z"), toCaptor.getValue());
    }

    @Test
    void doesNotSendReminderWhenTaskStartedTooLongAgo() {
        Tarea tarea = buildTask(Instant.parse("2026-04-02T09:30:00Z"), Instant.parse("2026-04-02T18:00:00Z"), 15);

        when(tareaRepository.findByEstadoNotAndRecordatorioActivoTrueAndFechaInicioBetweenOrderByFechaInicioAsc(
                eq(EstadoTarea.COMPLETADA),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(List.of(tarea));

        service.procesarRecordatoriosPendientes();

        verify(emailSender, never()).send(any());
        verify(recordatorioTareaRepository, never()).save(any());
    }

    private Tarea buildTask(Instant fechaInicio, Instant fechaLimite, int recordatorioMinutosAntes) {
        Usuario usuario = Usuario.builder()
                .id(7L)
                .nombre("Jose")
                .email("user@taskflow.dev")
                .timezone("Europe/Madrid")
                .activo(true)
                .build();

        return Tarea.builder()
                .id(12L)
                .titulo("Preparar demo final")
                .prioridad(PrioridadTarea.ALTA)
                .estado(EstadoTarea.PENDIENTE)
                .fechaInicio(fechaInicio)
                .fechaLimite(fechaLimite)
                .recordatorioActivo(true)
                .recordatorioMinutosAntes(recordatorioMinutosAntes)
                .usuario(usuario)
                .build();
    }
}
