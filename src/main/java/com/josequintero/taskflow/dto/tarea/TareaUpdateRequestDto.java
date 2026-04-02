package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
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
public class TareaUpdateRequestDto {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 3, max = 100, message = "El titulo debe tener entre 3 y 100 caracteres")
    private String titulo;

    @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadTarea prioridad;

    @NotNull(message = "El estado es obligatorio")
    private EstadoTarea estado;

    @Size(max = 50, message = "La fecha limite debe enviarse en formato ISO")
    private String fechaLimite;

    private Long categoriaId;

    private Boolean recordatorioActivo;

    @Min(value = 5, message = "El recordatorio minimo es de 5 minutos")
    @Max(value = 10080, message = "El recordatorio maximo es de 10080 minutos")
    private Integer recordatorioMinutosAntes;
}
