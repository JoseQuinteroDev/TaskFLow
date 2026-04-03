package com.josequintero.taskflow.mapper;

import com.josequintero.taskflow.dto.admin.AdminReminderFailureResponseDto;
import com.josequintero.taskflow.model.RecordatorioTarea;
import org.springframework.stereotype.Component;

@Component
public class AdminReminderFailureMapper {

    public AdminReminderFailureResponseDto toResponseDto(RecordatorioTarea recordatorio) {
        if (recordatorio == null) {
            return null;
        }

        return AdminReminderFailureResponseDto.builder()
                .id(recordatorio.getId())
                .tareaId(recordatorio.getTarea().getId())
                .tareaTitulo(recordatorio.getTarea().getTitulo())
                .destinatario(recordatorio.getDestinatario())
                .usuarioEmail(recordatorio.getTarea().getUsuario().getEmail())
                .tipo(recordatorio.getTipo())
                .canal(recordatorio.getCanal())
                .error(recordatorio.getError())
                .fechaProgramada(recordatorio.getFechaProgramada())
                .fechaCreacion(recordatorio.getFechaCreacion())
                .build();
    }
}
