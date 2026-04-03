package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.dto.categoria.CategoriaResumenDto;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
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
    private Instant fechaInicio;
    private Instant fechaLimite;
    private boolean vencida;
    private boolean completada;
    private boolean recordatorioActivo;
    private Integer recordatorioMinutosAntes;
    private CategoriaResumenDto categoria;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
