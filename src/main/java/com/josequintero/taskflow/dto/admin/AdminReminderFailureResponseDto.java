package com.josequintero.taskflow.dto.admin;

import com.josequintero.taskflow.model.enums.CanalNotificacion;
import com.josequintero.taskflow.model.enums.TipoRecordatorioTarea;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReminderFailureResponseDto {

    private Long id;
    private Long tareaId;
    private String tareaTitulo;
    private String destinatario;
    private String usuarioEmail;
    private TipoRecordatorioTarea tipo;
    private CanalNotificacion canal;
    private String error;
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaCreacion;
}
