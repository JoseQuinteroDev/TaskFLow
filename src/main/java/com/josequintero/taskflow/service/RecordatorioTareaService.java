package com.josequintero.taskflow.service;

import com.josequintero.taskflow.config.ReminderProperties;
import com.josequintero.taskflow.model.RecordatorioTarea;
import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.enums.CanalNotificacion;
import com.josequintero.taskflow.model.enums.EstadoEnvioNotificacion;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.TipoRecordatorioTarea;
import com.josequintero.taskflow.repositories.RecordatorioTareaRepository;
import com.josequintero.taskflow.repositories.TareaRepository;
import com.josequintero.taskflow.service.email.EmailMessage;
import com.josequintero.taskflow.service.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RecordatorioTareaService {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioTareaService.class);

    private final TareaRepository tareaRepository;
    private final RecordatorioTareaRepository recordatorioTareaRepository;
    private final ReminderProperties reminderProperties;
    private final EmailSender emailSender;
    private final TareaTemporalService tareaTemporalService;

    public RecordatorioTareaService(
            TareaRepository tareaRepository,
            RecordatorioTareaRepository recordatorioTareaRepository,
            ReminderProperties reminderProperties,
            EmailSender emailSender,
            TareaTemporalService tareaTemporalService
    ) {
        this.tareaRepository = tareaRepository;
        this.recordatorioTareaRepository = recordatorioTareaRepository;
        this.reminderProperties = reminderProperties;
        this.emailSender = emailSender;
        this.tareaTemporalService = tareaTemporalService;
    }

    @Transactional
    public void procesarRecordatoriosPendientes() {
        if (!reminderProperties.isEnabled()) {
            return;
        }

        Instant ahora = tareaTemporalService.ahora();
        Instant ventanaInicio = ahora.minus(reminderProperties.getOverdueWindow());
        Instant ventanaFin = ahora.plus(reminderProperties.getSoonThreshold());

        List<Tarea> tareas = tareaRepository.findByEstadoNotAndRecordatorioActivoTrueAndFechaLimiteBetweenOrderByFechaLimiteAsc(
                EstadoTarea.COMPLETADA,
                ventanaInicio,
                ventanaFin
        );

        for (Tarea tarea : tareas) {
            if (tarea.getFechaLimite() == null
                    || !Boolean.TRUE.equals(tarea.getUsuario().getActivo())
                    || !tarea.tieneRecordatorioActivo()) {
                continue;
            }

            if (!tarea.getFechaLimite().isAfter(ahora)) {
                if (!tarea.getFechaLimite().isBefore(ahora.minus(reminderProperties.getOverdueWindow()))) {
                    enviarSiCorresponde(tarea, TipoRecordatorioTarea.VENCIDA, ahora);
                }
                continue;
            }

            Instant fechaDisparo = tarea.getFechaLimite()
                    .minus(tarea.getRecordatorioMinutosAntes(), ChronoUnit.MINUTES);

            if (!ahora.isBefore(fechaDisparo)) {
                enviarSiCorresponde(tarea, TipoRecordatorioTarea.PROXIMO_VENCIMIENTO, ahora);
            }
        }
    }

    private void enviarSiCorresponde(Tarea tarea, TipoRecordatorioTarea tipo, Instant ahora) {
        RecordatorioTarea recordatorio = recordatorioTareaRepository
                .findByTareaIdAndTipoAndCanal(tarea.getId(), tipo, CanalNotificacion.EMAIL)
                .orElseGet(() -> RecordatorioTarea.builder()
                        .tarea(tarea)
                        .tipo(tipo)
                        .canal(CanalNotificacion.EMAIL)
                        .destinatario(tarea.getUsuario().getEmail())
                        .fechaProgramada(tareaTemporalService.toUtcLocalDateTime(ahora))
                        .build());

        if (recordatorio.getEstado() == EstadoEnvioNotificacion.ENVIADO) {
            return;
        }

        recordatorio.setFechaProgramada(tareaTemporalService.toUtcLocalDateTime(ahora));
        recordatorio.setDestinatario(tarea.getUsuario().getEmail());

        try {
            emailSender.send(buildMessage(tarea, tipo));
            recordatorio.setEstado(EstadoEnvioNotificacion.ENVIADO);
            recordatorio.setFechaEnvio(tareaTemporalService.toUtcLocalDateTime(ahora));
            recordatorio.setError(null);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el recordatorio {} de la tarea {}: {}", tipo, tarea.getId(), ex.getMessage());
            recordatorio.setEstado(EstadoEnvioNotificacion.FALLIDO);
            recordatorio.setError(truncate(ex.getMessage()));
        }

        recordatorioTareaRepository.save(recordatorio);
    }

    private EmailMessage buildMessage(Tarea tarea, TipoRecordatorioTarea tipo) {
        String fechaLimite = tareaTemporalService.formatForEmail(tarea.getFechaLimite(), tarea.getUsuario().getTimezone());
        String subject = switch (tipo) {
            case PROXIMO_VENCIMIENTO -> "TaskFlow | Tu tarea vence pronto";
            case VENCIDA -> "TaskFlow | Tienes una tarea vencida";
        };

        String intro = switch (tipo) {
            case PROXIMO_VENCIMIENTO -> "Te avisamos de que esta tarea esta cerca de su vencimiento.";
            case VENCIDA -> "Esta tarea ya ha superado su fecha limite y necesita atencion.";
        };

        String textBody = """
                %s

                Tarea: %s
                Prioridad: %s
                Fecha limite: %s
                Recordatorio: %s minutos antes

                Revisa TaskFlow para actualizar su estado o ajustar la planificacion.
                """.formatted(
                intro,
                tarea.getTitulo(),
                tarea.getPrioridad().name(),
                fechaLimite,
                tarea.getRecordatorioMinutosAntes()
        );

        String htmlBody = """
                <html>
                  <body style="font-family:Arial,sans-serif;background:#0b0d12;color:#e8edf7;padding:24px;">
                    <div style="max-width:560px;margin:0 auto;background:#12161f;border:1px solid #232936;border-radius:20px;padding:28px;">
                      <p style="margin:0 0 10px;color:#92a0b8;font-size:12px;letter-spacing:0.16em;text-transform:uppercase;">TaskFlow Reminder</p>
                      <h1 style="margin:0 0 14px;font-size:24px;color:#f4f7fb;">%s</h1>
                      <p style="margin:0 0 22px;color:#cbd5e1;line-height:1.6;">%s</p>
                      <div style="padding:18px;border-radius:16px;background:#0f131b;border:1px solid #232936;">
                        <p style="margin:0 0 8px;color:#92a0b8;font-size:12px;text-transform:uppercase;">Tarea</p>
                        <p style="margin:0 0 16px;font-size:18px;color:#f4f7fb;font-weight:700;">%s</p>
                        <p style="margin:0 0 8px;color:#92a0b8;font-size:12px;text-transform:uppercase;">Prioridad</p>
                        <p style="margin:0 0 16px;color:#f4f7fb;">%s</p>
                        <p style="margin:0 0 8px;color:#92a0b8;font-size:12px;text-transform:uppercase;">Fecha limite</p>
                        <p style="margin:0 0 16px;color:#87f3b0;font-weight:700;">%s</p>
                        <p style="margin:0 0 8px;color:#92a0b8;font-size:12px;text-transform:uppercase;">Recordatorio</p>
                        <p style="margin:0;color:#f4f7fb;">%s minutos antes</p>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(
                subject,
                intro,
                tarea.getTitulo(),
                tarea.getPrioridad().name(),
                fechaLimite,
                tarea.getRecordatorioMinutosAntes()
        );

        return new EmailMessage(tarea.getUsuario().getEmail(), subject, textBody, htmlBody);
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 500) {
            return message;
        }

        return message.substring(0, 500);
    }
}
