package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.model.enums.PrioridadTarea;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

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

    @FutureOrPresent(message = "La fecha límite debe ser hoy o una fecha futura")
    private LocalDate fechaLimite;

    private Long categoriaId;
}