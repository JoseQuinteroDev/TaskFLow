package com.josequintero.taskflow.dto.tarea;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaResumenDto {

    private long total;
    private long pendientes;
    private long enProceso;
    private long completadas;
    private long vencidas;
}