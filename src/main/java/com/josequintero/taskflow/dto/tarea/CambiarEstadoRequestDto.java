package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.model.enums.EstadoTarea;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoRequestDto {

    @NotNull(message = "El estado es obligatorio")
    private EstadoTarea estado;
}