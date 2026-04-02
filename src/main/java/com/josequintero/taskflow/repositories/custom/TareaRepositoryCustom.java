package com.josequintero.taskflow.repositories.custom;

import com.josequintero.taskflow.model.Tarea;
import com.josequintero.taskflow.model.enums.EstadoTarea;
import com.josequintero.taskflow.model.enums.PrioridadTarea;

import java.time.LocalDateTime;
import java.util.List;

public interface TareaRepositoryCustom {

    List<Tarea> buscarConFiltros(
            Long usuarioId,
            String texto,
            EstadoTarea estado,
            PrioridadTarea prioridad,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Long categoriaId
    );
}
