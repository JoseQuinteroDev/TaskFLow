package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.dto.categoria.CategoriaResumenDto;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaResponseDto {

    private Long id;
    private String titulo;
    private String descripcion;
    private PrioridadTarea prioridad;
    private EstadoTarea estado;
    private LocalDate fechaLimite;
    private boolean vencida;
    private CategoriaResumenDto categoria;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}