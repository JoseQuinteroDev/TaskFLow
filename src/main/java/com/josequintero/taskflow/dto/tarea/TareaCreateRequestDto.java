package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.model.enums.PrioridadTarea;
import com.josequintero.taskflow.service.RecordatorioTareaRules;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaCreateRequestDto {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 100, message = "El título debe tener entre 3 y 100 caracteres")
    private String titulo;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadTarea prioridad;

    @NotBlank(message = "La fecha de inicio es obligatoria")
    @Size(max = 50, message = "La fecha de inicio debe enviarse en formato ISO")
    private String fechaInicio;

    @Size(max = 50, message = "La fecha límite debe enviarse en formato ISO")
    private String fechaLimite;

    private Long categoriaId;

    private Boolean recordatorioActivo;

    @Min(value = RecordatorioTareaRules.MIN_MINUTOS_ANTES, message = "El recordatorio mínimo es de 5 minutos")
    @Max(value = RecordatorioTareaRules.MAX_MINUTOS_ANTES, message = "El recordatorio máximo es de 10080 minutos")
    private Integer recordatorioMinutosAntes;
}
