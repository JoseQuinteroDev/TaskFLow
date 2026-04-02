package com.josequintero.taskflow.dto.tarea;

import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;
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
public class TareaFiltroRequestDto {

    private String texto;
    private EstadoTarea estado;
    private PrioridadTarea prioridad;
    private String desde;
    private String hasta;
    private Long categoriaId;
}
